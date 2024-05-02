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
import org.hl7.fhir.r4.model.ImmunizationRecommendation;
import org.hl7.fhir.r4.model.ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent;
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
public class ResourcemetadataImmunizationRecommendation extends ResourcemetadataProxy {

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
        ByteArrayInputStream iImmunizationRecommendation = null;

		try {
			// Extract and convert the resource contents to a ImmunizationRecommendation object
			if (chainedResource != null) {
				iImmunizationRecommendation = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iImmunizationRecommendation = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			ImmunizationRecommendation immunizationRecommendation = (ImmunizationRecommendation) xmlP.parse(iImmunizationRecommendation);
			iImmunizationRecommendation.close();

			/*
			 * Create new Resourcemetadata objects for each ImmunizationRecommendation metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, immunizationRecommendation, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", immunizationRecommendation.getId());
			resourcemetadataList.add(_id);

			// _language : token
			if (immunizationRecommendation.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", immunizationRecommendation.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (immunizationRecommendation.getMeta() != null && immunizationRecommendation.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(immunizationRecommendation.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(immunizationRecommendation.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// date : datetime
			if (immunizationRecommendation.hasDate()) {
				Resourcemetadata rDate = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(immunizationRecommendation.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(immunizationRecommendation.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rDate);
			}

			// identifier : token
			if (immunizationRecommendation.hasIdentifier()) {

				for (Identifier identifier : immunizationRecommendation.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// patient : reference
			if (immunizationRecommendation.hasPatient() && immunizationRecommendation.getPatient().hasReference()) {
				Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", generateFullLocalReference(immunizationRecommendation.getPatient().getReference(), baseUrl));
				resourcemetadataList.add(rPatient);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rPatientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, immunizationRecommendation.getPatient().getReference());
					resourcemetadataList.addAll(rPatientChain);
				}
			}

			if (immunizationRecommendation.hasRecommendation()) {

				Resourcemetadata rCode = null;
				for (ImmunizationRecommendationRecommendationComponent recommendation : immunizationRecommendation.getRecommendation()) {

					// information : reference
					if (recommendation.hasSupportingPatientInformation()) {

						List<Resourcemetadata> rInformationChain = null;
						for (Reference supportingPatientInformation : recommendation.getSupportingPatientInformation()) {

							if (supportingPatientInformation.hasReference()) {
								Resourcemetadata rSupportingPatientInformation = generateResourcemetadata(resource, chainedResource, chainedParameter+"information", generateFullLocalReference(supportingPatientInformation.getReference(), baseUrl));
								resourcemetadataList.add(rSupportingPatientInformation);

								if (chainedResource == null) {
									// Add chained parameters for any
									rInformationChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "information", 0, supportingPatientInformation.getReference());
									resourcemetadataList.addAll(rInformationChain);
								}
							}
						}
					}

					// status : token
					if (recommendation.hasForecastStatus() && recommendation.getForecastStatus().hasCoding()) {

						for (Coding code : recommendation.getForecastStatus().getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}

					// support : reference
					if (recommendation.hasSupportingImmunization()) {

						List<Resourcemetadata> rSupportingInformationChain = null;
						for (Reference supportingImmunization : recommendation.getSupportingImmunization()) {

							if (supportingImmunization.hasReference()) {
								Resourcemetadata rSupportingImmunization = generateResourcemetadata(resource, chainedResource, chainedParameter+"support", generateFullLocalReference(supportingImmunization.getReference(), baseUrl));
								resourcemetadataList.add(rSupportingImmunization);

								if (chainedResource == null) {
									// Add chained parameters
									rSupportingInformationChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "support", 0, supportingImmunization.getReference());
									resourcemetadataList.addAll(rSupportingInformationChain);
								}
							}
						}
					}

					// vaccine-type : token
					if (recommendation.hasVaccineCode()) {

						for (CodeableConcept vaccineCode : recommendation.getVaccineCode()) {

							if (vaccineCode.hasCoding()) {
								for (Coding code : vaccineCode.getCoding()) {
									rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"vaccine-type", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
									resourcemetadataList.add(rCode);
								}
							}
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
