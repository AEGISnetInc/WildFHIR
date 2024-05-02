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
package net.aegis.fhir.service.summary;

import org.hl7.fhir.r4.model.MedicationKnowledge;
import org.hl7.fhir.r4.model.MedicationKnowledge.MedicationKnowledgeAdministrationGuidelinesComponent;
import org.hl7.fhir.r4.model.MedicationKnowledge.MedicationKnowledgeAdministrationGuidelinesPatientCharacteristicsComponent;
import org.hl7.fhir.r4.model.MedicationKnowledge.MedicationKnowledgeCostComponent;
import org.hl7.fhir.r4.model.MedicationKnowledge.MedicationKnowledgeIngredientComponent;
import org.hl7.fhir.r4.model.MedicationKnowledge.MedicationKnowledgeMedicineClassificationComponent;
import org.hl7.fhir.r4.model.MedicationKnowledge.MedicationKnowledgeRegulatoryComponent;
import org.hl7.fhir.r4.model.MedicationKnowledge.MedicationKnowledgeRegulatoryMaxDispenseComponent;
import org.hl7.fhir.r4.model.Resource;

/**
 * @author richard.ettema
 *
 */
public class ResourceSummaryMedicationKnowledge extends ResourceSummaryProxy {

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.summary.ResourceSummaryProxy#generateSummary(org.hl7.fhir.r4.model.Resource)
	 */
	@Override
	public Resource generateSummary(Resource resource) throws Exception {

		MedicationKnowledge summary = null;

		try {
			// Cast original resource to expected type
			MedicationKnowledge original = (MedicationKnowledge) resource;

			// Instantiate summary resource
			summary = new MedicationKnowledge();

//			// Copy original resource and remove text
//			summary = original.copy();
//			((Resource)original).copyValues(summary);
//
//			// Remove non-summary Resource elements
//			removeNonSummaryResourceElements(summary);
//
//			// Remove non-summary DomainResource elements
//			removeNonSummaryDomainResourceElements(summary);
//
//			// Remove Resource Type non-summary data elements

			// Copy summary Resource elements
			summary.setId(original.getId());
			summary.setMeta(original.getMeta());
			summary.setImplicitRules(original.getImplicitRules());

			// Copy summary DomainResource elements
			// None

			// Copy summary Resource Type elements
			summary.setCode(original.getCode());
			summary.setStatus(original.getStatus());
			summary.setManufacturer(original.getManufacturer());
			summary.setAmount(original.getAmount());
			summary.setSynonym(original.getSynonym());

			// Set Meta Security Tag SUBSETTED
			setMetaTagSubsetted(summary);
		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return summary;
	}

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.summary.ResourceSummaryProxy#generateDataSummary(org.hl7.fhir.r4.model.Resource)
	 */
	@Override
	public Resource generateDataSummary(Resource resource) throws Exception {

		MedicationKnowledge summary = null;

		try {
			// Cast original resource to expected type
			MedicationKnowledge original = (MedicationKnowledge) resource;

			// Copy original resource and remove text
			summary = original.copy();
			((Resource)original).copyValues(summary);

			// Remove text element
			summary.setText(null);

			// Set Meta Security Tag SUBSETTED
			setMetaTagSubsetted(summary);
		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return summary;
	}

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.summary.ResourceSummaryProxy#generateTextSummary(org.hl7.fhir.r4.model.Resource)
	 */
	@Override
	public Resource generateTextSummary(Resource resource) throws Exception {

		MedicationKnowledge summary = null;

		try {
			// Cast original resource to expected type
			MedicationKnowledge original = (MedicationKnowledge) resource;

			// Instantiate summary resource
			summary = new MedicationKnowledge();

			// Copy text-only DomainResource elements
			copyTextOnlyDomainResourceElements(original, summary);

			// Set Meta Security Tag SUBSETTED
			setMetaTagSubsetted(summary);

			// Copy mandatory Resource Type elements
			summary.setRelatedMedicationKnowledge(original.getRelatedMedicationKnowledge());
			MedicationKnowledgeIngredientComponent summaryIngredient = null;
			for (MedicationKnowledgeIngredientComponent ingredient : original.getIngredient()) {
				summaryIngredient = new MedicationKnowledgeIngredientComponent();
				summaryIngredient.setItem(ingredient.getItem());
				summary.addIngredient(summaryIngredient);
			}
			MedicationKnowledgeCostComponent summaryCost = null;
			for (MedicationKnowledgeCostComponent cost : original.getCost()) {
				summaryCost = new MedicationKnowledgeCostComponent();
				summaryCost.setType(cost.getType());
				summaryCost.setCost(cost.getCost());
				summary.addCost(summaryCost);
			}
			MedicationKnowledgeAdministrationGuidelinesComponent summaryAdministrationGuidelines = null;
			for (MedicationKnowledgeAdministrationGuidelinesComponent administrationGuidelines : original.getAdministrationGuidelines()) {
				summaryAdministrationGuidelines = new MedicationKnowledgeAdministrationGuidelinesComponent();
				summaryAdministrationGuidelines.setDosage(administrationGuidelines.getDosage());

				MedicationKnowledgeAdministrationGuidelinesPatientCharacteristicsComponent summaryPatientCharacteristics = null;
				for (MedicationKnowledgeAdministrationGuidelinesPatientCharacteristicsComponent patientCharacteristics : administrationGuidelines.getPatientCharacteristics()) {
					summaryPatientCharacteristics = new MedicationKnowledgeAdministrationGuidelinesPatientCharacteristicsComponent();
					summaryPatientCharacteristics.setCharacteristic(patientCharacteristics.getCharacteristic());
					administrationGuidelines.addPatientCharacteristics(summaryPatientCharacteristics);
				}
				summary.addAdministrationGuidelines(summaryAdministrationGuidelines);
			}
			MedicationKnowledgeMedicineClassificationComponent summaryMedicineClassification = null;
			for (MedicationKnowledgeMedicineClassificationComponent medicineClassification : original.getMedicineClassification()) {
				summaryMedicineClassification = new MedicationKnowledgeMedicineClassificationComponent();
				summaryMedicineClassification.setType(medicineClassification.getType());
				summary.addMedicineClassification(summaryMedicineClassification);
			}
			MedicationKnowledgeRegulatoryComponent summaryRegulatory = null;
			for (MedicationKnowledgeRegulatoryComponent regulatory : original.getRegulatory()) {
				summaryRegulatory = new MedicationKnowledgeRegulatoryComponent();
				summaryRegulatory.setRegulatoryAuthority(regulatory.getRegulatoryAuthority());
				summaryRegulatory.setSubstitution(regulatory.getSubstitution());
				summaryRegulatory.setSchedule(regulatory.getSchedule());
				if (regulatory.hasMaxDispense()) {
					MedicationKnowledgeRegulatoryMaxDispenseComponent summaryRegulatoryMaxDispense = new MedicationKnowledgeRegulatoryMaxDispenseComponent();
					summaryRegulatoryMaxDispense.setQuantity(regulatory.getMaxDispense().getQuantity());
					summaryRegulatory.setMaxDispense(summaryRegulatoryMaxDispense);
				}
				summary.addRegulatory(summaryRegulatory);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return summary;
	}

}
