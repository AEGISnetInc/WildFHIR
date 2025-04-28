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
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Encounter.DiagnosisComponent;
import org.hl7.fhir.r4.model.Encounter.EncounterLocationComponent;
import org.hl7.fhir.r4.model.Encounter.EncounterParticipantComponent;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataEncounter extends ResourcemetadataProxy {

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
        ByteArrayInputStream iEncounter = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

		try {
			// Extract and convert the resource contents to a Encounter object
			if (chainedResource != null) {
				iEncounter = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iEncounter = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			Encounter encounter = (Encounter) xmlP.parse(iEncounter);
			iEncounter.close();

			/*
			 * Create new Resourcemetadata objects for each Encounter metadata value and add to the resourcemetadataList
			 */

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, encounter, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// account : reference
			if (encounter.hasAccount()) {

				for (Reference account : encounter.getAccount()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "account", 0, account, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// appointment : reference
			if (encounter.hasAppointment()) {

				for (Reference appointment : encounter.getAppointment()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "appointment", 0, appointment, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// based-on : reference
			if (encounter.hasBasedOn()) {

				for (Reference basedOn : encounter.getBasedOn()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "based-on", 0, basedOn, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// class : token
			if (encounter.hasClass_()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"class", encounter.getClass_().getCode(), encounter.getClass_().getSystem(), null, ServicesUtil.INSTANCE.getTextValue(encounter.getClass_()));
				resourcemetadataList.add(rMetadata);
			}

			// date : date(period)
			if (encounter.hasPeriod()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(encounter.getPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(encounter.getPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(encounter.getPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(encounter.getPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
				resourcemetadataList.add(rMetadata);
			}

			// diagnosis : reference
			if (encounter.hasDiagnosis()) {
				for (DiagnosisComponent diagnosis : encounter.getDiagnosis()) {

					if (diagnosis.hasCondition()) {
						rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "diagnosis", 0, diagnosis.getCondition(), null);
						resourcemetadataList.addAll(rMetadataChain);
					}
				}
			}

			// episode-of-care : reference
			if (encounter.hasEpisodeOfCare()) {

				for (Reference episodeOfCare : encounter.getEpisodeOfCare()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "episode-of-care", 0, episodeOfCare, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// identifier : token
			if (encounter.hasIdentifier()) {

				for (Identifier identifier : encounter.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}

			// length : number
			if (encounter.hasLength()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"length", encounter.getLength().getCode(), encounter.getLength().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// location : reference
			// location-period : date(period)
			if (encounter.hasLocation()) {
				for (EncounterLocationComponent location : encounter.getLocation()) {

					if (location.hasLocation()) {
						rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "location", 0, location.getLocation(), null);
						resourcemetadataList.addAll(rMetadataChain);
					}

					if (location.hasPeriod()) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"location-period", utcDateUtil.formatDate(location.getPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(location.getPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(location.getPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(location.getPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
						resourcemetadataList.add(rMetadata);
					}

				}
			}

			// part-of : reference
			if (encounter.hasPartOf()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "part-of", 0, encounter.getPartOf(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			if (encounter.hasParticipant()) {
				for (EncounterParticipantComponent participant : encounter.getParticipant()) {

					// participant-type : token
					if (participant.hasType()) {
						for (CodeableConcept particpantType : participant.getType()) {

							if (particpantType.hasCoding()) {
								for (Coding type : particpantType.getCoding()) {
									rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"participant-type", type.getCode(), type.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(type));
									resourcemetadataList.add(rMetadata);
								}
							}
						}
					}

					// participant : reference
					if (participant.hasIndividual()) {
						rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "participant", 0, participant.getIndividual(), null);
						resourcemetadataList.addAll(rMetadataChain);

						// practitioner : reference
						if ((participant.getIndividual().hasReference() && participant.getIndividual().getReference().indexOf("Practitioner") >= 0)
								|| (participant.getIndividual().hasType() && participant.getIndividual().getType().equals("Practitioner"))) {
							rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "practitioner", 0, participant.getIndividual(), null);
							resourcemetadataList.addAll(rMetadataChain);
						}
					}
				}
			}

			// subject : reference
			if (encounter.hasSubject()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "subject", 0, encounter.getSubject(), null);
				resourcemetadataList.addAll(rMetadataChain);

				// patient : reference
				if ((encounter.getSubject().hasReference() && encounter.getSubject().getReference().indexOf("Patient") >= 0)
						|| (encounter.getSubject().hasType() && encounter.getSubject().getType().equals("Patient"))) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "patient", 0, encounter.getSubject(), null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// reason-code : token
			if (encounter.hasReasonCode()) {

				for (CodeableConcept reasonCode : encounter.getReasonCode()) {

					if (reasonCode.hasCoding()) {
						for (Coding code : reasonCode.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"reason-code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// reason-reference : reference
			if (encounter.hasReasonReference()) {

				for (Reference reasonReference : encounter.getReasonReference()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "reason-reference", 0, reasonReference, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// service-provider : reference
			if (encounter.hasServiceProvider()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "service-provider", 0, encounter.getServiceProvider(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// special-arrangement : token
			if (encounter.hasHospitalization()) {

				if (encounter.getHospitalization().hasSpecialArrangement()) {

					for (CodeableConcept arrangement : encounter.getHospitalization().getSpecialArrangement()) {

						if (arrangement.hasCoding()) {
							for (Coding code : arrangement.getCoding()) {
								rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"special-arrangement", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
								resourcemetadataList.add(rMetadata);
							}
						}
					}
				}
			}

			// status : token
			if (encounter.hasStatus() && encounter.getStatus() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", encounter.getStatus().toCode(), encounter.getStatus().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// type : token
			if (encounter.hasType()) {

				for (CodeableConcept encounterType : encounter.getType()) {

					if (encounterType.hasCoding()) {
						for (Coding type : encounterType.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"type", type.getCode(), type.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(type));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		} finally {
	        rMetadata = null;
	        rMetadataChain = null;
            if (iEncounter != null) {
                try {
                	iEncounter.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
