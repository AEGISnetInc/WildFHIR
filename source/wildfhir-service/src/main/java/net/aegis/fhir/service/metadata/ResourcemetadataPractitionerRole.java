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

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Reference;

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataPractitionerRole extends ResourcemetadataProxy {

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
        ByteArrayInputStream iPractitionerRole = null;

		try {
            // Extract and convert the resource contents to a PractitionerRole object
			if (chainedResource != null) {
				iPractitionerRole = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iPractitionerRole = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            PractitionerRole practitionerRole = (PractitionerRole) xmlP.parse(iPractitionerRole);
            iPractitionerRole.close();

			/*
             * Create new Resourcemetadata objects for each PractitionerRole metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, practitionerRole, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			if (practitionerRole.getId() != null) {
				Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", practitionerRole.getId());
				resourcemetadataList.add(_id);
			}

			// _language : token
			if (practitionerRole.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", practitionerRole.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (practitionerRole.getMeta() != null && practitionerRole.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(practitionerRole.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(practitionerRole.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// active : token
			if (practitionerRole.hasActive()) {
				Resourcemetadata rActive = generateResourcemetadata(resource, chainedResource, chainedParameter+"active", Boolean.toString(practitionerRole.getActive()));
				resourcemetadataList.add(rActive);
			}

			// date : date(period)
			if (practitionerRole.hasPeriod()) {
				Resourcemetadata rDate = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(practitionerRole.getPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(practitionerRole.getPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(practitionerRole.getPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(practitionerRole.getPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
				resourcemetadataList.add(rDate);
			}

			// (email) telecom.system=email : token
			// (phone) telecom.system=phone : token
			// telecom : token
			if (practitionerRole.hasTelecom()) {

				for (ContactPoint telecom : practitionerRole.getTelecom()) {

					if (telecom.hasValue()) {

						String telecomSystemName = null;
						if (telecom.hasSystem() && telecom.getSystem() != null) {

							telecomSystemName = telecom.getSystem().toCode();

							if (telecom.getSystem().equals(ContactPointSystem.EMAIL)) {

								Resourcemetadata rTelecom = generateResourcemetadata(resource, chainedResource, chainedParameter + "email", telecom.getValue(), telecomSystemName);
								resourcemetadataList.add(rTelecom);
							}
							else if (telecom.getSystem().equals(ContactPointSystem.PHONE)) {

								Resourcemetadata rTelecom = generateResourcemetadata(resource, chainedResource, chainedParameter + "phone", telecom.getValue(), telecomSystemName);
								resourcemetadataList.add(rTelecom);
							}
						}

						Resourcemetadata rTelecom = generateResourcemetadata(resource, chainedResource, chainedParameter + "telecom", telecom.getValue(), telecomSystemName);
						resourcemetadataList.add(rTelecom);
					}
				}
			}

			// endpoint : reference
			if (practitionerRole.hasEndpoint()) {

				Resourcemetadata rEndpoint = null;
				List<Resourcemetadata> rEndpointChain = null;
				for (Reference endpoint : practitionerRole.getEndpoint()) {

					if (endpoint.hasReference()) {
						rEndpoint = generateResourcemetadata(resource, chainedResource, chainedParameter+"endpoint", generateFullLocalReference(endpoint.getReference(), baseUrl));
						resourcemetadataList.add(rEndpoint);

						if (chainedResource == null) {
							// Add chained parameters
							rEndpointChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "endpoint", 0, endpoint.getReference(), null);
							resourcemetadataList.addAll(rEndpointChain);
						}
					}
				}
			}

			// identifier : token
			if (practitionerRole.hasIdentifier()) {

				Resourcemetadata rIdentifier = null;
				for (Identifier identifier : practitionerRole.getIdentifier()) {

					rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// location : reference
			if (practitionerRole.hasLocation()) {

				Resourcemetadata rLocation = null;
				List<Resourcemetadata> rLocationChain = null;
				for (Reference location : practitionerRole.getLocation()) {

					if (location.hasReference()) {
						rLocation = generateResourcemetadata(resource, chainedResource, chainedParameter+"location", generateFullLocalReference(location.getReference(), baseUrl));
						resourcemetadataList.add(rLocation);

						if (chainedResource == null) {
							// Add chained parameters
							rLocationChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "location", 0, location.getReference(), null);
							resourcemetadataList.addAll(rLocationChain);
						}
					}
				}
			}

			// organization : reference
			if (practitionerRole.hasOrganization() && practitionerRole.getOrganization().hasReference()) {
				Resourcemetadata rOrganization = generateResourcemetadata(resource, chainedResource, chainedParameter+"organization", generateFullLocalReference(practitionerRole.getOrganization().getReference(), baseUrl));
				resourcemetadataList.add(rOrganization);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rOrganizationChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "organization", 0, practitionerRole.getOrganization().getReference(), null);
					resourcemetadataList.addAll(rOrganizationChain);
				}
			}

			// practitioner : reference
			if (practitionerRole.hasPractitioner() && practitionerRole.getPractitioner().hasReference()) {
				Resourcemetadata rPractitioner = generateResourcemetadata(resource, chainedResource, chainedParameter+"practitioner", generateFullLocalReference(practitionerRole.getPractitioner().getReference(), baseUrl));
				resourcemetadataList.add(rPractitioner);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rPractitionerChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "practitioner", 0, practitionerRole.getPractitioner().getReference(), null);
					resourcemetadataList.addAll(rPractitionerChain);
				}
			}

			// role : token
			if (practitionerRole.hasCode()) {

				Resourcemetadata rCode = null;
				for (CodeableConcept role : practitionerRole.getCode()) {

					if (role.hasCoding()) {
						for (Coding code : role.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"role", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// service : reference
			if (practitionerRole.hasHealthcareService()) {

				Resourcemetadata rService = null;
				List<Resourcemetadata> rServiceChain = null;
				for (Reference service : practitionerRole.getHealthcareService()) {

					if (service.hasReference()) {
						rService = generateResourcemetadata(resource, chainedResource, chainedParameter+"service", generateFullLocalReference(service.getReference(), baseUrl));
						resourcemetadataList.add(rService);

						if (chainedResource == null) {
							// Add chained parameters
							rServiceChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "service", 0, service.getReference(), null);
							resourcemetadataList.addAll(rServiceChain);
						}
					}
				}
			}

			// specialty : token
			if (practitionerRole.hasSpecialty()) {

				Resourcemetadata rCode = null;
				for (CodeableConcept specialty : practitionerRole.getSpecialty()) {

					if (specialty.hasCoding()) {
						for (Coding code : specialty.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"specialty", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
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
