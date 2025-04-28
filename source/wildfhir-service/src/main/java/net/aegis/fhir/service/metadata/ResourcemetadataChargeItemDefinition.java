/**
 *
 */
package net.aegis.fhir.service.metadata;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.ChargeItemDefinition;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.UsageContext;

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataChargeItemDefinition extends ResourcemetadataProxy {

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
        ByteArrayInputStream iChargeItemDefinition = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

		try {
			// Extract and convert the resource contents to a ChargeItemDefinition object
			if (chainedResource != null) {
				iChargeItemDefinition = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iChargeItemDefinition = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			ChargeItemDefinition chargeItemDefinition = (ChargeItemDefinition) xmlP.parse(iChargeItemDefinition);
			iChargeItemDefinition.close();

			/*
			 * Create new Resourcemetadata objects for each ChargeItemDefinition metadata value and add to the resourcemetadataList
			 */

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, chargeItemDefinition, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// context-type-[x] : composite
			StringBuilder conextTypeComposite = new StringBuilder("");

			// context
			if (chargeItemDefinition.hasUseContext()) {

				for (UsageContext context : chargeItemDefinition.getUseContext()) {

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
			if (chargeItemDefinition.hasDate()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(chargeItemDefinition.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(chargeItemDefinition.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rMetadata);
			}

			// description : string
			if (chargeItemDefinition.hasDescription()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"description", chargeItemDefinition.getDescription());
				resourcemetadataList.add(rMetadata);
			}

			// effective : date(period)
			if (chargeItemDefinition.hasEffectivePeriod()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"effective", utcDateUtil.formatDate(chargeItemDefinition.getEffectivePeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(chargeItemDefinition.getEffectivePeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(chargeItemDefinition.getEffectivePeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(chargeItemDefinition.getEffectivePeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
				resourcemetadataList.add(rMetadata);
			}

			// identifier : token
			if (chargeItemDefinition.hasIdentifier()) {

				for (Identifier identifier : chargeItemDefinition.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}

			// jurisdiction : token
			if (chargeItemDefinition.hasJurisdiction()) {

				for (CodeableConcept jurisdiction : chargeItemDefinition.getJurisdiction()) {

					if (jurisdiction.hasCoding()) {
						for (Coding code : jurisdiction.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"jurisdiction", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// name : string
			if (chargeItemDefinition.hasName()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"name", chargeItemDefinition.getName());
				resourcemetadataList.add(rMetadata);
			}

			// publisher : string
			if (chargeItemDefinition.hasPublisher()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"publisher", chargeItemDefinition.getPublisher());
				resourcemetadataList.add(rMetadata);
			}

			// status : token
			if (chargeItemDefinition.hasStatus() && chargeItemDefinition.getStatus() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", chargeItemDefinition.getStatus().toCode(), chargeItemDefinition.getStatus().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// title : string
			if (chargeItemDefinition.hasTitle()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"title", chargeItemDefinition.getTitle());
				resourcemetadataList.add(rMetadata);
			}

			// url : uri
			if (chargeItemDefinition.hasUrl()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"url", chargeItemDefinition.getUrl());
				resourcemetadataList.add(rMetadata);
			}

			// version : string
			if (chargeItemDefinition.hasVersion()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"version", chargeItemDefinition.getVersion());
				resourcemetadataList.add(rMetadata);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
        } finally {
	        rMetadata = null;
	        rMetadataChain = null;
            if (iChargeItemDefinition != null) {
                try {
                	iChargeItemDefinition.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
