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
        ByteArrayInputStream iConceptMap = null;

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

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, conceptMap, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", conceptMap.getId());
			resourcemetadataList.add(_id);

			// _language : token
			if (conceptMap.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", conceptMap.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (conceptMap.getMeta() != null && conceptMap.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(conceptMap.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(conceptMap.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// context-type-[x] : composite
			StringBuilder conextTypeComposite = new StringBuilder("");

			// context
			if (conceptMap.hasUseContext()) {

				for (UsageContext context : conceptMap.getUseContext()) {

					// context-type : token
					Resourcemetadata rContextType = generateResourcemetadata(resource, chainedResource, chainedParameter+"context-type", context.getCode().getCode(), context.getCode().getSystem());
					resourcemetadataList.add(rContextType);

					// Start building the context-type-[x] composite
					if (context.getCode().hasSystem()) {
						conextTypeComposite.append(context.getCode().getSystem());
					}
					conextTypeComposite.append("|").append(context.getCode().getCode());

					if (context.hasValueCodeableConcept() && context.getValueCodeableConcept().hasCoding()) {
						// context : token
						Resourcemetadata rCode = null;
						for (Coding code : context.getValueCodeableConcept().getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"context", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}

						// context-type-value : composite
						conextTypeComposite.append("$");
						if (context.getValueCodeableConcept().getCodingFirstRep().hasSystem()) {
							conextTypeComposite.append(context.getValueCodeableConcept().getCodingFirstRep().getSystem());
						}
						conextTypeComposite.append("|").append(context.getValueCodeableConcept().getCodingFirstRep().getCode());

						Resourcemetadata rContextTypeValue = generateResourcemetadata(resource, chainedResource, chainedParameter+"context-type-value", conextTypeComposite.toString());
						resourcemetadataList.add(rContextTypeValue);
					}

					if (context.hasValueQuantity()) {
						// context-quantity : quantity
						String quantityCode = (context.getValueQuantity().getCode() != null ? context.getValueQuantity().getCode() : context.getValueQuantity().getUnit());
						Resourcemetadata rContextQuantity = generateResourcemetadata(resource, chainedResource, chainedParameter+"context-quantity", context.getValueQuantity().getValue().toPlainString(), context.getValueQuantity().getSystem(), quantityCode);
						resourcemetadataList.add(rContextQuantity);

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

						Resourcemetadata rContextTypeQuantity = generateResourcemetadata(resource, chainedResource, chainedParameter+"context-type-quantity", conextTypeComposite.toString());
						resourcemetadataList.add(rContextTypeQuantity);
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
							Resourcemetadata rContextQuantity = generateResourcemetadata(resource, chainedResource, chainedParameter+"context-quantity", rangeValue.getValue().toPlainString(), rangeValue.getSystem(), quantityCode);
							resourcemetadataList.add(rContextQuantity);

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

							Resourcemetadata rContextTypeQuantity = generateResourcemetadata(resource, chainedResource, chainedParameter+"context-type-quantity", conextTypeComposite.toString());
							resourcemetadataList.add(rContextTypeQuantity);
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
						Resourcemetadata rSourceSystem = generateResourcemetadata(resource, chainedResource, chainedParameter+"source-system", group.getSource());
						resourcemetadataList.add(rSourceSystem);
					}

					// target-system : uri
					if (group.hasTarget()) {
						Resourcemetadata rTargetSystem = generateResourcemetadata(resource, chainedResource, chainedParameter+"target-system", group.getTarget());
						resourcemetadataList.add(rTargetSystem);
					}

					if (group.hasElement()) {

						for (SourceElementComponent element : group.getElement()) {

							// source-code : token
							if (element.hasCode()) {
								Resourcemetadata rSourceCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"source-code", element.getCode());
								resourcemetadataList.add(rSourceCode);
							}

							if (element.hasTarget()) {

								for (TargetElementComponent target : element.getTarget()) {

									// target-code : token
									if (target.hasCode()) {
										Resourcemetadata rTargetCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"target-code", target.getCode());
										resourcemetadataList.add(rTargetCode);
									}

									if (target.hasDependsOn()) {

										for (OtherElementComponent dependson : target.getDependsOn()) {

											// dependson : uri
											if (dependson.hasProperty()) {
												Resourcemetadata rDependsOn = generateResourcemetadata(resource, chainedResource, chainedParameter+"dependson", dependson.getProperty());
												resourcemetadataList.add(rDependsOn);
											}
										}
									}

									if (target.hasProduct()) {

										for (OtherElementComponent product : target.getProduct()) {

											// product : uri
											if (product.hasProperty()) {
												Resourcemetadata rProduct = generateResourcemetadata(resource, chainedResource, chainedParameter+"product", product.getProperty());
												resourcemetadataList.add(rProduct);
											}
										}
									}

								}

							}

						}
					}

					if (group.hasUnmapped() && group.getUnmapped().hasUrl()) {
						Resourcemetadata rOther = generateResourcemetadata(resource, chainedResource, chainedParameter+"other", group.getUnmapped().getUrl());
						resourcemetadataList.add(rOther);
					}
				}
			}

			// date : date
			if (conceptMap.hasDate()) {
				Resourcemetadata rDate = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(conceptMap.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(conceptMap.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rDate);
			}

			// description : string
			if (conceptMap.hasDescription()) {
				Resourcemetadata rDescription = generateResourcemetadata(resource, chainedResource, chainedParameter+"description", conceptMap.getDescription());
				resourcemetadataList.add(rDescription);
			}

			// identifier : token
			if (conceptMap.hasIdentifier()) {
				Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", conceptMap.getIdentifier().getValue(), conceptMap.getIdentifier().getSystem(), null, ServicesUtil.INSTANCE.getTextValue(conceptMap.getIdentifier()));
				resourcemetadataList.add(rIdentifier);
			}

			// jurisdiction : token
			if (conceptMap.hasJurisdiction()) {

				Resourcemetadata rCode = null;
				for (CodeableConcept jurisdiction : conceptMap.getJurisdiction()) {

					if (jurisdiction.hasCoding()) {
						for (Coding code : jurisdiction.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"jurisdiction", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// name : string
			if (conceptMap.hasName()) {
				Resourcemetadata rName = generateResourcemetadata(resource, chainedResource, chainedParameter+"name", conceptMap.getName());
				resourcemetadataList.add(rName);
			}

			// publisher : string
			if (conceptMap.hasPublisher()) {
				Resourcemetadata rPublisher = generateResourcemetadata(resource, chainedResource, chainedParameter+"publisher", conceptMap.getPublisher());
				resourcemetadataList.add(rPublisher);
			}

			// source : reference
			if (conceptMap.hasSourceCanonicalType()) {
				String sourceReference = generateFullLocalReference(conceptMap.getSourceCanonicalType().getValue(), baseUrl);

				Resourcemetadata rSource = generateResourcemetadata(resource, chainedResource, chainedParameter+"source", sourceReference);
				resourcemetadataList.add(rSource);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rSourceChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "source", 0, conceptMap.getSourceCanonicalType().getValue());
					resourcemetadataList.addAll(rSourceChain);
				}
			}

			// source-uri : uri
			if (conceptMap.hasSourceUriType()) {
				Resourcemetadata rSourceUri = generateResourcemetadata(resource, chainedResource, chainedParameter+"source-uri", conceptMap.getSourceUriType().getValue());
				resourcemetadataList.add(rSourceUri);
			}

			// status : token
			if (conceptMap.hasStatus() && conceptMap.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", conceptMap.getStatus().toCode(), conceptMap.getStatus().getSystem());
				resourcemetadataList.add(rStatus);
			}

			// target : reference
			if (conceptMap.hasTargetCanonicalType()) {
				String targetReference = generateFullLocalReference(conceptMap.getTargetCanonicalType().getValue(), baseUrl);

				Resourcemetadata rTarget = generateResourcemetadata(resource, chainedResource, chainedParameter+"target", targetReference);
				resourcemetadataList.add(rTarget);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rTargetChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "target", 0, conceptMap.getTargetCanonicalType().getValue());
					resourcemetadataList.addAll(rTargetChain);
				}
			}

			// target-uri : uri
			if (conceptMap.hasTargetUriType()) {
				Resourcemetadata rTargetUri = generateResourcemetadata(resource, chainedResource, chainedParameter+"target-uri", conceptMap.getTargetUriType().getValue());
				resourcemetadataList.add(rTargetUri);
			}

			// title : string
			if (conceptMap.hasTitle()) {
				Resourcemetadata rTitle = generateResourcemetadata(resource, chainedResource, chainedParameter+"title", conceptMap.getTitle());
				resourcemetadataList.add(rTitle);
			}

			// url : uri
			if (conceptMap.hasUrl()) {
				Resourcemetadata rUrl = generateResourcemetadata(resource, chainedResource, chainedParameter+"url", conceptMap.getUrl());
				resourcemetadataList.add(rUrl);
			}

			// version : token
			if (conceptMap.hasVersion()) {
				Resourcemetadata rVersion = generateResourcemetadata(resource, chainedResource, chainedParameter+"version", conceptMap.getVersion());
				resourcemetadataList.add(rVersion);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return resourcemetadataList;
	}

}
