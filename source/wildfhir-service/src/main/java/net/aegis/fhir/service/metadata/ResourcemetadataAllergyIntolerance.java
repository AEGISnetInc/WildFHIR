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
        ByteArrayInputStream iAllergyIntolerance = null;

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

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, allergyIntolerance, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", allergyIntolerance.getId());
			resourcemetadataList.add(_id);

			// _language : token
			if (allergyIntolerance.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", allergyIntolerance.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (allergyIntolerance.getMeta() != null && allergyIntolerance.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(allergyIntolerance.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(allergyIntolerance.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// asserter : reference
			if (allergyIntolerance.hasAsserter() && allergyIntolerance.getAsserter().hasReference()) {
				String asserterReference = generateFullLocalReference(allergyIntolerance.getAsserter().getReference(), baseUrl);

				Resourcemetadata rAsserter = generateResourcemetadata(resource, chainedResource, chainedParameter+"asserter", asserterReference);
				resourcemetadataList.add(rAsserter);

				if (chainedResource == null) {
					// Add chained parameters for any
					List<Resourcemetadata> rAsserterChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "asserter", 0, allergyIntolerance.getAsserter().getReference());
					resourcemetadataList.addAll(rAsserterChain);
				}
			}

			// category : token
			if (allergyIntolerance.hasCategory()) {

				for (Enumeration<AllergyIntoleranceCategory> category : allergyIntolerance.getCategory()) {

					if (category != null) {
						Resourcemetadata rCategory = generateResourcemetadata(resource, chainedResource, chainedParameter+"category", category.getValue().toCode(), category.getValue().getSystem());
						resourcemetadataList.add(rCategory);
					}
				}
			}

			// clinical-status : token
			if (allergyIntolerance.hasClinicalStatus() && allergyIntolerance.getClinicalStatus().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : allergyIntolerance.getClinicalStatus().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"clinical-status", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			// code : token
			if (allergyIntolerance.hasCode() && allergyIntolerance.getCode().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : allergyIntolerance.getCode().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			// criticality : token
			if (allergyIntolerance.hasCriticality() && allergyIntolerance.getCriticality() != null) {
				Resourcemetadata rCriticality = generateResourcemetadata(resource, chainedResource, chainedParameter+"criticality", allergyIntolerance.getCriticality().toCode(), allergyIntolerance.getCriticality().getSystem());
				resourcemetadataList.add(rCriticality);
			}

			// date : date
			if (allergyIntolerance.hasRecordedDate()) {
				Resourcemetadata rDate = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(allergyIntolerance.getRecordedDate(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(allergyIntolerance.getRecordedDate(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rDate);
			}

			// identifier : token
			if (allergyIntolerance.hasIdentifier()) {

				for (Identifier identifier : allergyIntolerance.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// last-date : date
			if (allergyIntolerance.hasLastOccurrence()) {
				Resourcemetadata rDate = generateResourcemetadata(resource, chainedResource, chainedParameter+"last-date", utcDateUtil.formatDate(allergyIntolerance.getLastOccurrence(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(allergyIntolerance.getLastOccurrence(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rDate);
			}

			// patient : reference
			if (allergyIntolerance.hasPatient() && allergyIntolerance.getPatient().hasReference()) {
				String patientReference = generateFullLocalReference(allergyIntolerance.getPatient().getReference(), baseUrl);

				Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", patientReference);
				resourcemetadataList.add(rPatient);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rPatientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, allergyIntolerance.getPatient().getReference());
					resourcemetadataList.addAll(rPatientChain);
				}
			}

			// recorder : reference
			if (allergyIntolerance.hasRecorder() && allergyIntolerance.getRecorder().hasReference()) {
				String recorderReference = generateFullLocalReference(allergyIntolerance.getRecorder().getReference(), baseUrl);

				Resourcemetadata rRecorder = generateResourcemetadata(resource, chainedResource, chainedParameter+"recorder", recorderReference);
				resourcemetadataList.add(rRecorder);

				if (chainedResource == null) {
					// Add chained parameters for any
					List<Resourcemetadata> rRecorderChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "recorder", 0, allergyIntolerance.getRecorder().getReference());
					resourcemetadataList.addAll(rRecorderChain);
				}
			}

			// type : token
			if (allergyIntolerance.hasType() && allergyIntolerance.getType() != null) {
				Resourcemetadata rType = generateResourcemetadata(resource, chainedResource, chainedParameter+"type", allergyIntolerance.getType().toCode(), allergyIntolerance.getType().getSystem());
				resourcemetadataList.add(rType);
			}

			// AllergyIntolerance.reaction parameters
			if (allergyIntolerance.hasReaction()) {

				Resourcemetadata rCode = null;
				for (AllergyIntoleranceReactionComponent reaction : allergyIntolerance.getReaction()) {

					// reaction.manifestation : token
					if (reaction.hasManifestation()) {

						for (CodeableConcept manifestation : reaction.getManifestation()) {

							if (manifestation.hasCoding()) {
								for (Coding code : manifestation.getCoding()) {
									rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"manifestation", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
									resourcemetadataList.add(rCode);
								}
							}
						}
					}

					// reaction.onset : date
					if (reaction.hasOnset()) {
						Resourcemetadata rDate = generateResourcemetadata(resource, chainedResource, chainedParameter+"onset", utcDateUtil.formatDate(reaction.getOnset(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(reaction.getOnset(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
						resourcemetadataList.add(rDate);
					}

					// reaction.route : token
					if (reaction.hasExposureRoute() && reaction.getExposureRoute().hasCoding()) {

						for (Coding code : reaction.getExposureRoute().getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"route", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}

					// reaction.severity : token
					if (reaction.hasSeverity() && reaction.getSeverity() != null) {
						Resourcemetadata rSeverity = generateResourcemetadata(resource, chainedResource, chainedParameter+"severity", reaction.getSeverity().toCode(), reaction.getSeverity().getSystem());
						resourcemetadataList.add(rSeverity);
					}

					// reaction.substance(code) : token
					if (reaction.hasSubstance() && reaction.getSubstance().hasCoding()) {

						for (Coding code : reaction.getSubstance().getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// verification-status : token
			if (allergyIntolerance.hasVerificationStatus() && allergyIntolerance.getVerificationStatus().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : allergyIntolerance.getVerificationStatus().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"verification-status", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
        } finally {
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