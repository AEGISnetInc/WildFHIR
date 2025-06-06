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

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.CommunicationRequest;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataCommunicationRequest extends ResourcemetadataProxy {

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
        ByteArrayInputStream iCommunicationRequest = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

		try {
            // Extract and convert the resource contents to a CommunicationRequest object
			if (chainedResource != null) {
				iCommunicationRequest = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iCommunicationRequest = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            CommunicationRequest communicationRequest = (CommunicationRequest) xmlP.parse(iCommunicationRequest);
            iCommunicationRequest.close();

			/*
             * Create new Resourcemetadata objects for each CommunicationRequest metadata value and add to the resourcemetadataList
			 */

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, communicationRequest, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// authored : date
			if (communicationRequest.hasAuthoredOn()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"authored", utcDateUtil.formatDate(communicationRequest.getAuthoredOn(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(communicationRequest.getAuthoredOn(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rMetadata);
			}

			// based-on : reference
			if (communicationRequest.hasBasedOn()) {

				for (Reference basedOn : communicationRequest.getBasedOn()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "based-on", 0, basedOn, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// category : token
			if (communicationRequest.hasCategory()) {

				for (CodeableConcept category : communicationRequest.getCategory()) {

					if (category.hasCoding()) {
						for (Coding code : category.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"category", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// encounter : reference
			if (communicationRequest.hasEncounter() && communicationRequest.getEncounter().hasReference()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "encounter", 0, communicationRequest.getEncounter(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// group-identifier : token
			if (communicationRequest.hasGroupIdentifier()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"group-identifier", communicationRequest.getGroupIdentifier().getValue(), communicationRequest.getGroupIdentifier().getSystem(), null, ServicesUtil.INSTANCE.getTextValue(communicationRequest.getGroupIdentifier()));
				resourcemetadataList.add(rMetadata);
			}

			// identifier : token
			if (communicationRequest.hasIdentifier()) {

				for (Identifier identifier : communicationRequest.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}

			// medium : token
			if (communicationRequest.hasMedium()) {

				for (CodeableConcept medium : communicationRequest.getMedium()) {
					if (medium.hasCoding()) {

						for (Coding code : medium.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"medium", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// occurrence : datetime
			if (communicationRequest.hasOccurrenceDateTimeType()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"occurrence", utcDateUtil.formatDate(communicationRequest.getOccurrenceDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(communicationRequest.getOccurrenceDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rMetadata);
			}

			// priority : token
			if (communicationRequest.hasPriority() && communicationRequest.getPriority() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"priority", communicationRequest.getPriority().toCode(), communicationRequest.getPriority().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// recipient : reference
			if (communicationRequest.hasRecipient()) {

				for (Reference recipient : communicationRequest.getRecipient()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "recipient", 0, recipient, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// replaces : reference
			if (communicationRequest.hasReplaces()) {

				for (Reference replaces : communicationRequest.getReplaces()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "replaces", 0, replaces, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// requester : reference
			if (communicationRequest.hasRequester()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "requester", 0, communicationRequest.getRequester(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// sender : reference
			if (communicationRequest.hasSender() && communicationRequest.getSender().hasReference()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "sender", 0, communicationRequest.getSender(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// subject : reference
			if (communicationRequest.hasSubject()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "subject", 0, communicationRequest.getSubject(), null);
				resourcemetadataList.addAll(rMetadataChain);

				// patient : reference
				if ((communicationRequest.getSubject().hasReference() && communicationRequest.getSubject().getReference().indexOf("Patient") >= 0)
						|| (communicationRequest.getSubject().hasType() && communicationRequest.getSubject().getType().equals("Patient"))) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "patient", 0, communicationRequest.getSubject(), null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// status : token
			if (communicationRequest.hasStatus() && communicationRequest.getStatus() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", communicationRequest.getStatus().toCode(), communicationRequest.getStatus().getSystem());
				resourcemetadataList.add(rMetadata);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		} finally {
	        rMetadata = null;
	        rMetadataChain = null;
            if (iCommunicationRequest != null) {
                try {
                	iCommunicationRequest.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
