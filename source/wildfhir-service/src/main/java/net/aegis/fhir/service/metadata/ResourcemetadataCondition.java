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
import org.hl7.fhir.r4.model.Age;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Condition.ConditionEvidenceComponent;
import org.hl7.fhir.r4.model.Condition.ConditionStageComponent;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Range;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataCondition extends ResourcemetadataProxy {

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
        ByteArrayInputStream iCondition = null;
		Resourcemetadata rCode = null;

		try {
			// Extract and convert the resource contents to a Condition object
			if (chainedResource != null) {
				iCondition = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iCondition = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			Condition condition = (Condition) xmlP.parse(iCondition);
			iCondition.close();

			/*
			 * Create new Resourcemetadata objects for each Condition metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, condition, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			if (condition.getId() != null) {
				Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", condition.getId());
				resourcemetadataList.add(_id);
			}

			// _language : token
			if (condition.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", condition.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (condition.getMeta() != null && condition.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(condition.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(condition.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// abatement-age : age(range)
			// abatement-date : datetime(period)
			// abatement-string : string
			if (condition.hasAbatement()) {

				if (condition.getAbatement() instanceof DateTimeType) {
					Resourcemetadata rAbatementDateTimeType = generateResourcemetadata(resource, chainedResource, chainedParameter+"abatement-date", utcDateUtil.formatDate(condition.getAbatementDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(condition.getAbatementDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
					resourcemetadataList.add(rAbatementDateTimeType);
				}
				else if (condition.getAbatement() instanceof Period) {
					Resourcemetadata rAbatementPeriod = generateResourcemetadata(resource, chainedResource, chainedParameter+"abatement-date", utcDateUtil.formatDate(condition.getAbatementPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(condition.getAbatementPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(condition.getAbatementPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(condition.getAbatementPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
					resourcemetadataList.add(rAbatementPeriod);
				}
				else if (condition.getAbatement() instanceof Age) {
					Resourcemetadata rAbatementAge = generateResourcemetadata(resource, chainedResource, chainedParameter+"abatement-age", condition.getAbatementAge().getValueElement().asStringValue());
					resourcemetadataList.add(rAbatementAge);
				}
				else if (condition.getAbatement() instanceof Range) {
					Resourcemetadata rAbatementRange = generateResourcemetadata(resource, chainedResource, chainedParameter+"abatement-age", condition.getAbatementRange().getLow().getValueElement().asStringValue());
					resourcemetadataList.add(rAbatementRange);
				}
				else if (condition.getAbatement() instanceof StringType) {
					Resourcemetadata rAbatementStringType = generateResourcemetadata(resource, chainedResource, chainedParameter+"abatement-string", condition.getAbatementStringType().asStringValue());
					resourcemetadataList.add(rAbatementStringType);
				}
			}

			// asserter : reference
			if (condition.hasAsserter() && condition.getAsserter().hasReference()) {
				String asserterString = generateFullLocalReference(condition.getAsserter().getReference(), baseUrl);

				Resourcemetadata rAsserter = generateResourcemetadata(resource, chainedResource, chainedParameter+"asserter", asserterString);
				resourcemetadataList.add(rAsserter);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rAsserterChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "asserter", 0, condition.getAsserter().getReference(), null);
					resourcemetadataList.addAll(rAsserterChain);
				}
			}

			// body-site : token
			if (condition.hasBodySite()) {

				for (CodeableConcept bodySite : condition.getBodySite()) {

					if (bodySite.hasCoding()) {
						for (Coding code : bodySite.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"body-site", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// category : token
			if (condition.hasCategory()) {

				for (CodeableConcept category : condition.getCategory()) {

					if (category.hasCoding()) {
						for (Coding code : category.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"category", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// clinical-status : token
			if (condition.hasClinicalStatus() && condition.getClinicalStatus().hasCoding()) {

				for (Coding code : condition.getClinicalStatus().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"clinical-status", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			// code : token
			if (condition.hasCode() && condition.getCode().hasCoding()) {

				for (Coding code : condition.getCode().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			// encounter : reference
			if (condition.hasEncounter() && condition.getEncounter().hasReference()) {
				String encounterReference = generateFullLocalReference(condition.getEncounter().getReference(), baseUrl);

				Resourcemetadata rEncounter = generateResourcemetadata(resource, chainedResource, chainedParameter+"encounter", encounterReference);
				resourcemetadataList.add(rEncounter);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rEncounterChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "encounter", 0, condition.getEncounter().getReference(), null);
					resourcemetadataList.addAll(rEncounterChain);
				}
			}

			// evidence : token
			// evidence-detail : reference
			if (condition.hasEvidence()) {

				String detailReference = null;
				for (ConditionEvidenceComponent evidence : condition.getEvidence()) {

					if (evidence.hasCode()) {

						for (CodeableConcept evidenceCode : evidence.getCode()) {

							if (evidenceCode.hasCoding()) {
								for (Coding code : evidenceCode.getCoding()) {
									rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"evidence", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
									resourcemetadataList.add(rCode);
								}
							}
						}
					}

					if (evidence.hasDetail()) {

						for (Reference detail : evidence.getDetail()) {

							if (detail.hasReference()) {
								detailReference = generateFullLocalReference(detail.getReference(), baseUrl);

								Resourcemetadata rDetail = generateResourcemetadata(resource, chainedResource, chainedParameter+"evidence-detail", detailReference);
								resourcemetadataList.add(rDetail);

								if (chainedResource == null) {
									// Add chained parameters for any
									List<Resourcemetadata> rDetailChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "evidence-detail", 02, detail.getReference(), null);
									resourcemetadataList.addAll(rDetailChain);
								}
							}
						}
					}
				}
			}

			// identifier : token
			if (condition.hasIdentifier()) {

				for (Identifier identifier : condition.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// onset-age : age(range)
			// onset-date : datetime(period)
			// onset-info : string
			if (condition.hasOnset()) {

				if (condition.getOnset() instanceof DateTimeType) {
					Resourcemetadata rOnsetDateTimeType = generateResourcemetadata(resource, chainedResource, chainedParameter+"onset-date", utcDateUtil.formatDate(condition.getOnsetDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(condition.getOnsetDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
					resourcemetadataList.add(rOnsetDateTimeType);
				}
				else if (condition.getOnset() instanceof Period) {
					Resourcemetadata rOnsetPeriod = generateResourcemetadata(resource, chainedResource, chainedParameter+"onset-date", utcDateUtil.formatDate(condition.getOnsetPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(condition.getOnsetPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(condition.getOnsetPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(condition.getOnsetPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
					resourcemetadataList.add(rOnsetPeriod);
				}
				else if (condition.getOnset() instanceof Age) {
					Resourcemetadata rOnsetAge = generateResourcemetadata(resource, chainedResource, chainedParameter+"onset-age", condition.getOnsetAge().getValueElement().asStringValue());
					resourcemetadataList.add(rOnsetAge);
				}
				else if (condition.getOnset() instanceof Range) {
					Resourcemetadata rOnsetRange = generateResourcemetadata(resource, chainedResource, chainedParameter+"onset-age", condition.getOnsetRange().getLow().getValueElement().asStringValue());
					resourcemetadataList.add(rOnsetRange);
				}
				else if (condition.getOnset() instanceof StringType) {
					Resourcemetadata rOnsetStringType = generateResourcemetadata(resource, chainedResource, chainedParameter+"onset-info", condition.getOnsetStringType().asStringValue());
					resourcemetadataList.add(rOnsetStringType);
				}
			}

			// patient : reference
			// subject : reference
			if (condition.hasSubject() && condition.getSubject().hasReference()) {
				String subjectReference = generateFullLocalReference(condition.getSubject().getReference(), baseUrl);

				Resourcemetadata rSubject = generateResourcemetadata(resource, chainedResource, chainedParameter+"subject", subjectReference);
				resourcemetadataList.add(rSubject);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rSubjectChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "subject", 0, condition.getSubject().getReference(), null);
					resourcemetadataList.addAll(rSubjectChain);
				}

				if (subjectReference.indexOf("Patient") >= 0) {
					Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", subjectReference);
					resourcemetadataList.add(rPatient);

					if (chainedResource == null) {
						// Add chained parameters
						List<Resourcemetadata> rPatientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, condition.getSubject().getReference(), null);
						resourcemetadataList.addAll(rPatientChain);
					}
				}
			}

			// recorded-date : date
			if (condition.hasRecordedDate()) {
				Resourcemetadata rDate = generateResourcemetadata(resource, chainedResource, chainedParameter+"recorded-date", utcDateUtil.formatDate(condition.getRecordedDate(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(condition.getRecordedDate(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rDate);
			}

			// severity : token
			if (condition.hasSeverity() && condition.getSeverity().hasCoding()) {

				for (Coding code : condition.getSeverity().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"severity", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			// stage : token
			if (condition.hasStage()) {

				for (ConditionStageComponent stage : condition.getStage()) {

					if (stage.hasSummary() && stage.getSummary().hasCoding()) {
						for (Coding code : stage.getSummary().getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"stage", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// verification-status : token
			if (condition.hasVerificationStatus() && condition.getVerificationStatus().hasCoding()) {

				for (Coding code : condition.getVerificationStatus().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"verification-status", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
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
