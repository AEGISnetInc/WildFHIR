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
package net.aegis.fhir.service.paging;

import java.io.ByteArrayOutputStream;
import java.util.logging.Logger;

import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.CacheAccess;
import org.apache.commons.jcs.access.exception.CacheException;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.formats.IParser.OutputStyle;
import org.hl7.fhir.r4.model.Bundle;

/**
 * @author richard.ettema
 *
 */
public enum PagingHistoryManager {

	INSTANCE;

	private Logger log = Logger.getLogger("PagingHistoryManager");

	private CacheAccess<String, String> cache = null;

	private PagingHistoryManager() {
		try {
			cache = JCS.getInstance("default");
		}
		catch (CacheException e) {
			System.out.println(String.format("Problem initializing cache: %s", e.getMessage()));
		}
	}

	public void putInCache(String key, Bundle page) {

		log.fine("[START] PagingHistoryManager.putInCache(" + key + ")");

		try {
			// Convert FHIR Bundle to String
			ByteArrayOutputStream oBundle = new ByteArrayOutputStream();
			String sBundle = "";
			XmlParser xmlParser = new XmlParser();
			xmlParser.setOutputStyle(OutputStyle.PRETTY);
			xmlParser.compose(oBundle, page, true);
			sBundle = oBundle.toString();

			cache.put(key, sBundle);
		}
		catch (CacheException e) {
			log.severe(String.format("Problem putting history page in the cache, for key %s%n%s", key, e.getMessage()));
		}
		catch (Exception e) {
			log.severe(String.format("Problem composing history page before putting in the cache, for key %s%n%s", key, e.getMessage()));
		}
	}

	public Bundle retrieveFromCache(String key) {

		log.fine("[START] PagingHistoryManager.retrieveFromCache(" + key + ")");

		Bundle page = null;

		try {
			// Get cached Bundle stored as String
			String sBundle = cache.get(key);

			// Convert back to FHIR Bundle object
			XmlParser xmlP = new XmlParser();
			page = (Bundle) xmlP.parse(sBundle.getBytes());
		}
		catch (CacheException e) {
			log.severe(String.format("Problem putting history page in the cache, for key %s%n%s", key, e.getMessage()));
		}
		catch (Exception e) {
			log.severe(String.format("Problem parsing history page after getting from the cache, for key %s%n%s", key, e.getMessage()));
		}

		return page;
	}

}
