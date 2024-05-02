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
import org.hl7.fhir.r4.model.ChargeItem;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ChargeItem.ChargeItemPerformerComponent;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataChargeItem extends ResourcemetadataProxy {

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
        ByteArrayInputStream iChargeItem = null;

		try {
			// Extract and convert the resource contents to a ChargeItem object
			if (chainedResource != null) {
				iChargeItem = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iChargeItem = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			ChargeItem chargeItem = (ChargeItem) xmlP.parse(iChargeItem);
			iChargeItem.close();

			/*
			 * Create new Resourcemetadata objects for each ChargeItem metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, chargeItem, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", chargeItem.getId());
			resourcemetadataList.add(_id);

			// _language : token
			if (chargeItem.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", chargeItem.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (chargeItem.getMeta() != null && chargeItem.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(chargeItem.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(chargeItem.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// account : reference
			if (chargeItem.hasAccount()) {

				String accountReference = null;
				for (Reference account : chargeItem.getAccount()) {

					if (account.hasReference()) {
						accountReference = generateFullLocalReference(account.getReference(), baseUrl);

						Resourcemetadata rAccount = generateResourcemetadata(resource, chainedResource, chainedParameter+"account", accountReference);
						resourcemetadataList.add(rAccount);

						if (chainedResource == null) {
							// Add chained parameters
							List<Resourcemetadata> rAccountChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "account", 0, account.getReference());
							resourcemetadataList.addAll(rAccountChain);
						}
					}
				}
			}

			// code : token
			if (chargeItem.hasCode() && chargeItem.getCode().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : chargeItem.getCode().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			// context : reference
			// encounter : reference
			if (chargeItem.hasContext() && chargeItem.getContext().hasReference()) {
				String contextReference = generateFullLocalReference(chargeItem.getContext().getReference(), baseUrl);

				Resourcemetadata rContext = generateResourcemetadata(resource, chainedResource, chainedParameter+"context", contextReference);
				resourcemetadataList.add(rContext);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rContextChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "context", 0, chargeItem.getContext().getReference());
					resourcemetadataList.addAll(rContextChain);
				}

				if (contextReference.indexOf("Encounter") >= 0) {
					Resourcemetadata rEncounter = generateResourcemetadata(resource, chainedResource, chainedParameter+"encounter", contextReference);
					resourcemetadataList.add(rEncounter);

					if (chainedResource == null) {
						// Add chained parameters
						List<Resourcemetadata> rEncounterChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "encounter", 0, chargeItem.getContext().getReference());
						resourcemetadataList.addAll(rEncounterChain);
					}
				}
			}

			// entered-date : date
			if (chargeItem.hasEnteredDate()) {
				Resourcemetadata rEnteredDate = generateResourcemetadata(resource, chainedResource, chainedParameter+"entered-date", utcDateUtil.formatDate(chargeItem.getEnteredDate(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(chargeItem.getEnteredDate(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rEnteredDate);
			}

			// enterer : reference
			if (chargeItem.hasEnterer() && chargeItem.getEnterer().hasReference()) {
				String entererReference = generateFullLocalReference(chargeItem.getEnterer().getReference(), baseUrl);

				Resourcemetadata rEnterer = generateResourcemetadata(resource, chainedResource, chainedParameter+"enterer", entererReference);
				resourcemetadataList.add(rEnterer);

				if (chainedResource == null) {
					// Add chained parameters for any
					List<Resourcemetadata> rEntererChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "enterer", 0, chargeItem.getEnterer().getReference());
					resourcemetadataList.addAll(rEntererChain);
				}
			}

			// factor-override : number(decimal)
			if (chargeItem.hasFactorOverride()) {
				Resourcemetadata rFactorOverride = generateResourcemetadata(resource, chainedResource, chainedParameter+"factor-override", chargeItem.getFactorOverride().toPlainString());
				resourcemetadataList.add(rFactorOverride);
			}

			// identifier : token
			if (chargeItem.hasIdentifier()) {

				for (Identifier identifier : chargeItem.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// occurrence : date
			if (chargeItem.hasOccurrence()) {

				Resourcemetadata rOccurrence = null;

				if (chargeItem.hasOccurrenceDateTimeType()) {
					rOccurrence = generateResourcemetadata(resource, chainedResource, chainedParameter+"occurrence", utcDateUtil.formatDate(chargeItem.getOccurrenceDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(chargeItem.getOccurrenceDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				}
				// occurrence : date(period)
				else if (chargeItem.hasOccurrencePeriod()) {
					rOccurrence = generateResourcemetadata(resource, chainedResource, chainedParameter+"occurrence", utcDateUtil.formatDate(chargeItem.getOccurrencePeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(chargeItem.getOccurrencePeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(chargeItem.getOccurrencePeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(chargeItem.getOccurrencePeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
				}
				else if (chargeItem.hasOccurrenceTiming() && chargeItem.getOccurrenceTiming().hasEvent()) {
					rOccurrence = generateResourcemetadata(resource, chainedResource, chainedParameter+"occurrence", utcDateUtil.formatDate(chargeItem.getOccurrenceTiming().getEvent().get(0).getValue(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(chargeItem.getOccurrenceTiming().getEvent().get(0).getValue(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				}

				resourcemetadataList.add(rOccurrence);
			}

			// performer-actor : reference
			// performer-function : token
			if (chargeItem.hasPerformer()) {

				String participantReference = null;
				for (ChargeItemPerformerComponent performer : chargeItem.getPerformer()) {

					if (performer.hasActor() && performer.getActor().hasReference()) {
						participantReference = generateFullLocalReference(performer.getActor().getReference(), baseUrl);

						Resourcemetadata rParticipant = generateResourcemetadata(resource, chainedResource, chainedParameter+"performer-actor", participantReference);
						resourcemetadataList.add(rParticipant);

						if (chainedResource == null) {
							// Add chained parameters for any
							List<Resourcemetadata> rParticipantChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "performer-actor", 0, performer.getActor().getReference());
							resourcemetadataList.addAll(rParticipantChain);
						}
					}

					if (performer.hasFunction() && performer.getFunction().hasCoding()) {

						Resourcemetadata rCode = null;
						for (Coding code : performer.getFunction().getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"performer-function", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// performing-organization : reference
			if (chargeItem.hasPerformingOrganization() && chargeItem.getPerformingOrganization().hasReference()) {
				String performingOrganizationReference = generateFullLocalReference(chargeItem.getPerformingOrganization().getReference(), baseUrl);

				Resourcemetadata rPerformingOrganization = generateResourcemetadata(resource, chainedResource, chainedParameter+"performing-organization", performingOrganizationReference);
				resourcemetadataList.add(rPerformingOrganization);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rPerformingOrganizationChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "performing-organization", 0, chargeItem.getPerformingOrganization().getReference());
					resourcemetadataList.addAll(rPerformingOrganizationChain);
				}
			}

			// price-override : money
			if (chargeItem.hasPriceOverride()) {
				Resourcemetadata rPriceOverride = generateResourcemetadata(resource, chainedResource, chainedParameter+"price-override", chargeItem.getPriceOverride().getValue().toPlainString());
				resourcemetadataList.add(rPriceOverride);
			}

			// quantity : quantity
			if (chargeItem.hasQuantity()) {
				String quantityCode = (chargeItem.getQuantity().getCode() != null ? chargeItem.getQuantity().getCode() : chargeItem.getQuantity().getUnit());
				Resourcemetadata rQuantity = generateResourcemetadata(resource, chainedResource, chainedParameter+"quantity", chargeItem.getQuantity().getValue().toPlainString(), chargeItem.getQuantity().getSystem(), quantityCode);
				resourcemetadataList.add(rQuantity);
			}

			// subject : reference
			// patient : reference
			if (chargeItem.hasSubject() && chargeItem.getSubject().hasReference()) {
				String subjectReference = generateFullLocalReference(chargeItem.getSubject().getReference(), baseUrl);
				List<Resourcemetadata> rSubjectChain = null;

				Resourcemetadata rSubject = generateResourcemetadata(resource, chainedResource, chainedParameter+"subject", subjectReference);
				resourcemetadataList.add(rSubject);

				if (subjectReference.indexOf("Patient") >= 0) {
					Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", subjectReference);
					resourcemetadataList.add(rPatient);

					if (chainedResource == null) {
						rSubjectChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, chargeItem.getSubject().getReference());
						resourcemetadataList.addAll(rSubjectChain);
					}
				}

				if (chainedResource == null) {
					// Add chained parameters for any
					rSubjectChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "subject", 0, chargeItem.getSubject().getReference());
					resourcemetadataList.addAll(rSubjectChain);
				}
			}

			// requesting-organization : reference
			if (chargeItem.hasRequestingOrganization() && chargeItem.getRequestingOrganization().hasReference()) {
				String requestingOrganizationReference = generateFullLocalReference(chargeItem.getRequestingOrganization().getReference(), baseUrl);

				Resourcemetadata rRequestingOrganization = generateResourcemetadata(resource, chainedResource, chainedParameter+"requesting-organization", requestingOrganizationReference);
				resourcemetadataList.add(rRequestingOrganization);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rRequestingOrganizationChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "requesting-organization", 0, chargeItem.getRequestingOrganization().getReference());
					resourcemetadataList.addAll(rRequestingOrganizationChain);
				}
			}

			// service : reference
			if (chargeItem.hasService()) {

				String serviceReference = null;
				for (Reference service : chargeItem.getService()) {

					if (service.hasReference()) {
						serviceReference = generateFullLocalReference(service.getReference(), baseUrl);

						Resourcemetadata rService = generateResourcemetadata(resource, chainedResource, chainedParameter+"service", serviceReference);
						resourcemetadataList.add(rService);

						if (chainedResource == null) {
							// Add chained parameters for any
							List<Resourcemetadata> rServiceChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "service", 0, service.getReference());
							resourcemetadataList.addAll(rServiceChain);
						}
					}
				}
			}

			// status : token
			if (chargeItem.hasStatus() && chargeItem.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", chargeItem.getStatus().toCode(), chargeItem.getStatus().getSystem());
				resourcemetadataList.add(rStatus);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
        } finally {
            if (iChargeItem != null) {
                try {
                	iChargeItem.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
