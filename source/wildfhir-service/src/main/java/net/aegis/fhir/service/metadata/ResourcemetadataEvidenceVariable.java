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
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.EvidenceVariable;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.RelatedArtifact;
import org.hl7.fhir.r4.model.RelatedArtifact.RelatedArtifactType;
import org.hl7.fhir.r4.model.UsageContext;

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataEvidenceVariable extends ResourcemetadataProxy {

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
        ByteArrayInputStream iEvidenceVariable = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

		try {
            // Extract and convert the resource contents to a EvidenceVariable object
			if (chainedResource != null) {
				iEvidenceVariable = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iEvidenceVariable = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            EvidenceVariable evidenceVariable = (EvidenceVariable) xmlP.parse(iEvidenceVariable);
            iEvidenceVariable.close();

			/*
             * Create new Resourcemetadata objects for each EvidenceVariable metadata value and add to the resourcemetadataList
			 */

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, evidenceVariable, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// context-type-[x] : composite
			StringBuilder conextTypeComposite = new StringBuilder("");

			// context
			if (evidenceVariable.hasUseContext()) {

				for (UsageContext context : evidenceVariable.getUseContext()) {

					// context-type : token
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"context-type", context.getCode().getCode(), context.getCode().getSystem());
					resourcemetadataList.add(rMetadata);

					// Start building the context-type-[x] composite
					if (context.getCode().hasSystem()) {
						conextTypeComposite.append(context.getCode().getSystem());
					}
					conextTypeComposite.append("|").append(context.getCode().getCode());

					if (context.hasValueCodeableConcept() && context.getValueCodeableConcept().hasCoding()) {
						// context : token
						for (Coding code : context.getValueCodeableConcept().getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"context", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}

						// context-type-value : composite
						conextTypeComposite.append("$");
						if (context.getValueCodeableConcept().getCodingFirstRep().hasSystem()) {
							conextTypeComposite.append(context.getValueCodeableConcept().getCodingFirstRep().getSystem());
						}
						conextTypeComposite.append("|").append(context.getValueCodeableConcept().getCodingFirstRep().getCode());

						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"context-type-value", conextTypeComposite.toString(), null, null, null, "COMPOSITE");
						resourcemetadataList.add(rMetadata);
					}

					if (context.hasValueQuantity()) {
						// context-quantity : quantity
						String quantityCode = (context.getValueQuantity().getCode() != null ? context.getValueQuantity().getCode() : context.getValueQuantity().getUnit());
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"context-quantity", context.getValueQuantity().getValue().toPlainString(), context.getValueQuantity().getSystem(), quantityCode);
						resourcemetadataList.add(rMetadata);

						// context-type-quantity : composite
						conextTypeComposite.append("$");
						if (context.getValueQuantity().hasValue()) {
							conextTypeComposite.append(context.getValueQuantity().getValue().toPlainString());
						}
						conextTypeComposite.append("|");
						if (context.getValueQuantity().hasSystem()) {
							conextTypeComposite.append(context.getValueQuantity().getSystem());
						}
						conextTypeComposite.append("|").append(quantityCode);

						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"context-type-quantity", conextTypeComposite.toString(), null, null, null, "COMPOSITE");
						resourcemetadataList.add(rMetadata);
					}

					if (context.hasValueRange()) {
						// context-quantity : range
						String quantityCode = "";
						Quantity rangeValue = null;
						if (context.getValueRange().hasLow()) {
							rangeValue = context.getValueRange().getLow();
						}
						else if (context.getValueRange().hasHigh()) {
							rangeValue = context.getValueRange().getHigh();
						}
						if (rangeValue != null) {
							quantityCode = (rangeValue.getCode() != null ? rangeValue.getCode() : rangeValue.getUnit());
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"context-quantity", rangeValue.getValue().toPlainString(), rangeValue.getSystem(), quantityCode);
							resourcemetadataList.add(rMetadata);

							// context-type-quantity : composite
							conextTypeComposite.append("$");
							if (rangeValue.hasValue()) {
								conextTypeComposite.append(rangeValue.getValue().toPlainString());
							}
							conextTypeComposite.append("|");
							if (rangeValue.hasSystem()) {
								conextTypeComposite.append(rangeValue.getSystem());
							}
							conextTypeComposite.append("|").append(quantityCode);

							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"context-type-quantity", conextTypeComposite.toString(), null, null, null, "COMPOSITE");
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// date : date
			if (evidenceVariable.hasDate()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(evidenceVariable.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(evidenceVariable.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rMetadata);
			}

			// description : string
			if (evidenceVariable.hasDescription()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"description", evidenceVariable.getDescription());
				resourcemetadataList.add(rMetadata);
			}

			// effective : date(period)
			if (evidenceVariable.hasEffectivePeriod()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"effective", utcDateUtil.formatDate(evidenceVariable.getEffectivePeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(evidenceVariable.getEffectivePeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(evidenceVariable.getEffectivePeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(evidenceVariable.getEffectivePeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
				resourcemetadataList.add(rMetadata);
			}

			// identifier : token
			if (evidenceVariable.hasIdentifier()) {

				for (Identifier identifier : evidenceVariable.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}

			// jurisdiction : token
			if (evidenceVariable.hasJurisdiction()) {
				for (CodeableConcept jurisdiction : evidenceVariable.getJurisdiction()) {

					if (jurisdiction.hasCoding()) {
						for (Coding code : jurisdiction.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"jurisdiction", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// name : string
			if (evidenceVariable.hasName()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"name", evidenceVariable.getName());
				resourcemetadataList.add(rMetadata);
			}

			// publisher : string
			if (evidenceVariable.hasPublisher()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"publisher", evidenceVariable.getPublisher());
				resourcemetadataList.add(rMetadata);
			}

			// status : token
			if (evidenceVariable.hasStatus() && evidenceVariable.getStatus() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", evidenceVariable.getStatus().toCode(), evidenceVariable.getStatus().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// title : string
			if (evidenceVariable.hasTitle()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"title", evidenceVariable.getTitle());
				resourcemetadataList.add(rMetadata);
			}

			// url : uri
			if (evidenceVariable.hasUrl()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"url", evidenceVariable.getUrl());
				resourcemetadataList.add(rMetadata);
			}

			// version : token
			if (evidenceVariable.hasVersion()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"version", evidenceVariable.getVersion());
				resourcemetadataList.add(rMetadata);
			}

			// relatedArtifact - RelatedArtifact.resource is a Canonical, no Reference.identifier
			if (evidenceVariable.hasRelatedArtifact()) {

				String relatedArtifactReference = null;
				for (RelatedArtifact relatedArtifact : evidenceVariable.getRelatedArtifact()) {

					if (relatedArtifact.hasType() && relatedArtifact.hasResource()) {
						relatedArtifactReference = generateFullLocalReference(relatedArtifact.getResource(), baseUrl);

						RelatedArtifactType type = relatedArtifact.getType();
						switch (type) {
						case COMPOSEDOF:
							// composed-of : reference
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"composed-of", relatedArtifactReference);
							resourcemetadataList.add(rMetadata);

							if (chainedResource == null) {
								// Add chained parameters for any
								rMetadataChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "composed-of", 0, relatedArtifact.getResource(), null);
								resourcemetadataList.addAll(rMetadataChain);
							}
							break;
						case DEPENDSON:
							// depends-on : reference
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"depends-on", relatedArtifactReference);
							resourcemetadataList.add(rMetadata);

							if (chainedResource == null) {
								// Add chained parameters for any
								rMetadataChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "depends-on", 0, relatedArtifact.getResource(), null);
								resourcemetadataList.addAll(rMetadataChain);
							}
							break;
						case DERIVEDFROM:
							// derived-from : reference
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"derived-from", relatedArtifactReference);
							resourcemetadataList.add(rMetadata);

							if (chainedResource == null) {
								// Add chained parameters for any
								rMetadataChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "derived-from", 0, relatedArtifact.getResource(), null);
								resourcemetadataList.addAll(rMetadataChain);
							}
							break;
						case PREDECESSOR:
							// predecessor : reference
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"predecessor", relatedArtifactReference);
							resourcemetadataList.add(rMetadata);

							if (chainedResource == null) {
								// Add chained parameters for any
								rMetadataChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "predecessor", 0, relatedArtifact.getResource(), null);
								resourcemetadataList.addAll(rMetadataChain);
							}
							break;
						case SUCCESSOR:
							// successor : reference
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"successor", relatedArtifactReference);
							resourcemetadataList.add(rMetadata);

							if (chainedResource == null) {
								// Add chained parameters for any
								rMetadataChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "successor", 0, relatedArtifact.getResource(), null);
								resourcemetadataList.addAll(rMetadataChain);
							}
							break;
						default:
							break;
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
            if (iEvidenceVariable != null) {
                try {
                	iEvidenceVariable.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
