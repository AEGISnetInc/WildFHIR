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
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonDiff;
import com.flipkart.zjsonpatch.JsonPatch;

/**
 * JsonPatchUtil - Wrapper methods for JSON PATCH functionality
 *
 * @author richard.ettema
 *
 */
public enum JsonPatchUtil {

	INSTANCE;

	private Logger log = Logger.getLogger("JsonPatchUtil");

	private JsonPatchUtil() {
	}

	/**
	 *
	 * @param source
	 * @param target
	 * @return
	 */
	public String getJsonDiff(String source, String target) throws Exception, IOException, JsonProcessingException {

		log.fine("[START] JsonPatchUtil.getJsonDiff(String source, String target)");

		String jsonDiffString = null;

		ObjectMapper mapper = new ObjectMapper();
		JsonFactory factory = mapper.getFactory();
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		// Convert(parse) argument strings to JsonNode objects
		JsonNode sourceNode = mapper.readTree(source);
		if (sourceNode == null) {
			throw new Exception("Invalid JSON source input resolved to null JsonNode!");
		}
		JsonNode targetNode = mapper.readTree(target);
		if (targetNode == null) {
			throw new Exception("Invalid JSON target input resolved to null JsonNode!");
		}

		// Generate difference JsonNode
		JsonNode diffNode = this.getJsonDiff(sourceNode, targetNode);

		// Convert to Json String
		JsonGenerator generator = factory.createGenerator(out);
		mapper.writeTree(generator, diffNode);

		jsonDiffString = out.toString();

		return jsonDiffString;
	}

	public JsonNode getJsonDiff(JsonNode source, JsonNode target) throws IOException, JsonProcessingException {

		log.fine("[START] JsonPatchUtil.getJsonDiff(JsonNode source, JsonNode target)");

		JsonNode patch = JsonDiff.asJson(source, target);

		return patch;
	}

	/**
	 *
	 * @param patch
	 * @param source
	 * @return
	 * @throws Exception, IOException, JsonProcessingException
	 */
	public String applyJsonPatch(String patch, String source) throws Exception, IOException, JsonProcessingException {

		log.fine("[START] JsonPatchUtil.applyJsonPatch(String patch, String source)");

		String jsonTargetString = null;

		ObjectMapper mapper = new ObjectMapper();
		JsonFactory factory = mapper.getFactory();
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		// Convert(parse) argument strings to JsonNode objects
		JsonNode patchNode = mapper.readTree(patch);
		if (patchNode == null) {
			throw new Exception("Invalid JSON PATCH input resolved to null JsonNode!");
		}
		JsonNode sourceNode = mapper.readTree(source);
		if (sourceNode == null) {
			throw new Exception("Invalid JSON source input resolved to null JsonNode!");
		}

		// Generate difference JsonNode
		JsonNode targetfNode = this.applyJsonPatch(patchNode, sourceNode);

		// Convert to Json String
		JsonGenerator generator = factory.createGenerator(out);
		mapper.writeTree(generator, targetfNode);

		jsonTargetString = out.toString();

		return jsonTargetString;
	}

	public JsonNode applyJsonPatch(JsonNode patch, JsonNode source) throws IOException, JsonProcessingException {

		log.fine("[START] JsonPatchUtil.applyJsonPatch(JsonNode patch, JsonNode source)");

		JsonNode target = JsonPatch.apply(patch, source);

		return target;
	}

	/**
	 *
	 * @param patch
	 * @return
	 * @throws Exception, IOException, JsonProcessingException
	 */
	public boolean isJsonPatchTestOnly(String patch) throws Exception, IOException, JsonProcessingException {

		log.fine("[START] JsonPatchUtil.isJsonPatchTestOnly(String patch)");

		boolean isTestOnly = true;

		String fieldName = null;
		String fieldOpValue = null;

		JsonFactory factory = new JsonFactory();
		JsonParser parser = factory.createParser(patch);

		while(!parser.isClosed()){
		    JsonToken jsonToken = parser.nextToken();

		    if(JsonToken.FIELD_NAME.equals(jsonToken)){
		        fieldName = parser.getCurrentName();

		        jsonToken = parser.nextToken();

		        if ("op".equals(fieldName)) {
		        	fieldOpValue = parser.getValueAsString();
		        	if (!"test".equals(fieldOpValue)) {
		        		isTestOnly = false;
		        		parser.close();
		        		break;
		        	}
		        }
		    }
		}

		return isTestOnly;
	}

}
