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
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r4.model.Reference;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataQuestionnaireResponse extends ResourcemetadataProxy {

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
        ByteArrayInputStream iQuestionnaireResponse = null;

		try {
            // Extract and convert the resource contents to a QuestionnaireResponse object
			if (chainedResource != null) {
				iQuestionnaireResponse = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iQuestionnaireResponse = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            QuestionnaireResponse questionnaireResponse = (QuestionnaireResponse) xmlP.parse(iQuestionnaireResponse);
            iQuestionnaireResponse.close();

			/*
             * Create new Resourcemetadata objects for each QuestionnaireResponse metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, questionnaireResponse, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", questionnaireResponse.getId());
			resourcemetadataList.add(_id);

			// _language : token
			if (questionnaireResponse.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", questionnaireResponse.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (questionnaireResponse.getMeta() != null && questionnaireResponse.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(questionnaireResponse.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(questionnaireResponse.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// author : reference
			if (questionnaireResponse.hasAuthor() && questionnaireResponse.getAuthor().hasReference()) {
				Resourcemetadata rAuthor = generateResourcemetadata(resource, chainedResource, chainedParameter+"author", questionnaireResponse.getAuthor().getReference());
				resourcemetadataList.add(rAuthor);

				if (chainedResource == null) {
					// Add chained parameters for any
					List<Resourcemetadata> rAuthorChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "author", 0, questionnaireResponse.getAuthor().getReference());
					resourcemetadataList.addAll(rAuthorChain);
				}
			}

			// authored : datetime
			if (questionnaireResponse.hasAuthored()) {
				Resourcemetadata rAuthored = generateResourcemetadata(resource, chainedResource, chainedParameter+"authored", utcDateUtil.formatDate(questionnaireResponse.getAuthored(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(questionnaireResponse.getAuthored(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rAuthored);
			}

			// based-on : reference
			if (questionnaireResponse.hasBasedOn()) {

				List<Resourcemetadata> rBasedOnChain = null;
				for (Reference basedOn : questionnaireResponse.getBasedOn()) {

					if (basedOn.hasReference()) {
						Resourcemetadata rBasedOn = generateResourcemetadata(resource, chainedResource, chainedParameter+"based-on", generateFullLocalReference(basedOn.getReference(), baseUrl));
						resourcemetadataList.add(rBasedOn);

						if (chainedResource == null) {
							// Add chained parameters for any
							rBasedOnChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "based-on", 0, basedOn.getReference());
							resourcemetadataList.addAll(rBasedOnChain);
						}
					}
				}
			}

			// encounter : reference
			if (questionnaireResponse.hasEncounter() && questionnaireResponse.getEncounter().hasReference()) {
				Resourcemetadata rEncounter = generateResourcemetadata(resource, chainedResource, chainedParameter+"encounter", generateFullLocalReference(questionnaireResponse.getEncounter().getReference(), baseUrl));
				resourcemetadataList.add(rEncounter);

				if (chainedResource == null) {
					// Add chained parameters for any
					List<Resourcemetadata> rEncounterChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "encounter", 0, questionnaireResponse.getEncounter().getReference());
					resourcemetadataList.addAll(rEncounterChain);
				}
			}

			// identifier : token
			if (questionnaireResponse.hasIdentifier() && questionnaireResponse.getIdentifier().hasValue()) {
				Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", questionnaireResponse.getIdentifier().getValue(), questionnaireResponse.getIdentifier().getSystem(), null, ServicesUtil.INSTANCE.getTextValue(questionnaireResponse.getIdentifier()));
				resourcemetadataList.add(rIdentifier);
			}

			// part-of : reference
			if (questionnaireResponse.hasPartOf()) {

				List<Resourcemetadata> rPartOfChain = null;
				for (Reference partOf : questionnaireResponse.getPartOf()) {

					if (partOf.hasReference()) {
						Resourcemetadata rPartOf = generateResourcemetadata(resource, chainedResource, chainedParameter+"part-of", generateFullLocalReference(partOf.getReference(), baseUrl));
						resourcemetadataList.add(rPartOf);

						if (chainedResource == null) {
							// Add chained parameters for any
							rPartOfChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "part-of", 0, partOf.getReference());
							resourcemetadataList.addAll(rPartOfChain);
						}
					}
				}
			}

			// questionnaire : reference
			if (questionnaireResponse.hasQuestionnaire()) {
				Resourcemetadata rQuestionnaire = generateResourcemetadata(resource, chainedResource, chainedParameter+"questionnaire", generateFullLocalReference(questionnaireResponse.getQuestionnaire(), baseUrl));
				resourcemetadataList.add(rQuestionnaire);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rQuestionnaireChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "questionnaire", 0,  questionnaireResponse.getQuestionnaire());
					resourcemetadataList.addAll(rQuestionnaireChain);
				}
			}

			// patient : reference
			// subject : reference
			if (questionnaireResponse.hasSubject() && questionnaireResponse.getSubject().hasReference()) {
				String subjectReference = generateFullLocalReference(questionnaireResponse.getSubject().getReference(), baseUrl);

				Resourcemetadata rSubject = generateResourcemetadata(resource, chainedResource, chainedParameter+"subject", subjectReference);
				resourcemetadataList.add(rSubject);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rSubjectChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "subject", 0, questionnaireResponse.getSubject().getReference());
					resourcemetadataList.addAll(rSubjectChain);
				}

				if (subjectReference.indexOf("Patient") >= 0) {
					Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", subjectReference);
					resourcemetadataList.add(rPatient);

					if (chainedResource == null) {
						// Add chained parameters
						List<Resourcemetadata> rPatientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, questionnaireResponse.getSubject().getReference());
						resourcemetadataList.addAll(rPatientChain);
					}
				}
			}

			// source : reference
			if (questionnaireResponse.hasSource() && questionnaireResponse.getSource().hasReference()) {
				Resourcemetadata rSource = generateResourcemetadata(resource, chainedResource, chainedParameter+"source", generateFullLocalReference(questionnaireResponse.getSource().getReference(), baseUrl));
				resourcemetadataList.add(rSource);

				if (chainedResource == null) {
					// Add chained parameters for any
					List<Resourcemetadata> rSourceChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "source", 0, questionnaireResponse.getSource().getReference());
					resourcemetadataList.addAll(rSourceChain);
				}
			}

			// status : token
			if (questionnaireResponse.hasStatus() && questionnaireResponse.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", questionnaireResponse.getStatus().toCode(), questionnaireResponse.getStatus().getSystem());
				resourcemetadataList.add(rStatus);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return resourcemetadataList;
	}

}
