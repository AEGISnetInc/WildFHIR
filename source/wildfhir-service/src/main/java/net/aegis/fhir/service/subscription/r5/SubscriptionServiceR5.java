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
package net.aegis.fhir.service.subscription.r5;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.hl7.fhir.r4.formats.IParser.OutputStyle;
import org.hl7.fhir.r4.formats.JsonParser;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Subscription;

import net.aegis.fhir.model.LabelKeyValueBean;
import net.aegis.fhir.model.ResourceContainer;
import net.aegis.fhir.service.CodeService;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.ResourcemetadataService;
import net.aegis.fhir.service.audit.AuditEventService;
import net.aegis.fhir.service.client.ResourceRESTClient;
import net.aegis.fhir.service.provenance.ProvenanceService;
import net.aegis.fhir.service.subscription.r5.topic.SubscriptionTopicProxy;
import net.aegis.fhir.service.subscription.r5.topic.SubscriptionTopicProxyObjectFactory;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

/**
 * Subscription Service for performing the set of functions supporting the
 * FHIR R5 Backport to R4 Subscriptions Framework with Topics.
 *
 * Supported channel types: rest-hook
 *
 * The @Stateless annotation eliminates the need for manual transaction demarcation
 *
 * @author richard.ettema
 *
 */
@Stateless
public class SubscriptionServiceR5 {

	private Logger log = Logger.getLogger("SubscriptionServiceR5");

	@Inject
	AuditEventService auditEventService;

    @Inject
    CodeService codeService;

	@Inject
    ProvenanceService provenanceService;

    @Inject
	private ResourceService resourceService;

	@Inject
	ResourcemetadataService resourcemetadataService;

	@Inject
	private UTCDateUtil utcDateUtil;

	private ResourceRESTClient resourceClient;

	/*
	 * Public Methods
	 */

	public SubscriptionServiceR5() throws Exception {
		this.resourceClient = new ResourceRESTClient(codeService);
	}

