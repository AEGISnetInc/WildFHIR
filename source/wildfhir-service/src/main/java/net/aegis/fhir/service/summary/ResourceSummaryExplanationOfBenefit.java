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

import org.hl7.fhir.r4.model.ExplanationOfBenefit;
import org.hl7.fhir.r4.model.ExplanationOfBenefit.AddedItemComponent;
import org.hl7.fhir.r4.model.ExplanationOfBenefit.AddedItemDetailComponent;
import org.hl7.fhir.r4.model.ExplanationOfBenefit.AddedItemDetailSubDetailComponent;
import org.hl7.fhir.r4.model.ExplanationOfBenefit.AdjudicationComponent;
import org.hl7.fhir.r4.model.ExplanationOfBenefit.BenefitBalanceComponent;
import org.hl7.fhir.r4.model.ExplanationOfBenefit.BenefitComponent;
import org.hl7.fhir.r4.model.ExplanationOfBenefit.CareTeamComponent;
import org.hl7.fhir.r4.model.ExplanationOfBenefit.DetailComponent;
import org.hl7.fhir.r4.model.ExplanationOfBenefit.DiagnosisComponent;
import org.hl7.fhir.r4.model.ExplanationOfBenefit.InsuranceComponent;
import org.hl7.fhir.r4.model.ExplanationOfBenefit.ItemComponent;
import org.hl7.fhir.r4.model.ExplanationOfBenefit.ProcedureComponent;
import org.hl7.fhir.r4.model.ExplanationOfBenefit.SubDetailComponent;
import org.hl7.fhir.r4.model.ExplanationOfBenefit.SupportingInformationComponent;
import org.hl7.fhir.r4.model.ExplanationOfBenefit.TotalComponent;
import org.hl7.fhir.r4.model.Resource;

/**
 * @author richard.ettema
 *
 */
public class ResourceSummaryExplanationOfBenefit extends ResourceSummaryProxy {

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.summary.ResourceSummaryProxy#generateSummary(org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public Resource generateSummary(Resource resource) throws Exception {

		ExplanationOfBenefit summary = null;

