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

import java.util.UUID;

/**
 * UUID generate utility.
 *
 * @author richard.ettema
 *
 */
public class UUIDUtil {

	public static String UUID_PREFIX = "urn:uuid:";

	public static String getGUID() {
		String guidString = UUID.randomUUID().toString();

		guidString = guidString.replaceAll("-", "");

		return guidString;
	}

	public static String getUUID() {
		return getUUID(false);
	}

	public static String getUUID(boolean usePrefix) {
		String uuidString = UUID.randomUUID().toString();

		if (usePrefix) {
			uuidString = UUID_PREFIX + uuidString;
		}

		return uuidString;
	}

	public static void main(String[] args) {

		StringBuffer sbUUIDs = new StringBuffer("");

		for (int i=0; i<300; i++) {
			sbUUIDs.append(UUIDUtil.getUUID()).append("\n");
		}

		System.out.println(sbUUIDs.toString());
	}

}
