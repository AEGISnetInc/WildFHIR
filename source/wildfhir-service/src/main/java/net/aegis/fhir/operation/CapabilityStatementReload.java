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
package net.aegis.fhir.operation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.formats.IParser.OutputStyle;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.ContactDetail;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementImplementationComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementKind;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestResourceComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestResourceOperationComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestResourceSearchParamComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.ConditionalDeleteStatus;
import org.hl7.fhir.r4.model.CapabilityStatement.ConditionalReadStatus;
import org.hl7.fhir.r4.model.CapabilityStatement.ReferenceHandlingPolicy;
import org.hl7.fhir.r4.model.CapabilityStatement.ResourceInteractionComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.ResourceVersionPolicy;
import org.hl7.fhir.r4.model.CapabilityStatement.SystemInteractionComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.SystemRestfulInteraction;
import org.hl7.fhir.r4.model.CapabilityStatement.TypeRestfulInteraction;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r4.model.Enumerations.FHIRVersion;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;

import net.aegis.fhir.model.Conformance;
import net.aegis.fhir.model.LabelKeyValueBean;
import net.aegis.fhir.model.ResourceContainer;
import net.aegis.fhir.model.ResourceType;
import net.aegis.fhir.service.BatchService;
import net.aegis.fhir.service.CodeService;
import net.aegis.fhir.service.ConformanceService;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.ResourcemetadataService;
import net.aegis.fhir.service.TransactionService;
import net.aegis.fhir.service.narrative.FHIRNarrativeGeneratorClient;

public class CapabilityStatementReload extends ResourceOperationProxy {

	private Logger log = Logger.getLogger("CapabilityStatementReload");
	private static CapabilityStatementReload capst;

	public static Integer CONFORMANCE_ID = 1;
	public static String CONFORMANCE_RESOURCE_ID = "0";
	public static Integer CONFORMANCE_VERSION_ID = 1;
	public final Charset UTF8_CHARSET = Charset.forName("UTF-8");

	public static CapabilityStatementReload instance() {
		if (capst == null) {
			capst = new CapabilityStatementReload();
		}
		return capst;
	}

	/* (non-Javadoc)
	 * @see net.aegis.fhir.operation.ResourceOperationProxy#executeOperation(javax.ws.rs.core.UriInfo, javax.ws.rs.core.HttpHeaders, net.aegis.fhir.service.ResourceService, net.aegis.fhir.service.ResourcemetadataService, net.aegis.fhir.service.BatchService, net.aegis.fhir.service.TransactionService, net.aegis.fhir.service.CodeService, net.aegis.fhir.service.ConformanceService, java.lang.String, java.lang.String, java.lang.String, org.hl7.fhir.r4.model.Parameters, org.hl7.fhir.r4.model.Resource, java.lang.String, java.lang.String, boolean, java.lang.StringBuffer)
	 */
	@Override
	public Parameters executeOperation(UriInfo context, HttpHeaders headers, ResourceService resourceService, ResourcemetadataService resourcemetadataService, BatchService batchService, TransactionService transactionService, CodeService codeService, ConformanceService conformanceService, String softwareVersion, String resourceType, String resourceId, Parameters inputParameters, org.hl7.fhir.r4.model.Resource inputResource, String inputString, String contentType, boolean isPost, StringBuffer returnedDirective) throws Exception {

        log.fine("CapabilityStatementReload.executeOperation() - [START] ");

		Parameters out = new Parameters();

		try {
			ResourceContainer resourceContainer = conformanceService.fetchFhirBaseCapabilityStatement();
			ParametersParameterComponent parameter = new ParametersParameterComponent();

			if (resourceContainer != null && resourceContainer.getConformance() != null && resourceContainer.getConformance().getResourceContents() != null) {
				byte baseCapStmt[] = resourceContainer.getConformance().getResourceContents();
				String baseCapStatement = new String(baseCapStmt, UTF8_CHARSET);
				log.fine("softwareVersion:" + softwareVersion);
				log.fine("baseCapStatement:" + baseCapStatement);
				String baseUrl = "";
				if (codeService != null)	{
					baseUrl = codeService.getCodeValue("baseUrl");
					log.fine("baseUrl:" + baseUrl);
				}

				boolean capStatementLoaded = reloadCapabilityStatement(conformanceService, softwareVersion, baseUrl, baseCapStmt);

				if (capStatementLoaded) {
					parameter.setName("result");
					StringType resultString = new StringType("Capability statement reload complete.");
					parameter.setValue(resultString);
					out.addParameter(parameter);
				} else {
					parameter.setName("result");
					StringType resultString = new StringType("Capability statement reload failed.");
					parameter.setValue(resultString);
					out.addParameter(parameter);
				}
			}
			else {
				parameter.setName("result");
				StringType resultString = new StringType("Capability statement reload failed. Could not find base Capability Statement.");
				parameter.setValue(resultString);
				out.addParameter(parameter);
			}
		}
		catch (Exception e) {
			// Throw exceptions back
			throw e;
		}

		return out;
	}

