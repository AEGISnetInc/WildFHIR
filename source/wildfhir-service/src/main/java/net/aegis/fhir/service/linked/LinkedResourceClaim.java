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

import org.hl7.fhir.r4.model.Claim;
import org.hl7.fhir.r4.model.Claim.CareTeamComponent;
import org.hl7.fhir.r4.model.Claim.DetailComponent;
import org.hl7.fhir.r4.model.Claim.DiagnosisComponent;
import org.hl7.fhir.r4.model.Claim.InsuranceComponent;
import org.hl7.fhir.r4.model.Claim.ItemComponent;
import org.hl7.fhir.r4.model.Claim.ProcedureComponent;
import org.hl7.fhir.r4.model.Claim.RelatedClaimComponent;
import org.hl7.fhir.r4.model.Claim.SubDetailComponent;
import org.hl7.fhir.r4.model.Claim.SupportingInformationComponent;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

import net.aegis.fhir.service.ResourceService;

/**
 * @author richard.ettema
 *
 */
public class LinkedResourceClaim extends LinkedResourceProxy {

	private Logger log = Logger.getLogger("LinkedResourceClaim");

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.linked.LinkedResourceProxy#getLinkedResources(net.aegis.fhir.service.ResourceService, org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public List<Resource> getLinkedResources(ResourceService resourceService, Resource containerResource) throws Exception {

		log.fine("[START] LinkedResourceClaim.getLinkedResources()");

		List<Resource> linkedResources = new ArrayList<Resource>();

