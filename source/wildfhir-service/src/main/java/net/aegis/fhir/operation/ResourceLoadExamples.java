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
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import net.aegis.fhir.model.ResourceContainer;
import net.aegis.fhir.service.BatchService;
import net.aegis.fhir.service.CodeService;
import net.aegis.fhir.service.ConformanceService;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.ResourcemetadataService;
import net.aegis.fhir.service.TransactionService;
import net.aegis.fhir.service.util.UUIDUtil;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.r4.formats.JsonParser;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.formats.IParser.OutputStyle;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;

/**
 * @author richard.ettema
 *
 */
public class ResourceLoadExamples extends ResourceOperationProxy {

	private Logger log = Logger.getLogger("ResourceLoadExamples");

	private ResourceService resourceService;
	private BatchService batchService;
	private TransactionService transactionService;

	private int resourcesImported = 0;
	private int resourcesSkipped = 0;

	private XmlParser xmlP;
	private JsonParser jsonP;

	/* (non-Javadoc)
	 * @see net.aegis.fhir.operation.ResourceOperationProxy#executeOperation(javax.ws.rs.core.UriInfo, javax.ws.rs.core.HttpHeaders, net.aegis.fhir.service.ResourceService, net.aegis.fhir.service.ResourcemetadataService, net.aegis.fhir.service.BatchService, net.aegis.fhir.service.TransactionService, net.aegis.fhir.service.CodeService, net.aegis.fhir.service.ConformanceService, java.lang.String, java.lang.String, java.lang.String, org.hl7.fhir.r4.model.Parameters, org.hl7.fhir.r4.model.Resource, java.lang.String, java.lang.String, boolean, java.lang.StringBuffer)
	 */
	@Override
	public Parameters executeOperation(UriInfo context, HttpHeaders headers, ResourceService resourceService, ResourcemetadataService resourcemetadataService, BatchService batchService, TransactionService transactionService, CodeService codeService, ConformanceService conformanceService, String softwareVersion, String resourceType, String resourceId, Parameters inputParameters, org.hl7.fhir.r4.model.Resource inputResource, String inputString, String contentType, boolean isPost, StringBuffer returnedDirective) throws Exception {

		log.fine("[START] ResourceLoadExamples.executeOperation()");

		this.resourceService = resourceService;
		this.batchService = batchService;
		this.transactionService = transactionService;

		Parameters out = null;

		try {
			/*
			 * If inputParameters is null, attempt to extract parameters from context
			 */
			if (inputParameters == null) {
				inputParameters = getParametersFromQueryParams(context);
			}

			/*
			 * Extract the individual expected parameters
			 */
			StringType baseurl = null;
			StringType dirpath = null;

			if (inputParameters != null && inputParameters.hasParameter()) {

				for (ParametersParameterComponent parameter : inputParameters.getParameter()) {

					if (parameter.getName() != null) {

						if (parameter.getName().equals("baseurl") && parameter.getValue() instanceof StringType) {
							baseurl = (StringType) parameter.getValue();
						}
						else if (parameter.getName().equals("dirpath") && parameter.getValue() instanceof StringType) {
							dirpath = (StringType) parameter.getValue();
						}
					}
				}
			}

			if (dirpath != null) {
				xmlP = new XmlParser();
				xmlP.setOutputStyle(OutputStyle.PRETTY);
				jsonP = new JsonParser();
				out = processDirectory(context, headers, contentType, dirpath, baseurl);
			}
		}
		catch (Exception e) {
			// Throw exceptions back
			throw e;
		}

		return out;
	}

	/**
	 * <p>
	 * This method does the actual work of inserting the resource into the repository.
	 * </p>
	 *
	 * @param dirpath
	 *            - path to directory of files to be inserted
	 */
	private Parameters processDirectory(UriInfo context, HttpHeaders headers, String contentType, StringType dirpath, StringType baseurl) {

		log.info("Process directory of resources");

		Parameters out = new Parameters();
		ParametersParameterComponent parameter = new ParametersParameterComponent();
		parameter.setName("result");
		out.addParameter(parameter);

		if (dirpath != null && baseurl != null) {

			File dir = new File(dirpath.getValue());

			if (dir.listFiles() != null) {

				for (File file : dir.listFiles()) {
					String format = file.getName().substring(file.getName().lastIndexOf('.') + 1, file.getName().length()).toUpperCase();

					processResourceFileImport(context, headers, contentType, format, file, baseurl.getValue());
				}
			}

			StringType valueString = new StringType("Processing complete. " + resourcesImported + " resources imported; " + resourcesSkipped + " resources skipped.");
			parameter.setValue(valueString);
		}
		else {
			// A required parameter was null, report error
			StringType valueString = new StringType("Processing aborted. dirpath or baseurl was not defined.");
			parameter.setValue(valueString);
		}

		return out;
	}

