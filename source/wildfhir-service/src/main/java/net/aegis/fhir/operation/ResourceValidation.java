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

import java.io.ByteArrayOutputStream;
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
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.validation.FHIRValidatorClient;

import org.hl7.fhir.r4.formats.JsonParser;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.UriType;
import org.hl7.fhir.r4.utils.NarrativeGenerator;

/**
 * Resource validation generic proxy implementation
 *
 * @author richard.ettema
 *
 */
public class ResourceValidation extends ResourceOperationProxy {

	private Logger log = Logger.getLogger("ResourceValidation");

	/* (non-Javadoc)
	 * @see net.aegis.fhir.operation.ResourceOperationProxy#executeOperation(javax.ws.rs.core.UriInfo, javax.ws.rs.core.HttpHeaders, net.aegis.fhir.service.ResourceService, net.aegis.fhir.service.ResourcemetadataService, net.aegis.fhir.service.BatchService, net.aegis.fhir.service.TransactionService, net.aegis.fhir.service.CodeService, net.aegis.fhir.service.ConformanceService, java.lang.String, java.lang.String, java.lang.String, org.hl7.fhir.r4.model.Parameters, org.hl7.fhir.r4.model.Resource, java.lang.String, java.lang.String, boolean, java.lang.StringBuffer)
	 */
	@Override
	public Parameters executeOperation(UriInfo context, HttpHeaders headers, ResourceService resourceService, ResourcemetadataService resourcemetadataService, BatchService batchService, TransactionService transactionService, CodeService codeService, ConformanceService conformanceService, String softwareVersion, String resourceType, String resourceId, Parameters inputParameters, org.hl7.fhir.r4.model.Resource inputResource, String inputString, String contentType, boolean isPost, StringBuffer returnedDirective) throws Exception {

		log.fine("[START] ResourceValidation.executeOperation()");

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
			Resource resource = null;
			CodeType mode = null;
			UriType uri = null;

			// Set resource from inputResource if present
			if (inputResource != null) {
				resource = inputResource;
			}

			if (inputParameters != null && inputParameters.hasParameter()) {

				for (ParametersParameterComponent parameter : inputParameters.getParameter()) {

					if (parameter.getName() != null && parameter.getName().equals("resource")) {

						resource = parameter.getResource();
					}

					if (parameter.getName() != null && parameter.getName().equals("mode")) {

						if (parameter.getValue() instanceof CodeType) {
							mode = (CodeType) parameter.getValue();
						}
					}

					if (parameter.getName() != null && parameter.getName().equals("profile")) {

						if (parameter.getValue() instanceof UriType) {
							uri = (UriType) parameter.getValue();
						}
					}
				}
			}

			if (mode == null || mode.isEmpty()) {
				out = defaultValidate(resourceService, resource, uri, resourceType, resourceId, contentType, inputString);
			}
			else {
				String modeString = mode.getValue();

				if (modeString.equals("create")) {
					/*
					 * create mode is against the resource type only; the resource parameter is required
					 */
					out = createValidate(resource, uri, resourceType, resourceId, contentType);
				}
				else if (modeString.equals("update")) {
					/*
					 * update mode is against the resource instance only; the resource parameter is required
					 */
					out = updateValidate(resourceService, resource, uri, resourceType, resourceId, contentType);
				}
				else if (modeString.equals("delete")) {
					/*
					 * delete mode is against the resource instance only; the resource parameter must not be present
					 */
					out = deleteValidate(resourceService, resource, uri, resourceType, resourceId);
				}
				else {
					/*
					 * Mis-matched parameters; return error OperationOutcome
					 */
					String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.CONFLICT, "Invalid mode parameter '" + modeString + "'.", null, null, "application/fhir+xml");

					XmlParser xmlParser = new XmlParser();

					OperationOutcome rOutcome = (OperationOutcome) xmlParser.parse(outcome);

					out = new Parameters();

					ParametersParameterComponent parameter = new ParametersParameterComponent();
					parameter.setName("return");
					parameter.setResource(rOutcome);

					out.addParameter(parameter);
				}
			}
		}
		catch (Exception e) {
			// Throw exceptions back
			throw e;
		}

		return out;
	}

	/**
	 * The default validate operation checks whether the attached content represents a valid instance
	 * of that resource type.
	 *
	 * @param resourceService
	 * @param resource
	 * @param uri
	 * @param resourceType
	 * @param resourceId
	 * @param contentType
	 * @param payload
	 * @return <code>Parameters</code>
	 */
	public Parameters defaultValidate(ResourceService resourceService, Resource resource, UriType uri, String resourceType, String resourceId, String contentType, String payload) throws Exception {

		log.fine("[START] ResourceValidation.defaultValidate()");

		OperationOutcome rOutcome = new OperationOutcome();
		Parameters out = null;

		String profileUrl = null;
		if (uri != null) {
			profileUrl = uri.getValue();
		}

		try {
			if (payload != null) {
				// Check for and validate raw payload if present

				// FHIRValidatorClient singleton validate method
				rOutcome = FHIRValidatorClient.instance().validateResource(resource.getResourceType().name(), payload.getBytes(), profileUrl);
			}
			else if (resource != null) {
				// Check for and validate processed resource instance if present

				ByteArrayOutputStream oOp = new ByteArrayOutputStream();

				// FHIR-167 - Add check for possible contentType equal to null
				if (contentType != null && contentType.indexOf("json") >= 0) {
					JsonParser jsonParser = new JsonParser();
					jsonParser.compose(oOp, resource);
				}
				else {
					// Default to XML if not JSON
					XmlParser xmlParser = new XmlParser();
					xmlParser.compose(oOp, resource, true);
				}

				// FHIRValidatorClient singleton validate method
				rOutcome = FHIRValidatorClient.instance().validateResource(resource.getResourceType().name(), oOp.toByteArray(), profileUrl);
			}
			else {
				// Check for resourceId sent via GET operation
				if (resourceId != null) {
					// Perform read operation based on resourceType
					ResourceContainer resourceContainer = resourceService.read(resourceType, resourceId, null);

					if (resourceContainer == null || resourceContainer.getResource() == null || !resourceContainer.getResponseStatus().equals(Response.Status.OK)) {
						OperationOutcome.OperationOutcomeIssueComponent issue =
								ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND,
										"$validate failed. No [resource] parameter value found and the [id] url value does not reference a valid, non-deleted resource instance.", null, null);

						if (issue != null) {
							rOutcome.setText(null);
							rOutcome.getIssue().add(issue);

							// Use RI NarrativeGenerator
							NarrativeGenerator narrativeGenerator = new NarrativeGenerator("", "", null);
							narrativeGenerator.generate(rOutcome, null);
						}
					}
					else {
						// Resource instance found for resourceId, validate it

						// FHIRValidatorClient singleton validate method
						rOutcome = FHIRValidatorClient.instance().validateResource(resourceType, resourceContainer.getResource().getResourceContents(), profileUrl);
					}
				}
				else {
					rOutcome = new OperationOutcome();

					OperationOutcome.OperationOutcomeIssueComponent issue =
							ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.INCOMPLETE,
									"$validate failed. No [resource] parameter value found. Validation of create mode cannot be performed without a [resource] parameter.", null, null);

					if (issue != null) {
						rOutcome.setText(null);
						rOutcome.getIssue().add(issue);

						// Use RI NarrativeGenerator
						NarrativeGenerator narrativeGenerator = new NarrativeGenerator("", "", null);
						narrativeGenerator.generate(rOutcome, null);
					}
				}
			}

			out = new Parameters();

			ParametersParameterComponent parameter = new ParametersParameterComponent();
			parameter.setName("return");
			parameter.setResource(rOutcome);

			out.addParameter(parameter);
		}
		catch (Exception e) {
			// Throw exceptions back
			throw e;
		}

		return out;

	}

	/**
	 * The create validate operation checks whether the attached content would be acceptable in a create
	 * operation. The validation operation may be the first part of a light two- phase commit process.
	 *
	 * create mode is against the resource type only; the resource parameter is required
	 *
	 * @param resource
	 * @param uri
	 * @param resourceId
	 * @param contentType
	 * @return <code>Parameters</code>
	 */
	public Parameters createValidate(Resource resource, UriType uri, String resourceType, String resourceId, String contentType) throws Exception {

		log.fine("[START] ResourceValidation.createValidate()");

		OperationOutcome rOutcome = new OperationOutcome();
		Parameters out = null;

		try {
			if (resource != null) {

				// Use FHIR RI Validator
				ByteArrayOutputStream oOp = new ByteArrayOutputStream();

				// FHIR-167 - Add check for possible contentType equal to null
				if (contentType != null && contentType.indexOf("json") >= 0) {
					JsonParser jsonParser = new JsonParser();
					jsonParser.compose(oOp, resource);
				}
				else {
					// Default to XML if not JSON
					XmlParser xmlParser = new XmlParser();
					xmlParser.compose(oOp, resource, true);
				}

				String profileUrl = null;
				if (uri != null) {
					profileUrl = uri.getValue();
				}

				// FHIRValidatorClient singleton validate method
				rOutcome = FHIRValidatorClient.instance().validateResource(resource.getResourceType().name(), oOp.toByteArray(), profileUrl);
			}
			else {
				rOutcome = new OperationOutcome();

				OperationOutcome.OperationOutcomeIssueComponent issue = ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.INCOMPLETE,
						"$validate failed. No [resource] parameter value found. Validation of create mode cannot be performed without a [resource] parameter.", null, null);

				if (issue != null) {
					rOutcome.setText(null);
					rOutcome.getIssue().add(issue);

					// Use RI NarrativeGenerator
					NarrativeGenerator narrativeGenerator = new NarrativeGenerator("", "", null);
					narrativeGenerator.generate(rOutcome, null);
				}
			}

			out = new Parameters();

			ParametersParameterComponent parameter = new ParametersParameterComponent();
			parameter.setName("return");
			parameter.setResource(rOutcome);

			out.addParameter(parameter);
		}
		catch (Exception e) {
			// Throw exceptions back
			throw e;
		}

		return out;

	}

	/**
	 * The update validate operation checks whether the attached content would be acceptable as an update to an
	 * existing resource. The validation operation may be the first part of a light two- phase commit process.
	 *
	 * @param resourceService
	 * @param resource
	 * @param uri
	 * @param resourceType
	 * @param resourceId
	 * @param contentType
	 * @return <code>Parameters</code>
	 */
	public Parameters updateValidate(ResourceService resourceService, Resource resource, UriType uri, String resourceType, String resourceId, String contentType) throws Exception {

		log.fine("[START] ResourceValidation.updateValidate()");

		OperationOutcome rOutcome = new OperationOutcome();
		Parameters out = null;

		try {
			/*
			 * update validation check - $validate.update must be executed against an existing resource instance; i.e.
			 * the url path must contain the id [base]/[resource]/[id]/$validate
			 */
			if (resourceId != null) {

				if (resource != null) {

					// Use FHIR RI Validator
					ByteArrayOutputStream oOp = new ByteArrayOutputStream();

					// FHIR-167 - Add check for possible contentType equal to null
					if (contentType != null && contentType.indexOf("json") >= 0) {
						JsonParser jsonParser = new JsonParser();
						jsonParser.compose(oOp, resource);
					}
					else {
						// Default to XML if not JSON
						XmlParser xmlParser = new XmlParser();
						xmlParser.compose(oOp, resource, true);
					}

					String profileUrl = null;
					if (uri != null) {
						profileUrl = uri.getValue();
					}

					// FHIRValidatorClient singleton validate method
					rOutcome = FHIRValidatorClient.instance().validateResource(resource.getResourceType().name(), oOp.toByteArray(), profileUrl);

					/*
					 * update validation check - insure resource content does contain an id element - insure resource
					 * content contains a matching id element value to the url [id] value - insure resource instance for
					 * the url [id] value exists
					 */
					if (resource.hasIdElement()) {

						if (!resource.getId().equals(resourceId)) {
							OperationOutcome.OperationOutcomeIssueComponent issue = ServicesUtil.INSTANCE
									.getOperationOutcomeIssueComponent(
											OperationOutcome.IssueSeverity.ERROR,
											OperationOutcome.IssueType.CONFLICT,
											"$validate failed. The update operation will fail. The [id] url value does not match the id element found in the resource contents.  The request body SHALL be a Resource with an id element that has an identical value to the [id] in the URL.",
											null,
											"/" + resourceType + "/id");

							if (issue != null) {
								rOutcome.setText(null);
								rOutcome.getIssue().add(issue);

								// Use RI NarrativeGenerator
								NarrativeGenerator narrativeGenerator = new NarrativeGenerator("", "", null);
								narrativeGenerator.generate(rOutcome, null);
							}
						}
						else {
							ResourceContainer resourceContainer = resourceService.read(resource.getResourceType().name(), resourceId, null);

							if (resourceContainer == null || resourceContainer.getResource() == null || !resourceContainer.getResponseStatus().equals(Response.Status.OK)) {
								OperationOutcome.OperationOutcomeIssueComponent issue = ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.CONFLICT,
										"$validate failed. The update operation will fail. The [id] url value or the id element found in the resource contents does not reference a valid resource instance.", null, "/" + resourceType + "/id");

								if (issue != null) {
									rOutcome.setText(null);
									rOutcome.getIssue().add(issue);

									// Use RI NarrativeGenerator
									NarrativeGenerator narrativeGenerator = new NarrativeGenerator("", "", null);
									narrativeGenerator.generate(rOutcome, null);
								}
							}
						}
					}
					else {
						OperationOutcome.OperationOutcomeIssueComponent issue = ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.CONFLICT,
								"$validate failed. The update operation will fail. No id element found in the resource contents. The request body SHALL be a Resource with an id element that has an identical value to the [id] in the URL.", null,
								"/" + resourceType + "/id");

						if (issue != null) {
							rOutcome.setText(null);
							rOutcome.getIssue().add(issue);

							// Use RI NarrativeGenerator
							NarrativeGenerator narrativeGenerator = new NarrativeGenerator("", "", null);
							narrativeGenerator.generate(rOutcome, null);
						}
					}
				}
				else {
					rOutcome = new OperationOutcome();

					OperationOutcome.OperationOutcomeIssueComponent issue = ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.INCOMPLETE,
							"$validate failed. No [resource] parameter value found. Validation of update mode cannot be performed without a [resource] parameter.", null, null);

					if (issue != null) {
						rOutcome.setText(null);
						rOutcome.getIssue().add(issue);

						// Use RI NarrativeGenerator
						NarrativeGenerator narrativeGenerator = new NarrativeGenerator("", "", null);
						narrativeGenerator.generate(rOutcome, null);
					}
				}
			}
			else {
				rOutcome = new OperationOutcome();

				OperationOutcome.OperationOutcomeIssueComponent issue = ServicesUtil.INSTANCE
						.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.INCOMPLETE,
								"$validate failed. The update operation will fail. No [id] value found in the $validate path. The request body SHALL be a Resource with an id element that has an identical value to the [id] in the URL.",
								null,
								"/" + resourceType + "/id");

				if (issue != null) {
					rOutcome.setText(null);
					rOutcome.getIssue().add(issue);

					// Use RI NarrativeGenerator
					NarrativeGenerator narrativeGenerator = new NarrativeGenerator("", "", null);
					narrativeGenerator.generate(rOutcome, null);
				}
			}

			out = new Parameters();

			ParametersParameterComponent parameter = new ParametersParameterComponent();
			parameter.setName("return");
			parameter.setResource(rOutcome);

			out.addParameter(parameter);
		}
		catch (Exception e) {
			// Throw exceptions back
			throw e;
		}

		return out;

	}

	/**
	 * The delete validate operation checks whether the indicated resource instance would be acceptable for a
	 * delete operation. The validation operation may be the first part of a light two- phase commit process.
	 *
	 * @param resourceService
	 * @param resource
	 * @param uri
	 * @param resourceType
	 * @param resourceId
	 * @return <code>Parameters</code>
	 */
	public Parameters deleteValidate(ResourceService resourceService, Resource resource, UriType uri, String resourceType, String resourceId) throws Exception {

		log.fine("[START] ResourceValidation.deleteValidate()");

		OperationOutcome rOutcome = new OperationOutcome();
		Parameters out = null;

		try {
			/*
			 * delete validation check
			 * - $validate.delete must be executed against an existing resource instance; i.e. the url path must contain the id [base]/[resource]/[id]/$validate
			 * - insure resource content is null or empty
			 * - insure resource instance for the url [id] value exists and is not deleted
			 */
			if (resourceId != null) {

				if (resource == null) {
					ResourceContainer resourceContainer = resourceService.read(resourceType, resourceId, null);

					if (resourceContainer == null || resourceContainer.getResource() == null || !resourceContainer.getResponseStatus().equals(Response.Status.OK)) {
						OperationOutcome.OperationOutcomeIssueComponent issue =
								ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.CONFLICT,
										"$validate failed. The delete operation will fail. The [id] url value does not reference a valid, non-deleted resource instance.", null, null);

						if (issue != null) {
							rOutcome.setText(null);
							rOutcome.getIssue().add(issue);

							// Use RI NarrativeGenerator
							NarrativeGenerator narrativeGenerator = new NarrativeGenerator("", "", null);
							narrativeGenerator.generate(rOutcome, null);
						}
					}
					else {
						// No issues detected
					}
				}
				else {
					OperationOutcome.OperationOutcomeIssueComponent issue =
							ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.INVALID,
									"$validate failed. The resource contents shall be empty for the $validate operation delete mode.", null, null);

					if (issue != null) {
						rOutcome.setText(null);
						rOutcome.getIssue().add(issue);

						// Use RI NarrativeGenerator
						NarrativeGenerator narrativeGenerator = new NarrativeGenerator("", "", null);
						narrativeGenerator.generate(rOutcome, null);
					}
				}
			}
			else {
				OperationOutcome.OperationOutcomeIssueComponent issue =
						ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.INCOMPLETE,
								"$validate failed. The delete operation will fail. No [id] value found in the $validate path.  The interaction is performed by an HTTP DELETE command as shown: DELETE [base]/[type]/[id].", null, null);

				if (issue != null) {
					rOutcome.setText(null);
					rOutcome.getIssue().add(issue);

					// Use RI NarrativeGenerator
					NarrativeGenerator narrativeGenerator = new NarrativeGenerator("", "", null);
					narrativeGenerator.generate(rOutcome, null);
				}
			}

			out = new Parameters();

			ParametersParameterComponent parameter = new ParametersParameterComponent();
			parameter.setName("return");
			parameter.setResource(rOutcome);

			out.addParameter(parameter);
		}
		catch (Exception e) {
			// Throw exceptions back
			throw e;
		}

		return out;

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
				log.info("Checking for validate parameters...");

				/*
				 * Extract the individual expected parameters
				 */
				CodeType mode = null;
				UriType uri = null;

				// Get the query parameters that represent the search criteria
				MultivaluedMap<String, String> queryParams = context.getQueryParameters();

				if (queryParams != null && queryParams.size() > 0) {
					Set<Entry<String, List<String>>> paramSet = queryParams.entrySet();

					for (Entry<String, List<String>> entry : paramSet) {

						String key = entry.getKey();
						String value = entry.getValue().get(0);

						if (key.equals("mode")) {
							ParametersParameterComponent parameter = new ParametersParameterComponent();
							parameter.setName(key);
							mode = new CodeType();
							mode.setValue(value);
							parameter.setValue(mode);
							queryParameters.addParameter(parameter);
						}
						else if (key.equals("profile")) {
							ParametersParameterComponent parameter = new ParametersParameterComponent();
							parameter.setName(key);
							uri = new UriType();
							uri.setValue(value);
							parameter.setValue(uri);
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
