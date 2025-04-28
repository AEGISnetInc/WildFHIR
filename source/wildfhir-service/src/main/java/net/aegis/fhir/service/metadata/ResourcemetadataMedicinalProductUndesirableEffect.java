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
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.MedicinalProductUndesirableEffect;
import org.hl7.fhir.r4.model.Reference;

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataMedicinalProductUndesirableEffect extends ResourcemetadataProxy {

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
        ByteArrayInputStream iMedicinalProductUndesirableEffect = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

		try {
            // Extract and convert the resource contents to a MedicinalProductUndesirableEffect object
			if (chainedResource != null) {
				iMedicinalProductUndesirableEffect = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iMedicinalProductUndesirableEffect = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            MedicinalProductUndesirableEffect medicinalProductUndesirableEffect = (MedicinalProductUndesirableEffect) xmlP.parse(iMedicinalProductUndesirableEffect);
            iMedicinalProductUndesirableEffect.close();

			/*
             * Create new Resourcemetadata objects for each MedicinalProductUndesirableEffect metadata value and add to the resourcemetadataList
			 */

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, medicinalProductUndesirableEffect, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// classification : token
			if (medicinalProductUndesirableEffect.hasClassification() && medicinalProductUndesirableEffect.getClassification().hasCoding()) {

				for (Coding code : medicinalProductUndesirableEffect.getClassification().getCoding()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"classification", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rMetadata);
				}
			}

			// subject : reference
			if (medicinalProductUndesirableEffect.hasSubject()) {

				for (Reference subject : medicinalProductUndesirableEffect.getSubject()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "subject", 0, subject, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// symptom : token
			if (medicinalProductUndesirableEffect.hasSymptomConditionEffect() && medicinalProductUndesirableEffect.getSymptomConditionEffect().hasCoding()) {

				for (Coding code : medicinalProductUndesirableEffect.getSymptomConditionEffect().getCoding()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"symptom", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rMetadata);
				}
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		} finally {
	        rMetadata = null;
	        rMetadataChain = null;
            if (iMedicinalProductUndesirableEffect != null) {
                try {
                	iMedicinalProductUndesirableEffect.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
