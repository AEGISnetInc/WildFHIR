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
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.ListResource;
import org.hl7.fhir.r4.model.ListResource.ListEntryComponent;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataList extends ResourcemetadataProxy {

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
        ByteArrayInputStream iList = null;

		try {
            // Extract and convert the resource contents to a List_ object
			if (chainedResource != null) {
				iList = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iList = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			ListResource list = (ListResource) xmlP.parse(iList);
			iList.close();

			/*
			 * Create new Resourcemetadata objects for each List metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, list, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			if (list.getId() != null) {
				Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", list.getId());
				resourcemetadataList.add(_id);
			}

			// _language : token
			if (list.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", list.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (list.getMeta() != null && list.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(list.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(list.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// code : token
			if (list.hasCode() && list.getCode().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : list.getCode().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);

				}
			}

			// date : datetime
			if (list.hasDate()) {
				Resourcemetadata rDate = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(list.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(list.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rDate);
			}

			// empty-reason : token
			if (list.hasEmptyReason() && list.getEmptyReason().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : list.getEmptyReason().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"empty-reason", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);

				}
			}

			// encounter : reference
			if (list.hasEncounter() && list.getEncounter().hasReference()) {
				Resourcemetadata rEncounter = generateResourcemetadata(resource, chainedResource, chainedParameter+"encounter", generateFullLocalReference(list.getEncounter().getReference(), baseUrl));
				resourcemetadataList.add(rEncounter);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rEncounterChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "encounter", 0, list.getEncounter().getReference(), null);
					resourcemetadataList.addAll(rEncounterChain);
				}
			}

			// identifier : token
			if (list.hasIdentifier()) {

				for (Identifier identifier : list.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// item : reference
			if (list.hasEntry()) {

				List<Resourcemetadata> rItemChain = null;
				for (ListEntryComponent entry : list.getEntry()) {

					if (entry.hasItem() && entry.getItem().hasReference()) {
						Resourcemetadata rItem = generateResourcemetadata(resource, chainedResource, chainedParameter+"item", generateFullLocalReference(entry.getItem().getReference(), baseUrl));
						resourcemetadataList.add(rItem);

						if (chainedResource == null) {
							// Add chained parameters for any
							rItemChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "item", 0, entry.getItem().getReference(), null);
							resourcemetadataList.addAll(rItemChain);
						}
					}
				}
			}

			// notes : string
			if (list.hasNote()) {

				for (Annotation notes : list.getNote()) {

					if (notes.hasText()) {
						Resourcemetadata rNote = generateResourcemetadata(resource, chainedResource, chainedParameter+"notes", notes.getText());
						resourcemetadataList.add(rNote);
					}
				}
			}

			// patient : reference
			// subject : reference
			if (list.hasSubject() && list.getSubject().hasReference()) {
				String subjectReference = generateFullLocalReference(list.getSubject().getReference(), baseUrl);

				Resourcemetadata rSubject = generateResourcemetadata(resource, chainedResource, chainedParameter+"subject", subjectReference);
				resourcemetadataList.add(rSubject);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rSubjectChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "subject", 0, list.getSubject().getReference(), null);
					resourcemetadataList.addAll(rSubjectChain);
				}

				if (subjectReference.indexOf("Patient") >= 0) {
					Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", subjectReference);
					resourcemetadataList.add(rPatient);

					if (chainedResource == null) {
						// Add chained parameters
						List<Resourcemetadata> rPatientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, list.getSubject().getReference(), null);
						resourcemetadataList.addAll(rPatientChain);
					}
				}
			}

			// source : reference
			if (list.hasSource() && list.getSource().hasReference()) {
				Resourcemetadata rSource = generateResourcemetadata(resource, chainedResource, chainedParameter+"source", generateFullLocalReference(list.getSource().getReference(), baseUrl));
				resourcemetadataList.add(rSource);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rSourceChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "source", 0, list.getSource().getReference(), null);
					resourcemetadataList.addAll(rSourceChain);
				}
			}

			// status : token
			if (list.hasStatus() && list.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", list.getStatus().toCode(), list.getStatus().getSystem());
				resourcemetadataList.add(rStatus);
			}

			// title : string
			if (list.hasTitle()) {
				Resourcemetadata rTitle = generateResourcemetadata(resource, chainedResource, chainedParameter+"title", list.getTitle());
				resourcemetadataList.add(rTitle);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return resourcemetadataList;
	}

}
