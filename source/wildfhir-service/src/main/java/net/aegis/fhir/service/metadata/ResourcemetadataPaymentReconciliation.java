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
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.PaymentReconciliation;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataPaymentReconciliation extends ResourcemetadataProxy {

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
        ByteArrayInputStream iPaymentReconciliation = null;

		try {
            // Extract and convert the resource contents to a PaymentReconciliation object
			if (chainedResource != null) {
				iPaymentReconciliation = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iPaymentReconciliation = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            PaymentReconciliation paymentReconciliation = (PaymentReconciliation) xmlP.parse(iPaymentReconciliation);
            iPaymentReconciliation.close();

			/*
             * Create new Resourcemetadata objects for each PaymentReconciliation metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, paymentReconciliation, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			if (paymentReconciliation.getId() != null) {
				Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", paymentReconciliation.getId());
				resourcemetadataList.add(_id);
			}

			// _language : token
			if (paymentReconciliation.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", paymentReconciliation.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (paymentReconciliation.getMeta() != null && paymentReconciliation.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(paymentReconciliation.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(paymentReconciliation.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// created : date
			if (paymentReconciliation.hasCreated()) {
				Resourcemetadata rDate = generateResourcemetadata(resource, chainedResource, chainedParameter+"created", utcDateUtil.formatDate(paymentReconciliation.getCreated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(paymentReconciliation.getCreated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rDate);
			}

			// disposition : string
			if (paymentReconciliation.hasDisposition()) {
				Resourcemetadata rDisposition = generateResourcemetadata(resource, chainedResource, chainedParameter+"disposition", paymentReconciliation.getDisposition());
				resourcemetadataList.add(rDisposition);
			}

			// identifier : token
			if (paymentReconciliation.hasIdentifier()) {

				for (Identifier identifier : paymentReconciliation.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// outcome : token
			if (paymentReconciliation.hasOutcome() && paymentReconciliation.getOutcome() != null) {
				Resourcemetadata rOutcome = generateResourcemetadata(resource, chainedResource, chainedParameter+"outcome", paymentReconciliation.getOutcome().toCode(), paymentReconciliation.getOutcome().getSystem());
				resourcemetadataList.add(rOutcome);
			}

			// payment-issuer : reference
			if (paymentReconciliation.hasPaymentIssuer() && paymentReconciliation.getPaymentIssuer().hasReference()) {
				Resourcemetadata rPaymentIssuer = generateResourcemetadata(resource, chainedResource, chainedParameter+"payment-issuer", generateFullLocalReference(paymentReconciliation.getPaymentIssuer().getReference(), baseUrl));
				resourcemetadataList.add(rPaymentIssuer);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rPaymentIssuerChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "payment-issuer", 0, paymentReconciliation.getPaymentIssuer().getReference(), null);
					resourcemetadataList.addAll(rPaymentIssuerChain);
				}
			}

			// request : reference
			if (paymentReconciliation.hasRequest()) {
				Resourcemetadata rRequestReference = generateResourcemetadata(resource, chainedResource, chainedParameter+"request", generateFullLocalReference(paymentReconciliation.getRequest().getReference(), baseUrl));
				resourcemetadataList.add(rRequestReference);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rRequestChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "request", 0, paymentReconciliation.getRequest().getReference(), null);
					resourcemetadataList.addAll(rRequestChain);
				}
			}

			// requestor : reference
			if (paymentReconciliation.hasRequestor()) {
				Resourcemetadata rRequestor = generateResourcemetadata(resource, chainedResource, chainedParameter+"requestor", generateFullLocalReference(paymentReconciliation.getRequestor().getReference(), baseUrl));
				resourcemetadataList.add(rRequestor);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rRequestorChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "requestor", 0, paymentReconciliation.getRequestor().getReference(), null);
					resourcemetadataList.addAll(rRequestorChain);
				}
			}

			// status : token
			if (paymentReconciliation.hasStatus() && paymentReconciliation.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", paymentReconciliation.getStatus().toCode(), paymentReconciliation.getStatus().getSystem());
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
