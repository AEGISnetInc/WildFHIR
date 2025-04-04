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

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.SearchParameter;
import org.hl7.fhir.r4.model.UsageContext;
import org.hl7.fhir.r4.model.SearchParameter.SearchParameterComponentComponent;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataSearchParameter extends ResourcemetadataProxy {

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
        ByteArrayInputStream iSearchParameter = null;

		try {
            // Extract and convert the resource contents to a SearchParameter object
			if (chainedResource != null) {
				iSearchParameter = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iSearchParameter = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            SearchParameter searchParameter = (SearchParameter) xmlP.parse(iSearchParameter);
            iSearchParameter.close();

			/*
             * Create new Resourcemetadata objects for each SearchParameter metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, searchParameter, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			if (searchParameter.getId() != null) {
				Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", searchParameter.getId());
				resourcemetadataList.add(_id);
			}

			// _language : token
			if (searchParameter.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", searchParameter.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (searchParameter.getMeta() != null && searchParameter.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(searchParameter.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(searchParameter.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// base : token
			if (searchParameter.hasBase()) {

				for (CodeType base : searchParameter.getBase()) {
					Resourcemetadata rBase = generateResourcemetadata(resource, chainedResource, chainedParameter+"base", base.getValue());
					resourcemetadataList.add(rBase);
				}
			}

			// code : token
			if (searchParameter.hasCode()) {
				Resourcemetadata rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"code", searchParameter.getCode());
				resourcemetadataList.add(rCode);
			}

			// component : reference
			if (searchParameter.hasComponent()) {

				List<Resourcemetadata> rComponentChain = null;
				for (SearchParameterComponentComponent component : searchParameter.getComponent()) {

					if (component.hasDefinition()) {
						Resourcemetadata rComponent = generateResourcemetadata(resource, chainedResource, chainedParameter+"component", generateFullLocalReference(component.getDefinition(), baseUrl));
						resourcemetadataList.add(rComponent);

						if (chainedResource == null) {
							// Add chained parameters
							rComponentChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "component", 0, component.getDefinition(), null);
							resourcemetadataList.addAll(rComponentChain);
						}
					}
				}
			}

			// context-type-[x] : composite
			StringBuilder conextTypeComposite = new StringBuilder("");

			// context
			if (searchParameter.hasUseContext()) {

				for (UsageContext context : searchParameter.getUseContext()) {

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

						Resourcemetadata rContextTypeValue = generateResourcemetadata(resource, chainedResource, chainedParameter+"context-type-value", conextTypeComposite.toString(), null, null, null, "COMPOSITE");
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

						Resourcemetadata rContextTypeQuantity = generateResourcemetadata(resource, chainedResource, chainedParameter+"context-type-quantity", conextTypeComposite.toString(), null, null, null, "COMPOSITE");
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

							Resourcemetadata rContextTypeQuantity = generateResourcemetadata(resource, chainedResource, chainedParameter+"context-type-quantity", conextTypeComposite.toString(), null, null, null, "COMPOSITE");
							resourcemetadataList.add(rContextTypeQuantity);
						}
					}
				}
			}

			// date : date
			if (searchParameter.hasDate()) {
				Resourcemetadata rDate = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(searchParameter.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(searchParameter.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rDate);
			}

			// derived-from : uri
			if (searchParameter.hasDerivedFrom()) {
				Resourcemetadata rDerivedFrom = generateResourcemetadata(resource, chainedResource, chainedParameter+"derived-from", searchParameter.getDerivedFrom());
				resourcemetadataList.add(rDerivedFrom);
			}

			// description : string
			if (searchParameter.hasDescription()) {
				Resourcemetadata rDescription = generateResourcemetadata(resource, chainedResource, chainedParameter+"description", searchParameter.getDescription());
				resourcemetadataList.add(rDescription);
			}

			// jurisdiction : token
			if (searchParameter.hasJurisdiction()) {

				Resourcemetadata rCode = null;
				for (CodeableConcept jurisdiction : searchParameter.getJurisdiction()) {

					if (jurisdiction.hasCoding()) {
						for (Coding code : jurisdiction.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"jurisdiction", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// name : string
			if (searchParameter.hasName()) {
				Resourcemetadata rName = generateResourcemetadata(resource, chainedResource, chainedParameter+"name", searchParameter.getName());
				resourcemetadataList.add(rName);
			}

			// publisher : string
			if (searchParameter.hasPublisher()) {
				Resourcemetadata rPublisher = generateResourcemetadata(resource, chainedResource, chainedParameter+"publisher", searchParameter.getPublisher());
				resourcemetadataList.add(rPublisher);
			}

			// status : token
			if (searchParameter.hasStatus() && searchParameter.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", searchParameter.getStatus().toCode(), searchParameter.getStatus().getSystem());
				resourcemetadataList.add(rStatus);
			}

			// target : token
			if (searchParameter.hasTarget()) {

				for (CodeType target : searchParameter.getTarget()) {
					Resourcemetadata rTarget = generateResourcemetadata(resource, chainedResource, chainedParameter+"target", target.asStringValue());
					resourcemetadataList.add(rTarget);
				}
			}

			// type : token
			if (searchParameter.hasType() && searchParameter.getType() != null) {
				Resourcemetadata rType = generateResourcemetadata(resource, chainedResource, chainedParameter+"type", searchParameter.getType().toCode(), searchParameter.getType().getSystem());
				resourcemetadataList.add(rType);
			}

			// url : uri
			if (searchParameter.hasUrl()) {
				Resourcemetadata rUrl = generateResourcemetadata(resource, chainedResource, chainedParameter+"url", searchParameter.getUrl());
				resourcemetadataList.add(rUrl);
			}

			// version : string
			if (searchParameter.hasVersion()) {
				Resourcemetadata rVersion = generateResourcemetadata(resource, chainedResource, chainedParameter+"version", searchParameter.getVersion());
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
