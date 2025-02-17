/*
 * #%L
 * WildFHIR - wildfhir-service
 * %%
 * Copyright (C) 2024 AEGIS.net, Inc.
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
package net.aegis.fhir.service.subscription;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Subscription;

import net.aegis.fhir.client.ResourceRESTClient;
import net.aegis.fhir.model.LabelKeyValueBean;
import net.aegis.fhir.model.ResourceContainer;
import net.aegis.fhir.service.CodeService;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

/**
 * Subscription Service for performing the set of functions supporting the
 * Subscriptions Framework.
 *
 * Supported channel types: rest-hook
 *
 * The @Stateless annotation eliminates the need for manual transaction demarcation
 *
 * @author richard.ettema
 *
 */
@Stateless
public class SubscriptionService {

	private Logger log = Logger.getLogger("SubscriptionService");

    @Inject
    CodeService codeService;

    @Inject
	private ResourceService resourceService;

	@Inject
	private UTCDateUtil utcDateUtil;

	private ResourceRESTClient resourceClient;

	/*
	 * Public Methods
	 */

	public SubscriptionService() throws Exception {
		this.resourceClient = new ResourceRESTClient(codeService);
	}

	/**
	 * Process all active Subscription resources based on since datetime
	 *
	 * @param since
	 * @return List<LabelKeyValueBean> - Results
	 * @throws Exception
	 */
	public List<LabelKeyValueBean> processSubscriptions(Date since, Date until) throws Exception {

		log.fine("[START] SubscriptionService.processSubscriptions()");

		// Verify since date is not null
		if (since == null) {
			throw new Exception("processSubscriptions requires a valid, non-null date!");
		}

		// Verify until date; if null, assign current date
		if (until == null) {
			until = new Date();
		}

		List<LabelKeyValueBean> results = new ArrayList<LabelKeyValueBean>();

		/*
		 * Search for all Subscriptions with status = active
		 * For each Subscription
		 *   Perform search using criteria adding _lastUpdated=gt:since and _lastUpdated=lt:newSince parameters
		 *   For each matched resource instance
		 *     Send FHIR update to Subscription endpoint with matched resource instance as the payload
		 * Save new since date
		 */
		try {
			// Construct _lastUpdated parameters string
			StringBuilder sbSinceParams = new StringBuilder("_lastUpdated=gt")
					.append(utcDateUtil.formatDate(since, UTCDateUtil.DATE_PARAMETER_FORMAT));
//					.append("&_lastUpdated=lt")
//					.append(utcDateUtil.formatDate(until, UTCDateUtil.DATE_PARAMETER_FORMAT));

			log.info("sbSinceParams [" + sbSinceParams.toString() + "]");

			// Convert search parameter string into queryParams map
			List<NameValuePair> params = URLEncodedUtils.parse("status=active", Charset.defaultCharset());
			MultivaluedMap<String, String> queryParams = ServicesUtil.INSTANCE.listNameValuePairToMultivaluedMapString(params);

			// Search for all Subscriptions with status = active; return as searchset Bundle
			ResourceContainer rcSubscriptions = resourceService.search(queryParams, null, null, null, "Subscription", "INTERNAL", null, null, null, false);

			// Check for matched Subscription resources
			if (rcSubscriptions != null && rcSubscriptions.getBundle() != null && !rcSubscriptions.getBundle().getEntry().isEmpty()) {

				// For each Subscription entry
				Subscription subscription = null;
				LabelKeyValueBean result = null;
				int paramsStart = -1;
				String subscriptionResourceType = null;
				String paramsOnly = null;
				StringBuilder sbParams = null;

				for (BundleEntryComponent subscriptionEntry : rcSubscriptions.getBundle().getEntry()) {

					subscription = (Subscription)subscriptionEntry.getResource();

					log.info("Processing Subscription [" + subscription.getId() + "] with criteria [" + subscription.getCriteria() + "] for channel type [" + subscription.getChannel().getType().name() + "]");

					// Initialize result bean
					result = new LabelKeyValueBean(subscription.getId(), subscription.getChannel().getType().name(), subscription.getCriteria(), "", "processing");

					// Select processing based on channel.type
					switch (subscription.getChannel().getType()) {
					case EMAIL:
						result.setPath("not supported");
						log.info("Email channel type not currently supported.");
						break;
					case MESSAGE:
						result.setPath("not supported");
						log.info("FHIR messaging channel type not currently supported.");
						break;
					case NULL:
						result.setPath("not supported");
						log.info("NULL channel type not currently supported.");
						break;
					case RESTHOOK:
						// Perform search using criteria adding _lastUpdated=gt:since and _lastUpdated=lt:newSince parameters
						paramsStart = subscription.getCriteria().indexOf("?");
						if (paramsStart > -1 && paramsStart < subscription.getCriteria().length()) {
							subscriptionResourceType = subscription.getCriteria().substring(0, paramsStart);
							paramsOnly = subscription.getCriteria().substring(paramsStart + 1);
							sbParams = new StringBuilder(paramsOnly).append("&").append(sbSinceParams.toString());
						}
						else {
							subscriptionResourceType = subscription.getCriteria();
							paramsOnly = "";
							sbParams = new StringBuilder(sbSinceParams);
						}
						params = URLEncodedUtils.parse(sbParams.toString(), Charset.defaultCharset());
						log.info("params [" + params.toString() + "]");

						queryParams = ServicesUtil.INSTANCE.listNameValuePairToMultivaluedMapString(params);

						// Search for all criteria matches since last check; return as searchset Bundle
						ResourceContainer rcMatches = resourceService.search(queryParams, null, null, null, subscriptionResourceType, "INTERNAL", null, null, null, false);

						// Check for matched Subscription resources
						if (rcMatches != null && rcMatches.getBundle() != null && !rcMatches.getBundle().getEntry().isEmpty()) {

							int successes = 0;
							int failures = 0;
							Response response = null;
							// For each matched entry
							for (BundleEntryComponent matchEntry : rcMatches.getBundle().getEntry()) {

								log.info("-- Processing Matched Resource [" + matchEntry.getResource().getResourceType().name() + "/" + matchEntry.getResource().getId() + "] using payload mime type [" + subscription.getChannel().getPayload() + "]");

								response = null;

								// Process HTTP headers if present
								List<String> headers = new ArrayList<String>();
								for (StringType header : subscription.getChannel().getHeader()) {
									headers.add(header.asStringValue());
								}

								if (!subscription.getChannel().hasPayload()) {
									// If subscription.payload mime type is empty; send simple POST to subscription.endpoint
									response = resourceClient.post(subscription.getChannel().getEndpoint(), null, null, null, headers);
								}
								else {
									// send FHIR update to subscription.endpoint
									response = resourceClient.update(matchEntry.getResource().getId(), matchEntry.getResource(), subscription.getChannel().getEndpoint(), subscriptionResourceType,
											subscription.getChannel().getPayload(), null, null, null, null, headers);
								}

								if (response.getStatus() < 400) {
									successes++;
								}
								else {
									failures++;
								}
							}

							result.setType(successes + " processed; " + failures + " failures");
						}
						else {
							result.setType("0 matches");
							log.info("-- No matches found.");
						}
						result.setPath("complete");
						break;
					case SMS:
						result.setPath("not supported");
						log.info("SMS channel type not currently supported.");
						break;
					case WEBSOCKET:
						result.setPath("not supported");
						log.info("Websocket channel type not currently supported.");
						break;
					default:
						result.setPath("unknown");
						log.info("Unknown channel type!");
						break;

					}

					// Add result bean to results
					results.add(result);
				}
			}
			else {
				log.info("SubscriptionService.processSubscriptions() - No active subscriptions found.");
			}

		} catch (Exception e) {
			log.severe(e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return results;

	}

}
