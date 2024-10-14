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
import org.hl7.fhir.r4.model.CoverageEligibilityRequest;
import org.hl7.fhir.r4.model.Identifier;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataCoverageEligibilityRequest extends ResourcemetadataProxy {

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
        ByteArrayInputStream iCoverageEligibilityRequest = null;

		try {
            // Extract and convert the resource contents to a CoverageEligibilityRequest object
			if (chainedResource != null) {
				iCoverageEligibilityRequest = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iCoverageEligibilityRequest = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            CoverageEligibilityRequest coverageEligibilityRequest = (CoverageEligibilityRequest) xmlP.parse(iCoverageEligibilityRequest);
            iCoverageEligibilityRequest.close();

			/*
             * Create new Resourcemetadata objects for each CoverageEligibilityRequest metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, coverageEligibilityRequest, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			if (coverageEligibilityRequest.getId() != null) {
				Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", coverageEligibilityRequest.getId());
				resourcemetadataList.add(_id);
			}

			// _language : token
			if (coverageEligibilityRequest.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", coverageEligibilityRequest.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (coverageEligibilityRequest.getMeta() != null && coverageEligibilityRequest.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(coverageEligibilityRequest.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(coverageEligibilityRequest.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// created : date
			if (coverageEligibilityRequest.hasCreated()) {
				Resourcemetadata rDate = generateResourcemetadata(resource, chainedResource, chainedParameter+"created", utcDateUtil.formatDate(coverageEligibilityRequest.getCreated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(coverageEligibilityRequest.getCreated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rDate);
			}

			// enterer : reference
			if (coverageEligibilityRequest.hasEnterer() && coverageEligibilityRequest.getEnterer().hasReference()) {
				Resourcemetadata rEntererReference = generateResourcemetadata(resource, chainedResource, chainedParameter+"enterer", generateFullLocalReference(coverageEligibilityRequest.getEnterer().getReference(), baseUrl));
				resourcemetadataList.add(rEntererReference);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rEntererChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "enterer", 0, coverageEligibilityRequest.getEnterer().getReference(), null);
					resourcemetadataList.addAll(rEntererChain);
				}
			}

			// facility : reference
			if (coverageEligibilityRequest.hasFacility() && coverageEligibilityRequest.getFacility().hasReference()) {
				Resourcemetadata rFacilityReference = generateResourcemetadata(resource, chainedResource, chainedParameter+"facility", generateFullLocalReference(coverageEligibilityRequest.getFacility().getReference(), baseUrl));
				resourcemetadataList.add(rFacilityReference);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rFacilityChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "facility", 0, coverageEligibilityRequest.getFacility().getReference(), null);
					resourcemetadataList.addAll(rFacilityChain);
				}
			}

			// identifier : token
			if (coverageEligibilityRequest.hasIdentifier()) {

				for (Identifier identifier : coverageEligibilityRequest.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier[" + 0 + "]", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// patient : reference
			if (coverageEligibilityRequest.hasPatient() && coverageEligibilityRequest.getPatient().hasReference()) {
				Resourcemetadata rPatientReference = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", generateFullLocalReference(coverageEligibilityRequest.getPatient().getReference(), baseUrl));
				resourcemetadataList.add(rPatientReference);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rPatientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, coverageEligibilityRequest.getPatient().getReference(), null);
					resourcemetadataList.addAll(rPatientChain);
				}
			}

			// provider : reference
			if (coverageEligibilityRequest.hasProvider() && coverageEligibilityRequest.getProvider().hasReference()) {
				Resourcemetadata rProviderReference = generateResourcemetadata(resource, chainedResource, chainedParameter+"provider", generateFullLocalReference(coverageEligibilityRequest.getProvider().getReference(), baseUrl));
				resourcemetadataList.add(rProviderReference);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rProviderChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "provider", 0, coverageEligibilityRequest.getProvider().getReference(), null);
					resourcemetadataList.addAll(rProviderChain);
				}
			}

			// status : token
			if (coverageEligibilityRequest.hasStatus() && coverageEligibilityRequest.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", coverageEligibilityRequest.getStatus().toCode(), coverageEligibilityRequest.getStatus().getSystem());
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
