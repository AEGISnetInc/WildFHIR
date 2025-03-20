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
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ElementDefinition;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.UsageContext;
import org.hl7.fhir.r4.model.StructureDefinition.StructureDefinitionContextComponent;

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataStructureDefinition extends ResourcemetadataProxy {

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
        ByteArrayInputStream iStructureDefinition = null;

		try {
            // Extract and convert the resource contents to a StructureDefinition object
			if (chainedResource != null) {
				iStructureDefinition = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iStructureDefinition = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            StructureDefinition structureDefinition = (StructureDefinition) xmlP.parse(iStructureDefinition);
            iStructureDefinition.close();

			/*
             * Create new Resourcemetadata objects for each StructureDefinition metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, structureDefinition, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			if (structureDefinition.getId() != null) {
				Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", structureDefinition.getId());
				resourcemetadataList.add(_id);
			}

			// _language : token
			if (structureDefinition.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", structureDefinition.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (structureDefinition.getMeta() != null && structureDefinition.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(structureDefinition.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(structureDefinition.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// abstract : token
			if (structureDefinition.hasAbstract()) {
				Resourcemetadata rAbstract = generateResourcemetadata(resource, chainedResource, chainedParameter+"abstract", structureDefinition.getAbstractElement().getValue().toString());
				resourcemetadataList.add(rAbstract);
			}

			// base : uri
			if (structureDefinition.hasBaseDefinition()) {
				Resourcemetadata rBase = generateResourcemetadata(resource, chainedResource, chainedParameter+"base", structureDefinition.getBaseDefinition());
				resourcemetadataList.add(rBase);
			}

			// context-type-[x] : composite
			StringBuilder conextTypeComposite = new StringBuilder("");

			// context
			if (structureDefinition.hasUseContext()) {

				for (UsageContext context : structureDefinition.getUseContext()) {

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
			if (structureDefinition.hasDate()) {
				Resourcemetadata rDate = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(structureDefinition.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(structureDefinition.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rDate);
			}

			// derivation : token
			if (structureDefinition.hasDerivation() && structureDefinition.getDerivation() != null) {
				Resourcemetadata rDerivation = generateResourcemetadata(resource, chainedResource, chainedParameter+"derivation", structureDefinition.getDerivation().toCode(), structureDefinition.getDerivation().getSystem());
				resourcemetadataList.add(rDerivation);
			}

			// description : string
			if (structureDefinition.hasDescription()) {
				Resourcemetadata rDescription = generateResourcemetadata(resource, chainedResource, chainedParameter+"description", structureDefinition.getDescription());
				resourcemetadataList.add(rDescription);
			}

			// experimental : token
			if (structureDefinition.hasExperimental()) {
				Resourcemetadata rExperimental = generateResourcemetadata(resource, chainedResource, chainedParameter+"experimental", structureDefinition.getExperimentalElement().asStringValue());
				resourcemetadataList.add(rExperimental);
			}

			// ext-context : string
			if (structureDefinition.hasContext()) {

				for (StructureDefinitionContextComponent context : structureDefinition.getContext()) {

					if (context.hasType() && context.getType() != null) {
						Resourcemetadata rContext = generateResourcemetadata(resource, chainedResource, chainedParameter+"ext-context", context.getType().toCode(), context.getType().getSystem());
						resourcemetadataList.add(rContext);
					}
				}
			}

			// identifier : token
			if (structureDefinition.hasIdentifier()) {

				for (Identifier identifier : structureDefinition.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// jurisdiction : token
			if (structureDefinition.hasJurisdiction()) {

				Resourcemetadata rCode = null;
				for (CodeableConcept jurisdiction : structureDefinition.getJurisdiction()) {

					if (jurisdiction.hasCoding()) {
						for (Coding code : jurisdiction.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"jurisdiction", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// keyword : token
			if (structureDefinition.hasKeyword()) {

				Resourcemetadata rCode = null;
				for (Coding code : structureDefinition.getKeyword()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"keyword", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);

				}
			}

			// kind : token
			if (structureDefinition.hasKind() && structureDefinition.getKind() != null) {
				Resourcemetadata rKind = generateResourcemetadata(resource, chainedResource, chainedParameter+"kind", structureDefinition.getKind().toCode(), structureDefinition.getKind().getSystem());
				resourcemetadataList.add(rKind);
			}

			// name : string
			if (structureDefinition.hasName()) {
				Resourcemetadata rName = generateResourcemetadata(resource, chainedResource, chainedParameter+"name", structureDefinition.getName());
				resourcemetadataList.add(rName);
			}

			// publisher : token
			if (structureDefinition.hasPublisher()) {
				Resourcemetadata rPublisher = generateResourcemetadata(resource, chainedResource, chainedParameter+"publisher", structureDefinition.getPublisher());
				resourcemetadataList.add(rPublisher);
			}

			// status : token
			if (structureDefinition.hasStatus() && structureDefinition.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", structureDefinition.getStatus().toCode(), structureDefinition.getStatus().getSystem());
				resourcemetadataList.add(rStatus);
			}

			// title : string
			if (structureDefinition.hasTitle()) {
				Resourcemetadata rTitle = generateResourcemetadata(resource, chainedResource, chainedParameter+"title", structureDefinition.getTitle());
				resourcemetadataList.add(rTitle);
			}

			// type : token
			if (structureDefinition.hasType()) {
				Resourcemetadata rType = generateResourcemetadata(resource, chainedResource, chainedParameter+"type", structureDefinition.getType());
				resourcemetadataList.add(rType);
			}

			// url : uri
			if (structureDefinition.hasUrl()) {
				Resourcemetadata rUrl = generateResourcemetadata(resource, chainedResource, chainedParameter+"url", structureDefinition.getUrl());
				resourcemetadataList.add(rUrl);
			}

			// version : token
			if (structureDefinition.hasVersion()) {
				Resourcemetadata rVersion = generateResourcemetadata(resource, chainedResource, chainedParameter+"version", structureDefinition.getVersion());
				resourcemetadataList.add(rVersion);
			}

			// Prevent duplicate 'base-path' and 'path' parameter entries
			HashSet<String> basePathList = new HashSet<String>();
			HashSet<String> pathList = new HashSet<String>();

			// base-path : token (continued)
			// path : token (continued)
			// valueset : reference
			if (structureDefinition.hasSnapshot()) {

				if (structureDefinition.getSnapshot().hasElement()) {

					for (ElementDefinition snapshotElement : structureDefinition.getSnapshot().getElement()) {

						if (snapshotElement.hasBase() && snapshotElement.getBase().hasPath() && !basePathList.contains(snapshotElement.getBase().getPath())) {
							Resourcemetadata rBasePath = generateResourcemetadata(resource, chainedResource, chainedParameter+"base-path", snapshotElement.getBase().getPath());
							resourcemetadataList.add(rBasePath);
							basePathList.add(snapshotElement.getBase().getPath());
						}

						if (snapshotElement.hasPath() && !basePathList.contains(snapshotElement.getPath())) {
							Resourcemetadata rPath = generateResourcemetadata(resource, chainedResource, chainedParameter+"path", snapshotElement.getPath());
							resourcemetadataList.add(rPath);
							pathList.add(snapshotElement.getPath());
						}

						if (snapshotElement.hasBinding() && snapshotElement.getBinding().hasValueSet()) {
							Resourcemetadata rValueSet = generateResourcemetadata(resource, chainedResource, chainedParameter+"valueset", snapshotElement.getBinding().getValueSet());
							resourcemetadataList.add(rValueSet);
						}
					}
				}
			}

			// base-path : token
			// path : token
			if (structureDefinition.hasDifferential()) {

				if (structureDefinition.getDifferential().hasElement()) {

					for (ElementDefinition differentialElement : structureDefinition.getDifferential().getElement()) {

						if (differentialElement.hasBase() && differentialElement.getBase().hasPath() && !basePathList.contains(differentialElement.getBase().getPath())) {
							Resourcemetadata rBasePath = generateResourcemetadata(resource, chainedResource, chainedParameter+"base-path", differentialElement.getBase().getPath());
							resourcemetadataList.add(rBasePath);
							basePathList.add(differentialElement.getBase().getPath());
						}

						if (differentialElement.hasPath() && !pathList.contains(differentialElement.getPath())) {
							Resourcemetadata rPath = generateResourcemetadata(resource, chainedResource, chainedParameter+"path", differentialElement.getPath());
							resourcemetadataList.add(rPath);
							pathList.add(differentialElement.getPath());
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
