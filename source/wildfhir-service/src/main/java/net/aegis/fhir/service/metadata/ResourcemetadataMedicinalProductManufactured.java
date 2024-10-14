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
import org.hl7.fhir.r4.model.MedicinalProductManufactured;
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
public class ResourcemetadataMedicinalProductManufactured extends ResourcemetadataProxy {

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
        ByteArrayInputStream iMedicinalProductManufactured = null;

		try {
            // Extract and convert the resource contents to a MedicinalProductManufactured object
			if (chainedResource != null) {
				iMedicinalProductManufactured = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iMedicinalProductManufactured = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            MedicinalProductManufactured medicinalProductManufactured = (MedicinalProductManufactured) xmlP.parse(iMedicinalProductManufactured);
            iMedicinalProductManufactured.close();

			/*
             * Create new Resourcemetadata objects for each MedicinalProductManufactured metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, medicinalProductManufactured, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			if (medicinalProductManufactured.getId() != null) {
				Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", medicinalProductManufactured.getId());
				resourcemetadataList.add(_id);
			}

			// _language : token
			if (medicinalProductManufactured.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", medicinalProductManufactured.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (medicinalProductManufactured.getMeta() != null && medicinalProductManufactured.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(medicinalProductManufactured.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(medicinalProductManufactured.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// dose-form : token
			if (medicinalProductManufactured.hasManufacturedDoseForm() && medicinalProductManufactured.getManufacturedDoseForm().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : medicinalProductManufactured.getManufacturedDoseForm().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"dose-form", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			// ingredient : reference
			if (medicinalProductManufactured.hasIngredient()) {

				Resourcemetadata rIngredient = null;
				List<Resourcemetadata> rIngredientChain = null;
				for (Reference ingredient : medicinalProductManufactured.getManufacturer()) {

					if (ingredient.hasReference()) {
						rIngredient = generateResourcemetadata(resource, chainedResource, chainedParameter+"ingredient", generateFullLocalReference(ingredient.getReference(), baseUrl));
						resourcemetadataList.add(rIngredient);

						if (chainedResource == null) {
							// Add chained parameters for any
							rIngredientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "ingredient", 0, ingredient.getReference(), null);
							resourcemetadataList.addAll(rIngredientChain);
						}
					}
				}
			}

			// manufacturer : reference
			if (medicinalProductManufactured.hasManufacturer()) {

				Resourcemetadata rManufacturer = null;
				List<Resourcemetadata> rManufacturerChain = null;
				for (Reference manufacturer : medicinalProductManufactured.getManufacturer()) {

					if (manufacturer.hasReference()) {
						rManufacturer = generateResourcemetadata(resource, chainedResource, chainedParameter+"manufacturer", generateFullLocalReference(manufacturer.getReference(), baseUrl));
						resourcemetadataList.add(rManufacturer);

						if (chainedResource == null) {
							// Add chained parameters for any
							rManufacturerChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "manufacturer", 0, manufacturer.getReference(), null);
							resourcemetadataList.addAll(rManufacturerChain);
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
