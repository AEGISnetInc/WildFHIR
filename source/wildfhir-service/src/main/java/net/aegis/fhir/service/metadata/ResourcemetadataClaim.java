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
import java.io.IOException;
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
		return generateAllForResource(resource, baseUrl, resourceService, null, null, 0, null);
	}

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.metadata.ResourcemetadataProxy#generateAllForResource(net.aegis.fhir.model.Resource, java.lang.String, net.aegis.fhir.service.ResourceService, net.aegis.fhir.model.Resource, java.lang.String, int, org.hl7.fhir.r4.model.Resource)
	 */
	@Override
	public List<Resourcemetadata> generateAllForResource(Resource resource, String baseUrl, ResourceService resourceService, Resource chainedResource, String chainedParameter, int chainedIndex, org.hl7.fhir.r4.model.Resource fhirResource) throws Exception {

		if (StringUtils.isEmpty(chainedParameter)) {
			chainedParameter = "";
		}

		List<Resourcemetadata> resourcemetadataList = new ArrayList<Resourcemetadata>();
        ByteArrayInputStream iClaim = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

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
			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, claim, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// care-team : reference
			if (claim.hasCareTeam()) {

				for (CareTeamComponent careTeam : claim.getCareTeam()) {

					if (careTeam.hasProvider() && careTeam.getProvider().hasReference()) {
						rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "care-team", 0, careTeam.getProvider(), null);
						resourcemetadataList.addAll(rMetadataChain);
					}
				}
			}

			// created : date
			if (claim.hasCreated()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"created", utcDateUtil.formatDate(claim.getCreated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(claim.getCreated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rMetadata);
			}

			// encounter : reference
			if (claim.hasItem()) {

				for (ItemComponent item : claim.getItem()) {

					// encounter : reference
					if (item.hasEncounter()) {

						for (Reference encounter : item.getEncounter()) {
							rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "encounter", 0, encounter, null);
							resourcemetadataList.addAll(rMetadataChain);
						}
					}

					// item-udi : reference
					if (item.hasUdi()) {

						for (Reference itemUdi : item.getUdi()) {
							rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "item-udi", 0, itemUdi, null);
							resourcemetadataList.addAll(rMetadataChain);
						}
					}

					if (item.hasDetail()) {

						for (DetailComponent detail : item.getDetail()) {

							// detail-udi : reference
							if (detail.hasUdi()) {

								for (Reference detailUdi : detail.getUdi()) {
									rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "detail-udi", 0, detailUdi, null);
									resourcemetadataList.addAll(rMetadataChain);
								}
							}

							if (detail.hasSubDetail()) {

								for (SubDetailComponent subDetail : detail.getSubDetail()) {

									// subdetail-udi : reference
									if (subDetail.hasUdi()) {

										for (Reference subDetailUdi : subDetail.getUdi()) {
											rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "subdetail-udi", 0, subDetailUdi, null);
											resourcemetadataList.addAll(rMetadataChain);
										}
									}
								}
							}
						}
					}
				}
			}

			// enterer : reference
			if (claim.hasEnterer()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "enterer", 0, claim.getEnterer(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// facility : reference
			if (claim.hasFacility() && claim.getFacility().hasReference()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "facility", 0, claim.getFacility(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// identifier : token
			if (claim.hasIdentifier()) {

				for (Identifier identifier : claim.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}

			// insurer : reference
			if (claim.hasInsurer() && claim.getInsurer().hasReference()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "insurer", 0, claim.getInsurer(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// patient : reference
			if (claim.hasPatient() && claim.getPatient().hasReference()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "patient", 0, claim.getPatient(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// payee : reference
			if (claim.hasPayee() && claim.getPayee().hasParty() && claim.getPayee().getParty().hasReference()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "payee", 0, claim.getPayee().getParty(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// priority : token
			if (claim.hasPriority() && claim.getPriority().hasCoding()) {

				for (Coding code : claim.getPriority().getCoding()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"priority", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rMetadata);
				}
			}

			// procedure-udi : reference
			if (claim.hasProcedure()) {

				for (ProcedureComponent procedure : claim.getProcedure()) {
					if (procedure.hasUdi()) {

						for (Reference procedureUdi : procedure.getUdi()) {
							rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "procedure-udi", 0, procedureUdi, null);
							resourcemetadataList.addAll(rMetadataChain);
						}
					}
				}
			}

			// provider : reference
			if (claim.hasProvider()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "provider", 0, claim.getProvider(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// status : token
			if (claim.hasStatus() && claim.getStatus() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", claim.getStatus().toCode(), claim.getStatus().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// use : token
			if (claim.hasUse() && claim.getUse() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"use", claim.getUse().toCode(), claim.getUse().getSystem());
				resourcemetadataList.add(rMetadata);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		} finally {
	        rMetadata = null;
	        rMetadataChain = null;
            if (iClaim != null) {
                try {
                	iClaim.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
