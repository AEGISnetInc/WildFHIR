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
import org.hl7.fhir.r4.model.ConceptMap;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.UsageContext;
import org.hl7.fhir.r4.model.ConceptMap.ConceptMapGroupComponent;
import org.hl7.fhir.r4.model.ConceptMap.OtherElementComponent;
import org.hl7.fhir.r4.model.ConceptMap.SourceElementComponent;
import org.hl7.fhir.r4.model.ConceptMap.TargetElementComponent;

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataConceptMap extends ResourcemetadataProxy {

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
        ByteArrayInputStream iConceptMap = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

		try {
			// Extract and convert the resource contents to a ConceptMap object
			if (chainedResource != null) {
				iConceptMap = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iConceptMap = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			ConceptMap conceptMap = (ConceptMap) xmlP.parse(iConceptMap);
			iConceptMap.close();

			/*
			 * Create new Resourcemetadata objects for each ConceptMap metadata value and add to the resourcemetadataList
			 */

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, conceptMap, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// context-type-[x] : composite
			StringBuilder conextTypeComposite = new StringBuilder("");

			// context
			if (conceptMap.hasUseContext()) {

				for (UsageContext context : conceptMap.getUseContext()) {

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

			// source-system : token
			// target-system : token
			// source-code : token
			// target-code : token
			// dependson : uri
			// product : uri
			// other : uri
			if (conceptMap.hasGroup()) {

				for (ConceptMapGroupComponent group : conceptMap.getGroup()) {

					// source-system : uri
					if (group.hasSource()) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"source-system", group.getSource());
						resourcemetadataList.add(rMetadata);
					}

					// target-system : uri
					if (group.hasTarget()) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"target-system", group.getTarget());
						resourcemetadataList.add(rMetadata);
					}

					if (group.hasElement()) {

						for (SourceElementComponent element : group.getElement()) {

							// source-code : token
							if (element.hasCode()) {
								rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"source-code", element.getCode());
								resourcemetadataList.add(rMetadata);
							}

							if (element.hasTarget()) {

								for (TargetElementComponent target : element.getTarget()) {

									// target-code : token
									if (target.hasCode()) {
										rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"target-code", target.getCode());
										resourcemetadataList.add(rMetadata);
									}

									if (target.hasDependsOn()) {

										for (OtherElementComponent dependson : target.getDependsOn()) {

											// dependson : uri
											if (dependson.hasProperty()) {
												rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"dependson", dependson.getProperty());
												resourcemetadataList.add(rMetadata);
											}
										}
									}

									if (target.hasProduct()) {

										for (OtherElementComponent product : target.getProduct()) {

											// product : uri
											if (product.hasProperty()) {
												rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"product", product.getProperty());
												resourcemetadataList.add(rMetadata);
											}
										}
									}

								}

							}

						}
					}

					if (group.hasUnmapped() && group.getUnmapped().hasUrl()) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"other", group.getUnmapped().getUrl());
						resourcemetadataList.add(rMetadata);
					}
				}
			}

			// date : date
			if (conceptMap.hasDate()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(conceptMap.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(conceptMap.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rMetadata);
			}

			// description : string
			if (conceptMap.hasDescription()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"description", conceptMap.getDescription());
				resourcemetadataList.add(rMetadata);
			}

			// identifier : token
			if (conceptMap.hasIdentifier()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", conceptMap.getIdentifier().getValue(), conceptMap.getIdentifier().getSystem(), null, ServicesUtil.INSTANCE.getTextValue(conceptMap.getIdentifier()));
				resourcemetadataList.add(rMetadata);
			}

			// jurisdiction : token
			if (conceptMap.hasJurisdiction()) {

				for (CodeableConcept jurisdiction : conceptMap.getJurisdiction()) {

					if (jurisdiction.hasCoding()) {
						for (Coding code : jurisdiction.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"jurisdiction", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// name : string
			if (conceptMap.hasName()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"name", conceptMap.getName());
				resourcemetadataList.add(rMetadata);
			}

			// publisher : string
			if (conceptMap.hasPublisher()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"publisher", conceptMap.getPublisher());
				resourcemetadataList.add(rMetadata);
			}

			// source : reference - source is a Canonical, no Reference.identifier
			if (conceptMap.hasSourceCanonicalType()) {
				String sourceReference = generateFullLocalReference(conceptMap.getSourceCanonicalType().getValue(), baseUrl);

				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"source", sourceReference);
				resourcemetadataList.add(rMetadata);

				if (chainedResource == null) {
					// Add chained parameters
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "source", 0, conceptMap.getSourceCanonicalType().getValue(), null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// source-uri : uri
			if (conceptMap.hasSourceUriType()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"source-uri", conceptMap.getSourceUriType().getValue());
				resourcemetadataList.add(rMetadata);
			}

			// status : token
			if (conceptMap.hasStatus() && conceptMap.getStatus() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", conceptMap.getStatus().toCode(), conceptMap.getStatus().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// target : reference - target is a Canonical, no Reference.identifier
			if (conceptMap.hasTargetCanonicalType()) {
				String targetReference = generateFullLocalReference(conceptMap.getTargetCanonicalType().getValue(), baseUrl);

				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"target", targetReference);
				resourcemetadataList.add(rMetadata);

				if (chainedResource == null) {
					// Add chained parameters
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "target", 0, conceptMap.getTargetCanonicalType().getValue(), null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// target-uri : uri
			if (conceptMap.hasTargetUriType()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"target-uri", conceptMap.getTargetUriType().getValue());
				resourcemetadataList.add(rMetadata);
			}

			// title : string
			if (conceptMap.hasTitle()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"title", conceptMap.getTitle());
				resourcemetadataList.add(rMetadata);
			}

			// url : uri
			if (conceptMap.hasUrl()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"url", conceptMap.getUrl());
				resourcemetadataList.add(rMetadata);
			}

			// version : token
			if (conceptMap.hasVersion()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"version", conceptMap.getVersion());
				resourcemetadataList.add(rMetadata);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		} finally {
	        rMetadata = null;
	        rMetadataChain = null;
            if (iConceptMap != null) {
                try {
                	iConceptMap.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
