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
import org.hl7.fhir.r4.model.Slot;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataSlot extends ResourcemetadataProxy {

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
        ByteArrayInputStream iSlot = null;

		try {
            // Extract and convert the resource contents to a Slot object
			if (chainedResource != null) {
				iSlot = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iSlot = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            Slot slot = (Slot) xmlP.parse(iSlot);
            iSlot.close();

			/*
             * Create new Resourcemetadata objects for each Slot metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, slot, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			if (slot.getId() != null) {
				Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", slot.getId());
				resourcemetadataList.add(_id);
			}

			// _language : token
			if (slot.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", slot.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (slot.getMeta() != null && slot.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(slot.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(slot.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// appointment-type : token
			if (slot.hasAppointmentType() && slot.getAppointmentType().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : slot.getAppointmentType().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"appointment-type", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			// identifier : token
			if (slot.hasIdentifier()) {

				for (Identifier identifier : slot.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// schedule : reference
			if (slot.hasSchedule() && slot.getSchedule().hasReference()) {
				Resourcemetadata rSchedule = generateResourcemetadata(resource, chainedResource, chainedParameter+"schedule", generateFullLocalReference(slot.getSchedule().getReference(), baseUrl));
				resourcemetadataList.add(rSchedule);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rScheduleChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "schedule", 0, slot.getSchedule().getReference(), null);
					resourcemetadataList.addAll(rScheduleChain);
				}
			}

			// service-category : token
			if (slot.hasServiceCategory()) {

				Resourcemetadata rCode = null;
				for (CodeableConcept category : slot.getServiceCategory()) {

					if (category.hasCoding()) {
						for (Coding code : category.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"service-category", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// service-type : token
			if (slot.hasServiceType()) {

				Resourcemetadata rType = null;
				for (CodeableConcept slotType : slot.getServiceType()) {

					if (slotType.hasCoding()) {
						for (Coding type : slotType.getCoding()) {
							rType = generateResourcemetadata(resource, chainedResource, chainedParameter+"service-type", type.getCode(), type.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(type));
							resourcemetadataList.add(rType);
						}
					}
				}
			}

			// specialty : token
			if (slot.hasSpecialty()) {

				Resourcemetadata rCode = null;
				for (CodeableConcept specialty : slot.getSpecialty()) {

					if (specialty.hasCoding()) {
						for (Coding code : specialty.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"specialty", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// start : datetime
			if (slot.hasStart()) {
				Resourcemetadata rStart = generateResourcemetadata(resource, chainedResource, chainedParameter+"start", utcDateUtil.formatDate(slot.getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(slot.getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rStart);
			}

			// status : token
			if (slot.hasStatus() && slot.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", slot.getStatus().toCode(), slot.getStatus().getSystem());
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
