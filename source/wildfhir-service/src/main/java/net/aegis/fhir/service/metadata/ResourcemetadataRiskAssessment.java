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
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.RiskAssessment;
import org.hl7.fhir.r4.model.RiskAssessment.RiskAssessmentPredictionComponent;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataRiskAssessment extends ResourcemetadataProxy {

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
        ByteArrayInputStream iRiskAssessment = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

		try {
            // Extract and convert the resource contents to a RiskAssessment object
			if (chainedResource != null) {
				iRiskAssessment = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iRiskAssessment = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            RiskAssessment riskAssessment = (RiskAssessment) xmlP.parse(iRiskAssessment);
            iRiskAssessment.close();

			/*
             * Create new Resourcemetadata objects for each RiskAssessment metadata value and add to the resourcemetadataList
			 */

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, riskAssessment, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// condition : reference
			if (riskAssessment.hasCondition()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "condition", 0, riskAssessment.getCondition(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// date : datetime
			if (riskAssessment.hasOccurrenceDateTimeType()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(riskAssessment.getOccurrenceDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(riskAssessment.getOccurrenceDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rMetadata);
			}

			// encounter : reference
			if (riskAssessment.hasEncounter()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "encounter", 0, riskAssessment.getEncounter(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// identifier : token
			if (riskAssessment.hasIdentifier()) {

				for (Identifier identifier : riskAssessment.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}

			// method : token
			if (riskAssessment.hasMethod() && riskAssessment.getMethod().hasCoding()) {

				for (Coding code : riskAssessment.getMethod().getCoding()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"method", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rMetadata);
				}
			}

			// subject : reference
			if (riskAssessment.hasSubject() && riskAssessment.getSubject().hasReference()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "subject", 0, riskAssessment.getSubject(), null);
				resourcemetadataList.addAll(rMetadataChain);

				// patient : reference
				if ((riskAssessment.getSubject().hasReference() && riskAssessment.getSubject().getReference().indexOf("Patient") >= 0)
						|| (riskAssessment.getSubject().hasType() && riskAssessment.getSubject().getType().equals("Patient"))) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "patient", 0, riskAssessment.getSubject(), null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// performer : reference
			if (riskAssessment.hasPerformer()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "performer", 0, riskAssessment.getPerformer(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// probability : number
			// risk : token
			if (riskAssessment.hasPrediction()) {

				for (RiskAssessmentPredictionComponent prediction : riskAssessment.getPrediction()) {

					if (prediction.hasProbabilityDecimalType()) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"probability", prediction.getProbabilityDecimalType().getValueAsString());
						resourcemetadataList.add(rMetadata);
					}
					else if (prediction.hasProbabilityRange() && prediction.getProbabilityRange().hasHigh()) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"probability", prediction.getProbabilityRange().getHigh().primitiveValue());
						resourcemetadataList.add(rMetadata);
					}

					if (prediction.hasQualitativeRisk() && prediction.getQualitativeRisk().hasCoding()) {

						for (Coding code : prediction.getQualitativeRisk().getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"risk", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		} finally {
	        rMetadata = null;
	        rMetadataChain = null;
            if (iRiskAssessment != null) {
                try {
                	iRiskAssessment.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
