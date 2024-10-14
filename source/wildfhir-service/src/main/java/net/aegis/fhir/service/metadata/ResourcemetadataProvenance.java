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

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, provenance, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			if (provenance.getId() != null) {
				Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", provenance.getId());
				resourcemetadataList.add(_id);
			}

			// _language : token
			if (provenance.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", provenance.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (provenance.getMeta() != null && provenance.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(provenance.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(provenance.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			if (provenance.hasAgent()) {

				Resourcemetadata rCode = null;
				List<Resourcemetadata> rAgentChain = null;
				for (ProvenanceAgentComponent agent : provenance.getAgent()) {

					// agent : reference
					if (agent.hasWho() && agent.getWho().hasReference()) {
						Resourcemetadata rWho = generateResourcemetadata(resource, chainedResource, chainedParameter+"agent", generateFullLocalReference(agent.getWho().getReference(), baseUrl));
						resourcemetadataList.add(rWho);

						if (chainedResource == null) {
							// Add chained parameters for any
							rAgentChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "agent", 0, agent.getWho().getReference(), null);
							resourcemetadataList.addAll(rAgentChain);
						}
					}

					// agent-role : token
					if (agent.hasRole()) {

						for (CodeableConcept role : agent.getRole()) {

							if (role.hasCoding()) {
								for (Coding code : role.getCoding()) {
									rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"agent-role", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
									resourcemetadataList.add(rCode);
								}
							}
						}
					}

					// agent-type : token
					if (agent.hasType() && agent.getType().hasCoding()) {
						for (Coding code : agent.getType().getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"agent-type", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// entity : reference
			if (provenance.hasEntity()) {

				List<Resourcemetadata> rEntityChain = null;
				for (ProvenanceEntityComponent entity : provenance.getEntity()) {

					if (entity.hasWhat() && entity.getWhat().hasReference()) {
						Resourcemetadata rEntity = generateResourcemetadata(resource, chainedResource, chainedParameter+"entity", entity.getWhat().getReference());
						resourcemetadataList.add(rEntity);

						if (chainedResource == null) {
							// Add chained parameters for any
							rEntityChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "entity", 0, entity.getWhat().getReference(), null);
							resourcemetadataList.addAll(rEntityChain);
						}
					}
				}
			}

			// location : reference
			if (provenance.hasLocation() && provenance.getLocation().hasReference()) {
				Resourcemetadata rLocation = generateResourcemetadata(resource, chainedResource, chainedParameter+"location", generateFullLocalReference(provenance.getLocation().getReference(), baseUrl));
				resourcemetadataList.add(rLocation);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rLocationChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "location", 0, provenance.getLocation().getReference(), null);
					resourcemetadataList.addAll(rLocationChain);
				}
			}

			// patient : reference
			// target : reference
			if (provenance.hasTarget()) {

				String targetReference = null;
				List<Resourcemetadata> rTargetChain = null;
				for (Reference target : provenance.getTarget()) {

					if (target.hasReference()) {
						targetReference = generateFullLocalReference(target.getReference(), baseUrl);

						Resourcemetadata rTarget = generateResourcemetadata(resource, chainedResource, chainedParameter+"target", targetReference);
						resourcemetadataList.add(rTarget);

						if (chainedResource == null) {
							// Add chained parameters for any
							rTargetChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "target", 0, target.getReference(), null);
							resourcemetadataList.addAll(rTargetChain);
						}

						if (target.getReference().contains("Patient")) {
							Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", targetReference);
							resourcemetadataList.add(rPatient);

							if (chainedResource == null) {
								// Add chained parameters
								rTargetChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, target.getReference(), null);
								resourcemetadataList.addAll(rTargetChain);
							}
						}
					}
				}
			}

			// recorded : date
			if (provenance.hasRecorded()) {
				Resourcemetadata rRecorded = generateResourcemetadata(resource, chainedResource, chainedParameter+"recorded", utcDateUtil.formatDate(provenance.getRecorded(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(provenance.getRecorded(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rRecorded);
			}

			// signature-type : token
			if (provenance.hasSignature()) {

				for (Signature signature : provenance.getSignature()) {

					for (Coding sigtype : signature.getType()) {

						Resourcemetadata rAdditiveType = generateResourcemetadata(resource, chainedResource, chainedParameter+"signature-type", sigtype.getCode(), sigtype.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(sigtype));
						resourcemetadataList.add(rAdditiveType);
					}
				}
			}

			// when : datetime
			if (provenance.hasOccurredDateTimeType()) {
				Resourcemetadata rWhen = generateResourcemetadata(resource, chainedResource, chainedParameter+"when", utcDateUtil.formatDate(provenance.getOccurredDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(provenance.getOccurredDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rWhen);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return resourcemetadataList;
	}

}
