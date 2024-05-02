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
package net.aegis.fhir.service.linked;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Provenance.ProvenanceAgentComponent;
import org.hl7.fhir.r4.model.Provenance.ProvenanceEntityComponent;
import org.hl7.fhir.r4.model.Resource;

import net.aegis.fhir.service.ResourceService;

/**
 * @author richard.ettema
 *
 */
public class LinkedResourceProvenance extends LinkedResourceProxy {

	private Logger log = Logger.getLogger("LinkedResourceProvenance");

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.linked.LinkedResourceProxy#getLinkedResources(net.aegis.fhir.service.ResourceService, org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public List<Resource> getLinkedResources(ResourceService resourceService, Resource containerResource) throws Exception {

		log.fine("[START] LinkedResourceProvenance.getLinkedResources()");

		List<Resource> linkedResources = new ArrayList<Resource>();

		try {
			// Test for valid container Resource
			if (containerResource != null && containerResource instanceof Provenance) {

				Provenance typedContainerResource = (Provenance) containerResource;

				/*
				 * Provenance linked Resource references
				 */
				String ref = null;
				Resource linkedResource = null;

				// target
				if (typedContainerResource.hasTarget()) {

					for (Reference target : typedContainerResource.getTarget()) {
						if (target.hasReference()) {

							ref = target.getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// location (Location)
				if (typedContainerResource.hasLocation() && typedContainerResource.getLocation().hasReference()) {

					ref = typedContainerResource.getLocation().getReference();
					linkedResource = this.getLinkedResource(resourceService, ref, "Location");

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// agent
				if (typedContainerResource.hasAgent()) {

					for (ProvenanceAgentComponent agent : typedContainerResource.getAgent()) {
						// agent.who
						if (agent.hasWho() && agent.getWho().hasReference()) {

							ref = agent.getWho().getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}

						// agent.onBehalfOf
						if (agent.hasOnBehalfOf() && agent.getOnBehalfOf().hasReference()) {

							ref = agent.getOnBehalfOf().getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// entity
				if (typedContainerResource.hasEntity()) {

					for (ProvenanceEntityComponent entity : typedContainerResource.getEntity()) {
						// entity.what
						if (entity.hasWhat() && entity.getWhat().hasReference()) {

							ref = entity.getWhat().getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}

						// entity.agent
						if (entity.hasAgent()) {

							for (ProvenanceAgentComponent agent : entity.getAgent()) {
								// entity.agent.who
								if (agent.hasWho() && agent.getWho().hasReference()) {

									ref = agent.getWho().getReference();
									linkedResource = this.getLinkedResourceAny(resourceService, ref);

									if (linkedResource != null) {
										linkedResources.add(linkedResource);
									}
								}

								// entity.agent.onBehalfOf
								if (agent.hasOnBehalfOf() && agent.getOnBehalfOf().hasReference()) {

									ref = agent.getOnBehalfOf().getReference();
									linkedResource = this.getLinkedResourceAny(resourceService, ref);

									if (linkedResource != null) {
										linkedResources.add(linkedResource);
									}
								}
							}
						}
					}
				}
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
        }

		return linkedResources;
	}

}
