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
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.VisionPrescription;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataVisionPrescription extends ResourcemetadataProxy {

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
        ByteArrayInputStream iVisionPrescription = null;

		try {
            // Extract and convert the resource contents to a VisionPrescription object
			if (chainedResource != null) {
				iVisionPrescription = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iVisionPrescription = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            VisionPrescription visionPrescription = (VisionPrescription) xmlP.parse(iVisionPrescription);
            iVisionPrescription.close();

			/*
             * Create new Resourcemetadata objects for each VisionPrescription metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, visionPrescription, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", visionPrescription.getId());
			resourcemetadataList.add(_id);

			// _language : token
			if (visionPrescription.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", visionPrescription.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (visionPrescription.getMeta() != null && visionPrescription.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(visionPrescription.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(visionPrescription.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// datewritten : datetime
			if (visionPrescription.hasDateWritten()) {
				Resourcemetadata rDateWritten = generateResourcemetadata(resource, chainedResource, chainedParameter+"datewritten", utcDateUtil.formatDate(visionPrescription.getDateWritten(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(visionPrescription.getDateWritten(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rDateWritten);
			}

			// encounter : reference
			if (visionPrescription.hasEncounter() && visionPrescription.getEncounter().hasReference()) {
				Resourcemetadata rEncounter = generateResourcemetadata(resource, chainedResource, chainedParameter+"encounter", generateFullLocalReference(visionPrescription.getEncounter().getReference(), baseUrl));
				resourcemetadataList.add(rEncounter);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rEncounterChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "encounter", 0, visionPrescription.getEncounter().getReference());
					resourcemetadataList.addAll(rEncounterChain);
				}
			}

			// identifier : token
			if (visionPrescription.hasIdentifier()) {

				for (Identifier identifier : visionPrescription.getIdentifier()) {
					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// patient : reference
			if (visionPrescription.hasPatient() && visionPrescription.getPatient().hasReference()) {
				Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", generateFullLocalReference(visionPrescription.getPatient().getReference(), baseUrl));
				resourcemetadataList.add(rPatient);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rPatientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, visionPrescription.getPatient().getReference());
					resourcemetadataList.addAll(rPatientChain);
				}
			}

			// prescriber : reference
			if (visionPrescription.hasPrescriber() && visionPrescription.getPrescriber().hasReference()) {
				Resourcemetadata rPrescriber = generateResourcemetadata(resource, chainedResource, chainedParameter+"prescriber", generateFullLocalReference(visionPrescription.getPrescriber().getReference(), baseUrl));
				resourcemetadataList.add(rPrescriber);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rPrescriberChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "prescriber", 0, visionPrescription.getPrescriber().getReference());
					resourcemetadataList.addAll(rPrescriberChain);
				}
			}

			// status : token
			if (visionPrescription.hasStatus() && visionPrescription.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", visionPrescription.getStatus().toCode(), visionPrescription.getStatus().getSystem());
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
