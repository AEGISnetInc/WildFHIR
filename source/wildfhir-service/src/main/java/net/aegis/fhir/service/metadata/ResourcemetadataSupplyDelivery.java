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
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.SupplyDelivery;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataSupplyDelivery extends ResourcemetadataProxy {

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
        ByteArrayInputStream iSupplyDelivery = null;

		try {
			// Extract and convert the resource contents to a SupplyDelivery object
			if (chainedResource != null) {
				iSupplyDelivery = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iSupplyDelivery = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			SupplyDelivery supplyDelivery = (SupplyDelivery) xmlP.parse(iSupplyDelivery);
			iSupplyDelivery.close();

			/*
			 * Create new Resourcemetadata objects for each SupplyDelivery metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, supplyDelivery, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			if (supplyDelivery.getId() != null) {
				Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", supplyDelivery.getId());
				resourcemetadataList.add(_id);
			}

			// _language : token
			if (supplyDelivery.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", supplyDelivery.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (supplyDelivery.getMeta() != null && supplyDelivery.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(supplyDelivery.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(supplyDelivery.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// identifier : token
			if (supplyDelivery.hasIdentifier()) {

				for (Identifier identifier : supplyDelivery.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// occurrence : date(period)
			if (supplyDelivery.hasOccurrenceDateTimeType()) {
				Resourcemetadata rOccurrence = generateResourcemetadata(resource, chainedResource, chainedParameter+"occurrence", utcDateUtil.formatDate(supplyDelivery.getOccurrenceDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(supplyDelivery.getOccurrenceDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rOccurrence);
			}
			else if (supplyDelivery.hasOccurrencePeriod()) {
				Resourcemetadata rOccurrence = generateResourcemetadata(resource, chainedResource, chainedParameter+"occurrence", utcDateUtil.formatDate(supplyDelivery.getOccurrencePeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(supplyDelivery.getOccurrencePeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(supplyDelivery.getOccurrencePeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(supplyDelivery.getOccurrencePeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
				resourcemetadataList.add(rOccurrence);
			}

			// patient : reference
			if (supplyDelivery.hasPatient() && supplyDelivery.getPatient().hasReference()) {
				Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", generateFullLocalReference(supplyDelivery.getPatient().getReference(), baseUrl));
				resourcemetadataList.add(rPatient);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rPatientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, supplyDelivery.getPatient().getReference(), null);
					resourcemetadataList.addAll(rPatientChain);
				}
			}

			// receiver : reference
			if (supplyDelivery.hasReceiver()) {

				List<Resourcemetadata> rReceiverChain = null;
				for (Reference receiver : supplyDelivery.getReceiver()) {

					if (receiver.hasReference()) {
						Resourcemetadata rReceiver = generateResourcemetadata(resource, chainedResource, chainedParameter+"receiver", generateFullLocalReference(receiver.getReference(), baseUrl));
						resourcemetadataList.add(rReceiver);

						if (chainedResource == null) {
							// Add chained parameters
							rReceiverChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "receiver", 0, receiver.getReference(), null);
							resourcemetadataList.addAll(rReceiverChain);
						}
					}
				}
			}

			// status : token
			if (supplyDelivery.hasStatus() && supplyDelivery.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", supplyDelivery.getStatus().toCode(), supplyDelivery.getStatus().getSystem());
				resourcemetadataList.add(rStatus);
			}

			// supplier : reference
			if (supplyDelivery.hasSupplier() && supplyDelivery.getSupplier().hasReference()) {
				Resourcemetadata rSupplier = generateResourcemetadata(resource, chainedResource, chainedParameter+"supplier", generateFullLocalReference(supplyDelivery.getSupplier().getReference(), baseUrl));
				resourcemetadataList.add(rSupplier);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rSupplierChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "supplier", 0, supplyDelivery.getSupplier().getReference(), null);
					resourcemetadataList.addAll(rSupplierChain);
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
