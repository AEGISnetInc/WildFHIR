/*
Copyright (c) 2020, AEGIS.net, Inc.
All rights reserved.

Redistribution and use in source and biologicallyDerivedProduct forms, with or without modification,
are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
 * Redistributions in biologicallyDerivedProduct form must reproduce the above copyright notice,
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

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.BiologicallyDerivedProduct;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataBiologicallyDerivedProduct extends ResourcemetadataProxy {

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
        ByteArrayInputStream iBiologicallyDerivedProduct = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

		try {
			// Extract and convert the resource contents to a BiologicallyDerivedProduct object
			if (chainedResource != null) {
				iBiologicallyDerivedProduct = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iBiologicallyDerivedProduct = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			BiologicallyDerivedProduct biologicallyDerivedProduct = (BiologicallyDerivedProduct) xmlP.parse(iBiologicallyDerivedProduct);
			iBiologicallyDerivedProduct.close();

			/*
			 * Create new Resourcemetadata objects for each BiologicallyDerivedProduct metadata value and add to the resourcemetadataList
			 */

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, biologicallyDerivedProduct, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// identifier : token
			if (biologicallyDerivedProduct.hasIdentifier()) {

				for (Identifier identifier : biologicallyDerivedProduct.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}

			// product-category : token
			if (biologicallyDerivedProduct.hasProductCategory() && biologicallyDerivedProduct.getProductCategory() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"product-category", biologicallyDerivedProduct.getProductCategory().toCode(), biologicallyDerivedProduct.getProductCategory().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// product-code : token
			if (biologicallyDerivedProduct.hasProductCode() && biologicallyDerivedProduct.getProductCode().hasCoding()) {

				for (Coding code : biologicallyDerivedProduct.getProductCode().getCoding()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"product-code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rMetadata);
				}
			}

			// request : reference
			if (biologicallyDerivedProduct.hasRequest()) {

				for (Reference request : biologicallyDerivedProduct.getRequest()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "request", 0, request, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// status : token
			if (biologicallyDerivedProduct.hasStatus() && biologicallyDerivedProduct.getStatus() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", biologicallyDerivedProduct.getStatus().toCode(), biologicallyDerivedProduct.getStatus().getSystem());
				resourcemetadataList.add(rMetadata);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		} finally {
	        rMetadata = null;
	        rMetadataChain = null;
            if (iBiologicallyDerivedProduct != null) {
                try {
                	iBiologicallyDerivedProduct.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
