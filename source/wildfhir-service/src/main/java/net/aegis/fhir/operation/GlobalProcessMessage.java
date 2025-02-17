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
package net.aegis.fhir.operation;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.MessageHeader;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r4.model.UrlType;

import net.aegis.fhir.message.ProcessMessageProxy;
import net.aegis.fhir.message.ProcessMessageProxyObjectFactory;
import net.aegis.fhir.service.BatchService;
import net.aegis.fhir.service.CodeService;
import net.aegis.fhir.service.ConformanceService;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.ResourcemetadataService;
import net.aegis.fhir.service.TransactionService;
import net.aegis.fhir.service.util.ServicesUtil;

/**
 * @author richard.ettema
 *
 */
public class GlobalProcessMessage extends ResourceOperationProxy {

	private Logger log = Logger.getLogger("GlobalProcessMessage");

	/* (non-Javadoc)
	 * @see net.aegis.fhir.operation.ResourceOperationProxy#executeOperation(javax.ws.rs.core.UriInfo, javax.ws.rs.core.HttpHeaders, net.aegis.fhir.service.ResourceService, net.aegis.fhir.service.ResourcemetadataService, net.aegis.fhir.service.BatchService, net.aegis.fhir.service.TransactionService, net.aegis.fhir.service.CodeService, net.aegis.fhir.service.ConformanceService, java.lang.String, java.lang.String, java.lang.String, org.hl7.fhir.r4.model.Parameters, org.hl7.fhir.r4.model.Resource, java.lang.String, java.lang.String, boolean, java.lang.StringBuffer)
	 */
	@Override
	public Parameters executeOperation(UriInfo context, HttpHeaders headers, ResourceService resourceService, ResourcemetadataService resourcemetadataService, BatchService batchService, TransactionService transactionService, CodeService codeService, ConformanceService conformanceService, String softwareVersion, String resourceType, String resourceId, Parameters inputParameters, org.hl7.fhir.r4.model.Resource inputResource, String inputString, String contentType, boolean isPost, StringBuffer returnedDirective) throws Exception {

        log.fine("[START] GlobalProcessMessage.executeOperation()");

		BooleanType async = null;
		UrlType responseUrl = null;
		String messageEvent = null;

		Parameters out = null;
		ParametersParameterComponent parameter = null;

		XmlParser xmlParser = new XmlParser();
		OperationOutcome rOutcome = null;
		Bundle messageResponseBundle = null;

		try {
			// Check for operation level of global
			if (resourceType == null && resourceId == null) {
				// Global - return supported version(s) from code table
				/*
				 * inputParameters is expected to be null, attempt to extract parameters from context
				 */
				if (inputParameters == null) {

					/*
					 * inputResource is expected to be not null and a message Bundle
					 */
					if (inputResource != null && inputResource.fhirType().equals("Bundle")) {

						Bundle requestBundle = (Bundle)inputResource;

						/*
						 * First Bundle.entry must be a MessageHeader where we extract the eventCoding.code
						 *
						 * USE OF eventUri CURRENTLY NOT SUPPORTED
						 */
						if (requestBundle.hasEntry() && requestBundle.getEntryFirstRep().hasResource() && requestBundle.getEntryFirstRep().getResource().fhirType().equals("MessageHeader")) {

							MessageHeader requestMessageHeader = (MessageHeader)requestBundle.getEntryFirstRep().getResource();

							if (requestMessageHeader.hasEventCoding() && requestMessageHeader.getEventCoding().hasCode()) {

								// Get eventCoding.code for message event name
								messageEvent = requestMessageHeader.getEventCoding().getCode();

								// extract parameters from context (if present)
								inputParameters = getParametersFromQueryParams(context);

								// inputParameters is optional; if present, extract async and/or response-url values
								if (inputParameters != null && inputParameters.hasParameter()) {

									for (ParametersParameterComponent parameterComp : inputParameters.getParameter()) {

										if (parameterComp.getName() != null && parameterComp.getName().equals("async")) {

											if (parameterComp.getValue() instanceof BooleanType) {
												async = (BooleanType) parameterComp.getValue();
											}
										}

										if (parameterComp.getName() != null && parameterComp.getName().equals("response-url")) {

											if (parameterComp.getValue() instanceof UrlType) {
												responseUrl = (UrlType) parameterComp.getValue();
											}
										}
									}
								}

								// Processing starts here...
								if (async != null) {
									log.info("GlobalProcessMessage.executeOperation() - async parameter found '" + async.asStringValue() + "'");

									// Return response-url or source.endpoint only if async == true
									if (async.asStringValue().equalsIgnoreCase("true" )) {
										if (responseUrl != null) {
											log.info("GlobalProcessMessage.executeOperation() - responseUrl parameter found '" + responseUrl.asStringValue() + "'");
											returnedDirective.append(responseUrl.asStringValue());
										}
										else {
											// Check MessageHeader.source.endpoint
											if (requestMessageHeader.hasSource() && requestMessageHeader.getSource().hasEndpoint()) {
												log.info("GlobalProcessMessage.executeOperation() - MessageHeader.source.endpoint found '" + requestMessageHeader.getSource().getEndpoint() + "'");
												returnedDirective.append(requestMessageHeader.getSource().getEndpoint());
											}
										}
									}
								}

								/*
								 * Use Factory Pattern for execution of $process-message operation
								 */
								ProcessMessageProxyObjectFactory messageFactory = new ProcessMessageProxyObjectFactory();

								ProcessMessageProxy messageProxy = messageFactory.getProcessMessageProxy(messageEvent);

								// Test proxy
								if (messageProxy != null) {
									log.info("Calling messageProxy.processMessage()");

									messageResponseBundle = messageProxy.processMessage(context, resourceService, resourcemetadataService, codeService, requestBundle, async, responseUrl);
								}
								else {
									// Something went wrong
									String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.TRANSIENT, "$process-message operation failure, no message event interface defined for '" + messageEvent + "'", null, null, "application/fhir+xml");

									rOutcome = (OperationOutcome) xmlParser.parse(outcome);
								}
							}
							else {
								/*
								 * Message Bundle first entry not a MessageHeader; return error OperationOutcome
								 */
								String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.INVALID, "$process-message invalid request message Bundle payload. Message Bundle first entry MessageHeader resource must have eventCoding; eventUri reference to EventDefinition is not currently supported.", null, null, "application/fhir+xml");

								rOutcome = (OperationOutcome) xmlParser.parse(outcome);
							}
						}
						else {
							/*
							 * Message Bundle first entry not a MessageHeader; return error OperationOutcome
							 */
							String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.INVALID, "$process-message invalid request message Bundle payload. Message Bundle first entry must have MessageHeader resource.", null, null, "application/fhir+xml");

							rOutcome = (OperationOutcome) xmlParser.parse(outcome);
						}
					}
					else {
						/*
						 * inputResource is null or not a Bundle; return error OperationOutcome
						 */
						String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.INVALID, "$process-message invalid request FHIR resource payload. Message Bundle expected.", null, null, "application/fhir+xml");

						rOutcome = (OperationOutcome) xmlParser.parse(outcome);
					}
				}
				else {
					/*
					 * Unexpected inputParameters; return error OperationOutcome
					 */
					String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.INVALID, "$process-message invalid request Parameters payload. Message Bundle expected.", null, null, "application/fhir+xml");

					rOutcome = (OperationOutcome) xmlParser.parse(outcome);
				}
			}
			else {
				throw new Exception("Invalid $process-message operation request! Global-only operation cannot specify resource type or id.");
			}

		}
		catch (Exception e) {
			// Throw exceptions back
			throw e;
		}

		if (messageResponseBundle != null) {
			out = new Parameters();
			parameter = new ParametersParameterComponent();
			parameter.setName("return");
			parameter.setResource(messageResponseBundle);
			out.addParameter(parameter);
		}
		else if (rOutcome != null) {
			out = new Parameters();
			parameter = new ParametersParameterComponent();
			parameter.setName("return");
			parameter.setResource(rOutcome);
			out.addParameter(parameter);
		}

		return out;
	}

	/**
	 *
	 * @param context
	 * @return <code>Parameters</code>
	 * @throws Exception
	 */
	private Parameters getParametersFromQueryParams(UriInfo context) throws Exception {

		log.fine("[START] GlobalProcessMessage.getParametersFromQueryParams()");

		// Default empty Parameters
		Parameters queryParameters = new Parameters();

		try {
			if (context != null) {
				log.info("Checking for operation parameters...");

				/*
				 * Extract the individual expected parameters
				 */
				BooleanType async = null;
				UrlType responseUrl = null;

				// Get the query parameters that represent the search criteria
				MultivaluedMap<String, String> queryParams = context.getQueryParameters();

				if (queryParams != null && queryParams.size() > 0) {
					Set<Entry<String, List<String>>> paramSet = queryParams.entrySet();

					for (Entry<String, List<String>> entry : paramSet) {

						String key = entry.getKey();
						String value = entry.getValue().get(0);

						if (key.equals("async")) {
							ParametersParameterComponent parameter = new ParametersParameterComponent();
							parameter.setName(key);
							async = new BooleanType();
							async.setValueAsString(value);
							parameter.setValue(async);
							queryParameters.addParameter(parameter);
						}
						else if (key.equals("response-url")) {
							ParametersParameterComponent parameter = new ParametersParameterComponent();
							parameter.setName(key);
							responseUrl = new UrlType();
							responseUrl.setValue(value);
							parameter.setValue(responseUrl);
							queryParameters.addParameter(parameter);
						}
					}
				}
			}
		}
		catch (Exception e) {
			// Handle generic exceptions
			log.severe(e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return queryParameters;
	}

}
