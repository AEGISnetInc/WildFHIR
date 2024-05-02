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

import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Communication;
import org.hl7.fhir.r4.model.Communication.CommunicationPayloadComponent;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

import net.aegis.fhir.service.ResourceService;

/**
 * @author richard.ettema
 *
 */
public class LinkedResourceCommunication extends LinkedResourceProxy {

	private Logger log = Logger.getLogger("LinkedResourceCommunication");

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.linked.LinkedResourceProxy#getLinkedResources(net.aegis.fhir.service.ResourceService, org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public List<Resource> getLinkedResources(ResourceService resourceService, Resource containerResource) throws Exception {

		log.fine("[START] LinkedResourceCommunication.getLinkedResources()");

		List<Resource> linkedResources = new ArrayList<Resource>();

		try {
			// Test for valid container Resource
			if (containerResource != null && containerResource instanceof Communication) {

				Communication typedContainerResource = (Communication) containerResource;

				/*
				 * Communication linked Resource references
				 */
				String ref = null;
				Resource linkedResource = null;

				// instantiatesCanonical
				if (typedContainerResource.hasInstantiatesCanonical()) {

					for (CanonicalType instantiatesCanonical : typedContainerResource.getInstantiatesCanonical()) {
						if (instantiatesCanonical.hasValue()) {

							ref = instantiatesCanonical.getValue();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// basedOn
				if (typedContainerResource.hasBasedOn()) {

					for (Reference basedOn : typedContainerResource.getBasedOn()) {
						if (basedOn.hasReference()) {

							ref = basedOn.getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// partOf
				if (typedContainerResource.hasPartOf()) {

					for (Reference partOf : typedContainerResource.getPartOf()) {
						if (partOf.hasReference()) {

							ref = partOf.getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// inResponseTo (Communication)
				if (typedContainerResource.hasInResponseTo()) {

					for (Reference inResponseTo : typedContainerResource.getInResponseTo()) {
						if (inResponseTo.hasReference()) {

							ref = inResponseTo.getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "Communication");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
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
				// about
				if (typedContainerResource.hasAbout()) {

					for (Reference about : typedContainerResource.getAbout()) {
						if (about.hasReference()) {

							ref = about.getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
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
				// recipient
				if (typedContainerResource.hasRecipient()) {

					for (Reference recipient : typedContainerResource.getRecipient()) {
						if (recipient.hasReference()) {

							ref = recipient.getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// sender
				if (typedContainerResource.hasSender() && typedContainerResource.getSender().hasReference()) {

					ref = typedContainerResource.getSender().getReference();
					linkedResource = this.getLinkedResourceAny(resourceService, ref);

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// reasonReference
				if (typedContainerResource.hasReasonReference()) {

					for (Reference reasonReference : typedContainerResource.getReasonReference()) {
						if (reasonReference.hasReference()) {

							ref = reasonReference.getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// payload.contentReference
				if (typedContainerResource.hasPayload()) {

					for (CommunicationPayloadComponent payload : typedContainerResource.getPayload()) {
						if (payload.hasContentReference() && payload.getContentReference().hasReference()) {

							ref = payload.getContentReference().getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

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
