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
package net.aegis.fhir.client;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.hl7.fhir.r4.formats.IParser.OutputStyle;
import org.hl7.fhir.r4.formats.JsonParser;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.Resource;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import net.aegis.fhir.model.Constants;
import net.aegis.fhir.service.CodeService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.WebClientHelper;

/**
 * @author richard.ettema
 * @author rizwan.tanoli
 *
 */
public class ResourceRESTClient implements Serializable {

	private static final long serialVersionUID = -9149169751426055826L;

	private Logger log = Logger.getLogger("ResourceRESTClient");

    private String fhirVersion = "; fhirVersion=4.0";

	/**
	 * Initialize codeService
	 */
	public ResourceRESTClient(CodeService codeService) {
		super();
		if (codeService != null) {
			try {
				fhirVersion = "; fhirVersion=" + codeService.getCodeValue("supportedVersions");
			} catch (Exception e) {
				fhirVersion = "";
				e.printStackTrace();
			}
		}
	}

	/**
	 *
	 * @param resourceId
	 * @param baseUrl
	 * @param resourceType
	 * @param contentType
	 * @param ifModifiedSince
	 * @param ifNonMatch
	 * @param _format
	 * @param _summary
	 * @return {@link Response}
	 * @throws Exception
	 */
	public Response read(String resourceId, String baseUrl, String resourceType, String contentType, String ifModifiedSince, String ifNoneMatch, String _format, String _summary, List<String> headers) throws Exception {

		log.fine("[START] ResourceRESTClient.read() - resourceId: " + resourceId + "; baseUrl: " + baseUrl + "; resourceType: " + resourceType + "; contentType: " + contentType + "; ifModifiedSince: " + ifModifiedSince + "; ifNonMatch: " + ifNoneMatch + "; _format: " + _format + "; _summary: " + _summary);

		Response resourceResponse = null;

		try {

			StringBuilder sbReadUrl = new StringBuilder(buildURL(baseUrl, resourceType));
			sbReadUrl.append("/").append(resourceId);
			if (!StringUtils.isEmpty(_format) || !StringUtils.isEmpty(_summary)) {
				int paramCount = 0;
				sbReadUrl.append("?");
				if (!StringUtils.isEmpty(_format)) {
					sbReadUrl.append("_format=").append(_format);
					paramCount++;
				}
				if (!StringUtils.isEmpty(_summary)) {
					if (paramCount > 0) {
						sbReadUrl.append("&");
					}
					sbReadUrl.append("_summary=").append(_summary);
					paramCount++;
				}
			}

			ResteasyClient client = WebClientHelper.createClientWihtoutHostVerification();
			ResteasyWebTarget webTarget = client.target(sbReadUrl.toString());
			Builder targetBuilder = webTarget.request();

			if (contentType != null && contentType.toLowerCase().indexOf("json") >= 0) {
				targetBuilder = targetBuilder.header(HttpHeaders.ACCEPT, "application/fhir+json" + Constants.CHARSET_UTF8_EXT + fhirVersion);
			}
			else {
				targetBuilder = targetBuilder.header(HttpHeaders.ACCEPT, "application/fhir+xml" + Constants.CHARSET_UTF8_EXT + fhirVersion);
			}

			// Conditional Read parameters
			if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
				targetBuilder = targetBuilder.header(HttpHeaders.IF_MODIFIED_SINCE, ifModifiedSince);
			}
			if (ifNoneMatch != null && !ifNoneMatch.isEmpty()) {
				targetBuilder = targetBuilder.header(HttpHeaders.IF_NONE_MATCH, ifNoneMatch);
			}

			// Add any additional headers
			targetBuilder = addHeaders(targetBuilder, headers);

			log.info("Resource read request uri: " + webTarget.getUri());

			resourceResponse = targetBuilder.get();

			if (resourceResponse.hasEntity()) {
				resourceResponse.bufferEntity();
			}

			debugResponse(resourceResponse);

		}
		catch (Exception e) {
			// Exception caught
			log.severe(e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return resourceResponse;
	}

	/**
	 *
	 * @param resourceId
	 * @param versionId
	 * @param baseUrl
	 * @param resourceType
	 * @param contentType
	 * @param _format
	 * @return {@link Response}
	 * @throws Exception
	 */
	public Response vread(String resourceId, Integer versionId, String baseUrl, String resourceType, String contentType, String _format, List<String> headers) throws Exception {

		log.fine("[START] ResourceRESTClient.vread() - resourceId: " + resourceId + "; baseUrl: " + baseUrl + "; resourceType: " + resourceType + "; contentType: " + contentType + "; _format: " + _format);

		Response resourceResponse = null;

		try {

			// Resource read - specific version
			StringBuilder sbVreadUrl = new StringBuilder(buildURL(baseUrl, resourceType));
			sbVreadUrl.append("/").append(resourceId).append("/_history/").append(versionId.toString());
			if (!StringUtils.isEmpty(_format)) {
				sbVreadUrl.append("?_format=").append(_format);
			}

			ResteasyClient client = WebClientHelper.createClientWihtoutHostVerification();
			ResteasyWebTarget webTarget = client.target(sbVreadUrl.toString());
			Builder targetBuilder = webTarget.request();

			if (contentType != null && contentType.toLowerCase().indexOf("json") >= 0) {
				targetBuilder = targetBuilder.header(HttpHeaders.ACCEPT, "application/fhir+json" + Constants.CHARSET_UTF8_EXT + fhirVersion);
			}
			else {
				targetBuilder = targetBuilder.header(HttpHeaders.ACCEPT, "application/fhir+xml" + Constants.CHARSET_UTF8_EXT + fhirVersion);
			}

			// Add any additional headers
			targetBuilder = addHeaders(targetBuilder, headers);

			log.info("Resource vread request uri: " + webTarget.getUri());

			resourceResponse = targetBuilder.get();

			// buffering the response allows for multiple invocations of readEntity(...) on it.
			if (resourceResponse.hasEntity()) {
				resourceResponse.bufferEntity();
			}

			debugResponse(resourceResponse);

		}
		catch (Exception e) {
			// Exception caught
			log.severe(e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return resourceResponse;
	}

	/**
	 *
	 * @param resourceId
	 * @param resource
	 * @param baseUrl
	 * @param resourceType
	 * @param contentType
	 * @param ifMatch
	 * @param prefer
	 * @param _format
	 * @return {@link Response}
	 * @throws Exception
	 */
	public Response update(String resourceId, Resource resource, String baseUrl, String resourceType, String contentType, String updateQuery, String ifMatch, String prefer, String _format, List<String> headers) throws Exception {

		log.fine("[START] ResourceRESTClient.update() - resourceId: " + resourceId + "; baseUrl: " + baseUrl + "; resourceType: " + resourceType + "; contentType: " + contentType + "; updateQuery: " + updateQuery + "; ifMatch: " + ifMatch + "; prefer: " + prefer + "; _format: " + _format);

		Response resourceResponse = null;

		ByteArrayOutputStream oResource = new ByteArrayOutputStream();
		String sResource = null;

		try {

			// Resource update
			StringBuilder sbUpdateUrl = new StringBuilder(buildURL(baseUrl, resourceType));
			sbUpdateUrl.append("/").append(resourceId);
			if (!StringUtils.isEmpty(_format)) {
				sbUpdateUrl.append("?_format=").append(_format);
			}

			ResteasyClient client = WebClientHelper.createClientWihtoutHostVerification();
			ResteasyWebTarget webTarget = client.target(sbUpdateUrl.toString());

			// Conditional Update parameters
			if (updateQuery != null && !updateQuery.isEmpty()) {
				// Convert updateQuery into queryParams map
				List<NameValuePair> params = URLEncodedUtils.parse(updateQuery, Charset.defaultCharset());
				MultivaluedMap<String, Object> queryParams = ServicesUtil.INSTANCE.listNameValuePairToMultivaluedMapObject(params);
				webTarget = webTarget.queryParams(queryParams);
			}

			Builder targetBuilder = webTarget.request();

			if (contentType != null && contentType.toLowerCase().indexOf("json") >= 0) {
				targetBuilder = targetBuilder.header(HttpHeaders.CONTENT_TYPE, "application/fhir+json" + Constants.CHARSET_UTF8_EXT + fhirVersion)
						.header(HttpHeaders.ACCEPT, "application/fhir+json" + Constants.CHARSET_UTF8_EXT + fhirVersion);
			}
			else {
				targetBuilder = targetBuilder.header(HttpHeaders.CONTENT_TYPE, "application/fhir+xml" + Constants.CHARSET_UTF8_EXT + fhirVersion)
						.header(HttpHeaders.ACCEPT, "application/fhir+xml" + Constants.CHARSET_UTF8_EXT + fhirVersion);
			}

			// Conditional Update parameters
			if (ifMatch != null && !ifMatch.isEmpty()) {
				targetBuilder = targetBuilder.header(HttpHeaders.IF_MATCH, ifMatch);
			}
			if (prefer != null && !prefer.isEmpty()) {
				targetBuilder = targetBuilder.header("Prefer", prefer);
			}

			// Add any additional headers
			targetBuilder = addHeaders(targetBuilder, headers);

			log.info("Resource update request uri: " + webTarget.getUri());

			if (contentType != null && contentType.toLowerCase().indexOf("json") >= 0) {

				JsonParser jsonParser = new JsonParser();
				jsonParser.setOutputStyle(OutputStyle.PRETTY);
				jsonParser.compose(oResource, resource);
				sResource = oResource.toString();

				resourceResponse = targetBuilder.put(Entity.entity(sResource, "application/fhir+json" + Constants.CHARSET_UTF8_EXT + fhirVersion));

			}
			else {

				XmlParser xmlParser = new XmlParser();
				xmlParser.setOutputStyle(OutputStyle.PRETTY);
				xmlParser.compose(oResource, resource, true);
				sResource = oResource.toString();

				resourceResponse = targetBuilder.put(Entity.entity(sResource, "application/fhir+xml" + Constants.CHARSET_UTF8_EXT + fhirVersion));
			}

			log.info("Resource object sent: " + sResource);

			if (resourceResponse.hasEntity()) {
				resourceResponse.bufferEntity();
			}

			debugResponse(resourceResponse);

		}
		catch (Exception e) {
			// Exception caught
			log.severe(e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return resourceResponse;
	}

	/**
	 *
	 * @param resourceId
	 * @param patchString
	 * @param baseUrl
	 * @param resourceType
	 * @param contentType
	 * @param ifMatch
	 * @param prefer
	 * @param _format
	 * @return {@link Response}
	 * @throws Exception
	 */
	public Response patch(String resourceId, String patchString, String baseUrl, String resourceType, String contentType, String ifMatch, String prefer, String _format, List<String> headers) throws Exception {

		log.fine("[START] ResourceRESTClient.patch() - resourceId: " + resourceId + "; baseUrl: " + baseUrl + "; resourceType: " + resourceType + "; contentType: " + contentType + "; ifMatch: " + ifMatch + "; prefer: " + prefer + "; _format: " + _format);

		Response resourceResponse = null;

		try {
			// Resource patch
			StringBuilder sbUpdateUrl = new StringBuilder(buildURL(baseUrl, resourceType));
			sbUpdateUrl.append("/").append(resourceId);
			if (!StringUtils.isEmpty(_format)) {
				sbUpdateUrl.append("?_format=").append(_format);
			}

			ResteasyClient client = WebClientHelper.createClientWihtoutHostVerification();
			ResteasyWebTarget webTarget = client.target(sbUpdateUrl.toString());
			Builder targetBuilder = webTarget.request();

			if (contentType.toLowerCase().indexOf("xml-patch") >= 0) {
				targetBuilder = targetBuilder.header(HttpHeaders.CONTENT_TYPE, "application/xml-patch+xml" + Constants.CHARSET_UTF8_EXT + fhirVersion)
						.header(HttpHeaders.ACCEPT, "application/fhir+xml" + Constants.CHARSET_UTF8_EXT + fhirVersion);
			}
			else if (contentType.toLowerCase().indexOf("json-patch") >= 0) {
				targetBuilder = targetBuilder.header(HttpHeaders.CONTENT_TYPE, "application/json-patch+json" + Constants.CHARSET_UTF8_EXT + fhirVersion)
						.header(HttpHeaders.ACCEPT, "application/fhir+xml" + Constants.CHARSET_UTF8_EXT + fhirVersion);
			}
			else if (contentType.toLowerCase().indexOf("xml") >= 0) {
				targetBuilder = targetBuilder.header(HttpHeaders.CONTENT_TYPE, "application/fhir+xml" + Constants.CHARSET_UTF8_EXT + fhirVersion)
						.header(HttpHeaders.ACCEPT, "application/fhir+xml" + Constants.CHARSET_UTF8_EXT + fhirVersion);
			}
			else {
				targetBuilder = targetBuilder.header(HttpHeaders.CONTENT_TYPE, "application/fhir+json" + Constants.CHARSET_UTF8_EXT + fhirVersion)
						.header(HttpHeaders.ACCEPT, "application/fhir+json" + Constants.CHARSET_UTF8_EXT + fhirVersion);
			}

			// Conditional Update parameters
			if (ifMatch != null && !ifMatch.isEmpty()) {
				targetBuilder = targetBuilder.header(HttpHeaders.IF_MATCH, ifMatch);
			}
			if (prefer != null && !prefer.isEmpty()) {
				targetBuilder = targetBuilder.header("Prefer", prefer);
			}

			// Add any additional headers
			targetBuilder = addHeaders(targetBuilder, headers);

			log.info("Resource patch request uri: " + webTarget.getUri());

			log.info("Patch object to be sent: " + patchString);

			if (contentType.toLowerCase().indexOf("xml-patch") >= 0) {
				resourceResponse = targetBuilder.method("PATCH", Entity.entity(patchString, "application/xml-patch+xml; charset=utf-8"));
			}
			else if (contentType.toLowerCase().indexOf("json-patch") >= 0) {
				resourceResponse = targetBuilder.method("PATCH", Entity.entity(patchString, "application/json-patch+json; charset=utf-8"));
			}
			else if (contentType.toLowerCase().indexOf("xml") >= 0) {
				resourceResponse = targetBuilder.method("PATCH", Entity.entity(patchString, "application/fhir+xml; charset=utf-8"));
			}
			else {
				resourceResponse = targetBuilder.method("PATCH", Entity.entity(patchString, "application/fhir+json; charset=utf-8"));
			}

			if (resourceResponse.hasEntity()) {
				resourceResponse.bufferEntity();
			}

			debugResponse(resourceResponse);

			client.close();
		}
		catch (Exception e) {
			// Exception caught
			log.severe(e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return resourceResponse;
	}

	/**
	 *
	 * @param resourceId
	 * @param baseUrl
	 * @param resourceType
	 * @param contentType
	 * @return {@link Response}
	 * @throws Exception
	 */
	public Response delete(String resourceId, String baseUrl, String resourceType, String contentType, List<String> headers) throws Exception {

		log.fine("[START] ResourceRESTClient.delete() - resourceId: " + resourceId + "; baseUrl: " + baseUrl + "; resourceType: " + resourceType + "; contentType: " + contentType);

		Response resourceResponse = null;

		try {
			// Response delete - latest version
			String sDelete = buildURL(baseUrl, resourceType) + "/" + resourceId;

			ResteasyClient client = WebClientHelper.createClientWihtoutHostVerification();
			ResteasyWebTarget webTarget = client.target(sDelete);
			Builder targetBuilder = webTarget.request();

			if (contentType != null && contentType.toLowerCase().indexOf("json") >= 0) {
				targetBuilder = targetBuilder.header(HttpHeaders.ACCEPT, "application/fhir+json" + Constants.CHARSET_UTF8_EXT + fhirVersion);
			}
			else {
				targetBuilder = targetBuilder.header(HttpHeaders.ACCEPT, "application/fhir+xml" + Constants.CHARSET_UTF8_EXT + fhirVersion);
			}

			// Add any additional headers
			targetBuilder = addHeaders(targetBuilder, headers);

			log.info("Resource delete request uri: " + webTarget.getUri());

			resourceResponse = targetBuilder.delete();

			if (resourceResponse.hasEntity() || !((resourceResponse.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) || (resourceResponse.getStatus() == Response.Status.GONE.getStatusCode()))) {
				resourceResponse.bufferEntity();
			}

			debugResponse(resourceResponse);

		}
		catch (Exception e) {
			// Exception caught
			log.severe(e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return resourceResponse;
	}

	/**
	 *
	 * @param resourceId
	 * @param baseUrl
	 * @param resourceType
	 * @param contentType
	 * @param _format
	 * @param _count
	 * @return {@link Response}
	 * @throws Exception
	 */
	public Response history(String resourceId, String baseUrl, String resourceType, String contentType, String _format, String _count, String _since, List<String> headers) throws Exception {

		log.fine("[START] ResourceRESTClient.history() - resourceId: " + resourceId + "; baseUrl: " + baseUrl + "; resourceType: " + resourceType + "; contentType: " + contentType + "; _format: " + _format + "; _count: " + _since + "; _since: " + _count);

		Response resourceResponse = null;

		try {

			// Response history
			StringBuilder sbHistoryUrl = new StringBuilder(buildURL(baseUrl, resourceType));

			if (resourceId != null) {
				sbHistoryUrl.append("/").append(resourceId);
			}
			sbHistoryUrl.append("/_history");

			if (!StringUtils.isEmpty(_format) || !StringUtils.isEmpty(_count) || !StringUtils.isEmpty(_since)) {
				sbHistoryUrl.append("?");
				if (!StringUtils.isEmpty(_format)) {
					sbHistoryUrl.append("_format=").append(_format);
					if (!StringUtils.isEmpty(_count) || !StringUtils.isEmpty(_since)) {
						sbHistoryUrl.append("&");
					}
				}
				if (!StringUtils.isEmpty(_count)) {
					sbHistoryUrl.append("_count=").append(_count);
					if (!StringUtils.isEmpty(_since)) {
						sbHistoryUrl.append("&");
					}
				}
				if (!StringUtils.isEmpty(_since)) {
					sbHistoryUrl.append("_since=").append(_since);
				}
			}

			ResteasyClient client = WebClientHelper.createClientWihtoutHostVerification();
			ResteasyWebTarget webTarget = client.target(sbHistoryUrl.toString());
			Builder targetBuilder = webTarget.request();

			if (contentType != null && contentType.toLowerCase().indexOf("json") >= 0) {
				targetBuilder = targetBuilder.header(HttpHeaders.ACCEPT, "application/fhir+json" + Constants.CHARSET_UTF8_EXT + fhirVersion);
			}
			else {
				targetBuilder = targetBuilder.header(HttpHeaders.ACCEPT, "application/fhir+xml" + Constants.CHARSET_UTF8_EXT + fhirVersion);
			}

			// Add any additional headers
			targetBuilder = addHeaders(targetBuilder, headers);

			log.info("Resource history request uri: " + webTarget.getUri());

			resourceResponse = targetBuilder.get();

			if (resourceResponse.hasEntity()) {
				resourceResponse.bufferEntity();
			}

			debugResponse(resourceResponse);

		}
		catch (Exception e) {
			// Exception caught
			log.severe(e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return resourceResponse;
	}

	/**
	 *
	 * @param historyPageUrl
	 * @param contentType
	 * @return {@link Response}
	 * @throws Exception
	 */
	public Response historyPage(String historyPageUrl, String contentType, List<String> headers) throws Exception {

		log.fine("[START] ResourceRESTClient.historyPage() - historyPageUrl: " + historyPageUrl);

		Response resourceResponse = null;

		try {

			// Response history page
			historyPageUrl = StringEscapeUtils.unescapeXml(historyPageUrl);

			ResteasyClient client = WebClientHelper.createClientWihtoutHostVerification();
			ResteasyWebTarget webTarget = client.target(historyPageUrl);
			Builder targetBuilder = webTarget.request();

			if (contentType != null && contentType.toLowerCase().indexOf("json") >= 0) {
				targetBuilder = targetBuilder.header(HttpHeaders.ACCEPT, "application/fhir+json" + Constants.CHARSET_UTF8_EXT + fhirVersion);
			}
			else {
				targetBuilder = targetBuilder.header(HttpHeaders.ACCEPT, "application/fhir+xml" + Constants.CHARSET_UTF8_EXT + fhirVersion);
			}

			// Add any additional headers
			targetBuilder = addHeaders(targetBuilder, headers);

			log.info("Resource history request uri: " + webTarget.getUri());

			resourceResponse = targetBuilder.get();

			if (resourceResponse.hasEntity()) {
				resourceResponse.bufferEntity();
			}

			debugResponse(resourceResponse);

		}
		catch (Exception e) {
			// Exception caught
			log.severe(e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return resourceResponse;
	}

	/**
	 *
	 * @param resource
	 * @param baseUrl
	 * @param resourceType
	 * @param contentType
	 * @param prefer
	 * @param _format
	 * @return {@link Response}
	 * @throws Exception
	 */
	public Response create(Resource resource, String baseUrl, String resourceType, String contentType, String ifNoneExist, String prefer, String _format, List<String> headers) throws Exception {

		log.fine("[START] ResourceRESTClient.create() - baseUrl: " + baseUrl + "; resourceType: " + resourceType + "; contentType: " + contentType + "; ifNoneExist: " + ifNoneExist + "; prefer: " + prefer + "; _format: " + _format);

		Response resourceResponse = null;

		ByteArrayOutputStream oResource = new ByteArrayOutputStream();
		String sResource = null;

		try {

			// Response create
			StringBuilder sbCreateUrl = new StringBuilder(buildURL(baseUrl, resourceType));
			if (!StringUtils.isEmpty(_format)) {
				sbCreateUrl.append("?_format=").append(_format);
			}

			ResteasyClient client = WebClientHelper.createClientWihtoutHostVerification();
			ResteasyWebTarget webTarget = client.target(sbCreateUrl.toString());
			Builder targetBuilder = webTarget.request();

			if (contentType != null && contentType.toLowerCase().indexOf("json") >= 0) {
				targetBuilder = targetBuilder.header(HttpHeaders.CONTENT_TYPE, "application/fhir+json" + Constants.CHARSET_UTF8_EXT + fhirVersion)
						.header(HttpHeaders.ACCEPT, "application/fhir+json" + Constants.CHARSET_UTF8_EXT + fhirVersion);
			}
			else {
				targetBuilder = targetBuilder.header(HttpHeaders.CONTENT_TYPE, "application/fhir+xml" + Constants.CHARSET_UTF8_EXT + fhirVersion)
						.header(HttpHeaders.ACCEPT, "application/fhir+xml" + Constants.CHARSET_UTF8_EXT + fhirVersion);
			}

			// Conditional Create parameters
			if (ifNoneExist != null && !ifNoneExist.isEmpty()) {
				targetBuilder = targetBuilder.header("If-None-Exist", ifNoneExist);
			}
			if (prefer != null && !prefer.isEmpty()) {
				targetBuilder = targetBuilder.header("Prefer", prefer);
			}

			// Add any additional headers
			targetBuilder = addHeaders(targetBuilder, headers);

			log.info("Resource update request uri: " + webTarget.getUri());

			if (contentType != null && contentType.toLowerCase().indexOf("json") >= 0) {

				JsonParser jsonParser = new JsonParser();
				jsonParser.setOutputStyle(OutputStyle.PRETTY);
				jsonParser.compose(oResource, resource);
				sResource = oResource.toString();

				resourceResponse = targetBuilder.post(Entity.entity(sResource, "application/fhir+json" + Constants.CHARSET_UTF8_EXT + fhirVersion));
			}
			else {

				XmlParser xmlParser = new XmlParser();
				xmlParser.setOutputStyle(OutputStyle.PRETTY);
				xmlParser.compose(oResource, resource, true);
				sResource = oResource.toString();

				resourceResponse = targetBuilder.post(Entity.entity(sResource, "application/fhir+xml" + Constants.CHARSET_UTF8_EXT + fhirVersion));
			}

			log.info("Resource object sent: " + sResource);

			if (resourceResponse.hasEntity()) {
				resourceResponse.bufferEntity();
			}

			debugResponse(resourceResponse);

		}
		catch (Exception e) {
			// Exception caught
			log.severe(e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return resourceResponse;
	}

	/**
	 *
	 * @param parameterMap
	 * @param baseUrl
	 * @param resourceType
	 * @param contentType
	 * @param _format
	 * @param _summary
	 * @return {@link Response}
	 * @throws Exception
	 */
	public Response searchGet(Map<String, String> parameterMap, String baseUrl, String resourceType, String contentType, String _format, String _summary, List<String> headers) throws Exception {

		log.fine("[START] ResourceRESTClient.searchGet() - resourceType: " + resourceType + "; contentType: " + contentType + "; _format: " + _format + "; _summary: " + _summary);

		Response resourceResponse = null;

		try {

			MultivaluedMap<String, Object> criteriaMap = new MultivaluedHashMap<String, Object>();
			String keyValue = null;
			String splitValue1 = null;

			// First add _format and _summary if defined
			if (!StringUtils.isEmpty(_format)) {
				criteriaMap.add("_format", _format);
			}
			if (!StringUtils.isEmpty(_summary)) {
				criteriaMap.add("_summary", _summary);
			}

			// Next add parameter map keys and values
			for (String key : parameterMap.keySet()) {
				keyValue = parameterMap.get(key);
				log.info("parameterMap.key = " + key + "; value = " + keyValue);
				if (keyValue.startsWith(":")) {
					log.info("Modifier found; Split on expected equals sign...");
					String[] splitValue = keyValue.split("=");
					if (splitValue.length > 1) {
						splitValue1 = StringEscapeUtils.escapeHtml4(splitValue[1]);
					}
					else {
						splitValue1 = "";
					}
					criteriaMap.add(key + splitValue[0], splitValue1);
				}
				else {
					log.info("No modifier found...");
					criteriaMap.add(key, keyValue);
				}
			}

			// Patient search
			String sSearch = buildURL(baseUrl, resourceType);

			ResteasyClient client = WebClientHelper.createClientWihtoutHostVerification();
			ResteasyWebTarget webTarget = client.target(sSearch);

			// set query parameters
			if (criteriaMap != null && !criteriaMap.isEmpty()) {
				webTarget = webTarget.queryParams(criteriaMap);
			}

			Builder targetBuilder = webTarget.request();

			if (contentType != null && contentType.toLowerCase().indexOf("json") >= 0) {
				targetBuilder = targetBuilder.header(HttpHeaders.ACCEPT, "application/fhir+json" + Constants.CHARSET_UTF8_EXT + fhirVersion);
			}
			else {
				targetBuilder = targetBuilder.header(HttpHeaders.ACCEPT, "application/fhir+xml" + Constants.CHARSET_UTF8_EXT + fhirVersion);
			}

			// Add any additional headers
			targetBuilder = addHeaders(targetBuilder, headers);

			log.info("Resource Search request uri: " + webTarget.getUri());

			resourceResponse = targetBuilder.get();

			if (resourceResponse.hasEntity()) {
				resourceResponse.bufferEntity();
			}

			debugResponse(resourceResponse);

		}
		catch (Exception e) {
			// Exception caught
			log.severe(e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return resourceResponse;
	}

	/**
	 *
	 * @param parameterMap
	 * @param baseUrl
	 * @param resourceType
	 * @param contentType
	 * @param _format
	 * @param _summary
	 * @return {@link Response}
	 * @throws Exception
	 */
	public Response searchPost(Map<String, String> parameterMap, String baseUrl, String resourceType, String contentType, String _format, String _summary, List<String> headers) throws Exception {

		log.fine("[START] ResourceRESTClient.searchPost() - resourceType: " + resourceType + "; contentType: " + contentType + "; _format: " + _format + "; _summary: " + _summary);

		Response resourceResponse = null;

		try {

			MultivaluedMap<String, Object> criteriaMap = new MultivaluedHashMap<String, Object>();
			String keyValue = null;
			String splitValue1 = null;

			// First add _format and _summary if defined
			if (!StringUtils.isEmpty(_format)) {
				criteriaMap.add("_format", _format);
			}
			if (!StringUtils.isEmpty(_summary)) {
				criteriaMap.add("_summary", _summary);
			}

			// Next add parameter map keys and values
			for (String key : parameterMap.keySet()) {
				keyValue = parameterMap.get(key);
				log.info("parameterMap.key = " + key + "; value = " + keyValue);
				if (keyValue.startsWith(":")) {
					log.info("Modifier found; Split on expected equals sign...");
					String[] splitValue = keyValue.split("=");
					if (splitValue.length > 1) {
						splitValue1 = StringEscapeUtils.escapeHtml4(splitValue[1]);
					}
					else {
						splitValue1 = "";
					}
					criteriaMap.add(key + splitValue[0], splitValue1);
				}
				else {
					log.info("No modifier found...");
					criteriaMap.add(key, keyValue);
				}
			}

			// Patient search
			String sSearch = buildURL(baseUrl, resourceType) + "/_search";

			ResteasyClient client = WebClientHelper.createClientWihtoutHostVerification();
			ResteasyWebTarget webTarget = client.target(sSearch);

			// set query parameters
			if (criteriaMap != null && !criteriaMap.isEmpty()) {
				webTarget = webTarget.queryParams(criteriaMap);
			}

			Builder targetBuilder = webTarget.request();

			if (contentType != null && contentType.toLowerCase().indexOf("json") >= 0) {
				targetBuilder = targetBuilder.header(HttpHeaders.CONTENT_TYPE, "application/fhir+json" + Constants.CHARSET_UTF8_EXT + fhirVersion)
						.header(HttpHeaders.ACCEPT, "application/fhir+json" + Constants.CHARSET_UTF8_EXT + fhirVersion);
			}
			else {
				targetBuilder = targetBuilder.header(HttpHeaders.CONTENT_TYPE, "application/fhir+xml" + Constants.CHARSET_UTF8_EXT + fhirVersion)
						.header(HttpHeaders.ACCEPT, "application/fhir+xml" + Constants.CHARSET_UTF8_EXT + fhirVersion);
			}

			// Add any additional headers
			targetBuilder = addHeaders(targetBuilder, headers);

			log.info("Resource Search request uri: " + webTarget.getUri());

			if (contentType != null && contentType.toLowerCase().indexOf("json") >= 0) {
				resourceResponse = targetBuilder.post(Entity.entity("", "application/fhir+json" + Constants.CHARSET_UTF8_EXT + fhirVersion));
			}
			else {
				resourceResponse = targetBuilder.post(Entity.entity("", "application/fhir+xml" + Constants.CHARSET_UTF8_EXT + fhirVersion));
			}

			if (resourceResponse.hasEntity()) {
				resourceResponse.bufferEntity();
			}

			debugResponse(resourceResponse);

		}
		catch (Exception e) {
			// Exception caught
			log.severe(e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return resourceResponse;
	}

	/**
	 *
	 * @param searchPageUrl
	 * @param contentType
	 * @return {@link Response}
	 * @throws Exception
	 */
	public Response searchPage(String searchPageUrl, String contentType, List<String> headers) throws Exception {

		log.fine("[START] ResourceRESTClient.searchPage() - searchPageUrl: " + searchPageUrl);

		Response resourceResponse = null;

		try {

			// Response history page
			searchPageUrl = StringEscapeUtils.unescapeXml(searchPageUrl);

			ResteasyClient client = WebClientHelper.createClientWihtoutHostVerification();
			ResteasyWebTarget webTarget = client.target(searchPageUrl);
			Builder targetBuilder = webTarget.request();

			if (contentType != null && contentType.toLowerCase().indexOf("json") >= 0) {
				targetBuilder = targetBuilder.header(HttpHeaders.ACCEPT, "application/fhir+json" + Constants.CHARSET_UTF8_EXT + fhirVersion);
			}
			else {
				targetBuilder = targetBuilder.header(HttpHeaders.ACCEPT, "application/fhir+xml" + Constants.CHARSET_UTF8_EXT + fhirVersion);
			}

			// Add any additional headers
			targetBuilder = addHeaders(targetBuilder, headers);

			log.info("Resource search request uri: " + webTarget.getUri());

			resourceResponse = targetBuilder.get();

			if (resourceResponse.hasEntity()) {
				resourceResponse.bufferEntity();
			}

			debugResponse(resourceResponse);

		}
		catch (Exception e) {
			// Exception caught
			log.severe(e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return resourceResponse;
	}

	/**
	 *
	 * @param baseUrl
	 * @param payload
	 * @param contentType
	 * @return {@link Response}
	 * @throws Exception
	 */
	public Response get(String baseUrl, String params, List<String> headers) throws Exception {

		log.fine("[START] ResourceRESTClient.get() - baseUrl: " + baseUrl + "; params: " + params);

		Response resourceResponse = null;

		try {
			// Response post
			ResteasyClient client = WebClientHelper.createClientWihtoutHostVerification();
			ResteasyWebTarget webTarget = client.target(buildURL(baseUrl, params));
			Builder targetBuilder = webTarget.request();

			// Add any additional headers
			targetBuilder = addHeaders(targetBuilder, headers);

			resourceResponse = targetBuilder.get();

			if (resourceResponse.hasEntity()) {
				resourceResponse.bufferEntity();
			}

			debugResponse(resourceResponse);

		}
		catch (Exception e) {
			// Exception caught
			log.severe(e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return resourceResponse;
	}

	/**
	 *
	 * @param baseUrl
	 * @param payload
	 * @param contentType
	 * @return {@link Response}
	 * @throws Exception
	 */
	public Response post(String baseUrl, String params, String payload, String contentType, List<String> headers) throws Exception {

		log.fine("[START] ResourceRESTClient.post() - baseUrl: " + baseUrl + "; params: " + params + "; payload: [snipped]; contentType: " + contentType);

		Response resourceResponse = null;

		try {
			// Response post
			ResteasyClient client = WebClientHelper.createClientWihtoutHostVerification();
			ResteasyWebTarget webTarget = client.target(buildURL(baseUrl, params));
			Builder targetBuilder = webTarget.request();

			// Add any additional headers
			targetBuilder = addHeaders(targetBuilder, headers);

			if (payload != null) {
				if (contentType != null) {
					resourceResponse = targetBuilder.post(Entity.entity(payload, contentType));
				}
				else {
					resourceResponse = targetBuilder.post(Entity.entity(payload, "text/plain"));
				}
			}
			else {
				resourceResponse = targetBuilder.post(Entity.text(""));
			}

			debugResponse(resourceResponse);

		}
		catch (Exception e) {
			// Exception caught
			log.severe(e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return resourceResponse;
	}

	/**
	 * <p>
	 * Build a URL by joining the baseURL and resourceType together.<br/>
	 * Check baseURL for trailing {@code /}.
	 * </p>
	 *
	 * @param baseUrl
	 * @param resourceType
	 * @return url
	 */
	private String buildURL(String baseUrl, String resourceType) {
		String url = "";

		if (resourceType != null && !resourceType.isEmpty()) {
			if (baseUrl.endsWith("/")) {
				url = baseUrl + resourceType;
			}
			else {
				url = baseUrl + "/" + resourceType;
			}
		}
		else {
			url = baseUrl;
		}

		return url;
	}

	/**
	 * @param targetBuilder
	 * @param headers
	 * @return Builder targetBuilder
	 * @throws Exception
	 */
	public Builder addHeaders(Builder targetBuilder, List<String> headers) throws Exception {

		log.fine("[START] ResourceRESTClient.addHeaders()");

		if (headers != null && !headers.isEmpty()) {
			int separator = -1;
			String headerName = null;
			String headerValue = null;

			for (String header : headers) {
				separator = header.indexOf(":");
				if (separator > -1) {
					headerName = header.substring(0, separator).trim();
					headerValue = header.substring(separator + 1).trim();

					targetBuilder = targetBuilder.header(headerName, headerValue);
				}
			}
		}

		return targetBuilder;
	}

	/**
	 * <p>
	 * Prints the contents of the supplied {@link Response}.<br/>
	 * Useful for debugging purposes.
	 * </p>
	 *
	 * @param response
	 */
	private void debugResponse(Response response) {

		if (response != null) {
			if (response.getHeaders() != null) {

				log.info("----- HTTP HEADERS (RESPONSE) -----");

				for (String key : response.getHeaders().keySet()) {
					log.info("header(" + key + ") is " + response.getHeaders().get(key).toString());
				}
			}

			log.info("----- RESPONSE STATUS -----");
			log.info(Integer.toString(response.getStatus()));

			log.info("----- PAYLOAD (ENTITY) -----");
			String entity = null;
			if (response.getStatus() == Response.Status.NOT_MODIFIED.getStatusCode()) {
				entity = Response.Status.NOT_MODIFIED.getReasonPhrase();
			} else {
				if (response.hasEntity()) {
					entity = response.readEntity(String.class);
				} else {
					entity = ">> NO ENTITY PAYLOAD <<";
				}
			}
			log.info(entity);

		}
		else {
			log.info("Response is NULL.");
		}
	}

}
