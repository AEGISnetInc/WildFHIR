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
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.PaymentNotice;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataPaymentNotice extends ResourcemetadataProxy {

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
        ByteArrayInputStream iPaymentNotice = null;

		try {
            // Extract and convert the resource contents to a PaymentNotice object
			if (chainedResource != null) {
				iPaymentNotice = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iPaymentNotice = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            PaymentNotice paymentNotice = (PaymentNotice) xmlP.parse(iPaymentNotice);
            iPaymentNotice.close();

			/*
             * Create new Resourcemetadata objects for each PaymentNotice metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, paymentNotice, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", paymentNotice.getId());
			resourcemetadataList.add(_id);

			// _language : token
			if (paymentNotice.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", paymentNotice.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (paymentNotice.getMeta() != null && paymentNotice.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(paymentNotice.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(paymentNotice.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// created : date
			if (paymentNotice.hasCreated()) {
				Resourcemetadata rDate = generateResourcemetadata(resource, chainedResource, chainedParameter+"created", utcDateUtil.formatDate(paymentNotice.getCreated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(paymentNotice.getCreated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rDate);
			}

			// identifier : token
			if (paymentNotice.hasIdentifier()) {

				for (Identifier identifier : paymentNotice.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// payment-status : token
			if (paymentNotice.hasPaymentStatus() && paymentNotice.getPaymentStatus().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : paymentNotice.getPaymentStatus().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"payment-status", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			// provider : reference
			if (paymentNotice.hasProvider()) {
				Resourcemetadata rProviderReference = generateResourcemetadata(resource, chainedResource, chainedParameter+"provider", generateFullLocalReference(paymentNotice.getProvider().getReference(), baseUrl));
				resourcemetadataList.add(rProviderReference);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rProviderChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "provider", 0, paymentNotice.getProvider().getReference());
					resourcemetadataList.addAll(rProviderChain);
				}
			}

			// request : reference
			if (paymentNotice.hasRequest()) {
				Resourcemetadata rRequestReference = generateResourcemetadata(resource, chainedResource, chainedParameter+"request", generateFullLocalReference(paymentNotice.getRequest().getReference(), baseUrl));
				resourcemetadataList.add(rRequestReference);

				if (chainedResource == null) {
					// Add chained parameters for any
					List<Resourcemetadata> rRequestChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "request", 0, paymentNotice.getRequest().getReference());
					resourcemetadataList.addAll(rRequestChain);
				}
			}

			// response : reference
			if (paymentNotice.hasResponse()) {
				Resourcemetadata rResponseReference = generateResourcemetadata(resource, chainedResource, chainedParameter+"response", generateFullLocalReference(paymentNotice.getResponse().getReference(), baseUrl));
				resourcemetadataList.add(rResponseReference);

				if (chainedResource == null) {
					// Add chained parameters for any
					List<Resourcemetadata> rResponseChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "response", 0, paymentNotice.getResponse().getReference());
					resourcemetadataList.addAll(rResponseChain);
				}
			}

			// status : token
			if (paymentNotice.hasStatus() && paymentNotice.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", paymentNotice.getStatus().toCode(), paymentNotice.getStatus().getSystem());
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
