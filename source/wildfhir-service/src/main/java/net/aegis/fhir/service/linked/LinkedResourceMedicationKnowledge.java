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

import org.hl7.fhir.r4.model.MedicationKnowledge;
import org.hl7.fhir.r4.model.MedicationKnowledge.MedicationKnowledgeAdministrationGuidelinesComponent;
import org.hl7.fhir.r4.model.MedicationKnowledge.MedicationKnowledgeIngredientComponent;
import org.hl7.fhir.r4.model.MedicationKnowledge.MedicationKnowledgeMonographComponent;
import org.hl7.fhir.r4.model.MedicationKnowledge.MedicationKnowledgeRegulatoryComponent;
import org.hl7.fhir.r4.model.MedicationKnowledge.MedicationKnowledgeRelatedMedicationKnowledgeComponent;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

import net.aegis.fhir.service.ResourceService;

/**
 * @author richard.ettema
 *
 */
public class LinkedResourceMedicationKnowledge extends LinkedResourceProxy {

	private Logger log = Logger.getLogger("LinkedResourceMedicationKnowledge");

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.linked.LinkedResourceProxy#getLinkedResources(net.aegis.fhir.service.ResourceService, org.hl7.fhir.r4.model.Resource)
	 */
	@Override
	public List<Resource> getLinkedResources(ResourceService resourceService, Resource containerResource) throws Exception {

		log.fine("[START] LinkedResourceMedicationKnowledge.getLinkedResources()");

		List<Resource> linkedResources = new ArrayList<Resource>();

		try {
			// Test for valid container Resource
			if (containerResource != null && containerResource instanceof MedicationKnowledge) {

				MedicationKnowledge typedContainerResource = (MedicationKnowledge) containerResource;

				/*
				 * MedicationKnowledge linked Resource references
				 */
				String ref = null;
				Resource linkedResource = null;

				// manufacturer (Organization)
				if (typedContainerResource.hasManufacturer() && typedContainerResource.getManufacturer().hasReference()) {

					ref = typedContainerResource.getManufacturer().getReference();
					linkedResource = this.getLinkedResource(resourceService, ref, "Organization");

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// relatedMedication.reference (MedicationKnowledge)
				if (typedContainerResource.hasRelatedMedicationKnowledge()) {

					for (MedicationKnowledgeRelatedMedicationKnowledgeComponent relatedMedication : typedContainerResource.getRelatedMedicationKnowledge()) {
						if (relatedMedication.hasReference()) {

							for (Reference relatedMedRef : relatedMedication.getReference()) {
								if (relatedMedRef.hasReference()) {

									ref = relatedMedRef.getReference();
									linkedResource = this.getLinkedResource(resourceService, ref, "MedicationKnowledge");

									if (linkedResource != null) {
										linkedResources.add(linkedResource);
									}
								}
							}
						}
					}
				}

				linkedResource = null;
				// associatedMedication (Medication)
				if (typedContainerResource.hasAssociatedMedication()) {

					for (Reference medicationRef : typedContainerResource.getAssociatedMedication()) {
						linkedResource = null;

						if (medicationRef.hasReference()) {
							ref = medicationRef.getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "Medication");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// monograph.source
				if (typedContainerResource.hasMonograph()) {

					for (MedicationKnowledgeMonographComponent monograph : typedContainerResource.getMonograph()) {
						if (monograph.hasSource() && monograph.getSource().hasReference()) {

							ref = monograph.getSource().getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// ingredient.itemReference (Substance)
				if (typedContainerResource.hasIngredient()) {

					for (MedicationKnowledgeIngredientComponent ingredient : typedContainerResource.getIngredient()) {
						if (ingredient.hasItemReference() && ingredient.getItemReference().hasReference()) {

							ref = ingredient.getItemReference().getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "Substance");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// administrationGuidelines.indicationReference (ObservationDefinition)
				if (typedContainerResource.hasAdministrationGuidelines()) {

					for (MedicationKnowledgeAdministrationGuidelinesComponent administrationGuidelines : typedContainerResource.getAdministrationGuidelines()) {
						if (administrationGuidelines.hasIndicationReference() && administrationGuidelines.getIndicationReference().hasReference()) {

							ref = administrationGuidelines.getIndicationReference().getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "ObservationDefinition");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// contraindication (DetectedIssue)
				if (typedContainerResource.hasContraindication()) {

					for (Reference detectedIssue : typedContainerResource.getContraindication()) {
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
				// regulatory.regulatoryAuthority (Organization)
				if (typedContainerResource.hasRegulatory()) {

					for (MedicationKnowledgeRegulatoryComponent regulatory : typedContainerResource.getRegulatory()) {
						if (regulatory.hasRegulatoryAuthority() && regulatory.getRegulatoryAuthority().hasReference()) {

							ref = regulatory.getRegulatoryAuthority().getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "Organization");

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
