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
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResearchStudy;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataResearchStudy extends ResourcemetadataProxy {

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
        ByteArrayInputStream iResearchStudy = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

		try {
			// Extract and convert the resource contents to a ResearchStudy object
			if (chainedResource != null) {
				iResearchStudy = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iResearchStudy = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			ResearchStudy researchStudy = (ResearchStudy) xmlP.parse(iResearchStudy);
			iResearchStudy.close();

			/*
			 * Create new Resourcemetadata objects for each ResearchStudy metadata value and add to the resourcemetadataList
			 */

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, researchStudy, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// category : token
			if (researchStudy.hasCategory()) {
				for (CodeableConcept category : researchStudy.getCategory()) {

					if (category.hasCoding()) {
						for (Coding code : category.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"category", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// date : date(period)
			if (researchStudy.hasPeriod()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(researchStudy.getPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(researchStudy.getPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(researchStudy.getPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(researchStudy.getPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
				resourcemetadataList.add(rMetadata);
			}

			// focus : token
			if (researchStudy.hasFocus()) {
				for (CodeableConcept focus : researchStudy.getFocus()) {

					if (focus.hasCoding()) {
						for (Coding code : focus.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"focus", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// identifier : token
			if (researchStudy.hasIdentifier()) {

				for (Identifier identifier : researchStudy.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}

			// keyword : token
			if (researchStudy.hasKeyword()) {
				for (CodeableConcept keyword : researchStudy.getKeyword()) {

					if (keyword.hasCoding()) {
						for (Coding code : keyword.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"keyword", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// location : token
			if (researchStudy.hasLocation()) {
				for (CodeableConcept location : researchStudy.getLocation()) {

					if (location.hasCoding()) {
						for (Coding code : location.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"location", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// partof : reference
			if (researchStudy.hasPartOf()) {

				for (Reference partof : researchStudy.getPartOf()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "partof", 0, partof, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// principalinvestigator : reference
			if (researchStudy.hasPrincipalInvestigator()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "principalinvestigator", 0, researchStudy.getPrincipalInvestigator(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// protocol : reference
			if (researchStudy.hasProtocol()) {

				for (Reference protocol : researchStudy.getProtocol()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "protocol", 0, protocol, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// site : reference
			if (researchStudy.hasSite()) {

				for (Reference site : researchStudy.getSite()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "site", 0, site, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// sponsor : reference
			if (researchStudy.hasSponsor()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "sponsor", 0, researchStudy.getSponsor(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// status : token
			if (researchStudy.hasStatus() && researchStudy.getStatus() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", researchStudy.getStatus().toCode(), researchStudy.getStatus().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// title : string
			if (researchStudy.hasTitle()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"title", researchStudy.getTitle());
				resourcemetadataList.add(rMetadata);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		} finally {
	        rMetadata = null;
	        rMetadataChain = null;
            if (iResearchStudy != null) {
                try {
                	iResearchStudy.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
