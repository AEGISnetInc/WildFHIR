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
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.MedicinalProductUndesirableEffect;
import org.hl7.fhir.r4.model.Reference;

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

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
        ByteArrayInputStream iMedicinalProductUndesirableEffect = null;

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

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, medicinalProductUndesirableEffect, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", medicinalProductUndesirableEffect.getId());
			resourcemetadataList.add(_id);

			// _language : token
			if (medicinalProductUndesirableEffect.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", medicinalProductUndesirableEffect.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (medicinalProductUndesirableEffect.getMeta() != null && medicinalProductUndesirableEffect.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(medicinalProductUndesirableEffect.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(medicinalProductUndesirableEffect.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// classification : token
			if (medicinalProductUndesirableEffect.hasClassification() && medicinalProductUndesirableEffect.getClassification().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : medicinalProductUndesirableEffect.getClassification().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"classification", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			// subject : reference
			if (medicinalProductUndesirableEffect.hasSubject()) {

				Resourcemetadata rSubject = null;
				List<Resourcemetadata> rSubjectChain = null;
				for (Reference subject : medicinalProductUndesirableEffect.getSubject()) {

					if (subject.hasReference()) {
						rSubject = generateResourcemetadata(resource, chainedResource, chainedParameter+"subject", generateFullLocalReference(subject.getReference(), baseUrl));
						resourcemetadataList.add(rSubject);

						if (chainedResource == null) {
							// Add chained parameters for any
							rSubjectChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "subject", 0, subject.getReference());
							resourcemetadataList.addAll(rSubjectChain);
						}
					}
				}
			}

			// symptom : token
			if (medicinalProductUndesirableEffect.hasSymptomConditionEffect() && medicinalProductUndesirableEffect.getSymptomConditionEffect().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : medicinalProductUndesirableEffect.getSymptomConditionEffect().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"symptom", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
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
