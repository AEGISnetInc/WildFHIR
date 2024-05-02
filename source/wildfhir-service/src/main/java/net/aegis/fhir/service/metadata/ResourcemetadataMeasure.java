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
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.RelatedArtifact;
import org.hl7.fhir.r4.model.UsageContext;
import org.hl7.fhir.r4.model.RelatedArtifact.RelatedArtifactType;
import org.hl7.fhir.r4.model.Identifier;

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataMeasure extends ResourcemetadataProxy {

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
        ByteArrayInputStream iMeasure = null;

		try {
			// Extract and convert the resource contents to a Measure object
			if (chainedResource != null) {
				iMeasure = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iMeasure = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			Measure measure = (Measure) xmlP.parse(iMeasure);
			iMeasure.close();

			/*
			 * Create new Resourcemetadata objects for each Measure metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, measure, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", measure.getId());
			resourcemetadataList.add(_id);

			// _language : token
			if (measure.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", measure.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (measure.getMeta() != null && measure.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(measure.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(measure.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// context-type-[x] : composite
			StringBuilder conextTypeComposite = new StringBuilder("");

			// context
			if (measure.hasUseContext()) {

				for (UsageContext context : measure.getUseContext()) {

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
			if (measure.hasDate()) {
				Resourcemetadata rDate = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(measure.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(measure.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rDate);
			}

			// description : string
			if (measure.hasDescription()) {
				Resourcemetadata rDescription = generateResourcemetadata(resource, chainedResource, chainedParameter+"description", measure.getDescription());
				resourcemetadataList.add(rDescription);
			}

			// effective : date(period)
			if (measure.hasEffectivePeriod()) {
				Resourcemetadata rEffective = generateResourcemetadata(resource, chainedResource, chainedParameter+"effective", utcDateUtil.formatDate(measure.getEffectivePeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(measure.getEffectivePeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(measure.getEffectivePeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(measure.getEffectivePeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
				resourcemetadataList.add(rEffective);
			}

			// identifier : token
			if (measure.hasIdentifier()) {

				for (Identifier identifier : measure.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// jurisdiction : token
			if (measure.hasJurisdiction()) {

				Resourcemetadata rCode = null;
				for (CodeableConcept jurisdiction : measure.getJurisdiction()) {

					if (jurisdiction.hasCoding()) {
						for (Coding code : jurisdiction.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"jurisdiction", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// name : string
			if (measure.hasName()) {
				Resourcemetadata rName = generateResourcemetadata(resource, chainedResource, chainedParameter+"name", measure.getName());
				resourcemetadataList.add(rName);
			}

			// publisher : string
			if (measure.hasPublisher()) {
				Resourcemetadata rPublisher = generateResourcemetadata(resource, chainedResource, chainedParameter+"publisher", measure.getPublisher());
				resourcemetadataList.add(rPublisher);
			}

			// status : token
			if (measure.hasStatus() && measure.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", measure.getStatus().toCode(), measure.getStatus().getSystem());
				resourcemetadataList.add(rStatus);
			}

			// title : string
			if (measure.hasTitle()) {
				Resourcemetadata rTitle = generateResourcemetadata(resource, chainedResource, chainedParameter+"title", measure.getTitle());
				resourcemetadataList.add(rTitle);
			}

			// topic : token
			if (measure.hasTopic()) {

				Resourcemetadata rCode = null;
				for (CodeableConcept topic : measure.getTopic()) {

					if (topic.hasCoding()) {
						for (Coding code : topic.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"topic", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// url : uri
			if (measure.hasUrl()) {
				Resourcemetadata rUrl = generateResourcemetadata(resource, chainedResource, chainedParameter+"url", measure.getUrl());
				resourcemetadataList.add(rUrl);
			}

			// version : string
			if (measure.hasVersion()) {
				Resourcemetadata rVersion = generateResourcemetadata(resource, chainedResource, chainedParameter+"version", measure.getVersion());
				resourcemetadataList.add(rVersion);
			}

			// relatedArtifact
			if (measure.hasRelatedArtifact()) {

				String relatedArtifactReference = null;
				Resourcemetadata rRelatedArtifact = null;
				for (RelatedArtifact relatedArtifact : measure.getRelatedArtifact()) {

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
