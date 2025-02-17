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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.logging.Logger;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

import com.github.dnault.xmlpatch.Patcher;
import com.github.dnault.xmlpatch.internal.XmlHelper;

/**
 * XmlPatchUtil - Wrapper methods for XML PATCH functionality
 *
 * @author richard.ettema
 *
 */
public enum XmlPatchUtil {

	INSTANCE;

	private Logger log = Logger.getLogger("XmlPatchUtil");

	private Namespace fhirNs = Namespace.getNamespace("http://hl7.org/fhir");
	private Namespace xhtmlNs = Namespace.getNamespace("http://www.w3.org/1999/xhtml");

	private XmlPatchUtil() {
	}

	/**
	 *
	 * @param patch
	 * @param source
	 * @return
	 * @throws Exception
	 *             , IOException
	 */
	public String applyXmlPatch(String patch, String source) throws Exception, IOException {

		log.fine("[START] XmlPatchUtil.applyXmlPatch(String patch, String source)");

		// Check patch object for at least one operation. If no operations found, throw exception
		String patchCopy = patch;

		int patchOps = XmlHelper.parse(asStream(patchCopy)).getRootElement().getChildren().size();

		if (patchOps == 0) {
			throw new Exception("No patch operations found!");
		}

		String xmlResultString = null;

		// Create output stream for result
		ByteArrayOutputStream result = new ByteArrayOutputStream();

		// Remove namespaces from FHIR XML resource
		SAXBuilder builder = new SAXBuilder();

		// DTD validation is makes an HTTP request and is slow. Don't need this feature, so disable it.
		builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

		Document targetDoc = builder.build(new ByteArrayInputStream(source.getBytes("UTF-8")));

		Element targetRoot = targetDoc.getRootElement();
		removeChildrenNamespace(targetRoot);

		XMLOutputter outputter = new XMLOutputter();

		ByteArrayOutputStream newSource = new ByteArrayOutputStream();
		outputter.output(targetDoc, newSource);

		// Apply patch
		Patcher.patch(asStream(newSource.toString("UTF-8")), asStream(patch), result);

		// Add required FHIR namespaces back
		Document resultDoc = builder.build(new ByteArrayInputStream(result.toString("UTF-8").getBytes("UTF-8")));

		Element resultRoot = resultDoc.getRootElement();
		addFhirNamespaces(resultRoot);

		ByteArrayOutputStream newResult = new ByteArrayOutputStream();
		outputter.output(resultDoc, newResult);

		// Convert result to string
		xmlResultString = newResult.toString("UTF-8");

		return xmlResultString;
	}

	private InputStream asStream(String s) {
		try {
			return new ByteArrayInputStream(s.getBytes("UTF-8"));
		}
		catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	private void removeChildrenNamespace(Element rootElement) {
		// Remove name space from root element
		rootElement.setNamespace(Namespace.NO_NAMESPACE);

		// Get all children
		List<Element> children = rootElement.getChildren();

		// if any children, remove their namespace
		if (!children.isEmpty()) {
			for (Element child : children) {
				removeChildrenNamespace(child);
			}
		}
	}

	private void addFhirNamespaces(Element rootElement) {
		// Add FHIR namespace to root element
		rootElement.setNamespace(fhirNs);

		// Iterate through children and set the Narrative text div element namespace
		List<Element> children = rootElement.getChildren();

		// if any children, check for div element
		if (!children.isEmpty()) {
			for (Element child : children) {
				addDivNamespace(child);
			}
		}
	}

	private void addDivNamespace(Element element) {
		// Check for div element and if found set namespace and return
		if (element.getName().toLowerCase().equals("div")) {
			element.setNamespace(xhtmlNs);
			return;
		}
		else {
			// Iterate through children
			List<Element> children = element.getChildren();

			// if any children, check for div element
			if (!children.isEmpty()) {
				for (Element child : children) {
					addDivNamespace(child);
				}
			}
		}
	}
}
