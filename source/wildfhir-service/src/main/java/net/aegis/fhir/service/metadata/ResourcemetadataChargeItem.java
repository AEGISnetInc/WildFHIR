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
        ByteArrayInputStream iChargeItem = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

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

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, chargeItem, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// account : reference
			if (chargeItem.hasAccount()) {

				for (Reference account : chargeItem.getAccount()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "account", 0, account, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// code : token
			if (chargeItem.hasCode() && chargeItem.getCode().hasCoding()) {

				for (Coding code : chargeItem.getCode().getCoding()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rMetadata);
				}
			}

			// context : reference
			// encounter : reference
			if (chargeItem.hasContext()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "context", 0, chargeItem.getContext(), null);
				resourcemetadataList.addAll(rMetadataChain);

				// patient : reference
				if ((chargeItem.getContext().hasReference() && chargeItem.getContext().getReference().indexOf("Encounter") >= 0)
						|| (chargeItem.getContext().hasType() && chargeItem.getContext().getType().equals("Encounter"))) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "encounter", 0, chargeItem.getContext(), null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// entered-date : date
			if (chargeItem.hasEnteredDate()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"entered-date", utcDateUtil.formatDate(chargeItem.getEnteredDate(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(chargeItem.getEnteredDate(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rMetadata);
			}

			// enterer : reference
			if (chargeItem.hasEnterer()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "enterer", 0, chargeItem.getEnterer(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// factor-override : number(decimal)
			if (chargeItem.hasFactorOverride()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"factor-override", chargeItem.getFactorOverride().toPlainString());
				resourcemetadataList.add(rMetadata);
			}

			// identifier : token
			if (chargeItem.hasIdentifier()) {

				for (Identifier identifier : chargeItem.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}

			// occurrence : date
			if (chargeItem.hasOccurrence()) {

				if (chargeItem.hasOccurrenceDateTimeType()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"occurrence", utcDateUtil.formatDate(chargeItem.getOccurrenceDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(chargeItem.getOccurrenceDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				}
				// occurrence : date(period)
				else if (chargeItem.hasOccurrencePeriod()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"occurrence", utcDateUtil.formatDate(chargeItem.getOccurrencePeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(chargeItem.getOccurrencePeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(chargeItem.getOccurrencePeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(chargeItem.getOccurrencePeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
				}
				else if (chargeItem.hasOccurrenceTiming() && chargeItem.getOccurrenceTiming().hasEvent()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"occurrence", utcDateUtil.formatDate(chargeItem.getOccurrenceTiming().getEvent().get(0).getValue(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(chargeItem.getOccurrenceTiming().getEvent().get(0).getValue(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				}

				resourcemetadataList.add(rMetadata);
			}

			// performer-actor : reference
			// performer-function : token
			if (chargeItem.hasPerformer()) {

				for (ChargeItemPerformerComponent performer : chargeItem.getPerformer()) {

					if (performer.hasActor()) {
						rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "performer-actor", 0, performer.getActor(), null);
						resourcemetadataList.addAll(rMetadataChain);
					}

					if (performer.hasFunction() && performer.getFunction().hasCoding()) {

						for (Coding code : performer.getFunction().getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"performer-function", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// performing-organization : reference
			if (chargeItem.hasPerformingOrganization()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "performing-organization", 0, chargeItem.getPerformingOrganization(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// price-override : money
			if (chargeItem.hasPriceOverride()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"price-override", chargeItem.getPriceOverride().getValue().toPlainString());
				resourcemetadataList.add(rMetadata);
			}

			// quantity : quantity
			if (chargeItem.hasQuantity()) {
				String quantityCode = (chargeItem.getQuantity().getCode() != null ? chargeItem.getQuantity().getCode() : chargeItem.getQuantity().getUnit());
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"quantity", chargeItem.getQuantity().getValue().toPlainString(), chargeItem.getQuantity().getSystem(), quantityCode);
				resourcemetadataList.add(rMetadata);
			}

			// subject : reference
			// patient : reference
			if (chargeItem.hasSubject() && chargeItem.getSubject().hasReference()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "subject", 0, chargeItem.getSubject(), null);
				resourcemetadataList.addAll(rMetadataChain);

				// patient : reference
				if ((chargeItem.getSubject().hasReference() && chargeItem.getSubject().getReference().indexOf("Patient") >= 0)
						|| (chargeItem.getSubject().hasType() && chargeItem.getSubject().getType().equals("Patient"))) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "patient", 0, chargeItem.getSubject(), null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// requesting-organization : reference
			if (chargeItem.hasRequestingOrganization()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "requesting-organization", 0, chargeItem.getRequestingOrganization(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// service : reference
			if (chargeItem.hasService()) {

				for (Reference service : chargeItem.getService()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "service", 0, service, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// status : token
			if (chargeItem.hasStatus() && chargeItem.getStatus() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", chargeItem.getStatus().toCode(), chargeItem.getStatus().getSystem());
				resourcemetadataList.add(rMetadata);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
        } finally {
	        rMetadata = null;
	        rMetadataChain = null;
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
