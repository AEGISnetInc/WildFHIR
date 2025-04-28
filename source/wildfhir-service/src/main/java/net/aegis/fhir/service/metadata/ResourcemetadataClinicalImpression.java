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

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.ClinicalImpression;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.ClinicalImpression.ClinicalImpressionFindingComponent;
import org.hl7.fhir.r4.model.ClinicalImpression.ClinicalImpressionInvestigationComponent;
import org.hl7.fhir.r4.model.Reference;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataClinicalImpression extends ResourcemetadataProxy {

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
        ByteArrayInputStream iClinicalImpression = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

		try {
            // Extract and convert the resource contents to a ClinicalImpression object
			if (chainedResource != null) {
				iClinicalImpression = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iClinicalImpression = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            ClinicalImpression clinicalImpression = (ClinicalImpression) xmlP.parse(iClinicalImpression);
            iClinicalImpression.close();

			/*
             * Create new Resourcemetadata objects for each ClinicalImpression metadata value and add to the resourcemetadataList
			 */

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, clinicalImpression, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// assessor : reference
			if (clinicalImpression.hasAssessor() && clinicalImpression.getAssessor().hasReference()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "assessor", 0, clinicalImpression.getAssessor(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// date : date
			if (clinicalImpression.hasDate()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(clinicalImpression.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(clinicalImpression.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rMetadata);
			}

			// encounter : reference
			if (clinicalImpression.hasEncounter()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "encounter", 0, clinicalImpression.getEncounter(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// finding : token
			if (clinicalImpression.hasFinding()) {

				for (ClinicalImpressionFindingComponent finding : clinicalImpression.getFinding()) {

					if (finding.hasItemCodeableConcept() && finding.getItemCodeableConcept().hasCoding()) {

						for (Coding code : finding.getItemCodeableConcept().getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"finding-code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
					else if (finding.hasItemReference()) {
						rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "finding-ref", 0, finding.getItemReference(), null);
						resourcemetadataList.addAll(rMetadataChain);
					}
				}
			}

			// identifier : token
			if (clinicalImpression.hasIdentifier()) {

				for (Identifier identifier : clinicalImpression.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}

			// investigation : reference
			if (clinicalImpression.hasInvestigation()) {

				for (ClinicalImpressionInvestigationComponent investigation : clinicalImpression.getInvestigation()) {
					if (investigation.hasItem()) {

						for (Reference item : investigation.getItem()) {
							rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "investigation", 0, item, null);
							resourcemetadataList.addAll(rMetadataChain);
						}
					}
				}
			}

			// subject : reference
			if (clinicalImpression.hasSubject() && clinicalImpression.getSubject().hasReference()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "subject", 0, clinicalImpression.getSubject(), null);
				resourcemetadataList.addAll(rMetadataChain);

				// patient : reference
				if ((clinicalImpression.getSubject().hasReference() && clinicalImpression.getSubject().getReference().indexOf("Patient") >= 0)
						|| (clinicalImpression.getSubject().hasType() && clinicalImpression.getSubject().getType().equals("Patient"))) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "patient", 0, clinicalImpression.getSubject(), null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// previous : reference
			if (clinicalImpression.hasPrevious()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "previous", 0, clinicalImpression.getPrevious(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// problem : reference
			if (clinicalImpression.hasProblem()) {

				for (Reference problem : clinicalImpression.getProblem()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "problem", 0, problem, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// status : token
			if (clinicalImpression.hasStatus() && clinicalImpression.getStatus() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", clinicalImpression.getStatus().toCode(), clinicalImpression.getStatus().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// supporting-info : reference
			if (clinicalImpression.hasSupportingInfo()) {

				for (Reference supportingInfo : clinicalImpression.getSupportingInfo()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "supporting-info", 0, supportingInfo, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		} finally {
	        rMetadata = null;
	        rMetadataChain = null;
            if (iClinicalImpression != null) {
                try {
                	iClinicalImpression.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