		try {
			// Cast original resource to expected type
			ExplanationOfBenefit original = (ExplanationOfBenefit) resource;

			// Instantiate summary resource
			summary = new ExplanationOfBenefit();

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
			summary.setStatus(original.getStatus());
			summary.setType(original.getType());
			summary.setUse(original.getUse());
			summary.setPatient(original.getPatient());
			summary.setBillablePeriod(original.getBillablePeriod());
			summary.setCreated(original.getCreated());
			summary.setInsurer(original.getInsurer());
			summary.setProvider(original.getProvider());
			summary.setOutcome(original.getOutcome());

			InsuranceComponent summaryInsurance;
			for (InsuranceComponent insurance : original.getInsurance()) {
				summaryInsurance = new InsuranceComponent();
				summaryInsurance.setFocal(insurance.getFocal());
				summaryInsurance.setCoverage(insurance.getCoverage());
				summary.addInsurance(summaryInsurance);
			}

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
	 * @see net.aegis.fhir.service.summary.ResourceSummaryProxy#generateDataSummary(org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public Resource generateDataSummary(Resource resource) throws Exception {

		ExplanationOfBenefit summary = null;

		try {
			// Cast original resource to expected type
			ExplanationOfBenefit original = (ExplanationOfBenefit) resource;

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

		ExplanationOfBenefit summary = null;

		try {
			// Cast original resource to expected type
			ExplanationOfBenefit original = (ExplanationOfBenefit) resource;

			// Instantiate summary resource
			summary = new ExplanationOfBenefit();

			// Copy text-only DomainResource elements
			copyTextOnlyDomainResourceElements(original, summary);

			// Set Meta Security Tag SUBSETTED
			setMetaTagSubsetted(summary);

			// Copy mandatory Resource Type elements
			summary.setStatus(original.getStatus());
			summary.setType(original.getType());
			summary.setUse(original.getUse());
			summary.setPatient(original.getPatient());
			summary.setCreated(original.getCreated());
			summary.setInsurer(original.getInsurer());
			summary.setProvider(original.getProvider());
			summary.setOutcome(original.getOutcome());

			CareTeamComponent summaryCareTeam;
			for (CareTeamComponent careTeam : original.getCareTeam()) {
				summaryCareTeam = new CareTeamComponent();
				summaryCareTeam.setSequence(careTeam.getSequence());
				summaryCareTeam.setProvider(careTeam.getProvider());
				summary.addCareTeam(summaryCareTeam);
			}
			SupportingInformationComponent summarysupportingInfo;
			for (SupportingInformationComponent supportingInfo : original.getSupportingInfo()) {
				summarysupportingInfo = new SupportingInformationComponent();
				summarysupportingInfo.setSequence(supportingInfo.getSequence());
				summarysupportingInfo.setCategory(supportingInfo.getCategory());
				summary.addSupportingInfo(summarysupportingInfo);
			}
			DiagnosisComponent summaryDiagnosis;
			for (DiagnosisComponent diagnosis : original.getDiagnosis()) {
				summaryDiagnosis = new DiagnosisComponent();
				summaryDiagnosis.setSequence(diagnosis.getSequence());
				summaryDiagnosis.setDiagnosis(diagnosis.getDiagnosis());
				summary.addDiagnosis(summaryDiagnosis);
			}
			ProcedureComponent summaryProcedure;
			for (ProcedureComponent procedure : original.getProcedure()) {
				summaryProcedure = new ProcedureComponent();
				summaryProcedure.setSequence(procedure.getSequence());
				summaryProcedure.setProcedure(procedure.getProcedure());
				summary.addProcedure(summaryProcedure);
			}
			InsuranceComponent summaryInsurance;
			for (InsuranceComponent insurance : original.getInsurance()) {
				summaryInsurance = new InsuranceComponent();
				summaryInsurance.setFocal(insurance.getFocal());
				summaryInsurance.setCoverage(insurance.getCoverage());
				summary.addInsurance(summaryInsurance);
			}
			ItemComponent summaryItem;
			for (ItemComponent item : original.getItem()) {
				summaryItem = new ItemComponent();
				summaryItem.setSequence(item.getSequence());
				summaryItem.setProductOrService(item.getProductOrService());
				AdjudicationComponent summaryItemAdjudication;
				for (AdjudicationComponent adjudication : item.getAdjudication()) {
					summaryItemAdjudication = new AdjudicationComponent();
					summaryItemAdjudication.setCategory(adjudication.getCategory());
					item.addAdjudication(summaryItemAdjudication);
				}
				DetailComponent summaryItemDetail;
				for (DetailComponent detail : item.getDetail()) {
					summaryItemDetail = new DetailComponent();
					summaryItemDetail.setSequence(detail.getSequence());
					summaryItemDetail.setProductOrService(detail.getProductOrService());
					AdjudicationComponent summaryItemDetailAdjudication;
					for (AdjudicationComponent adjudication : detail.getAdjudication()) {
						summaryItemDetailAdjudication = new AdjudicationComponent();
						summaryItemDetailAdjudication.setCategory(adjudication.getCategory());
						detail.addAdjudication(summaryItemDetailAdjudication);
					}
					SubDetailComponent summaryItemDetailSubDetail;
					for (SubDetailComponent subDetail : detail.getSubDetail()) {
						summaryItemDetailSubDetail = new SubDetailComponent();
						summaryItemDetailSubDetail.setSequence(subDetail.getSequence());
						summaryItemDetailSubDetail.setProductOrService(subDetail.getProductOrService());
						AdjudicationComponent summaryItemDetailSubDetailAdjudication;
						for (AdjudicationComponent adjudication : subDetail.getAdjudication()) {
							summaryItemDetailSubDetailAdjudication = new AdjudicationComponent();
							summaryItemDetailSubDetailAdjudication.setCategory(adjudication.getCategory());
							subDetail.addAdjudication(summaryItemDetailSubDetailAdjudication);
						}
						detail.addSubDetail(summaryItemDetailSubDetail);
					}
					item.addDetail(summaryItemDetail);
				}
				summary.addItem(summaryItem);
			}
			AddedItemComponent summaryAddItem;
			for (AddedItemComponent addItem : original.getAddItem()) {
				summaryAddItem = new AddedItemComponent();
				summaryAddItem.setProductOrService(addItem.getProductOrService());
				AdjudicationComponent summaryAddItemAdjudication;
				for (AdjudicationComponent adjudication : addItem.getAdjudication()) {
					summaryAddItemAdjudication = new AdjudicationComponent();
					summaryAddItemAdjudication.setCategory(adjudication.getCategory());
					summaryAddItem.addAdjudication(summaryAddItemAdjudication);
				}
				AddedItemDetailComponent summaryAddItemDetail;
				for (AddedItemDetailComponent detail : addItem.getDetail()) {
					summaryAddItemDetail = new AddedItemDetailComponent();
					summaryAddItemDetail.setProductOrService(detail.getProductOrService());
					AdjudicationComponent summaryAddItemDetailAdjudication;
					for (AdjudicationComponent adjudication : detail.getAdjudication()) {
						summaryAddItemDetailAdjudication = new AdjudicationComponent();
						summaryAddItemDetailAdjudication.setCategory(adjudication.getCategory());
						detail.addAdjudication(summaryAddItemDetailAdjudication);
					}
					AddedItemDetailSubDetailComponent summaryAddItemDetailSubDetail;
					for (AddedItemDetailSubDetailComponent subDetail : detail.getSubDetail()) {
						summaryAddItemDetailSubDetail = new AddedItemDetailSubDetailComponent();
						summaryAddItemDetailSubDetail.setProductOrService(subDetail.getProductOrService());
						AdjudicationComponent summaryItemDetailSubDetailAdjudication;
						for (AdjudicationComponent adjudication : subDetail.getAdjudication()) {
							summaryItemDetailSubDetailAdjudication = new AdjudicationComponent();
							summaryItemDetailSubDetailAdjudication.setCategory(adjudication.getCategory());
							subDetail.addAdjudication(summaryItemDetailSubDetailAdjudication);
						}
						detail.addSubDetail(summaryAddItemDetailSubDetail);
					}
					addItem.addDetail(summaryAddItemDetail);
				}
				summary.addAddItem(summaryAddItem);
			}
			AdjudicationComponent summaryAdjudication;
			for (AdjudicationComponent adjudication : original.getAdjudication()) {
				summaryAdjudication = new AdjudicationComponent();
				summaryAdjudication.setCategory(adjudication.getCategory());
				summary.addAdjudication(summaryAdjudication);
			}
			TotalComponent summaryTotal;
			for (TotalComponent total : original.getTotal()) {
				summaryTotal = new TotalComponent();
				summaryTotal.setCategory(total.getCategory());
				summaryTotal.setAmount(total.getAmount());
				summary.addTotal(summaryTotal);
			}
			BenefitBalanceComponent summaryBenefitBalance;
			for (BenefitBalanceComponent benefitBalance : original.getBenefitBalance()) {
				summaryBenefitBalance = new BenefitBalanceComponent();
				summaryBenefitBalance.setCategory(benefitBalance.getCategory());
				BenefitComponent summaryBenefitBalanceFinancial;
				for (BenefitComponent financial : benefitBalance.getFinancial()) {
					summaryBenefitBalanceFinancial = new BenefitComponent();
					summaryBenefitBalanceFinancial.setType(financial.getType());
					summaryBenefitBalance.addFinancial(summaryBenefitBalanceFinancial);
				}
				summary.addBenefitBalance(summaryBenefitBalance);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return summary;
	}

}
