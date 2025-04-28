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
        ByteArrayInputStream iNutritionOrder = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

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

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, nutritionOrder, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// datetime : datetime
			if (nutritionOrder.hasDateTime()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"datetime", utcDateUtil.formatDate(nutritionOrder.getDateTime(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(nutritionOrder.getDateTime(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rMetadata);
			}

			// encounter : reference
			if (nutritionOrder.hasEncounter()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "encounter", 0, nutritionOrder.getEncounter(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// identifier : token
			if (nutritionOrder.hasIdentifier()) {

				for (Identifier identifier : nutritionOrder.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}

			// instantiates-canonical : reference - instantiates is a Canonical, no Reference.identifier
			if (nutritionOrder.hasInstantiatesCanonical()) {

				for (CanonicalType instantiates : nutritionOrder.getInstantiatesCanonical()) {
					String objectReference = generateFullLocalReference(instantiates.asStringValue(), baseUrl);

					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"instantiates-canonical", objectReference);
					resourcemetadataList.add(rMetadata);

					if (chainedResource == null) {
						// Add chained parameters
						rMetadataChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "instantiates-canonical", 0, instantiates.asStringValue(), null);
						resourcemetadataList.addAll(rMetadataChain);
					}
				}
			}

			// instantiates-uri : uri
			if (nutritionOrder.hasInstantiatesUri()) {

				for (UriType instantiates : nutritionOrder.getInstantiatesUri()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"instantiates-uri", instantiates.asStringValue());
					resourcemetadataList.add(rMetadata);
				}
			}

			// patient : reference
			if (nutritionOrder.hasPatient()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "patient", 0, nutritionOrder.getPatient(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// provider : reference
			if (nutritionOrder.hasOrderer()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "provider", 0, nutritionOrder.getOrderer(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// status : token
			if (nutritionOrder.hasStatus() && nutritionOrder.getStatus() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", nutritionOrder.getStatus().toCode(), nutritionOrder.getStatus().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			if (nutritionOrder.hasEnteralFormula()) {

				// additive : token
				if (nutritionOrder.getEnteralFormula().hasAdditiveType() && nutritionOrder.getEnteralFormula().getAdditiveType().hasCoding()) {

					for (Coding code : nutritionOrder.getEnteralFormula().getAdditiveType().getCoding()) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"additive", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
						resourcemetadataList.add(rMetadata);
					}
				}

				// formula : token
				if (nutritionOrder.getEnteralFormula().hasBaseFormulaType() && nutritionOrder.getEnteralFormula().getBaseFormulaType().hasCoding()) {

					for (Coding code : nutritionOrder.getEnteralFormula().getBaseFormulaType().getCoding()) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"formula", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
						resourcemetadataList.add(rMetadata);
					}
				}
			}

			// supplement : token
			if (nutritionOrder.hasSupplement()) {
				for (NutritionOrderSupplementComponent supplement : nutritionOrder.getSupplement()) {

					if (supplement.hasType() && supplement.getType().hasCoding()) {
						for (Coding code : supplement.getType().getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"supplement", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		} finally {
	        rMetadata = null;
	        rMetadataChain = null;
            if (iNutritionOrder != null) {
                try {
                	iNutritionOrder.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
