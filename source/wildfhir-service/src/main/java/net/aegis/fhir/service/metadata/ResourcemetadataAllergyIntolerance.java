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
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.AllergyIntolerance.AllergyIntoleranceCategory;
import org.hl7.fhir.r4.model.AllergyIntolerance.AllergyIntoleranceReactionComponent;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Enumeration;
import org.hl7.fhir.r4.model.Identifier;

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataAllergyIntolerance extends ResourcemetadataProxy {

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
        ByteArrayInputStream iAllergyIntolerance = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

		try {
			// Extract and convert the resource contents to a AllergyIntolerance object
			if (chainedResource != null) {
				iAllergyIntolerance = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iAllergyIntolerance = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			AllergyIntolerance allergyIntolerance = (AllergyIntolerance) xmlP.parse(iAllergyIntolerance);
			iAllergyIntolerance.close();

			/*
			 * Create new Resourcemetadata objects for each AllergyIntolerance metadata value and add to the resourcemetadataList
			 */

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, allergyIntolerance, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// asserter : reference
			if (allergyIntolerance.hasAsserter()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "asserter", 0, allergyIntolerance.getAsserter(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// category : token
			if (allergyIntolerance.hasCategory()) {

				for (Enumeration<AllergyIntoleranceCategory> category : allergyIntolerance.getCategory()) {

					if (category != null) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"category", category.getValue().toCode(), category.getValue().getSystem());
						resourcemetadataList.add(rMetadata);
					}
				}
			}

			// clinical-status : token
			if (allergyIntolerance.hasClinicalStatus() && allergyIntolerance.getClinicalStatus().hasCoding()) {

				for (Coding code : allergyIntolerance.getClinicalStatus().getCoding()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"clinical-status", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rMetadata);
				}
			}

			// code : token
			if (allergyIntolerance.hasCode() && allergyIntolerance.getCode().hasCoding()) {

				for (Coding code : allergyIntolerance.getCode().getCoding()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rMetadata);
				}
			}

			// criticality : token
			if (allergyIntolerance.hasCriticality() && allergyIntolerance.getCriticality() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"criticality", allergyIntolerance.getCriticality().toCode(), allergyIntolerance.getCriticality().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// date : date
			if (allergyIntolerance.hasRecordedDate()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(allergyIntolerance.getRecordedDate(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(allergyIntolerance.getRecordedDate(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rMetadata);
			}

			// identifier : token
			if (allergyIntolerance.hasIdentifier()) {

				for (Identifier identifier : allergyIntolerance.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}

			// last-date : date
			if (allergyIntolerance.hasLastOccurrence()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"last-date", utcDateUtil.formatDate(allergyIntolerance.getLastOccurrence(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(allergyIntolerance.getLastOccurrence(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rMetadata);
			}

			// patient : reference
			if (allergyIntolerance.hasPatient()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "patient", 0, allergyIntolerance.getPatient(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// recorder : reference
			if (allergyIntolerance.hasRecorder()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "recorder", 0, allergyIntolerance.getRecorder(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// type : token
			if (allergyIntolerance.hasType() && allergyIntolerance.getType() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"type", allergyIntolerance.getType().toCode(), allergyIntolerance.getType().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// AllergyIntolerance.reaction parameters
			if (allergyIntolerance.hasReaction()) {

				for (AllergyIntoleranceReactionComponent reaction : allergyIntolerance.getReaction()) {

					// reaction.manifestation : token
					if (reaction.hasManifestation()) {

						for (CodeableConcept manifestation : reaction.getManifestation()) {

							if (manifestation.hasCoding()) {
								for (Coding code : manifestation.getCoding()) {
									rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"manifestation", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
									resourcemetadataList.add(rMetadata);
								}
							}
						}
					}

					// reaction.onset : date
					if (reaction.hasOnset()) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"onset", utcDateUtil.formatDate(reaction.getOnset(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(reaction.getOnset(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
						resourcemetadataList.add(rMetadata);
					}

					// reaction.route : token
					if (reaction.hasExposureRoute() && reaction.getExposureRoute().hasCoding()) {

						for (Coding code : reaction.getExposureRoute().getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"route", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}

					// reaction.severity : token
					if (reaction.hasSeverity() && reaction.getSeverity() != null) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"severity", reaction.getSeverity().toCode(), reaction.getSeverity().getSystem());
						resourcemetadataList.add(rMetadata);
					}

					// reaction.substance(code) : token
					if (reaction.hasSubstance() && reaction.getSubstance().hasCoding()) {

						for (Coding code : reaction.getSubstance().getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// verification-status : token
			if (allergyIntolerance.hasVerificationStatus() && allergyIntolerance.getVerificationStatus().hasCoding()) {

				for (Coding code : allergyIntolerance.getVerificationStatus().getCoding()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"verification-status", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
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
            if (iAllergyIntolerance != null) {
                try {
                	iAllergyIntolerance.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
