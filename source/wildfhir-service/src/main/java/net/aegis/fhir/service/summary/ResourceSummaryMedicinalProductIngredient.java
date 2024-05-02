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

import org.hl7.fhir.r4.model.MedicinalProductIngredient;
import org.hl7.fhir.r4.model.MedicinalProductIngredient.MedicinalProductIngredientSpecifiedSubstanceComponent;
import org.hl7.fhir.r4.model.MedicinalProductIngredient.MedicinalProductIngredientSpecifiedSubstanceStrengthComponent;
import org.hl7.fhir.r4.model.MedicinalProductIngredient.MedicinalProductIngredientSpecifiedSubstanceStrengthReferenceStrengthComponent;
import org.hl7.fhir.r4.model.MedicinalProductIngredient.MedicinalProductIngredientSubstanceComponent;
import org.hl7.fhir.r4.model.Resource;

/**
 * @author richard.ettema
 *
 */
public class ResourceSummaryMedicinalProductIngredient extends ResourceSummaryProxy {

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.summary.ResourceSummaryProxy#generateSummary(org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public Resource generateSummary(Resource resource) throws Exception {

		MedicinalProductIngredient summary = null;

		try {
			// Cast original resource to expected type
			MedicinalProductIngredient original = (MedicinalProductIngredient) resource;

			// Copy original resource and remove text
			summary = original.copy();
			((Resource)original).copyValues(summary);

			// Remove non-summary Resource elements
			removeNonSummaryResourceElements(summary);

			// Remove non-summary DomainResource elements
			removeNonSummaryDomainResourceElements(summary);

			// Set Meta Security Tag SUBSETTED
			setMetaTagSubsetted(summary);

			// Remove Resource Type non-summary data elements
			// None
		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return summary;
	}

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.summary.ResourceSummaryProxy#generateDataSummary(org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public Resource generateDataSummary(Resource resource) throws Exception {

		MedicinalProductIngredient summary = null;

		try {
			// Cast original resource to expected type
			MedicinalProductIngredient original = (MedicinalProductIngredient) resource;

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
	 * @see net.aegis.fhir.service.summary.ResourceSummaryProxy#generateTextSummary(org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public Resource generateTextSummary(Resource resource) throws Exception {

		MedicinalProductIngredient summary = null;

		try {
			// Cast original resource to expected type
			MedicinalProductIngredient original = (MedicinalProductIngredient) resource;

			// Instantiate summary resource
			summary = new MedicinalProductIngredient();

			// Copy text-only DomainResource elements
			copyTextOnlyDomainResourceElements(original, summary);

			// Set Meta Security Tag SUBSETTED
			setMetaTagSubsetted(summary);

			// Copy mandatory Resource Type elements
			summary.setRole(original.getRole());

			MedicinalProductIngredientSpecifiedSubstanceComponent summarySpecifiedSubstance = null;
			for (MedicinalProductIngredientSpecifiedSubstanceComponent specifiedSubstance : original.getSpecifiedSubstance()) {
				summarySpecifiedSubstance = new MedicinalProductIngredientSpecifiedSubstanceComponent();
				summarySpecifiedSubstance.setCode(specifiedSubstance.getCode());
				summarySpecifiedSubstance.setGroup(specifiedSubstance.getGroup());

				MedicinalProductIngredientSpecifiedSubstanceStrengthComponent summaryStrength = null;
				for (MedicinalProductIngredientSpecifiedSubstanceStrengthComponent strength : specifiedSubstance.getStrength()) {
					summaryStrength = new MedicinalProductIngredientSpecifiedSubstanceStrengthComponent();
					summaryStrength.setPresentation(strength.getPresentation());

					MedicinalProductIngredientSpecifiedSubstanceStrengthReferenceStrengthComponent summarySpecifiedSubstanceStrengthReferenceStrength = null;
					for (MedicinalProductIngredientSpecifiedSubstanceStrengthReferenceStrengthComponent referenceStrength : strength.getReferenceStrength()) {
						summarySpecifiedSubstanceStrengthReferenceStrength = new MedicinalProductIngredientSpecifiedSubstanceStrengthReferenceStrengthComponent();
						summarySpecifiedSubstanceStrengthReferenceStrength.setStrength(referenceStrength.getStrength());
						summaryStrength.addReferenceStrength(summarySpecifiedSubstanceStrengthReferenceStrength);
					}
					summarySpecifiedSubstance.addStrength(summaryStrength);
				}
				summary.addSpecifiedSubstance(summarySpecifiedSubstance);
			}
			if (original.hasSubstance()) {
				MedicinalProductIngredientSubstanceComponent summarySubstance = new MedicinalProductIngredientSubstanceComponent();
				MedicinalProductIngredientSpecifiedSubstanceStrengthComponent summarySubstanceStrength = null;
				for (MedicinalProductIngredientSpecifiedSubstanceStrengthComponent substanceStrength : original.getSubstance().getStrength()) {
					summarySubstanceStrength = new MedicinalProductIngredientSpecifiedSubstanceStrengthComponent();
					summarySubstanceStrength.setPresentation(substanceStrength.getPresentation());

					MedicinalProductIngredientSpecifiedSubstanceStrengthReferenceStrengthComponent summarySubstanceStrengthReferenceStrength = null;
					for (MedicinalProductIngredientSpecifiedSubstanceStrengthReferenceStrengthComponent referenceStrength : substanceStrength.getReferenceStrength()) {
						summarySubstanceStrengthReferenceStrength = new MedicinalProductIngredientSpecifiedSubstanceStrengthReferenceStrengthComponent();
						summarySubstanceStrengthReferenceStrength.setStrength(referenceStrength.getStrength());
						summarySubstanceStrength.addReferenceStrength(summarySubstanceStrengthReferenceStrength);
					}
					summarySubstance.addStrength(summarySubstanceStrength);
				}
				summary.setSubstance(summarySubstance);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return summary;
	}

}
