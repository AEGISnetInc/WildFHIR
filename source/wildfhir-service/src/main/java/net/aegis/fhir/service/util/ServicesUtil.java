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
package net.aegis.fhir.service.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import net.aegis.fhir.model.Constants;
import net.aegis.fhir.model.ResourceType;
import net.aegis.fhir.service.narrative.FHIRNarrativeGeneratorClient;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.NameValuePair;
import org.hl7.fhir.r4.formats.IParser.OutputStyle;
import org.hl7.fhir.r4.formats.JsonParser;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.StringType;

/**
 * ServicesUtil - Common methods used across the services layer
 *
 * @author richard.ettema
 *
 */
public enum ServicesUtil {

	INSTANCE;

	private Logger log = Logger.getLogger("ServicesUtil");

	private String responseFhirVersion = null;

	private ServicesUtil() {
	}

	/**
	 *
	 * @param headers
	 * @param headerName
	 * @return http header value; null if not found or undefined
	 * @throws Exception
	 */
	public String getHttpHeader(HttpHeaders headers, String headerName) throws Exception {

		log.fine("[START] ServicesUtil.getHttpHeader()");

		String headerValue = null;

		try {
			if (headers != null && headerName != null && headers.getRequestHeader(headerName) != null && headers.getRequestHeader(headerName).size() > 0) {

				headerValue = headers.getRequestHeader(headerName).get(0);
				log.fine(headerName + " = " + headerValue);
			}

		}
		catch (Exception e) {
			// Handle generic exceptions
			log.severe(e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return headerValue;
	}

	/**
	 *
	 * @param headers
	 * @param headerName
	 * @return list of http header values; null if not found or undefined
	 * @throws Exception
	 */
	public List<String> getHttpHeaderList(HttpHeaders headers, String headerName) throws Exception {

		log.fine("[START] ServicesUtil.getHttpHeader()");

		List<String> headerValues = null;

		try {
			if (headers != null && headerName != null && headers.getRequestHeader(headerName) != null && headers.getRequestHeader(headerName).size() > 0) {

				headerValues = headers.getRequestHeader(headerName);
				for (String value : headerValues) {
					log.fine(headerName + " = " + value);
				}
			}
		}
		catch (Exception e) {
			// Handle generic exceptions
			log.severe(e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return headerValues;
	}

	/**
	 *
	 * @param headers
	 * @return list of all request http header values; null if not found or undefined
	 * @throws Exception
	 */
	public List<String> getRequestHttpHeaders(HttpHeaders headers) throws Exception {

		log.fine("[START] ServicesUtil.getRequestHttpHeaders()");

		List<String> headerValues = null;
		MultivaluedMap<String, String> allheaders = null;

		try {
			if (headers != null) {

				allheaders = headers.getRequestHeaders();

				if (allheaders != null && allheaders.size() > 0) {

					headerValues = new ArrayList<String>();
					StringBuffer headerEntry = null;
					Set<Entry<String, List<String>>> paramSet = allheaders.entrySet();

					for (Entry<String, List<String>> entry : paramSet) {

						headerEntry = new StringBuffer(entry.getKey()).append("=");
						List<String> values = entry.getValue();
						boolean firstEntry = true;
						for (String value : values) {
							if (firstEntry == false) {
								headerEntry.append(",");
								firstEntry = false;
							}
							headerEntry.append(value);
						}

						headerValues.add(headerEntry.toString());
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

		return headerValues;
	}

	/**
	 *
	 * @param headers
	 * @return list of all response http header values; null if not found or undefined
	 * @throws Exception
	 */
	public List<String> getResponseHttpHeaders(MultivaluedMap<String, String> headers) throws Exception {

		log.fine("[START] ServicesUtil.getResponseHttpHeaders()");

		List<String> headerValues = null;

		try {
			if (headers != null && headers.size() > 0) {

				headerValues = new ArrayList<String>();
				StringBuffer headerEntry = null;
				Set<Entry<String, List<String>>> paramSet = headers.entrySet();

				for (Entry<String, List<String>> entry : paramSet) {

					headerEntry = new StringBuffer(entry.getKey()).append("=");
					List<String> values = entry.getValue();
					boolean firstEntry = true;
					for (String value : values) {
						if (firstEntry == false) {
							headerEntry.append(",");
							firstEntry = false;
						}
						headerEntry.append(value);
					}

					headerValues.add(headerEntry.toString());
				}
			}
		}
		catch (Exception e) {
			// Handle generic exceptions
			log.severe(e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return headerValues;
	}

	/**
	 *
	 * @param headers
	 * @param context
	 * @return produces type based on request Accept header value
	 * @throws Exception
	 */
	public String getProducesType(HttpHeaders headers, UriInfo context) throws Exception {

		log.fine("[START] ServicesUtil.getProducesType(headers, context)");

		return getProducesType(headers, context, null);
	}

	/**
	 *
	 * @param headers
	 * @param context
	 * @return produces type based on request Accept header value
	 * @throws Exception
	 */
	public String getProducesType(HttpHeaders headers, UriInfo context, MultivaluedMap<String,String> formMap) throws Exception {

		log.fine("[START] ServicesUtil.getProducesType(headers, context, formMap)");

		// Default produces type to XML
		String producesType = null;

		try {
			if (context != null || formMap != null) {
				log.fine("Checking for _format parameter...");

				// Get the query parameters that represent the search criteria
				Set<Entry<String, List<String>>> paramSet = new HashSet<Entry<String, List<String>>>();

				if (context != null) {
					MultivaluedMap<String, String> queryParams = context.getQueryParameters();

					if (queryParams != null && queryParams.size() > 0) {
						Set<Entry<String, List<String>>> parameterSet = queryParams.entrySet();

						paramSet.addAll(parameterSet);
					}
				}

				// Include parameters from formMap if present
				if (formMap != null) {
					Set<Entry<String, List<String>>> formSet = formMap.entrySet();

					paramSet.addAll(formSet);
				}

				// Iterate thru the parameter map and locate _format
				for (Entry<String, List<String>> entry : paramSet) {

					String key = entry.getKey();
					String value = entry.getValue().get(0);

					if (key.equals("_format")) {
						producesType = value;
						log.fine("Produces (_format): " + producesType);
						break;
					}
				}

				// Check producesType for valid 'xml' or 'json' content
				if (producesType != null && !(producesType.toLowerCase().contains("xml") || producesType.toLowerCase().contains("json"))) {
					// Invalid format, force producesType to null
					producesType = null;
				}
			}

			if (producesType == null) {
				// _format not found; Default producesType to XML
				producesType = "application/fhir+xml";

				if (headers != null && headers.getRequestHeader("Accept") != null && headers.getRequestHeader("Accept").size() > 0) {
					String contentType = headers.getRequestHeader("Accept").get(0);
					log.fine("Accept: " + contentType);

					// Check for current STU3 valid mime types and previous version mime types
					if (contentType.contains("application/fhir+json")) {
						producesType = "application/fhir+json";
					}
					else if (contentType.contains("application/fhir+xml")) {
						producesType = "application/fhir+xml";
					}
					else if (contentType.contains("application/json+fhir")) {
						producesType = "application/json+fhir";
					}
					else if (contentType.contains("application/xml+fhir")) {
						producesType = "application/xml+fhir";
					}
					else if (contentType.contains("application/json")) {
						producesType = "application/json";
					}
					else if (contentType.contains("application/xml")) {
						producesType = "application/xml";
					}
					else if (contentType.contains("text/json")) {
						producesType = "text/json";
					}
					else if (contentType.contains("text/xml")) {
						producesType = "text/xml";
					}
					else {
						// If json or xml in contentType, return current STU3 valid mime type
						if (contentType.indexOf("json") >= 0) {
							producesType = "application/fhir+json";
						}
						else if (contentType.indexOf("xml") >= 0) {
							producesType = "application/fhir+xml";
						}
					}
				}
				log.fine("Produces (Accept): " + producesType);
			}
			else {
				if (producesType.contains("xml")) {
					producesType = "application/fhir+xml";
				}
				else {
					producesType = "application/fhir+json";
				}
				log.fine("Produces (Evaluated): " + producesType);
			}
		}
		catch (Exception e) {
			// Handle generic exceptions
			log.severe(e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return producesType;
	}

	/**
	 *
	 * @param paramName
	 * @param context
	 * @return string value of uri parameter; null if not present
	 * @throws Exception
	 */
	public String getUriParameter(String paramName, UriInfo context) throws Exception {

		log.fine("[START] ServicesUtil.getUriParameter(" + paramName + ", context)");

		// Default parameter value to null
		String paramValue = null;

		try {
			if (paramName != null && context != null) {
				log.fine("Checking for " + paramName + " parameter...");

				// Get the query parameters that represent the search criteria
				MultivaluedMap<String, String> queryParams = context.getQueryParameters();

				if (queryParams != null && queryParams.size() > 0) {
					Set<Entry<String, List<String>>> paramSet = queryParams.entrySet();

					for (Entry<String, List<String>> entry : paramSet) {

						String key = entry.getKey();
						String value = entry.getValue().get(0);

						if (key.equals(paramName)) {
							paramValue = value;
							log.fine(paramName + " = " + paramValue);
							break;
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

		return paramValue;
	}

	/**
	 *
	 * @param paramName
	 * @param queryParams
	 * @return string value of uri parameter; null if not present
	 * @throws Exception
	 */
	public String getUriParameter(String paramName, MultivaluedMap<String, String> queryParams) throws Exception {

		log.fine("[START] ServicesUtil.getUriParameter(" + paramName + ", queryParams)");

		// Default parameter value to null
		String paramValue = null;

		try {
			if (paramName != null && queryParams != null) {
				log.fine("Checking for " + paramName + " parameter...");

				if (queryParams != null && queryParams.size() > 0) {
					Set<Entry<String, List<String>>> paramSet = queryParams.entrySet();

					for (Entry<String, List<String>> entry : paramSet) {

						String key = entry.getKey();
						String value = entry.getValue().get(0);

						if (key.equals(paramName)) {
							paramValue = value;
							log.fine(paramName + " = " + paramValue);
							break;
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

		return paramValue;
	}

	/**
	 *
	 * @param paramName
	 * @param queryParams
	 * @return string value of uri parameter; null if not present
	 * @throws Exception
	 */
	public List<String> getUriParameters(String paramName, MultivaluedMap<String, String> queryParams) throws Exception {

		log.fine("[START] ServicesUtil.getUriParameters(" + paramName + ", queryParams)");

		// Default parameter values to empty List
		List<String> paramValues = new ArrayList<String>();

		try {
			if (paramName != null && queryParams != null) {
				log.fine("Checking for " + paramName + " parameters...");

				if (queryParams != null && queryParams.size() > 0) {
					Set<Entry<String, List<String>>> paramSet = queryParams.entrySet();

					for (Entry<String, List<String>> entry : paramSet) {

						String key = entry.getKey();

						if (key.equals(paramName)) {

							for (String entryValue : entry.getValue()) {

								/*
								 * Check for comma separated list of values
								 */
								String[] valueList = entryValue.split("\\,");

								for (String listValue : valueList) {
									paramValues.add(listValue);
									log.fine(paramName + " = " + listValue);
								}

							}
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

		return paramValues;
	}

	/**
	 *
	 * @param url
	 * @return
	 */
	public String extractRelativeReferenceFromURL(String url) {
		log.fine("extractRelativeReferenceFromURL(" + url + ")");

		String relativeReference = "";

		try {
			int startIndex = -1;

			int endIndex = url.indexOf("/_history/", 0);
			//System.out.println("  >> Check for /_history/ - endIndex = " + endIndex);
			if (endIndex == -1) {
				endIndex = url.indexOf("?", 0);
				//System.out.println("  >> Check for ? - endIndex = " + endIndex);
			}
			if (endIndex == -1) {
				endIndex = url.length();
				//System.out.println("  >> Default endIndex = " + endIndex);
			}
			log.fine("extractRelativeReferenceFromURL - endIndex is " + endIndex);

			// /111/_history/2
			// 0123456789012345
			int slashCount = 0;
			for (int i = endIndex - 1; i > -1; i--) {
				log.fine("charAt(" + i + ") is " + url.charAt(i));
				// if (!isDigit(url.charAt(i))) {
				if (url.charAt(i) == '/') {
					slashCount++;
					if (slashCount >= 2) {
						startIndex = i + 1;
						break;
					}
				}
			}
			log.fine("extractRelativeReferenceFromURL - startIndex is " + startIndex);

			//resourceId = "-1";
			if (startIndex == -1) {
				startIndex = 0;
			}
			relativeReference = url.substring(startIndex, endIndex);
		}
		catch (Exception e) {
			log.severe("Exception parsing resourceId in URL: " + e.getMessage());
			// Return blank on exception
			relativeReference = "";
		}

		return relativeReference;
	}

	/**
	 *
	 * @param url
	 * @return
	 */
	public String extractResourceIdFromURL(String url) {
		log.fine("extractResourceIdFromURL(" + url + ")");

		String resourceId = "";

		try {
			int startIndex = -1;

			int endIndex = url.indexOf("/_history/", 0);
			//System.out.println("  >> Check for /_history/ - endIndex = " + endIndex);
			if (endIndex == -1) {
				endIndex = url.indexOf("?", 0);
				//System.out.println("  >> Check for ? - endIndex = " + endIndex);
			}
			if (endIndex == -1) {
				endIndex = url.length();
				//System.out.println("  >> Default endIndex = " + endIndex);
			}
			log.fine("extractResourceIdFromURL - endIndex is " + endIndex);

			// /111/_history/2
			// 0123456789012345
			for (int i = endIndex - 1; i > -1; i--) {
				log.fine("charAt(" + i + ") is " + url.charAt(i));
				// if (!isDigit(url.charAt(i))) {
				if (url.charAt(i) == '/') {
					startIndex = i + 1;
					break;
				}
			}
			log.fine("extractResourceIdFromURL - startIndex is " + startIndex);

			//resourceId = "-1";
			if (startIndex == -1) {
				startIndex = 0;
			}
			resourceId = url.substring(startIndex, endIndex);

			if (ResourceType.isValidResourceType(resourceId)) {
				resourceId = "";
			}
		}
		catch (Exception e) {
			log.severe("Exception parsing resourceId in URL: " + e.getMessage());
			// Return blank on exception
			resourceId = "";
		}

		return resourceId;
	}

	/**
	 * Expect '_history' in url; if not found, return empty string
	 *
	 * @param url
	 * @return
	 */
	public String extractVersionIdFromURL(String url) {
		log.fine("extractVersionIdFromURL(" + url + ")");

		String versionId = "";

		try {
			int endIndex = url.indexOf("?", 0);
			if (endIndex == -1) {
				endIndex = url.length();
			}

			int startIndex = url.indexOf("/_history/", 0);
			if (startIndex > -1) {
				startIndex += 10;
				versionId = url.substring(startIndex, endIndex);
				log.fine("extractVersionIdFromURL - startIndex is " + startIndex + "; endIndex is " + endIndex + "; versionId is " + versionId);
			}
		}
		catch (Exception e) {
			log.severe("Exception parsing versionId in URL: " + e.getMessage());
			// Return blank on exception
			versionId = "";
		}

		return versionId;
	}

	/**
	 * Extract the base URL path up to and excluding the '?' character.
	 *
	 * @param url
	 * @return
	 */
	public String extractBaseURL(String url) {
		log.fine("extractBaseURL(" + url + ")");

		String baseUrl = "";

		try {
			int endIndex = url.indexOf("?", 0);

			if (endIndex == -1) {
				endIndex = url.length();
			}
			log.fine("extractBaseURL - endIndex is " + endIndex);

			baseUrl = url.substring(0, endIndex);
		}
		catch (Exception e) {
			log.severe("Exception parsing baseUrl in URL: " + e.getMessage());
			// Return blank on exception
			baseUrl = "";
		}

		return baseUrl;
	}

	/**
	 * Extract the base URL path up to and excluding the excludeString value.
	 *
	 * @param url
	 * @param excludeString
	 * @return
	 */
	public String extractBaseURL(String url, String excludeString) {
		log.fine("extractBaseURL(" + url + ", " + excludeString + ")");

		String baseUrl = "";

		try {
			int endIndex = url.indexOf(excludeString, 0);

			if (endIndex == -1) {
				endIndex = url.length();
			}
			log.fine("extractBaseURL - endIndex is " + endIndex);

			baseUrl = url.substring(0, endIndex);
		}
		catch (Exception e) {
			log.severe("Exception parsing baseUrl in URL: " + e.getMessage());
			// Return blank on exception
			baseUrl = "";
		}

		return baseUrl;
	}

	/**
	 * Extract the URL parameters after the '?' character.
	 *
	 * @param url
	 * @return
	 */
	public String extractURLParams(String url) {
		log.fine("extractURLParams(" + url + ")");

		String urlParams = "";

		try {
			int startIndex = url.indexOf("?", 0);

			if (startIndex > -1) {
				startIndex++;

				log.fine("extractURLParams - startIndex is " + startIndex);

				urlParams = StringEscapeUtils.unescapeHtml4(url.substring(startIndex, url.length()));
			}
		}
		catch (Exception e) {
			log.severe("Exception parsing urlParams in URL: " + e.getMessage());
			// Return blank on exception
			urlParams = "";
		}

		return urlParams;
	}

	/**
	 * Extract the URL parameters after the '?' character.
	 *
	 * @param url
	 * @return
	 */
	public String extractDocumentCompositionPatientId(Bundle documentBundle) {
		log.fine("extractDocumentCompositionPatientId()");

		String patientId = "";

		try {
			if (documentBundle != null) {
				for (BundleEntryComponent bundleEntry : documentBundle.getEntry()) {
					if (bundleEntry.hasResource() && bundleEntry.getResource().fhirType().equals("Composition")) {
						Composition composition = (Composition)bundleEntry.getResource();
						if (composition.hasSubject() && composition.getSubject().hasReference()) {
							patientId = extractResourceIdFromURL(composition.getSubject().getReference());
						}
						break;
					}
					else if (bundleEntry.hasResource() && bundleEntry.getResource().fhirType().equals("Bundle")) {
						Bundle entryBundle = (Bundle)bundleEntry.getResource();
						patientId = extractDocumentCompositionPatientId(entryBundle);
						if (patientId != null && !patientId.isEmpty()) {
							break;
						}
					}
				}
			}
		}
		catch (Exception e) {
			log.severe("Exception parsing document bundle composition patient id: " + e.getMessage());
			// Return blank on exception
			patientId = "";
		}

		return patientId;
	}

	public boolean areSearchParametersValid(String resourceType, MultivaluedMap<String, String> queryParams, List<String> errorMessage) {
		log.fine("areSearchParametersValid(" + resourceType + ")");

		boolean isValid = true;
		if (errorMessage == null) {
			errorMessage = new ArrayList<String>();
		}
		StringBuffer sb = new StringBuffer("");

		if (queryParams != null && queryParams.size() > 0) {

			// Iterate thru the parameter map and check criteria, return false on first invalid parameter
			Set<Entry<String, List<String>>> paramSet = queryParams.entrySet();

			for (Entry<String, List<String>> entry : paramSet) {

				if (!ResourceType.isSupportedResourceCriteriaType(resourceType, entry.getKey())) {
					isValid = false;
					if (sb.length() > 2) {
						sb.append(", ");
					}
					sb.append("Invalid search parameter ").append(entry.getKey());
				}
			}
		}

		errorMessage.add(sb.toString());
		return isValid;
	}

	/**
	 * Transform a List<NameValuePair> into a {@link MultivaluedMap} which is needed to represent url query parameters
	 * casting the parameter value to Object. It also validates that no empty parameters are being send.
	 *
	 * @param listNameValuePair
	 *            the list of query parameters
	 * @return the same list in a {@link MultivaluedMap} object.
	 */
	public MultivaluedMap<String, Object> listNameValuePairToMultivaluedMapObject(final List<NameValuePair> listNameValuePair) {
		final MultivaluedMap<String, Object> multivaluedMap = new MultivaluedHashMap<String, Object>();

		if (listNameValuePair != null && !listNameValuePair.isEmpty()) {
			for (final NameValuePair pair : listNameValuePair) {
				multivaluedMap.add(pair.getName(), pair.getValue());
			}
		}

		if (multivaluedMap.isEmpty()) {
			log.fine("listNameValuePairToMultivaluedMapObject - multivaluedMap resolved to empty");
		}
		else {
			log.fine("listNameValuePairToMultivaluedMapObject - multivaluedMap resolved " + multivaluedMap.size() + " entries");
		}

		return multivaluedMap;
	}

	/**
	 * Transform a List<NameValuePair> into a {@link MultivaluedMap} which is needed to represent url query parameters
	 * casting the parameter value to String. It also validates that no empty parameters are being send.
	 *
	 * @param listNameValuePair
	 *            the list of query parameters
	 * @return the same list in a {@link MultivaluedMap} object.
	 */
	public MultivaluedMap<String, String> listNameValuePairToMultivaluedMapString(final List<NameValuePair> listNameValuePair) {
		final MultivaluedMap<String, String> multivaluedMap = new MultivaluedHashMap<String, String>();

		if (listNameValuePair != null && !listNameValuePair.isEmpty()) {
			for (final NameValuePair pair : listNameValuePair) {
				multivaluedMap.add(pair.getName(), pair.getValue());
			}
		}

		if (multivaluedMap.isEmpty()) {
			log.fine("listNameValuePairToMultivaluedMapString - multivaluedMap resolved to empty");
		}
		else {
			log.fine("listNameValuePairToMultivaluedMapString - multivaluedMap resolved " + multivaluedMap.size() + " entries");
		}

		return multivaluedMap;
	}

	/**
	 * (Overloaded) Get the search parameter text value from an CodeableConcept data type.
	 *
	 * @param codeableConcept
	 * @return text value string if found, otherwise null
	 */
	public String getTextValue(CodeableConcept codeableConcept) {
		String textValue = null;
		StringBuilder sbTextValue = new StringBuilder("");

		if (codeableConcept.hasCoding() && codeableConcept.getCodingFirstRep().hasDisplay()) {
			sbTextValue.append(codeableConcept.getCodingFirstRep().getDisplay()).append(" ");
		}
		if (codeableConcept.hasText()) {
			sbTextValue.append(codeableConcept.getText());
		}
		if (sbTextValue.length() > 1) {
			textValue = sbTextValue.toString();
		}

		return textValue;
	}

	/**
	 * (Overloaded) Get the search parameter text value from an Coding data type.
	 *
	 * @param coding
	 * @return text value string if found, otherwise null
	 */
	public String getTextValue(Coding coding) {
		String textValue = null;

		if (coding.hasDisplay()) {
			textValue = coding.getDisplay();
		}

		return textValue;
	}

	/**
	 * (Overloaded) Get the search parameter text value from an Identifier data type.
	 *
	 * @param identifier
	 * @return text value string if found, otherwise null
	 */
	public String getTextValue(Identifier identifier) {
		String textValue = null;

		if (identifier.hasType() && identifier.getType().hasText()) {
			textValue = identifier.getType().getText();
		}

		return textValue;
	}

	/**
	 *
	 * @param severity
	 * @param type
	 * @param details
	 * @param location
	 * @return XML string representation of <code>OperationOutcome</code>
	 */
	public OperationOutcome getOperationOutcomeResource(OperationOutcome.IssueSeverity severity) {
		return getOperationOutcomeResource(severity, OperationOutcome.IssueType.UNKNOWN);
	}

	public OperationOutcome getOperationOutcomeResource(OperationOutcome.IssueSeverity severity, OperationOutcome.IssueType type) {
		return getOperationOutcomeResource(severity, type, null);
	}

	public OperationOutcome getOperationOutcomeResource(OperationOutcome.IssueSeverity severity, OperationOutcome.IssueType type, String details) {
		return getOperationOutcomeResource(severity, type, details, null);
	}

	public OperationOutcome getOperationOutcomeResource(OperationOutcome.IssueSeverity severity, OperationOutcome.IssueType type, String details, String diagnostics) {
		return getOperationOutcomeResource(severity, type, details, diagnostics, null);
	}

	public OperationOutcome getOperationOutcomeResource(OperationOutcome.IssueSeverity severity, OperationOutcome.IssueType type, String details, String diagnostics, String location) {

		log.fine("[START] ServicesUtil.getOperationOutcomeResource(issue)");

		OperationOutcome op = new OperationOutcome();

		op.setId(UUIDUtil.getGUID());

		OperationOutcome.OperationOutcomeIssueComponent issue = getOperationOutcomeIssueComponent(severity, type, details, diagnostics, location);

		if (issue != null) {
			op.getIssue().add(issue);
		}

		try {
			// Use RI NarrativeGenerator
			FHIRNarrativeGeneratorClient.instance().generate(op);
		}
		catch (Exception e) {
			// Handle generic exceptions
			log.severe(e.getMessage());
			e.printStackTrace();
			// Exception not thrown to allow operation to complete
		}

		log.fine("[END] getOperationOutcomeResource(issue)");

		return op;
	}

	/**
	 * @param issues
	 * @return
	 */
	public OperationOutcome getOperationOutcomeResource(List<OperationOutcome.OperationOutcomeIssueComponent> issues) {

		log.fine("[START] ServicesUtil.getOperationOutcomeResource(List<issue>)");

		OperationOutcome op = new OperationOutcome();

		op.setId(UUIDUtil.getGUID());

		for (OperationOutcome.OperationOutcomeIssueComponent issue : issues) {
			if (issue != null) {
				op.getIssue().add(issue);
			}
		}

		try {
			// Use RI NarrativeGenerator
			FHIRNarrativeGeneratorClient.instance().generate(op);
		}
		catch (Exception e) {
			// Handle generic exceptions
			log.severe(e.getMessage());
			e.printStackTrace();
			// Exception not thrown to allow operation to complete
		}

		log.fine("[END] getOperationOutcomeResource(List<issue>)");

		return op;
	}

	/**
	 *
	 * @param severity
	 * @param type
	 * @param details
	 * @param location
	 * @return XML string representation of <code>OperationOutcome</code>
	 */
	public String getOperationOutcome(OperationOutcome.IssueSeverity severity) {
		return getOperationOutcome(severity, OperationOutcome.IssueType.UNKNOWN);
	}

	public String getOperationOutcome(OperationOutcome.IssueSeverity severity, OperationOutcome.IssueType type) {
		return getOperationOutcome(severity, type, null);
	}

	public String getOperationOutcome(OperationOutcome.IssueSeverity severity, OperationOutcome.IssueType type, String details) {
		return getOperationOutcome(severity, type, details, null);
	}

	public String getOperationOutcome(OperationOutcome.IssueSeverity severity, OperationOutcome.IssueType type, String details, String diagnostics) {
		return getOperationOutcome(severity, type, details, diagnostics, null);
	}

	public String getOperationOutcome(OperationOutcome.IssueSeverity severity, OperationOutcome.IssueType type, String details, String diagnostics, String location) {
		return getOperationOutcome(severity, type, details, diagnostics, location, null);
	}

	public String getOperationOutcome(OperationOutcome.IssueSeverity severity, OperationOutcome.IssueType type, String details, String diagnostics, String location, String producesType) {

		log.fine("[START] ServicesUtil.getOperationOutcome()");

		String sOp = "";

		try {
			OperationOutcome op = new OperationOutcome();

			op.setId(UUIDUtil.getGUID());

			OperationOutcome.OperationOutcomeIssueComponent issue = getOperationOutcomeIssueComponent(severity, type, details, diagnostics, location);

			if (issue != null) {
				op.getIssue().add(issue);
			}

			// Use RI NarrativeGenerator
			FHIRNarrativeGeneratorClient.instance().generate(op);

			// Convert the OperationOutcome to XML or JSON string
			ByteArrayOutputStream oOp = new ByteArrayOutputStream();

			if (producesType == null || producesType.indexOf("xml") >= 0) {
				XmlParser xmlParser = new XmlParser();
				xmlParser.setOutputStyle(OutputStyle.PRETTY);
				xmlParser.compose(oOp, op, true);
				sOp = oOp.toString();
			}
			else {
				JsonParser jsonParser = new JsonParser();
				jsonParser.setOutputStyle(OutputStyle.PRETTY);
				jsonParser.compose(oOp, op);
				sOp = oOp.toString();
			}

		}
		catch (Exception e) {
			// Handle generic exceptions
			log.severe(e.getMessage());
			e.printStackTrace();
			// Exception not thrown to allow operation to complete
		}

		log.fine("getOperationOutcome() - generated OutcomeOperation: " + sOp);

		return sOp;
	}

	public String getOperationOutcome(OperationOutcome outcome, String producesType) {

		log.fine("[START] ServicesUtil.getOperationOutcome(outcome)");

		String sOp = "";

		try {
			// Convert the OperationOutcome to XML or JSON string
			ByteArrayOutputStream oOp = new ByteArrayOutputStream();

			if (producesType == null || producesType.indexOf("xml") >= 0) {
				XmlParser xmlParser = new XmlParser();
				xmlParser.setOutputStyle(OutputStyle.PRETTY);
				xmlParser.compose(oOp, outcome, true);
				sOp = oOp.toString();
			}
			else {
				JsonParser jsonParser = new JsonParser();
				jsonParser.setOutputStyle(OutputStyle.PRETTY);
				jsonParser.compose(oOp, outcome);
				sOp = oOp.toString();
			}

		}
		catch (Exception e) {
			// Handle generic exceptions
			log.severe(e.getMessage());
			e.printStackTrace();
			// Exception not thrown to allow operation to complete
		}

		log.fine("getOperationOutcome() - OperationOutcome string: " + sOp);

		return sOp;
	}

	public OperationOutcome.OperationOutcomeIssueComponent getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity severity, OperationOutcome.IssueType type, String details, String diagnostics, String location) {

		log.fine("[START] ServicesUtil.getOperationOutcomeIssueComponent()");

		OperationOutcome.OperationOutcomeIssueComponent issue = null;

		try {
			issue = new OperationOutcome.OperationOutcomeIssueComponent();

			issue.setSeverity(severity);
			if (type != null) {
				issue.setCode(type);
			}
			if (details != null) {
				CodeableConcept outcomeDetails = new CodeableConcept();
				outcomeDetails.setText(details);
				issue.setDetails(outcomeDetails);
			}
			if (diagnostics != null) {
				issue.setDiagnostics(diagnostics);
			}
			if (location != null) {
				issue.getLocation().add(new StringType(location));
			}

		}
		catch (Exception e) {
			// Handle generic exceptions
			log.severe(e.getMessage());
			e.printStackTrace();
			// Exception not thrown to allow operation to complete
		}

		return issue;
	}

	/**
	 * Return the FHIR resource type string value from a reference string; e.g. Patient/example
	 *
	 * @param referenceString
	 * @return String - FHIR resource type string value
	 */
	public String getResourceTypeFromReference(String referenceString) {

		log.fine("[START] ServicesUtil.getUriParameter()");

		// Default resourceTypeString value to null
		String resourceTypeString = null;

		try {
			if (referenceString != null && !referenceString.isEmpty()) {

				// TSP-2416 - Replace use of FHIR STU3 enum with local reversed ordered resource type list
				for (String resourceType : ResourceType.getResourceTypesOrdered()) {

					if (referenceString.contains(resourceType)) {
						resourceTypeString = resourceType;
						break;
					}
				}
			}
		}
		catch (Exception e) {
			// Handle generic exceptions
			log.severe(e.getMessage());
			e.printStackTrace();
			// Exception not thrown to allow operation to complete
		}

		return resourceTypeString;
	}

	/**
	 * Returns the elapsed time as a formatted string
	 *
	 * @param startMillis
	 * @return
	 */
	public String getElapsedTime(long startMillis) {
		long different = System.currentTimeMillis() - startMillis;

		long secondsInMilli = 1000;
		long minutesInMilli = secondsInMilli * 60;
		long hoursInMilli = minutesInMilli * 60;
		long daysInMilli = hoursInMilli * 24;

		long elapsedDays = different / daysInMilli;
		different = different % daysInMilli;

		long elapsedHours = different / hoursInMilli;
		different = different % hoursInMilli;

		long elapsedMinutes = different / minutesInMilli;
		different = different % minutesInMilli;

		long elapsedSeconds = different / secondsInMilli;

		long elapsedMillis = different % secondsInMilli;

		return (elapsedDays > 0 ? elapsedDays + " day" : "") + (elapsedDays > 1 ? "s" : "") +
				(elapsedHours > 0 ? elapsedHours + " hr" : "") + (elapsedHours > 1 ? "s" : "") +
				(elapsedMinutes > 0 ? " " + elapsedMinutes + " min" : "") + (elapsedMinutes > 1 ? "s" : "") +
				(elapsedSeconds > 0 ? " " + elapsedSeconds + " sec" : "") + (elapsedSeconds > 1 ? "s" : "") +
				(elapsedMillis > 0 ? " " + elapsedMillis + " millis " : "");
	}

	/**
	 * build response for fhir version mismatch
	 * @param headers
	 * @return
	 * @throws Exception
	 */
	public Response fhirVersioMismatchedResponse(HttpHeaders headers, String supportedFhirVersion, UriInfo context) throws Exception {

		log.fine("Begin ServicesUtil.fhirVersioNotnMismatchedResponse()");

		Response.ResponseBuilder builder = null;
		String producesType = getProducesType(headers, context);

		log.fine("fhirVersionMatched.producesType: " + producesType);

		String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTSUPPORTED,
				"Unsupported FHIR Version: fhirVersion on HTTP request headers does not match with the supported version (" + supportedFhirVersion + ") of the FHIR Server.", null, null, producesType);

		builder = Response.status(Response.Status.BAD_REQUEST).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + "; fhirVersion=" + supportedFhirVersion);

		return builder.build();
	}

	/**
	 * Validates the fhirVersion in Accept and Content-type request headers
	 * matches with the serving wildfhir version
	 *
	 * @param request
	 * @param headers
	 * @return
	 */
	public boolean fhirVersionMatched(HttpServletRequest request, HttpHeaders headers, String supportedFhirVersion) {
		log.fine("Begin ServicesUtil.fhirVersionMatched()");
		boolean versionMatched = true;
		try {
			if (headers != null) {
				MultivaluedMap<String, String> requestHeaders = headers.getRequestHeaders();
				// Retrieve fhirVersion from Accept request header
				if (requestHeaders != null) {
					String acceptHeaderFhirVersion = "";
					String contenTypeHeaderFhirVersion = "";
					String requestFhirVersion = "";
					for (String key : requestHeaders.keySet()) {
						for (String keyValue : requestHeaders.get(key)) {
							log.fine("header(" + key + ") is " + keyValue);

							// Get fhirVersion from Accept header
							if (key != null && key.equals("Accept")) {
								acceptHeaderFhirVersion = getFhirVersion(keyValue);
								if (acceptHeaderFhirVersion != null && !acceptHeaderFhirVersion.isEmpty()) {
									requestFhirVersion = acceptHeaderFhirVersion;
								}
								break;
							}
							// Get fhirVersion from Content-Type header
							if (key != null && key.equalsIgnoreCase("Content-Type")) {
								contenTypeHeaderFhirVersion = getFhirVersion(keyValue);
								if (contenTypeHeaderFhirVersion != null && !contenTypeHeaderFhirVersion.isEmpty()) {
									requestFhirVersion = contenTypeHeaderFhirVersion;
								}
								break;
							}
						}
					}

					log.fine("acceptHeaderFhirVersion:" + acceptHeaderFhirVersion);
					log.fine("contenTypeHeaderFhirVersion:" + contenTypeHeaderFhirVersion);
					log.fine("requestFhirVersion:" + requestFhirVersion);

					// Verify the fhir version on Accept and Content type header matched (if both present)
					if (acceptHeaderFhirVersion != null && !acceptHeaderFhirVersion.isEmpty() && contenTypeHeaderFhirVersion != null && !contenTypeHeaderFhirVersion.isEmpty()) {
						if (!acceptHeaderFhirVersion.equals(contenTypeHeaderFhirVersion)) {
							log.fine("versionMatched: false");
							return false;
						}
					}

					// Get Supported fhir version from wildfhir code table and compare with requested fhir version
					if (supportedFhirVersion != null && !supportedFhirVersion.isEmpty() && requestFhirVersion != null && !requestFhirVersion.isEmpty()) {
						versionMatched = false;
						log.fine("supportedFhirVersion:" + supportedFhirVersion);
						if (supportedFhirVersion.equals(requestFhirVersion)) {
							versionMatched = true;
						}
					}
				}
			}
		}
		catch (Exception e) {
			log.severe(e.getMessage());
			e.printStackTrace();
		}

		log.fine("fhirVersionMatched:" + versionMatched);
		return versionMatched;
	}


	/**
	 * Get fhirVersion from request header keyvalue pair
	 * @param keyValue
	 * @return
	 */
	public String getFhirVersion(String keyValue) {
		String requestFhirVersion = "";
		List<String> headerVals = Arrays.asList(keyValue.split(";"));

		if (headerVals != null && !headerVals.isEmpty()) {

			String fhirVersionKeyVal = headerVals
					.stream()
					.filter(fv -> fv.contains("fhirVersion"))
					.findFirst()
					.orElse("");

			if(fhirVersionKeyVal != null && !fhirVersionKeyVal.isEmpty()) {
				String[] requestFhirVersions = (fhirVersionKeyVal.split("="));
				if(requestFhirVersions != null && requestFhirVersions.length > 1)
				requestFhirVersion = (requestFhirVersions[1] != null)?requestFhirVersions[1].trim():"";
			}
		}

		log.fine("requestFhirVersion:" + requestFhirVersion);
		return requestFhirVersion;
	}

	public String getSoftwareVersion() throws IOException {
        InputStream inputStream = null;
        String softwareVersion = "";
        try {
            Properties properties = new Properties();
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            inputStream = loader.getResourceAsStream("application.properties");
            properties.load(inputStream);

            String versionNumber = (properties.getProperty("version.number") == null)?"":properties.getProperty("version.number");
            String buildNumber = (properties.getProperty("build.number") == null)?"":properties.getProperty("build.number");
            String buildTimestamp = (properties.getProperty("build.timestamp") == null)?"":properties.getProperty("build.timestamp");

            softwareVersion = versionNumber + " Build " + buildNumber + " [" + buildTimestamp + "]";
            log.fine("softwareVersion: " + softwareVersion);

        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return softwareVersion;
	}


	/*
	 * Public getters and setters
	 */

	public String getResponseFhirVersion() {
		return responseFhirVersion;
	}

	public void setResponseFhirVersion(String responseFhirVersion) {
		this.responseFhirVersion = responseFhirVersion;
	}

}
