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

import org.hl7.fhir.r4.model.MolecularSequence;
import org.hl7.fhir.r4.model.MolecularSequence.MolecularSequenceVariantComponent;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

import net.aegis.fhir.service.ResourceService;

/**
 * @author richard.ettema
 *
 */
public class LinkedResourceMolecularSequence extends LinkedResourceProxy {

	private Logger log = Logger.getLogger("LinkedResourceMolecularSequence");

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.linked.LinkedResourceProxy#getLinkedResources(net.aegis.fhir.service.ResourceService, org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public List<Resource> getLinkedResources(ResourceService resourceService, Resource containerResource) throws Exception {

		log.fine("[START] LinkedResourceMolecularSequence.getLinkedResources()");

		List<Resource> linkedResources = new ArrayList<Resource>();

		try {
			// Test for valid container Resource
			if (containerResource != null && containerResource instanceof MolecularSequence) {

				MolecularSequence typedContainerResource = (MolecularSequence) containerResource;

				/*
				 * MolecularSequence linked Resource references
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
				// specimen (Specimen)
				if (typedContainerResource.hasSpecimen() && typedContainerResource.getSpecimen().hasReference()) {

					ref = typedContainerResource.getSpecimen().getReference();
					linkedResource = this.getLinkedResource(resourceService, ref, "Specimen");

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// device (Device)
				if (typedContainerResource.hasDevice() && typedContainerResource.getDevice().hasReference()) {

					ref = typedContainerResource.getDevice().getReference();
					linkedResource = this.getLinkedResource(resourceService, ref, "Specimen");

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// performer (Organization)
				if (typedContainerResource.hasPerformer() && typedContainerResource.getPerformer().hasReference()) {

					ref = typedContainerResource.getPerformer().getReference();
					linkedResource = this.getLinkedResource(resourceService, ref, "Organization");

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// referenceSeq.referenceSeqPointer (MolecularSequence)
				if (typedContainerResource.hasReferenceSeq() && typedContainerResource.getReferenceSeq().hasReferenceSeqPointer() && typedContainerResource.getReferenceSeq().getReferenceSeqPointer().hasReference()) {

					ref = typedContainerResource.getReferenceSeq().getReferenceSeqPointer().getReference();
					linkedResource = this.getLinkedResource(resourceService, ref, "MolecularSequence");

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// variant.variantPointer (Observation)
				if (typedContainerResource.hasVariant()) {

					for (MolecularSequenceVariantComponent variant : typedContainerResource.getVariant()) {
						if (variant.hasVariantPointer() && variant.getVariantPointer().hasReference()) {

							ref = variant.getVariantPointer().getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "Observation");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// pointer (MolecularSequence)
				if (typedContainerResource.hasPointer()) {

					for (Reference pointer : typedContainerResource.getPointer()) {
						if (pointer.hasReference()) {

							ref = pointer.getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "MolecularSequence");

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
