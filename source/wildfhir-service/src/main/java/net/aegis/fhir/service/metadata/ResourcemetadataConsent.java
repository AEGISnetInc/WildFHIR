/*
Copyright (c) 2020, AEGIS.net, Inc.
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following consents are met:

 * Redistributions of source code must retain the above copyright notice, this
   list of consents and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
   this list of consents and the following disclaimer in the documentation
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

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Consent;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Consent.provisionActorComponent;
import org.hl7.fhir.r4.model.Consent.provisionDataComponent;
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
public class ResourcemetadataConsent extends ResourcemetadataProxy {

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
        ByteArrayInputStream iConsent = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

		try {
			// Extract and convert the resource contents to a Consent object
			if (chainedResource != null) {
				iConsent = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iConsent = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			Consent consent = (Consent) xmlP.parse(iConsent);
			iConsent.close();

			/*
			 * Create new Resourcemetadata objects for each Consent metadata value and add to the resourcemetadataList
			 */

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, consent, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			if (consent.hasProvision()) {

				// action : token
				if (consent.getProvision().hasAction()) {

					for (CodeableConcept action : consent.getProvision().getAction()) {
						if (action.hasCoding()) {

							for (Coding code : action.getCoding()) {
								rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"action", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
								resourcemetadataList.add(rMetadata);
							}
						}
					}
				}

				// actor : reference
				if (consent.getProvision().hasActor()) {

					for (provisionActorComponent actor : consent.getProvision().getActor()) {

						if (actor.hasReference()) {
							rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "actor", 0, actor.getReference(), null);
							resourcemetadataList.addAll(rMetadataChain);
						}
					}
				}

				// data : reference
				if (consent.getProvision().hasData()) {

					for (provisionDataComponent data : consent.getProvision().getData()) {

						if (data.hasReference()) {
							rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "data", 0, data.getReference(), null);
							resourcemetadataList.addAll(rMetadataChain);
						}
					}
				}

				// period : date(period)
				if (consent.getProvision().hasPeriod()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"period", utcDateUtil.formatDate(consent.getProvision().getPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(consent.getProvision().getPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(consent.getProvision().getPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(consent.getProvision().getPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
					resourcemetadataList.add(rMetadata);
				}

				// purpose : token
				if (consent.getProvision().hasPurpose()) {

					for (Coding purpose : consent.getProvision().getPurpose()) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"purpose", purpose.getCode(), purpose.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(purpose));
						resourcemetadataList.add(rMetadata);
					}
				}

				// security-label : token
				if (consent.getProvision().hasSecurityLabel()) {

					for (Coding security : consent.getProvision().getSecurityLabel()) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"security-label", security.getCode(), security.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(security));
						resourcemetadataList.add(rMetadata);
					}
				}
			}

			// category : token
			if (consent.hasCategory()) {

				for (CodeableConcept category : consent.getCategory()) {
					if (category.hasCoding()) {

						for (Coding code : category.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"category", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// consentor : reference
			if (consent.hasPerformer()) {

				for (Reference consentor : consent.getPerformer()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "consentor", 0, consentor, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// date : date
			if (consent.hasDateTime()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(consent.getDateTime(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(consent.getDateTime(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rMetadata);
			}

			// identifier : token
			if (consent.hasIdentifier()) {

				for (Identifier identifier : consent.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}

			// organization : reference
			if (consent.hasOrganization()) {

				for (Reference organization : consent.getOrganization()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "organization", 0, organization, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// patient : reference
			if (consent.hasPatient()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "patient", 0, consent.getPatient(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// scope : token
			if (consent.hasScope() && consent.getScope().hasCoding()) {

				for (Coding code : consent.getScope().getCoding()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"scope", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rMetadata);
				}
			}

			// source-reference : reference
			if (consent.hasSourceReference()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "source-reference", 0, consent.getSourceReference(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// status : token
			if (consent.hasStatus() && consent.getStatus() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", consent.getStatus().toCode(), consent.getStatus().getSystem());
				resourcemetadataList.add(rMetadata);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		} finally {
	        rMetadata = null;
	        rMetadataChain = null;
            if (iConsent != null) {
                try {
                	iConsent.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
