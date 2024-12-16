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
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import net.aegis.fhir.model.ResourceContainer;
import net.aegis.fhir.service.BatchService;
import net.aegis.fhir.service.CodeService;
import net.aegis.fhir.service.ConformanceService;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.ResourcemetadataService;
import net.aegis.fhir.service.TransactionService;
import net.aegis.fhir.service.util.UUIDUtil;

import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.formats.IParser.OutputStyle;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Type;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;

/**
 * @author richard.ettema
 *
 */
public class ResourceMetaDelete extends ResourceOperationProxy {

	private Logger log = Logger.getLogger("ResourceMetaDelete");

	private ResourceService resourceService;

	/* (non-Javadoc)
	 * @see net.aegis.fhir.operation.ResourceOperationProxy#executeOperation(javax.ws.rs.core.UriInfo, javax.ws.rs.core.HttpHeaders, net.aegis.fhir.service.ResourceService, net.aegis.fhir.service.ResourcemetadataService, net.aegis.fhir.service.BatchService, net.aegis.fhir.service.TransactionService, net.aegis.fhir.service.CodeService, net.aegis.fhir.service.ConformanceService, java.lang.String, java.lang.String, java.lang.String, org.hl7.fhir.r4.model.Parameters, org.hl7.fhir.r4.model.Resource, java.lang.String, java.lang.String, boolean, java.lang.StringBuffer)
	 */
	@Override
	public Parameters executeOperation(UriInfo context, HttpHeaders headers, ResourceService resourceService, ResourcemetadataService resourcemetadataService, BatchService batchService, TransactionService transactionService, CodeService codeService, ConformanceService conformanceService, String softwareVersion, String resourceType, String resourceId, Parameters inputParameters, org.hl7.fhir.r4.model.Resource inputResource, String inputString, String contentType, boolean isPost, StringBuffer returnedDirective) throws Exception {

		log.fine("[START] ResourceMetaDelete.executeOperation()");

		this.resourceService = resourceService;

		String baseUrl = context.getAbsolutePath().toString();

		Parameters out = new Parameters();

		ParametersParameterComponent parameter = new ParametersParameterComponent();
		parameter.setName("return");
		out.addParameter(parameter);

		// Get Meta from current resource
		// Resource-instance - return the meta data entries for the resource instance
		ResourceContainer resourceContainer = this.resourceService.read(resourceType, resourceId, null);

		if (resourceContainer != null && resourceContainer.getResource() != null && resourceContainer.getResponseStatus().equals(Response.Status.OK)) {
			XmlParser xmlP = new XmlParser();
			Resource resource = xmlP.parse(new ByteArrayInputStream(resourceContainer.getResource().getResourceContents()));

			Meta resourceMeta = new Meta();

			if (resource.hasMeta()) {
				resourceMeta = resource.getMeta().copy();

				// Remove versionId and lastUpdated values
				resourceMeta.setVersionIdElement(null);
				resourceMeta.setLastUpdatedElement(null);
			}

			// Input Parameters contain Meta to delete
			if (inputParameters != null && inputParameters.hasParameter()) {

				// We expect only one parameter named meta of type Meta
				ParametersParameterComponent inputParameter = inputParameters.getParameter().get(0);

				if (inputParameter.getName().equals("meta") && inputParameter.hasValue()) {

					Type valueType = inputParameter.getValue();

					if (valueType instanceof Meta) {
						// Delete Meta from Input Parameters; skip elements not in current Meta
						Meta inputMeta = (Meta)valueType;

						// Delete Profiles only if the resource has any
						if (resourceMeta.hasProfile()) {

							boolean keepInputProfile = true;
							List<CanonicalType> resourceProfileToKeep = new ArrayList<CanonicalType>();

							for (CanonicalType resourceProfile : resourceMeta.getProfile()) {

								keepInputProfile = true;
								if (inputMeta.hasProfile()) {
									for (CanonicalType inputProfile : inputMeta.getProfile()) {
										if (resourceProfile.getValue().equals(inputProfile.getValue())) {
											keepInputProfile = false;
											break;
										}
									}
								}

								if (keepInputProfile) {
									resourceProfileToKeep.add(resourceProfile);
								}
							}

							resourceMeta.getProfile().clear();
							if (!resourceProfileToKeep.isEmpty()) {
								resourceMeta.getProfile().addAll(resourceProfileToKeep);
							}
						}

						// Delete Security
						if (resourceMeta.hasSecurity()) {

							boolean keepInputSecurity = true;
							List<Coding> resourceSecurityToKeep = new ArrayList<Coding>();

							for (Coding resourceSecurity : resourceMeta.getSecurity()) {

								keepInputSecurity = true;
								if (inputMeta.hasSecurity()) {
									for (Coding inputSecurity : inputMeta.getSecurity()) {
										if (inputSecurity.equalsDeep(resourceSecurity)) {
											keepInputSecurity = false;
											break;
										}
									}
								}

								if (keepInputSecurity) {
									resourceSecurityToKeep.add(resourceSecurity);
								}
							}

							resourceMeta.getSecurity().clear();
							if (!resourceSecurityToKeep.isEmpty()) {
								resourceMeta.getSecurity().addAll(resourceSecurityToKeep);
							}
						}

						// Add Tags
						if (resourceMeta.hasTag()) {

							boolean keepInputTag = true;
							List<Coding> resourceTagToKeep = new ArrayList<Coding>();

							for (Coding resourceTag : inputMeta.getTag()) {

								keepInputTag = true;
								if (inputMeta.hasTag()) {
									for (Coding inputTag : inputMeta.getTag()) {
										if (inputTag.equalsDeep(resourceTag)) {
											keepInputTag = false;
											break;
										}
									}
								}

								if (keepInputTag) {
									resourceTagToKeep.add(resourceTag);
								}
							}

							resourceMeta.getTag().clear();
							if (!resourceTagToKeep.isEmpty()) {
								resourceMeta.getTag().addAll(resourceTagToKeep);
							}
						}
					}
				}
			}

			if (!resourceMeta.hasProfile() && !resourceMeta.hasSecurity() && !resourceMeta.hasTag()) {
				// Populate meta.id with dummy value for conformance if no meta found
				resourceMeta.setId(UUIDUtil.getUUID());
			}

			parameter.setValue(resourceMeta);

			// Update the resource without increasing the version id
			net.aegis.fhir.model.Resource updateResource = resourceContainer.getResource();

			Meta updateMeta = resource.getMeta();
			updateMeta.getProfile().clear();
			if (resourceMeta.hasProfile()) {
				updateMeta.getProfile().addAll(resourceMeta.getProfile());
			}
			updateMeta.getSecurity().clear();
			if (resourceMeta.hasSecurity()) {
				updateMeta.getSecurity().addAll(resourceMeta.getSecurity());
			}
			updateMeta.getTag().clear();
			if (resourceMeta.hasTag()) {
				updateMeta.getTag().addAll(resourceMeta.getTag());
			}

			// Convert the Resource to XML byte[]
			ByteArrayOutputStream oResource = new ByteArrayOutputStream();
			xmlP.setOutputStyle(OutputStyle.PRETTY);
			xmlP.compose(oResource, resource, true);
			byte[] bResource = oResource.toByteArray();

			updateResource.setResourceContents(bResource);

			this.resourceService.updateOnly(updateResource, baseUrl);
		}

		return out;
	}

}
