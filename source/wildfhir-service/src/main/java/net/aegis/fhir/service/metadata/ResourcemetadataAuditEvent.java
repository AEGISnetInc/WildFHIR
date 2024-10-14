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
import org.hl7.fhir.r4.model.AuditEvent;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.AuditEvent.AuditEventAgentComponent;
import org.hl7.fhir.r4.model.AuditEvent.AuditEventEntityComponent;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.UriType;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataAuditEvent extends ResourcemetadataProxy {

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
        ByteArrayInputStream iAuditEvent = null;

		try {
			// Extract and convert the resource contents to a AuditEvent object
			if (chainedResource != null) {
				iAuditEvent = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iAuditEvent = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			AuditEvent auditEvent = (AuditEvent) xmlP.parse(iAuditEvent);
			iAuditEvent.close();

			/*
			 * Create new Resourcemetadata objects for each AuditEvent metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, auditEvent, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			if (auditEvent.getId() != null) {
				Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", auditEvent.getId());
				resourcemetadataList.add(_id);
			}

			// _language : token
			if (auditEvent.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", auditEvent.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (auditEvent.getMeta() != null && auditEvent.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(auditEvent.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(auditEvent.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// action : token
			if (auditEvent.hasAction() && auditEvent.getAction() != null) {
				Resourcemetadata rAction = generateResourcemetadata(resource, chainedResource, chainedParameter+"action", auditEvent.getAction().toCode(), auditEvent.getAction().getSystem());
				resourcemetadataList.add(rAction);
			}

			// date : datetime
			if (auditEvent.hasRecorded()) {
				Resourcemetadata rRecorded = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(auditEvent.getRecorded(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(auditEvent.getRecorded(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rRecorded);
			}

			// outcome : token
			if (auditEvent.hasOutcome() && auditEvent.getOutcome() != null) {
				Resourcemetadata rOutcome = generateResourcemetadata(resource, chainedResource, chainedParameter+"outcome", auditEvent.getOutcome().toCode(), auditEvent.getOutcome().getSystem());
				resourcemetadataList.add(rOutcome);
			}

			// subtype : token
			if (auditEvent.hasSubtype()) {

				for (Coding subtype : auditEvent.getSubtype()) {

					Resourcemetadata rCoding = generateResourcemetadata(resource, chainedResource, chainedParameter+"subtype", subtype.getCode(), subtype.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(subtype));
					resourcemetadataList.add(rCoding);
				}
			}

			// type : token
			if (auditEvent.hasType()) {
				Resourcemetadata rCoding = generateResourcemetadata(resource, chainedResource, chainedParameter+"type", auditEvent.getType().getCode(), auditEvent.getType().getSystem(), null, ServicesUtil.INSTANCE.getTextValue(auditEvent.getType()));
				resourcemetadataList.add(rCoding);
			}

			// auditEvent.agent
			if (auditEvent.hasAgent()) {

				Resourcemetadata rCode = null;
				for (AuditEventAgentComponent agent : auditEvent.getAgent()) {

					// agent.network.address : token
					if (agent.hasNetwork() && agent.getNetwork().hasAddress()) {
						Resourcemetadata rAddress = generateResourcemetadata(resource, chainedResource, chainedParameter+"address", agent.getNetwork().getAddress());
						resourcemetadataList.add(rAddress);
					}

					// agent.reference : reference
					if (agent.hasWho() && agent.getWho().hasReference()) {
						String objectReference = generateFullLocalReference(agent.getWho().getReference(), baseUrl);

						List<Resourcemetadata> rAgentChain = null;
						Resourcemetadata rAgent = generateResourcemetadata(resource, chainedResource, chainedParameter+"agent", objectReference);
						resourcemetadataList.add(rAgent);

						if (chainedResource == null) {
							// Add chained parameters
							rAgentChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "agent", 0, agent.getWho().getReference(), null);
							resourcemetadataList.addAll(rAgentChain);
						}

						// (patient) agent.reference : reference
						if (objectReference.contains("Patient")) {
							Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", objectReference);
							resourcemetadataList.add(rPatient);

							if (chainedResource == null) {
								// Add chained parameters
								rAgentChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, agent.getWho().getReference(), null);
								resourcemetadataList.addAll(rAgentChain);
							}
						}
					}

					// agent.name : string
					if (agent.hasName()) {
						Resourcemetadata rName = generateResourcemetadata(resource, chainedResource, chainedParameter+"agent-name", agent.getName());
						resourcemetadataList.add(rName);
					}

					// agent.role : token
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

					// agent.altid : token
					if (agent.hasAltId()) {
						Resourcemetadata rAltId = generateResourcemetadata(resource, chainedResource, chainedParameter+"altid", agent.getAltId());
						resourcemetadataList.add(rAltId);
					}

					// agent.policy : uri
					if (agent.hasPolicy()) {

						for (UriType policy : agent.getPolicy()) {

							Resourcemetadata rPolicy = generateResourcemetadata(resource, chainedResource, chainedParameter+"policy", policy.getValueAsString());
							resourcemetadataList.add(rPolicy);
						}
					}
				}
			}

			// auditEvent.entity
			if (auditEvent.hasEntity()) {

				for (AuditEventEntityComponent entity : auditEvent.getEntity()) {

					// entity.reference : reference
					if (entity.hasWhat() && entity.getWhat().hasReference()) {
						String objectReference = generateFullLocalReference(entity.getWhat().getReference(), baseUrl);

						List<Resourcemetadata> rEntityChain = null;
						Resourcemetadata rReference = generateResourcemetadata(resource, chainedResource, chainedParameter+"entity", objectReference);
						resourcemetadataList.add(rReference);

						if (chainedResource == null) {
							// Add chained parameters
							rEntityChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "entity", 0, entity.getWhat().getReference(), null);
							resourcemetadataList.addAll(rEntityChain);
						}

						// (patient) object.reference : reference
						if (objectReference.contains("Patient")) {
							Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", objectReference);
							resourcemetadataList.add(rPatient);

							if (chainedResource == null) {
								// Add chained parameters
								rEntityChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, entity.getWhat().getReference(), null);
								resourcemetadataList.addAll(rEntityChain);
							}
						}
						else {
						}
					}

					// entity.name : string
					if (entity.hasName()) {
						Resourcemetadata rName = generateResourcemetadata(resource, chainedResource, chainedParameter+"entity-name", entity.getName());
						resourcemetadataList.add(rName);
					}

					// entity.role : token
					if (entity.hasType()) {
						Resourcemetadata rRole = generateResourcemetadata(resource, chainedResource, chainedParameter+"entity-role", entity.getRole().getCode(), entity.getRole().getSystem());
						resourcemetadataList.add(rRole);
					}

					// entity.type : token
					if (entity.hasType()) {
						Resourcemetadata rType = generateResourcemetadata(resource, chainedResource, chainedParameter+"entity-type", entity.getType().getCode(), entity.getType().getSystem());
						resourcemetadataList.add(rType);
					}
				}
			}

			// auditEvent.source
			if (auditEvent.hasSource()) {

				// source.site : token
				if (auditEvent.getSource().hasSite()) {
					Resourcemetadata rSite = generateResourcemetadata(resource, chainedResource, chainedParameter+"site", auditEvent.getSource().getSite());
					resourcemetadataList.add(rSite);
				}

				// source.source : reference
				if (auditEvent.getSource().hasObserver() && auditEvent.getSource().getObserver().hasReference()) {
					String objectReference = generateFullLocalReference(auditEvent.getSource().getObserver().getReference(), baseUrl);

					List<Resourcemetadata> rSourceChain = null;
					Resourcemetadata rReference = generateResourcemetadata(resource, chainedResource, chainedParameter+"source", objectReference);
					resourcemetadataList.add(rReference);

					if (chainedResource == null) {
						// Add chained parameters
						rSourceChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "source", 0, auditEvent.getSource().getObserver().getReference(), null);
						resourcemetadataList.addAll(rSourceChain);
					}
				}
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
        } finally {
            if (iAuditEvent != null) {
                try {
                	iAuditEvent.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
