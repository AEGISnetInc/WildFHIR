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
        ByteArrayInputStream iResearchStudy = null;

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

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, researchStudy, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", researchStudy.getId());
			resourcemetadataList.add(_id);

			// _language : token
			if (researchStudy.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", researchStudy.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (researchStudy.getMeta() != null && researchStudy.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(researchStudy.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(researchStudy.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// category : token
			if (researchStudy.hasCategory()) {

				Resourcemetadata rCode = null;
				for (CodeableConcept category : researchStudy.getCategory()) {

					if (category.hasCoding()) {
						for (Coding code : category.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"category", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// date : date(period)
			if (researchStudy.hasPeriod()) {
				Resourcemetadata rDate = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(researchStudy.getPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(researchStudy.getPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(researchStudy.getPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(researchStudy.getPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
				resourcemetadataList.add(rDate);
			}

			// focus : token
			if (researchStudy.hasFocus()) {

				Resourcemetadata rCode = null;
				for (CodeableConcept focus : researchStudy.getFocus()) {

					if (focus.hasCoding()) {
						for (Coding code : focus.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"focus", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// identifier : token
			if (researchStudy.hasIdentifier()) {

				for (Identifier identifier : researchStudy.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// keyword : token
			if (researchStudy.hasKeyword()) {

				Resourcemetadata rCode = null;
				for (CodeableConcept keyword : researchStudy.getKeyword()) {

					if (keyword.hasCoding()) {
						for (Coding code : keyword.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"keyword", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// location : token
			if (researchStudy.hasLocation()) {

				Resourcemetadata rCode = null;
				for (CodeableConcept location : researchStudy.getLocation()) {

					if (location.hasCoding()) {
						for (Coding code : location.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"location", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// partof : reference
			if (researchStudy.hasPartOf()) {

				Resourcemetadata rPartOf = null;
				List<Resourcemetadata> rPartOfChain = null;

				for (Reference partof : researchStudy.getPartOf()) {
					if (partof.hasReference()) {
						rPartOf = generateResourcemetadata(resource, chainedResource, chainedParameter + "partof", generateFullLocalReference(partof.getReference(), baseUrl));
						resourcemetadataList.add(rPartOf);

						if (chainedResource == null) {
							// Add chained parameters for any
							rPartOfChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "partof", 0, partof.getReference());
							resourcemetadataList.addAll(rPartOfChain);
						}
					}
				}
			}

			// principalinvestigator : reference
			if (researchStudy.hasPrincipalInvestigator()) {

				if (researchStudy.getPrincipalInvestigator().hasReference()) {
					Resourcemetadata rPrincipalInvestigator = generateResourcemetadata(resource, chainedResource, chainedParameter+"principalinvestigator", generateFullLocalReference(researchStudy.getPrincipalInvestigator().getReference(), baseUrl));
					resourcemetadataList.add(rPrincipalInvestigator);

					if (chainedResource == null) {
						// Add chained parameters for any
						List<Resourcemetadata> rPrincipalInvestigatorChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "principalinvestigator", 0, researchStudy.getPrincipalInvestigator().getReference());
						resourcemetadataList.addAll(rPrincipalInvestigatorChain);
					}
				}
			}

			// protocol : reference
			if (researchStudy.hasProtocol()) {

				Resourcemetadata rProtocol = null;
				List<Resourcemetadata> rProtocolChain = null;

				for (Reference protocol : researchStudy.getProtocol()) {
					if (protocol.hasReference()) {
						rProtocol = generateResourcemetadata(resource, chainedResource, chainedParameter + "protocol", generateFullLocalReference(protocol.getReference(), baseUrl));
						resourcemetadataList.add(rProtocol);

						if (chainedResource == null) {
							// Add chained parameters for any
							rProtocolChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "protocol", 0, protocol.getReference());
							resourcemetadataList.addAll(rProtocolChain);
						}
					}
				}
			}

			// site : reference
			if (researchStudy.hasSite()) {

				Resourcemetadata rSite = null;
				List<Resourcemetadata> rSiteChain = null;

				for (Reference site : researchStudy.getSite()) {
					if (site.hasReference()) {
						rSite = generateResourcemetadata(resource, chainedResource, chainedParameter + "site", generateFullLocalReference(site.getReference(), baseUrl));
						resourcemetadataList.add(rSite);

						if (chainedResource == null) {
							// Add chained parameters for any
							rSiteChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "site", 0, site.getReference());
							resourcemetadataList.addAll(rSiteChain);
						}
					}
				}
			}

			// sponsor : reference
			if (researchStudy.hasSponsor()) {

				if (researchStudy.getSponsor().hasReference()) {
					Resourcemetadata rSponsor = generateResourcemetadata(resource, chainedResource, chainedParameter+"sponsor", generateFullLocalReference(researchStudy.getSponsor().getReference(), baseUrl));
					resourcemetadataList.add(rSponsor);

					if (chainedResource == null) {
						// Add chained parameters for any
						List<Resourcemetadata> rSponsorChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "sponsor", 0, researchStudy.getSponsor().getReference());
						resourcemetadataList.addAll(rSponsorChain);
					}
				}
			}

			// status : token
			if (researchStudy.hasStatus() && researchStudy.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", researchStudy.getStatus().toCode(), researchStudy.getStatus().getSystem());
				resourcemetadataList.add(rStatus);
			}

			// title : string
			if (researchStudy.hasTitle()) {
				Resourcemetadata rTitle = generateResourcemetadata(resource, chainedResource, chainedParameter+"title", researchStudy.getTitle());
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
