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
package net.aegis.fhir.service.validation;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.hl7.fhir.convertors.factory.VersionConvertorFactory_40_50;
import org.hl7.fhir.r5.elementmodel.Manager.FhirFormat;
import org.hl7.fhir.r5.formats.IParser.OutputStyle;
import org.hl7.fhir.r5.formats.JsonParser;
import org.hl7.fhir.r5.formats.XmlParser;
import org.hl7.fhir.r5.model.CodeableConcept;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.hl7.fhir.r5.model.StringType;
import org.hl7.fhir.r5.renderers.RendererFactory;
import org.hl7.fhir.r5.renderers.utils.RenderingContext;
import org.hl7.fhir.utilities.FhirPublication;
import org.hl7.fhir.utilities.validation.ValidationMessage;
import org.hl7.fhir.validation.ValidationEngine;
import org.hl7.fhir.validation.ValidationEngine.ValidationEngineBuilder;

import net.aegis.fhir.service.util.ServicesUtil;

/**
 * Singleton class to provide access to FHIR Validator.
 *
 * @author richard.ettema
 *
 */
public class FHIRValidatorClient {

	private static Logger log = Logger.getLogger("FHIRValidatorClient");

    private static FHIRValidatorClient me;

    private static String FHIR_PACKAGES_ENV_VAR = "FHIR_PACKAGES";

	private ValidationEngine engine = null;

	private JsonParser jsonParser = new JsonParser();
	private org.hl7.fhir.r4.formats.JsonParser jsonParserR4 = new org.hl7.fhir.r4.formats.JsonParser();

	private org.hl7.fhir.r4.formats.RdfParser rdfParserR4 = new org.hl7.fhir.r4.formats.RdfParser();

	private XmlParser xmlParser = new XmlParser();
	private org.hl7.fhir.r4.formats.XmlParser xmlParserR4 = new org.hl7.fhir.r4.formats.XmlParser();

	private void initializeClient() {

		try {
			log.info("Initialize FHIRValidatorClient Instance");

			long start = System.currentTimeMillis();

			ValidationEngineBuilder builder = new ValidationEngine.ValidationEngineBuilder();
			engine = builder.fromSource("hl7.fhir.r4.core");

			// Check for additional packages via environment variable
			String fhirPackages = System.getenv(FHIR_PACKAGES_ENV_VAR);
			if (fhirPackages != null && !fhirPackages.isEmpty()) {
				loadPackages(fhirPackages);
			}

			// Set anyExtensionsAllowed equal to true to relax error rule on unknown extensions
			engine.setAnyExtensionsAllowed(true);

			// Set assume valid REST references
			engine.setAssumeValidRestReferences(true);

			// Set allow example paths
			engine.setAllowExampleUrls(true);

			engine.connectToTSServer("http://tx.fhir.org/r4", null, FhirPublication.R4, false);

			log.info("FHIR R4 v4.0.1 Validation Engine initialization completed in " + ServicesUtil.INSTANCE.getElapsedTime(start));
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to initialize FHIR Validation Engine!", e);
		}
	}

	public static FHIRValidatorClient instance() {
		if (me == null) {
			synchronized (FHIRValidatorClient.class) {
				if (me == null) {
					me = new FHIRValidatorClient();

					if (me.engine == null) {
						me.initializeClient();
					}
				}
			}
		}
		return me;
	}

	public ValidationEngine getEngine() {
		return engine;
	}

	public void reload() {
		me.initializeClient();
	}

	/**
	 * Process FHIR validation request using available pooled FHIRValidator object.
	 *
	 * @param resourceName
	 * @param resourceContents
	 * @param profileUri
	 * @return <code>OperationOutcome</code> with results of the validation
	 * @throws Exception
	 */
	public org.hl7.fhir.r4.model.OperationOutcome validateResource(String resourceName, byte[] resourceContents, String profileUri) throws Exception {
		long start = System.currentTimeMillis();

		log.fine("FHIRValidatorClient.validateResource() - START");

		OperationOutcome rOutcome = null;

		try {
			List<String> profiles = null;
			if (profileUri != null) {
				profiles = new ArrayList<String>();
				profiles.add(profileUri);
			}

			FhirFormat cntType = getFhirFormat(resourceContents);

			List<ValidationMessage> messages = new ArrayList<ValidationMessage>();

			rOutcome = engine.validate(resourceContents, cntType, profiles, messages);

			try {
				// Now parse the Resource contents; this is the last validation to insure the contents is a valid FHIR resource instance
				if (cntType.equals(FhirFormat.JSON)) {
					jsonParserR4.parse((byte[])resourceContents);
				}
				else if (cntType.equals(FhirFormat.XML)) {
					xmlParserR4.parse((byte[])resourceContents);
				}
				else if (cntType.equals(FhirFormat.TURTLE)) {
					rdfParserR4.parse((byte[])resourceContents);
				}
			}
			catch (Exception ie) {
				ie.printStackTrace();

				// FHIR resource parsing failed, content is not a valid FHIR resource; throw appropriate exception to catch below
				throw ie;
			}

		} catch (Exception e) {
			String outcome = getOperationOutcome(OperationOutcome.IssueSeverity.FATAL, OperationOutcome.IssueType.PROCESSING,
					"Exception validating resource " + resourceName + ". " + e.getMessage(), null, "application/xml+fhir");

			rOutcome = (OperationOutcome) xmlParser.parse(outcome);
		} catch (Throwable e) {
			String outcome = getOperationOutcome(OperationOutcome.IssueSeverity.FATAL, OperationOutcome.IssueType.PROCESSING,
					"Exception validating resource " + (String)resourceName + ". " + e.getMessage(), null, "application/xml+fhir");

			rOutcome = (OperationOutcome) xmlParser.parse(outcome);
		}

		log.fine("FHIRValidatorClient.validateResource() - END");

		log.info("FHIR Validator - validation of resource completed in " + ServicesUtil.INSTANCE.getElapsedTime(start));

		org.hl7.fhir.r4.model.OperationOutcome r4Outcome = convertR5OOR4(rOutcome);

		return r4Outcome;
	}

