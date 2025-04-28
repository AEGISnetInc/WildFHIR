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
        ByteArrayInputStream iAppointmentResponse = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

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

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, appointmentResponse, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// appointment : reference
			if (appointmentResponse.hasAppointment()) {
				Resourcemetadata rAppointment = generateResourcemetadata(resource, chainedResource, chainedParameter+"appointment", generateFullLocalReference(appointmentResponse.getAppointment().getReference(), baseUrl));
				resourcemetadataList.add(rAppointment);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rAppointmentChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "appointment", 0, appointmentResponse.getAppointment().getReference(), null);
					resourcemetadataList.addAll(rAppointmentChain);
				}
			}

			// identifier : token
			if (appointmentResponse.hasIdentifier()) {

				for (Identifier identifier : appointmentResponse.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}

			// part-status : token
			if (appointmentResponse.hasParticipantStatus() && appointmentResponse.getParticipantStatus() !=  null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"part-status", appointmentResponse.getParticipantStatus().toCode(), appointmentResponse.getParticipantStatus().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// actor : reference
			if (appointmentResponse.hasActor()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "actor", 0, appointmentResponse.getActor(), null);
				resourcemetadataList.addAll(rMetadataChain);

				/*
				 *  Examine actor reference for specific resource types
				 */

				// location : reference
				if ((appointmentResponse.getActor().hasReference() && appointmentResponse.getActor().getReference().indexOf("Location") >= 0)
						|| (appointmentResponse.getActor().hasType() && appointmentResponse.getActor().getType().equals("Location"))) {

					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "location", 0, appointmentResponse.getActor(), null);
					resourcemetadataList.addAll(rMetadataChain);
				}
				// patient : reference
				else if ((appointmentResponse.getActor().hasReference() && appointmentResponse.getActor().getReference().indexOf("Patient") >= 0)
						|| (appointmentResponse.getActor().hasType() && appointmentResponse.getActor().getType().equals("Patient"))) {

					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "patient", 0, appointmentResponse.getActor(), null);
					resourcemetadataList.addAll(rMetadataChain);
				}
				// practitioner : reference
				else if ((appointmentResponse.getActor().hasReference() && appointmentResponse.getActor().getReference().indexOf("Practitioner") >= 0)
						|| (appointmentResponse.getActor().hasType() && appointmentResponse.getActor().getType().equals("Practitioner"))) {

					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "practitioner", 0, appointmentResponse.getActor(), null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		} finally {
	        rMetadata = null;
	        rMetadataChain = null;
            if (iAppointmentResponse != null) {
                try {
                	iAppointmentResponse.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
