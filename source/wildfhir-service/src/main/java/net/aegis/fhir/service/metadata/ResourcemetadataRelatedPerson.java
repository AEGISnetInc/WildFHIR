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

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.RelatedPerson;
import org.hl7.fhir.r4.model.StringType;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataRelatedPerson extends ResourcemetadataProxy {

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
        ByteArrayInputStream iRelatedPerson = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

		try {
			// Extract and convert the resource contents to a RelatedPerson object
			if (chainedResource != null) {
				iRelatedPerson = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iRelatedPerson = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			RelatedPerson relatedPerson = (RelatedPerson) xmlP.parse(iRelatedPerson);
			iRelatedPerson.close();

			/*
			 * Create new Resourcemetadata objects for each RelatedPerson metadata value and add to the resourcemetadataList
			 */

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, relatedPerson, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// active : token
			if (relatedPerson.hasActive()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"active", Boolean.toString(relatedPerson.getActive()));
				resourcemetadataList.add(rMetadata);
			}

			// address : string - one for each address
			if (relatedPerson.hasAddress()) {

				StringBuilder sbAddress = null;

				for (Address address : relatedPerson.getAddress()) {

					sbAddress = new StringBuilder();

					// address : string
					if (address.hasLine()) {

						for (StringType line : address.getLine()) {
							if (line.hasValue()) {
								if (sbAddress.length() > 0) {
									sbAddress.append(" ");
								}
								sbAddress.append(line.getValue());
							}
						}
					}

					// address-city : string
					if (address.hasCity()) {
						if (sbAddress.length() > 0) {
							sbAddress.append(" ");
						}
						sbAddress.append(address.getCity());
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"address-city", address.getCity());
						resourcemetadataList.add(rMetadata);
					}

					// address-state : string
					if (address.hasState()) {
						if (sbAddress.length() > 0) {
							sbAddress.append(" ");
						}
						sbAddress.append(address.getState());
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"address-state", address.getState());
						resourcemetadataList.add(rMetadata);
					}

					// address-district : string
					if (address.hasDistrict()) {
						if (sbAddress.length() > 0) {
							sbAddress.append(" ");
						}
						sbAddress.append(address.getDistrict());
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"address-district", address.getDistrict());
						resourcemetadataList.add(rMetadata);
					}

					// address-country : string
					if (address.hasCountry()) {
						if (sbAddress.length() > 0) {
							sbAddress.append(" ");
						}
						sbAddress.append(address.getCountry());
						rMetadata= generateResourcemetadata(resource, chainedResource, chainedParameter+"address-country", address.getCountry());
						resourcemetadataList.add(rMetadata);
					}

					// address-postalcode : string
					if (address.hasPostalCode()) {
						if (sbAddress.length() > 0) {
							sbAddress.append(" ");
						}
						sbAddress.append(address.getPostalCode());
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"address-postalcode", address.getPostalCode());
						resourcemetadataList.add(rMetadata);
					}

					// address-use : token
					if (address.hasUse() && address.getUse() != null) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"address-use", address.getUse().toCode(), address.getUse().getSystem());
						resourcemetadataList.add(rMetadata);
					}

					// address : string
					if (address.hasText()) {
						if (sbAddress.length() > 0) {
							sbAddress.append(" ");
						}
						sbAddress.append(address.getText());
					}

					if (sbAddress.length() > 0) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"address", sbAddress.toString());
						resourcemetadataList.add(rMetadata);
					}
				}
			}

			// birthdate : date
			if (relatedPerson.hasBirthDate()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"birthdate", utcDateUtil.formatDate(relatedPerson.getBirthDate(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(relatedPerson.getBirthDate(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rMetadata);
			}

			// (email) telecom.system=email : token
			// (phone) telecom.system=phone : token
			// telecom : token
			if (relatedPerson.hasTelecom()) {
				for (ContactPoint telecom : relatedPerson.getTelecom()) {

					if (telecom.hasValue()) {
						String telecomSystemName = null;
						if (telecom.hasSystem() && telecom.getSystem() != null) {

							telecomSystemName = telecom.getSystem().toCode();

							if (telecom.getSystem().equals(ContactPointSystem.EMAIL)) {
								rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter + "email", telecom.getValue(), telecomSystemName);
								resourcemetadataList.add(rMetadata);
							}
							else if (telecom.getSystem().equals(ContactPointSystem.PHONE)) {
								rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter + "phone", telecom.getValue(), telecomSystemName);
								resourcemetadataList.add(rMetadata);
							}
						}

						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter + "telecom", telecom.getValue(), telecomSystemName);
						resourcemetadataList.add(rMetadata);
					}
				}
			}

			// gender : token
			if (relatedPerson.hasGender() && relatedPerson.getGender() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"gender", relatedPerson.getGender().toCode(), relatedPerson.getGender().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// identifier : token
			if (relatedPerson.hasIdentifier()) {

				for (Identifier identifier : relatedPerson.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}

			// name : string
			// phonetic : string
			if (relatedPerson.hasName()) {

				for (HumanName humanName : relatedPerson.getName()) {
					StringBuffer sb = new StringBuffer("");

					if (humanName.hasPrefix()) {

						for (StringType prefix : humanName.getPrefix()) {
							if (prefix != null && prefix.getValue() != null) {
								if (sb.length() > 0) {
									sb.append(" ");
								}
								sb.append(prefix.getValue());
							}
						}
					}
					if (humanName.hasGiven()) {

						for (StringType given : humanName.getGiven()) {
							if (given != null && given.getValue() != null) {
								if (sb.length() > 0) {
									sb.append(" ");
								}
								sb.append(given.getValue());
							}
						}
					}
					if (humanName.hasFamily()) {

						if (humanName.getFamily() != null) {
							if (sb.length() > 0) {
								sb.append(" ");
							}
							sb.append(humanName.getFamily());
						}
					}
					if (humanName.hasSuffix()) {

						for (StringType suffix : humanName.getSuffix()) {
							if (suffix != null && suffix.getValue() != null) {
								if (sb.length() > 0) {
									sb.append(" ");
								}
								sb.append(suffix.getValue());
							}
						}
					}
					if (humanName.hasText()) {

						if (humanName.getText() != null) {
							if (sb.length() > 0) {
								sb.append(" ");
							}
							sb.append(humanName.getText());
						}
					}
					if (sb.length() > 0) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"name", sb.toString());
						resourcemetadataList.add(rMetadata);
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"phonetic", sb.toString());
						resourcemetadataList.add(rMetadata);
					}
				}
			}

			// patient : reference
			if (relatedPerson.hasPatient()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "patient", 0, relatedPerson.getPatient(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// relationship : token
			if (relatedPerson.hasRelationship()) {
				for (CodeableConcept relationship : relatedPerson.getRelationship()) {

					if (relationship.hasCoding()) {
						for (Coding code : relationship.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"relationship", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
        } finally {
	        rMetadata = null;
	        rMetadataChain = null;
            if (iRelatedPerson != null) {
                try {
                	iRelatedPerson.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
