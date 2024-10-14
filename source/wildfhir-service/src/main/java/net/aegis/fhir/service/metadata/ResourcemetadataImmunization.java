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
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.Immunization.ImmunizationPerformerComponent;
import org.hl7.fhir.r4.model.Immunization.ImmunizationProtocolAppliedComponent;
import org.hl7.fhir.r4.model.Immunization.ImmunizationReactionComponent;
import org.hl7.fhir.r4.model.Reference;

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataImmunization extends ResourcemetadataProxy {

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
        ByteArrayInputStream iImmunization = null;

		try {
			// Extract and convert the resource contents to a Immunization object
			if (chainedResource != null) {
				iImmunization = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iImmunization = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			Immunization immunization = (Immunization) xmlP.parse(iImmunization);
			iImmunization.close();

			/*
			 * Create new Resourcemetadata objects for each Immunization metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, immunization, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			if (immunization.getId() != null) {
				Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", immunization.getId());
				resourcemetadataList.add(_id);
			}

			// _language : token
			if (immunization.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", immunization.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (immunization.getMeta() != null && immunization.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(immunization.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(immunization.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// date : datetime
			if (immunization.hasOccurrenceDateTimeType()) {
				Resourcemetadata rDate = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(immunization.getOccurrenceDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(immunization.getOccurrenceDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rDate);
			}

			// identifier : token
			if (immunization.hasIdentifier()) {

				for (Identifier identifier : immunization.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// location : reference
			if (immunization.hasLocation() && immunization.getLocation().hasReference()) {
				Resourcemetadata rLocation = generateResourcemetadata(resource, chainedResource, chainedParameter+"location", generateFullLocalReference(immunization.getLocation().getReference(), baseUrl));
				resourcemetadataList.add(rLocation);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rLocationChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "location", 0, immunization.getLocation().getReference(), null);
					resourcemetadataList.addAll(rLocationChain);
				}
			}

			// lot-number : string
			if (immunization.hasLotNumber()) {
				Resourcemetadata rLotNumber = generateResourcemetadata(resource, chainedResource, chainedParameter+"lot-number", immunization.getLotNumber());
				resourcemetadataList.add(rLotNumber);
			}

			// manufacturer : reference
			if (immunization.hasManufacturer() && immunization.getManufacturer().hasReference()) {
				Resourcemetadata rManufacturer = generateResourcemetadata(resource, chainedResource, chainedParameter+"manufacturer", generateFullLocalReference(immunization.getManufacturer().getReference(), baseUrl));
				resourcemetadataList.add(rManufacturer);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rOrganizationChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "manufacturer", 0, immunization.getManufacturer().getReference(), null);
					resourcemetadataList.addAll(rOrganizationChain);
				}
			}

			// patient : reference
			if (immunization.hasPatient() && immunization.getPatient().hasReference()) {
				Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", generateFullLocalReference(immunization.getPatient().getReference(), baseUrl));
				resourcemetadataList.add(rPatient);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rPatientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, immunization.getPatient().getReference(), null);
					resourcemetadataList.addAll(rPatientChain);
				}
			}

			// performer : reference
			if (immunization.hasPerformer()) {

				Resourcemetadata rPerformer = null;
				List<Resourcemetadata> rPerformerChain = null;
				for (ImmunizationPerformerComponent performer : immunization.getPerformer()) {

					if (performer.hasActor() && performer.getActor().hasReference()) {
						rPerformer = generateResourcemetadata(resource, chainedResource, chainedParameter+"performer", generateFullLocalReference(performer.getActor().getReference(), baseUrl));
						resourcemetadataList.add(rPerformer);

						if (chainedResource == null) {
							// Add chained parameters
							rPerformerChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "performer", 0, performer.getActor().getReference(), null);
							resourcemetadataList.addAll(rPerformerChain);
						}
					}
				}
			}

			// reaction : reference
			// reaction-date : date
			if (immunization.hasReaction()) {

				Resourcemetadata rReaction = null;
				List<Resourcemetadata> rReactionChain = null;
				for (ImmunizationReactionComponent reaction : immunization.getReaction()) {

					if (reaction.hasDetail() && reaction.getDetail().hasReference()) {
						rReaction = generateResourcemetadata(resource, chainedResource, chainedParameter+"reaction", generateFullLocalReference(reaction.getDetail().getReference(), baseUrl));
						resourcemetadataList.add(rReaction);

						if (chainedResource == null) {
							// Add chained parameters
							rReactionChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "reaction", 0, reaction.getDetail().getReference(), null);
							resourcemetadataList.addAll(rReactionChain);
						}
					}

					if (reaction.hasDate()) {
						rReaction = generateResourcemetadata(resource, chainedResource, chainedParameter+"reaction-date", utcDateUtil.formatDate(reaction.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(reaction.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
						resourcemetadataList.add(rReaction);
					}
				}
			}

			// reason-code : token
			if (immunization.hasReasonCode()) {

				Resourcemetadata rCode = null;
				for (CodeableConcept reasonCode : immunization.getReasonCode()) {

					if (reasonCode.hasCoding()) {
						for (Coding code : reasonCode.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"reason-code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// reason-reference : reference
			if (immunization.hasReasonReference()) {

				Resourcemetadata rReasonReference = null;
				List<Resourcemetadata> rReasonReferenceChain = null;
				for (Reference reasonReference : immunization.getReasonReference()) {

					if (reasonReference.hasReference()) {
						rReasonReference = generateResourcemetadata(resource, chainedResource, chainedParameter+"reason-reference", generateFullLocalReference(reasonReference.getReference(), baseUrl));
						resourcemetadataList.add(rReasonReference);

						if (chainedResource == null) {
							// Add chained parameters
							rReasonReferenceChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "reason-reference", 0, reasonReference.getReference(), null);
							resourcemetadataList.addAll(rReasonReferenceChain);
						}
					}
				}
			}

			// series : string
			// target-disease: token
			if (immunization.hasProtocolApplied()) {

				Resourcemetadata rSeries = null;
				Resourcemetadata rCode = null;
				for (ImmunizationProtocolAppliedComponent protocolApplied : immunization.getProtocolApplied()) {

					if (protocolApplied.hasSeries()) {
						rSeries = generateResourcemetadata(resource, chainedResource, chainedParameter+"series", protocolApplied.getSeries());
						resourcemetadataList.add(rSeries);
					}

					if (protocolApplied.hasTargetDisease()) {

						for (CodeableConcept targetDisease : protocolApplied.getTargetDisease()) {

							if (targetDisease.hasCoding()) {
								for (Coding code : targetDisease.getCoding()) {
									rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"target-disease", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
									resourcemetadataList.add(rCode);
								}
							}
						}
					}
				}
			}

			// status : token
			if (immunization.hasStatus() && immunization.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", immunization.getStatus().toCode(), immunization.getStatus().getSystem());
				resourcemetadataList.add(rStatus);
			}

			// status-reason : token
			if (immunization.hasStatusReason() && immunization.getStatusReason().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : immunization.getStatusReason().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"status-reason", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			// vaccine-code : token
			if (immunization.hasVaccineCode() && immunization.getVaccineCode().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : immunization.getVaccineCode().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"vaccine-code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
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