	/**
	 * <p>
	 * This method does the actual work of inserting the resource into the repository.
	 * </p>
	 *
	 * @param format
	 *            - format type of the resource file (XML or JSON)
	 * @param file
	 *            - file to be inserted
	 * @throws IOException
	 * @throws SQLException
	 */
	private void processResourceFileImport(UriInfo context, HttpHeaders headers, String contentType, String format, File file, String baseurl) {

		log.info("Process as resource: " + file.getName());

		try {
			byte fileContent[] = FileUtils.readFileToByteArray(file);

			ByteArrayInputStream iResource = new ByteArrayInputStream(fileContent);
			Resource exampleResource;
			ByteArrayOutputStream oResource = null;

			if (format.equals("XML")) {
				exampleResource = xmlP.parse(iResource); // Parse into a base Resource object
			}
			else {
				exampleResource = jsonP.parse(iResource); // Parse into a base Resource object
				// Convert fileContent to XML for WildFHIR resource update
				oResource = new ByteArrayOutputStream();
				xmlP.compose(oResource, exampleResource, true);
				fileContent = oResource.toByteArray();
			}

			boolean okToProcessResource = true;
			boolean okToProcessBundleBatch = false;
			boolean okToProcessBundleCollection = false;
			boolean okToProcessBundleTransaction = false;

			if (exampleResource.getResourceType().name().equals("Bundle")) {
				BundleType bundleType = ((Bundle)exampleResource).getType();

				if (bundleType.equals(BundleType.BATCH)) {
					okToProcessResource = false;
					okToProcessBundleBatch = true;
				}

				if (bundleType.equals(BundleType.COLLECTION)) {
					okToProcessResource = false;
					okToProcessBundleCollection = true;
				}

				if (bundleType.equals(BundleType.TRANSACTION)) {
					okToProcessResource = false;
					okToProcessBundleTransaction = true;
				}
			}

			if (okToProcessResource) {
				// Initialize a DB Resource to be updated
				net.aegis.fhir.model.Resource updateResource = new net.aegis.fhir.model.Resource();
				updateResource.setResourceType(exampleResource.getResourceType().name());
				updateResource.setResourceContents(fileContent);

				// Verify resource id; if not defined, use filename
				String resourceId = null;
				if (exampleResource.hasId()) {
					resourceId = exampleResource.getId();
				}
				else {
					resourceId = file.getName();
				}

				//resourceService.update(resourceId, updateResource, "http://wildfhir.aegis.net/fhir");
				resourceService.update(resourceId, updateResource, baseurl);

				log.warning(" --> Import of resource [" + resourceId + "] complete.");
				resourcesImported++;
			}
			else if (okToProcessBundleCollection) {
				// Initialize a DB Resource to be updated
				net.aegis.fhir.model.Resource updateResource = null;

				// Get Bundle resource id; if not defined, use filename
				String bundleResourceId = null;
				if (exampleResource.hasId()) {
					bundleResourceId = exampleResource.getId();
				}
				else {
					bundleResourceId = file.getName();
				}

				log.info(" --> Importing resources for Bundle [" + bundleResourceId + "]...");

				Bundle bundleResource = (Bundle)exampleResource;
				Resource entryResource = null;
				String entryResourceId = null;

				for (BundleEntryComponent entry : bundleResource.getEntry()) {
					updateResource = new net.aegis.fhir.model.Resource();

					entryResource = entry.getResource();

					// Convert the Resource to XML byte[]
					oResource = new ByteArrayOutputStream();
					xmlP.compose(oResource, entryResource, true);
					byte[] bResource = oResource.toByteArray();

					updateResource.setResourceType(entryResource.getResourceType().name());
					updateResource.setResourceContents(bResource);

					// Verify entry resource id; if not defined, generate GUID
					if (entryResource.hasId()) {
						entryResourceId = entryResource.getId();
					}
					else {
						entryResourceId = UUIDUtil.getGUID();
					}

					resourceService.update(entryResourceId, updateResource, baseurl);

					log.info(" --> Import of resource [" + entryResourceId + "] complete.");
					resourcesImported++;
				}

				log.warning(" --> Collection Bundle [" + bundleResourceId + "] complete");
			}
			else if (okToProcessBundleBatch) {
				// Process batch bundle

				// Get Bundle resource id; if not defined, use filename
				String bundleResourceId = null;
				if (exampleResource.hasId()) {
					bundleResourceId = exampleResource.getId();
				}
				else {
					bundleResourceId = file.getName();
				}

				log.info(" --> Process resources for Batch Bundle [" + bundleResourceId + "]...");

				Bundle bundleResource = (Bundle)exampleResource;

				ResourceContainer resourceContainer = batchService.batch(context, headers, contentType, contentType, bundleResource, null, null);

				if (resourceContainer != null &&
						resourceContainer.getResponseStatus().equals(Response.Status.OK) &&
						resourceContainer.getBundle() != null) {

					if (resourceContainer.getBundle().hasEntry()) {
						resourcesImported += resourceContainer.getBundle().getEntry().size();
					}
				}
				else {
					if (bundleResource.hasEntry()) {
						resourcesSkipped += bundleResource.getEntry().size();
					}
				}

				log.warning(" --> Batch Bundle [" + bundleResourceId + "] complete.");
			}
			else if (okToProcessBundleTransaction) {
				// Process transaction bundle

				// Get Bundle resource id; if not defined, use filename
				String bundleResourceId = null;
				if (exampleResource.hasId()) {
					bundleResourceId = exampleResource.getId();
				}
				else {
					bundleResourceId = file.getName();
				}

				log.info(" --> Process resources for Transaction Bundle [" + bundleResourceId + "]...");

				Bundle bundleResource = (Bundle)exampleResource;

				ResourceContainer resourceContainer = transactionService.transaction(context, headers, contentType, contentType, bundleResource, null, null);

				if (resourceContainer != null &&
						resourceContainer.getResponseStatus().equals(Response.Status.OK) &&
						resourceContainer.getBundle() != null) {

					if (resourceContainer.getBundle().hasEntry()) {
						resourcesImported += resourceContainer.getBundle().getEntry().size();
					}
				}
				else {
					if (bundleResource.hasEntry()) {
						resourcesSkipped += bundleResource.getEntry().size();
					}
				}

				log.warning(" --> Transaction Bundle [" + bundleResourceId + "] complete.");
			}
			else {
				log.info(" --> Import of resource file [" + file.getName() + "] skipped.");
				resourcesSkipped++;
			}
		}
		catch (Exception e) {
			// Swallow exception in order to continue processing remaining files
			log.warning(" --> Failed import of resource file! [" + file.getName() + "] -- " + e.getMessage());
		}

	}

