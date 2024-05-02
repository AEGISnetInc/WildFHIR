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

import java.util.logging.Logger;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;

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
public class ResourcePurgeAll extends ResourceOperationProxy {

	private Logger log = Logger.getLogger("ResourcePurgeAll");

	private CodeService codeService;

	private ResourceService resourceService;

	/* (non-Javadoc)
	 * @see net.aegis.fhir.operation.ResourceOperationProxy#executeOperation(javax.ws.rs.core.UriInfo, javax.ws.rs.core.HttpHeaders, net.aegis.fhir.service.ResourceService, net.aegis.fhir.service.ResourcemetadataService, net.aegis.fhir.service.BatchService, net.aegis.fhir.service.TransactionService, net.aegis.fhir.service.CodeService, net.aegis.fhir.service.ConformanceService, java.lang.String, java.lang.String, java.lang.String, org.hl7.fhir.r4.model.Parameters, org.hl7.fhir.r4.model.Resource, java.lang.String, java.lang.String, boolean, java.lang.StringBuffer)
	 */
	@Override
	public Parameters executeOperation(UriInfo context, HttpHeaders headers, ResourceService resourceService, ResourcemetadataService resourcemetadataService, BatchService batchService, TransactionService transactionService, CodeService codeService, ConformanceService conformanceService, String softwareVersion, String resourceType, String resourceId, Parameters inputParameters, org.hl7.fhir.r4.model.Resource inputResource, String inputString, String contentType, boolean isPost, StringBuffer returnedDirective) throws Exception {

		log.fine("[START] ResourcePurgeAll.executeOperation()");

        this.codeService = codeService;
		this.resourceService = resourceService;

		Parameters out = new Parameters();
		OperationOutcome rOutcome = null;

		try {
			// Ignore any input parameters as they are not expected

			// Check for operation level of global
			if (resourceType == null && resourceId == null) {
				// Check for Resource Purge All Enabled
				if (this.codeService.isSupported("resourcePurgeAllEnabled")) {
					// Global - Truncate resource and resourcemetadata tables
					this.resourceService.resourcePurgeAll();

					ParametersParameterComponent parameter = new ParametersParameterComponent();
					parameter.setName("result");
					StringType resultString = new StringType("Resource Purge All operation complete.");
					parameter.setValue(resultString);
					out.addParameter(parameter);
				}
				else {
					rOutcome = new OperationOutcome();
					OperationOutcome.OperationOutcomeIssueComponent issue =
							ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTSUPPORTED,
									"Resource $purge-all not enabled!", null, null);

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
				}
			}
			else {
				throw new Exception("Invalid $purge-all operation request! Global-only operation cannot specify resource type or id.");
			}

		}
		catch (Exception e) {
			// Throw exceptions back
			throw new Exception("Exception processing $purge-all operation request! " + e.getMessage());
		}

		return out;
	}

}
