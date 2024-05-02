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

import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r4.model.StringType;

import net.aegis.fhir.model.Code;
import net.aegis.fhir.service.BatchService;
import net.aegis.fhir.service.CodeService;
import net.aegis.fhir.service.ConformanceService;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.ResourcemetadataService;
import net.aegis.fhir.service.TransactionService;
import net.aegis.fhir.service.narrative.FHIRNarrativeGeneratorClient;
import net.aegis.fhir.service.util.ServicesUtil;

/**
 * @author richard.ettema
 *
 */
public class CodeConfiguration extends ResourceOperationProxy {

	private Logger log = Logger.getLogger("CodeConfiguration");

	private CodeService codeService;

	/* (non-Javadoc)
	 * @see net.aegis.fhir.operation.ResourceOperationProxy#executeOperation(javax.ws.rs.core.UriInfo, javax.ws.rs.core.HttpHeaders, net.aegis.fhir.service.ResourceService, net.aegis.fhir.service.ResourcemetadataService, net.aegis.fhir.service.BatchService, net.aegis.fhir.service.TransactionService, net.aegis.fhir.service.CodeService, net.aegis.fhir.service.ConformanceService, java.lang.String, java.lang.String, java.lang.String, org.hl7.fhir.r4.model.Parameters, org.hl7.fhir.r4.model.Resource, java.lang.String, java.lang.String, boolean, java.lang.StringBuffer)
	 */
	@Override
	public Parameters executeOperation(UriInfo context, HttpHeaders headers, ResourceService resourceService, ResourcemetadataService resourcemetadataService, BatchService batchService, TransactionService transactionService, CodeService codeService, ConformanceService conformanceService, String softwareVersion, String resourceType, String resourceId, Parameters inputParameters, org.hl7.fhir.r4.model.Resource inputResource, String inputString, String contentType, boolean isPost, StringBuffer returnedDirective) throws Exception {

		log.fine("[START] CodeConfiguration.executeOperation()");

        this.codeService = codeService;

		Parameters out = null;
		OperationOutcome.OperationOutcomeIssueComponent issue = null;

		try {
			// Check for operation level of global
			if (resourceType == null && resourceId == null) {
				/*
				 * If inputParameters is null, attempt to extract parameters from context
				 */
				if (inputParameters == null) {
					inputParameters = getParametersFromQueryParams(context);
				}

				/*
				 * Extract the individual expected parameters
				 */
				StringType operationString = null;
				StringType codeNameString = null;
				StringType valueString = null;
				IntegerType intValueInteger = null;
				StringType resourceContentsString = null;

				if (inputParameters != null && inputParameters.hasParameter()) {

					for (ParametersParameterComponent parameter : inputParameters.getParameter()) {

						if (parameter.getName() != null && parameter.getName().equals("operation")) {

							if (parameter.getValue() instanceof StringType) {
								operationString = (StringType) parameter.getValue();
							}
						}

						if (parameter.getName() != null && parameter.getName().equals("codeName")) {

							if (parameter.getValue() instanceof StringType) {
								codeNameString = (StringType) parameter.getValue();
							}
						}

						if (parameter.getName() != null && parameter.getName().equals("value")) {

							if (parameter.getValue() instanceof StringType) {
								valueString = (StringType) parameter.getValue();
							}
						}

						if (parameter.getName() != null && parameter.getName().equals("intValue")) {

							if (parameter.getValue() instanceof IntegerType) {
								intValueInteger = (IntegerType) parameter.getValue();
							}
						}

						if (parameter.getName() != null && parameter.getName().equals("resourceContents")) {

							if (parameter.getValue() instanceof StringType) {
								resourceContentsString = (StringType) parameter.getValue();
							}
						}
					}
				}

				/*
				 * code configuration check
				 * - list operation - operation input parameter is required, all other input parameters are ignored
				 * - update operation - all input parameters are required
				 */
				if (operationString != null) {
					// list operation
					if (operationString.getValue().equals("list")) {
						// Return list of all code configuration types, names and current values
						List<Code> allCodes = codeService.findAll();

						if (allCodes != null && !allCodes.isEmpty()) {
							out = new Parameters();
							ParametersParameterComponent parameter = null;
							ParametersParameterComponent parameterPart = null;
							StringType partString = null;

							for (Code code : allCodes) {
								parameter = new ParametersParameterComponent();
								parameter.setName("code");

								parameterPart = new ParametersParameterComponent();
								parameterPart.setName("codeName");
								partString = new StringType(code.getCodeName());
								parameterPart.setValue(partString);
								parameter.addPart(parameterPart);

								parameterPart = new ParametersParameterComponent();
								parameterPart.setName("value");
								partString = new StringType(code.getValue());
								parameterPart.setValue(partString);
								parameter.addPart(parameterPart);

								parameterPart = new ParametersParameterComponent();
								parameterPart.setName("description");
								partString = new StringType(code.getDescription());
								parameterPart.setValue(partString);
								parameter.addPart(parameterPart);

								out.addParameter(parameter);
							}
						}
						else {
							// No code configuration settings returned
							issue = ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.INFORMATION, OperationOutcome.IssueType.INFORMATIONAL,
									"$code-configuration list empty. No code configuration settings found.", null, null);

							out = buildOperationOutcomeParameter(issue);
						}
					}
					else if (operationString.getValue().equals("update")) {
						// Check for required input parameters
						if (codeNameString != null && (valueString != null || intValueInteger != null || resourceContentsString != null)) {
							// Get existing code
							Code existingCode = this.codeService.findCodeByName(codeNameString.getValue());

							if (existingCode != null) {
								// Update existing code
								if (valueString != null) {
									existingCode.setValue(valueString.getValue());
								}
								if (intValueInteger != null) {
									existingCode.setIntValue(intValueInteger.getValue());
								}
								if (resourceContentsString != null) {
									existingCode.setResourceContents(resourceContentsString.getValue().getBytes());
								}

								Code updatedCode = this.codeService.update(existingCode);

								if (updatedCode != null) {
									issue = ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.INFORMATION, OperationOutcome.IssueType.INFORMATIONAL,
											"$code-configuration update completed. Update of existing code successful - '" + codeNameString.getValue() + "', '" + valueString.getValue() + "'.", null, null);
								}
								else {
									issue = ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.PROCESSING,
											"$code-configuration update failed. Update of existing code did not complete - '" + codeNameString.getValue() + "'.", null, null);
								}

							}
							else {
								issue = ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND,
										"$code-configuration update failed. Existing code not found - '" + codeNameString.getValue() + "'.", null, null);
							}

						}
						else {
							issue = ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.CONFLICT,
											"$code-configuration update failed. All input parameters are required - codeName=" + (codeNameString != null ? codeNameString.getValue() : "null") +
											" and value=" + (valueString != null ? valueString.getValue() : "null") + ".", null, null);
						}

