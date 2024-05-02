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
import org.hl7.fhir.r4.model.MedicinalProductIndication;
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
public class ResourcemetadataMedicinalProductIndication extends ResourcemetadataProxy {

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
        ByteArrayInputStream iMedicinalProductIndication = null;

		try {
            // Extract and convert the resource contents to a MedicinalProductIndication object
			if (chainedResource != null) {
				iMedicinalProductIndication = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iMedicinalProductIndication = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            MedicinalProductIndication medicinalProductIndication = (MedicinalProductIndication) xmlP.parse(iMedicinalProductIndication);
            iMedicinalProductIndication.close();

			/*
             * Create new Resourcemetadata objects for each MedicinalProductIndication metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, medicinalProductIndication, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", medicinalProductIndication.getId());
			resourcemetadataList.add(_id);

			// _language : token
			if (medicinalProductIndication.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", medicinalProductIndication.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (medicinalProductIndication.getMeta() != null && medicinalProductIndication.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(medicinalProductIndication.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(medicinalProductIndication.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// disease : token
			if (medicinalProductIndication.hasDiseaseSymptomProcedure() && medicinalProductIndication.getDiseaseSymptomProcedure().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : medicinalProductIndication.getDiseaseSymptomProcedure().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"disease", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			// disease-status : token
			if (medicinalProductIndication.hasDiseaseStatus() && medicinalProductIndication.getDiseaseStatus().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : medicinalProductIndication.getDiseaseStatus().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"disease-status", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			// subject : reference
			if (medicinalProductIndication.hasSubject()) {

				Resourcemetadata rSubject = null;
				List<Resourcemetadata> rSubjectChain = null;
				for (Reference subject : medicinalProductIndication.getSubject()) {

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

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return resourcemetadataList;
	}

}
