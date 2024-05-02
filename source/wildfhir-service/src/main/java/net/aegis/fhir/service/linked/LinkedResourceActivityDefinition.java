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

import org.hl7.fhir.r4.model.ActivityDefinition;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

import net.aegis.fhir.service.ResourceService;

/**
 * @author richard.ettema
 *
 */
public class LinkedResourceActivityDefinition extends LinkedResourceProxy {

	private Logger log = Logger.getLogger("LinkedResourceActivityDefinition");

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.linked.LinkedResourceProxy#getLinkedResources(net.aegis.fhir.service.ResourceService, org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public List<Resource> getLinkedResources(ResourceService resourceService, Resource containerResource) throws Exception {

		log.fine("[START] LinkedResourceActivityDefinition.getLinkedResources()");

		List<Resource> linkedResources = new ArrayList<Resource>();

		try {
			// Test for valid container Resource
			if (containerResource != null && containerResource instanceof ActivityDefinition) {

				ActivityDefinition typedContainerResource = (ActivityDefinition) containerResource;

				/*
				 * ActivityDefinition linked Resource references
				 */
				String ref = null;
				Resource linkedResource = null;

				// subjectReference (Group)
				if (typedContainerResource.hasSubjectReference() && typedContainerResource.getSubjectReference().hasReference()) {

					ref = typedContainerResource.getSubjectReference().getReference();
					linkedResource = this.getLinkedResource(resourceService, ref, "Group");

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// library (Library)
				if (typedContainerResource.hasLibrary()) {

					for (CanonicalType library : typedContainerResource.getLibrary()) {
						if (library.hasValue()) {

							ref = library.getValue();
							linkedResource = this.getLinkedResource(resourceService, ref, "Library");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				// profile (StructureDefinition) - conformance resource

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
				// productReference
				if (typedContainerResource.hasProductReference() && typedContainerResource.getProductReference().hasReference()) {

					ref = typedContainerResource.getProductReference().getReference();
					linkedResource = this.getLinkedResourceAny(resourceService, ref);

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// specimenRequirement (SpecimenDefinition)
				if (typedContainerResource.hasSpecimenRequirement()) {

					for (Reference specimenRequirement : typedContainerResource.getSpecimenRequirement()) {
						if (specimenRequirement.hasReference()) {

							ref = specimenRequirement.getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "SpecimenDefinition");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// observationRequirement (ObservationDefinition)
				if (typedContainerResource.hasObservationRequirement()) {

					for (Reference observationRequirement : typedContainerResource.getObservationRequirement()) {
						if (observationRequirement.hasReference()) {

							ref = observationRequirement.getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "ObservationDefinition");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// observationResultRequirement (ObservationDefinition)
				if (typedContainerResource.hasObservationResultRequirement()) {

					for (Reference observationResultRequirement : typedContainerResource.getObservationResultRequirement()) {
						if (observationResultRequirement.hasReference()) {

							ref = observationResultRequirement.getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "ObservationDefinition");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				// transform (StructureMap) - conformance resource
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
        }

		return linkedResources;
	}

}
