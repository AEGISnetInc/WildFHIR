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
        ByteArrayInputStream iGroup = null;

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

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, group, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", group.getId());
			resourcemetadataList.add(_id);

			// _language : token
			if (group.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", group.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (group.getMeta() != null && group.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(group.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(group.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

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
							Resourcemetadata rCoding = generateResourcemetadata(resource, chainedResource, chainedParameter+"characteristic", coding.getCode(), coding.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(coding));
							resourcemetadataList.add(rCoding);
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
									Resourcemetadata rCoding = generateResourcemetadata(resource, chainedResource, chainedParameter+"valueCodeableConcept", coding.getCode(), coding.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(coding));
									resourcemetadataList.add(rCoding);
									break; // Only take the first one
								}
							}

						} else if (characteristic.getValue() instanceof BooleanType) {
							BooleanType valueBoolean = (BooleanType) characteristic.getValue();

							characteristicValue = valueBoolean.asStringValue();
							Resourcemetadata rCoding = generateResourcemetadata(resource, chainedResource, chainedParameter+"valueBoolean", valueBoolean.asStringValue());
							resourcemetadataList.add(rCoding);

						} else if (characteristic.getValue() instanceof Quantity) {
							Quantity valueQuantity = (Quantity)characteristic.getValue();

							if (valueQuantity.getValue() != null) {
								characteristicValue = valueQuantity.getValue().toPlainString();
								Resourcemetadata rValue = generateResourcemetadata(resource, chainedResource, chainedParameter+"valueQuantity", valueQuantity.getValue().toPlainString());
								resourcemetadataList.add(rValue);
							}

						} else if (characteristic.getValue() instanceof Range) {
							Range valueRange = (Range)characteristic.getValue();

							if (valueRange.getLow() != null && valueRange.getLow().getValue() != null) {
								characteristicValue = valueRange.getLow().getValue().toPlainString();
								Resourcemetadata rValue = generateResourcemetadata(resource, chainedResource, chainedParameter+"valueRangeLow", valueRange.getLow().getValue().toPlainString());
								resourcemetadataList.add(rValue);
							}

							if (valueRange.getHigh() != null && valueRange.getHigh().getValue() != null) {
								if (characteristicValue != null) {
									characteristicValue += ":" + valueRange.getHigh().getValue().toPlainString();
								} else {
									characteristicValue = valueRange.getHigh().getValue().toPlainString();
								}
								Resourcemetadata rValue = generateResourcemetadata(resource, chainedResource, chainedParameter+"valueRangeHigh", valueRange.getHigh().getValue().toPlainString());
								resourcemetadataList.add(rValue);
							}

						}
					}

					// characteristic-value : composite
					if (characteristicCode != null && characteristicValue != null) {
						Resourcemetadata rValue = generateResourcemetadata(resource, chainedResource, chainedParameter+"characteristic-value", characteristicCode + "$" + characteristicValue);
						resourcemetadataList.add(rValue);
					}

					// exclude : token
					Resourcemetadata rExclude = generateResourcemetadata(resource, chainedResource, chainedParameter+"exclude", Boolean.toString(characteristic.getExclude()));
					resourcemetadataList.add(rExclude);
				}
			}

			// code : token
			if (group.hasCode()) {

				for (Coding coding : group.getCode().getCoding()) {

					Resourcemetadata rCoding = generateResourcemetadata(resource, chainedResource, chainedParameter+"code", coding.getCode(), coding.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(coding));
					resourcemetadataList.add(rCoding);
					break; // Only take the first one
				}
			}

			// identifier : token
			if (group.hasIdentifier()) {

				for (Identifier identifier : group.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// managing-entity : reference
			if (group.hasManagingEntity() && group.getManagingEntity().hasReference()) {
				String managingEntityReference = generateFullLocalReference(group.getManagingEntity().getReference(), baseUrl);

				Resourcemetadata rManagingEntity = generateResourcemetadata(resource, chainedResource, chainedParameter+"managing-entity", managingEntityReference);
				resourcemetadataList.add(rManagingEntity);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rManagingEntityChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "managing-entity", 0, group.getManagingEntity().getReference());
					resourcemetadataList.addAll(rManagingEntityChain);
				}
			}

			// member : reference
			if (group.hasMember()) {

				List<Resourcemetadata> rMemberChain = null;
				for (GroupMemberComponent member : group.getMember()) {

					if (member.hasEntity()) {

						if (member.getEntity().hasReference()) {
							Resourcemetadata rMember = generateResourcemetadata(resource, chainedResource, chainedParameter+"member", generateFullLocalReference(member.getEntity().getReference(), baseUrl));
							resourcemetadataList.add(rMember);

							if (chainedResource == null) {
								// Add chained parameters for any
								rMemberChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "member", 0, member.getEntity().getReference());
								resourcemetadataList.addAll(rMemberChain);
							}
						}
					}

					// member-period : date(period)
					if (member.hasPeriod()) {
						Resourcemetadata rMemberPeriod = generateResourcemetadata(resource, chainedResource, chainedParameter+"member-period", utcDateUtil.formatDate(member.getPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(member.getPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(member.getPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(member.getPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
						resourcemetadataList.add(rMemberPeriod);
					}
				}
			}

			// type : token
			if (group.hasType() && group.getType() != null) {
				Resourcemetadata rType = generateResourcemetadata(resource, chainedResource, chainedParameter+"type", group.getType().toCode(), group.getType().getSystem());
				resourcemetadataList.add(rType);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
        } finally {
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
