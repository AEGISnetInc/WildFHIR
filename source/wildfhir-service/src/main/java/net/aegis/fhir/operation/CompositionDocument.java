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
package net.aegis.fhir.operation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.formats.IParser.OutputStyle;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleLinkComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Composition.CompositionAttesterComponent;
import org.hl7.fhir.r4.model.Composition.CompositionEventComponent;
import org.hl7.fhir.r4.model.Composition.SectionComponent;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Signature;
import org.hl7.fhir.r4.model.UriType;

import net.aegis.fhir.model.ResourceContainer;
import net.aegis.fhir.service.BatchService;
import net.aegis.fhir.service.CodeService;
import net.aegis.fhir.service.ConformanceService;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.ResourcemetadataService;
import net.aegis.fhir.service.TransactionService;
import net.aegis.fhir.service.linked.LinkedResourceProxy;
import net.aegis.fhir.service.linked.LinkedResourceProxyObjectFactory;
import net.aegis.fhir.service.narrative.FHIRNarrativeGeneratorClient;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UUIDUtil;
//import net.aegis.fhir.service.validation.FHIRValidatorClient;

/**
 * @author richard.ettema
 *
 */
public class CompositionDocument extends ResourceOperationProxy {

	private Logger log = Logger.getLogger("CompositionDocument");

