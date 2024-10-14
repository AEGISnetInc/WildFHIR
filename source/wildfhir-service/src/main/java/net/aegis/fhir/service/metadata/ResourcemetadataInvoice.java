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

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, invoice, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			if (invoice.getId() != null) {
				Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", invoice.getId());
				resourcemetadataList.add(_id);
			}

			// _language : token
			if (invoice.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", invoice.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (invoice.getMeta() != null && invoice.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(invoice.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(invoice.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// account : reference
			if (invoice.hasAccount() && invoice.getAccount().hasReference()) {
				Resourcemetadata rAccount = generateResourcemetadata(resource, chainedResource, chainedParameter+"account", generateFullLocalReference(invoice.getAccount().getReference(), baseUrl));
				resourcemetadataList.add(rAccount);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rAccountChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "account", 0, invoice.getAccount().getReference(), null);
					resourcemetadataList.addAll(rAccountChain);
				}
			}

			// date : datetime
			if (invoice.hasDate()) {
				Resourcemetadata rDate = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(invoice.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(invoice.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rDate);
			}

			// identifier : token
			if (invoice.hasIdentifier()) {

				for (Identifier identifier : invoice.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// issuer : reference
			if (invoice.hasIssuer() && invoice.getIssuer().hasReference()) {
				Resourcemetadata rIssuer = generateResourcemetadata(resource, chainedResource, chainedParameter+"issuer", generateFullLocalReference(invoice.getIssuer().getReference(), baseUrl));
				resourcemetadataList.add(rIssuer);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rIssuerChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "issuer", 0, invoice.getIssuer().getReference(), null);
					resourcemetadataList.addAll(rIssuerChain);
				}
			}

			// participant
			if (invoice.hasParticipant()) {

				Resourcemetadata rCode = null;
				String participantReference = null;
				List<Resourcemetadata> rParticipantChain = null;

				for (InvoiceParticipantComponent participant : invoice.getParticipant()) {

					// participant-role : token
					if (participant.hasRole() && participant.getRole().hasCoding()) {

						for (Coding code : participant.getRole().getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"participant-role", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}

					// participant : reference
					if (participant.hasActor() && participant.getActor().hasReference()) {

						participantReference = generateFullLocalReference(participant.getActor().getReference(), baseUrl);

						Resourcemetadata rParticipant = generateResourcemetadata(resource, chainedResource, chainedParameter+"participant", participantReference);
						resourcemetadataList.add(rParticipant);

						if (chainedResource == null) {
							// Add chained parameters
							rParticipantChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "participant", 0, participant.getActor().getReference(), null);
							resourcemetadataList.addAll(rParticipantChain);
						}
					}
				}
			}

			// patient : reference
			// subject : reference
			if (invoice.hasSubject() && invoice.getSubject().hasReference()) {
				String subjectReference = generateFullLocalReference(invoice.getSubject().getReference(), baseUrl);

				Resourcemetadata rSubject = generateResourcemetadata(resource, chainedResource, chainedParameter+"subject", subjectReference);
				resourcemetadataList.add(rSubject);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rSubjectChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "subject", 0, invoice.getSubject().getReference(), null);
					resourcemetadataList.addAll(rSubjectChain);
				}

				if (subjectReference.indexOf("Patient") >= 0) {
					Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", subjectReference);
					resourcemetadataList.add(rPatient);

					if (chainedResource == null) {
						// Add chained parameters
						List<Resourcemetadata> rPatientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, invoice.getSubject().getReference(), null);
						resourcemetadataList.addAll(rPatientChain);
					}
				}
			}

			// recipient : reference
			if (invoice.hasRecipient() && invoice.getRecipient().hasReference()) {
				Resourcemetadata rRecipient = generateResourcemetadata(resource, chainedResource, chainedParameter+"recipient", generateFullLocalReference(invoice.getRecipient().getReference(), baseUrl));
				resourcemetadataList.add(rRecipient);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rRecipientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "recipient", 0, invoice.getIssuer().getReference(), null);
					resourcemetadataList.addAll(rRecipientChain);
				}
			}

			// status : token
			if (invoice.hasStatus() && invoice.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", invoice.getStatus().toCode(), invoice.getStatus().getSystem());
				resourcemetadataList.add(rStatus);
			}

			// totalgross : money
			if (invoice.hasTotalGross()) {
				Resourcemetadata rTotalGross = generateResourcemetadata(resource, chainedResource, chainedParameter+"totalgross", invoice.getTotalGross().primitiveValue());
				resourcemetadataList.add(rTotalGross);
			}

			// totalnet : money
			if (invoice.hasTotalNet()) {
				Resourcemetadata rTotalNet = generateResourcemetadata(resource, chainedResource, chainedParameter+"totalnet", invoice.getTotalNet().primitiveValue());
				resourcemetadataList.add(rTotalNet);
			}

			// type : token
			if (invoice.hasType() && invoice.getType().hasCoding()) {

				Resourcemetadata rType = null;
				for (Coding type : invoice.getType().getCoding()) {
					rType = generateResourcemetadata(resource, chainedResource, chainedParameter+"type", type.getCode(), type.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(type));
					resourcemetadataList.add(rType);
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
