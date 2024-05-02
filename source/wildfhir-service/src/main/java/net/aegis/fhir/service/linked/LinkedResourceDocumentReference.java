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

import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.DocumentReference.DocumentReferenceRelatesToComponent;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

import net.aegis.fhir.service.ResourceService;

/**
 * @author richard.ettema
 *
 */
public class LinkedResourceDocumentReference extends LinkedResourceProxy {

	private Logger log = Logger.getLogger("LinkedResourceDocumentReference");

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.linked.LinkedResourceProxy#getLinkedResources(net.aegis.fhir.service.ResourceService, org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public List<Resource> getLinkedResources(ResourceService resourceService, Resource containerResource) throws Exception {

		log.fine("[START] LinkedResourceDocumentReference.getLinkedResources()");

		List<Resource> linkedResources = new ArrayList<Resource>();

		try {
			// Test for valid container Resource
			if (containerResource != null && containerResource instanceof DocumentReference) {

				DocumentReference typedContainerResource = (DocumentReference) containerResource;

				/*
				 * Account linked Resource references
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
				// author
				if (typedContainerResource.hasAuthor()) {

					for (Reference author : typedContainerResource.getAuthor()) {
						if (author.hasReference()) {

							ref = author.getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// authenticator
				if (typedContainerResource.hasAuthenticator() && typedContainerResource.getAuthenticator().hasReference()) {

					ref = typedContainerResource.getAuthenticator().getReference();
					linkedResource = this.getLinkedResourceAny(resourceService, ref);

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// custodian (Organization)
				if (typedContainerResource.hasCustodian() && typedContainerResource.getCustodian().hasReference()) {

					ref = typedContainerResource.getCustodian().getReference();
					linkedResource = this.getLinkedResource(resourceService, ref, "Organization");

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// relatesTo.target (DocumentReference)
				if (typedContainerResource.hasRelatesTo()) {

					for (DocumentReferenceRelatesToComponent relatesTo : typedContainerResource.getRelatesTo()) {
						if (relatesTo.hasTarget() && relatesTo.getTarget().hasReference()) {

							ref = relatesTo.getTarget().getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "DocumentReference");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// context
				if (typedContainerResource.hasContext()) {

					// context.encounter (Encounter)
					if (typedContainerResource.getContext().hasEncounter()) {

						for (Reference encounter : typedContainerResource.getContext().getEncounter()) {
							if (encounter.hasReference()) {

								ref = encounter.getReference();
								linkedResource = this.getLinkedResource(resourceService, ref, "Encounter");

								if (linkedResource != null) {
									linkedResources.add(linkedResource);
								}
							}
						}
					}

					// context.sourcePatientInfo (Patient)
					if (typedContainerResource.getContext().hasSourcePatientInfo() && typedContainerResource.getContext().getSourcePatientInfo().hasReference()) {

						ref = typedContainerResource.getContext().getSourcePatientInfo().getReference();
						linkedResource = this.getLinkedResource(resourceService, ref, "Patient");

						if (linkedResource != null) {
							linkedResources.add(linkedResource);
						}
					}

					// context.related
					if (typedContainerResource.getContext().hasRelated()) {

						for (Reference related : typedContainerResource.getContext().getRelated()) {
							if (related.hasReference()) {

								ref = related.getReference();
								linkedResource = this.getLinkedResourceAny(resourceService, ref);

								if (linkedResource != null) {
									linkedResources.add(linkedResource);
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
