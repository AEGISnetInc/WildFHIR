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
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

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

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, immunization, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// date : datetime
			if (immunization.hasOccurrenceDateTimeType()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(immunization.getOccurrenceDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(immunization.getOccurrenceDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rMetadata);
			}

			// identifier : token
			if (immunization.hasIdentifier()) {

				for (Identifier identifier : immunization.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}

			// location : reference
			if (immunization.hasLocation()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "location", 0, immunization.getLocation(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// lot-number : string
			if (immunization.hasLotNumber()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"lot-number", immunization.getLotNumber());
				resourcemetadataList.add(rMetadata);
			}

			// manufacturer : reference
			if (immunization.hasManufacturer()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "manufacturer", 0, immunization.getManufacturer(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// patient : reference
			if (immunization.hasPatient()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "patient", 0, immunization.getPatient(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// performer : reference
			if (immunization.hasPerformer()) {
				for (ImmunizationPerformerComponent performer : immunization.getPerformer()) {

					if (performer.hasActor()) {
						rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "performer", 0, performer.getActor(), null);
						resourcemetadataList.addAll(rMetadataChain);
					}
				}
			}

			// reaction : reference
			// reaction-date : date
			if (immunization.hasReaction()) {

				for (ImmunizationReactionComponent reaction : immunization.getReaction()) {

					if (reaction.hasDetail()) {
						rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "reaction", 0, reaction.getDetail(), null);
						resourcemetadataList.addAll(rMetadataChain);
					}

					if (reaction.hasDate()) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"reaction-date", utcDateUtil.formatDate(reaction.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(reaction.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
						resourcemetadataList.add(rMetadata);
					}
				}
			}

			// reason-code : token
			if (immunization.hasReasonCode()) {
				for (CodeableConcept reasonCode : immunization.getReasonCode()) {

					if (reasonCode.hasCoding()) {
						for (Coding code : reasonCode.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"reason-code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// reason-reference : reference
			if (immunization.hasReasonReference()) {

				for (Reference reasonReference : immunization.getReasonReference()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "reason-reference", 0, reasonReference, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// series : string
			// target-disease: token
			if (immunization.hasProtocolApplied()) {

				for (ImmunizationProtocolAppliedComponent protocolApplied : immunization.getProtocolApplied()) {

					if (protocolApplied.hasSeries()) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"series", protocolApplied.getSeries());
						resourcemetadataList.add(rMetadata);
					}

					if (protocolApplied.hasTargetDisease()) {
						for (CodeableConcept targetDisease : protocolApplied.getTargetDisease()) {

							if (targetDisease.hasCoding()) {
								for (Coding code : targetDisease.getCoding()) {
									rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"target-disease", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
									resourcemetadataList.add(rMetadata);
								}
							}
						}
					}
				}
			}

			// status : token
			if (immunization.hasStatus() && immunization.getStatus() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", immunization.getStatus().toCode(), immunization.getStatus().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// status-reason : token
			if (immunization.hasStatusReason() && immunization.getStatusReason().hasCoding()) {

				for (Coding code : immunization.getStatusReason().getCoding()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"status-reason", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rMetadata);
				}
			}

			// vaccine-code : token
			if (immunization.hasVaccineCode() && immunization.getVaccineCode().hasCoding()) {

				for (Coding code : immunization.getVaccineCode().getCoding()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"vaccine-code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rMetadata);
				}
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		} finally {
	        rMetadata = null;
	        rMetadataChain = null;
            if (iImmunization != null) {
                try {
                	iImmunization.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
