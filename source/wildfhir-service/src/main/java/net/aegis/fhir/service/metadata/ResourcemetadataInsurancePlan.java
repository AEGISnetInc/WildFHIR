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
import net.aegis.fhir.service.util.UTCDateUtil;

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
        ByteArrayInputStream iInsurancePlan = null;

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

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, insurancePlan, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", insurancePlan.getId());
			resourcemetadataList.add(_id);

			// _language : token
			if (insurancePlan.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", insurancePlan.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (insurancePlan.getMeta() != null && insurancePlan.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(insurancePlan.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(insurancePlan.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

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
							Resourcemetadata rCity = generateResourcemetadata(resource, chainedResource, chainedParameter + "address-city", contact.getAddress().getCity());
							resourcemetadataList.add(rCity);
						}

						// address-state : string
						if (contact.getAddress().hasState()) {
							if (sbAddress.length() > 0) {
								sbAddress.append(" ");
							}
							sbAddress.append(contact.getAddress().getState());
							Resourcemetadata rState = generateResourcemetadata(resource, chainedResource, chainedParameter + "address-state", contact.getAddress().getState());
							resourcemetadataList.add(rState);
						}

						// address-country : string
						if (contact.getAddress().hasCountry()) {
							if (sbAddress.length() > 0) {
								sbAddress.append(" ");
							}
							sbAddress.append(contact.getAddress().getCountry());
							Resourcemetadata rCountry = generateResourcemetadata(resource, chainedResource, chainedParameter + "address-country", contact.getAddress().getCountry());
							resourcemetadataList.add(rCountry);
						}

						// address-postalcode : string
						if (contact.getAddress().hasPostalCode()) {
							if (sbAddress.length() > 0) {
								sbAddress.append(" ");
							}
							sbAddress.append(contact.getAddress().getPostalCode());
							Resourcemetadata rPostalCode = generateResourcemetadata(resource, chainedResource, chainedParameter + "address-postalcode", contact.getAddress().getPostalCode());
							resourcemetadataList.add(rPostalCode);
						}

						// address-use : token
						if (contact.getAddress().hasUse() && contact.getAddress().getUse() != null) {
							Resourcemetadata rUse = generateResourcemetadata(resource, chainedResource, chainedParameter + "address-use", contact.getAddress().getUse().toCode(), contact.getAddress().getUse().getSystem());
							resourcemetadataList.add(rUse);
						}

						// address : string
						if (contact.getAddress().hasText()) {
							if (sbAddress.length() > 0) {
								sbAddress.append(" ");
							}
							sbAddress.append(contact.getAddress().getText());
						}

						if (sbAddress.length() > 0) {
							Resourcemetadata rAddress = generateResourcemetadata(resource, chainedResource, chainedParameter + "address", sbAddress.toString());
							resourcemetadataList.add(rAddress);
						}
					}
				}
			}

			// administered-by : reference
			if (insurancePlan.hasAdministeredBy() && insurancePlan.getAdministeredBy().hasReference()) {
				Resourcemetadata rAdministeredBy = generateResourcemetadata(resource, chainedResource, chainedParameter+"administered-by", generateFullLocalReference(insurancePlan.getAdministeredBy().getReference(), baseUrl));
				resourcemetadataList.add(rAdministeredBy);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rAdministeredByChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "administered-by", 0, insurancePlan.getAdministeredBy().getReference());
					resourcemetadataList.addAll(rAdministeredByChain);
				}
			}

			// endpoint : reference
			if (insurancePlan.hasEndpoint()) {

				String endpointReference = null;
				Resourcemetadata rEndpoint = null;
				List<Resourcemetadata> rEndpointChain = null;
				for (Reference endpoint : insurancePlan.getEndpoint()) {

					if (endpoint.hasReference()) {
						endpointReference = generateFullLocalReference(endpoint.getReference(), baseUrl);

						rEndpoint = generateResourcemetadata(resource, chainedResource, chainedParameter+"endpoint", endpointReference);
						resourcemetadataList.add(rEndpoint);

						if (chainedResource == null) {
							// Add chained parameters for any
							rEndpointChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "endpoint", 0, endpoint.getReference());
							resourcemetadataList.addAll(rEndpointChain);
						}
					}
				}
			}

			// identifier : token
			if (insurancePlan.getIdentifier() != null) {

				Resourcemetadata rIdentifier = null;
				for (Identifier identifier : insurancePlan.getIdentifier()) {

					rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// name : string
			// phonetic : string
			if (insurancePlan.hasName()) {
				Resourcemetadata rName = generateResourcemetadata(resource, chainedResource, chainedParameter+"name", insurancePlan.getName());
				resourcemetadataList.add(rName);
				rName = generateResourcemetadata(resource, chainedResource, chainedParameter+"phonetic", insurancePlan.getName());
				resourcemetadataList.add(rName);
			}
			if (insurancePlan.hasAlias()) {

				for (StringType alias : insurancePlan.getAlias()) {

					Resourcemetadata rName = generateResourcemetadata(resource, chainedResource, chainedParameter+"name", alias.getValue());
					resourcemetadataList.add(rName);
				}
			}

			// owned-by : reference
			if (insurancePlan.hasOwnedBy() && insurancePlan.getOwnedBy().hasReference()) {
				Resourcemetadata rOwnedBy = generateResourcemetadata(resource, chainedResource, chainedParameter+"owned-by", generateFullLocalReference(insurancePlan.getOwnedBy().getReference(), baseUrl));
				resourcemetadataList.add(rOwnedBy);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rOwnedByChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "owned-by", 0, insurancePlan.getOwnedBy().getReference());
					resourcemetadataList.addAll(rOwnedByChain);
				}
			}

			// status : token
			if (insurancePlan.hasStatus() && insurancePlan.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", insurancePlan.getStatus().toCode(), insurancePlan.getStatus().getSystem());
				resourcemetadataList.add(rStatus);
			}

			// type : token
			if (insurancePlan.hasType()) {

				Resourcemetadata rType = null;
				for (CodeableConcept insuranceType : insurancePlan.getType()) {

					if (insuranceType.hasCoding()) {
						for (Coding type : insuranceType.getCoding()) {
							rType = generateResourcemetadata(resource, chainedResource, chainedParameter+"type", type.getCode(), type.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(type));
							resourcemetadataList.add(rType);
						}
					}
				}
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
        } finally {
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
