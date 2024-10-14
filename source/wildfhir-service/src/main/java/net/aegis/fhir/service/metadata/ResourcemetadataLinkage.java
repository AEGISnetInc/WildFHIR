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
import net.aegis.fhir.service.util.UTCDateUtil;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.Linkage;
import org.hl7.fhir.r4.model.Linkage.LinkageItemComponent;
import org.hl7.fhir.r4.model.Linkage.LinkageType;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataLinkage extends ResourcemetadataProxy {

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
        ByteArrayInputStream iLinkage = null;

		try {
			// Extract and convert the resource contents to a Linkage object
			if (chainedResource != null) {
				iLinkage = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iLinkage = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			Linkage linkage = (Linkage) xmlP.parse(iLinkage);
			iLinkage.close();

			/*
			 * Create new Resourcemetadata objects for each Linkage metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, linkage, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			if (linkage.getId() != null) {
				Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", linkage.getId());
				resourcemetadataList.add(_id);
			}

			// _language : token
			if (linkage.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", linkage.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (linkage.getMeta() != null && linkage.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(linkage.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(linkage.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// author : reference
			if (linkage.hasAuthor() && linkage.getAuthor().hasReference()) {
				Resourcemetadata rAuthor = generateResourcemetadata(resource, chainedResource, chainedParameter+"author", generateFullLocalReference(linkage.getAuthor().getReference(), baseUrl));
				resourcemetadataList.add(rAuthor);

				if (chainedResource == null) {
					// Add chained parameters for any
					List<Resourcemetadata> rAuthorChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "author", 0, linkage.getAuthor().getReference(), null);
					resourcemetadataList.addAll(rAuthorChain);
				}
			}

			if (linkage.hasItem()) {

				List<Resourcemetadata> rItemChain = null;
				for (LinkageItemComponent item : linkage.getItem()) {

					if (item.hasResource() && item.getResource().hasReference()) {
						String itemReference = generateFullLocalReference(item.getResource().getReference(), baseUrl);

						// item : reference
						Resourcemetadata rItem = generateResourcemetadata(resource, chainedResource, chainedParameter+"item", itemReference);
						resourcemetadataList.add(rItem);

						if (chainedResource == null) {
							// Add chained parameters for any
							rItemChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "item", 0, item.getResource().getReference(), null);
							resourcemetadataList.addAll(rItemChain);
						}

						// source : reference
						if (item.hasType() && item.getType().equals(LinkageType.SOURCE)) {
							Resourcemetadata rSource = generateResourcemetadata(resource, chainedResource, chainedParameter+"source", itemReference);
							resourcemetadataList.add(rSource);

							if (chainedResource == null) {
								// Add chained parameters for any
								rItemChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "source", 0, item.getResource().getReference(), null);
								resourcemetadataList.addAll(rItemChain);
							}
						}
					}
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
