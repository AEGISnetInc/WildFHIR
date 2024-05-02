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
import org.hl7.fhir.r4.model.OperationDefinition;
import org.hl7.fhir.r4.model.Quantity;
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
public class ResourcemetadataOperationDefinition extends ResourcemetadataProxy {

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
        ByteArrayInputStream iOperationDefinition = null;

		try {
            // Extract and convert the resource contents to a OperationDefinition object
			if (chainedResource != null) {
				iOperationDefinition = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iOperationDefinition = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            OperationDefinition operationDefinition = (OperationDefinition) xmlP.parse(iOperationDefinition);
            iOperationDefinition.close();

			/*
             * Create new Resourcemetadata objects for each OperationDefinition metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, operationDefinition, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", operationDefinition.getId());
			resourcemetadataList.add(_id);

			// _language : token
			if (operationDefinition.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", operationDefinition.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (operationDefinition.getMeta() != null && operationDefinition.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(operationDefinition.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(operationDefinition.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// base : reference
			if (operationDefinition.hasBase()) {
				Resourcemetadata rBase = generateResourcemetadata(resource, chainedResource, chainedParameter+"base", generateFullLocalReference(operationDefinition.getBase(), baseUrl));
				resourcemetadataList.add(rBase);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rBaseChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "base", 0, operationDefinition.getBase());
					resourcemetadataList.addAll(rBaseChain);
				}
			}

			// code : token
			if (operationDefinition.hasCode()) {
				Resourcemetadata rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"code", operationDefinition.getCode());
				resourcemetadataList.add(rCode);
			}

			// context-type-[x] : composite
			StringBuilder conextTypeComposite = new StringBuilder("");

			// context
			if (operationDefinition.hasUseContext()) {

				for (UsageContext context : operationDefinition.getUseContext()) {

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
			if (operationDefinition.hasDate()) {
				Resourcemetadata rDate = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(operationDefinition.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(operationDefinition.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rDate);
			}

			// description : string
			if (operationDefinition.hasDescription()) {
				Resourcemetadata rDescription = generateResourcemetadata(resource, chainedResource, chainedParameter+"description", operationDefinition.getDescription());
				resourcemetadataList.add(rDescription);
			}

			// input-profile : reference
			if (operationDefinition.hasInputProfile()) {
				Resourcemetadata rInputProfile = generateResourcemetadata(resource, chainedResource, chainedParameter+"input-profile", generateFullLocalReference(operationDefinition.getInputProfile(), baseUrl));
				resourcemetadataList.add(rInputProfile);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rInputProfileChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "input-profile", 0, operationDefinition.getInputProfile());
					resourcemetadataList.addAll(rInputProfileChain);
				}
			}

			// instance : token
			if (operationDefinition.hasInstance()) {
				Resourcemetadata rInstance = generateResourcemetadata(resource, chainedResource, chainedParameter+"instance", operationDefinition.getInstanceElement().getValue().toString());
				resourcemetadataList.add(rInstance);
			}

			// jurisdiction : token
			if (operationDefinition.hasJurisdiction()) {

				Resourcemetadata rCode = null;
				for (CodeableConcept jurisdiction : operationDefinition.getJurisdiction()) {

					if (jurisdiction.hasCoding()) {
						for (Coding code : jurisdiction.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"jurisdiction", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// kind : token
			if (operationDefinition.hasKind() && operationDefinition.getKind() != null) {
				Resourcemetadata rKind = generateResourcemetadata(resource, chainedResource, chainedParameter+"kind", operationDefinition.getKind().toCode(), operationDefinition.getKind().getSystem());
				resourcemetadataList.add(rKind);
			}

			// name : string
			if (operationDefinition.hasName()) {
				Resourcemetadata rName = generateResourcemetadata(resource, chainedResource, chainedParameter+"name", operationDefinition.getName());
				resourcemetadataList.add(rName);
			}

			// output-profile : reference
			if (operationDefinition.hasOutputProfile()) {
				Resourcemetadata rOutputProfile = generateResourcemetadata(resource, chainedResource, chainedParameter+"output-profile", generateFullLocalReference(operationDefinition.getOutputProfile(), baseUrl));
				resourcemetadataList.add(rOutputProfile);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rOutputProfileChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "output-profile", 0, operationDefinition.getOutputProfile());
					resourcemetadataList.addAll(rOutputProfileChain);
				}
			}

			// publisher : token
			if (operationDefinition.hasPublisher()) {
				Resourcemetadata rPublisher = generateResourcemetadata(resource, chainedResource, chainedParameter+"publisher", operationDefinition.getPublisher());
				resourcemetadataList.add(rPublisher);
			}

			// status : token
			if (operationDefinition.hasStatus() && operationDefinition.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", operationDefinition.getStatus().toCode(), operationDefinition.getStatus().getSystem());
				resourcemetadataList.add(rStatus);
			}

			// system : token
			if (operationDefinition.hasSystem()) {
				Resourcemetadata rSystem = generateResourcemetadata(resource, chainedResource, chainedParameter+"system", operationDefinition.getSystemElement().getValue().toString());
				resourcemetadataList.add(rSystem);
			}

			// title : string
			if (operationDefinition.hasTitle()) {
				Resourcemetadata rTitle = generateResourcemetadata(resource, chainedResource, chainedParameter+"title", operationDefinition.getTitle());
				resourcemetadataList.add(rTitle);
			}

			// type : token
			if (operationDefinition.hasType()) {
				Resourcemetadata rType = generateResourcemetadata(resource, chainedResource, chainedParameter+"type", operationDefinition.getTypeElement().getValueAsString());
				resourcemetadataList.add(rType);
			}

			// url : uri
			if (operationDefinition.hasUrl()) {
				Resourcemetadata rUrl = generateResourcemetadata(resource, chainedResource, chainedParameter+"url", operationDefinition.getUrl());
				resourcemetadataList.add(rUrl);
			}

			// version : token
			if (operationDefinition.hasVersion()) {
				Resourcemetadata rVersion = generateResourcemetadata(resource, chainedResource, chainedParameter+"version", operationDefinition.getVersion());
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
