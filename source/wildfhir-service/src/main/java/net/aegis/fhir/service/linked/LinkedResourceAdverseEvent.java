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

import org.hl7.fhir.r4.model.AdverseEvent;
import org.hl7.fhir.r4.model.AdverseEvent.AdverseEventSuspectEntityCausalityComponent;
import org.hl7.fhir.r4.model.AdverseEvent.AdverseEventSuspectEntityComponent;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

import net.aegis.fhir.service.ResourceService;

/**
 * @author richard.ettema
 *
 */
public class LinkedResourceAdverseEvent extends LinkedResourceProxy {

	private Logger log = Logger.getLogger("LinkedResourceAdverseEvent");

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.linked.LinkedResourceProxy#getLinkedResources(net.aegis.fhir.service.ResourceService, org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public List<Resource> getLinkedResources(ResourceService resourceService, Resource containerResource) throws Exception {

		log.fine("[START] LinkedResourceAdverseEvent.getLinkedResources()");

		List<Resource> linkedResources = new ArrayList<Resource>();

		try {
			// Test for valid container Resource
			if (containerResource != null && containerResource instanceof AdverseEvent) {

				AdverseEvent typedContainerResource = (AdverseEvent) containerResource;

				/*
				 * AdverseEvent linked Resource references
				 */
				String ref = null;
				Resource linkedResource = null;

				// subject
				if (typedContainerResource.hasSubject() && typedContainerResource.getSubject().hasReference()) {

					ref = typedContainerResource.getSubject().getReference();
					linkedResource = this.getLinkedResourceAny(resourceService, ref);

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// encounter (Encounter)
				if (typedContainerResource.hasEncounter() && typedContainerResource.getEncounter().hasReference()) {

					ref = typedContainerResource.getEncounter().getReference();
					linkedResource = this.getLinkedResource(resourceService, ref, "Encounter");

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// resultingCondition (Condition)
				if (typedContainerResource.hasResultingCondition()) {

					for (Reference resultingCondition : typedContainerResource.getResultingCondition()) {
						if (resultingCondition.hasReference()) {

							ref = resultingCondition.getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "Condition");

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
				// recorder
				if (typedContainerResource.hasRecorder() && typedContainerResource.getRecorder().hasReference()) {

					ref = typedContainerResource.getRecorder().getReference();
					linkedResource = this.getLinkedResourceAny(resourceService, ref);

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// contributor
				if (typedContainerResource.hasContributor()) {

					for (Reference contributor : typedContainerResource.getContributor()) {
						if (contributor.hasReference()) {

							ref = contributor.getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// suspectEntity
				if (typedContainerResource.hasSuspectEntity()) {

					for (AdverseEventSuspectEntityComponent suspectEntity : typedContainerResource.getSuspectEntity()) {

						// suspectEntity.instance
						if (suspectEntity.hasInstance() && suspectEntity.getInstance().hasReference()) {

							ref = suspectEntity.getInstance().getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}

						// suspectEntity.causality.author
						if (suspectEntity.hasCausality()) {

							for (AdverseEventSuspectEntityCausalityComponent causality : suspectEntity.getCausality()) {
								if (causality.hasAuthor() && causality.getAuthor().hasReference()) {

									ref = causality.getAuthor().getReference();
									linkedResource = this.getLinkedResourceAny(resourceService, ref);

									if (linkedResource != null) {
										linkedResources.add(linkedResource);
									}
								}
							}
						}
					}
				}

				linkedResource = null;
				// subjectMedicalHistory
				if (typedContainerResource.hasSubjectMedicalHistory()) {

					for (Reference subjectMedicalHistory : typedContainerResource.getSubjectMedicalHistory()) {
						if (subjectMedicalHistory.hasReference()) {

							ref = subjectMedicalHistory.getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// referenceDocument (DocumentReference)
				if (typedContainerResource.hasReferenceDocument()) {

					for (Reference referenceDocument : typedContainerResource.getReferenceDocument()) {
						if (referenceDocument.hasReference()) {

							ref = referenceDocument.getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "DocumentReference");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// study (ResearchStudy)
				if (typedContainerResource.hasStudy()) {

					for (Reference study : typedContainerResource.getStudy()) {
						if (study.hasReference()) {

							ref = study.getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "ResearchStudy");

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
