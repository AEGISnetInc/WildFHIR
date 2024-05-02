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
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.PlanDefinition;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.PlanDefinition.PlanDefinitionActionComponent;
import org.hl7.fhir.r4.model.RelatedArtifact;
import org.hl7.fhir.r4.model.UsageContext;
import org.hl7.fhir.r4.model.RelatedArtifact.RelatedArtifactType;

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataPlanDefinition extends ResourcemetadataProxy {

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
        ByteArrayInputStream iPlanDefinition = null;

		try {
			// Extract and convert the resource contents to a PlanDefinition object
			if (chainedResource != null) {
				iPlanDefinition = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iPlanDefinition = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			PlanDefinition planDefinition = (PlanDefinition) xmlP.parse(iPlanDefinition);
			iPlanDefinition.close();

			/*
			 * Create new Resourcemetadata objects for each PlanDefinition metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, planDefinition, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", planDefinition.getId());
			resourcemetadataList.add(_id);

			// _language : token
			if (planDefinition.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", planDefinition.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (planDefinition.getMeta() != null && planDefinition.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(planDefinition.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(planDefinition.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// context-type-[x] : composite
			StringBuilder conextTypeComposite = new StringBuilder("");

			// context
			if (planDefinition.hasUseContext()) {

				for (UsageContext context : planDefinition.getUseContext()) {

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

			// date : date
			if (planDefinition.hasDate()) {
				Resourcemetadata rDate = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(planDefinition.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(planDefinition.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rDate);
			}

			// definition : reference
			if (planDefinition.hasAction()) {

				Resourcemetadata rDefinition = null;
				List<Resourcemetadata> rDefinitionChain = null;
				for (PlanDefinitionActionComponent action : planDefinition.getAction()) {

					if (action.hasDefinitionCanonicalType()) {
						rDefinition = generateResourcemetadata(resource, chainedResource, chainedParameter+"definition", generateFullLocalReference(action.getDefinitionCanonicalType().getValue(), baseUrl));
						resourcemetadataList.add(rDefinition);

						if (chainedResource == null) {
							// Add chained parameters
							rDefinitionChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "definition", 0, action.getDefinitionCanonicalType().getValue());
							resourcemetadataList.addAll(rDefinitionChain);
						}
					}
				}
			}

			// description : string
			if (planDefinition.hasDescription()) {
				Resourcemetadata rDescription = generateResourcemetadata(resource, chainedResource, chainedParameter+"description", planDefinition.getDescription());
				resourcemetadataList.add(rDescription);
			}

			// effective : date(period)
			if (planDefinition.hasEffectivePeriod()) {
				Resourcemetadata rEffective = generateResourcemetadata(resource, chainedResource, chainedParameter+"effective", utcDateUtil.formatDate(planDefinition.getEffectivePeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(planDefinition.getEffectivePeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(planDefinition.getEffectivePeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(planDefinition.getEffectivePeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
				resourcemetadataList.add(rEffective);
			}

			// identifier : token
			if (planDefinition.hasIdentifier()) {

				for (Identifier identifier : planDefinition.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// jurisdiction : token
			if (planDefinition.hasJurisdiction()) {

				Resourcemetadata rCode = null;
				for (CodeableConcept jurisdiction : planDefinition.getJurisdiction()) {

					if (jurisdiction.hasCoding()) {
						for (Coding code : jurisdiction.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"jurisdiction", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// name : string
			if (planDefinition.hasName()) {
				Resourcemetadata rName = generateResourcemetadata(resource, chainedResource, chainedParameter+"name", planDefinition.getName());
				resourcemetadataList.add(rName);
			}

			// publisher : string
			if (planDefinition.hasPublisher()) {
				Resourcemetadata rPublisher = generateResourcemetadata(resource, chainedResource, chainedParameter+"publisher", planDefinition.getPublisher());
				resourcemetadataList.add(rPublisher);
			}

			// status : token
			if (planDefinition.hasStatus() && planDefinition.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", planDefinition.getStatus().toCode(), planDefinition.getStatus().getSystem());
				resourcemetadataList.add(rStatus);
			}

			// title : string
			if (planDefinition.hasTitle()) {
				Resourcemetadata rTitle = generateResourcemetadata(resource, chainedResource, chainedParameter+"title", planDefinition.getTitle());
				resourcemetadataList.add(rTitle);
			}

			// topic : token
			if (planDefinition.hasTopic()) {

				for (CodeableConcept topic : planDefinition.getTopic()) {

					if (topic.hasCoding()) {
						Resourcemetadata rTopic = generateResourcemetadata(resource, chainedResource, chainedParameter+"topic", topic.getCoding().get(0).getCode(), topic.getCoding().get(0).getSystem(), null, ServicesUtil.INSTANCE.getTextValue(topic));
						resourcemetadataList.add(rTopic);
					}
				}
			}

			// type : token
			if (planDefinition.hasType() && planDefinition.getType().hasCoding()) {

				Resourcemetadata rType = null;
				for (Coding type : planDefinition.getType().getCoding()) {
					rType = generateResourcemetadata(resource, chainedResource, chainedParameter+"type", type.getCode(), type.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(type));
					resourcemetadataList.add(rType);
				}
			}

			// url : uri
			if (planDefinition.hasUrl()) {
				Resourcemetadata rUrl = generateResourcemetadata(resource, chainedResource, chainedParameter+"url", planDefinition.getUrl());
				resourcemetadataList.add(rUrl);
			}

			// version : string
			if (planDefinition.hasVersion()) {
				Resourcemetadata rVersion = generateResourcemetadata(resource, chainedResource, chainedParameter+"version", planDefinition.getVersion());
				resourcemetadataList.add(rVersion);
			}

			// relatedArtifact
			if (planDefinition.hasRelatedArtifact()) {

				String relatedArtifactReference = null;
				Resourcemetadata rRelatedArtifact = null;
				for (RelatedArtifact relatedArtifact : planDefinition.getRelatedArtifact()) {

					if (relatedArtifact.hasResource()) {

						relatedArtifactReference = generateFullLocalReference(relatedArtifact.getResource(), baseUrl);

						RelatedArtifactType relatedType = relatedArtifact.getType();

						switch(relatedType) {

						// composed-of : reference
						case COMPOSEDOF :

							rRelatedArtifact = generateResourcemetadata(resource, chainedResource, chainedParameter+"composed-of", relatedArtifactReference);
							resourcemetadataList.add(rRelatedArtifact);

							if (chainedResource == null) {
								// Add chained parameters for any
								List<Resourcemetadata> rRelatedArtifactChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "composed-of", 0, relatedArtifact.getResource());
								resourcemetadataList.addAll(rRelatedArtifactChain);
							}
							break;

						// depends-on : reference
						case DEPENDSON :

							rRelatedArtifact = generateResourcemetadata(resource, chainedResource, chainedParameter+"depends-on", relatedArtifactReference);
							resourcemetadataList.add(rRelatedArtifact);

							if (chainedResource == null) {
								// Add chained parameters for any
								List<Resourcemetadata> rRelatedArtifactChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "depends-on", 0, relatedArtifact.getResource());
								resourcemetadataList.addAll(rRelatedArtifactChain);
							}
							break;

						// derived-from : reference
						case DERIVEDFROM :

							rRelatedArtifact = generateResourcemetadata(resource, chainedResource, chainedParameter+"derived-from", relatedArtifactReference);
							resourcemetadataList.add(rRelatedArtifact);

							if (chainedResource == null) {
								// Add chained parameters for any
								List<Resourcemetadata> rRelatedArtifactChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "derived-from", 0, relatedArtifact.getResource());
								resourcemetadataList.addAll(rRelatedArtifactChain);
							}
							break;

						// predecessor : reference
						case PREDECESSOR :

							rRelatedArtifact = generateResourcemetadata(resource, chainedResource, chainedParameter+"predecessor", relatedArtifactReference);
							resourcemetadataList.add(rRelatedArtifact);

							if (chainedResource == null) {
								// Add chained parameters for any
								List<Resourcemetadata> rRelatedArtifactChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "predecessor", 0, relatedArtifact.getResource());
								resourcemetadataList.addAll(rRelatedArtifactChain);
							}
							break;

						// successor : reference
						case SUCCESSOR :

							rRelatedArtifact = generateResourcemetadata(resource, chainedResource, chainedParameter+"successor", relatedArtifactReference);
							resourcemetadataList.add(rRelatedArtifact);

							if (chainedResource == null) {
								// Add chained parameters for any
								List<Resourcemetadata> rRelatedArtifactChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "successor", 0, relatedArtifact.getResource());
								resourcemetadataList.addAll(rRelatedArtifactChain);
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
		}

		return resourcemetadataList;
	}

}
