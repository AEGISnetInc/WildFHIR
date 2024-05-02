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
import org.hl7.fhir.r4.model.Claim;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Claim.CareTeamComponent;
import org.hl7.fhir.r4.model.Claim.DetailComponent;
import org.hl7.fhir.r4.model.Claim.ItemComponent;
import org.hl7.fhir.r4.model.Claim.ProcedureComponent;
import org.hl7.fhir.r4.model.Claim.SubDetailComponent;
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
public class ResourcemetadataClaim extends ResourcemetadataProxy {

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
        ByteArrayInputStream iClaim = null;

		try {
            // Extract and convert the resource contents to a Claim object
			if (chainedResource != null) {
				iClaim = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iClaim = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            Claim claim = (Claim) xmlP.parse(iClaim);
            iClaim.close();

			/*
             * Create new Resourcemetadata objects for each Claim metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, claim, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", claim.getId());
			resourcemetadataList.add(_id);

			// _language : token
			if (claim.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", claim.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (claim.getMeta() != null && claim.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(claim.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(claim.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// care-team : reference
			if (claim.hasCareTeam()) {

				String careTeamReference = null;
				Resourcemetadata rCareTeam = null;
				List<Resourcemetadata> rCareTeamChain = null;
				for (CareTeamComponent careTeam : claim.getCareTeam()) {

					if (careTeam.hasProvider() && careTeam.getProvider().hasReference()) {
						careTeamReference = generateFullLocalReference(careTeam.getProvider().getReference(), baseUrl);

						rCareTeam = generateResourcemetadata(resource, chainedResource, chainedParameter+"care-team", careTeamReference);
						resourcemetadataList.add(rCareTeam);

						if (chainedResource == null) {
							// Add chained parameters for any
							rCareTeamChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "care-team", 0, careTeam.getProvider().getReference());
							resourcemetadataList.addAll(rCareTeamChain);
						}
					}
				}
			}

			// created : date
			if (claim.hasCreated()) {
				Resourcemetadata rCreated = generateResourcemetadata(resource, chainedResource, chainedParameter+"created", utcDateUtil.formatDate(claim.getCreated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(claim.getCreated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rCreated);
			}

			// encounter : reference
			if (claim.hasItem()) {

				String itemReference = null;
				Resourcemetadata rItem = null;
				List<Resourcemetadata> rItemChain = null;
				for (ItemComponent item : claim.getItem()) {

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

						for (DetailComponent detail : item.getDetail()) {

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

								for (SubDetailComponent subDetail : detail.getSubDetail()) {

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
			if (claim.hasEnterer() && claim.getEnterer().hasReference()) {
				Resourcemetadata rEntererReference = generateResourcemetadata(resource, chainedResource, chainedParameter+"enterer", generateFullLocalReference(claim.getEnterer().getReference(), baseUrl));
				resourcemetadataList.add(rEntererReference);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rEntererReferenceChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "enterer", 0, claim.getFacility().getReference());
					resourcemetadataList.addAll(rEntererReferenceChain);
				}
			}

			// facility : reference
			if (claim.hasFacility() && claim.getFacility().hasReference()) {
				Resourcemetadata rFacilityReference = generateResourcemetadata(resource, chainedResource, chainedParameter+"facility", generateFullLocalReference(claim.getFacility().getReference(), baseUrl));
				resourcemetadataList.add(rFacilityReference);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rFacilityReferenceChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "facility", 0, claim.getFacility().getReference());
					resourcemetadataList.addAll(rFacilityReferenceChain);
				}
			}

			// identifier : token
			if (claim.hasIdentifier()) {

				for (Identifier identifier : claim.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// insurer : reference
			if (claim.hasInsurer() && claim.getInsurer().hasReference()) {
				Resourcemetadata rInsurerReference = generateResourcemetadata(resource, chainedResource, chainedParameter+"insurer", generateFullLocalReference(claim.getInsurer().getReference(), baseUrl));
				resourcemetadataList.add(rInsurerReference);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rInsurerReferenceChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "insurer", 0, claim.getInsurer().getReference());
					resourcemetadataList.addAll(rInsurerReferenceChain);
				}
			}

			// patient : reference
			if (claim.hasPatient() && claim.getPatient().hasReference()) {
				Resourcemetadata rPatientReference = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", generateFullLocalReference(claim.getPatient().getReference(), baseUrl));
				resourcemetadataList.add(rPatientReference);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rPatientReferenceChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, claim.getPatient().getReference());
					resourcemetadataList.addAll(rPatientReferenceChain);
				}
			}

			// payee : reference
			if (claim.hasPayee() && claim.getPayee().hasParty() && claim.getPayee().getParty().hasReference()) {
				Resourcemetadata rPayeeReference = generateResourcemetadata(resource, chainedResource, chainedParameter+"payee", generateFullLocalReference(claim.getPayee().getParty().getReference(), baseUrl));
				resourcemetadataList.add(rPayeeReference);

				if (chainedResource == null) {
					// Add chained parameters for any
					List<Resourcemetadata> rPayeeChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "payee", 0, claim.getPayee().getParty().getReference());
					resourcemetadataList.addAll(rPayeeChain);
				}
			}

			// priority : token
			if (claim.hasPriority() && claim.getPriority().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : claim.getPriority().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"priority", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			// procedure-udi : reference
			if (claim.hasProcedure()) {

				String procedureUdiReference = null;
				Resourcemetadata rProcedureUdi = null;
				List<Resourcemetadata> rProcedureUdiChain = null;
				for (ProcedureComponent procedure : claim.getProcedure()) {

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
			if (claim.hasProvider() && claim.getProvider().hasReference()) {
				Resourcemetadata rProviderReference = generateResourcemetadata(resource, chainedResource, chainedParameter+"provider", generateFullLocalReference(claim.getProvider().getReference(), baseUrl));
				resourcemetadataList.add(rProviderReference);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rProviderReferenceChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "provider", 0, claim.getProvider().getReference());
					resourcemetadataList.addAll(rProviderReferenceChain);
				}
			}

			// status : token
			if (claim.hasStatus() && claim.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", claim.getStatus().toCode(), claim.getStatus().getSystem());
				resourcemetadataList.add(rStatus);
			}

			// use : token
			if (claim.hasUse() && claim.getUse() != null) {
				Resourcemetadata rUse = generateResourcemetadata(resource, chainedResource, chainedParameter+"use", claim.getUse().toCode(), claim.getUse().getSystem());
				resourcemetadataList.add(rUse);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return resourcemetadataList;
	}

}
