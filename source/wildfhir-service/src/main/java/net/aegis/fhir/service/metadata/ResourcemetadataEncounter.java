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

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, encounter, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			if (encounter.getId() != null) {
				Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", encounter.getId());
				resourcemetadataList.add(_id);
			}

			// _language : token
			if (encounter.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", encounter.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (encounter.getMeta() != null && encounter.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(encounter.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(encounter.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// account : reference
			if (encounter.hasAccount()) {

				Resourcemetadata rAccount = null;
				List<Resourcemetadata> rAccountChain = null;
				for (Reference account : encounter.getAccount()) {

					if (account.hasReference()) {
						rAccount = generateResourcemetadata(resource, chainedResource, chainedParameter+"account", account.getReference());
						resourcemetadataList.add(rAccount);

						if (chainedResource == null) {
							// Add chained parameters
							rAccountChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "account", 0, account.getReference(), null);
							resourcemetadataList.addAll(rAccountChain);
						}
					}
				}
			}

			// appointment : reference
			if (encounter.hasAppointment()) {

				Resourcemetadata rAppointment = null;
				List<Resourcemetadata> rAppointmentChain = null;
				for (Reference appointment : encounter.getAppointment()) {

					if (appointment.hasReference()) {
						rAppointment = generateResourcemetadata(resource, chainedResource, chainedParameter+"appointment", appointment.getReference());
						resourcemetadataList.add(rAppointment);

						if (chainedResource == null) {
							// Add chained parameters
							rAppointmentChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "appointment", 0, appointment.getReference(), null);
							resourcemetadataList.addAll(rAppointmentChain);
						}
					}
				}
			}

			// based-on : reference
			if (encounter.hasBasedOn()) {

				Resourcemetadata rIncomingReferralRequest = null;
				List<Resourcemetadata> rBasedOnChain = null;
				for (Reference basedOn : encounter.getBasedOn()) {

					if (basedOn.hasReference()) {
						rIncomingReferralRequest = generateResourcemetadata(resource, chainedResource, chainedParameter+"based-on", basedOn.getReference());
						resourcemetadataList.add(rIncomingReferralRequest);

						if (chainedResource == null) {
							// Add chained parameters
							rBasedOnChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "based-on", 0, basedOn.getReference(), null);
							resourcemetadataList.addAll(rBasedOnChain);
						}
					}
				}
			}

			// class : token
			if (encounter.hasClass_()) {
				Resourcemetadata rClass = generateResourcemetadata(resource, chainedResource, chainedParameter+"class", encounter.getClass_().getCode(), encounter.getClass_().getSystem(), null, ServicesUtil.INSTANCE.getTextValue(encounter.getClass_()));
				resourcemetadataList.add(rClass);
			}

			// date : date(period)
			if (encounter.hasPeriod()) {
				Resourcemetadata rPeriod = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(encounter.getPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(encounter.getPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(encounter.getPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(encounter.getPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
				resourcemetadataList.add(rPeriod);
			}

			// diagnosis : reference
			if (encounter.hasDiagnosis()) {

				Resourcemetadata rDiagnosis = null;
				List<Resourcemetadata> rDiagnosisChain = null;
				for (DiagnosisComponent diagnosis : encounter.getDiagnosis()) {

					if (diagnosis.hasCondition() && diagnosis.getCondition().hasReference()) {
						rDiagnosis = generateResourcemetadata(resource, chainedResource, chainedParameter+"diagnosis", generateFullLocalReference(diagnosis.getCondition().getReference(), baseUrl));
						resourcemetadataList.add(rDiagnosis);

						if (chainedResource == null) {
							// Add chained parameters for any
							rDiagnosisChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "diagnosis", 0, diagnosis.getCondition().getReference(), null);
							resourcemetadataList.addAll(rDiagnosisChain);
						}
					}
				}
			}

			// episode-of-care : reference
			if (encounter.hasEpisodeOfCare()) {

				List<Resourcemetadata> rEpisodeOfCareChain = null;
				for (Reference episodeOfCare : encounter.getEpisodeOfCare()) {

					if (episodeOfCare.hasReference()) {
						Resourcemetadata rEpisodeOfCare = generateResourcemetadata(resource, chainedResource, chainedParameter+"episode-of-care", generateFullLocalReference(episodeOfCare.getReference(), baseUrl));
						resourcemetadataList.add(rEpisodeOfCare);

						if (chainedResource == null) {
							// Add chained parameters
							rEpisodeOfCareChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "episode-of-care", 0, episodeOfCare.getReference(), null);
							resourcemetadataList.addAll(rEpisodeOfCareChain);
						}
					}
				}
			}

			// identifier : token
			if (encounter.hasIdentifier()) {

				for (Identifier identifier : encounter.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// length : number
			if (encounter.hasLength()) {
				Resourcemetadata rLength = generateResourcemetadata(resource, chainedResource, chainedParameter+"length", encounter.getLength().getCode(), encounter.getLength().getSystem());
				resourcemetadataList.add(rLength);
			}

			// location : reference
			// location-period : date(period)
			if (encounter.hasLocation()) {

				List<Resourcemetadata> rLocationChain = null;
				for (EncounterLocationComponent location : encounter.getLocation()) {

					if (location.hasLocation() && location.getLocation().hasReference()) {
						Resourcemetadata rLocation = generateResourcemetadata(resource, chainedResource, chainedParameter+"location", generateFullLocalReference(location.getLocation().getReference(), baseUrl));
						resourcemetadataList.add(rLocation);

						if (chainedResource == null) {
							// Add chained parameters
							rLocationChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "location", 0, location.getLocation().getReference(), null);
							resourcemetadataList.addAll(rLocationChain);
						}
					}

					if (location.hasPeriod()) {
						Resourcemetadata rPeriod = generateResourcemetadata(resource, chainedResource, chainedParameter+"location-period", utcDateUtil.formatDate(location.getPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(location.getPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(location.getPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(location.getPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
						resourcemetadataList.add(rPeriod);
					}

				}
			}

			// part-of : reference
			if (encounter.hasPartOf() && encounter.getPartOf().hasReference()) {
				Resourcemetadata rPartOf = generateResourcemetadata(resource, chainedResource, chainedParameter+"part-of", generateFullLocalReference(encounter.getPartOf().getReference(), baseUrl));
				resourcemetadataList.add(rPartOf);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rPartOfChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "part-of", 0, encounter.getPartOf().getReference(), null);
					resourcemetadataList.addAll(rPartOfChain);
				}
			}

			if (encounter.hasParticipant()) {

				Resourcemetadata rType = null;
				List<Resourcemetadata> rParticipantChain = null;
				for (EncounterParticipantComponent participant : encounter.getParticipant()) {

					// participant-type : token
					if (participant.hasType()) {

						for (CodeableConcept particpantType : participant.getType()) {

							if (particpantType.hasCoding()) {
								for (Coding type : particpantType.getCoding()) {
									rType = generateResourcemetadata(resource, chainedResource, chainedParameter+"participant-type", type.getCode(), type.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(type));
									resourcemetadataList.add(rType);
								}
							}
						}
					}

					// practitioner : reference
					// participant : reference
					if (participant.hasIndividual() && participant.getIndividual().hasReference()) {
						String participantReference = generateFullLocalReference(participant.getIndividual().getReference(), baseUrl);

						Resourcemetadata rParticipant = generateResourcemetadata(resource, chainedResource, chainedParameter+"participant", participantReference);
						resourcemetadataList.add(rParticipant);

						if (chainedResource == null) {
							// Add chained parameters for any
							rParticipantChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "participant", 0, participant.getIndividual().getReference(), null);
							resourcemetadataList.addAll(rParticipantChain);
						}

						if (participant.getIndividual().getReference().contains("Practitioner")) {
							Resourcemetadata rPractitioner = generateResourcemetadata(resource, chainedResource, chainedParameter+"practitioner", participantReference);
							resourcemetadataList.add(rPractitioner);

							if (chainedResource == null) {
								// Add chained parameters
								rParticipantChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "practitioner", 0, participant.getIndividual().getReference(), null);
								resourcemetadataList.addAll(rParticipantChain);
							}
						}

					}
				}
			}

			// patient : reference
			// subject : reference
			if (encounter.hasSubject() && encounter.getSubject().hasReference()) {
				String subjectReference = generateFullLocalReference(encounter.getSubject().getReference(), baseUrl);

				Resourcemetadata rSubject = generateResourcemetadata(resource, chainedResource, chainedParameter+"subject", subjectReference);
				resourcemetadataList.add(rSubject);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rSubjectChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "subject", 0, encounter.getSubject().getReference(), null);
					resourcemetadataList.addAll(rSubjectChain);
				}

				if (subjectReference.indexOf("Patient") >= 0) {
					Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", subjectReference);
					resourcemetadataList.add(rPatient);

					if (chainedResource == null) {
						// Add chained parameters
						List<Resourcemetadata> rPatientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, encounter.getSubject().getReference(), null);
						resourcemetadataList.addAll(rPatientChain);
					}
				}
			}

			// reason-code : token
			if (encounter.hasReasonCode()) {

				Resourcemetadata rCode = null;
				for (CodeableConcept reasonCode : encounter.getReasonCode()) {

					if (reasonCode.hasCoding()) {
						for (Coding code : reasonCode.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"reason-code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// reason-reference : reference
			if (encounter.hasReasonReference()) {

				Resourcemetadata rReasonReference = null;
				List<Resourcemetadata> rReasonReferenceChain = null;
				for (Reference reasonReference : encounter.getReasonReference()) {

					if (reasonReference.hasReference()) {
						rReasonReference = generateResourcemetadata(resource, chainedResource, chainedParameter+"reason-reference", generateFullLocalReference(reasonReference.getReference(), baseUrl));
						resourcemetadataList.add(rReasonReference);

						if (chainedResource == null) {
							// Add chained parameters for any
							rReasonReferenceChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "reason-reference", 0, reasonReference.getReference(), null);
							resourcemetadataList.addAll(rReasonReferenceChain);
						}
					}
				}
			}

			// service-provider : reference
			if (encounter.hasServiceProvider()) {
				Resourcemetadata rServiceProvider = generateResourcemetadata(resource, chainedResource, chainedParameter+"service-provider", generateFullLocalReference(encounter.getServiceProvider().getReference(), baseUrl));
				resourcemetadataList.add(rServiceProvider);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rServiceProviderChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "service-provider", 0, encounter.getServiceProvider().getReference(), null);
					resourcemetadataList.addAll(rServiceProviderChain);
				}
			}

			// special-arrangement : token
			if (encounter.hasHospitalization()) {

				if (encounter.getHospitalization().hasSpecialArrangement()) {

					Resourcemetadata rCode = null;
					for (CodeableConcept arrangement : encounter.getHospitalization().getSpecialArrangement()) {

						if (arrangement.hasCoding()) {
							for (Coding code : arrangement.getCoding()) {
								rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"special-arrangement", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
								resourcemetadataList.add(rCode);
							}
						}
					}
				}
			}

			// status : token
			if (encounter.hasStatus() && encounter.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", encounter.getStatus().toCode(), encounter.getStatus().getSystem());
				resourcemetadataList.add(rStatus);
			}

			// type : token
			if (encounter.hasType()) {

				Resourcemetadata rType = null;
				for (CodeableConcept encounterType : encounter.getType()) {

					if (encounterType.hasCoding()) {
						for (Coding type : encounterType.getCoding()) {
							rType = generateResourcemetadata(resource, chainedResource, chainedParameter+"type", type.getCode(), type.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(type));
							resourcemetadataList.add(rType);
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
