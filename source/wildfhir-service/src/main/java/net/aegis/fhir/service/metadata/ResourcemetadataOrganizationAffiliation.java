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
import org.hl7.fhir.r4.model.OrganizationAffiliation;
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
public class ResourcemetadataOrganizationAffiliation extends ResourcemetadataProxy {

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
        ByteArrayInputStream iOrganizationAffiliation = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

		try {
            // Extract and convert the resource contents to a OrganizationAffiliation object
			if (chainedResource != null) {
				iOrganizationAffiliation = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iOrganizationAffiliation = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            OrganizationAffiliation organizationAffiliation = (OrganizationAffiliation) xmlP.parse(iOrganizationAffiliation);
            iOrganizationAffiliation.close();

			/*
             * Create new Resourcemetadata objects for each OrganizationAffiliation metadata value and add to the resourcemetadataList
			 */

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, organizationAffiliation, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// active : token
			if (organizationAffiliation.hasActive()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"active", Boolean.toString(organizationAffiliation.getActive()));
				resourcemetadataList.add(rMetadata);
			}

			// date : date(period)
			if (organizationAffiliation.hasPeriod()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(organizationAffiliation.getPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(organizationAffiliation.getPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(organizationAffiliation.getPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(organizationAffiliation.getPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
				resourcemetadataList.add(rMetadata);
			}

			// (email) telecom.system=email : token
			// (phone) telecom.system=phone : token
			// telecom : token
			if (organizationAffiliation.hasTelecom()) {

				for (ContactPoint telecom : organizationAffiliation.getTelecom()) {

					String telecomSystemName = null;
					if (telecom.hasSystem() && telecom.getSystem() != null) {

						telecomSystemName = telecom.getSystem().toCode();

						if (telecom.getSystem().equals(ContactPointSystem.EMAIL)) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"email", telecom.getValue(), telecomSystemName);
							resourcemetadataList.add(rMetadata);
						}
						else if (telecom.getSystem().equals(ContactPointSystem.PHONE)) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"phone", telecom.getValue(), telecomSystemName);
							resourcemetadataList.add(rMetadata);
						}
					}

					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"telecom", telecom.getValue(), telecomSystemName);
					resourcemetadataList.add(rMetadata);
				}
			}

			// endpoint : reference
			if (organizationAffiliation.hasEndpoint()) {

				for (Reference endpoint : organizationAffiliation.getEndpoint()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "endpoint", 0, endpoint, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// identifier : token
			if (organizationAffiliation.hasIdentifier()) {

				for (Identifier identifier : organizationAffiliation.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}

			// location : reference
			if (organizationAffiliation.hasLocation()) {

				for (Reference location : organizationAffiliation.getLocation()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "location", 0, location, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// network : reference
			if (organizationAffiliation.hasNetwork()) {

				for (Reference network : organizationAffiliation.getNetwork()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "network", 0, network, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// participating-organization : reference
			if (organizationAffiliation.hasParticipatingOrganization()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "participating-organization", 0, organizationAffiliation.getParticipatingOrganization(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// primary-organization : reference
			if (organizationAffiliation.hasOrganization()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "primary-organization", 0, organizationAffiliation.getOrganization(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// role : token
			if (organizationAffiliation.hasCode()) {
				for (CodeableConcept role : organizationAffiliation.getCode()) {

					if (role.hasCoding()) {
						for (Coding code : role.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"role", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// service : reference
			if (organizationAffiliation.hasHealthcareService()) {

				for (Reference service : organizationAffiliation.getHealthcareService()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "service", 0, service, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// specialty : token
			if (organizationAffiliation.hasSpecialty()) {
				for (CodeableConcept specialty : organizationAffiliation.getSpecialty()) {

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
            if (iOrganizationAffiliation != null) {
                try {
                	iOrganizationAffiliation.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
