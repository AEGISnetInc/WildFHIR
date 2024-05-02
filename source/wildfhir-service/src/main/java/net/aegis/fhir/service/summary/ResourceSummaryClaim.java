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

import org.hl7.fhir.r4.model.Claim;
import org.hl7.fhir.r4.model.Claim.AccidentComponent;
import org.hl7.fhir.r4.model.Claim.CareTeamComponent;
import org.hl7.fhir.r4.model.Claim.DetailComponent;
import org.hl7.fhir.r4.model.Claim.DiagnosisComponent;
import org.hl7.fhir.r4.model.Claim.InsuranceComponent;
import org.hl7.fhir.r4.model.Claim.ItemComponent;
import org.hl7.fhir.r4.model.Claim.PayeeComponent;
import org.hl7.fhir.r4.model.Claim.ProcedureComponent;
import org.hl7.fhir.r4.model.Claim.SubDetailComponent;
import org.hl7.fhir.r4.model.Claim.SupportingInformationComponent;
import org.hl7.fhir.r4.model.Resource;

/**
 * @author richard.ettema
 *
 */
public class ResourceSummaryClaim extends ResourceSummaryProxy {

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.summary.ResourceSummaryProxy#generateSummary(org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public Resource generateSummary(Resource resource) throws Exception {

		Claim summary = null;

		try {
			// Cast original resource to expected type
			Claim original = (Claim) resource;

			// Instantiate summary resource
			summary = new Claim();

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
			summary.setPriority(original.getPriority());

			InsuranceComponent summaryInsurance;
			for (InsuranceComponent insurance : original.getInsurance()) {
				summaryInsurance = new InsuranceComponent();
				summaryInsurance.setSequence(insurance.getSequence());
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

		Claim summary = null;

		try {
			// Cast original resource to expected type
			Claim original = (Claim) resource;

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

		Claim summary = null;

		try {
			// Cast original resource to expected type
			Claim original = (Claim) resource;

			// Instantiate summary resource
			summary = new Claim();

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
			summary.setProvider(original.getProvider());
			summary.setPriority(original.getPriority());
			if (original.hasPayee()) {
				PayeeComponent summaryPayee = new PayeeComponent();
				summaryPayee.setType(original.getPayee().getType());
				summary.setPayee(summaryPayee);
			}
			CareTeamComponent summaryCareTeam;
			for (CareTeamComponent careTeam : original.getCareTeam()) {
				summaryCareTeam = new CareTeamComponent();
				summaryCareTeam.setSequence(careTeam.getSequence());
				summaryCareTeam.setProvider(careTeam.getProvider());
				summary.addCareTeam(summaryCareTeam);
			}
			SupportingInformationComponent summarySupportingInfo;
			for (SupportingInformationComponent supportingInfo : original.getSupportingInfo()) {
				summarySupportingInfo = new SupportingInformationComponent();
				summarySupportingInfo.setSequence(supportingInfo.getSequence());
				summarySupportingInfo.setCategory(supportingInfo.getCategory());
				summary.addSupportingInfo(summarySupportingInfo);
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
				summaryInsurance.setSequence(insurance.getSequence());
				summaryInsurance.setFocal(insurance.getFocal());
				summaryInsurance.setCoverage(insurance.getCoverage());
				summary.addInsurance(summaryInsurance);
			}
			if (original.hasAccident()) {
				AccidentComponent summaryAccident = new AccidentComponent();
				summaryAccident.setDate(original.getAccident().getDate());
				summary.setAccident(summaryAccident);
			}
			ItemComponent summaryItem;
			for (ItemComponent item : original.getItem()) {
				summaryItem = new ItemComponent();
				summaryItem.setSequence(item.getSequence());
				summaryItem.setProductOrService(item.getProductOrService());
				DetailComponent summaryItemDetail;
				for (DetailComponent detail : item.getDetail()) {
					summaryItemDetail = new DetailComponent();
					summaryItemDetail.setSequence(detail.getSequence());
					summaryItemDetail.setProductOrService(detail.getProductOrService());
					SubDetailComponent summaryItemDetailSubDetail;
					for (SubDetailComponent subDetail : detail.getSubDetail()) {
						summaryItemDetailSubDetail = new SubDetailComponent();
						summaryItemDetailSubDetail.setSequence(subDetail.getSequence());
						summaryItemDetailSubDetail.setProductOrService(subDetail.getProductOrService());
						summaryItemDetail.addSubDetail(summaryItemDetailSubDetail);
					}
					item.addDetail(summaryItemDetail);
				}
				summary.addItem(summaryItem);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return summary;
	}

}
