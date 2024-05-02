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
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.MedicationKnowledge;
import org.hl7.fhir.r4.model.MedicationKnowledge.MedicationKnowledgeCostComponent;
import org.hl7.fhir.r4.model.MedicationKnowledge.MedicationKnowledgeIngredientComponent;
import org.hl7.fhir.r4.model.MedicationKnowledge.MedicationKnowledgeMedicineClassificationComponent;
import org.hl7.fhir.r4.model.MedicationKnowledge.MedicationKnowledgeMonitoringProgramComponent;
import org.hl7.fhir.r4.model.MedicationKnowledge.MedicationKnowledgeMonographComponent;

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataMedicationKnowledge extends ResourcemetadataProxy {

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
        ByteArrayInputStream iMedicationKnowledge = null;

		try {
            // Extract and convert the resource contents to a MedicationKnowledge object
			if (chainedResource != null) {
				iMedicationKnowledge = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iMedicationKnowledge = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            MedicationKnowledge medicationKnowledge = (MedicationKnowledge) xmlP.parse(iMedicationKnowledge);
            iMedicationKnowledge.close();

			/*
             * Create new Resourcemetadata objects for each MedicationKnowledge metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, medicationKnowledge, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", medicationKnowledge.getId());
			resourcemetadataList.add(_id);

			// _language : token
			if (medicationKnowledge.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", medicationKnowledge.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (medicationKnowledge.getMeta() != null && medicationKnowledge.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(medicationKnowledge.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(medicationKnowledge.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// classification : token
			// classification-type : token
			if (medicationKnowledge.hasMedicineClassification()) {

				Resourcemetadata rCode = null;
				for (MedicationKnowledgeMedicineClassificationComponent medicineClassification : medicationKnowledge.getMedicineClassification()) {

					for (CodeableConcept classification : medicineClassification.getClassification()) {

						if (classification.hasCoding()) {
							for (Coding code : classification.getCoding()) {
								rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"category", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
								resourcemetadataList.add(rCode);
							}
						}
					}

					if (medicineClassification.hasType() && medicineClassification.getType().hasCoding()) {
						for (Coding code : medicineClassification.getType().getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"category-type", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// code : token
			if (medicationKnowledge.hasCode() && medicationKnowledge.getCode().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : medicationKnowledge.getCode().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			// doseform : token
			if (medicationKnowledge.hasDoseForm() && medicationKnowledge.getDoseForm().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : medicationKnowledge.getDoseForm().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"doseform", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			// ingredient : reference
			// ingredient-code : token
			if (medicationKnowledge.hasIngredient()) {

				Resourcemetadata rCode = null;
				Resourcemetadata rIngredient = null;
				List<Resourcemetadata> rIngredientChain = null;
				for (MedicationKnowledgeIngredientComponent ingredient : medicationKnowledge.getIngredient()) {

					if (ingredient.hasItemReference() && ingredient.getItemReference().hasReference()) {
						rIngredient = generateResourcemetadata(resource, chainedResource, chainedParameter+"ingredient", generateFullLocalReference(ingredient.getItemReference().getReference(), baseUrl));
						resourcemetadataList.add(rIngredient);

						if (chainedResource == null) {
							// Add chained parameters
							rIngredientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "ingredient", 0, ingredient.getItemReference().getReference());
							resourcemetadataList.addAll(rIngredientChain);
						}
					}
					else if (ingredient.hasItemCodeableConcept() && ingredient.getItemCodeableConcept().hasCoding()) {
						for (Coding code : ingredient.getItemCodeableConcept().getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"ingredient-code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// manufacturer : reference
			if (medicationKnowledge.hasManufacturer() && medicationKnowledge.getManufacturer().hasReference()) {
				Resourcemetadata rManufacturer = generateResourcemetadata(resource, chainedResource, chainedParameter+"manufacturer", generateFullLocalReference(medicationKnowledge.getManufacturer().getReference(), baseUrl));
				resourcemetadataList.add(rManufacturer);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rManufacturerChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "manufacturer", 0, medicationKnowledge.getManufacturer().getReference());
					resourcemetadataList.addAll(rManufacturerChain);
				}
			}

			// monitoring-program-name : string
			// monitoring-program-type : token
			if (medicationKnowledge.hasMonitoringProgram()) {

				Resourcemetadata rType = null;
				Resourcemetadata rMonitoringProgram = null;
				for (MedicationKnowledgeMonitoringProgramComponent monitoringProgram : medicationKnowledge.getMonitoringProgram()) {

					if (monitoringProgram.hasName()) {
						rMonitoringProgram = generateResourcemetadata(resource, chainedResource, chainedParameter+"monitoring-program-name", monitoringProgram.getName());
						resourcemetadataList.add(rMonitoringProgram);
					}

					if (monitoringProgram.hasType() && monitoringProgram.getType().hasCoding()) {

						for (Coding type : monitoringProgram.getType().getCoding()) {
							rType = generateResourcemetadata(resource, chainedResource, chainedParameter+"monitoring-program-type", type.getCode(), type.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(type));
							resourcemetadataList.add(rType);
						}
					}
				}
			}

			// monograph : reference
			// monograph-type : token
			if (medicationKnowledge.hasMonograph()) {

				Resourcemetadata rType = null;
				Resourcemetadata rMonograph = null;
				List<Resourcemetadata> rMonographChain = null;
				for (MedicationKnowledgeMonographComponent monograph : medicationKnowledge.getMonograph()) {

					if (monograph.hasSource() && monograph.getSource().hasReference()) {
						rMonograph = generateResourcemetadata(resource, chainedResource, chainedParameter+"monograph", generateFullLocalReference(monograph.getSource().getReference(), baseUrl));
						resourcemetadataList.add(rMonograph);

						if (chainedResource == null) {
							// Add chained parameters for any
							rMonographChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "monograph", 0, monograph.getSource().getReference());
							resourcemetadataList.addAll(rMonographChain);
						}
					}

					if (monograph.hasType() && monograph.getType().hasCoding()) {

						for (Coding type : monograph.getType().getCoding()) {
							rType = generateResourcemetadata(resource, chainedResource, chainedParameter+"monograph-program-type", type.getCode(), type.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(type));
							resourcemetadataList.add(rType);
						}
					}
				}
			}

			// source-cost : string
			if (medicationKnowledge.hasCost()) {

				Resourcemetadata rCost = null;
				for (MedicationKnowledgeCostComponent cost : medicationKnowledge.getCost()) {

					if (cost.hasSource()) {
						rCost = generateResourcemetadata(resource, chainedResource, chainedParameter+"source-cost", cost.getSource());
						resourcemetadataList.add(rCost);
					}
				}
			}

			// status : token
			if (medicationKnowledge.hasStatus() && medicationKnowledge.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", medicationKnowledge.getStatus().toCode(), medicationKnowledge.getStatus().getSystem());
				resourcemetadataList.add(rStatus);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return resourcemetadataList;
	}

}
