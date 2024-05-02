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
import org.hl7.fhir.r4.model.CoverageEligibilityResponse;
import org.hl7.fhir.r4.model.Identifier;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataCoverageEligibilityResponse extends ResourcemetadataProxy {

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
        ByteArrayInputStream iCoverageEligibilityResponse = null;

		try {
            // Extract and convert the resource contents to a CoverageEligibilityResponse object
			if (chainedResource != null) {
				iCoverageEligibilityResponse = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iCoverageEligibilityResponse = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            CoverageEligibilityResponse coverageEligibilityResponse = (CoverageEligibilityResponse) xmlP.parse(iCoverageEligibilityResponse);
            iCoverageEligibilityResponse.close();

			/*
             * Create new Resourcemetadata objects for each CoverageEligibilityResponse metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, coverageEligibilityResponse, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", coverageEligibilityResponse.getId());
			resourcemetadataList.add(_id);

			// _language : token
			if (coverageEligibilityResponse.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", coverageEligibilityResponse.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (coverageEligibilityResponse.getMeta() != null && coverageEligibilityResponse.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(coverageEligibilityResponse.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(coverageEligibilityResponse.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// created : date
			if (coverageEligibilityResponse.hasCreated()) {
				Resourcemetadata rDate = generateResourcemetadata(resource, chainedResource, chainedParameter+"created", utcDateUtil.formatDate(coverageEligibilityResponse.getCreated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(coverageEligibilityResponse.getCreated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rDate);
			}

			// disposition : string
			if (coverageEligibilityResponse.hasDisposition()) {
				Resourcemetadata rDisposition = generateResourcemetadata(resource, chainedResource, chainedParameter+"disposition", coverageEligibilityResponse.getDisposition());
				resourcemetadataList.add(rDisposition);
			}

			// identifier : token
			if (coverageEligibilityResponse.hasIdentifier()) {

				for (Identifier identifier : coverageEligibilityResponse.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// insurer : reference
			if (coverageEligibilityResponse.hasInsurer() && coverageEligibilityResponse.getInsurer().hasReference()) {
				Resourcemetadata rInsurer = generateResourcemetadata(resource, chainedResource, chainedParameter+"insurer", generateFullLocalReference(coverageEligibilityResponse.getInsurer().getReference(), baseUrl));
				resourcemetadataList.add(rInsurer);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rInsurerChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "insurer", 0, coverageEligibilityResponse.getInsurer().getReference());
					resourcemetadataList.addAll(rInsurerChain);
				}
			}

			// outcome : token
			if (coverageEligibilityResponse.hasOutcome() && coverageEligibilityResponse.getOutcome() != null) {
				Resourcemetadata rOutcome = generateResourcemetadata(resource, chainedResource, chainedParameter+"outcome", coverageEligibilityResponse.getOutcome().toCode(), coverageEligibilityResponse.getOutcome().getSystem());
				resourcemetadataList.add(rOutcome);
			}

			// patient : reference
			if (coverageEligibilityResponse.hasPatient() && coverageEligibilityResponse.getPatient().hasReference()) {
				Resourcemetadata rPatientReference = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", generateFullLocalReference(coverageEligibilityResponse.getPatient().getReference(), baseUrl));
				resourcemetadataList.add(rPatientReference);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rPatientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, coverageEligibilityResponse.getPatient().getReference());
					resourcemetadataList.addAll(rPatientChain);
				}
			}

			// request : reference
			if (coverageEligibilityResponse.hasRequest() && coverageEligibilityResponse.getRequest().hasReference()) {
				Resourcemetadata rRequestReference = generateResourcemetadata(resource, chainedResource, chainedParameter+"request", generateFullLocalReference(coverageEligibilityResponse.getRequest().getReference(), baseUrl));
				resourcemetadataList.add(rRequestReference);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rRequestChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "request", 0,  coverageEligibilityResponse.getRequest().getReference());
					resourcemetadataList.addAll(rRequestChain);
				}
			}

			// requestor : reference
			if (coverageEligibilityResponse.hasRequestor() && coverageEligibilityResponse.getRequestor().hasReference()) {
					Resourcemetadata rRequestorReference = generateResourcemetadata(resource, chainedResource, chainedParameter+"requestor", generateFullLocalReference(coverageEligibilityResponse.getRequestor().getReference(), baseUrl));
					resourcemetadataList.add(rRequestorReference);

					if (chainedResource == null) {
						// Add chained parameters
						List<Resourcemetadata> rRequestProviderChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "requestor", 0, coverageEligibilityResponse.getRequestor().getReference());
						resourcemetadataList.addAll(rRequestProviderChain);
					}
			}

			// status : token
			if (coverageEligibilityResponse.hasStatus() && coverageEligibilityResponse.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", coverageEligibilityResponse.getStatus().toCode(), coverageEligibilityResponse.getStatus().getSystem());
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
