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
import java.util.List;
import java.util.logging.Logger;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.UserTransaction;

import net.aegis.fhir.model.Subscriptionactivity;

/**
 * SubscriptionactivityService services for basic data operations: findAll, create,
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
public class SubscriptionactivityService {

	@Inject
	private Logger log;

	@PersistenceContext
	private EntityManager em;

	@Resource
	private UserTransaction userTransaction;

	@Inject
	private Event<Subscriptionactivity> subscriptionactivityEventSrc;

	/**
	 * The create interaction creates a new Server Activity record.
	 *
	 * @param subscriptionactivity
	 * @return <code>Subscriptionactivity</code>
	 * @throws Exception
	 */
	public Subscriptionactivity create(Subscriptionactivity subscriptionactivity) throws Exception {

		log.fine("[START] SubscriptionactivityService.create");

		Subscriptionactivity newSubscriptionactivity = null;

		try {
			newSubscriptionactivity = subscriptionactivity.clone(false);

			/*
			 * TRANSACTION BEGIN
			 */
			userTransaction.begin();

			em.persist(newSubscriptionactivity);

			subscriptionactivityEventSrc.fire(newSubscriptionactivity);

			/*
			 * TRANSACTION COMMIT(END)
			 */
			userTransaction.commit();
		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return newSubscriptionactivity;
	}

	/**
	 * The read interaction returns a single Server Activity record.
	 *
	 * @param id
	 * @return <code>Subscriptionactivity</code>
	 * @throws Exception
	 */
	public Subscriptionactivity read(Integer id) throws Exception {

		log.fine("[START] SubscriptionactivityService.read");

		Subscriptionactivity subscriptionactivity = null;

		try {
			/*
			 * TRANSACTION BEGIN
			 */
			userTransaction.begin();

			subscriptionactivity = em.find(Subscriptionactivity.class, id);

			/*
			 * TRANSACTION COMMIT(END)
			 */
			userTransaction.commit();
		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return subscriptionactivity;
	}

	/**
	 * The update interaction modifies an existing Server Activity record.
	 *
	 * @param updateSubscriptionactivity
	 * @return <code>Subscriptionactivity</code>
	 * @throws Exception
	 */
	public Subscriptionactivity update(Subscriptionactivity updateSubscriptionactivity) throws Exception {

		log.fine("[START] SubscriptionactivityService.update");

		Subscriptionactivity subscriptionactivity = null;

		try {
			subscriptionactivity = read(updateSubscriptionactivity.getId());

			subscriptionactivity.setSubscriptionId(updateSubscriptionactivity.getSubscriptionId());
			subscriptionactivity.setRecorded(updateSubscriptionactivity.getRecorded());
			subscriptionactivity.setType(updateSubscriptionactivity.getType());
			subscriptionactivity.setStatus(updateSubscriptionactivity.getStatus());
			subscriptionactivity.setDescription(updateSubscriptionactivity.getDescription());

			/*
			 * TRANSACTION BEGIN
			 */
			userTransaction.begin();

			em.merge(subscriptionactivity);
			subscriptionactivityEventSrc.fire(subscriptionactivity);

			/*
			 * TRANSACTION COMMIT(END)
			 */
			userTransaction.commit();
		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return subscriptionactivity;
	}

	/**
	 * The delete interaction deletes a Server Activity record.
	 *
	 * @param id
	 * @throws Exception
	 */
	public int delete(Integer id) throws Exception {

		log.fine("[START] SubscriptionactivityService.delete");

		int result = 0;

		try {
			Subscriptionactivity subscriptionactivity = em.find(Subscriptionactivity.class, id);

			if (subscriptionactivity != null) {
				/*
				 * TRANSACTION BEGIN
				 */
				userTransaction.begin();

				em.remove(subscriptionactivity);
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
	 * The findAll interaction returns all Server Activity records sorted by id
	 * ascending.
	 *
	 * @return <code>List<Subscriptionactivity></code>
	 * @throws Exception
	 */
	public List<Subscriptionactivity> findAll() throws Exception {

		log.fine("[START] SubscriptionactivityService.findAll");

		List<Subscriptionactivity> result = new ArrayList<Subscriptionactivity>();

		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Subscriptionactivity> criteria = cb.createQuery(Subscriptionactivity.class);
			Root<Subscriptionactivity> rootSubscriptionactivity = criteria.from(Subscriptionactivity.class);

			criteria.select(rootSubscriptionactivity).orderBy(cb.asc(rootSubscriptionactivity.get("id")));

			result = em.createQuery(criteria).getResultList();

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return result;
	}

	/**
	 * Get the Server Activity records for a specific Subscription Id
	 *
	 * @param subscriptionId
	 * @return <code>List<Subscriptionactivity></code>
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<Subscriptionactivity> findSubscriptionactivityById(String subscriptionId) throws Exception {

		log.fine("[START] SubscriptionactivityService.findSubscriptionactivityById");

		List<Subscriptionactivity> result = new ArrayList<Subscriptionactivity>();

		Query subscriptionactivityQuery = null;

		/*
		 * Generate the Subscriptionactivity list of all records for a basePath value; should
		 * only be one
		 */
		try {
			subscriptionactivityQuery = em.createNamedQuery("findSubscriptionactivityById").setParameter("subscriptionId",
					subscriptionId);

			result = (List<Subscriptionactivity>) subscriptionactivityQuery.getResultList();

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return result;

	}

}
