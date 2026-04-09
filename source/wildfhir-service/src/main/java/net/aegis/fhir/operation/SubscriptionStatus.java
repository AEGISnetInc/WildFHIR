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
package net.aegis.fhir.operation;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedMap;
import net.aegis.fhir.model.ResourceContainer;
import net.aegis.fhir.service.BatchService;
import net.aegis.fhir.service.CodeService;
import net.aegis.fhir.service.ConformanceService;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.ResourcemetadataService;
import net.aegis.fhir.service.TransactionService;
import net.aegis.fhir.service.audit.AuditEventService;
import net.aegis.fhir.service.narrative.FHIRNarrativeGeneratorClient;
import net.aegis.fhir.service.provenance.ProvenanceService;
import net.aegis.fhir.service.util.ServicesUtil;

/**
 * @author richard.ettema
 *
 */
public class SubscriptionStatus extends ResourceOperationProxy {

	private Logger log = Logger.getLogger("SubscriptionStatus");

	@Override
	public Parameters executeOperation(HttpServletRequest request, HttpHeaders headers, ResourceService resourceService, ResourcemetadataService resourcemetadataService, BatchService batchService, TransactionService transactionService, CodeService codeService, AuditEventService auditEventService, ProvenanceService provenanceService, ConformanceService conformanceService, String softwareVersion, String resourceType, String resourceId, Parameters inputParameters, org.hl7.fhir.r4.model.Resource inputResource, String inputString, String contentType, boolean isPost, StringBuffer returnedDirective) throws Exception {

		log.fine("[START] SubscriptionStatus.executeOperation()");

		Parameters out = null;
		Bundle subscriptionSearchSet = null;
		OperationOutcome rOutcome = null;

		try {
			/*
			 * If inputParameters is null, attempt to extract parameters from context
			 */
			if (inputParameters == null) {
				inputParameters = getParametersFromQueryParams(request);
			}

			List<String> ids = new ArrayList<String>();
			List<String> statuses = new ArrayList<String>();

			/*
			 * If resourceId is not defined, extract the individual expected parameters
			 */
			if (resourceId == null || resourceId.isEmpty()) {

				if (inputParameters != null && inputParameters.hasParameter()) {

					for (ParametersParameterComponent parameter : inputParameters.getParameter()) {

						if (parameter.getName() != null && parameter.getName().equals("id")) {

							if (parameter.getValue() instanceof IdType) {
								ids.add(((IdType) parameter.getValue()).getValueAsString());
							}
						}

						if (parameter.getName() != null && parameter.getName().equals("status")) {

							if (parameter.getValue() instanceof CodeType) {
								statuses.add(((CodeType) parameter.getValue()).getValueAsString());
							}
						}
					}
				}
			}

			// Get SubscriptionsStatus search results based on given parameters
			subscriptionSearchSet = getSubscriptionStatus(request, resourceService, resourceId, ids, statuses);

			out = new Parameters();

			ParametersParameterComponent parameter = new ParametersParameterComponent();
			parameter.setName("return");

			if (subscriptionSearchSet != null) {
				parameter.setResource(subscriptionSearchSet);
			}
			else {
				rOutcome = new OperationOutcome();
				OperationOutcome.OperationOutcomeIssueComponent issue =
						ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.INCOMPLETE,
								"Subscription $status failed! Search returned null based on given parameters.", null, null);

				if (issue != null) {
					rOutcome.setText(null);
					rOutcome.getIssue().add(issue);

					// Use RI NarrativeGenerator
					FHIRNarrativeGeneratorClient.instance().generate(rOutcome);
				}

				parameter.setResource(rOutcome);
			}

			out.addParameter(parameter);
		}
		catch (Exception e) {
			// Throw exceptions back
			throw e;
		}

		return out;
	}

	/**
	 * @param request
	 * @param resourceService
	 * @param resourceId
	 * @param ids
	 * @param status
	 * @return Bundle
	 */
	private Bundle getSubscriptionStatus(HttpServletRequest request, ResourceService resourceService, String resourceId, List<String> ids, List<String> statuses) throws Exception {

		Bundle searchBundle = null;

		// Construct full request URL with any query parameters
		StringBuffer requestURL = request.getRequestURL();
		String queryString = request.getQueryString();
		if (queryString != null) {
			requestURL.append("?").append(queryString);
		}
		String locationPath = requestURL.toString();

		// Build search parameter string
		StringBuilder sbParams = new StringBuilder();
		MultivaluedMap<String, String> queryParams = null;

		if (resourceId != null && !resourceId.isEmpty()) {
			sbParams.append("subscription=Subscription/").append(resourceId);
		}
		else if (!ids.isEmpty() || !statuses.isEmpty()) {
			int count = 0;
			if (!ids.isEmpty()) {
				sbParams.append("subscription=");
				for (String id : ids) {
					if (count > 0) {
						sbParams.append(",");
					}
					sbParams.append("Subscription/").append(id);
					count++;
				}
			}

			if (!statuses.isEmpty()) {
				count = 0;
				if (sbParams.length() > 2) {
					sbParams.append("&");
				}
				sbParams.append("status=");
				for (String status : statuses) {
					if (count > 0) {
						sbParams.append(",");
					}
					sbParams.append(status);
					count++;
				}
			}
		}

		if (sbParams.length() > 2) {
			// Convert search parameter string into queryParams map
			List<NameValuePair> params = URLEncodedUtils.parse(sbParams.toString(), Charset.defaultCharset());
			queryParams = ServicesUtil.INSTANCE.listNameValuePairToMultivaluedMapString(params);
		}

		// Search for all SubscriptionStatus based on given parameters; return as searchset Bundle
		ResourceContainer rc = resourceService.search(queryParams, null, null, "SubscriptionStatus", locationPath, null, null, null, false);

		// Check for matched Subscription resources
		if (rc != null && rc.getBundle() != null) {

			searchBundle = rc.getBundle();
		}

		return searchBundle;
	}

	/**
	 *
	 * @param request
	 * @return <code>Parameters</code>
	 * @throws Exception
	 */
	private Parameters getParametersFromQueryParams(HttpServletRequest request) throws Exception {

		log.fine("[START] SubscriptionStatus.getParametersFromQueryParams()");

		// Default empty Parameters
		Parameters queryParameters = new Parameters();

		try {
			if (request != null) {
				/*
				 * Extract the individual expected parameters
				 */
				IdType id = null;
				CodeType status = null;

				// Get the query parameters that represent the search criteria
				MultivaluedMap<String, String> queryParams = ServicesUtil.INSTANCE.parseRequestQuery(request);

				if (queryParams != null && queryParams.size() > 0) {
					Set<Entry<String, List<String>>> paramSet = queryParams.entrySet();

					for (Entry<String, List<String>> entry : paramSet) {

						String key = entry.getKey();
						String value = entry.getValue().get(0);

						if (key.equals("id")) {
							ParametersParameterComponent parameter = new ParametersParameterComponent();
							parameter.setName(key);
							id = new IdType();
							id.setValue(value);
							parameter.setValue(id);
							queryParameters.addParameter(parameter);
						}
						else if (key.equals("status")) {
							ParametersParameterComponent parameter = new ParametersParameterComponent();
							parameter.setName(key);
							status = new CodeType();
							status.setValue(value);
							parameter.setValue(status);
							queryParameters.addParameter(parameter);
						}
					}
				}
			}
		}
		catch (Exception e) {
			// Handle generic exceptions
			e.printStackTrace();
			throw e;
		}

		return queryParameters;
	}

}
