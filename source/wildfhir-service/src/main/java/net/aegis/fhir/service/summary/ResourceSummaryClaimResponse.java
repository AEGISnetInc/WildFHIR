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

import org.hl7.fhir.r4.model.ClaimResponse;
import org.hl7.fhir.r4.model.ClaimResponse.AddedItemComponent;
import org.hl7.fhir.r4.model.ClaimResponse.AddedItemDetailComponent;
import org.hl7.fhir.r4.model.ClaimResponse.AddedItemSubDetailComponent;
import org.hl7.fhir.r4.model.ClaimResponse.AdjudicationComponent;
import org.hl7.fhir.r4.model.ClaimResponse.ErrorComponent;
import org.hl7.fhir.r4.model.ClaimResponse.InsuranceComponent;
import org.hl7.fhir.r4.model.ClaimResponse.ItemComponent;
import org.hl7.fhir.r4.model.ClaimResponse.ItemDetailComponent;
import org.hl7.fhir.r4.model.ClaimResponse.NoteComponent;
import org.hl7.fhir.r4.model.ClaimResponse.PaymentComponent;
import org.hl7.fhir.r4.model.ClaimResponse.SubDetailComponent;
import org.hl7.fhir.r4.model.ClaimResponse.TotalComponent;
import org.hl7.fhir.r4.model.Resource;

/**
 * @author richard.ettema
 *
 */
public class ResourceSummaryClaimResponse extends ResourceSummaryProxy {

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.summary.ResourceSummaryProxy#generateSummary(org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public Resource generateSummary(Resource resource) throws Exception {

		ClaimResponse summary = null;

