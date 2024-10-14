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
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Patient.PatientLinkComponent;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataPatient extends ResourcemetadataProxy {

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
        ByteArrayInputStream iPatient = null;

		try {
			// Extract and convert the resource contents to a Patient object
			if (chainedResource != null) {
				iPatient = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iPatient = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			Patient patient = (Patient) xmlP.parse(iPatient);
			iPatient.close();

			/*
			 * Create new Resourcemetadata objects for each Patient metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, patient, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			if (patient.getId() != null) {
				Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", patient.getId());
				resourcemetadataList.add(_id);
			}

			// _language : token
			if (patient.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", patient.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (patient.getMeta() != null && patient.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(patient.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(patient.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// active : token
			if (patient.hasActive()) {
				Resourcemetadata rActive = generateResourcemetadata(resource, chainedResource, chainedParameter+"active", Boolean.toString(patient.getActive()));
				resourcemetadataList.add(rActive);
			}

			// address : string - one for each address
			if (patient.hasAddress()) {

				StringBuilder sbAddress = null;

				for (Address address : patient.getAddress()) {

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
						Resourcemetadata rCity = generateResourcemetadata(resource, chainedResource, chainedParameter+"address-city", address.getCity());
						resourcemetadataList.add(rCity);
					}

					// address-state : string
					if (address.hasState()) {
						if (sbAddress.length() > 0) {
							sbAddress.append(" ");
						}
						sbAddress.append(address.getState());
						Resourcemetadata rState = generateResourcemetadata(resource, chainedResource, chainedParameter+"address-state", address.getState());
						resourcemetadataList.add(rState);
					}

					// address-country : string
					if (address.hasCountry()) {
						if (sbAddress.length() > 0) {
							sbAddress.append(" ");
						}
						sbAddress.append(address.getCountry());
						Resourcemetadata rCountry = generateResourcemetadata(resource, chainedResource, chainedParameter+"address-country", address.getCountry());
						resourcemetadataList.add(rCountry);
					}

					// address-postalcode : string
					if (address.hasPostalCode()) {
						if (sbAddress.length() > 0) {
							sbAddress.append(" ");
						}
						sbAddress.append(address.getPostalCode());
						Resourcemetadata rPostalCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"address-postalcode", address.getPostalCode());
						resourcemetadataList.add(rPostalCode);
					}

					// address-use : token
					if (address.hasUse() && address.getUse() != null) {
						Resourcemetadata rUse = generateResourcemetadata(resource, chainedResource, chainedParameter+"address-use", address.getUse().toCode(), address.getUse().getSystem());
						resourcemetadataList.add(rUse);
					}

					// address : string
					if (address.hasText()) {
						if (sbAddress.length() > 0) {
							sbAddress.append(" ");
						}
						sbAddress.append(address.getText());
					}

					if (sbAddress.length() > 0) {
						Resourcemetadata rAddress = generateResourcemetadata(resource, chainedResource, chainedParameter+"address", sbAddress.toString());
						resourcemetadataList.add(rAddress);
					}
				}
			}

			// birthdate : date
			if (patient.hasBirthDate()) {
				Resourcemetadata rBirthDate = generateResourcemetadata(resource, chainedResource, chainedParameter+"birthdate", utcDateUtil.formatDate(patient.getBirthDate(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), null, utcDateUtil.formatDate(patient.getBirthDate(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rBirthDate);
			}

			// death-date : date
			// deceased : token
			if (patient.hasDeceased()) {

				try {
					if (patient.getDeceasedDateTimeType() != null) {
						Resourcemetadata rDeathDate = generateResourcemetadata(resource, chainedResource, chainedParameter+"death-date", utcDateUtil.formatDate(patient.getDeceasedDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(patient.getDeceasedDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
						resourcemetadataList.add(rDeathDate);
					}
				} catch (Exception e) {
					// Swallow exception; not ideal but an exception here means the expected data type was not set
				}

				try {
					if (patient.getDeceasedBooleanType() != null) {
						Resourcemetadata rDeceased = generateResourcemetadata(resource, chainedResource, chainedParameter+"deceased", patient.getDeceasedBooleanType().getValueAsString());
						resourcemetadataList.add(rDeceased);
					}
				} catch (Exception e) {
					// Swallow exception; not ideal but an exception here means the expected data type was not set
				}
			}

			// (email) telecom.system=email : token
			// (phone) telecom.system=phone : token
			// telecom : token
			if (patient.hasTelecom()) {

				for (ContactPoint telecom : patient.getTelecom()) {

					if (telecom.hasValue()) {

						String telecomSystemName = null;
						if (telecom.hasSystem() && telecom.getSystem() != null) {

							telecomSystemName = telecom.getSystem().toCode();

							if (telecom.getSystem().equals(ContactPointSystem.EMAIL)) {

								Resourcemetadata rTelecom = generateResourcemetadata(resource, chainedResource, chainedParameter + "email", telecom.getValue(), telecomSystemName);
								resourcemetadataList.add(rTelecom);
							}
							else if (telecom.getSystem().equals(ContactPointSystem.PHONE)) {

								Resourcemetadata rTelecom = generateResourcemetadata(resource, chainedResource, chainedParameter + "phone", telecom.getValue(), telecomSystemName);
								resourcemetadataList.add(rTelecom);
							}
						}

						Resourcemetadata rTelecom = generateResourcemetadata(resource, chainedResource, chainedParameter + "telecom", telecom.getValue(), telecomSystemName);
						resourcemetadataList.add(rTelecom);
					}
				}
			}

			// gender : token
			if (patient.hasGender() && patient.getGender() != null) {
				Resourcemetadata rCoding = generateResourcemetadata(resource, chainedResource, chainedParameter+"gender", patient.getGender().toCode(), patient.getGender().getSystem());
				resourcemetadataList.add(rCoding);
			}

			// general-practitioner : reference
			if (patient.hasGeneralPractitioner()) {

				String generalPractitionerReference = null;
				List<Resourcemetadata> rGeneralPractitionerChain = null;
				for (Reference generalPractitioner : patient.getGeneralPractitioner()) {

					if (generalPractitioner.hasReference()) {
						generalPractitionerReference = generateFullLocalReference(generalPractitioner.getReference(), baseUrl);

						Resourcemetadata rCareprovider = generateResourcemetadata(resource, chainedResource, chainedParameter+"general-practitioner", generalPractitionerReference);
						resourcemetadataList.add(rCareprovider);

						if (chainedResource == null) {
							// Add chained parameters for any
							rGeneralPractitionerChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "general-practitioner", 0, generalPractitioner.getReference(), null);
							resourcemetadataList.addAll(rGeneralPractitionerChain);
						}
					}
				}
			}

			// identifier : token
			if (patient.hasIdentifier()) {

				for (Identifier identifier : patient.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// language : token
			if (patient.hasLanguage()) {
				Resourcemetadata rLanguage = generateResourcemetadata(resource, chainedResource, chainedParameter+"language", patient.getLanguage());
				resourcemetadataList.add(rLanguage);
			}

			// link : reference
			if (patient.hasLink()) {

				String linkReference = null;
				List<Resourcemetadata> rLinkChain = null;
				for (PatientLinkComponent link : patient.getLink()) {

					if (link.hasOther() && link.getOther().hasReference()) {
						linkReference = generateFullLocalReference(link.getOther().getReference(), baseUrl);

						Resourcemetadata rLink = generateResourcemetadata(resource, chainedResource, chainedParameter+"link", linkReference);
						resourcemetadataList.add(rLink);

						if (chainedResource == null) {
							// Add chained parameters for any
							rLinkChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "link", 0, link.getOther().getReference(), null);
							resourcemetadataList.addAll(rLinkChain);
						}
					}
				}
			}

			// organization : reference
			if (patient.hasManagingOrganization() && patient.getManagingOrganization().hasReference()) {
				String organizationReference = generateFullLocalReference(patient.getManagingOrganization().getReference(), baseUrl);

				Resourcemetadata rOrganization = generateResourcemetadata(resource, chainedResource, chainedParameter+"organization", organizationReference);
				resourcemetadataList.add(rOrganization);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rOrganizationChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "organization", 0, patient.getManagingOrganization().getReference(), null);
					resourcemetadataList.addAll(rOrganizationChain);
				}
			}

			// family : string
			// given : string
			if (patient.hasName()) {

				for (HumanName humanName : patient.getName()) {

					if (humanName.hasFamily()) {
						Resourcemetadata rFamily = generateResourcemetadata(resource, chainedResource, chainedParameter+"family", humanName.getFamily());
						resourcemetadataList.add(rFamily);
					}
					if (humanName.hasGiven()) {

						for (StringType given : humanName.getGiven()) {
							if (given != null && given.getValue() != null) {
								Resourcemetadata rGiven = generateResourcemetadata(resource, chainedResource, chainedParameter+"given", given.getValue());
								resourcemetadataList.add(rGiven);
							}
						}
					}
				}
			}

			// name : string
			// phonetic : string
			if (patient.hasName()) {

				for (HumanName humanName : patient.getName()) {
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
						Resourcemetadata rName = generateResourcemetadata(resource, chainedResource, chainedParameter+"name", sb.toString());
						resourcemetadataList.add(rName);
						rName = generateResourcemetadata(resource, chainedResource, chainedParameter+"phonetic", sb.toString());
						resourcemetadataList.add(rName);
					}
				}
			}

			// Extensions - United States Realm FHIR Profile
			if (patient.hasExtension()) {

				for (Extension extension : patient.getExtension()) {

					if (extension.hasUrl() && extension.hasValue()) {

						// ethnicity : token
						if (extension.getUrl().equals("http://hl7.org/fhir/StructureDefinition/us-core-ethnicity") && extension.getValue() instanceof CodeableConcept) {

							for (Coding ethnicity : ((CodeableConcept)extension.getValue()).getCoding()) {

								Resourcemetadata rEthnicity = generateResourcemetadata(resource, chainedResource, chainedParameter+"ethnicity", ethnicity.getCode(), ethnicity.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(ethnicity));
								resourcemetadataList.add(rEthnicity);
							}
						}

						// mothersMaidenName : token
						if (extension.getUrl().equals("http://hl7.org/fhir/StructureDefinition/patient-mothersMaidenName") && extension.getValue() instanceof StringType) {
							String mothersMaidenName = ((StringType)extension.getValue()).getValue();

							Resourcemetadata rMothersMaidenName = generateResourcemetadata(resource, chainedResource, chainedParameter+"mothersMaidenName", mothersMaidenName);
							resourcemetadataList.add(rMothersMaidenName);
						}

						// race : token
						if (extension.getUrl().equals("http://hl7.org/fhir/StructureDefinition/us-core-race") && extension.getValue() instanceof CodeableConcept) {

							for (Coding race : ((CodeableConcept)extension.getValue()).getCoding()) {

								Resourcemetadata rRace = generateResourcemetadata(resource, chainedResource, chainedParameter+"race", race.getCode(), race.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(race));
								resourcemetadataList.add(rRace);
							}
						}
					}
				}
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
        } finally {
            if (iPatient != null) {
                try {
                    iPatient.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
