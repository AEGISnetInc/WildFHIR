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
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

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

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, healthcareService, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// active : token
			if (healthcareService.hasActive()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"active", Boolean.toString(healthcareService.getActive()));
				resourcemetadataList.add(rMetadata);
			}

			// characteristic : token
			if (healthcareService.hasCharacteristic()) {
				for (CodeableConcept characteristic : healthcareService.getCharacteristic()) {

					if (characteristic.hasCoding()) {
						for (Coding code : characteristic.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"characteristic", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// coverage-area : reference
			if (healthcareService.hasCoverageArea()) {

				for (Reference coverageArea : healthcareService.getCoverageArea()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "coverage-area", 0, coverageArea, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// endpoint : reference
			if (healthcareService.hasEndpoint()) {

				for (Reference endpoint : healthcareService.getEndpoint()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "endpoint", 0, endpoint, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// identifier : token
			if (healthcareService.hasIdentifier()) {

				for (Identifier identifier : healthcareService.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}

			// location : reference
			if (healthcareService.hasLocation()) {

				for (Reference location : healthcareService.getLocation()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "location", 0, location, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// name : string
			if (healthcareService.hasName()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"name", healthcareService.getName());
				resourcemetadataList.add(rMetadata);
			}

			// organization : reference
			if (healthcareService.hasProvidedBy()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "organization", 0, healthcareService.getProvidedBy(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// program : string
			if (healthcareService.hasProgram()) {
				for (CodeableConcept program : healthcareService.getProgram()) {

					if (program.hasCoding()) {
						for (Coding code : program.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"program", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// service-category : token
			if (healthcareService.hasCategory()) {
				for (CodeableConcept category : healthcareService.getCategory()) {

					if (category.hasCoding()) {
						for (Coding code : category.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"service-category", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// service-type : token
			if (healthcareService.hasType()) {
				for (CodeableConcept type : healthcareService.getType()) {

					if (type.hasCoding()) {
						for (Coding code : type.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"service-type", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// specialty : token
			if (healthcareService.hasSpecialty()) {
				for (CodeableConcept specialty : healthcareService.getSpecialty()) {

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
            if (iHealthcareService != null) {
                try {
                	iHealthcareService.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