		try {
			// Cast original resource to expected type
			ClaimResponse original = (ClaimResponse) resource;

			// Instantiate summary resource
			summary = new ClaimResponse();

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
			summary.setCreated(original.getCreated());
			summary.setInsurer(original.getInsurer());
			summary.setRequest(original.getRequest());
			summary.setOutcome(original.getOutcome());

			TotalComponent summaryTotal;
			for (TotalComponent total : original.getTotal()) {
				summaryTotal = new TotalComponent();
				summaryTotal.setCategory(total.getCategory());
				summaryTotal.setAmount(total.getAmount());
				summary.addTotal(summaryTotal);
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

		ClaimResponse summary = null;

		try {
			// Cast original resource to expected type
			ClaimResponse original = (ClaimResponse) resource;

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

		ClaimResponse summary = null;

		try {
			// Cast original resource to expected type
			ClaimResponse original = (ClaimResponse) resource;

			// Instantiate summary resource
			summary = new ClaimResponse();

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
			summary.setOutcome(original.getOutcome());

			ItemComponent summaryItem;
			for (ItemComponent item : original.getItem()) {
				summaryItem = new ItemComponent();
				summaryItem.setItemSequence(item.getItemSequence());
				AdjudicationComponent summaryItemAdjudication = null;
				for (AdjudicationComponent adjudication : item.getAdjudication()) {
					summaryItemAdjudication = new AdjudicationComponent();
					summaryItemAdjudication.setCategory(adjudication.getCategory());
					summaryItem.addAdjudication(summaryItemAdjudication);
				}
				ItemDetailComponent summaryItemDetail;
				for (ItemDetailComponent detail : item.getDetail()) {
					summaryItemDetail = new ItemDetailComponent();
					summaryItemDetail.setDetailSequence(detail.getDetailSequence());
					AdjudicationComponent summaryItemDetailAdjudication = null;
					for (AdjudicationComponent adjudication : detail.getAdjudication()) {
						summaryItemDetailAdjudication = new AdjudicationComponent();
						summaryItemDetailAdjudication.setCategory(adjudication.getCategory());
						summaryItemDetail.addAdjudication(summaryItemDetailAdjudication);
					}
					SubDetailComponent summaryItemDetailSubDetail;
					for (SubDetailComponent subDetail : detail.getSubDetail()) {
						summaryItemDetailSubDetail = new SubDetailComponent();
						summaryItemDetailSubDetail.setSubDetailSequence(subDetail.getSubDetailSequence());
						AdjudicationComponent summaryItemDetailSubDetailAdjudication = null;
						for (AdjudicationComponent adjudication : subDetail.getAdjudication()) {
							summaryItemDetailSubDetailAdjudication = new AdjudicationComponent();
							summaryItemDetailSubDetailAdjudication.setCategory(adjudication.getCategory());
							summaryItemDetailSubDetail.addAdjudication(summaryItemDetailSubDetailAdjudication);
						}
						summaryItemDetail.addSubDetail(summaryItemDetailSubDetail);
					}
					summaryItem.addDetail(summaryItemDetail);
				}
				summary.addItem(summaryItem);
			}

			AddedItemComponent summaryAddItem = null;
			for (AddedItemComponent addItem : original.getAddItem()) {
				if (addItem != null) {
					summaryAddItem = new AddedItemComponent();
					summaryAddItem.setProductOrService(addItem.getProductOrService());
					if (addItem.hasAdjudication()) {
						AdjudicationComponent summaryAddItemAdjudication = null;
						for (AdjudicationComponent adjudication : addItem.getAdjudication()) {
							summaryAddItemAdjudication = new AdjudicationComponent();
							summaryAddItemAdjudication.setCategory(adjudication.getCategory());
							summaryAddItem.addAdjudication(summaryAddItemAdjudication);
						}
					}

					if (addItem.hasDetail()) {
						AddedItemDetailComponent summaryAddItemDetail = null;
						for (AddedItemDetailComponent addItemDetail : addItem.getDetail()) {
							if (addItemDetail != null) {
								summaryAddItemDetail = new AddedItemDetailComponent();
								summaryAddItemDetail.setProductOrService(addItemDetail.getProductOrService());
								if (addItemDetail.hasAdjudication()) {
									AdjudicationComponent summaryAddItemDetailAdjudication = null;
									for (AdjudicationComponent addItemDetailAdjudication : addItemDetail.getAdjudication()) {
										summaryAddItemDetailAdjudication = new AdjudicationComponent();
										summaryAddItemDetailAdjudication.setCategory(addItemDetailAdjudication.getCategory());
										summaryAddItemDetail.addAdjudication(summaryAddItemDetailAdjudication);
									}
								}

								if (addItemDetail.hasSubDetail()) {
									AddedItemSubDetailComponent summaryAddItemDetailSubDetail = null;
									for (AddedItemSubDetailComponent addItemDetailSubDetail : addItemDetail.getSubDetail()) {
										if (addItemDetailSubDetail != null) {
											summaryAddItemDetailSubDetail = new AddedItemSubDetailComponent();
											summaryAddItemDetailSubDetail.setProductOrService(addItemDetailSubDetail.getProductOrService());

											AdjudicationComponent summaryAddItemDetailSubDetailAdjudication = null;
											for (AdjudicationComponent adjudication : addItemDetailSubDetail.getAdjudication()) {
												summaryAddItemDetailSubDetailAdjudication = new AdjudicationComponent();
												summaryAddItemDetailSubDetailAdjudication.setCategory(adjudication.getCategory());
												summaryAddItemDetailSubDetail.addAdjudication(summaryAddItemDetailSubDetailAdjudication);
											}

											summaryAddItemDetail.addSubDetail(summaryAddItemDetailSubDetail);
										}
									}
								}

								if (summaryAddItemDetail != null) {
									summaryAddItem.addDetail(summaryAddItemDetail);
								}
							}

							summaryAddItemDetail = null; // Reset addItem.detail to null for next iteration
						}

					}

					if (summaryAddItem != null) {
						summary.addAddItem(summaryAddItem);
					}
				}

				summaryAddItem = null; // Reset addItem to null for next iteration
			}

			TotalComponent summaryTotal;
			for (TotalComponent total : original.getTotal()) {
				summaryTotal = new TotalComponent();
				summaryTotal.setCategory(total.getCategory());
				summaryTotal.setAmount(total.getAmount());
				summary.addTotal(summaryTotal);
			}
			if (original.hasPayment()) {
				PaymentComponent summaryPayment = new PaymentComponent();
				summaryPayment.setType(original.getPayment().getType());
				summaryPayment.setAmount(original.getPayment().getAmount());
				summary.setPayment(summaryPayment);
			}
			NoteComponent summaryProcessNote;
			for (NoteComponent processNote : original.getProcessNote()) {
				summaryProcessNote = new NoteComponent();
				summaryProcessNote.setText(processNote.getText());
				summary.addProcessNote(summaryProcessNote);
			}
			InsuranceComponent summaryInsurance;
			for (InsuranceComponent insurance : original.getInsurance()) {
				summaryInsurance = new InsuranceComponent();
				summaryInsurance.setSequence(insurance.getSequence());
				summaryInsurance.setFocal(insurance.getFocal());
				summaryInsurance.setCoverage(insurance.getCoverage());
				summary.addInsurance(summaryInsurance);
			}
			ErrorComponent summaryError;
			for (ErrorComponent error : original.getError()) {
				summaryError = new ErrorComponent();
				summaryError.setCode(error.getCode());
				summary.addError(summaryError);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return summary;
	}

}
