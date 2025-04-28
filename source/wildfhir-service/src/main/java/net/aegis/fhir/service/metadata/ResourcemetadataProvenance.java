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
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.Provenance.ProvenanceAgentComponent;
import org.hl7.fhir.r4.model.Provenance.ProvenanceEntityComponent;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Signature;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataProvenance extends ResourcemetadataProxy {

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
        ByteArrayInputStream iProvenance = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

		try {
			// Extract and convert the resource contents to a Provenance object
			if (chainedResource != null) {
				iProvenance = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iProvenance = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			Provenance provenance = (Provenance) xmlP.parse(iProvenance);
			iProvenance.close();

			/*
			 * Create new Resourcemetadata objects for each Provenance metadata value and add to the resourcemetadataList
			 */

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, provenance, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			if (provenance.hasAgent()) {

				for (ProvenanceAgentComponent agent : provenance.getAgent()) {

					// agent : reference
					if (agent.hasWho()) {
						rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "agent", 0, agent.getWho(), null);
						resourcemetadataList.addAll(rMetadataChain);
					}

					// agent-role : token
					if (agent.hasRole()) {

						for (CodeableConcept role : agent.getRole()) {

							if (role.hasCoding()) {
								for (Coding code : role.getCoding()) {
									rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"agent-role", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
									resourcemetadataList.add(rMetadata);
								}
							}
						}
					}

					// agent-type : token
					if (agent.hasType() && agent.getType().hasCoding()) {
						for (Coding code : agent.getType().getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"agent-type", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// entity : reference
			if (provenance.hasEntity()) {
				for (ProvenanceEntityComponent entity : provenance.getEntity()) {

					if (entity.hasWhat()) {
						rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "entity", 0, entity.getWhat(), null);
						resourcemetadataList.addAll(rMetadataChain);
					}
				}
			}

			// location : reference
			if (provenance.hasLocation()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "location", 0, provenance.getLocation(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// patient : reference
			// target : reference
			if (provenance.hasTarget()) {

				for (Reference target : provenance.getTarget()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "target", 0, target, null);
					resourcemetadataList.addAll(rMetadataChain);

					// patient : reference
					if ((target.hasReference() && target.getReference().indexOf("Patient") >= 0)
							|| (target.hasType() && target.getType().equals("Patient"))) {
						rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "patient", 0, target, null);
						resourcemetadataList.addAll(rMetadataChain);
					}
				}
			}

			// recorded : date
			if (provenance.hasRecorded()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"recorded", utcDateUtil.formatDate(provenance.getRecorded(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(provenance.getRecorded(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rMetadata);
			}

			// signature-type : token
			if (provenance.hasSignature()) {
				for (Signature signature : provenance.getSignature()) {

					for (Coding sigtype : signature.getType()) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"signature-type", sigtype.getCode(), sigtype.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(sigtype));
						resourcemetadataList.add(rMetadata);
					}
				}
			}

			// when : datetime
			if (provenance.hasOccurredDateTimeType()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"when", utcDateUtil.formatDate(provenance.getOccurredDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(provenance.getOccurredDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rMetadata);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		} finally {
	        rMetadata = null;
	        rMetadataChain = null;
            if (iProvenance != null) {
                try {
                	iProvenance.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
