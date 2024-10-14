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

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.MolecularSequence;
import org.hl7.fhir.r4.model.MolecularSequence.MolecularSequenceVariantComponent;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataMolecularSequence extends ResourcemetadataProxy {

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
        ByteArrayInputStream iMolecularSequence = null;

		try {
            // Extract and convert the resource contents to a MolecularSequence object
			if (chainedResource != null) {
				iMolecularSequence = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iMolecularSequence = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            MolecularSequence molecularSequence = (MolecularSequence) xmlP.parse(iMolecularSequence);
            iMolecularSequence.close();

			/*
             * Create new Resourcemetadata objects for each MolecularSequence metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, molecularSequence, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			if (molecularSequence.getId() != null) {
				Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", molecularSequence.getId());
				resourcemetadataList.add(_id);
			}

			// _language : token
			if (molecularSequence.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", molecularSequence.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (molecularSequence.getMeta() != null && molecularSequence.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(molecularSequence.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(molecularSequence.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}


			int coordinateSystem = 0;
			if (molecularSequence.hasCoordinateSystem()) {
				coordinateSystem = molecularSequence.getCoordinateSystem();
			}
			String chromosome = null;
			StringBuffer sbChromosomeVariantCoordinate = null;
			StringBuffer sbChromosomeWindowCoordinate = null;
			String referenceseqid = null;
			StringBuffer sbReferenceSeqIdVariantCoordinate = null;
			StringBuffer sbReferenceSeqIdWindowCoordinate = null;

			// molecularSequence.referenceSeq
			if (molecularSequence.hasReferenceSeq()) {

				Resourcemetadata rwindowCoordinate = null;

				// chromosome : token
				if (molecularSequence.getReferenceSeq().hasChromosome() && molecularSequence.getReferenceSeq().getChromosome().hasCoding()) {
					for (Coding code : molecularSequence.getReferenceSeq().getChromosome().getCoding()) {
						rwindowCoordinate = generateResourcemetadata(resource, chainedResource, chainedParameter+"chromosome", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
						resourcemetadataList.add(rwindowCoordinate);
					}

					chromosome = molecularSequence.getReferenceSeq().getChromosome().getCodingFirstRep().getCode();
				}

				// referenceseqid : token
				if (molecularSequence.getReferenceSeq().hasReferenceSeqId() && molecularSequence.getReferenceSeq().getReferenceSeqId().hasCoding()) {
					for (Coding code : molecularSequence.getReferenceSeq().getReferenceSeqId().getCoding()) {
						rwindowCoordinate = generateResourcemetadata(resource, chainedResource, chainedParameter+"referenceseqid", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
						resourcemetadataList.add(rwindowCoordinate);

					}

					referenceseqid = molecularSequence.getReferenceSeq().getReferenceSeqId().getCodingFirstRep().getCode();
				}

				StringBuffer sbChromosomeCoordinate = new StringBuffer("");

				// window-start : number
				if (molecularSequence.getReferenceSeq().hasWindowStart()) {
					rwindowCoordinate = generateResourcemetadata(resource, chainedResource, chainedParameter+"window-start", molecularSequence.getReferenceSeq().getWindowStartElement().asStringValue());
					resourcemetadataList.add(rwindowCoordinate);

					if (coordinateSystem == 0) {
						sbChromosomeCoordinate.append(String.format("%09d", molecularSequence.getReferenceSeq().getWindowStart() - 1));
					}
					else {
						sbChromosomeCoordinate.append(String.format("%09d", molecularSequence.getReferenceSeq().getWindowStart()));
					}
				}
				else {
					sbChromosomeCoordinate.append("000000000");
				}

				// window-end : number
				if (molecularSequence.getReferenceSeq().hasWindowEnd()) {
					rwindowCoordinate = generateResourcemetadata(resource, chainedResource, chainedParameter+"window-end", molecularSequence.getReferenceSeq().getWindowEndElement().asStringValue());
					resourcemetadataList.add(rwindowCoordinate);

					sbChromosomeCoordinate.append(String.format("%09d", molecularSequence.getReferenceSeq().getWindowEnd()));
				}
				else {
					sbChromosomeCoordinate.append("999999999");
				}

				// chromosome-window-coordinate : composite
				if (chromosome != null) {
					sbChromosomeWindowCoordinate = new StringBuffer(chromosome).append(":").append(sbChromosomeCoordinate.toString());

					rwindowCoordinate = generateResourcemetadata(resource, chainedResource, chainedParameter+"chromosome-window-coordinate", sbChromosomeWindowCoordinate.toString());
					resourcemetadataList.add(rwindowCoordinate);
				}

				// referenceseqid-window-coordinate : composite
				if (referenceseqid != null) {
					sbReferenceSeqIdWindowCoordinate = new StringBuffer(referenceseqid).append(":").append(sbChromosomeCoordinate.toString());;

					rwindowCoordinate = generateResourcemetadata(resource, chainedResource, chainedParameter+"referenceseqid-window-coordinate", sbReferenceSeqIdWindowCoordinate.toString());
					resourcemetadataList.add(rwindowCoordinate);
				}
			}

			// molecularSequence.variant
			if (molecularSequence.hasVariant()) {

				StringBuffer sbVariantCoordinate = null;
				Resourcemetadata rVariantCoordinate = null;

				for (MolecularSequenceVariantComponent variant : molecularSequence.getVariant()) {

					sbVariantCoordinate = new StringBuffer("");

					if (variant.hasStart()) {
						rVariantCoordinate = generateResourcemetadata(resource, chainedResource, chainedParameter+"variant-start", variant.getStartElement().asStringValue());
						resourcemetadataList.add(rVariantCoordinate);

						if (coordinateSystem == 0) {
							sbVariantCoordinate.append(String.format("%09d", variant.getStart() - 1));
						}
						else {
							sbVariantCoordinate.append(String.format("%09d", variant.getStart()));
						}
					}
					else {
						sbVariantCoordinate.append("000000000");
					}
					sbVariantCoordinate.append("$");
					if (variant.hasEnd()) {
						rVariantCoordinate = generateResourcemetadata(resource, chainedResource, chainedParameter+"variant-end", variant.getEndElement().asStringValue());
						resourcemetadataList.add(rVariantCoordinate);

						sbVariantCoordinate.append(String.format("%09d", variant.getEnd()));
					}
					else {
						sbVariantCoordinate.append("999999999");
					}

					// chromosome-variant-coordinate : composite
					if (chromosome != null) {
						sbChromosomeVariantCoordinate = new StringBuffer(chromosome).append(":").append(sbVariantCoordinate.toString());

						rVariantCoordinate = generateResourcemetadata(resource, chainedResource, chainedParameter+"chromosome-variant-coordinate", sbChromosomeVariantCoordinate.toString());
						resourcemetadataList.add(rVariantCoordinate);
					}

					// referenceseqid-variant-coordinate : composite
					if (referenceseqid != null) {
						sbReferenceSeqIdVariantCoordinate = new StringBuffer(referenceseqid).append(":").append(sbVariantCoordinate.toString());;

						rVariantCoordinate = generateResourcemetadata(resource, chainedResource, chainedParameter+"referenceseqid-variant-coordinate", sbReferenceSeqIdVariantCoordinate.toString());
						resourcemetadataList.add(rVariantCoordinate);
					}

				}

			}

			// identifier : token
			if (molecularSequence.hasIdentifier()) {

				for (Identifier identifier : molecularSequence.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// patient : reference
			if (molecularSequence.hasPatient() && molecularSequence.getPatient().hasReference()) {
				Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", generateFullLocalReference(molecularSequence.getPatient().getReference(), baseUrl));
				resourcemetadataList.add(rPatient);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rPatientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, molecularSequence.getPatient().getReference(), null);
					resourcemetadataList.addAll(rPatientChain);
				}
			}

			// type : token
			if (molecularSequence.hasType() && molecularSequence.getType() != null) {
				Resourcemetadata rType = generateResourcemetadata(resource, chainedResource, chainedParameter+"type", molecularSequence.getType().toCode(), molecularSequence.getType().getSystem());
				resourcemetadataList.add(rType);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return resourcemetadataList;
	}

}
