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
import org.hl7.fhir.r4.model.AdverseEvent;
import org.hl7.fhir.r4.model.AdverseEvent.AdverseEventSuspectEntityComponent;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
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
public class ResourcemetadataAdverseEvent extends ResourcemetadataProxy {

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
        ByteArrayInputStream iAdverseEvent = null;

		try {
			// Extract and convert the resource contents to a AdverseEvent object
			if (chainedResource != null) {
				iAdverseEvent = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iAdverseEvent = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			AdverseEvent adverseEvent = (AdverseEvent) xmlP.parse(iAdverseEvent);
			iAdverseEvent.close();

			/*
			 * Create new Resourcemetadata objects for each AdverseEvent metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, adverseEvent, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", adverseEvent.getId());
			resourcemetadataList.add(_id);

			// _language : token
			if (adverseEvent.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", adverseEvent.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (adverseEvent.getMeta() != null && adverseEvent.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(adverseEvent.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(adverseEvent.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// actuality : token
			if (adverseEvent.hasActuality() && adverseEvent.getActuality() != null) {
				Resourcemetadata rActuality = generateResourcemetadata(resource, chainedResource, chainedParameter+"actuality", adverseEvent.getActuality().toCode(), adverseEvent.getActuality().getSystem());
				resourcemetadataList.add(rActuality);
			}

			// category : token
			if (adverseEvent.hasCategory()) {

				Resourcemetadata rCategory = null;
				for (CodeableConcept category : adverseEvent.getCategory()) {
					if (category.hasCoding()) {
						for (Coding code : category.getCoding()) {
							rCategory = generateResourcemetadata(resource, chainedResource, chainedParameter+"category", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCategory);
						}
					}
				}
			}

			// date : date
			if (adverseEvent.hasDate()) {
				Resourcemetadata rDate = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(adverseEvent.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(adverseEvent.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rDate);
			}

			// event : token
			if (adverseEvent.hasEvent() && adverseEvent.getEvent().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : adverseEvent.getEvent().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"event", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			// identifier : token
			if (adverseEvent.hasIdentifier()) {
				Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", adverseEvent.getIdentifier().getValue(), adverseEvent.getIdentifier().getSystem(), null, ServicesUtil.INSTANCE.getTextValue(adverseEvent.getIdentifier()));
				resourcemetadataList.add(rIdentifier);
			}

			// location : reference
			if (adverseEvent.hasLocation() && adverseEvent.getLocation().hasReference()) {
				String locationReference = generateFullLocalReference(adverseEvent.getLocation().getReference(), baseUrl);

				Resourcemetadata rLocation = generateResourcemetadata(resource, chainedResource, chainedParameter+"location", locationReference);
				resourcemetadataList.add(rLocation);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rLocationChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "location", 0, adverseEvent.getLocation().getReference());
					resourcemetadataList.addAll(rLocationChain);
				}
			}

			// recorder : reference
			if (adverseEvent.hasRecorder() && adverseEvent.getRecorder().hasReference()) {
				String recorderReference = generateFullLocalReference(adverseEvent.getRecorder().getReference(), baseUrl);

				Resourcemetadata rRecorder = generateResourcemetadata(resource, chainedResource, chainedParameter+"recorder", recorderReference);
				resourcemetadataList.add(rRecorder);

				if (chainedResource == null) {
					// Add chained parameters for any
					List<Resourcemetadata> rRecorderChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "recorder", 0, adverseEvent.getRecorder().getReference());
					resourcemetadataList.addAll(rRecorderChain);
				}
			}

			// resultingcondition : reference
			if (adverseEvent.hasResultingCondition()) {

				String resultingconditionReference = null;
				Resourcemetadata rResultingCondition = null;
				for (Reference resultingcondition : adverseEvent.getResultingCondition()) {

					if (resultingcondition.hasReference()) {
						resultingconditionReference = generateFullLocalReference(resultingcondition.getReference(), baseUrl);

						rResultingCondition = generateResourcemetadata(resource, chainedResource, chainedParameter+"reaction", resultingconditionReference);
						resourcemetadataList.add(rResultingCondition);

						if (chainedResource == null) {
							// Add chained parameters
							List<Resourcemetadata> rResultingConditionChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "resultingcondition", 0, resultingcondition.getReference());
							resourcemetadataList.addAll(rResultingConditionChain);
						}
					}
				}
			}

			// seriousness : token
			if (adverseEvent.hasSeriousness() && adverseEvent.getSeriousness().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : adverseEvent.getSeriousness().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"seriousness", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			// severity : token
			if (adverseEvent.hasSeverity() && adverseEvent.getSeverity().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : adverseEvent.getSeverity().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"severity", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			// study : reference
			if (adverseEvent.hasStudy()) {

				String studyReference = null;
				Resourcemetadata rStudy = null;
				for (Reference study : adverseEvent.getStudy()) {

					if (study.hasReference()) {
						studyReference = generateFullLocalReference(study.getReference(), baseUrl);

						rStudy = generateResourcemetadata(resource, chainedResource, chainedParameter+"study", studyReference);
						resourcemetadataList.add(rStudy);

						if (chainedResource == null) {
							// Add chained parameters
							List<Resourcemetadata> rStudyChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "study", 0, study.getReference());
							resourcemetadataList.addAll(rStudyChain);
						}
					}
				}
			}

			// subject : reference
			if (adverseEvent.hasSubject() && adverseEvent.getSubject().hasReference()) {
				String subjectReference = generateFullLocalReference(adverseEvent.getSubject().getReference(), baseUrl);
				List<Resourcemetadata> rSubjectChain = null;

				Resourcemetadata rSubject = generateResourcemetadata(resource, chainedResource, chainedParameter+"subject", subjectReference);
				resourcemetadataList.add(rSubject);

				if (chainedResource == null) {
					// Add chained parameters for any
					rSubjectChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "subject", 0, adverseEvent.getSubject().getReference());
					resourcemetadataList.addAll(rSubjectChain);
				}
			}

			// substance : reference
			if (adverseEvent.hasSuspectEntity()) {

				String substanceReference = null;
				Resourcemetadata rSubstance = null;
				for (AdverseEventSuspectEntityComponent suspectEntity : adverseEvent.getSuspectEntity()) {

					if (suspectEntity.hasInstance() && suspectEntity.getInstance().hasReference()) {
						substanceReference = generateFullLocalReference(suspectEntity.getInstance().getReference(), baseUrl);

						rSubstance = generateResourcemetadata(resource, chainedResource, chainedParameter+"substance", substanceReference);
						resourcemetadataList.add(rSubstance);

						if (chainedResource == null) {
							// Add chained parameters for any
							List<Resourcemetadata> rSubstanceChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "substance", 0, suspectEntity.getInstance().getReference());
							resourcemetadataList.addAll(rSubstanceChain);
						}
					}
				}
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
        } finally {
            if (iAdverseEvent != null) {
                try {
                	iAdverseEvent.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
