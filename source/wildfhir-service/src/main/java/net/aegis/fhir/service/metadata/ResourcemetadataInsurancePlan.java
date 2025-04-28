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
import org.hl7.fhir.r4.model.InsurancePlan;
import org.hl7.fhir.r4.model.InsurancePlan.InsurancePlanContactComponent;
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
public class ResourcemetadataInsurancePlan extends ResourcemetadataProxy {

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
        ByteArrayInputStream iInsurancePlan = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

		try {
			// Extract and convert the resource contents to a InsurancePlan object
			if (chainedResource != null) {
				iInsurancePlan = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iInsurancePlan = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			InsurancePlan insurancePlan = (InsurancePlan) xmlP.parse(iInsurancePlan);
			iInsurancePlan.close();

			/*
			 * Create new Resourcemetadata objects for each InsurancePlan metadata value and add to the resourcemetadataList
			 */

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, insurancePlan, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// address : string
			if (insurancePlan.hasContact()) {

				for (InsurancePlanContactComponent contact : insurancePlan.getContact()) {

					if (contact.hasAddress()) {
						StringBuilder sbAddress = new StringBuilder();

						// address : string
						if (contact.getAddress().hasLine()) {

							for (StringType line : contact.getAddress().getLine()) {
								if (line.hasValue()) {
									if (sbAddress.length() > 0) {
										sbAddress.append(" ");
									}
									sbAddress.append(line.getValue());
								}
							}
						}

						// address-city : string
						if (contact.getAddress().hasCity()) {
							if (sbAddress.length() > 0) {
								sbAddress.append(" ");
							}
							sbAddress.append(contact.getAddress().getCity());
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter + "address-city", contact.getAddress().getCity());
							resourcemetadataList.add(rMetadata);
						}

						// address-state : string
						if (contact.getAddress().hasState()) {
							if (sbAddress.length() > 0) {
								sbAddress.append(" ");
							}
							sbAddress.append(contact.getAddress().getState());
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter + "address-state", contact.getAddress().getState());
							resourcemetadataList.add(rMetadata);
						}

						// address-district : string
						if (contact.getAddress().hasDistrict()) {
							if (sbAddress.length() > 0) {
								sbAddress.append(" ");
							}
							sbAddress.append(contact.getAddress().getDistrict());
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"address-district", contact.getAddress().getDistrict());
							resourcemetadataList.add(rMetadata);
						}

						// address-country : string
						if (contact.getAddress().hasCountry()) {
							if (sbAddress.length() > 0) {
								sbAddress.append(" ");
							}
							sbAddress.append(contact.getAddress().getCountry());
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter + "address-country", contact.getAddress().getCountry());
							resourcemetadataList.add(rMetadata);
						}

						// address-postalcode : string
						if (contact.getAddress().hasPostalCode()) {
							if (sbAddress.length() > 0) {
								sbAddress.append(" ");
							}
							sbAddress.append(contact.getAddress().getPostalCode());
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter + "address-postalcode", contact.getAddress().getPostalCode());
							resourcemetadataList.add(rMetadata);
						}

						// address-use : token
						if (contact.getAddress().hasUse() && contact.getAddress().getUse() != null) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter + "address-use", contact.getAddress().getUse().toCode(), contact.getAddress().getUse().getSystem());
							resourcemetadataList.add(rMetadata);
						}

						// address : string
						if (contact.getAddress().hasText()) {
							if (sbAddress.length() > 0) {
								sbAddress.append(" ");
							}
							sbAddress.append(contact.getAddress().getText());
						}

						if (sbAddress.length() > 0) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter + "address", sbAddress.toString());
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// administered-by : reference
			if (insurancePlan.hasAdministeredBy()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "administered-by", 0, insurancePlan.getAdministeredBy(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// endpoint : reference
			if (insurancePlan.hasEndpoint()) {

				for (Reference endpoint : insurancePlan.getEndpoint()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "endpoint", 0, endpoint, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// identifier : token
			if (insurancePlan.getIdentifier() != null) {

				for (Identifier identifier : insurancePlan.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}

			// name : string
			// phonetic : string
			if (insurancePlan.hasName()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"name", insurancePlan.getName());
				resourcemetadataList.add(rMetadata);
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"phonetic", insurancePlan.getName());
				resourcemetadataList.add(rMetadata);
			}
			if (insurancePlan.hasAlias()) {

				for (StringType alias : insurancePlan.getAlias()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"name", alias.getValue());
					resourcemetadataList.add(rMetadata);
				}
			}

			// owned-by : reference
			if (insurancePlan.hasOwnedBy()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "owned-by", 0, insurancePlan.getOwnedBy(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// status : token
			if (insurancePlan.hasStatus() && insurancePlan.getStatus() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", insurancePlan.getStatus().toCode(), insurancePlan.getStatus().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// type : token
			if (insurancePlan.hasType()) {
				for (CodeableConcept insuranceType : insurancePlan.getType()) {

					if (insuranceType.hasCoding()) {
						for (Coding type : insuranceType.getCoding()) {
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
            if (iInsurancePlan != null) {
                try {
                    iInsurancePlan.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
