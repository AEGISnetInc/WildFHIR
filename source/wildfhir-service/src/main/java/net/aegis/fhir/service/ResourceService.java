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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
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
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Status;
import javax.transaction.UserTransaction;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.hl7.fhir.r4.formats.IParser.OutputStyle;
import org.hl7.fhir.r4.formats.JsonParser;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntryRequestComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntryResponseComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntrySearchComponent;
import org.hl7.fhir.r4.model.Bundle.BundleLinkComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.Bundle.SearchEntryMode;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.Subscription;
import org.hl7.fhir.r4.model.Subscription.SubscriptionStatus;

import net.aegis.fhir.model.ResourceContainer;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.paging.PagingHistoryManager;
import net.aegis.fhir.service.paging.PagingSearchManager;
import net.aegis.fhir.service.util.JsonPatchUtil;
import net.aegis.fhir.service.util.NullChecker;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.SummaryUtil;
import net.aegis.fhir.service.util.UTCDateUtil;
import net.aegis.fhir.service.util.UUIDUtil;
import net.aegis.fhir.service.util.XmlPatchUtil;

/**
 * Resource services for basic data operations: create, delete, read and update.
 *
 * The @Stateless annotation eliminates the need for manual transaction demarcation
 *
 * @author richard.ettema
 *
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class ResourceService {

    private Logger log = Logger.getLogger("ResourceService");

    @PersistenceContext
	private EntityManager em;

    @Resource
    private UserTransaction userTransaction;

    @Inject
	private Event<net.aegis.fhir.model.Resource> resourceEventSrc;

	@Inject
	private CodeService codeService;

	@Inject
	private ResourcemetadataService resourcemetadataService;

	@Inject
	private UTCDateUtil utcDateUtil;

	/**
	 * Used for Patient $match operation
	 *
	 * @param resource
	 * @param baseUrl
	 * @return List of generated <code>Resourcemetadata</code>
	 * @throws Exception
	 */
	public List<Resourcemetadata> getResourcemetadataMatch(org.hl7.fhir.r4.model.Resource resource, String baseUrl) throws Exception {

		log.fine("[START] ResourceService.getResourcemetadata");

		net.aegis.fhir.model.Resource wildfhirResource = new net.aegis.fhir.model.Resource();

		XmlParser xmlP = new XmlParser();
		xmlP.setOutputStyle(OutputStyle.PRETTY);
		byte[] resourceBytes = xmlP.composeBytes(resource);

		wildfhirResource.setResourceType(resource.getResourceType().name() + "Match");
		wildfhirResource.setResourceContents(resourceBytes);

		// Generate the list of Resourcemetadata objects for the Resource
		List<Resourcemetadata> resourcemetadataList = resourcemetadataService.generateAllForResource(wildfhirResource, baseUrl, this);

		return resourcemetadataList;

	}

	/**
	 * The create interaction creates a new resource in a server assigned location. If the client wishes to have control
	 * over the id of a newly submitted resource, it should use the update interaction instead. The create interaction
	 * is performed by an HTTP POST command.
	 *
	 * @param resource
	 * @param resourceId
	 * @param baseUrl
	 * @return <code>ResourceContainer</code>
	 * @throws Exception
	 */
	public ResourceContainer create(net.aegis.fhir.model.Resource resource, String resourceId, String baseUrl) throws Exception {

		log.fine("[START] ResourceService.create");

		ResourceContainer resourceContainer = new ResourceContainer();

		try {
			/*
			 * resourceId is now a VARCHAR(255); using GUID values now
			 */
			String nextResourceIdString = null;
			int nextVersionId = 1;

			if (resourceId == null) {
				nextResourceIdString = UUIDUtil.getGUID();
				log.info("Next Resource Id String is " + nextResourceIdString);
			}
			else {
				nextResourceIdString = resourceId;

				// Check for existing, deleted version of this resource
				ResourceContainer readResourceContainer = this.read(resource.getResourceType(), resourceId, null);
				if (readResourceContainer != null && readResourceContainer.getResource() != null && readResourceContainer.getResource().getVersionId() != null) {
					nextVersionId = readResourceContainer.getResource().getVersionId().intValue() + 1;
				}
			}

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

			/*
			 * Subscription Framework - check for Subscription resource type and status of 'requested'
			 * if found and SF enabled, change status to 'active'
			 */
			if (codeService.isSupported("subscriptionServiceEnabled")) {
				if (resourceObject.getResourceType().equals(ResourceType.Subscription)) {
					Subscription subscription = (Subscription)resourceObject;
					if (subscription.getStatus().equals(SubscriptionStatus.REQUESTED)) {
						subscription.setStatus(SubscriptionStatus.ACTIVE);
					}
				}
			}

			byte[] resourceBytes = xmlP.composeBytes(resourceObject);

			newResource.setResourceContents(resourceBytes);

			/*
			 *  TRANSACTION BEGIN
			 */
			userTransaction.begin();
			em.persist(newResource);
			resourceEventSrc.fire(newResource);

			// Generate the list of Resourcemetadata objects for the new Resource
			List<Resourcemetadata> resourcemetadataList = resourcemetadataService.generateAllForResource(newResource, baseUrl, this);

			// Create the new Resourcemetadata objects for the new Resource
			resourcemetadataService.createAllForResource(newResource, resourcemetadataList);

			/*
			 *  TRANSACTION COMMIT(END)
			 */
			userTransaction.commit();

			resourceContainer.setResponseStatus(Response.Status.CREATED);
			resourceContainer.setResource(newResource);

		} catch (Exception e) {
			log.severe(e.getMessage());
			// Exception caught
			resourceContainer.setResource(null);
			resourceContainer.setResponseStatus(Response.Status.INTERNAL_SERVER_ERROR);
			resourceContainer.setMessage(e.getMessage());
		}

		return resourceContainer;
	}

	/**
	 * Purge a Resource instance
	 *
	 * @param id
	 * @return integer - 0 failure; 1 success
	 * @throws Exception
	 */
	public int purge(Integer id) throws Exception {

		log.fine("[START] ResourceService.purge (DB)");

		int result = 0;

		try {
			/*
			 *  TRANSACTION BEGIN
			 */
			userTransaction.begin();

			net.aegis.fhir.model.Resource resource = em.find(net.aegis.fhir.model.Resource.class, id);

			if (resource != null) {
				log.info("     Resource found - GET ALL RESOURCE HISTORY");

				// Build and execute query to return all of the resource version rows
				List<net.aegis.fhir.model.Resource> resourceList = readAllHistoryForResource(resource);

				// Delete all resource history rows
				for (net.aegis.fhir.model.Resource resourceInstance : resourceList) {
					log.info("     Resource found - DELETE RESOURCE HISTORY [" + resourceInstance.getId() + "]");

					List<Resourcemetadata> resourcemetadataList = resourcemetadataService.readAllForResource(resourceInstance);

					for (Resourcemetadata resourcemetadata : resourcemetadataList) {
						deleteResourcemetadata(resourcemetadata.getId());
					}

					em.remove(resourceInstance);
					resourceEventSrc.fire(resourceInstance);
				}

				result = 1;
			}

			/*
			 *  TRANSACTION COMMIT(END)
			 */
			userTransaction.commit();

		} catch (Exception e) {
			// Exception caught
			log.severe(e.getMessage());
			throw e;
		}

		return result;
	}

	/**
	 * Execute a DDL truncate on the resource and resourcemetadata tables
	 *
	 * @return <code>int</code> Number of entities updated or deleted
	 * @throws Exception
	 */
	public int resourcePurgeAll() throws Exception {

		log.fine("[START] ResourcemetadataService.resourcePurgeAll");

		Query resourcemetadataQuery = null;
		int result = 0;

		try {
			/*
			 *  TRANSACTION BEGIN
			 */
			userTransaction.begin();

			// Build native query for drop foreign key fk_resourcemetatdata_resource
			// MUST FIRST DROP FOREIGN KEY CONSTRAINT
			StringBuffer sbQuery = new StringBuffer("alter table resourcemetadata drop foreign key fk_resourcemetatdata_resource");

			log.info("Native Query: " + sbQuery.toString());

			resourcemetadataQuery = em.createNativeQuery(sbQuery.toString());

			result = resourcemetadataQuery.executeUpdate();

			// Build native query for drop index fk_resourcemetatdata_resource_idx
			sbQuery = new StringBuffer("alter table resourcemetadata drop index fk_resourcemetatdata_resource_idx");

			log.info("Native Query: " + sbQuery.toString());

			resourcemetadataQuery = em.createNativeQuery(sbQuery.toString());

			result = resourcemetadataQuery.executeUpdate();

			// Build native query for truncate resourcemetadata
			sbQuery = new StringBuffer("truncate resourcemetadata");

			log.info("Native Query: " + sbQuery.toString());

			resourcemetadataQuery = em.createNativeQuery(sbQuery.toString());

			result = resourcemetadataQuery.executeUpdate();

			// Build native query for truncate resource
			sbQuery = new StringBuffer("truncate resource");

			log.info("Native Query: " + sbQuery.toString());

			resourcemetadataQuery = em.createNativeQuery(sbQuery.toString());

			result = resourcemetadataQuery.executeUpdate();

			// Build native query to re-create foreign key fk_resourcemetatdata_resource
			sbQuery = new StringBuffer("alter table resourcemetadata add constraint fk_resourcemetatdata_resource foreign key (resourcejoinid) references resource (id)");

			log.info("Native Query: " + sbQuery.toString());

			resourcemetadataQuery = em.createNativeQuery(sbQuery.toString());

			result = resourcemetadataQuery.executeUpdate();

			// Build native query to re-create index fk_resourcemetatdata_resource_idx
			sbQuery = new StringBuffer("create index fk_resourcemetatdata_resource_idx on resourcemetadata (resourcejoinid asc)");

			log.info("Native Query: " + sbQuery.toString());

			resourcemetadataQuery = em.createNativeQuery(sbQuery.toString());

			result = resourcemetadataQuery.executeUpdate();

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
	 * Delete a Resourcemetadata instance
	 *
	 * @param id
	 * @return integer - 0 failure; 1 success
	 * @throws Exception
	 */
	public int deleteResourcemetadata(Integer id) throws Exception {

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
			throw e;
		}

		return result;
	}

	/**
	 * Delete a Resource instance
	 *
	 * @param id
	 * @return integer - 0 failure; 1 success
	 * @throws Exception
	 */
	public int delete(Integer id) throws Exception {

		log.fine("[START] ResourceService.delete (DB)");

		int result = 0;

		try {
			net.aegis.fhir.model.Resource resource = em.find(net.aegis.fhir.model.Resource.class, id);

			if (resource != null) {
				em.remove(resource);
				result = 1;
			}
		} catch (Exception e) {
			// Exception caught
			log.severe(e.getMessage());
			throw e;
		}

		return result;
	}

	/**
	 * The delete interaction removes an existing resource. The interaction is performed by an HTTP DELETE command.
	 * Deleting a resource is simply updating the current resource to create a new version with a status of 'DELETED'.
	 *
	 * @param resourceId
	 * @return <code>ResourceContainer</code>
	 * @throws Exception
	 */
	public ResourceContainer delete(String resourceType, String resourceId) throws Exception {

		log.fine("[START] ResourceService.delete");

		// Get the current version of the resource
		ResourceContainer resourceContainer = read(resourceType, resourceId, null);

		// Check the status of the resource to determine the action to perform
		if (resourceContainer.getResponseStatus().equals(Response.Status.NOT_FOUND)) {
			// the resource was not found; update the response status to no content
			resourceContainer.setResponseStatus(Response.Status.NO_CONTENT);

		}
		else if (resourceContainer.getResponseStatus().equals(Response.Status.GONE)) {
			// the current version of this resource is already deleted; update the response status to ok
			resourceContainer.setResponseStatus(Response.Status.OK);

		}
		else if (resourceContainer.getResponseStatus().equals(Response.Status.OK)) {
			net.aegis.fhir.model.Resource currentResource = resourceContainer.getResource();

			// Delete the Resourcemetadata objects for the current Resource
			resourcemetadataService.deleteAllForResource(currentResource);

			Date updatedTime = new Date();

			// the current version of this resource is valid; create a new version with a status of "DELETED"
			net.aegis.fhir.model.Resource newResource = new net.aegis.fhir.model.Resource();
			newResource.setResourceId(resourceId);
			Integer newVersion = Integer.valueOf(resourceContainer.getResource().getVersionId().intValue() + 1);
			newResource.setVersionId(newVersion);
			newResource.setResourceType(resourceContainer.getResource().getResourceType());
			newResource.setStatus("deleted");
			newResource.setLastUser("system");
			newResource.setLastUpdate(updatedTime);

			// Convert XML contents to of current Resource object and set id and meta
			ByteArrayInputStream iResource = new ByteArrayInputStream(currentResource.getResourceContents());
			XmlParser xmlP = new XmlParser();
			xmlP.setOutputStyle(OutputStyle.PRETTY);
			org.hl7.fhir.r4.model.Resource resourceObject = xmlP.parse(iResource);

			resourceObject.setId(resourceId);

			Meta resourceMeta = new Meta();
			if (resourceObject.hasMeta()) {
				resourceMeta = resourceObject.getMeta();
			}
			resourceMeta.setVersionId(newVersion.toString());
			resourceMeta.setLastUpdated(updatedTime);
			resourceObject.setMeta(resourceMeta);

			byte[] resourceBytes = xmlP.composeBytes(resourceObject);

			newResource.setResourceContents(resourceBytes);

			/*
			 *  TRANSACTION BEGIN
			 */
			userTransaction.begin();
			em.persist(newResource);
			resourceEventSrc.fire(newResource);

			/*
			 *  TRANSACTION COMMIT(END)
			 */
			userTransaction.commit();

			// set the response status to no content
			resourceContainer.setResponseStatus(Response.Status.NO_CONTENT);

		}

		return resourceContainer;
	}

	/**
	 * The delete multiple interaction is performed via the conditional delete logic and removes an existing resources
	 * based on the passed in list of resource ids. The interaction is performed by an HTTP DELETE command.
	 * Deleting a resource is simply updating the current resource's version with a status of 'DELETED'.
	 *
	 * @param resourceIds
	 * @return <code>ResourceContainer</code>
	 * @throws Exception
	 */
	public ResourceContainer deleteMultiple(String resourceType, List<String> resourceIds) throws Exception {

		log.fine("[START] ResourceService.deleteMultiple");

		ResourceContainer readResourceContainer = null;

		ResourceContainer resourceContainer = new ResourceContainer();
		XmlParser xmlP = new XmlParser();

		try {
			/*
			 *  TRANSACTION BEGIN
			 */
			userTransaction.begin();

			// Iterate through the list of resource ids and perform the delete operation on each one
			for (String resourceId : resourceIds) {
				// Get the current version of the resource
				readResourceContainer = read(resourceType, resourceId, null);

				// Check the status of the resource to determine the action to perform
				if (readResourceContainer.getResponseStatus().equals(Response.Status.NOT_FOUND)) {
					// the resource was not found; skip
				}
				else if (readResourceContainer.getResponseStatus().equals(Response.Status.GONE)) {
					// the current version of this resource is already deleted; skip
				}
				else if (readResourceContainer.getResponseStatus().equals(Response.Status.OK)) {
					net.aegis.fhir.model.Resource currentResource = readResourceContainer.getResource();

					// Delete the Resourcemetadata objects for the current Resource
					resourcemetadataService.deleteAllForResource(currentResource);

					Date updatedTime = new Date();

					// the current version of this resource is valid; create a new version with a status of "DELETED"
					net.aegis.fhir.model.Resource newResource = new net.aegis.fhir.model.Resource();
					newResource.setResourceId(resourceId);
					Integer newVersion = Integer.valueOf(currentResource.getVersionId().intValue() + 1);
					newResource.setVersionId(newVersion);
					newResource.setResourceType(currentResource.getResourceType());
					newResource.setStatus("deleted");
					newResource.setLastUser("system");
					newResource.setLastUpdate(updatedTime);

					// Convert XML contents to of current Resource object and set id and meta
					ByteArrayInputStream iResource = new ByteArrayInputStream(currentResource.getResourceContents());
					xmlP.setOutputStyle(OutputStyle.PRETTY);
					org.hl7.fhir.r4.model.Resource resourceObject = xmlP.parse(iResource);

					resourceObject.setId(resourceId);

					Meta resourceMeta = new Meta();
					if (resourceObject.hasMeta()) {
						resourceMeta = resourceObject.getMeta();
					}
					resourceMeta.setVersionId(newVersion.toString());
					resourceMeta.setLastUpdated(updatedTime);
					resourceObject.setMeta(resourceMeta);

					byte[] resourceBytes = xmlP.composeBytes(resourceObject);

					newResource.setResourceContents(resourceBytes);
					em.persist(newResource);
					resourceEventSrc.fire(newResource);
				}
			}

			/*
			 *  TRANSACTION COMMIT(END)
			 */
			userTransaction.commit();

			// All deletes successfully completed; set resourceContainer response to OK
			resourceContainer.setResponseStatus(Response.Status.OK);
		}
		catch (Exception e) {
			log.severe(e.getMessage());
			// Exception caught
			resourceContainer.setResource(null);
			resourceContainer.setResponseStatus(Response.Status.INTERNAL_SERVER_ERROR);
			resourceContainer.setMessage(e.getMessage());
		}

		return resourceContainer;
	}

	/**
	 * The history interaction retrieves the history of either a particular resource, all resources of a given type,
	 * or all resources supported by the system. Depending on the values of resourceType and resourceId, this method
	 * returns the history for a single resource instance, a resource type, or all supported resource types in the system.
	 *
	 * @param resourceId
	 * @param count_
	 * @param since_
	 * @param page_
	 * @param resourceType
	 * @param authMapPatient
	 * @return <code>ResourceContainer</code>
	 * @throws Exception
	 */
	public ResourceContainer history(String resourceId, Integer count_, Date since_, Integer page_, String locationPath, String resourceType, List<String> authMapPatient) throws Exception {

		log.fine("[START] ResourceService.history() - resourceId: " + resourceId + "; count_: " + count_ + "; since_: " + since_ + "; page_: " + page_ + "; locationPath: " + locationPath + "; resourceType: " + resourceType + "; authMapPatient: " + authMapPatient);

		ResourceContainer resourceContainer = new ResourceContainer();
		List<net.aegis.fhir.model.Resource> resources = null;
		Integer maxCount = Integer.valueOf(500);

		ByteArrayInputStream iResource = null;

		try {
			// Check for paged request; page parameter is not null
			if (page_ != null && page_.intValue() > 0) {
				log.info("ResourceService.history - cached page requested");

				// Retrieve page from history cache using locationPath as the key
				Bundle bundle = PagingHistoryManager.INSTANCE.retrieveFromCache(locationPath);

				if (bundle != null) {
					resourceContainer.setBundle(bundle);
					resourceContainer.setResponseStatus(Response.Status.OK);
				}
				else {
					// Page cache has expired
					resourceContainer.setResource(null);
					resourceContainer.setResponseStatus(Response.Status.NOT_FOUND);
					resourceContainer.setMessage("History page cache has expired.");
				}
			}
			else {
				log.info("ResourceService.history - new history request");

				CriteriaBuilder cb = em.getCriteriaBuilder();
				CriteriaQuery<net.aegis.fhir.model.Resource> criteria = cb.createQuery(net.aegis.fhir.model.Resource.class);
				Root<net.aegis.fhir.model.Resource> resource = criteria.from(net.aegis.fhir.model.Resource.class);
				List<Predicate> predicateList = new ArrayList<Predicate>();

				// History Resource
				if (resourceType != null) {
					predicateList.add(cb.equal(resource.get("resourceType"), resourceType));

					// History Instance
					if (resourceId != null) {
						predicateList.add(cb.equal(resource.get("resourceId"), resourceId));
					}
				}

				// History Global - if both resourceId and resourceType are null

				if (since_ != null) {
					predicateList.add(cb.greaterThan(resource.<Date> get("lastUpdate"), since_));
				}

				criteria.select(resource)
					.where(cb.and(predicateList.toArray(new Predicate[predicateList.size()])))
					.orderBy(cb.desc(resource.get("resourceType")))
					.orderBy(cb.desc(resource.get("resourceId")))
					.orderBy(cb.desc(resource.get("versionId")));

				List<net.aegis.fhir.model.Resource> historyResources = em.createQuery(criteria).getResultList();

				log.info("ResourceService.history - historyResources.size() = " + historyResources.size());

				// Check for Authorization Mapped Patient. If defined, pre-process returned history resources
				// If resourceType not equals 'Patient' or global history, filter results to only include resource instances for authMapPatient
				if (authMapPatient != null && !authMapPatient.isEmpty() && (resourceType == null || !resourceType.equals("Patient"))) {

					int countFilter = 0;
					String patientFilter = null;
					List<net.aegis.fhir.model.Resource> historyFliterResources = new ArrayList<net.aegis.fhir.model.Resource>();
					String resourceContents = null;

					for (String authPatientId : authMapPatient) {
						patientFilter = "Patient/" + authPatientId;
						log.info("ResourceService.history - Authorization pre-process for Patient/" + authPatientId);

						for (net.aegis.fhir.model.Resource r : historyResources) {
							resourceContents = new String(r.getResourceContents());

							if (resourceContents.contains(patientFilter)) {
								countFilter++;
								historyFliterResources.add(r);
							}

							if (countFilter >= maxCount.intValue()) {
								break;
							}
						}
					}

					historyResources = historyFliterResources;
				}

				if (historyResources != null && historyResources.size() > 0) {
					// 1 or more Resources found, build Bundle list of Element entry objects for each resource version

					// Extract base url from locationPath for use in Bundle.entry.fullUrl element
					String baseUrl = ServicesUtil.INSTANCE.extractBaseURL(locationPath, "/_history");
					// Initialize fullUrl for use in populating Bundle.entry.fullUrl element
					//String fullUrl = "";

					if (historyResources.size() > maxCount.intValue()) {
						// Maximum count allowed for is less than number of resources returned; reduce resources to maxCount limit
						log.info("Total resources returned: " + historyResources.size() + "; Maximum count allowed for: " + maxCount);

						int totalCount = 0;
						resources = new ArrayList<net.aegis.fhir.model.Resource>();

						for (net.aegis.fhir.model.Resource r : historyResources) {
							resources.add(r);
							totalCount++;
							if (totalCount >= maxCount.intValue()) {
								break;
							}
						}

					} else {
						resources = historyResources;
					}

					// Check for _count
					int pageSize = 0;
					if (count_ != null && count_.intValue() > 0) {
						// if defined use as page size; may need to support paging
						pageSize = count_.intValue();
					}
					else {
						// if not defined set page size equal to number of found resources; i.e. no paging
						pageSize = resources.size();
					}

					log.info("ResourceService.history - pageSize = " + pageSize);

					// Test whether paging is needed
					boolean needPaging = false;
					int pageCount = 1;
					int pageNum = 1;
					if (pageSize < resources.size()) {
						needPaging = true;
						pageCount = this.divideAndRoundUp(resources.size(), pageSize);
					}

					log.info("ResourceService.history - pageCount = " + pageCount + "; needPaging = " + needPaging);

					String currentPage = locationPath + "&page=1";
					String firstPage = locationPath + "&page=1";
					String lastPage = locationPath + "&page=" + pageCount;
					int nextPageNum = -1;
					int prevPageNum = -1;

					// Initialize first Bundle to return
					Bundle bundle = new Bundle();

					bundle.setId(UUIDUtil.getUUID());
					Meta bundleMeta = new Meta();
					bundleMeta.setVersionId("1");
					bundleMeta.setLastUpdated(new Date());
					bundle.setMeta(bundleMeta);
					bundle.setType(BundleType.HISTORY);
					bundle.setTotal(resources.size());
					BundleLinkComponent selfLink = new BundleLinkComponent();
					selfLink.setRelation("self");
					if (needPaging) {
						selfLink.setUrl(currentPage);
					}
					else {
						selfLink.setUrl(locationPath);
					}
					bundle.getLink().add(selfLink);

					int resourceCount = 0;

					for (net.aegis.fhir.model.Resource resourceEntry : resources) {
						resourceCount++;
						if (resourceCount > pageSize) {

							log.info("ResourceService.history - Done with pageNum = " + pageNum);

							// Reset resourceCount for next page Bundle
							resourceCount = 1;

							// Calculate next and previous pageNum values
							nextPageNum = pageNum + 1;
							prevPageNum = pageNum - 1;

							// Populate paging links in current page Bundle
							BundleLinkComponent firstPageLink = new BundleLinkComponent();
							firstPageLink.setRelation("first");
							firstPageLink.setUrl(firstPage);
							bundle.getLink().add(firstPageLink);

							if (prevPageNum >= 1) {
								BundleLinkComponent prevPageLink = new BundleLinkComponent();
								prevPageLink.setRelation("previous");
								prevPageLink.setUrl(locationPath + "&page=" + prevPageNum);
								bundle.getLink().add(prevPageLink);
							}

							if (nextPageNum <= pageCount) {
								BundleLinkComponent nextPageLink = new BundleLinkComponent();
								nextPageLink.setRelation("next");
								nextPageLink.setUrl(locationPath + "&page=" + nextPageNum);
								bundle.getLink().add(nextPageLink);
							}

							BundleLinkComponent lastPageLink = new BundleLinkComponent();
							lastPageLink.setRelation("last");
							lastPageLink.setUrl(lastPage);
							bundle.getLink().add(lastPageLink);

							// Cache current page Bundle
							PagingHistoryManager.INSTANCE.putInCache(currentPage, bundle);

							// Increment pageNum
							pageNum++;
							currentPage = locationPath + "&page=" + pageNum;

							// Start new page Bundle
							bundle = new Bundle();
							bundle.setId(UUIDUtil.getUUID());
							bundleMeta = new Meta();
							bundleMeta.setVersionId("1");
							bundleMeta.setLastUpdated(new Date());
							bundle.setMeta(bundleMeta);
							bundle.setType(BundleType.HISTORY);
							bundle.setTotal(resources.size());
							selfLink = new BundleLinkComponent();
							selfLink.setRelation("self");
							selfLink.setUrl(currentPage);
							bundle.getLink().add(selfLink);
						}

						BundleEntryComponent bundleEntry = new BundleEntryComponent();

						// Bundle.entry.request
						BundleEntryRequestComponent requestComponent = new BundleEntryRequestComponent();

						HTTPVerb requestMethod = HTTPVerb.PUT; // default to update
						String requestUrl = resourceEntry.getResourceType() + "/" + resourceEntry.getResourceId();
						if (resourceEntry.getStatus().equalsIgnoreCase("DELETED")) {
							requestMethod = HTTPVerb.DELETE; // delete
						}
						if (resourceEntry.getVersionId().intValue() == 1) {
							requestMethod = HTTPVerb.POST; // create
							requestUrl = resourceEntry.getResourceType();
						}

						requestComponent.setMethod(requestMethod);
						requestComponent.setUrl(requestUrl);

						bundleEntry.setRequest(requestComponent);

						// Bundle.entry.response
						BundleEntryResponseComponent responseComponent = new BundleEntryResponseComponent();

						String responseStatus = "200 (OK)";
						if (resourceEntry.getVersionId().intValue() == 1) {
							responseStatus = "201 (Created)";
						}
						responseComponent.setStatus(responseStatus);
						responseComponent.setLastModified(resourceEntry.getLastUpdate());

						bundleEntry.setResponse(responseComponent);

						// Bundle.entry.resource
						if (!requestMethod.equals(HTTPVerb.DELETE)) {
							// Convert XML contents to Resource object
							iResource = new ByteArrayInputStream(resourceEntry.getResourceContents());
							XmlParser xmlP = new XmlParser();
							org.hl7.fhir.r4.model.Resource resourceObject = xmlP.parse(iResource);

							bundleEntry.setResource(resourceObject);
						}

						// Build and set Bundle.entry.fullUrl as reference to the resource id
						//fullUrl = baseUrl + "/" + resourceEntry.getResourceId();
						// Build and set Bundle.entry.fullUrl as unique UUID value
						//fullUrl = UUIDUtil.getUUID(true);
						bundleEntry.setFullUrl(baseUrl);

						bundle.getEntry().add(bundleEntry);
					}

					if (needPaging) {
						// Calculate next and previous pageNum values
						nextPageNum = pageNum + 1;
						prevPageNum = pageNum - 1;

						// Populate paging links in current page Bundle
						BundleLinkComponent firstPageLink = new BundleLinkComponent();
						firstPageLink.setRelation("first");
						firstPageLink.setUrl(firstPage);
						bundle.getLink().add(firstPageLink);

						if (prevPageNum >= 1) {
							BundleLinkComponent prevPageLink = new BundleLinkComponent();
							prevPageLink.setRelation("previous");
							prevPageLink.setUrl(locationPath + "&page=" + prevPageNum);
							bundle.getLink().add(prevPageLink);
						}

						if (nextPageNum <= pageCount) {
							BundleLinkComponent nextPageLink = new BundleLinkComponent();
							nextPageLink.setRelation("next");
							nextPageLink.setUrl(locationPath + "&page=" + nextPageNum);
							bundle.getLink().add(nextPageLink);
						}

						BundleLinkComponent lastPageLink = new BundleLinkComponent();
						lastPageLink.setRelation("last");
						lastPageLink.setUrl(lastPage);
						bundle.getLink().add(lastPageLink);

						// Cache current page Bundle
						PagingHistoryManager.INSTANCE.putInCache(currentPage, bundle);

						// Get first page from cache
						bundle = PagingHistoryManager.INSTANCE.retrieveFromCache(firstPage);
					}

					resourceContainer.setBundle(bundle);
					resourceContainer.setResponseStatus(Response.Status.OK);
				}
				else {
					// No match found
					resourceContainer.setResource(null);
					resourceContainer.setResponseStatus(Response.Status.NOT_FOUND);
					resourceContainer.setMessage("No history found.");
				}
			}
		} catch (Exception e) {
			log.severe(e.getMessage());
			// Exception caught
			resourceContainer.setResource(null);
			resourceContainer.setResponseStatus(Response.Status.INTERNAL_SERVER_ERROR);
			resourceContainer.setMessage(e.getMessage());
        } finally {
            if (iResource != null) {
                try {
                	iResource.close();
                } catch (IOException ioe) {
                    log.warning("Exception closing ByteArrayInputStream: " + ioe.getMessage());
                }
            }
		}

		return resourceContainer;
	}

	/**
	 * The read interaction accesses the current contents of a resource of the specified type. The interaction is performed by an HTTP GET
	 * command.
	 *
	 * @param resourceType
	 * @param resourceId
	 * @return <code>ResourceContainer</code>
	 * @throws Exception
	 */
	public ResourceContainer read(String resourceType, String resourceId, String _summary) throws Exception {

		log.fine("[START] ResourceService.read(" + resourceType + ", " + resourceId + ", " + _summary + ")");

		ResourceContainer resourceContainer = new ResourceContainer();

		try {
			// Check for invalid _summary=count
			if (!StringUtils.isEmpty(_summary) && _summary.equals("count")) {
				OperationOutcome outcome = null;
				OperationOutcome.OperationOutcomeIssueComponent issue = null;
				List<OperationOutcome.OperationOutcomeIssueComponent> issues = new ArrayList<OperationOutcome.OperationOutcomeIssueComponent>();

				issue = ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTSUPPORTED, "Invalid read parameter _summary=count! Not supported.", null, null);
				if (issue != null) {
					issues.add(issue);
				}

				String ooResourceId = UUIDUtil.getUUID(false);

				outcome = ServicesUtil.INSTANCE.getOperationOutcomeResource(issues);

				outcome.setId(ooResourceId);

				// Convert OperationOutcome Resource object to byte array
				ByteArrayOutputStream oResource = new ByteArrayOutputStream();
				XmlParser xmlP = new XmlParser();
				xmlP.setOutputStyle(OutputStyle.PRETTY);
				xmlP.compose(oResource, outcome);
				byte[] resourceContents = oResource.toByteArray();

				net.aegis.fhir.model.Resource ooResource = new  net.aegis.fhir.model.Resource();
				ooResource.setResourceContents(resourceContents);
				ooResource.setResourceId(ooResourceId);
				ooResource.setVersionId(1);
				ooResource.setResourceType("OperationOutcome");

				resourceContainer.setResource(ooResource);
				resourceContainer.setResponseStatus(Response.Status.BAD_REQUEST);
			}
			else {
				CriteriaBuilder cb = em.getCriteriaBuilder();
				CriteriaQuery<net.aegis.fhir.model.Resource> criteria = cb.createQuery(net.aegis.fhir.model.Resource.class);
				Root<net.aegis.fhir.model.Resource> resource = criteria.from(net.aegis.fhir.model.Resource.class);
				List<Predicate> predicateList = new ArrayList<Predicate>();
				predicateList.add(cb.equal(resource.get("resourceId"), resourceId));
				predicateList.add(cb.equal(resource.get("resourceType"), resourceType));

				criteria.select(resource)
					.where(cb.and(predicateList.toArray(new Predicate[predicateList.size()])))
					.orderBy(cb.desc(resource.get("versionId")));

				List<net.aegis.fhir.model.Resource> resources = em.createQuery(criteria).getResultList();

				if (resources != null && resources.size() > 0) {

					if (!StringUtils.isEmpty(_summary)) {
						// Summary requested, modify copy of found resource
						net.aegis.fhir.model.Resource foundResource = resources.get(0).copy();

						SummaryUtil.INSTANCE.generateResourceSummary(foundResource, _summary);

						resourceContainer.setResource(foundResource);
					}
					else {
						// Resource ID found, use the first one
						resourceContainer.setResource(resources.get(0));
					}

					// Check the resource status; if not 'DELETED' then consider it 'VALID'
					if (resources.get(0).getStatus() != null && resources.get(0).getStatus().equalsIgnoreCase("DELETED")) {
						resourceContainer.setResponseStatus(Response.Status.GONE);
					} else {
						resourceContainer.setResponseStatus(Response.Status.OK);
						try {
							if (resourceType.equals("Bundle")) {
								// Convert XML contents to Bundle object
								ByteArrayInputStream iResource = new ByteArrayInputStream(resourceContainer.getResource().getResourceContents());
								XmlParser xmlP = new XmlParser();
								xmlP.setOutputStyle(OutputStyle.PRETTY);
								Bundle bundleObject = (Bundle)xmlP.parse(iResource);
								// Populate resourceContainer.bundle
								resourceContainer.setBundle(bundleObject);
							}
						}
						catch (Exception e) {
							log.severe(e.getMessage());
							// Exception not thrown to allow operation to complete
						}
					}
				} else {
					// No match found
					resourceContainer.setResponseStatus(Response.Status.NOT_FOUND);
				}
			}
		} catch (Exception e) {
			// Exception caught
			resourceContainer.setResource(null);
			resourceContainer.setResponseStatus(Response.Status.INTERNAL_SERVER_ERROR);
			resourceContainer.setMessage(e.getMessage());

			log.severe(e.getMessage());
			// Exception not thrown to allow operation to complete
		}

		return resourceContainer;
	}

	/**
	 * Return the List of Resource instances for the history of a single Resource
	 *
	 * @param resource
	 * @return <code>List<Resource></code>
	 * @throws Exception
	 */
	public List<net.aegis.fhir.model.Resource> readAllHistoryForResource(net.aegis.fhir.model.Resource resource) throws Exception {

		log.fine("[START] ResourceService.readAllHistoryForResource");

		List<net.aegis.fhir.model.Resource> resourceList = null;

		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<net.aegis.fhir.model.Resource> criteria = cb.createQuery(net.aegis.fhir.model.Resource.class);
			Root<net.aegis.fhir.model.Resource> resourceRoot = criteria.from(net.aegis.fhir.model.Resource.class);

			List<Predicate> predicateList = new ArrayList<Predicate>();
			predicateList.add(cb.equal(resourceRoot.get("resourceId"), resource.getResourceId()));
			predicateList.add(cb.equal(resourceRoot.get("resourceType"), resource.getResourceType()));

			criteria.select(resourceRoot)
				.where(cb.and(predicateList.toArray(new Predicate[predicateList.size()])));

			resourceList = em.createQuery(criteria).getResultList();

		} catch (Exception e) {
			// Exception caught
			log.severe(e.getMessage());
			throw e;
		}

		return resourceList;
	}

	/**
	 * The vread interaction performs a version specific read of the specified resource type. The interaction is performed by an HTTP
	 * GET command.
	 * @param resourceType type of resource we are trying to read
	 * @param resource
	 * @return <code>ResourceContainer</code>
	 * @throws Exception
	 */
	public ResourceContainer vread(String resourceType, String resourceId, Integer versionId) throws Exception {

		log.fine("[START] ResourceService.vread");

		ResourceContainer resourceContainer = new ResourceContainer();

		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<net.aegis.fhir.model.Resource> criteria = cb.createQuery(net.aegis.fhir.model.Resource.class);
			Root<net.aegis.fhir.model.Resource> resource = criteria.from(net.aegis.fhir.model.Resource.class);

			List<Predicate> predicateList = new ArrayList<Predicate>();

			predicateList.add(cb.equal(resource.get("resourceId"), resourceId));
			predicateList.add(cb.equal(resource.get("versionId"), versionId));
			predicateList.add(cb.equal(resource.get("resourceType"), resourceType));

			criteria.select(resource)
				.where(cb.and(predicateList.toArray(new Predicate[predicateList.size()])))
				.orderBy(cb.desc(resource.get("versionId")));

			List<net.aegis.fhir.model.Resource> resources = em.createQuery(criteria).getResultList();

			if (resources != null && resources.size() > 0) {
				// Resource ID found, assign first one
				resourceContainer.setResource(resources.get(0));

				// Check the resource status; if not 'DELETED' then consider it 'VALID'
				if (resources.get(0).getStatus() != null && resources.get(0).getStatus().equalsIgnoreCase("DELETED")) {
					resourceContainer.setResponseStatus(Response.Status.GONE);
				} else {
					resourceContainer.setResponseStatus(Response.Status.OK);
					try {
						if (resourceType.equals("Bundle")) {
							// Convert XML contents to Bundle object
							ByteArrayInputStream iResource = new ByteArrayInputStream(resourceContainer.getResource().getResourceContents());
							XmlParser xmlP = new XmlParser();
							xmlP.setOutputStyle(OutputStyle.PRETTY);
							Bundle bundleObject = (Bundle)xmlP.parse(iResource);
							// Populate resourceContainer.bundle
							resourceContainer.setBundle(bundleObject);
						}
					}
					catch (Exception e) {
						log.severe(e.getMessage());
						// Exception not thrown to allow operation to complete
					}
				}
			} else {
				// No match found
				resourceContainer.setResponseStatus(Response.Status.NOT_FOUND);
			}
		} catch (Exception e) {
			// Exception caught
			resourceContainer.setResource(null);
			resourceContainer.setResponseStatus(Response.Status.INTERNAL_SERVER_ERROR);
			resourceContainer.setMessage(e.getMessage());

			log.severe(e.getMessage());
			// Exception not thrown to allow operation to complete
		}

		return resourceContainer;
	}

	/**
	 * Update the existing resource only; i.e. no version change
	 *
	 * @param resource
	 * @param baseUrl
	 * @throws Exception
	 */
	public void updateOnly(net.aegis.fhir.model.Resource resource, String baseUrl) throws Exception {

		log.fine("[START] ResourceService.updateOnly");

		if (resource != null) {
			// Delete the Resourcemetadata objects for the current Resource
			resourcemetadataService.deleteAllForResource(resource);

			Date updatedTime = new Date();

			/*
			 *  TRANSACTION BEGIN
			 */
			userTransaction.begin();

			// Read current resource record
			net.aegis.fhir.model.Resource currentResource = em.find(net.aegis.fhir.model.Resource.class, resource.getId());

			// Update resource record last updated time
			currentResource.setLastUpdate(updatedTime);
			// Assign updated resource contents
			currentResource.setResourceContents(resource.getResourceContents());

			em.merge(currentResource);
			resourceEventSrc.fire(currentResource);

			/*
			 *  TRANSACTION COMMIT(END)
			 */
			userTransaction.commit();

			// Generate the list of Resourcemetadata objects for the updated Resource
			List<Resourcemetadata> resourcemetadataList = resourcemetadataService.generateAllForResource(resource, baseUrl, this);

			// Create the new Resourcemetadata objects for the updated Resource
			resourcemetadataService.createAllForResource(resource, resourcemetadataList);
		}
		else {
			throw new Exception("Resource to update is null!");
		}
	}

	/**
	 * The update interaction creates a new current version for an existing resource or creates a new resource if no
	 * resource already exists for the given id. The update interaction is performed by an HTTP PUT command.
	 *
	 * @param resourceId
	 * @param resource
	 * @param baseUrl
	 * @return <code>ResourceContainer</code>
	 * @throws Exception
	 */
	public ResourceContainer update(String resourceId, net.aegis.fhir.model.Resource resource, String baseUrl) throws Exception {

		log.fine("[START] ResourceService.update");

		// Get the current version of the resource
		ResourceContainer resourceContainer = read(resource.getResourceType(), resourceId, null);

		// Check the status of the resource to determine the action to perform
		if (resourceContainer.getResponseStatus().equals(Response.Status.NOT_FOUND)) {

			// operation not allowed on a not found resource
			//resourceContainer.setResponseStatus(Response.Status.SERVICE_UNAVAILABLE);

			Date updatedTime = new Date();

			// create a new wildfhir resource; version 1
			net.aegis.fhir.model.Resource newResource = new net.aegis.fhir.model.Resource();
			newResource.setResourceId(resourceId);
			newResource.setVersionId(Integer.valueOf(1));
			newResource.setResourceType(resource.getResourceType());
			newResource.setStatus("valid");
			newResource.setLastUser("system");
			newResource.setLastUpdate(updatedTime);

			// Convert XML contents to Resource object and set id and meta
			ByteArrayInputStream iResource = new ByteArrayInputStream(resource.getResourceContents());
			XmlParser xmlP = new XmlParser();
			xmlP.setOutputStyle(OutputStyle.PRETTY);
			org.hl7.fhir.r4.model.Resource resourceObject = xmlP.parse(iResource);

			resourceObject.setId(resourceId);

			Meta resourceMeta = new Meta();
			if (resourceObject.hasMeta()) {
				resourceMeta = resourceObject.getMeta();
			}
			resourceMeta.setVersionId("1");
			resourceMeta.setLastUpdated(updatedTime);
			resourceObject.setMeta(resourceMeta);

			byte[] resourceBytes = xmlP.composeBytes(resourceObject);

			newResource.setResourceContents(resourceBytes);

			/*
			 *  TRANSACTION BEGIN
			 */
			userTransaction.begin();
			em.persist(newResource);
			resourceEventSrc.fire(newResource);

			// Extract base url from update path
			baseUrl = ServicesUtil.INSTANCE.extractBaseURL(baseUrl, resource.getResourceType());

			// Generate the list of Resourcemetadata objects for the new Resource
			List<Resourcemetadata> resourcemetadataList = resourcemetadataService.generateAllForResource(newResource, baseUrl, this);

			// Create the new Resourcemetadata objects for the new Resource
			resourcemetadataService.createAllForResource(newResource, resourcemetadataList);

			/*
			 *  TRANSACTION COMMIT(END)
			 */
			userTransaction.commit();

			resourceContainer.setResponseStatus(Response.Status.CREATED);
			resourceContainer.setResource(newResource);

		} else if (resourceContainer.getResponseStatus().equals(Response.Status.GONE)
				|| resourceContainer.getResponseStatus().equals(Response.Status.OK)) {

			// Delete the Resourcemetadata objects for the current Resource
			resourcemetadataService.deleteAllForResource(resourceContainer.getResource());

			Date updatedTime = new Date();

			// the current version of this resource exists; create a new version
			net.aegis.fhir.model.Resource newResource = new net.aegis.fhir.model.Resource();
			newResource.setResourceId(resourceId);
			Integer newVersion = Integer.valueOf(resourceContainer.getResource().getVersionId().intValue() + 1);
			newResource.setVersionId(newVersion);
			newResource.setResourceType(resourceContainer.getResource().getResourceType());
			newResource.setStatus("valid");
			newResource.setLastUser("system");
			newResource.setLastUpdate(updatedTime);

			// Convert XML contents to Resource object and set id and meta
			ByteArrayInputStream iResource = new ByteArrayInputStream(resource.getResourceContents());
			XmlParser xmlP = new XmlParser();
			xmlP.setOutputStyle(OutputStyle.PRETTY);
			org.hl7.fhir.r4.model.Resource resourceObject = xmlP.parse(iResource);

			resourceObject.setId(resourceId);

			Meta resourceMeta = new Meta();
			if (resourceObject.hasMeta()) {
				resourceMeta = resourceObject.getMeta();
			}
			resourceMeta.setVersionId(newVersion.toString());
			resourceMeta.setLastUpdated(updatedTime);
			resourceObject.setMeta(resourceMeta);

			byte[] resourceBytes = xmlP.composeBytes(resourceObject);

			newResource.setResourceContents(resourceBytes);

			/*
			 *  TRANSACTION BEGIN
			 */
			userTransaction.begin();
			em.persist(newResource);
			resourceEventSrc.fire(newResource);

			// Extract base url from update path
			baseUrl = ServicesUtil.INSTANCE.extractBaseURL(baseUrl, resource.getResourceType());

			// Generate the list of Resourcemetadata objects for the new Resource
			List<Resourcemetadata> resourcemetadataList = resourcemetadataService.generateAllForResource(newResource, baseUrl, this);

			// Create the new Resourcemetadata objects for the new Resource
			resourcemetadataService.createAllForResource(newResource, resourcemetadataList);

			/*
			 *  TRANSACTION COMMIT(END)
			 */
			userTransaction.commit();

			resourceContainer.setResponseStatus(Response.Status.OK);
			resourceContainer.setResource(newResource);
		}

		return resourceContainer;
	}

	/**
	 * The json patch interaction creates a new current version for an existing resource. The patch interaction is performed
	 * by an HTTP PATCH command.
	 *
	 * @param jsonPatchString
	 * @param resource
	 * @return <code>ResourceContainer</code>
	 * @throws Exception
	 */
	public ResourceContainer jsonPatch(String jsonPatchString, ResourceContainer resourceContainer, String baseUrl) throws Exception {

		log.fine("[START] ResourceService.jsonPatch");

		boolean isTestOnly = false;
		String resourceId = resourceContainer.getResource().getResourceId();
		net.aegis.fhir.model.Resource resource = resourceContainer.getResource();
		org.hl7.fhir.r4.model.Resource newResourceObject = null;
		String resourceMessage = null;
		XmlParser xmlParser = new XmlParser();

		log.info("JSON PATCH String: " + jsonPatchString);

		try {
			// Convert XML contents to Resource object
			ByteArrayInputStream iResource = new ByteArrayInputStream(resource.getResourceContents());
			org.hl7.fhir.r4.model.Resource resourceObject = xmlParser.parse(iResource);

			// Convert Resource object to be patched to a JSON string
			ByteArrayOutputStream oResource = new ByteArrayOutputStream();
			JsonParser jsonParser = new JsonParser();
            jsonParser.setOutputStyle(OutputStyle.PRETTY);
			jsonParser.compose(oResource, resourceObject);
			String jsonSourceString = oResource.toString();

			// Apply JSON Patch and get updated resource back as a JSON string
			String jsonTargetString = JsonPatchUtil.INSTANCE.applyJsonPatch(jsonPatchString, jsonSourceString);

			// Convert the patched(updated) JSON string back to a Resource object
			iResource = new ByteArrayInputStream(jsonTargetString.getBytes());
			newResourceObject = jsonParser.parse(iResource);

			isTestOnly = JsonPatchUtil.INSTANCE.isJsonPatchTestOnly(jsonPatchString);
		}
		catch (Exception e) {
			newResourceObject = null;
			resourceMessage = "Exception applying JSON Patch! " + e.getMessage();
			log.severe("Exception applying JSON Patch! " + e.getMessage());
		}

		if (newResourceObject != null) {

			// If not test only, then apply update to repository
			if (!isTestOnly) {

				// Delete the Resourcemetadata objects for the current Resource
				resourcemetadataService.deleteAllForResource(resourceContainer.getResource());

				Date updatedTime = new Date();

				// the current version of this resource exists; create a new version
				net.aegis.fhir.model.Resource newResource = new net.aegis.fhir.model.Resource();
				newResource.setResourceId(resourceId);
				Integer newVersion = Integer.valueOf(resourceContainer.getResource().getVersionId().intValue() + 1);
				newResource.setVersionId(newVersion);
				newResource.setResourceType(resourceContainer.getResource().getResourceType());
				newResource.setStatus("valid");
				newResource.setLastUser("system");
				newResource.setLastUpdate(updatedTime);

				// Set id and meta on new resource object
				newResourceObject.setId(resourceId);

				Meta resourceMeta = new Meta();
				resourceMeta.setVersionId(newVersion.toString());
				resourceMeta.setLastUpdated(updatedTime);
				newResourceObject.setMeta(resourceMeta);

				xmlParser.setOutputStyle(OutputStyle.PRETTY);
				byte[] resourceBytes = xmlParser.composeBytes(newResourceObject);

				newResource.setResourceContents(resourceBytes);

				/*
				 *  TRANSACTION BEGIN
				 */
				userTransaction.begin();
				em.persist(newResource);
				resourceEventSrc.fire(newResource);

				// Generate the list of Resourcemetadata objects for the new Resource
				List<Resourcemetadata> resourcemetadataList = resourcemetadataService.generateAllForResource(newResource, baseUrl, this);

				// Create the new Resourcemetadata objects for the new Resource
				resourcemetadataService.createAllForResource(newResource, resourcemetadataList);

				/*
				 *  TRANSACTION COMMIT(END)
				 */
				userTransaction.commit();

				resourceContainer.setResponseStatus(Response.Status.OK);
				resourceContainer.setResource(newResource);
			}
			// Else test only, simply set the resourceContainer.resource contents to patch return
			else {
				xmlParser.setOutputStyle(OutputStyle.PRETTY);
				byte[] resourceBytes = xmlParser.composeBytes(newResourceObject);

				resourceContainer.setResponseStatus(Response.Status.OK);
				resourceContainer.getResource().setResourceContents(resourceBytes);
			}
		}
		else {
			resourceContainer.setResponseStatus(Response.Status.BAD_REQUEST);
			resourceContainer.setResource(null);
			resourceContainer.setMessage(resourceMessage);
		}

		return resourceContainer;
	}

	/**
	 * The xml patch interaction creates a new current version for an existing resource. The patch interaction is performed
	 * by an HTTP PATCH command.
	 *
	 * @param xmlPatchString
	 * @param resource
	 * @return <code>ResourceContainer</code>
	 * @throws Exception
	 */
	public ResourceContainer xmlPatch(String xmlPatchString, ResourceContainer resourceContainer, String baseUrl) throws Exception {

		log.fine("[START] ResourceService.xmlPatch");

		String resourceId = resourceContainer.getResource().getResourceId();
		net.aegis.fhir.model.Resource resource = resourceContainer.getResource();
		org.hl7.fhir.r4.model.Resource newResourceObject = null;
		String resourceMessage = null;
		XmlParser xmlParser = new XmlParser();

		log.info("XML PATCH String: " + xmlPatchString);

		try {
			// Convert XML contents to String
			String xmlSourceString = new String(resource.getResourceContents());

			log.info("XML Source String: " + xmlSourceString);

			// Apply XML Patch and get updated resource back as an XML string
			String xmlTargetString = XmlPatchUtil.INSTANCE.applyXmlPatch(xmlPatchString, xmlSourceString);

			// Convert the patched(updated) XML string back to a Resource object
			ByteArrayInputStream iResource = new ByteArrayInputStream(xmlTargetString.getBytes());
			xmlParser.setOutputStyle(OutputStyle.PRETTY);
			newResourceObject = xmlParser.parse(iResource);
		}
		catch (Exception e) {
			newResourceObject = null;
			resourceMessage = "Exception applying XML Patch! " + e.getMessage();
			log.severe("Exception applying XML Patch! " + e.getMessage());
		}

		if (newResourceObject != null) {
			// Delete the Resourcemetadata objects for the current Resource
			resourcemetadataService.deleteAllForResource(resourceContainer.getResource());

			Date updatedTime = new Date();

			// the current version of this resource exists; create a new version
			net.aegis.fhir.model.Resource newResource = new net.aegis.fhir.model.Resource();
			newResource.setResourceId(resourceId);
			Integer newVersion = Integer.valueOf(resourceContainer.getResource().getVersionId().intValue() + 1);
			newResource.setVersionId(newVersion);
			newResource.setResourceType(resourceContainer.getResource().getResourceType());
			newResource.setStatus("valid");
			newResource.setLastUser("system");
			newResource.setLastUpdate(updatedTime);

			// Set id and meta on new resource object
			newResourceObject.setId(resourceId);

			Meta resourceMeta = new Meta();
			resourceMeta.setVersionId(newVersion.toString());
			resourceMeta.setLastUpdated(updatedTime);
			newResourceObject.setMeta(resourceMeta);

			xmlParser.setOutputStyle(OutputStyle.PRETTY);
			byte[] resourceBytes = xmlParser.composeBytes(newResourceObject);

			newResource.setResourceContents(resourceBytes);

			/*
			 *  TRANSACTION BEGIN
			 */
			userTransaction.begin();
			em.persist(newResource);
			resourceEventSrc.fire(newResource);

			// Generate the list of Resourcemetadata objects for the new Resource
			List<Resourcemetadata> resourcemetadataList = resourcemetadataService.generateAllForResource(newResource, baseUrl, this);

			// Create the new Resourcemetadata objects for the new Resource
			resourcemetadataService.createAllForResource(newResource, resourcemetadataList);

			/*
			 *  TRANSACTION COMMIT(END)
			 */
			userTransaction.commit();

			resourceContainer.setResponseStatus(Response.Status.OK);
			resourceContainer.setResource(newResource);
		}
		else {
			resourceContainer.setResponseStatus(Response.Status.BAD_REQUEST);
			resourceContainer.setResource(null);
			resourceContainer.setMessage(resourceMessage);
		}

		return resourceContainer;
	}

	/**
	 * This interaction searches a set of resources based on some filter criteria. The interaction can be performed by several different HTTP commands.
	 *
	 * @param parameterMap
	 * @param formMap
	 * @param authPatientMap
	 * @param orderedParams
	 * @param resourceType
	 * @param requestUrl
	 * @param locationPath
	 * @param count_
	 * @param page_
	 * @param isCompartment
	 * @return <code>ResourceContainer</code>
	 * @throws Exception
	 */
	public ResourceContainer search(MultivaluedMap<String,String> parameterMap, MultivaluedMap<String,String> formMap, MultivaluedMap<String,String> authPatientMap, List<NameValuePair> orderedParams, String resourceType, String locationPath, Integer count_, Integer page_, String summary_, boolean isCompartment) throws Exception {

		log.fine("[START] ResourceService.search");

		ResourceContainer resourceContainer = new ResourceContainer();

		ByteArrayInputStream iResource = null;

		String searchResponsePayload = codeService.getCodeValue("searchResponsePayload");

		OperationOutcome outcome = null;
		List<OperationOutcome.OperationOutcomeIssueComponent> issues = null;

		try {
			// Check for paged request; page parameter is not null
			if (page_ != null && page_.intValue() > 0) {
				log.info("ResourceService.search - cached page requested");

				// Retrieve page from history cache using locationPath as the key
				Bundle bundle = PagingSearchManager.INSTANCE.retrieveFromCache(locationPath);

				if (bundle != null) {
					resourceContainer.setBundle(bundle);
					resourceContainer.setResponseStatus(Response.Status.OK);
				}
				else {
					// Page cache has expired
					resourceContainer.setResource(null);
					resourceContainer.setResponseStatus(Response.Status.NOT_FOUND);
					resourceContainer.setMessage("Search page cache has expired.");
				}
			}
			else {
				log.info("ResourceService.search - new search request; compartment is " + isCompartment);

				List<String> _matchedId = new ArrayList<String>();
				List<String[]> _include = new ArrayList<String[]>();
				List<String> _includedId = new ArrayList<String>();
				List<String[]> _includeIterate = new ArrayList<String[]>();
				//List<String> _includeIteratedId = new ArrayList<String>();
				List<String[]> _revinclude = new ArrayList<String[]>();
				List<String> _revincludedId = new ArrayList<String>();
				List<String[]> validParams = new ArrayList<String[]>();
				List<String[]> invalidParams = new ArrayList<String[]>();

				List<net.aegis.fhir.model.Resource> resources = searchQuery(parameterMap, formMap, authPatientMap, resourceType, isCompartment, _include, _includeIterate, _revinclude, validParams, invalidParams);

				log.info("ResourceService.search - resources.size() = " + resources.size());

				// Extract base url from locationPath for use in Bundle.entry.fullUrl element
				String baseSelfUrl = ServicesUtil.INSTANCE.extractBaseURL(locationPath, "?");
				// FHIR-159 - Check for trailing forward slash and remove if present
				if (baseSelfUrl.endsWith("/")){
					baseSelfUrl = baseSelfUrl.substring(0, baseSelfUrl.length() - 1);
				}
				StringBuffer selfUrl = new StringBuffer(baseSelfUrl);
				int validCount = 0;
				if (orderedParams != null && !orderedParams.isEmpty()) {
					selfUrl.append("?");
					for (NameValuePair param : orderedParams) {
						log.info("  param.name = '" + param.getName() + "'; param.value = '" + param.getValue() + "'");

						// Add orderedParam to selfUrl only if param.name in validParams
						for (String[] validParam : validParams) {
							if (validParam[0].equals(param.getName())) {
								if (validCount > 0) {
									selfUrl.append("&");
								}
								selfUrl.append(param.getName()).append("=").append(URLEncoder.encode(param.getValue(), StandardCharsets.UTF_8.toString()));
								validCount++;

								log.info("      --> Adding " + param.getName() + " = '" + param.getValue() + "'");
								break; // Only include first validParam match
							}
						}
					}
				}

				// Process any invalidParams into a Bundle.entry.resource OperationOutcome
				BundleEntryComponent bundleEntryOutcome = null;
				if (!invalidParams.isEmpty()) {
					bundleEntryOutcome = new BundleEntryComponent();
					OperationOutcome.OperationOutcomeIssueComponent issue = null;
					issues = new ArrayList<OperationOutcome.OperationOutcomeIssueComponent>();

					for (String[] invalidParam : invalidParams) {
						if (invalidParam[0].equals("ERROR")) {
							issue = ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.INVALID, invalidParam[1], null, null);
						}
						else {
							issue = ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.WARNING, OperationOutcome.IssueType.INVALID, "Invalid search parameter '" + invalidParam[0] + "' found in search criteria." + (invalidParam.length > 1 && invalidParam[1] != null ? " " + invalidParam[1] : ""), null, invalidParam[0]);
						}
						if (issue != null) {
							issues.add(issue);
						}
					}

					String ooResourceId = UUIDUtil.getUUID(false);

					outcome = ServicesUtil.INSTANCE.getOperationOutcomeResource(issues);

					outcome.setId(ooResourceId);

					bundleEntryOutcome.setFullUrl("urn:uuid:" + ooResourceId);
					bundleEntryOutcome.setResource(outcome);

					BundleEntrySearchComponent bundleEntryOutcomeSearch = new BundleEntrySearchComponent();
					bundleEntryOutcomeSearch.setMode(SearchEntryMode.OUTCOME);
					bundleEntryOutcome.setSearch(bundleEntryOutcomeSearch);
				}

				if (resources != null && resources.size() > 0) {

					/*
					 *  Check for count=0 or _summary=count parameter setting; if set, then only return total
					 *  without any page links and without any entries
					 */
					if ((count_ != null && count_.intValue() == 0) || (summary_ != null && summary_.equals("count"))) {
						Bundle bundle = new Bundle();

						bundle.setId(UUIDUtil.getUUID());
						Meta bundleMeta = new Meta();
						bundleMeta.setVersionId("1");
						bundleMeta.setLastUpdated(new Date());
						bundle.setMeta(bundleMeta);
						bundle.setType(BundleType.SEARCHSET);
						bundle.setTotal(resources.size());
						BundleLinkComponent selfLink = new BundleLinkComponent();
						selfLink.setRelation("self");
						selfLink.setUrl(selfUrl.toString());
						bundle.getLink().add(selfLink);

						if (bundleEntryOutcome != null) {
							bundle.getEntry().add(bundleEntryOutcome);
						}

						resourceContainer.setBundle(bundle);
						resourceContainer.setResponseStatus(Response.Status.OK);
					}
					else {
						// 1 or more Resources found, build Bundle list of Element entry objects for each resource

						// First, populate the _matchedId list for use in _include and _revInclude duplicate check
						for (net.aegis.fhir.model.Resource resourceEntry : resources) {
							_matchedId.add(resourceEntry.getResourceType() + "/" + resourceEntry.getResourceId());
						}

						// Extract base url from locationPath for use in Bundle.entry.fullUrl element
						String baseUrl = null;

						if (resourceType != null) {
							baseUrl = ServicesUtil.INSTANCE.extractBaseURL(locationPath, resourceType);
						}
						else {
							baseUrl = ServicesUtil.INSTANCE.extractBaseURL(locationPath, "?");
							// FHIR-159 - Check for trailing forward slash, add if not present
							if (!baseUrl.endsWith("/")){
								baseUrl += "/";
							}
						}

						String revIncludeBaseUrl = baseUrl;
						String fullUrl = "";

						// Check for _count
						int pageSize = 0;
						if (count_ != null && count_.intValue() > 0) {
							// if defined use as page size; may need to support paging
							pageSize = count_.intValue();
						}
						else {
							// if not defined set page size equal to number of found resources; i.e. no paging
							pageSize = resources.size();
						}

						log.info("ResourceService.search - pageSize = " + pageSize);

						// Test whether paging is needed
						boolean needPaging = false;
						int pageCount = 1;
						int pageNum = 1;
						if (pageSize < resources.size()) {
							needPaging = true;
							pageCount = this.divideAndRoundUp(resources.size(), pageSize);
						}

						log.info("ResourceService.search - pageCount = " + pageCount + "; needPaging = " + needPaging);

						String currentPage = selfUrl.toString() + "&page=1";
						String firstPage = selfUrl.toString() + "&page=1";
						String lastPage = selfUrl.toString() + "&page=" + pageCount;
						int nextPageNum = -1;
						int prevPageNum = -1;

						Bundle bundle = new Bundle();

						bundle.setId(UUIDUtil.getUUID());
						Meta bundleMeta = new Meta();
						bundleMeta.setVersionId("1");
						bundleMeta.setLastUpdated(new Date());
						bundle.setMeta(bundleMeta);
						bundle.setType(BundleType.SEARCHSET);
						bundle.setTotal(resources.size());
						BundleLinkComponent selfLink = new BundleLinkComponent();
						selfLink.setRelation("self");
						if (needPaging) {
							selfLink.setUrl(currentPage);
						}
						else {
							selfLink.setUrl(selfUrl.toString());
						}
						bundle.getLink().add(selfLink);

						int resourceCount = 0;

						XmlParser xmlP = new XmlParser();

						org.hl7.fhir.r4.model.Resource resourceObject = null;

						List<String[]> placeHolderValidParams = null;

						for (net.aegis.fhir.model.Resource resourceEntry : resources) {
							resourceCount++;
							if (resourceCount > pageSize) {

								log.info("ResourceService.search - Done with pageNum = " + pageNum);

								// Reset resourceCount for next page Bundle
								resourceCount = 1;

								// Calculate next and previous pageNum values
								nextPageNum = pageNum + 1;
								prevPageNum = pageNum - 1;

								// Populate paging links in current page Bundle
								BundleLinkComponent firstPageLink = new BundleLinkComponent();
								firstPageLink.setRelation("first");
								firstPageLink.setUrl(firstPage);
								bundle.getLink().add(firstPageLink);

								if (prevPageNum >= 1) {
									BundleLinkComponent prevPageLink = new BundleLinkComponent();
									prevPageLink.setRelation("previous");
									prevPageLink.setUrl(selfUrl.toString() + "&page=" + prevPageNum);
									bundle.getLink().add(prevPageLink);
								}

								if (nextPageNum <= pageCount) {
									BundleLinkComponent nextPageLink = new BundleLinkComponent();
									nextPageLink.setRelation("next");
									nextPageLink.setUrl(selfUrl.toString() + "&page=" + nextPageNum);
									bundle.getLink().add(nextPageLink);
								}

								BundleLinkComponent lastPageLink = new BundleLinkComponent();
								lastPageLink.setRelation("last");
								lastPageLink.setUrl(lastPage);
								bundle.getLink().add(lastPageLink);

								// Cache current page Bundle
								PagingSearchManager.INSTANCE.putInCache(currentPage, bundle);

								// Increment pageNum
								pageNum++;
								currentPage = selfUrl.toString() + "&page=" + pageNum;

								// Start new page Bundle
								bundle = new Bundle();
								bundle.setId(UUIDUtil.getUUID());
								bundleMeta = new Meta();
								bundleMeta.setVersionId("1");
								bundleMeta.setLastUpdated(new Date());
								bundle.setMeta(bundleMeta);
								bundle.setType(BundleType.SEARCHSET);
								bundle.setTotal(resources.size());
								selfLink = new BundleLinkComponent();
								selfLink.setRelation("self");
								selfLink.setUrl(currentPage);
								bundle.getLink().add(selfLink);
							}

							BundleEntryComponent bundleEntry = new BundleEntryComponent();

							// Build and set Bundle.entry.fullUrl
							fullUrl = baseUrl + resourceEntry.getResourceType() + "/" + resourceEntry.getResourceId();
							bundleEntry.setFullUrl(fullUrl);

							// Check for _summary
							if (!StringUtils.isEmpty(summary_)) {
								// Summary requested, modify copy of found resource
								net.aegis.fhir.model.Resource foundResource = resourceEntry.copy();

								SummaryUtil.INSTANCE.generateResourceSummary(foundResource, summary_);

								// Convert XML contents of copy to Resource object
								iResource = new ByteArrayInputStream(foundResource.getResourceContents());
							}
							else {
								// Convert XML contents to Resource object
								iResource = new ByteArrayInputStream(resourceEntry.getResourceContents());
							}

							resourceObject = xmlP.parse(iResource);

							bundleEntry.setResource(resourceObject);

							BundleEntrySearchComponent bundleEntrySearch = new BundleEntrySearchComponent();
							bundleEntrySearch.setMode(SearchEntryMode.MATCH);
							bundleEntrySearch.setScore(new BigDecimal(1));
							bundleEntry.setSearch(bundleEntrySearch);

							bundle.getEntry().add(bundleEntry);

							// Process _include
							if (_include != null && _include.size() > 0) {
								log.fine("Processing _include...");

								String source = null;
								String parameter = null;
								String type = null;

								for (String[] include : _include) {
									// Extract include parameter parts
									source = include[0];
									if (include.length > 1) {
										parameter = include[1];
									}
									else {
										parameter = null;
									}
									if (include.length > 2) {
										type = include[2];
									}
									else {
										type = null;
									}

									log.fine("--> _include is '" + source + ":" + (parameter != null ? parameter : "null") + ":" + (type != null ? type : "null") + "'");

									// Proceed only if current resource type matches include source and we have a parameter
									if (resourceEntry.getResourceType().equals(source) && parameter != null) {
										log.fine("-->--> _include resource type match (" + source + "); _include parameter is reference (" + parameter + ")");

										boolean isParamRef = false;
										String resolvedParameter = null;
										int resolvedParamEnd = -1;

										List<Resourcemetadata> paramMetaData = null;

										// Check for wild card parameter
										if (parameter.equals("*")) {
											paramMetaData = resourcemetadataService.findMetadataByResourceIdTypeLevel1Param(resourceEntry.getResourceId(), source);
										}
										else {
											// Query the resourcemetadata for the current resource parameter
											paramMetaData = resourcemetadataService.findMetadataByResourceIdTypeParam(resourceEntry.getResourceId(), source, parameter);
										}

										if (paramMetaData != null && paramMetaData.size() > 0) {
											log.fine("-->-->-->--> _include parameter meta data found");

											for (Resourcemetadata metadata : paramMetaData) {
												if (parameter.equals("*")) {
													resolvedParamEnd = metadata.getParamName().indexOf("[");
													if (resolvedParamEnd < 0) {
														resolvedParamEnd = metadata.getParamName().length();
													}
													resolvedParameter = metadata.getParamName().substring(0, resolvedParamEnd);
												}
												else {
													resolvedParameter = parameter;
												}
												isParamRef = (net.aegis.fhir.model.ResourceType.findResourceTypeResourceCriteriaType(source, resolvedParameter).equalsIgnoreCase("REFERENCE") ? true : false);

												if (isParamRef == true && !metadata.getParamValue().isEmpty()) {
													log.fine("-->-->-->--> _include parameter (" + resolvedParameter + ") meta data reference found (" + metadata.getParamValue() + ")");

													// Extract resource type and id
													String[] refParts = metadata.getParamValue().split("/");

													int refPartsLength = refParts.length;
													if (refPartsLength > 1) {
														String refResourceType = refParts[refPartsLength - 2];
														String refResourceId = refParts[refPartsLength - 1];

														// Check already _includedId and _matchedId lists for this included resource; if found, skip
														String refResourceCheckId = refResourceType + "/" + refResourceId;
														if (!_includedId.contains(refResourceCheckId) && !_matchedId.contains(refResourceCheckId)) {
															log.fine("-->-->-->-->--> _include resource (" + refResourceCheckId + ")");

															// If type defined, check for match
															if (type == null || refResourceType.equals(type)) {
																if (type != null) {
																	log.fine("-->-->-->-->--> _include type match (" + type + ")");
																}
																ResourceContainer refResource = this.read(refResourceType, refResourceId, summary_);

																if (refResource.getResponseStatus().equals(Response.Status.OK)) {
																	log.fine("-->-->-->-->--> _include resource read OK (" + refResourceId + ")");

																	// Create and add bundle entry for included resource
																	bundleEntry = new BundleEntryComponent();

																	// Set Bundle.entry.fullUrl
																	fullUrl = baseUrl + refResourceType + "/" + refResourceId;
																	bundleEntry.setFullUrl(fullUrl);

																	// Convert XML contents to Resource object
																	iResource = new ByteArrayInputStream(refResource.getResource().getResourceContents());

																	resourceObject = xmlP.parse(iResource);

																	bundleEntry.setResource(resourceObject);

																	bundleEntrySearch = new BundleEntrySearchComponent();
																	bundleEntrySearch.setMode(SearchEntryMode.INCLUDE);
																	bundleEntry.setSearch(bundleEntrySearch);

																	bundle.getEntry().add(bundleEntry);

																	// Add to _includedId
																	_includedId.add(refResourceCheckId);
																}
																else {
																	log.fine("-->-->-->-->--> _include resource read NOT OK (" + refResourceId + ") --> " + refResource.getResponseStatus().name());
																}
															}
															else {
																log.fine("-->-->-->-->--> _type mismatch! refResourceType = '" + refResourceType + "', type = '" + (type != null ? type : "null") + "'");
															}
														}
														else {
															log.fine("-->-->-->-->--> _include resource (" + refResourceCheckId + ") - already included!");
														}
													}
												}
											}
										}
									}
								}
							}

							// Process _include:iterate
							if (_includeIterate != null && _includeIterate.size() > 0) {
								log.info("Processing _include:iterate...");

								String source = null;
								String parameter = null;
								String type = null;

								for (String[] includeIterate : _includeIterate) {
									// Extract include parameter parts
									source = includeIterate[0];
									if (includeIterate.length > 1) {
										parameter = includeIterate[1];
									}
									else {
										parameter = null;
									}
									if (includeIterate.length > 2) {
										type = includeIterate[2];
									}
									else {
										type = null;
									}

									log.fine("--> _include:iterate is '" + source + ":" + (parameter != null ? parameter : "null") + ":" + (type != null ? type : "null") + "'");

									// Proceed only if current resource type matches include source and we have a parameter
									if (resourceEntry.getResourceType().equals(source) && parameter != null) {
										log.fine("-->--> _include:iterate resource type match (" + source + "); _include:iterate parameter is reference (" + parameter + ")");

										boolean isParamRef = false;
										String resolvedParameter = null;
										int resolvedParamEnd = -1;

										List<Resourcemetadata> paramMetaData = null;

										// Check for wild card parameter
										if (parameter.equals("*")) {
											paramMetaData = resourcemetadataService.findMetadataByResourceIdTypeLevel1Param(resourceEntry.getResourceId(), source);
										}
										else {
											// Query the resourcemetadata for the current resource parameter
											paramMetaData = resourcemetadataService.findMetadataByResourceIdTypeParam(resourceEntry.getResourceId(), source, parameter);
										}

										if (paramMetaData != null && paramMetaData.size() > 0) {
											log.fine("-->-->-->--> _include:iterate parameter meta data found");

											for (Resourcemetadata metadata : paramMetaData) {
												if (parameter.equals("*")) {
													resolvedParamEnd = metadata.getParamName().indexOf("[");
													if (resolvedParamEnd < 0) {
														resolvedParamEnd = metadata.getParamName().length();
													}
													resolvedParameter = metadata.getParamName().substring(0, resolvedParamEnd);
												}
												else {
													resolvedParameter = parameter;
												}
												isParamRef = (net.aegis.fhir.model.ResourceType.findResourceTypeResourceCriteriaType(source, resolvedParameter).equalsIgnoreCase("REFERENCE") ? true : false);

												if (isParamRef == true && !metadata.getParamValue().isEmpty()) {
													log.fine("-->-->-->--> _include:iterate parameter (" + resolvedParameter + ") meta data reference found (" + metadata.getParamValue() + ")");

													// Extract resource type and id
													String[] refParts = metadata.getParamValue().split("/");

													int refPartsLength = refParts.length;
													if (refPartsLength > 1) {
														String refResourceType = refParts[refPartsLength - 2];
														String refResourceId = refParts[refPartsLength - 1];

														// Check already _includedId and _matchedId lists for this included resource; if found, skip
														String refResourceCheckId = refResourceType + "/" + refResourceId;
														if (!_includedId.contains(refResourceCheckId) && !_matchedId.contains(refResourceCheckId)) {
															log.fine("-->-->-->-->--> _include:iterate resource (" + refResourceCheckId + ")");

															// If type defined, check for match
															if (type == null || refResourceType.equals(type)) {
																if (type != null) {
																	log.fine("-->-->-->-->--> _include:iterate type match (" + type + ")");
																}
																ResourceContainer refResource = this.read(refResourceType, refResourceId, summary_);

																if (refResource.getResponseStatus().equals(Response.Status.OK)) {
																	log.fine("-->-->-->-->--> _include:iterate resource read OK (" + refResourceId + ")");

																	// Create and add bundle entry for included resource
																	bundleEntry = new BundleEntryComponent();

																	// Set Bundle.entry.fullUrl
																	fullUrl = baseUrl + refResourceType + "/" + refResourceId;
																	bundleEntry.setFullUrl(fullUrl);

																	// Convert XML contents to Resource object
																	iResource = new ByteArrayInputStream(refResource.getResource().getResourceContents());

																	resourceObject = xmlP.parse(iResource);

																	bundleEntry.setResource(resourceObject);

																	bundleEntrySearch = new BundleEntrySearchComponent();
																	bundleEntrySearch.setMode(SearchEntryMode.INCLUDE);
																	bundleEntry.setSearch(bundleEntrySearch);

																	bundle.getEntry().add(bundleEntry);

																	// Add to _includedId
																	_includedId.add(refResourceCheckId);

																	// Call includeIterate for this resource instance
																	includeIterate(bundle, _includedId, refResourceId, source, parameter, type, summary_, baseUrl, xmlP);
																}
																else {
																	log.fine("-->-->-->-->--> _include:iterate resource read NOT OK (" + refResourceId + ") --> " + refResource.getResponseStatus().name());
																}
															}
															else {
																log.fine("-->-->-->-->--> _include:iterate _type mismatch! refResourceType = '" + refResourceType + "', type = '" + (type != null ? type : "null") + "'");
															}
														}
														else {
															log.fine("-->-->-->-->--> _include:iterate resource (" + refResourceCheckId + ") - already included!");
														}
													}
												}
											}
										}
									}
								}
							}

							// Process _revinclude
							if (_revinclude != null && _revinclude.size() > 0) {
								log.fine("Processing _revinclude...");

								String source = null;
								String parameter = null;
								String type = null;

								for (String[] revinclude : _revinclude) {
									log.fine("--> _revinclude is '" + revinclude[0] + ":" + revinclude[1] + "'");

									// Extract revinclude parameter parts
									source = revinclude[0];
									if (revinclude.length > 1) {
										parameter = revinclude[1];
									}
									else {
										parameter = null;
									}
									if (revinclude.length > 2) {
										type = revinclude[2];
									}
									else {
										type = null;
									}

									// Proceed based on revinclude source and current resource type
									log.fine("-->--> _revinclude resource type (" + source + "); current resource type (" + resourceEntry.getResourceType() + ")");

									// Proceed only if we have a parameter
									if (parameter != null) {
										boolean isParamRef = (net.aegis.fhir.model.ResourceType.findResourceTypeResourceCriteriaType(source, parameter).equalsIgnoreCase("REFERENCE") ? true : false);

										if (isParamRef) {
											log.fine("-->-->--> _revinclude parameter is reference (" + parameter + "); current resource id entry (" + resourceEntry.getResourceId() + ")");

											// If type defined, check for current resource type match
											if (type == null || resourceEntry.getResourceType().equals(type)) {
												if (type != null) {
													log.fine("-->-->-->-->--> _revinclude type match (" + type + ")");
												}

												// Build reverse search parameter
												String revSearchParameter = parameter + "=" + resourceEntry.getResourceType() + "/" + resourceEntry.getResourceId();

												// Convert search parameter string into queryParams map
												List<NameValuePair> params = URLEncodedUtils.parse(revSearchParameter, Charset.defaultCharset());
												MultivaluedMap<String, String> queryParams = ServicesUtil.INSTANCE.listNameValuePairToMultivaluedMapString(params);

												// Search for resources with reverse search
												placeHolderValidParams = new ArrayList<String[]>();
												List<net.aegis.fhir.model.Resource> revSearch = this.searchQuery(queryParams, null, null, source, false, null, null, null, placeHolderValidParams, null);

												if (revSearch != null && revSearch.size() > 0) {
													log.fine("-->-->-->--> _revinclude reverse search found matches (" + revSearch.size() + ")");

													for (net.aegis.fhir.model.Resource revResource : revSearch) {
														String revResourceCheckId = revResource.getResourceType() + "/" + revResource.getResourceId();
														log.fine("-->-->-->-->--> _revinclude resource (" + revResourceCheckId + ")");

														// Check already _revincludedId and _matchedId lists for this revincluded resource; if found, skip
														if (!_revincludedId.contains(revResourceCheckId) && !_matchedId.contains(revResourceCheckId)) {

															// Create and add bundle entry for included resource
															bundleEntry = new BundleEntryComponent();

															// Set Bundle.entry.fullUrl
															bundleEntry.setFullUrl(revIncludeBaseUrl + revResourceCheckId);

															// Check for _summary
															if (!StringUtils.isEmpty(summary_)) {
																// Summary requested, modify copy of found resource
																net.aegis.fhir.model.Resource foundRevResource = revResource.copy();

																SummaryUtil.INSTANCE.generateResourceSummary(foundRevResource, summary_);

																// Convert XML contents of copy to Resource object
																iResource = new ByteArrayInputStream(foundRevResource.getResourceContents());
															}
															else {
																// Convert XML contents to Resource object
																iResource = new ByteArrayInputStream(revResource.getResourceContents());
															}

															resourceObject = xmlP.parse(iResource);

															bundleEntry.setResource(resourceObject);

															bundleEntrySearch = new BundleEntrySearchComponent();
															bundleEntrySearch.setMode(SearchEntryMode.INCLUDE);
															bundleEntry.setSearch(bundleEntrySearch);

															bundle.getEntry().add(bundleEntry);

															// Add to _revincludedId
															_revincludedId.add(revResourceCheckId);
														}
														else {
															log.fine("-->-->-->-->--> _revinclude resource (" + revResourceCheckId + ") - already included!");
														}
													}
												}
											}
										}
									}
								}
							}
						}

						// Call processIncluded
						processIncluded(0, bundle, _includedId, _matchedId, _include, _includeIterate, summary_, baseUrl, xmlP);

						if (bundleEntryOutcome != null) {
							bundle.getEntry().add(bundleEntryOutcome);
						}

						if (needPaging) {
							// Calculate next and previous pageNum values
							nextPageNum = pageNum + 1;
							prevPageNum = pageNum - 1;

							// Populate paging links in current page Bundle
							BundleLinkComponent firstPageLink = new BundleLinkComponent();
							firstPageLink.setRelation("first");
							firstPageLink.setUrl(firstPage);
							bundle.getLink().add(firstPageLink);

							if (prevPageNum >= 1) {
								BundleLinkComponent prevPageLink = new BundleLinkComponent();
								prevPageLink.setRelation("previous");
								prevPageLink.setUrl(selfUrl.toString() + "&page=" + prevPageNum);
								bundle.getLink().add(prevPageLink);
							}

							if (nextPageNum <= pageCount) {
								BundleLinkComponent nextPageLink = new BundleLinkComponent();
								nextPageLink.setRelation("next");
								nextPageLink.setUrl(selfUrl.toString() + "&page=" + nextPageNum);
								bundle.getLink().add(nextPageLink);
							}

							BundleLinkComponent lastPageLink = new BundleLinkComponent();
							lastPageLink.setRelation("last");
							lastPageLink.setUrl(lastPage);
							bundle.getLink().add(lastPageLink);

							// Cache current page Bundle
							PagingSearchManager.INSTANCE.putInCache(currentPage, bundle);

							// Get first page from cache
							bundle = PagingSearchManager.INSTANCE.retrieveFromCache(firstPage);
						}

						resourceContainer.setBundle(bundle);
						resourceContainer.setResponseStatus(Response.Status.OK);
					}
				}
				else {
					// No match found
					Bundle bundle = new Bundle();

					bundle.setId(UUIDUtil.getUUID());
					Meta bundleMeta = new Meta();
					bundleMeta.setVersionId("1");
					bundleMeta.setLastUpdated(new Date());
					bundle.setMeta(bundleMeta);
					bundle.setType(BundleType.SEARCHSET);
					bundle.setTotal(0);
					BundleLinkComponent selfLink = new BundleLinkComponent();
					selfLink.setRelation("self");
					selfLink.setUrl(selfUrl.toString());
					bundle.getLink().add(selfLink);

					if (searchResponsePayload != null && searchResponsePayload.equals("OperationOutcome")) {
						OperationOutcome.OperationOutcomeIssueComponent issue = ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.INFORMATION, OperationOutcome.IssueType.NOTFOUND, "No matches were found for the given search criteria.", null, null);

						if (bundleEntryOutcome == null) {
							bundleEntryOutcome = new BundleEntryComponent();
							if (issues == null) {
								issues = new ArrayList<OperationOutcome.OperationOutcomeIssueComponent>();
							}
							issues.add(issue);

							String ooResourceId = UUIDUtil.getUUID(false);

							outcome = ServicesUtil.INSTANCE.getOperationOutcomeResource(issues);

							outcome.setId(ooResourceId);

							bundleEntryOutcome.setFullUrl("urn:uuid:" + ooResourceId);
							bundleEntryOutcome.setResource(outcome);

							BundleEntrySearchComponent bundleEntryOutcomeSearch = new BundleEntrySearchComponent();
							bundleEntryOutcomeSearch.setMode(SearchEntryMode.OUTCOME);
							bundleEntryOutcome.setSearch(bundleEntryOutcomeSearch);
						}
						else {
							outcome = (OperationOutcome)bundleEntryOutcome.getResource();
							outcome.addIssue(issue);
						}
					}

					if (bundleEntryOutcome != null) {
						bundle.getEntry().add(bundleEntryOutcome);
					}

					resourceContainer.setBundle(bundle);
					resourceContainer.setResponseStatus(Response.Status.OK);
				}
			}
		} catch (Exception e) {
			// Exception caught
			resourceContainer.setResource(null);
			resourceContainer.setResponseStatus(Response.Status.INTERNAL_SERVER_ERROR);
			resourceContainer.setMessage(e.getMessage());

			log.severe(e.getMessage());
			// Exception not thrown to allow operation to complete
        } finally {
            if (iResource != null) {
                try {
                	iResource.close();
                } catch (IOException ioe) {
                    log.warning("Exception closing ByteArrayInputStream: " + ioe.getMessage());
                }
            }
		}

		return resourceContainer;
	}

	/*
	 * Called after processing initial search results. This method processes any
	 * additional _include parameters based on just those included resources.
	 */
	private void processIncluded(int recurseLevel, Bundle bundle, List<String> _includedId, List<String> _matchedId, List<String[]> _include, List<String[]> _includeIterate, String summary_, String baseUrl, XmlParser xmlP) throws Exception {

		log.fine("[START] ResourceService.processIncluded(" + recurseLevel + ", bundle, _includedId, _include, '" + summary_ + "', '" + baseUrl + "', xmlP)");

		boolean isParamRef = false;
		String resolvedParameter = null;
		int resolvedParamEnd = -1;
		BundleEntryComponent bundleEntry = null;
		String fullUrl = "";
		ByteArrayInputStream iResource = null;
		org.hl7.fhir.r4.model.Resource resourceObject = null;
		BundleEntrySearchComponent bundleEntrySearch = null;

		List<Resourcemetadata> paramMetaData = null;

		// Use traditional for loop to avoid ConcurrentModificationException
		for (int i = 0; i < _includedId.size(); i++) {
			String includedId = _includedId.get(i);

			/*
			 * 1. Process _include parameters against _includedId
			 * 2. Process _includeIterate parameters against _includedId
			 * 
			 * Recursive call to the method is not needed. The _includeId
			 * list will increase with additional included resources which
			 * will be processed via the traditional for loop logic.
			 */

			// Extract resource type and id
			String[] includedIdParts = includedId.split("/");

			int includedIdPartsLength = includedIdParts.length;
			
			// This shouldn't be an issue but, it's always good to double-check
			if (includedIdPartsLength > 1) {
				String includedIdResourceType = includedIdParts[includedIdPartsLength - 2];
				String includedIdResourceId = includedIdParts[includedIdPartsLength - 1];

				// Process _include
				if (_include != null && _include.size() > 0) {
					log.fine("Processing _include for " + includedId + "...");

					String source = null;
					String parameter = null;
					String type = null;

					for (String[] include : _include) {
						// Extract include parameter parts
						source = include[0];
						if (include.length > 1) {
							parameter = include[1];
						}
						else {
							parameter = null;
						}
						if (include.length > 2) {
							type = include[2];
						}
						else {
							type = null;
						}

						log.fine("--> _include is '" + source + ":" + (parameter != null ? parameter : "null") + ":" + (type != null ? type : "null") + "'");

						// Proceed only if current resource type matches include source and we have a parameter
						if (includedIdResourceType.equals(source) && parameter != null) {
							log.fine("-->--> _include resource type match (" + source + "); _include parameter is reference (" + parameter + ")");

							isParamRef = false;
							resolvedParameter = null;
							resolvedParamEnd = -1;

							paramMetaData = null;

							// Check for wild card parameter
							if (parameter.equals("*")) {
								paramMetaData = resourcemetadataService.findMetadataByResourceIdTypeLevel1Param(includedIdResourceId, source);
							}
							else {
								// Query the resourcemetadata for the current resource parameter
								paramMetaData = resourcemetadataService.findMetadataByResourceIdTypeParam(includedIdResourceId, source, parameter);
							}

							if (paramMetaData != null && paramMetaData.size() > 0) {
								log.fine("-->-->-->--> _include parameter meta data found");

								for (Resourcemetadata metadata : paramMetaData) {
									if (parameter.equals("*")) {
										resolvedParamEnd = metadata.getParamName().indexOf("[");
										if (resolvedParamEnd < 0) {
											resolvedParamEnd = metadata.getParamName().length();
										}
										resolvedParameter = metadata.getParamName().substring(0, resolvedParamEnd);
									}
									else {
										resolvedParameter = parameter;
									}
									isParamRef = (net.aegis.fhir.model.ResourceType.findResourceTypeResourceCriteriaType(source, resolvedParameter).equalsIgnoreCase("REFERENCE") ? true : false);

									if (isParamRef == true && !metadata.getParamValue().isEmpty()) {
										log.fine("-->-->-->--> _include parameter (" + resolvedParameter + ") meta data reference found (" + metadata.getParamValue() + ")");

										// Extract resource type and id
										String[] refParts = metadata.getParamValue().split("/");

										int refPartsLength = refParts.length;
										if (refPartsLength > 1) {
											String refResourceType = refParts[refPartsLength - 2];
											String refResourceId = refParts[refPartsLength - 1];

											// Check already _includedId and _matchedId lists for this included resource; if found, skip
											String refResourceCheckId = refResourceType + "/" + refResourceId;
											if (!_includedId.contains(refResourceCheckId) && !_matchedId.contains(refResourceCheckId)) {
												log.fine("-->-->-->-->--> _include resource (" + refResourceCheckId + ")");

												// If type defined, check for match
												if (type == null || refResourceType.equals(type)) {
													if (type != null) {
														log.fine("-->-->-->-->--> _include type match (" + type + ")");
													}
													ResourceContainer refResource = this.read(refResourceType, refResourceId, summary_);

													if (refResource.getResponseStatus().equals(Response.Status.OK)) {
														log.fine("-->-->-->-->--> _include resource read OK (" + refResourceId + ")");

														// Create and add bundle entry for included resource
														bundleEntry = new BundleEntryComponent();

														// Set Bundle.entry.fullUrl
														fullUrl = baseUrl + refResourceType + "/" + refResourceId;
														bundleEntry.setFullUrl(fullUrl);

														// Convert XML contents to Resource object
														iResource = new ByteArrayInputStream(refResource.getResource().getResourceContents());

														resourceObject = xmlP.parse(iResource);

														bundleEntry.setResource(resourceObject);

														bundleEntrySearch = new BundleEntrySearchComponent();
														bundleEntrySearch.setMode(SearchEntryMode.INCLUDE);
														bundleEntry.setSearch(bundleEntrySearch);

														bundle.getEntry().add(bundleEntry);

														// Add to _includedId
														_includedId.add(refResourceCheckId);
													}
													else {
														log.fine("-->-->-->-->--> _include resource read NOT OK (" + refResourceId + ") --> " + refResource.getResponseStatus().name());
													}
												}
												else {
													log.fine("-->-->-->-->--> _type mismatch! refResourceType = '" + refResourceType + "', type = '" + (type != null ? type : "null") + "'");
												}
											}
											else {
												log.fine("-->-->-->-->--> _include resource (" + refResourceCheckId + ") - already included!");
											}
										}
									}
								}
							}
						}
					}
				} // End process _include

				// Process _include:iterate
				if (_includeIterate != null && _includeIterate.size() > 0) {
					log.fine("Processing _include:iterate for " + includedId + "...");

					String source = null;
					String parameter = null;
					String type = null;

					for (String[] includeIterate : _includeIterate) {
						// Extract include parameter parts
						source = includeIterate[0];
						if (includeIterate.length > 1) {
							parameter = includeIterate[1];
						}
						else {
							parameter = null;
						}
						if (includeIterate.length > 2) {
							type = includeIterate[2];
						}
						else {
							type = null;
						}

						log.fine("--> _include:iterate is '" + source + ":" + (parameter != null ? parameter : "null") + ":" + (type != null ? type : "null") + "'");

						// Proceed only if current resource type matches include source and we have a parameter
						if (includedIdResourceType.equals(source) && parameter != null) {
							log.fine("-->--> _include:iterate resource type match (" + source + "); _include:iterate parameter is reference (" + parameter + ")");

							isParamRef = false;
							resolvedParameter = null;
							resolvedParamEnd = -1;

							paramMetaData = null;

							// Check for wild card parameter
							if (parameter.equals("*")) {
								paramMetaData = resourcemetadataService.findMetadataByResourceIdTypeLevel1Param(includedIdResourceId, source);
							}
							else {
								// Query the resourcemetadata for the current resource parameter
								paramMetaData = resourcemetadataService.findMetadataByResourceIdTypeParam(includedIdResourceId, source, parameter);
							}

							if (paramMetaData != null && paramMetaData.size() > 0) {
								log.fine("-->-->-->--> _include:iterate parameter meta data found");

								for (Resourcemetadata metadata : paramMetaData) {
									if (parameter.equals("*")) {
										resolvedParamEnd = metadata.getParamName().indexOf("[");
										if (resolvedParamEnd < 0) {
											resolvedParamEnd = metadata.getParamName().length();
										}
										resolvedParameter = metadata.getParamName().substring(0, resolvedParamEnd);
									}
									else {
										resolvedParameter = parameter;
									}
									isParamRef = (net.aegis.fhir.model.ResourceType.findResourceTypeResourceCriteriaType(source, resolvedParameter).equalsIgnoreCase("REFERENCE") ? true : false);

									if (isParamRef == true && !metadata.getParamValue().isEmpty()) {
										log.fine("-->-->-->--> _include:iterate parameter (" + resolvedParameter + ") meta data reference found (" + metadata.getParamValue() + ")");

										// Extract resource type and id
										String[] refParts = metadata.getParamValue().split("/");

										int refPartsLength = refParts.length;
										if (refPartsLength > 1) {
											String refResourceType = refParts[refPartsLength - 2];
											String refResourceId = refParts[refPartsLength - 1];

											// Check already _includedId and _matchedId lists for this included resource; if found, skip
											String refResourceCheckId = refResourceType + "/" + refResourceId;
											if (!_includedId.contains(refResourceCheckId) && !_matchedId.contains(refResourceCheckId)) {
												log.fine("-->-->-->-->--> _include:iterate resource (" + refResourceCheckId + ")");

												// If type defined, check for match
												if (type == null || refResourceType.equals(type)) {
													if (type != null) {
														log.fine("-->-->-->-->--> _include:iterate type match (" + type + ")");
													}
													ResourceContainer refResource = this.read(refResourceType, refResourceId, summary_);

													if (refResource.getResponseStatus().equals(Response.Status.OK)) {
														log.fine("-->-->-->-->--> _include:iterate resource read OK (" + refResourceId + ")");

														// Create and add bundle entry for included resource
														bundleEntry = new BundleEntryComponent();

														// Set Bundle.entry.fullUrl
														fullUrl = baseUrl + refResourceType + "/" + refResourceId;
														bundleEntry.setFullUrl(fullUrl);

														// Convert XML contents to Resource object
														iResource = new ByteArrayInputStream(refResource.getResource().getResourceContents());

														resourceObject = xmlP.parse(iResource);

														bundleEntry.setResource(resourceObject);

														bundleEntrySearch = new BundleEntrySearchComponent();
														bundleEntrySearch.setMode(SearchEntryMode.INCLUDE);
														bundleEntry.setSearch(bundleEntrySearch);

														bundle.getEntry().add(bundleEntry);

														// Add to _includedId
														_includedId.add(refResourceCheckId);

														// Call includeIterate for this resource instance
														includeIterate(bundle, _includedId, refResourceId, source, parameter, type, summary_, baseUrl, xmlP);
													}
													else {
														log.fine("-->-->-->-->--> _include:iterate resource read NOT OK (" + refResourceId + ") --> " + refResource.getResponseStatus().name());
													}
												}
												else {
													log.fine("-->-->-->-->--> _include:iterate _type mismatch! refResourceType = '" + refResourceType + "', type = '" + (type != null ? type : "null") + "'");
												}
											}
											else {
												log.fine("-->-->-->-->--> _include:iterate resource (" + refResourceCheckId + ") - already included!");
											}
										}
									}
								}
							}
						}
					}
				} // End process _includeIterate

			} // End if (includedIdPartsLength > 1)
		} // End for (includeId)
	}

	private void includeIterate(Bundle bundle, List<String> _includedId, String resourceId, String source, String parameter, String type, String summary_, String baseUrl, XmlParser xmlP) throws Exception {

		log.fine("[START] ResourceService.includeIterate(bundle, _includedId, '" + resourceId + "', '" + source + "', '" + parameter + "', '" + (type == null ? "null" : type) + "', '");

		boolean isParamRef = false;
		String resolvedParameter = null;
		int resolvedParamEnd = -1;
		BundleEntryComponent bundleEntry = null;
		String fullUrl = "";
		ByteArrayInputStream iResource = null;
		org.hl7.fhir.r4.model.Resource resourceObject = null;
		BundleEntrySearchComponent bundleEntrySearch = null;

		List<Resourcemetadata> paramMetaData = null;

		// Check for wild card parameter
		if (parameter.equals("*")) {
			paramMetaData = resourcemetadataService.findMetadataByResourceIdTypeLevel1Param(resourceId, source);
		}
		else {
			// Query the resourcemetadata for the current resource parameter
			paramMetaData = resourcemetadataService.findMetadataByResourceIdTypeParam(resourceId, source, parameter);
		}

		if (paramMetaData != null && paramMetaData.size() > 0) {
			log.fine("-->-->-->--> includeIterate parameter meta data found");

			for (Resourcemetadata metadata : paramMetaData) {
				if (parameter.equals("*")) {
					resolvedParamEnd = metadata.getParamName().indexOf("[");
					if (resolvedParamEnd < 0) {
						resolvedParamEnd = metadata.getParamName().length();
					}
					resolvedParameter = metadata.getParamName().substring(0, resolvedParamEnd);
				}
				else {
					resolvedParameter = parameter;
				}
				isParamRef = (net.aegis.fhir.model.ResourceType.findResourceTypeResourceCriteriaType(source, resolvedParameter).equalsIgnoreCase("REFERENCE") ? true : false);

				if (isParamRef == true && !metadata.getParamValue().isEmpty()) {
					log.fine("-->-->-->--> includeIterate parameter (" + resolvedParameter + ") meta data reference found (" + metadata.getParamValue() + ")");

					// Extract resource type and id
					String[] refParts = metadata.getParamValue().split("/");

					int refPartsLength = refParts.length;
					if (refPartsLength > 1) {
						String refResourceType = refParts[refPartsLength - 2];
						String refResourceId = refParts[refPartsLength - 1];

						// Check already _includedId list for this included resource; if found, skip
						String refResourceCheckId = refResourceType + "/" + refResourceId;
						if (!_includedId.contains(refResourceCheckId)) {
							log.fine("-->-->-->-->--> includeIterate resource (" + refResourceCheckId + ")");

							// If type defined, check for match
							if (type == null || refResourceType.equals(type)) {
								if (type != null) {
									log.fine("-->-->-->-->--> includeIterate type match (" + type + ")");
								}
								ResourceContainer refResource = this.read(refResourceType, refResourceId, summary_);

								if (refResource.getResponseStatus().equals(Response.Status.OK)) {
									log.fine("-->-->-->-->--> includeIterate resource read OK (" + refResourceId + ")");

									// Create and add bundle entry for included resource
									bundleEntry = new BundleEntryComponent();

									// Set Bundle.entry.fullUrl
									fullUrl = baseUrl + refResourceType + "/" + refResourceId;
									bundleEntry.setFullUrl(fullUrl);

									// Convert XML contents to Resource object
									iResource = new ByteArrayInputStream(refResource.getResource().getResourceContents());

									resourceObject = xmlP.parse(iResource);

									bundleEntry.setResource(resourceObject);

									bundleEntrySearch = new BundleEntrySearchComponent();
									bundleEntrySearch.setMode(SearchEntryMode.INCLUDE);
									bundleEntry.setSearch(bundleEntrySearch);

									bundle.getEntry().add(bundleEntry);

									// Add to _includedId
									_includedId.add(refResourceCheckId);

									// Call includeIterate for this resource instance
									includeIterate(bundle, _includedId, refResourceId, source, parameter, type, summary_, baseUrl, xmlP);
								}
								else {
									log.fine("-->-->-->-->--> includeIterate resource read NOT OK (" + refResourceId + ") --> " + refResource.getResponseStatus().name());
								}
							}
							else {
								log.fine("-->-->-->-->--> includeIterate _type mismatch! refResourceType = '" + refResourceType + "', type = '" + (type != null ? type : "null") + "'");
							}
						}
						else {
							log.fine("-->-->-->-->--> includeIterate resource (" + refResourceCheckId + ") - already included!");
						}
					}
				}
			}
		}

	}

	/**
	 *
	 * @param parameterMap
	 * @param formMap
	 * @param authPatientMap
	 * @param resourceType
	 * @param isCompartment
	 * @param _include
	 * @param _includeIterate
	 * @param _revinclude
	 * @param validParams
	 * @param invalidParams
	 * @return CriteriaQuery<Resource>
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<net.aegis.fhir.model.Resource> searchQuery(MultivaluedMap<String,String> parameterMap, MultivaluedMap<String,String> formMap, MultivaluedMap<String,String> authPatientMap, String resourceType, boolean isCompartment, List<String[]> _include, List<String[]> _includeIterate, List<String[]> _revinclude, List<String[]> validParams, List<String[]> invalidParams) throws Exception {

		log.fine("[START] ResourceService.searchQuery");

		Query resourceQuery = null;
		List<net.aegis.fhir.model.Resource> resourcesReturned = null;
		Integer maxCount = Integer.valueOf(500);
		boolean bDropTempTable = false;
		StringBuffer sbDropTempTable = new StringBuffer("");
		int parameterCount = 0;
		boolean isValidSearchParameters = false;

		List<String[]> _sort = new ArrayList<String[]>();

		try {
			log.info("Native query based on resource type and parameters");

			String tempTableName = "temp" + UUIDUtil.getGUID();

			// Build native query based on resource type and parameters
			StringBuffer sbQuery = new StringBuffer("select r1.id, r1.resourceId, r1.versionId, r1.resourceType, r1.status, r1.lastUser, r1.lastUpdate, r1.resourceContents");
			StringBuffer sbCriteria = new StringBuffer(" from resource r1 where");

			// FHIR-??? - SQL performance modifications: add engine=memory, remove unnecessary outer select * from () AS t1
			//StringBuffer sbCreateTempTable = new StringBuffer("create temporary table ").append(tempTableName).append(" as select * from ( ");
			StringBuffer sbCreateTempTable = new StringBuffer("create temporary table ").append(tempTableName).append(" engine=memory as ");

			StringBuffer sbCreateTempSelect = new StringBuffer("select rm.resourceJoinId as id from resourcemetadata rm"); // Append " where " when combining
			StringBuffer sbCreateTempWhereJoin = new StringBuffer(" "); // If where join length > 5 then append " and " when combining
			StringBuffer sbCreateTempWhereCriteria = new StringBuffer("");

			sbDropTempTable.append("drop temporary table ").append(tempTableName);

			if (resourceType != null && !resourceType.isEmpty()) {
				sbCriteria.append(" r1.resourceType = '").append(resourceType).append("'").append(" and r1.status = 'valid'");
				sbCriteria.append(" and r1.versionId = (select max(r2.versionId) from resource r2 where r2.resourceType = '").append(resourceType).append("' and r2.resourceId = r1.resourceId)");
			}
			else {
				resourceType = null;
				sbCriteria.append(" r1.status = 'valid'");
				sbCriteria.append(" and r1.versionId = (select max(r2.versionId) from resource r2 where r2.resourceType = r1.resourceType and r2.resourceId = r1.resourceId)");
			}


			if ((parameterMap != null && parameterMap.size() > 0) || (formMap != null && formMap.size() > 0) || (authPatientMap != null && authPatientMap.size() > 0)) {

				HashMap<String, String[]> nearParams = new HashMap<String, String[]>();

				List<Entry<String, List<String>>> compartmentSet = new ArrayList<Entry<String, List<String>>>();

				Set<Entry<String, List<String>>> paramSet = new HashSet<Entry<String, List<String>>>();

				// Include parameters from parameterMap if present
				if (parameterMap != null) {
					Set<Entry<String, List<String>>> parameterSet = parameterMap.entrySet();

					paramSet.addAll(parameterSet);
				}

				// Include parameters from formMap if present
				if (formMap != null) {
					Set<Entry<String, List<String>>> formSet = formMap.entrySet();

					paramSet.addAll(formSet);
				}

				List<String> typeList = new ArrayList<String>();

				// Iterate thru the parameter map for the _id criteria (special case)
				for (Entry<String, List<String>> entry : paramSet) {

					String key = entry.getKey();

					for (String value : entry.getValue()) {

						if (key.equals("_id") && value != null && value.length() > 0) {
							/*
							 * Check for comma separated list of values
							 * e.g. "id1,id2,id3"
							 */
							String[] idList = value.split("\\,");
							int idListInd = 0;
							int idListCount = idList.length;

							if (idListCount == 1) {
								sbCriteria.append(" and r1.resourceId = '").append(value).append("'");
							}
							else {
								sbCriteria.append(" and r1.resourceId IN ('");
								for (String idValue : idList) {
									sbCriteria.append(idValue);

									idListInd++;

									if (idListInd < idListCount) {
										sbCriteria.append("','");
									}
								}
								sbCriteria.append("')");
							}
						}

						if (key.equals("_type") && value != null && value.length() > 0) {
							/*
							 * Check for special _type parameter and save all Resource Types
							 */
							String[] typeArray = value.split(",");
							for (String type : typeArray) {
								typeList.add(type);
							}
						}
					}
				}

				// Nictiz Enhancement
				// Iterate thru the parameter map for the pharmaceutical-treatment-identifier criteria if the resource type is null/empty
				if (resourceType == null || resourceType.isEmpty()) {

					for (Entry<String, List<String>> entry : paramSet) {

						String key = entry.getKey();

						for (String value : entry.getValue()) {

							if (key.equals("pharmaceutical-treatment-identifier") && value != null && value.length() > 0) {
								sbCriteria.append(" and r1.resourceType IN ('MedicationAdministration','MedicationDispense','MedicationRequest','MedicationStatement')");
							}
						}
					}
				}

				int iExists = 0;
				String sExistsBase = "rm";
				String sExists = "rm";

				// Iterate thru the parameter map and build temporary table select definition
				for (Entry<String, List<String>> entry : paramSet) {

					String key = entry.getKey();
					//log.info("--> Processing search parameter [" + key + "]");

					boolean isValidSearchParameter = false;
					String invalidParamMessage = null;
					String criteriaType = null;

					// Need to check resourceType; if null, then check for special _type parameter
					if (resourceType != null) {
						//log.info("   --> Resource Type is [" + resourceType + "]");
						isValidSearchParameter = net.aegis.fhir.model.ResourceType.isSupportedResourceCriteriaType(resourceType, key);
						if (isValidSearchParameter) {
							// Determine criteria type
							criteriaType = net.aegis.fhir.model.ResourceType.findResourceTypeResourceCriteriaType(resourceType, key);

							// Check for valid parameter type based on full key value
							if (criteriaType == null || criteriaType.isEmpty()) {
								// THIS SHOULD ONLY HAPPEN FOR CHAINED PARAMETERS WITH MULITIPLE POSSIBLE RESOURCE TYPES AND NO DECLARED RESOURCE TYPE
								isValidSearchParameter = false;
								invalidParamMessage = "Chained parameter with multiple possible resource types does not declare explicit resource type!";
							}
						}
					}
					else {
						if (!typeList.isEmpty()) {
							//log.info("   --> Resource Type is null and _type defined");
							for (String type : typeList) {
								//log.info("   --> Processing _type [" + type + "]");
								isValidSearchParameter = net.aegis.fhir.model.ResourceType.isSupportedResourceCriteriaType(type, key);
								if (isValidSearchParameter) {
									//log.info("      --> Valid parameter '" + key + "' for [" + type + "]");
									// Determine criteria type
									criteriaType = net.aegis.fhir.model.ResourceType.findResourceTypeResourceCriteriaType(type, key);

									// Check for valid parameter type based on full key value
									if (criteriaType == null || criteriaType.isEmpty()) {
										// THIS SHOULD ONLY HAPPEN FOR CHAINED PARAMETERS WITH MULITIPLE POSSIBLE RESOURCE TYPES AND NO DECLARED RESOURCE TYPE
										isValidSearchParameter = false;
										invalidParamMessage = "Chained parameter with multiple possible resource types does not declare explicit resource type!";
									}

									break;
								}
							}
						}
						if (!isValidSearchParameter) {
							//log.info("   -->  Resource Type is null and _type not defined; check for global parameter");
							isValidSearchParameter = net.aegis.fhir.model.ResourceType.isSupportedResourceCriteriaType(null, key);
						}
					}

					if (!isValidSearchParameter) {
						// Add invalid parameter to invalidParams list

						if (invalidParams != null) {
							String[] invalidParam = new String[2];
							invalidParam[0] = key;
							invalidParam[1] = invalidParamMessage;
							invalidParams.add(invalidParam);
							log.warning("   --> Invalid Param [" + key + "]");
						}
					}

					if (isValidSearchParameter) {
						//log.info("   --> Process valid search parameter");

						boolean isDateType = (criteriaType.equalsIgnoreCase("DATE") ? true : false);
						boolean isNumericType = (criteriaType.equalsIgnoreCase("NUMBER") ? true : false);
						boolean isPeriodType = (criteriaType.equalsIgnoreCase("PERIOD") ? true : false);
						boolean isQuantityType = (criteriaType.equalsIgnoreCase("QUANTITY") ? true : false);
						boolean isReferenceType = (criteriaType.equalsIgnoreCase("REFERENCE") ? true : false);
						boolean isStringType = (criteriaType.equalsIgnoreCase("STRING") ? true : false);
						boolean isTokenType = (criteriaType.equalsIgnoreCase("TOKEN") ? true : false);
						boolean isUriType = (criteriaType.equalsIgnoreCase("URI") ? true : false);
						boolean splitCriteriaWritten = false;

						String sqValue = "";

						for (String value : entry.getValue()) {

							// Set single quote escaped string value
							if (value.contains("'")) {
								sqValue = value.replaceAll("'", "''");
							}
							else {
								sqValue = value;
							}

							// Save valid parameter name IF validParams is not null

							String[] validParam = new String[2];
							validParam[0] = key;
							validParam[1] = value;
							if (validParams != null) {
								validParams.add(validParam);
								//log.info("   --> Valid Param [" + key + "] Value [" + value + "] sqValue [" + sqValue + "]");
							}

							// Next process known, special parameters

							if (key.equals("_count")) {
								// _count parameter is handled in ResourceOps.search calling method; ignore here

							}
							else if (key.equals("_format")) {
								// _format parameter is handled in ResourceOps.search calling method; ignore here

							}
							else if (key.equals("_summary")) {
								// _summary parameter is handled in ResourceOps.search calling method; ignore here

							}
							else if (key.equals("_elements") || key.equals("_contained")
									|| key.equals("_containedType") || key.equals("_text") || key.equals("_content") || key.equals("_list")
									|| key.equals("_has") || key.equals("_query")) {
								// not supported at this time; ignore here

							}
							else if (key.equals("_id")) {
								// special case handled above

							}
							else if (_include != null && key.equals("_include")) {
								String[] includeArray = value.split(":");
								_include.add(includeArray);

							}
							else if (_includeIterate != null && key.equals("_include:iterate")) {
								String[] includeArray = value.split(":");
								_includeIterate.add(includeArray);

							}
							else if (_revinclude != null && key.equals("_revinclude")) {
								String[] revincludeArray = value.split(":");
								_revinclude.add(revincludeArray);

							}
							else if (key.equals("_sort")) {
								String[] sortValueArray = value.split(",");
								if (sortValueArray.length > 0) {
									String[] sortArray = null;
									for (String sortValue : sortValueArray) {
										sortArray = new String[3];
										if (sortValue.startsWith("-")) {
											sortArray[0] = sortValue.substring(1, sortValue.length());
											sortArray[1] = "desc";
										}
										else {
											sortArray[0] = sortValue;
											sortArray[1] = "asc";
										}
										sortArray[2] = net.aegis.fhir.model.ResourceType.findResourceTypeResourceCriteriaType(resourceType, sortArray[0]);

										_sort.add(sortArray);
									}
									sortArray = null;
								}
								sortValueArray = null;

							}
							else if (key.equals("_since") && value != null && value.length() > 0) {
								if (sbCreateTempWhereCriteria.length() > 5) {
									iExists++;
									sExists = sExistsBase + iExists;

									sbCreateTempSelect.append(", resourcemetadata ").append(sExists);
									sbCreateTempWhereCriteria.append(" and ");
									if (iExists > 1) {
										sbCreateTempWhereJoin.append(" and ");
									}
									sbCreateTempWhereJoin.append(sExists).append(".resourceJoinId = rm.resourceJoinId");
								}

								DateTimeType dateTimeType = new DateTimeType(value);
								Date dateValue = dateTimeType.getValue();
								String stringValue = null;
								if (utcDateUtil.hasTimeZone(value)) {
									stringValue = utcDateUtil.formatDate(dateValue, UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getTimeZone(UTCDateUtil.TIME_ZONE_UTC));

									sbCreateTempWhereCriteria.append("(")
										.append(sExists).append(".paramName = '_lastUpdated' and ").append(sExists).append(".paramValue >= '").append(stringValue).append("')");
								}
								else {
									stringValue = utcDateUtil.formatDate(dateValue, UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault());

									sbCreateTempWhereCriteria.append("(")
										.append(sExists).append(".paramName = '_lastUpdated' and ").append(sExists).append(".codeValue >= '").append(stringValue).append("')");
								}

							}
							else if (key.equals("_type")) {
								if (resourceType == null) {
									String[] typeArray = value.split(",");

									if (typeArray.length > 0) {
										sbCriteria.append(" and r1.resourceType IN (");
										int typeCount = 0;
										for (String type : typeArray) {
											if (typeCount > 0) {
												sbCriteria.append(",");
											}
											sbCriteria.append("'").append(type).append("'");
											typeCount++;
										}
										sbCriteria.append(")");
									}
								}

							}
							else if (key.contains(":missing") && value != null && value.length() > 0) {
								key = key.substring(0, key.indexOf(":missing"));

								if (value.equalsIgnoreCase("true")) {
									sbCriteria.append(" and r1.id NOT IN (select rm.resourceJoinId as id from resourcemetadata rm where rm.paramName = '").append(key).append("')");
								}
								else if (value.equalsIgnoreCase("false")) {
									sbCriteria.append(" and r1.id IN (select rm.resourceJoinId as id from resourcemetadata rm where rm.paramName = '").append(key).append("')");
								}

							}
							else if (key.contains(":exact") && value != null && value.length() > 0) {
								key = key.substring(0, key.indexOf(":exact"));

								if (sbCreateTempWhereCriteria.length() > 5) {
									iExists++;
									sExists = sExistsBase + iExists;

									sbCreateTempSelect.append(", resourcemetadata ").append(sExists);
									sbCreateTempWhereCriteria.append(" and ");
									if (iExists > 1) {
										sbCreateTempWhereJoin.append(" and ");
									}
									sbCreateTempWhereJoin.append(sExists).append(".resourceJoinId = rm.resourceJoinId");
								}
								sbCreateTempWhereCriteria.append("(").append(sExists).append(".paramName = '")
									.append(key).append("' and ").append(sExists).append(".paramValue like '").append(sqValue.toUpperCase()).append("')");

							}
							else if (key.contains(":text") && value != null && value.length() > 0) {
								key = key.substring(0, key.indexOf(":text"));

								if (sbCreateTempWhereCriteria.length() > 5) {
									iExists++;
									sExists = sExistsBase + iExists;

									sbCreateTempSelect.append(", resourcemetadata ").append(sExists);
									sbCreateTempWhereCriteria.append(" and ");
									if (iExists > 1) {
										sbCreateTempWhereJoin.append(" and ");
									}
									sbCreateTempWhereJoin.append(sExists).append(".resourceJoinId = rm.resourceJoinId");
								}
								sbCreateTempWhereCriteria.append("(").append(sExists).append(".paramName = '")
									.append(key).append("' and ").append(sExists).append(".textValueU like '%").append(sqValue.toUpperCase()).append("%')");

							}
							else if (key.contains("COMPARTMENT-") && value != null && value.length() > 0) {
								compartmentSet.add(entry);

							}
							else if (key.equals("age")) {
								Integer ageValue = Integer.valueOf(value);
								Date dateStartValue = utcDateUtil.calculateAgeStartDate(ageValue);
								String stringStartValue = utcDateUtil.formatDate(dateStartValue, UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault());
								Date dateEndValue = utcDateUtil.calculateAgeEndDate(ageValue);
								String stringEndValue = utcDateUtil.formatDate(dateEndValue, UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault());

								if (sbCreateTempWhereCriteria.length() > 5) {
									iExists++;
									sExists = sExistsBase + iExists;

									sbCreateTempSelect.append(", resourcemetadata ").append(sExists);
									sbCreateTempWhereCriteria.append(" and ");
									if (iExists > 1) {
										sbCreateTempWhereJoin.append(" and ");
									}
									sbCreateTempWhereJoin.append(sExists).append(".resourceJoinId = rm.resourceJoinId");
								}

								sbCreateTempWhereCriteria.append("(").append(sExists).append(".paramName = '")
									.append("age' and ").append(sExists).append(".codeValue >= '").append(stringStartValue).append("' and ").append(sExists).append(".codeValue <= '")
										.append(stringEndValue).append("')");

							}
							else if (key.equals("name") || key.contains(".name")) {
								if (sbCreateTempWhereCriteria.length() > 5) {
									iExists++;
									sExists = sExistsBase + iExists;

									sbCreateTempSelect.append(", resourcemetadata ").append(sExists);
									sbCreateTempWhereCriteria.append(" and ");
									if (iExists > 1) {
										sbCreateTempWhereJoin.append(" and ");
									}
									sbCreateTempWhereJoin.append(sExists).append(".resourceJoinId = rm.resourceJoinId");
								}
								sbCreateTempWhereCriteria.append("(").append(sExists).append(".paramName = '")
									.append(key).append("' and ").append(sExists).append(".paramValue like '%").append(sqValue).append("%')");

							}
							else if (key.equals("near") || key.contains(".near")) {
								log.fine("searchQuery - near parameter '" + key + "'");
								// store near parameter latitude|longitude|distance|units for subsequent processing
								String[] nearParam = {"", "", "", ""};
								String[] nearArray = value.split("|");

								if (nearArray.length > 1) {
									nearParam[0] = nearArray[0];
									nearParam[1] = nearArray[1];

									if (nearArray.length > 2) {
										nearParam[2] = nearArray[2];
									}
									else {
										nearParam[2] = "10"; // Default distance to 10
									}
									if (nearArray.length > 3) {
										nearParam[3] = nearArray[3];
									}
									else {
										nearParam[3] = "km"; // Default units to kilometers
									}

									if (nearParam[3].equals("km") || nearParam[3].contains("mi")) {
										log.fine("            - near latitude = [" + nearParam[0] + "]; longitude = [" + nearParam[1] + "]; distance = [" + nearParam[2] + "]; units = [" + nearParam[3] + "]");
										nearParams.put(key, nearParam);
									}
									else {
										String[] invalidParam = new String[2];
										invalidParam[0] = key;
										invalidParam[1] = "near parameter distance units '" + nearParam[3] + "' not supported! Please use 'mi_i', 'mi_us' or 'km'.";
										invalidParams.add(invalidParam);

										// No validParam values found; blank out validParam name and value; skip processing of this param
										validParam[0] = "";
										validParam[1] = "";
										isValidSearchParameter = false;
									}
								}
								else {
									String[] invalidParam = new String[2];
									invalidParam[0] = key;
									invalidParam[1] = "near parameter value '" + value + "' must contain at minimum [latitude]|[longitude].";
									invalidParams.add(invalidParam);

									// No validParam values found; blank out validParam name and value; skip processing of this param
									validParam[0] = "";
									validParam[1] = "";
									isValidSearchParameter = false;
								}

							}
							else if (key.equals("coordinate") || key.contains(".coordinate")) {
								// Special case for Sequence.coordinate search parameter
								String[] sValues = value.split("\\$");
								if (sValues.length != 3) {
									throw new Exception("Invalid coordinate parameter value! Expected composite value formatted as 'n$ltnnn$gtnnn' but found '" + value + "'.");
								}
								String prefixControl = "";
								String prefixValue = "";
								int lowValue = 0;
								int highValue = 999999999;
								if (!sValues[1].isEmpty() && sValues[1].length() > 2) {
									prefixControl = sValues[1].substring(0, 2);
									prefixValue = sValues[1].substring(2);
									if (prefixControl.equals("gt")) {
										lowValue = Integer.parseInt(prefixValue);
									}
									else {
										highValue = Integer.parseInt(prefixValue);
									}
								}
								if (!sValues[2].isEmpty() && sValues[2].length() > 2) {
									prefixControl = sValues[2].substring(0, 2);
									prefixValue = sValues[2].substring(2);
									if (prefixControl.equals("gt")) {
										lowValue = Integer.parseInt(prefixValue);
									}
									else {
										highValue = Integer.parseInt(prefixValue);
									}
								}

								// Check for coordinateSystem
								if (!sValues[0].isEmpty() && sValues[0].equals("0")) {
									lowValue--;
								}

								if (sbCreateTempWhereCriteria.length() > 5) {
									iExists++;
									sExists = sExistsBase + iExists;

									sbCreateTempSelect.append(", resourcemetadata ").append(sExists);
									sbCreateTempWhereCriteria.append(" and ");
									if (iExists > 1) {
										sbCreateTempWhereJoin.append(" and ");
									}
									sbCreateTempWhereJoin.append(sExists).append(".resourceJoinId = rm.resourceJoinId");
								}
								sbCreateTempWhereCriteria.append("(").append(sExists).append(".paramName = '")
									.append(key).append("' and cast(left(").append(sExists).append(".paramValue,9) as signed) > ").append(lowValue)
									.append(" and cast(right(").append(sExists).append(".paramValue,9) as signed) < ").append(highValue).append(")");

							}
							else if (key.equals("max")) {
								/*
								 * Special processing for Observation $lastn - handled in ObservationLastNOperation class
								 *
								 * The resourceType must be 'Observation'.
								 * The first _sort must be set by the ObservationLastNOperation class as the Observation code search parameter.
								 * A SQL group by clause on the first _sort column will be added to the generated query.
								 */
							}
							else {
								if (value != null & value.length() > 0) {
									log.info("resourceType = '" + (resourceType == null ? "null" : resourceType) + "'; key = '" + key + "'; value = '" + value + "'");
									// Initialize TimeZones
									TimeZone timeZoneDefault = TimeZone.getDefault();
									TimeZone timeZoneUTC = TimeZone.getTimeZone(UTCDateUtil.TIME_ZONE_UTC);

									/*
									 * Check for comma separated list of sqValues
									 */
									String[] valueList = sqValue.split("\\,");
									int valueListInd = 0;
									int valueListCount = valueList.length;

									// Parameter value(s) data type check(s)
									boolean isValidParamValues = true;
									String[] validValueList = valueList;

									// Check date, numeric and quantity types
									if (isDateType || isPeriodType || isNumericType || isQuantityType) {
										//log.info("--> date, numeric or quantity parameter type - check for valid parameter value(s) '" + value + "'");
										/*
										 *  IF NOT IS VALID DATE, NUMERIC OR QUANTITY VALUE, ADD TO LIST OF INVALID PARAMETERS
										 *  - Check for valid prefix control if first character is not numeric
										 *  - If valid prefix or no prefix, check for valid data typed value - date, numeric or numeric before '|' character
										 */
										String listPrefixControl = "";
										String listPrefixValue = "";
										boolean isValidListValue = false;
										String notValidMessage = null;
										String[] invalidParam = null;
										validValueList = new String[valueListCount];
										for (String validListValue : valueList) {
											isValidListValue = false;
											notValidMessage = null;

											if (validListValue != null && !validListValue.isEmpty() && validListValue.length() > 0) {

												if (StringUtils.isAlpha(validListValue.substring(0, 1))) {

													if (validListValue.length() > 2) {
														listPrefixControl = validListValue.substring(0, 2);

														if (isMatchingControlPrefix(listPrefixControl)) {
															listPrefixValue = validListValue.substring(2);
															// if date or period, check for valid date format
															if (isDateType || isPeriodType) {
																try {
																	if (utcDateUtil.computeSortFormatLength(listPrefixValue) == 12) {
																		listPrefixValue += ":00";
																	}

																	DateTimeType dateTimeType = new DateTimeType(listPrefixValue);
																	dateTimeType.getValue();
																	isValidListValue = true;
																}
																catch (Exception e) {
																	isValidListValue = false;
																	notValidMessage = e.getMessage();
																}
															}
															// if numeric, check for valid numeric format
															if (isNumericType) {
																isValidListValue = net.aegis.fhir.service.util.StringUtils.isNumericOrDecimal(listPrefixValue);
																if (isValidListValue == false) {
																	notValidMessage = "Invalid number format: '" + listPrefixValue + "'";
																}
															}
															// if quantity, check for valid number before '|' character
															if (isQuantityType) {
																int delimPos = listPrefixValue.indexOf("\\|");
																if (delimPos > 0) {
																	isValidListValue = net.aegis.fhir.service.util.StringUtils.isNumericOrDecimal(listPrefixValue.substring(0, delimPos));
																}
																else {
																	isValidListValue = net.aegis.fhir.service.util.StringUtils.isNumericOrDecimal(listPrefixValue);
																}
																if (isValidListValue == false) {
																	notValidMessage = "Invalid quantity number format: '" + listPrefixValue + "'";
																}
															}
															// if valid, add validListValue to validValueList
															if (isValidListValue == true) {
																validValueList[valueListInd] = validListValue;
															}
															else {
																// INVALID PARAMETER VALUE - Add invalidParams list - Invalid format
																invalidParam = new String[2];
																invalidParam[0] = key;
																invalidParam[1] = "Invalid parameter value '" + validListValue + "'; " + notValidMessage;
																invalidParams.add(invalidParam);
																log.warning("   --> Invalid Param [" + key + "] and value [" + validListValue + "]");
															}
														}
														else {
															// INVALID PARAMETER VALUE - Add invalidParams list - bad prefix control
															invalidParam = new String[2];
															invalidParam[0] = key;
															invalidParam[1] = "Invalid parameter value '" + validListValue + "'; unknown or unsupported prefix control value!";
															invalidParams.add(invalidParam);
															log.warning("   --> Invalid Param [" + key + "] and value [" + validListValue + "]");
														}
													}
													else {
														// INVALID PARAMETER VALUE - Add invalidParams list - data value format does not match expected prefixed date, numeric or quantity
														invalidParam = new String[2];
														invalidParam[0] = key;
														invalidParam[1] = "Invalid parameter value '" + validListValue + "'; data value does not match expected prefixed date, numeric or quantity format!";
														invalidParams.add(invalidParam);
														log.warning("   --> Invalid Param [" + key + "] and value [" + validListValue + "]");
													}
												}
												else if (net.aegis.fhir.service.util.StringUtils.isNumericOrDecimal(validListValue.substring(0, 1))) {
													// if date or period, check for valid date format
													if (isDateType || isPeriodType) {
														try {
															if (utcDateUtil.computeSortFormatLength(listPrefixValue) == 12) {
																listPrefixValue += ":00";
															}

															DateTimeType dateTimeType = new DateTimeType(validListValue);
															dateTimeType.getValue();
															isValidListValue = true;
														}
														catch (Exception e) {
															isValidListValue = false;
															notValidMessage = e.getMessage();
														}
													}
													// if numeric, check for valid numeric format
													if (isNumericType) {
														isValidListValue = net.aegis.fhir.service.util.StringUtils.isNumericOrDecimal(validListValue);
														if (isValidListValue == false) {
															notValidMessage = "Invalid number format: '" + validListValue + "'";
														}
													}
													// if quantity, check for valid number before '|' character
													if (isQuantityType) {
														int delimPos = validListValue.indexOf("\\|");
														if (delimPos > 0) {
															isValidListValue = net.aegis.fhir.service.util.StringUtils.isNumericOrDecimal(validListValue.substring(0, delimPos));
														}
														else {
															isValidListValue = net.aegis.fhir.service.util.StringUtils.isNumericOrDecimal(validListValue);
														}
														if (isValidListValue == false) {
															notValidMessage = "Invalid quantity number format: '" + validListValue + "'";
														}
													}
													// if valid, add validListValue to validValueList
													if (isValidListValue == true) {
														validValueList[valueListInd] = validListValue;
													}
													else {
														// INVALID PARAMETER VALUE - Add invalidParams list - Invalid format
														invalidParam = new String[2];
														invalidParam[0] = key;
														invalidParam[1] = "Invalid parameter value '" + validListValue + "'; " + notValidMessage;
														invalidParams.add(invalidParam);
														log.warning("   --> Invalid Param [" + key + "] and value [" + validListValue + "]");
													}
												}
												else {
													// INVALID PARAMETER VALUE - Add invalidParams list - data value format does not match expected date, numeric or quantity
													invalidParam = new String[2];
													invalidParam[0] = key;
													invalidParam[1] = "Invalid parameter value '" + validListValue + "'; data value does not match expected date, numeric or quantity format!";
													invalidParams.add(invalidParam);
													log.warning("   --> Invalid Param [" + key + "] and value [" + validListValue + "]");
												}
											}
											else {
												// INVALID PARAMETER VALUE - Add invalidParams list - empty value
												invalidParam = new String[2];
												invalidParam[0] = key;
												invalidParam[1] = "Invalid parameter value '" + validListValue + "'; data value cannot be empty!";
												invalidParams.add(invalidParam);
												log.warning("   --> Invalid Param [" + key + "] and value [" + validListValue + "]");
											}
											valueListInd++;
										}

										// Rebuild validParam value
										StringBuffer validParamValue = new StringBuffer("");
										for (String listValue : validValueList) {
											if (listValue != null && !listValue.isEmpty()) {
												if (validParamValue.length() > 2) {
													validParamValue.append(",");
												}
												// ADD THIS VALUE
												validParamValue.append(listValue);
											}
										}
										if (validParamValue.length() == 0) {
											// No validParam values found; blank out validParam name and value; skip processing of this param
											validParam[0] = "";
											validParam[1] = "";
											isValidSearchParameter = false;
											isValidParamValues = false;
										}
										else {
											// Update validParam valid value
											validParam[1] = validParamValue.toString();
										}
									}
									// Check reference type
									else if (isReferenceType) {
										//log.info("   --> Reference parameter type - check for valid parameter value(s) '" + value + "'");
										/*
										 *  IF NOT IS VALID REFERENCE VALUE, ADD TO LIST OF INVALID PARAMETERS
										 *  - Invalid reference parameter value is for a reference parameter where multiple resource types are allowed
										 *  and the value does not contain a valid resource type prefix.
										 *  - Also, if only a single resource type is allowed and the parameter value does not contain a valid resource
										 *  type, modify the parameter value with a prefix of the allowed resource type.
										 */
										String refType = net.aegis.fhir.model.ResourceType.findResourceTypeResourceRefType(resourceType, key);
										//log.info("   --> Reference parameter type - parameter refType '" + refType + "'");
										validValueList = new String[valueListCount];
										for (String listValue : valueList) {
											String validResourceType = net.aegis.fhir.model.ResourceType.findValidResourceType(listValue);

											if (validResourceType != null) {
												//log.info("   --> Reference parameter contains a valid resource type '" + validResourceType + "'");
												validValueList[valueListInd] = listValue;
											}
											else {
												if (refType == null || refType.isEmpty() || refType.equals("*")) {
													//log.info("   --> INVALID! Reference parameter value does not contain valid resource type AND multiple types allowed!");
													// missing refType for search parameter and parameter value does not contain a valid resource type
													// INVALID PARAMETER VALUE - Add invalidParams list

													if (invalidParams != null) {
														String[] invalidParam = new String[2];
														invalidParam[0] = key;
														invalidParam[1] = "Invalid reference parameter value '" + listValue + "'; missing or invalid resource type when parameter can reference multiple resource types!";
														invalidParams.add(invalidParam);
														log.warning("   --> Invalid Param [" + key + "] and value [" + listValue + "]");
													}
												}
												else {
													//log.info("   --> Reference parameter value does not contain valid resource type AND single type allowed '" + refType + "'");
													// single refType for search parameter and parameter value does not contain a valid resource type
													// make sure we extract just the resource id value from the parameter value and then prefix with refType
													String extractedResourceId = ServicesUtil.INSTANCE.extractResourceIdFromURL(listValue);
													if (extractedResourceId != null && !extractedResourceId.isEmpty()) {
														validValueList[valueListInd] = refType + "/" + extractedResourceId;
													}
												}
											}
											valueListInd++;
										}

										// Rebuild validParam value
										StringBuffer validParamValue = new StringBuffer("");
										for (String listValue : validValueList) {
											if (listValue != null && !listValue.isEmpty()) {
												if (validParamValue.length() > 2) {
													validParamValue.append(",");
												}
												// ADD THIS VALUE
												validParamValue.append(listValue);
											}
										}
										if (validParamValue.length() == 0) {
											// No validParam values found; blank out validParam name and value; skip processing of this param
											validParam[0] = "";
											validParam[1] = "";
											isValidSearchParameter = false;
											isValidParamValues = false;
										}
										else {
											// Update validParam valid value
											validParam[1] = validParamValue.toString();
										}
									} // END isReferenceType

									if (isValidParamValues) {
										valueListInd = 0;

										if (sbCreateTempWhereCriteria.length() > 5) {
											iExists++;
											sExists = sExistsBase + iExists;

											sbCreateTempSelect.append(", resourcemetadata ").append(sExists);
											sbCreateTempWhereCriteria.append(" and ");
											if (iExists > 1) {
												sbCreateTempWhereJoin.append(" and ");
											}
											sbCreateTempWhereJoin.append(sExists).append(".resourceJoinId = rm.resourceJoinId");
										}
										if (valueListCount == 1) {
											sbCreateTempWhereCriteria.append("(").append(sExists).append(".paramName = '").append(key).append("' and");
										}
										else {
											sbCreateTempWhereCriteria.append("(").append(sExists).append(".paramName = '").append(key).append("' and ((");
										}

										for (String listValue : validValueList) {
											if (listValue == null || listValue.isEmpty()) {
												// SKIP THIS VALUE - REMOVED AS INVALID
												continue;
											}

											log.info("listValue[" + valueListInd + "] = " + listValue);

											splitCriteriaWritten = false;

											if (valueListInd > 0) {
												sbCreateTempWhereCriteria.append(") or (");
											}

											String prefixControl = "";
											String prefixValue = "";
											String lowRangeValue = "";
											String highRangeValue = "";
											Integer dateFormatLength = Integer.valueOf(14);
											String dateParamValueColName = sExists + ".paramValue";
											String periodStartValueColName = sExists + ".paramValue";
											String periodEndValueColName = sExists + ".systemValue";

											/*
											 * Process system|value|code if found
											 */
											if (listValue.indexOf("|") >= 0) {
												log.info("Process system|value|code; original = " + listValue);

												String[] splitValue = listValue.split("\\|");
												String pairValue = "";
												String pairNamespace = splitValue[0];
												if (splitValue.length > 1) {
													pairValue = splitValue[1];
												}
												String pairCodeValue = "";
												if (splitValue.length > 2) {
													pairCodeValue = splitValue[2];
												}

												if (isDateType || isPeriodType || isNumericType || isQuantityType) {
													/*
													 * if QUANTITY criteria type, switch the namespace and value due to
													 * quantity search criteria ordering of value|system|code
													 */
													if (isQuantityType) {
														log.info("isQuantityType = " + isQuantityType + " switch the namespace and value due to quantity search criteria ordering");

														pairValue = splitValue[0];
														if (splitValue.length > 1) {
															pairNamespace = splitValue[1];
														}
														else {
															pairNamespace = "";
														}
													}

													/*
													 * NEED TO CHECK FOR VALUE MATCHING CONTROL PREFIX
													 */
													if (pairValue.length() > 2) {
														prefixControl = pairValue.substring(0, 2);

														if (isMatchingControlPrefix(prefixControl)) {
															prefixValue = pairValue.substring(2);

															/*
															 * if DATE criteria type, convert date value to DATETIME_SORT_FORMAT
															 */
															if (isDateType || isPeriodType) {
																log.info("isDateType || isPeriodType = " + (isDateType || isPeriodType) + " convert date value to DATETIME_SORT_FORMAT");

																dateFormatLength = utcDateUtil.computeSortFormatLength(prefixValue);
																if (dateFormatLength == 12) {
																	prefixValue += ":00";
																}
																DateTimeType dateTimeType = new DateTimeType(prefixValue);
																Date dateValue = dateTimeType.getValue();

																if (utcDateUtil.hasTimeZone(prefixValue)) {
																	prefixValue = utcDateUtil.formatDate(dateValue, UTCDateUtil.DATETIME_SORT_FORMAT, timeZoneUTC);

																	dateParamValueColName = sExists + ".paramValue";
																	periodStartValueColName = sExists + ".paramValue";
																	periodEndValueColName = sExists + ".systemValue";
																}
																else {
																	prefixValue = utcDateUtil.formatDate(dateValue, UTCDateUtil.DATETIME_SORT_FORMAT, timeZoneDefault, dateFormatLength);

																	dateParamValueColName = sExists + ".codeValue";
																	periodStartValueColName = sExists + ".codeValue";
																	periodEndValueColName = sExists + ".textValue";
																}
															}

															log.info("pairValue = " + pairValue);
															log.info("pairNamespace = " + pairNamespace);
															log.info("pairCodeValue = " + pairCodeValue);

															lowRangeValue = this.computeLowRangeValue(prefixValue, isDateType, isPeriodType, isNumericType, isQuantityType);
															highRangeValue = this.computeHighRangeValue(prefixValue, isDateType, isPeriodType, isNumericType, isQuantityType);

															/*
															 * EQUALS, STARTS AFTER, ENDS BEFORE, APPROXIMATE
															 */
															if (prefixControl.equals("eq") || prefixControl.equals("sa") || prefixControl.equals("eb") || prefixControl.equals("ap")) {
																if (isNumericType || isQuantityType) {
																	// Value is numeric, do not enclose value in quotes
																	sbCreateTempWhereCriteria.append(" (CAST(").append(sExists).append(".paramValue AS DECIMAL(30,15)) >= ").append(lowRangeValue)
																		.append(" AND CAST(").append(sExists).append(".paramValue AS DECIMAL(30,15)) <= ").append(highRangeValue).append(")");
																}
																else if (isPeriodType) {
																	// isPeriodType EQUALS, APPROXIMATE
																	if (prefixControl.equals("eq") || prefixControl.equals("ap")) {
																		sbCreateTempWhereCriteria.append(" (").append(periodStartValueColName).append(" IS NOT NULL AND ").append(periodStartValueColName)
																			.append(" >= '").append(lowRangeValue).append("' AND ").append(periodEndValueColName).append(" IS NOT NULL AND ")
																			.append(periodEndValueColName).append(" <= '").append(highRangeValue).append("')");
																	}
																	// isPeriodType STARTS AFTER
																	if (prefixControl.equals("sa")) {
																		sbCreateTempWhereCriteria.append(" (").append(periodStartValueColName).append(" IS NOT NULL AND ").append(periodStartValueColName)
																			.append(" > '").append(highRangeValue).append("')");
																	}
																	// isPeriodType ENDS BEFORE
																	if (prefixControl.equals("eb")) {
																		sbCreateTempWhereCriteria.append(" (").append(periodEndValueColName).append(" IS NOT NULL AND ").append(periodEndValueColName)
																			.append(" < '").append(lowRangeValue).append("')");
																	}
																}
																else {
																	// isDateType - must check actual paramType in criteria; if PERIOD, apply isPeriodType logic
																	// -- EQUALS, APPROXIMATE
																	if (prefixControl.equals("eq") || prefixControl.equals("ap")) {
																		sbCreateTempWhereCriteria.append(" ((").append(sExists).append(".paramType = 'PERIOD' AND ").append(periodStartValueColName)
																			.append(" IS NOT NULL AND ").append(periodStartValueColName).append(" >= '").append(lowRangeValue).append("' AND ")
																			.append(periodEndValueColName).append(" IS NOT NULL AND ").append(periodEndValueColName).append(" <= '")
																			.append(highRangeValue).append("') OR (").append(sExists).append(".paramType = 'DATE' AND ").append(dateParamValueColName).append(" >= '")
																			.append(lowRangeValue).append("' AND ").append(dateParamValueColName).append(" <= '").append(highRangeValue).append("'))");
																	}
																	// -- STARTS AFTER
																	if (prefixControl.equals("sa")) {
																		sbCreateTempWhereCriteria.append(" ((").append(sExists).append(".paramType = 'PERIOD' AND ").append(periodStartValueColName)
																			.append(" IS NOT NULL AND ").append(periodStartValueColName).append(" > '").append(highRangeValue).append("') OR (")
																			.append(sExists).append(".paramType = 'DATE' AND ").append(dateParamValueColName).append(" > '").append(highRangeValue).append("'))");
																	}
																	// -- ENDS BEFORE
																	if (prefixControl.equals("eb")) {
																		sbCreateTempWhereCriteria.append(" ((").append(sExists).append(".paramType = 'PERIOD' AND ").append(periodEndValueColName)
																			.append(" IS NOT NULL AND ").append(periodEndValueColName).append(" < '").append(lowRangeValue).append("') OR (")
																			.append(sExists).append(".paramType = 'DATE' AND ").append(dateParamValueColName).append(" < '").append(lowRangeValue).append("'))");
																	}
																}
																splitCriteriaWritten = true;
															}
															/*
															 * NOT EQUALS
															 */
															else if (prefixControl.equals("ne")) {
																if (isNumericType || isQuantityType) {
																	// Value is numeric, do not enclose value in quotes
																	sbCreateTempWhereCriteria.append(" (CAST(").append(sExists).append(".paramValue AS DECIMAL(30,15)) > ").append(lowRangeValue)
																		.append(" OR CAST(").append(sExists).append(".paramValue AS DECIMAL(30,15)) < ").append(highRangeValue).append(")");
																}
																else if (isPeriodType) {
																	// isPeriodType NOT EQUALS
																	sbCreateTempWhereCriteria.append(" ((").append(periodStartValueColName).append(" IS NULL OR ").append(periodEndValueColName)
																		.append(" IS NULL) OR ((").append(periodStartValueColName).append(" IS NOT NULL AND ").append(periodStartValueColName)
																		.append(" < '").append(lowRangeValue).append("') OR (").append(periodEndValueColName).append(" IS NOT NULL AND ")
																		.append(periodEndValueColName).append(" > '").append(highRangeValue).append("')))");
																}
																else {
																	// isDateType NOT EQUALS - must check actual paramType in criteria; if PERIOD, apply isPeriodType logic
																	sbCreateTempWhereCriteria.append(" ((").append(sExists).append(".paramType = 'PERIOD' AND ((").append(periodStartValueColName)
																		.append(" IS NULL OR ").append(periodEndValueColName).append(" IS NULL) OR ((").append(periodStartValueColName).append(" IS NOT NULL AND ")
																		.append(periodStartValueColName).append(" < '").append(lowRangeValue).append("') OR (").append(periodEndValueColName).append(" IS NOT NULL AND ")
																		.append(periodEndValueColName).append(" > '").append(highRangeValue).append("')))) OR (").append(sExists).append(".paramType = 'DATE' AND (")
																		.append(dateParamValueColName).append(" < '").append(lowRangeValue).append("' OR ").append(dateParamValueColName).append(" > '")
																		.append(highRangeValue).append("')))");
																}
																splitCriteriaWritten = true;
															}
															/*
															 * GREATER THAN
															 */
															else if (prefixControl.equals("gt")) {
																if (isNumericType || isQuantityType) {
																	// Value is numeric, do not enclose value in quotes
																	sbCreateTempWhereCriteria.append(" CAST(").append(sExists).append(".paramValue AS DECIMAL(30,15)) > ").append(highRangeValue);
																}
																else if (isPeriodType) {
																	// isPeriodType GREATER THAN
																	sbCreateTempWhereCriteria.append(" (").append(periodEndValueColName).append(" IS NULL OR ").append(periodEndValueColName)
																		.append(" > '").append(highRangeValue).append("')");
																}
																else {
																	// isDateType GREATER THAN - must check actual paramType in criteria; if PERIOD, apply isPeriodType logic
																	sbCreateTempWhereCriteria.append(" ((").append(sExists).append(".paramType = 'PERIOD' AND (").append(periodEndValueColName)
																		.append(" IS NULL or ").append(periodEndValueColName).append(" > '").append(highRangeValue).append("')) OR (").append(sExists)
																		.append(".paramType = 'DATE' AND ").append(dateParamValueColName).append(" > '").append(highRangeValue).append("'))");
																}
																splitCriteriaWritten = true;
															}
															/*
															 * LESS THAN
															 */
															else if (prefixControl.equals("lt")) {
																if (isNumericType || isQuantityType) {
																	// Value is numeric, do not enclose value in quotes
																	sbCreateTempWhereCriteria.append(" CAST(").append(sExists).append(".paramValue AS DECIMAL(30,15)) < ").append(lowRangeValue);
																}
																else if (isPeriodType) {
																	// isPeriodType LESS THAN
																	sbCreateTempWhereCriteria.append(" (").append(periodStartValueColName).append(" IS NULL OR ").append(periodStartValueColName)
																		.append(" < '").append(lowRangeValue).append("')");
																}
																else {
																	// isDateType LESS THAN - must check actual paramType in criteria; if PERIOD, apply isPeriodType logic
																	sbCreateTempWhereCriteria.append(" ((").append(sExists).append(".paramType = 'PERIOD' and (").append(periodStartValueColName)
																		.append(" IS NULL OR ").append(periodStartValueColName).append(" < '").append(lowRangeValue).append("')) OR (")
																		.append(sExists).append(".paramType = 'DATE' AND ").append(dateParamValueColName).append(" < '").append(lowRangeValue).append("'))");
																}
																splitCriteriaWritten = true;
															}
															/*
															 * GREATER THAN OR EQUALS
															 */
															else if (prefixControl.equals("ge")) {
																if (isNumericType || isQuantityType) {
																	// Value is numeric, do not enclose value in quotes
																	sbCreateTempWhereCriteria.append(" CAST(").append(sExists).append(".paramValue AS DECIMAL(30,15)) >= ").append(highRangeValue);
																}
																else if (isPeriodType) {
																	// isPeriodType GREATER THAN OR EQUALS
																	sbCreateTempWhereCriteria.append(" ((").append(periodEndValueColName).append(" IS NULL OR ").append(periodEndValueColName)
																		.append(" > '").append(highRangeValue).append("') OR (").append(periodStartValueColName).append(" IS NOT NULL AND ")
																		.append(periodStartValueColName).append(" >= '").append(lowRangeValue).append("' AND ").append(periodEndValueColName)
																		.append(" IS NOT NULL AND ").append(periodEndValueColName).append(" <= '").append(highRangeValue).append("'))");
																}
																else {
																	// isDateType GREATER THAN OR EQUALS - must check actual paramType in criteria; if PERIOD, apply isPeriodType logic
																	sbCreateTempWhereCriteria.append(" ((").append(sExists).append(".paramType = 'PERIOD' AND ((").append(periodEndValueColName)
																		.append(" IS NULL OR ").append(periodEndValueColName).append(" > '").append(highRangeValue).append("') OR (")
																		.append(periodStartValueColName).append(" IS NOT NULL AND ").append(periodStartValueColName).append(" >= '").append(lowRangeValue)
																		.append("' AND ").append(periodEndValueColName).append(" IS NOT NULL AND ").append(periodEndValueColName).append(" <= '")
																		.append(highRangeValue).append("'))) OR (").append(sExists).append(".paramType = 'DATE' AND ((").append(dateParamValueColName)
																		.append(" >= '").append(highRangeValue).append("') OR (").append(dateParamValueColName).append(" >= '").append(lowRangeValue)
																		.append("' AND ").append(dateParamValueColName).append(" <= '").append(highRangeValue).append("'))))");
																}
																splitCriteriaWritten = true;
															}
															/*
															 * LESS THAN OR EQUALS
															 */
															else if (prefixControl.equals("le")) {
																if (isNumericType || isQuantityType) {
																	// Value is numeric, do not enclose value in quotes
																	sbCreateTempWhereCriteria.append(" CAST(").append(sExists).append(".paramValue AS DECIMAL(30,15)) <= ").append(lowRangeValue);
																}
																else if (isPeriodType) {
																	// isPeriodType LESS THAN OR EQUALS
																	sbCreateTempWhereCriteria.append(" ((").append(periodStartValueColName).append(" IS NULL OR ").append(periodStartValueColName)
																		.append(" < '").append(lowRangeValue).append("') OR (").append(periodStartValueColName).append(" IS NOT NULL AND ")
																		.append(periodStartValueColName).append(" >= '").append(lowRangeValue).append("' AND ").append(periodEndValueColName)
																		.append(" IS NOT NULL AND ").append(periodEndValueColName).append(" <= '").append(highRangeValue).append("'))");
																}
																else {
																	// isDateType LESS THAN OR EQUALS - must check actual paramType in criteria; if PERIOD, apply isPeriodType logic
																	sbCreateTempWhereCriteria.append(" ((").append(sExists).append(".paramType = 'PERIOD' AND ((").append(periodStartValueColName)
																		.append(" IS NULL OR ").append(periodStartValueColName).append(" < '").append(lowRangeValue).append("') OR (")
																		.append(periodStartValueColName).append(" IS NOT NULL AND ").append(periodStartValueColName).append(" >= '")
																		.append(lowRangeValue).append("' AND ").append(periodEndValueColName).append(" IS NOT NULL AND ").append(periodEndValueColName)
																		.append(" <= '").append(highRangeValue).append("'))) OR (").append(sExists).append(".paramType = 'DATE' AND ((")
																		.append(dateParamValueColName).append(" <= '").append(lowRangeValue).append("') OR (").append(dateParamValueColName).append(" >= '")
																		.append(lowRangeValue).append("' AND ").append(dateParamValueColName).append(" <= '").append(highRangeValue).append("'))))");
																}
																splitCriteriaWritten = true;
															}
														}
														else {
															prefixControl = "";
														}
													}

													if (prefixControl.isEmpty()) {
														/*
														 * if DATE criteria type, convert date value to DATETIME_SORT_FORMAT
														 */
														if (isDateType || isPeriodType) {
															log.info("isDateType || isPeriodType = " + (isDateType || isPeriodType) + " convert date value to DATETIME_SORT_FORMAT");

															dateFormatLength = utcDateUtil.computeSortFormatLength(pairValue);
															if (dateFormatLength == 12) {
																pairValue += ":00";
															}
															DateTimeType dateTimeType = new DateTimeType(pairValue);
															Date dateValue = dateTimeType.getValue();

															if (utcDateUtil.hasTimeZone(pairValue)) {
																pairValue = utcDateUtil.formatDate(dateValue, UTCDateUtil.DATETIME_SORT_FORMAT, timeZoneUTC);

																dateParamValueColName = sExists + ".paramValue";
																periodStartValueColName = sExists + ".paramValue";
																periodEndValueColName = sExists + ".systemValue";
															}
															else {
																pairValue = utcDateUtil.formatDate(dateValue, UTCDateUtil.DATETIME_SORT_FORMAT, timeZoneDefault, dateFormatLength);

																dateParamValueColName = sExists + ".codeValue";
																periodStartValueColName = sExists + ".codeValue";
																periodEndValueColName = sExists + ".textValue";
															}
														}

														/*
														 * DEFAULT TO EQUALS
														 */
														lowRangeValue = this.computeLowRangeValue(pairValue, isDateType, isPeriodType, isNumericType, isQuantityType);
														highRangeValue = this.computeHighRangeValue(pairValue, isDateType, isPeriodType, isNumericType, isQuantityType);

														if (splitCriteriaWritten == true) {
															sbCreateTempWhereCriteria.append(" and");
														}
														if (isNumericType || isQuantityType) {
															// Value is numeric, do not enclose value in quotes
															sbCreateTempWhereCriteria.append(" (CAST(").append(sExists).append(".paramValue AS DECIMAL(30,15)) >= ").append(lowRangeValue)
																.append(" and CAST(").append(sExists).append(".paramValue AS DECIMAL(30,15)) <= ").append(highRangeValue).append(")");
														}
														else if (isPeriodType) {
															// isPeriodType DEFAULT TO EQUALS
															sbCreateTempWhereCriteria.append(" (").append(periodStartValueColName).append(" IS NOT NULL AND ").append(periodStartValueColName)
																.append(" >= '").append(lowRangeValue).append("' AND ").append(periodEndValueColName).append(" IS NOT NULL AND ")
																.append(periodEndValueColName).append(" <= '").append(highRangeValue).append("')");
														}
														else {
															// isDateType DEFAULT TO EQUALS - must check actual paramType in criteria; if PERIOD, apply isPeriodType logic
															sbCreateTempWhereCriteria.append(" ((").append(sExists).append(".paramType = 'PERIOD' AND ").append(periodStartValueColName)
																.append(" IS NOT NULL AND ").append(periodStartValueColName).append(" >= '").append(lowRangeValue).append("' AND ")
																.append(periodEndValueColName).append(" IS NOT NULL AND ").append(periodEndValueColName).append(" <= '")
																.append(highRangeValue).append("') OR (").append(sExists).append(".paramType = 'DATE' AND ").append(dateParamValueColName).append(" >= '")
																.append(lowRangeValue).append("' AND ").append(dateParamValueColName).append(" <= '").append(highRangeValue).append("'))");
														}
													}
												}
												else {
													// FHIR-164/KT-130 - exclude paramValue criteria if empty
													if (!StringUtils.isEmpty(pairValue)) {
														sbCreateTempWhereCriteria.append(" ").append(sExists).append(".paramValue = '").append(pairValue).append("'");
													}
													else {
														sbCreateTempWhereCriteria.append(" 1 = 1");
													}
												}

												if (!StringUtils.isEmpty(pairNamespace)) {
													sbCreateTempWhereCriteria.append(" and ").append(sExists).append(".systemValue = '").append(pairNamespace).append("'");
												}
												if (!StringUtils.isEmpty(pairCodeValue)) {
													sbCreateTempWhereCriteria.append(" and ").append(sExists).append(".codeValue = '").append(pairCodeValue).append("'");
												}
											}
											/*
											 * Process non-delimited value
											 */
											else {
												log.info("Process non-delimited; value = " + listValue);

												if (isDateType || isPeriodType || isNumericType || isQuantityType) {
													/*
													 * NEED TO CHECK FOR VALUE MATCHING CONTROL PREFIX
													 */
													if (listValue.length() > 2) {
														prefixControl = listValue.substring(0, 2);

														if (isMatchingControlPrefix(prefixControl)) {
															prefixValue = listValue.substring(2);

															/*
															 * if DATE criteria type, convert date value to DATETIME_SORT_FORMAT
															 */
															if (isDateType || isPeriodType) {
																log.info("isDateType || isPeriodType = " + (isDateType || isPeriodType) + " convert date value to DATETIME_SORT_FORMAT");

																dateFormatLength = utcDateUtil.computeSortFormatLength(prefixValue);
																if (dateFormatLength == 12) {
																	prefixValue += ":00";
																}
																DateTimeType dateTimeType = new DateTimeType(prefixValue);
																Date dateValue = dateTimeType.getValue();

																if (utcDateUtil.hasTimeZone(prefixValue)) {
																	prefixValue = utcDateUtil.formatDate(dateValue, UTCDateUtil.DATETIME_SORT_FORMAT, timeZoneUTC);

																	dateParamValueColName = sExists + ".paramValue";
																	periodStartValueColName = sExists + ".paramValue";
																	periodEndValueColName = sExists + ".systemValue";
																}
																else {
																	prefixValue = utcDateUtil.formatDate(dateValue, UTCDateUtil.DATETIME_SORT_FORMAT, timeZoneDefault, dateFormatLength);

																	dateParamValueColName = sExists + ".codeValue";
																	periodStartValueColName = sExists + ".codeValue";
																	periodEndValueColName = sExists + ".textValue";
																}
															}

															lowRangeValue = this.computeLowRangeValue(prefixValue, isDateType, isPeriodType, isNumericType, isQuantityType);
															highRangeValue = this.computeHighRangeValue(prefixValue, isDateType, isPeriodType, isNumericType, isQuantityType);

															/*
															 * EQUALS, STARTS AFTER, ENDS BEFORE, APPROXIMATE
															 */
															if (prefixControl.equals("eq") || prefixControl.equals("sa") || prefixControl.equals("eb") || prefixControl.equals("ap")) {
																if (isNumericType || isQuantityType) {
																	// Value is numeric, do not enclose value in quotes
																	sbCreateTempWhereCriteria.append(" (CAST(").append(sExists).append(".paramValue AS DECIMAL(30,15)) >= ").append(lowRangeValue)
																		.append(" AND CAST(").append(sExists).append(".paramValue AS DECIMAL(30,15)) <= ").append(highRangeValue).append(")");
																}
																else if (isPeriodType) {
																	// isPeriodType EQUALS, APPROXIMATE
																	if (prefixControl.equals("eq") || prefixControl.equals("ap")) {
																		sbCreateTempWhereCriteria.append(" (").append(periodStartValueColName).append(" IS NOT NULL AND ").append(periodStartValueColName)
																			.append(" >= '").append(lowRangeValue).append("' AND ").append(periodEndValueColName).append(" IS NOT NULL AND ")
																			.append(periodEndValueColName).append(" <= '").append(highRangeValue).append("')");
																	}
																	// isPeriodType STARTS AFTER
																	if (prefixControl.equals("sa")) {
																		sbCreateTempWhereCriteria.append(" (").append(periodStartValueColName).append(" IS NOT NULL AND ").append(periodStartValueColName)
																			.append(" > '").append(highRangeValue).append("')");
																	}
																	// isPeriodType ENDS BEFORE
																	if (prefixControl.equals("eb")) {
																		sbCreateTempWhereCriteria.append(" (").append(periodEndValueColName).append(" IS NOT NULL AND ").append(periodEndValueColName)
																			.append(" < '").append(lowRangeValue).append("')");
																	}
																}
																else {
																	// isDateType - must check actual paramType in criteria; if PERIOD, apply isPeriodType logic
																	// -- EQUALS, APPROXIMATE
																	if (prefixControl.equals("eq") || prefixControl.equals("ap")) {
																		sbCreateTempWhereCriteria.append(" ((").append(sExists).append(".paramType = 'PERIOD' AND ").append(periodStartValueColName)
																			.append(" IS NOT NULL AND ").append(periodStartValueColName).append(" >= '").append(lowRangeValue).append("' AND ")
																			.append(periodEndValueColName).append(" IS NOT NULL AND ").append(periodEndValueColName).append(" <= '")
																			.append(highRangeValue).append("') OR (").append(sExists).append(".paramType = 'DATE' AND ").append(dateParamValueColName).append(" >= '")
																			.append(lowRangeValue).append("' AND ").append(dateParamValueColName).append(" <= '").append(highRangeValue).append("'))");
																	}
																	// -- STARTS AFTER
																	if (prefixControl.equals("sa")) {
																		sbCreateTempWhereCriteria.append(" ((").append(sExists).append(".paramType = 'PERIOD' AND ").append(periodStartValueColName)
																			.append(" IS NOT NULL AND ").append(periodStartValueColName).append(" > '").append(highRangeValue).append("') OR (")
																			.append(sExists).append(".paramType = 'DATE' AND ").append(dateParamValueColName).append(" > '").append(highRangeValue).append("'))");
																	}
																	// -- ENDS BEFORE
																	if (prefixControl.equals("eb")) {
																		sbCreateTempWhereCriteria.append(" ((").append(sExists).append(".paramType = 'PERIOD' AND ").append(periodEndValueColName)
																			.append(" IS NOT NULL AND ").append(periodEndValueColName).append(" < '").append(lowRangeValue).append("') OR (")
																			.append(sExists).append(".paramType = 'DATE' AND ").append(dateParamValueColName).append(" < '").append(lowRangeValue).append("'))");
																	}
																}
																splitCriteriaWritten = true;
															}
															/*
															 * NOT EQUALS
															 */
															else if (prefixControl.equals("ne")) {
																if (isNumericType || isQuantityType) {
																	// Value is numeric, do not enclose value in quotes
																	sbCreateTempWhereCriteria.append(" (CAST(").append(sExists).append(".paramValue AS DECIMAL(30,15)) > ").append(lowRangeValue)
																		.append(" OR CAST(").append(sExists).append(".paramValue AS DECIMAL(30,15)) < ").append(highRangeValue).append(")");
																}
																else if (isPeriodType) {
																	// isPeriodType NOT EQUALS
																	sbCreateTempWhereCriteria.append(" ((").append(periodStartValueColName).append(" IS NULL OR ").append(periodEndValueColName)
																		.append(" IS NULL) OR ((").append(periodStartValueColName).append(" IS NOT NULL AND ").append(periodStartValueColName)
																		.append(" < '").append(lowRangeValue).append("') OR (").append(periodEndValueColName).append(" IS NOT NULL AND ")
																		.append(periodEndValueColName).append(" > '").append(highRangeValue).append("')))");
																}
																else {
																	// isDateType NOT EQUALS - must check actual paramType in criteria; if PERIOD, apply isPeriodType logic
																	sbCreateTempWhereCriteria.append(" ((").append(sExists).append(".paramType = 'PERIOD' AND ((").append(periodStartValueColName)
																		.append(" IS NULL OR ").append(periodEndValueColName).append(" IS NULL) OR ((").append(periodStartValueColName).append(" IS NOT NULL AND ")
																		.append(periodStartValueColName).append(" < '").append(lowRangeValue).append("') OR (").append(periodEndValueColName).append(" IS NOT NULL AND ")
																		.append(periodEndValueColName).append(" > '").append(highRangeValue).append("')))) OR (").append(sExists).append(".paramType = 'DATE' AND (")
																		.append(dateParamValueColName).append(" < '").append(lowRangeValue).append("' OR ").append(dateParamValueColName).append(" > '")
																		.append(highRangeValue).append("')))");
																}
																splitCriteriaWritten = true;
															}
															/*
															 * GREATER THAN
															 */
															else if (prefixControl.equals("gt")) {
																if (isNumericType || isQuantityType) {
																	// Value is numeric, do not enclose value in quotes
																	sbCreateTempWhereCriteria.append(" CAST(").append(sExists).append(".paramValue AS DECIMAL(30,15)) > ").append(highRangeValue);
																}
																else if (isPeriodType) {
																	// isPeriodType GREATER THAN
																	sbCreateTempWhereCriteria.append(" (").append(periodEndValueColName).append(" IS NULL OR ").append(periodEndValueColName)
																		.append(" > '").append(highRangeValue).append("')");
																}
																else {
																	// isDateType GREATER THAN - must check actual paramType in criteria; if PERIOD, apply isPeriodType logic
																	sbCreateTempWhereCriteria.append(" ((").append(sExists).append(".paramType = 'PERIOD' AND (").append(periodEndValueColName)
																		.append(" IS NULL or ").append(periodEndValueColName).append(" > '").append(highRangeValue).append("')) OR (").append(sExists)
																		.append(".paramType = 'DATE' AND ").append(dateParamValueColName).append(" > '").append(highRangeValue).append("'))");
																}
																splitCriteriaWritten = true;
															}
															/*
															 * LESS THAN
															 */
															else if (prefixControl.equals("lt")) {
																if (isNumericType || isQuantityType) {
																	// Value is numeric, do not enclose value in quotes
																	sbCreateTempWhereCriteria.append(" CAST(").append(sExists).append(".paramValue AS DECIMAL(30,15)) < ").append(lowRangeValue);
																}
																else if (isPeriodType) {
																	// isPeriodType LESS THAN
																	sbCreateTempWhereCriteria.append(" (").append(periodStartValueColName).append(" IS NULL OR ").append(periodStartValueColName)
																		.append(" < '").append(lowRangeValue).append("')");
																}
																else {
																	// isDateType LESS THAN - must check actual paramType in criteria; if PERIOD, apply isPeriodType logic
																	sbCreateTempWhereCriteria.append(" ((").append(sExists).append(".paramType = 'PERIOD' and (").append(periodStartValueColName)
																		.append(" IS NULL OR ").append(periodStartValueColName).append(" < '").append(lowRangeValue).append("')) OR (")
																		.append(sExists).append(".paramType = 'DATE' AND ").append(dateParamValueColName).append(" < '").append(lowRangeValue).append("'))");
																}
																splitCriteriaWritten = true;
															}
															/*
															 * GREATER THAN OR EQUALS
															 */
															else if (prefixControl.equals("ge")) {
																if (isNumericType || isQuantityType) {
																	// Value is numeric, do not enclose value in quotes
																	sbCreateTempWhereCriteria.append(" CAST(").append(sExists).append(".paramValue AS DECIMAL(30,15)) >= ").append(highRangeValue);
																}
																else if (isPeriodType) {
																	// isPeriodType GREATER THAN OR EQUALS
																	sbCreateTempWhereCriteria.append(" ((").append(periodEndValueColName).append(" IS NULL OR ").append(periodEndValueColName)
																		.append(" > '").append(highRangeValue).append("') OR (").append(periodStartValueColName).append(" IS NOT NULL AND ")
																		.append(periodStartValueColName).append(" >= '").append(lowRangeValue).append("' AND ").append(periodEndValueColName)
																		.append(" IS NOT NULL AND ").append(periodEndValueColName).append(" <= '").append(highRangeValue).append("'))");
																}
																else {
																	// isDateType GREATER THAN OR EQUALS - must check actual paramType in criteria; if PERIOD, apply isPeriodType logic
																	sbCreateTempWhereCriteria.append(" ((").append(sExists).append(".paramType = 'PERIOD' AND ((").append(periodEndValueColName)
																		.append(" IS NULL OR ").append(periodEndValueColName).append(" > '").append(highRangeValue).append("') OR (")
																		.append(periodStartValueColName).append(" IS NOT NULL AND ").append(periodStartValueColName).append(" >= '").append(lowRangeValue)
																		.append("' AND ").append(periodEndValueColName).append(" IS NOT NULL AND ").append(periodEndValueColName).append(" <= '")
																		.append(highRangeValue).append("'))) OR (").append(sExists).append(".paramType = 'DATE' AND ((").append(dateParamValueColName)
																		.append(" >= '").append(highRangeValue).append("') OR (").append(dateParamValueColName).append(" >= '").append(lowRangeValue)
																		.append("' AND ").append(dateParamValueColName).append(" <= '").append(highRangeValue).append("'))))");
																}
																splitCriteriaWritten = true;
															}
															/*
															 * LESS THAN OR EQUALS
															 */
															else if (prefixControl.equals("le")) {
																if (isNumericType || isQuantityType) {
																	// Value is numeric, do not enclose value in quotes
																	sbCreateTempWhereCriteria.append(" CAST(").append(sExists).append(".paramValue AS DECIMAL(30,15)) <= ").append(lowRangeValue);
																}
																else if (isPeriodType) {
																	// isPeriodType LESS THAN OR EQUALS
																	sbCreateTempWhereCriteria.append(" ((").append(periodStartValueColName).append(" IS NULL OR ").append(periodStartValueColName)
																		.append(" < '").append(lowRangeValue).append("') OR (").append(periodStartValueColName).append(" IS NOT NULL AND ")
																		.append(periodStartValueColName).append(" >= '").append(lowRangeValue).append("' AND ").append(periodEndValueColName)
																		.append(" IS NOT NULL AND ").append(periodEndValueColName).append(" <= '").append(highRangeValue).append("'))");
																}
																else {
																	// isDateType LESS THAN OR EQUALS - must check actual paramType in criteria; if PERIOD, apply isPeriodType logic
																	sbCreateTempWhereCriteria.append(" ((").append(sExists).append(".paramType = 'PERIOD' AND ((").append(periodStartValueColName)
																		.append(" IS NULL OR ").append(periodStartValueColName).append(" < '").append(lowRangeValue).append("') OR (")
																		.append(periodStartValueColName).append(" IS NOT NULL AND ").append(periodStartValueColName).append(" >= '")
																		.append(lowRangeValue).append("' AND ").append(periodEndValueColName).append(" IS NOT NULL AND ").append(periodEndValueColName)
																		.append(" <= '").append(highRangeValue).append("'))) OR (").append(sExists).append(".paramType = 'DATE' AND ((")
																		.append(dateParamValueColName).append(" <= '").append(lowRangeValue).append("') OR (").append(dateParamValueColName).append(" >= '")
																		.append(lowRangeValue).append("' AND ").append(dateParamValueColName).append(" <= '").append(highRangeValue).append("'))))");
																}
																splitCriteriaWritten = true;
															}
														}
														else {
															prefixControl = "";
														}
													}

													if (prefixControl.isEmpty()) {
														/*
														 * if DATE criteria type, convert date value to DATETIME_SORT_FORMAT
														 */
														if (isDateType || isPeriodType) {
															log.info("isDateType || isPeriodType = " + (isDateType || isPeriodType) + " convert date value to DATETIME_SORT_FORMAT");

															dateFormatLength = utcDateUtil.computeSortFormatLength(listValue);
															if (dateFormatLength == 12) {
																listValue += ":00";
															}
															DateTimeType dateTimeType = new DateTimeType(listValue);
															Date dateValue = dateTimeType.getValue();

															if (utcDateUtil.hasTimeZone(listValue)) {
																listValue = utcDateUtil.formatDate(dateValue, UTCDateUtil.DATETIME_SORT_FORMAT, timeZoneUTC);

																dateParamValueColName = sExists + ".paramValue";
																periodStartValueColName = sExists + ".paramValue";
																periodEndValueColName = sExists + ".systemValue";
															}
															else {
																listValue = utcDateUtil.formatDate(dateValue, UTCDateUtil.DATETIME_SORT_FORMAT, timeZoneDefault, dateFormatLength);

																dateParamValueColName = sExists + ".codeValue";
																periodStartValueColName = sExists + ".codeValue";
																periodEndValueColName = sExists + ".textValue";
															}
														}

														/*
														 * DEFAULT TO EQUALS
														 */
														lowRangeValue = this.computeLowRangeValue(listValue, isDateType, isPeriodType, isNumericType, isQuantityType);
														highRangeValue = this.computeHighRangeValue(listValue, isDateType, isPeriodType, isNumericType, isQuantityType);

														if (splitCriteriaWritten == true) {
															sbCreateTempWhereCriteria.append(" and");
														}
														if (isNumericType || isQuantityType) {
															// Value is numeric, do not enclose value in quotes
															sbCreateTempWhereCriteria.append(" (CAST(").append(sExists).append(".paramValue AS DECIMAL(30,15)) >= ").append(lowRangeValue)
																.append(" and CAST(").append(sExists).append(".paramValue AS DECIMAL(30,15)) <= ").append(highRangeValue).append(")");
														}
														else if (isPeriodType) {
															// isPeriodType DEFAULT TO EQUALS
															sbCreateTempWhereCriteria.append(" (").append(periodStartValueColName).append(" IS NOT NULL AND ").append(periodStartValueColName)
																.append(" >= '").append(lowRangeValue).append("' AND ").append(periodEndValueColName).append(" IS NOT NULL AND ")
																.append(periodEndValueColName).append(" <= '").append(highRangeValue).append("')");
														}
														else {
															// isDateType DEFAULT TO EQUALS - must check actual paramType in criteria; if PERIOD, apply isPeriodType logic
															sbCreateTempWhereCriteria.append(" ((").append(sExists).append(".paramType = 'PERIOD' AND ").append(periodStartValueColName)
																.append(" IS NOT NULL AND ").append(periodStartValueColName).append(" >= '").append(lowRangeValue).append("' AND ")
																.append(periodEndValueColName).append(" IS NOT NULL AND ").append(periodEndValueColName).append(" <= '")
																.append(highRangeValue).append("') OR (").append(sExists).append(".paramType = 'DATE' AND ").append(dateParamValueColName).append(" >= '")
																.append(lowRangeValue).append("' AND ").append(dateParamValueColName).append(" <= '").append(highRangeValue).append("'))");
														}
													}
												}
												else {
													/*
													 * if not DATE, PERIOD, NUMERIC OR QUANTITY criteria type, check for TOKEN, STRING, URI or REFERENCE (default)
													 */
													if (isStringType) {
														// CASE-INSENSITIVE, STARTS WITH OR EXACT MATCH
														sbCreateTempWhereCriteria.append(" ").append(sExists).append(".paramValueU like '").append(listValue.toUpperCase()).append("%'");
													}
													else if (isTokenType) {
														// CASE-INSENSITIVE, EXACT MATCH
														sbCreateTempWhereCriteria.append(" ").append(sExists).append(".paramValueU = '").append(listValue.toUpperCase()).append("'");
													}
													else if (isUriType) {
														// CASE-SENSITIVE, EXACT MATCH
														// Force parameter value comparison to be case-sensitive via MySQL binary qualifier on string value
														sbCreateTempWhereCriteria.append(" ").append(sExists).append(".paramValue = binary '").append(listValue).append("'");
													}
													else if (isReferenceType) {
														// (REFERENCE) CASE-SENSITIVE, ENDS WITH OR EXACT MATCH
														// Force parameter value comparison to be case-sensitive via MySQL binary qualifier on string value
														sbCreateTempWhereCriteria.append(" ").append(sExists).append(".paramValue like binary '%").append(listValue).append("'");
													}
													else {
														// (default) CASE-INSENSITIVE, CONTAINS OR EXACT MATCH
														sbCreateTempWhereCriteria.append(" ").append(sExists).append(".paramValueU like '%").append(listValue.toUpperCase()).append("%'");
													}
												}
											}

											valueListInd++;
										}

										if (valueListCount > 1) {
											sbCreateTempWhereCriteria.append(")))");
										}
										else {
											sbCreateTempWhereCriteria.append(")");
										}
									}
								}
							}
						}

						parameterCount++;

						// At least one parameter is valid
						if (isValidSearchParameter) {
							isValidSearchParameters = true;
						}
					} // End If valid search parameter

				} // End Iterate thru the parameter map

				// Check for and process near parameters
				if (!nearParams.isEmpty()) {
					Set<Entry<String, String[]>> nearParamSet = nearParams.entrySet();

					// Iterate thru the near parameter map and build criteria
					for (Entry<String, String[]> entry : nearParamSet) {
						log.fine("searchQuery - process near parameter '" + entry.getKey() + "'");

						// build near SQL criteria based on distance units
						if (entry.getValue()[3].contains("mi")) {
							if (sbCreateTempWhereCriteria.length() > 5) {
								iExists++;
								sExists = sExistsBase + iExists;

								sbCreateTempSelect.append(", resourcemetadata ").append(sExists);
								sbCreateTempWhereCriteria.append(" and ");
								if (iExists > 1) {
									sbCreateTempWhereJoin.append(" and ");
								}
								sbCreateTempWhereJoin.append(sExists).append(".resourceJoinId = rm.resourceJoinId");
							}
							sbCreateTempWhereCriteria.append("(").append(sExists).append(".paramName = '")
								.append(entry.getKey()).append("' and ").append(sExists).append(".paramValue is not null and ").append(sExists).append(".systemValue is not null ")
								.append(" and calcDistanceMi(").append(sExists).append(".paramValue, ").append(sExists).append(".systemValue, ")
								.append(entry.getValue()[0]).append(", ").append(entry.getValue()[1]).append(") <= ").append(entry.getValue()[2]).append(")");
						}
						else if (entry.getValue()[3].contains("km")) {
							if (sbCreateTempWhereCriteria.length() > 5) {
								iExists++;
								sExists = sExistsBase + iExists;

								sbCreateTempSelect.append(", resourcemetadata ").append(sExists);
								sbCreateTempWhereCriteria.append(" and ");
								if (iExists > 1) {
									sbCreateTempWhereJoin.append(" and ");
								}
								sbCreateTempWhereJoin.append(sExists).append(".resourceJoinId = rm.resourceJoinId");
							}
							sbCreateTempWhereCriteria.append("(").append(sExists).append(".paramName = '")
								.append(entry.getKey()).append("' and ").append(sExists).append(".paramValue is not null and ").append(sExists).append(".systemValue is not null ")
								.append(" and calcDistanceKm(").append(sExists).append(".paramValue, ").append(sExists).append(".systemValue, ")
								.append(entry.getValue()[0]).append(", ").append(entry.getValue()[1]).append(") <= ").append(entry.getValue()[2]).append(")");
						}
						else {
							// Should not get here
							throw new Exception("Search parameter " + entry.getKey() + " distance units '" + entry.getValue()[3] + "' not supported! Please use 'mi_i', 'mi_us' or 'km'.");
						}
					}
				}

				// Check for and process compartment entries
				if (!compartmentSet.isEmpty()) {
					// Initialize compartment temp select criteria
					if (sbCreateTempWhereCriteria.length() > 5) {
						iExists++;
						sExists = sExistsBase + iExists;

						sbCreateTempSelect.append(", resourcemetadata ").append(sExists);
						sbCreateTempWhereCriteria.append(" and ");
						if (iExists > 1) {
							sbCreateTempWhereJoin.append(" and ");
						}
						sbCreateTempWhereJoin.append(sExists).append(".resourceJoinId = rm.resourceJoinId");
					}
					sbCreateTempWhereCriteria.append("(");

					// Build temp select criteria to or these parameters
					boolean firstEntry = true;
					for (Entry<String, List<String>> entry : compartmentSet) {

						String key = entry.getKey();
						key = key.substring(12);
						String value = entry.getValue().get(0);

						if (!firstEntry) {
							sbCreateTempWhereCriteria.append(" or ");
						}

						sbCreateTempWhereCriteria.append("(").append(sExists).append(".paramName = '").append(key).append("' and ")
							.append(sExists).append(".paramValue like '%").append(value).append("%')");

						firstEntry = false;
					}

					// Close compartment temp select criteria
					sbCreateTempWhereCriteria.append(")");
				}

				// Check for and process authorization patient map parameters
				// Authorization Patient Map Parameters DO NOT COUNT IN OVERALL PARAMETER COUNT
				if (authPatientMap != null && authPatientMap.size() > 0) {
					// Initialize authorization patient map temp select criteria
					if (sbCreateTempWhereCriteria.length() > 5) {
						iExists++;
						sExists = sExistsBase + iExists;

						sbCreateTempSelect.append(", resourcemetadata ").append(sExists);
						sbCreateTempWhereCriteria.append(" and ");
						if (iExists > 1) {
							sbCreateTempWhereJoin.append(" and ");
						}
						sbCreateTempWhereJoin.append(sExists).append(".resourceJoinId = rm.resourceJoinId");
					}
					sbCreateTempWhereCriteria.append("(");

					// Build temp select criteria to or these parameters
					Set<Entry<String, List<String>>> authPatientSet = authPatientMap.entrySet();

					boolean firstEntry = true;
					for (Entry<String, List<String>> entry : authPatientSet) {

						// Need to check for single-quote characters; if found, escape with leading single-quote
						String sqValue = "";
						if (entry.getValue().get(0).contains("'")) {
							sqValue = entry.getValue().get(0).replaceAll("'", "''");
						}
						else {
							sqValue = entry.getValue().get(0);
						}

						// Now check for comma-delimited value
						String[] valueList = sqValue.split("\\,");
						int valueListInd = 0;

						// Generate criteria for each comma-delimited value
						for (String listValue : valueList) {
							log.info("   --> authorization patient map param listValue[" + valueListInd + "] = " + listValue);

							if (!firstEntry) {
								sbCreateTempWhereCriteria.append(" or ");
							}

							// FHIR-158 - Force parameter value comparison to be case-sensitive via MySQL binary qualifier on string value
							sbCreateTempWhereCriteria.append("(").append(sExists).append(".paramName = '").append(entry.getKey()).append("' and ")
								.append(sExists).append(".paramValue like binary '%").append(listValue).append("%')");

							firstEntry = false;
							valueListInd++;
						}
					}

					// Close authorization patient map temp select criteria
					sbCreateTempWhereCriteria.append(")");
				}

			}

			resourcesReturned = new ArrayList<net.aegis.fhir.model.Resource>();

			if (parameterCount > 0 && !isValidSearchParameters) {
				// ERROR - At least one parameter was processed but none were valid; return ERROR invalidParam and empty resources
				if (invalidParams != null) {
					String[] invalidParam = new String[2];
					invalidParam[0] = "ERROR";
					invalidParam[1] = "Search operation processing stopped! No valid search parameters or values found!";
					invalidParams.add(invalidParam);
					log.severe("   --> Search operation processing stopped! No valid search parameters or values found!");
				}
			}
			else {
				boolean needTransaction = false;
				if (Status.STATUS_NO_TRANSACTION == userTransaction.getStatus()) {
					/*
					 *  TRANSACTION BEGIN
					 */
					userTransaction.begin();
					needTransaction = true;
				}

				// Add final criteria for temp table if present
				if (sbCreateTempWhereCriteria.length() > 5) {
					sbCriteria.append(" and r1.id IN (select id from ").append(tempTableName).append(")");

					// FHIR-??? - SQL performance modifications: remove unnecessary outer select * from () AS t1
					//sbCreateTempTable.append(sbCreateTempSelect.toString()).append(") AS t1");
					sbCreateTempTable.append(sbCreateTempSelect.toString()).append(" where ");
					if (sbCreateTempWhereJoin.length() > 5) {
						sbCreateTempWhereJoin.append(" and ");
					}
					sbCreateTempTable.append(sbCreateTempWhereJoin.toString()).append(sbCreateTempWhereCriteria.toString());

					log.info("Create Temp Table: " + sbCreateTempTable.toString());

					// Create temporary table for main Query
					em.createNativeQuery(sbCreateTempTable.toString()).executeUpdate();

					bDropTempTable = true;
				}

				// Check for sort
				int sortCount = 0;

				if (NullChecker.isNotNullish(_sort)) {

					// Build sort column and order by criteria here
					if (_sort.size() > 0) {
						sbCriteria.append(" order by");
					}

					for (String[] sortCriteria : _sort) {

						// Append sort column to query
						sbQuery.append(", (select ");
						// determine sort column type
						if (sortCriteria[2] != null && sortCriteria[2].equalsIgnoreCase("DATE")) {
							sbQuery.append(" ifnull(rmsort").append(sortCount).append(".systemValue, rmsort").append(sortCount).append(".paramValue)");
						}
						else {
							sbQuery.append(" rmsort").append(sortCount).append(".paramValue");
						}
						sbQuery.append(" from resourcemetadata rmsort").append(sortCount)
							.append(" where rmsort").append(sortCount)
							.append(".resourceJoinId = r1.id and rmsort").append(sortCount)
							.append(".paramName = '").append(sortCriteria[0]).append("' limit 1) sort").append(sortCount);

						// Next append sort order by to criteria

						if (sortCount > 0) {
							sbCriteria.append(",");
						}
						sbCriteria.append(" sort").append(sortCount).append(" ").append(sortCriteria[1]);

						// Increment sort count; if max 10 sort criteria reached, break
						sortCount++;
						if (sortCount > 9) {
							break;
						}
					}
				}

				// Fill remaining sort columns to query column list
				if (sortCount < 10) {
					for (int remainingCount=sortCount; remainingCount<10; remainingCount++) {
						sbQuery.append(", sort").append(remainingCount);
					}
				}

				sbQuery.append(sbCriteria.toString());

				log.info("Native Query: " + sbQuery.toString());

				resourceQuery = em.createNativeQuery(sbQuery.toString(), net.aegis.fhir.model.Resource.class);

				// Execute query
				List<net.aegis.fhir.model.Resource> resources = (List<net.aegis.fhir.model.Resource>) resourceQuery.getResultList();

				if (resources.size() > 0 && resources.size() > maxCount.intValue()) {
					// Maximum count allowed for is less than number of resources returned; reduce resources to maxCount limit
					log.info("Total resources returned: " + resources.size() + "; Maximum count allowed for: " + maxCount);

					int totalCount = 0;

					for (net.aegis.fhir.model.Resource resource : resources) {
						resourcesReturned.add(resource);
						totalCount++;
						if (totalCount >= maxCount.intValue()) {
							break;
						}
					}

				} else {
					resourcesReturned = resources;
				}

				if (bDropTempTable) {
					log.info("Drop Temp Table: " + sbDropTempTable.toString());

					em.createNativeQuery(sbDropTempTable.toString()).executeUpdate();
				}

				if (needTransaction == true) {
					/*
					 *  TRANSACTION COMMIT(END)
					 */
					userTransaction.commit();
				}
			}

		} catch (Exception e) {
			// Exception caught
			log.severe(e.getMessage());
			throw e;
		}

		return resourcesReturned;
	}

	/**
	 * Used in search logic for date, datetime, numeric and quantity criteria values
	 *
	 * @param prefix
	 * @return
	 */
	private boolean isMatchingControlPrefix(String prefix) {

		log.fine("[START] ResourceService.isMatchingControlPrefix(" + prefix + ")");

		boolean isValidPrefix = false;
		String[] validPrefixes = {"eq", "ne", "gt", "lt", "ge", "le", "sa", "eb", "ap"};

		if (prefix != null && !prefix.isEmpty()) {

			for (String validPrefix : validPrefixes) {

				if (prefix.equals(validPrefix)) {
					isValidPrefix = true;
					break;
				}
			}
		}

		return isValidPrefix;
	}

	/**
	 * Used in search logic for date, datetime, numeric and quantity criteria values
	 *
	 * @param value
	 * @param isDateType
	 * @param isDatePeriodType
	 * @param isNumericType
	 * @param isQuantityType
	 * @return
	 */
	private String computeLowRangeValue(String value, boolean isDateType, boolean isPeriodType, boolean isNumericType, boolean isQuantityType) {

		log.fine("[START] ResourceService.computeLowRangeValue");

		String lowRangeValue = value;

		if (isDateType || isPeriodType) {
			try {
				/*
				 *  Expected value is a date string of a given precision:
				 *  - Year only "yyyy" - append "0101000000"
				 *  - Year and month only "yyyyMM" - append "01000000"
				 *  - Year, month and day only "yyyyMMdd" - append "000000"
				 *  - Year, month, day, hours and minutes only "yyyyMMddhhmm" - append "00"
				 *  - Year, month, day, hours, minutes, seconds "yyyyMMddhhmmss" - no change
				 */
				int length = value.length();

				if (length == 4) {
					lowRangeValue = value + "0101000000";
				}
				else if (length == 6) {
					lowRangeValue = value + "01000000";
				}
				else if (length == 8) {
					lowRangeValue = value + "000000";
				}
				else if (length == 12) {
					lowRangeValue = value + "00";
				}

			}
			catch (Exception e) {
				// Swallow exception and send back the original value
				log.warning("Exception parsing and evaluating low range value for date or period! " + e.getMessage());
				lowRangeValue = value;
			}
		}
		else if (isNumericType || isQuantityType) {
			try {
				/*
				 *  Expected value is a numeric string
				 *  - Save decimal position and remove decimal point
				 *  - Convert to whole number
				 *  - Subtract 1
				 *  - Convert back to string
				 *  - Append '5'
				 *  - Re-insert the decimal point
				 *
				 *  012345
				 *  100.24
				 */

				// - Save decimal position and remove decimal point
				int decPos = value.indexOf(".");
				String wholeValue = value;
				if (decPos > -1) {
					if (decPos == 0) {
						wholeValue = value.substring(1);
					}
					else if (decPos + 1 < value.length()) {
						wholeValue = value.substring(0, decPos) + value.substring(decPos + 1, value.length());
					}
				}
				else {
					decPos = value.length();
				}

				/*
				 * If leading zero '0' character, then prefix with '1' for accurate conversion to/from long value
				 */
				boolean isLeadingZero = false;
				if (wholeValue.substring(0, 1).equals("0")) {
					isLeadingZero = true;
					wholeValue = "1" + wholeValue;
				}

				// - Convert to whole number
				// - Subtract 1
				long longValue = Long.parseLong(wholeValue) - 1;

				// - Convert back to string
				// - Append '5'
				wholeValue = Long.toString(longValue) + "5";

				/*
				 * If leading zero '0' character, then remove prefix '1' for accurate conversion to/from long value
				 */
				if (isLeadingZero) {
					wholeValue = wholeValue.substring(1);
				}

				// - Re-insert the decimal point
				lowRangeValue = wholeValue.substring(0, decPos) + "." + wholeValue.substring(decPos, wholeValue.length());
			}
			catch (Exception e) {
				// Swallow exception and send back the original value
				log.warning("Exception parsing and evaluating low range value for numeric or quantity! " + e.getMessage());
				lowRangeValue = value;
			}
		}

		return lowRangeValue;
	}

	/**
	 * Used in search logic for date, datetime, numeric and quantity criteria values
	 *
	 * @param value
	 * @param isDateType
	 * @param isPeriodType
	 * @param isNumericType
	 * @param isQuantityType
	 * @return
	 */
	private String computeHighRangeValue(String value, boolean isDateType, boolean isPeriodType, boolean isNumericType, boolean isQuantityType) {

		log.fine("[START] ResourceService.computeHighRangeValue");

		String highRangeValue = value;

		if (isDateType) {
			try {
				/*
				 *  Expected value is a date string of a given precision:
				 *  - Year only "yyyy" - append "1231235959"
				 *  - Year and month only "yyyyMM" - append "NN235959" where "NN" is the correct last day of the month
				 *  - Year, month and day only "yyyyMMdd" - append "235959"
				 *  - Year, month, day, hours and minutes only "yyyyMMddhhmm" - append "59"
				 *  - Year, month, day, hours, minutes, seconds "yyyyMMddhhmmss" - no change
				 */
				int length = value.length();

				if (length == 4) {
					highRangeValue = value + "1231235959";
				}
				else if (length == 6) {
					int monthValue = Integer.valueOf(value.substring(4, 6));
					int yearValue = Integer.valueOf(value.substring(0, 4));
					boolean isLeap = Year.of(yearValue).isLeap();
					String lastDayOfMonth = "31";
					if (monthValue == 2) {
						if (isLeap == true) {
							lastDayOfMonth = "29";
						}
						else {
							lastDayOfMonth = "28";
						}
					}
					else if (monthValue == 4 || monthValue == 6 || monthValue == 9 || monthValue == 11) {
						lastDayOfMonth = "30";
					}
					highRangeValue = value + lastDayOfMonth + "235959";
				}
				else if (length == 8) {
					highRangeValue = value + "235959";
				}
				else if (length == 12) {
					highRangeValue = value + "59";
				}

			}
			catch (Exception e) {
				// Swallow exception and send back the original value
				log.warning("Exception parsing and evaluating high range value for date or period! " + e.getMessage());
				highRangeValue = value;
			}
		}
		else if (isPeriodType) {
			try {
				/*
				 *  Expected value is a date string of a given precision:
				 *  - Year only "yyyy" - append "0101000000"
				 *  - Year and month only "yyyyMM" - append "01000000"
				 *  - Year, month and day only "yyyyMMdd" - append "000000"
				 *  - Year, month, day, hours and minutes only "yyyyMMddhhmm" - append "00"
				 *  - Year, month, day, hours, minutes, seconds "yyyyMMddhhmmss" - no change
				 */
				int length = value.length();

				if (length == 4) {
					highRangeValue = value + "0101000000";
				}
				else if (length == 6) {
					highRangeValue = value + "01000000";
				}
				else if (length == 8) {
					highRangeValue = value + "000000";
				}
				else if (length == 12) {
					highRangeValue = value + "00";
				}

			}
			catch (Exception e) {
				// Swallow exception and send back the original value
				log.warning("Exception parsing and evaluating low range value for date or period! " + e.getMessage());
				highRangeValue = value;
			}
		}
		else if (isNumericType || isQuantityType) {
			try {
				/*
				 *  Expected value is a numeric string
				 *  - Save decimal position and remove decimal point
				 *  - Append '5'
				 *  - Re-insert the decimal point
				 *
				 *  012345
				 *  100.24
				 */

				// - Save decimal position and remove decimal point
				int decPos = value.indexOf(".");
				String wholeValue = value;
				if (decPos > -1) {
					if (decPos == 0) {
						wholeValue = value.substring(1);
					}
					else if (decPos + 1 < value.length()) {
						wholeValue = value.substring(0, decPos) + value.substring(decPos + 1, value.length());
					}
				}
				else {
					decPos = value.length();
				}

				// - Append '5'
				wholeValue = wholeValue + "5";

				// - Re-insert the decimal point
				highRangeValue = wholeValue.substring(0, decPos) + "." + wholeValue.substring(decPos, wholeValue.length());
			}
			catch (Exception e) {
				// Swallow exception and send back the original value
				log.warning("Exception parsing and evaluating low range value! " + e.getMessage());
				highRangeValue = value;
			}
		}

		return highRangeValue;
	}

	/**
	 * Used in paging logic
	 *
	 * @param num
	 * @param divisor
	 * @return
	 */
	private int divideAndRoundUp(int num, int divisor) {
		if (num == 0 || divisor == 0) {
			return 0;
		}

		int sign = (num > 0 ? 1 : -1) * (divisor > 0 ? 1 : -1);

		if (sign > 0) {
			return (num + divisor - 1) / divisor;
		}
		else {
			return (num / divisor);
		}
	}

}
