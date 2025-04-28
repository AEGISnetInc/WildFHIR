/*
Copyright (c) 2020, AEGIS.net, Inc.
All rights reserved.

Redistribution and use in source and condition forms, with or without modification,
are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disstructureDefinitioner.
 * Redistributions in condition form must reproduce the above copyright notice,
   this list of conditions and the following disstructureDefinitioner in the documentation
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
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ElementDefinition;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.UsageContext;
import org.hl7.fhir.r4.model.StructureDefinition.StructureDefinitionContextComponent;

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataStructureDefinition extends ResourcemetadataProxy {

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
        ByteArrayInputStream iStructureDefinition = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

		try {
            // Extract and convert the resource contents to a StructureDefinition object
			if (chainedResource != null) {
				iStructureDefinition = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iStructureDefinition = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            StructureDefinition structureDefinition = (StructureDefinition) xmlP.parse(iStructureDefinition);
            iStructureDefinition.close();

			/*
             * Create new Resourcemetadata objects for each StructureDefinition metadata value and add to the resourcemetadataList
			 */

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, structureDefinition, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// abstract : token
			if (structureDefinition.hasAbstract()) {
				Resourcemetadata rAbstract = generateResourcemetadata(resource, chainedResource, chainedParameter+"abstract", structureDefinition.getAbstractElement().getValue().toString());
				resourcemetadataList.add(rAbstract);
			}

			// base : uri
			if (structureDefinition.hasBaseDefinition()) {
				Resourcemetadata rBase = generateResourcemetadata(resource, chainedResource, chainedParameter+"base", structureDefinition.getBaseDefinition());
				resourcemetadataList.add(rBase);
			}

			// context-type-[x] : composite
			StringBuilder conextTypeComposite = new StringBuilder("");

			// context
			if (structureDefinition.hasUseContext()) {

				for (UsageContext context : structureDefinition.getUseContext()) {

					// context-type : token
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"context-type", context.getCode().getCode(), context.getCode().getSystem());
					resourcemetadataList.add(rMetadata);

					// Start building the context-type-[x] composite
					if (context.getCode().hasSystem()) {
						conextTypeComposite.append(context.getCode().getSystem());
					}
					conextTypeComposite.append("|").append(context.getCode().getCode());

					if (context.hasValueCodeableConcept() && context.getValueCodeableConcept().hasCoding()) {
						// context : token
						for (Coding code : context.getValueCodeableConcept().getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"context", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}

						// context-type-value : composite
						conextTypeComposite.append("$");
						if (context.getValueCodeableConcept().getCodingFirstRep().hasSystem()) {
							conextTypeComposite.append(context.getValueCodeableConcept().getCodingFirstRep().getSystem());
						}
						conextTypeComposite.append("|").append(context.getValueCodeableConcept().getCodingFirstRep().getCode());

						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"context-type-value", conextTypeComposite.toString(), null, null, null, "COMPOSITE");
						resourcemetadataList.add(rMetadata);
					}

					if (context.hasValueQuantity()) {
						// context-quantity : quantity
						String quantityCode = (context.getValueQuantity().getCode() != null ? context.getValueQuantity().getCode() : context.getValueQuantity().getUnit());
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"context-quantity", context.getValueQuantity().getValue().toPlainString(), context.getValueQuantity().getSystem(), quantityCode);
						resourcemetadataList.add(rMetadata);

						// context-type-quantity : composite
						conextTypeComposite.append("$");
						if (context.getValueQuantity().hasValue()) {
							conextTypeComposite.append(context.getValueQuantity().getValue().toPlainString());
						}
						conextTypeComposite.append("|");
						if (context.getValueQuantity().hasSystem()) {
							conextTypeComposite.append(context.getValueQuantity().getSystem());
						}
						conextTypeComposite.append("|").append(quantityCode);

						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"context-type-quantity", conextTypeComposite.toString(), null, null, null, "COMPOSITE");
						resourcemetadataList.add(rMetadata);
					}

					if (context.hasValueRange()) {
						// context-quantity : range
						String quantityCode = "";
						Quantity rangeValue = null;
						if (context.getValueRange().hasLow()) {
							rangeValue = context.getValueRange().getLow();
						}
						else if (context.getValueRange().hasHigh()) {
							rangeValue = context.getValueRange().getHigh();
						}
						if (rangeValue != null) {
							quantityCode = (rangeValue.getCode() != null ? rangeValue.getCode() : rangeValue.getUnit());
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"context-quantity", rangeValue.getValue().toPlainString(), rangeValue.getSystem(), quantityCode);
							resourcemetadataList.add(rMetadata);

							// context-type-quantity : composite
							conextTypeComposite.append("$");
							if (rangeValue.hasValue()) {
								conextTypeComposite.append(rangeValue.getValue().toPlainString());
							}
							conextTypeComposite.append("|");
							if (rangeValue.hasSystem()) {
								conextTypeComposite.append(rangeValue.getSystem());
							}
							conextTypeComposite.append("|").append(quantityCode);

							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"context-type-quantity", conextTypeComposite.toString(), null, null, null, "COMPOSITE");
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// date : date
			if (structureDefinition.hasDate()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(structureDefinition.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(structureDefinition.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rMetadata);
			}

			// derivation : token
			if (structureDefinition.hasDerivation() && structureDefinition.getDerivation() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"derivation", structureDefinition.getDerivation().toCode(), structureDefinition.getDerivation().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// description : string
			if (structureDefinition.hasDescription()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"description", structureDefinition.getDescription());
				resourcemetadataList.add(rMetadata);
			}

			// experimental : token
			if (structureDefinition.hasExperimental()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"experimental", structureDefinition.getExperimentalElement().asStringValue());
				resourcemetadataList.add(rMetadata);
			}

			// ext-context : string
			if (structureDefinition.hasContext()) {
				for (StructureDefinitionContextComponent context : structureDefinition.getContext()) {

					if (context.hasType() && context.getType() != null) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"ext-context", context.getType().toCode(), context.getType().getSystem());
						resourcemetadataList.add(rMetadata);
					}
				}
			}

			// identifier : token
			if (structureDefinition.hasIdentifier()) {

				for (Identifier identifier : structureDefinition.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}

			// jurisdiction : token
			if (structureDefinition.hasJurisdiction()) {
				for (CodeableConcept jurisdiction : structureDefinition.getJurisdiction()) {

					if (jurisdiction.hasCoding()) {
						for (Coding code : jurisdiction.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"jurisdiction", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// keyword : token
			if (structureDefinition.hasKeyword()) {

				for (Coding code : structureDefinition.getKeyword()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"keyword", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rMetadata);

				}
			}

			// kind : token
			if (structureDefinition.hasKind() && structureDefinition.getKind() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"kind", structureDefinition.getKind().toCode(), structureDefinition.getKind().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// name : string
			if (structureDefinition.hasName()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"name", structureDefinition.getName());
				resourcemetadataList.add(rMetadata);
			}

			// publisher : token
			if (structureDefinition.hasPublisher()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"publisher", structureDefinition.getPublisher());
				resourcemetadataList.add(rMetadata);
			}

			// status : token
			if (structureDefinition.hasStatus() && structureDefinition.getStatus() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", structureDefinition.getStatus().toCode(), structureDefinition.getStatus().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// title : string
			if (structureDefinition.hasTitle()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"title", structureDefinition.getTitle());
				resourcemetadataList.add(rMetadata);
			}

			// type : token
			if (structureDefinition.hasType()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"type", structureDefinition.getType());
				resourcemetadataList.add(rMetadata);
			}

			// url : uri
			if (structureDefinition.hasUrl()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"url", structureDefinition.getUrl());
				resourcemetadataList.add(rMetadata);
			}

			// version : token
			if (structureDefinition.hasVersion()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"version", structureDefinition.getVersion());
				resourcemetadataList.add(rMetadata);
			}

			// Prevent duplicate 'base-path' and 'path' parameter entries
			HashSet<String> basePathList = new HashSet<String>();
			HashSet<String> pathList = new HashSet<String>();

			// base-path : token (continued)
			// path : token (continued)
			// valueset : reference
			if (structureDefinition.hasSnapshot()) {

				if (structureDefinition.getSnapshot().hasElement()) {

					for (ElementDefinition snapshotElement : structureDefinition.getSnapshot().getElement()) {

						if (snapshotElement.hasBase() && snapshotElement.getBase().hasPath() && !basePathList.contains(snapshotElement.getBase().getPath())) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"base-path", snapshotElement.getBase().getPath());
							resourcemetadataList.add(rMetadata);
							basePathList.add(snapshotElement.getBase().getPath());
						}

						if (snapshotElement.hasPath() && !basePathList.contains(snapshotElement.getPath())) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"path", snapshotElement.getPath());
							resourcemetadataList.add(rMetadata);
							pathList.add(snapshotElement.getPath());
						}

						// valueset - binding.valueset is a Canonical, no Reference.identifier
						if (snapshotElement.hasBinding() && snapshotElement.getBinding().hasValueSet()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"valueset", snapshotElement.getBinding().getValueSet());
							resourcemetadataList.add(rMetadata);

							if (chainedResource == null) {
								// Add chained parameters for any
								rMetadataChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "valueset", 0, snapshotElement.getBinding().getValueSet(), null);
								resourcemetadataList.addAll(rMetadataChain);
							}
						}
					}
				}
			}

			// base-path : token
			// path : token
			if (structureDefinition.hasDifferential()) {

				if (structureDefinition.getDifferential().hasElement()) {

					for (ElementDefinition differentialElement : structureDefinition.getDifferential().getElement()) {

						if (differentialElement.hasBase() && differentialElement.getBase().hasPath() && !basePathList.contains(differentialElement.getBase().getPath())) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"base-path", differentialElement.getBase().getPath());
							resourcemetadataList.add(rMetadata);
							basePathList.add(differentialElement.getBase().getPath());
						}

						if (differentialElement.hasPath() && !pathList.contains(differentialElement.getPath())) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"path", differentialElement.getPath());
							resourcemetadataList.add(rMetadata);
							pathList.add(differentialElement.getPath());
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
            if (iStructureDefinition != null) {
                try {
                	iStructureDefinition.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
