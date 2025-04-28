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
import org.hl7.fhir.r4.model.DeviceRequest;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.UriType;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataDeviceRequest extends ResourcemetadataProxy {

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
        ByteArrayInputStream iDeviceRequest = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

		try {
            // Extract and convert the resource contents to a DeviceRequest object
			if (chainedResource != null) {
				iDeviceRequest = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iDeviceRequest = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            DeviceRequest deviceRequest = (DeviceRequest) xmlP.parse(iDeviceRequest);
            iDeviceRequest.close();

			/*
             * Create new Resourcemetadata objects for each DeviceRequest metadata value and add to the resourcemetadataList
			 */

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, deviceRequest, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// authored-on : date
			if (deviceRequest.hasAuthoredOn()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"authored-on", utcDateUtil.formatDate(deviceRequest.getAuthoredOn(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(deviceRequest.getAuthoredOn(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rMetadata);
			}

			// based-on : reference
			if (deviceRequest.hasBasedOn()) {

				for (Reference basedOn : deviceRequest.getBasedOn()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "based-on", 0, basedOn, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// code : token
			if (deviceRequest.hasCodeCodeableConcept() && deviceRequest.getCodeCodeableConcept().hasCoding()) {

				for (Coding code : deviceRequest.getCodeCodeableConcept().getCoding()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rMetadata);
				}
			}

			// device : reference
			if (deviceRequest.hasCodeReference()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "device", 0, deviceRequest.getCodeReference(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// encounter : reference
			if (deviceRequest.hasEncounter()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "encounter", 0, deviceRequest.getEncounter(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// event-date : date(period)
			if (deviceRequest.hasOccurrencePeriod()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"event-date", utcDateUtil.formatDate(deviceRequest.getOccurrencePeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(deviceRequest.getOccurrencePeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(deviceRequest.getOccurrencePeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(deviceRequest.getOccurrencePeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
				resourcemetadataList.add(rMetadata);
			}
			if (deviceRequest.hasOccurrenceDateTimeType()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"event-date", utcDateUtil.formatDate(deviceRequest.getOccurrenceDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(deviceRequest.getOccurrenceDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rMetadata);
			}

			// group-identifier : token
			if (deviceRequest.hasGroupIdentifier()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"group-identifier", deviceRequest.getGroupIdentifier().getValue(), deviceRequest.getGroupIdentifier().getSystem(), null, ServicesUtil.INSTANCE.getTextValue(deviceRequest.getGroupIdentifier()));
				resourcemetadataList.add(rMetadata);
			}

			// identifier : token
			if (deviceRequest.hasIdentifier()) {

				for (Identifier identifier : deviceRequest.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}

			// instantiates-canonical : reference - instantiates is a Canonical, no Reference.identifier
			if (deviceRequest.hasInstantiatesCanonical()) {

				for (CanonicalType instantiates : deviceRequest.getInstantiatesCanonical()) {
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
			if (deviceRequest.hasInstantiatesUri()) {

				for (UriType instantiates : deviceRequest.getInstantiatesUri()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"instantiates-uri", instantiates.asStringValue());
					resourcemetadataList.add(rMetadata);
				}
			}

			// insurance : reference
			if (deviceRequest.hasInsurance()) {

				for (Reference insurance : deviceRequest.getInsurance()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "insurance", 0, insurance, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// intent : token
			if (deviceRequest.hasIntent() && deviceRequest.getIntent() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"intent", deviceRequest.getIntent().toCode(), deviceRequest.getIntent().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// subject : reference
			if (deviceRequest.hasSubject()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "subject", 0, deviceRequest.getSubject(), null);
				resourcemetadataList.addAll(rMetadataChain);

				// patient : reference
				if ((deviceRequest.getSubject().hasReference() && deviceRequest.getSubject().getReference().indexOf("Patient") >= 0)
						|| (deviceRequest.getSubject().hasType() && deviceRequest.getSubject().getType().equals("Patient"))) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "patient", 0, deviceRequest.getSubject(), null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// performer : reference
			if (deviceRequest.hasPerformer()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "performer", 0, deviceRequest.getPerformer(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// prior-request : reference
			if (deviceRequest.hasPriorRequest()) {

				for (Reference priorRequest : deviceRequest.getPriorRequest()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "prior-request", 0, priorRequest, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// requester : reference
			if (deviceRequest.hasRequester()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "requester", 0, deviceRequest.getRequester(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// status : token
			if (deviceRequest.hasStatus() && deviceRequest.getStatus() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", deviceRequest.getStatus().toCode(), deviceRequest.getStatus().getSystem());
				resourcemetadataList.add(rMetadata);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		} finally {
	        rMetadata = null;
	        rMetadataChain = null;
            if (iDeviceRequest != null) {
                try {
                	iDeviceRequest.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
