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
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Subscription;

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.UTCDateUtil;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataSubscription extends ResourcemetadataProxy {

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
        ByteArrayInputStream iSubscription = null;

		try {
            // Extract and convert the resource contents to a Subscription object
			if (chainedResource != null) {
				iSubscription = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iSubscription = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            Subscription subscription = (Subscription) xmlP.parse(iSubscription);
            iSubscription.close();

			/*
             * Create new Resourcemetadata objects for each Subscription metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, subscription, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", subscription.getId());
			resourcemetadataList.add(_id);

			// _language : token
			if (subscription.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", subscription.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (subscription.getMeta() != null && subscription.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(subscription.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(subscription.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// contact : token
			if (subscription.hasContact()) {

				for (ContactPoint contact : subscription.getContact()) {
					String contactSystem = null;
					if (contact.hasSystem() && contact.getSystem() != null) {
						contactSystem = contact.getSystem().toCode();
					}
					Resourcemetadata rContact = generateResourcemetadata(resource, chainedResource, chainedParameter+"contact", contact.getValue(), contactSystem);
					resourcemetadataList.add(rContact);
				}
			}

			// criteria : string
			if (subscription.hasCriteria()) {
				Resourcemetadata rCriteria = generateResourcemetadata(resource, chainedResource, chainedParameter+"criteria", subscription.getCriteria());
				resourcemetadataList.add(rCriteria);
			}

			// status : token
			if (subscription.hasStatus() && subscription.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", subscription.getStatus().toCode(), subscription.getStatus().getSystem());
				resourcemetadataList.add(rStatus);
			}

			// payload : string
			// type : token
			// url : uri
			if (subscription.hasChannel()) {

				if (subscription.getChannel().hasPayload()) {
					Resourcemetadata rPayload = generateResourcemetadata(resource, chainedResource, chainedParameter+"payload", subscription.getChannel().getPayload());
					resourcemetadataList.add(rPayload);
				}

				if (subscription.getChannel().hasType() && subscription.getChannel().getType() != null) {
					Resourcemetadata rType = generateResourcemetadata(resource, chainedResource, chainedParameter+"type", subscription.getChannel().getType().toCode(), subscription.getChannel().getType().getSystem());
					resourcemetadataList.add(rType);
				}

				if (subscription.getChannel().hasEndpoint()) {
					Resourcemetadata rEndpoint = generateResourcemetadata(resource, chainedResource, chainedParameter+"url", subscription.getChannel().getEndpoint());
					resourcemetadataList.add(rEndpoint);
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
