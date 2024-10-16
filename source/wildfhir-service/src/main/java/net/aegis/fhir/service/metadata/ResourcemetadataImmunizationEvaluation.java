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
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.ImmunizationEvaluation;

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataImmunizationEvaluation extends ResourcemetadataProxy {

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
        ByteArrayInputStream iImmunizationEvaluation = null;

		try {
			// Extract and convert the resource contents to a ImmunizationEvaluation object
			if (chainedResource != null) {
				iImmunizationEvaluation = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iImmunizationEvaluation = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			ImmunizationEvaluation immunizationEvaluation = (ImmunizationEvaluation) xmlP.parse(iImmunizationEvaluation);
			iImmunizationEvaluation.close();

			/*
			 * Create new Resourcemetadata objects for each ImmunizationEvaluation metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, immunizationEvaluation, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			if (immunizationEvaluation.getId() != null) {
				Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", immunizationEvaluation.getId());
				resourcemetadataList.add(_id);
			}

			// _language : token
			if (immunizationEvaluation.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", immunizationEvaluation.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (immunizationEvaluation.getMeta() != null && immunizationEvaluation.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(immunizationEvaluation.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(immunizationEvaluation.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// date : datetime
			if (immunizationEvaluation.hasDate()) {
				Resourcemetadata rDate = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(immunizationEvaluation.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(immunizationEvaluation.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rDate);
			}

			// dose-status : token
			if (immunizationEvaluation.hasDoseStatus() && immunizationEvaluation.getDoseStatus().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : immunizationEvaluation.getDoseStatus().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"dose-status", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			// identifier : token
			if (immunizationEvaluation.hasIdentifier()) {

				for (Identifier identifier : immunizationEvaluation.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// immunization-event : reference
			if (immunizationEvaluation.hasImmunizationEvent() && immunizationEvaluation.getImmunizationEvent().hasReference()) {
				Resourcemetadata rImmunizationEvent = generateResourcemetadata(resource, chainedResource, chainedParameter+"immunization-event", generateFullLocalReference(immunizationEvaluation.getImmunizationEvent().getReference(), baseUrl));
				resourcemetadataList.add(rImmunizationEvent);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rImmunizationEventChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "immunization-event", 0, immunizationEvaluation.getImmunizationEvent().getReference(), null);
					resourcemetadataList.addAll(rImmunizationEventChain);
				}
			}

			// patient : reference
			if (immunizationEvaluation.hasPatient() && immunizationEvaluation.getPatient().hasReference()) {
				Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", generateFullLocalReference(immunizationEvaluation.getPatient().getReference(), baseUrl));
				resourcemetadataList.add(rPatient);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rPatientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, immunizationEvaluation.getPatient().getReference(), null);
					resourcemetadataList.addAll(rPatientChain);
				}
			}

			// status : token
			if (immunizationEvaluation.hasStatus() && immunizationEvaluation.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", immunizationEvaluation.getStatus().toCode(), immunizationEvaluation.getStatus().getSystem());
				resourcemetadataList.add(rStatus);
			}

			// target-disease : token
			if (immunizationEvaluation.hasTargetDisease() && immunizationEvaluation.getTargetDisease().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : immunizationEvaluation.getTargetDisease().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"target-disease", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
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
