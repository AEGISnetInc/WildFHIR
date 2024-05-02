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
package net.aegis.fhir.service;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.UserTransaction;
import javax.ws.rs.core.Response;

import net.aegis.fhir.model.Code;
import net.aegis.fhir.model.Conformance;
import net.aegis.fhir.model.ResourceContainer;

/**
 * Generate the AEGIS WildHFIR Server CapabilityStatement resource object based on the WildFHIR ResourceType definitions.
 *
 * @author richard.ettema
 * @see net.aegis.fhir.model.ResourceType
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class ConformanceService {

    private Logger log = Logger.getLogger("ConformanceService");

    @PersistenceContext
	private EntityManager em;

    @Inject
	CodeService codeService;

    @Resource
    private UserTransaction userTransaction;

    private final Charset UTF8_CHARSET = Charset.forName("UTF-8");

	/**
	 * The read interaction accesses the current contents of a resource of the specified type. This method is coded specifically
	 * for this server's CapabilityStatement resource.
	 *
	 * @param resourceType
	 * @param resourceId
	 * @return <code>ResourceContainer</code>
	 * @throws Exception
	 */
	public ResourceContainer read() throws Exception {

		return this.read("0");

	}

	/**
	 * The read interaction accesses the current contents of a resource of the specified type. This method is coded specifically
	 * for this server's CapabilityStatement resource.
	 *
	 * @param resourceType
	 * @param resourceId
	 * @return <code>ResourceContainer</code>
	 * @throws Exception
	 */
	public ResourceContainer read(String resourceId) throws Exception {

		log.fine("[START] ConformanceService.read(" + resourceId + ")");

		ResourceContainer resourceContainer = new ResourceContainer();

		try {
			/*
			 *  TRANSACTION BEGIN
			 */
			userTransaction.begin();

			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<net.aegis.fhir.model.Conformance> criteria = cb.createQuery(net.aegis.fhir.model.Conformance.class);
			Root<net.aegis.fhir.model.Conformance> conformance = criteria.from(net.aegis.fhir.model.Conformance.class);
			List<Predicate> predicateList = new ArrayList<Predicate>();
			predicateList.add(cb.equal(conformance.get("resourceId"), resourceId));
			predicateList.add(cb.equal(conformance.get("resourceType"), "CapabilityStatement"));

			criteria.select(conformance)
				.where(cb.and(predicateList.toArray(new Predicate[predicateList.size()])))
				.orderBy(cb.desc(conformance.get("versionId")));

			List<net.aegis.fhir.model.Conformance> conformances = em.createQuery(criteria).getResultList();

			if (conformances != null && conformances.size() > 0) {
				// Resource ID found, assign first one
				resourceContainer.setConformance(conformances.get(0));

				// Check the conformance status; if not 'DELETED' then consider it 'VALID'
				if (conformances.get(0).getStatus() != null && conformances.get(0).getStatus().equalsIgnoreCase("DELETED")) {
					resourceContainer.setResponseStatus(Response.Status.GONE);
				} else {
					resourceContainer.setResponseStatus(Response.Status.OK);
				}
			} else {
				// No match found
				resourceContainer.setResponseStatus(Response.Status.NOT_FOUND);
			}

			/*
			 *  TRANSACTION COMMIT(END)
			 */
			userTransaction.commit();
		} catch (Exception e) {
			// Exception caught
			resourceContainer.setResource(null);
			resourceContainer.setResponseStatus(Response.Status.INTERNAL_SERVER_ERROR);
			resourceContainer.setMessage(e.getMessage());

			log.severe(e.getMessage());
			e.printStackTrace();
			// Exception not thrown to allow operation to complete
		}

		return resourceContainer;
	}

    /**
	 * The read interaction returns a single Conformance record.
	 *
	 * @param id
	 * @return Conformance
	 * @throws Exception
	 */
	public Conformance read(Integer id) throws Exception {

		log.fine("[START] ConformanceService.read");

		Conformance conformance = null;

		try {
			/*
			 *  TRANSACTION BEGIN
			 */
			userTransaction.begin();

			conformance = em.find(Conformance.class, id);

			/*
			 *  TRANSACTION COMMIT(END)
			 */
			userTransaction.commit();
		} catch (Exception e) {
			// Exception caught
			log.severe(e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return conformance;
	}

	/**
	 * The create interaction creates a new Conformance record.
	 *
	 * @param conformance
	 * @return Conformance
	 * @throws Exception
	 */
	public Conformance create(Conformance conformance) throws Exception {

		log.fine("[START] ConformanceService.create");

		Conformance newConformance = null;

		try {
			newConformance = conformance.clone(false);

			/*
			 *  TRANSACTION BEGIN
			 */
			userTransaction.begin();

			em.persist(newConformance);

			//codeEventSrc.fire(newCode);

			/*
			 *  TRANSACTION COMMIT(END)
			 */
			userTransaction.commit();
		} catch (Exception e) {
			// Exception caught
			log.severe(e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return newConformance;
	}

    /**
	 * The update interaction modifies an existing Conformance record.
	 *
	 * @param updateConformance
	 * @return Conformance
	 * @throws Exception
	 */
	public Conformance update(Conformance updateConformance) throws Exception {

		log.fine("[START] Conformance.update");

		try {
			/*
			 *  TRANSACTION BEGIN
			 */
			userTransaction.begin();

			em.merge(updateConformance);

			/*
			 *  TRANSACTION COMMIT(END)
			 */
			userTransaction.commit();
		} catch (Exception e) {
			// Exception caught
			log.severe(e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return updateConformance;
	}

	/**
	 * The delete interaction deletes a Conformance record.
	 *
	 * @param id
	 * @throws Exception
	 */
	public int delete(Integer id) throws Exception {

		log.fine("[START] ConformanceService.delete");

		int result = 0;

		try {
			/*
			 *  TRANSACTION BEGIN
			 */
			userTransaction.begin();

			Conformance conformance = em.find(Conformance.class, id);

			if (conformance != null) {
				em.remove(conformance);
				result = 1;
			}

			/*
			 *  TRANSACTION COMMIT(END)
			 */
			userTransaction.commit();
		} catch (Exception e) {
			// Exception caught
			log.severe(e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return result;
	}

    public ResourceContainer fetchFhirBaseCapabilityStatement() {

		log.fine("[START] ConformanceService.generateWildFHIRConformance");

		ResourceContainer resourceContainer = new ResourceContainer();

		try {
			// Fetch Conformance resource for FHIR base capability statement
			resourceContainer = this.read("fhir-base");
			if (resourceContainer != null && resourceContainer.getConformance() != null && resourceContainer.getConformance().getResourceContents() != null) {
				byte rcs[] =  resourceContainer.getConformance().getResourceContents();
				String rcsStr = new String(rcs, UTF8_CHARSET);
				log.fine("conformance resource: " + rcsStr);

				// Fetch baseUrl
				String baseUrl = getBaseUrl();
				log.fine("getBaseUrl: " + baseUrl);
				resourceContainer.setResponseStatus(Response.Status.OK);
			}else {
				resourceContainer.setResponseStatus(Response.Status.NOT_FOUND);
			}

		} catch (Exception e) {
			e.printStackTrace();
			log.severe(e.getMessage());
		}

		return resourceContainer;
    }

    /**
	 * @return String - RS Algorithm-based Jwk Provider URL
	 * @throws Exception
	 */
	public String getBaseUrl() throws Exception {
		String baseUrl = "";

		Code code = codeService.findCodeByName("baseUrl");

		if (code != null && !code.getValue().isEmpty()) {
			baseUrl = code.getValue();
		}

		return baseUrl;
	}

}
