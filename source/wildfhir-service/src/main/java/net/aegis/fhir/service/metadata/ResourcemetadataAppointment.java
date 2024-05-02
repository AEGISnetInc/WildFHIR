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
import org.hl7.fhir.r4.model.Appointment;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Appointment.AppointmentParticipantComponent;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataAppointment extends ResourcemetadataProxy {

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
        ByteArrayInputStream iAppointment = null;

		try {
            // Extract and convert the resource contents to a Appointment object
			if (chainedResource != null) {
				iAppointment = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iAppointment = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            Appointment appointment = (Appointment) xmlP.parse(iAppointment);
            iAppointment.close();

			/*
             * Create new Resourcemetadata objects for each Appointment metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, appointment, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", appointment.getId());
			resourcemetadataList.add(_id);

			// _language : token
			if (appointment.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", appointment.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (appointment.getMeta() != null && appointment.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(appointment.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(appointment.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// appointment-type : token
			if (appointment.hasAppointmentType() && appointment.getAppointmentType().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : appointment.getAppointmentType().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"appointment-type", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			// based-on : reference
			if (appointment.hasBasedOn()) {

				String basedOnReference = null;
				Resourcemetadata rBasedOn = null;
				List<Resourcemetadata> rBasedOnChain = null;
				for (Reference basedOn : appointment.getBasedOn()) {

					if (basedOn.hasReference()) {
						basedOnReference = generateFullLocalReference(basedOn.getReference(), baseUrl);

						rBasedOn = generateResourcemetadata(resource, chainedResource, chainedParameter+"based-on", basedOnReference);
						resourcemetadataList.add(rBasedOn);

						if (chainedResource == null) {
							// Add chained parameters
							rBasedOnChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "based-on", 0, basedOn.getReference());
							resourcemetadataList.addAll(rBasedOnChain);
						}
					}
				}
			}

			// date : date
			if (appointment.hasStart()) {
				Resourcemetadata rStart = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(appointment.getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(appointment.getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rStart);
			}

			// identifier : token
			if (appointment.hasIdentifier()) {

				for (Identifier identifier : appointment.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// participant
			if (appointment.hasParticipant()) {

				Resourcemetadata rParticipant = null;
				List<Resourcemetadata> rParticipantChain = null;
				for (AppointmentParticipantComponent participant : appointment.getParticipant()) {

					// participant.partstatus : token
					if (participant.hasStatus() && participant.getStatus() != null) {
						rParticipant = generateResourcemetadata(resource, chainedResource, chainedParameter+"part-status", participant.getStatus().toCode(), participant.getStatus().getSystem());
						resourcemetadataList.add(rParticipant);
					}

					// participant.actor : reference
					if (participant.hasActor() && participant.getActor().hasReference()) {
						String actorString = generateFullLocalReference(participant.getActor().getReference(), baseUrl);

						rParticipant = generateResourcemetadata(resource, chainedResource, chainedParameter+"actor", actorString);
						resourcemetadataList.add(rParticipant);

						if (chainedResource == null) {
							// Add chained parameters
							rParticipantChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "actor", 0, participant.getActor().getReference());
							resourcemetadataList.addAll(rParticipantChain);
						}

						/*
						 *  Examine actor reference for specific resource types
						 */

						// location : reference
						if (actorString.indexOf("Location") >= 0) {
							rParticipant = generateResourcemetadata(resource, chainedResource, chainedParameter+"location", actorString);
							resourcemetadataList.add(rParticipant);

							if (chainedResource == null) {
								// Add chained parameters
								rParticipantChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "location", 0, participant.getActor().getReference());
								resourcemetadataList.addAll(rParticipantChain);
							}
						}
						// patient : reference
						else if (actorString.indexOf("Patient") >= 0) {
							rParticipant = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", actorString);
							resourcemetadataList.add(rParticipant);

							if (chainedResource == null) {
								// Add chained parameters
								rParticipantChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, participant.getActor().getReference());
								resourcemetadataList.addAll(rParticipantChain);
							}
						}
						// practitioner : reference
						else if (actorString.indexOf("Practitioner") >= 0) {
							rParticipant = generateResourcemetadata(resource, chainedResource, chainedParameter+"practitioner", actorString);
							resourcemetadataList.add(rParticipant);

							if (chainedResource == null) {
								// Add chained parameters
								rParticipantChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "practitioner", 0, participant.getActor().getReference());
								resourcemetadataList.addAll(rParticipantChain);
							}
						}
					}
				}
			}

			// reason-code : token
			if (appointment.hasReasonCode()) {

				Resourcemetadata rCode = null;
				for (CodeableConcept reasonCode : appointment.getReasonCode()) {
					if (reasonCode.hasCoding()) {

						for (Coding code : reasonCode.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"reason-code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// reason-reference : reference
			if (appointment.hasReasonReference()) {

				String reasonRefReference = null;
				Resourcemetadata rReasonRef = null;
				List<Resourcemetadata> rReasonRefChain = null;
				for (Reference reasonRef : appointment.getReasonReference()) {

					if (reasonRef.hasReference()) {
						reasonRefReference = generateFullLocalReference(reasonRef.getReference(), baseUrl);

						rReasonRef = generateResourcemetadata(resource, chainedResource, chainedParameter+"reason-reference", reasonRefReference);
						resourcemetadataList.add(rReasonRef);

						if (chainedResource == null) {
							// Add chained parameters for any
							rReasonRefChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "reason-reference", 0, reasonRef.getReference());
							resourcemetadataList.addAll(rReasonRefChain);
						}
					}
				}
			}

			// service-category : token
			if (appointment.hasServiceCategory()) {

				Resourcemetadata rCode = null;
				for (CodeableConcept serviceCategory : appointment.getServiceCategory()) {
					if (serviceCategory.hasCoding()) {

						for (Coding code : serviceCategory.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"service-category", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// service-type : token
			if (appointment.hasServiceType()) {

				Resourcemetadata rCode = null;
				for (CodeableConcept serviceType : appointment.getServiceType()) {
					if (serviceType.hasCoding()) {

						for (Coding code : serviceType.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"service-type", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// slot : reference
			if (appointment.hasSlot()) {

				String slotReference = null;
				Resourcemetadata rSlot = null;
				List<Resourcemetadata> rSlotChain = null;
				for (Reference slot : appointment.getBasedOn()) {

					if (slot.hasReference()) {
						slotReference = generateFullLocalReference(slot.getReference(), baseUrl);

						rSlot = generateResourcemetadata(resource, chainedResource, chainedParameter+"slot", slotReference);
						resourcemetadataList.add(rSlot);

						if (chainedResource == null) {
							// Add chained parameters
							rSlotChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "slot", 0, slot.getReference());
							resourcemetadataList.addAll(rSlotChain);
						}
					}
				}
			}

			// specialty : token
			if (appointment.hasSpecialty()) {

				Resourcemetadata rCode = null;
				for (CodeableConcept specialty : appointment.getSpecialty()) {
					if (specialty.hasCoding()) {

						for (Coding code : specialty.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"specialty", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// status : token
			if (appointment.hasStatus() && appointment.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", appointment.getStatus().toCode(), appointment.getStatus().getSystem());
				resourcemetadataList.add(rStatus);
			}

			// supporting-info : reference
			if (appointment.hasSupportingInformation()) {

				String supportingInformationReference = null;
				Resourcemetadata rSupportingInformation = null;
				List<Resourcemetadata> rSupportingInformationChain = null;
				for (Reference supportingInformation : appointment.getReasonReference()) {

					if (supportingInformation.hasReference()) {
						supportingInformationReference = generateFullLocalReference(supportingInformation.getReference(), baseUrl);

						rSupportingInformation = generateResourcemetadata(resource, chainedResource, chainedParameter+"supporting-info", supportingInformationReference);
						resourcemetadataList.add(rSupportingInformation);

						if (chainedResource == null) {
							// Add chained parameters for any
							rSupportingInformationChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "supporting-info", 0, supportingInformation.getReference());
							resourcemetadataList.addAll(rSupportingInformationChain);
						}
					}
				}
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return resourcemetadataList;
	}

}
