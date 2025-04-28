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
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.Group.GroupCharacteristicComponent;
import org.hl7.fhir.r4.model.Group.GroupMemberComponent;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Range;

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataGroup extends ResourcemetadataProxy {

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
        ByteArrayInputStream iGroup = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

		try {
			// Extract and convert the resource contents to a Group object
			if (chainedResource != null) {
				iGroup = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iGroup = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			Group group = (Group) xmlP.parse(iGroup);
			iGroup.close();

			/*
			 * Create new Resourcemetadata objects for each Group metadata value and add to the resourcemetadataList
			 */

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, group, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// actual : token
			Resourcemetadata rActual = generateResourcemetadata(resource, chainedResource, chainedParameter+"actual", Boolean.toString(group.getActual()));
			resourcemetadataList.add(rActual);

			// characteristic : token
			// characteristic-value : composite
			// value : token
			// exclude : token
			if (group.getCharacteristic() != null && group.getCharacteristic().size() > 0) {

				// For Each Characteristic
				for (GroupCharacteristicComponent characteristic : group.getCharacteristic()) {

					// characteristic : token
					String characteristicCode = null;
					if (characteristic.getCode() != null && characteristic.getCode().getCoding() != null && characteristic.getCode().getCoding().size() > 0) {

						for (Coding coding : characteristic.getCode().getCoding()) {

							characteristicCode = coding.getCode();
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"characteristic", coding.getCode(), coding.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(coding));
							resourcemetadataList.add(rMetadata);
							break; // Only take the first one
						}
					}

					// value : token
					String characteristicValue = null;
					if (characteristic.getValue() != null) {

						if (characteristic.getValue() instanceof CodeableConcept) {
							CodeableConcept valueCodeableConcept = (CodeableConcept)characteristic.getValue();

							if (valueCodeableConcept.getCoding() != null && valueCodeableConcept.getCoding().size() > 0) {

								for (Coding coding : valueCodeableConcept.getCoding()) {
									characteristicValue = coding.getCode();
									rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"valueCodeableConcept", coding.getCode(), coding.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(coding));
									resourcemetadataList.add(rMetadata);
									break; // Only take the first one
								}
							}

						} else if (characteristic.getValue() instanceof BooleanType) {
							BooleanType valueBoolean = (BooleanType) characteristic.getValue();

							characteristicValue = valueBoolean.asStringValue();
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"valueBoolean", valueBoolean.asStringValue());
							resourcemetadataList.add(rMetadata);

						} else if (characteristic.getValue() instanceof Quantity) {
							Quantity valueQuantity = (Quantity)characteristic.getValue();

							if (valueQuantity.getValue() != null) {
								characteristicValue = valueQuantity.getValue().toPlainString();
								rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"valueQuantity", valueQuantity.getValue().toPlainString());
								resourcemetadataList.add(rMetadata);
							}

						} else if (characteristic.getValue() instanceof Range) {
							Range valueRange = (Range)characteristic.getValue();

							if (valueRange.getLow() != null && valueRange.getLow().getValue() != null) {
								characteristicValue = valueRange.getLow().getValue().toPlainString();
								rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"valueRangeLow", valueRange.getLow().getValue().toPlainString());
								resourcemetadataList.add(rMetadata);
							}

							if (valueRange.getHigh() != null && valueRange.getHigh().getValue() != null) {
								if (characteristicValue != null) {
									characteristicValue += ":" + valueRange.getHigh().getValue().toPlainString();
								} else {
									characteristicValue = valueRange.getHigh().getValue().toPlainString();
								}
								rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"valueRangeHigh", valueRange.getHigh().getValue().toPlainString());
								resourcemetadataList.add(rMetadata);
							}

						}
					}

					// characteristic-value : composite
					if (characteristicCode != null && characteristicValue != null) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"characteristic-value", characteristicCode + "$" + characteristicValue);
						resourcemetadataList.add(rMetadata);
					}

					// exclude : token
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"exclude", Boolean.toString(characteristic.getExclude()));
					resourcemetadataList.add(rMetadata);
				}
			}

			// code : token
			if (group.hasCode()) {

				for (Coding coding : group.getCode().getCoding()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"code", coding.getCode(), coding.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(coding));
					resourcemetadataList.add(rMetadata);
					break; // Only take the first one
				}
			}

			// identifier : token
			if (group.hasIdentifier()) {

				for (Identifier identifier : group.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}

			// managing-entity : reference
			if (group.hasManagingEntity()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "managing-entity", 0, group.getManagingEntity(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// member : reference
			if (group.hasMember()) {
				for (GroupMemberComponent member : group.getMember()) {

					if (member.hasEntity()) {
						rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "member", 0, member.getEntity(), null);
						resourcemetadataList.addAll(rMetadataChain);
					}

					// member-period : date(period)
					if (member.hasPeriod()) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"member-period", utcDateUtil.formatDate(member.getPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(member.getPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(member.getPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(member.getPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
						resourcemetadataList.add(rMetadata);
					}
				}
			}

			// type : token
			if (group.hasType() && group.getType() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"type", group.getType().toCode(), group.getType().getSystem());
				resourcemetadataList.add(rMetadata);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
        } finally {
	        rMetadata = null;
	        rMetadataChain = null;
            if (iGroup != null) {
                try {
                    iGroup.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
