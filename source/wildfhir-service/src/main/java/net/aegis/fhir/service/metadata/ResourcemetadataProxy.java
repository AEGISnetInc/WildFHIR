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

//import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.formats.IParser.OutputStyle;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.UriType;

import net.aegis.fhir.model.LabelKeyValueBean;
import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.ResourceContainer;
import net.aegis.fhir.model.ResourceType;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.model.Tag;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

/**
 * @author richard.ettema
 *
 */
public abstract class ResourcemetadataProxy {

    protected UTCDateUtil utcDateUtil = new UTCDateUtil();

	/**
	 * Generate the list of Resourcemetadata objects to persist; one for each non-null valid resource metadata value
	 *
	 * @param resource
	 * @param baseUrl
	 * @param resourceService
	 * @return <code>List<Resourcemetadata></code>
	 * @throws Exception
	 */
	public abstract List<Resourcemetadata> generateAllForResource(Resource resource, String baseUrl, ResourceService resourceService) throws Exception;

	/**
	 * Generate the list of Resourcemetadata objects to persist; one for each non-null valid resource metadata value
	 * where each resource parameter is chained to the specified chainedParameter name
	 *
	 * @param resource
	 * @param baseUrl
	 * @param resourceService
	 * @param chainedResource
	 * @param chainedParameter
	 * @param chainedIndex
	 * @param fhirResource
	 * @return <code>List<Resourcemetadata></code>
	 * @throws Exception
	 */
	public abstract List<Resourcemetadata> generateAllForResource(Resource resource, String baseUrl, ResourceService resourceService, Resource chainedResource, String chainedParameter, int chainedIndex, org.hl7.fhir.r4.model.Resource fhirResource) throws Exception;

	/**
	 * (Overloaded) Generate a new Resourcemetadata instance initialized with all properties where system is
	 * defaulted to null.
	 *
	 * @param resource
	 * @param chainedResource
	 * @param name
	 * @param value
	 * @return Generated <code>Resourcemetadata</code>
	 */
	protected Resourcemetadata generateResourcemetadata(Resource resource, Resource chainedResource, String name, String value) {
		return generateResourcemetadata(resource, chainedResource, name, value, null);
	}

	/**
	 * (Overloaded) Generate a new Resourcemetadata instance initialized with all properties where code is
	 * defaulted to null.
	 *
	 * @param resource
	 * @param chainedResource
	 * @param name
	 * @param value
	 * @param system
	 * @return Generated <code>Resourcemetadata</code>
	 */
	protected Resourcemetadata generateResourcemetadata(Resource resource, Resource chainedResource, String name, String value, String system) {
		return generateResourcemetadata(resource, chainedResource, name, value, system, null);
	}

	/**
	 * (Overloaded) Generate a new Resourcemetadata instance initialized with all properties where textValue is
	 * defaulted to null.
	 *
	 * @param resource
	 * @param chainedResource
	 * @param name
	 * @param value
	 * @param system
	 * @param code
	 * @return Generated <code>Resourcemetadata</code>
	 */
	protected Resourcemetadata generateResourcemetadata(Resource resource, Resource chainedResource, String name, String value, String system, String code) {
		return generateResourcemetadata(resource, chainedResource, name, value, system, code, null);
	}

	/**
	 * (Overloaded) Generate a new Resourcemetadata instance initialized with all properties where overrideType is
	 * defaulted to null.
	 *
	 * @param resource
	 * @param chainedResource
	 * @param name
	 * @param value
	 * @param system
	 * @param code
	 * @param textValue
	 * @return Generated <code>Resourcemetadata</code>
	 */
	protected Resourcemetadata generateResourcemetadata(Resource resource, Resource chainedResource, String name, String value, String system, String code, String textValue) {
		return generateResourcemetadata(resource, chainedResource, name, value, system, code, textValue, null);
	}

