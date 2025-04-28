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
        ByteArrayInputStream iOperationDefinition = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

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

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, operationDefinition, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// base : reference - base is a Canonical, no Reference.identifier
			if (operationDefinition.hasBase()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"base", generateFullLocalReference(operationDefinition.getBase(), baseUrl));
				resourcemetadataList.add(rMetadata);

				if (chainedResource == null) {
					// Add chained parameters
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "base", 0, operationDefinition.getBase(), null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// code : token
			if (operationDefinition.hasCode()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"code", operationDefinition.getCode());
				resourcemetadataList.add(rMetadata);
			}

			// context-type-[x] : composite
			StringBuilder conextTypeComposite = new StringBuilder("");

			// context
			if (operationDefinition.hasUseContext()) {

				for (UsageContext context : operationDefinition.getUseContext()) {

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
			if (operationDefinition.hasDate()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(operationDefinition.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(operationDefinition.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rMetadata);
			}

			// description : string
			if (operationDefinition.hasDescription()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"description", operationDefinition.getDescription());
				resourcemetadataList.add(rMetadata);
			}

			// input-profile : reference - input-profile is a Canonical, no Reference.identifier
			if (operationDefinition.hasInputProfile()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"input-profile", generateFullLocalReference(operationDefinition.getInputProfile(), baseUrl));
				resourcemetadataList.add(rMetadata);

				if (chainedResource == null) {
					// Add chained parameters
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "input-profile", 0, operationDefinition.getInputProfile(), null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// instance : token
			if (operationDefinition.hasInstance()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"instance", operationDefinition.getInstanceElement().getValue().toString());
				resourcemetadataList.add(rMetadata);
			}

			// jurisdiction : token
			if (operationDefinition.hasJurisdiction()) {
				for (CodeableConcept jurisdiction : operationDefinition.getJurisdiction()) {

					if (jurisdiction.hasCoding()) {
						for (Coding code : jurisdiction.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"jurisdiction", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// kind : token
			if (operationDefinition.hasKind() && operationDefinition.getKind() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"kind", operationDefinition.getKind().toCode(), operationDefinition.getKind().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// name : string
			if (operationDefinition.hasName()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"name", operationDefinition.getName());
				resourcemetadataList.add(rMetadata);
			}

			// output-profile : reference - output-profile is a Canonical, no Reference.identifier
			if (operationDefinition.hasOutputProfile()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"output-profile", generateFullLocalReference(operationDefinition.getOutputProfile(), baseUrl));
				resourcemetadataList.add(rMetadata);

				if (chainedResource == null) {
					// Add chained parameters
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "output-profile", 0, operationDefinition.getOutputProfile(), null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// publisher : token
			if (operationDefinition.hasPublisher()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"publisher", operationDefinition.getPublisher());
				resourcemetadataList.add(rMetadata);
			}

			// status : token
			if (operationDefinition.hasStatus() && operationDefinition.getStatus() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", operationDefinition.getStatus().toCode(), operationDefinition.getStatus().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// system : token
			if (operationDefinition.hasSystem()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"system", operationDefinition.getSystemElement().getValue().toString());
				resourcemetadataList.add(rMetadata);
			}

			// title : string
			if (operationDefinition.hasTitle()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"title", operationDefinition.getTitle());
				resourcemetadataList.add(rMetadata);
			}

			// type : token
			if (operationDefinition.hasType()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"type", operationDefinition.getTypeElement().getValueAsString());
				resourcemetadataList.add(rMetadata);
			}

			// url : uri
			if (operationDefinition.hasUrl()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"url", operationDefinition.getUrl());
				resourcemetadataList.add(rMetadata);
			}

			// version : token
			if (operationDefinition.hasVersion()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"version", operationDefinition.getVersion());
				resourcemetadataList.add(rMetadata);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		} finally {
	        rMetadata = null;
	        rMetadataChain = null;
            if (iOperationDefinition != null) {
                try {
                	iOperationDefinition.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
