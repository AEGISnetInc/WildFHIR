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

import java.util.ArrayList;
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
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.UserTransaction;

import net.aegis.fhir.model.Code;

/**
 * Code services for basic data operations: findAll, create, delete, read and update
 * and extended operations: findAllByCodeType, findCodeByTypeName and boolean checks.
 *
 * The @Stateless annotation eliminates the need for manual transaction demarcation
 *
 * @author richard.ettema
 *
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class CodeService {

    private Logger log = Logger.getLogger("CodeService");

    @PersistenceContext
	private EntityManager em;

    @Resource
    private UserTransaction userTransaction;

	@Inject
	private Event<Code> codeEventSrc;

	/**
	 * The create interaction creates a new Code record.
	 *
	 * @param code
	 * @return <code>Code</code>
	 * @throws Exception
	 */
	public Code create(Code code) throws Exception {

		log.fine("[START] CodeService.create");

		Code newCode = null;

		try {
			newCode = code.clone(false);

			/*
			 *  TRANSACTION BEGIN
			 */
			userTransaction.begin();

			em.persist(newCode);

			codeEventSrc.fire(newCode);

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

		return newCode;
	}

	/**
	 * The delete interaction deletes a Code record.
	 *
	 * @param id
	 * @throws Exception
	 */
	public int delete(Integer id) throws Exception {

		log.fine("[START] CodeService.delete");

		int result = 0;

		try {
			/*
			 *  TRANSACTION BEGIN
			 */
			userTransaction.begin();

			Code code = em.find(Code.class, id);

			if (code != null) {
				em.remove(code);
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

	/**
	 * The read interaction returns a single Code record.
	 *
	 * @param id
	 * @return <code>Code</code>
	 * @throws Exception
	 */
	public Code read(Integer id) throws Exception {

		log.fine("[START] CodeService.read");

		Code code = null;

		try {
			/*
			 *  TRANSACTION BEGIN
			 */
			userTransaction.begin();

			code = em.find(Code.class, id);

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

		return code;
	}

	/**
	 * The update interaction modifies an existing Code record.
	 *
	 * @param providerdirectory
	 * @return <code>Code</code>
	 * @throws Exception
	 */
	public Code update(Code updateCode) throws Exception {

		log.fine("[START] CodeService.update");

		Code code = null;

		try {
			code = read(updateCode.getId());

			code.setCodeName(updateCode.getCodeName());
			code.setValue(updateCode.getValue());
			code.setIntValue(updateCode.getIntValue());
			code.setDescription(updateCode.getDescription());
			code.setResourceContents(updateCode.getResourceContents());

			/*
			 *  TRANSACTION BEGIN
			 */
			userTransaction.begin();

			em.merge(code);
			codeEventSrc.fire(code);

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

		return code;
	}

	/**
	 * The findAll interaction returns all Code records sorted by codeType, codeName and value.
	 *
	 * @return <code>List<Code></code>
	 * @throws Exception
	 */
	public List<Code> findAll() throws Exception {

		log.fine("[START] CodeService.findAll");

		List<Code> result = new ArrayList<Code>();

		try {
			/*
			 *  TRANSACTION BEGIN
			 */
			userTransaction.begin();

			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Code> criteria = cb.createQuery(Code.class);
			Root<Code> rootCode = criteria.from(Code.class);

			criteria.select(rootCode)
				.orderBy(cb.asc(rootCode.get("id")));

			result = em.createQuery(criteria).getResultList();

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

	/**
	 * The findCodeByName interaction returns the Code record for a given codeName.
	 *
	 * @param codeName
	 * @return <code>Code</code>
	 * @throws Exception
	 */
	public Code findCodeByName(String codeName) throws Exception {

		log.fine("[START] CodeService.findCodeByName");

		List<Code> result = new ArrayList<Code>();
		Code resultCode = null;

		try {
			/*
			 *  TRANSACTION BEGIN
			 */
			userTransaction.begin();

			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Code> criteria = cb.createQuery(Code.class);
			Root<Code> rootCode = criteria.from(Code.class);
			List<Predicate> predicateList = new ArrayList<Predicate>();
			predicateList.add(cb.equal(rootCode.get("codeName"), codeName));

			criteria.select(rootCode)
				.where(cb.and(predicateList.toArray(new Predicate[predicateList.size()])))
				.orderBy(cb.asc(rootCode.get("codeName")), cb.asc(rootCode.get("value")));

			result = em.createQuery(criteria).getResultList();

			if (result != null && result.size() > 0) {
				// Return the first found Code; should only be one
				resultCode = result.get(0);
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

		return resultCode;
	}

	/**
	 * The findCodeValueByName interaction returns the string value for a given codeName.
	 *
	 * @param codeName
	 * @return String code value
	 * @throws Exception
	 */
	public String findCodeValueByName(String codeName) throws Exception {

		log.fine("[START] CodeService.findCodeValueByName");

		List<Code> result = new ArrayList<Code>();
		Code resultCode = null;
		String codeValue = null;

		try {
			/*
			 *  TRANSACTION BEGIN
			 */
			userTransaction.begin();

			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Code> criteria = cb.createQuery(Code.class);
			Root<Code> rootCode = criteria.from(Code.class);
			List<Predicate> predicateList = new ArrayList<Predicate>();
			predicateList.add(cb.equal(rootCode.get("codeName"), codeName));

			criteria.select(rootCode)
				.where(cb.and(predicateList.toArray(new Predicate[predicateList.size()])))
				.orderBy(cb.asc(rootCode.get("codeName")), cb.asc(rootCode.get("value")));

			result = em.createQuery(criteria).getResultList();

			if (result != null && result.size() > 0) {
				// Return the first found Code; should only be one
				resultCode = result.get(0);
				codeValue = resultCode.getValue();
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

		return codeValue;
	}

	/**
	 * The findCodeIntValueByName interaction returns the integer value for a given codeName.
	 *
	 * @param codeName
	 * @return Integer code int value
	 * @throws Exception
	 */
	public Integer findCodeIntValueByName(String codeName) throws Exception {

		log.fine("[START] CodeService.findCodeIntValueByName");

		List<Code> result = new ArrayList<Code>();
		Code resultCode = null;
		Integer codeIntValue = null;

		try {
			/*
			 *  TRANSACTION BEGIN
			 */
			userTransaction.begin();

			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Code> criteria = cb.createQuery(Code.class);
			Root<Code> rootCode = criteria.from(Code.class);
			List<Predicate> predicateList = new ArrayList<Predicate>();
			predicateList.add(cb.equal(rootCode.get("codeName"), codeName));

			criteria.select(rootCode)
				.where(cb.and(predicateList.toArray(new Predicate[predicateList.size()])))
				.orderBy(cb.asc(rootCode.get("codeName")), cb.asc(rootCode.get("value")));

			result = em.createQuery(criteria).getResultList();

			if (result != null && result.size() > 0) {
				// Return the first found Code; should only be one
				resultCode = result.get(0);
				codeIntValue = resultCode.getIntValue();
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

		return codeIntValue;
	}

	/**
	 * @param codeName
	 * @return boolean - true, if boolean code value is supported; false, otherwise
	 * @throws Exception
	 */
	public boolean isSupported(String codeName) throws Exception {
		boolean result = false;

		Code code = this.findCodeByName(codeName);

		if (code != null && code.getValue().equals("true")) {
			result = true;
		}

		return result;
	}

	/**
	 * @return boolean - true, if code value equals argument value; false, otherwise
	 * @throws Exception
	 */
	public boolean isValueSupported(String codeName, String codeValue) throws Exception {
		boolean result = false;

		Code code = this.findCodeByName(codeName);

		if (code != null && code.getValue().equals(codeValue)) {
			result = true;
		}

		return result;
	}

	/**
	 * @return String - code value for code name
	 * @throws Exception
	 */
	public String getCodeValue(String codeName) throws Exception {
		String result = null;

		Code code = this.findCodeByName(codeName);

		if (code != null && !code.getValue().isEmpty()) {
			result = code.getValue();
		}

		return result;
	}

	/**
	 * @return byte[] - MedMij $medication-overview default Organization resource
	 * @throws Exception
	 */
	public byte[] getCodeResourceContents(String codeName) throws Exception {
		byte[] result = null;

		Code code = this.findCodeByName(codeName);

		if (code != null && code.getResourceContents() != null) {
			result = code.getResourceContents();
		}

		return result;
	}

//	/**
//	 * @return String - Response FHIR Version appended to response Content-Type header
//	 * @throws Exception
//	 */
//	public String getResponseFhirVersion() {
//
//		try {
//			if (ServicesUtil.INSTANCE.getResponseFhirVersion() == null) {
//				Code supportedVersionsCode = this.findCodeByName("supportedVersions");
//				if (supportedVersionsCode != null) {
//					ServicesUtil.INSTANCE.setResponseFhirVersion("; fhirVersion=" + supportedVersionsCode.getValue());
//				}
//				else {
//					ServicesUtil.INSTANCE.setResponseFhirVersion("");
//				}
//			}
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		return ServicesUtil.INSTANCE.getResponseFhirVersion();
//	}

}
