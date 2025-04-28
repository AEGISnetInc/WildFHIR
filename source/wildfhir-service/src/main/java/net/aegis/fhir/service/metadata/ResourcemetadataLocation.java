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

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataLocation extends ResourcemetadataProxy {

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
        ByteArrayInputStream iLocation = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

		try {
			// Extract and convert the resource contents to a Location object
			if (chainedResource != null) {
				iLocation = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iLocation = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			Location location = (Location) xmlP.parse(iLocation);
			iLocation.close();

			/*
			 * Create new Resourcemetadata objects for each Location metadata value and add to the resourcemetadataList
			 */

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, location, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// address : string
			if (location.hasAddress()) {
				StringBuilder sbAddress = new StringBuilder();

				// address : string
				if (location.getAddress().hasLine()) {

					for (StringType line : location.getAddress().getLine()) {
						if (line.hasValue()) {
							if (sbAddress.length() > 0) {
								sbAddress.append(" ");
							}
							sbAddress.append(line.getValue());
						}
					}
				}

				// address-city : string
				if (location.getAddress().hasCity()) {
					if (sbAddress.length() > 0) {
						sbAddress.append(" ");
					}
					sbAddress.append(location.getAddress().getCity());
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"address-city", location.getAddress().getCity());
					resourcemetadataList.add(rMetadata);
				}

				// address-state : string
				if (location.getAddress().hasState()) {
					if (sbAddress.length() > 0) {
						sbAddress.append(" ");
					}
					sbAddress.append(location.getAddress().getState());
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"address-state", location.getAddress().getState());
					resourcemetadataList.add(rMetadata);
				}

				// address-district : string
				if (location.getAddress().hasDistrict()) {
					if (sbAddress.length() > 0) {
						sbAddress.append(" ");
					}
					sbAddress.append(location.getAddress().getDistrict());
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"address-district", location.getAddress().getDistrict());
					resourcemetadataList.add(rMetadata);
				}

				// address-country : string
				if (location.getAddress().hasCountry()) {
					if (sbAddress.length() > 0) {
						sbAddress.append(" ");
					}
					sbAddress.append(location.getAddress().getCountry());
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"address-country", location.getAddress().getCountry());
					resourcemetadataList.add(rMetadata);
				}

				// address-postalcode : string
				if (location.getAddress().hasPostalCode()) {
					if (sbAddress.length() > 0) {
						sbAddress.append(" ");
					}
					sbAddress.append(location.getAddress().getPostalCode());
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"address-postalcode", location.getAddress().getPostalCode());
					resourcemetadataList.add(rMetadata);
				}

				// address-use : token
				if (location.getAddress().hasUse() && location.getAddress().getUse() != null) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"address-use", location.getAddress().getUse().toCode(), location.getAddress().getUse().getSystem());
					resourcemetadataList.add(rMetadata);
				}

				// address : string
				if (location.getAddress().hasText()) {
					if (sbAddress.length() > 0) {
						sbAddress.append(" ");
					}
					sbAddress.append(location.getAddress().getText());
				}

				if (sbAddress.length() > 0) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"address", sbAddress.toString());
					resourcemetadataList.add(rMetadata);
				}

			}

			// endpoint : reference
			if (location.hasEndpoint()) {

				for (Reference endpoint : location.getEndpoint()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "endpoint", 0, endpoint, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// identifier : token
			if (location.getIdentifier() != null) {

				for (Identifier identifier : location.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}

			// name : string
			if (location.hasName()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"name", location.getName());
				resourcemetadataList.add(rMetadata);
			}
			if (location.hasAlias()) {

				for (StringType alias : location.getAlias()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"name", alias.getValue());
					resourcemetadataList.add(rMetadata);
				}
			}

			/*
			 * Search criteria for the near and near-distance token will be based
			 * on the position values for latitude, longitude.
			 * NOT SUPPORTED: altitude
			 */
			// near : token - value=latitude; system=longitude
			if (location.hasPosition()) {

				String sLatitude = null;
				String sLongitude = null;

				if (location.getPosition().hasLatitude()) {
					sLatitude = location.getPosition().getLatitude().toPlainString();
				}
				if (location.getPosition().hasLongitude()) {
					sLongitude = location.getPosition().getLongitude().toPlainString();
				}

				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"near", sLatitude, sLongitude);
				resourcemetadataList.add(rMetadata);
			}

			// operational-status : token
			if (location.hasOperationalStatus()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"operational-status", location.getOperationalStatus().getCode(), location.getOperationalStatus().getSystem(), null, ServicesUtil.INSTANCE.getTextValue(location.getOperationalStatus()));
				resourcemetadataList.add(rMetadata);
			}

			// organization : reference
			if (location.hasManagingOrganization()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "organization", 0, location.getManagingOrganization(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// partof : reference
			if (location.hasPartOf()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "partof", 0, location.getPartOf(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// status : token
			if (location.hasStatus() && location.getStatus() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", location.getStatus().toCode(), location.getStatus().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// type : token
			if (location.hasType()) {
				for (CodeableConcept locationType : location.getType()) {

					if (locationType.hasCoding()) {
						for (Coding type : locationType.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"type", type.getCode(), type.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(type));
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
            if (iLocation != null) {
                try {
                    iLocation.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
