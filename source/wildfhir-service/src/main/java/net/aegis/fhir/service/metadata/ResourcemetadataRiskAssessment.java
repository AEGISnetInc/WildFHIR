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
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.RiskAssessment;
import org.hl7.fhir.r4.model.RiskAssessment.RiskAssessmentPredictionComponent;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataRiskAssessment extends ResourcemetadataProxy {

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
        ByteArrayInputStream iRiskAssessment = null;

		try {
            // Extract and convert the resource contents to a RiskAssessment object
			if (chainedResource != null) {
				iRiskAssessment = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iRiskAssessment = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            RiskAssessment riskAssessment = (RiskAssessment) xmlP.parse(iRiskAssessment);
            iRiskAssessment.close();

			/*
             * Create new Resourcemetadata objects for each RiskAssessment metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, riskAssessment, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			if (riskAssessment.getId() != null) {
				Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", riskAssessment.getId());
				resourcemetadataList.add(_id);
			}

			// _language : token
			if (riskAssessment.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", riskAssessment.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (riskAssessment.getMeta() != null && riskAssessment.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(riskAssessment.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(riskAssessment.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// condition : reference
			if (riskAssessment.hasCondition() && riskAssessment.getCondition().hasReference()) {
				Resourcemetadata rCondition = generateResourcemetadata(resource, chainedResource, chainedParameter+"condition", generateFullLocalReference(riskAssessment.getCondition().getReference(), baseUrl));
				resourcemetadataList.add(rCondition);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rConditionChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "condition", 0, riskAssessment.getCondition().getReference(), null);
					resourcemetadataList.addAll(rConditionChain);
				}
			}

			// date : datetime
			if (riskAssessment.hasOccurrenceDateTimeType()) {
				Resourcemetadata rDate = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(riskAssessment.getOccurrenceDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(riskAssessment.getOccurrenceDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rDate);
			}

			// encounter : reference
			if (riskAssessment.hasEncounter() && riskAssessment.getEncounter().hasReference() && riskAssessment.getEncounter().getReference().contains("Encounter")) {
				Resourcemetadata rEncounter = generateResourcemetadata(resource, chainedResource, chainedParameter+"encounter", generateFullLocalReference(riskAssessment.getEncounter().getReference(), baseUrl));
				resourcemetadataList.add(rEncounter);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rEncounterChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "encounter", 0, riskAssessment.getEncounter().getReference(), null);
					resourcemetadataList.addAll(rEncounterChain);
				}
			}

			// identifier : token
			if (riskAssessment.hasIdentifier()) {

				for (Identifier identifier : riskAssessment.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// method : token
			if (riskAssessment.hasMethod() && riskAssessment.getMethod().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : riskAssessment.getMethod().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"method", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			// patient : reference
			// subject : reference
			if (riskAssessment.hasSubject() && riskAssessment.getSubject().hasReference()) {
				String subjectReference = generateFullLocalReference(riskAssessment.getSubject().getReference(), baseUrl);

				Resourcemetadata rSubject = generateResourcemetadata(resource, chainedResource, chainedParameter+"subject", subjectReference);
				resourcemetadataList.add(rSubject);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rSubjectChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "subject", 0, riskAssessment.getSubject().getReference(), null);
					resourcemetadataList.addAll(rSubjectChain);
				}

				if (subjectReference.indexOf("Patient") >= 0) {
					Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", subjectReference);
					resourcemetadataList.add(rPatient);

					if (chainedResource == null) {
						// Add chained parameters
						List<Resourcemetadata> rPatientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, riskAssessment.getSubject().getReference(), null);
						resourcemetadataList.addAll(rPatientChain);
					}
				}
			}

			// performer : reference
			if (riskAssessment.hasPerformer() && riskAssessment.getPerformer().hasReference()) {
				Resourcemetadata rPerformer = generateResourcemetadata(resource, chainedResource, chainedParameter+"performer", generateFullLocalReference(riskAssessment.getPerformer().getReference(), baseUrl));
				resourcemetadataList.add(rPerformer);

				if (chainedResource == null) {
					// Add chained parameters for any
					List<Resourcemetadata> rPerformerChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "performer", 0, riskAssessment.getPerformer().getReference(), null);
					resourcemetadataList.addAll(rPerformerChain);
				}
			}

			// probability : number
			// risk : token
			if (riskAssessment.hasPrediction()) {

				for (RiskAssessmentPredictionComponent prediction : riskAssessment.getPrediction()) {

					if (prediction.hasProbabilityDecimalType()) {
						Resourcemetadata rProbability = generateResourcemetadata(resource, chainedResource, chainedParameter+"probability", prediction.getProbabilityDecimalType().getValueAsString());
						resourcemetadataList.add(rProbability);
					}
					else if (prediction.hasProbabilityRange() && prediction.getProbabilityRange().hasHigh()) {
						Resourcemetadata rProbability = generateResourcemetadata(resource, chainedResource, chainedParameter+"probability", prediction.getProbabilityRange().getHigh().primitiveValue());
						resourcemetadataList.add(rProbability);
					}

					if (prediction.hasQualitativeRisk() && prediction.getQualitativeRisk().hasCoding()) {

						Resourcemetadata rCode = null;
						for (Coding code : prediction.getQualitativeRisk().getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"risk", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
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
