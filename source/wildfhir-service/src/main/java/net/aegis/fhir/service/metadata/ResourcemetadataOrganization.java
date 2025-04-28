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
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Organization;
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
public class ResourcemetadataOrganization extends ResourcemetadataProxy {

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
        ByteArrayInputStream iOrganization = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

		try {
			// Extract and convert the resource contents to a Organization object
			if (chainedResource != null) {
				iOrganization = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iOrganization = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			Organization organization = (Organization) xmlP.parse(iOrganization);
			iOrganization.close();

			/*
			 * Create new Resourcemetadata objects for each Organization metadata value and add to the resourcemetadataList
			 */

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, organization, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// active : token
			if (organization.hasActive()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"active", Boolean.toString(organization.getActive()));
				resourcemetadataList.add(rMetadata);
			}

			// [extra] address : string - one for each address
			if (organization.hasAddress()) {

				StringBuilder sbAddress = null;

				for (Address address : organization.getAddress()) {

					sbAddress = new StringBuilder();

					// address : string
					if (address.hasLine()) {

						for (StringType line : address.getLine()) {
							if (line.hasValue()) {
								if (sbAddress.length() > 0) {
									sbAddress.append(" ");
								}
								sbAddress.append(line.getValue());
							}
						}
					}

					// address-city : string
					if (address.hasCity()) {
						if (sbAddress.length() > 0) {
							sbAddress.append(" ");
						}
						sbAddress.append(address.getCity());
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"address-city", address.getCity());
						resourcemetadataList.add(rMetadata);
					}

					// address-state : string
					if (address.hasState()) {
						if (sbAddress.length() > 0) {
							sbAddress.append(" ");
						}
						sbAddress.append(address.getState());
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"address-state", address.getState());
						resourcemetadataList.add(rMetadata);
					}

					// address-district : string
					if (address.hasDistrict()) {
						if (sbAddress.length() > 0) {
							sbAddress.append(" ");
						}
						sbAddress.append(address.getDistrict());
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"address-district", address.getDistrict());
						resourcemetadataList.add(rMetadata);
					}

					// address-country : string
					if (address.hasCountry()) {
						if (sbAddress.length() > 0) {
							sbAddress.append(" ");
						}
						sbAddress.append(address.getCountry());
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"address-country", address.getCountry());
						resourcemetadataList.add(rMetadata);
					}

					// address-postalcode : string
					if (address.hasPostalCode()) {
						if (sbAddress.length() > 0) {
							sbAddress.append(" ");
						}
						sbAddress.append(address.getPostalCode());
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"address-postalcode", address.getPostalCode());
						resourcemetadataList.add(rMetadata);
					}

					// address-use : token
					if (address.hasUse() && address.getUse() != null) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"address-use", address.getUse().toCode(), address.getUse().getSystem());
						resourcemetadataList.add(rMetadata);
					}

					// address : string
					if (address.hasText()) {
						if (sbAddress.length() > 0) {
							sbAddress.append(" ");
						}
						sbAddress.append(address.getText());
					}

					if (sbAddress.length() > 0) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"address", sbAddress.toString());
						resourcemetadataList.add(rMetadata);
					}
				}
			}

			// endpoint : reference
			if (organization.hasEndpoint()) {

				for (Reference endpoint : organization.getEndpoint()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "endpoint", 0, endpoint, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// identifier : token
			if (organization.hasIdentifier()) {

				for (Identifier identifier : organization.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}

			// name : string
			// phonetic : string
			if (organization.hasName()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"name", organization.getName());
				resourcemetadataList.add(rMetadata);
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"phonetic", organization.getName());
				resourcemetadataList.add(rMetadata);
			}
			if (organization.hasAlias()) {

				for (StringType alias : organization.getAlias()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"name", alias.getValue());
					resourcemetadataList.add(rMetadata);
				}
			}

			// partof : reference
			if (organization.hasPartOf()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "partof", 0, organization.getPartOf(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// type : token
			if (organization.hasType()) {
				for (CodeableConcept organizationType : organization.getType()) {

					if (organizationType.hasCoding()) {
						for (Coding type : organizationType.getCoding()) {
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
            if (iOrganization != null) {
                try {
                    iOrganization.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
