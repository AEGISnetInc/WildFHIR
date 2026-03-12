/*
 * #%L
 * WildFHIR - wildfhir-service
 * %%
 * Copyright (C) 2025 AEGIS.net, Inc.
 * All rights reserved.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  - Neither the name of AEGIS nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without specific
 *    prior written permission.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package net.aegis.fhir.service.subscription.r5.topic;

import java.util.logging.Logger;

import jakarta.ws.rs.core.MultivaluedMap;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.formats.IParser.OutputStyle;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntryRequestComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntryResponseComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Subscription;
import org.hl7.fhir.r5.model.Enumerations.SubscriptionStatusCodes;
import org.hl7.fhir.r5.model.Reference;
import org.hl7.fhir.r5.model.SubscriptionStatus;
import org.hl7.fhir.r5.model.SubscriptionStatus.SubscriptionNotificationType;
import org.hl7.fhir.r5.model.SubscriptionStatus.SubscriptionStatusNotificationEventComponent;

import net.aegis.fhir.model.ResourceContainer;
import net.aegis.fhir.service.CodeService;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.ResourcemetadataService;
import net.aegis.fhir.service.audit.AuditEventService;
import net.aegis.fhir.service.provenance.ProvenanceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;
import net.aegis.fhir.service.util.UUIDUtil;

/**
 * WildFHIR CE Subscription Topic for default processing based on the
 * FHIR R5 Subscription Backport to R4 Implementation Guide
 * 
 * @author richard.ettema
 *
 */
public class WildfhirCESubscriptionTopic extends SubscriptionTopicProxy {

	private Logger log = Logger.getLogger("WildfhirCESubscriptionTopic");

