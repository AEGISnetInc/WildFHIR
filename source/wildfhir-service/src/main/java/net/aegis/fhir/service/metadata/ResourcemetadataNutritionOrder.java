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
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.NutritionOrder;
import org.hl7.fhir.r4.model.UriType;
import org.hl7.fhir.r4.model.NutritionOrder.NutritionOrderSupplementComponent;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataNutritionOrder extends ResourcemetadataProxy {

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
        ByteArrayInputStream iNutritionOrder = null;

		try {
            // Extract and convert the resource contents to a NutritionOrder object
			if (chainedResource != null) {
				iNutritionOrder = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iNutritionOrder = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            NutritionOrder nutritionOrder = (NutritionOrder) xmlP.parse(iNutritionOrder);
            iNutritionOrder.close();

			/*
             * Create new Resourcemetadata objects for each NutritionOrder metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, nutritionOrder, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", nutritionOrder.getId());
			resourcemetadataList.add(_id);

			// _language : token
			if (nutritionOrder.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", nutritionOrder.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (nutritionOrder.getMeta() != null && nutritionOrder.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(nutritionOrder.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(nutritionOrder.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// datetime : datetime
			if (nutritionOrder.hasDateTime()) {
				Resourcemetadata rDateTime = generateResourcemetadata(resource, chainedResource, chainedParameter+"datetime", utcDateUtil.formatDate(nutritionOrder.getDateTime(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(nutritionOrder.getDateTime(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rDateTime);
			}

			// encounter : reference
			if (nutritionOrder.hasEncounter() && nutritionOrder.getEncounter().hasReference()) {
				Resourcemetadata rEncounter = generateResourcemetadata(resource, chainedResource, chainedParameter+"encounter", generateFullLocalReference(nutritionOrder.getEncounter().getReference(), baseUrl));
				resourcemetadataList.add(rEncounter);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rEncounterChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "encounter", 0, nutritionOrder.getEncounter().getReference());
					resourcemetadataList.addAll(rEncounterChain);
				}
			}

			// identifier : reference
			if (nutritionOrder.hasIdentifier()) {

				for (Identifier identifier : nutritionOrder.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// instantiates-canonical : reference
			if (nutritionOrder.hasInstantiatesCanonical()) {

				for (CanonicalType instantiates : nutritionOrder.getInstantiatesCanonical()) {
					String objectReference = generateFullLocalReference(instantiates.asStringValue(), baseUrl);

					List<Resourcemetadata> rInstantiatesChain = null;
					Resourcemetadata rReference = generateResourcemetadata(resource, chainedResource, chainedParameter+"instantiates-canonical", objectReference);
					resourcemetadataList.add(rReference);

					if (chainedResource == null) {
						// Add chained parameters
						rInstantiatesChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "instantiates-canonical", 0, instantiates.asStringValue());
						resourcemetadataList.addAll(rInstantiatesChain);
					}
				}
			}

			// instantiates-uri : uri
			if (nutritionOrder.hasInstantiatesUri()) {

				for (UriType instantiates : nutritionOrder.getInstantiatesUri()) {

					Resourcemetadata rInstantiates = generateResourcemetadata(resource, chainedResource, chainedParameter+"instantiates-uri", instantiates.asStringValue());
					resourcemetadataList.add(rInstantiates);
				}
			}

			// patient : reference
			if (nutritionOrder.hasPatient() && nutritionOrder.getPatient().hasReference()) {
				Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", generateFullLocalReference(nutritionOrder.getPatient().getReference(), baseUrl));
				resourcemetadataList.add(rPatient);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rPatientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, nutritionOrder.getPatient().getReference());
					resourcemetadataList.addAll(rPatientChain);
				}
			}

			// provider : reference
			if (nutritionOrder.hasOrderer() && nutritionOrder.getOrderer().hasReference()) {
				Resourcemetadata rOrderer = generateResourcemetadata(resource, chainedResource, chainedParameter+"provider", generateFullLocalReference(nutritionOrder.getOrderer().getReference(), baseUrl));
				resourcemetadataList.add(rOrderer);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rProviderChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "provider", 0, nutritionOrder.getOrderer().getReference());
					resourcemetadataList.addAll(rProviderChain);
				}
			}

			// status : token
			if (nutritionOrder.hasStatus() && nutritionOrder.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", nutritionOrder.getStatus().toCode(), nutritionOrder.getStatus().getSystem());
				resourcemetadataList.add(rStatus);
			}

			if (nutritionOrder.hasEnteralFormula()) {

				// additive : token
				if (nutritionOrder.getEnteralFormula().hasAdditiveType() && nutritionOrder.getEnteralFormula().getAdditiveType().hasCoding()) {

					Resourcemetadata rCode = null;
					for (Coding code : nutritionOrder.getEnteralFormula().getAdditiveType().getCoding()) {
						rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"additive", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
						resourcemetadataList.add(rCode);
					}
				}

				// formula : token
				if (nutritionOrder.getEnteralFormula().hasBaseFormulaType() && nutritionOrder.getEnteralFormula().getBaseFormulaType().hasCoding()) {

					Resourcemetadata rCode = null;
					for (Coding code : nutritionOrder.getEnteralFormula().getBaseFormulaType().getCoding()) {
						rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"formula", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
						resourcemetadataList.add(rCode);
					}
				}
			}

			// supplement : token
			if (nutritionOrder.hasSupplement()) {

				Resourcemetadata rCode = null;
				for (NutritionOrderSupplementComponent supplement : nutritionOrder.getSupplement()) {

					if (supplement.hasType() && supplement.getType().hasCoding()) {
						for (Coding code : supplement.getType().getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"supplement", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
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
