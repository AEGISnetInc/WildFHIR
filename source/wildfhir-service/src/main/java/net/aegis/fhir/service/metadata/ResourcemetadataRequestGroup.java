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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.RequestGroup;
import org.hl7.fhir.r4.model.RequestGroup.RequestGroupActionComponent;
import org.hl7.fhir.r4.model.UriType;

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataRequestGroup extends ResourcemetadataProxy {

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.metadata.ResourcemetadataProxy#generateAllForResource(net.aegis.fhir.model.Resource, java.lang.String, net.aegis.fhir.service.ResourceService)
	 */
	@Override
	public List<Resourcemetadata> generateAllForResource(Resource resource, String baseUrl, ResourceService resourceService) throws Exception {
		return generateAllForResource(resource, baseUrl, resourceService, null, null, 0, null);
	}

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.metadata.ResourcemetadataProxy#generateAllForResource(net.aegis.fhir.model.Resource, java.lang.String, net.aegis.fhir.service.ResourceService, net.aegis.fhir.model.Resource, java.lang.String, int, org.hl7.fhir.r4.model.Resource)
	 */
	@Override
	public List<Resourcemetadata> generateAllForResource(Resource resource, String baseUrl, ResourceService resourceService, Resource chainedResource, String chainedParameter, int chainedIndex, org.hl7.fhir.r4.model.Resource fhirResource) throws Exception {

		if (StringUtils.isEmpty(chainedParameter)) {
			chainedParameter = "";
		}

		List<Resourcemetadata> resourcemetadataList = new ArrayList<Resourcemetadata>();
        ByteArrayInputStream iRequestGroup = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

		try {
            // Extract and convert the resource contents to a RequestGroup object
			if (chainedResource != null) {
				iRequestGroup = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iRequestGroup = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            RequestGroup requestGroup = (RequestGroup) xmlP.parse(iRequestGroup);
            iRequestGroup.close();

			/*
             * Create new Resourcemetadata objects for each RequestGroup metadata value and add to the resourcemetadataList
			 */

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, requestGroup, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// author : reference
			if (requestGroup.hasAuthor()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "author", 0, requestGroup.getAuthor(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// authored : date
			if (requestGroup.hasAuthoredOn()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"authored", utcDateUtil.formatDate(requestGroup.getAuthoredOn(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(requestGroup.getAuthoredOn(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rMetadata);
			}

			// code : token
			if (requestGroup.hasCode() && requestGroup.getCode().hasCoding()) {

				for (Coding code : requestGroup.getCode().getCoding()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rMetadata);
				}
			}

			// encounter : reference
			if (requestGroup.hasEncounter() && requestGroup.getEncounter().hasReference()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "encounter", 0, requestGroup.getEncounter(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// group-identifier : token
			if (requestGroup.hasGroupIdentifier()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"group-identifier", requestGroup.getGroupIdentifier().getValue(), requestGroup.getGroupIdentifier().getSystem(), null, ServicesUtil.INSTANCE.getTextValue(requestGroup.getGroupIdentifier()));
				resourcemetadataList.add(rMetadata);
			}

			// identifier : token
			if (requestGroup.hasIdentifier()) {

				for (Identifier identifier : requestGroup.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}

			// instantiates-canonical : canonical - instantiates is a Canonical, no Reference.identifier
			if (requestGroup.hasInstantiatesCanonical()) {

				for (CanonicalType instantiatesCanonical : requestGroup.getInstantiatesCanonical()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"instantiates-canonical", instantiatesCanonical.getValue());
					resourcemetadataList.add(rMetadata);

					if (chainedResource == null) {
						// Add chained parameters for any
						rMetadataChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "instantiates-canonical", 0, instantiatesCanonical.getValue(), null);
						resourcemetadataList.addAll(rMetadataChain);
					}
				}
			}

			// instantiates-uri : uri
			if (requestGroup.hasInstantiatesUri()) {

				for (UriType instantiatesUri : requestGroup.getInstantiatesUri()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"instantiates-uri", instantiatesUri.getValue());
					resourcemetadataList.add(rMetadata);
				}
			}

			// intent : token
			if (requestGroup.hasIntent() && requestGroup.getIntent() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"intent", requestGroup.getIntent().toCode(), requestGroup.getIntent().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// participant : reference
			if (requestGroup.hasAction()) {
				for (RequestGroupActionComponent action : requestGroup.getAction()) {

					if (action.hasParticipant()) {
						for (Reference participant : action.getParticipant()) {
							rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "participant", 0, participant, null);
							resourcemetadataList.addAll(rMetadataChain);
						}
					}
				}
			}

			// subject : reference
			if (requestGroup.hasSubject()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "subject", 0, requestGroup.getSubject(), null);
				resourcemetadataList.addAll(rMetadataChain);

				// patient : reference
				if ((requestGroup.getSubject().hasReference() && requestGroup.getSubject().getReference().indexOf("Patient") >= 0)
						|| (requestGroup.getSubject().hasType() && requestGroup.getSubject().getType().equals("Patient"))) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "patient", 0, requestGroup.getSubject(), null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// priority : token
			if (requestGroup.hasPriority() && requestGroup.getPriority() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"priority", requestGroup.getPriority().toCode(), requestGroup.getPriority().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// status : token
			if (requestGroup.hasStatus() && requestGroup.getStatus() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", requestGroup.getStatus().toCode(), requestGroup.getStatus().getSystem());
				resourcemetadataList.add(rMetadata);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		} finally {
	        rMetadata = null;
	        rMetadataChain = null;
            if (iRequestGroup != null) {
                try {
                	iRequestGroup.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
