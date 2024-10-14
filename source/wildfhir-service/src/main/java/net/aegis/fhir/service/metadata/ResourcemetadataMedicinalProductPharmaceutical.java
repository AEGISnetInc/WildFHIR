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
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.MedicinalProductPharmaceutical;
import org.hl7.fhir.r4.model.MedicinalProductPharmaceutical.MedicinalProductPharmaceuticalRouteOfAdministrationComponent;
import org.hl7.fhir.r4.model.MedicinalProductPharmaceutical.MedicinalProductPharmaceuticalRouteOfAdministrationTargetSpeciesComponent;
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
public class ResourcemetadataMedicinalProductPharmaceutical extends ResourcemetadataProxy {

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
        ByteArrayInputStream iMedicinalProductPharmaceutical = null;

		try {
            // Extract and convert the resource contents to a MedicinalProductPharmaceutical object
			if (chainedResource != null) {
				iMedicinalProductPharmaceutical = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iMedicinalProductPharmaceutical = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            MedicinalProductPharmaceutical medicinalProductPharmaceutical = (MedicinalProductPharmaceutical) xmlP.parse(iMedicinalProductPharmaceutical);
            iMedicinalProductPharmaceutical.close();

			/*
             * Create new Resourcemetadata objects for each MedicinalProductPharmaceutical metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, medicinalProductPharmaceutical, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			if (medicinalProductPharmaceutical.getId() != null) {
				Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", medicinalProductPharmaceutical.getId());
				resourcemetadataList.add(_id);
			}

			// _language : token
			if (medicinalProductPharmaceutical.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", medicinalProductPharmaceutical.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (medicinalProductPharmaceutical.getMeta() != null && medicinalProductPharmaceutical.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(medicinalProductPharmaceutical.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(medicinalProductPharmaceutical.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// identifier : token
			if (medicinalProductPharmaceutical.hasIdentifier()) {

				Resourcemetadata rIdentifier = null;
				for (Identifier identifier : medicinalProductPharmaceutical.getIdentifier()) {

					rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// route : token
			// species : token
			if (medicinalProductPharmaceutical.hasRouteOfAdministration()) {

				Resourcemetadata rCode = null;
				for (MedicinalProductPharmaceuticalRouteOfAdministrationComponent route : medicinalProductPharmaceutical.getRouteOfAdministration()) {

					if (route.hasCode() && route.getCode().hasCoding()) {
						for (Coding code : route.getCode().getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"route", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}

					if (route.hasTargetSpecies()) {
						for (MedicinalProductPharmaceuticalRouteOfAdministrationTargetSpeciesComponent targetSpecies : route.getTargetSpecies()) {

							if (targetSpecies.hasCode() && targetSpecies.getCode().hasCoding()) {
								for (Coding code : targetSpecies.getCode().getCoding()) {
									rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"species", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
									resourcemetadataList.add(rCode);
								}
							}
						}
					}
				}
			}

			// device : reference
			if (medicinalProductPharmaceutical.hasDevice()) {

				Resourcemetadata rDevice = null;
				List<Resourcemetadata> rDeviceChain = null;
				for (Reference device : medicinalProductPharmaceutical.getDevice()) {

					if (device.hasReference()) {
						rDevice = generateResourcemetadata(resource, chainedResource, chainedParameter+"device", generateFullLocalReference(device.getReference(), baseUrl));
						resourcemetadataList.add(rDevice);

						if (chainedResource == null) {
							// Add chained parameters for any
							rDeviceChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "device", 0, device.getReference(), null);
							resourcemetadataList.addAll(rDeviceChain);
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