	private UTCDateUtil utcDateUtil = new UTCDateUtil();

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.subscription.r5.topic.SubscriptionTopicProxy#processTopic(net.aegis.fhir.service.ResourceService, net.aegis.fhir.service.ResourcemetadataService, net.aegis.fhir.service.CodeService, net.aegis.fhir.service.audit.AuditEventService, net.aegis.fhir.service.provenance.ProvenanceService, org.hl7.fhir.r4.model.Subscription, java.util.Date, java.lang.StringBuffer)
	 */
	@Override
	public Bundle processTopic(ResourceService resourceService,
			ResourcemetadataService resourcemetadataService, CodeService codeService,
			AuditEventService auditEventService, ProvenanceService provenanceService,
			Subscription subscription, Date since, StringBuffer returnedDetails)
			throws Exception {

		log.info("[START] WildfhirCESubscriptionTopic.processTopic()");

		Bundle subscriptionBundle = null;
		BundleEntryComponent subscriptionEntry = null;
		SubscriptionStatus subscriptionStatus = null;
		SubscriptionStatus existingStatus = null;
		Parameters pSubscriptionStatus = null;
		String payloadContent = "full-resource"; // Default

		try {
			// Get subscription.channel backport-payload-content code value
			if (subscription.hasChannel() && subscription.getChannel().hasPayload() &&
					subscription.getChannel().getPayloadElement().hasExtension()) {

				for (Extension payloadExt : subscription.getChannel().getPayloadElement().getExtension()) {
					if (payloadExt.hasUrl() && payloadExt.getUrl().equals("http://hl7.org/fhir/uv/subscriptions-backport/StructureDefinition/backport-payload-content")) {
						payloadContent = ((CodeType) payloadExt.getValue()).getCode();
						break;
					}
				}
			}

			/*
			 *  Get current SubscriptionStatus; if not found, create
			 */

			// Convert search parameter string into queryParams map
			String paramsString = "subscription=Subscription/" + subscription.getId() + "&type=event-notification&_sort=-_lastUpdated&_count=1";
			List<NameValuePair> params = URLEncodedUtils.parse(paramsString, Charset.defaultCharset());
			MultivaluedMap<String, String> queryParams = ServicesUtil.INSTANCE.listNameValuePairToMultivaluedMapString(params);

			// Search for all SubscriptionStatus with subscription = current Subscription; return as searchset Bundle
			ResourceContainer rc = resourceService.search(queryParams, null, null, null, "SubscriptionStatus", "INTERNAL", null, null, null, false);

			// Check for matched SubscriptionStatus resources
			if (rc != null && rc.getBundle() != null && !rc.getBundle().getEntry().isEmpty()) {

				// Should only be one SubscriptionStatus so take the first entry
				existingStatus = (SubscriptionStatus) ServicesUtil.INSTANCE.convertR4ParametersToR5SubscriptionStatus(rc.getBundle().getEntryFirstRep().getResource());
			}

			/*
			 *  Build Consent search parameters
			 *  - _lastUpdated=ge since date
			 *  - subscription.criteria backport-filter-criteria extension(s)
			 */
			StringBuilder sbParams = new StringBuilder("_lastUpdated=ge")
					.append(utcDateUtil.formatDate(since, UTCDateUtil.DATE_PARAMETER_FORMAT));

			if (subscription.hasCriteria() && subscription.getCriteriaElement().hasExtension()) {

				for (Extension criteriaExt : subscription.getCriteriaElement().getExtension()) {
					if (criteriaExt.hasUrl() && criteriaExt.getUrl().equals("http://hl7.org/fhir/uv/subscriptions-backport/StructureDefinition/backport-filter-criteria")) {
						sbParams.append("&").append(((StringType) criteriaExt.getValue()).getValueAsString());
					}
				}
			}

			params = URLEncodedUtils.parse(sbParams.toString(), Charset.defaultCharset());
			queryParams = ServicesUtil.INSTANCE.listNameValuePairToMultivaluedMapString(params);

			// Search for all Consent matching criteria; return as searchset Bundle
			rc = resourceService.search(queryParams, null, null, null, "Consent", "INTERNAL", null, null, null, false);

			// Check for matched Subscription resources
			if (rc != null && rc.getBundle() != null && !rc.getBundle().getEntry().isEmpty()) {

				// Subscription Notification Bundle
				subscriptionBundle = new Bundle();
				subscriptionBundle.setId(UUIDUtil.getUUID());
				Meta meta = new Meta();
				meta.addProfile("http://hl7.org/fhir/uv/subscriptions-backport/StructureDefinition/backport-subscription-notification-r4");
				subscriptionBundle.setMeta(meta);
				subscriptionBundle.setType(BundleType.HISTORY);
				subscriptionBundle.setTimestamp(new Date());

				String baseUrl = codeService.findCodeValueByName("baseUrl");

				// SubscriptionStatus for Subscription
				subscriptionStatus = newSubscriptionStatus(subscription, existingStatus, rc.getBundle(), baseUrl, payloadContent);

				pSubscriptionStatus = (Parameters) ServicesUtil.INSTANCE.convertR5SubscriptionStatusToR4Parameters(subscriptionStatus);

				// Convert the Resource to XML byte[]
				ByteArrayOutputStream oResource = new ByteArrayOutputStream();
				XmlParser xmlParser = new XmlParser();
				xmlParser.setOutputStyle(OutputStyle.PRETTY);
				xmlParser.compose(oResource, pSubscriptionStatus, true);
				byte[] bResource = oResource.toByteArray();

				// Initialize a Resource to be created
				net.aegis.fhir.model.Resource aegisResource = new net.aegis.fhir.model.Resource();
				aegisResource.setResourceType("SubscriptionStatus");
				aegisResource.setResourceContents(bResource);

				// Create new SubscriptionStatus
				ResourceContainer rcStatus = resourceService.create(aegisResource, null, baseUrl);

				aegisResource = rcStatus.getResource();

				// Add R4 Parameters (SubscriptionStatus) to subscription notification bundle
				subscriptionEntry = new BundleEntryComponent();
				subscriptionEntry.setFullUrl(baseUrl + "/Parameters/" + aegisResource.getResourceId());
				pSubscriptionStatus.setId(aegisResource.getResourceId());
				subscriptionEntry.setResource(pSubscriptionStatus);
				// Set request and response
				BundleEntryRequestComponent entryRequest = new BundleEntryRequestComponent();
				entryRequest.setMethod(HTTPVerb.GET);
				entryRequest.setUrl(baseUrl + "/Subscription/" + subscription.getId() + "/$status");
				subscriptionEntry.setRequest(entryRequest);
				BundleEntryResponseComponent entryResponse = new BundleEntryResponseComponent();
				entryResponse.setStatus("200");
				subscriptionEntry.setResponse(entryResponse);
				subscriptionBundle.addEntry(subscriptionEntry);

				// Iterate over all matched Consent; add to subscription notification bundle if payloadContent equal "full-resource"
				if (payloadContent.equals("full-resource")) {
					String responseCode = null;

					for (BundleEntryComponent consentEntry : rc.getBundle().getEntry()) {
						responseCode = "200";
						subscriptionEntry = new BundleEntryComponent();
						subscriptionEntry.setFullUrl(baseUrl + "/Consent/" + consentEntry.getResource().getId());
						subscriptionEntry.setResource(consentEntry.getResource());
						// Set request and response
						entryRequest = new BundleEntryRequestComponent();
						if (consentEntry.getResource().hasMeta() && consentEntry.getResource().getMeta().hasVersionId()) {
							if (consentEntry.getResource().getMeta().getVersionId().equals("1")) {
								responseCode = "201";
							}
						}
						if (responseCode.equals("201")) {
							entryRequest.setMethod(HTTPVerb.POST);
							entryRequest.setUrl("Consent");
						}
						else {
							entryRequest.setMethod(HTTPVerb.PUT);
							entryRequest.setUrl("Consent/" + consentEntry.getResource().getId());
						}
						subscriptionEntry.setRequest(entryRequest);
						entryResponse = new BundleEntryResponseComponent();
						entryResponse.setStatus(responseCode);
						subscriptionEntry.setResponse(entryResponse);
						subscriptionBundle.addEntry(subscriptionEntry);
					}
				}

			}
			else {
				// If zero matches, returnDetails = "0 matches"
				if (returnedDetails == null) {
					returnedDetails = new StringBuffer();
				}
				returnedDetails.append("No matched Consent resources found for Subscription criteria.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

		return subscriptionBundle;
	}

	/**
	 * @param subscription
	 * @param existingStatus
	 * @param consentSearch
	 * @param baseUrl
	 * @param payloadContent
	 * @return
	 * @throws Exception
	 */
	private SubscriptionStatus newSubscriptionStatus(Subscription subscription, SubscriptionStatus existingStatus, Bundle consentSearch, String baseUrl, String payloadContent) throws Exception {

		SubscriptionStatus subscriptionStatus = new SubscriptionStatus();

		long eventNumber = 1;
		if (existingStatus != null) {
			eventNumber = existingStatus.getEventsSinceSubscriptionStart() + 1;
		}

		org.hl7.fhir.r5.model.Reference reference = new org.hl7.fhir.r5.model.Reference();
		reference.setReference("Subscription/" + subscription.getId());
		subscriptionStatus.setSubscription(reference);

		subscriptionStatus.setTopic(subscription.getCriteria());

		subscriptionStatus.setStatus(SubscriptionStatusCodes.ACTIVE);

		subscriptionStatus.setType(SubscriptionNotificationType.EVENTNOTIFICATION);

		subscriptionStatus.setEventsSinceSubscriptionStart(eventNumber);

		SubscriptionStatusNotificationEventComponent ssne = new SubscriptionStatusNotificationEventComponent();

		ssne.setEventNumber(eventNumber);
		ssne.setTimestamp(new Date());

		if (!payloadContent.equals("empty")) {
			// Iterate over Consent search Bundle; add Consent reference(s)
			int iEntry = 0;
			Reference consentReference = null;
			for (BundleEntryComponent consentEntry : consentSearch.getEntry()) {
				consentReference = new Reference();
				consentReference.setReference(baseUrl + "/Consent/" + consentEntry.getResource().getId());

				if (iEntry == 0) {
					ssne.setFocus(consentReference);
				}
				else {
					ssne.addAdditionalContext(consentReference);
				}

				iEntry++;
			}
		}

		subscriptionStatus.addNotificationEvent(ssne);

		return subscriptionStatus;
	}

}
