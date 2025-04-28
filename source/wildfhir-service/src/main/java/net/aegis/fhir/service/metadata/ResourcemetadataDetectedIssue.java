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

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DetectedIssue;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataDetectedIssue extends ResourcemetadataProxy {

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
        ByteArrayInputStream iDetectedIssue = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

		try {
            // Extract and convert the resource contents to a DetectedIssue object
			if (chainedResource != null) {
				iDetectedIssue = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iDetectedIssue = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            DetectedIssue detectedIssue = (DetectedIssue) xmlP.parse(iDetectedIssue);
            iDetectedIssue.close();

			/*
             * Create new Resourcemetadata objects for each DetectedIssue metadata value and add to the resourcemetadataList
			 */

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, detectedIssue, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// author : reference
			if (detectedIssue.hasAuthor()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "author", 0, detectedIssue.getAuthor(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// code : token
			if (detectedIssue.hasCode() && detectedIssue.getCode().hasCoding()) {

				for (Coding code : detectedIssue.getCode().getCoding()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rMetadata);
				}
			}

			// identified : datetime
			if (detectedIssue.hasIdentifiedDateTimeType()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identified", utcDateUtil.formatDate(detectedIssue.getIdentifiedDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(detectedIssue.getIdentifiedDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rMetadata);
			}
			// identified : period
			else if (detectedIssue.hasIdentifiedPeriod()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identified", utcDateUtil.formatDate(detectedIssue.getIdentifiedPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(detectedIssue.getIdentifiedPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(detectedIssue.getIdentifiedPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(detectedIssue.getIdentifiedPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
				resourcemetadataList.add(rMetadata);
			}

			// identifier : token
			if (detectedIssue.hasIdentifier()) {

				for (Identifier identifier : detectedIssue.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}

			// implicated : reference
			if (detectedIssue.hasImplicated()) {

				for (Reference implicated : detectedIssue.getImplicated()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "implicated", 0, implicated, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// patient : reference
			if (detectedIssue.hasPatient()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "patient", 0, detectedIssue.getPatient(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		} finally {
	        rMetadata = null;
	        rMetadataChain = null;
            if (iDetectedIssue != null) {
                try {
                	iDetectedIssue.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
