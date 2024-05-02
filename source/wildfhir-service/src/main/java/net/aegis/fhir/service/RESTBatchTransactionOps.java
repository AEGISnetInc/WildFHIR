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
package net.aegis.fhir.service;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.hl7.fhir.r4.formats.JsonParser;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.formats.IParser.OutputStyle;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Bundle.BundleType;

import net.aegis.fhir.model.Constants;
import net.aegis.fhir.model.ResourceContainer;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.StringUtils;

/**
 * @author richard.ettema
 *
 */
public class RESTBatchTransactionOps {

    private Logger log = Logger.getLogger("RESTBatchTransactionOps");

    @Inject
    BatchService batchService;
    @Inject
    CodeService codeService;
    @Inject
    TransactionService transactionService;

    /**
     * This returns a <code>Bundle</code> with the found history contents for the resource type.
     *
     * @param context
     * @param headers
     * @param id
     * @param resourceType
     * @return <code>Response</code>
     */
    public Response batchTransaction(UriInfo context, HttpHeaders headers, String payload) {

        log.fine("[START] RESTBatchTransactionOps.batchTransaction()");

        Response.ResponseBuilder builder = null;
        String contentType = null;
        String producesType = null;
        String outcome = null;
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

			if (contentType != null && !contentType.equals(MediaType.APPLICATION_OCTET_STREAM)) {
				// Get the produces type based on the request Accept
				producesType = ServicesUtil.INSTANCE.getProducesType(headers, context);

				// Validate input format check; instantiate the Resource
				Resource resource = null;

				if (contentType.indexOf("xml") >= 0) {
					// Convert XML contents to Resource
					XmlParser xmlP = new XmlParser();
					resource = xmlP.parse(payload.getBytes());
				}
				else if (contentType.indexOf("json") >= 0) {
					// Convert JSON contents to Resource
					JsonParser jsonP = new JsonParser();
					resource = jsonP.parse(payload.getBytes());
				}
				else {
					// contentType did not contain a valid media type or was null; attempt to determine based on starting character
					int firstValid = payload.indexOf("<"); // check for xml first
					if (firstValid > -1 && firstValid < 5) {
						if (firstValid > 0) {
							payload = payload.substring(firstValid);
						}
						// Convert XML contents to Resource
						XmlParser xmlP = new XmlParser();
						resource = xmlP.parse(payload.getBytes());
					}
					else {
						firstValid = payload.indexOf("{"); // check for json next
						if (firstValid > -1 && firstValid < 5) {
							if (firstValid > 0) {
								payload = payload.substring(firstValid);
							}
							// Convert JSON contents to Resource
							JsonParser jsonP = new JsonParser();
							resource = jsonP.parse(payload.getBytes());
						}
					}
				}

				// Verify resource contents are Bundle of type 'batch' or 'transaction'
				if (resource != null && resource.getResourceType().equals(org.hl7.fhir.r4.model.ResourceType.Bundle)) {

		        	List<String> authMapPatient = null;

		        	String locationPath = context.getRequestUri().toString();

					Bundle bundleToProcess = (Bundle) resource;

					// If 'batch', call batch processing
					if (bundleToProcess.hasType() && bundleToProcess.getType().equals(BundleType.BATCH)) {

						ResourceContainer resourceContainer = batchService.batch(context, headers, contentType, producesType, bundleToProcess, locationPath, authMapPatient);

						builder = responseBundle(producesType, resourceContainer, locationPath, responseFhirVersion);
					}
					// Else if 'transaction', call transaction processing
					else if (bundleToProcess.hasType() && bundleToProcess.getType().equals(BundleType.TRANSACTION)) {

						ResourceContainer resourceContainer = transactionService.transaction(context, headers, contentType, producesType, bundleToProcess, locationPath, authMapPatient);

						builder = responseBundle(producesType, resourceContainer, locationPath, responseFhirVersion);
					}
					// Else report error 'invalid Bundle.type'
					else {
						outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.INVALID, "Bundle resource type is undefined or not set to 'batch' or 'transaction'.", null, null, producesType);

						builder = Response.status(Response.Status.BAD_REQUEST).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
					}
				}
				else {
					// Resource is empty or not a Bundle, report error
					outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.INVALID, "Payload is empty or not a valid Bundle resource instance.", null, null, producesType);

					builder = Response.status(Response.Status.BAD_REQUEST).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
				}
			}
			else {
				// Request Content-Type was empty or set to MediaType.APPLICATION_OCTET_STREAM, report error "415 (Unsupported Media Type)"
				outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.INVALID, "The required request Content-Type mime type is not defined or not supported.", null, "HTTP Header Content-Type", producesType);

				builder = Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
			}
		}
		catch (Exception e) {
			// Handle generic exceptions
			outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.FATAL, OperationOutcome.IssueType.EXCEPTION, e.getMessage(), null, null, producesType);

			builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);

			e.printStackTrace();
		}

		return builder.build();
    }

    /**
    *
    * @param resourcePath
    * @param producesType
    * @param resourceContainer
    * @param context
    * @return <code>Response.ResponseBuilder</code>
    * @throws URISyntaxException
    * @throws Exception
    */
   private Response.ResponseBuilder responseBundle(String producesType, ResourceContainer resourceContainer, String locationPath, String responseFhirVersion)
           throws URISyntaxException, Exception {

       log.fine("[START] RESTBatchTransactionOps.responseBundle()");

   	Response.ResponseBuilder builder;
       ByteArrayOutputStream oResourceBundle;

       if (resourceContainer != null) {
           builder = Response.status(resourceContainer.getResponseStatus()).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);

           if (resourceContainer.getResponseStatus().equals(Response.Status.OK)) {
               // Define URI location
               URI resourceLocation = new URI(locationPath);
               builder = builder.contentLocation(resourceLocation);

               if (resourceContainer.getBundle() != null) {

                   String sResourceBundle = "";

                   if (producesType.indexOf("xml") >= 0) {
                       // Convert Bundle to XML
                       oResourceBundle = new ByteArrayOutputStream();
       				XmlParser xmlParser = new XmlParser();
       				xmlParser.setOutputStyle(OutputStyle.PRETTY);
       				xmlParser.compose(oResourceBundle, resourceContainer.getBundle(), true);
                       sResourceBundle = oResourceBundle.toString();
                   } else {
                       // Convert Bundle to JSON
                       oResourceBundle = new ByteArrayOutputStream();
                       JsonParser jsonParser = new JsonParser();
                       jsonParser.setOutputStyle(OutputStyle.PRETTY);
                       jsonParser.compose(oResourceBundle, resourceContainer.getBundle());
                       sResourceBundle = oResourceBundle.toString();
                   }

                   builder = builder.entity(sResourceBundle);

               } else {
               	// Response status is not OK;; build OperationOutcome response resource
               	String message = "No response data found. Bundle is empty.";
               	if (!StringUtils.isNullOrEmpty(resourceContainer.getMessage())) {
               		message = resourceContainer.getMessage();
               	}

               	// Default outcome severity to ERROR and Type to TRANSIENT
               	OperationOutcome.IssueSeverity outcomeIssueSeverity = OperationOutcome.IssueSeverity.ERROR;
               	OperationOutcome.IssueType outcomeIssueType = OperationOutcome.IssueType.TRANSIENT;

               	// If returned response status is NOT_IMPLEMENTED, set severity to WARNING and type to NOTSUPPORTED
               	if (resourceContainer.getResponseStatus() != null && resourceContainer.getResponseStatus().equals(Response.Status.NOT_IMPLEMENTED)) {
               		outcomeIssueSeverity = OperationOutcome.IssueSeverity.WARNING;
               		outcomeIssueType = OperationOutcome.IssueType.NOTSUPPORTED;
               	}

               	String outcome = ServicesUtil.INSTANCE.getOperationOutcome(outcomeIssueSeverity, outcomeIssueType, message, null, null, producesType);

                   builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
               }
           } else {
               // Something went wrong
           	String message = "Failure processing batch/transaction bundle.";
           	if (!StringUtils.isNullOrEmpty(resourceContainer.getMessage())) {
           		message = resourceContainer.getMessage();
           	}
               String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.TRANSIENT, message, null, null, producesType);

               builder = Response.status(resourceContainer.getResponseStatus()).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
           }
       } else {
           // Something went wrong
           String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.TRANSIENT, "No response container returned.", null, null, producesType);

           builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
       }

       return builder;
   }

}
