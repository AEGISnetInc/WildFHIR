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
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

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

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, practitionerRole, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// active : token
			if (practitionerRole.hasActive()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"active", Boolean.toString(practitionerRole.getActive()));
				resourcemetadataList.add(rMetadata);
			}

			// date : date(period)
			if (practitionerRole.hasPeriod()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(practitionerRole.getPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(practitionerRole.getPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(practitionerRole.getPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(practitionerRole.getPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
				resourcemetadataList.add(rMetadata);
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
								rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter + "email", telecom.getValue(), telecomSystemName);
								resourcemetadataList.add(rMetadata);
							}
							else if (telecom.getSystem().equals(ContactPointSystem.PHONE)) {
								rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter + "phone", telecom.getValue(), telecomSystemName);
								resourcemetadataList.add(rMetadata);
							}
						}

						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter + "telecom", telecom.getValue(), telecomSystemName);
						resourcemetadataList.add(rMetadata);
					}
				}
			}

			// endpoint : reference
			if (practitionerRole.hasEndpoint()) {

				for (Reference endpoint : practitionerRole.getEndpoint()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "endpoint", 0, endpoint, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// identifier : token
			if (practitionerRole.hasIdentifier()) {

				for (Identifier identifier : practitionerRole.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}

			// location : reference
			if (practitionerRole.hasLocation()) {

				for (Reference location : practitionerRole.getLocation()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "location", 0, location, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// organization : reference
			if (practitionerRole.hasOrganization()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "organization", 0, practitionerRole.getOrganization(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// practitioner : reference
			if (practitionerRole.hasPractitioner()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "practitioner", 0, practitionerRole.getPractitioner(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// role : token
			if (practitionerRole.hasCode()) {
				for (CodeableConcept role : practitionerRole.getCode()) {

					if (role.hasCoding()) {
						for (Coding code : role.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"role", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// service : reference
			if (practitionerRole.hasHealthcareService()) {

				for (Reference service : practitionerRole.getHealthcareService()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "service", 0, service, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// specialty : token
			if (practitionerRole.hasSpecialty()) {
				for (CodeableConcept specialty : practitionerRole.getSpecialty()) {

					if (specialty.hasCoding()) {
						for (Coding code : specialty.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"specialty", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
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
            if (iPractitionerRole != null) {
                try {
                	iPractitionerRole.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
