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
import org.hl7.fhir.r4.model.AppointmentResponse;
import org.hl7.fhir.r4.model.Identifier;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataAppointmentResponse extends ResourcemetadataProxy {

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
        ByteArrayInputStream iAppointmentResponse = null;

		try {
            // Extract and convert the resource contents to a AppointmentResponse object
			if (chainedResource != null) {
				iAppointmentResponse = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iAppointmentResponse = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            AppointmentResponse appointmentResponse = (AppointmentResponse) xmlP.parse(iAppointmentResponse);
            iAppointmentResponse.close();

			/*
             * Create new Resourcemetadata objects for each AppointmentResponse metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, appointmentResponse, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", appointmentResponse.getId());
			resourcemetadataList.add(_id);

			// _language : token
			if (appointmentResponse.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", appointmentResponse.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (appointmentResponse.getMeta() != null && appointmentResponse.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(appointmentResponse.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(appointmentResponse.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// appointment : reference
			if (appointmentResponse.hasAppointment() && appointmentResponse.getAppointment().hasReference()) {
				Resourcemetadata rAppointment = generateResourcemetadata(resource, chainedResource, chainedParameter+"appointment", generateFullLocalReference(appointmentResponse.getAppointment().getReference(), baseUrl));
				resourcemetadataList.add(rAppointment);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rAppointmentChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "appointment", 0, appointmentResponse.getAppointment().getReference());
					resourcemetadataList.addAll(rAppointmentChain);
				}
			}

			// identifier : token
			if (appointmentResponse.hasIdentifier()) {

				for (Identifier identifier : appointmentResponse.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// part-status : token
			if (appointmentResponse.hasParticipantStatus() && appointmentResponse.getParticipantStatus() !=  null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"part-status", appointmentResponse.getParticipantStatus().toCode(), appointmentResponse.getParticipantStatus().getSystem());
				resourcemetadataList.add(rStatus);
			}

			// actor : reference
			if (appointmentResponse.hasActor() && appointmentResponse.getActor().hasReference()) {
				String actorString = generateFullLocalReference(appointmentResponse.getActor().getReference(), baseUrl);

				List<Resourcemetadata> rActorChain = null;
				Resourcemetadata rActor = generateResourcemetadata(resource, chainedResource, chainedParameter+"actor", actorString);
				resourcemetadataList.add(rActor);

				if (chainedResource == null) {
					// Add chained parameters
					rActorChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "actor", 0, appointmentResponse.getActor().getReference());
					resourcemetadataList.addAll(rActorChain);
				}

				/*
				 *  Examine actor reference for specific resource types
				 */

				// location : reference
				if (actorString.indexOf("Location") >= 0) {
					Resourcemetadata rLocation = generateResourcemetadata(resource, chainedResource, chainedParameter+"location", actorString);
					resourcemetadataList.add(rLocation);

					if (chainedResource == null) {
						// Add chained parameters
						rActorChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "location", 0, appointmentResponse.getActor().getReference());
						resourcemetadataList.addAll(rActorChain);
					}
				}

				// patient : reference
				if (actorString.indexOf("Patient") >= 0) {
					Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", actorString);
					resourcemetadataList.add(rPatient);

					if (chainedResource == null) {
						// Add chained parameters
						rActorChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, appointmentResponse.getActor().getReference());
						resourcemetadataList.addAll(rActorChain);
					}
				}

				// practitioner : reference
				if (actorString.indexOf("Practitioner") >= 0) {
					Resourcemetadata rPractitioner = generateResourcemetadata(resource, chainedResource, chainedParameter+"practitioner", actorString);
					resourcemetadataList.add(rPractitioner);

					if (chainedResource == null) {
						// Add chained parameters
						rActorChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "practitioner", 0, appointmentResponse.getActor().getReference());
						resourcemetadataList.addAll(rActorChain);
					}
				}
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return resourcemetadataList;
	}

}
