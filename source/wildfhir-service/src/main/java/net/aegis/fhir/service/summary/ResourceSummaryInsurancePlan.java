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

import org.hl7.fhir.r4.model.InsurancePlan;
import org.hl7.fhir.r4.model.InsurancePlan.CoverageBenefitComponent;
import org.hl7.fhir.r4.model.InsurancePlan.InsurancePlanCoverageComponent;
import org.hl7.fhir.r4.model.InsurancePlan.InsurancePlanPlanComponent;
import org.hl7.fhir.r4.model.InsurancePlan.InsurancePlanPlanSpecificCostComponent;
import org.hl7.fhir.r4.model.InsurancePlan.PlanBenefitComponent;
import org.hl7.fhir.r4.model.InsurancePlan.PlanBenefitCostComponent;
import org.hl7.fhir.r4.model.Resource;

/**
 * @author richard.ettema
 *
 */
public class ResourceSummaryInsurancePlan extends ResourceSummaryProxy {

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.summary.ResourceSummaryProxy#generateSummary(org.hl7.fhir.r4.model.Resource)
	 */
	@Override
	public Resource generateSummary(Resource resource) throws Exception {
		InsurancePlan summary = null;

		try {
			// Cast original resource to expected type
			InsurancePlan original = (InsurancePlan) resource;

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
			summary.setAlias(null);
			summary.setPeriod(null);
			summary.setContact(null);
			summary.setEndpoint(null);
			summary.setNetwork(null);
			summary.setCoverage(null);
			for (InsurancePlanPlanComponent plan : summary.getPlan()) {
				plan.setType(null);
				plan.setNetwork(null);
				plan.setGeneralCost(null);
				plan.setSpecificCost(null);
			}
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

		InsurancePlan summary = null;

		try {
			// Cast original resource to expected type
			InsurancePlan original = (InsurancePlan) resource;

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

		InsurancePlan summary = null;

		try {
			// Cast original resource to expected type
			InsurancePlan original = (InsurancePlan) resource;

			// Instantiate summary resource
			summary = new InsurancePlan();

			// Copy text-only DomainResource elements
			copyTextOnlyDomainResourceElements(original, summary);

			// Set Meta Security Tag SUBSETTED
			setMetaTagSubsetted(summary);

			// Copy mandatory Resource Type elements
			InsurancePlanCoverageComponent summaryCoverage = null;
			for (InsurancePlanCoverageComponent coverage : original.getCoverage()) {
				summaryCoverage = new InsurancePlanCoverageComponent();
				summaryCoverage.setType(coverage.getType());
				CoverageBenefitComponent summaryBenefit = null;
				for (CoverageBenefitComponent benefit : coverage.getBenefit()) {
					summaryBenefit = new CoverageBenefitComponent();
					summaryBenefit.setType(benefit.getType());
					summaryCoverage.addBenefit(summaryBenefit);
				}
				summary.addCoverage(summaryCoverage);
			}

			InsurancePlanPlanComponent summaryPlan = null;
			for (InsurancePlanPlanComponent plan : original.getPlan()) {
				if (plan.hasSpecificCost()) {
					summaryPlan = new InsurancePlanPlanComponent();

					InsurancePlanPlanSpecificCostComponent summarySpecificCost = null;
					for (InsurancePlanPlanSpecificCostComponent specificCost : plan.getSpecificCost()) {
						summarySpecificCost = new InsurancePlanPlanSpecificCostComponent();
						summarySpecificCost.setCategory(specificCost.getCategory());

						PlanBenefitComponent summarySpecificCostBenefit = null;
						for (PlanBenefitComponent benefit : specificCost.getBenefit()) {
							summarySpecificCostBenefit = new PlanBenefitComponent();
							summarySpecificCostBenefit.setType(benefit.getType());

							PlanBenefitCostComponent summarySpecificCostBenefitCost = null;
							for (PlanBenefitCostComponent cost : benefit.getCost()) {
								summarySpecificCostBenefitCost = new PlanBenefitCostComponent();
								summarySpecificCostBenefitCost.setType(cost.getType());
								summarySpecificCostBenefit.addCost(summarySpecificCostBenefitCost);
							}
							summarySpecificCost.addBenefit(summarySpecificCostBenefit);
						}
						summaryPlan.addSpecificCost(summarySpecificCost);
					}

					summary.addPlan(summaryPlan);
				}
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return summary;
	}

}
