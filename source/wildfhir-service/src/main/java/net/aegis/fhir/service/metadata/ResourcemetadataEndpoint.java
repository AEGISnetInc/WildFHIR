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
package net.aegis.fhir.service.metadata;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Identifier;

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataEndpoint extends ResourcemetadataProxy {

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.metadata.ResourcemetadataProxy#generateAllForResource(net.aegis.fhir.model.Resource, java.lang.String, net.aegis.fhir.service.ResourceService)
	 */
	@Override
	public List<Resourcemetadata> generateAllForResource(Resource resource, String baseUrl, ResourceService resourceService) throws Exception {
		return generateAllForResource(resource, baseUrl, resourceService, null, null, 0);
	}

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.metadata.ResourcemetadataProxy#generateAllForResource(net.aegis.fhir.model.Resource, java.lang.String, net.aegis.fhir.service.ResourceService, net.aegis.fhir.model.Resource, java.lang.String, int)
	 */
	@Override
	public List<Resourcemetadata> generateAllForResource(Resource resource, String baseUrl, ResourceService resourceService, Resource chainedResource, String chainedParameter, int chainedIndex) throws Exception {

		if (StringUtils.isEmpty(chainedParameter)) {
			chainedParameter = "";
		}

		List<Resourcemetadata> resourcemetadataList = new ArrayList<Resourcemetadata>();
        ByteArrayInputStream iEndpoint = null;

		try {
			// Extract and convert the resource contents to a Endpoint object
			if (chainedResource != null) {
				iEndpoint = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iEndpoint = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			Endpoint endpoint = (Endpoint) xmlP.parse(iEndpoint);
			iEndpoint.close();

			/*
			 * Create new Resourcemetadata objects for each Endpoint metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, endpoint, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", endpoint.getId());
			resourcemetadataList.add(_id);

			// _language : token
			if (endpoint.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", endpoint.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (endpoint.getMeta() != null && endpoint.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(endpoint.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(endpoint.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// connection-type : token
			if (endpoint.hasConnectionType()) {
				Resourcemetadata rConnectionType = generateResourcemetadata(resource, chainedResource, chainedParameter+"connection-type", endpoint.getConnectionType().getCode(), endpoint.getConnectionType().getSystem());
				resourcemetadataList.add(rConnectionType);
			}

			// identifier : token
			if (endpoint.hasIdentifier()) {

				Resourcemetadata rIdentifier = null;
				for (Identifier identifier : endpoint.getIdentifier()) {

					rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// name : string
			if (endpoint.hasName()) {
				Resourcemetadata rName = generateResourcemetadata(resource, chainedResource, chainedParameter+"name", endpoint.getName());
				resourcemetadataList.add(rName);
			}

			// organization : reference
			if (endpoint.hasManagingOrganization()) {
				Resourcemetadata rOrganization = generateResourcemetadata(resource, chainedResource, chainedParameter+"organization", generateFullLocalReference(endpoint.getManagingOrganization().getReference(), baseUrl));
				resourcemetadataList.add(rOrganization);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rOrganizationChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "organization", 0, endpoint.getManagingOrganization().getReference());
					resourcemetadataList.addAll(rOrganizationChain);
				}
			}

			// payload-type : token
			if (endpoint.hasPayloadType()) {

				Resourcemetadata rType = null;
				for (CodeableConcept payloadType : endpoint.getPayloadType()) {

					if (payloadType.hasCoding()) {
						for (Coding type : payloadType.getCoding()) {
							rType = generateResourcemetadata(resource, chainedResource, chainedParameter+"payload-type", type.getCode(), type.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(type));
							resourcemetadataList.add(rType);
						}
					}
				}
			}

			// status : token
			if (endpoint.hasStatus() && endpoint.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", endpoint.getStatus().toCode(), endpoint.getStatus().getSystem());
				resourcemetadataList.add(rStatus);
			}


		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return resourcemetadataList;
	}

}
