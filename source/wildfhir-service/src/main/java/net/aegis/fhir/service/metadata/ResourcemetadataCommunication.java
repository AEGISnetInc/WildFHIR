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

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, communication, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			if (communication.getId() != null) {
				Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", communication.getId());
				resourcemetadataList.add(_id);
			}

			// _language : token
			if (communication.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", communication.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (communication.getMeta() != null && communication.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(communication.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(communication.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// based-on : reference
			if (communication.hasBasedOn()) {

				String basedOnReference = null;
				List<Resourcemetadata> rBasedOnChain = null;
				for (Reference basedOn : communication.getBasedOn()) {

					if (basedOn.hasReference()) {
						basedOnReference = generateFullLocalReference(basedOn.getReference(), baseUrl);

						Resourcemetadata rBasedOn = generateResourcemetadata(resource, chainedResource, chainedParameter+"based-on", basedOnReference);
						resourcemetadataList.add(rBasedOn);

						if (chainedResource == null) {
							// Add chained parameters for any
							rBasedOnChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "based-on", 0, basedOn.getReference(), null);
							resourcemetadataList.addAll(rBasedOnChain);
						}
					}
				}
			}

			// category : token
			if (communication.hasCategory()) {

				Resourcemetadata rCode = null;
				for (CodeableConcept category : communication.getCategory()) {

					if (category.hasCoding()) {
						for (Coding code : category.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"category", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// encounter : reference
			if (communication.hasEncounter() && communication.getEncounter().hasReference()) {
				String encounterReference = generateFullLocalReference(communication.getEncounter().getReference(), baseUrl);

				Resourcemetadata rEncounter = generateResourcemetadata(resource, chainedResource, chainedParameter+"encounter", encounterReference);
				resourcemetadataList.add(rEncounter);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rEncounterChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "encounter", 0, communication.getEncounter().getReference(), null);
					resourcemetadataList.addAll(rEncounterChain);
				}
			}

			// identifier : token
			if (communication.hasIdentifier()) {

				for (Identifier identifier : communication.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// instantiates-canonical : reference
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

					Resourcemetadata rInstantiates = generateResourcemetadata(resource, chainedResource, chainedParameter+"instantiates-uri", instantiates.asStringValue());
					resourcemetadataList.add(rInstantiates);
				}
			}

			// medium : token
			if (communication.hasMedium()) {

				Resourcemetadata rCode = null;
				for (CodeableConcept medium : communication.getMedium()) {

					if (medium.hasCoding()) {
						for (Coding code : medium.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"medium", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// part-of : reference
			if (communication.hasPartOf()) {

				String partOfReference = null;
				List<Resourcemetadata> rPartOfChain = null;
				for (Reference partOf : communication.getPartOf()) {

					if (partOf.hasReference()) {
						partOfReference = generateFullLocalReference(partOf.getReference(), baseUrl);

						Resourcemetadata rPartOf = generateResourcemetadata(resource, chainedResource, chainedParameter+"part-of", partOfReference);
						resourcemetadataList.add(rPartOf);

						if (chainedResource == null) {
							// Add chained parameters for any
							rPartOfChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "part-of", 0, partOf.getReference(), null);
							resourcemetadataList.addAll(rPartOfChain);
						}
					}
				}
			}

			// patient : reference
			// subject : reference
			if (communication.hasSubject() && communication.getSubject().hasReference()) {
				String subjectReference = generateFullLocalReference(communication.getSubject().getReference(), baseUrl);

				Resourcemetadata rSubject = generateResourcemetadata(resource, chainedResource, chainedParameter+"subject", subjectReference);
				resourcemetadataList.add(rSubject);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rSubjectChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "subject", 0, communication.getSubject().getReference(), null);
					resourcemetadataList.addAll(rSubjectChain);
				}

				if (subjectReference.indexOf("Patient") >= 0) {
					Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", subjectReference);
					resourcemetadataList.add(rPatient);

					if (chainedResource == null) {
						// Add chained parameters
						List<Resourcemetadata> rPatientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, communication.getSubject().getReference(), null);
						resourcemetadataList.addAll(rPatientChain);
					}
				}
			}

			// received : date
			if (communication.hasReceived()) {
				Resourcemetadata rReceived = generateResourcemetadata(resource, chainedResource, chainedParameter+"received", utcDateUtil.formatDate(communication.getReceived(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(communication.getReceived(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rReceived);
			}

			// recipient : reference
			if (communication.hasRecipient()) {

				String recipientReference = null;
				for (Reference recipient : communication.getRecipient()) {

					if (recipient.hasReference()) {
						recipientReference = generateFullLocalReference(recipient.getReference(), baseUrl);

						Resourcemetadata rRecipient = generateResourcemetadata(resource, chainedResource, chainedParameter+"recipient", recipientReference);
						resourcemetadataList.add(rRecipient);

						if (chainedResource == null) {
							// Add chained parameters for any
							List<Resourcemetadata> rRecipientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "recipient", 0, recipient.getReference(), null);
							resourcemetadataList.addAll(rRecipientChain);
						}
					}
				}
			}

			// sender : reference
			if (communication.hasSender() && communication.getSender().hasReference()) {
				String senderReference = generateFullLocalReference(communication.getSender().getReference(), baseUrl);

				Resourcemetadata rSender = generateResourcemetadata(resource, chainedResource, chainedParameter+"sender", senderReference);
				resourcemetadataList.add(rSender);

				if (chainedResource == null) {
					// Add chained parameters for any
					List<Resourcemetadata> rSenderChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "sender", 0, communication.getSender().getReference(), null);
					resourcemetadataList.addAll(rSenderChain);
				}
			}

			// sent : date
			if (communication.hasSent()) {
				Resourcemetadata rSent = generateResourcemetadata(resource, chainedResource, chainedParameter+"sent", utcDateUtil.formatDate(communication.getSent(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(communication.getSent(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rSent);
			}

			// status : token
			if (communication.hasStatus() && communication.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", communication.getStatus().toCode(), communication.getStatus().getSystem());
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
