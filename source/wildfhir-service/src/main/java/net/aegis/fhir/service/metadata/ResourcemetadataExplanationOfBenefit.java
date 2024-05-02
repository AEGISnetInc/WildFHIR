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
package net.aegis.fhir.service.metadata;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.ExplanationOfBenefit;
import org.hl7.fhir.r4.model.ExplanationOfBenefit.CareTeamComponent;
import org.hl7.fhir.r4.model.ExplanationOfBenefit.InsuranceComponent;
import org.hl7.fhir.r4.model.ExplanationOfBenefit.ItemComponent;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataExplanationOfBenefit extends ResourcemetadataProxy {

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.metadata.ResourcemetadataProxy#generateAllForResource(net.aegis.fhir.model.Resource, java.lang.String, net.aegis.fhir.service.ResourceService)
	 */
	@Override
	public List<Resourcemetadata> generateAllForResource(Resource resource, String baseUrl, ResourceService resourceService) throws Exception {
		return generateAllForResource(resource, baseUrl, resourceService, null, null, 0);
	}

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.metadata.ResourcemetadataProxy#generateAllForResource(net.aegis.fhir.model.Resource, java.lang.String, net.aegis.fhir.service.ResourceService, net.aegis.fhir.model.Resource, java.lang.String, int)
	 */
	@Override
	public List<Resourcemetadata> generateAllForResource(Resource resource, String baseUrl, ResourceService resourceService, Resource chainedResource, String chainedParameter, int chainedIndex) throws Exception {

		if (StringUtils.isEmpty(chainedParameter)) {
			chainedParameter = "";
		}

		List<Resourcemetadata> resourcemetadataList = new ArrayList<Resourcemetadata>();
        ByteArrayInputStream iExplanationOfBenefit = null;

		try {
            // Extract and convert the resource contents to a ExplanationOfBenefit object
			if (chainedResource != null) {
				iExplanationOfBenefit = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iExplanationOfBenefit = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            ExplanationOfBenefit explanationOfBenefit = (ExplanationOfBenefit) xmlP.parse(iExplanationOfBenefit);
            iExplanationOfBenefit.close();

			/*
             * Create new Resourcemetadata objects for each ExplanationOfBenefit metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, explanationOfBenefit, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", explanationOfBenefit.getId());
			resourcemetadataList.add(_id);

			// _language : token
			if (explanationOfBenefit.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", explanationOfBenefit.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (explanationOfBenefit.getMeta() != null && explanationOfBenefit.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(explanationOfBenefit.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(explanationOfBenefit.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// care-team : reference
			if (explanationOfBenefit.hasCareTeam()) {

				Resourcemetadata rCareTeamRequest = null;
				List<Resourcemetadata> rCareTeamRequestChain = null;
				for (CareTeamComponent careTeam : explanationOfBenefit.getCareTeam()) {

					if (careTeam.hasProvider() && careTeam.getProvider().hasReference()) {
						rCareTeamRequest = generateResourcemetadata(resource, chainedResource, chainedParameter+"care-team", generateFullLocalReference(careTeam.getProvider().getReference(), baseUrl));
						resourcemetadataList.add(rCareTeamRequest);

						if (chainedResource == null) {
							// Add chained parameters for any
							rCareTeamRequestChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "care-team.", 0, careTeam.getProvider().getReference());
							resourcemetadataList.addAll(rCareTeamRequestChain);
						}
					}
				}
			}

			// claim : reference
			if (explanationOfBenefit.hasClaim()) {
				Resourcemetadata rClaimReference = generateResourcemetadata(resource, chainedResource, chainedParameter+"claim", generateFullLocalReference(explanationOfBenefit.getClaim().getReference(), baseUrl));
				resourcemetadataList.add(rClaimReference);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rClaimChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "claim", 0, explanationOfBenefit.getClaim().getReference());
					resourcemetadataList.addAll(rClaimChain);
				}
			}

			// coverage : reference
			if (explanationOfBenefit.hasInsurance()) {

				Resourcemetadata rCoverage = null;
				List<Resourcemetadata> rCoverageChain = null;
				for (InsuranceComponent insurance : explanationOfBenefit.getInsurance()) {

					if (insurance.hasCoverage() && insurance.getCoverage().hasReference()) {
						rCoverage = generateResourcemetadata(resource, chainedResource, chainedParameter+"coverage", generateFullLocalReference(insurance.getCoverage().getReference(), baseUrl));
						resourcemetadataList.add(rCoverage);

						if (chainedResource == null) {
							// Add chained parameters
							rCoverageChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "coverage", 0, insurance.getCoverage().getReference());
							resourcemetadataList.addAll(rCoverageChain);
						}
					}
				}
			}

			// created : date
			if (explanationOfBenefit.hasCreated()) {
				Resourcemetadata rCreated = generateResourcemetadata(resource, chainedResource, chainedParameter+"created", utcDateUtil.formatDate(explanationOfBenefit.getCreated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(explanationOfBenefit.getCreated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rCreated);
			}

			// disposition : string
			if (explanationOfBenefit.hasDisposition()) {
				Resourcemetadata rDisposition = generateResourcemetadata(resource, chainedResource, chainedParameter+"disposition", explanationOfBenefit.getDisposition());
				resourcemetadataList.add(rDisposition);
			}

			// encounter : reference
			if (explanationOfBenefit.hasItem()) {

				String itemReference = null;
				Resourcemetadata rItem = null;
				List<Resourcemetadata> rItemChain = null;
				for (ItemComponent item : explanationOfBenefit.getItem()) {

					// encounter : reference
					if (item.hasEncounter()) {

						for (Reference encounter : item.getEncounter()) {

							if (encounter.hasReference()) {
								itemReference = generateFullLocalReference(encounter.getReference(), baseUrl);

								rItem = generateResourcemetadata(resource, chainedResource, chainedParameter + "encounter", itemReference);
								resourcemetadataList.add(rItem);

								if (chainedResource == null) {
									// Add chained parameters for any
									rItemChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "encounter", 0, encounter.getReference());
									resourcemetadataList.addAll(rItemChain);
								}
							}
						}
					}

					// item-udi : reference
					if (item.hasUdi()) {

						for (Reference itemUdi : item.getUdi()) {

							if (itemUdi.hasReference()) {
								itemReference = generateFullLocalReference(itemUdi.getReference(), baseUrl);

								rItem = generateResourcemetadata(resource, chainedResource, chainedParameter + "item-udi", itemReference);
								resourcemetadataList.add(rItem);

								if (chainedResource == null) {
									// Add chained parameters for any
									rItemChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "item-udi", 0, itemUdi.getReference());
									resourcemetadataList.addAll(rItemChain);
								}
							}
						}
					}

					if (item.hasDetail()) {

						for (org.hl7.fhir.r4.model.ExplanationOfBenefit.DetailComponent detail : item.getDetail()) {

							// detail-udi : reference
							if (detail.hasUdi()) {

								for (Reference detailUdi : detail.getUdi()) {

									if (detailUdi.hasReference()) {
										itemReference = generateFullLocalReference(detailUdi.getReference(), baseUrl);

										rItem = generateResourcemetadata(resource, chainedResource, chainedParameter + "detail-udi", itemReference);
										resourcemetadataList.add(rItem);

										if (chainedResource == null) {
											// Add chained parameters for any
											rItemChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "detail-udi", 0, detailUdi.getReference());
											resourcemetadataList.addAll(rItemChain);
										}
									}
								}
							}

							if (detail.hasSubDetail()) {

								for (org.hl7.fhir.r4.model.ExplanationOfBenefit.SubDetailComponent subDetail : detail.getSubDetail()) {

									// subdetail-udi : reference
									if (subDetail.hasUdi()) {

										for (Reference subDetailUdi : subDetail.getUdi()) {

											if (subDetailUdi.hasReference()) {
												itemReference = generateFullLocalReference(subDetailUdi.getReference(), baseUrl);

												rItem = generateResourcemetadata(resource, chainedResource, chainedParameter + "subdetail-udi", itemReference);
												resourcemetadataList.add(rItem);

												if (chainedResource == null) {
													// Add chained parameters for any
													rItemChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "subdetail-udi", 0, subDetailUdi.getReference());
													resourcemetadataList.addAll(rItemChain);
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

			// enterer : reference
			if (explanationOfBenefit.hasEnterer()) {
				Resourcemetadata rEntererReference = generateResourcemetadata(resource, chainedResource, chainedParameter+"enterer", generateFullLocalReference(explanationOfBenefit.getEnterer().getReference(), baseUrl));
				resourcemetadataList.add(rEntererReference);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rEntererReferenceChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "enterer", 0, explanationOfBenefit.getFacility().getReference());
					resourcemetadataList.addAll(rEntererReferenceChain);
				}
			}

			// facility : reference
			if (explanationOfBenefit.hasFacility()) {
				Resourcemetadata rFacilityReference = generateResourcemetadata(resource, chainedResource, chainedParameter+"facility", generateFullLocalReference(explanationOfBenefit.getFacility().getReference(), baseUrl));
				resourcemetadataList.add(rFacilityReference);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rFacilityChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "facility", 0, explanationOfBenefit.getFacility().getReference());
					resourcemetadataList.addAll(rFacilityChain);
				}
			}

			// identifier : token
			if (explanationOfBenefit.hasIdentifier()) {

				for (Identifier identifier : explanationOfBenefit.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// patient : reference
			if (explanationOfBenefit.hasPatient()) {
				Resourcemetadata rPatientReference = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", generateFullLocalReference(explanationOfBenefit.getPatient().getReference(), baseUrl));
				resourcemetadataList.add(rPatientReference);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rPatientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, explanationOfBenefit.getPatient().getReference());
					resourcemetadataList.addAll(rPatientChain);
				}
			}

			// payee : reference
			if (explanationOfBenefit.hasPayee() && explanationOfBenefit.getPayee().hasParty()) {
				Resourcemetadata rPayeeReference = generateResourcemetadata(resource, chainedResource, chainedParameter+"payee", generateFullLocalReference(explanationOfBenefit.getPayee().getParty().getReference(), baseUrl));
				resourcemetadataList.add(rPayeeReference);

				if (chainedResource == null) {
					// Add chained parameters for any
					List<Resourcemetadata> rPayeeChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "payee", 0, explanationOfBenefit.getPayee().getParty().getReference());
					resourcemetadataList.addAll(rPayeeChain);
				}
			}

			// procedure-udi : reference
			if (explanationOfBenefit.hasProcedure()) {

				String procedureUdiReference = null;
				Resourcemetadata rProcedureUdi = null;
				List<Resourcemetadata> rProcedureUdiChain = null;
				for (org.hl7.fhir.r4.model.ExplanationOfBenefit.ProcedureComponent procedure : explanationOfBenefit.getProcedure()) {

					if (procedure.hasUdi()) {

						for (Reference procedureUdi : procedure.getUdi()) {

							if (procedureUdi.hasReference()) {
								procedureUdiReference = generateFullLocalReference(procedureUdi.getReference(), baseUrl);

								rProcedureUdi = generateResourcemetadata(resource, chainedResource, chainedParameter + "procedure-udi", procedureUdiReference);
								resourcemetadataList.add(rProcedureUdi);

								if (chainedResource == null) {
									// Add chained parameters for any
									rProcedureUdiChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "procedure-udi", 0, procedureUdi.getReference());
									resourcemetadataList.addAll(rProcedureUdiChain);
								}
							}
						}
					}
				}
			}

			// provider : reference
			if (explanationOfBenefit.hasProvider()) {
				Resourcemetadata rProviderReference = generateResourcemetadata(resource, chainedResource, chainedParameter+"provider", generateFullLocalReference(explanationOfBenefit.getProvider().getReference(), baseUrl));
				resourcemetadataList.add(rProviderReference);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rProviderChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "provider", 0, explanationOfBenefit.getProvider().getReference());
					resourcemetadataList.addAll(rProviderChain);
				}
			}

			// status : token
			if (explanationOfBenefit.hasStatus() && explanationOfBenefit.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", explanationOfBenefit.getStatus().toCode(), explanationOfBenefit.getStatus().getSystem());
				resourcemetadataList.add(rStatus);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return resourcemetadataList;
	}

}
