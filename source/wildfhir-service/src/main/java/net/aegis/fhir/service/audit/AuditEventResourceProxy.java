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
package net.aegis.fhir.service.audit;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.AuditEvent;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Enumeration;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.AuditEvent.AuditEventAction;
import org.hl7.fhir.r4.model.AuditEvent.AuditEventActionEnumFactory;
import org.hl7.fhir.r4.model.AuditEvent.AuditEventOutcome;
import org.hl7.fhir.r4.model.AuditEvent.AuditEventOutcomeEnumFactory;
import org.hl7.fhir.r4.model.AuditEvent.AuditEventSourceComponent;
import org.hl7.fhir.r4.model.AuditEvent.AuditEventAgentComponent;
import org.hl7.fhir.r4.model.AuditEvent.AuditEventAgentNetworkComponent;
import org.hl7.fhir.r4.model.AuditEvent.AuditEventAgentNetworkType;

import net.aegis.fhir.service.util.ServicesUtil;

/**
 * @author Venkat.Keesara
 * @author richard.ettema
 *
 */
public abstract class AuditEventResourceProxy {
	private Logger log = Logger.getLogger(getClass().getName());
	public static String appId = AuditEventConstant.HEADER_APPLICATION_NAME;
	public static String userId = AuditEventConstant.HEADER_USER_ID;
	public static String userName = AuditEventConstant.HEADER_USER_NAME;
	public static String site = AuditEventConstant.HEADER_SITE_NAME;

	public abstract Resource generateAuditEvent(UriInfo context, HttpHeaders headers, String payload, String resourceType, Response response, String resourceId, String operation) throws Exception;

	public abstract AuditEventAction setAction();

	protected void prepareBasicData(AuditEvent audit, UriInfo context, HttpHeaders headers, Response response) throws Exception {

		try {
			// audit.addChild("type"); // Not recommended as Oath is not implemented yet
			audit.setType(getType(headers));

			audit.addSubtype();
			// No enum needed for SUB type . We can use this based on operation
			/*
			 * TypeRestfulInteraction.READ.getSystem(); TypeRestfulInteraction.READ.getDefinition();
			 * TypeRestfulInteraction.READ.getDisplay(); TypeRestfulInteraction.READ.toCode();
			 */
			// audit.addChild("action");
			audit.setAction(setAction());

			audit.setRecorded(new Date());
			audit.setOutcome(getOutcome(response));
			//audit.setOutcomeDesc(value);
			audit.addChild("purposeOfEvent");
			List<AuditEventAgentComponent> agentList = new ArrayList<>();
			agentList.add(getAgent(headers));
			audit.setAgent(agentList);
			// context will have Source info ( who sent to us )
			audit.setSource(getSourceElement(headers));

			audit.addEntity();
			log.info("prepareBasicData");
		}
		catch (FHIRException e) {
			log.severe(e.getMessage());
			//e.printStackTrace();
		}

	}

	protected Coding getType(HttpHeaders headers) {
		Coding type = new Coding(AuditEventTypeEnum.REST.getSystem(), AuditEventTypeEnum.REST.getCode(), AuditEventTypeEnum.REST.getDisplay());
		return type;
	}

	protected AuditEventSourceComponent getSourceElement(HttpHeaders headers) throws Exception {

		String appId = ServicesUtil.INSTANCE.getHttpHeader(headers, AuditEventConstant.HEADER_APPLICATION_NAME);

		AuditEventSourceComponent source = new AuditEventSourceComponent();
		if (StringUtils.isEmpty(appId)) {
			appId = AuditEventResourceProxy.appId;
		}
		source.setId(appId);
		source.setObserver(getObserver(appId));
		source.setType(getSourceTypes(headers));
		source.setSite(getSiteId(headers));
		return source;
	}

	protected Enumeration<AuditEventAction> getActionEnum(HttpHeaders headers) throws RuntimeException {

		AuditEventActionEnumFactory auditEventActionEnumFactory = new AuditEventActionEnumFactory();
		Enumeration<AuditEventAction> action = new Enumeration<AuditEventAction>(auditEventActionEnumFactory);

		return action;
	}

	/*
	 * protected AuditEventAction getAction(HttpHeaders headers) throws RuntimeException {
	 *
	 * AuditEventAction enumValue = AuditEventAction.C ; return enumValue ;
	 *
	 * }
	 */

