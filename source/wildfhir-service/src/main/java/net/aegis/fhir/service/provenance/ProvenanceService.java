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
package net.aegis.fhir.service.provenance;

import java.util.logging.Logger;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.Response.Status;

import org.hl7.fhir.r4.model.Identifier;

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.ResourceContainer;
import net.aegis.fhir.service.CodeService;
import net.aegis.fhir.service.ResourceService;

/**
 * @author Venkat.Keesara
 *
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class ProvenanceService {

	private Logger log = Logger.getLogger(getClass().getName());

	@Inject
	CodeService codeService;

	@Inject
	ResourceService resourceService;

	/**
	 * @param context
	 * @param headers
	 * @param payload
	 * @param resourceType
	 * @param locationPath
	 * @param resourceId
	 * @param operation
	 * @throws Exception
	 */
	public void createProvenance(UriInfo context, HttpHeaders headers, String payload, String resourceType, String locationPath, String resourceId, Identifier identifier, String operation) throws Exception {

		Resource resource = null;
		ResourceContainer resCon = null;
		String baseUrl = null;

		try {
			if (codeService.isSupported("provenanceServiceEnabled")) {
				resource = ProvenanceServiceUtil.INSTANCE.generateProvenance(context, headers, payload, resourceType, locationPath, resourceId, identifier, operation);

				// Get server base url from code table configuration
				baseUrl = codeService.getCodeValue("baseUrl");

				resCon = resourceService.create(resource, null, baseUrl);

				if (resCon.getResponseStatus().equals(Status.CREATED)) {
					log.fine("ProvenanceService.createProvenance() - Provenance/" + resCon.getResource().getResourceId() + " successfully created.");
				}
				else {
					throw new Exception("Error attempting to create Provenance! " +
							resCon.getResponseStatus().getStatusCode() + " " + resCon.getResponseStatus().toString() + "" + (resCon.getMessage() != null ? resCon.getMessage() : ""));
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			// Release resources for garbage collection
			resource = null;
			resCon = null;
			baseUrl = null;
		}

	}

}
