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
package net.aegis.fhir.rest;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.Encoded;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import net.aegis.fhir.model.Constants;
import net.aegis.fhir.service.CodeService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.validation.FHIRValidatorClient;

import org.apache.commons.io.IOUtils;
import org.hl7.fhir.r4.formats.JsonParser;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StringType;

/**
 * JAX-RS FHIR Path Evaluator Service
 * <p/>
 * This class produces the RESTful services for the FHIR Path Evaluator Service Operations.
 *
 * @author richard.ettema
 *
 */
@Path("/fhirpath/evaluate")
@ApplicationScoped
@Encoded
public class FHIRPathEvaluatorRESTService {

	private static final Logger log = Logger.getLogger("FHIRPathEvaluatorRESTService");

	@Inject
    CodeService codeService;

	@Context
	private UriInfo context;

	/**
	 * Validate POST operation where expected parameters are passed in a FHIR Parameters payload.
	 *
	 * @param request
	 * @param headers
	 * @param ui
	 * @param validateInputStream
	 * @return <code>Response</code> containing a FHIR Parameters payload
	 */
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json" })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json" })
	public Response evaluatePost(@Context HttpServletRequest request, @Context HttpHeaders headers, @Context UriInfo ui, InputStream evalutateInputStream) {

		log.fine("[START] FHIRPathEvaluatorRESTService.evaluatePost()");

		debugRequest(request, headers, null);

		Response.ResponseBuilder builder = null;
		String contentType = null;
		String producesType = null;
		Parameters outputParameters = new Parameters();
		ParametersParameterComponent outputParameter = null;
		ByteArrayOutputStream oOp = new ByteArrayOutputStream();
		XmlParser xmlP = new XmlParser();
		JsonParser jsonP = new JsonParser();
        String responseFhirVersion = "";
        try {
        	responseFhirVersion = codeService.getCodeValue("supportedVersions");
			if (responseFhirVersion != null) {
				responseFhirVersion = "; fhirVersion=" + responseFhirVersion;
			}
        }
        catch (Exception e) {
			responseFhirVersion = "";
        }

		try {
			// Get the content type based on the request Content-Type
			contentType = ServicesUtil.INSTANCE.getHttpHeader(headers, HttpHeaders.CONTENT_TYPE);

			// Get the produces type based on the request Accept
			producesType = ServicesUtil.INSTANCE.getProducesType(headers, context);

			// Validate input format check; instantiate the Parameters
			Parameters inputParameters = null;

			if (evalutateInputStream != null) {
				// POST Operations with expected ValidationParameters payload
				log.info("POST evaluate operation with expected Parameters payload");

				if (contentType.indexOf("xml") >= 0) {
					// Convert XML contents to Resource
					inputParameters = (Parameters) xmlP.parse(evalutateInputStream);
				} else if (contentType.indexOf("json") >= 0) {
					// Convert JSON contents to Resource
					inputParameters = (Parameters) jsonP.parse(evalutateInputStream);
				} else {
					// convert input stream to String
					String payload = IOUtils.toString(evalutateInputStream, "UTF-8");
					// contentType did not contain a valid media type or was null; attempt to determine based on starting character
					int firstValid = payload.indexOf("<"); // check for xml first
					if (firstValid > -1 && firstValid < 5) {
						if (firstValid > 0) {
							payload = payload.substring(firstValid);
						}
						// Convert XML contents to Resource
						inputParameters = (Parameters) xmlP.parse(payload.getBytes());
						contentType = "xml";
					} else {
						firstValid = payload.indexOf("{"); // check for json next
						if (firstValid > -1 && firstValid < 5) {
							if (firstValid > 0) {
								payload = payload.substring(firstValid);
							}
							// Convert JSON contents to Resource
							inputParameters = (Parameters) jsonP.parse(payload.getBytes());
							contentType = "json";
						}
					}
				}
			}

			if (inputParameters != null) {
				// Extract expected parameters
				log.info("Extract expected evaluate parameters");
				StringType method = null;
				Resource resource = null;
				byte[] resourceContents = null;
				StringType expression = null;

				if (inputParameters.hasParameter()) {

					for (ParametersParameterComponent parameter : inputParameters.getParameter()) {

						if (parameter.getName() != null && parameter.getName().equals("method")) {

							if (parameter.getValue() instanceof StringType) {
								method = (StringType) parameter.getValue();
							}
						}

						if (parameter.getName() != null && parameter.getName().equals("resource")) {

							resource = parameter.getResource();

							if (contentType.indexOf("xml") >= 0) {
								// Convert Resource to base64binary XML contents
								oOp = new ByteArrayOutputStream();
								xmlP.compose(oOp, resource);
							} else if (contentType.indexOf("json") >= 0) {
								// Convert Resource to base64binary JSON contents
								oOp = new ByteArrayOutputStream();
								jsonP.compose(oOp, resource);
							}

							resourceContents = oOp.toByteArray();
						}

						if (parameter.getName() != null && parameter.getName().equals("expression")) {

							if (parameter.getValue() instanceof StringType) {
								expression = (StringType) parameter.getValue();
							}
						}
					}

					// Build evaluate output parameters
					outputParameter = new ParametersParameterComponent();
					outputParameter.setName("method");
					outputParameter.setValue(method);
					outputParameters.getParameter().add(outputParameter);

					// Call evaluate based on method and build evaluate output parameters
					if (method.getValue().equalsIgnoreCase("evaluate")) {
						log.warning("Calling FHIRValidatorClient with parameters for evaluate method");
						List<Base> items = FHIRValidatorClient.instance().evaluate(resourceContents, expression.getValue());

						// Iterate over list of base items and add parameter for each one based on its type
						for (Base item : items) {

							outputParameter = new ParametersParameterComponent();
							outputParameter.setName("returnItem");

							if (item.isPrimitive()) {
								StringType itemValue = new StringType(item.primitiveValue());
								outputParameter.setValue(itemValue);
							}
							else if (item instanceof Resource) {
								outputParameter.setResource((Resource) item);
							}
							else {
								StringType itemValue = new StringType(item.getClass().getName() + "CONTENTS NOT AVAILABLE");
								outputParameter.setValue(itemValue);
							}
							outputParameters.getParameter().add(outputParameter);
						}
					}
					else if (method.getValue().equalsIgnoreCase("evaluateToBoolean")) {
						log.warning("Calling FHIRValidatorClient with parameters for evaluateToBoolean method");
						boolean result = FHIRValidatorClient.instance().evaluateToBoolean(resourceContents, expression.getValue());

						outputParameter = new ParametersParameterComponent();
						outputParameter.setName("returnBoolean");
						StringType itemValue = new StringType(Boolean.toString(result));
						outputParameter.setValue(itemValue);
						outputParameters.getParameter().add(outputParameter);
					}
					else if (method.getValue().equalsIgnoreCase("evaluateToString")) {
						log.warning("Calling FHIRValidatorClient with parameters for evaluateToString method");
						String result = FHIRValidatorClient.instance().evaluateToString(resourceContents, expression.getValue());

						outputParameter = new ParametersParameterComponent();
						outputParameter.setName("returnString");
						StringType itemValue = new StringType(result);
						outputParameter.setValue(itemValue);
						outputParameters.getParameter().add(outputParameter);
					}

					if (contentType.indexOf("xml") >= 0) {
						// Convert Resource to string XML contents
						oOp = new ByteArrayOutputStream();
						xmlP.compose(oOp, outputParameters);
					} else if (contentType.indexOf("json") >= 0) {
						// Convert Resource to string JSON contents
						oOp = new ByteArrayOutputStream();
						jsonP.compose(oOp, outputParameters);
					}

					builder = Response.status(Response.Status.OK).entity(oOp.toString()).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
				}
				else {
					// Missing parameters in Parameters payload
					String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.TRANSIENT, "operation failure, input parameters not found", null, null, producesType);

					builder = Response.status(Response.Status.BAD_REQUEST).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
				}
			}
			else {
				// Missing or invalid XML format of Parameters payload
				String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.TRANSIENT, "operation failure, input ValidationParameters missing or not formatted", null, null, producesType);

				builder = Response.status(Response.Status.BAD_REQUEST).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
			}
		}
		catch (Exception e) {
			// Write exception to server log first
			e.printStackTrace();

			// Handle generic exceptions
			String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.FATAL, OperationOutcome.IssueType.EXCEPTION, e.getMessage());

			builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
		}

		log.fine("[END] FHIRPathEvaluatorRESTService.evaluatePost()");

		return builder.build();
	}

	/**
	 * <p>
	 * Prints the contents of the received request.<br/>
	 * Useful for debugging purposes.
	 * </p>
	 *
	 * @param request
	 * @param headers
	 * @param validateInputStream
	 * @return payload
	 */
	private String debugRequest(HttpServletRequest request, HttpHeaders headers, InputStream evalutateInputStream) {

		StringBuilder debugString = new StringBuilder();
		String payload = null;

		if (request != null) {
			debugString.append("----- HTTP REQUEST -----\n");

			debugString.append("Remote host is '" + (request.getRemoteHost() == null ? "NOT FOUND" : request.getRemoteHost()) + "'\n");
		}

		if (headers != null) {
			debugString.append("----- HTTP HEADERS (REQUEST) -----\n");

			MultivaluedMap<String, String> requestHeaders = headers.getRequestHeaders();

			if (requestHeaders != null) {

				for (String key : requestHeaders.keySet()) {

					for (String keyValue : requestHeaders.get(key)) {
						debugString.append("header(" + key + ") is " + keyValue + "\n");
					}
				}
			}
		}

		debugString.append("----- REQUEST URL -----\n");
		StringBuilder sbRequestUrl = new StringBuilder(context.getAbsolutePath().getPath());
		MultivaluedMap<String, String> queryParams = context.getQueryParameters();
		if (queryParams != null && !queryParams.isEmpty()) {
			sbRequestUrl.append("?");
			boolean first = true;
			for (String key : queryParams.keySet()) {
				if (!first) {
					sbRequestUrl.append("&");
				}
				debugString.append("param(" + key + ") is " + queryParams.get(key).toString() + "\n");
				sbRequestUrl.append(key).append("=").append(queryParams.get(key).toString());
			}
		}
		debugString.append("Absolute Path: " + sbRequestUrl.toString() + "\n");
		debugString.append("Request URL: " + context.getRequestUri().toString() + "\n");

		debugString.append("----- PAYLOAD -----\n");
		if (evalutateInputStream != null) {
			try {
				StringWriter writer = new StringWriter();
				String encoding = "UTF-8";
				IOUtils.copy(evalutateInputStream, writer, encoding);
				payload = writer.toString();

				debugString.append(payload);
				debugString.append("\n");

			}
			catch (Exception e) {
				log.severe("Exception parsing payload! " + e.getMessage());
				e.printStackTrace();
			}
		}
		else {
			debugString.append(">> NO PAYLOAD OR SNIPPED <<\n");
		}

		// log level FINE used for debug
		log.fine(debugString.toString());

		return payload;
	}

}
