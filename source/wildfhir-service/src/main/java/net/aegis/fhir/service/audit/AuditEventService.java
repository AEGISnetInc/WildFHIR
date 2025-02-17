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

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.hl7.fhir.r4.formats.IParser.OutputStyle;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.Meta;

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.service.CodeService;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.UUIDUtil;

/**
 * @author Venkat.Keesara
 *
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class AuditEventService {

	private Logger log = Logger.getLogger(getClass().getName());

	@Inject
	CodeService codeService;
	@Inject
	ResourceService resourceService;

	@PersistenceContext
	private EntityManager em;

	@javax.annotation.Resource
	private UserTransaction userTransaction;
	@Inject
	private Event<net.aegis.fhir.model.Resource> resourceEventSrc;

	/**
	 * @param context
	 * @param headers
	 * @param payload
	 * @param resourceType
	 * @param response
	 * @param string
	 * @throws Exception
	 */
	public void createAuditEvent(UriInfo context, HttpHeaders headers, String payload, String resourceType, Response response, String resourceId, String operation) {

		try {
			if (codeService.isSupported("auditEventServiceEnabled")) {
				Resource resource = AuditEventServiceUtil.INSTANCE.generateAuditEvent(context, headers, payload, resourceType, response, resourceId, operation);

				String nextResourceIdString = UUIDUtil.getGUID();
				int nextVersionId = 1;
				Date updatedTime = new Date();

				// create a new wildfhir resource; version based on whether we came from an update or not
				net.aegis.fhir.model.Resource newResource = new net.aegis.fhir.model.Resource();
				newResource.setResourceId(nextResourceIdString);
				newResource.setVersionId(Integer.valueOf(nextVersionId));
				newResource.setResourceType(resource.getResourceType());
				newResource.setStatus("valid");
				newResource.setLastUser("system");
				newResource.setLastUpdate(updatedTime);

				// Convert XML contents to Resource object and set id and meta
				ByteArrayInputStream iResource = new ByteArrayInputStream(resource.getResourceContents());
				XmlParser xmlP = new XmlParser();
				xmlP.setOutputStyle(OutputStyle.PRETTY);
				org.hl7.fhir.r4.model.Resource resourceObject = xmlP.parse(iResource);

				resourceObject.setId(nextResourceIdString);

				Meta resourceMeta = new Meta();
				if (resourceObject.hasMeta()) {
					resourceMeta = resourceObject.getMeta();
				}
				resourceMeta.setVersionId(Integer.toString(nextVersionId));
				resourceMeta.setLastUpdated(updatedTime);
				resourceObject.setMeta(resourceMeta);
				byte[] resourceBytes = xmlP.composeBytes(resourceObject);

				newResource.setResourceContents(resourceBytes);

				/*
				 * TRANSACTION BEGIN
				 */
				userTransaction.begin();
				em.persist(newResource);
				resourceEventSrc.fire(newResource);

				/*
				 * TRANSACTION COMMIT(END)
				 */
				userTransaction.commit();
			}
		}
		catch (Exception e) {
			log.severe(e.getMessage());
			// e.printStackTrace();
		}

	}

}
