/*
 * #%L
 * WildFHIR - wildfhir-model
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
package net.aegis.fhir.service.validation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.r4.formats.IParser.OutputStyle;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.OperationOutcome;

/**
 * @author richard.ettema
 *
 */
public class TestFHIRValidatorClient {

	public static Logger log = Logger.getLogger("TestValidationEngineR4");

	public static void main(String[] args) {

		try {
			byte[] resourceBytes = null;
			String profile = null;

			if (args != null && args.length >= 2) {
				log.info("Argument Flag: " + args[0]);
				log.info("File to validate: " + args[1]);

				if (args[0].equals("-f")) {
					File resourceFile = new File(args[1]);

					resourceBytes = FileUtils.readFileToByteArray(resourceFile);

					if (args.length > 2) {
						log.info("Profile for validation: " + args[2]);
						profile = args[2];
					}

					OperationOutcome outcome = FHIRValidatorClient.instance().validateResource("", resourceBytes, profile);

					XmlParser xmlParserR4 = new XmlParser();
					xmlParserR4.setOutputStyle(OutputStyle.PRETTY);
					ByteArrayOutputStream oOp = new ByteArrayOutputStream();
					xmlParserR4.compose(oOp, outcome, true);
					System.out.println(oOp.toString());
				} else {
					log.severe("Invalid argument flag '" + args[0] + "'! Please use '-f' for resource filename.");
					TestFHIRValidatorClient.usage();
				}
			} else {
				System.out.println("Invalid usage! Two or three arguments expected; arg0 is '-f'; arg1 is a filename; arg2 (optional) is a FHIR profile canonical url.");
				TestFHIRValidatorClient.usage();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}

	private static void usage() {
		System.out.println("");
		System.out.println("(Windows) Argument usage example:");
		System.out.println("-f \"C:\\Temp\\patient.json\" \"http://hl7.org/fhir/us/core/StructureDefinition/us-core-patient\"");
		System.out.println("");
		System.out.println("(Linux) Argument usage example:");
		System.out.println("-f /tmp/patient.json http://hl7.org/fhir/us/core/StructureDefinition/us-core-patient");
		System.out.println("");
	}

}