	/**
	 * (Overloaded) Generate a new Resourcemetadata instance initialized with all properties.
	 *
	 * @param resource
	 * @param chainedResource
	 * @param name
	 * @param value
	 * @param system
	 * @param code
	 * @param textValue
	 * @param overrideType
	 * @return Generated <code>Resourcemetadata</code>
	 */
	protected Resourcemetadata generateResourcemetadata(Resource resource, Resource chainedResource, String name, String value, String systemValue, String codeValue, String textValue, String overrideType) {

//		System.out.println("");
//		System.out.println("generateResourcemetadata( " + resource.getResourceType() + ", " + ((chainedResource != null) ? "chainedResource:" : "null:") + ((chainedResource != null && !chainedResource.getResourceType().isEmpty()) ? chainedResource.getResourceType() : "null") + ", " + name + " )");

		Resourcemetadata r = new Resourcemetadata();

		r.setResource(resource);
		r.setParamName(name);

		// Set type to overrideType if present
		if (overrideType != null) {
//			System.out.println("  --> [override] " + overrideType);
			r.setParamType(overrideType);
		}
		// Otherwise, determine type from Resource Type and Metadata name
		else {
			String type = ResourceType.findResourceTypeResourceCriteriaType(resource.getResourceType(), name);
//			System.out.println("  --> " + type);
			r.setParamType(type);
		}

		if (value != null && !value.isEmpty()) {
			r.setParamValue(value);
			r.setParamValueU(value);
		}

		if (systemValue != null && !systemValue.isEmpty()) {
			r.setSystemValue(systemValue);
		}

		if (codeValue != null && !codeValue.isEmpty()) {
			r.setCodeValue(codeValue);
		}

		if (textValue != null && !textValue.isEmpty()) {
			r.setTextValue(textValue);
			r.setTextValueU(textValue);
		}

		return r;
	}

	/**
	 * Generate the Resourcemetadata instances for the chained resource.
	 *
	 * @param resource
	 * @param baseUrl
	 * @param resourceService
	 * @param chainedParameter
	 * @param chainedIndex
	 * @param chainedResourceType
	 * @param reference
	 * @param fhirResource
	 * @return Generated <code>List<Resourcemetadata></code>
	 */
	protected List<Resourcemetadata> generateChainedResourcemetadataAny(Resource resource, String baseUrl, ResourceService resourceService, String chainedParameter, int chainedIndex, String reference, org.hl7.fhir.r4.model.Resource fhirResource) throws Exception {

//		System.out.println("");
//		System.out.println(">> generateChainedResourcemetadataAny");
//		System.out.println("   -- resource:            " + (resource != null ? resource.getResourceType() : "null"));
//		System.out.println("   -- baseUrl:             " + baseUrl);
//		System.out.println("   -- chainedParameter:    " + chainedParameter);
//		System.out.println("   -- chainedIndex:        " + chainedIndex);
//		System.out.println("   -- reference:           " + reference);
//		System.out.println("   -- fhirResource:        " + (fhirResource != null ? fhirResource.fhirType() : "null"));

		List<Resourcemetadata> rList = new ArrayList<Resourcemetadata>();

			try {
				// Determine resource type from reference
				String chainedResourceType = ServicesUtil.INSTANCE.getResourceTypeFromReference(reference);

				if ((chainedResourceType == null || chainedResourceType.isEmpty()) && fhirResource != null) {
					// Use fhirResource type if chainedResourceType not found from reference; this happens if reference is not http based
					chainedResourceType = fhirResource.fhirType();
				}

				if (chainedResourceType != null) {
//					System.out.println("   -- chainedResourceType: " + chainedResourceType);

					// Check reference resource type; if single resource type, generate chained parameters without explicit chained resource type
					LabelKeyValueBean resourceSearchParam = ResourceType.findResourceTypeResourceCriteria(resource.getResourceType(), chainedParameter);
					if (resourceSearchParam != null && resourceSearchParam.getRefType() != null && !resourceSearchParam.getRefType().isEmpty() && !resourceSearchParam.getRefType().equals("*")) {
//						System.out.println("   -- search param type:   " + resourceSearchParam.getRefType());

						if (resourceSearchParam.getRefType().equals(chainedResourceType)) {
							String chainedParameterSingle = chainedParameter + ".";

							List<Resourcemetadata> rList1 = this.generateChainedResourcemetadata(resource, baseUrl, resourceService, chainedParameterSingle, chainedIndex, chainedResourceType, reference, fhirResource);

							rList.addAll(rList1);
						}
					}

					// Generate parameter with explicit chained resource type
					String fullReference = generateFullLocalReference(reference, baseUrl);
					Resourcemetadata rChainedRef = generateResourcemetadata(resource, null, chainedParameter + ":" + chainedResourceType, fullReference);
					rList.add(rChainedRef);

					// Generate chained parameters with explicit chained resource type
					String chainedParameterAny = chainedParameter + ":" + chainedResourceType + ".";

					List<Resourcemetadata> rList2 = this.generateChainedResourcemetadata(resource, baseUrl, resourceService, chainedParameterAny, chainedIndex, chainedResourceType, reference, fhirResource);

					rList.addAll(rList2);
				}
			}
			catch (Exception e) {
				// Swallow exception for now which means no chained parameter values will be saved
				System.out.println("Chained resource any generation of search parameters failed! " + e.getMessage());
			}

		return rList;
	}

