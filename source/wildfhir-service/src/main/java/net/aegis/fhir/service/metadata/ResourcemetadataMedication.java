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
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.Medication.MedicationIngredientComponent;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataMedication extends ResourcemetadataProxy {

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
        ByteArrayInputStream iMedication = null;

		try {
            // Extract and convert the resource contents to a Medication object
			if (chainedResource != null) {
				iMedication = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iMedication = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            Medication medication = (Medication) xmlP.parse(iMedication);
            iMedication.close();

			/*
             * Create new Resourcemetadata objects for each Medication metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, medication, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", medication.getId());
			resourcemetadataList.add(_id);

			// _language : token
			if (medication.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", medication.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (medication.getMeta() != null && medication.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(medication.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(medication.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// code : token
			if (medication.hasCode() && medication.getCode().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : medication.getCode().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			if (medication.hasBatch()) {

				// expiration-date : datetime
				if (medication.getBatch().hasExpirationDate()) {
					Resourcemetadata rExpirationDate = generateResourcemetadata(resource, chainedResource, chainedParameter+"expiration-date", utcDateUtil.formatDate(medication.getBatch().getExpirationDate(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(medication.getBatch().getExpirationDate(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
					resourcemetadataList.add(rExpirationDate);
				}

				// lot-number : string
				if (medication.getBatch().hasLotNumber()) {
					Resourcemetadata rLotNumber = generateResourcemetadata(resource, chainedResource, chainedParameter+"lot-number", medication.getBatch().getLotNumber());
					resourcemetadataList.add(rLotNumber);
				}
			}

			// form : token
			if (medication.hasForm() && medication.getForm().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : medication.getForm().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"form", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			if (medication.hasIngredient()) {

				Resourcemetadata rCode = null;
				List<Resourcemetadata> rIngredientChain = null;
				for (MedicationIngredientComponent ingredient : medication.getIngredient()) {

					// ingredient : reference
					if (ingredient.hasItemReference()) {
						Resourcemetadata rIngredient = generateResourcemetadata(resource, chainedResource, chainedParameter + "ingredient", generateFullLocalReference(ingredient.getItemReference().getReference(), baseUrl));
						resourcemetadataList.add(rIngredient);

						if (chainedResource == null) {
							// Add chained parameters for any
							rIngredientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "ingredient", 0, ingredient.getItemReference().getReference());
							resourcemetadataList.addAll(rIngredientChain);
						}
					}

					// ingredient-code : token
					if (ingredient.hasItemCodeableConcept() && ingredient.getItemCodeableConcept().hasCoding()) {

						for (Coding code : ingredient.getItemCodeableConcept().getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"ingredient-code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// manufacturer : reference
			if (medication.hasManufacturer() && medication.getManufacturer().hasReference()) {
				Resourcemetadata rManufacturer = generateResourcemetadata(resource, chainedResource, chainedParameter+"manufacturer", generateFullLocalReference(medication.getManufacturer().getReference(), baseUrl));
				resourcemetadataList.add(rManufacturer);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rOrganizationChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "manufacturer", 0, medication.getManufacturer().getReference());
					resourcemetadataList.addAll(rOrganizationChain);
				}
			}

			// status : token
			if (medication.hasStatus() && medication.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", medication.getStatus().toCode(), medication.getStatus().getSystem());
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