	protected Enumeration<AuditEventOutcome> getOutcomeEnum(Response response) {
		AuditEventOutcomeEnumFactory auditEventoutcomeEnumFactory = new AuditEventOutcomeEnumFactory();
		Enumeration<AuditEventOutcome> outcome = new Enumeration<AuditEventOutcome>(auditEventoutcomeEnumFactory);

		if (response != null) {
			int status = response.getStatus();

			if (Response.Status.BAD_REQUEST.getStatusCode() != status) {

			}

		}
		return outcome;
	}

	protected AuditEventOutcome getOutcome(Response response) {
		AuditEventOutcome enumValue = AuditEventOutcome._0;
		if (response != null) {
			int status = response.getStatus();

			if (Response.Status.BAD_REQUEST.getStatusCode() != status) {
				enumValue = AuditEventOutcome._12;
			}
		}
		return enumValue;
	}

	protected AuditEventAgentComponent getAgent(HttpHeaders headers) throws Exception {
		AuditEventAgentComponent agent = new AuditEventAgentComponent();
		String userId = ServicesUtil.INSTANCE.getHttpHeader(headers, AuditEventConstant.HEADER_USER_ID);
		String userName = ServicesUtil.INSTANCE.getHttpHeader(headers, AuditEventConstant.HEADER_USER_NAME);
		if (StringUtils.isEmpty(userId)) {
			userId = AuditEventResourceProxy.userId;
		}
		agent.setUserData("userId", userId);
		if (StringUtils.isEmpty(userName)) {
			userName = AuditEventResourceProxy.userName;
		}
		agent.setName(userName);
		if (StringUtils.isEmpty(userId) && StringUtils.isEmpty(userName)) {
			agent.setRequestor(false);
		}
		else {
			agent.setRequestor(true);
		}

		AuditEventAgentNetworkComponent network = new AuditEventAgentNetworkComponent();
		// IP address
		String remoteAdd = ServicesUtil.INSTANCE.getHttpHeader(headers, AuditEventConstant.REMOTE_ADDR);
		if (StringUtils.isNotEmpty(remoteAdd)) {
			network.setAddress(remoteAdd);
			network.setType(AuditEventAgentNetworkType._2);
		}
		else {
			network.setAddress("Machine name Not found");
			network.setType(AuditEventAgentNetworkType._1);
		}

		return agent;
	}

	private List<Coding> getSourceTypes(HttpHeaders headers) throws Exception {
		List<Coding> types = new ArrayList<Coding>();
		String header = ServicesUtil.INSTANCE.getHttpHeader(headers, AuditEventConstant.HEADER_AUTHORIZATION);
		// If it has Oauth
		if (header != null && header.startsWith("OAuth")) {
			types.add(new Coding(AuditEventSourceTypeEnum.USER_DEVICE.getSystem(), AuditEventSourceTypeEnum.USER_DEVICE.getCode(), AuditEventSourceTypeEnum.USER_DEVICE.getDisplay()));
		}
		else {
			String userId = ServicesUtil.INSTANCE.getHttpHeader(headers, AuditEventConstant.HEADER_USER_ID);
			String appId = ServicesUtil.INSTANCE.getHttpHeader(headers, AuditEventConstant.HEADER_APPLICATION_NAME);
			if (StringUtils.isEmpty(appId)) {
				appId = AuditEventResourceProxy.appId;
			}
			if (userId == null && appId != null)
				types.add(new Coding(AuditEventSourceTypeEnum.APPLICATION_SERVER.getSystem(), AuditEventSourceTypeEnum.APPLICATION_SERVER.getCode(), AuditEventSourceTypeEnum.APPLICATION_SERVER.getDisplay()));
			else
				types.add(new Coding(AuditEventSourceTypeEnum.USER_DEVICE.getSystem(), AuditEventSourceTypeEnum.USER_DEVICE.getCode(), AuditEventSourceTypeEnum.USER_DEVICE.getDisplay()));
		}

		return types;
	}

	private Reference getObserver(String appId) {
		Reference ref = new Reference();
		Identifier identifier = new Identifier();
		identifier.setValue(appId);

		ref.setIdentifier(identifier);
		return ref;
	}

	private String getSiteId(HttpHeaders headers) {
		return site; // the site id from the request info
	}

}
