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

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import net.aegis.fhir.model.ResourceContainer;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.model.Tag;
import net.aegis.fhir.service.BatchService;
import net.aegis.fhir.service.CodeService;
import net.aegis.fhir.service.ConformanceService;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.ResourcemetadataService;
import net.aegis.fhir.service.TransactionService;
import net.aegis.fhir.service.narrative.FHIRNarrativeGeneratorClient;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UUIDUtil;

import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r4.model.Resource;

/**
 * @author richard.ettema
 *
 */
public class ResourceMeta extends ResourceOperationProxy {

	private Logger log = Logger.getLogger("ResourceMeta");

	private ResourceService resourceService;
	private ResourcemetadataService resourcemetadataService;

	/* (non-Javadoc)
	 * @see net.aegis.fhir.operation.ResourceOperationProxy#executeOperation(javax.ws.rs.core.UriInfo, javax.ws.rs.core.HttpHeaders, net.aegis.fhir.service.ResourceService, net.aegis.fhir.service.ResourcemetadataService, net.aegis.fhir.service.BatchService, net.aegis.fhir.service.TransactionService, net.aegis.fhir.service.CodeService, net.aegis.fhir.service.ConformanceService, java.lang.String, java.lang.String, java.lang.String, org.hl7.fhir.r4.model.Parameters, org.hl7.fhir.r4.model.Resource, java.lang.String, java.lang.String, boolean, java.lang.StringBuffer)
	 */
	@Override
	public Parameters executeOperation(UriInfo context, HttpHeaders headers, ResourceService resourceService, ResourcemetadataService resourcemetadataService, BatchService batchService, TransactionService transactionService, CodeService codeService, ConformanceService conformanceService, String softwareVersion, String resourceType, String resourceId, Parameters inputParameters, org.hl7.fhir.r4.model.Resource inputResource, String inputString, String contentType, boolean isPost, StringBuffer returnedDirective) throws Exception {

		log.fine("[START] ResourceMeta.executeOperation()");

		this.resourceService = resourceService;
		this.resourcemetadataService = resourcemetadataService;

		Parameters out = new Parameters();

		ParametersParameterComponent parameter = new ParametersParameterComponent();
		parameter.setName("return");
		out.addParameter(parameter);

		// Ignore any input parameters as they are not expected

		// Check for operation level - global, resource-type or resource-instance
		if (resourceType == null && resourceId == null) {
			// Global - return distinct list of meta data entries within the entire repository
			List<Resourcemetadata> tagList = this.resourcemetadataService.findAllDistinctTags();

			Meta resourceMeta = new Meta();
			// Populate meta.id with dummy value for conformance if no meta found
			resourceMeta.setId(UUIDUtil.getUUID());

			if (tagList != null && !tagList.isEmpty()) {

				for (Resourcemetadata metadata : tagList) {

					if (metadata.getParamName().equals(Tag.METADATA_NAME_PROFILE_TAG)) {
						resourceMeta.addProfile(metadata.getParamValue());
					}
					if (metadata.getParamName().equals(Tag.METADATA_NAME_SECURITY_TAG)) {
						Coding securityCoding = new Coding(metadata.getSystemValue(), metadata.getParamValue(), metadata.getCodeValue());
						resourceMeta.addSecurity(securityCoding);
					}
					if (metadata.getParamName().equals(Tag.METADATA_NAME_GENERAL_TAG)) {
						Coding tagCoding = new Coding(metadata.getSystemValue(), metadata.getParamValue(), metadata.getCodeValue());
						resourceMeta.addTag(tagCoding);
					}
				}
			}

			parameter.setValue(resourceMeta);
		}
		else if (resourceType != null && resourceId == null) {
			// Resource-type - return distinct list of meta data entries for the specific resource type
			List<Resourcemetadata> tagList = this.resourcemetadataService.findAllDistinctTagsByResourceType(resourceType);

			Meta resourceMeta = new Meta();
			// Populate meta.id with dummy value for conformance if no meta found
			resourceMeta.setId(UUIDUtil.getUUID());

			if (tagList != null && !tagList.isEmpty()) {

				for (Resourcemetadata metadata : tagList) {

					if (metadata.getParamName().equals(Tag.METADATA_NAME_PROFILE_TAG)) {
						resourceMeta.addProfile(metadata.getParamValue());
					}
					if (metadata.getParamName().equals(Tag.METADATA_NAME_SECURITY_TAG)) {
						Coding securityCoding = new Coding(metadata.getSystemValue(), metadata.getParamValue(), metadata.getCodeValue());
						resourceMeta.addSecurity(securityCoding);
					}
					if (metadata.getParamName().equals(Tag.METADATA_NAME_GENERAL_TAG)) {
						Coding tagCoding = new Coding(metadata.getSystemValue(), metadata.getParamValue(), metadata.getCodeValue());
						resourceMeta.addTag(tagCoding);
					}
				}
			}

			parameter.setValue(resourceMeta);
		}
		else {
			// Resource-instance - return the meta data entries for the resource instance
			ResourceContainer resourceContainer = this.resourceService.read(resourceType, resourceId, null);

			if (resourceContainer == null || resourceContainer.getResource() == null || !resourceContainer.getResponseStatus().equals(Response.Status.OK)) {
				OperationOutcome rOutcome = new OperationOutcome();
				OperationOutcome.OperationOutcomeIssueComponent issue = ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.CONFLICT,
						"$meta failed. The [id] url value does not reference a valid resource instance.", null, "/" + resourceType + "/id");

				if (issue != null) {
					rOutcome.setText(null);
					rOutcome.getIssue().add(issue);

					// Use RI NarrativeGenerator
					FHIRNarrativeGeneratorClient.instance().generate(rOutcome);
				}

				parameter.setResource(rOutcome);
			}
			else {
				XmlParser xmlP = new XmlParser();
				Resource resource = xmlP.parse(new ByteArrayInputStream(resourceContainer.getResource().getResourceContents()));

				Meta resourceMeta = new Meta();

				if (resource.hasMeta()) {
					resourceMeta = resource.getMeta();

					if (resourceMeta.hasProfile() || resourceMeta.hasSecurity() || resourceMeta.hasTag()) {
						// Remove versionId and lastUpdated values
						resourceMeta.setVersionIdElement(null);
						resourceMeta.setLastUpdatedElement(null);
					}
				}

				// Populate meta.id with dummy value for conformance if no meta found
				resourceMeta.setId(UUIDUtil.getUUID());

				parameter.setValue(resourceMeta);
			}
		}

		return out;
	}

}