	/**
	 * Generate the Resourcemetadata instances for the chained resource.
	 *
	 * @param resource
	 * @param baseUrl
	 * @param resourceService
	 * @param chainedParameter
	 * @param chainedIndex
	 * @param chainedResourceType
	 * @param reference
	 * @param fhirResource
	 * @return Generated <code>List<Resourcemetadata></code>
	 */
	protected List<Resourcemetadata> generateChainedResourcemetadata(Resource resource, String baseUrl, ResourceService resourceService, String chainedParameter, int chainedIndex, String chainedResourceType, String reference, org.hl7.fhir.r4.model.Resource fhirResource) throws Exception {

//		System.out.println("");
//		System.out.println(">> generateChainedResourcemetadata [normal]");
//		System.out.println("   -- resource:            " + (resource != null ? resource.getResourceType() : "null"));
//		System.out.println("   -- baseUrl:             " + baseUrl);
//		System.out.println("   -- chainedParameter:    " + chainedParameter);
//		System.out.println("   -- chainedIndex:        " + chainedIndex);
//		System.out.println("   -- chainedResourceType: " + chainedResourceType);
//		System.out.println("   -- fhirResource:        " + (fhirResource != null ? fhirResource.fhirType() : "null"));

		List<Resourcemetadata> rList = new ArrayList<Resourcemetadata>();

		ResourcemetadataProxyObjectFactory proxyObjectFactory = new ResourcemetadataProxyObjectFactory();
		ResourcemetadataProxy proxy = proxyObjectFactory.getResourcemetadataProxy(chainedResourceType);

		if (proxy != null) {
			try {
				// Check for fhirResource - should only get from Bundle Composition or MessageHeader
				if (fhirResource != null) {
					// Use provided chained resource and build the required WildFHIR Resource
					Resource chainedResource = new Resource();
					chainedResource.setResourceId(fhirResource.getId());

					// Convert the Resource to XML byte[]
					ByteArrayOutputStream oResource = new ByteArrayOutputStream();
					XmlParser xmlParser = new XmlParser();
					xmlParser.setOutputStyle(OutputStyle.PRETTY);
					xmlParser.compose(oResource, fhirResource, true);
					byte[] bResource = oResource.toByteArray();

					chainedResource.setResourceContents(bResource);
					chainedResource.setResourceType(fhirResource.getResourceType().name());

					rList = proxy.generateAllForResource(resource, baseUrl, resourceService, chainedResource, chainedParameter, chainedIndex, fhirResource);
				}
				else {
					// Read chained resource - for now expect the resource on this local server
					String resourceId = ServicesUtil.INSTANCE.extractResourceIdFromURL(reference);

					ResourceContainer chainedResourceContainer = resourceService.read(chainedResourceType, resourceId, null);

					if (chainedResourceContainer != null &&
							chainedResourceContainer.getResponseStatus().equals(Response.Status.OK) &&
							chainedResourceContainer.getResource() != null) {

						rList = proxy.generateAllForResource(resource, baseUrl, resourceService, chainedResourceContainer.getResource(), chainedParameter, chainedIndex, fhirResource);
					}
				}
			}
			catch (Exception e) {
				// Swallow exception for now which means no chained parameter values will be saved
				System.out.println("Chained resource generation of search parameters failed! " + e.getMessage());
			}
		}

		return rList;
	}

