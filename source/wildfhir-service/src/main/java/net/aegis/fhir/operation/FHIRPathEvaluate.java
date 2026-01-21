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

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import org.hl7.fhir.r4.formats.JsonParser;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r4.utils.NarrativeGenerator;

import net.aegis.fhir.service.BatchService;
import net.aegis.fhir.service.CodeService;
import net.aegis.fhir.service.ConformanceService;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.ResourcemetadataService;
import net.aegis.fhir.service.TransactionService;
import net.aegis.fhir.service.audit.AuditEventService;
import net.aegis.fhir.service.provenance.ProvenanceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.validation.FHIRValidatorClient;

/**
 * @author richard.ettema
 *
 */
public class FHIRPathEvaluate extends ResourceOperationProxy {

	private Logger log = Logger.getLogger("FHIRPathEvaluate");

	/* (non-Javadoc)
	 * @see net.aegis.fhir.operation.ResourceOperationProxy#executeOperation(javax.ws.rs.core.UriInfo, javax.ws.rs.core.HttpHeaders, net.aegis.fhir.service.ResourceService, net.aegis.fhir.service.ResourcemetadataService, net.aegis.fhir.service.BatchService, net.aegis.fhir.service.TransactionService, net.aegis.fhir.service.CodeService, net.aegis.fhir.service.audit.AuditEventService, net.aegis.fhir.service.provenance.ProvenanceService, net.aegis.fhir.service.ConformanceService, java.lang.String, java.lang.String, java.lang.String, org.hl7.fhir.r4.model.Parameters, org.hl7.fhir.r4.model.Resource, java.lang.String, java.lang.String, boolean, java.lang.StringBuffer)
	 */
	@Override
	public Parameters executeOperation(UriInfo context, HttpHeaders headers, ResourceService resourceService, ResourcemetadataService resourcemetadataService, BatchService batchService, TransactionService transactionService, CodeService codeService, AuditEventService auditEventService, ProvenanceService provenanceService, ConformanceService conformanceService, String softwareVersion, String resourceType, String resourceId, Parameters inputParameters, org.hl7.fhir.r4.model.Resource inputResource, String inputString, String contentType, boolean isPost, StringBuffer returnedDirective) throws Exception {

		log.fine("[START] FHIRPathEvaluate.executeOperation()");

		Parameters outputParameters = new Parameters();
		ParametersParameterComponent outputParameter = null;
		OperationOutcome rOutcome = null;

		ByteArrayOutputStream oOp = new ByteArrayOutputStream();
		XmlParser xmlP = new XmlParser();
		JsonParser jsonP = new JsonParser();

		try {
			// Ignore any input parameters as they are not expected

			// Check for operation level of global
			if (resourceType == null && resourceId == null) {

				if (inputParameters != null) {
					// Extract expected parameters
					log.info("Extract expected evaluate parameters");
					StringType method = null;
					Resource resource = null;
					byte[] resourceContents = null;
					StringType expression = null;

					if (inputParameters.hasParameter()) {

						for (ParametersParameterComponent parameter : inputParameters.getParameter()) {

							if (parameter.getName() != null && parameter.getName().equals("method")) {

								if (parameter.getValue() instanceof StringType) {
									method = (StringType) parameter.getValue();
								}
							}

							if (parameter.getName() != null && parameter.getName().equals("resource")) {

								resource = parameter.getResource();

								if (contentType.indexOf("xml") >= 0) {
									// Convert Resource to base64binary XML contents
									oOp = new ByteArrayOutputStream();
									xmlP.compose(oOp, resource);
								} else if (contentType.indexOf("json") >= 0) {
									// Convert Resource to base64binary JSON contents
									oOp = new ByteArrayOutputStream();
									jsonP.compose(oOp, resource);
								}

								resourceContents = oOp.toByteArray();
							}

							if (parameter.getName() != null && parameter.getName().equals("expression")) {

								if (parameter.getValue() instanceof StringType) {
									expression = (StringType) parameter.getValue();
								}
							}
						}

						// Build evaluate output parameters
						outputParameter = new ParametersParameterComponent();
						outputParameter.setName("method");
						outputParameter.setValue(method);
						outputParameters.getParameter().add(outputParameter);

						// Call evaluate based on method and build evaluate output parameters
						if (method.getValue().equalsIgnoreCase("evaluate")) {
							log.warning("Calling FHIRValidatorClient with parameters for evaluate method");
							List<Base> items = FHIRValidatorClient.instance().evaluate(resourceContents, expression.getValue());

							// Iterate over list of base items and add parameter for each one based on its type
							for (Base item : items) {

								outputParameter = new ParametersParameterComponent();
								outputParameter.setName("returnItem");

								if (item.isPrimitive()) {
									StringType itemValue = new StringType(item.primitiveValue());
									outputParameter.setValue(itemValue);
								}
								else if (item instanceof Resource) {
									outputParameter.setResource((Resource) item);
								}
								else {
									StringType itemValue = new StringType(item.getClass().getName() + "CONTENTS NOT AVAILABLE");
									outputParameter.setValue(itemValue);
								}
								outputParameters.getParameter().add(outputParameter);
							}
						}
						else if (method.getValue().equalsIgnoreCase("evaluateToBoolean")) {
							log.warning("Calling FHIRValidatorClient with parameters for evaluateToBoolean method");
							boolean result = FHIRValidatorClient.instance().evaluateToBoolean(resourceContents, expression.getValue());

							outputParameter = new ParametersParameterComponent();
							outputParameter.setName("returnBoolean");
							StringType itemValue = new StringType(Boolean.toString(result));
							outputParameter.setValue(itemValue);
							outputParameters.getParameter().add(outputParameter);
						}
						else if (method.getValue().equalsIgnoreCase("evaluateToString")) {
							log.warning("Calling FHIRValidatorClient with parameters for evaluateToString method");
							String result = FHIRValidatorClient.instance().evaluateToString(resourceContents, expression.getValue());

							outputParameter = new ParametersParameterComponent();
							outputParameter.setName("returnString");
							StringType itemValue = new StringType(result);
							outputParameter.setValue(itemValue);
							outputParameters.getParameter().add(outputParameter);
						}
					}
					else {
						// Missing parameters in Parameters payload
						throw new Exception("$fhirpath-evalute operation failure! Input parameters not found.");
					}
				}
				else {
					// Missing or invalid XML format of Parameters payload
					throw new Exception("$fhirpath-evalute operation failure! Input ValidationParameters missing or not formatted.");
				}
			}
			else {
				throw new Exception("Invalid $fhirpath-evalute operation request! Global-only operation cannot specify resource type or id.");
			}
		}
		catch (Exception e) {
			// Write exception to server log first
			e.printStackTrace();

			// Handle generic exceptions
			rOutcome = new OperationOutcome();
			OperationOutcome.OperationOutcomeIssueComponent issue =
					ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.FATAL, OperationOutcome.IssueType.EXCEPTION,
							"$fhirpath-evalute operation failed!", e.getMessage(), null);

			if (issue != null) {
				rOutcome.setText(null);
				rOutcome.getIssue().add(issue);

				// Use RI NarrativeGenerator
				NarrativeGenerator narrativeGenerator = new NarrativeGenerator("", "", null);
				narrativeGenerator.generate(rOutcome, null);
			}
		}

		if (rOutcome != null) {
			outputParameters = new Parameters();
			outputParameter = new ParametersParameterComponent();
			outputParameter.setName("return");
			outputParameter.setResource(rOutcome);
			outputParameters.addParameter(outputParameter);
		}

		return outputParameters;
	}

}
