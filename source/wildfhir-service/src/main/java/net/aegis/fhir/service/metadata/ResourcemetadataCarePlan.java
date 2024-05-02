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
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CarePlan;
import org.hl7.fhir.r4.model.CarePlan.CarePlanActivityComponent;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.UriType;

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataCarePlan extends ResourcemetadataProxy {

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
        ByteArrayInputStream iCarePlan = null;

		try {
			// Extract and convert the resource contents to a CarePlan object
			if (chainedResource != null) {
				iCarePlan = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iCarePlan = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			CarePlan carePlan = (CarePlan) xmlP.parse(iCarePlan);
			iCarePlan.close();

			/*
			 * Create new Resourcemetadata objects for each CarePlan metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, carePlan, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", carePlan.getId());
			resourcemetadataList.add(_id);

			// _language : token
			if (carePlan.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", carePlan.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (carePlan.getMeta() != null && carePlan.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(carePlan.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(carePlan.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// carePlan.activity
			if (carePlan.hasActivity()) {

				for (CarePlanActivityComponent activity : carePlan.getActivity()) {

					// carePlan.activity.detail
					if (activity.hasDetail()) {

						// (activity-code) detail.code : token
						if (activity.getDetail().hasCode() && activity.getDetail().getCode().hasCoding()) {
							Resourcemetadata rCode = null;
							for (Coding code : activity.getDetail().getCode().getCoding()) {
								rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"activity-code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
								resourcemetadataList.add(rCode);
							}
						}

						// activity-date : date(period) - activity.detail.scheduledPeriod
						if (activity.getDetail().hasScheduledPeriod()) {
							Period activityDate = activity.getDetail().getScheduledPeriod();

							Resourcemetadata rDateStart = generateResourcemetadata(resource, chainedResource, chainedParameter+"activity-date", utcDateUtil.formatDate(activityDate.getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(activityDate.getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(activityDate.getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(activityDate.getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
							resourcemetadataList.add(rDateStart);
						}

						// detail.performer : reference
						if (activity.getDetail().hasPerformer()) {

							String performerReference = null;
							for (Reference performer : activity.getDetail().getPerformer()) {

								if (performer.hasReference()) {
									performerReference = generateFullLocalReference(performer.getReference(), baseUrl);

									Resourcemetadata rDetailPerformer = generateResourcemetadata(resource, chainedResource, chainedParameter+"performer", performerReference);
									resourcemetadataList.add(rDetailPerformer);

									if (chainedResource == null) {
										// Add chained parameters for any
										List<Resourcemetadata> rPerformerChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "performer", 0, performer.getReference());
										resourcemetadataList.addAll(rPerformerChain);
									}
								}
							}
						}
					}

					// (activity-reference) activity.reference : reference
					if (activity.hasReference() && activity.getReference().hasReference()) {
						String activityreferenceReference = generateFullLocalReference(activity.getReference().getReference(), baseUrl);

						Resourcemetadata rActivityReference = generateResourcemetadata(resource, chainedResource, chainedParameter+"activity-reference", activityreferenceReference);
						resourcemetadataList.add(rActivityReference);

						if (chainedResource == null) {
							// Add chained parameters for any
							List<Resourcemetadata> rActivityReferenceChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "activity-reference", 0, activity.getReference().getReference());
							resourcemetadataList.addAll(rActivityReferenceChain);
						}
					}
				}
			}

			// carePlan.based-on
			if (carePlan.hasBasedOn()) {

				for (Reference basedOn : carePlan.getBasedOn()) {

					// based-on : reference
					if (basedOn.hasReference()) {
						Resourcemetadata rBasedOn = generateResourcemetadata(resource, chainedResource, chainedParameter+"based-on", generateFullLocalReference(basedOn.getReference(), baseUrl));
						resourcemetadataList.add(rBasedOn);

						if (chainedResource == null) {
							// Add chained parameters
							List<Resourcemetadata> rBasedOnChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "based-on", 0, basedOn.getReference());
							resourcemetadataList.addAll(rBasedOnChain);
						}
					}
				}
			}

			// care-team : reference
			if (carePlan.hasCareTeam()) {

				String careteamReference = null;
				for (Reference careteam : carePlan.getAddresses()) {

					if (careteam.hasReference()) {
						careteamReference = generateFullLocalReference(careteam.getReference(), baseUrl);

						Resourcemetadata rCareTeam = generateResourcemetadata(resource, chainedResource, chainedParameter+"care-team", careteamReference);
						resourcemetadataList.add(rCareTeam);

						if (chainedResource == null) {
							// Add chained parameters
							List<Resourcemetadata> rCareTeamChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "care-team", 0, careteam.getReference());
							resourcemetadataList.addAll(rCareTeamChain);
						}
					}
				}
			}

			// category : token
			if (carePlan.hasCategory()) {

				Resourcemetadata rCode = null;
				for (CodeableConcept category : carePlan.getCategory()) {

					if (category.hasCoding()) {
						for (Coding code : category.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"category", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// condition : reference
			if (carePlan.hasAddresses()) {

				String conditionReference = null;
				for (Reference condition : carePlan.getAddresses()) {

					if (condition.hasReference()) {
						conditionReference = generateFullLocalReference(condition.getReference(), baseUrl);

						Resourcemetadata rAddresses = generateResourcemetadata(resource, chainedResource, chainedParameter+"condition", conditionReference);
						resourcemetadataList.add(rAddresses);

						if (chainedResource == null) {
							// Add chained parameters
							List<Resourcemetadata> rAddressesChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "condition", 0, condition.getReference());
							resourcemetadataList.addAll(rAddressesChain);
						}
					}
				}
			}

			// encounter : reference
			if (carePlan.hasEncounter() && carePlan.getEncounter().hasReference()) {
				String encounterReference = generateFullLocalReference(carePlan.getEncounter().getReference(), baseUrl);

				Resourcemetadata rEncounter = generateResourcemetadata(resource, chainedResource, chainedParameter+"encounter", encounterReference);
				resourcemetadataList.add(rEncounter);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rEncounterChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "encounter", 0, carePlan.getEncounter().getReference());
					resourcemetadataList.addAll(rEncounterChain);
				}
			}

			// date : date(period)
			if (carePlan.hasPeriod()) {
				Resourcemetadata rDateStart = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(carePlan.getPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(carePlan.getPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(carePlan.getPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(carePlan.getPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
				resourcemetadataList.add(rDateStart);
			}

			// goal : reference
			if (carePlan.hasGoal()) {

				String goalReference = null;
				for (Reference goal : carePlan.getGoal()) {

					if (goal.hasReference()) {
						goalReference = generateFullLocalReference(goal.getReference(), baseUrl);

						Resourcemetadata rGoal = generateResourcemetadata(resource, chainedResource, chainedParameter+"goal", goalReference);
						resourcemetadataList.add(rGoal);

						if (chainedResource == null) {
							// Add chained parameters
							List<Resourcemetadata> rGoalChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "goal", 0, goal.getReference());
							resourcemetadataList.addAll(rGoalChain);
						}
					}
				}
			}

			// identifier : token
			if (carePlan.hasIdentifier()) {

				for (Identifier identifier : carePlan.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// instantiates-canonical : reference
			if (carePlan.hasInstantiatesCanonical()) {

				for (CanonicalType instantiates : carePlan.getInstantiatesCanonical()) {
					String objectReference = generateFullLocalReference(instantiates.asStringValue(), baseUrl);

					List<Resourcemetadata> rInstantiatesChain = null;
					Resourcemetadata rReference = generateResourcemetadata(resource, chainedResource, chainedParameter+"instantiates-canonical", objectReference);
					resourcemetadataList.add(rReference);

					if (chainedResource == null) {
						// Add chained parameters
						rInstantiatesChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "instantiates-canonical", 0, instantiates.asStringValue());
						resourcemetadataList.addAll(rInstantiatesChain);
					}
				}
			}

			// instantiates-uri : uri
			if (carePlan.hasInstantiatesUri()) {

				for (UriType instantiates : carePlan.getInstantiatesUri()) {

					Resourcemetadata rInstantiates = generateResourcemetadata(resource, chainedResource, chainedParameter+"instantiates-uri", instantiates.asStringValue());
					resourcemetadataList.add(rInstantiates);
				}
			}

			// intent : token
			if (carePlan.hasIntent() && carePlan.getIntent() != null) {
				Resourcemetadata rIntent = generateResourcemetadata(resource, chainedResource, chainedParameter+"intent", carePlan.getIntent().toCode(), carePlan.getIntent().getSystem());
				resourcemetadataList.add(rIntent);
			}

			// carePlan.part-of
			if (carePlan.hasPartOf()) {

				for (Reference partOf : carePlan.getPartOf()) {

					// replaces : reference
					if (partOf.hasReference()) {
						Resourcemetadata rPartOf = generateResourcemetadata(resource, chainedResource, chainedParameter+"part-of", generateFullLocalReference(partOf.getReference(), baseUrl));
						resourcemetadataList.add(rPartOf);

						if (chainedResource == null) {
							// Add chained parameters
							List<Resourcemetadata> rPartOfChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "part-of", 0, partOf.getReference());
							resourcemetadataList.addAll(rPartOfChain);
						}
					}
				}
			}

			// patient : reference
			// subject : reference
			if (carePlan.hasSubject() && carePlan.getSubject().hasReference()) {
				String subjectReference = generateFullLocalReference(carePlan.getSubject().getReference(), baseUrl);

				Resourcemetadata rSubject = generateResourcemetadata(resource, chainedResource, chainedParameter+"subject", subjectReference);
				resourcemetadataList.add(rSubject);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rSubjectChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "subject", 0, carePlan.getSubject().getReference());
					resourcemetadataList.addAll(rSubjectChain);
				}

				if (subjectReference.indexOf("Patient") >= 0) {
					Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", subjectReference);
					resourcemetadataList.add(rPatient);

					if (chainedResource == null) {
						// Add chained parameters
						List<Resourcemetadata> rPatientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, carePlan.getSubject().getReference());
						resourcemetadataList.addAll(rPatientChain);
					}
				}
			}

			// carePlan.replaces
			if (carePlan.hasReplaces()) {

				for (Reference replaces : carePlan.getReplaces()) {

					// replaces : reference
					if (replaces.hasReference()) {
						Resourcemetadata rReplaces = generateResourcemetadata(resource, chainedResource, chainedParameter+"replaces", generateFullLocalReference(replaces.getReference(), baseUrl));
						resourcemetadataList.add(rReplaces);

						if (chainedResource == null) {
							// Add chained parameters
							List<Resourcemetadata> rReplacesChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "replaces", 0, replaces.getReference());
							resourcemetadataList.addAll(rReplacesChain);
						}
					}
				}
			}

			// status : token
			if (carePlan.hasStatus() && carePlan.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", carePlan.getStatus().toCode(), carePlan.getStatus().getSystem());
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