	/**
	 * Generate the Resourcemetadata instances for the chained resource.
	 *
	 * @param resource
	 * @param baseUrl
	 * @param resourceService
	 * @param chainedParameter
	 * @param chainedResourceType
	 * @param chainedFHIRResource
	 * @param fhirResource
	 * @return Generated <code>List<Resourcemetadata></code>
	 */
	protected List<Resourcemetadata> generateChainedResourcemetadata(Resource resource, String baseUrl, ResourceService resourceService, String chainedParameter, String chainedResourceType, org.hl7.fhir.r4.model.Resource chainedFHIRResource, org.hl7.fhir.r4.model.Resource fhirResource) throws Exception {

//		System.out.println("");
//		System.out.println(">> generateChainedResourcemetadata [nested]");
//		System.out.println("   -- resource:            " + (resource != null ? resource.getResourceType() : "null"));
//		System.out.println("   -- baseUrl:             " + baseUrl);
//		System.out.println("   -- chainedParameter:    " + chainedParameter);
//		System.out.println("   -- chainedResourceType: " + chainedResourceType);
//		System.out.println("   -- chainedFHIRResource: " + (chainedFHIRResource != null ? chainedFHIRResource.fhirType() : "null"));
//		System.out.println("   -- fhirResource:        " + (fhirResource != null ? fhirResource.fhirType() : "null"));

		List<Resourcemetadata> rList = new ArrayList<Resourcemetadata>();

		ResourcemetadataProxyObjectFactory proxyObjectFactory = new ResourcemetadataProxyObjectFactory();
		ResourcemetadataProxy proxy = proxyObjectFactory.getResourcemetadataProxy(chainedResourceType);

		if (proxy != null) {
			try {
				if (chainedFHIRResource != null) {
					// Use provided chained resource and build the required WildFHIR Resource
					Resource chainedResource = new Resource();
					chainedResource.setResourceId(chainedFHIRResource.getId());

					// Convert the Resource to XML byte[]
					ByteArrayOutputStream oResource = new ByteArrayOutputStream();
					XmlParser xmlParser = new XmlParser();
					xmlParser.setOutputStyle(OutputStyle.PRETTY);
					xmlParser.compose(oResource, chainedFHIRResource, true);
					byte[] bResource = oResource.toByteArray();

					chainedResource.setResourceContents(bResource);
					chainedResource.setResourceType(chainedResourceType);

					rList = proxy.generateAllForResource(resource, baseUrl, resourceService, chainedResource, chainedParameter, 0, fhirResource);
				}
			}
			catch (Exception e) {
				// Swallow exception for now which means no chained parameter values will be saved
				System.out.println("Chained FHIR resource generation of search parameters failed! " + e.getMessage());
			}
		}

		return rList;
	}

//	/**
//	 * Generate a list of Resourcemetadata instances initialized for all tags
//	 *
//	 * @param resource
//	 * @return Generated List<Resourcemetadata>
//	 */
//	protected List<Resourcemetadata> generateResourcemetadataTagList(Resource resource) {
//
//		List<Resourcemetadata> resourcemetadataTagList = new ArrayList<Resourcemetadata>();
//
//		/*
//		 *  Process meta data within the resource
//		 */
//		try {
//			if (resource != null && resource.getResourceContents() != null) {
//				// Convert XML contents to FHIR Resource
//				XmlParser xmlP = new XmlParser();
//				org.hl7.fhir.r4.model.Resource fhirResource = xmlP.parse(new ByteArrayInputStream(resource.getResourceContents()));
//
//				if (fhirResource != null && fhirResource.getMeta() != null) {
//
//					if (fhirResource.getMeta().getTag() != null) {
//						for (Coding tagCoding : fhirResource.getMeta().getTag()) {
//							Resourcemetadata r = new Resourcemetadata();
//
//							r.setResource(resource);
//							r.setParamName(Tag.METADATA_NAME_GENERAL_TAG);
//							r.setParamType("tag");
//							r.setParamValue(tagCoding.getCode());
//							if (tagCoding.hasSystem()) {
//								r.setSystem(tagCoding.getSystem());
//							}
//							if (tagCoding.hasDisplay()) {
//								r.setCode(tagCoding.getDisplay());
//							}
//
//							resourcemetadataTagList.add(r);
//						}
//					}
//
//					if (fhirResource.getMeta().getProfile() != null) {
//						for (UriType tagUri : fhirResource.getMeta().getProfile()) {
//							Resourcemetadata r = new Resourcemetadata();
//
//							r.setResource(resource);
//							r.setParamName(Tag.METADATA_NAME_PROFILE_TAG);
//							r.setParamType("tag");
//							r.setParamValue(tagUri.getValue());
//
//							resourcemetadataTagList.add(r);
//						}
//					}
//
//					if (fhirResource.getMeta().getSecurity() != null) {
//						for (Coding tagCoding : fhirResource.getMeta().getSecurity()) {
//							Resourcemetadata r = new Resourcemetadata();
//
//							r.setResource(resource);
//							r.setParamName(Tag.METADATA_NAME_SECURITY_TAG);
//							r.setParamType("tag");
//							r.setParamValue(tagCoding.getCode());
//							if (tagCoding.hasSystem()) {
//								r.setSystem(tagCoding.getSystem());
//							}
//							if (tagCoding.hasDisplay()) {
//								r.setCode(tagCoding.getDisplay());
//							}
//
//							resourcemetadataTagList.add(r);
//						}
//					}
//				}
//			}
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		return resourcemetadataTagList;
//	}

