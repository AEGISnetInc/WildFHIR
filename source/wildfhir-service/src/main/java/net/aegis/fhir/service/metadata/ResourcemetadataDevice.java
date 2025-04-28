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

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;

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
        ByteArrayInputStream iDevice = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

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

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, device, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// identifier : token
			if (device.hasIdentifier()) {

				for (Identifier identifier : device.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}

			// location : reference
			if (device.hasLocation()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "location", 0, device.getLocation(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// manufacturer : string
			if (device.hasManufacturer()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"manufacturer", device.getManufacturer());
				resourcemetadataList.add(rMetadata);
			}

			// model : string
			if (device.hasModelNumber()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"model", device.getModelNumber());
				resourcemetadataList.add(rMetadata);
			}

			// organization : reference
			if (device.hasOwner()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "organization", 0, device.getOwner(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// patient : reference
			if (device.hasPatient()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "patient", 0, device.getPatient(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// status : token
			if (device.hasStatus() && device.getStatus() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", device.getStatus().toCode(), device.getStatus().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// type : token
			if (device.hasType() && device.getType().hasCoding()) {

				for (Coding type : device.getType().getCoding()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"type", type.getCode(), type.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(type));
					resourcemetadataList.add(rMetadata);

					// device-name : string
					if (type.hasDisplay()) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"device-name", type.getDisplay());
						resourcemetadataList.add(rMetadata);
					}
				}

				// device-name : string
				if (device.getType().hasText()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"device-name", device.getType().getText());
					resourcemetadataList.add(rMetadata);
				}
			}

			// url : uri
			if (device.hasUrl()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"url", device.getUrl());
				resourcemetadataList.add(rMetadata);
			}

			if (device.hasUdiCarrier()) {

				for (DeviceUdiCarrierComponent udiCarrier : device.getUdiCarrier()) {
					// udi-di : string
					if (udiCarrier.hasDeviceIdentifier()) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"udi-di", udiCarrier.getDeviceIdentifier());
						resourcemetadataList.add(rMetadata);
					}

					// udi-carrier : string
					if (udiCarrier.hasCarrierHRF()) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"udi-carrier", udiCarrier.getCarrierHRF());
						resourcemetadataList.add(rMetadata);
					}
					if (udiCarrier.hasCarrierAIDC()) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"udi-carrier", udiCarrier.getCarrierAIDC().toString());
						resourcemetadataList.add(rMetadata);
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
