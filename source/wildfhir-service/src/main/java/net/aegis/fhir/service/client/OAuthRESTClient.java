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
package net.aegis.fhir.service.client;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import net.aegis.fhir.model.Serverdirectory;
import net.aegis.fhir.service.CodeService;
import net.aegis.fhir.service.util.DebugUtil;
import net.aegis.fhir.service.util.WebClientHelper;

/**
 * 
 */
public class OAuthRESTClient implements Serializable {

	@Serial
	private static final long serialVersionUID = 1817743118884807072L;

	private Logger log = Logger.getLogger("OAuthRESTClient");

	CodeService codeService;

	/**
	 * Constructor - Default
	 */
	public OAuthRESTClient(CodeService codeService) {
		super();
		this.codeService = codeService;
	}

	public Response getAccessTokenResponse(Serverdirectory serverdirectory) throws Exception {
		log.fine("[START] OAuthRESTClient.getAccessTokenResponse()");

		ResteasyClient client = null;
		Response resourceResponse;

		log.fine("OAuthRESTClient.getAccessTokenResponse(): "
				+ "\ngrant_type   : " + serverdirectory.getOauthGrantType()
				+ "\nclient_id    : " + serverdirectory.getOauthClientId()
				+ "\nclient_secret: " + serverdirectory.getOauthClientSecret()
				+ "\nscope        : " + serverdirectory.getOauthScope());

		try {
			client = WebClientHelper.createClientWihtoutHostVerification();
			ResteasyWebTarget webTarget = client.target(serverdirectory.getOauthTokenUrl());
			Builder targetBuilder = webTarget.request();

			targetBuilder = targetBuilder.header(HttpHeaders.ACCEPT, "application/json");
			targetBuilder = targetBuilder.header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");

			// Build OAuth form payload - concatenate Serverdirectory oauth data
			Form fOAuthForm = new Form();

			fOAuthForm.param("grant_type", serverdirectory.getOauthGrantType())
					.param("client_id", serverdirectory.getOauthClientId())
					.param("client_secret", serverdirectory.getOauthClientSecret())
					.param("scope", serverdirectory.getOauthScope());

			resourceResponse = targetBuilder.post(Entity.form(fOAuthForm));

			if (resourceResponse.hasEntity()) {
				resourceResponse.bufferEntity();
			}			
			DebugUtil.debugResponse(resourceResponse);			
		}
		catch (Exception e) {
			log.severe(e.getMessage());
			throw e;
		}
		return resourceResponse;
	}

	public List<String> getHeadersWithAuthToken(List<String> headers, Serverdirectory serverdirectory) throws Exception {
		List<String> newHeaders = new ArrayList<>();
		if (headers != null && !headers.isEmpty()) {
			newHeaders.addAll(headers);
		}

		// Get Serverdirectory OAuth token and add to headers only if OAuth Client processing is enabled/supported
		if (codeService.isSupported("oauthClientEnabled")) {
			// Check Serverdirectory OAuth client_id
			if (serverdirectory != null && serverdirectory.getOauthClientId() != null) {
				Response oauthResponse = this.getAccessTokenResponse(serverdirectory);
				String access_token = this.getResponseOAuthAccessToken(oauthResponse);

				if (access_token != null) {
					newHeaders.add("Authorization:" + access_token);
				}
			}
		}

		return newHeaders;
	}

	public String getResponseOAuthAccessToken(Response response) throws Exception, IOException, JsonProcessingException {

		String access_token = null;
		String token_type = null;
		String entityString = response.readEntity(String.class);

		JsonFactory factory = new JsonFactory();
		com.fasterxml.jackson.core.JsonParser parser = factory.createParser(entityString);

		while(!parser.isClosed()) {
		    JsonToken jsonToken = parser.nextToken();

		    if(JsonToken.FIELD_NAME.equals(jsonToken)){
		        parser.nextToken();

		        if ("access_token".equals(parser.currentName())) {
		        	access_token = parser.getValueAsString();
		        	//break;
		        }

		        if ("token_type".equals(parser.currentName())) {
		        	token_type = parser.getValueAsString();
		        	//break;
		        }
		    }

		    if (access_token != null && token_type != null) {
		    	break;
		    }
		}

		if (token_type != null && !token_type.isEmpty()) {
			log.fine("getResponseOAuthAccessToken - token_type is '" + token_type + "'");

				if (token_type.equals("basic")) {
					token_type = "Basic ";
				} else if (token_type.equals("bearer")) {
					token_type = "Bearer ";
				}
				else {
					token_type += " ";
				}

		}
		else {
			log.warning("getResponseOAuthAccessToken - token_type is NULL");
			token_type = "";
		}

		if (access_token != null) {
			access_token = token_type + access_token;
		}

		return access_token;
	}

}
