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
        ByteArrayInputStream iDeviceRequest = null;

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

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, deviceRequest, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", deviceRequest.getId());
			resourcemetadataList.add(_id);

			// _language : token
			if (deviceRequest.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", deviceRequest.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (deviceRequest.getMeta() != null && deviceRequest.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(deviceRequest.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(deviceRequest.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// authored-on : date
			if (deviceRequest.hasAuthoredOn()) {
				Resourcemetadata rAuthoredOn = generateResourcemetadata(resource, chainedResource, chainedParameter+"authored-on", utcDateUtil.formatDate(deviceRequest.getAuthoredOn(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(deviceRequest.getAuthoredOn(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rAuthoredOn);
			}

			// based-on : reference
			if (deviceRequest.hasBasedOn()) {

				List<Resourcemetadata> rBasedOnChain = null;
				for (Reference basedOn : deviceRequest.getBasedOn()) {

					if (basedOn.hasReference()) {
						Resourcemetadata rBasedOn = generateResourcemetadata(resource, chainedResource, chainedParameter+"based-on", generateFullLocalReference(basedOn.getReference(), baseUrl));
						resourcemetadataList.add(rBasedOn);

						if (chainedResource == null) {
							// Add chained parameters for any
							rBasedOnChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "based-on", 0, basedOn.getReference());
							resourcemetadataList.addAll(rBasedOnChain);
						}
					}
				}
			}

			// code : token
			if (deviceRequest.hasCodeCodeableConcept() && deviceRequest.getCodeCodeableConcept().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : deviceRequest.getCodeCodeableConcept().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			// device : reference
			if (deviceRequest.hasCodeReference()) {
				Resourcemetadata rDevice = generateResourcemetadata(resource, chainedResource, chainedParameter+"device", generateFullLocalReference(deviceRequest.getCodeReference().getReference(), baseUrl));
				resourcemetadataList.add(rDevice);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rDeviceChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "device", 0, deviceRequest.getCodeReference().getReference());
					resourcemetadataList.addAll(rDeviceChain);
				}
			}

			// encounter : reference
			if (deviceRequest.hasEncounter()) {
				Resourcemetadata rEncounter = generateResourcemetadata(resource, chainedResource, chainedParameter+"encounter", generateFullLocalReference(deviceRequest.getEncounter().getReference(), baseUrl));
				resourcemetadataList.add(rEncounter);

				if (chainedResource == null) {
					// Add chained parameters for any
					List<Resourcemetadata> rEncounterChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "encounter", 0, deviceRequest.getEncounter().getReference());
					resourcemetadataList.addAll(rEncounterChain);
				}
			}

			// event-date : date(period)
			if (deviceRequest.hasOccurrencePeriod()) {
				Resourcemetadata rOccurrencePeriod = generateResourcemetadata(resource, chainedResource, chainedParameter+"event-date", utcDateUtil.formatDate(deviceRequest.getOccurrencePeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(deviceRequest.getOccurrencePeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(deviceRequest.getOccurrencePeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(deviceRequest.getOccurrencePeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
				resourcemetadataList.add(rOccurrencePeriod);
			}
			if (deviceRequest.hasOccurrenceDateTimeType()) {
				Resourcemetadata rOccurrenceDateTime = generateResourcemetadata(resource, chainedResource, chainedParameter+"event-date", utcDateUtil.formatDate(deviceRequest.getOccurrenceDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(deviceRequest.getOccurrenceDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rOccurrenceDateTime);
			}

			// group-identifier : token
			if (deviceRequest.hasGroupIdentifier()) {
				Resourcemetadata rGroupIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"group-identifier", deviceRequest.getGroupIdentifier().getValue(), deviceRequest.getGroupIdentifier().getSystem(), null, ServicesUtil.INSTANCE.getTextValue(deviceRequest.getGroupIdentifier()));
				resourcemetadataList.add(rGroupIdentifier);
			}

			// identifier : token
			if (deviceRequest.hasIdentifier()) {

				for (Identifier identifier : deviceRequest.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// instantiates-canonical : reference
			if (deviceRequest.hasInstantiatesCanonical()) {

				for (CanonicalType instantiates : deviceRequest.getInstantiatesCanonical()) {
					String objectReference = generateFullLocalReference(instantiates.asStringValue(), baseUrl);

					List<Resourcemetadata> rInstantiatesChain = null;
					Resourcemetadata rReference = generateResourcemetadata(resource, chainedResource, chainedParameter+"instantiates-canonical", objectReference);
					resourcemetadataList.add(rReference);

					if (chainedResource == null) {
						// Add chained parameters
						rInstantiatesChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "instantiates-canonical", 0, instantiates.asStringValue());
						resourcemetadataList.addAll(rInstantiatesChain);
					}
				}
			}

			// instantiates-uri : uri
			if (deviceRequest.hasInstantiatesUri()) {

				for (UriType instantiates : deviceRequest.getInstantiatesUri()) {

					Resourcemetadata rInstantiates = generateResourcemetadata(resource, chainedResource, chainedParameter+"instantiates-uri", instantiates.asStringValue());
					resourcemetadataList.add(rInstantiates);
				}
			}

			// insurance : reference
			if (deviceRequest.hasInsurance()) {

				Resourcemetadata rInsurance = null;
				List<Resourcemetadata> rFillerChain = null;
				for (Reference insurance : deviceRequest.getInsurance()) {

					if (insurance.hasReference()) {
						rInsurance = generateResourcemetadata(resource, chainedResource, chainedParameter+"insurance", generateFullLocalReference(insurance.getReference(), baseUrl));
						resourcemetadataList.add(rInsurance);

						if (chainedResource == null) {
							// Add chained parameters for any
							rFillerChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "insurance", 0, deviceRequest.getPerformer().getReference());
							resourcemetadataList.addAll(rFillerChain);
						}
					}
				}
			}

			// intent : token
			if (deviceRequest.hasIntent() && deviceRequest.getIntent() != null) {
				Resourcemetadata rIntent = generateResourcemetadata(resource, chainedResource, chainedParameter+"intent", deviceRequest.getIntent().toCode(), deviceRequest.getIntent().getSystem());
				resourcemetadataList.add(rIntent);
			}

			// patient : reference
			// subject : reference
			if (deviceRequest.hasSubject() && deviceRequest.getSubject().hasReference()) {
				String subjectReference = generateFullLocalReference(deviceRequest.getSubject().getReference(), baseUrl);

				Resourcemetadata rSubject = generateResourcemetadata(resource, chainedResource, chainedParameter+"subject", subjectReference);
				resourcemetadataList.add(rSubject);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rSubjectChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "subject", 0, deviceRequest.getSubject().getReference());
					resourcemetadataList.addAll(rSubjectChain);
				}

				if (subjectReference.indexOf("Patient") >= 0) {
					Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", subjectReference);
					resourcemetadataList.add(rPatient);

					if (chainedResource == null) {
						// Add chained parameters
						List<Resourcemetadata> rPatientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, deviceRequest.getSubject().getReference());
						resourcemetadataList.addAll(rPatientChain);
					}
				}
			}

			// performer : reference
			if (deviceRequest.hasPerformer()) {
				Resourcemetadata rFiller = generateResourcemetadata(resource, chainedResource, chainedParameter+"performer", generateFullLocalReference(deviceRequest.getPerformer().getReference(), baseUrl));
				resourcemetadataList.add(rFiller);

				if (chainedResource == null) {
					// Add chained parameters for any
					List<Resourcemetadata> rFillerChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "performer", 0, deviceRequest.getPerformer().getReference());
					resourcemetadataList.addAll(rFillerChain);
				}
			}

			// prior-request : reference
			if (deviceRequest.hasPriorRequest()) {

				List<Resourcemetadata> rPriorRequestChain = null;
				for (Reference priorRequest : deviceRequest.getPriorRequest()) {

					if (priorRequest.hasReference()) {
						Resourcemetadata rRecipient = generateResourcemetadata(resource, chainedResource, chainedParameter+"prior-request", generateFullLocalReference(priorRequest.getReference(), baseUrl));
						resourcemetadataList.add(rRecipient);

						if (chainedResource == null) {
							// Add chained parameters for any
							rPriorRequestChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "prior-request", 0, priorRequest.getReference());
							resourcemetadataList.addAll(rPriorRequestChain);
						}
					}
				}
			}

			// requester : reference
			if (deviceRequest.hasRequester() && deviceRequest.getRequester().hasReference()) {
				Resourcemetadata rRequester = generateResourcemetadata(resource, chainedResource, chainedParameter+"requester", generateFullLocalReference(deviceRequest.getRequester().getReference(), baseUrl));
				resourcemetadataList.add(rRequester);

				if (chainedResource == null) {
					// Add chained parameters for any
					List<Resourcemetadata> rRequesterChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "requester", 0, deviceRequest.getRequester().getReference());
					resourcemetadataList.addAll(rRequesterChain);
				}
			}

			// status : token
			if (deviceRequest.hasStatus() && deviceRequest.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", deviceRequest.getStatus().toCode(), deviceRequest.getStatus().getSystem());
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
