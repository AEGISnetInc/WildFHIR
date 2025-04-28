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
        ByteArrayInputStream iObservation = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

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

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, observation, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// based-on : reference
			if (observation.hasBasedOn()) {

				for (Reference basedOn : observation.getBasedOn()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "based-on", 0, basedOn, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// category : token
			if (observation.hasCategory()) {
				for (CodeableConcept category : observation.getCategory()) {

					if (category.hasCoding()) {
						for (Coding code : category.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"category", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// encounter : reference
			if (observation.hasEncounter()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "encounter", 0, observation.getEncounter(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// data-absent-reason : token
			if (observation.hasDataAbsentReason()) {

				for (Coding coding : observation.getDataAbsentReason().getCoding()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"data-absent-reason", coding.getCode(), coding.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(coding));
					resourcemetadataList.add(rMetadata);
				}
			}

			// date : date(datetime,instant,period,timing)
			if (observation.hasEffective()) {

				if (observation.hasEffectiveDateTimeType()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(observation.getEffectiveDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(observation.getEffectiveDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
					resourcemetadataList.add(rMetadata);
				}
				else if (observation.hasEffectiveInstantType()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(observation.getEffectiveInstantType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(observation.getEffectiveInstantType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
					resourcemetadataList.add(rMetadata);
				}
				else if (observation.hasEffectivePeriod()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(observation.getEffectivePeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(observation.getEffectivePeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(observation.getEffectivePeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(observation.getEffectivePeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
					resourcemetadataList.add(rMetadata);
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

						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(earliest, UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(latest, UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(earliest, UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(latest, UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
						resourcemetadataList.add(rMetadata);
					}
				}

			}

			// device : reference
			if (observation.hasDevice()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "device", 0, observation.getDevice(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// focus : reference
			if (observation.hasFocus()) {

				for (Reference focus : observation.getFocus()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "focus", 0, focus, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// has-member : reference
			if (observation.hasHasMember()) {

				for (Reference hasMember : observation.getHasMember()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "has-member", 0, hasMember, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// identifier : token
			if (observation.hasIdentifier()) {

				for (Identifier identifier : observation.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}

			// method : token
			if (observation.hasMethod() && observation.getMethod().hasCoding()) {

				for (Coding code : observation.getMethod().getCoding()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"method", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rMetadata);
				}
			}

			// part-of : reference
			if (observation.hasPartOf()) {

				for (Reference partOf : observation.getPartOf()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "part-of", 0, partOf, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// subject : reference
			if (observation.hasSubject()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "subject", 0, observation.getSubject(), null);
				resourcemetadataList.addAll(rMetadataChain);

				// patient : reference
				if ((observation.getSubject().hasReference() && observation.getSubject().getReference().indexOf("Patient") >= 0)
						|| (observation.getSubject().hasType() && observation.getSubject().getType().equals("Patient"))) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "patient", 0, observation.getSubject(), null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// performer : reference
			if (observation.hasPerformer()) {

				for (Reference performer : observation.getPerformer()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "performer", 0, performer, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// specimen : reference
			if (observation.hasSpecimen()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "specimen", 0, observation.getSpecimen(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// status : token
			if (observation.hasStatus() && observation.getStatus() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", observation.getStatus().toCode(), observation.getStatus().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// code-value[x] : composite
			StringBuilder codeValueComposite = new StringBuilder("");

			// code : token
			if (observation.hasCode() && observation.getCode().hasCoding()) {

				for (Coding code : observation.getCode().getCoding()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rMetadata);

					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"combo-code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code), "COMPOSITE");
					resourcemetadataList.add(rMetadata);
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

					for (Coding code : observation.getValueCodeableConcept().getCoding()) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"value-concept", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
						resourcemetadataList.add(rMetadata);

						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"combo-value-concept", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code), "COMPOSITE");
						resourcemetadataList.add(rMetadata);
					}

					// If CodeableConcept has text, create string composite
					if (observation.getValueCodeableConcept().hasText()) {
						StringBuilder codeValueCompositeConceptText = new StringBuilder(codeValueComposite.toString());
						codeValueCompositeConceptText.append("$").append(observation.getValueCodeableConcept().getText());

						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"code-value-string", codeValueCompositeConceptText.toString(), null, null, null, "COMPOSITE");
						resourcemetadataList.add(rMetadata);
					}

					codeValueComposite.append("$");
					if (observation.getValueCodeableConcept().getCodingFirstRep().getSystem() != null) {
						codeValueComposite.append(observation.getValueCodeableConcept().getCodingFirstRep().getSystem());
					}
					codeValueComposite.append("|").append(observation.getValueCodeableConcept().getCodingFirstRep().getCode());

					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"code-value-concept", codeValueComposite.toString(), null, null, null, "COMPOSITE");
					resourcemetadataList.add(rMetadata);

					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"combo-code-value-concept", codeValueComposite.toString(), null, null, null, "COMPOSITE");
					resourcemetadataList.add(rMetadata);
				}
				// value-date : date
				else if (observation.getValue() instanceof DateTimeType) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"value-date", utcDateUtil.formatDate(observation.getValueDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(observation.getValueDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
					resourcemetadataList.add(rMetadata);

					StringBuilder codeValueDateGMTText = new StringBuilder(codeValueComposite.toString());
					codeValueDateGMTText.append("$");
					codeValueDateGMTText.append(utcDateUtil.formatDate(observation.getValueDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT));
					StringBuilder codeValueDateAsIsText = new StringBuilder(codeValueComposite.toString());
					codeValueDateAsIsText.append("$");
					codeValueDateAsIsText.append(utcDateUtil.formatDate(observation.getValueDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));

					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"code-value-date", codeValueDateGMTText.toString(), null, codeValueDateAsIsText.toString(), null, "COMPOSITE");
					resourcemetadataList.add(rMetadata);
				}
				// value-date : date(period)
				else if (observation.getValue() instanceof Period) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"value-date", utcDateUtil.formatDate(observation.getValuePeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(observation.getValuePeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(observation.getValuePeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(observation.getValuePeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
					resourcemetadataList.add(rMetadata);

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

					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"code-value-date", codeValueDateStartGMTText.toString(), codeValueDateEndGMTText.toString(), codeValueDateStartAsIsText.toString(), codeValueDateEndAsIsText.toString(), "COMPOSITE");
					resourcemetadataList.add(rMetadata);
				}
				// value-quantity : quantity
				else if (observation.getValue() instanceof Quantity) {
					String valueQuantityCode = (observation.getValueQuantity().getCode() != null ? observation.getValueQuantity().getCode() : observation.getValueQuantity().getUnit());
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"value-quantity", observation.getValueQuantity().getValue().toString(), observation.getValueQuantity().getSystem(), valueQuantityCode);
					resourcemetadataList.add(rMetadata);

					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"combo-value-quantity", observation.getValueQuantity().getValue().toString(), observation.getValueQuantity().getSystem(), valueQuantityCode, null, "COMPOSITE");
					resourcemetadataList.add(rMetadata);

					codeValueComposite.append("$");
					if (observation.getValueQuantity().getSystem() != null) {
						codeValueComposite.append(observation.getValueQuantity().getSystem());
					}
					codeValueComposite.append("|").append(observation.getValueQuantity().getValue().toString());

					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"code-value-quantity", codeValueComposite.toString(), null, null, null, "COMPOSITE");
					resourcemetadataList.add(rMetadata);

					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"combo-code-value-quantity", codeValueComposite.toString(), null, null, null, "COMPOSITE");
					resourcemetadataList.add(rMetadata);
				}
				// value-quantity : quantity(sampled data)
				else if (observation.getValue() instanceof SampledData) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"value-quantity", observation.getValueSampledData().getLowerLimit().toString());
					resourcemetadataList.add(rMetadata);

					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"combo-value-quantity", observation.getValueSampledData().getLowerLimit().toString());
					resourcemetadataList.add(rMetadata);

					codeValueComposite.append("$");
					codeValueComposite.append(observation.getValueSampledData().getLowerLimit().toString());

					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"code-value-quantity", codeValueComposite.toString(), null, null, null, "COMPOSITE");
					resourcemetadataList.add(rMetadata);

					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"combo-code-value-quantity", codeValueComposite.toString(), null, null, null, "COMPOSITE");
					resourcemetadataList.add(rMetadata);
				}
				// value-string : string
				else if (observation.getValue() instanceof StringType) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"value-string", observation.getValueStringType().getValue());
					resourcemetadataList.add(rMetadata);

					codeValueComposite.append("$");
					codeValueComposite.append(observation.getValueStringType().getValue());

					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"code-value-string", codeValueComposite.toString(), null, null, null, "COMPOSITE");
					resourcemetadataList.add(rMetadata);
				}
			}

			if (observation.hasComponent()) {

				for (ObservationComponentComponent component : observation.getComponent()) {

					// component-data-absent-reason : token
					if (component.hasDataAbsentReason() && component.getDataAbsentReason().hasCoding()) {
						for (Coding code : component.getDataAbsentReason().getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"combo-data-absent-reason", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);

							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"component-data-absent-reason", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}

					// component-code-value[x] : composite
					StringBuilder componentCodeValueComposite = new StringBuilder("");

					// component-code : token
					if (component.hasCode() && component.getCode().hasCoding()) {
						for (Coding code : component.getCode().getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"combo-code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);

							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"component-code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
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
								rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"component-value-concept", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
								resourcemetadataList.add(rMetadata);

								rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"combo-value-concept", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
								resourcemetadataList.add(rMetadata);
							}

							// If CodeableConcept has text, create string composite
							if (component.getValueCodeableConcept().hasText()) {
								StringBuilder componentCodeValueCompositeConceptText = new StringBuilder(componentCodeValueComposite.toString());
								componentCodeValueCompositeConceptText.append("$").append(component.getValueCodeableConcept().getText());

								rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"component-code-value-string", componentCodeValueCompositeConceptText.toString(), null, null, null, "COMPOSITE");
								resourcemetadataList.add(rMetadata);

								rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"combo-code-value-string", componentCodeValueCompositeConceptText.toString(), null, null, null, "COMPOSITE");
								resourcemetadataList.add(rMetadata);
							}

							componentCodeValueComposite.append("$");
							if (component.getValueCodeableConcept().getCodingFirstRep().getSystem() != null) {
								componentCodeValueComposite.append(component.getValueCodeableConcept().getCodingFirstRep().getSystem());
							}
							componentCodeValueComposite.append("|").append(component.getValueCodeableConcept().getCodingFirstRep().getCode());

							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"component-code-value-concept", componentCodeValueComposite.toString(), null, null, null, "COMPOSITE");
							resourcemetadataList.add(rMetadata);

							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"combo-code-value-concept", componentCodeValueComposite.toString(), null, null, null, "COMPOSITE");
							resourcemetadataList.add(rMetadata);
						}
						// component-value-quantity : quantity
						else if (component.getValue() instanceof Quantity) {
							String valueQuantityCode = (component.getValueQuantity().getCode() != null ? component.getValueQuantity().getCode() : component.getValueQuantity().getUnit());
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"component-value-quantity", component.getValueQuantity().getValue().toString(), component.getValueQuantity().getSystem(), valueQuantityCode);
							resourcemetadataList.add(rMetadata);

							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"combo-code-value-quantity", component.getValueQuantity().getValue().toString(), component.getValueQuantity().getSystem(), valueQuantityCode);
							resourcemetadataList.add(rMetadata);

							componentCodeValueComposite.append("$");
							if (component.getValueQuantity().getSystem() != null) {
								componentCodeValueComposite.append(component.getValueQuantity().getSystem());
							}
							componentCodeValueComposite.append("|").append(component.getValueQuantity().getValue().toString());

							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"component-code-value-quantity", componentCodeValueComposite.toString(), null, null, null, "COMPOSITE");
							resourcemetadataList.add(rMetadata);

							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"combo-code-value-quantity", componentCodeValueComposite.toString(), null, null, null, "COMPOSITE");
							resourcemetadataList.add(rMetadata);
						}
						// component-value-quantity : quantity(sampled data)
						else if (component.getValue() instanceof SampledData) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"component-value-quantity", component.getValueSampledData().getLowerLimit().toString());
							resourcemetadataList.add(rMetadata);

							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"combo-value-quantity", component.getValueSampledData().getLowerLimit().toString());
							resourcemetadataList.add(rMetadata);

							componentCodeValueComposite.append("$");
							componentCodeValueComposite.append(component.getValueSampledData().getLowerLimit().toString());

							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"component-code-value-quantity", componentCodeValueComposite.toString(), null, null, null, "COMPOSITE");
							resourcemetadataList.add(rMetadata);

							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"combo-code-value-quantity", componentCodeValueComposite.toString(), null, null, null, "COMPOSITE");
							resourcemetadataList.add(rMetadata);
						}
						// component-value-string : string
						else if (component.getValue() instanceof StringType) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"component-value-string", component.getValueStringType().getValue());
							resourcemetadataList.add(rMetadata);

							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"combo-value-string", component.getValueStringType().getValue());
							resourcemetadataList.add(rMetadata);

							componentCodeValueComposite.append("$");
							componentCodeValueComposite.append(component.getValueStringType().getValue());

							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"component-code-value-string", componentCodeValueComposite.toString(), null, null, null, "COMPOSITE");
							resourcemetadataList.add(rMetadata);

							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"combo-code-value-string", componentCodeValueComposite.toString(), null, null, null, "COMPOSITE");
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
            if (iObservation != null) {
                try {
                	iObservation.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
