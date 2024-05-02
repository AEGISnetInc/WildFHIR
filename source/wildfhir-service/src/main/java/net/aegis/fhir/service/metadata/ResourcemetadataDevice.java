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
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Device;
import org.hl7.fhir.r4.model.Device.DeviceUdiCarrierComponent;
import org.hl7.fhir.r4.model.Identifier;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataDevice extends ResourcemetadataProxy {

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
        ByteArrayInputStream iDevice = null;

		try {
			// Extract and convert the resource contents to a Device object
			if (chainedResource != null) {
				iDevice = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iDevice = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			Device device = (Device) xmlP.parse(iDevice);
			iDevice.close();

			/*
			 * Create new Resourcemetadata objects for each Device metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, device, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", device.getId());
			resourcemetadataList.add(_id);

			// _language : token
			if (device.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", device.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (device.getMeta() != null && device.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(device.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(device.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// identifier : token
			if (device.hasIdentifier()) {

				for (Identifier identifier : device.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// location : reference
			if (device.hasLocation() && device.getLocation().hasReference()) {
				Resourcemetadata rLocation = generateResourcemetadata(resource, chainedResource, chainedParameter+"location", generateFullLocalReference(device.getLocation().getReference(), baseUrl));
				resourcemetadataList.add(rLocation);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rLocationChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "location", 0, device.getLocation().getReference());
					resourcemetadataList.addAll(rLocationChain);
				}
			}

			// manufacturer : string
			if (device.hasManufacturer()) {
				Resourcemetadata rManufacturer = generateResourcemetadata(resource, chainedResource, chainedParameter+"manufacturer", device.getManufacturer());
				resourcemetadataList.add(rManufacturer);
			}

			// model : string
			if (device.hasModelNumber()) {
				Resourcemetadata rModel = generateResourcemetadata(resource, chainedResource, chainedParameter+"model", device.getModelNumber());
				resourcemetadataList.add(rModel);
			}

			// organization : reference
			if (device.hasOwner() && device.getOwner().hasReference()) {
				Resourcemetadata rOwner = generateResourcemetadata(resource, chainedResource, chainedParameter+"organization", generateFullLocalReference(device.getOwner().getReference(), baseUrl));
				resourcemetadataList.add(rOwner);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rOwnerChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "organization", 0, device.getOwner().getReference());
					resourcemetadataList.addAll(rOwnerChain);
				}
			}

			// patient : reference
			if (device.hasPatient() && device.getPatient().hasReference()) {
				Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", generateFullLocalReference(device.getPatient().getReference(), baseUrl));
				resourcemetadataList.add(rPatient);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rPatientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, device.getPatient().getReference());
					resourcemetadataList.addAll(rPatientChain);
				}
			}

			// status : token
			if (device.hasStatus() && device.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", device.getStatus().toCode(), device.getStatus().getSystem());
				resourcemetadataList.add(rStatus);
			}

			// type : token
			if (device.hasType() && device.getType().hasCoding()) {

				Resourcemetadata rType = null;
				Resourcemetadata rDeviceName = null;
				for (Coding type : device.getType().getCoding()) {
					rType = generateResourcemetadata(resource, chainedResource, chainedParameter+"type", type.getCode(), type.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(type));
					resourcemetadataList.add(rType);

					// device-name : string
					if (type.hasDisplay()) {
						rDeviceName = generateResourcemetadata(resource, chainedResource, chainedParameter+"device-name", type.getDisplay());
						resourcemetadataList.add(rDeviceName);
					}
				}

				// device-name : string
				if (device.getType().hasText()) {
					rDeviceName = generateResourcemetadata(resource, chainedResource, chainedParameter+"device-name", device.getType().getText());
					resourcemetadataList.add(rDeviceName);
				}
			}

			// url : uri
			if (device.hasUrl()) {
				Resourcemetadata rUrl = generateResourcemetadata(resource, chainedResource, chainedParameter+"url", device.getUrl());
				resourcemetadataList.add(rUrl);
			}

			if (device.hasUdiCarrier()) {

				for (DeviceUdiCarrierComponent udiCarrier : device.getUdiCarrier()) {
					// udi-di : string
					if (udiCarrier.hasDeviceIdentifier()) {
						Resourcemetadata rDeviceIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"udi-di", udiCarrier.getDeviceIdentifier());
						resourcemetadataList.add(rDeviceIdentifier);
					}

					// udi-carrier : string
					if (udiCarrier.hasCarrierHRF()) {
						Resourcemetadata rUdiCarrier = generateResourcemetadata(resource, chainedResource, chainedParameter+"udi-carrier", udiCarrier.getCarrierHRF());
						resourcemetadataList.add(rUdiCarrier);
					}
					if (udiCarrier.hasCarrierAIDC()) {
						Resourcemetadata rUdiCarrier = generateResourcemetadata(resource, chainedResource, chainedParameter+"udi-carrier", udiCarrier.getCarrierAIDC().toString());
						resourcemetadataList.add(rUdiCarrier);
					}
				}

			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
        } finally {
            if (iDevice != null) {
                try {
                    iDevice.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