	/**
	 * Process all active Subscription resources based on since datetime
	 *
	 * @param since
	 * @return List<LabelKeyValueBean> - Results
	 * @throws Exception
	 */
	public List<LabelKeyValueBean> processSubscriptions(Date since) throws Exception {

		log.fine("[START] SubscriptionServiceR5.processSubscriptions()");

		// Verify since date is not null
		if (since == null) {
			throw new Exception("processSubscriptions requires a valid, non-null date defining the date since the last processing execution!");
		}

		List<LabelKeyValueBean> results = new ArrayList<LabelKeyValueBean>();
		LabelKeyValueBean result = null;

		/*
		 * Search for all Subscriptions with status = active
		 * For each Subscription
		 *   Perform search using criteria adding _lastUpdated=gt:since parameter
		 *   For each matched resource instance
		 *     Send FHIR update to Subscription endpoint with matched resource instance as the payload
		 * Save new since date
		 */
		try {
			// Construct _lastUpdated parameters string
			StringBuilder sbSinceParams = new StringBuilder("_lastUpdated=ge")
					.append(utcDateUtil.formatDate(since, UTCDateUtil.DATETIME_ONLY_PARAMETER_FORMAT));

			log.fine("sbSinceParams [" + sbSinceParams.toString() + "]");

			// Convert search parameter string into queryParams map
			List<NameValuePair> params = URLEncodedUtils.parse("status=active", Charset.defaultCharset());
			MultivaluedMap<String, String> queryParams = ServicesUtil.INSTANCE.listNameValuePairToMultivaluedMapString(params);

			// Search for all Subscriptions with status = active; return as searchset Bundle
			ResourceContainer rcSubscriptions = resourceService.search(queryParams, null, null, null, "Subscription", "INTERNAL", null, null, null, false);

			// Check for matched Subscription resources
			if (rcSubscriptions != null && rcSubscriptions.getBundle() != null && !rcSubscriptions.getBundle().getEntry().isEmpty()) {

				ByteArrayOutputStream oResource = null;
				XmlParser xmlParser = new XmlParser();
				xmlParser.setOutputStyle(OutputStyle.PRETTY);
				JsonParser jsonParse = new JsonParser();
				jsonParse.setOutputStyle(OutputStyle.PRETTY);
				String payload = null;

				// For each Subscription entry
				Subscription subscription = null;

				for (BundleEntryComponent subscriptionEntry : rcSubscriptions.getBundle().getEntry()) {

					subscription = (Subscription)subscriptionEntry.getResource();

					log.fine("Processing Subscription [" + subscription.getId() + "] with criteria [" + subscription.getCriteria() + "] for channel type [" + subscription.getChannel().getType().name() + "]");

					// Initialize result bean
					result = new LabelKeyValueBean(subscription.getId(), subscription.getChannel().getType().name() + "; " + subscription.getChannel().getEndpoint(),
							subscription.getCriteria(), "", "processing", "");

					/*
					 * Use Factory Pattern for execution of SubscriptionTopic operation
					 */
					SubscriptionTopicProxyObjectFactory topicFactory = new SubscriptionTopicProxyObjectFactory();
					SubscriptionTopicProxy topicProxy = topicFactory.getSubscriptionTopicProxy(subscription.getCriteria());

					// Check for non-null topic proxy
					if (topicProxy != null) {
						StringBuffer returnedDetails = new StringBuffer();

						Bundle subscriptionBundle = topicProxy.processTopic(resourceService, resourcemetadataService, codeService, auditEventService, provenanceService, subscription, since, returnedDetails);

						// Select processing based on channel.type
						switch (subscription.getChannel().getType()) {
						case EMAIL:
							result.setPath("Email channel type not supported");
							log.fine("Email channel type not currently supported.");
							break;
						case MESSAGE:
							result.setPath("FHIR messaging channel not supported");
							log.fine("FHIR messaging channel type not currently supported.");
							break;
						case NULL:
							result.setPath("NULL channel type not supported");
							log.fine("NULL channel type not currently supported.");
							break;
						case RESTHOOK:
							/*
							 * If returnedDetails not empty topic processing either did not find any updated resources or failed, record outcome in result
							 *
							 * Else, post subscriptionBundle to subscription end point
							 */
							boolean okToPost = false;
							Response response = null;
							if (returnedDetails != null && returnedDetails.length() > 0) {
								result.setType(returnedDetails.toString());
								log.fine("REST Hook " + returnedDetails.toString());
							}
							else {
								// Parse subscriptionBundle to XML or JSON String based on the Subscription payload
								oResource = new ByteArrayOutputStream();
								if (subscription.getChannel().getPayload().contains("xml")) {
									xmlParser.compose(oResource, subscriptionBundle, true);
									payload = oResource.toString();
									okToPost = true;
								}
								else if (subscription.getChannel().getPayload().contains("json")) {
									jsonParse.compose(oResource, subscriptionBundle);
									payload = oResource.toString();
									okToPost = true;
								}
								else {
									// Unsupported mime type format
									result.setType("Invalid channel payload mime type '" + subscription.getChannel().getPayload() + "'!");
									log.fine("REST Hook Invalid channel payload mime type '" + subscription.getChannel().getPayload() + "'!");
								}
							}

							if (okToPost == true) {
								// Process HTTP headers if present
								List<String> headers = new ArrayList<String>();
								for (StringType header : subscription.getChannel().getHeader()) {
									headers.add(header.asStringValue());
								}
								response = resourceClient.post(subscription.getChannel().getEndpoint(), null, payload, subscription.getChannel().getPayload(), headers);

								result.setRefType(payload);
							}

							// Update Subscription status based on current result and post response
							this.setSubscriptionStatus(subscription, response, result);

							result.setPath("complete");
							break;
						case SMS:
							result.setPath("SMS channel type not supported");
							log.fine("SMS channel type not currently supported.");
							break;
						case WEBSOCKET:
							result.setPath("Websocket channel type not supported");
							log.fine("Websocket channel type not currently supported.");
							break;
						default:
							result.setPath("Unknown channel type");
							log.fine("Unknown channel type!");
							break;
						}
					}
					else {
						result.setType("Unsupported subscription topic '" + subscription.getCriteria() + "'!");
					}

					// Add result bean to results
					results.add(result);
				}
			}
			else {
				log.fine("SubscriptionServiceR5.processSubscriptions() - No active subscriptions found.");
				result = new LabelKeyValueBean("", "", "", "No active subscriptions found.", "complete");
				results.add(result);
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

		return results;
	}

	/*
	 * Private methods
	 */

	/**
	 * Update Subscription status and result outcome (path) based on current result and post response
	 *
	 * @param subscription
	 * @param response
	 * @param result
	 * @throws Exception
	 */
	private void setSubscriptionStatus(Subscription subscription, Response response, LabelKeyValueBean result) throws Exception {

		if (response != null) {
			if (response.getStatus() < 400) {
				result.setType("Subscription '" + subscription.getId() + "' processed successfully.");
			}
			else {
				result.setType("Subscription '" + subscription.getId() + "' process failed! Response from notification request '" +
						response.getStatus() + "'.");

				// TODO response failure! - update Subscription.status = error; create new SubscriptionStatus with error

			}
		}
		else {
			// TODO response is null - update Subscription.status = error; create new SubscriptionStatus with error
		}
	}

}