						out = buildOperationOutcomeParameter(issue);
					}
					else {
						// Invalid operation input parameter value!
						issue = ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.CONFLICT,
								"$code-configuration operation invalid '" + operationString.getValue() + "'! Allowed operations are 'list' and 'update'.", null, null);

						out = buildOperationOutcomeParameter(issue);
					}
				}
				else {
					// operation input parameter missing!
					issue = ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.PROCESSING,
							"$code-configuration operation missing!", null, null);

					out = buildOperationOutcomeParameter(issue);
				}

			}
			else {
				throw new Exception("Invalid $code-configuration operation request! Global-only operation cannot specify resource type or id.");
			}

		}
		catch (Exception e) {
			// Throw exceptions back
			throw e;
		}

		return out;
	}

	private Parameters buildOperationOutcomeParameter(OperationOutcome.OperationOutcomeIssueComponent issue) throws Exception {

		log.fine("[START] CodeConfiguration.buildOperationOutcomeParameter()");

		Parameters out = new Parameters();
		OperationOutcome rOutcome = new OperationOutcome();

		if (issue != null) {
			rOutcome.setText(null);
			rOutcome.getIssue().add(issue);

			// Use RI NarrativeGenerator
			FHIRNarrativeGeneratorClient.instance().generate(rOutcome);
		}

		ParametersParameterComponent parameter = new ParametersParameterComponent();
		parameter.setName("return");
		parameter.setResource(rOutcome);

		out.addParameter(parameter);

		return out;
	}

	/**
	 *
	 * @param context
	 * @return <code>Parameters</code>
	 * @throws Exception
	 */
	private Parameters getParametersFromQueryParams(UriInfo context) throws Exception {

		log.fine("[START] CodeConfiguration.getParametersFromQueryParams()");

		// Default empty Parameters
		Parameters queryParameters = new Parameters();

		try {
			if (context != null) {
				log.info("Checking for url parameters...");

				/*
				 * Extract the individual expected parameters
				 */
				StringType paramString = null;

				// Get the query parameters that represent the search criteria
				MultivaluedMap<String, String> queryParams = context.getQueryParameters();

				if (queryParams != null && queryParams.size() > 0) {
					Set<Entry<String, List<String>>> paramSet = queryParams.entrySet();

					for (Entry<String, List<String>> entry : paramSet) {

						String key = entry.getKey();
						String value = entry.getValue().get(0);

						if (key.equals("operation") || key.equals("codeType") || key.equals("codeName") || key.equals("value")) {
							ParametersParameterComponent parameter = new ParametersParameterComponent();
							parameter.setName(key);
							paramString = new StringType();
							paramString.setValueAsString(value);
							parameter.setValue(paramString);
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