	/* (non-Javadoc)
	 * @see net.aegis.fhir.operation.ResourceOperationProxy#executeOperation(javax.ws.rs.core.UriInfo, javax.ws.rs.core.HttpHeaders, net.aegis.fhir.service.ResourceService, net.aegis.fhir.service.ResourcemetadataService, net.aegis.fhir.service.BatchService, net.aegis.fhir.service.TransactionService, net.aegis.fhir.service.CodeService, net.aegis.fhir.service.ConformanceService, java.lang.String, java.lang.String, java.lang.String, org.hl7.fhir.r4.model.Parameters, org.hl7.fhir.r4.model.Resource, java.lang.String, java.lang.String, boolean, java.lang.StringBuffer)
	 */
	@Override
	public Parameters executeOperation(UriInfo context, HttpHeaders headers, ResourceService resourceService, ResourcemetadataService resourcemetadataService, BatchService batchService, TransactionService transactionService, CodeService codeService, ConformanceService conformanceService, String softwareVersion, String resourceType, String resourceId, Parameters inputParameters, org.hl7.fhir.r4.model.Resource inputResource, String inputString, String contentType, boolean isPost, StringBuffer returnedDirective) throws Exception {

		log.fine("[START] CompositionDocument.executeOperation()");

		BooleanType persist = null;
		UriType id = null;
		UriType graph = null;
		Resource documentResource = null;
		OperationOutcome rOutcome = null;
		OperationOutcome.OperationOutcomeIssueComponent issue = null;
		Parameters out = null;

		try {
			/*
			 * If inputParameters is null, attempt to extract parameters from context
			 */
			if (inputParameters == null) {
				inputParameters = getParametersFromQueryParams(context);
			}

			// inputParameters is optional; if present, extract max value
			if (inputParameters != null && inputParameters.hasParameter()) {

				for (ParametersParameterComponent parameter : inputParameters.getParameter()) {

					if (parameter.getName() != null) {

						if (parameter.getName().equals("id")) {
							id = (UriType) parameter.getValue();
						}
						else if (parameter.getName().equals("persist")) {
							persist = (BooleanType) parameter.getValue();
						}
						else if (parameter.getName().equals("graph")) {
							graph = (UriType) parameter.getValue();
						}
					}
				}
			}

			/*
			 * authorization check
			 * - default authorized to true
			 * - if authMapPatient not null, read Patient; if not found, authorized false
			 * composition document check
			 * - $document must be executed against an existing Composition instance; i.e. the url path must contain the id [base]/Composition/[id]/$document or,
			 * - if the url path does not contain the id; i.e. [base]/Composition/$document, then the id parameter must be defined or search parameters must be present to find an exact Composition resource instance match
			 * - insure resource content is not null or empty
			 * - insure resource instance for the url [id] value exists and is not deleted
			 */
			ResourceContainer resourceContainer = null;
			Composition composition = null;

			// Check graph argument - CURRENTLY NOT SUPPORTED
			if (graph != null) {
				// generate OperationOutcome.issue
				issue = ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTSUPPORTED,
						"Use of the $document graph parameter is not currently supported.", null, null);
			}
			else {
				// Check for resourceId or id parameter - ONLY ONE MUST BE DEFINED
				if (resourceId != null || id != null) {
					// Make sure either resourceId or id parameter is defined but NOT BOTH
					if (resourceId != null && id != null) {
						issue = ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.CONFLICT,
								"Composition $document failed. The [id] url value '" + resourceId + "' and the id parameter value '" + id.asStringValue() + "' cannot both be defined.", null, null);
					}
					else {
						if (id != null) {
							// Assign id parameter's resource id value to resourceId
							resourceId = ServicesUtil.INSTANCE.extractResourceIdFromURL(id.asStringValue());
						}
						// else resourceId already set

						// perform read - assign found Composition, else generate OperationOutcome.issue
						resourceContainer = resourceService.read(resourceType, resourceId, null);

						if (resourceContainer == null || resourceContainer.getResource() == null || !resourceContainer.getResponseStatus().equals(Response.Status.OK)) {
							issue = ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND,
										"Composition $document failed. The resource id value '" + resourceId + "' does not reference a valid Composition resource instance.", null, null);
						}
						else {
							// Convert XML contents to Composition Resource object
							ByteArrayInputStream iResource = new ByteArrayInputStream(resourceContainer.getResource().getResourceContents());
							XmlParser xmlP = new XmlParser();
							composition = (Composition) xmlP.parse(iResource);
						}
					}
				}
				// else resourceId not present
				else {
					// check for search criteria, perform search - assign found Composition exact match, else generate OperationOutcome.issue
					// Get the query parameters that represent the search criteria
					MultivaluedMap<String, String> queryParams = context.getQueryParameters();

					// if queryParams not empty, perform search
					if (!queryParams.isEmpty()) {
						resourceContainer = resourceService.search(queryParams, null, null, null, "Composition", context.getRequestUri().toString(), null, null, null, false);

						// check for found matches and only one exact match
						if (resourceContainer != null && resourceContainer.getBundle() != null &&
								!resourceContainer.getBundle().getEntry().isEmpty() && resourceContainer.getBundle().getEntry().size() == 1) {

							composition = (Composition) resourceContainer.getBundle().getEntry().get(0).getResource();
						}
						else {
							// generate OperationOutcome.issue
							issue = ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND,
									"Composition $document failed. Search parameters to locate an exact Composition match failed.", null, null);
						}
					}
					// else queryParams are empty
					else {
						// generate OperationOutcome.issue
						issue = ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.INCOMPLETE,
								"Composition $document failed. Missing search parameters. When the [id] url or the id parameter value is not used, search parameters to locate an exact Composition match must be provided.", null, null);
					}
				}
			}

			// Check for found Composition resource instance
			if (composition != null) {

				// call getCompositionDocument method
				documentResource = this.getCompositionDocument(composition, context, resourceService, codeService, persist, graph, returnedDirective);
			}
			else {
				if (issue == null) {
					// generate OperationOutcome.issue
					issue = ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND,
							"Composition $document failed. Composition resource not found.", null, null);
				}
			}

			out = new Parameters();
			ParametersParameterComponent outParameter = new ParametersParameterComponent();
			outParameter.setName("return");

			if (documentResource != null) {
				outParameter.setResource(documentResource);
			}
			else {
				if (issue == null) {
					// if we got here something went horribly wrong - if documentBundle or issue is null, then we should have thrown an exception
					// generate generic failure OperationOutcome.issue
					issue = ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.FATAL, OperationOutcome.IssueType.UNKNOWN,
							"Composition $document failed. Document generation produced an unknown/unexpected outcome.", null, null);
				}

				rOutcome = new OperationOutcome();
				rOutcome.setText(null);
				rOutcome.getIssue().add(issue);

				// Use RI NarrativeGenerator
				FHIRNarrativeGeneratorClient.instance().generate(rOutcome);

				outParameter.setResource(rOutcome);
			}

			out.addParameter(outParameter);
		}
		catch (Exception e) {
			// Throw exceptions back
			throw e;
		}

		return out;
	}

	/**
	 * @param composition
	 * @param context
	 * @param resourceService
	 * @param resourcemetadataService
	 * @param codeService
	 * @param persist
	 * @param graph
	 * @param returnedDirective
	 * @return Constructed Document Bundle or OperationOutcome Resource
	 * @throws Exception
	 */
	private Resource getCompositionDocument(Composition composition, UriInfo context, ResourceService resourceService, CodeService codeService, BooleanType persist, UriType graph, StringBuffer returnedDirective) throws Exception {

		log.fine("[START] CompositionDocument.getCompositionDocument()");

		XmlParser xmlP = new XmlParser();
		xmlP.setOutputStyle(OutputStyle.PRETTY);

		// Before generating a new document bundle instance, search for existing document bundle for the Composition
		Resource existingResource = this.getExistingCompositionDocument(composition, resourceService, xmlP);

		if (existingResource != null) {
			// Set returnedDirective to generate HTTP response Location header
			returnedDirective.append("persisted");

			// Return existing document bundle if found
			return existingResource;
		}

		String locationPath = context.getRequestUri().toString();

		// Extract base url from locationPath for use in Bundle.entry.fullUrl element
		String baseUrl = ServicesUtil.INSTANCE.extractBaseURL(locationPath, "Composition");

		Bundle bundle = new Bundle();

		bundle.setId(UUIDUtil.getUUID());
		Meta bundleMeta = new Meta();
		bundleMeta.setVersionId("1");
		bundleMeta.setLastUpdated(new Date());
		bundle.setMeta(bundleMeta);
		Identifier bundleIdentifier = new Identifier();
		bundleIdentifier.setSystem("urn:ietf:rfc:3986");
		bundleIdentifier.setValue(UUIDUtil.getUUID(true));
		bundle.setIdentifier(bundleIdentifier);
		bundle.setType(BundleType.DOCUMENT);
		bundle.setTimestamp(new Date()); // TSP-2331
		BundleLinkComponent selfLink = new BundleLinkComponent();
		selfLink.setRelation("self");
		selfLink.setUrl(locationPath);
		bundle.getLink().add(selfLink);

		BundleEntryComponent bundleEntry = null;

		// Add Composition as first Bundle.entry
		bundleEntry = new BundleEntryComponent();
		bundleEntry.setFullUrl(baseUrl + composition.fhirType() + "/" + composition.getId());
		// check Composition for narrative text; if not found, generate text
		if (!composition.hasText()) {
			FHIRNarrativeGeneratorClient.instance().generate(composition);
		}
		bundleEntry.setResource(composition);
		bundle.getEntry().add(bundleEntry);

		List<OperationOutcome.OperationOutcomeIssueComponent> issues = new ArrayList<OperationOutcome.OperationOutcomeIssueComponent>();

		String messagePrefix = null;

		// Add Composition.subject referenced resource to document bundle
		if (composition.hasSubject() && composition.getSubject().hasReference()) {
			messagePrefix = "Composition.subject reference '" + composition.getSubject().getReference() + "'";
			this.addReferenceToDocument(resourceService, bundle, composition.getSubject(), baseUrl, xmlP, issues, messagePrefix);
		}

		// Add Composition.encounter referenced resource to document bundle
		if (composition.hasEncounter() && composition.getEncounter().hasReference()) {
			messagePrefix = "Composition.encounter reference '" + composition.getEncounter().getReference() + "'";
			this.addReferenceToDocument(resourceService, bundle, composition.getEncounter(), baseUrl, xmlP, issues, messagePrefix);
		}

		// Add Composition.author referenced resources to document bundle
		int authorCount = 0;
		for (Reference author : composition.getAuthor()) {
			authorCount++;
			if (author.hasReference()) {
				messagePrefix = "Composition.author [" + authorCount + "] reference '" + author.getReference() + "'";
				this.addReferenceToDocument(resourceService, bundle, author, baseUrl, xmlP, issues, messagePrefix);
			}
		}

		// Add Composition.attester.party referenced resources to document bundle
		int attesterCount = 0;
		for (CompositionAttesterComponent attester : composition.getAttester()) {
			attesterCount++;
			if (attester.hasParty() && attester.getParty().hasReference()) {
				messagePrefix = "Composition.attester [" + attesterCount + "].party reference '" + attester.getParty().getReference() + "'";
				this.addReferenceToDocument(resourceService, bundle, attester.getParty(), baseUrl, xmlP, issues, messagePrefix);
			}
		}

		// Add Composition.custodian referenced resource to document bundle
		if (composition.hasCustodian() && composition.getCustodian().hasReference()) {
			messagePrefix = "Composition.custodian reference '" + composition.getCustodian().getReference() + "'";
			this.addReferenceToDocument(resourceService, bundle, composition.getCustodian(), baseUrl, xmlP, issues, messagePrefix);
		}

		// Add Composition.event.detail referenced resources to document bundle
		int eventCount = 0;
		int detailCount = 0;
		for (CompositionEventComponent event : composition.getEvent()) {
			eventCount++;
			detailCount = 0;
			for (Reference detail : event.getDetail()) {
				detailCount++;
				if (detail.hasReference()) {
					messagePrefix = "Composition.event [" + eventCount + "].detail[" + detailCount + "] reference '" + detail.getReference() + "'";
					this.addReferenceToDocument(resourceService, bundle, detail, baseUrl, xmlP, issues, messagePrefix);
				}
			}
		}

		// Add any Composition extensions with references
		this.getResourceExtensionReferences(resourceService, bundle, composition, baseUrl, xmlP, issues);

		// traverse Composition section tree and add all valid entry referenced resources to document bundle
		this.getSections(resourceService, bundle, composition.getSection(), baseUrl, xmlP, issues);

		// if any issues, then generate an OperationOutcome
		boolean anyErrors = false;
		OperationOutcome issuesOutcome = new OperationOutcome();
		issuesOutcome.setText(null);
		if (!issues.isEmpty()) {
			for (OperationOutcome.OperationOutcomeIssueComponent issue : issues) {
				issuesOutcome.getIssue().add(issue);
				IssueSeverity severity = issue.getSeverity();
				if (severity.equals(IssueSeverity.ERROR) || severity.equals(IssueSeverity.FATAL)) {
					anyErrors = true;
				}
			}
		}

		if (anyErrors == true) {
			FHIRNarrativeGeneratorClient.instance().generate(issuesOutcome);

			return issuesOutcome;
		}
		else {
			// Generate and add signature to document bundle
			this.generateDocumentSignature(bundle, codeService, issues);

//			// If any non-error and non-fatal issues encountered, add issues OperationOutcome to document bundle
//			if (issuesOutcome.hasIssue()) {
//				narrativeGenerator.generate(issuesOutcome);
//
//				bundleEntry = new BundleEntryComponent();
//				bundleEntry.setFullUrl(UUIDUtil.getUUID(true));
//				bundleEntry.setResource(issuesOutcome);
//				bundle.getEntry().add(bundleEntry);
//			}

			// If persist has been defined and is equal to true, then save (create) document bundle to database repository. Need way to set response HTTP Location Header.
			if (persist != null && persist.booleanValue() == true) {
				// Compose document bundle to XML format
				ByteArrayOutputStream oOp = new ByteArrayOutputStream();
				xmlP.compose(oOp, bundle, true);
				String sOp = oOp.toString();

				// Perform FHIR update to retain assigned resource id
				net.aegis.fhir.model.Resource updateResource = new net.aegis.fhir.model.Resource();
				updateResource.setResourceType("Bundle");
				updateResource.setResourceContents(sOp.getBytes());

				resourceService.update(bundle.getId(), updateResource, baseUrl);

				returnedDirective.append("persisted");
			}

			return bundle;
		}
	}

	/**
	 * TSP-2416 - Add logic to process any extensions with Reference values
	 *
	 * @param resourceService
	 * @param resourcemetadataService
	 * @param bundle
	 * @param resource
	 * @param baseUrl
	 * @param xmlP
	 * @param issues
	 * @throws Exception
	 */
	private void getResourceExtensionReferences(ResourceService resourceService, Bundle bundle, Resource resource, String baseUrl, XmlParser xmlP, List<OperationOutcome.OperationOutcomeIssueComponent> issues) throws Exception {

		log.fine("[START] CompositionDocument.getResourceExtensionReferences - Processing resource " + resource.fhirType() + " '" + (resource.hasId() ? resource.getId() : "NO ID") + "'");

		Extension ext = null;
		Reference ref = null;

		try {
			// Get and assemble all (modifier) extensions in the resource
			List<Base> allExtensions = new ArrayList<Base>();
			List<Base> extensions = resource.listChildrenByName("extension");
			if (extensions != null && !extensions.isEmpty()) {
				allExtensions.addAll(extensions);
			}
			List<Base> modExtensions = resource.listChildrenByName("modifierExtension");
			if (modExtensions != null && !modExtensions.isEmpty()) {
				allExtensions.addAll(modExtensions);
			}

			// Process all found extensions
			for (Base extBase : allExtensions) {
				// Check found element is an Extension
				if (extBase.fhirType().equals("Extension")) {
					ext = (Extension)extBase;

					if (ext.hasValue() && ext.getValue() instanceof Reference) {
						ref = (Reference)ext.getValue();

						if (!ref.hasReference()) {
							log.warning("      --> [SKIP] NO REFERENCE");
							// generate OperationOutcome.issue
							issues.add(ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.WARNING, OperationOutcome.IssueType.NOTFOUND,
									"Resource " + resource.fhirType() + " '" + (resource.hasId() ? resource.getId() : "NO ID") +
									"' extension '" + (ext.hasUrl() ? ext.getUrl() : "NO URL") + "' does not contain a reference. Cannot resolve and add to document contents.", null, null));
						}
						else {
							log.fine("      --> Processing reference extension '" + (ext.hasUrl() ? ext.getUrl() : "NO URL") + "'");
							// add referenced resource to document bundle

							String messagePrefix = "Resource " + resource.fhirType() + " '" + (resource.hasId() ? resource.getId() : "NO ID") + "' extension '" + (ext.hasUrl() ? ext.getUrl() : "NO URL") + "' reference '" + ref.getReference() + "'";

							this.addReferenceToDocument(resourceService, bundle, ref, baseUrl, xmlP, issues, messagePrefix);
						}
					}
				}
			}
		}
		catch (Exception e) {
			// Capture exception as fatal OperationOutcome issue
			log.severe("      --> [FATAL] EXCEPTION! " + e.getMessage());

			// generate OperationOutcome.issue
			issues.add(ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.FATAL, OperationOutcome.IssueType.PROCESSING,
					"Resource " + resource.fhirType() + " '" + (resource.hasId() ? resource.getId() : "NO ID") + "' processing extensions - fatal exception! " + e.getMessage(), null, null));
		}
	}

	/**
	 * Iterate through Composition.section elements and add all valid entry referenced resources to document bundle
	 *
	 * @param resourceService
	 * @param resourcemetadataService
	 * @param bundle
	 * @param sections
	 * @param baseUrl
	 * @param xmlP
	 * @param issues
	 * @throws Exception
	 */
	private void getSections(ResourceService resourceService, Bundle bundle, List<SectionComponent> sections, String baseUrl, XmlParser xmlP, List<OperationOutcome.OperationOutcomeIssueComponent> issues) throws Exception {

		log.fine("[START] CompositionDocument.getSections");

		int sectionCount = 0;

		for (SectionComponent section : sections) {
			sectionCount++;
			log.fine("   --> Processing section #" + sectionCount + (section.hasTitle() ? " - '" + section.getTitle() : ""));

			if (section.hasEntry()) {
				this.getSectionEntries(resourceService, bundle, section.getEntry(), baseUrl, xmlP, issues, sectionCount);
			}
			// TSP-2416 - Check for sub-sections moved to else condition for correct processing
			else if (section.hasSection()) {
				this.getSections(resourceService, bundle, section.getSection(), baseUrl, xmlP, issues);
			}
			else {
				log.warning("   --> [SKIP] NO ENTRIES");
				// generate OperationOutcome.issue
				// TSP-2416 - Improve diagnostics description
				issues.add(ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.WARNING, OperationOutcome.IssueType.NOTFOUND,
						"Composition section[" + (section.hasTitle() ? section.getTitle() : sectionCount) + "] does not contain any entry references. Nothing to add to document contents.", null, null));
			}
		}
	}

	/**
	 * @param resourceService
	 * @param resourcemetadataService
	 * @param bundle
	 * @param entries
	 * @param baseUrl
	 * @param xmlP
	 * @param issues
	 * @param sectionCount
	 * @throws Exception
	 */
	private void getSectionEntries(ResourceService resourceService, Bundle bundle, List<Reference> entries, String baseUrl, XmlParser xmlP, List<OperationOutcome.OperationOutcomeIssueComponent> issues, int sectionCount) throws Exception {

		log.fine("[START] CompositionDocument.getSectionEntries");

		int entryCount = 0;

		try {
			for (Reference entry : entries) {
				entryCount++;
				log.fine("      --> Processing entry #" + entryCount + (entry.hasReference() ? " - '" + entry.getReference() : ""));

				if (!entry.hasReference()) {
					log.warning("      --> [SKIP] NO REFERENCE");
					// generate OperationOutcome.issue
					issues.add(ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.WARNING, OperationOutcome.IssueType.NOTFOUND,
							"Composition section[" + sectionCount + "].entry[" + entryCount + "] does not contain a reference. Cannot resolve and add to document contents.", null, null));
				}
				else {
					// add referenced resource to document bundle

					String messagePrefix = "Composition section[" + sectionCount + "].entry[" + entryCount + "] reference '" + entry.getReference() + "'";

					this.addReferenceToDocument(resourceService, bundle, entry, baseUrl, xmlP, issues, messagePrefix);
				}
			}
		}
		catch (Exception e) {
			// Capture exception as fatal OperationOutcome issue
			log.severe("      --> [FATAL] EXCEPTION! " + e.getMessage());

			// generate OperationOutcome.issue
			issues.add(ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.FATAL, OperationOutcome.IssueType.PROCESSING,
					"Composition section[" + sectionCount + "] - fatal exception! " + e.getMessage(), null, null));
		}
	}

	/**
	 * Use reference value to get (read) resource from local database repository. Current
	 * support is for local references only - expect relative references only
	 *
	 * @param resourceService
	 * @param bundle
	 * @param refResource
	 * @param baseUrl
	 * @param xmlP
	 * @param issues
	 * @param messagePrefix
	 * @throws Exception
	 */
	private void addReferenceToDocument(ResourceService resourceService, Bundle bundle, Reference resourceReference, String baseUrl, XmlParser xmlP, List<OperationOutcome.OperationOutcomeIssueComponent> issues, String messagePrefix) throws Exception {

		log.fine("[START] CompositionDocument.addReferenceToDocument");

		if (resourceReference != null && resourceReference.hasReference()) {
			ResourceContainer resourceContainer = null;
			String resourceType = null;
			String resourceId = null;
			ByteArrayInputStream iResource = null;
			Resource refResource = null;
			BundleEntryComponent bundleEntry = null;
			String fullUrl = null;
			boolean isDuplicate = false;
			LinkedResourceProxyObjectFactory linkedProxyFactory = new LinkedResourceProxyObjectFactory();

			try {
				// extract resource type and resource id from reference value
				resourceType = ServicesUtil.INSTANCE.getResourceTypeFromReference(resourceReference.getReference());
				resourceId = ServicesUtil.INSTANCE.extractResourceIdFromURL(resourceReference.getReference());

				if (resourceType != null && resourceId != null) {
					resourceContainer = resourceService.read(resourceType, resourceId, null);

					if (resourceContainer == null || resourceContainer.getResource() == null || !resourceContainer.getResponseStatus().equals(Response.Status.OK)) {
						issues.add(ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.WARNING, OperationOutcome.IssueType.NOTFOUND,
								messagePrefix + " read failed - " + resourceContainer.getResponseStatus().getStatusCode() + ". Cannot resolve and add to document contents.", null, null));
					}
					else {
						// Convert XML contents to Composition Resource object
						iResource = new ByteArrayInputStream(resourceContainer.getResource().getResourceContents());
						refResource = xmlP.parse(iResource);

						// Check for duplicate resource
						fullUrl = baseUrl + refResource.fhirType() + "/" + refResource.getId();
						isDuplicate = false;
						for (BundleEntryComponent checkEntry : bundle.getEntry()) {
							if (checkEntry.hasFullUrl() && checkEntry.getFullUrl().equals(fullUrl)) {
								isDuplicate = true;
								break;
							}
						}

						if (isDuplicate == false) {
							// Add Resource as next Bundle.entry
							bundleEntry = new BundleEntryComponent();
							bundleEntry.setFullUrl(fullUrl);
							// check Resource for narrative text; if not found, generate text
							if (refResource instanceof DomainResource) {
								if (!((DomainResource)refResource).hasText()) {
									FHIRNarrativeGeneratorClient.instance().generate(((DomainResource)refResource));
								}
							}
							bundleEntry.setResource(refResource);
							bundle.getEntry().add(bundleEntry);

							log.fine("      --> Resource added to document");

							log.fine("      --> Check for extensions with references");

							// getResourceExtensionReferences
							this.getResourceExtensionReferences(resourceService, bundle, refResource, baseUrl, xmlP, issues);

							log.fine("      --> Check for linked resources - 1 level deep only");

							// Check for linked resources
							LinkedResourceProxy linkedProxy = linkedProxyFactory.getLinkedResourceProxy(refResource.getResourceType().getPath());

							if (linkedProxy != null) {
								log.fine("      --> Found Linked Resource Proxy " + linkedProxy.getClass().getName());

								List<org.hl7.fhir.r4.model.Resource> linkedResources = linkedProxy.getLinkedResources(resourceService, refResource);

								if (linkedResources != null && !linkedResources.isEmpty()) {

									for (org.hl7.fhir.r4.model.Resource linkedResource : linkedResources) {
										log.fine("      --> Adding linked resource type " + linkedResource.getResourceType().getPath() + "; resource id " + linkedResource.getId());

										// Check for duplicate resource
										fullUrl = baseUrl + linkedResource.fhirType() + "/" + linkedResource.getId();
										isDuplicate = false;
										for (BundleEntryComponent checkEntry : bundle.getEntry()) {
											if (checkEntry.hasFullUrl() && checkEntry.getFullUrl().equals(fullUrl)) {
												isDuplicate = true;
												break;
											}
										}

										if (isDuplicate == false) {
											// Add Resource as next Bundle.entry
											bundleEntry = new BundleEntryComponent();
											bundleEntry.setFullUrl(fullUrl);
											// check Resource for narrative text; if not found, generate text
											if (linkedResource instanceof DomainResource) {
												if (!((DomainResource)linkedResource).hasText()) {
													FHIRNarrativeGeneratorClient.instance().generate(((DomainResource)linkedResource));
												}
											}
											bundleEntry.setResource(linkedResource);
											bundle.getEntry().add(bundleEntry);
										}
										else {
											log.fine("      --> [SKIP] DUPLICATE REFERENCE ALREADY IN DOCUMENT");
										}
									}
								}
								else {
									log.fine("      --> No Linked Resources found");
								}
							}
							else {
								log.fine("      --> Linked Resource Proxy for '" + refResource.getResourceType().getPath() + "' NOT FOUND");
							}
						}
						else {
							log.fine("      --> [SKIP] DUPLICATE REFERENCE ALREADY IN DOCUMENT");
						}
					}
				}
				else {
					log.severe("      --> [ERROR] VALID RESOURCE TYPE OR ID NOT FOUND IN REFERENCE");
					// generate OperationOutcome.issue
					issues.add(ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.PROCESSING,
							messagePrefix + " does not contain a valid resource type or id. Cannot resolve and add to document contents.", null, null));
				}
			}
			catch (Exception e) {
				// Capture exception as fatal OperationOutcome issue
				log.severe("      --> [FATAL] EXCEPTION! " + e.getMessage());

				// generate OperationOutcome.issue
				issues.add(ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.FATAL, OperationOutcome.IssueType.PROCESSING,
						messagePrefix + " - fatal exception! " + e.getMessage(), null, null));
			}
			finally {
				// Release local variables
				resourceContainer = null;
				resourceType = null;
				resourceId = null;
				iResource = null;
				refResource = null;
				bundleEntry = null;
				fullUrl = null;
				linkedProxyFactory = null;
			}
		}
		else {
			log.warning("      --> [SKIP] REFERENCE OR REFERENCE VALUE IS NULL");
		}
	}

	private void generateDocumentSignature(Bundle bundle, CodeService codeService, List<OperationOutcome.OperationOutcomeIssueComponent> issues) throws Exception {

		log.fine("[START] CompositionDocument.generateDocumentSignature()");

		Signature sig = null;
		Coding sigType = null;
		Reference sigWho = null;
		byte[] docSig = null;

		try {
			sig = new Signature();

			// Set Signature.type
			sigType = new Coding();
			sigType.setSystem("urn:iso-astm:E1762-95:2013");
			sigType.setCode("1.2.840.10065.1.12.1.14");
			sigType.setDisplay("Source Signature");
			sig.getType().add(sigType);

			// Set Signature.when
			sig.setWhen(new Date());

			// Set Signature.whoReference
			sigWho = new Reference();
			sigWho.setDisplay("WildFHIR Application");
			sig.setWho(sigWho);

			// Set Signature.contentType
			sig.setSigFormat("image/jpg");

			// Get document signature image file contents
			docSig = codeService.getCodeResourceContents("documentSignature");
			sig.setData(docSig);

			bundle.setSignature(sig);
		}
		catch (Exception e) {
			// Capture exception as fatal OperationOutcome issue
			log.severe("      --> [FATAL] EXCEPTION! " + e.getMessage());

			// generate OperationOutcome.issue
			issues.add(ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.FATAL, OperationOutcome.IssueType.PROCESSING,
					"Signature generation - fatal exception! " + e.getMessage(), null, null));
		}
		finally {
			// Release local variables
			docSig = null;
			sigWho = null;
			sigType = null;
			sig = null;
		}
	}

	/**
	 * @param composition
	 * @param resourceService
	 * @param narrativeGenerator
	 * @param xmlP
	 * @return Existing document Bundle or OperationOutcome
	 * @throws Exception
	 */
	private Resource getExistingCompositionDocument(Composition composition, ResourceService resourceService, XmlParser xmlP) throws Exception {

		log.fine("[START] CompositionDocument.getExistingCompositionDocument()");

		ByteArrayInputStream iResource = null;
		Resource returnResource = null;
		OperationOutcome rOutcome = null;
		OperationOutcome.OperationOutcomeIssueComponent issue = null;

		try {
			// Define composition document bundle search parameters
			MultivaluedMap<String, String> searchParams = new MultivaluedHashMap<String, String>();
			searchParams.add("composition._id", composition.getId());

			// Execute search query
			List<net.aegis.fhir.model.Resource> resources = resourceService.searchQuery(searchParams, null, null, "Bundle", false, null, null, null, null, null);

			log.info("CompositionDocument - existing document resources.size() = " + resources.size());

			if (resources != null && resources.size() == 1) {
				// Existing Composition document bundle found, return it
				iResource = new ByteArrayInputStream(resources.get(0).getResourceContents());
				returnResource = xmlP.parse(iResource);
			}
			else if (resources != null && resources.size() > 1) {
				// Error! Multiple Composition document bundles found, generate OperationOutcome
				// generate OperationOutcome.issue
				issue = ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.CONFLICT,
						"Composition $document failed. Multiple documents for Composition resource; only one persisted document should be found.", null, null);
			}
		}
		catch (Exception e) {
			// Capture exception as fatal OperationOutcome issue
			log.severe("      --> [FATAL] EXCEPTION! " + e.getMessage());

			// generate OperationOutcome.issue
			issue = ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.FATAL, OperationOutcome.IssueType.PROCESSING,
					"Composition $document failed. Error attempting to retrieve existing document for Composition: " + e.getMessage(), null, null);
		}
		finally {
			// Release local variables
			iResource = null;
		}

		if (issue != null) {
			// generate and return OperationOutcome
			rOutcome = new OperationOutcome();
			rOutcome.setText(null);
			rOutcome.getIssue().add(issue);

			// Use RI NarrativeGenerator
			FHIRNarrativeGeneratorClient.instance().generate(rOutcome);

			return rOutcome;
		}
		else {
			return returnResource;
		}
	}

	/**
	 *
	 * @param context
	 * @return <code>Parameters</code>
	 * @throws Exception
	 */
	private Parameters getParametersFromQueryParams(UriInfo context) throws Exception {

		log.fine("[START] CompositionDocument.getParametersFromQueryParams()");

		// Default empty Parameters
		Parameters queryParameters = new Parameters();

		try {
			if (context != null) {
				log.info("Checking for document parameters...");

				/*
				 * Extract the individual expected parameters
				 */
				BooleanType persist = null;
				UriType id = null;
				UriType graph = null;

				// Get the query parameters that represent the search criteria
				MultivaluedMap<String, String> queryParams = context.getQueryParameters();

				if (queryParams != null && queryParams.size() > 0) {
					ParametersParameterComponent parameter = null;
					Set<Entry<String, List<String>>> paramSet = queryParams.entrySet();

					for (Entry<String, List<String>> entry : paramSet) {

						String key = entry.getKey();
						String value = entry.getValue().get(0);

						if (key.equals("id")) {
							parameter = new ParametersParameterComponent();
							parameter.setName(key);
							id = new UriType();
							id.setValueAsString(value);
							parameter.setValue(id);
							queryParameters.addParameter(parameter);
						}
						else if (key.equals("persist")) {
							parameter = new ParametersParameterComponent();
							parameter.setName(key);
							persist = new BooleanType();
							persist.setValueAsString(value);
							parameter.setValue(persist);
							queryParameters.addParameter(parameter);
						}
						else if (key.equals("graph")) {
							parameter = new ParametersParameterComponent();
							parameter.setName(key);
							graph = new UriType();
							graph.setValueAsString(value);
							parameter.setValue(graph);
							queryParameters.addParameter(parameter);
						}
					}
				}
			}
		}
		catch (Exception e) {
			// Handle generic exceptions
			log.severe(e.getMessage());
			throw e;
		}

		return queryParameters;
	}

}
