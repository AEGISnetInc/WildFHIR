/*
 * #%L
 * WildFHIR - wildfhir-service
 * %%
 * Copyright (C) 2025 AEGIS.net, Inc.
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.UserTransaction;

import net.aegis.fhir.model.Serverdirectory;

/**
 * ServerdirectoryService services for basic data operations: findAll, create,
 * delete, read and update.
 *
 * The @Stateless annotation eliminates the need for manual transaction
 * demarcation
 *
 * @author richard.ettema
 *
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class ServerdirectoryService {

	@Inject
	private Logger log;

	@PersistenceContext
	private EntityManager em;

	@Resource
	private UserTransaction userTransaction;

	@Inject
	private Event<Serverdirectory> serverdirectoryEventSrc;

	/**
	 * The create interaction creates a new Server Directory record.
	 *
	 * @param serverdirectory
	 * @return <code>Serverdirectory</code>
	 * @throws Exception
	 */
	public Serverdirectory create(Serverdirectory serverdirectory) throws Exception {

		log.fine("[START] ServerdirectoryService.create");

		Serverdirectory newServerdirectory = null;

		try {
			newServerdirectory = serverdirectory.clone(false);
			newServerdirectory.setLastUser("system");
			newServerdirectory.setLastUpdate(new Date());

			/*
			 * TRANSACTION BEGIN
			 */
			userTransaction.begin();

			em.persist(newServerdirectory);

			serverdirectoryEventSrc.fire(newServerdirectory);

			/*
			 * TRANSACTION COMMIT(END)
			 */
			userTransaction.commit();
		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return newServerdirectory;
	}

	/**
	 * The read interaction returns a single Server Directory record.
	 *
	 * @param id
	 * @return <code>Serverdirectory</code>
	 * @throws Exception
	 */
	public Serverdirectory read(Integer id) throws Exception {

		log.fine("[START] ServerdirectoryService.read");

		Serverdirectory serverdirectory = null;

		try {
			/*
			 * TRANSACTION BEGIN
			 */
			userTransaction.begin();

			serverdirectory = em.find(Serverdirectory.class, id);

			/*
			 * TRANSACTION COMMIT(END)
			 */
			userTransaction.commit();
		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return serverdirectory;
	}

	/**
	 * The update interaction modifies an existing Server Directory record.
	 *
	 * @param updateServerdirectory
	 * @return <code>Serverdirectory</code>
	 * @throws Exception
	 */
	public Serverdirectory update(Serverdirectory updateServerdirectory) throws Exception {

		log.fine("[START] ServerdirectoryService.update");

		Serverdirectory serverdirectory = null;

		try {
			serverdirectory = read(updateServerdirectory.getId());

			serverdirectory.setName(updateServerdirectory.getName());
			serverdirectory.setDescription(updateServerdirectory.getDescription());
			serverdirectory.setBasePath(updateServerdirectory.getBasePath());
			serverdirectory.setLastUser("system");
			serverdirectory.setLastUpdate(new Date());

			/*
			 * TRANSACTION BEGIN
			 */
			userTransaction.begin();

			em.merge(serverdirectory);
			serverdirectoryEventSrc.fire(serverdirectory);

			/*
			 * TRANSACTION COMMIT(END)
			 */
			userTransaction.commit();
		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return serverdirectory;
	}

	/**
	 * The delete interaction deletes a Server Directory record.
	 *
	 * @param id
	 * @throws Exception
	 */
	public int delete(Integer id) throws Exception {

		log.fine("[START] ServerdirectoryService.delete");

		int result = 0;

		try {
			Serverdirectory serverdirectory = em.find(Serverdirectory.class, id);

			if (serverdirectory != null) {
				/*
				 * TRANSACTION BEGIN
				 */
				userTransaction.begin();

				em.remove(serverdirectory);
				result = 1;

				/*
				 * TRANSACTION COMMIT(END)
				 */
				userTransaction.commit();
			}
		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return result;
	}

	/**
	 * The findAll interaction returns all Server Directory records sorted by name
	 * ascending.
	 *
	 * @return <code>List<Serverdirectory></code>
	 * @throws Exception
	 */
	public List<Serverdirectory> findAll() throws Exception {

		log.fine("[START] ServerdirectoryService.findAll");

		List<Serverdirectory> result = new ArrayList<Serverdirectory>();

		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Serverdirectory> criteria = cb.createQuery(Serverdirectory.class);
			Root<Serverdirectory> rootServerdirectory = criteria.from(Serverdirectory.class);

			criteria.select(rootServerdirectory).orderBy(cb.asc(rootServerdirectory.get("name")));

			result = em.createQuery(criteria).getResultList();

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return result;
	}

	/**
	 * Get the Server Directory record for a specific basePath or url
	 *
	 * @param basePath
	 * @return <code>Serverdirectory</code>
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Serverdirectory findServerdirectoryByBasePath(String basePath) throws Exception {

		log.fine("[START] ServerdirectoryService.findServerdirectoryByBasePath");

		Query serverdirectoryQuery = null;
		Serverdirectory serverdirectoryMatch = null;

		/*
		 * Generate the Serverdirectory list of all records for a basePath value; should
		 * only be one
		 */
		try {
			serverdirectoryQuery = em.createNamedQuery("findServerdirectoryByBasePath").setParameter("basePath",
					basePath);

			List<Serverdirectory> serverdirectoryList = (List<Serverdirectory>) serverdirectoryQuery.getResultList();

			if (serverdirectoryList != null && !serverdirectoryList.isEmpty()) {
				// Return the first Serverdirectory; should only be one
				serverdirectoryMatch = serverdirectoryList.get(0);
			}
		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return serverdirectoryMatch;

	}

}
