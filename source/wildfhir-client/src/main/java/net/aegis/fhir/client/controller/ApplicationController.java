/*
 * #%L
 * WildFHIR - wildfhir-client
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
package net.aegis.fhir.client.controller;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import jakarta.faces.annotation.ManagedProperty;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.Response;

import org.hl7.fhir.r4.formats.IParser.OutputStyle;
import org.hl7.fhir.r4.formats.JsonParser;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleLinkComponent;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StringType;
import org.primefaces.event.TabChangeEvent;

import net.aegis.fhir.client.ApplicationContext;
import net.aegis.fhir.client.model.BundleWrapper;
import net.aegis.fhir.client.model.ResourceResponseWrapper;
import net.aegis.fhir.model.Constants;
import net.aegis.fhir.model.LabelKeyValueBean;
import net.aegis.fhir.model.ResourceType;
import net.aegis.fhir.model.Serverdirectory;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.StringUtils;
import net.aegis.fhir.service.util.UTCDateUtil;

/**
 * <p>
 * This class is the controller to handle interactions with the application view.</br> At a later point this controller
 * can be broken apart to separate varies functionality pieces managed by their own controllers e.g. Search,
 * Registration, Preferences, Security etc.
 * </p>
 *
 * @author richard.ettema
 *
 */
@Named("controller")
@SessionScoped
public class ApplicationController implements Serializable {

	private static final long serialVersionUID = 5848069089082841377L;

	private Logger log = Logger.getLogger("ApplicationController");

	@Inject
	transient UTCDateUtil utcDateUtil;

	@Inject
	@ManagedProperty("#{context}")
	private ApplicationContext context;

	public ApplicationController() {
	}

	/**
	 * Method gets executed when user changes tab in UI
	 *
	 * @param event
	 */
	public void onTabChange(TabChangeEvent<?> event) {
		context.clear();
		Iterator<FacesMessage> iter = FacesContext.getCurrentInstance().getMessages();
    	while (iter.hasNext()) {
    		iter.remove();
    	}
	}

	public ApplicationContext getContext() {
		return context;
	}

	public void setContext(ApplicationContext context) {
		this.context = context;
	}

	/*
	 * FHIR Interaction methods
	 */