	/**
	 * Generate a list of Resourcemetadata instances initialized for all tags
	 *
	 * @param resource
	 * @param fhirResource
	 * @param chainedParameter
	 * @return Generated List<Resourcemetadata>
	 */
	protected List<Resourcemetadata> generateResourcemetadataTagList(Resource resource, org.hl7.fhir.r4.model.Resource fhirResource, String chainedParameter) {

		List<Resourcemetadata> resourcemetadataTagList = new ArrayList<Resourcemetadata>();

		/*
		 *  Process meta data within the resource
		 */
		try {
			if (resource != null && resource.getResourceContents() != null) {

				if (fhirResource != null && fhirResource.getMeta() != null) {

					if (fhirResource.getMeta().getTag() != null) {
						for (Coding tagCoding : fhirResource.getMeta().getTag()) {
							Resourcemetadata r = new Resourcemetadata();

							r.setResource(resource);
							r.setParamName(chainedParameter + Tag.METADATA_NAME_GENERAL_TAG);
							r.setParamType("tag");
							r.setParamValue(tagCoding.getCode());
							r.setParamValueU(tagCoding.getCode());
							if (tagCoding.hasSystem()) {
								r.setSystemValue(tagCoding.getSystem());
							}
							if (tagCoding.hasDisplay()) {
								r.setCodeValue(tagCoding.getDisplay());
							}

							resourcemetadataTagList.add(r);
						}
					}

					if (fhirResource.getMeta().getProfile() != null) {
						for (UriType tagUri : fhirResource.getMeta().getProfile()) {
							Resourcemetadata r = new Resourcemetadata();

							r.setResource(resource);
							r.setParamName(chainedParameter + Tag.METADATA_NAME_PROFILE_TAG);
							r.setParamType("tag");
							r.setParamValue(tagUri.getValue());
							r.setParamValueU(tagUri.getValue());

							resourcemetadataTagList.add(r);
						}
					}

					if (fhirResource.getMeta().getSecurity() != null) {
						for (Coding tagCoding : fhirResource.getMeta().getSecurity()) {
							Resourcemetadata r = new Resourcemetadata();

							r.setResource(resource);
							r.setParamName(chainedParameter + Tag.METADATA_NAME_SECURITY_TAG);
							r.setParamType("tag");
							r.setParamValue(tagCoding.getCode());
							r.setParamValueU(tagCoding.getCode());
							if (tagCoding.hasSystem()) {
								r.setSystemValue(tagCoding.getSystem());
							}
							if (tagCoding.hasDisplay()) {
								r.setCodeValue(tagCoding.getDisplay());
							}

							resourcemetadataTagList.add(r);
						}
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return resourcemetadataTagList;
	}

	/**
	 *
	 * @param referenceUrl
	 * @param baseUrl
	 * @return reference url prefixed with local server's base url if base url not present
	 */
	protected String generateFullLocalReference(String referenceUrl, String baseUrl) {
		String fullLocalReference = referenceUrl;

		if (baseUrl != null && !baseUrl.isEmpty() && referenceUrl != null && !referenceUrl.contains("http") && !referenceUrl.startsWith("#")) {

			if (baseUrl.endsWith("/")) {
				baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
			}

			if (referenceUrl.startsWith("/")) {
				fullLocalReference = baseUrl + referenceUrl;
			}
			else {
				fullLocalReference = baseUrl + "/" + referenceUrl;
			}
		}

		return fullLocalReference;
	}

	/**
	 * @param bundle
	 * @param entryReference
	 * @return Bundle.entry.resource
	 */
	protected org.hl7.fhir.r4.model.Resource getReferencedBundleEntryResource(Bundle bundle, String entryReference) {

		org.hl7.fhir.r4.model.Resource entryResource = null;

		/*
		 *  Iterate through all Bundle entries and return the first match of the reference
		 *  to the entry resource id or fullUrl
		 */
		try {
			if (this.isAbsoluteUrl(entryReference)) {
				// if the reference is absolute, then you resolve by fullUrl. No other thinking is required.
				for (BundleEntryComponent entry : bundle.getEntry()) {
					if (entry.hasFullUrl() && entry.getFullUrl().equals(entryReference) && entry.hasResource()) {
						entryResource = entry.getResource();
						break;
					}
				}
			}
			else {
				// extract and match by type and id
				String type = ServicesUtil.INSTANCE.getResourceTypeFromReference(entryReference);
				String id = ServicesUtil.INSTANCE.extractResourceIdFromURL(entryReference);
				for (BundleEntryComponent entry : bundle.getEntry()) {
					if (entry.hasResource()) {
						if (entry.hasFullUrl() && entry.getFullUrl().endsWith(type + "/" + id)) {
							entryResource = entry.getResource();
							break;
						}
						else {
							String entryType = entry.getResource().fhirType();
							String entryId = entry.getResource().getId();
							if (type.equals(entryType) && id.equals(entryId)) {
								entryResource = entry.getResource();
								break;
							}
						}
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return entryResource;
	}

	protected boolean isAbsoluteUrl(String ref) {
		return ref != null && (ref.startsWith("http:") || ref.startsWith("https:") || ref.startsWith("urn:uuid:") || ref.startsWith("urn:oid:")
				|| ref.startsWith("urn:iso:") || ref.startsWith("urn:iso-iec:") || ref.startsWith("urn:iso-cie:")
				|| ref.startsWith("urn:iso-astm:") || ref.startsWith("urn:iso-ieee:") || ref.startsWith("urn:iec:"));
	}

}
