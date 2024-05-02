/*
 * #%L
 * WildFHIR - wildfhir-rest-server
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
package net.aegis.fhir.rest.annotations;

import java.io.IOException;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

/**
 * jax-rs Request Filter to intercept HTTP PATCH method and convert Content-Type
 * "application/json-patch+json" to "application/json" due to invalid handling of
 * JSON Patch Media Type.
 *
 * @author richard.ettema
 *
 */
@Provider
@PreMatching
public class JsonPatchEnableFilter implements ContainerRequestFilter {

	@Override
	public void filter(ContainerRequestContext ctx) throws IOException {
		if (ctx.getMethod().equals(HttpMethod.PATCH)) {
			String patchMediaType = ctx.getMediaType().toString();
			String newMediaType = MediaType.APPLICATION_JSON;

			System.out.println("JsonPatchEnableFilter.filter - PATCH; Original MediaType is '" + patchMediaType);

			if (patchMediaType.startsWith(MediaType.APPLICATION_JSON)) {

				int attrPos = patchMediaType.indexOf(";");

				if (attrPos > 0) {
					// Append any extra sent mime-type attributes as-is
					newMediaType += patchMediaType.substring(attrPos, patchMediaType.length());
				}
				ctx.getHeaders().putSingle("Content-Type", newMediaType);

				System.out.println("JsonPatchEnableFilter.filter - PATCH; New MediaType is '" + newMediaType);
			}

		}
	}

}
