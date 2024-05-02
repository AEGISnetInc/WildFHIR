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

import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

import net.aegis.fhir.service.ResourceService;

/**
 * @author richard.ettema
 *
 */
public class LinkedResourceResearchStudy extends LinkedResourceProxy {

	private Logger log = Logger.getLogger("LinkedResourceResearchStudy");

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.linked.LinkedResourceProxy#getLinkedResources(net.aegis.fhir.service.ResourceService, org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public List<Resource> getLinkedResources(ResourceService resourceService, Resource containerResource) throws Exception {

		log.fine("[START] LinkedResourceResearchStudy.getLinkedResources()");

		List<Resource> linkedResources = new ArrayList<Resource>();

		try {
			// Test for valid container Resource
			if (containerResource != null && containerResource instanceof ResearchStudy) {

				ResearchStudy typedContainerResource = (ResearchStudy) containerResource;

				/*
				 * Account linked Resource references
				 */
				String ref = null;
				Resource linkedResource = null;

				// protocol (PlanDefinition)
				if (typedContainerResource.hasProtocol()) {

					for (Reference protocol : typedContainerResource.getProtocol()) {
						if (protocol.hasReference()) {

							ref = protocol.getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "PlanDefinition");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// partOf (ResearchStudy)
				if (typedContainerResource.hasPartOf()) {

					for (Reference partOf : typedContainerResource.getPartOf()) {
						if (partOf.hasReference()) {

							ref = partOf.getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "ResearchStudy");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// enrollment (Group)
				if (typedContainerResource.hasEnrollment()) {

					for (Reference enrollment : typedContainerResource.getEnrollment()) {
						if (enrollment.hasReference()) {

							ref = enrollment.getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "Group");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// sponsor (Organization)
				if (typedContainerResource.hasSponsor() && typedContainerResource.getSponsor().hasReference()) {

					ref = typedContainerResource.getSponsor().getReference();
					linkedResource = this.getLinkedResource(resourceService, ref, "Organization");

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// principalInvestigator
				if (typedContainerResource.hasPrincipalInvestigator() && typedContainerResource.getPrincipalInvestigator().hasReference()) {

					ref = typedContainerResource.getPrincipalInvestigator().getReference();
					linkedResource = this.getLinkedResourceAny(resourceService, ref);

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// site (Location)
				if (typedContainerResource.hasSite()) {

					for (Reference site : typedContainerResource.getSite()) {
						if (site.hasReference()) {

							ref = site.getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "Location");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
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
