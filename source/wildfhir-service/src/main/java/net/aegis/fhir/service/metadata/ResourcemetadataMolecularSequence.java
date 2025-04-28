/*
Copyright (c) 2013-2015, AEGIS.net, Inc.
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.
 * Neither the name of AEGIS nor the names of its contributors may be used to
   endorse or promote products derived from this software without specific
   prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.

 */
package net.aegis.fhir.service.metadata;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;

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
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

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

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, molecularSequence, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);


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

				// chromosome : token
				if (molecularSequence.getReferenceSeq().hasChromosome() && molecularSequence.getReferenceSeq().getChromosome().hasCoding()) {
					for (Coding code : molecularSequence.getReferenceSeq().getChromosome().getCoding()) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"chromosome", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
						resourcemetadataList.add(rMetadata);
					}

					chromosome = molecularSequence.getReferenceSeq().getChromosome().getCodingFirstRep().getCode();
				}

				// referenceseqid : token
				if (molecularSequence.getReferenceSeq().hasReferenceSeqId() && molecularSequence.getReferenceSeq().getReferenceSeqId().hasCoding()) {
					for (Coding code : molecularSequence.getReferenceSeq().getReferenceSeqId().getCoding()) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"referenceseqid", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
						resourcemetadataList.add(rMetadata);

					}

					referenceseqid = molecularSequence.getReferenceSeq().getReferenceSeqId().getCodingFirstRep().getCode();
				}

				StringBuffer sbChromosomeCoordinate = new StringBuffer("");

				// window-start : number
				if (molecularSequence.getReferenceSeq().hasWindowStart()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"window-start", molecularSequence.getReferenceSeq().getWindowStartElement().asStringValue());
					resourcemetadataList.add(rMetadata);

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
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"window-end", molecularSequence.getReferenceSeq().getWindowEndElement().asStringValue());
					resourcemetadataList.add(rMetadata);

					sbChromosomeCoordinate.append(String.format("%09d", molecularSequence.getReferenceSeq().getWindowEnd()));
				}
				else {
					sbChromosomeCoordinate.append("999999999");
				}

				// chromosome-window-coordinate : composite
				if (chromosome != null) {
					sbChromosomeWindowCoordinate = new StringBuffer(chromosome).append(":").append(sbChromosomeCoordinate.toString());

					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"chromosome-window-coordinate", sbChromosomeWindowCoordinate.toString(), null, null, null, "COMPOSITE");
					resourcemetadataList.add(rMetadata);
				}

				// referenceseqid-window-coordinate : composite
				if (referenceseqid != null) {
					sbReferenceSeqIdWindowCoordinate = new StringBuffer(referenceseqid).append(":").append(sbChromosomeCoordinate.toString());;

					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"referenceseqid-window-coordinate", sbReferenceSeqIdWindowCoordinate.toString(), null, null, null, "COMPOSITE");
					resourcemetadataList.add(rMetadata);
				}
			}

			// molecularSequence.variant
			if (molecularSequence.hasVariant()) {

				StringBuffer sbVariantCoordinate = null;

				for (MolecularSequenceVariantComponent variant : molecularSequence.getVariant()) {

					sbVariantCoordinate = new StringBuffer("");

					if (variant.hasStart()) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"variant-start", variant.getStartElement().asStringValue());
						resourcemetadataList.add(rMetadata);

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
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"variant-end", variant.getEndElement().asStringValue());
						resourcemetadataList.add(rMetadata);

						sbVariantCoordinate.append(String.format("%09d", variant.getEnd()));
					}
					else {
						sbVariantCoordinate.append("999999999");
					}

					// chromosome-variant-coordinate : composite
					if (chromosome != null) {
						sbChromosomeVariantCoordinate = new StringBuffer(chromosome).append(":").append(sbVariantCoordinate.toString());

						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"chromosome-variant-coordinate", sbChromosomeVariantCoordinate.toString(), null, null, null, "COMPOSITE");
						resourcemetadataList.add(rMetadata);
					}

					// referenceseqid-variant-coordinate : composite
					if (referenceseqid != null) {
						sbReferenceSeqIdVariantCoordinate = new StringBuffer(referenceseqid).append(":").append(sbVariantCoordinate.toString());;

						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"referenceseqid-variant-coordinate", sbReferenceSeqIdVariantCoordinate.toString(), null, null, null, "COMPOSITE");
						resourcemetadataList.add(rMetadata);
					}

				}

			}

			// identifier : token
			if (molecularSequence.hasIdentifier()) {

				for (Identifier identifier : molecularSequence.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}

			// patient : reference
			if (molecularSequence.hasPatient()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "patient", 0, molecularSequence.getPatient(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// type : token
			if (molecularSequence.hasType() && molecularSequence.getType() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"type", molecularSequence.getType().toCode(), molecularSequence.getType().getSystem());
				resourcemetadataList.add(rMetadata);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		} finally {
	        rMetadata = null;
	        rMetadataChain = null;
            if (iMolecularSequence != null) {
                try {
                	iMolecularSequence.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
