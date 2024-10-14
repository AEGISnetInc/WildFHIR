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

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, capabilityStatement, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			if (capabilityStatement.getId() != null) {
				Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", capabilityStatement.getId());
				resourcemetadataList.add(_id);
			}

			// _language : token
			if (capabilityStatement.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", capabilityStatement.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (capabilityStatement.getMeta() != null && capabilityStatement.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(capabilityStatement.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(capabilityStatement.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// context-type-[x] : composite
			StringBuilder conextTypeComposite = new StringBuilder("");

			// context
			if (capabilityStatement.hasUseContext()) {

				for (UsageContext context : capabilityStatement.getUseContext()) {

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
			if (capabilityStatement.hasDate()) {
				Resourcemetadata rDate = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(capabilityStatement.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(capabilityStatement.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rDate);
			}

			// description : string
			if (capabilityStatement.hasDescription()) {
				Resourcemetadata rDescription = generateResourcemetadata(resource, chainedResource, chainedParameter+"description", capabilityStatement.getDescription());
				resourcemetadataList.add(rDescription);
			}

			// fhirversion : token
			if (capabilityStatement.hasVersion()) {
				Resourcemetadata rFhirversion = generateResourcemetadata(resource, chainedResource, chainedParameter+"fhirversion", capabilityStatement.getVersion());
				resourcemetadataList.add(rFhirversion);
			}

			// format : token
			if (capabilityStatement.hasFormat()) {

				for (CodeType formatCode : capabilityStatement.getFormat()) {

					if (formatCode.hasValue()) {
						Resourcemetadata rFormatCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"format", formatCode.getValue());
						resourcemetadataList.add(rFormatCode);
					}
				}
			}

			// guide : canonical
			if (capabilityStatement.hasImplementationGuide()) {

				for (CanonicalType guide : capabilityStatement.getImplementationGuide()) {

					if (guide.hasValue()) {
						Resourcemetadata rGuide = generateResourcemetadata(resource, chainedResource, chainedParameter+"guide", guide.getValue());
						resourcemetadataList.add(rGuide);
					}
				}
			}

			// jurisdiction : token
			if (capabilityStatement.hasJurisdiction()) {

				Resourcemetadata rCode = null;
				for (CodeableConcept jurisdiction : capabilityStatement.getJurisdiction()) {

					if (jurisdiction.hasCoding()) {
						for (Coding code : jurisdiction.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"jurisdiction", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
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
						Resourcemetadata rMode = generateResourcemetadata(resource, chainedResource, chainedParameter+"mode", rest.getMode().toCode(), rest.getMode().getSystem());
						resourcemetadataList.add(rMode);
					}

					if (rest.hasResource()) {

						for (CapabilityStatementRestResourceComponent restResource : rest.getResource()) {

							// supported-profile : reference
							if (restResource.hasSupportedProfile()) {

								for (CanonicalType profile : restResource.getSupportedProfile()) {

									Resourcemetadata rProfile = generateResourcemetadata(resource, chainedResource, chainedParameter+"supported-profile", profile.getValue());
									resourcemetadataList.add(rProfile);

									if (chainedResource == null) {
										// Add chained parameters
										List<Resourcemetadata> rSupportedProfileChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "supported-profile", 0, profile.getValue(), null);
										resourcemetadataList.addAll(rSupportedProfileChain);
									}
								}
							}

							// resource : token
							if (restResource.hasType()) {
								Resourcemetadata rType = generateResourcemetadata(resource, chainedResource, chainedParameter+"resource", restResource.getType());
								resourcemetadataList.add(rType);
							}

							// resource-profile : reference
							if (restResource.hasProfile()) {
								Resourcemetadata rProfile = generateResourcemetadata(resource, chainedResource, chainedParameter+"resource-profile", restResource.getProfile());
								resourcemetadataList.add(rProfile);

								if (chainedResource == null) {
									// Add chained parameters
									List<Resourcemetadata> rResourceProfileChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "resource-profile", 0, restResource.getProfile(), null);
									resourcemetadataList.addAll(rResourceProfileChain);
								}
							}
						}
					}

					// security-service : token
					if (rest.hasSecurity()) {

						Resourcemetadata rCode = null;
						for (CodeableConcept service : rest.getSecurity().getService()) {

							if (service.hasCoding()) {
								for (Coding code : service.getCoding()) {
									rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"security-service", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
									resourcemetadataList.add(rCode);
								}
							}
						}
					}
				}
			}

			// name : string
			if (capabilityStatement.hasName()) {
				Resourcemetadata rName = generateResourcemetadata(resource, chainedResource, chainedParameter+"name", capabilityStatement.getName());
				resourcemetadataList.add(rName);
			}

			// publisher : string
			if (capabilityStatement.hasPublisher()) {
				Resourcemetadata rPublisher = generateResourcemetadata(resource, chainedResource, chainedParameter+"publisher", capabilityStatement.getPublisher());
				resourcemetadataList.add(rPublisher);
			}

			// software : string
			if (capabilityStatement.hasSoftware()) {
				Resourcemetadata rSoftware = generateResourcemetadata(resource, chainedResource, chainedParameter+"software", capabilityStatement.getSoftware().getName());
				resourcemetadataList.add(rSoftware);
			}

			// status : token
			if (capabilityStatement.hasStatus() && capabilityStatement.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", capabilityStatement.getStatus().toCode(), capabilityStatement.getStatus().getSystem());
				resourcemetadataList.add(rStatus);
			}

			// url : uri
			if (capabilityStatement.hasUrl()) {
				Resourcemetadata rUrl = generateResourcemetadata(resource, chainedResource, chainedParameter+"url", capabilityStatement.getUrl());
				resourcemetadataList.add(rUrl);
			}

			// version : token
			if (capabilityStatement.hasVersion()) {
				Resourcemetadata rFhirversion = generateResourcemetadata(resource, chainedResource, chainedParameter+"version", capabilityStatement.getVersion());
				resourcemetadataList.add(rFhirversion);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
        } finally {
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
