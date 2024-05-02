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
import org.hl7.fhir.r4.model.Location;
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
public class ResourcemetadataLocation extends ResourcemetadataProxy {

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
        ByteArrayInputStream iLocation = null;

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

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, location, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", location.getId());
			resourcemetadataList.add(_id);

			// _language : token
			if (location.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", location.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (location.getMeta() != null && location.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(location.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(location.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

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
					Resourcemetadata rCity = generateResourcemetadata(resource, chainedResource, chainedParameter+"address-city", location.getAddress().getCity());
					resourcemetadataList.add(rCity);
				}

				// address-state : string
				if (location.getAddress().hasState()) {
					if (sbAddress.length() > 0) {
						sbAddress.append(" ");
					}
					sbAddress.append(location.getAddress().getState());
					Resourcemetadata rState = generateResourcemetadata(resource, chainedResource, chainedParameter+"address-state", location.getAddress().getState());
					resourcemetadataList.add(rState);
				}

				// address-country : string
				if (location.getAddress().hasCountry()) {
					if (sbAddress.length() > 0) {
						sbAddress.append(" ");
					}
					sbAddress.append(location.getAddress().getCountry());
					Resourcemetadata rCountry = generateResourcemetadata(resource, chainedResource, chainedParameter+"address-country", location.getAddress().getCountry());
					resourcemetadataList.add(rCountry);
				}

				// address-postalcode : string
				if (location.getAddress().hasPostalCode()) {
					if (sbAddress.length() > 0) {
						sbAddress.append(" ");
					}
					sbAddress.append(location.getAddress().getPostalCode());
					Resourcemetadata rPostalCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"address-postalcode", location.getAddress().getPostalCode());
					resourcemetadataList.add(rPostalCode);
				}

				// address-use : token
				if (location.getAddress().hasUse() && location.getAddress().getUse() != null) {
					Resourcemetadata rUse = generateResourcemetadata(resource, chainedResource, chainedParameter+"address-use", location.getAddress().getUse().toCode(), location.getAddress().getUse().getSystem());
					resourcemetadataList.add(rUse);
				}

				// address : string
				if (location.getAddress().hasText()) {
					if (sbAddress.length() > 0) {
						sbAddress.append(" ");
					}
					sbAddress.append(location.getAddress().getText());
				}

				if (sbAddress.length() > 0) {
					Resourcemetadata rAddress = generateResourcemetadata(resource, chainedResource, chainedParameter+"address", sbAddress.toString());
					resourcemetadataList.add(rAddress);
				}

			}

			// endpoint : reference
			if (location.hasEndpoint()) {

				Resourcemetadata rEndpoint = null;
				String endpointReference = null;
				List<Resourcemetadata> rEndpointChain = null;
				for (Reference endpoint : location.getEndpoint()) {

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
			if (location.getIdentifier() != null) {

				Resourcemetadata rIdentifier = null;
				for (Identifier identifier : location.getIdentifier()) {

					rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// name : string
			if (location.hasName()) {
				Resourcemetadata rName = generateResourcemetadata(resource, chainedResource, chainedParameter+"name", location.getName());
				resourcemetadataList.add(rName);
			}
			if (location.hasAlias()) {

				for (StringType alias : location.getAlias()) {

					Resourcemetadata rName = generateResourcemetadata(resource, chainedResource, chainedParameter+"name", alias.getValue());
					resourcemetadataList.add(rName);
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

				Resourcemetadata rNear = generateResourcemetadata(resource, chainedResource, chainedParameter+"near", sLatitude, sLongitude);
				resourcemetadataList.add(rNear);
			}

			// operational-status : token
			if (location.hasOperationalStatus()) {
				Resourcemetadata rOperationalStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"operational-status", location.getOperationalStatus().getCode(), location.getOperationalStatus().getSystem(), null, ServicesUtil.INSTANCE.getTextValue(location.getOperationalStatus()));
				resourcemetadataList.add(rOperationalStatus);
			}

			// organization : reference
			if (location.hasManagingOrganization() && location.getManagingOrganization().hasReference()) {
				Resourcemetadata rOrganization = generateResourcemetadata(resource, chainedResource, chainedParameter+"organization", generateFullLocalReference(location.getManagingOrganization().getReference(), baseUrl));
				resourcemetadataList.add(rOrganization);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rOrganizationChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "organization", 0, location.getManagingOrganization().getReference());
					resourcemetadataList.addAll(rOrganizationChain);
				}
			}

			// partof : reference
			if (location.hasPartOf() && location.getPartOf().hasReference()) {
				Resourcemetadata rPartOf = generateResourcemetadata(resource, chainedResource, chainedParameter+"partof", generateFullLocalReference(location.getPartOf().getReference(), baseUrl));
				resourcemetadataList.add(rPartOf);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rPartOfChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "partof", 0, location.getPartOf().getReference());
					resourcemetadataList.addAll(rPartOfChain);
				}
			}

			// status : token
			if (location.hasStatus() && location.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", location.getStatus().toCode(), location.getStatus().getSystem());
				resourcemetadataList.add(rStatus);
			}

			// type : token
			if (location.hasType()) {

				Resourcemetadata rType = null;
				for (CodeableConcept locationType : location.getType()) {

					if (locationType.hasCoding()) {
						for (Coding type : locationType.getCoding()) {
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
