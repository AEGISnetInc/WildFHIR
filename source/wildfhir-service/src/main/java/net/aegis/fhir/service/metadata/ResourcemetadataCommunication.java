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
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Communication;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.UriType;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataCommunication extends ResourcemetadataProxy {

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
        ByteArrayInputStream iCommunication = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

		try {
            // Extract and convert the resource contents to a Communication object
			if (chainedResource != null) {
				iCommunication = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iCommunication = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            Communication communication = (Communication) xmlP.parse(iCommunication);
            iCommunication.close();

			/*
             * Create new Resourcemetadata objects for each Communication metadata value and add to the resourcemetadataList
			 */

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, communication, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// based-on : reference
			if (communication.hasBasedOn()) {

				for (Reference basedOn : communication.getBasedOn()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "based-on", 0, basedOn, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// category : token
			if (communication.hasCategory()) {

				for (CodeableConcept category : communication.getCategory()) {

					if (category.hasCoding()) {
						for (Coding code : category.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"category", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// encounter : reference
			if (communication.hasEncounter()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "encounter", 0, communication.getEncounter(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// identifier : token
			if (communication.hasIdentifier()) {

				for (Identifier identifier : communication.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}

			// instantiates-canonical : reference - instantiates is a Canonical, no Reference.identifier
			if (communication.hasInstantiatesCanonical()) {

				for (CanonicalType instantiates : communication.getInstantiatesCanonical()) {
					String objectReference = generateFullLocalReference(instantiates.asStringValue(), baseUrl);

					List<Resourcemetadata> rInstantiatesChain = null;
					Resourcemetadata rReference = generateResourcemetadata(resource, chainedResource, chainedParameter+"instantiates-canonical", objectReference);
					resourcemetadataList.add(rReference);

					if (chainedResource == null) {
						// Add chained parameters
						rInstantiatesChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "instantiates-canonical", 0, instantiates.asStringValue(), null);
						resourcemetadataList.addAll(rInstantiatesChain);
					}
				}
			}

			// instantiates-uri : uri
			if (communication.hasInstantiatesUri()) {

				for (UriType instantiates : communication.getInstantiatesUri()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"instantiates-uri", instantiates.asStringValue());
					resourcemetadataList.add(rMetadata);
				}
			}

			// medium : token
			if (communication.hasMedium()) {

				for (CodeableConcept medium : communication.getMedium()) {

					if (medium.hasCoding()) {
						for (Coding code : medium.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"medium", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// part-of : reference
			if (communication.hasPartOf()) {

				for (Reference partOf : communication.getPartOf()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "part-of", 0, partOf, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// subject : reference
			if (communication.hasSubject()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "subject", 0, communication.getSubject(), null);
				resourcemetadataList.addAll(rMetadataChain);

				// patient : reference
				if ((communication.getSubject().hasReference() && communication.getSubject().getReference().indexOf("Patient") >= 0)
						|| (communication.getSubject().hasType() && communication.getSubject().getType().equals("Patient"))) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "patient", 0, communication.getSubject(), null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// received : date
			if (communication.hasReceived()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"received", utcDateUtil.formatDate(communication.getReceived(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(communication.getReceived(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rMetadata);
			}

			// recipient : reference
			if (communication.hasRecipient()) {

				for (Reference recipient : communication.getRecipient()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "recipient", 0, recipient, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// sender : reference
			if (communication.hasSender()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "sender", 0, communication.getSender(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// sent : date
			if (communication.hasSent()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"sent", utcDateUtil.formatDate(communication.getSent(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(communication.getSent(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rMetadata);
			}

			// status : token
			if (communication.hasStatus() && communication.getStatus() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", communication.getStatus().toCode(), communication.getStatus().getSystem());
				resourcemetadataList.add(rMetadata);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		} finally {
	        rMetadata = null;
	        rMetadataChain = null;
            if (iCommunication != null) {
                try {
                	iCommunication.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