	/**
	 *
	 * @param context
	 * @return <code>Parameters</code>
	 * @throws Exception
	 */
	private Parameters getParametersFromQueryParams(UriInfo context) throws Exception {

		log.fine("[START] ResourceOperationsRESTService.getParametersFromQueryParams()");

		// Default empty Parameters
		Parameters queryParameters = new Parameters();

		try {
			if (context != null) {
				log.info("Checking for search parameters...");

				/*
				 * Extract the individual expected parameters
				 */
		        StringType baseurl = null;
		        StringType dirpath = null;

				// Get the query parameters that represent the search criteria
				MultivaluedMap<String, String> queryParams = context.getQueryParameters();

				if (queryParams != null && queryParams.size() > 0) {
					Set<Entry<String, List<String>>> paramSet = queryParams.entrySet();

					for (Entry<String, List<String>> entry : paramSet) {

						String key = entry.getKey();
						String value = entry.getValue().get(0);

						if (key.equals("baseurl")) {
							ParametersParameterComponent parameter = new ParametersParameterComponent();
							parameter.setName(key);
							baseurl = new StringType();
							baseurl.setValue(value);
							parameter.setValue(baseurl);
							queryParameters.addParameter(parameter);
						}
						else if (key.equals("dirpath")) {
							ParametersParameterComponent parameter = new ParametersParameterComponent();
							parameter.setName(key);
							dirpath = new StringType();
							dirpath.setValue(value);
							parameter.setValue(dirpath);
							queryParameters.addParameter(parameter);
						}
					}
				}
			}
		}
		catch (Exception e) {
			// Handle generic exceptions
			log.severe(e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return queryParameters;
	}

}
