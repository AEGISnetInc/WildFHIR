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
import org.hl7.fhir.r4.model.DeviceRequest;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

import net.aegis.fhir.service.ResourceService;

/**
 * @author richard.ettema
 *
 */
public class LinkedResourceDeviceRequest extends LinkedResourceProxy {

	private Logger log = Logger.getLogger("LinkedResourceDeviceRequest");

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.linked.LinkedResourceProxy#getLinkedResources(net.aegis.fhir.service.ResourceService, org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public List<Resource> getLinkedResources(ResourceService resourceService, Resource containerResource) throws Exception {

		log.fine("[START] LinkedResourceDeviceRequest.getLinkedResources()");

		List<Resource> linkedResources = new ArrayList<Resource>();

		try {
			// Test for valid container Resource
			if (containerResource != null && containerResource instanceof DeviceRequest) {

				DeviceRequest typedContainerResource = (DeviceRequest) containerResource;

				/*
				 * DeviceRequest linked Resource references
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
				// priorRequest
				if (typedContainerResource.hasPriorRequest()) {

					for (Reference priorRequest : typedContainerResource.getPriorRequest()) {
						if (priorRequest.hasReference()) {

							ref = priorRequest.getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// codeReference (Device)
				if (typedContainerResource.hasCodeReference() && typedContainerResource.getCodeReference().hasReference()) {

					ref = typedContainerResource.getCodeReference().getReference();
					linkedResource = this.getLinkedResource(resourceService, ref, "Device");

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
				// encounter (Encounter)
				if (typedContainerResource.hasEncounter() && typedContainerResource.getEncounter().hasReference()) {

					ref = typedContainerResource.getEncounter().getReference();
					linkedResource = this.getLinkedResource(resourceService, ref, "Encounter");

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// requester
				if (typedContainerResource.hasRequester() && typedContainerResource.getRequester().hasReference()) {

					ref = typedContainerResource.getRequester().getReference();
					linkedResource = this.getLinkedResourceAny(resourceService, ref);

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// performer
				if (typedContainerResource.hasPerformer() && typedContainerResource.getPerformer().hasReference()) {

					ref = typedContainerResource.getPerformer().getReference();
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
				// insurance
				if (typedContainerResource.hasInsurance()) {

					for (Reference insurance : typedContainerResource.getInsurance()) {
						if (insurance.hasReference()) {

							ref = insurance.getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// supportingInfo
				if (typedContainerResource.hasSupportingInfo()) {

					for (Reference supportingInfo : typedContainerResource.getSupportingInfo()) {
						if (supportingInfo.hasReference()) {

							ref = supportingInfo.getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// relevantHistory (Provenance)
				if (typedContainerResource.hasRelevantHistory()) {

					for (Reference relevantHistory : typedContainerResource.getRelevantHistory()) {
						if (relevantHistory.hasReference()) {

							ref = relevantHistory.getReference();
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
