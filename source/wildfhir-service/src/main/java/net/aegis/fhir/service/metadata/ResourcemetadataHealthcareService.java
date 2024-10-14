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
import org.hl7.fhir.r4.model.HealthcareService;
import org.hl7.fhir.r4.model.Identifier;
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
public class ResourcemetadataHealthcareService extends ResourcemetadataProxy {

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
        ByteArrayInputStream iHealthcareService = null;

		try {
            // Extract and convert the resource contents to a HealthcareService object
			if (chainedResource != null) {
				iHealthcareService = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iHealthcareService = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            HealthcareService healthcareService = (HealthcareService) xmlP.parse(iHealthcareService);
            iHealthcareService.close();

			/*
             * Create new Resourcemetadata objects for each HealthcareService metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, healthcareService, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			if (healthcareService.getId() != null) {
				Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", healthcareService.getId());
				resourcemetadataList.add(_id);
			}

			// _language : token
			if (healthcareService.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", healthcareService.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (healthcareService.getMeta() != null && healthcareService.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(healthcareService.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(healthcareService.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// active : token
			if (healthcareService.hasActive()) {
				Resourcemetadata rActive = generateResourcemetadata(resource, chainedResource, chainedParameter+"active", Boolean.toString(healthcareService.getActive()));
				resourcemetadataList.add(rActive);
			}

			// characteristic : token
			if (healthcareService.hasCharacteristic()) {

				Resourcemetadata rCode = null;
				for (CodeableConcept characteristic : healthcareService.getCharacteristic()) {

					if (characteristic.hasCoding()) {
						for (Coding code : characteristic.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"characteristic", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// coverage-area : reference
			if (healthcareService.hasCoverageArea()) {

				Resourcemetadata rCoverageArea = null;
				List<Resourcemetadata> rCoverageAreaChain = null;
				for (Reference coverageArea : healthcareService.getCoverageArea()) {

					rCoverageArea = generateResourcemetadata(resource, chainedResource, chainedParameter+"coverage-area", generateFullLocalReference(coverageArea.getReference(), baseUrl));
					resourcemetadataList.add(rCoverageArea);

					if (chainedResource == null) {
						// Add chained parameters
						rCoverageAreaChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "coverage-area", 0, coverageArea.getReference(), null);
						resourcemetadataList.addAll(rCoverageAreaChain);
					}
				}
			}

			// endpoint : reference
			if (healthcareService.hasEndpoint()) {

				String endpointReference = null;
				Resourcemetadata rEndpoint = null;
				List<Resourcemetadata> rEndpointChain = null;
				for (Reference endpoint : healthcareService.getEndpoint()) {

					if (endpoint.hasReference()) {
						endpointReference = generateFullLocalReference(endpoint.getReference(), baseUrl);

						rEndpoint = generateResourcemetadata(resource, chainedResource, chainedParameter+"endpoint", endpointReference);
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
			if (healthcareService.hasIdentifier()) {

				Resourcemetadata rIdentifier = null;
				for (Identifier identifier : healthcareService.getIdentifier()) {

					rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// location : reference
			if (healthcareService.hasLocation()) {

				Resourcemetadata rLocation = null;
				List<Resourcemetadata> rLocationChain = null;
				for (Reference location : healthcareService.getLocation()) {

					rLocation = generateResourcemetadata(resource, chainedResource, chainedParameter+"location", generateFullLocalReference(location.getReference(), baseUrl));
					resourcemetadataList.add(rLocation);

					if (chainedResource == null) {
						// Add chained parameters
						rLocationChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "location", 0, location.getReference(), null);
						resourcemetadataList.addAll(rLocationChain);
					}
				}
			}

			// name : string
			if (healthcareService.hasName()) {
				Resourcemetadata rName = generateResourcemetadata(resource, chainedResource, chainedParameter+"name", healthcareService.getName());
				resourcemetadataList.add(rName);
			}

			// organization : reference
			if (healthcareService.hasProvidedBy() && healthcareService.getProvidedBy().hasReference()) {
				Resourcemetadata rProvidedBy = generateResourcemetadata(resource, chainedResource, chainedParameter+"organization", generateFullLocalReference(healthcareService.getProvidedBy().getReference(), baseUrl));
				resourcemetadataList.add(rProvidedBy);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rOrganizationChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "organization", 0, healthcareService.getProvidedBy().getReference(), null);
					resourcemetadataList.addAll(rOrganizationChain);
				}
			}

			// program : string
			if (healthcareService.hasProgram()) {

				Resourcemetadata rCode = null;
				for (CodeableConcept program : healthcareService.getProgram()) {

					if (program.hasCoding()) {
						for (Coding code : program.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"program", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// service-category : token
			if (healthcareService.hasCategory()) {

				Resourcemetadata rCode = null;
				for (CodeableConcept category : healthcareService.getCategory()) {

					if (category.hasCoding()) {
						for (Coding code : category.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"service-category", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// service-type : token
			if (healthcareService.hasType()) {

				Resourcemetadata rCode = null;
				for (CodeableConcept type : healthcareService.getType()) {

					if (type.hasCoding()) {
						for (Coding code : type.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"service-type", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// specialty : token
			if (healthcareService.hasSpecialty()) {

				Resourcemetadata rCode = null;
				for (CodeableConcept specialty : healthcareService.getSpecialty()) {

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
