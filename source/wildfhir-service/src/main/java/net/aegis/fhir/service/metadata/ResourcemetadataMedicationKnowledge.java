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
        ByteArrayInputStream iMedicationKnowledge = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

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

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, medicationKnowledge, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// classification : token
			// classification-type : token
			if (medicationKnowledge.hasMedicineClassification()) {
				for (MedicationKnowledgeMedicineClassificationComponent medicineClassification : medicationKnowledge.getMedicineClassification()) {

					for (CodeableConcept classification : medicineClassification.getClassification()) {

						if (classification.hasCoding()) {
							for (Coding code : classification.getCoding()) {
								rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"category", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
								resourcemetadataList.add(rMetadata);
							}
						}
					}

					if (medicineClassification.hasType() && medicineClassification.getType().hasCoding()) {
						for (Coding code : medicineClassification.getType().getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"category-type", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// code : token
			if (medicationKnowledge.hasCode() && medicationKnowledge.getCode().hasCoding()) {

				for (Coding code : medicationKnowledge.getCode().getCoding()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rMetadata);
				}
			}

			// doseform : token
			if (medicationKnowledge.hasDoseForm() && medicationKnowledge.getDoseForm().hasCoding()) {

				for (Coding code : medicationKnowledge.getDoseForm().getCoding()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"doseform", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rMetadata);
				}
			}

			// ingredient : reference
			// ingredient-code : token
			if (medicationKnowledge.hasIngredient()) {

				for (MedicationKnowledgeIngredientComponent ingredient : medicationKnowledge.getIngredient()) {

					if (ingredient.hasItemReference()) {
						rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "ingredient", 0, ingredient.getItemReference(), null);
						resourcemetadataList.addAll(rMetadataChain);
					}
					else if (ingredient.hasItemCodeableConcept() && ingredient.getItemCodeableConcept().hasCoding()) {
						for (Coding code : ingredient.getItemCodeableConcept().getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"ingredient-code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// manufacturer : reference
			if (medicationKnowledge.hasManufacturer()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "manufacturer", 0, medicationKnowledge.getManufacturer(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// monitoring-program-name : string
			// monitoring-program-type : token
			if (medicationKnowledge.hasMonitoringProgram()) {
				for (MedicationKnowledgeMonitoringProgramComponent monitoringProgram : medicationKnowledge.getMonitoringProgram()) {

					if (monitoringProgram.hasName()) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"monitoring-program-name", monitoringProgram.getName());
						resourcemetadataList.add(rMetadata);
					}

					if (monitoringProgram.hasType() && monitoringProgram.getType().hasCoding()) {

						for (Coding type : monitoringProgram.getType().getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"monitoring-program-type", type.getCode(), type.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(type));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// monograph : reference
			// monograph-type : token
			if (medicationKnowledge.hasMonograph()) {

				for (MedicationKnowledgeMonographComponent monograph : medicationKnowledge.getMonograph()) {

					if (monograph.hasSource()) {
						rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "monograph", 0, monograph.getSource(), null);
						resourcemetadataList.addAll(rMetadataChain);
					}

					if (monograph.hasType() && monograph.getType().hasCoding()) {

						for (Coding type : monograph.getType().getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"monograph-program-type", type.getCode(), type.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(type));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// source-cost : string
			if (medicationKnowledge.hasCost()) {
				for (MedicationKnowledgeCostComponent cost : medicationKnowledge.getCost()) {

					if (cost.hasSource()) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"source-cost", cost.getSource());
						resourcemetadataList.add(rMetadata);
					}
				}
			}

			// status : token
			if (medicationKnowledge.hasStatus() && medicationKnowledge.getStatus() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", medicationKnowledge.getStatus().toCode(), medicationKnowledge.getStatus().getSystem());
				resourcemetadataList.add(rMetadata);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		} finally {
	        rMetadata = null;
	        rMetadataChain = null;
            if (iMedicationKnowledge != null) {
                try {
                	iMedicationKnowledge.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
