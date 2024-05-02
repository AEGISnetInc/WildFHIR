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
package net.aegis.fhir.service.linked;

import java.io.ByteArrayInputStream;
import java.util.List;

import javax.ws.rs.core.Response;

import net.aegis.fhir.model.ResourceContainer;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;

import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.Resource;

/**
 * @author richard.ettema
 *
 */
public abstract class LinkedResourceProxy {

	protected XmlParser xmlP = new XmlParser();

	/**
	 * Execute the logic to return a List of linked or referenced Resources within the container Resource
	 *
	 * @param resourceService
	 * @param containerResource
	 * @return List of linked or referenced Resources
	 * @throws Exception
	 */
	public abstract List<Resource> getLinkedResources(ResourceService resourceService, Resource containerResource) throws Exception;

	/**
	 *
	 * @param resourceService
	 * @param ref
	 * @param resourceType
	 * @return Resource if found, null if not
	 * @throws Exception
	 */
	protected Resource getLinkedResource(ResourceService resourceService, String ref, String resourceType) throws Exception {

		Resource linkedResource = null;

		String resourceId = ServicesUtil.INSTANCE.extractResourceIdFromURL(ref);

		ResourceContainer resourceContainer = resourceService.read(resourceType, resourceId, null);

		// If linked Resource is found, add to list of linked Resources
		if (resourceContainer.getResponseStatus().equals(Response.Status.OK)) {

			// Convert XML contents to Resource object
			ByteArrayInputStream iResource = new ByteArrayInputStream(resourceContainer.getResource().getResourceContents());
			linkedResource = xmlP.parse(iResource);
		}

		return linkedResource;
	}

	/**
	 * @param resourceService
	 * @param ref
	 * @return Resource if found, null if not
	 * @throws Exception
	 */
	protected Resource getLinkedResourceAny(ResourceService resourceService, String ref) throws Exception {

		Resource linkedResource = null;

		String resourceType = ServicesUtil.INSTANCE.getResourceTypeFromReference(ref);
		String resourceId = ServicesUtil.INSTANCE.extractResourceIdFromURL(ref);

		ResourceContainer resourceContainer = resourceService.read(resourceType, resourceId, null);

		// If linked Resource is found, add to list of linked Resources
		if (resourceContainer.getResponseStatus().equals(Response.Status.OK)) {

			// Convert XML contents to Resource object
			ByteArrayInputStream iResource = new ByteArrayInputStream(resourceContainer.getResource().getResourceContents());
			linkedResource = xmlP.parse(iResource);
		}

		return linkedResource;
	}

}
