/*
Copyright (c) 2020, AEGIS.net, Inc.
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following discodeSystemer.
 * Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following discodeSystemer in the documentation
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
import java.util.TimeZone;

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.CodeSystem.ConceptDefinitionComponent;
import org.hl7.fhir.r4.model.CodeSystem.ConceptDefinitionDesignationComponent;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.UsageContext;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataCodeSystem extends ResourcemetadataProxy {

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
        ByteArrayInputStream iCodeSystem = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

		try {
            // Extract and convert the resource contents to a CodeSystem object
			if (chainedResource != null) {
				iCodeSystem = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iCodeSystem = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            CodeSystem codeSystem = (CodeSystem) xmlP.parse(iCodeSystem);
            iCodeSystem.close();

			/*
             * Create new Resourcemetadata objects for each CodeSystem metadata value and add to the resourcemetadataList
			 */

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, codeSystem, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// context-type-[x] : composite
			StringBuilder conextTypeComposite = new StringBuilder("");

			// context
			if (codeSystem.hasUseContext()) {

				for (UsageContext context : codeSystem.getUseContext()) {

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

			if (codeSystem.hasConcept()) {

				for (ConceptDefinitionComponent concept : codeSystem.getConcept()) {

					// code: token
					if (concept.hasCode()) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"code", concept.getCode());
						resourcemetadataList.add(rMetadata);
					}

					// language: token
					if (concept.hasDesignation()) {

						for (ConceptDefinitionDesignationComponent designation : concept.getDesignation()) {

							if (designation.hasLanguage()) {
								rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"language", designation.getLanguage());
								resourcemetadataList.add(rMetadata);
							}
						}
					}
				}
			}

			// content-mode : token
			if (codeSystem.hasContent() && codeSystem.getContent() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"content-mode", codeSystem.getContent().toCode(), codeSystem.getContent().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// date : date
			if (codeSystem.hasDate()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(codeSystem.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(codeSystem.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rMetadata);
			}

			// description : string
			if (codeSystem.hasDescription()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"description", codeSystem.getDescription());
				resourcemetadataList.add(rMetadata);
			}

			// identifier : token
			if (codeSystem.hasIdentifier()) {

				for (Identifier identifier : codeSystem.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}
			// jurisdiction : token
			if (codeSystem.hasJurisdiction()) {

				for (CodeableConcept jurisdiction : codeSystem.getJurisdiction()) {

					if (jurisdiction.hasCoding()) {
						for (Coding code : jurisdiction.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"jurisdiction", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// name : string
			if (codeSystem.hasName()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"name", codeSystem.getName());
				resourcemetadataList.add(rMetadata);
			}

			// publisher : string
			if (codeSystem.hasPublisher()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"publisher", codeSystem.getPublisher());
				resourcemetadataList.add(rMetadata);
			}

			// status : token
			if (codeSystem.hasStatus() && codeSystem.getStatus() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", codeSystem.getStatus().toCode(), codeSystem.getStatus().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// supplements : reference - supplements is a Canonical, no Reference.identifier
			if (codeSystem.hasSupplements()) {
				String supplementsReference = generateFullLocalReference(codeSystem.getSupplements(), baseUrl);

				Resourcemetadata rSupplements = generateResourcemetadata(resource, chainedResource, chainedParameter+"supplements", supplementsReference);
				resourcemetadataList.add(rSupplements);

				if (chainedResource == null) {
					// Add chained parameters
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "supplements", 0, codeSystem.getSupplements(), null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// system : uri
			// url : uri
			if (codeSystem.hasUrl()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"system", codeSystem.getUrl());
				resourcemetadataList.add(rMetadata);

				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"url", codeSystem.getUrl());
				resourcemetadataList.add(rMetadata);
			}

			// title : string
			if (codeSystem.hasTitle()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"title", codeSystem.getTitle());
				resourcemetadataList.add(rMetadata);
			}

			// version : string
			if (codeSystem.hasVersion()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"version", codeSystem.getVersion());
				resourcemetadataList.add(rMetadata);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		} finally {
	        rMetadata = null;
	        rMetadataChain = null;
            if (iCodeSystem != null) {
                try {
                	iCodeSystem.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
