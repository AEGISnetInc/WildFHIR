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
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.ResourceType;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataBundle extends ResourcemetadataProxy {

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
        ByteArrayInputStream iBundle = null;

		try {
            // Extract and convert the resource contents to a Bundle object
			if (chainedResource != null) {
				iBundle = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iBundle = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            Bundle bundle = (Bundle) xmlP.parse(iBundle);
            iBundle.close();

			/*
             * Create new Resourcemetadata objects for each Bundle metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, bundle, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			if (bundle.getId() != null) {
				Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", bundle.getId());
				resourcemetadataList.add(_id);
			}

			// _language : token
			if (bundle.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", bundle.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (bundle.getMeta() != null && bundle.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(bundle.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(bundle.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// identifier : token
			if (bundle.hasIdentifier()) {
				Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", bundle.getIdentifier().getValue(), bundle.getIdentifier().getSystem(), null, ServicesUtil.INSTANCE.getTextValue(bundle.getIdentifier()));
				resourcemetadataList.add(rIdentifier);
			}

			// composition : reference
			// message : reference
			if (bundle.hasEntry()) {
				BundleEntryComponent firstEntry = bundle.getEntry().get(0);

				if (firstEntry.hasResource()) {
					org.hl7.fhir.r4.model.Resource first = firstEntry.getResource();

					if (first != null) {

						String entryReference = null;
						Resourcemetadata rFirst = null;
						// Add reference and chained parameters
						List<Resourcemetadata> rFirstEntryChain = null;
						if (first.getResourceType().equals(ResourceType.Composition)) {
							// Add Composition reference search parameter constructed from Composition.id
							if (first.hasId()) {
								entryReference = "Composition/" + first.getId();
							}
							if (entryReference != null) {
								rFirst = generateResourcemetadata(resource, chainedResource, chainedParameter+"composition", entryReference);
								resourcemetadataList.add(rFirst);
							}

							if (chainedResource == null) {
								// Add Composition chained parameters
								rFirstEntryChain = this.generateChainedResourcemetadata(resource, baseUrl, resourceService, "composition.", ResourceType.Composition.name(), first, null);
								resourcemetadataList.addAll(rFirstEntryChain);
							}
						}
						else if (first.getResourceType().equals(ResourceType.MessageHeader)) {
							// Add MessageHeader reference search parameter constructed from MessageHeader.id
							if (first.hasId()) {
								entryReference = "MessageHeader/" + first.getId();
							}
							if (entryReference != null) {
								rFirst = generateResourcemetadata(resource, chainedResource, chainedParameter+"message", entryReference);
								resourcemetadataList.add(rFirst);
							}

							if (chainedResource == null) {
								// Add MessageHeader chained parameters
								rFirstEntryChain = this.generateChainedResourcemetadata(resource, baseUrl, resourceService, "message.", ResourceType.MessageHeader.name(), first, null);
								resourcemetadataList.addAll(rFirstEntryChain);
							}
						}
					}
				}
			}

			// timestamp : date
			if (bundle.hasTimestamp()) {
				Resourcemetadata rTimestamp = generateResourcemetadata(resource, chainedResource, chainedParameter+"timestamp", utcDateUtil.formatDate(bundle.getTimestamp(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(bundle.getTimestamp(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rTimestamp);
			}

			// type : token
			if (bundle.hasType() && bundle.getType() != null) {
				Resourcemetadata rType = generateResourcemetadata(resource, chainedResource, chainedParameter+"type", bundle.getType().toCode(), bundle.getType().getSystem());
				resourcemetadataList.add(rType);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return resourcemetadataList;
	}

}