	/*
	 * Private methods
	 */

	/**
	 * Load FHIR packages from environment variable FHIR_PACKAGES
	 * Expect a string with comma delimited list of package#version, ...
	 * NOTE - NO ERROR CHECKING IS DONE TO VERIFY THE PACKAGE NAME AND VERSION
	 * 
	 * @param fhirPackages
	 */
	private void loadPackages(String fhirPackages) {

		// Split list of packages by comma
		String[] packageList = fhirPackages.split(",");

		for (String pkg : packageList) {
			// Split pkg into package name and version by '#'
			String[] pkgSplit = pkg.split("#");
			String pkgName = pkgSplit[0];
			String pkgVersion = pkgSplit[1];
			if (pkgVersion == null || pkgVersion.isEmpty()) {
				pkgVersion = null;
			}

			try {
				log.info("Load package " + pkgName + "#" + (pkgVersion != null ? pkgVersion : "null"));
				engine.loadPackage(pkgName, pkgVersion);
			} catch (Exception e) {
				log.severe("Load package " + pkgName + "#" + (pkgVersion != null ? pkgVersion : "null") + " failed! " + e.getMessage());
			}
		}
	}

	/**
	 * Build an OperationOutcome resource with a single issue.
	 *
	 * @param severity
	 * @param type
	 * @param details
	 * @param location
	 * @param producesType
	 * @return
	 */
	private String getOperationOutcome(OperationOutcome.IssueSeverity severity, OperationOutcome.IssueType type, String details, String location, String producesType) {
		String sOp = "";

		try {
			OperationOutcome op = new OperationOutcome();

			OperationOutcome.OperationOutcomeIssueComponent issue = getOperationOutcomeIssueComponent(severity, type, details, location);

			if (issue != null) {
				op.getIssue().add(issue);
			}

			// Use Java Core Library RenderingContext
			RenderingContext rc = new RenderingContext(engine.getContext(), null, null, "http://hl7.org/fhir", "", null, RenderingContext.ResourceRendererMode.END_USER, RenderingContext.GenerationRules.VALID_RESOURCE);
			RendererFactory.factory(op, rc).render(op);

			// Convert the OperationOutcome to XML or JSON string
			ByteArrayOutputStream oOp = new ByteArrayOutputStream();

			if (producesType == null || producesType.indexOf("xml") >= 0) {
				xmlParser.setOutputStyle(OutputStyle.PRETTY);
				xmlParser.compose(oOp, op, true);
				sOp = oOp.toString();
			}
			else {
				jsonParser.setOutputStyle(OutputStyle.PRETTY);
				jsonParser.compose(oOp, op);
				sOp = oOp.toString();
			}

		}
		catch (Exception e) {
			// Handle generic exceptions
			e.printStackTrace();
			// Exception not thrown to allow operation to complete
		}

		return sOp;
	}

	/**
	 * Build a single OperationOutcomeIssueComponent object
	 *
	 * @param severity
	 * @param type
	 * @param details
	 * @param location
	 * @return
	 */
	private OperationOutcome.OperationOutcomeIssueComponent getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity severity, OperationOutcome.IssueType type, String details, String location) {
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
			if (location != null) {
				issue.getLocation().add(new StringType(location));
			}

		}
		catch (Exception e) {
			// Handle generic exceptions
			e.printStackTrace();
			// Exception not thrown to allow operation to complete
		}

		return issue;
	}

	private org.hl7.fhir.r4.model.OperationOutcome convertR5OOR4(OperationOutcome rOutcome) throws Exception {
		org.hl7.fhir.r4.model.OperationOutcome r4Outcome = null;

		try {
			r4Outcome = (org.hl7.fhir.r4.model.OperationOutcome)VersionConvertorFactory_40_50.convertResource(rOutcome);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

		return r4Outcome;
	}

	private FhirFormat getFhirFormat(byte[] source) {
		FhirFormat cntType = FhirFormat.TEXT;

		if (source != null) {
			if (isXml(source)) {
				cntType = FhirFormat.XML;
			}
			else if (isJson(source)) {
				cntType = FhirFormat.JSON;
			}
			else if (isTurtle(source)) {
				cntType = FhirFormat.TURTLE;
			}
		}

		return cntType;
	}

	private boolean isXml(byte[] source) {
		int x = position(source, '<');
		int j = position(source, '{');
		int t = position(source, '@');
		if (x == Integer.MAX_VALUE && j == Integer.MAX_VALUE || (t < j && t < x))
			return false;
		return (x < j);
	}

	private boolean isJson(byte[] source) {
		int x = position(source, '<');
		int j = position(source, '{');
		int t = position(source, '@');
		if (x == Integer.MAX_VALUE && j == Integer.MAX_VALUE || (t < j && t < x))
			return false;
		return (j < x);
	}

	private boolean isTurtle(byte[] source) {
		int x = position(source, '<');
		int j = position(source, '{');
		int t = position(source, '@');
		if (x == Integer.MAX_VALUE && j == Integer.MAX_VALUE || (t > j) || (t > x))
			return false;
		return true;
	}

	private int position(byte[] bytes, char target) {
		byte t = (byte) target;
		for (int i = 0; i < bytes.length; i++)
			if (bytes[i] == t)
				return i;
		return Integer.MAX_VALUE;
	}

}
