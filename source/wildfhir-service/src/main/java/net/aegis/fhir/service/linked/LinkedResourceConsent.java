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

import org.hl7.fhir.r4.model.Consent;
import org.hl7.fhir.r4.model.Consent.ConsentVerificationComponent;
import org.hl7.fhir.r4.model.Consent.provisionActorComponent;
import org.hl7.fhir.r4.model.Consent.provisionComponent;
import org.hl7.fhir.r4.model.Consent.provisionDataComponent;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

import net.aegis.fhir.service.ResourceService;

/**
 * @author richard.ettema
 *
 */
public class LinkedResourceConsent extends LinkedResourceProxy {

	private Logger log = Logger.getLogger("LinkedResourceConsent");

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.linked.LinkedResourceProxy#getLinkedResources(net.aegis.fhir.service.ResourceService, org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public List<Resource> getLinkedResources(ResourceService resourceService, Resource containerResource) throws Exception {

		log.fine("[START] LinkedResourceConsent.getLinkedResources()");

		List<Resource> linkedResources = new ArrayList<Resource>();

		try {
			// Test for valid container Resource
			if (containerResource != null && containerResource instanceof Consent) {

				Consent typedContainerResource = (Consent) containerResource;

				/*
				 * Consent linked Resource references
				 */
				String ref = null;
				Resource linkedResource = null;

				// patient (Patient)
				if (typedContainerResource.hasPatient() && typedContainerResource.getPatient().hasReference()) {

					ref = typedContainerResource.getPatient().getReference();
					linkedResource = this.getLinkedResource(resourceService, ref, "Patient");

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// performer
				if (typedContainerResource.hasPerformer()) {

					for (Reference performer : typedContainerResource.getPerformer()) {
						if (performer.hasReference()) {

							ref = performer.getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// organization (Organization)
				if (typedContainerResource.hasOrganization()) {

					for (Reference organization : typedContainerResource.getOrganization()) {
						if (organization.hasReference()) {

							ref = organization.getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "Organization");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// sourceReference
				if (typedContainerResource.hasPatient() && typedContainerResource.getPatient().hasReference()) {

					ref = typedContainerResource.getPatient().getReference();
					linkedResource = this.getLinkedResourceAny(resourceService, ref);

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// verification.verifiedWith
				if (typedContainerResource.hasVerification()) {

					for (ConsentVerificationComponent verification : typedContainerResource.getVerification()) {
						if (verification.hasVerifiedWith() && verification.getVerifiedWith().hasReference()) {

							ref = verification.getVerifiedWith().getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// provision
				if (typedContainerResource.hasProvision()) {

					this.getProvision(typedContainerResource.getProvision(), resourceService, linkedResources);
				}
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
        }

		return linkedResources;
	}

	/**
	 * @param provision
	 * @param resourceService
	 * @param linkedResources
	 * @throws Exception
	 */
	private void getProvision(provisionComponent provision, ResourceService resourceService, List<Resource> linkedResources) throws Exception {

		String ref = null;
		Resource linkedResource = null;

		// provision.actor.reference
		if (provision.hasActor()) {

			for (provisionActorComponent actor : provision.getActor()) {
				if (actor.hasReference() && actor.getReference().hasReference()) {

					ref = actor.getReference().getReference();
					linkedResource = this.getLinkedResourceAny(resourceService, ref);

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}
			}
		}

		// provision.data.reference
		if (provision.hasData()) {

			for (provisionDataComponent data : provision.getData()) {
				if (data.hasReference() && data.getReference().hasReference()) {

					ref = data.getReference().getReference();
					linkedResource = this.getLinkedResourceAny(resourceService, ref);

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}
			}
		}

		// provision.provision
		if (provision.hasProvision()) {

			for (provisionComponent provisionProvision : provision.getProvision()) {
				this.getProvision(provisionProvision, resourceService, linkedResources);
			}
		}
	}

}