	/**
	 * Creates a new Resource
	 *
	 * @see Resource
	 */
	public void fhirCreate() {
		log.fine("[START] ApplicationController.fhirCeate()");

		try {
			log.fine("BasePath for FHIR create: " + context.getSelectedServerURL());

			context.setCurrentView("create");

			String formatType = context.getSelectedFormatType();
			String ifNoneExist = context.getIfNoneExist();
			String prefer = context.getPrefer();
			String _format = context.get_format();
			String resourceString = context.getResourceString();

			ByteArrayInputStream iResource = null;
			Resource resource = null;
			Response response = null;
			ResourceResponseWrapper wrapper = null;

			if (resourceString.isEmpty()) {
				throw new Exception("No resource content provided.");
			}

			if (formatType.equals("XML")) {
				// Convert XML contents to Resource
				XmlParser xmlP = new XmlParser();
				int firstValid = resourceString.indexOf("<");
				if (firstValid > 0) {
					resourceString = resourceString.substring(firstValid);
				}

				iResource = new ByteArrayInputStream(resourceString.getBytes());
				resource = xmlP.parse(iResource);

				response = context.getResourceRESTClient().create(resource, context.getSelectedServerURL(), context.getSelectedResourceType(), Constants.FHIR_XML_CONTENT, ifNoneExist, prefer, _format, null);
			}
			else {
				// Convert JSON contents to Resource
				JsonParser jsonP = new JsonParser();
				int firstValid = resourceString.indexOf("{");
				if (firstValid > 0) {
					resourceString = resourceString.substring(firstValid);
				}

				iResource = new ByteArrayInputStream(resourceString.getBytes());
				resource = jsonP.parse(iResource);

				response = context.getResourceRESTClient().create(resource, context.getSelectedServerURL(), context.getSelectedResourceType(), Constants.FHIR_JSON_CONTENT, ifNoneExist, prefer, _format, null);
			}

			if (response != null) {
				String contentType = response.getHeaderString("Content-Type");
				if (contentType != null) {
					if (contentType.toUpperCase().contains("XML")) {
						context.setReturnedFormatType("XML");
					}
					else if (contentType.toUpperCase().contains("JSON")) {
						context.setReturnedFormatType("JSON");
					}
					else {
						context.setReturnedFormatType(formatType);
					}
				}
				else {
					context.setReturnedFormatType(formatType);
				}

				if ((response.getStatus() == Response.Status.OK.getStatusCode()) || (response.getStatus() == Response.Status.CREATED.getStatusCode())) {
					try {
						wrapper = new ResourceResponseWrapper(response);

						if (context.getReturnedFormatType().equals("XML")) {
							context.setResponseString(wrapper.getResourceXML());
						}
						else {
							context.setResponseString(wrapper.getResourceJSON());
						}

						FacesContext.getCurrentInstance().addMessage(
								"tabView:interactionsTabView:createForm",
								new FacesMessage(FacesMessage.SEVERITY_INFO, "Resource with ID: " + wrapper.getResourceBean().getResourceId() + " successfully created.", "Resource with ID: " + wrapper.getResourceBean().getResourceId()
										+ " successfully created."));
					}
					catch (Exception e) {
						log.fine(e.getMessage());
						FacesContext.getCurrentInstance().addMessage("tabView:interactionsTabView:createForm",
								new FacesMessage(FacesMessage.SEVERITY_ERROR, "Resource parsing failed! Please check the client logs.", "Resource parsing failed! Please check the client logs."));
						e.printStackTrace();
					}
				}
				else {
					try {
						wrapper = new ResourceResponseWrapper(response);

						if (context.getReturnedFormatType().equals("XML")) {
							context.setResponseString(wrapper.getResourceXML());
						}
						else {
							context.setResponseString(wrapper.getResourceJSON());
						}

						FacesContext.getCurrentInstance().addMessage("tabView:interactionsTabView:createForm", new FacesMessage(FacesMessage.SEVERITY_INFO, "Response " + response.getStatus() + " - Failed to create new Resource entry.", "Response " + response.getStatus() + " - Failed to create new Resource entry."));
					}
					catch (Exception e) {
						log.fine(e.getMessage());
						FacesContext.getCurrentInstance().addMessage("tabView:interactionsTabView:createForm",
								new FacesMessage(FacesMessage.SEVERITY_ERROR, "Resource parsing failed! Please check the client logs.", "Resource parsing failed! Please check the client logs."));
						e.printStackTrace();
					}
				}
			}

		}
		catch (Exception e) {
			log.fine(e.getMessage());
			FacesContext.getCurrentInstance().addMessage("tabView:interactionsTabView:createForm", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error creating resource! Please check the client logs.", "Error reading resource! Please check the client logs."));
			e.printStackTrace();
		}

		context.setResourceString(null);

		log.fine("[END] ApplicationController.fhirCreate()");
	}

	/**
	 * Perform a FHIR read for the supplied resource id
	 *
	 * @return
	 */
	public void fhirRead(ActionEvent event) {
		log.fine("[START] ApplicationController.fhirRead()");

		try {
			log.fine("BasePath for FHIR read: " + context.getSelectedServerURL());
			context.setResourceResults(null);
			String resourceId = context.getResourceId();
			String ifModifiedSince = context.getIfModifiedSince();
			String ifNoneMatch = context.getIfNoneMatch();
			String _format = context.get_format();
			String _summary = context.get_summary();
			Response resourceResponse = null;
			context.setResourceResults(new ArrayList<ResourceResponseWrapper>());
			String formatType = context.getSelectedFormatType();

			resourceResponse = context.getResourceRESTClient().read(resourceId, context.getSelectedServerURL(), context.getSelectedResourceType(), formatType, ifModifiedSince, ifNoneMatch, _format, _summary, null);

			if (resourceResponse != null) {
				String contentType = resourceResponse.getHeaderString("Content-Type");
				if (contentType != null) {
					if (contentType.toUpperCase().contains("XML")) {
						context.setReturnedFormatType("XML");
					}
					else if (contentType.toUpperCase().contains("JSON")) {
						context.setReturnedFormatType("JSON");
					}
					else {
						context.setReturnedFormatType(formatType);
					}
				}
				else {
					context.setReturnedFormatType(formatType);
				}

				if (resourceResponse.getStatus() == (Response.Status.OK.getStatusCode())) {
					try {
						context.getResourceResults().add((new ResourceResponseWrapper(resourceResponse)));
					}
					catch (Exception e) {
						log.fine(e.getMessage());
						FacesContext.getCurrentInstance().addMessage("tabView:interactionsTabView:fhirReadForm",
								new FacesMessage(FacesMessage.SEVERITY_ERROR, "Resource parsing failed! Please check the client logs.", "Resource parsing failed! Please check the client logs."));
						e.printStackTrace();
					}

				}
				else if (resourceResponse.getStatus() == (Response.Status.NOT_MODIFIED.getStatusCode())) {
					log.fine(Integer.toString(resourceResponse.getStatus()));
					FacesContext.getCurrentInstance().addMessage("tabView:interactionsTabView:fhirReadForm", new FacesMessage(FacesMessage.SEVERITY_INFO, "RESOURCE " + resourceId + " NOT MODIFIED", "RESOURCE " + resourceId + " NOT MODIFIED"));
				}
				else {
					log.fine(Integer.toString(resourceResponse.getStatus()));
					FacesContext.getCurrentInstance().addMessage("tabView:interactionsTabView:fhirReadForm", new FacesMessage(FacesMessage.SEVERITY_INFO, "Response " + resourceResponse.getStatus() + " - No Resource found matching ID " + resourceId, "Response " + resourceResponse.getStatus() + " - No Resource found matching ID " + resourceId));
				}
			}
		}
		catch (Exception e) {
			log.fine(e.getMessage());
			FacesContext.getCurrentInstance().addMessage("tabView:interactionsTabView:fhirReadForm", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error reading resource! Please check the client logs.", "Error reading resource! Please check the client logs."));
			e.printStackTrace();
		}

		log.fine("[END] ApplicationController.fhirRead()");
	}

	/**
	 * Perform a FHIR read for the supplied resource id
	 *
	 * @return
	 */
	public void fhirVRead(ActionEvent event) {
		log.fine("[START] ApplicationController.fhirVRead()");

		try {
			log.fine("BasePath for FHIR vread: " + context.getSelectedServerURL());
			context.setResourceResults(null);
			String resourceId = context.getResourceId();
			String versionId = context.getResourceVersion();
			String _format = context.get_format();
			String _summary = context.get_summary();
			Response resourceResponse = null;
			context.setResourceResults(new ArrayList<ResourceResponseWrapper>());
			String formatType = context.getSelectedFormatType();

			resourceResponse = context.getResourceRESTClient().vread(resourceId, versionId, context.getSelectedServerURL(), context.getSelectedResourceType(), formatType, _format, _summary, null);

			if (resourceResponse != null) {
				String contentType = resourceResponse.getHeaderString("Content-Type");
				if (contentType != null) {
					if (contentType.toUpperCase().contains("XML")) {
						context.setReturnedFormatType("XML");
					}
					else if (contentType.toUpperCase().contains("JSON")) {
						context.setReturnedFormatType("JSON");
					}
					else {
						context.setReturnedFormatType(formatType);
					}
				}
				else {
					context.setReturnedFormatType(formatType);
				}

				if (resourceResponse.getStatus() == (Response.Status.OK.getStatusCode())) {
					try {
						context.getResourceResults().add((new ResourceResponseWrapper(resourceResponse)));
					}
					catch (Exception e) {
						log.fine(e.getMessage());
						FacesContext.getCurrentInstance().addMessage("tabView:interactionsTabView:fhirVReadForm",
								new FacesMessage(FacesMessage.SEVERITY_ERROR, "Resource parsing failed! Please check the client logs.", "Resource parsing failed! Please check the client logs."));
						e.printStackTrace();
					}

				}
				else if (resourceResponse.getStatus() == (Response.Status.NOT_MODIFIED.getStatusCode())) {
					log.fine(Integer.toString(resourceResponse.getStatus()));
					FacesContext.getCurrentInstance().addMessage("tabView:interactionsTabView:fhirVReadForm", new FacesMessage(FacesMessage.SEVERITY_INFO, "RESOURCE " + resourceId + " NOT MODIFIED", "RESOURCE " + resourceId + " NOT MODIFIED"));
				}
				else {
					log.fine(Integer.toString(resourceResponse.getStatus()));
					FacesContext.getCurrentInstance().addMessage("tabView:interactionsTabView:fhirVReadForm", new FacesMessage(FacesMessage.SEVERITY_INFO, "Response " + resourceResponse.getStatus() + " - No Resource found matching ID " + resourceId, "Response " + resourceResponse.getStatus() + " - No Resource found matching ID " + resourceId));
				}
			}
		}
		catch (Exception e) {
			log.fine(e.getMessage());
			FacesContext.getCurrentInstance().addMessage("tabView:interactionsTabView:fhirVReadForm", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error reading resource! Please check the client logs.", "Error reading resource! Please check the client logs."));
			e.printStackTrace();
		}

		log.fine("[END] ApplicationController.fhirVRead()");
	}

	/**
	 * Performs a FHIR history read for specified patient record in List of patient results, Updates messages for the UI
	 * form whose id is supplied
	 *
	 * @param event
	 */
	public void fhirHistory(ActionEvent event) {
		log.fine("[START] ApplicationController.fhirHistory()");
		log.fine("BasePath for FHIR history: " + context.getSelectedServerURL());

		String formatType = context.getSelectedFormatType();

		String _format = context.get_format();
		String _count = context.get_count();
		String _since = context.get_since();

		if (!StringUtils.isNullOrEmpty(_since)) {
			try {
				utcDateUtil.parseXMLDate(_since);
				log.fine("fhirHistory _since = " + _since);
			}
			catch (Exception e) {
				log.severe("Exception parsing _since parameter to UTC Date! " + e.getMessage());
				FacesContext.getCurrentInstance().addMessage("tabView:interactionsTabView:historyForm",
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "_since parameter is not a UTC Date! Example '2015-12-09T15:53:18Z'.", "_since parameter is not a UTC Date! Example '2015-12-09T15:53:18Z'."));
				return;
			}
		}

		Response response = null;
		ResourceResponseWrapper wrapper = null;
		context.setResourceResults(new ArrayList<ResourceResponseWrapper>());

		try {
			if (formatType.equals("XML")) {
				response = context.getResourceRESTClient().history(context.getResourceId(), context.getSelectedServerURL(), context.getSelectedResourceType(), Constants.FHIR_XML_CONTENT, _format, _count, _since, null);
			}
			else {
				response = context.getResourceRESTClient().history(context.getResourceId(), context.getSelectedServerURL(), context.getSelectedResourceType(), Constants.FHIR_JSON_CONTENT, _format, _count, _since, null);
			}
		}
		catch (NumberFormatException e) {
			FacesContext.getCurrentInstance().addMessage("tabView:interactionsTabView:historyForm",
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Number format error getting resource history! Please check the client logs.", "Error reading resource! Please check the client logs."));
			e.printStackTrace();
		}
		catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage("tabView:interactionsTabView:historyForm",
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error getting resource history! Please check the client logs.", "Error reading resource! Please check the client logs."));
			e.printStackTrace();
		}

		if (response != null) {

			String contentType = response.getHeaderString("Content-Type");
			if (contentType != null) {
				if (contentType.toUpperCase().contains("XML")) {
					context.setReturnedFormatType("XML");
				}
				else if (contentType.toUpperCase().contains("JSON")) {
					context.setReturnedFormatType("JSON");
				}
				else {
					context.setReturnedFormatType(formatType);
				}
			}
			else {
				context.setReturnedFormatType(formatType);
			}

			if (response.getStatus() == Response.Status.OK.getStatusCode()) {
				try {
					wrapper = new ResourceResponseWrapper(response);
					context.getResourceResults().add(wrapper);

					// Check for Bundle Links
					BundleWrapper historyBundleWrapper = wrapper.getBundle();
					Bundle historyBundle = historyBundleWrapper.getBundle();

					if (historyBundle.hasLink()) {
						context.getPageReference().clear();

						for (BundleLinkComponent bundleLink : historyBundle.getLink()) {

							if (bundleLink.hasRelation()) {

								if (bundleLink.getRelation().equals("first")) {
									LabelKeyValueBean firstPage = new LabelKeyValueBean("First", "first", bundleLink.getUrl());
									context.getPageReference().add(firstPage);
								}

								if (bundleLink.getRelation().equals("next")) {
									LabelKeyValueBean nextPage = new LabelKeyValueBean("Next", "next", bundleLink.getUrl());
									context.getPageReference().add(nextPage);
								}

								if (bundleLink.getRelation().equals("previous")) {
									LabelKeyValueBean prevPage = new LabelKeyValueBean("Prev", "previous", bundleLink.getUrl());
									context.getPageReference().add(prevPage);
								}

								if (bundleLink.getRelation().equals("last")) {
									LabelKeyValueBean lastPage = new LabelKeyValueBean("Last", "last", bundleLink.getUrl());
									context.getPageReference().add(lastPage);
								}
							}
						}
					}

					FacesContext.getCurrentInstance().addMessage(
							"tabView:interactionsTabView:historyForm",
							new FacesMessage(FacesMessage.SEVERITY_INFO, "History for Resource with ID: " + context.getResourceId() + " successfully returned.", ""));
				}
				catch (Exception e) {
					FacesContext.getCurrentInstance().addMessage("tabView:interactionsTabView:historyForm",
							new FacesMessage(FacesMessage.SEVERITY_ERROR, "Resource parsing failed! Please check the client logs.", ""));
					e.printStackTrace();
				}
			}
			else {
				FacesContext.getCurrentInstance().addMessage("tabView:interactionsTabView:historyForm",
						new FacesMessage(FacesMessage.SEVERITY_WARN, "Response code: " + Integer.toString(response.getStatus()) + " from server not ok.", ""));
			}
		}

		log.fine("[END] ApplicationController.fhirHistory()");
	}

	/**
	 * Performs a FHIR history read for a specific page from a previous history result Bundle.
	 *
	 * @param historyPageUrl
	 */
	public void fhirHistoryPage(String historyPageUrl) {
		log.fine("[START] ApplicationController.fhirHistoryPage()");
		log.fine("BasePath for FHIR delete: " + context.getSelectedServerURL());

		String formatType = context.getSelectedFormatType();

		Response response = null;
		ResourceResponseWrapper wrapper = null;
		context.setResourceResults(new ArrayList<ResourceResponseWrapper>());

		try {
			if (formatType.equals("XML")) {
				response = context.getResourceRESTClient().historyPage(historyPageUrl, Constants.FHIR_XML_CONTENT, null);
			}
			else {
				response = context.getResourceRESTClient().historyPage(historyPageUrl, Constants.FHIR_JSON_CONTENT, null);
			}
		}
		catch (NumberFormatException e) {
			FacesContext.getCurrentInstance().addMessage("tabView:interactionsTabView:historyForm",
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Number format error getting resource history! Please check the client logs.", "Error reading resource! Please check the client logs."));
			e.printStackTrace();
		}
		catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage("tabView:interactionsTabView:historyForm",
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error getting resource history! Please check the client logs.", "Error reading resource! Please check the client logs."));
			e.printStackTrace();
		}

		if (response != null) {

			String contentType = response.getHeaderString("Content-Type");
			if (contentType != null) {
				if (contentType.toUpperCase().contains("XML")) {
					context.setReturnedFormatType("XML");
				}
				else if (contentType.toUpperCase().contains("JSON")) {
					context.setReturnedFormatType("JSON");
				}
				else {
					context.setReturnedFormatType(formatType);
				}
			}
			else {
				context.setReturnedFormatType(formatType);
			}

			if (response.getStatus() == Response.Status.OK.getStatusCode()) {
				try {
					wrapper = new ResourceResponseWrapper(response);
					context.getResourceResults().add(wrapper);

					// Check for Bundle Links
					BundleWrapper historyBundleWrapper = wrapper.getBundle();
					Bundle historyBundle = historyBundleWrapper.getBundle();

					if (historyBundle.hasLink()) {
						context.getPageReference().clear();

						for (BundleLinkComponent bundleLink : historyBundle.getLink()) {

							if (bundleLink.hasRelation()) {

								if (bundleLink.getRelation().equals("first")) {
									LabelKeyValueBean firstPage = new LabelKeyValueBean("First", "first", bundleLink.getUrl());
									context.getPageReference().add(firstPage);
								}

								if (bundleLink.getRelation().equals("next")) {
									LabelKeyValueBean nextPage = new LabelKeyValueBean("Next", "next", bundleLink.getUrl());
									context.getPageReference().add(nextPage);
								}

								if (bundleLink.getRelation().equals("previous")) {
									LabelKeyValueBean prevPage = new LabelKeyValueBean("Prev", "previous", bundleLink.getUrl());
									context.getPageReference().add(prevPage);
								}

								if (bundleLink.getRelation().equals("last")) {
									LabelKeyValueBean lastPage = new LabelKeyValueBean("Last", "last", bundleLink.getUrl());
									context.getPageReference().add(lastPage);
								}
							}
						}
					}

					FacesContext.getCurrentInstance().addMessage(
							"tabView:interactionsTabView:historyForm",
							new FacesMessage(FacesMessage.SEVERITY_INFO, "History for Resource with ID: " + context.getResourceId() + " successfully returned.", ""));
				}
				catch (Exception e) {
					FacesContext.getCurrentInstance().addMessage("tabView:interactionsTabView:historyForm",
							new FacesMessage(FacesMessage.SEVERITY_ERROR, "Resource parsing failed! Please check the client logs.", ""));
					e.printStackTrace();
				}
			}
			else {
				FacesContext.getCurrentInstance().addMessage("tabView:interactionsTabView:historyForm",
						new FacesMessage(FacesMessage.SEVERITY_WARN, "Response code: " + Integer.toString(response.getStatus()) + " from server not ok.", ""));
			}
		}

		log.fine("[END] ApplicationController.fhirHistoryPage()");
	}

	/**
	 * Perform a FHIR search operation based on the entered resource type (optional) and search parameter values
	 *
	 * @param event
	 */
	public void fhirSearch(ActionEvent event) {
		log.fine("[START] ApplicationController.fhirSearch()");
		log.fine("BasePath for FHIR search: " + context.getSelectedServerURL());
		log.fine("Search Criteria: ");

		Map<String, String> criteriaToSend = new HashMap<String, String>();

		for (LabelKeyValueBean lkvb : context.getResourceCriteria()) {
			if (!lkvb.getValue().isEmpty()) {
				log.fine(lkvb.getKey() + " = " + lkvb.getValue());
				criteriaToSend.put(lkvb.getKey(), lkvb.getValue());
			}
		}

		String _format = context.get_format();
		String _summary = context.get_summary();
		String formatType = context.getSelectedFormatType();
		String httpOperation = context.getSelectedHttpOperation();

		Response response = null;
		ResourceResponseWrapper wrapper = null;
		context.setResourceResults(new ArrayList<ResourceResponseWrapper>());

		try {
			if (formatType.equals("XML")) {
				if (httpOperation.equals("GET")) {
					response = context.getResourceRESTClient().searchGet(criteriaToSend, context.getSelectedServerURL(), context.getSelectedResourceType(), Constants.FHIR_XML_CONTENT, _format, _summary, null);
				}
				else {
					response = context.getResourceRESTClient().searchPost(criteriaToSend, context.getSelectedServerURL(), context.getSelectedResourceType(), Constants.FHIR_XML_CONTENT, _format, _summary, null);
				}
			}
			else {
				if (httpOperation.equals("GET")) {
					response = context.getResourceRESTClient().searchGet(criteriaToSend, context.getSelectedServerURL(), context.getSelectedResourceType(), Constants.FHIR_JSON_CONTENT, _format, _summary, null);
				}
				else {
					response = context.getResourceRESTClient().searchPost(criteriaToSend, context.getSelectedServerURL(), context.getSelectedResourceType(), Constants.FHIR_JSON_CONTENT, _format, _summary, null);
				}
			}
		}
		catch (Exception e1) {
			log.fine(e1.getMessage());
			e1.printStackTrace();
		}
		if (response != null) {
			String contentType = response.getHeaderString("Content-Type");
			if (contentType != null) {
				if (contentType.toUpperCase().contains("XML")) {
					context.setReturnedFormatType("XML");
				}
				else if (contentType.toUpperCase().contains("JSON")) {
					context.setReturnedFormatType("JSON");
				}
				else {
					context.setReturnedFormatType(formatType);
				}
			}
			else {
				context.setReturnedFormatType(formatType);
			}

			if (response.getStatus() == Response.Status.OK.getStatusCode()) {
				try {
					wrapper = new ResourceResponseWrapper(response);
					context.getResourceResults().add(wrapper);

					// Check for Bundle Links
					BundleWrapper searchBundleWrapper = wrapper.getBundle();
					Bundle searchBundle = searchBundleWrapper.getBundle();

					if (searchBundle.hasLink()) {
						context.getPageReference().clear();

						for (BundleLinkComponent bundleLink : searchBundle.getLink()) {

							if (bundleLink.hasRelation()) {

								if (bundleLink.getRelation().equals("first")) {
									LabelKeyValueBean firstPage = new LabelKeyValueBean("First", "first", bundleLink.getUrl());
									context.getPageReference().add(firstPage);
								}

								if (bundleLink.getRelation().equals("next")) {
									LabelKeyValueBean nextPage = new LabelKeyValueBean("Next", "next", bundleLink.getUrl());
									context.getPageReference().add(nextPage);
								}

								if (bundleLink.getRelation().equals("previous")) {
									LabelKeyValueBean prevPage = new LabelKeyValueBean("Prev", "previous", bundleLink.getUrl());
									context.getPageReference().add(prevPage);
								}

								if (bundleLink.getRelation().equals("last")) {
									LabelKeyValueBean lastPage = new LabelKeyValueBean("Last", "last", bundleLink.getUrl());
									context.getPageReference().add(lastPage);
								}
							}
						}
					}

					FacesContext.getCurrentInstance().addMessage("tabView:interactionsTabView:fhirSearchForm", new FacesMessage(FacesMessage.SEVERITY_INFO, "Search successfully executed.", "Search successfully excuted."));
				}
				catch (Exception e1) {
					log.fine(e1.getMessage());
					FacesContext.getCurrentInstance().addMessage("tabView:interactionsTabView:fhirSearchForm",
							new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error searching resource(s)! Please check the client logs.", "Error reading resource! Please check the client logs."));
					e1.printStackTrace();
				}

			}
			else {
				FacesContext.getCurrentInstance().addMessage("tabView:interactionsTabView:fhirSearchForm", new FacesMessage(FacesMessage.SEVERITY_INFO, "No Resource Results found based on supplied criteria", "No Resource Results found based on supplied criteria"));
			}
		}

		log.fine("[END] ApplicationController.fhirSearch()");
	}

	/**
	 * Performs a FHIR search read for a specific page from a previous history result Bundle.
	 *
	 * @param searchPageUrl
	 */
	public void fhirSearchPage(String searchPageUrl) {
		log.fine("[START] ApplicationController.fhirSearchPage()");
		log.fine("BasePath for FHIR delete: " + context.getSelectedServerURL());

		String formatType = context.getSelectedFormatType();

		Response response = null;
		ResourceResponseWrapper wrapper = null;
		context.setResourceResults(new ArrayList<ResourceResponseWrapper>());

		try {
			if (formatType.equals("XML")) {
				response = context.getResourceRESTClient().searchPage(searchPageUrl, Constants.FHIR_XML_CONTENT, null);
			}
			else {
				response = context.getResourceRESTClient().searchPage(searchPageUrl, Constants.FHIR_JSON_CONTENT, null);
			}
		}
		catch (NumberFormatException e) {
			FacesContext.getCurrentInstance().addMessage("tabView:interactionsTabView:fhirSearchForm",
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Number format error getting page resources! Please check the client logs.", "Number format error getting page resources! Please check the client logs."));
			e.printStackTrace();
		}
		catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage("tabView:interactionsTabView:fhirSearchForm",
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error getting page resources! Please check the client logs.", "Error getting page resources! Please check the client logs."));
			e.printStackTrace();
		}

		if (response != null) {

			String contentType = response.getHeaderString("Content-Type");
			if (contentType != null) {
				if (contentType.toUpperCase().contains("XML")) {
					context.setReturnedFormatType("XML");
				}
				else if (contentType.toUpperCase().contains("JSON")) {
					context.setReturnedFormatType("JSON");
				}
				else {
					context.setReturnedFormatType(formatType);
				}
			}
			else {
				context.setReturnedFormatType(formatType);
			}

			if (response.getStatus() == Response.Status.OK.getStatusCode()) {
				try {
					wrapper = new ResourceResponseWrapper(response);
					context.getResourceResults().add(wrapper);

					// Check for Bundle Links
					BundleWrapper searchBundleWrapper = wrapper.getBundle();
					Bundle searchBundle = searchBundleWrapper.getBundle();

					if (searchBundle.hasLink()) {
						context.getPageReference().clear();

						for (BundleLinkComponent bundleLink : searchBundle.getLink()) {

							if (bundleLink.hasRelation()) {

								if (bundleLink.getRelation().equals("first")) {
									LabelKeyValueBean firstPage = new LabelKeyValueBean("First", "first", bundleLink.getUrl());
									context.getPageReference().add(firstPage);
								}

								if (bundleLink.getRelation().equals("next")) {
									LabelKeyValueBean nextPage = new LabelKeyValueBean("Next", "next", bundleLink.getUrl());
									context.getPageReference().add(nextPage);
								}

								if (bundleLink.getRelation().equals("previous")) {
									LabelKeyValueBean prevPage = new LabelKeyValueBean("Prev", "previous", bundleLink.getUrl());
									context.getPageReference().add(prevPage);
								}

								if (bundleLink.getRelation().equals("last")) {
									LabelKeyValueBean lastPage = new LabelKeyValueBean("Last", "last", bundleLink.getUrl());
									context.getPageReference().add(lastPage);
								}
							}
						}
					}

					FacesContext.getCurrentInstance().addMessage("tabView:interactionsTabView:fhirSearchForm", new FacesMessage(FacesMessage.SEVERITY_INFO, "Search successfully executed.", "Search successfully excuted."));
				}
				catch (Exception e1) {
					log.fine(e1.getMessage());
					FacesContext.getCurrentInstance().addMessage("tabView:interactionsTabView:fhirSearchForm",
							new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error searching resource(s)! Please check the client logs.", "Error searching resource(s)! Please check the client logs."));
					e1.printStackTrace();
				}

			}
			else {
				FacesContext.getCurrentInstance().addMessage("tabView:interactionsTabView:fhirSearchForm", new FacesMessage(FacesMessage.SEVERITY_INFO, "No Resource Results found based on supplied criteria", "No Resource Results found based on supplied criteria"));
			}
		}

		log.fine("[END] ApplicationController.fhirSearchPage()");
	}

	/**
	 * Read resource for subsequent operations of update or validate
	 *
	 * @param event
	 */
	public void searchResourceForOperation(ActionEvent event) {
		log.fine("[START] ApplicationController.searchResourceForOperation()");
		context.setCurrentView("update");

		String formatType = context.getSelectedFormatType();

		// fetch resource record
		fhirRead(event);
		String operation = (String) event.getComponent().getAttributes().get("operation");

		if (context.getResourceResults() != null && context.getResourceResults().size() > 0) {

			String resourceString = null;

			if (formatType.equals("XML")) {
				resourceString = context.getResourceResults().get(0).getResourceXML();
			}
			else {
				resourceString = context.getResourceResults().get(0).getResourceJSON();
			}

			if (operation.equals("validate")) {
				Parameters input = new Parameters();
	    		ParametersParameterComponent parameter = new ParametersParameterComponent();
	    		parameter.setName("profile");
	    		StringType profile = new StringType("http://hl7.org/fhir/StructureDefinition/" + context.getSelectedResourceType());
	    		parameter.setValue(profile);
	    		input.addParameter(parameter);
	    		parameter = new ParametersParameterComponent();
	    		parameter.setName("resource");
	    		parameter.setResource(context.getResourceResults().get(0).getResource());
	    		input.addParameter(parameter);

				ByteArrayOutputStream oOp = new ByteArrayOutputStream();

				if (formatType.equals("XML")) {
					XmlParser xmlParser = new XmlParser();
					try {
						xmlParser.setOutputStyle(OutputStyle.PRETTY);
						xmlParser.compose(oOp, input, true);

						resourceString = oOp.toString();
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
				else {
					JsonParser jsonParser = new JsonParser();
					try {
						jsonParser.setOutputStyle(OutputStyle.PRETTY);
						jsonParser.compose(oOp, input);

						resourceString = oOp.toString();
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			context.setResourceString(resourceString);

			context.setResourceResults(new ArrayList<ResourceResponseWrapper>());

		}
		else {
			log.fine("No Resource found matching ID: " + context.getResourceId());
			String form = "tabView:interactionsTabView:" + operation + "Form";
			FacesContext.getCurrentInstance().addMessage(form, new FacesMessage(FacesMessage.SEVERITY_WARN, "No Resource found matching ID: " + context.getResourceId(), ""));
			context.setResourceString(null);
		}

		log.fine("resource id: " + context.getResourceId());
		log.fine("[END] ApplicationController.searchResourceForUpdate()");
	}

	/**
	 * Display resource specific search criteria based on the selected resource type
	 *
	 * @param event
	 */
	public void showSearchCriteria(ActionEvent event) {
		log.fine("[START] ApplicationController.showSearchCriteria()");

		List<LabelKeyValueBean> criteriaList = new ArrayList<LabelKeyValueBean>();

		criteriaList.addAll(ResourceType.getGlobalCriteria());

		String resourceType = context.getSelectedResourceType();
		criteriaList.addAll(ResourceType.getResourceTypeCriteria().get(resourceType));

		context.setResourceCriteria(criteriaList);

		context.setResourceResults(null);

		log.fine("[END] ApplicationController.showSearchCriteria()");
	}

	/**
	 * Update a resource
	 *
	 * @param event
	 */
	public void fhirUpdate(ActionEvent event) {
		log.fine("[START] ApplicationController.fhirUpdate()");
		log.fine("BasePath for FHIR update: " + context.getSelectedServerURL());

		context.setCurrentView("update");

		String formatType = context.getSelectedFormatType();
		String updateQuery = context.getUpdateQuery();
		String ifMatch = context.getIfMatch();
		String prefer = context.getPrefer();
		String _format = context.get_format();
		String resourceString = context.getResourceString();

		ByteArrayInputStream iResource = null;
		Resource resource = null;
		Response response = null;
		ResourceResponseWrapper wrapper = null;

		try {
			if (resourceString.isEmpty()) {
				throw new Exception("No found resource content to update.");
			}

			if (formatType.equals("XML")) {
				// Convert XML contents to Resource
				XmlParser xmlP = new XmlParser();
				int firstValid = resourceString.indexOf("<");
				if (firstValid > 0) {
					resourceString = resourceString.substring(firstValid);
				}
				iResource = new ByteArrayInputStream(resourceString.getBytes());
				resource = xmlP.parse(iResource);

				response = context.getResourceRESTClient().update(context.getResourceId(), resource, context.getSelectedServerURL(), context.getSelectedResourceType(), Constants.FHIR_XML_CONTENT, updateQuery, ifMatch, prefer, _format, null);
			}
			else {
				// Convert JSON contents to Resource
				JsonParser jsonP = new JsonParser();
				int firstValid = resourceString.indexOf("{");
				if (firstValid > 0) {
					resourceString = resourceString.substring(firstValid);
				}

				iResource = new ByteArrayInputStream(resourceString.getBytes());
				resource = jsonP.parse(iResource);

				response = context.getResourceRESTClient().update(context.getResourceId(), resource, context.getSelectedServerURL(), context.getSelectedResourceType(), Constants.FHIR_JSON_CONTENT, updateQuery, ifMatch, prefer, _format, null);
			}

		}
		catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage("tabView:interactionsTabView:updateForm", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error updating resource! Please check the client logs.", "Error reading resource! Please check the client logs."));
			e.printStackTrace();
		}

		if (response != null) {
			String contentType = response.getHeaderString("Content-Type");
			if (contentType != null) {
				if (contentType.toUpperCase().contains("XML")) {
					context.setReturnedFormatType("XML");
				}
				else if (contentType.toUpperCase().contains("JSON")) {
					context.setReturnedFormatType("JSON");
				}
				else {
					context.setReturnedFormatType(formatType);
				}
			}
			else {
				context.setReturnedFormatType(formatType);
			}

			try {
				wrapper = new ResourceResponseWrapper(response);

				if (context.getReturnedFormatType().equals("XML")) {
					context.setResponseString(wrapper.getResourceXML());
				}
				else {
					context.setResponseString(wrapper.getResourceJSON());
				}
			}
			catch (Exception e) {
				FacesContext.getCurrentInstance().addMessage("tabView:interactionsTabView:updateForm",
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "Resource parsing failed! Please check the client logs.", "Resource parsing failed! Please check the client logs."));
				e.printStackTrace();
			}

			if (response.getStatus() == Response.Status.OK.getStatusCode()) {
				FacesContext.getCurrentInstance().addMessage("tabView:interactionsTabView:updateForm",
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Resource with ID: " + context.getResourceId() + " updated.", "Resource with ID: " + context.getResourceId() + " updated."));

			}
			else if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
				FacesContext.getCurrentInstance().addMessage("tabView:interactionsTabView:updateForm",
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Resource with ID: " + context.getResourceId() + " created.", "Resource with ID: " + context.getResourceId() + " created."));

			}
			else {
				FacesContext.getCurrentInstance().addMessage("tabView:interactionsTabView:updateForm",
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Resource with ID: " + context.getResourceId() + " update failed.", "Resource with ID: " + context.getResourceId() + " update failed."));
			}
		}

		context.setResourceString(null);
		context.setResourceId("");

		log.fine("[END] ApplicationController.fhirUpdate()");
	}

	/**
	 * Patch (partial update) a resource
	 *
	 * @param event
	 */
	public void fhirPatch(ActionEvent event) {
		log.fine("[START] ApplicationController.fhirPatch()");
		log.fine("BasePath for FHIR patch: " + context.getSelectedServerURL());

		String patchFormatType = context.getSelectedPatchFormatType();
		String ifMatch = context.getIfMatch();
		String prefer = context.getPrefer();
		String _format = context.get_format();
		String resourceString = context.getResourceString();
		Response response = null;
		ResourceResponseWrapper wrapper = null;

		try {
			if (resourceString.isEmpty()) {
				throw new Exception("No found resource content to update.");
			}

			if (patchFormatType.equals("FHIR Path (JSON)")) {
				response = context.getResourceRESTClient().patch(context.getResourceId(), resourceString, context.getSelectedServerURL(), context.getSelectedResourceType(), Constants.FHIR_PATCH_JSON_CONTENT, ifMatch, prefer, _format, null);
			}
			else if (patchFormatType.equals("FHIR Path (XML)")) {
				response = context.getResourceRESTClient().patch(context.getResourceId(), resourceString, context.getSelectedServerURL(), context.getSelectedResourceType(), Constants.FHIR_PATCH_XML_CONTENT, ifMatch, prefer, _format, null);
			}
			else if (patchFormatType.equals("JSON Patch")) {
				response = context.getResourceRESTClient().patch(context.getResourceId(), resourceString, context.getSelectedServerURL(), context.getSelectedResourceType(), Constants.JSON_PATCH_CONTENT, ifMatch, prefer, _format, null);
			}
			else {
				response = context.getResourceRESTClient().patch(context.getResourceId(), resourceString, context.getSelectedServerURL(), context.getSelectedResourceType(), Constants.XML_PATCH_CONTENT, ifMatch, prefer, _format, null);
			}
		}
		catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage("tabView:interactionsTabView:updateForm", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error patching resource! Please check the client logs.", "Error patching resource! Please check the client logs."));
			e.printStackTrace();
		}

		if (response != null) {
			String contentType = response.getHeaderString("Content-Type");
			if (contentType != null) {
				if (contentType.toUpperCase().contains("XML")) {
					context.setReturnedFormatType("XML");
				}
				else if (contentType.toUpperCase().contains("JSON")) {
					context.setReturnedFormatType("JSON");
				}
				else {
					if (patchFormatType.toUpperCase().contains("XML")) {
						context.setReturnedFormatType("XML");
					}
					else if (patchFormatType.toUpperCase().contains("JSON")) {
						context.setReturnedFormatType("JSON");
					}
				}
			}
			else {
				if (patchFormatType.toUpperCase().contains("XML")) {
					context.setReturnedFormatType("XML");
				}
				else if (patchFormatType.toUpperCase().contains("JSON")) {
					context.setReturnedFormatType("JSON");
				}
			}

			try {
				wrapper = new ResourceResponseWrapper(response);

				if (context.getReturnedFormatType().equals("XML")) {
					context.setResponseString(wrapper.getResourceXML());
				}
				else {
					context.setResponseString(wrapper.getResourceJSON());
				}
			}
			catch (Exception e) {
				FacesContext.getCurrentInstance().addMessage("tabView:interactionsTabView:patchForm",
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "Resource parsing failed! Please check the client logs.", "Resource parsing failed! Please check the client logs."));
				e.printStackTrace();
			}

			if (response.getStatus() == Response.Status.OK.getStatusCode()) {
				FacesContext.getCurrentInstance().addMessage("tabView:interactionsTabView:patchForm",
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Resource with ID: " + context.getResourceId() + " patched.", "Resource with ID: " + context.getResourceId() + " patched."));

			}
			else {
				FacesContext.getCurrentInstance().addMessage("tabView:interactionsTabView:patchForm",
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Resource with ID: " + context.getResourceId() + " patch failed.", "Resource with ID: " + context.getResourceId() + " patch failed."));
			}
		}

		context.setResourceString(null);
		context.setResourceId("");

		log.fine("[END] ApplicationController.fhirPatch()");
	}

	/**
	 * Deletes a resource who's ID is supplied from the view
	 *
	 * @param id
	 * @param form
	 */
	public void fhirDelete(String id, String form) {
		log.fine("[START] ApplicationController.delete()");

		String formId = "tabView:interactionsTabView:" + form;
		if (form == null || form == "" || form.length() < 1) {
			return;
		}

		try {
			log.fine("BasePath for FHIR delete: " + context.getSelectedServerURL());
			context.setCurrentView("delete");
			String formatType = context.getSelectedFormatType();

			Response response = context.getResourceRESTClient().delete(id, context.getSelectedServerURL(), context.getSelectedResourceType(), formatType, null);

			if (response != null) {
				if ((response.getStatus() == Response.Status.OK.getStatusCode()) || (response.getStatus() == Response.Status.GONE.getStatusCode()) || (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode())) {
					FacesContext.getCurrentInstance().addMessage(formId, new FacesMessage(FacesMessage.SEVERITY_INFO, "Resource with ID: " + id + " deleted.", "Resource with ID: " + id + " deleted."));
					context.setResourceResults(new ArrayList<ResourceResponseWrapper>());
				}
				else {
					FacesContext.getCurrentInstance().addMessage(formId, new FacesMessage(FacesMessage.SEVERITY_INFO, "Response " + response.getStatus() + " - Resource with ID: " + id + " deletion failed.", "Response " + response.getStatus() + " - Resource with ID: " + id + " deletion failed."));
				}
			}
		}
		catch (NumberFormatException e) {
			log.fine(e.getMessage());
			FacesContext.getCurrentInstance().addMessage(formId, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Number format error deleting resource! Please check the client logs.", "Error reading resource! Please check the client logs."));
			e.printStackTrace();
		}
		catch (Exception e) {
			log.fine(e.getMessage());
			FacesContext.getCurrentInstance().addMessage(formId, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error deleting resource! Please check the client logs.", "Error reading resource! Please check the client logs."));
			e.printStackTrace();
		}

		log.fine("[END] ApplicationController.delete()");
	}

	/**
	 * Execute the $validate operation
	 *
	 * @param event
	 */
	public void fhirValidate(ActionEvent event) {
		log.fine("[START] ApplicationController.fhirValidate()");
		log.fine("BasePath for FHIR validate: " + context.getSelectedServerURL());

		String formatType = context.getSelectedFormatType();
		String resourceString = context.getResourceString();

		ByteArrayInputStream iResource = null;
		Resource resource = null;
		Response response = null;
		ResourceResponseWrapper wrapper = null;
		String validateExceptionOutcomeString = null;

		try {
			if (resourceString.isEmpty()) {
				throw new Exception("No content provided for $validate operation.");
			}

			if (formatType.equals("XML")) {
				// Convert XML contents to Resource
				XmlParser xmlP = new XmlParser();
				int firstValid = resourceString.indexOf("<");
				if (firstValid > 0) {
					resourceString = resourceString.substring(firstValid);
				}
				iResource = new ByteArrayInputStream(resourceString.getBytes());
				resource = xmlP.parse(iResource);
			}
			else {
				// Convert JSON contents to Resource
				JsonParser jsonP = new JsonParser();
				int firstValid = resourceString.indexOf("{");
				if (firstValid > 0) {
					resourceString = resourceString.substring(firstValid);
				}
				iResource = new ByteArrayInputStream(resourceString.getBytes());
				resource = jsonP.parse(iResource);
			}

			if (resource instanceof Parameters) {
				Parameters parameters = (Parameters)resource;

				if (formatType.equals("XML")) {
					response = context.getResourceOperationClient().resourceOperation(parameters, null, context.getSelectedServerURL(), context.getSelectedResourceType(), Constants.FHIR_XML_CONTENT, Constants.FHIR_XML_CONTENT, null, "validate", null, null);
				}
				else {
					response = context.getResourceOperationClient().resourceOperation(parameters, null, context.getSelectedServerURL(), context.getSelectedResourceType(), Constants.FHIR_JSON_CONTENT, Constants.FHIR_JSON_CONTENT, null, "validate", null, null);
				}
			}
			else {
				validateExceptionOutcomeString = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.FATAL, OperationOutcome.IssueType.EXCEPTION, "Validate operation requires input contained in a Parameters resource type.", null, null, formatType.toLowerCase());
				FacesContext.getCurrentInstance().addMessage("tabView:operationsTabView:validateForm", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validate operation requires input contained in a Parameters resource type.", "Validate operation requires input contained in a Parameters resource type."));
			}
		}
		catch (NumberFormatException e) {
			validateExceptionOutcomeString = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.FATAL, OperationOutcome.IssueType.EXCEPTION, e.getMessage(), null, null, formatType.toLowerCase());
			FacesContext.getCurrentInstance().addMessage("tabView:operationsTabView:validateForm",
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Number format error validating resource request! Please check the client logs.", "Error reading resource! Please check the client logs."));
			e.printStackTrace();
		}
		catch (Exception e) {
			validateExceptionOutcomeString = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.FATAL, OperationOutcome.IssueType.EXCEPTION, e.getMessage(), null, null, formatType.toLowerCase());
			FacesContext.getCurrentInstance().addMessage("tabView:operationsTabView:validateForm",
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error validating resource request! Please check the client logs.", "Error reading resource! Please check the client logs."));
			e.printStackTrace();
		}

		if (response != null) {
			String contentType = response.getHeaderString("Content-Type");
			if (contentType != null) {
				if (contentType.toUpperCase().contains("XML")) {
					context.setReturnedFormatType("XML");
				}
				else if (contentType.toUpperCase().contains("JSON")) {
					context.setReturnedFormatType("JSON");
				}
				else {
					context.setReturnedFormatType(formatType);
				}
			}
			else {
				context.setReturnedFormatType(formatType);
			}

			try {
				wrapper = new ResourceResponseWrapper(response);

				Resource responseResource = wrapper.getResource();
				if (responseResource instanceof OperationOutcome) {
					context.setValidateOperationOutcome((OperationOutcome) responseResource);
				}

				if (formatType.equals("XML")) {
					context.setResponseString(wrapper.getResourceXML());
				}
				else {
					context.setResponseString(wrapper.getResourceJSON());
				}

				FacesContext.getCurrentInstance().addMessage("tabView:operationsTabView:validateForm", new FacesMessage(FacesMessage.SEVERITY_INFO, "Resource validate complete.", "Resource validate complete."));
			}
			catch (Exception e) {
				FacesContext.getCurrentInstance().addMessage("tabView:operationsTabView:validateForm",
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "Resource parsing failed! Please check the client logs.", "Resource parsing failed! Please check the client logs."));
				e.printStackTrace();
			}
		}
		else if (validateExceptionOutcomeString != null) {
			context.setReturnedFormatType(formatType);
			context.setResponseString(validateExceptionOutcomeString);

			// Exception caught parsing input parameters; generate an OperationOutcome for the display
			try {
				if (formatType.equals("XML")) {
					// Convert XML contents to Resource
					XmlParser xmlP = new XmlParser();
					int firstValid = validateExceptionOutcomeString.indexOf("<");
					if (firstValid > 0) {
						validateExceptionOutcomeString = validateExceptionOutcomeString.substring(firstValid);
					}
					iResource = new ByteArrayInputStream(validateExceptionOutcomeString.getBytes());
					resource = xmlP.parse(iResource);
				}
				else {
					// Convert JSON contents to Resource
					JsonParser jsonP = new JsonParser();
					int firstValid = validateExceptionOutcomeString.indexOf("{");
					if (firstValid > 0) {
						validateExceptionOutcomeString = validateExceptionOutcomeString.substring(firstValid);
					}
					iResource = new ByteArrayInputStream(validateExceptionOutcomeString.getBytes());
					resource = jsonP.parse(iResource);
				}

				if (resource instanceof OperationOutcome) {
					context.setValidateOperationOutcome((OperationOutcome) resource);
				}

				FacesContext.getCurrentInstance().addMessage("tabView:operationsTabView:validateForm", new FacesMessage(FacesMessage.SEVERITY_INFO, "Resource validate operation errors found.", "Resource validate operation errors found."));
			}
			catch (Exception e) {
				FacesContext.getCurrentInstance().addMessage("tabView:operationsTabView:validateForm",
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error reporting validate operations errors! Please check the client logs.", "Error reporting validate operations errors! Please check the client logs."));
				e.printStackTrace();
			}
		}
		else {
			FacesContext.getCurrentInstance().addMessage("tabView:operationsTabView:validateForm", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validate operation did not report any results.", "Validate operation did not report any results."));
		}

		log.fine("[END] ApplicationController.fhirValidate()");
	}

	/**
	 * Perform a FHIR metadata against the specified server
	 *
	 * @return
	 */
	public void fhirMetadata(ActionEvent event) {
		log.fine("[START] ApplicationController.fhirMetadata()");
		log.fine("BasePath for FHIR metadata " + context.getSelectedServerURL());

		String formatType = context.getSelectedFormatType();

		Response response = null;
		context.setResourceResults(new ArrayList<ResourceResponseWrapper>());

		try {
			if (formatType.equals("XML")) {
				response = context.getConformanceResourceRESTClient().metadata(context.getSelectedServerURL(), Constants.FHIR_XML_CONTENT);
			}
			else {
				response = context.getConformanceResourceRESTClient().metadata(context.getSelectedServerURL(), Constants.FHIR_JSON_CONTENT);
			}
		}
		catch (Exception e) {
			log.fine(e.getMessage());
			FacesContext.getCurrentInstance().addMessage("tabView:interactionsTabView:fhirMetadataForm",
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error executing metadata request! " + e.getMessage(), ""));
			e.printStackTrace();
		}

		if (response != null) {
			String contentType = response.getHeaderString("Content-Type");
			if (contentType != null) {
				if (contentType.toUpperCase().contains("XML")) {
					context.setReturnedFormatType("XML");
				}
				else if (contentType.toUpperCase().contains("JSON")) {
					context.setReturnedFormatType("JSON");
				}
				else {
					context.setReturnedFormatType(formatType);
				}
			}
			else {
				context.setReturnedFormatType(formatType);
			}

			if (response.getStatus() == Response.Status.OK.getStatusCode()) {
				try {
					context.getResourceResults().add(new ResourceResponseWrapper(response));
				}
				catch (Exception e) {
					FacesContext.getCurrentInstance().addMessage("tabView:interactionsTabView:fhirMetadataForm",
							new FacesMessage(FacesMessage.SEVERITY_ERROR, "CapabilityStatement parsing failed! " + e.getMessage(), ""));
					e.printStackTrace();
				}

			}
			else {
				FacesContext.getCurrentInstance().addMessage("tabView:interactionsTabView:fhirMetadataForm",
						new FacesMessage(FacesMessage.SEVERITY_INFO, "CapabilityStatement retrieval failed; response [" + response.getStatus() + "].", ""));
			}
		}

		log.fine("[END] ApplicationController.fhirMetadata()");
	}

	/**
	 * Execute the FHIR metadata operation and display the response in a new HTML page
	 * @param event
	 */
	public void fhirMetadataNewPage(ActionEvent event) {
		log.fine("[START] ApplicationController.fhirMetadataNewPage");
		log.fine("BasePath for FHIR metadata (new page) " + context.getSelectedServerURL());

		String formatType = context.getSelectedFormatType();

		String conformanceUrl = context.getSelectedServerURL() + "/metadata";

		try {
			FacesContext facesContext = FacesContext.getCurrentInstance();
			ExternalContext externalContext = facesContext.getExternalContext();
			HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();

			if (formatType.equals("XML")) {
				response.setHeader("Accept", Constants.FHIR_XML_CONTENT);
			}
			else {
				response.setHeader("Accept", Constants.FHIR_JSON_CONTENT);
			}

			externalContext.redirect(conformanceUrl);
			facesContext.responseComplete();
		}
		catch (IOException e) {
			log.severe(e.getMessage());
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error accessing server's metadata endpoint! " + e.getMessage(), ""));
		}

		log.fine("[END] ApplicationController.fhirMetadataNewPage");
	}

	/**
	 * Execute the $convert-format operation
	 *
	 * @param event
	 */
	public void fhirConvertFormat(ActionEvent event) {
		log.fine("[START] ApplicationController.fhirConvertFormat()");
		log.fine("BasePath for FHIR convert: " + context.getSelectedServerURL());

		String convertFromFormatType = context.getConvertFromFormatType();
		String convertToFormatType = context.getConvertToFormatType();
		String resourceString = context.getResourceString();
		String acceptFormatType = null;
		String contentTypeFormatType = null;

		ByteArrayInputStream iResource = null;
		Resource resource = null;
		Response response = null;
		ResourceResponseWrapper wrapper = null;
		String convertFormatExceptionOutcomeString = null;

		try {
			if (resourceString.isEmpty() || resourceString == null) {
				throw new Exception("No content provided for $convert operation.");
			}

			if (convertFromFormatType.equals("XML")) {
				contentTypeFormatType = Constants.FHIR_XML_CONTENT;
				// Convert XML contents to Resource
				XmlParser xmlP = new XmlParser();
				int firstValid = resourceString.indexOf("<");
				if (firstValid > 0) {
					resourceString = resourceString.substring(firstValid);
				}
				iResource = new ByteArrayInputStream(resourceString.getBytes());
				resource = xmlP.parse(iResource);
			}
			else {
				contentTypeFormatType = Constants.FHIR_JSON_CONTENT;
				// Convert JSON contents to Resource
				JsonParser jsonP = new JsonParser();
				int firstValid = resourceString.indexOf("{");
				if (firstValid > 0) {
					resourceString = resourceString.substring(firstValid);
				}
				iResource = new ByteArrayInputStream(resourceString.getBytes());
				resource = jsonP.parse(iResource);
			}

			if (convertToFormatType.equals("XML")) {
				acceptFormatType = Constants.FHIR_XML_CONTENT;
			}
			else {
				acceptFormatType = Constants.FHIR_JSON_CONTENT;
			}

			response = context.getResourceOperationClient().resourceOperation(null, resourceString, context.getSelectedServerURL(), null, acceptFormatType, contentTypeFormatType, null, "convert", null, null);
		}
		catch (Exception e) {
			convertFormatExceptionOutcomeString = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.FATAL, OperationOutcome.IssueType.EXCEPTION, e.getMessage(), null, null, convertToFormatType.toLowerCase());
			FacesContext.getCurrentInstance().addMessage("tabView:operationsTabView:convertFormatForm",
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error parsing convert-format resource request body! Please check the client logs.", "Error parsing convert-format resource request body! Please check the client logs."));
			e.printStackTrace();
		}

		if (response != null) {
			context.setConvertFormatOperationOutcome(null);

			String contentType = response.getHeaderString("Content-Type");
			if (contentType != null) {
				if (contentType.toUpperCase().contains("XML")) {
					context.setReturnedFormatType("XML");
				}
				else if (contentType.toUpperCase().contains("JSON")) {
					context.setReturnedFormatType("JSON");
				}
				else {
					context.setReturnedFormatType(convertToFormatType);
				}
			}
			else {
				context.setReturnedFormatType(convertToFormatType);
			}

			try {
				wrapper = new ResourceResponseWrapper(response);

				Resource responseResource = wrapper.getResource();
				if (responseResource instanceof OperationOutcome) {
					context.setConvertFormatOperationOutcome((OperationOutcome) responseResource);
				}

				if (convertToFormatType.equals("XML")) {
					context.setResponseString(wrapper.getResourceXML());
				}
				else {
					context.setResponseString(wrapper.getResourceJSON());
				}

				FacesContext.getCurrentInstance().addMessage("tabView:operationsTabView:convertFormatForm", new FacesMessage(FacesMessage.SEVERITY_INFO, "Convert format complete.", "Convert format complete."));
			}
			catch (Exception e) {
				FacesContext.getCurrentInstance().addMessage("tabView:operationsTabView:convertFormatForm",
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "Resource parsing failed! Please check the client logs.", "Resource parsing failed! Please check the client logs."));
				e.printStackTrace();
			}
		}
		else if (convertFormatExceptionOutcomeString != null) {
			context.setResponseString(null);
			context.setReturnedFormatType(convertToFormatType);
			context.setResponseString(convertFormatExceptionOutcomeString);

			// Exception caught parsing input parameters; generate an OperationOutcome for the display
			try {
				if (convertToFormatType.equals("XML")) {
					// Convert XML contents to Resource
					XmlParser xmlP = new XmlParser();
					int firstValid = convertFormatExceptionOutcomeString.indexOf("<");
					if (firstValid > 0) {
						convertFormatExceptionOutcomeString = convertFormatExceptionOutcomeString.substring(firstValid);
					}
					iResource = new ByteArrayInputStream(convertFormatExceptionOutcomeString.getBytes());
					resource = xmlP.parse(iResource);
				}
				else {
					// Convert JSON contents to Resource
					JsonParser jsonP = new JsonParser();
					int firstValid = convertFormatExceptionOutcomeString.indexOf("{");
					if (firstValid > 0) {
						convertFormatExceptionOutcomeString = convertFormatExceptionOutcomeString.substring(firstValid);
					}
					iResource = new ByteArrayInputStream(convertFormatExceptionOutcomeString.getBytes());
					resource = jsonP.parse(iResource);
				}

				if (resource instanceof OperationOutcome) {
					context.setConvertFormatOperationOutcome((OperationOutcome) resource);
					context.setResponseString(convertFormatExceptionOutcomeString);
				}

				FacesContext.getCurrentInstance().addMessage("tabView:operationsTabView:convertFormatForm", new FacesMessage(FacesMessage.SEVERITY_INFO, "Convert format operation errors found.", "Convert format operation errors found."));
			}
			catch (Exception e) {
				FacesContext.getCurrentInstance().addMessage("tabView:operationsTabView:convertFormatForm",
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error reporting convert format operations errors! Please check the client logs.", "Error reporting convert format operations errors! Please check the client logs."));
				e.printStackTrace();
			}
		}
		else {
			FacesContext.getCurrentInstance().addMessage("tabView:operationsTabView:convertFormatForm", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Convert format operation did not report any results.", "Convert format operation did not report any results."));
		}

		log.fine("[END] ApplicationController.fhirConvertFormat()");
	}

	/**
	 * Generate the $validate operation Parameters payload template
	 *
	 * @param event
	 */
	public void everythingShowTemplate(ActionEvent event) {
		log.fine("[START] ApplicationController.everythingShowTemplate()");

		StringBuilder sbTemplate = new StringBuilder("");
		sbTemplate.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n");
		sbTemplate.append("<Parameters xmlns=\"http://hl7.org/fhir\">\n");
		sbTemplate.append("  <parameter>\n");
		sbTemplate.append("    <name value=\"start\"/>\n");
		sbTemplate.append("    <valueDate value=\"YYYY-MM-DD\"/>\n");
		sbTemplate.append("  </parameter>\n");
		sbTemplate.append("  <parameter>\n");
		sbTemplate.append("    <name value=\"end\"/>\n");
		sbTemplate.append("    <valueDate value=\"YYYY-MM-DD\"/>\n");
		sbTemplate.append("  </parameter>\n");
		sbTemplate.append("</Parameters>");

		context.setResourceString(sbTemplate.toString());
	}

	/**
	 * Execute the $everything operation
	 *
	 * @param event
	 */
	public void fhirEverything(ActionEvent event) {
		log.fine("[START] ApplicationController.fhirEverything()");
		log.fine("BasePath for FHIR everything: " + context.getSelectedServerURL());

		String formatType = context.getSelectedFormatType();
		String resourceId = context.getResourceId();
		String resourceString = context.getResourceString();

		ByteArrayInputStream iResource = null;
		Resource resource = null;
		Response response = null;
		ResourceResponseWrapper wrapper = null;
		String everythingExceptionOutcomeString = null;

		try {
			if (!resourceString.isEmpty()) {

				if (formatType.equals("XML")) {
					// Convert XML contents to Resource
					XmlParser xmlP = new XmlParser();
					int firstValid = resourceString.indexOf("<");
					if (firstValid > 0) {
						resourceString = resourceString.substring(firstValid);
					}
					iResource = new ByteArrayInputStream(resourceString.getBytes());
					resource = xmlP.parse(iResource);
				}
				else {
					// Convert JSON contents to Resource
					JsonParser jsonP = new JsonParser();
					int firstValid = resourceString.indexOf("{");
					if (firstValid > 0) {
						resourceString = resourceString.substring(firstValid);
					}
					iResource = new ByteArrayInputStream(resourceString.getBytes());
					resource = jsonP.parse(iResource);
				}
			}

			if (resource != null) {

				if (resource instanceof Parameters) {

					Parameters parameters = (Parameters) resource;

					if (formatType.equals("XML")) {
						response = context.getResourceOperationClient().resourceOperation(parameters, null, context.getSelectedServerURL(), context.getSelectedResourceType(), Constants.FHIR_XML_CONTENT, Constants.FHIR_XML_CONTENT, resourceId, "everything", null, null);
					}
					else {
						response = context.getResourceOperationClient().resourceOperation(parameters, null, context.getSelectedServerURL(), context.getSelectedResourceType(), Constants.FHIR_JSON_CONTENT, Constants.FHIR_JSON_CONTENT, resourceId, "everything", null, null);
					}
				}
				else {
					everythingExceptionOutcomeString = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.FATAL, OperationOutcome.IssueType.EXCEPTION,
							"Everything operation requires input contained in a Parameters resource type if a request payload is provided.", null, null, formatType.toLowerCase());
					FacesContext.getCurrentInstance().addMessage(
							"tabView:operationsTabView:validateForm",
							new FacesMessage(FacesMessage.SEVERITY_ERROR, "Everything operation requires input contained in a Parameters resource type if a request payload is provided.",
									"Everything operation requires input contained in a Parameters resource type if a request payload is provided."));
				}
			}
			else {

				if (formatType.equals("XML")) {
					response = context.getResourceOperationClient().resourceOperation(null, null, context.getSelectedServerURL(), context.getSelectedResourceType(), Constants.FHIR_XML_CONTENT, Constants.FHIR_XML_CONTENT, resourceId, "everything", null, null);
				}
				else {
					response = context.getResourceOperationClient().resourceOperation(null, null, context.getSelectedServerURL(), context.getSelectedResourceType(), Constants.FHIR_JSON_CONTENT, Constants.FHIR_JSON_CONTENT, resourceId, "everything", null, null);
				}
			}
		}
		catch (NumberFormatException e) {
			everythingExceptionOutcomeString = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.FATAL, OperationOutcome.IssueType.EXCEPTION, e.getMessage(), null, null, formatType.toLowerCase());
			FacesContext.getCurrentInstance().addMessage("tabView:operationsTabView:everythingForm",
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Number format error validating resource request! Please check the client logs.", "Error reading resource! Please check the client logs."));
			e.printStackTrace();
		}
		catch (Exception e) {
			everythingExceptionOutcomeString = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.FATAL, OperationOutcome.IssueType.EXCEPTION, e.getMessage(), null, null, formatType.toLowerCase());
			FacesContext.getCurrentInstance().addMessage("tabView:operationsTabView:everythingForm",
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error validating resource request! Please check the client logs.", "Error reading resource! Please check the client logs."));
			e.printStackTrace();
		}

		if (response != null) {
			String contentType = response.getHeaderString("Content-Type");
			if (contentType != null) {
				if (contentType.toUpperCase().contains("XML")) {
					context.setReturnedFormatType("XML");
				}
				else if (contentType.toUpperCase().contains("JSON")) {
					context.setReturnedFormatType("JSON");
				}
				else {
					context.setReturnedFormatType(formatType);
				}
			}
			else {
				context.setReturnedFormatType(formatType);
			}

			try {
				wrapper = new ResourceResponseWrapper(response);

				Resource responseResource = wrapper.getResource();
				if (responseResource instanceof OperationOutcome) {
					context.setValidateOperationOutcome((OperationOutcome) responseResource);
				}

				if (formatType.equals("XML")) {
					context.setResponseString(wrapper.getResourceXML());
				}
				else {
					context.setResponseString(wrapper.getResourceJSON());
				}

				FacesContext.getCurrentInstance().addMessage("tabView:operationsTabView:everythingForm", new FacesMessage(FacesMessage.SEVERITY_INFO, context.getSelectedResourceType() + " everything complete.", context.getSelectedResourceType() + " everything complete."));
			}
			catch (Exception e) {
				FacesContext.getCurrentInstance().addMessage("tabView:operationsTabView:everythingForm",
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "Resource parsing failed! Please check the client logs.", "Resource parsing failed! Please check the client logs."));
				e.printStackTrace();
			}
		}
		else if (everythingExceptionOutcomeString != null) {
			context.setReturnedFormatType(formatType);
			context.setResponseString(everythingExceptionOutcomeString);

			// Exception caught parsing input parameters; generate an OperationOutcome for the display
			try {
				if (formatType.equals("XML")) {
					// Convert XML contents to Resource
					XmlParser xmlP = new XmlParser();
					int firstValid = everythingExceptionOutcomeString.indexOf("<");
					if (firstValid > 0) {
						everythingExceptionOutcomeString = everythingExceptionOutcomeString.substring(firstValid);
					}
					iResource = new ByteArrayInputStream(everythingExceptionOutcomeString.getBytes());
					resource = xmlP.parse(iResource);
				}
				else {
					// Convert JSON contents to Resource
					JsonParser jsonP = new JsonParser();
					int firstValid = everythingExceptionOutcomeString.indexOf("{");
					if (firstValid > 0) {
						everythingExceptionOutcomeString = everythingExceptionOutcomeString.substring(firstValid);
					}
					iResource = new ByteArrayInputStream(everythingExceptionOutcomeString.getBytes());
					resource = jsonP.parse(iResource);
				}

				if (resource instanceof OperationOutcome) {
					context.setValidateOperationOutcome((OperationOutcome) resource);
				}

				FacesContext.getCurrentInstance().addMessage("tabView:operationsTabView:everythingForm", new FacesMessage(FacesMessage.SEVERITY_INFO, "Resource everything operation errors found.", "Resource everything operation errors found."));
			}
			catch (Exception e) {
				FacesContext.getCurrentInstance().addMessage("tabView:operationsTabView:everythingForm",
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error reporting everything operation errors! Please check the client logs.", "Error reporting everything operation errors! Please check the client logs."));
				e.printStackTrace();
			}
		}
		else {
			FacesContext.getCurrentInstance().addMessage("tabView:operationsTabView:everythingForm", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Everything operation did not report any results.", "Validate operation did not report any results."));
		}

		log.fine("[END] ApplicationController.fhirEverything()");
	}

	/**
	 * Execute the FHIR Path Evaluate
	 *
	 * @param event
	 */
	public void fhirpathEvaluate(ActionEvent event) {
		log.fine("[START] ApplicationController.fhirpathEvaluate()");
		log.fine("BasePath for FHIR fhirpath evaluate: " + context.getSelectedServerURL());

		String formatType = context.getSelectedFormatType();
		String methodString = context.getMethodString();
		String expressionString = context.getExpressionString();
		String resourceString = context.getResourceString();

		ByteArrayInputStream iResource = null;
		ByteArrayOutputStream oResponse = new ByteArrayOutputStream();
		Resource resource = null;
		Response response = null;
		ResourceResponseWrapper wrapper = null;
		String fhirpathEvaluateExceptionOutcomeString = null;

		XmlParser xmlP = new XmlParser();
		JsonParser jsonP = new JsonParser();

		try {
			if (expressionString.isEmpty() || resourceString.isEmpty()) {
				StringBuffer exceptionMessage = new StringBuffer("");

				if (expressionString.isEmpty()) {
					exceptionMessage.append("fhirpath expression is undefined or empty. ");
				}
				if (resourceString.isEmpty()) {
					exceptionMessage.append("FHIR Resource contents are undefined or empty.");
				}

				throw new Exception(exceptionMessage.toString());
			}

			if (formatType.equals("XML")) {
				// Convert XML contents to Resource
				int firstValid = resourceString.indexOf("<");
				if (firstValid > 0) {
					resourceString = resourceString.substring(firstValid);
				}
				iResource = new ByteArrayInputStream(resourceString.getBytes());
				resource = xmlP.parse(iResource);
			}
			else {
				// Convert JSON contents to Resource
				int firstValid = resourceString.indexOf("{");
				if (firstValid > 0) {
					resourceString = resourceString.substring(firstValid);
				}
				iResource = new ByteArrayInputStream(resourceString.getBytes());
				resource = jsonP.parse(iResource);
			}

			// Build input parameters
			Parameters inputParameters = new Parameters();

			ParametersParameterComponent inputParameter = new ParametersParameterComponent();
			inputParameter.setName("method");
			inputParameter.setValue(new StringType(methodString));
			inputParameters.getParameter().add(inputParameter);

			inputParameter = new ParametersParameterComponent();
			inputParameter.setName("resource");
			inputParameter.setResource(resource);
			inputParameters.getParameter().add(inputParameter);

			inputParameter = new ParametersParameterComponent();
			inputParameter.setName("expression");
			inputParameter.setValue(new StringType(expressionString));
			inputParameters.getParameter().add(inputParameter);

			if (formatType.equals("XML")) {
				response = context.getFhirpathEvaluatorRESTClient().evaluate(inputParameters, context.getSelectedServerURL(), Constants.FHIR_XML_CONTENT, null);
			}
			else {
				response = context.getFhirpathEvaluatorRESTClient().evaluate(inputParameters, context.getSelectedServerURL(), Constants.FHIR_JSON_CONTENT, null);
			}
		}
		catch (NumberFormatException e) {
			fhirpathEvaluateExceptionOutcomeString = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.FATAL, OperationOutcome.IssueType.EXCEPTION, e.getMessage(), null, null, formatType.toLowerCase());
			FacesContext.getCurrentInstance().addMessage("tabView:toolsTabView:fhirpathEvaluateForm",
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Number format error with $fhirpath-evaluate request! Please check the client logs.", "Number format error with $fhirpath-evaluate request! Please check the client logs."));
			e.printStackTrace();
		}
		catch (Exception e) {
			fhirpathEvaluateExceptionOutcomeString = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.FATAL, OperationOutcome.IssueType.EXCEPTION, e.getMessage(), null, null, formatType.toLowerCase());
			FacesContext.getCurrentInstance().addMessage("tabView:toolsTabView:fhirpathEvaluateForm",
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error with $fhirpath-evaluate request! Please check the client logs.", "Error with $fhirpath-evaluate request! Please check the client logs."));
			e.printStackTrace();
		}

		if (response != null) {
			String contentType = response.getHeaderString("Content-Type");
			if (contentType != null) {
				if (contentType.toUpperCase().contains("XML")) {
					context.setReturnedFormatType("XML");
				}
				else if (contentType.toUpperCase().contains("JSON")) {
					context.setReturnedFormatType("JSON");
				}
				else {
					context.setReturnedFormatType(formatType);
				}
			}
			else {
				context.setReturnedFormatType(formatType);
			}

			try {
				wrapper = new ResourceResponseWrapper(response);

				oResponse = new ByteArrayOutputStream();
				if (formatType.equals("XML")) {
					xmlP.setOutputStyle(OutputStyle.PRETTY);
					xmlP.compose(oResponse, wrapper.getResource(), true);

					context.setResponseString(oResponse.toString());
				}
				else {
					jsonP.setOutputStyle(OutputStyle.PRETTY);
					jsonP.compose(oResponse, wrapper.getResource());

					context.setResponseString(oResponse.toString());
				}

				FacesContext.getCurrentInstance().addMessage("tabView:toolsTabView:fhirpathEvaluateForm", new FacesMessage(FacesMessage.SEVERITY_INFO, "$fhirpath-evaluate complete.", "$fhirpath-evaluate complete."));
			}
			catch (Exception e) {
				FacesContext.getCurrentInstance().addMessage("tabView:toolsTabView:fhirpathEvaluateForm",
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "$fhirpath-evaluate failed! Please check the client logs.", "$fhirpath-evaluate failed! Please check the client logs."));
				e.printStackTrace();
			}
		}
		else if (fhirpathEvaluateExceptionOutcomeString != null) {
			context.setReturnedFormatType(formatType);
			context.setResponseString(fhirpathEvaluateExceptionOutcomeString);

			FacesContext.getCurrentInstance().addMessage("tabView:toolsTabView:fhirpathEvaluateForm", new FacesMessage(FacesMessage.SEVERITY_INFO, "$fhirpath-evaluate errors found.", "$fhirpath-evaluate errors found."));
		}
		else {
			FacesContext.getCurrentInstance().addMessage("tabView:toolsTabView:fhirpathEvaluateForm", new FacesMessage(FacesMessage.SEVERITY_ERROR, "$fhirpath-evaluate did not report any results.", "$fhirpath-evaluate did not report any results."));
		}

		log.fine("[END] ApplicationController.fhirpathEvaluate()");
	}

	/*
	 * Server directory manage methods
	 */

	/**
	 * Handles updating or creating new servers
	 *
	 * @param serverType
	 *            - new or existing
	 */
	public void manageServer(String serverType) {

		if (serverType.equalsIgnoreCase("existing")) {
			log.fine("[Start] ApplicationController.manageServer() - update Existing Server");
			try {

				Serverdirectory server = context.getServerDirectoryService().update(context.getSelectedServer());
				if (server != null) {
					FacesContext.getCurrentInstance().addMessage("tabView:serversForm", new FacesMessage(FacesMessage.SEVERITY_INFO, "Server: " + server.getName() + " updated.", ""));
				}
			}
			catch (Exception e) {
				log.severe(e.getMessage());
				FacesContext.getCurrentInstance().addMessage("tabView:serversForm", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error updating Server.", ""));
			}
		}

		if (serverType.equalsIgnoreCase("new")) {
			log.fine("[Start] ApplicationController.manageServer() - Create new Server");
			try {
				Serverdirectory server = context.getServerDirectoryService().create(context.getNewServer());
				if (server != null) {
					FacesContext.getCurrentInstance().addMessage("tabView:serversForm", new FacesMessage(FacesMessage.SEVERITY_INFO, "Server: " + server.getName() + " successfully created.", ""));
					context.setNewServer(new Serverdirectory());
					context.setAvailableServers(context.getServerDirectoryService().findAll());
				}
			}
			catch (Exception e) {
				log.severe(e.getMessage());
				FacesContext.getCurrentInstance().addMessage("tabView:serversForm", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error creating new Server.", ""));
			}
		}
	}

	/**
	 * set the server to update
	 *
	 * @param server
	 */
	public void serverToManage(Serverdirectory server) {
		log.fine("[START] ApplicationController.serverToManage");
		context.setSelectedServer(server);
		log.fine("serverToManage name: " + context.getSelectedServer().getName());
	}

	/**
	 * Remove server that matches supplied id
	 *
	 * @param server
	 */
	public void deleteServer(Serverdirectory server) {
		log.fine("[START] ApplicationController.deleteServer() - Server ID: " + server.getId());

		Integer serverId = server.getId();
		int result = -1;
		try {
			result = context.getServerDirectoryService().delete(serverId);
			if (result == 1) {
				FacesContext.getCurrentInstance().addMessage("tabView:adminTabView:serversForm", new FacesMessage(FacesMessage.SEVERITY_INFO, "Server with ID " + serverId + " successfully deleted.", ""));
				context.setAvailableServers(context.getServerDirectoryService().findAll());
			}
			else {
				throw new Exception("Error deleting Server");
			}
		}
		catch (NumberFormatException e) {
			log.severe(e.getMessage());
			FacesContext.getCurrentInstance().addMessage("tabView:adminTabView:serversForm", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Number format error deleting Server with ID " + serverId, ""));
		}
		catch (Exception e) {
			log.severe(e.getMessage());
			FacesContext.getCurrentInstance().addMessage("tabView:adminTabView:serversForm", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error deleting Server with ID " + serverId, ""));
		}
	}

	/*
	 * Subscription manage methods
	 */

	/**
	 * Start the Subscription Service using the passed in since date
	 *
	 * @param event
	 */
	public void processSubscriptions(ActionEvent event) {
		log.fine("[START] ApplicationController.processSubscriptions()");

		List<LabelKeyValueBean> results = null;

		try {
			if (context.getCodeService().isSupported("subscriptionServiceEnabled")) {
				LocalDateTime dateTimePicker = context.getDateTimePicker();

				if (dateTimePicker == null) {

					FacesContext.getCurrentInstance().addMessage("tabView:subscriptionClientTab:subscriptionClientForm",
							new FacesMessage(FacesMessage.SEVERITY_WARN, "Missing Criteria - please enter Since DateTime.", "Missing Criteria - please enter Since DateTime."));

				}
				else {
					// Convert from LocalDateTime to Date in current time zone
					Date datePicker = Date.from(dateTimePicker.atZone(ZoneId.of("GMT")).toInstant());

					log.fine("datePicker = " + utcDateUtil.formatDate(datePicker, UTCDateUtil.DATETIME_ONLY_PARAMETER_FORMAT));

					results = context.getSubscriptionServiceR5().processSubscriptions(datePicker);

					if (results == null) {
						results = new ArrayList<LabelKeyValueBean>();
					}
					if (results.isEmpty()) {
						results.add(new LabelKeyValueBean("No active subscriptions found.","",""));
					}

					context.setListLabelKeyValue(results);

					FacesContext.getCurrentInstance().addMessage("tabView:subscriptionClientTab:subscriptionClientForm",
							new FacesMessage(FacesMessage.SEVERITY_INFO, "Subscription processing successfully executed.", "Subscription processing successfully executed."));
				}
			}
			else {
				FacesContext.getCurrentInstance().addMessage("tabView:subscriptionClientTab:subscriptionClientForm",
						new FacesMessage(FacesMessage.SEVERITY_WARN, "Subscription processing is not enabled.", "Subscription processing is not enabled."));
			}
		}
		catch (Exception e1) {
			log.fine(e1.getMessage());
			FacesContext.getCurrentInstance().addMessage("tabView:subscriptionClientTab:subscriptionClientForm",
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error executing subscription processing! Please check the client logs.", "Error executing subscription processing! Please check the client logs."));

			e1.printStackTrace();
		}

		log.fine("[END] ApplicationController.fhirProcessSubscriptions()");
	}

	/**
	 * read a file and converting it to String using StringBuilder
	 */
	protected String stringBuilder(String fileName) throws IOException {

		StringBuilder sbuilder = null;
		FileInputStream fStream = null;
		BufferedReader input = null;

		try {

			fStream = new FileInputStream(fileName);
			input = new BufferedReader(new InputStreamReader(fStream, "UTF-8"));

			sbuilder = new StringBuilder();

			String str = input.readLine();

			while (str != null) {
				sbuilder.append(str);
				str = input.readLine();
				if (str != null) {

					sbuilder.append("\n");

				}
			}

		}
		finally {
			input.close();
			fStream.close();
		}

		return sbuilder.toString();
	}

}
