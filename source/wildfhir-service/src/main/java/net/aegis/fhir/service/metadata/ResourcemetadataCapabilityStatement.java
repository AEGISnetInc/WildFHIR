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
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestResourceComponent;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
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
public class ResourcemetadataCapabilityStatement extends ResourcemetadataProxy {

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
        ByteArrayInputStream iCapabilityStatement = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

		try {
			// Extract and convert the resource contents to a CapabilityStatement object
			if (chainedResource != null) {
				iCapabilityStatement = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iCapabilityStatement = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			CapabilityStatement capabilityStatement = (CapabilityStatement) xmlP.parse(iCapabilityStatement);
			iCapabilityStatement.close();

			/*
			 * Create new Resourcemetadata objects for each CapabilityStatement metadata value and add to the resourcemetadataList
			 */

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, capabilityStatement, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// context-type-[x] : composite
			StringBuilder conextTypeComposite = new StringBuilder("");

			// context
			if (capabilityStatement.hasUseContext()) {

				for (UsageContext context : capabilityStatement.getUseContext()) {

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
			if (capabilityStatement.hasDate()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(capabilityStatement.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(capabilityStatement.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rMetadata);
			}

			// description : string
			if (capabilityStatement.hasDescription()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"description", capabilityStatement.getDescription());
				resourcemetadataList.add(rMetadata);
			}

			// fhirversion : token
			if (capabilityStatement.hasVersion()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"fhirversion", capabilityStatement.getVersion());
				resourcemetadataList.add(rMetadata);
			}

			// format : token
			if (capabilityStatement.hasFormat()) {

				for (CodeType formatCode : capabilityStatement.getFormat()) {

					if (formatCode.hasValue()) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"format", formatCode.getValue());
						resourcemetadataList.add(rMetadata);
					}
				}
			}

			// guide : canonical
			if (capabilityStatement.hasImplementationGuide()) {

				for (CanonicalType guide : capabilityStatement.getImplementationGuide()) {

					if (guide.hasValue()) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"guide", guide.getValue());
						resourcemetadataList.add(rMetadata);
					}
				}
			}

			// jurisdiction : token
			if (capabilityStatement.hasJurisdiction()) {

				for (CodeableConcept jurisdiction : capabilityStatement.getJurisdiction()) {

					if (jurisdiction.hasCoding()) {
						for (Coding code : jurisdiction.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"jurisdiction", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// mode : token
			// resource-profile : reference
			// resource : token
			// security-service : token
			// supported-profile : reference
			if (capabilityStatement.hasRest()) {

				for (CapabilityStatementRestComponent rest : capabilityStatement.getRest()) {

					// mode : token
					if (rest.hasMode() && rest.getMode() != null) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"mode", rest.getMode().toCode(), rest.getMode().getSystem());
						resourcemetadataList.add(rMetadata);
					}

					if (rest.hasResource()) {

						for (CapabilityStatementRestResourceComponent restResource : rest.getResource()) {

							// supported-profile : reference - supportedProfile is a Canonical, no Reference.identifier
							if (restResource.hasSupportedProfile()) {

								for (CanonicalType profile : restResource.getSupportedProfile()) {

									rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"supported-profile", profile.getValue());
									resourcemetadataList.add(rMetadata);

									if (chainedResource == null) {
										// Add chained parameters
										rMetadataChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "supported-profile", 0, profile.getValue(), null);
										resourcemetadataList.addAll(rMetadataChain);
									}
								}
							}

							// resource : token
							if (restResource.hasType()) {
								rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"resource", restResource.getType());
								resourcemetadataList.add(rMetadata);
							}

							// resource-profile : reference - profile is a Canonical, no Reference.identifier
							if (restResource.hasProfile()) {
								rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"resource-profile", restResource.getProfile());
								resourcemetadataList.add(rMetadata);

								if (chainedResource == null) {
									// Add chained parameters
									rMetadataChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "resource-profile", 0, restResource.getProfile(), null);
									resourcemetadataList.addAll(rMetadataChain);
								}
							}
						}
					}

					// security-service : token
					if (rest.hasSecurity()) {

						for (CodeableConcept service : rest.getSecurity().getService()) {
							if (service.hasCoding()) {

								for (Coding code : service.getCoding()) {
									rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"security-service", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
									resourcemetadataList.add(rMetadata);
								}
							}
						}
					}
				}
			}

			// name : string
			if (capabilityStatement.hasName()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"name", capabilityStatement.getName());
				resourcemetadataList.add(rMetadata);
			}

			// publisher : string
			if (capabilityStatement.hasPublisher()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"publisher", capabilityStatement.getPublisher());
				resourcemetadataList.add(rMetadata);
			}

			// software : string
			if (capabilityStatement.hasSoftware()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"software", capabilityStatement.getSoftware().getName());
				resourcemetadataList.add(rMetadata);
			}

			// status : token
			if (capabilityStatement.hasStatus() && capabilityStatement.getStatus() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", capabilityStatement.getStatus().toCode(), capabilityStatement.getStatus().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// url : uri
			if (capabilityStatement.hasUrl()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"url", capabilityStatement.getUrl());
				resourcemetadataList.add(rMetadata);
			}

			// version : token
			if (capabilityStatement.hasVersion()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"version", capabilityStatement.getVersion());
				resourcemetadataList.add(rMetadata);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
        } finally {
	        rMetadata = null;
	        rMetadataChain = null;
            if (iCapabilityStatement != null) {
                try {
                    iCapabilityStatement.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