	public Boolean reloadCapabilityStatement(ConformanceService conformanceService, String softwareVersion, String baseUrl, byte baseCapStmt[]) {
		boolean capStatementLoaded = false;

		try {
			String conformanceString = buildCapabilityStatement(softwareVersion, baseUrl, baseCapStmt);
			log.fine("conformanceString:" + conformanceString);

			ByteArrayInputStream resourceContents = new ByteArrayInputStream(conformanceString.getBytes());
			XmlParser xmlP = new XmlParser();
			xmlP.setOutputStyle(OutputStyle.PRETTY);
			CapabilityStatement capabilityStatementResource = (CapabilityStatement) xmlP.parse(resourceContents);

			java.sql.Date lastUpdate = new java.sql.Date(capabilityStatementResource.getDate().getTime());
			Conformance conformance = new Conformance();
			conformance.setId(CONFORMANCE_ID);
			conformance.setResourceId(CONFORMANCE_RESOURCE_ID);
			conformance.setVersionId(CONFORMANCE_VERSION_ID);
			conformance.setResourceType("CapabilityStatement");
			conformance.setStatus("generated");
			conformance.setLastUser("system");
			conformance.setLastUpdate(lastUpdate);
			conformance.setResourceContents(conformanceString.getBytes());

			ResourceContainer resourceContainer = conformanceService.read();
			if (resourceContainer != null && resourceContainer.getConformance() != null && resourceContainer.getConformance().getResourceId().equals(CONFORMANCE_RESOURCE_ID)) {
				conformance.setId(resourceContainer.getConformance().getId());
				log.fine("reloadCapabilityStatement: Updating");
				conformanceService.update(conformance);
				capStatementLoaded = true;
			} else {
				log.fine("reloadCapabilityStatement: Creating");
				conformanceService.create(conformance);
				capStatementLoaded = true;
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error loading definitions for FHIR Validation Engine!", e);
		}


		return capStatementLoaded;

	}

	private String buildCapabilityStatement(String softwareVersion, String baseUrl, byte baseCapStmt[]) throws Exception {

		log.fine("[START] GenerateCapabilityStatement.buildCapabilityStatement");

		String conformanceString = "";

		try {
			// Convert XML contents to Resource object and set id and meta
			ByteArrayInputStream iResource = new ByteArrayInputStream(baseCapStmt);
			XmlParser xmlP = new XmlParser();
			xmlP.setOutputStyle(OutputStyle.PRETTY);
			CapabilityStatement capabilityStatementResource = (CapabilityStatement) xmlP.parse(iResource); // we expect a CapabilityStatement Resource

			// Modify the base CapabilityStatement properties for our server
			capabilityStatementResource.setId("0");
			capabilityStatementResource.setText(null);
			capabilityStatementResource.setUrl(baseUrl + "/metadata");
			capabilityStatementResource.setVersion(softwareVersion);
			capabilityStatementResource.setName("AEGISWildFHIR401");
			capabilityStatementResource.setTitle("AEGIS WildFHIR Test Server FHIR R4");
			capabilityStatementResource.setStatus(PublicationStatus.ACTIVE);
			capabilityStatementResource.setPublisher("AEGIS.net, Inc.");
			capabilityStatementResource.getContact().clear();
			ContactDetail contact = new ContactDetail();
			ContactPoint telcom = new ContactPoint();
			telcom.setSystem(ContactPointSystem.URL);
			telcom.setValue(baseUrl);
			contact.addTelecom(telcom);
			capabilityStatementResource.addContact(contact);
			capabilityStatementResource.setDescription("AEGIS WildFHIR Test Server supporting the HL7 FHIR R4 (v4.0.1-Official) specification.");
			capabilityStatementResource.setKind(CapabilityStatementKind.INSTANCE);
			DateTimeType publishDateTime = new DateTimeType();
			publishDateTime.setTimeZoneZulu(true);
			publishDateTime.setValue(new Date());
			capabilityStatementResource.setDateElement(publishDateTime);
			if (capabilityStatementResource.hasSoftware()) {
				capabilityStatementResource.getSoftware().setName("WildFHIR");
				// Set software version from build properties
				capabilityStatementResource.getSoftware().setVersion(softwareVersion);
				capabilityStatementResource.getSoftware().setReleaseDate(publishDateTime.getValue());
			}
			CapabilityStatementImplementationComponent implementation = new CapabilityStatementImplementationComponent();
			implementation.setDescription(capabilityStatementResource.getDescription());
			capabilityStatementResource.setImplementation(implementation);
			capabilityStatementResource.setFhirVersion(FHIRVersion._4_0_1);
			// Format already defined in base
			capabilityStatementResource.addPatchFormat("application/xml-patch+xml");
			capabilityStatementResource.addPatchFormat("application/json-patch+json");

			// IG Support manually entered into base CapabilityStatement

			log.fine("CapabilityStatement header attributes set...");

			if (capabilityStatementResource.hasRest()) {
				// Should only have one of these
				CapabilityStatementRestComponent rest = capabilityStatementResource.getRest().get(0);

				rest.setDocumentation("This server supports all instance, type and whole system operations on all resources including batch, transaction and validate. Paging is supported for both the history and search operations. Support for the patch operation is available for both json and xml. Additional custom operations $medication-overview and $purge are supported.");

				// CapabilityStatement Security manually entered in base capability statement

				// CapabilityStatement Resources
				if (rest.hasResource()) {

					for (CapabilityStatementRestResourceComponent restResource : rest.getResource()) {
						log.fine("Working on ResourceType " + restResource.getType());

						// Set resource type interactions
						if (restResource.hasInteraction()) {
							restResource.getInteraction().clear();
						}
						ResourceInteractionComponent resourceInteractionComponent = new ResourceInteractionComponent();
						resourceInteractionComponent.setCode(TypeRestfulInteraction.READ);
						resourceInteractionComponent.setDocumentation("Implemented per the specification");
						restResource.addInteraction(resourceInteractionComponent);
						resourceInteractionComponent = new ResourceInteractionComponent();
						resourceInteractionComponent.setCode(TypeRestfulInteraction.VREAD);
						resourceInteractionComponent.setDocumentation("Implemented per the specification");
						restResource.addInteraction(resourceInteractionComponent);
						resourceInteractionComponent = new ResourceInteractionComponent();
						resourceInteractionComponent.setCode(TypeRestfulInteraction.UPDATE);
						resourceInteractionComponent.setDocumentation("Implemented per the specification");
						restResource.addInteraction(resourceInteractionComponent);
						resourceInteractionComponent = new ResourceInteractionComponent();
						resourceInteractionComponent.setCode(TypeRestfulInteraction.DELETE);
						resourceInteractionComponent.setDocumentation("Implemented per the specification");
						restResource.addInteraction(resourceInteractionComponent);
						resourceInteractionComponent = new ResourceInteractionComponent();
						resourceInteractionComponent.setCode(TypeRestfulInteraction.HISTORYINSTANCE);
						resourceInteractionComponent.setDocumentation("Implemented per the specification");
						restResource.addInteraction(resourceInteractionComponent);
						resourceInteractionComponent = new ResourceInteractionComponent();
						resourceInteractionComponent.setCode(TypeRestfulInteraction.HISTORYTYPE);
						resourceInteractionComponent.setDocumentation("Implemented per the specification");
						restResource.addInteraction(resourceInteractionComponent);
						resourceInteractionComponent = new ResourceInteractionComponent();
						resourceInteractionComponent.setCode(TypeRestfulInteraction.CREATE);
						resourceInteractionComponent.setDocumentation("Implemented per the specification");
						restResource.addInteraction(resourceInteractionComponent);
						resourceInteractionComponent = new ResourceInteractionComponent();
						resourceInteractionComponent.setCode(TypeRestfulInteraction.SEARCHTYPE);
						resourceInteractionComponent.setDocumentation("Implemented per the specification");
						restResource.addInteraction(resourceInteractionComponent);
						resourceInteractionComponent = new ResourceInteractionComponent();
						resourceInteractionComponent.setCode(TypeRestfulInteraction.PATCH);
						resourceInteractionComponent.setDocumentation("Implemented per the specification");
						restResource.addInteraction(resourceInteractionComponent);

						// Set Versioning, Read History and Update Create
						restResource.setVersioning(ResourceVersionPolicy.VERSIONEDUPDATE);
						restResource.setReadHistory(true);
						restResource.setUpdateCreate(true);

						// Set Condition Create and Update to supported; Conditional Read to full-support; Conditional Delete to single mode
						restResource.setConditionalCreate(true);
						restResource.setConditionalRead(ConditionalReadStatus.FULLSUPPORT);
						restResource.setConditionalUpdate(true);
						restResource.setConditionalDelete(ConditionalDeleteStatus.MULTIPLE);

						// Set reference policies: literal, logical and local
						restResource.getReferencePolicy().clear();
						restResource.addReferencePolicy(ReferenceHandlingPolicy.LITERAL);
						restResource.addReferencePolicy(ReferenceHandlingPolicy.LOCAL);
						restResource.addReferencePolicy(ReferenceHandlingPolicy.LOGICAL);

						// TSS-118 - Correct searchInclude and searchRevInclude settings
						if (restResource.hasSearchInclude()) {
							String searchIncludeValue = null;
							for (StringType searchInclude : restResource.getSearchInclude()) {
								if (searchInclude.hasValue()) {
									searchIncludeValue = searchInclude.getValue().replace('.', ':');
									searchInclude.setValue(searchIncludeValue);
								}

							}
						}
						if (restResource.hasSearchRevInclude()) {
							String searchRevIncludeValue = null;
							for (StringType searchRevInclude : restResource.getSearchRevInclude()) {
								if (searchRevInclude.hasValue()) {
									searchRevIncludeValue = searchRevInclude.getValue().replace('.', ':');
									searchRevInclude.setValue(searchRevIncludeValue);
								}

							}
						}

						// Create new searchParam list based on the intersection between the conformance-base and the ResourceType listings
						List<CapabilityStatementRestResourceSearchParamComponent> newSearchParams = new ArrayList<CapabilityStatementRestResourceSearchParamComponent>();

						// Generate all supported FHIR resource search parameters
						List<LabelKeyValueBean> resourceCriteriaList = ResourceType.getResourceTypeResourceCriteria(restResource.getType());

						CapabilityStatementRestResourceSearchParamComponent newSearchParam = null;
						String criteriaType = null;

						for (LabelKeyValueBean criteria : resourceCriteriaList) {

							log.fine("Working on Criteria " + criteria.getKey() + " - ");

							newSearchParam = new CapabilityStatementRestResourceSearchParamComponent();
							if (criteria.getPath() != null && !criteria.getPath().isEmpty()) {
								newSearchParam.setDefinition(criteria.getPath());
							}
							newSearchParam.setDocumentation(criteria.getLabel());
							newSearchParam.setName(criteria.getKey());
							criteriaType = criteria.getType();
							if (criteriaType.contains("PERIOD")) {
								criteriaType = "DATE";
							}
							newSearchParam.setType(SearchParamType.fromCode(criteriaType.toLowerCase()));

							newSearchParams.add(newSearchParam);

							log.fine("Added");
						}

						restResource.getSearchParam().clear();
						restResource.getSearchParam().addAll(newSearchParams);

						// Generate all supported FHIR resource operations
						List<LabelKeyValueBean> resourceOperationList = ResourceType.getSupportedResourceOperations(restResource.getType());

						List<CapabilityStatementRestResourceOperationComponent> resourceOperations = new ArrayList<CapabilityStatementRestResourceOperationComponent>();
						CapabilityStatementRestResourceOperationComponent resourceOperation = null;

						for (LabelKeyValueBean rop : resourceOperationList) {
							if (rop.getLabel() != null && rop.getValue() != null) {
								resourceOperation = new CapabilityStatementRestResourceOperationComponent();
								resourceOperation.setName(rop.getLabel());
								resourceOperation.setDefinition(rop.getValue());
								resourceOperations.add(resourceOperation);
							}
						}

						restResource.getOperation().clear();
						if (!resourceOperations.isEmpty()) {
							restResource.getOperation().addAll(resourceOperations);
						}
					}
				}

				log.fine("Working on rest interaction(s)...");

				// Clear existing rest interactions
				if (rest.hasInteraction()) {
					rest.getInteraction().clear();
				}

				SystemInteractionComponent interaction = new SystemInteractionComponent();
				interaction.setCode(SystemRestfulInteraction.TRANSACTION);
				rest.getInteraction().add(interaction);

				interaction = new SystemInteractionComponent();
				interaction.setCode(SystemRestfulInteraction.BATCH);
				rest.getInteraction().add(interaction);

				interaction = new SystemInteractionComponent();
				interaction.setCode(SystemRestfulInteraction.HISTORYSYSTEM);
				rest.getInteraction().add(interaction);

				interaction = new SystemInteractionComponent();
				interaction.setCode(SystemRestfulInteraction.SEARCHSYSTEM);
				rest.getInteraction().add(interaction);

				log.fine("Working on rest search param(s)...");

				// Clear existing rest search params
				if (rest.hasSearchParam()) {
					rest.getSearchParam().clear();
				}

				// Create new rest searchParam list
				List<CapabilityStatementRestResourceSearchParamComponent> newRestSearchParams = new ArrayList<CapabilityStatementRestResourceSearchParamComponent>();

				// Get all global supported FHIR resource search parameters
				List<LabelKeyValueBean> globalCriteriaList = ResourceType.getGlobalCriteria();

				CapabilityStatementRestResourceSearchParamComponent restSearchParam = null;

				for (LabelKeyValueBean criteria : globalCriteriaList) {

					log.fine("Working on Global Criteria " + criteria.getKey() + " - ");

					restSearchParam = new CapabilityStatementRestResourceSearchParamComponent();
					if (criteria.getPath() != null && !criteria.getPath().isEmpty()) {
						restSearchParam.setDefinition(criteria.getPath());
					}
					restSearchParam.setDocumentation(criteria.getLabel());
					restSearchParam.setName(criteria.getKey());
					restSearchParam.setType(SearchParamType.fromCode(criteria.getType().toLowerCase()));

					newRestSearchParams.add(restSearchParam);

					log.fine("Added");
				}

				rest.getSearchParam().clear();
				rest.getSearchParam().addAll(newRestSearchParams);

				log.fine("Working on rest operation(s)...");

				// Initialize support operations list
				List<CapabilityStatementRestResourceOperationComponent> supportedOperations = new ArrayList<CapabilityStatementRestResourceOperationComponent>();

				List<LabelKeyValueBean> globalOperations = ResourceType.getGlobalOperations();
				CapabilityStatementRestResourceOperationComponent operation = null;

				for (LabelKeyValueBean globalOperation : globalOperations) {

					if (!globalOperation.getKey().equalsIgnoreCase("external")) {
						operation = new CapabilityStatementRestResourceOperationComponent();
						operation.setName(globalOperation.getLabel());
						operation.setDefinition(globalOperation.getValue());
						supportedOperations.add(operation);
					}
				}

				// Add supported global operations
				rest.getOperation().clear();
				rest.getOperation().addAll(supportedOperations);
			}

			// Use RI NarrativeGenerator
			FHIRNarrativeGeneratorClient.instance().generate(capabilityStatementResource);

			log.fine("=== CapabilityStatement Resource Complete ===");

			// Parse CapabilityStatement Resource to an XML string
			ByteArrayOutputStream oResource = new ByteArrayOutputStream();
			xmlP.compose(oResource, capabilityStatementResource, true);
			conformanceString = oResource.toString();

		} catch (Exception e) {
			log.severe("Exception building CapabilityStatement Resource output!" + e.getMessage());
			throw e;
		}

		log.fine("[END] GenerateCapabilityStatement.buildCapabilityStatement");

		return conformanceString;
	}

}
