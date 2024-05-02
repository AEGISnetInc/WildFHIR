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
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Substance;
import org.hl7.fhir.r4.model.Substance.SubstanceIngredientComponent;
import org.hl7.fhir.r4.model.Substance.SubstanceInstanceComponent;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataSubstance extends ResourcemetadataProxy {

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
        ByteArrayInputStream iSubstance = null;

		try {
			// Extract and convert the resource contents to a Substance object
			if (chainedResource != null) {
				iSubstance = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iSubstance = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			Substance substance = (Substance) xmlP.parse(iSubstance);
			iSubstance.close();

			/*
			 * Create new Resourcemetadata objects for each Substance metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, substance, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", substance.getId());
			resourcemetadataList.add(_id);

			// _language : token
			if (substance.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", substance.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (substance.getMeta() != null && substance.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(substance.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(substance.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// category : token
			if (substance.hasCategory()) {

				Resourcemetadata rCode = null;
				for (CodeableConcept category : substance.getCategory()) {

					if (category.hasCoding()) {
						for (Coding code : category.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"category", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// code : token
			if (substance.hasCode() && substance.getCode().hasCoding()) {
				Resourcemetadata rCode = null;
				for (Coding code : substance.getCode().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			// container-identifier : token
			// expiry : date
			// quantity : number
			if (substance.hasInstance()) {

				for (SubstanceInstanceComponent instance : substance.getInstance()) {

					// container-identifier : token
					if (instance.hasIdentifier()) {
						Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"container-identifier", instance.getIdentifier().getValue(), instance.getIdentifier().getSystem(), null, ServicesUtil.INSTANCE.getTextValue(instance.getIdentifier()));
						resourcemetadataList.add(rIdentifier);
					}

					// expiry : date
					if (instance.hasExpiry()) {
						Resourcemetadata rExpiry = generateResourcemetadata(resource, chainedResource, chainedParameter+"expiry", utcDateUtil.formatDate(instance.getExpiry(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(instance.getExpiry(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
						resourcemetadataList.add(rExpiry);
					}

					// quantity : number (quantity)
					if (instance.hasQuantity()) {
						String valueQuantityCode = (instance.getQuantity().getCode() != null ? instance.getQuantity().getCode() : instance.getQuantity().getUnit());
						Resourcemetadata rQuantity = generateResourcemetadata(resource, chainedResource, chainedParameter+"quantity", instance.getQuantity().getValue().toString(), instance.getQuantity().getSystem(), valueQuantityCode);
						resourcemetadataList.add(rQuantity);
					}
				}
			}

			// identifier : token
			if (substance.hasIdentifier()) {

				for (Identifier identifier : substance.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// status : token
			if (substance.hasStatus() && substance.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", substance.getStatus().toCode(), substance.getStatus().getSystem());
				resourcemetadataList.add(rStatus);
			}

			// substance-reference : reference
			if (substance.hasIngredient()) {

				List<Resourcemetadata> rSubstanceChain = null;
				for (SubstanceIngredientComponent ingredient : substance.getIngredient()) {

					if (ingredient.hasSubstanceReference() && ingredient.getSubstanceReference().hasReference()) {
						Resourcemetadata rSubstancereference = generateResourcemetadata(resource, chainedResource, chainedParameter+"substance-reference", generateFullLocalReference(ingredient.getSubstanceReference().getReference(), baseUrl));
						resourcemetadataList.add(rSubstancereference);

						if (chainedResource == null) {
							// Add chained parameters
							rSubstanceChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "substance-reference", 0, ingredient.getSubstanceReference().getReference());
							resourcemetadataList.addAll(rSubstanceChain);
						}
					}

					if (ingredient.hasSubstanceCodeableConcept() && ingredient.getSubstanceCodeableConcept().hasCoding()) {
						Resourcemetadata rCode = null;
						for (Coding code : ingredient.getSubstanceCodeableConcept().getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
        } finally {
            if (iSubstance != null) {
                try {
                    iSubstance.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
