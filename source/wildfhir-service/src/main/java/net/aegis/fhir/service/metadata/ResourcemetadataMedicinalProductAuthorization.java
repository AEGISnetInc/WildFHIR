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
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.MedicinalProductAuthorization;

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataMedicinalProductAuthorization extends ResourcemetadataProxy {

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
        ByteArrayInputStream iMedicinalProductAuthorization = null;

		try {
            // Extract and convert the resource contents to a MedicinalProductAuthorization object
			if (chainedResource != null) {
				iMedicinalProductAuthorization = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iMedicinalProductAuthorization = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            MedicinalProductAuthorization medicinalProductAuthorization = (MedicinalProductAuthorization) xmlP.parse(iMedicinalProductAuthorization);
            iMedicinalProductAuthorization.close();

			/*
             * Create new Resourcemetadata objects for each MedicinalProductAuthorization metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, medicinalProductAuthorization, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			if (medicinalProductAuthorization.getId() != null) {
				Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", medicinalProductAuthorization.getId());
				resourcemetadataList.add(_id);
			}

			// _language : token
			if (medicinalProductAuthorization.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", medicinalProductAuthorization.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (medicinalProductAuthorization.getMeta() != null && medicinalProductAuthorization.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(medicinalProductAuthorization.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(medicinalProductAuthorization.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// country : token
			if (medicinalProductAuthorization.hasCountry()) {

				Resourcemetadata rCode = null;
				for (CodeableConcept country : medicinalProductAuthorization.getCountry()) {

					if (country.hasCoding()) {
						for (Coding code : country.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"country", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// holder : reference
			if (medicinalProductAuthorization.hasHolder() && medicinalProductAuthorization.getHolder().hasReference()) {
				Resourcemetadata rHolder = generateResourcemetadata(resource, chainedResource, chainedParameter+"holder", generateFullLocalReference(medicinalProductAuthorization.getHolder().getReference(), baseUrl));
				resourcemetadataList.add(rHolder);

				if (chainedResource == null) {
					// Add chained parameters for any
					List<Resourcemetadata> rHolderChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "holder", 0, medicinalProductAuthorization.getHolder().getReference(), null);
					resourcemetadataList.addAll(rHolderChain);
				}
			}

			// identifier : token
			if (medicinalProductAuthorization.hasIdentifier()) {

				Resourcemetadata rIdentifier = null;
				for (Identifier identifier : medicinalProductAuthorization.getIdentifier()) {

					rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// status : token
			if (medicinalProductAuthorization.hasStatus() && medicinalProductAuthorization.getStatus().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : medicinalProductAuthorization.getStatus().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			// subject : reference
			if (medicinalProductAuthorization.hasSubject() && medicinalProductAuthorization.getSubject().hasReference()) {
				Resourcemetadata rSubject = generateResourcemetadata(resource, chainedResource, chainedParameter+"subject", generateFullLocalReference(medicinalProductAuthorization.getSubject().getReference(), baseUrl));
				resourcemetadataList.add(rSubject);

				if (chainedResource == null) {
					// Add chained parameters for any
					List<Resourcemetadata> rSubjectChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "subject", 0, medicinalProductAuthorization.getSubject().getReference(), null);
					resourcemetadataList.addAll(rSubjectChain);
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
