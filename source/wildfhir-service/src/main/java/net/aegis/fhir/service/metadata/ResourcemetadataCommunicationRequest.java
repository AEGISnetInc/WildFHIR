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
        ByteArrayInputStream iCommunicationRequest = null;

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

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, communicationRequest, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", communicationRequest.getId());
			resourcemetadataList.add(_id);

			// _language : token
			if (communicationRequest.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", communicationRequest.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (communicationRequest.getMeta() != null && communicationRequest.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(communicationRequest.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(communicationRequest.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// authored : date
			if (communicationRequest.hasAuthoredOn()) {
				Resourcemetadata rAuthoredOn = generateResourcemetadata(resource, chainedResource, chainedParameter+"authored", utcDateUtil.formatDate(communicationRequest.getAuthoredOn(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(communicationRequest.getAuthoredOn(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rAuthoredOn);
			}

			// based-on : reference
			if (communicationRequest.hasBasedOn()) {

				String basedOnReference = null;
				List<Resourcemetadata> rBasedOnChain = null;
				for (Reference basedOn : communicationRequest.getBasedOn()) {

					if (basedOn.hasReference()) {
						basedOnReference = generateFullLocalReference(basedOn.getReference(), baseUrl);

						Resourcemetadata rBasedOn = generateResourcemetadata(resource, chainedResource, chainedParameter+"based-on", basedOnReference);
						resourcemetadataList.add(rBasedOn);

						if (chainedResource == null) {
							// Add chained parameters for any
							rBasedOnChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "based-on", 0, basedOn.getReference());
							resourcemetadataList.addAll(rBasedOnChain);
						}
					}
				}
			}

			// category : token
			if (communicationRequest.hasCategory()) {

				Resourcemetadata rCode = null;
				for (CodeableConcept category : communicationRequest.getCategory()) {

					if (category.hasCoding()) {
						for (Coding code : category.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"category", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// encounter : reference
			if (communicationRequest.hasEncounter() && communicationRequest.getEncounter().hasReference()) {
				String encounterReference = generateFullLocalReference(communicationRequest.getEncounter().getReference(), baseUrl);

				Resourcemetadata rEncounter = generateResourcemetadata(resource, chainedResource, chainedParameter+"encounter", encounterReference);
				resourcemetadataList.add(rEncounter);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rEncounterChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "encounter", 0, communicationRequest.getEncounter().getReference());
					resourcemetadataList.addAll(rEncounterChain);
				}
			}

			// group-identifier : token
			if (communicationRequest.hasGroupIdentifier()) {
				Resourcemetadata rGroupIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"group-identifier", communicationRequest.getGroupIdentifier().getValue(), communicationRequest.getGroupIdentifier().getSystem(), null, ServicesUtil.INSTANCE.getTextValue(communicationRequest.getGroupIdentifier()));
				resourcemetadataList.add(rGroupIdentifier);
			}

			// identifier : token
			if (communicationRequest.hasIdentifier()) {

				for (Identifier identifier : communicationRequest.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// medium : token
			if (communicationRequest.hasMedium()) {

				Resourcemetadata rCode = null;
				for (CodeableConcept medium : communicationRequest.getMedium()) {

					if (medium.hasCoding()) {
						for (Coding code : medium.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"medium", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// occurrence : datetime
			if (communicationRequest.hasOccurrenceDateTimeType()) {
				Resourcemetadata rOccurrence = generateResourcemetadata(resource, chainedResource, chainedParameter+"occurrence", utcDateUtil.formatDate(communicationRequest.getOccurrenceDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(communicationRequest.getOccurrenceDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rOccurrence);
			}

			// priority : token
			if (communicationRequest.hasPriority() && communicationRequest.getPriority() != null) {
				Resourcemetadata rPriority = generateResourcemetadata(resource, chainedResource, chainedParameter+"priority", communicationRequest.getPriority().toCode(), communicationRequest.getPriority().getSystem());
				resourcemetadataList.add(rPriority);
			}

			// recipient : reference
			if (communicationRequest.hasRecipient()) {

				String recipientReference = null;
				for (Reference recipient : communicationRequest.getRecipient()) {

					if (recipient.hasReference()) {
						recipientReference = generateFullLocalReference(recipient.getReference(), baseUrl);

						Resourcemetadata rRecipient = generateResourcemetadata(resource, chainedResource, chainedParameter+"recipient", recipientReference);
						resourcemetadataList.add(rRecipient);

						if (chainedResource == null) {
							// Add chained parameters for any
							List<Resourcemetadata> rRecipientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "recipient", 0, recipient.getReference());
							resourcemetadataList.addAll(rRecipientChain);
						}
					}
				}
			}

			// replaces : reference
			if (communicationRequest.hasReplaces()) {

				String replacesReference = null;
				for (Reference replaces : communicationRequest.getReplaces()) {

					if (replaces.hasReference()) {
						replacesReference = generateFullLocalReference(replaces.getReference(), baseUrl);

						Resourcemetadata rReplaces = generateResourcemetadata(resource, chainedResource, chainedParameter+"replaces", replacesReference);
						resourcemetadataList.add(rReplaces);

						if (chainedResource == null) {
							// Add chained parameters
							List<Resourcemetadata> rReplacesChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "replaces", 0, replaces.getReference());
							resourcemetadataList.addAll(rReplacesChain);
						}
					}
				}
			}

			// requester : reference
			if (communicationRequest.hasRequester() && communicationRequest.getRequester().hasReference()) {
				String requesterReference = generateFullLocalReference(communicationRequest.getRequester().getReference(), baseUrl);

				Resourcemetadata rRequester = generateResourcemetadata(resource, chainedResource, chainedParameter+"requester", requesterReference);
				resourcemetadataList.add(rRequester);

				if (chainedResource == null) {
					// Add chained parameters for any
					List<Resourcemetadata> rRequesterChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "requester", 0, communicationRequest.getRequester().getReference());
					resourcemetadataList.addAll(rRequesterChain);
				}
			}

			// sender : reference
			if (communicationRequest.hasSender() && communicationRequest.getSender().hasReference()) {
				String senderReference = generateFullLocalReference(communicationRequest.getSender().getReference(), baseUrl);

				Resourcemetadata rSender = generateResourcemetadata(resource, chainedResource, chainedParameter+"sender", senderReference);
				resourcemetadataList.add(rSender);

				if (chainedResource == null) {
					// Add chained parameters for any
					List<Resourcemetadata> rSenderChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "sender", 0, communicationRequest.getSender().getReference());
					resourcemetadataList.addAll(rSenderChain);
				}
			}

			// patient : reference
			// subject : reference
			if (communicationRequest.hasSubject() && communicationRequest.getSubject().hasReference()) {
				String subjectReference = generateFullLocalReference(communicationRequest.getSubject().getReference(), baseUrl);

				Resourcemetadata rSubject = generateResourcemetadata(resource, chainedResource, chainedParameter+"subject", subjectReference);
				resourcemetadataList.add(rSubject);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rSubjectChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "subject", 0, communicationRequest.getSubject().getReference());
					resourcemetadataList.addAll(rSubjectChain);
				}

				if (subjectReference.indexOf("Patient") >= 0) {
					Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", subjectReference);
					resourcemetadataList.add(rPatient);

					if (chainedResource == null) {
						// Add chained parameters
						List<Resourcemetadata> rPatientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, communicationRequest.getSubject().getReference());
						resourcemetadataList.addAll(rPatientChain);
					}
				}
			}

			// status : token
			if (communicationRequest.hasStatus() && communicationRequest.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", communicationRequest.getStatus().toCode(), communicationRequest.getStatus().getSystem());
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
