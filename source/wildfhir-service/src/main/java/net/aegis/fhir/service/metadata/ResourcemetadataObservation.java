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
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Observation.ObservationComponentComponent;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.SampledData;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Timing;

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataObservation extends ResourcemetadataProxy {

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
        ByteArrayInputStream iObservation = null;

		try {
            // Extract and convert the resource contents to a Observation object
			if (chainedResource != null) {
				iObservation = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iObservation = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            Observation observation = (Observation) xmlP.parse(iObservation);
            iObservation.close();

			/*
             * Create new Resourcemetadata objects for each Observation metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, observation, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", observation.getId());
			resourcemetadataList.add(_id);

			// _language : token
			if (observation.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", observation.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (observation.getMeta() != null && observation.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(observation.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(observation.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// based-on : reference
			if (observation.hasBasedOn()) {

				List<Resourcemetadata> rBasedOnChain = null;
				for (Reference basedOn : observation.getBasedOn()) {

					if (basedOn.hasReference()) {
						Resourcemetadata rBasedOn = generateResourcemetadata(resource, chainedResource, chainedParameter+"based-on", generateFullLocalReference(basedOn.getReference(), baseUrl));
						resourcemetadataList.add(rBasedOn);

						if (chainedResource == null) {
							// Add chained parameters for any
							rBasedOnChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "based-on", 0, basedOn.getReference());
							resourcemetadataList.addAll(rBasedOnChain);
						}
					}
				}
			}

			// category : token
			if (observation.hasCategory()) {

				Resourcemetadata rCode = null;
				for (CodeableConcept category : observation.getCategory()) {

					if (category.hasCoding()) {
						for (Coding code : category.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"category", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// encounter : reference
			if (observation.hasEncounter() && observation.getEncounter().hasReference()) {

				Resourcemetadata rEncounter = generateResourcemetadata(resource, chainedResource, chainedParameter+"encounter", generateFullLocalReference(observation.getEncounter().getReference(), baseUrl));
				resourcemetadataList.add(rEncounter);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rEncounterChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "encounter", 0, observation.getEncounter().getReference());
					resourcemetadataList.addAll(rEncounterChain);
				}
			}

			// data-absent-reason : token
			if (observation.hasDataAbsentReason()) {

				for (Coding coding : observation.getDataAbsentReason().getCoding()) {

					Resourcemetadata rDataAbsentReason = generateResourcemetadata(resource, chainedResource, chainedParameter+"data-absent-reason", coding.getCode(), coding.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(coding));
					resourcemetadataList.add(rDataAbsentReason);
				}
			}

			// date : date(datetime,instant,period,timing)
			if (observation.hasEffective()) {

				if (observation.hasEffectiveDateTimeType()) {
					Resourcemetadata rDateDateTime = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(observation.getEffectiveDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(observation.getEffectiveDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
					resourcemetadataList.add(rDateDateTime);
				}
				else if (observation.hasEffectiveInstantType()) {
					Resourcemetadata rDateDateTime = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(observation.getEffectiveInstantType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(observation.getEffectiveInstantType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
					resourcemetadataList.add(rDateDateTime);
				}
				else if (observation.hasEffectivePeriod()) {
					Resourcemetadata rDatePeriod = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(observation.getEffectivePeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(observation.getEffectivePeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(observation.getEffectivePeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(observation.getEffectivePeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
					resourcemetadataList.add(rDatePeriod);
				}
				else if (observation.hasEffectiveTiming()) {
					// Special logic for Timing - evaluate earliest and latest event values, treat like Period start and end
					Timing effectiveTiming = observation.getEffectiveTiming();
					if (effectiveTiming.hasEvent()) {
						Date latest = effectiveTiming.getEvent().get(0).getValue();
						Date earliest = latest;
						Date current = null;
						for (DateTimeType event : effectiveTiming.getEvent()) {
							current = event.getValue();
							if (current.after(latest)) {
								latest = current;
							}
							if (current.before(earliest)) {
								earliest = current;
							}
						}

						Resourcemetadata rDatePeriod = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(earliest, UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(latest, UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(earliest, UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(latest, UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
						resourcemetadataList.add(rDatePeriod);
					}
				}

			}

			// device : reference
			if (observation.hasDevice() && observation.getDevice().hasReference()) {
				Resourcemetadata rDevice = generateResourcemetadata(resource, chainedResource, chainedParameter+"device", generateFullLocalReference(observation.getDevice().getReference(), baseUrl));
				resourcemetadataList.add(rDevice);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rDeviceChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "device", 0, observation.getDevice().getReference());
					resourcemetadataList.addAll(rDeviceChain);
				}
			}

			// focus : reference
			if (observation.hasFocus()) {

				for (Reference focus : observation.getFocus()) {

					if (focus.hasReference()) {
						Resourcemetadata rFocus = generateResourcemetadata(resource, chainedResource, chainedParameter+"focus", generateFullLocalReference(focus.getReference(), baseUrl));
						resourcemetadataList.add(rFocus);

						if (chainedResource == null) {
							// Add chained parameters for any
							List<Resourcemetadata> rFocusChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "focus", 0, focus.getReference());
							resourcemetadataList.addAll(rFocusChain);
						}
					}
				}
			}

			// has-member : reference
			if (observation.hasHasMember()) {

				List<Resourcemetadata> rHasMemberChain = null;
				for (Reference hasMember : observation.getHasMember()) {

					if (hasMember.hasReference()) {
						Resourcemetadata rHasMember = generateResourcemetadata(resource, chainedResource, chainedParameter+"has-member", generateFullLocalReference(hasMember.getReference(), baseUrl));
						resourcemetadataList.add(rHasMember);

						if (chainedResource == null) {
							// Add chained parameters for any
							rHasMemberChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "has-member", 0, hasMember.getReference());
							resourcemetadataList.addAll(rHasMemberChain);
						}
					}
				}
			}

			// identifier : token
			if (observation.hasIdentifier()) {

				for (Identifier identifier : observation.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// method : token
			if (observation.hasMethod() && observation.getMethod().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : observation.getMethod().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"method", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			// part-of : reference
			if (observation.hasPartOf()) {

				List<Resourcemetadata> rPartOfChain = null;
				for (Reference partOf : observation.getPartOf()) {

					if (partOf.hasReference()) {
						Resourcemetadata rPartOf = generateResourcemetadata(resource, chainedResource, chainedParameter+"part-of", generateFullLocalReference(partOf.getReference(), baseUrl));
						resourcemetadataList.add(rPartOf);

						if (chainedResource == null) {
							// Add chained parameters for any
							rPartOfChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "part-of", 0, partOf.getReference());
							resourcemetadataList.addAll(rPartOfChain);
						}
					}
				}
			}

			// patient : reference
			// subject : reference
			if (observation.hasSubject() && observation.getSubject().hasReference()) {
				String subjectReference = generateFullLocalReference(observation.getSubject().getReference(), baseUrl);

				Resourcemetadata rSubject = generateResourcemetadata(resource, chainedResource, chainedParameter+"subject", subjectReference);
				resourcemetadataList.add(rSubject);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rSubjectChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "subject", 0, observation.getSubject().getReference());
					resourcemetadataList.addAll(rSubjectChain);
				}

				if (subjectReference.indexOf("Patient") >= 0) {
					Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", subjectReference);
					resourcemetadataList.add(rPatient);

					if (chainedResource == null) {
						// Add chained parameters
						List<Resourcemetadata> rPatientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, observation.getSubject().getReference());
						resourcemetadataList.addAll(rPatientChain);
					}
				}
			}

			// performer : reference
			if (observation.hasPerformer()) {

				List<Resourcemetadata> rPerformerChain = null;
				for (Reference performer : observation.getPerformer()) {

					if (performer.hasReference()) {
						Resourcemetadata rPerformer = generateResourcemetadata(resource, chainedResource, chainedParameter+"performer", generateFullLocalReference(performer.getReference(), baseUrl));
						resourcemetadataList.add(rPerformer);

						if (chainedResource == null) {
							// Add chained parameters for any
							rPerformerChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "performer", 0, performer.getReference());
							resourcemetadataList.addAll(rPerformerChain);
						}
					}
				}
			}

			// specimen : reference
			if (observation.hasSpecimen() && observation.getSpecimen().hasReference()) {
				Resourcemetadata rSpecimen = generateResourcemetadata(resource, chainedResource, chainedParameter+"specimen", generateFullLocalReference(observation.getSpecimen().getReference(), baseUrl));
				resourcemetadataList.add(rSpecimen);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rSpecimenChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "specimen", 0, observation.getSpecimen().getReference());
					resourcemetadataList.addAll(rSpecimenChain);
				}
			}

			// status : token
			if (observation.hasStatus() && observation.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", observation.getStatus().toCode(), observation.getStatus().getSystem());
				resourcemetadataList.add(rStatus);
			}

			// code-value[x] : composite
			StringBuilder codeValueComposite = new StringBuilder("");

			// code : token
			if (observation.hasCode() && observation.getCode().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : observation.getCode().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);

					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"combo-code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}

				// Use only the first one for the code-value[x] composite
				if (observation.getCode().getCodingFirstRep().hasSystem()) {
					codeValueComposite.append(observation.getCode().getCodingFirstRep().getSystem());
				}
				codeValueComposite.append("|").append(observation.getCode().getCodingFirstRep().getCode());
			}

			// value[x]
			if (observation.hasValue()) {

				// value-concept : token
				if (observation.getValue() instanceof CodeableConcept && observation.getValueCodeableConcept().hasCoding()) {

					Resourcemetadata rCode = null;
					for (Coding code : observation.getValueCodeableConcept().getCoding()) {
						rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"value-concept", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
						resourcemetadataList.add(rCode);

						rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"combo-value-concept", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
						resourcemetadataList.add(rCode);
					}

					// If CodeableConcept has text, create string composite
					if (observation.getValueCodeableConcept().hasText()) {
						StringBuilder codeValueCompositeConceptText = new StringBuilder(codeValueComposite.toString());
						codeValueCompositeConceptText.append("$").append(observation.getValueCodeableConcept().getText());

						Resourcemetadata rCodeValueCodeableConcept = generateResourcemetadata(resource, chainedResource, chainedParameter+"code-value-string", codeValueCompositeConceptText.toString());
						resourcemetadataList.add(rCodeValueCodeableConcept);
					}

					codeValueComposite.append("$");
					if (observation.getValueCodeableConcept().getCodingFirstRep().getSystem() != null) {
						codeValueComposite.append(observation.getValueCodeableConcept().getCodingFirstRep().getSystem());
					}
					codeValueComposite.append("|").append(observation.getValueCodeableConcept().getCodingFirstRep().getCode());

					Resourcemetadata rCodeValueCodeableConcept = generateResourcemetadata(resource, chainedResource, chainedParameter+"code-value-concept", codeValueComposite.toString());
					resourcemetadataList.add(rCodeValueCodeableConcept);

					rCodeValueCodeableConcept = generateResourcemetadata(resource, chainedResource, chainedParameter+"combo-code-value-concept", codeValueComposite.toString());
					resourcemetadataList.add(rCodeValueCodeableConcept);
				}
				// value-date : date
				else if (observation.getValue() instanceof DateTimeType) {
					Resourcemetadata rValueDateTime = generateResourcemetadata(resource, chainedResource, chainedParameter+"value-date", utcDateUtil.formatDate(observation.getValueDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(observation.getValueDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
					resourcemetadataList.add(rValueDateTime);

					StringBuilder codeValueDateGMTText = new StringBuilder(codeValueComposite.toString());
					codeValueDateGMTText.append("$");
					codeValueDateGMTText.append(utcDateUtil.formatDate(observation.getValueDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT));
					StringBuilder codeValueDateAsIsText = new StringBuilder(codeValueComposite.toString());
					codeValueDateAsIsText.append("$");
					codeValueDateAsIsText.append(utcDateUtil.formatDate(observation.getValueDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));

					rValueDateTime = generateResourcemetadata(resource, chainedResource, chainedParameter+"code-value-date", codeValueDateGMTText.toString(), null, codeValueDateAsIsText.toString());
					resourcemetadataList.add(rValueDateTime);
				}
				// value-date : date(period)
				else if (observation.getValue() instanceof Period) {
					Resourcemetadata rValuePeriod = generateResourcemetadata(resource, chainedResource, chainedParameter+"value-date", utcDateUtil.formatDate(observation.getValuePeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(observation.getValuePeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(observation.getValuePeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(observation.getValuePeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
					resourcemetadataList.add(rValuePeriod);

					StringBuilder codeValueDateStartGMTText = new StringBuilder(codeValueComposite.toString());
					codeValueDateStartGMTText.append("$");
					codeValueDateStartGMTText.append(utcDateUtil.formatDate(observation.getValuePeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT));
					StringBuilder codeValueDateEndGMTText = new StringBuilder(codeValueComposite.toString());
					codeValueDateEndGMTText.append("$");
					codeValueDateEndGMTText.append(utcDateUtil.formatDate(observation.getValuePeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT));
					StringBuilder codeValueDateStartAsIsText = new StringBuilder(codeValueComposite.toString());
					codeValueDateStartAsIsText.append("$");
					codeValueDateStartAsIsText.append(utcDateUtil.formatDate(observation.getValuePeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
					StringBuilder codeValueDateEndAsIsText = new StringBuilder(codeValueComposite.toString());
					codeValueDateEndAsIsText.append("$");
					codeValueDateEndAsIsText.append(utcDateUtil.formatDate(observation.getValuePeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));

					Resourcemetadata rCodeValueCodeableConcept = generateResourcemetadata(resource, chainedResource, chainedParameter+"code-value-date", codeValueDateStartGMTText.toString(), codeValueDateEndGMTText.toString(), codeValueDateStartAsIsText.toString(), codeValueDateEndAsIsText.toString());
					resourcemetadataList.add(rCodeValueCodeableConcept);
				}
				// value-quantity : quantity
				else if (observation.getValue() instanceof Quantity) {
					String valueQuantityCode = (observation.getValueQuantity().getCode() != null ? observation.getValueQuantity().getCode() : observation.getValueQuantity().getUnit());
					Resourcemetadata rValueQuantity = generateResourcemetadata(resource, chainedResource, chainedParameter+"value-quantity", observation.getValueQuantity().getValue().toString(), observation.getValueQuantity().getSystem(), valueQuantityCode);
					resourcemetadataList.add(rValueQuantity);

					rValueQuantity = generateResourcemetadata(resource, chainedResource, chainedParameter+"combo-value-quantity", observation.getValueQuantity().getValue().toString(), observation.getValueQuantity().getSystem(), valueQuantityCode);
					resourcemetadataList.add(rValueQuantity);

					codeValueComposite.append("$");
					if (observation.getValueQuantity().getSystem() != null) {
						codeValueComposite.append(observation.getValueQuantity().getSystem());
					}
					codeValueComposite.append("|").append(observation.getValueQuantity().getValue().toString());

					Resourcemetadata rCodeValueCodeableConcept = generateResourcemetadata(resource, chainedResource, chainedParameter+"code-value-quantity", codeValueComposite.toString());
					resourcemetadataList.add(rCodeValueCodeableConcept);

					rCodeValueCodeableConcept = generateResourcemetadata(resource, chainedResource, chainedParameter+"combo-code-value-quantity", codeValueComposite.toString());
					resourcemetadataList.add(rCodeValueCodeableConcept);
				}
				// value-quantity : quantity(sampled data)
				else if (observation.getValue() instanceof SampledData) {
					Resourcemetadata rValueQuantity = generateResourcemetadata(resource, chainedResource, chainedParameter+"value-quantity", observation.getValueSampledData().getLowerLimit().toString());
					resourcemetadataList.add(rValueQuantity);

					rValueQuantity = generateResourcemetadata(resource, chainedResource, chainedParameter+"combo-value-quantity", observation.getValueSampledData().getLowerLimit().toString());
					resourcemetadataList.add(rValueQuantity);

					codeValueComposite.append("$");
					codeValueComposite.append(observation.getValueSampledData().getLowerLimit().toString());

					Resourcemetadata rCodeValueCodeableConcept = generateResourcemetadata(resource, chainedResource, chainedParameter+"code-value-quantity", codeValueComposite.toString());
					resourcemetadataList.add(rCodeValueCodeableConcept);

					rCodeValueCodeableConcept = generateResourcemetadata(resource, chainedResource, chainedParameter+"combo-code-value-quantity", codeValueComposite.toString());
					resourcemetadataList.add(rCodeValueCodeableConcept);
				}
				// value-string : string
				else if (observation.getValue() instanceof StringType) {
					Resourcemetadata rValueStringType = generateResourcemetadata(resource, chainedResource, chainedParameter+"value-string", observation.getValueStringType().getValue());
					resourcemetadataList.add(rValueStringType);

					codeValueComposite.append("$");
					codeValueComposite.append(observation.getValueStringType().getValue());

					Resourcemetadata rCodeValueCodeableConcept = generateResourcemetadata(resource, chainedResource, chainedParameter+"code-value-string", codeValueComposite.toString());
					resourcemetadataList.add(rCodeValueCodeableConcept);
				}
			}

			if (observation.hasComponent()) {

				Resourcemetadata rCode = null;
				for (ObservationComponentComponent component : observation.getComponent()) {

					// component-data-absent-reason : token
					if (component.hasDataAbsentReason() && component.getDataAbsentReason().hasCoding()) {
						for (Coding code : component.getDataAbsentReason().getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"combo-data-absent-reason", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);

							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"component-data-absent-reason", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}

					// component-code-value[x] : composite
					StringBuilder componentCodeValueComposite = new StringBuilder("");

					// component-code : token
					if (component.hasCode() && component.getCode().hasCoding()) {
						for (Coding code : component.getCode().getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"combo-code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);

							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"component-code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}

						// Use only the first one for the component-code-value[x] composite
						if (component.getCode().getCodingFirstRep().hasSystem()) {
							componentCodeValueComposite.append(component.getCode().getCodingFirstRep().getSystem());
						}
						componentCodeValueComposite.append("|").append(component.getCode().getCodingFirstRep().getCode());
					}

					// component-value[x]
					if (component.hasValue()) {

						// component-value-concept : token
						if (component.getValue() instanceof CodeableConcept && component.getValueCodeableConcept().hasCoding()) {
							for (Coding code : component.getValueCodeableConcept().getCoding()) {
								rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"component-value-concept", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
								resourcemetadataList.add(rCode);

								rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"combo-value-concept", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
								resourcemetadataList.add(rCode);
							}

							// If CodeableConcept has text, create string composite
							if (component.getValueCodeableConcept().hasText()) {
								StringBuilder componentCodeValueCompositeConceptText = new StringBuilder(componentCodeValueComposite.toString());
								componentCodeValueCompositeConceptText.append("$").append(component.getValueCodeableConcept().getText());

								Resourcemetadata rCodeValueCodeableConcept = generateResourcemetadata(resource, chainedResource, chainedParameter+"component-code-value-string", componentCodeValueCompositeConceptText.toString());
								resourcemetadataList.add(rCodeValueCodeableConcept);

								rCodeValueCodeableConcept = generateResourcemetadata(resource, chainedResource, chainedParameter+"combo-code-value-string", componentCodeValueCompositeConceptText.toString());
								resourcemetadataList.add(rCodeValueCodeableConcept);
							}

							componentCodeValueComposite.append("$");
							if (component.getValueCodeableConcept().getCodingFirstRep().getSystem() != null) {
								componentCodeValueComposite.append(component.getValueCodeableConcept().getCodingFirstRep().getSystem());
							}
							componentCodeValueComposite.append("|").append(component.getValueCodeableConcept().getCodingFirstRep().getCode());

							Resourcemetadata rCodeValueCodeableConcept = generateResourcemetadata(resource, chainedResource, chainedParameter+"component-code-value-concept", componentCodeValueComposite.toString());
							resourcemetadataList.add(rCodeValueCodeableConcept);

							rCodeValueCodeableConcept = generateResourcemetadata(resource, chainedResource, chainedParameter+"combo-code-value-concept", componentCodeValueComposite.toString());
							resourcemetadataList.add(rCodeValueCodeableConcept);
						}
						// component-value-quantity : quantity
						else if (component.getValue() instanceof Quantity) {
							String valueQuantityCode = (component.getValueQuantity().getCode() != null ? component.getValueQuantity().getCode() : component.getValueQuantity().getUnit());
							Resourcemetadata rValueQuantity = generateResourcemetadata(resource, chainedResource, chainedParameter+"component-value-quantity", component.getValueQuantity().getValue().toString(), component.getValueQuantity().getSystem(), valueQuantityCode);
							resourcemetadataList.add(rValueQuantity);

							rValueQuantity = generateResourcemetadata(resource, chainedResource, chainedParameter+"combo-code-value-quantity", component.getValueQuantity().getValue().toString(), component.getValueQuantity().getSystem(), valueQuantityCode);
							resourcemetadataList.add(rValueQuantity);

							componentCodeValueComposite.append("$");
							if (component.getValueQuantity().getSystem() != null) {
								componentCodeValueComposite.append(component.getValueQuantity().getSystem());
							}
							componentCodeValueComposite.append("|").append(component.getValueQuantity().getValue().toString());

							Resourcemetadata rCodeValueCodeableConcept = generateResourcemetadata(resource, chainedResource, chainedParameter+"component-code-value-quantity", componentCodeValueComposite.toString());
							resourcemetadataList.add(rCodeValueCodeableConcept);

							rCodeValueCodeableConcept = generateResourcemetadata(resource, chainedResource, chainedParameter+"combo-code-value-quantity", componentCodeValueComposite.toString());
							resourcemetadataList.add(rCodeValueCodeableConcept);
						}
						// component-value-quantity : quantity(sampled data)
						else if (component.getValue() instanceof SampledData) {
							Resourcemetadata rValueQuantity = generateResourcemetadata(resource, chainedResource, chainedParameter+"component-value-quantity", component.getValueSampledData().getLowerLimit().toString());
							resourcemetadataList.add(rValueQuantity);

							rValueQuantity = generateResourcemetadata(resource, chainedResource, chainedParameter+"combo-value-quantity", component.getValueSampledData().getLowerLimit().toString());
							resourcemetadataList.add(rValueQuantity);

							componentCodeValueComposite.append("$");
							componentCodeValueComposite.append(component.getValueSampledData().getLowerLimit().toString());

							Resourcemetadata rCodeValueCodeableConcept = generateResourcemetadata(resource, chainedResource, chainedParameter+"component-code-value-quantity", componentCodeValueComposite.toString());
							resourcemetadataList.add(rCodeValueCodeableConcept);

							rCodeValueCodeableConcept = generateResourcemetadata(resource, chainedResource, chainedParameter+"combo-code-value-quantity", componentCodeValueComposite.toString());
							resourcemetadataList.add(rCodeValueCodeableConcept);
						}
						// component-value-string : string
						else if (component.getValue() instanceof StringType) {
							Resourcemetadata rValueStringType = generateResourcemetadata(resource, chainedResource, chainedParameter+"component-value-string", component.getValueStringType().getValue());
							resourcemetadataList.add(rValueStringType);

							rValueStringType = generateResourcemetadata(resource, chainedResource, chainedParameter+"combo-value-string", component.getValueStringType().getValue());
							resourcemetadataList.add(rValueStringType);

							componentCodeValueComposite.append("$");
							componentCodeValueComposite.append(component.getValueStringType().getValue());

							Resourcemetadata rCodeValueCodeableConcept = generateResourcemetadata(resource, chainedResource, chainedParameter+"component-code-value-string", componentCodeValueComposite.toString());
							resourcemetadataList.add(rCodeValueCodeableConcept);

							rCodeValueCodeableConcept = generateResourcemetadata(resource, chainedResource, chainedParameter+"combo-code-value-string", componentCodeValueComposite.toString());
							resourcemetadataList.add(rCodeValueCodeableConcept);
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
