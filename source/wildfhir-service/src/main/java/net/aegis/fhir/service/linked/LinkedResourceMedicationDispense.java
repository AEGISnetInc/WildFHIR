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

import org.hl7.fhir.r4.model.MedicationDispense;
import org.hl7.fhir.r4.model.MedicationDispense.MedicationDispensePerformerComponent;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

import net.aegis.fhir.service.ResourceService;

/**
 * @author richard.ettema
 *
 */
public class LinkedResourceMedicationDispense extends LinkedResourceProxy {

	private Logger log = Logger.getLogger("LinkedResourceMedicationDispense");

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.linked.LinkedResourceProxy#getLinkedResources(net.aegis.fhir.service.ResourceService, org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public List<Resource> getLinkedResources(ResourceService resourceService, Resource containerResource) throws Exception {

		log.fine("[START] LinkedResourceMedicationDispense.getLinkedResources()");

		List<Resource> linkedResources = new ArrayList<Resource>();

		try {
			// Test for valid container Resource
			if (containerResource != null && containerResource instanceof MedicationDispense) {

				MedicationDispense typedContainerResource = (MedicationDispense) containerResource;

				/*
				 * Account linked Resource references
				 */
				String ref = null;
				Resource linkedResource = null;

				// partOf (Procedure)
				if (typedContainerResource.hasPartOf()) {

					for (Reference partOf : typedContainerResource.getPartOf()) {
						if (partOf.hasReference()) {

							ref = partOf.getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "Procedure");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// statusReasonReference (DetectedIssue)
				if (typedContainerResource.hasStatusReasonReference() && typedContainerResource.getStatusReasonReference().hasReference()) {

					ref = typedContainerResource.getStatusReasonReference().getReference();
					linkedResource = this.getLinkedResource(resourceService, ref, "DetectedIssue");

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// medicationReference (Medication)
				if (typedContainerResource.hasMedicationReference() && typedContainerResource.getMedicationReference().hasReference()) {

					ref = typedContainerResource.getMedicationReference().getReference();
					linkedResource = this.getLinkedResource(resourceService, ref, "Medication");

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// subject
				if (typedContainerResource.hasSubject() && typedContainerResource.getSubject().hasReference()) {

					ref = typedContainerResource.getSubject().getReference();
					linkedResource = this.getLinkedResourceAny(resourceService, ref);

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// context
				if (typedContainerResource.hasContext() && typedContainerResource.getContext().hasReference()) {

					ref = typedContainerResource.getContext().getReference();
					linkedResource = this.getLinkedResourceAny(resourceService, ref);

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// supportingInformation
				if (typedContainerResource.hasSupportingInformation()) {

					for (Reference supportingInformation : typedContainerResource.getSupportingInformation()) {
						if (supportingInformation.hasReference()) {

							ref = supportingInformation.getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// performer
				if (typedContainerResource.hasPerformer()) {

					for (MedicationDispensePerformerComponent performerRef : typedContainerResource.getPerformer()) {
						// performer.actor
						if (performerRef.hasActor() && performerRef.getActor().hasReference()) {

							ref = performerRef.getActor().getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// authorizingPrescription (MedicationRequest)
				if (typedContainerResource.hasAuthorizingPrescription()) {

					for (Reference authorizingPrescription : typedContainerResource.getAuthorizingPrescription()) {
						if (authorizingPrescription.hasReference()) {

							ref = authorizingPrescription.getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "MedicationRequest");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// destination (Location)
				if (typedContainerResource.hasDestination() && typedContainerResource.getDestination().hasReference()) {

					ref = typedContainerResource.getDestination().getReference();
					linkedResource = this.getLinkedResource(resourceService, ref, "Location");

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// receiver
				if (typedContainerResource.hasReceiver()) {

					for (Reference receiver : typedContainerResource.getReceiver()) {
						if (receiver.hasReference()) {

							ref = receiver.getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// substitution.responsibleParty (Practitioner)
				if (typedContainerResource.hasSubstitution() && typedContainerResource.getSubstitution().hasResponsibleParty()) {

					for (Reference responsibleParty : typedContainerResource.getSubstitution().getResponsibleParty()) {
						if (responsibleParty.hasReference()) {

							ref = responsibleParty.getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "Practitioner");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// detectedIssue (DetectedIssue)
				if (typedContainerResource.hasDetectedIssue()) {

					for (Reference detectedIssue : typedContainerResource.getDetectedIssue()) {
						if (detectedIssue.hasReference()) {

							ref = detectedIssue.getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "DetectedIssue");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// eventHistory (Provenance)
				if (typedContainerResource.hasEventHistory()) {

					for (Reference eventHistory : typedContainerResource.getEventHistory()) {
						if (eventHistory.hasReference()) {

							ref = eventHistory.getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "Provenance");

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
