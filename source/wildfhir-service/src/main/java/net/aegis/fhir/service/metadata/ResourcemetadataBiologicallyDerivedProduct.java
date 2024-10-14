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
import org.hl7.fhir.r4.model.BiologicallyDerivedProduct;
import org.hl7.fhir.r4.model.Coding;
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
public class ResourcemetadataBiologicallyDerivedProduct extends ResourcemetadataProxy {

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
        ByteArrayInputStream iBiologicallyDerivedProduct = null;

		try {
			// Extract and convert the resource contents to a BiologicallyDerivedProduct object
			if (chainedResource != null) {
				iBiologicallyDerivedProduct = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iBiologicallyDerivedProduct = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			BiologicallyDerivedProduct biologicallyDerivedProduct = (BiologicallyDerivedProduct) xmlP.parse(iBiologicallyDerivedProduct);
			iBiologicallyDerivedProduct.close();

			/*
			 * Create new Resourcemetadata objects for each BiologicallyDerivedProduct metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, biologicallyDerivedProduct, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			if (biologicallyDerivedProduct.getId() != null) {
				Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", biologicallyDerivedProduct.getId());
				resourcemetadataList.add(_id);
			}

			// _language : token
			if (biologicallyDerivedProduct.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", biologicallyDerivedProduct.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (biologicallyDerivedProduct.getMeta() != null && biologicallyDerivedProduct.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(biologicallyDerivedProduct.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(biologicallyDerivedProduct.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// identifier : token
			if (biologicallyDerivedProduct.hasIdentifier()) {

				for (Identifier identifier : biologicallyDerivedProduct.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// product-category : token
			if (biologicallyDerivedProduct.hasProductCategory() && biologicallyDerivedProduct.getProductCategory() != null) {
				Resourcemetadata rProductCategory = generateResourcemetadata(resource, chainedResource, chainedParameter+"product-category", biologicallyDerivedProduct.getProductCategory().toCode(), biologicallyDerivedProduct.getProductCategory().getSystem());
				resourcemetadataList.add(rProductCategory);
			}

			// product-code : token
			if (biologicallyDerivedProduct.hasProductCode() && biologicallyDerivedProduct.getProductCode().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : biologicallyDerivedProduct.getProductCode().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"product-code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			// request : reference
			if (biologicallyDerivedProduct.hasRequest()) {

				String requestReference = null;
				Resourcemetadata rRequest = null;
				for (Reference request : biologicallyDerivedProduct.getRequest()) {

					if (request.hasReference()) {
						requestReference = generateFullLocalReference(request.getReference(), baseUrl);

						rRequest = generateResourcemetadata(resource, chainedResource, chainedParameter+"request", requestReference);
						resourcemetadataList.add(rRequest);

						if (chainedResource == null) {
							// Add chained parameters
							List<Resourcemetadata> rRequestChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "request", 0, request.getReference(), null);
							resourcemetadataList.addAll(rRequestChain);
						}
					}
				}
			}

			// status : token
			if (biologicallyDerivedProduct.hasStatus() && biologicallyDerivedProduct.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", biologicallyDerivedProduct.getStatus().toCode(), biologicallyDerivedProduct.getStatus().getSystem());
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
