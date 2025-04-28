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

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Invoice;
import org.hl7.fhir.r4.model.Invoice.InvoiceParticipantComponent;

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataInvoice extends ResourcemetadataProxy {

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
        ByteArrayInputStream iInvoice = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

		try {
            // Extract and convert the resource contents to a Invoice object
			if (chainedResource != null) {
				iInvoice = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iInvoice = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            Invoice invoice = (Invoice) xmlP.parse(iInvoice);
            iInvoice.close();

			/*
             * Create new Resourcemetadata objects for each Invoice metadata value and add to the resourcemetadataList
			 */

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, invoice, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// account : reference
			if (invoice.hasAccount()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "account", 0, invoice.getAccount(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// date : datetime
			if (invoice.hasDate()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(invoice.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(invoice.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rMetadata);
			}

			// identifier : token
			if (invoice.hasIdentifier()) {

				for (Identifier identifier : invoice.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}

			// issuer : reference
			if (invoice.hasIssuer()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "issuer", 0, invoice.getIssuer(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// participant
			if (invoice.hasParticipant()) {

				for (InvoiceParticipantComponent participant : invoice.getParticipant()) {

					// participant-role : token
					if (participant.hasRole() && participant.getRole().hasCoding()) {

						for (Coding code : participant.getRole().getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"participant-role", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}

					// participant : reference
					if (participant.hasActor()) {
						rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "participant", 0, participant.getActor(), null);
						resourcemetadataList.addAll(rMetadataChain);
					}
				}
			}

			// subject : reference
			if (invoice.hasSubject()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "subject", 0, invoice.getSubject(), null);
				resourcemetadataList.addAll(rMetadataChain);

				// patient : reference
				if ((invoice.getSubject().hasReference() && invoice.getSubject().getReference().indexOf("Patient") >= 0)
						|| (invoice.getSubject().hasType() && invoice.getSubject().getType().equals("Patient"))) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "patient", 0, invoice.getSubject(), null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// recipient : reference
			if (invoice.hasRecipient()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "recipient", 0, invoice.getRecipient(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// status : token
			if (invoice.hasStatus() && invoice.getStatus() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", invoice.getStatus().toCode(), invoice.getStatus().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// totalgross : money
			if (invoice.hasTotalGross()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"totalgross", invoice.getTotalGross().primitiveValue());
				resourcemetadataList.add(rMetadata);
			}

			// totalnet : money
			if (invoice.hasTotalNet()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"totalnet", invoice.getTotalNet().primitiveValue());
				resourcemetadataList.add(rMetadata);
			}

			// type : token
			if (invoice.hasType() && invoice.getType().hasCoding()) {

				for (Coding type : invoice.getType().getCoding()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"type", type.getCode(), type.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(type));
					resourcemetadataList.add(rMetadata);
				}
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		} finally {
	        rMetadata = null;
	        rMetadataChain = null;
            if (iInvoice != null) {
                try {
                	iInvoice.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
