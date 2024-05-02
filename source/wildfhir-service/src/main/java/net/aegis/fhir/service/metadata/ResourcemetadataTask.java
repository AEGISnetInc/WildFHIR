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
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataTask extends ResourcemetadataProxy {

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
        ByteArrayInputStream iTask = null;

		try {
            // Extract and convert the resource contents to a Task object
			if (chainedResource != null) {
				iTask = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iTask = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            Task task = (Task) xmlP.parse(iTask);
            iTask.close();

			/*
             * Create new Resourcemetadata objects for each Task metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, task, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", task.getId());
			resourcemetadataList.add(_id);

			// _language : token
			if (task.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", task.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (task.getMeta() != null && task.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(task.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(task.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// authored-on : date
			if (task.hasAuthoredOn()) {
				Resourcemetadata rAuthoredOn = generateResourcemetadata(resource, chainedResource, chainedParameter+"authored-on", utcDateUtil.formatDate(task.getAuthoredOn(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(task.getAuthoredOn(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rAuthoredOn);
			}

			// based-on : reference
			if (task.hasBasedOn()) {

				List<Resourcemetadata> rBasedOnChain = null;
				for (Reference basedOn : task.getBasedOn()) {

					if (basedOn.hasReference()) {
						Resourcemetadata rBasedOn = generateResourcemetadata(resource, chainedResource, chainedParameter+"based-on", generateFullLocalReference(basedOn.getReference(), baseUrl));
						resourcemetadataList.add(rBasedOn);

						if (chainedResource == null) {
							// Add chained parameters
							rBasedOnChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "based-on", 0, task.getFocus().getReference());
							resourcemetadataList.addAll(rBasedOnChain);
						}
					}
				}
			}

			// business-status : token
			if (task.hasBusinessStatus() && task.getBusinessStatus().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : task.getBusinessStatus().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"business-status", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			// code : token
			if (task.hasCode() && task.getCode().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : task.getCode().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			// encounter : reference
			if (task.hasEncounter() && task.getEncounter().hasReference()) {
				Resourcemetadata rEncounter = generateResourcemetadata(resource, chainedResource, chainedParameter+"encounter", generateFullLocalReference(task.getEncounter().getReference(), baseUrl));
				resourcemetadataList.add(rEncounter);

				if (chainedResource == null) {
					// Add chained parameters for any
					List<Resourcemetadata> rEncounterChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "encounter", 0, task.getEncounter().getReference());
					resourcemetadataList.addAll(rEncounterChain);
				}
			}

			// focus : reference
			if (task.hasFocus() && task.getFocus().hasReference()) {
				Resourcemetadata rFocus = generateResourcemetadata(resource, chainedResource, chainedParameter+"focus", generateFullLocalReference(task.getFocus().getReference(), baseUrl));
				resourcemetadataList.add(rFocus);

				if (chainedResource == null) {
					// Add chained parameters for any
					List<Resourcemetadata> rFocusChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "focus", 0, task.getFocus().getReference());
					resourcemetadataList.addAll(rFocusChain);
				}
			}

			// group-identifier : token
			if (task.hasGroupIdentifier()) {
				Resourcemetadata rGroupIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"group-identifier", task.getGroupIdentifier().getValue(), task.getGroupIdentifier().getSystem(), null, ServicesUtil.INSTANCE.getTextValue(task.getGroupIdentifier()));
				resourcemetadataList.add(rGroupIdentifier);
			}

			// identifier : token
			if (task.hasIdentifier()) {

				for (Identifier identifier : task.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// intent : token
			if (task.hasIntent() && task.getIntent() != null) {
				Resourcemetadata rIntent = generateResourcemetadata(resource, chainedResource, chainedParameter+"intent", task.getIntent().toCode(), task.getIntent().getSystem());
				resourcemetadataList.add(rIntent);
			}

			// modified : date
			if (task.hasLastModified()) {
				Resourcemetadata rLastModified = generateResourcemetadata(resource, chainedResource, chainedParameter+"modified", utcDateUtil.formatDate(task.getLastModified(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(task.getLastModified(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rLastModified);
			}

			// owner : reference
			if (task.hasOwner() && task.getOwner().hasReference()) {
				Resourcemetadata rOwner = generateResourcemetadata(resource, chainedResource, chainedParameter+"owner", generateFullLocalReference(task.getOwner().getReference(), baseUrl));
				resourcemetadataList.add(rOwner);

				if (chainedResource == null) {
					// Add chained parameters for any
					List<Resourcemetadata> rOwnerChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "owner", 0, task.getOwner().getReference());
					resourcemetadataList.addAll(rOwnerChain);
				}
			}

			// part-of : reference
			if (task.hasPartOf()) {

				List<Resourcemetadata> rPartOfChain = null;
				for (Reference partOf : task.getPartOf()) {

					if (partOf.hasReference()) {
						Resourcemetadata rPartOf = generateResourcemetadata(resource, chainedResource, chainedParameter+"part-of", generateFullLocalReference(partOf.getReference(), baseUrl));
						resourcemetadataList.add(rPartOf);

						if (chainedResource == null) {
							// Add chained parameters
							rPartOfChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "part-of", 0, partOf.getReference());
							resourcemetadataList.addAll(rPartOfChain);
						}
					}
				}
			}

			// patient : reference
			// subject : reference
			if (task.hasFor() && task.getFor().hasReference()) {
				String subjectReference = generateFullLocalReference(task.getFor().getReference(), baseUrl);

				Resourcemetadata rSubject = generateResourcemetadata(resource, chainedResource, chainedParameter+"subject", subjectReference);
				resourcemetadataList.add(rSubject);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rSubjectChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "subject", 0, task.getFor().getReference());
					resourcemetadataList.addAll(rSubjectChain);
				}

				if (subjectReference.indexOf("Patient") >= 0) {
					Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", subjectReference);
					resourcemetadataList.add(rPatient);

					if (chainedResource == null) {
						// Add chained parameters
						List<Resourcemetadata> rPatientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, task.getFor().getReference());
						resourcemetadataList.addAll(rPatientChain);
					}
				}
			}

			// performer : token
			if (task.hasPerformerType()) {

				Resourcemetadata rCode = null;
				for (CodeableConcept performer : task.getPerformerType()) {

					if (performer.hasCoding()) {
						for (Coding code : performer.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"performer", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// period : date(period)
			if (task.hasExecutionPeriod()) {
				Resourcemetadata rPeriod = generateResourcemetadata(resource, chainedResource, chainedParameter+"period", utcDateUtil.formatDate(task.getExecutionPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(task.getExecutionPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), null, null, "PERIOD");
				resourcemetadataList.add(rPeriod);
			}

			// priority : token
			if (task.hasPriority() && task.getPriority() != null) {
				Resourcemetadata rPriority = generateResourcemetadata(resource, chainedResource, chainedParameter+"priority", task.getPriority().toCode(), task.getPriority().getSystem());
				resourcemetadataList.add(rPriority);
			}

			// requester : reference
			if (task.hasRequester() && task.getRequester().hasReference()) {
				Resourcemetadata rRequester = generateResourcemetadata(resource, chainedResource, chainedParameter+"requester", generateFullLocalReference(task.getRequester().getReference(), baseUrl));
				resourcemetadataList.add(rRequester);

				if (chainedResource == null) {
					// Add chained parameters for any
					List<Resourcemetadata> rRequesterChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "requester", 0, task.getRequester().getReference());
					resourcemetadataList.addAll(rRequesterChain);
				}
			}

			// status : token
			if (task.hasStatus() && task.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", task.getStatus().toCode(), task.getStatus().getSystem());
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
