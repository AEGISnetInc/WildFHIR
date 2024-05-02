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

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.metadata.ResourcemetadataProxy;
import net.aegis.fhir.service.metadata.ResourcemetadataProxyObjectFactory;
import net.aegis.fhir.service.util.ServicesUtil;

/**
 * Resource services for basic data operations: create, delete, read and update.
 *
 * The @Stateless annotation eliminates the need for manual transaction demarcation
 *
 * @author richard.ettema
 *
 */
@Stateless
public class ResourcemetadataService {

	@Inject
	private Logger log;

    @PersistenceContext
	private EntityManager em;

	@Inject
	private Event<Resourcemetadata> resourcemetadataSvc;

    /**
	 * Create a single instance of resourcemetadata
	 *
	 * @param resourcemetadata
	 * @return <code>Resourcemetadata</code>
	 * @throws Exception
	 */
	public Resourcemetadata create(Resourcemetadata resourcemetadata) throws Exception {

		log.fine("[START] ResourcemetadataService.create");

		try {
			resourcemetadata.setId(null);

			em.persist(resourcemetadata);

			resourcemetadataSvc.fire(resourcemetadata);
		} catch (Exception e) {
			// Exception caught
			log.severe(e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return resourcemetadata;
	}

	/**
	 * Delete a Resourcemetadata instance
	 *
	 * @param id
	 * @return integer - 0 failure; 1 success
	 * @throws Exception
	 */
	public int delete(Integer id) throws Exception {

		log.fine("[START] ResourcemetadataService.delete");

		int result = 0;

		try {
			Resourcemetadata resourcemetadata = em.find(Resourcemetadata.class, id);

			if (resourcemetadata != null) {
				em.remove(resourcemetadata);
				result = 1;
			}
		} catch (Exception e) {
			// Exception caught
			log.severe(e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return result;
	}

	/**
	 * Return the Resourcemetadata instance by the primary key identifier
	 *
	 * @param id
	 * @return <code>Resourcemetadata</code>
	 * @throws Exception
	 */
	public Resourcemetadata read(Integer id) throws Exception {

		log.fine("[START] ResourcemetadataService.read");

		Resourcemetadata resourcemetadata = null;

		try {
			resourcemetadata = em.find(Resourcemetadata.class, id);

		} catch (Exception e) {
			// Exception caught
			log.severe(e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return resourcemetadata;
	}

	/**
	 * Create new Resourcemetadata instances for a Resource
	 *
	 * @param resource
	 * @param resourcemetadataList
	 * @return integer - 0 failure; 1 success
	 * @throws Exception
	 */
	public int createAllForResource(net.aegis.fhir.model.Resource resource, List<Resourcemetadata> resourcemetadataList) throws Exception {

		log.fine("[START] ResourcemetadataService.createAllForResource");

		int result = 0;

		try {
			if (resource != null && resource.getId() != null) {
				for (Resourcemetadata resourcemetadata : resourcemetadataList) {

					if (resourcemetadata.getResource() == null || resourcemetadata.getResource().getId() == null) {
						resourcemetadata.setResource(resource);
					}

					create(resourcemetadata);
				}
			}

			// Success if we made it this far...
			result = 1;
		} catch (Exception e) {
			// Exception caught
			log.severe(e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return result;
	}

	/**
	 * Delete all Resourcemetadata for the Resource
	 *
	 * @param resource
	 * @return integer - 0 failure; 1 success
	 * @throws Exception
	 */
	public int deleteAllForResource(net.aegis.fhir.model.Resource resource) throws Exception {

		log.fine("[START] ResourcemetadataService.deleteAllForResource");

		int result = 0;

		try {
			if (resource != null && resource.getId() != null) {
				List<Resourcemetadata> resourcemetadataList = readAllForResource(resource);

				for (Resourcemetadata resourcemetadata : resourcemetadataList) {
					delete(resourcemetadata.getId());
				}
			}

			// Success if we made it this far...
			result = 1;
		} catch (Exception e) {
			// Exception caught
			log.severe(e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return result;
	}

	/**
	 * Return the List of Resourcemetadata instances for a single Resource
	 *
	 * @param resource
	 * @return <code>List<Resourcemetadata></code>
	 * @throws Exception
	 */
	public List<Resourcemetadata> readAllForResource(net.aegis.fhir.model.Resource resource) throws Exception {

		log.fine("[START] ResourcemetadataService.readAllForResource");

		List<Resourcemetadata> resourcemetadataList = null;

		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Resourcemetadata> criteria = cb.createQuery(Resourcemetadata.class);
			Root<Resourcemetadata> resourcemetadataRoot = criteria.from(Resourcemetadata.class);

			criteria.select(resourcemetadataRoot)
				.where(cb.equal(resourcemetadataRoot.get("resource").get("id"), resource.getId()));

			resourcemetadataList = em.createQuery(criteria).getResultList();

		} catch (Exception e) {
			// Exception caught
			log.severe(e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return resourcemetadataList;
	}

	/**
	 * Generate the list of Resourcemetadata objects to persist; one for each non-null valid resource metadata value
	 *
	 * @param resource
	 * @return <code>List<Resourcemetadata></code>
	 * @throws Exception
	 */
	public List<Resourcemetadata> generateAllForResource(net.aegis.fhir.model.Resource resource, String baseUrl, ResourceService resourceService) throws Exception {

		log.fine("[START] ResourcemetadataService.generateAllForResource");

		List<Resourcemetadata> resourcemetadataList = new ArrayList<Resourcemetadata>();

		/*
		 * Generate the Resourcemetadata list based on the Resource Type
		 */
		try {
			// Remove Resource type if present from baseUrl
			String baseUrl2 = baseUrl;
			String baseUrlResourceType = ServicesUtil.INSTANCE.getResourceTypeFromReference(baseUrl);
			if (baseUrlResourceType != null) {
				baseUrl2 = ServicesUtil.INSTANCE.extractBaseURL(baseUrl, baseUrlResourceType);
			}

			ResourcemetadataProxyObjectFactory objectFactory = new ResourcemetadataProxyObjectFactory();

			ResourcemetadataProxy proxy = objectFactory.getResourcemetadataProxy(resource.getResourceType());

			if (proxy != null) {
				resourcemetadataList = proxy.generateAllForResource(resource, baseUrl2, resourceService);
			}

		} catch (Exception e) {
			// Exception caught
			log.severe(e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return resourcemetadataList;
	}

	/**
	 * Generate the list of distinct Resourcemetadata objects for each assigned FHIR tag within the repository
	 *
	 * @return <code>List<Resourcemetadata></code>
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<Resourcemetadata> findAllDistinctTags() throws Exception {

		log.fine("[START] ResourcemetadataService.findAllDistinctTags");

		Query resourcemetadataQuery = null;
		List<Resourcemetadata> resourcemetadataList = new ArrayList<Resourcemetadata>();

		/*
		 * Generate the Resourcemetadata list of distinct tags for a resource type
		 */
		try {
			// Build native query based on resource type and parameters
			StringBuffer sbQuery = new StringBuffer("select distinct 0, 0, rm.paramName, rm.paramType, rm.paramValue, rm.systemValue, rm.codeValue");
			sbQuery.append(" from resourcemetadata rm");
			sbQuery.append(" where rm.paramType = 'tag'");
			sbQuery.append(" order by rm.paramName, rm.paramValue");

			log.info("Native Query: " + sbQuery.toString());

			resourcemetadataQuery = em.createNativeQuery(sbQuery.toString(), Resourcemetadata.class);

			resourcemetadataList = (List<Resourcemetadata>) resourcemetadataQuery.getResultList();

		} catch (Exception e) {
			// Exception caught
			log.severe(e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return resourcemetadataList;

	}

	/**
	 * Generate the list of distinct Resourcemetadata objects for each assigned FHIR tag to a known resource type
	 *
	 * @param resourceType
	 * @return <code>List<Resourcemetadata></code>
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<Resourcemetadata> findAllDistinctTagsByResourceType(String resourceType) throws Exception {

		log.fine("[START] ResourcemetadataService.findAllDistinctTagsByResourceType");

		Query resourcemetadataQuery = null;
		List<Resourcemetadata> resourcemetadataList = new ArrayList<Resourcemetadata>();

		/*
		 * Generate the Resourcemetadata list of distinct tags for a resource type
		 */
		try {
			// Build native query based on resource type and parameters
			StringBuffer sbQuery = new StringBuffer("select distinct 0, 0, rm.paramName, rm.paramType, rm.paramValue, rm.systemValue, rm.codeValue");
			sbQuery.append(" from resourcemetadata rm");
			sbQuery.append(" where rm.resourceJoinId in");
			sbQuery.append("   (select r1.resourceId from resource r1");
			sbQuery.append("    where r1.versionId =");
			sbQuery.append("      (select max(r2.versionId) from resource r2 where r2.resourceId = r1.resourceId)");
			sbQuery.append("    and r1.status <> 'deleted'");
			sbQuery.append("    and r1.resourceType = '").append(resourceType).append("')");
			sbQuery.append(" and rm.paramType = 'tag'");
			sbQuery.append(" order by rm.paramName, rm.paramValue");

			log.info("Native Query: " + sbQuery.toString());

			resourcemetadataQuery = em.createNativeQuery(sbQuery.toString(), Resourcemetadata.class);

			resourcemetadataList = (List<Resourcemetadata>) resourcemetadataQuery.getResultList();

		} catch (Exception e) {
			// Exception caught
			log.severe(e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return resourcemetadataList;

	}

	/**
	 * Generate the list of Resourcemetadata objects to persist; one for each assigned FHIR tag to a known resource
	 *
	 * @param resource
	 * @return <code>List<Resourcemetadata></code>
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<Resourcemetadata> findAllTagsByResource(net.aegis.fhir.model.Resource resource) throws Exception {

		log.fine("[START] ResourcemetadataService.findAllTagsByResource");

		Query resourcemetadataQuery = null;
		List<Resourcemetadata> resourcemetadataList = new ArrayList<Resourcemetadata>();

		/*
		 * Generate the Resourcemetadata list of all tags for a Resource
		 */
		try {
			resourcemetadataQuery = em.createNamedQuery("findAllTagsByResource").setParameter("resourceId", resource.getResourceId());

			resourcemetadataList = (List<Resourcemetadata>) resourcemetadataQuery.getResultList();

		} catch (Exception e) {
			// Exception caught
			log.severe(e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return resourcemetadataList;

	}

	/**
	 * Generate the list of Resourcemetadata objects to persist; one for each assigned FHIR General tag to a known resource
	 *
	 * @param resource
	 * @return <code>List<Resourcemetadata></code>
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<Resourcemetadata> findAllGeneralTagsByResource(net.aegis.fhir.model.Resource resource) throws Exception {

		log.fine("[START] ResourcemetadataService.findAllGeneralTagsByResource");

		Query resourcemetadataQuery = null;
		List<Resourcemetadata> resourcemetadataList = new ArrayList<Resourcemetadata>();

		/*
		 * Generate the Resourcemetadata list of all general tags for a Resource
		 */
		try {
			resourcemetadataQuery = em.createNamedQuery("findAllGeneralTagsByResource").setParameter("resourceId", resource.getResourceId());

			resourcemetadataList = (List<Resourcemetadata>) resourcemetadataQuery.getResultList();

		} catch (Exception e) {
			// Exception caught
			log.severe(e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return resourcemetadataList;

	}

	/**
	 * Generate the list of Resourcemetadata objects to persist; one for each assigned FHIR Profile tag to a known resource
	 *
	 * @param resource
	 * @return <code>List<Resourcemetadata></code>
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<Resourcemetadata> findAllProfileTagsByResource(net.aegis.fhir.model.Resource resource) throws Exception {

		log.fine("[START] ResourcemetadataService.findAllProfileTagsByResource");

		Query resourcemetadataQuery = null;
		List<Resourcemetadata> resourcemetadataList = new ArrayList<Resourcemetadata>();

		/*
		 * Generate the Resourcemetadata list of all profile tags for a Resource
		 */
		try {
			resourcemetadataQuery = em.createNamedQuery("findAllProfileTagsByResource").setParameter("resourceId", resource.getResourceId());

			resourcemetadataList = (List<Resourcemetadata>) resourcemetadataQuery.getResultList();

		} catch (Exception e) {
			// Exception caught
			log.severe(e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return resourcemetadataList;

	}

	/**
	 * Generate the list of Resourcemetadata objects to persist; one for each assigned FHIR Security tag to a known resource
	 *
	 * @param resource
	 * @return <code>List<Resourcemetadata></code>
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<Resourcemetadata> findAllSecurityTagsByResource(net.aegis.fhir.model.Resource resource) throws Exception {

		log.fine("[START] ResourcemetadataService.findAllSecurityTagsByResource");

		Query resourcemetadataQuery = null;
		List<Resourcemetadata> resourcemetadataList = new ArrayList<Resourcemetadata>();

		/*
		 * Generate the Resourcemetadata list of all security tags for a Resource
		 */
		try {
			resourcemetadataQuery = em.createNamedQuery("findAllSecurityTagsByResource").setParameter("resourceId", resource.getResourceId());

			resourcemetadataList = (List<Resourcemetadata>) resourcemetadataQuery.getResultList();

		} catch (Exception e) {
			// Exception caught
			log.severe(e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return resourcemetadataList;

	}

	/**
	 * Generate the list of Resourcemetadata objects for a specific parameter name and a known resource id
	 *
	 * @param resourceId
	 * @param paramName
	 * @return <code>List<Resourcemetadata></code>
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<Resourcemetadata> findMetadataByResourceIdTypeParam(String resourceId, String resourceType, String paramName) throws Exception {

		log.fine("[START] ResourcemetadataService.findMetadataByResourceIdTypeParam");

		Query resourcemetadataQuery = null;
		List<Resourcemetadata> resourcemetadataList = new ArrayList<Resourcemetadata>();

		/*
		 * Generate the Resourcemetadata list of all tags for a Resource
		 */
		try {
			resourcemetadataQuery = em.createNamedQuery("findParamByResource").setParameter("resourceId", resourceId).setParameter("resourceType", resourceType).setParameter("paramName", paramName);

			resourcemetadataList = (List<Resourcemetadata>) resourcemetadataQuery.getResultList();

		} catch (Exception e) {
			// Exception caught
			log.severe(e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return resourcemetadataList;

	}

	/**
	 * Generate the list of Resourcemetadata objects for a specific parameter name and a known resource id
	 *
	 * @param resourceId
	 * @param paramName
	 * @return <code>List<Resourcemetadata></code>
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<Resourcemetadata> findMetadataByResourceIdTypeLevel1Param(String resourceId, String resourceType) throws Exception {

		log.fine("[START] ResourcemetadataService.findMetadataByResourceIdTypeLevel1Param");

		Query resourcemetadataQuery = null;
		List<Resourcemetadata> resourcemetadataList = new ArrayList<Resourcemetadata>();

		/*
		 * Generate the Resourcemetadata list of all tags for a Resource
		 */
		try {
			resourcemetadataQuery = em.createNamedQuery("findLevel1ParamByResource").setParameter("resourceId", resourceId).setParameter("resourceType", resourceType);

			resourcemetadataList = (List<Resourcemetadata>) resourcemetadataQuery.getResultList();

		} catch (Exception e) {
			// Exception caught
			log.severe(e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return resourcemetadataList;

	}

	/**
	 *
	 * @param name
	 * @param value
	 * @return Generated <code>Resourcemetadata</code>
	 */
	protected Resourcemetadata generateResourcemetadata(net.aegis.fhir.model.Resource resource, String name, String value) {

		log.fine("[START] ResourcemetadataService.generateResourcemetadata(" + name + ", " + value + ")");

		Resourcemetadata r = new Resourcemetadata();

		r.setResource(resource);
		r.setParamName(name);
		r.setParamValue(value);
		r.setParamValueU(value);

		return r;
	}
}
