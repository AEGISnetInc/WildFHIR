/*
 * #%L
 * WildFHIR - wildfhir-service
 * %%
 * Copyright (C) 2026 AEGIS.net, Inc.
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

import java.io.InputStream;
import java.io.StringWriter;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedMap;

/**
 * <p>
 * Prints the contents of the received request.<br/>
 * Useful for debugging purposes.
 * </p>
 * 
 * @author padmanabha.ketha
 * @author richard.ettema
 */
public class DebugUtil {

    private static final Logger log = Logger.getLogger(DebugUtil.class.getName());

	/**
	 * <p>
	 * Prints the contents of the received request.<br/>
	 * Useful for debugging purposes.
	 * </p>
	 *
	 * @param request
	 * @param headers
	 * @param resourceInputStream
	 */
    public static String debugRequest(HttpServletRequest request, HttpHeaders headers, InputStream resourceInputStream) {
		return DebugUtil.debugRequest(request, headers, resourceInputStream, null);
	}

	/**
	 * <p>
	 * Prints the contents of the received request.<br/>
	 * Useful for debugging purposes.
	 * </p>
	 *
	 * @param request
	 * @param headers
	 * @param resourceInputStream
	 * @param form
	 */
	public static String debugRequest(HttpServletRequest request, HttpHeaders headers, InputStream resourceInputStream, MultivaluedMap<String, String> form) {
		return DebugUtil.debugRequest(request, headers, resourceInputStream, form, true);
	}

	/**
	 * <p>
	 * Prints the contents of the received request.<br/>
	 * Useful for debugging purposes.<br/>
	 * Use fine logging.
	 * </p>
	 *
	 * @param request
	 * @param headers
	 * @param resourceInputStream
	 * @param form
	 * @param snipped
	 */
	public static String debugRequest(HttpServletRequest request, HttpHeaders headers, InputStream resourceInputStream, MultivaluedMap<String, String> form, boolean snipped) {

		String payload = null;

		try {
			if (request != null) {
				log.fine("----- HTTP REQUEST -----");
				log.fine("Remote host is '" + (request.getRemoteHost() == null ? "NOT FOUND" : request.getRemoteHost()) + "'");
			}

			if (headers != null) {
				log.fine("----- HTTP HEADERS (REQUEST) -----");

				MultivaluedMap<String, String> requestHeaders = headers.getRequestHeaders();
				if (requestHeaders != null) {
					for (String key : requestHeaders.keySet()) {
						for (String keyValue : requestHeaders.get(key)) {
							log.fine("header(" + key + ") is " + keyValue);
						}
					}
				}
			}

			log.fine("----- REQUEST URL -----");

			// Use HttpServletRequest to get the raw, unescaped URL safely
			String rawPath = request.getRequestURI();
			String rawQuery = request.getQueryString();
			String fullRawUrl = rawQuery != null ? rawPath + "?" + rawQuery : rawPath;

			log.fine("Request URL: " + fullRawUrl);

		    log.fine("----- FORM INPUT PARAMS -----");
			if (form != null && !form.isEmpty()) {
				for (String key : form.keySet()) {
					log.fine("input(" + key + ") is " + form.get(key).toString());
				}
			}

			log.fine("----- PAYLOAD -----");
			if (resourceInputStream != null) {
				try {
					StringWriter writer = new StringWriter();
					String encoding = "UTF-8";
					IOUtils.copy(resourceInputStream, writer, encoding);
					payload = writer.toString();
	
					if (snipped == false) {
						log.fine(payload);
					}
					else {
						log.fine(">> SNIPPED <<");
					}
				}
				catch (Exception e) {
					log.severe("Exception parsing payload! " + e.getMessage());
					e.printStackTrace();
				}
			}
			else {
				log.fine(">> NO PAYLOAD <<");
			}
		}
		catch(Exception e) {
			log.severe(e.getMessage());
		}

		return payload;
    }
}