		try {
			// Test for valid container Resource
			if (containerResource != null && containerResource instanceof Claim) {

				Claim typedContainerResource = (Claim) containerResource;

				/*
				 * Claim linked Resource references
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
				// enterer
				if (typedContainerResource.hasEnterer() && typedContainerResource.getEnterer().hasReference()) {

					ref = typedContainerResource.getEnterer().getReference();
					linkedResource = this.getLinkedResourceAny(resourceService, ref);

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// insurer (Organization)
				if (typedContainerResource.hasInsurer() && typedContainerResource.getInsurer().hasReference()) {

					ref = typedContainerResource.getInsurer().getReference();
					linkedResource = this.getLinkedResource(resourceService, ref, "Organization");

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// provider
				if (typedContainerResource.hasProvider() && typedContainerResource.getProvider().hasReference()) {

					ref = typedContainerResource.getProvider().getReference();
					linkedResource = this.getLinkedResourceAny(resourceService, ref);

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// related
				if (typedContainerResource.hasRelated()) {

					for (RelatedClaimComponent related : typedContainerResource.getRelated()) {
						// related.claim (Claim)
						if (related.hasClaim() && related.getClaim().hasReference()) {

							ref = related.getClaim().getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "Claim");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// prescription
				if (typedContainerResource.hasPrescription() && typedContainerResource.getPrescription().hasReference()) {

					ref = typedContainerResource.getPrescription().getReference();
					linkedResource = this.getLinkedResourceAny(resourceService, ref);

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// originalPrescription (MedicationRequest)
				if (typedContainerResource.hasOriginalPrescription() && typedContainerResource.getOriginalPrescription().hasReference()) {

					ref = typedContainerResource.getOriginalPrescription().getReference();
					linkedResource = this.getLinkedResourceAny(resourceService, ref);

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// payee
				if (typedContainerResource.hasPayee() && typedContainerResource.getPayee().hasParty() && typedContainerResource.getPayee().getParty().hasReference()) {

					ref = typedContainerResource.getPayee().getParty().getReference();
					linkedResource = this.getLinkedResourceAny(resourceService, ref);

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// referral (ServiceRequest)
				if (typedContainerResource.hasReferral() && typedContainerResource.getReferral().hasReference()) {

					ref = typedContainerResource.getReferral().getReference();
					linkedResource = this.getLinkedResource(resourceService, ref, "ServiceRequest");

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// facility (Location)
				if (typedContainerResource.hasFacility() && typedContainerResource.getFacility().hasReference()) {

					ref = typedContainerResource.getFacility().getReference();
					linkedResource = this.getLinkedResource(resourceService, ref, "Location");

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// careTeam
				if (typedContainerResource.hasCareTeam()) {

					for (CareTeamComponent careTeam : typedContainerResource.getCareTeam()) {
						// careTeam.provider
						if (careTeam.hasProvider() && careTeam.getProvider().hasReference()) {

							ref = careTeam.getProvider().getReference();
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

					for (SupportingInformationComponent supportingInfo : typedContainerResource.getSupportingInfo()) {
						// supportingInfo.valueReference
						if (supportingInfo.hasValueReference() && supportingInfo.getValueReference().hasReference()) {

							ref = supportingInfo.getValueReference().getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// diagnosis
				if (typedContainerResource.hasDiagnosis()) {

					for (DiagnosisComponent diagnosis : typedContainerResource.getDiagnosis()) {
						// diagnosis.diagnosisReference (Condition)
						if (diagnosis.hasDiagnosisReference() && diagnosis.getDiagnosisReference().hasReference()) {

							ref = diagnosis.getDiagnosisReference().getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "Condition");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// procedure
				if (typedContainerResource.hasDiagnosis()) {

					for (ProcedureComponent procedure : typedContainerResource.getProcedure()) {
						// diagnosis.procedureReference (Procedure)
						if (procedure.hasProcedureReference() && procedure.getProcedureReference().hasReference()) {

							ref = procedure.getProcedureReference().getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "Procedure");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// insurance
				if (typedContainerResource.hasInsurance()) {

					for (InsuranceComponent insurance : typedContainerResource.getInsurance()) {
						// insurance.coverage (Coverage)
						if (insurance.hasCoverage() && insurance.getCoverage().hasReference()) {

							ref = insurance.getCoverage().getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "Coverage");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}

						// insurance.claimResponse (ClaimResponse)
						if (insurance.hasClaimResponse() && insurance.getClaimResponse().hasReference()) {

							ref = insurance.getClaimResponse().getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "ClaimResponse");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// accident.locationReference (Location)
				if (typedContainerResource.hasAccident() && typedContainerResource.getAccident().hasLocationReference() && typedContainerResource.getAccident().getLocationReference().hasReference()) {

					ref = typedContainerResource.getAccident().getLocationReference().getReference();
					linkedResource = this.getLinkedResource(resourceService, ref, "Location");

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// item
				if (typedContainerResource.hasItem()) {

					for (ItemComponent item : typedContainerResource.getItem()) {
						// item.locationReference (Location)
						if (item.hasLocationReference() && item.getLocationReference().hasReference()) {

							ref = item.getLocationReference().getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "Location");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}

						// item.udi (Device)
						if (item.hasUdi()) {

							for (Reference udi : item.getUdi()) {
								if (udi.hasReference()) {

									ref = udi.getReference();
									linkedResource = this.getLinkedResource(resourceService, ref, "Device");

									if (linkedResource != null) {
										linkedResources.add(linkedResource);
									}
								}
							}
						}

						// item.encounter (Location)
						if (item.hasLocationReference() && item.getLocationReference().hasReference()) {

							ref = item.getLocationReference().getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "Location");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}

						// item.detail
						if (item.hasDetail()) {

							for (DetailComponent detail : item.getDetail()) {

								// item.detail.udi (Device)
								if (detail.hasUdi()) {

									for (Reference udi : detail.getUdi()) {
										if (udi.hasReference()) {

											ref = udi.getReference();
											linkedResource = this.getLinkedResource(resourceService, ref, "Device");

											if (linkedResource != null) {
												linkedResources.add(linkedResource);
											}
										}
									}
								}

								// item.detail.subDetail
								if (detail.hasSubDetail()) {

									for (SubDetailComponent subDetail : detail.getSubDetail()) {

										// item.detail.subDetail.udi (Device)
										if (subDetail.hasUdi()) {

											for (Reference udi : subDetail.getUdi()) {
												if (udi.hasReference()) {

													ref = udi.getReference();
													linkedResource = this.getLinkedResource(resourceService, ref, "Device");

													if (linkedResource != null) {
														linkedResources.add(linkedResource);
													}
												}
											}
										}
									}
								}
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
