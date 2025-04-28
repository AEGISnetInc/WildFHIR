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
        ByteArrayInputStream iAppointment = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

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

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, appointment, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// appointment-type : token
			if (appointment.hasAppointmentType() && appointment.getAppointmentType().hasCoding()) {

				for (Coding code : appointment.getAppointmentType().getCoding()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"appointment-type", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rMetadata);
				}
			}

			// based-on : reference
			if (appointment.hasBasedOn()) {

				for (Reference basedOn : appointment.getBasedOn()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "based-on", 0, basedOn, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// date : date
			if (appointment.hasStart()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(appointment.getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(appointment.getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rMetadata);
			}

			// identifier : token
			if (appointment.hasIdentifier()) {

				for (Identifier identifier : appointment.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}

			// participant
			if (appointment.hasParticipant()) {

				for (AppointmentParticipantComponent participant : appointment.getParticipant()) {

					// participant.partstatus : token
					if (participant.hasStatus() && participant.getStatus() != null) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"part-status", participant.getStatus().toCode(), participant.getStatus().getSystem());
						resourcemetadataList.add(rMetadata);
					}

					// participant.actor : reference
					if (participant.hasActor()) {
						rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "actor", 0, participant.getActor(), null);
						resourcemetadataList.addAll(rMetadataChain);

						/*
						 *  Examine actor reference for specific resource types
						 */

						// location : reference
						if ((participant.getActor().hasReference() && participant.getActor().getReference().indexOf("Location") >= 0)
								|| (participant.getActor().hasType() && participant.getActor().getType().equals("Location"))) {

							rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "location", 0, participant.getActor(), null);
							resourcemetadataList.addAll(rMetadataChain);
						}
						// patient : reference
						else if ((participant.getActor().hasReference() && participant.getActor().getReference().indexOf("Patient") >= 0)
								|| (participant.getActor().hasType() && participant.getActor().getType().equals("Patient"))) {

							rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "patient", 0, participant.getActor(), null);
							resourcemetadataList.addAll(rMetadataChain);
						}
						// practitioner : reference
						else if ((participant.getActor().hasReference() && participant.getActor().getReference().indexOf("Practitioner") >= 0)
								|| (participant.getActor().hasType() && participant.getActor().getType().equals("Practitioner"))) {

							rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "practitioner", 0, participant.getActor(), null);
							resourcemetadataList.addAll(rMetadataChain);
						}
					}
				}
			}

			// reason-code : token
			if (appointment.hasReasonCode()) {

				for (CodeableConcept reasonCode : appointment.getReasonCode()) {
					if (reasonCode.hasCoding()) {

						for (Coding code : reasonCode.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"reason-code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// reason-reference : reference
			if (appointment.hasReasonReference()) {

				for (Reference reasonRef : appointment.getReasonReference()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "reason-reference", 0, reasonRef, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// service-category : token
			if (appointment.hasServiceCategory()) {

				for (CodeableConcept serviceCategory : appointment.getServiceCategory()) {
					if (serviceCategory.hasCoding()) {

						for (Coding code : serviceCategory.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"service-category", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// service-type : token
			if (appointment.hasServiceType()) {

				for (CodeableConcept serviceType : appointment.getServiceType()) {
					if (serviceType.hasCoding()) {

						for (Coding code : serviceType.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"service-type", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// slot : reference
			if (appointment.hasSlot()) {

				for (Reference slot : appointment.getBasedOn()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "slot", 0, slot, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// specialty : token
			if (appointment.hasSpecialty()) {

				for (CodeableConcept specialty : appointment.getSpecialty()) {
					if (specialty.hasCoding()) {

						for (Coding code : specialty.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"specialty", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// status : token
			if (appointment.hasStatus() && appointment.getStatus() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", appointment.getStatus().toCode(), appointment.getStatus().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// supporting-info : reference
			if (appointment.hasSupportingInformation()) {

				for (Reference supportingInformation : appointment.getReasonReference()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "supporting-info", 0, supportingInformation, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		} finally {
	        rMetadata = null;
	        rMetadataChain = null;
            if (iAppointment != null) {
                try {
                	iAppointment.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
