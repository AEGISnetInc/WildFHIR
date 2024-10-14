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
import org.hl7.fhir.r4.model.ClaimResponse;
import org.hl7.fhir.r4.model.Identifier;

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataClaimResponse extends ResourcemetadataProxy {

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
        ByteArrayInputStream iClaimResponse = null;

		try {
            // Extract and convert the resource contents to a ClaimResponse object
			if (chainedResource != null) {
				iClaimResponse = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iClaimResponse = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            ClaimResponse claimResponse = (ClaimResponse) xmlP.parse(iClaimResponse);
            iClaimResponse.close();

			/*
             * Create new Resourcemetadata objects for each ClaimResponse metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, claimResponse, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			if (claimResponse.getId() != null) {
				Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", claimResponse.getId());
				resourcemetadataList.add(_id);
			}

			// _language : token
			if (claimResponse.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", claimResponse.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (claimResponse.getMeta() != null && claimResponse.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(claimResponse.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(claimResponse.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// created : date
			if (claimResponse.hasCreated()) {
				Resourcemetadata rCreated = generateResourcemetadata(resource, chainedResource, chainedParameter+"created", utcDateUtil.formatDate(claimResponse.getCreated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(claimResponse.getCreated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rCreated);
			}

			// disposition : string
			if (claimResponse.hasDisposition()) {
				Resourcemetadata rDisposition = generateResourcemetadata(resource, chainedResource, chainedParameter+"disposition", claimResponse.getDisposition());
				resourcemetadataList.add(rDisposition);
			}

			// identifier : token
			if (claimResponse.hasIdentifier()) {

				for (Identifier identifier : claimResponse.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// insurer : reference
			if (claimResponse.hasInsurer() && claimResponse.getInsurer().hasReference()) {

				Resourcemetadata rInsurer = generateResourcemetadata(resource, chainedResource, chainedParameter + "insurer", generateFullLocalReference(claimResponse.getInsurer().getReference(), baseUrl));
				resourcemetadataList.add(rInsurer);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rInsurerChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "insurer", 0, claimResponse.getInsurer().getReference(), null);
					resourcemetadataList.addAll(rInsurerChain);
				}
			}

			// outcome : token
			if (claimResponse.hasOutcome() && claimResponse.getOutcome() != null) {
				Resourcemetadata rOutcome = generateResourcemetadata(resource, chainedResource, chainedParameter+"outcome", claimResponse.getOutcome().toCode(), claimResponse.getOutcome().getSystem());
				resourcemetadataList.add(rOutcome);
			}

			// patient : reference
			if (claimResponse.hasPatient() && claimResponse.getPatient().hasReference()) {

				Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter + "patient", generateFullLocalReference(claimResponse.getPatient().getReference(), baseUrl));
				resourcemetadataList.add(rPatient);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rPatientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, claimResponse.getPatient().getReference(), null);
					resourcemetadataList.addAll(rPatientChain);
				}
			}

			// payment-date : date
			if (claimResponse.hasPayment() && claimResponse.getPayment().hasDate()) {
				Resourcemetadata rPaymentDate = generateResourcemetadata(resource, chainedResource, chainedParameter+"payment-date", utcDateUtil.formatDate(claimResponse.getPayment().getDate(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(claimResponse.getPayment().getDate(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rPaymentDate);
			}

			// request : reference
			if (claimResponse.hasRequest() && claimResponse.getRequest().hasReference()) {
				Resourcemetadata rRequestReference = generateResourcemetadata(resource, chainedResource, chainedParameter+"request", generateFullLocalReference(claimResponse.getRequest().getReference(), baseUrl));
				resourcemetadataList.add(rRequestReference);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rRequestReferenceChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "request", 0, claimResponse.getRequest().getReference(), null);
					resourcemetadataList.addAll(rRequestReferenceChain);
				}
			}

			// requestor : reference
			if (claimResponse.hasRequestor() && claimResponse.getRequestor().hasReference()) {

				Resourcemetadata rRequestor = generateResourcemetadata(resource, chainedResource, chainedParameter + "requestor", generateFullLocalReference(claimResponse.getRequestor().getReference(), baseUrl));
				resourcemetadataList.add(rRequestor);

				if (chainedResource == null) {
					// Add chained parameters for any
					List<Resourcemetadata> rRequestProviderChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "requestor", 0, claimResponse.getRequestor().getReference(), null);
					resourcemetadataList.addAll(rRequestProviderChain);
				}
			}

			// status : token
			if (claimResponse.hasStatus() && claimResponse.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", claimResponse.getStatus().toCode(), claimResponse.getStatus().getSystem());
				resourcemetadataList.add(rStatus);
			}

			// use : token
			if (claimResponse.hasUse() && claimResponse.getUse() != null) {
				Resourcemetadata rUse = generateResourcemetadata(resource, chainedResource, chainedParameter+"use", claimResponse.getUse().toCode(), claimResponse.getUse().getSystem());
				resourcemetadataList.add(rUse);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return resourcemetadataList;
	}

}
