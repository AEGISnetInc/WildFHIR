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
package net.aegis.fhir.service.narrative;

import java.util.logging.Logger;

import org.hl7.fhir.r4.context.SimpleWorkerContext;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.utils.NarrativeGenerator;
import org.hl7.fhir.utilities.npm.FilesystemPackageCacheManager;
import org.hl7.fhir.utilities.npm.NpmPackage;

import net.aegis.fhir.service.util.ServicesUtil;

/**
 * Singleton class to provide access to the FHIR NarrativeGenerator.
 *
 * @author richard.ettema
 *
 */
public class FHIRNarrativeGeneratorClient {

	private static Logger log = Logger.getLogger("FHIRNarrativeGeneratorClient");

	private static FHIRNarrativeGeneratorClient me = new FHIRNarrativeGeneratorClient();

	private NarrativeGenerator narrativeGenerator;

	private FHIRNarrativeGeneratorClient() {
		try {
			log.info("Initialize FHIRNarrativeGeneratorClient Instance");

			long start = System.currentTimeMillis();

			/*
			 * INSTANTIATE NARRATIVE GENENERATOR AND SET REQUIRED CONTEXTS
			 */
			FilesystemPackageCacheManager pcm = new FilesystemPackageCacheManager.Builder().build();
			NpmPackage npmX = pcm.loadPackage("hl7.fhir.r4.core", "4.0.1");
			SimpleWorkerContext contextR4 = SimpleWorkerContext.fromPackage(npmX);
			narrativeGenerator = new NarrativeGenerator("", "", contextR4);

			log.info(NarrativeGenerator.class.getSimpleName() + " initialization completed in " + ServicesUtil.INSTANCE.getElapsedTime(start));
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to initialize " + NarrativeGenerator.class.getSimpleName(), e);
		}
	}

	public static FHIRNarrativeGeneratorClient instance() {
		return me;
	}

	/**
	 * Process FHIR NarrativeGenerator generate request using available pooled FHIRNarrativeGenerator object.
	 *
	 * @param domainResource
	 * @throws Exception
	 */
	public void generate(DomainResource domainResource) throws Exception {
		long start = System.currentTimeMillis();

		log.fine("FHIRNarrativeGeneratorClient.generate() - START");

		try {
			narrativeGenerator.generate(domainResource, null);
		}
		catch (Exception e) {
			// Swallow exception to allow calling process to continue
			log.severe(e.getMessage());
		}

		log.fine("FHIRNarrativeGeneratorClient.generate() - END");

		log.fine("FHIR NarrativeGenerator - generation of narrative text for resource completed in " + ServicesUtil.INSTANCE.getElapsedTime(start));

	}

}
