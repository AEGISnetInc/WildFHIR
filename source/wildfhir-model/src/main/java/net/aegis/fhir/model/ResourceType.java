/*
 * #%L
 * WildFHIR - wildfhir-model
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
package net.aegis.fhir.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author richard.ettema
 *
 */
public class ResourceType {

	private static List<String> operationResourceTypes;
	private static List<LabelKeyValueBean> globalOperations;
	private static Map<String, List<LabelKeyValueBean>> resourceOperations;
	private static List<String> resourceTypes;
	private static List<String> resourceTypesOrdered;
	private static List<LabelKeyValueBean> globalResourceCriteria;
//	private static List<LabelKeyValueBean> baseResourceCriteria;
	private static List<LabelKeyValueBean> allGlobalResourceCriteria;
	private static Map<String, List<LabelKeyValueBean>> resourceTypeCriteria;
	private static List<String> supportedResourceTypes;
	private static List<LabelKeyValueBean> compartmentResourceTypeCriteria;
	private static List<String> compartments;
	private static List<String> supportedCompartments;
	private static List<String> everythingResources;
	private static List<LabelKeyValueBean> everythingResourceTypeDateCriteria;

	public static List<String> getOperationResourceTypes() {
		return operationResourceTypes;
	}

	public static List<LabelKeyValueBean> getGlobalOperations() {
		return globalOperations;
	}

	public static Map<String, List<LabelKeyValueBean>> getResourceOperations() {
		return resourceOperations;
	}

	public static List<String> getResourceTypes() {
		return resourceTypes;
	}

	// TSP-2416 - Return resource types list in reverse order
	public static List<String> getResourceTypesOrdered() {
		return resourceTypesOrdered;
	}

	public static List<String> getSupportedResourceTypes() {
		return supportedResourceTypes;
	}

	public static List<LabelKeyValueBean> getGlobalCriteria() {
		return globalResourceCriteria;
	}

	public static Map<String, List<LabelKeyValueBean>> getResourceTypeCriteria() {
		return resourceTypeCriteria;
	}

	public static List<String> getEverythingResourceTypes() {
		return everythingResources;
	}

	static {
		initializeStatic1();
		initializeStatic2();
	}

	public static void initializeStatic1() {
		/*
		 * All FHIR resource types
		 */
		resourceTypes = new ArrayList<String>();

		resourceTypes.add("Account");
		resourceTypes.add("ActivityDefinition");
		resourceTypes.add("AdverseEvent");
		resourceTypes.add("AllergyIntolerance");
		resourceTypes.add("Appointment");
		resourceTypes.add("AppointmentResponse");
		resourceTypes.add("AuditEvent");
		resourceTypes.add("Basic");
		resourceTypes.add("Binary");
		resourceTypes.add("BiologicallyDerivedProduct");
		resourceTypes.add("BodyStructure");
		resourceTypes.add("Bundle");
		resourceTypes.add("CapabilityStatement");
		resourceTypes.add("CarePlan");
		resourceTypes.add("CareTeam");
		resourceTypes.add("CatalogEntry");
		resourceTypes.add("ChargeItem");
		resourceTypes.add("ChargeItemDefinition");
		resourceTypes.add("Claim");
		resourceTypes.add("ClaimResponse");
		resourceTypes.add("ClinicalImpression");
		resourceTypes.add("CodeSystem");
		resourceTypes.add("Communication");
		resourceTypes.add("CommunicationRequest");
		resourceTypes.add("CompartmentDefinition");
		resourceTypes.add("Composition");
		resourceTypes.add("ConceptMap");
		resourceTypes.add("Condition");
		resourceTypes.add("Consent");
		resourceTypes.add("Contract");
		resourceTypes.add("Coverage");
		resourceTypes.add("CoverageEligibilityRequest");
		resourceTypes.add("CoverageEligibilityResponse");
		resourceTypes.add("DetectedIssue");
		resourceTypes.add("Device");
		resourceTypes.add("DeviceDefinition");
		resourceTypes.add("DeviceMetric");
		resourceTypes.add("DeviceRequest");
		resourceTypes.add("DeviceUseStatement");
		resourceTypes.add("DiagnosticReport");
		resourceTypes.add("DocumentManifest");
		resourceTypes.add("DocumentReference");
		resourceTypes.add("EffectEvidenceSynthesis");
		resourceTypes.add("Encounter");
		resourceTypes.add("Endpoint");
		resourceTypes.add("EnrollmentRequest");
		resourceTypes.add("EnrollmentResponse");
		resourceTypes.add("EpisodeOfCare");
		resourceTypes.add("EventDefinition");
		resourceTypes.add("Evidence");
		resourceTypes.add("EvidenceVariable");
		resourceTypes.add("ExampleScenario");
		resourceTypes.add("ExplanationOfBenefit");
		resourceTypes.add("FamilyMemberHistory");
		resourceTypes.add("Flag");
		resourceTypes.add("Goal");
		resourceTypes.add("GraphDefinition");
		resourceTypes.add("Group");
		resourceTypes.add("GuidanceResponse");
		resourceTypes.add("HealthcareService");
		resourceTypes.add("ImagingStudy");
		resourceTypes.add("Immunization");
		resourceTypes.add("ImmunizationEvaluation");
		resourceTypes.add("ImmunizationRecommendation");
		resourceTypes.add("ImplementationGuide");
		resourceTypes.add("InsurancePlan");
		resourceTypes.add("Invoice");
		resourceTypes.add("Library");
		resourceTypes.add("Linkage");
		resourceTypes.add("List");
		resourceTypes.add("Location");
		resourceTypes.add("Measure");
		resourceTypes.add("MeasureReport");
		resourceTypes.add("Media");
		resourceTypes.add("Medication");
		resourceTypes.add("MedicationAdministration");
		resourceTypes.add("MedicationDispense");
		resourceTypes.add("MedicationKnowledge");
		resourceTypes.add("MedicationRequest");
		resourceTypes.add("MedicationStatement");
		resourceTypes.add("MedicinalProduct");
		resourceTypes.add("MedicinalProductAuthorization");
		resourceTypes.add("MedicinalProductContraindication");
		resourceTypes.add("MedicinalProductIndication");
		resourceTypes.add("MedicinalProductIngredient");
		resourceTypes.add("MedicinalProductInteraction");
		resourceTypes.add("MedicinalProductManufactured");
		resourceTypes.add("MedicinalProductPackaged");
		resourceTypes.add("MedicinalProductPharmaceutical");
		resourceTypes.add("MedicinalProductUndesirableEffect");
		resourceTypes.add("MessageDefinition");
		resourceTypes.add("MessageHeader");
		resourceTypes.add("MolecularSequence");
		resourceTypes.add("NamingSystem");
		resourceTypes.add("NutritionOrder");
		resourceTypes.add("Observation");
		resourceTypes.add("ObservationDefinition");
		resourceTypes.add("OperationDefinition");
		resourceTypes.add("OperationOutcome");
		resourceTypes.add("Organization");
		resourceTypes.add("OrganizationAffiliation");
		resourceTypes.add("Patient");
		resourceTypes.add("PaymentNotice");
		resourceTypes.add("PaymentReconciliation");
		resourceTypes.add("Person");
		resourceTypes.add("PlanDefinition");
		resourceTypes.add("Practitioner");
		resourceTypes.add("PractitionerRole");
		resourceTypes.add("Procedure");
		resourceTypes.add("Provenance");
		resourceTypes.add("Questionnaire");
		resourceTypes.add("QuestionnaireResponse");
		resourceTypes.add("RelatedPerson");
		resourceTypes.add("RequestGroup");
		resourceTypes.add("ResearchDefinition");
		resourceTypes.add("ResearchElementDefinition");
		resourceTypes.add("ResearchStudy");
		resourceTypes.add("ResearchSubject");
		resourceTypes.add("RiskAssessment");
		resourceTypes.add("RiskEvidenceSynthesis");
		resourceTypes.add("Schedule");
		resourceTypes.add("SearchParameter");
		resourceTypes.add("ServiceRequest");
		resourceTypes.add("Slot");
		resourceTypes.add("Specimen");
		resourceTypes.add("SpecimenDefinition");
		resourceTypes.add("StructureDefinition");
		resourceTypes.add("StructureMap");
		resourceTypes.add("Subscription");
		resourceTypes.add("Substance");
		resourceTypes.add("SubstanceNucleicAcid");
		resourceTypes.add("SubstancePolymer");
		resourceTypes.add("SubstanceProtein");
		resourceTypes.add("SubstanceReferenceInformation");
		resourceTypes.add("SubstanceSourceMaterial");
		resourceTypes.add("SubstanceSpecification");
		resourceTypes.add("SupplyDelivery");
		resourceTypes.add("SupplyRequest");
		resourceTypes.add("Task");
		resourceTypes.add("TerminologyCapabilities");
		resourceTypes.add("TestReport");
		resourceTypes.add("TestScript");
		resourceTypes.add("ValueSet");
		resourceTypes.add("VerificationResult");
		resourceTypes.add("VisionPrescription");

		/*
		 * TSP-2416 - All FHIR resource types Ordered (reverse for reference string resource type evaluation)
		 */
		resourceTypesOrdered = new ArrayList<String>();

		resourceTypesOrdered.add("VisionPrescription");
		resourceTypesOrdered.add("VerificationResult");
		resourceTypesOrdered.add("ValueSet");
		resourceTypesOrdered.add("TestScript");
		resourceTypesOrdered.add("TestReport");
		resourceTypesOrdered.add("TerminologyCapabilities");
		resourceTypesOrdered.add("Task");
		resourceTypesOrdered.add("SupplyRequest");
		resourceTypesOrdered.add("SupplyDelivery");
		resourceTypesOrdered.add("SubstanceSpecification");
		resourceTypesOrdered.add("SubstanceSourceMaterial");
		resourceTypesOrdered.add("SubstanceReferenceInformation");
		resourceTypesOrdered.add("SubstanceProtein");
		resourceTypesOrdered.add("SubstancePolymer");
		resourceTypesOrdered.add("SubstanceNucleicAcid");
		resourceTypesOrdered.add("Substance");
		resourceTypesOrdered.add("Subscription");
		resourceTypesOrdered.add("StructureMap");
		resourceTypesOrdered.add("StructureDefinition");
		resourceTypesOrdered.add("SpecimenDefinition");
		resourceTypesOrdered.add("Specimen");
		resourceTypesOrdered.add("Slot");
		resourceTypesOrdered.add("ServiceRequest");
		resourceTypesOrdered.add("SearchParameter");
		resourceTypesOrdered.add("Schedule");
		resourceTypesOrdered.add("RiskEvidenceSynthesis");
		resourceTypesOrdered.add("RiskAssessment");
		resourceTypesOrdered.add("ResearchSubject");
		resourceTypesOrdered.add("ResearchStudy");
		resourceTypesOrdered.add("ResearchElementDefinition");
		resourceTypesOrdered.add("ResearchDefinition");
		resourceTypesOrdered.add("RequestGroup");
		resourceTypesOrdered.add("RelatedPerson");
		resourceTypesOrdered.add("QuestionnaireResponse");
		resourceTypesOrdered.add("Questionnaire");
		resourceTypesOrdered.add("Provenance");
		resourceTypesOrdered.add("Procedure");
		resourceTypesOrdered.add("PractitionerRole");
		resourceTypesOrdered.add("Practitioner");
		resourceTypesOrdered.add("PlanDefinition");
		resourceTypesOrdered.add("Person");
		resourceTypesOrdered.add("PaymentReconciliation");
		resourceTypesOrdered.add("PaymentNotice");
		resourceTypesOrdered.add("Patient");
		resourceTypesOrdered.add("OrganizationAffiliation");
		resourceTypesOrdered.add("Organization");
		resourceTypesOrdered.add("OperationOutcome");
		resourceTypesOrdered.add("OperationDefinition");
		resourceTypesOrdered.add("ObservationDefinition");
		resourceTypesOrdered.add("Observation");
		resourceTypesOrdered.add("NutritionOrder");
		resourceTypesOrdered.add("NamingSystem");
		resourceTypesOrdered.add("MolecularSequence");
		resourceTypesOrdered.add("MessageHeader");
		resourceTypesOrdered.add("MessageDefinition");
		resourceTypesOrdered.add("MedicinalProductUndesirableEffect");
		resourceTypesOrdered.add("MedicinalProductPharmaceutical");
		resourceTypesOrdered.add("MedicinalProductPackaged");
		resourceTypesOrdered.add("MedicinalProductManufactured");
		resourceTypesOrdered.add("MedicinalProductInteraction");
		resourceTypesOrdered.add("MedicinalProductIngredient");
		resourceTypesOrdered.add("MedicinalProductIndication");
		resourceTypesOrdered.add("MedicinalProductContraindication");
		resourceTypesOrdered.add("MedicinalProductAuthorization");
		resourceTypesOrdered.add("MedicinalProduct");
		resourceTypesOrdered.add("MedicationStatement");
		resourceTypesOrdered.add("MedicationRequest");
		resourceTypesOrdered.add("MedicationKnowledge");
		resourceTypesOrdered.add("MedicationDispense");
		resourceTypesOrdered.add("MedicationAdministration");
		resourceTypesOrdered.add("Medication");
		resourceTypesOrdered.add("Media");
		resourceTypesOrdered.add("MeasureReport");
		resourceTypesOrdered.add("Measure");
		resourceTypesOrdered.add("Location");
		resourceTypesOrdered.add("List");
		resourceTypesOrdered.add("Linkage");
		resourceTypesOrdered.add("Library");
		resourceTypesOrdered.add("Invoice");
		resourceTypesOrdered.add("InsurancePlan");
		resourceTypesOrdered.add("ImplementationGuide");
		resourceTypesOrdered.add("ImmunizationRecommendation");
		resourceTypesOrdered.add("ImmunizationEvaluation");
		resourceTypesOrdered.add("Immunization");
		resourceTypesOrdered.add("ImagingStudy");
		resourceTypesOrdered.add("HealthcareService");
		resourceTypesOrdered.add("GuidanceResponse");
		resourceTypesOrdered.add("Group");
		resourceTypesOrdered.add("GraphDefinition");
		resourceTypesOrdered.add("Goal");
		resourceTypesOrdered.add("Flag");
		resourceTypesOrdered.add("FamilyMemberHistory");
		resourceTypesOrdered.add("ExplanationOfBenefit");
		resourceTypesOrdered.add("ExampleScenario");
		resourceTypesOrdered.add("EvidenceVariable");
		resourceTypesOrdered.add("Evidence");
		resourceTypesOrdered.add("EventDefinition");
		resourceTypesOrdered.add("EpisodeOfCare");
		resourceTypesOrdered.add("EnrollmentResponse");
		resourceTypesOrdered.add("EnrollmentRequest");
		resourceTypesOrdered.add("Endpoint");
		resourceTypesOrdered.add("Encounter");
		resourceTypesOrdered.add("EffectEvidenceSynthesis");
		resourceTypesOrdered.add("DocumentReference");
		resourceTypesOrdered.add("DocumentManifest");
		resourceTypesOrdered.add("DiagnosticReport");
		resourceTypesOrdered.add("DeviceUseStatement");
		resourceTypesOrdered.add("DeviceRequest");
		resourceTypesOrdered.add("DeviceMetric");
		resourceTypesOrdered.add("DeviceDefinition");
		resourceTypesOrdered.add("Device");
		resourceTypesOrdered.add("DetectedIssue");
		resourceTypesOrdered.add("CoverageEligibilityResponse");
		resourceTypesOrdered.add("CoverageEligibilityRequest");
		resourceTypesOrdered.add("Coverage");
		resourceTypesOrdered.add("Contract");
		resourceTypesOrdered.add("Consent");
		resourceTypesOrdered.add("Condition");
		resourceTypesOrdered.add("ConceptMap");
		resourceTypesOrdered.add("Composition");
		resourceTypesOrdered.add("CompartmentDefinition");
		resourceTypesOrdered.add("CommunicationRequest");
		resourceTypesOrdered.add("Communication");
		resourceTypesOrdered.add("CodeSystem");
		resourceTypesOrdered.add("ClinicalImpression");
		resourceTypesOrdered.add("ClaimResponse");
		resourceTypesOrdered.add("Claim");
		resourceTypesOrdered.add("ChargeItemDefinition");
		resourceTypesOrdered.add("ChargeItem");
		resourceTypesOrdered.add("CatalogEntry");
		resourceTypesOrdered.add("CareTeam");
		resourceTypesOrdered.add("CarePlan");
		resourceTypesOrdered.add("CapabilityStatement");
		resourceTypesOrdered.add("Bundle");
		resourceTypesOrdered.add("BodyStructure");
		resourceTypesOrdered.add("BiologicallyDerivedProduct");
		resourceTypesOrdered.add("Binary");
		resourceTypesOrdered.add("Basic");
		resourceTypesOrdered.add("AuditEvent");
		resourceTypesOrdered.add("AppointmentResponse");
		resourceTypesOrdered.add("Appointment");
		resourceTypesOrdered.add("AllergyIntolerance");
		resourceTypesOrdered.add("AdverseEvent");
		resourceTypesOrdered.add("ActivityDefinition");
		resourceTypesOrdered.add("Account");

		/*
		 * All FHIR supported resource types
		 */
		supportedResourceTypes = new ArrayList<String>();

		supportedResourceTypes.add("Account");
		supportedResourceTypes.add("ActivityDefinition");
		supportedResourceTypes.add("AdverseEvent");
		supportedResourceTypes.add("AllergyIntolerance");
		supportedResourceTypes.add("Appointment");
		supportedResourceTypes.add("AppointmentResponse");
		supportedResourceTypes.add("AuditEvent");
		supportedResourceTypes.add("Basic");
		supportedResourceTypes.add("Binary");
		supportedResourceTypes.add("BiologicallyDerivedProduct");
		supportedResourceTypes.add("BodyStructure");
		supportedResourceTypes.add("Bundle");
		supportedResourceTypes.add("CapabilityStatement");
		supportedResourceTypes.add("CarePlan");
		supportedResourceTypes.add("CareTeam");
		supportedResourceTypes.add("CatalogEntry");
		supportedResourceTypes.add("ChargeItem");
		supportedResourceTypes.add("ChargeItemDefinition");
		supportedResourceTypes.add("Claim");
		supportedResourceTypes.add("ClaimResponse");
		supportedResourceTypes.add("ClinicalImpression");
		supportedResourceTypes.add("CodeSystem");
		supportedResourceTypes.add("Communication");
		supportedResourceTypes.add("CommunicationRequest");
		supportedResourceTypes.add("CompartmentDefinition");
		supportedResourceTypes.add("Composition");
		supportedResourceTypes.add("ConceptMap");
		supportedResourceTypes.add("Condition");
		supportedResourceTypes.add("Consent");
		supportedResourceTypes.add("Contract");
		supportedResourceTypes.add("Coverage");
		supportedResourceTypes.add("CoverageEligibilityRequest");
		supportedResourceTypes.add("CoverageEligibilityResponse");
		supportedResourceTypes.add("DetectedIssue");
		supportedResourceTypes.add("Device");
		supportedResourceTypes.add("DeviceDefinition");
		supportedResourceTypes.add("DeviceMetric");
		supportedResourceTypes.add("DeviceRequest");
		supportedResourceTypes.add("DeviceUseStatement");
		supportedResourceTypes.add("DiagnosticReport");
		supportedResourceTypes.add("DocumentManifest");
		supportedResourceTypes.add("DocumentReference");
		supportedResourceTypes.add("EffectEvidenceSynthesis");
		supportedResourceTypes.add("Encounter");
		supportedResourceTypes.add("Endpoint");
		supportedResourceTypes.add("EnrollmentRequest");
		supportedResourceTypes.add("EnrollmentResponse");
		supportedResourceTypes.add("EpisodeOfCare");
		supportedResourceTypes.add("EventDefinition");
		supportedResourceTypes.add("Evidence");
		supportedResourceTypes.add("EvidenceVariable");
		supportedResourceTypes.add("ExampleScenario");
		supportedResourceTypes.add("ExplanationOfBenefit");
		supportedResourceTypes.add("FamilyMemberHistory");
		supportedResourceTypes.add("Flag");
		supportedResourceTypes.add("Goal");
		supportedResourceTypes.add("GraphDefinition");
		supportedResourceTypes.add("Group");
		supportedResourceTypes.add("GuidanceResponse");
		supportedResourceTypes.add("HealthcareService");
		supportedResourceTypes.add("ImagingStudy");
		supportedResourceTypes.add("Immunization");
		supportedResourceTypes.add("ImmunizationEvaluation");
		supportedResourceTypes.add("ImmunizationRecommendation");
		supportedResourceTypes.add("ImplementationGuide");
		supportedResourceTypes.add("InsurancePlan");
		supportedResourceTypes.add("Invoice");
		supportedResourceTypes.add("Library");
		supportedResourceTypes.add("Linkage");
		supportedResourceTypes.add("List");
		supportedResourceTypes.add("Location");
		supportedResourceTypes.add("Measure");
		supportedResourceTypes.add("MeasureReport");
		supportedResourceTypes.add("Media");
		supportedResourceTypes.add("Medication");
		supportedResourceTypes.add("MedicationAdministration");
		supportedResourceTypes.add("MedicationDispense");
		supportedResourceTypes.add("MedicationKnowledge");
		supportedResourceTypes.add("MedicationRequest");
		supportedResourceTypes.add("MedicationStatement");
		supportedResourceTypes.add("MedicinalProduct");
		supportedResourceTypes.add("MedicinalProductAuthorization");
		supportedResourceTypes.add("MedicinalProductContraindication");
		supportedResourceTypes.add("MedicinalProductIndication");
		supportedResourceTypes.add("MedicinalProductIngredient");
		supportedResourceTypes.add("MedicinalProductInteraction");
		supportedResourceTypes.add("MedicinalProductManufactured");
		supportedResourceTypes.add("MedicinalProductPackaged");
		supportedResourceTypes.add("MedicinalProductPharmaceutical");
		supportedResourceTypes.add("MedicinalProductUndesirableEffect");
		supportedResourceTypes.add("MessageDefinition");
		supportedResourceTypes.add("MessageHeader");
		supportedResourceTypes.add("MolecularSequence");
		supportedResourceTypes.add("NamingSystem");
		supportedResourceTypes.add("NutritionOrder");
		supportedResourceTypes.add("Observation");
		supportedResourceTypes.add("ObservationDefinition");
		supportedResourceTypes.add("OperationDefinition");
		supportedResourceTypes.add("OperationOutcome");
		supportedResourceTypes.add("Organization");
		supportedResourceTypes.add("OrganizationAffiliation");
		supportedResourceTypes.add("Patient");
		supportedResourceTypes.add("PaymentNotice");
		supportedResourceTypes.add("PaymentReconciliation");
		supportedResourceTypes.add("Person");
		supportedResourceTypes.add("PlanDefinition");
		supportedResourceTypes.add("Practitioner");
		supportedResourceTypes.add("PractitionerRole");
		supportedResourceTypes.add("Procedure");
		supportedResourceTypes.add("Provenance");
		supportedResourceTypes.add("Questionnaire");
		supportedResourceTypes.add("QuestionnaireResponse");
		supportedResourceTypes.add("RelatedPerson");
		supportedResourceTypes.add("RequestGroup");
		supportedResourceTypes.add("ResearchDefinition");
		supportedResourceTypes.add("ResearchElementDefinition");
		supportedResourceTypes.add("ResearchStudy");
		supportedResourceTypes.add("ResearchSubject");
		supportedResourceTypes.add("RiskAssessment");
		supportedResourceTypes.add("RiskEvidenceSynthesis");
		supportedResourceTypes.add("Schedule");
		supportedResourceTypes.add("SearchParameter");
		supportedResourceTypes.add("ServiceRequest");
		supportedResourceTypes.add("Slot");
		supportedResourceTypes.add("Specimen");
		supportedResourceTypes.add("SpecimenDefinition");
		supportedResourceTypes.add("StructureDefinition");
		supportedResourceTypes.add("StructureMap");
		supportedResourceTypes.add("Subscription");
		supportedResourceTypes.add("Substance");
		supportedResourceTypes.add("SubstanceNucleicAcid");
		supportedResourceTypes.add("SubstancePolymer");
		supportedResourceTypes.add("SubstanceProtein");
		supportedResourceTypes.add("SubstanceReferenceInformation");
		supportedResourceTypes.add("SubstanceSourceMaterial");
		supportedResourceTypes.add("SubstanceSpecification");
		supportedResourceTypes.add("SupplyDelivery");
		supportedResourceTypes.add("SupplyRequest");
		supportedResourceTypes.add("Task");
		supportedResourceTypes.add("TerminologyCapabilities");
		supportedResourceTypes.add("TestReport");
		supportedResourceTypes.add("TestScript");
		supportedResourceTypes.add("ValueSet");
		supportedResourceTypes.add("VerificationResult");
		supportedResourceTypes.add("VisionPrescription");

		/*
		 * All FHIR compartments
		 */
		compartments = new ArrayList<String>();

		compartments.add("Device");
		compartments.add("Encounter");
		compartments.add("Patient");
		compartments.add("Practitioner");
		compartments.add("RelatedPerson");

		/*
		 * All FHIR supported compartments
		 */
		supportedCompartments = new ArrayList<String>();

//		supportedCompartments.add("Device");
//		supportedCompartments.add("Encounter");
		supportedCompartments.add("Patient");
//		supportedCompartments.add("Practitioner");
//		supportedCompartments.add("RelatedPerson");

		/*
		 * All FHIR compartment resource type search criteria
		 */
		compartmentResourceTypeCriteria = new ArrayList<LabelKeyValueBean>();
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "Account", "subject"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "AdverseEvent", "subject"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "AllergyIntolerance", "patient"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "AllergyIntolerance", "recorder"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "AllergyIntolerance", "asserter"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "Appointment", "actor"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "AppointmentResponse", "actor"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "AuditEvent", "patient"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "Basic", "patient"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "Basic", "author"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "BodyStructure", "patient"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "CarePlan", "patient"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "CarePlan", "performer"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "CareTeam", "patient"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "CareTeam", "participant"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "ChargeItem", "subject"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "Claim", "patient"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "Claim", "payee"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "ClaimResponse", "patient"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "ClinicalImpression", "subject"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "Communication", "subject"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "Communication", "sender"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "Communication", "recipient"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "CommunicationRequest", "subject"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "CommunicationRequest", "sender"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "CommunicationRequest", "recipient"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "CommunicationRequest", "requester"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "Composition", "subject"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "Composition", "author"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "Composition", "attester"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "Condition", "patient"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "Condition", "asserter"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "Consent", "patient"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "Coverage", "policy-holder"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "Coverage", "subscriber"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "Coverage", "beneficiary"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "Coverage", "payor"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "CoverageEligibilityRequest", "patient"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "CoverageEligibilityResponse", "patient"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "DetectedIssue", "patient"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "DeviceRequest", "subject"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "DeviceRequest", "performer"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "DeviceUseStatement", "subject"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "DiagnosticReport", "subject"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "DocumentManifest", "patient"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "DocumentManifest", "agent"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "DocumentManifest", "recipient"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "DocumentReference", "subject"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "DocumentReference", "author"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "Encounter", "patient"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "EnrollmentRequest", "subject"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "EpisodeOfCare", "patient"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "ExplanationOfBenefit", "patient"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "ExplanationOfBenefit", "payee"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "FamilyMemberHistory", "patient"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "Flag", "patient"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "Goal", "patient"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "Group", "member"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "ImagingStudy", "patient"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "Immunization", "patient"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "ImmunizationEvaluation", "patient"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "ImmunizationRecommendation", "patient"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "Invoice", "patient"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "Invoice", "subject"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "Invoice", "recipient"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "List", "subject"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "List", "source"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "MeasureReport", "patient"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "Media", "subject"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "MedicationAdministration", "patient"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "MedicationAdministration", "performer"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "MedicationAdministration", "subject"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "MedicationDispense", "patient"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "MedicationDispense", "subject"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "MedicationDispense", "receiver"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "MedicationRequest", "subject"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "MedicationStatement", "subject"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "MolecularSequence", "patient"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "NutritionOrder", "patient"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "Observation", "patient"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "Observation", "performer"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "Patient", "link"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "Person", "patient"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "Procedure", "patient"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "Procedure", "performer"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "Provenance", "patient"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "QuestionnaireResponse", "subject"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "QuestionnaireResponse", "author"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "RelatedPerson", "patient"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "RequestGroup", "subject"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "RequestGroup", "participant"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "ResearchSubject", "individual"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "RiskAssessment", "subject"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "Schedule", "actor"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "ServiceRequest", "subject"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "ServiceRequest", "performer"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "Specimen", "subject"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "SupplyDelivery", "patient"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "SupplyRequest", "requester"));
		compartmentResourceTypeCriteria.add(new LabelKeyValueBean("Patient", "VisionPrescription", "patient"));

		/*
		 * All FHIR supported resource types that implement the everything operation
		 */
		everythingResources = new ArrayList<String>();

//		everythingResources.add("Encounter");
		everythingResources.add("Patient");

		/*
		 * All FHIR resource type search criteria to support $everything operation
		 */
		everythingResourceTypeDateCriteria = new ArrayList<LabelKeyValueBean>();
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "Account", "period"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "AdverseEvent", "date"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "AllergyIntolerance", "date"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "Appointment", "date"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "AppointmentResponse", "appointment.date"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "AuditEvent", "patient"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "Basic", "created"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "BodyStructure", ""));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "CarePlan", "date"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "CareTeam", "date"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "ChargeItem", "entered-date"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "Claim", "created"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "ClaimResponse", "created"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "ClinicalImpression", "date"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "Communication", "received"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "CommunicationRequest", "authored"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "Composition", "date"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "Condition", "recorded-date"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "Consent", "date"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "Contract", "issued")); // AEGIS
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "Coverage", "period")); // AEGIS
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "CoverageEligibilityRequest", "created"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "DetectedIssue", "date"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "DeviceRequest", "event-date"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "DeviceUseStatement", "recorded-on"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "DiagnosticReport", "date"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "DocumentManifest", "created"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "DocumentReference", "created"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "Encounter", "date"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "EnrollmentRequest", "created"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "EpisodeOfCare", "date"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "ExplanationOfBenefit", "created"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "FamilyMemberHistory", "date"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "Flag", "date"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "Goal", "start-date"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "Group", "member-period"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "ImagingStudy", "started"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "Immunization", "date"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "ImmunizationEvaluation", "date"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "ImmunizationRecommendation", "date"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "Invoice", "date"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "List", "date"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "MeasureReport", "date"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "Media", "created"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "MedicationAdministration", "effective-time"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "MedicationDispense", "whenprepared"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "MedicationRequest", "authoredon"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "MedicationStatement", "effective"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "NutritionOrder", "datetime"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "Observation", "date"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "Person", ""));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "Procedure", "date"));
		//everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "Provenance", "recorded")); // Excluded to reduce content size
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "QuestionnaireResponse", "authored"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "RelatedPerson", ""));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "RequestGroup", "authored"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "ResearchSubject", "period"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "RiskAssessment", "date"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "Schedule", "date"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "ServiceRequest", "authored"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "Specimen", "collected"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "SupplyDelivery", "occurrence"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "SupplyRequest", "date"));
		everythingResourceTypeDateCriteria.add(new LabelKeyValueBean("date", "VisionPrescription", "datewritten"));

		/*
		 * All global operations
		 */
		globalOperations = new ArrayList<LabelKeyValueBean>();
		globalOperations.add(new LabelKeyValueBean("capability-reload", "external", "", "write")); // WildFHIR global operation; does not match existing FHIR global operation
		globalOperations.add(new LabelKeyValueBean("code-configuration", "external", "", "write")); // WildFHIR global operation; does not match existing FHIR global operation
		globalOperations.add(new LabelKeyValueBean("convert", "global", "http://hl7.org/fhir/OperationDefinition/Resource-convert", "read"));
		globalOperations.add(new LabelKeyValueBean("load-examples", "external", "", "write")); // WildFHIR global operation; does not match existing FHIR global operation
		globalOperations.add(new LabelKeyValueBean("meta", "mixed", "http://hl7.org/fhir/OperationDefinition/Resource-meta", "read"));
		globalOperations.add(new LabelKeyValueBean("meta-add", "mixed", "http://hl7.org/fhir/OperationDefinition/Resource-meta-add", "write"));
		globalOperations.add(new LabelKeyValueBean("meta-delete", "mixed", "http://hl7.org/fhir/OperationDefinition/Resource-meta-delete", "write"));
		globalOperations.add(new LabelKeyValueBean("purge-all", "external", "", "write")); // WildFHIR global operation; does not match existing FHIR global operation
		globalOperations.add(new LabelKeyValueBean("validate", "mixed", "http://hl7.org/fhir/OperationDefinition/Resource-validate", "read"));
		globalOperations.add(new LabelKeyValueBean("versions", "global", "http://hl7.org/fhir/OperationDefinition/Resource-versions", "read"));

		/*
		 * All resource types with operations
		 */
		operationResourceTypes = new ArrayList<String>();

		resourceOperations = new HashMap<String,List<LabelKeyValueBean>>();

		// All supported resource types have at a minimum the operations listed in baseOperationList
		operationResourceTypes.addAll(supportedResourceTypes);

		List<LabelKeyValueBean> baseOperationList = new ArrayList<LabelKeyValueBean>();

		List<LabelKeyValueBean> operationList = null;

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("Account", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("ActivityDefinition", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("AdverseEvent", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("AllergyIntolerance", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("Appointment", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("AppointmentResponse", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("AuditEvent", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("Basic", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("Binary", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("BiologicallyDerivedProduct", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("BodyStructure", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("Bundle", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("CapabilityStatement", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("CarePlan", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("CareTeam", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("CatalogEntry", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("ChargeItem", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("ChargeItemDefinition", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("Claim", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("ClaimResponse", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("ClinicalImpression", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("CodeSystem", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("Communication", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("CommunicationRequest", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("CompartmentDefinition", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		operationList.add(new LabelKeyValueBean("document", "composition", "http://hl7.org/fhir/OperationDefinition/Composition-document", "read"));
		resourceOperations.put("Composition", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("ConceptMap", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("Condition", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("Consent", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("Contract", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("Coverage", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("CoverageEligibilityRequest", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("CoverageEligibilityResponse", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("DetectedIssue", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("Device", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("DeviceDefinition", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("DeviceMetric", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("DeviceRequest", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("DeviceUseStatement", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("DiagnosticReport", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("DocumentManifest", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("DocumentReference", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("EffectEvidenceSynthesis", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("Encounter", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("Endpoint", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("EnrollmentRequest", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("EnrollmentResponse", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("EpisodeOfCare", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("EventDefinition", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("Evidence", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("EvidenceVariable", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("ExampleScenario", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("ExplanationOfBenefit", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("FamilyMemberHistory", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("Flag", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("Goal", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("GraphDefinition", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("Group", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("GuidanceResponse", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("HealthcareService", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("ImagingStudy", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("Immunization", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("ImmunizationEvaluation", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("ImmunizationRecommendation", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("ImplementationGuide", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("InsurancePlan", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("Invoice", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("Library", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("Linkage", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("List", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("Location", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("Measure", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("MeasureReport", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("Media", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("Medication", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("MedicationAdministration", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("MedicationDispense", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("MedicationKnowledge", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("MedicationRequest", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("MedicationStatement", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("MedicinalProduct", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("MedicinalProductAuthorization", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("MedicinalProductContraindication", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("MedicinalProductIndication", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("MedicinalProductIngredient", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("MedicinalProductInteraction", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("MedicinalProductManufactured", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("MedicinalProductPackaged", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("MedicinalProductPharmaceutical", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("MedicinalProductUndesirableEffect", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("MessageDefinition", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("MessageHeader", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("MolecularSequence", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("NamingSystem", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("NutritionOrder", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		operationList.add(new LabelKeyValueBean("lastn", "observation", "http://hl7.org/fhir/OperationDefinition/Observation-lastn", "read"));
		resourceOperations.put("Observation", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("ObservationDefinition", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("OperationDefinition", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("OperationOutcome", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("Organization", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("OrganizationAffiliation", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("Parameters", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		operationList.add(new LabelKeyValueBean("everything", "patient", "http://hl7.org/fhir/OperationDefinition/Patient-everything", "read"));
		operationList.add(new LabelKeyValueBean("match", "patient", "http://hl7.org/fhir/OperationDefinition/Patient-match", "read"));
		operationList.add(new LabelKeyValueBean("purge", "patient", "http://wildfhir4.aegis.net/fhir/wildfhir/OperationDefinition/wildfhir-operation-patient-purge", "write"));
		resourceOperations.put("Patient", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("PaymentNotice", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("PaymentReconciliation", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("Person", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("PlanDefinition", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("Practitioner", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("PractitionerRole", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("Procedure", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("Provenance", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("Questionnaire", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("QuestionnaireResponse", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("RelatedPerson", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("RequestGroup", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("ResearchDefinition", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("ResearchElementDefinition", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("ResearchStudy", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("ResearchSubject", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("RiskAssessment", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("RiskEvidenceSynthesis", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("Schedule", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("SearchParameter", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("ServiceRequest", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("Slot", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("Specimen", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("SpecimenDefinition", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("StructureDefinition", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("StructureMap", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("Subscription", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("Substance", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("SubstanceNucleicAcid", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("SubstancePolymer", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("SubstanceProtein", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("SubstanceReferenceInformation", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("SubstanceSourceMaterial", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("SubstanceSpecification", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("SupplyDelivery", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("SupplyRequest", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("Task", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("TerminologyCapabilities", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("TestReport", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("TestScript", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("ValueSet", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("VerificationResult", operationList);

		operationList = new ArrayList<LabelKeyValueBean>();
		operationList.addAll(baseOperationList);
		resourceOperations.put("VisionPrescription", operationList);
	}

	public static void initializeStatic2() {

		/*
		 * Global criteria
		 */
		globalResourceCriteria = new ArrayList<LabelKeyValueBean>();
		globalResourceCriteria.add(new LabelKeyValueBean("Logical id of this artifact", "_id", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Resource-id"));
		// _language is not in the FHIR specification
		//globalResourceCriteria.add(new LabelKeyValueBean("Language of the resource content", "_language", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Resource-language"));
		globalResourceCriteria.add(new LabelKeyValueBean("When the resource version last changed", "_lastUpdated", "", "DATE", "http://hl7.org/fhir/SearchParameter/Resource-lastUpdated"));
		globalResourceCriteria.add(new LabelKeyValueBean("Profiles this resource claims to conform to", "_profile", "", "URI", "http://hl7.org/fhir/SearchParameter/Resource-profile"));
		globalResourceCriteria.add(new LabelKeyValueBean("Security Labels applied to this resource", "_security", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Resource-security"));
		globalResourceCriteria.add(new LabelKeyValueBean("Identifies where the resource comes from", "_source", "", "URI", "http://hl7.org/fhir/SearchParameter/Resource-source"));
		globalResourceCriteria.add(new LabelKeyValueBean("Tags applied", "_tag", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Resource-tag"));
		globalResourceCriteria.add(new LabelKeyValueBean("Type of resource (when doing cross-resource search", "_type", "", "TOKEN"));
		// Move all '_' parameters to global
		globalResourceCriteria.add(new LabelKeyValueBean("The criteria sort order", "_sort", "", "STRING"));
		globalResourceCriteria.add(new LabelKeyValueBean("The number of resources returned per page", "_count", "", "NUMBER"));
		globalResourceCriteria.add(new LabelKeyValueBean("Request that the engine return additional resources", "_include", "", "STRING"));
		globalResourceCriteria.add(new LabelKeyValueBean("Request that the engine return additional resources", "_revinclude", "", "STRING"));
		globalResourceCriteria.add(new LabelKeyValueBean("Specify the returned format of the response payload", "_format", "", "STRING"));
		globalResourceCriteria.add(new LabelKeyValueBean("Return only a portion of the resources", "_summary", "", "STRING"));
		// Add Common Search Parameters based on https://hl7.org/fhir/R4/searchparameter-registry.html
		globalResourceCriteria.add(new LabelKeyValueBean("A server defined search that may match any of the string fields in the Address, including line, city, district, state, country, postalCode, and/or text", "address", "", "STRING", "http://hl7.org/fhir/SearchParameter/individual-address"));
		globalResourceCriteria.add(new LabelKeyValueBean("A city specified in an address", "address-city", "", "STRING", "http://hl7.org/fhir/SearchParameter/individual-address-city"));
		globalResourceCriteria.add(new LabelKeyValueBean("A country specified in an address", "address-country", "", "STRING", "http://hl7.org/fhir/SearchParameter/individual-address-country"));
		globalResourceCriteria.add(new LabelKeyValueBean("A postalCode specified in an address", "address-postalcode", "", "STRING", "http://hl7.org/fhir/SearchParameter/individual-address-postalcode"));
		globalResourceCriteria.add(new LabelKeyValueBean("A state specified in an address", "address-state", "", "STRING", "http://hl7.org/fhir/SearchParameter/individual-address-state"));
		globalResourceCriteria.add(new LabelKeyValueBean("A use code specified in an address", "address-use", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/individual-address-use"));
		globalResourceCriteria.add(new LabelKeyValueBean("The patient's date of birth", "birthdate", "", "DATE", "http://hl7.org/fhir/SearchParameter/individual-birthdate"));
		globalResourceCriteria.add(new LabelKeyValueBean("A search by a clinical code", "code", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-code"));
		globalResourceCriteria.add(new LabelKeyValueBean("A conformance resource assigned use context", "context", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-context"));
		globalResourceCriteria.add(new LabelKeyValueBean("A conformance resource assigned quantity- or range-valued", "context-quantity", "", "QUANTITY", "http://hl7.org/fhir/SearchParameter/conformance-context-quantity"));
		globalResourceCriteria.add(new LabelKeyValueBean("A conformance resource assigned type of use context", "context-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-context-type"));
		globalResourceCriteria.add(new LabelKeyValueBean("A conformance resource assigned use context type and quantity- or range-based value", "context-type-quantity", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/conformance-context-type-quantity"));
		globalResourceCriteria.add(new LabelKeyValueBean("A conformance resource assigned use context type and value", "context-type-value", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/conformance-context-type-value"));
		globalResourceCriteria.add(new LabelKeyValueBean("A conformance resource publication date", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/conformance-date"));
		globalResourceCriteria.add(new LabelKeyValueBean("A conformance resource description", "description", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-description"));
		globalResourceCriteria.add(new LabelKeyValueBean("A value in an email contact", "email", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/individual-email"));
		globalResourceCriteria.add(new LabelKeyValueBean("Context of the clinical resource", "encounter", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-encounter", "Encounter"));
		globalResourceCriteria.add(new LabelKeyValueBean("A portion of the family name of the patient", "family", "", "STRING", "http://hl7.org/fhir/SearchParameter/individual-family"));
		globalResourceCriteria.add(new LabelKeyValueBean("Gender of the individual", "gender", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/individual-gender"));
		globalResourceCriteria.add(new LabelKeyValueBean("A portion of the given name of the individual", "given", "", "STRING", "http://hl7.org/fhir/SearchParameter/individual-given"));
		globalResourceCriteria.add(new LabelKeyValueBean("A business identifier", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-identifier"));
		globalResourceCriteria.add(new LabelKeyValueBean("Intended jurisdiction for the conformance resource", "jurisdiction", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-jurisdiction"));
		globalResourceCriteria.add(new LabelKeyValueBean("Returns uses of this medicine resource", "medication", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/medications-medication", "Medication"));
		globalResourceCriteria.add(new LabelKeyValueBean("Computationally friendly name of the conformance resource", "name", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-name"));
		globalResourceCriteria.add(new LabelKeyValueBean("The identity of a subject for the clinical resource", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-patient", "Patient"));
		globalResourceCriteria.add(new LabelKeyValueBean("A value in a phone contact", "phone", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/individual-phone"));
		globalResourceCriteria.add(new LabelKeyValueBean("A portion of name using some kind of phonetic matching algorithm", "phonetic", "", "STRING", "http://hl7.org/fhir/SearchParameter/individual-phonetic"));
		globalResourceCriteria.add(new LabelKeyValueBean("Name of the publisher of the conformance resource", "publisher", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-publisher"));
		globalResourceCriteria.add(new LabelKeyValueBean("The current status of the conformance resource", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-status"));
		globalResourceCriteria.add(new LabelKeyValueBean("The value in any kind of telecom details", "telecom", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/individual-telecom"));
		globalResourceCriteria.add(new LabelKeyValueBean("The human-friendly name of the conformance resource", "title", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-title"));
		globalResourceCriteria.add(new LabelKeyValueBean("The kind of clinical resource", "type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-type"));
		globalResourceCriteria.add(new LabelKeyValueBean("The uri that identifies the conformance resource", "url", "", "URI", "http://hl7.org/fhir/SearchParameter/conformance-url"));
		globalResourceCriteria.add(new LabelKeyValueBean("The business version of the conformance resource", "version", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-version"));

		/*
		 * All resource type criteria
		 */
		resourceTypeCriteria = new HashMap<String,List<LabelKeyValueBean>>();

		List<LabelKeyValueBean> resourceCriteria;

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Account number", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Account-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Human-readable label", "name", "", "STRING", "http://hl7.org/fhir/SearchParameter/Account-name"));
		resourceCriteria.add(new LabelKeyValueBean("Entity managing the Account", "owner", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Account-owner", "Organization"));
		resourceCriteria.add(new LabelKeyValueBean("The entity that caused the expenses", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Account-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("Transaction window", "period", "", "PERIOD", "http://hl7.org/fhir/SearchParameter/Account-period"));
		resourceCriteria.add(new LabelKeyValueBean("active | inactive | entered-in-error | on-hold | unknown", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Account-status"));
		resourceCriteria.add(new LabelKeyValueBean("The entity that caused the expenses", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Account-subject"));
		resourceCriteria.add(new LabelKeyValueBean("E.g. patient, expense, depreciation", "type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Account-type"));
		resourceTypeCriteria.put("Account", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("What resource is being referenced", "composed-of", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ActivityDefinition-composed-of"));
		resourceCriteria.add(new LabelKeyValueBean("A use context assigned to the activity definition", "context", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ActivityDefinition-context"));
		resourceCriteria.add(new LabelKeyValueBean("A quantity- or range-valued use context assigned to the activity definition", "context-quantity", "", "QUANTITY", "http://hl7.org/fhir/SearchParameter/ActivityDefinition-context-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A type of use context assigned to the activity definition", "context-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ActivityDefinition-context-type"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and quantity- or range-based value assigned to the activity definition", "context-type-quantity", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/ActivityDefinition-context-type-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and value assigned to the activity definition", "context-type-value", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/ActivityDefinition-context-type-value"));
		resourceCriteria.add(new LabelKeyValueBean("The activity definition publication date", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/ActivityDefinition-date"));
		resourceCriteria.add(new LabelKeyValueBean("What resource is being referenced", "depends-on", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ActivityDefinition-depends-on"));
		resourceCriteria.add(new LabelKeyValueBean("What resource is being referenced", "derived-from", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ActivityDefinition-derived-from"));
		resourceCriteria.add(new LabelKeyValueBean("The description of the activity definition", "description", "", "STRING", "http://hl7.org/fhir/SearchParameter/ActivityDefinition-description"));
		resourceCriteria.add(new LabelKeyValueBean("The time during which the activity definition is intended to be in use", "effective", "", "PERIOD", "http://hl7.org/fhir/SearchParameter/ActivityDefinition-effective"));
		resourceCriteria.add(new LabelKeyValueBean("External identifier for the activity definition", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ActivityDefinition-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Intended jurisdiction for the activity definition", "jurisdiction", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ActivityDefinition-jurisdiction"));
		resourceCriteria.add(new LabelKeyValueBean("Computationally friendly name of the activity definition", "name", "", "STRING", "http://hl7.org/fhir/SearchParameter/ActivityDefinition-name"));
		resourceCriteria.add(new LabelKeyValueBean("What resource is being referenced", "predecessor", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ActivityDefinition-predecessor"));
		resourceCriteria.add(new LabelKeyValueBean("Name of the publisher of the activity definition", "publisher", "", "STRING", "http://hl7.org/fhir/SearchParameter/ActivityDefinition-publisher"));
		resourceCriteria.add(new LabelKeyValueBean("The current status of the activity definition", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ActivityDefinition-status"));
		resourceCriteria.add(new LabelKeyValueBean("What resource is being referenced", "successor", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ActivityDefinition-successor"));
		resourceCriteria.add(new LabelKeyValueBean("The human-friendly name of the activity definition", "title", "", "STRING", "http://hl7.org/fhir/SearchParameter/ActivityDefinition-title"));
		resourceCriteria.add(new LabelKeyValueBean("Topics associated with the module", "topic", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ActivityDefinition-topic"));
		resourceCriteria.add(new LabelKeyValueBean("The uri that identifies the activity definition", "url", "", "URI", "http://hl7.org/fhir/SearchParameter/ActivityDefinition-url"));
		resourceCriteria.add(new LabelKeyValueBean("The business version of the activity definition", "version", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ActivityDefinition-version"));
		resourceTypeCriteria.put("ActivityDefinition", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("actual | potential", "actuality", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/AdverseEvent-actuality"));
		resourceCriteria.add(new LabelKeyValueBean("product-problem | product-quality | product-use-error | wrong-dose | incorrect-prescribing-information | wrong-technique | wrong-route-of-administration | wrong-rate | wrong-duration | wrong-time | expired-drug | medical-device-use-error | problem-different-manufacturer | unsafe-physical-environment", "category", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/AdverseEvent-category"));
		resourceCriteria.add(new LabelKeyValueBean("When the event occurred", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/AdverseEvent-date"));
		resourceCriteria.add(new LabelKeyValueBean("Type of the event itself in relation to the subject", "event", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/AdverseEvent-event"));
		resourceCriteria.add(new LabelKeyValueBean("Location where adverse event occurred", "location", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/AdverseEvent-location", "Location"));
		resourceCriteria.add(new LabelKeyValueBean("Who recorded the adverse event", "recorder", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/AdverseEvent-recorder"));
		resourceCriteria.add(new LabelKeyValueBean("Effect on the subject due to this event", "resultingcondition", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/AdverseEvent-resultingcondition", "Condition"));
		resourceCriteria.add(new LabelKeyValueBean("Seriousness of the event", "seriousness", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/AdverseEvent-seriousness"));
		resourceCriteria.add(new LabelKeyValueBean("mild | moderate | severe", "severity", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/AdverseEvent-severity"));
		resourceCriteria.add(new LabelKeyValueBean("AdverseEvent.study", "study", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/AdverseEvent-study", "ResearchStudy"));
		resourceCriteria.add(new LabelKeyValueBean("Subject impacted by event", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/AdverseEvent-subject"));
		resourceCriteria.add(new LabelKeyValueBean("Refers to the specific entity that caused the adverse event", "substance", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/AdverseEvent-substance"));
		// AEGIS Defined Search Parameters
		resourceCriteria.add(new LabelKeyValueBean("Business identifier for the event", "identifier", "", "TOKEN"));
		resourceTypeCriteria.put("AdverseEvent", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Source of the information about the allergy", "asserter", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/AllergyIntolerance-asserter"));
		resourceCriteria.add(new LabelKeyValueBean("food | medication | environment | biologic", "category", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/AllergyIntolerance-category"));
		resourceCriteria.add(new LabelKeyValueBean("active | inactive | resolved", "clinical-status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/AllergyIntolerance-clinical-status"));
		resourceCriteria.add(new LabelKeyValueBean("Code that identifies the allergy or intolerance", "code", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-code"));
		resourceCriteria.add(new LabelKeyValueBean("low | high | unable-to-assess", "criticality", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/AllergyIntolerance-criticality"));
		resourceCriteria.add(new LabelKeyValueBean("Date first version of the resource instance was recorded", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/clinical-date"));
		resourceCriteria.add(new LabelKeyValueBean("External ids for this item", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Date(/time) of last known occurrence of a reaction", "last-date", "", "DATE", "http://hl7.org/fhir/SearchParameter/AllergyIntolerance-last-date"));
		resourceCriteria.add(new LabelKeyValueBean("Clinical symptoms/signs associated with the Event", "manifestation", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/AllergyIntolerance-manifestation"));
		resourceCriteria.add(new LabelKeyValueBean("Date(/time) when manifestations showed", "onset", "", "DATE", "http://hl7.org/fhir/SearchParameter/AllergyIntolerance-onset"));
		resourceCriteria.add(new LabelKeyValueBean("Who the sensitivity is for", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("Who recorded the sensitivity", "recorder", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/AllergyIntolerance-recorder"));
		resourceCriteria.add(new LabelKeyValueBean("How the subject was exposed to the substance", "route", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/AllergyIntolerance-route"));
		resourceCriteria.add(new LabelKeyValueBean("mild | moderate | severe (of event as a whole)", "severity", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/AllergyIntolerance-severity"));
		resourceCriteria.add(new LabelKeyValueBean("allergy | intolerance - Underlying mechanism (if known)", "type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-type"));
		resourceCriteria.add(new LabelKeyValueBean("unconfirmed | confirmed | refuted | entered-in-error", "verification-status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/AllergyIntolerance-verification-status"));
		resourceTypeCriteria.put("AllergyIntolerance", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Any one of the individuals participating in the appointment", "actor", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Appointment-actor"));
		resourceCriteria.add(new LabelKeyValueBean("The style of appointment or patient that has been booked in the slot (not service type)", "appointment-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Appointment-appointment-type"));
		resourceCriteria.add(new LabelKeyValueBean("The service request this appointment is allocated to assess", "based-on", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Appointment-based-on", "ServiceRequest"));
		resourceCriteria.add(new LabelKeyValueBean("Appointment date/time.", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/Appointment-date"));
		resourceCriteria.add(new LabelKeyValueBean("An Identifier of the Appointment", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Appointment-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("This location is listed in the participants of the appointment", "location", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Appointment-location", "Location"));
		resourceCriteria.add(new LabelKeyValueBean("The Participation status of the subject, or other participant on the appointment. Can be used to locate participants that have not responded to meeting requests.", "part-status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Appointment-part-status"));
		resourceCriteria.add(new LabelKeyValueBean("One of the individuals of the appointment is this patient", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Appointment-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("One of the individuals of the appointment is this practitioner", "practitioner", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Appointment-practitioner", "Practitioner"));
		resourceCriteria.add(new LabelKeyValueBean("Coded reason this appointment is scheduled", "reason-code", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Appointment-reason-code"));
		resourceCriteria.add(new LabelKeyValueBean("Reason the appointment is to take place (resource)", "reason-reference", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Appointment-reason-reference"));
		resourceCriteria.add(new LabelKeyValueBean("A broad categorization of the service that is to be performed during this appointment", "service-category", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Appointment-service-category"));
		resourceCriteria.add(new LabelKeyValueBean("The specific service that is to be performed during this appointment", "service-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Appointment-service-type"));
		resourceCriteria.add(new LabelKeyValueBean("The slots that this appointment is filling", "slot", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Appointment-slot", "Slot"));
		resourceCriteria.add(new LabelKeyValueBean("The specialty of a practitioner that would be required to perform the service requested in this appointment", "specialty", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Appointment-specialty"));
		resourceCriteria.add(new LabelKeyValueBean("The overall status of the appointment", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Appointment-status"));
		resourceCriteria.add(new LabelKeyValueBean("Additional information to support the appointment", "supporting-info", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Appointment-supporting-info"));
		resourceTypeCriteria.put("Appointment", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("The Person, Location/HealthcareService or Device that this appointment response replies for", "actor", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/AppointmentResponse-actor"));
		resourceCriteria.add(new LabelKeyValueBean("The appointment that the response is attached to", "appointment", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/AppointmentResponse-appointment", "Appointment"));
		resourceCriteria.add(new LabelKeyValueBean("An Identifier in this appointment response", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/AppointmentResponse-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("This Response is for this Location", "location", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/AppointmentResponse-location", "Location"));
		resourceCriteria.add(new LabelKeyValueBean("The participants acceptance status for this appointment", "part-status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/AppointmentResponse-part-status"));
		resourceCriteria.add(new LabelKeyValueBean("This Response is for this Patient", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/AppointmentResponse-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("This Response is for this Practitioner", "practitioner", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/AppointmentResponse-practitioner", "Practitioner"));
		resourceTypeCriteria.put("AppointmentResponse", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Type of action performed during the event", "action", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/AuditEvent-action"));
		resourceCriteria.add(new LabelKeyValueBean("Identifier for the network access point of the user device", "address", "", "STRING", "http://hl7.org/fhir/SearchParameter/AuditEvent-address"));
		resourceCriteria.add(new LabelKeyValueBean("Identifier of who", "agent", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/AuditEvent-agent"));
		resourceCriteria.add(new LabelKeyValueBean("Human friendly name for the agent", "agent-name", "", "STRING", "http://hl7.org/fhir/SearchParameter/AuditEvent-agent-name"));
		resourceCriteria.add(new LabelKeyValueBean("Agent role in the event", "agent-role", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/AuditEvent-agent-role"));
		resourceCriteria.add(new LabelKeyValueBean("Alternative User identity", "altid", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/AuditEvent-altid"));
		resourceCriteria.add(new LabelKeyValueBean("Time when the event was recorded", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/AuditEvent-date"));
		resourceCriteria.add(new LabelKeyValueBean("Specific instance of resource", "entity", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/AuditEvent-entity"));
		resourceCriteria.add(new LabelKeyValueBean("Descriptor for entity", "entity-name", "", "STRING", "http://hl7.org/fhir/SearchParameter/AuditEvent-entity-name"));
		resourceCriteria.add(new LabelKeyValueBean("What role the entity played", "entity-role", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/AuditEvent-entity-role"));
		resourceCriteria.add(new LabelKeyValueBean("Type of entity involved", "entity-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/AuditEvent-entity-type"));
		resourceCriteria.add(new LabelKeyValueBean("Whether the event succeeded or failed", "outcome", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/AuditEvent-outcome"));
		resourceCriteria.add(new LabelKeyValueBean("Identifier of who", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/AuditEvent-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("Policy that authorized event", "policy", "", "URI", "http://hl7.org/fhir/SearchParameter/AuditEvent-policy"));
		resourceCriteria.add(new LabelKeyValueBean("Logical source location within the enterprise", "site", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/AuditEvent-site"));
		resourceCriteria.add(new LabelKeyValueBean("The identity of source detecting the event", "source", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/AuditEvent-source"));
		resourceCriteria.add(new LabelKeyValueBean("More specific type/id for the event", "subtype", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/AuditEvent-subtype"));
		resourceCriteria.add(new LabelKeyValueBean("Type/identifier of event", "type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/AuditEvent-type"));
		resourceTypeCriteria.put("AuditEvent", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Who created", "author", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Basic-author"));
		resourceCriteria.add(new LabelKeyValueBean("Kind of Resource", "code", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Basic-code"));
		resourceCriteria.add(new LabelKeyValueBean("When created", "created", "", "DATE", "http://hl7.org/fhir/SearchParameter/Basic-created"));
		resourceCriteria.add(new LabelKeyValueBean("Business identifier", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Basic-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Identifies the focus of this resource", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Basic-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("Identifies the focus of this resource", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Basic-subject"));
		resourceTypeCriteria.put("Basic", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceTypeCriteria.put("Binary", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		// AEGIS Defined Search Parameters
		resourceCriteria.add(new LabelKeyValueBean("BiologicallyDerivedProduct identifier", "identifier", "", "TOKEN"));
		resourceCriteria.add(new LabelKeyValueBean("Broad category of this product", "product-category", "", "TOKEN"));
		resourceCriteria.add(new LabelKeyValueBean("A code that defines the kind of this biologically derived product", "product-code", "", "TOKEN"));
		resourceCriteria.add(new LabelKeyValueBean("Request to obtain this biologically derived product", "request", "", "REFERENCE", null, "ServiceRequest"));
		resourceCriteria.add(new LabelKeyValueBean("Whether the product is currently available", "status", "", "TOKEN"));
		resourceTypeCriteria.put("BiologicallyDerivedProduct", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Bodystructure identifier", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/BodyStructure-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Body site", "location", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/BodyStructure-location"));
		resourceCriteria.add(new LabelKeyValueBean("Kind of Structure", "morphology", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/BodyStructure-morphology"));
		resourceCriteria.add(new LabelKeyValueBean("Who this is about", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/BodyStructure-patient", "Patient"));
		resourceTypeCriteria.put("BodyStructure", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("The first resource in the bundle, if the bundle type is \"document\" - this is a composition, and this parameter provides access to search its contents", "composition", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Bundle-composition", "Composition"));
		resourceCriteria.add(new LabelKeyValueBean("Persistent identifier for the bundle", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Bundle-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("The first resource in the bundle, if the bundle type is \"message\" - this is a message header, and this parameter provides access to search its contents", "message", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Bundle-message", "MessageHeader"));
		resourceCriteria.add(new LabelKeyValueBean("When the bundle was assembled", "timestamp", "", "DATE", "http://hl7.org/fhir/SearchParameter/Bundle-timestamp"));
		resourceCriteria.add(new LabelKeyValueBean("document | message | transaction | transaction-response | batch | batch-response | history | searchset | collection", "type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Bundle-type"));
		resourceTypeCriteria.put("Bundle", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("A use context assigned to the capability statement", "context", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-context"));
		resourceCriteria.add(new LabelKeyValueBean("A quantity- or range-valued use context assigned to the capability statement", "context-quantity", "", "QUANTITY", "http://hl7.org/fhir/SearchParameter/conformance-context-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A type of use context assigned to the capability statement", "context-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-context-type"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and quantity- or range-based value assigned to the capability statement", "context-type-quantity", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/conformance-context-type-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and value assigned to the capability statement", "context-type-value", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/conformance-context-type-value"));
		resourceCriteria.add(new LabelKeyValueBean("The capability statement publication date", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/conformance-date"));
		resourceCriteria.add(new LabelKeyValueBean("The description of the capability statement", "description", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-description"));
		resourceCriteria.add(new LabelKeyValueBean("The version of FHIR", "fhirversion", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/CapabilityStatement-fhirversion"));
		resourceCriteria.add(new LabelKeyValueBean("formats supported (xml | json | ttl | mime type)", "format", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/CapabilityStatement-format"));
		resourceCriteria.add(new LabelKeyValueBean("Implementation guides supported", "guide", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/CapabilityStatement-guide", "ImplementationGuide"));
		resourceCriteria.add(new LabelKeyValueBean("Intended jurisdiction for the capability statement", "jurisdiction", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-jurisdiction"));
		resourceCriteria.add(new LabelKeyValueBean("Mode - restful (server/client) or messaging (sender/receiver)", "mode", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/CapabilityStatement-mode"));
		resourceCriteria.add(new LabelKeyValueBean("Computationally friendly name of the capability statement", "name", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-name"));
		resourceCriteria.add(new LabelKeyValueBean("Name of the publisher of the capability statement", "publisher", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-publisher"));
		resourceCriteria.add(new LabelKeyValueBean("Name of a resource mentioned in a capability statement", "resource", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/CapabilityStatement-resource"));
		resourceCriteria.add(new LabelKeyValueBean("A profile id invoked in a capability statement", "resource-profile", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/CapabilityStatement-resource-profile", "StructureDefinition"));
		resourceCriteria.add(new LabelKeyValueBean("OAuth | SMART-on-FHIR | NTLM | Basic | Kerberos | Certificates", "security-service", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/CapabilityStatement-security-service"));
		resourceCriteria.add(new LabelKeyValueBean("Part of the name of a software application", "software", "", "STRING", "http://hl7.org/fhir/SearchParameter/CapabilityStatement-software"));
		resourceCriteria.add(new LabelKeyValueBean("The current status of the capability statement", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-status"));
		resourceCriteria.add(new LabelKeyValueBean("Profiles for use cases supported", "supported-profile", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/CapabilityStatement-supported-profile", "StructureDefinition"));
		resourceCriteria.add(new LabelKeyValueBean("The human-friendly name of the capability statement", "title", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-title"));
		resourceCriteria.add(new LabelKeyValueBean("The uri that identifies the capability statement", "url", "", "URI", "http://hl7.org/fhir/SearchParameter/conformance-url"));
		resourceCriteria.add(new LabelKeyValueBean("The business version of the capability statement", "version", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-version"));
		resourceTypeCriteria.put("CapabilityStatement", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Detail type of activity", "activity-code", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/CarePlan-activity-code"));
		resourceCriteria.add(new LabelKeyValueBean("Specified date occurs within period specified by CarePlan.activity.detail.scheduled[x]", "activity-date", "", "DATE", "http://hl7.org/fhir/SearchParameter/CarePlan-activity-date"));
		resourceCriteria.add(new LabelKeyValueBean("Activity details defined in specific resource", "activity-reference", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/CarePlan-activity-reference"));
		resourceCriteria.add(new LabelKeyValueBean("Fulfills CarePlan", "based-on", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/CarePlan-based-on", "CarePlan"));
		resourceCriteria.add(new LabelKeyValueBean("Who's involved in plan?", "care-team", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/CarePlan-care-team", "CareTeam"));
		resourceCriteria.add(new LabelKeyValueBean("Type of plan", "category", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/CarePlan-category"));
		resourceCriteria.add(new LabelKeyValueBean("Health issues this plan addresses", "condition", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/CarePlan-condition", "Condition"));
		resourceCriteria.add(new LabelKeyValueBean("Time period plan covers", "date", "", "PERIOD", "http://hl7.org/fhir/SearchParameter/clinical-date"));
		resourceCriteria.add(new LabelKeyValueBean("Encounter created as part of", "encounter", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/CarePlan-encounter", "Encounter"));
		resourceCriteria.add(new LabelKeyValueBean("Desired outcome of plan", "goal", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/CarePlan-goal", "Goal"));
		resourceCriteria.add(new LabelKeyValueBean("External Ids for this plan", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Instantiates FHIR protocol or definition", "instantiates-canonical", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/CarePlan-instantiates-canonical"));
		resourceCriteria.add(new LabelKeyValueBean("Instantiates external protocol or definition", "instantiates-uri", "", "URI", "http://hl7.org/fhir/SearchParameter/CarePlan-instantiates-uri"));
		resourceCriteria.add(new LabelKeyValueBean("proposal | plan | order | option", "intent", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/CarePlan-intent"));
		resourceCriteria.add(new LabelKeyValueBean("Part of referenced CarePlan", "part-of", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/CarePlan-part-of", "CarePlan"));
		resourceCriteria.add(new LabelKeyValueBean("Who the care plan is for", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("Matches if the practitioner is listed as a performer in any of the \"simple\" activities.  (For performers of the detailed activities, chain through the activitydetail search parameter.)", "performer", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/CarePlan-performer"));
		resourceCriteria.add(new LabelKeyValueBean("CarePlan replaced by this CarePlan", "replaces", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/CarePlan-replaces", "CarePlan"));
		resourceCriteria.add(new LabelKeyValueBean("draft | active | suspended | completed | entered-in-error | cancelled | unknown", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/CarePlan-status"));
		resourceCriteria.add(new LabelKeyValueBean("Who the care plan is for", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/CarePlan-subject"));
		resourceTypeCriteria.put("CarePlan", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Type of team", "category", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/CareTeam-category"));
		resourceCriteria.add(new LabelKeyValueBean("Time period team covers", "date", "", "PERIOD", "http://hl7.org/fhir/SearchParameter/clinical-date"));
		resourceCriteria.add(new LabelKeyValueBean("Encounter created as part of", "encounter", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/CareTeam-encounter", "Encounter"));
		resourceCriteria.add(new LabelKeyValueBean("External Ids for this team", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Who is involved", "participant", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/CareTeam-participant"));
		resourceCriteria.add(new LabelKeyValueBean("Who care team is for", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("proposed | active | suspended | inactive | entered-in-error", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/CareTeam-status"));
		resourceCriteria.add(new LabelKeyValueBean("Who care team is for", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/CareTeam-subject"));
		resourceTypeCriteria.put("CareTeam", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceTypeCriteria.put("CatalogEntry", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Account to place this charge", "account", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ChargeItem-account", "Account"));
		resourceCriteria.add(new LabelKeyValueBean("A code that identifies the charge, like a billing code", "code", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ChargeItem-code"));
		resourceCriteria.add(new LabelKeyValueBean("Encounter / Episode associated with event", "context", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ChargeItem-context"));
		resourceCriteria.add(new LabelKeyValueBean("Date the charge item was entered", "entered-date", "", "DATE", "http://hl7.org/fhir/SearchParameter/ChargeItem-entered-date"));
		resourceCriteria.add(new LabelKeyValueBean("Individual who was entering", "enterer", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ChargeItem-enterer"));
		resourceCriteria.add(new LabelKeyValueBean("Factor overriding the associated rules", "factor-override", "", "NUMBER", "http://hl7.org/fhir/SearchParameter/ChargeItem-factor-override"));
		resourceCriteria.add(new LabelKeyValueBean("Business Identifier for item", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ChargeItem-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("When the charged service was applied", "occurrence", "", "DATE", "http://hl7.org/fhir/SearchParameter/ChargeItem-occurrence"));
		resourceCriteria.add(new LabelKeyValueBean("Individual service was done for/to", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ChargeItem-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("Individual who was performing", "performer-actor", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ChargeItem-performer-actor"));
		resourceCriteria.add(new LabelKeyValueBean("What type of performance was done", "performer-function", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ChargeItem-performer-function"));
		resourceCriteria.add(new LabelKeyValueBean("Organization providing the charged service", "performing-organization", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ChargeItem-performing-organization", "Organization"));
		resourceCriteria.add(new LabelKeyValueBean("Price overriding the associated rules", "price-override", "", "QUANTITY", "http://hl7.org/fhir/SearchParameter/ChargeItem-price-override"));
		resourceCriteria.add(new LabelKeyValueBean("Quantity of which the charge item has been serviced", "quantity", "", "QUANTITY", "http://hl7.org/fhir/SearchParameter/ChargeItem-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("Organization requesting the charged service", "requesting-organization", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ChargeItem-requesting-organization", "Organization"));
		resourceCriteria.add(new LabelKeyValueBean("Which rendered service is being charged?", "service", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ChargeItem-service"));
		resourceCriteria.add(new LabelKeyValueBean("Individual service was done for/to", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ChargeItem-subject"));
		// EXTRA
		resourceCriteria.add(new LabelKeyValueBean("Encounter or episode associated with the charge", "encounter", "", "REFERENCE", null, "Encounter"));
		resourceCriteria.add(new LabelKeyValueBean("The current state of the ChargeItem", "status", "", "TOKEN", ""));
		resourceTypeCriteria.put("ChargeItem", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("A use context assigned to the charge item definition", "context", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ChargeItemDefinition-context"));
		resourceCriteria.add(new LabelKeyValueBean("A quantity- or range-valued use context assigned to the charge item definition", "context-quantity", "", "QUANTITY", "http://hl7.org/fhir/SearchParameter/ChargeItemDefinition-context-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A type of use context assigned to the charge item definition", "context-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ChargeItemDefinition-context-type"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and quantity- or range-based value assigned to the charge item definition", "context-type-quantity", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/ChargeItemDefinition-context-type-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and value assigned to the charge item definition", "context-type-value", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/ChargeItemDefinition-context-type-value"));
		resourceCriteria.add(new LabelKeyValueBean("The charge item definition publication date", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/ChargeItemDefinition-date"));
		resourceCriteria.add(new LabelKeyValueBean("The description of the charge item definition", "description", "", "STRING", "http://hl7.org/fhir/SearchParameter/ChargeItemDefinition-description"));
		resourceCriteria.add(new LabelKeyValueBean("The time during which the charge item definition is intended to be in use", "effective", "", "PERIOD", "http://hl7.org/fhir/SearchParameter/ChargeItemDefinition-effective"));
		resourceCriteria.add(new LabelKeyValueBean("External identifier for the charge item definition", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ChargeItemDefinition-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Intended jurisdiction for the charge item definition", "jurisdiction", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ChargeItemDefinition-jurisdiction"));
		resourceCriteria.add(new LabelKeyValueBean("Name of the publisher of the charge item definition", "publisher", "", "STRING", "http://hl7.org/fhir/SearchParameter/ChargeItemDefinition-publisher"));
		resourceCriteria.add(new LabelKeyValueBean("The current status of the charge item definition", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ChargeItemDefinition-status"));
		resourceCriteria.add(new LabelKeyValueBean("The human-friendly name of the charge item definition", "title", "", "STRING", "http://hl7.org/fhir/SearchParameter/ChargeItemDefinition-title"));
		resourceCriteria.add(new LabelKeyValueBean("The uri that identifies the charge item definition", "url", "", "URI", "http://hl7.org/fhir/SearchParameter/ChargeItemDefinition-url"));
		resourceCriteria.add(new LabelKeyValueBean("The business version of the charge item definition", "version", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ChargeItemDefinition-version"));
		// EXTRA
		resourceCriteria.add(new LabelKeyValueBean("Computationally friendly name of the charge item definition", "name", "", "STRING"));
		resourceTypeCriteria.put("ChargeItemDefinition", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Member of the CareTeam", "care-team", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Claim-care-team"));
		resourceCriteria.add(new LabelKeyValueBean("The creation date for the Claim", "created", "", "DATE", "http://hl7.org/fhir/SearchParameter/Claim-created"));
		resourceCriteria.add(new LabelKeyValueBean("UDI associated with a line item, detail product or service", "detail-udi", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Claim-detail-udi", "Device"));
		resourceCriteria.add(new LabelKeyValueBean("Encounters associated with a billed line item", "encounter", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Claim-encounter", "Encounter"));
		resourceCriteria.add(new LabelKeyValueBean("The party responsible for the entry of the Claim", "enterer", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Claim-enterer"));
		resourceCriteria.add(new LabelKeyValueBean("Facility where the products or services have been or will be provided", "facility", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Claim-facility", "Location"));
		resourceCriteria.add(new LabelKeyValueBean("The primary identifier of the financial resource", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Claim-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("The target payor/insurer for the Claim", "insurer", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Claim-insurer", "Organization"));
		resourceCriteria.add(new LabelKeyValueBean("UDI associated with a line item product or service", "item-udi", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Claim-item-udi", "Device"));
		resourceCriteria.add(new LabelKeyValueBean("Patient receiving the products or services", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Claim-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("The party receiving any payment for the Claim", "payee", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Claim-payee"));
		resourceCriteria.add(new LabelKeyValueBean("Processing priority requested", "priority", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Claim-priority"));
		resourceCriteria.add(new LabelKeyValueBean("UDI associated with a procedure", "procedure-udi", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Claim-procedure-udi", "Device"));
		resourceCriteria.add(new LabelKeyValueBean("Provider responsible for the Claim", "provider", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Claim-provider"));
		resourceCriteria.add(new LabelKeyValueBean("The status of the Claim instance.", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Claim-status"));
		resourceCriteria.add(new LabelKeyValueBean("UDI associated with a line item, detail, subdetail product or service", "subdetail-udi", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Claim-subdetail-udi", "Device"));
		resourceCriteria.add(new LabelKeyValueBean("The kind of financial resource", "use", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Claim-use"));
		resourceTypeCriteria.put("Claim", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("The creation date", "created", "", "DATE", "http://hl7.org/fhir/SearchParameter/ClaimResponse-created"));
		resourceCriteria.add(new LabelKeyValueBean("The contents of the disposition message", "disposition", "", "STRING", "http://hl7.org/fhir/SearchParameter/ClaimResponse-disposition"));
		resourceCriteria.add(new LabelKeyValueBean("The identity of the ClaimResponse", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ClaimResponse-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("The organization which generated this resource", "insurer", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ClaimResponse-insurer", "Organization"));
		resourceCriteria.add(new LabelKeyValueBean("The processing outcome", "outcome", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ClaimResponse-outcome"));
		resourceCriteria.add(new LabelKeyValueBean("The subject of care", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ClaimResponse-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("The expected payment date", "payment-date", "", "DATE", "http://hl7.org/fhir/SearchParameter/ClaimResponse-payment-date"));
		resourceCriteria.add(new LabelKeyValueBean("The claim reference", "request", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ClaimResponse-request", "Claim"));
		resourceCriteria.add(new LabelKeyValueBean("The Provider of the claim", "requestor", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ClaimResponse-requestor"));
		resourceCriteria.add(new LabelKeyValueBean("The status of the ClaimResponse", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ClaimResponse-status"));
		resourceCriteria.add(new LabelKeyValueBean("The type of claim", "use", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ClaimResponse-use"));
		resourceTypeCriteria.put("ClaimResponse", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("The clinician performing the assessment", "assessor", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ClinicalImpression-assessor"));
		resourceCriteria.add(new LabelKeyValueBean("When the assessment was documented", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/clinical-date"));
		resourceCriteria.add(new LabelKeyValueBean("Encounter created as part of", "encounter", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ClinicalImpression-encounter", "Encounter"));
		resourceCriteria.add(new LabelKeyValueBean("What was found", "finding-code", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ClinicalImpression-finding-code"));
		resourceCriteria.add(new LabelKeyValueBean("What was found", "finding-ref", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ClinicalImpression-finding-ref"));
		resourceCriteria.add(new LabelKeyValueBean("Business identifier", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ClinicalImpression-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Record of a specific investigation", "investigation", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ClinicalImpression-investigation"));
		resourceCriteria.add(new LabelKeyValueBean("Patient or group assessed", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("Reference to last assessment", "previous", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ClinicalImpression-previous", "ClinicalImpression"));
		resourceCriteria.add(new LabelKeyValueBean("Relevant impressions of patient state", "problem", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ClinicalImpression-problem"));
		resourceCriteria.add(new LabelKeyValueBean("draft | completed | entered-in-error", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ClinicalImpression-status"));
		resourceCriteria.add(new LabelKeyValueBean("Patient or group assessed", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ClinicalImpression-subject"));
		resourceCriteria.add(new LabelKeyValueBean("Information supporting the clinical impression", "supporting-info", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ClinicalImpression-supporting-info"));
		resourceTypeCriteria.put("ClinicalImpression", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("A code defined in the code system", "code", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/CodeSystem-code"));
		resourceCriteria.add(new LabelKeyValueBean("not-present | example | fragment | complete | supplement", "content-mode", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/CodeSystem-content-mode"));
		resourceCriteria.add(new LabelKeyValueBean("A use context assigned to the code system", "context", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-context"));
		resourceCriteria.add(new LabelKeyValueBean("A quantity- or range-valued use context assigned to the code system", "context-quantity", "", "QUANTITY", "http://hl7.org/fhir/SearchParameter/conformance-context-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A type of use context assigned to the code system", "context-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-context-type"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and quantity- or range-based value assigned to the code system", "context-type-quantity", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/conformance-context-type-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and value assigned to the code system", "context-type-value", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/conformance-context-type-value"));
		resourceCriteria.add(new LabelKeyValueBean("The code system publication date", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/conformance-date"));
		resourceCriteria.add(new LabelKeyValueBean("The description of the code system", "description", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-description"));
		resourceCriteria.add(new LabelKeyValueBean("External identifier for the code system", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Intended jurisdiction for the code system", "jurisdiction", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-jurisdiction"));
		resourceCriteria.add(new LabelKeyValueBean("A language in which a designation is provided", "language", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/CodeSystem-language"));
		resourceCriteria.add(new LabelKeyValueBean("Computationally friendly name of the code system", "name", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-name"));
		resourceCriteria.add(new LabelKeyValueBean("Name of the publisher of the code system", "publisher", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-publisher"));
		resourceCriteria.add(new LabelKeyValueBean("The current status of the code system", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-status"));
		resourceCriteria.add(new LabelKeyValueBean("Find code system supplements for the referenced code system", "supplements", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/CodeSystem-supplements", "CodeSystem"));
		resourceCriteria.add(new LabelKeyValueBean("The system for any codes defined by this code system (same as 'url')", "system", "", "URI", "http://hl7.org/fhir/SearchParameter/CodeSystem-system"));
		resourceCriteria.add(new LabelKeyValueBean("The human-friendly name of the code system", "title", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-title"));
		resourceCriteria.add(new LabelKeyValueBean("The uri that identifies the code system", "url", "", "URI", "http://hl7.org/fhir/SearchParameter/conformance-url"));
		resourceCriteria.add(new LabelKeyValueBean("The business version of the code system", "version", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-version"));
		resourceTypeCriteria.put("CodeSystem", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Request fulfilled by this communication", "based-on", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Communication-based-on"));
		resourceCriteria.add(new LabelKeyValueBean("Message category", "category", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Communication-category"));
		resourceCriteria.add(new LabelKeyValueBean("Encounter created as part of", "encounter", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Communication-encounter", "Encounter"));
		resourceCriteria.add(new LabelKeyValueBean("Unique identifier", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Communication-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Instantiates FHIR protocol or definition", "instantiates-canonical", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Communication-instantiates-canonical"));
		resourceCriteria.add(new LabelKeyValueBean("Instantiates external protocol or definition", "instantiates-uri", "", "URI", "http://hl7.org/fhir/SearchParameter/Communication-instantiates-uri"));
		resourceCriteria.add(new LabelKeyValueBean("A channel of communication", "medium", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Communication-medium"));
		resourceCriteria.add(new LabelKeyValueBean("Part of this action", "part-of", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Communication-part-of"));
		resourceCriteria.add(new LabelKeyValueBean("Focus of message", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Communication-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("When received", "received", "", "DATE", "http://hl7.org/fhir/SearchParameter/Communication-received"));
		resourceCriteria.add(new LabelKeyValueBean("Message recipient", "recipient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Communication-recipient"));
		resourceCriteria.add(new LabelKeyValueBean("Message sender", "sender", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Communication-sender"));
		resourceCriteria.add(new LabelKeyValueBean("When sent", "sent", "", "DATE", "http://hl7.org/fhir/SearchParameter/Communication-sent"));
		resourceCriteria.add(new LabelKeyValueBean("preparation | in-progress | not-done | suspended | aborted | completed | entered-in-error", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Communication-status"));
		resourceCriteria.add(new LabelKeyValueBean("Focus of message", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Communication-subject"));
		resourceTypeCriteria.put("Communication", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("When request transitioned to being actionable", "authored", "", "DATE", "http://hl7.org/fhir/SearchParameter/CommunicationRequest-authored"));
		resourceCriteria.add(new LabelKeyValueBean("Fulfills plan or proposal", "based-on", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/CommunicationRequest-based-on"));
		resourceCriteria.add(new LabelKeyValueBean("Message category", "category", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/CommunicationRequest-category"));
		resourceCriteria.add(new LabelKeyValueBean("Encounter created as part of", "encounter", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/CommunicationRequest-encounter", "Encounter"));
		resourceCriteria.add(new LabelKeyValueBean("Composite request this is part of", "group-identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/CommunicationRequest-group-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Unique identifier", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/CommunicationRequest-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("A channel of communication", "medium", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/CommunicationRequest-medium"));
		resourceCriteria.add(new LabelKeyValueBean("When scheduled", "occurrence", "", "DATE", "http://hl7.org/fhir/SearchParameter/CommunicationRequest-occurrence"));
		resourceCriteria.add(new LabelKeyValueBean("Focus of message", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/CommunicationRequest-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("Message urgency", "priority", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/CommunicationRequest-priority"));
		resourceCriteria.add(new LabelKeyValueBean("Message recipient", "recipient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/CommunicationRequest-recipient"));
		resourceCriteria.add(new LabelKeyValueBean("Request(s) replaced by this request", "replaces", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/CommunicationRequest-replaces", "CommunicationRequest"));
		resourceCriteria.add(new LabelKeyValueBean("Who/what is requesting service", "requester", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/CommunicationRequest-requester"));
		resourceCriteria.add(new LabelKeyValueBean("Message sender", "sender", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/CommunicationRequest-sender"));
		resourceCriteria.add(new LabelKeyValueBean("draft | active | suspended | cancelled | completed | entered-in-error | unknown", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/CommunicationRequest-status"));
		resourceCriteria.add(new LabelKeyValueBean("Focus of message", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/CommunicationRequest-subject"));
		resourceTypeCriteria.put("CommunicationRequest", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Patient | Encounter | RelatedPerson | Practitioner | Device", "code", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/CompartmentDefinition-code"));
		resourceCriteria.add(new LabelKeyValueBean("A use context assigned to the compartment definition", "context", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-context"));
		resourceCriteria.add(new LabelKeyValueBean("A quantity- or range-valued use context assigned to the compartment definition", "context-quantity", "", "QUANTITY", "http://hl7.org/fhir/SearchParameter/conformance-context-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A type of use context assigned to the compartment definition", "context-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-context-type"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and quantity- or range-based value assigned to the compartment definition", "context-type-quantity", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/conformance-context-type-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and value assigned to the compartment definition", "context-type-value", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/conformance-context-type-value"));
		resourceCriteria.add(new LabelKeyValueBean("The compartment definition publication date", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/conformance-date"));
		resourceCriteria.add(new LabelKeyValueBean("The description of the compartment definition", "description", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-description"));
		resourceCriteria.add(new LabelKeyValueBean("Computationally friendly name of the compartment definition", "name", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-name"));
		resourceCriteria.add(new LabelKeyValueBean("Name of the publisher of the compartment definition", "publisher", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-publisher"));
		resourceCriteria.add(new LabelKeyValueBean("Name of resource type", "resource", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/CompartmentDefinition-resource"));
		resourceCriteria.add(new LabelKeyValueBean("The current status of the compartment definition", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-status"));
		resourceCriteria.add(new LabelKeyValueBean("The uri that identifies the compartment definition", "url", "", "URI", "http://hl7.org/fhir/SearchParameter/conformance-url"));
		resourceCriteria.add(new LabelKeyValueBean("The business version of the compartment definition", "version", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-version"));
		resourceTypeCriteria.put("CompartmentDefinition", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Who attested the composition", "attester", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Composition-attester"));
		resourceCriteria.add(new LabelKeyValueBean("Who and/or what authored the composition", "author", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Composition-author"));
		resourceCriteria.add(new LabelKeyValueBean("Categorization of Composition", "category", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Composition-category"));
		resourceCriteria.add(new LabelKeyValueBean("As defined by affinity domain", "confidentiality", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Composition-confidentiality"));
		resourceCriteria.add(new LabelKeyValueBean("Code(s) that apply to the event being documented", "context", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Composition-context"));
		resourceCriteria.add(new LabelKeyValueBean("Composition editing time", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/clinical-date"));
		resourceCriteria.add(new LabelKeyValueBean("Context of the Composition", "encounter", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-encounter", "Encounter"));
		resourceCriteria.add(new LabelKeyValueBean("A reference to data that supports this section", "entry", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Composition-entry"));
		resourceCriteria.add(new LabelKeyValueBean("Version-independent identifier for the Composition", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Who and/or what the composition is about", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("The period covered by the documentation", "period", "", "PERIOD", "http://hl7.org/fhir/SearchParameter/Composition-period"));
		resourceCriteria.add(new LabelKeyValueBean("Target of the relationship", "related-id", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Composition-related-id"));
		resourceCriteria.add(new LabelKeyValueBean("Target of the relationship", "related-ref", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Composition-related-ref", "Composition"));
		resourceCriteria.add(new LabelKeyValueBean("Classification of section (recommended)", "section", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Composition-section"));
		resourceCriteria.add(new LabelKeyValueBean("preliminary | final | amended | entered-in-error", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Composition-status"));
		resourceCriteria.add(new LabelKeyValueBean("Who and/or what the composition is about", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Composition-subject"));
		resourceCriteria.add(new LabelKeyValueBean("Human Readable name/title", "title", "", "STRING", "http://hl7.org/fhir/SearchParameter/Composition-title"));
		resourceCriteria.add(new LabelKeyValueBean("Kind of composition (LOINC if possible)", "type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-type"));
		resourceTypeCriteria.put("Composition", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("A use context assigned to the concept map", "context", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-context"));
		resourceCriteria.add(new LabelKeyValueBean("A quantity- or range-valued use context assigned to the concept map", "context-quantity", "", "QUANTITY", "http://hl7.org/fhir/SearchParameter/conformance-context-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A type of use context assigned to the concept map", "context-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-context-type"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and quantity- or range-based value assigned to the concept map", "context-type-quantity", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/conformance-context-type-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and value assigned to the concept map", "context-type-value", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/conformance-context-type-value"));
		resourceCriteria.add(new LabelKeyValueBean("The concept map publication date", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/conformance-date"));
		resourceCriteria.add(new LabelKeyValueBean("Reference to property mapping depends on", "dependson", "", "URI", "http://hl7.org/fhir/SearchParameter/ConceptMap-dependson"));
		resourceCriteria.add(new LabelKeyValueBean("The description of the concept map", "description", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-description"));
		resourceCriteria.add(new LabelKeyValueBean("External identifier for the concept map", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Intended jurisdiction for the concept map", "jurisdiction", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-jurisdiction"));
		resourceCriteria.add(new LabelKeyValueBean("Computationally friendly name of the concept map", "name", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-name"));
		resourceCriteria.add(new LabelKeyValueBean("canonical reference to an additional ConceptMap to use for mapping if the source concept is unmapped", "other", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ConceptMap-other", "ConceptMap"));
		resourceCriteria.add(new LabelKeyValueBean("Reference to property mapping depends on", "product", "", "URI", "http://hl7.org/fhir/SearchParameter/ConceptMap-product"));
		resourceCriteria.add(new LabelKeyValueBean("Name of the publisher of the concept map", "publisher", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-publisher"));
		resourceCriteria.add(new LabelKeyValueBean("The source value set that contains the concepts that are being mapped", "source", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ConceptMap-source", "ValueSet"));
		resourceCriteria.add(new LabelKeyValueBean("Identifies element being mapped", "source-code", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ConceptMap-source-code"));
		resourceCriteria.add(new LabelKeyValueBean("Source system where concepts to be mapped are defined", "source-system", "", "URI", "http://hl7.org/fhir/SearchParameter/ConceptMap-source-system"));
		resourceCriteria.add(new LabelKeyValueBean("The source value set that contains the concepts that are being mapped", "source-uri", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ConceptMap-source-uri"));
		resourceCriteria.add(new LabelKeyValueBean("The current status of the concept map", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-status"));
		resourceCriteria.add(new LabelKeyValueBean("The target value set which provides context for the mappings", "target", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ConceptMap-target", "ValueSet"));
		resourceCriteria.add(new LabelKeyValueBean("Code that identifies the target element", "target-code", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ConceptMap-target-code"));
		resourceCriteria.add(new LabelKeyValueBean("Target system that the concepts are to be mapped to", "target-system", "", "URI", "http://hl7.org/fhir/SearchParameter/ConceptMap-target-system"));
		resourceCriteria.add(new LabelKeyValueBean("The target value set which provides context for the mappings", "target-uri", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ConceptMap-target-uri", "ValueSet"));
		resourceCriteria.add(new LabelKeyValueBean("The human-friendly name of the concept map", "title", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-title"));
		resourceCriteria.add(new LabelKeyValueBean("The uri that identifies the concept map", "url", "", "URI", "http://hl7.org/fhir/SearchParameter/conformance-url"));
		resourceCriteria.add(new LabelKeyValueBean("The business version of the concept map", "version", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-version"));
		resourceTypeCriteria.put("ConceptMap", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Abatement as age or age range", "abatement-age", "", "QUANTITY", "http://hl7.org/fhir/SearchParameter/Condition-abatement-age"));
		resourceCriteria.add(new LabelKeyValueBean("Date-related abatements (dateTime and period)", "abatement-date", "", "DATE", "http://hl7.org/fhir/SearchParameter/Condition-abatement-date"));
		resourceCriteria.add(new LabelKeyValueBean("Abatement as a string", "abatement-string", "", "STRING", "http://hl7.org/fhir/SearchParameter/Condition-abatement-string"));
		resourceCriteria.add(new LabelKeyValueBean("Person who asserts this condition", "asserter", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Condition-asserter"));
		resourceCriteria.add(new LabelKeyValueBean("Anatomical location, if relevant", "body-site", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Condition-body-site"));
		resourceCriteria.add(new LabelKeyValueBean("The category of the condition", "category", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Condition-category"));
		resourceCriteria.add(new LabelKeyValueBean("The clinical status of the condition", "clinical-status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Condition-clinical-status"));
		resourceCriteria.add(new LabelKeyValueBean("Code for the condition", "code", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-code"));
		resourceCriteria.add(new LabelKeyValueBean("Encounter created as part of", "encounter", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Condition-encounter", "Encounter"));
		resourceCriteria.add(new LabelKeyValueBean("Manifestation/symptom", "evidence", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Condition-evidence"));
		resourceCriteria.add(new LabelKeyValueBean("Supporting information found elsewhere", "evidence-detail", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Condition-evidence-detail"));
		resourceCriteria.add(new LabelKeyValueBean("A unique identifier of the condition record", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Onsets as age or age range", "onset-age", "", "QUANTITY", "http://hl7.org/fhir/SearchParameter/Condition-onset-age"));
		resourceCriteria.add(new LabelKeyValueBean("Date related onsets (dateTime and Period)", "onset-date", "", "DATE", "http://hl7.org/fhir/SearchParameter/Condition-onset-date"));
		resourceCriteria.add(new LabelKeyValueBean("Onsets as a string", "onset-info", "", "STRING", "http://hl7.org/fhir/SearchParameter/Condition-onset-info"));
		resourceCriteria.add(new LabelKeyValueBean("Who has the condition?", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("Date record was first recorded", "recorded-date", "", "DATE", "http://hl7.org/fhir/SearchParameter/Condition-recorded-date"));
		resourceCriteria.add(new LabelKeyValueBean("The severity of the condition", "severity", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Condition-severity"));
		resourceCriteria.add(new LabelKeyValueBean("Simple summary (disease specific)", "stage", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Condition-stage"));
		resourceCriteria.add(new LabelKeyValueBean("Who has the condition?", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Condition-subject"));
		resourceCriteria.add(new LabelKeyValueBean("unconfirmed | provisional | differential | confirmed | refuted | entered-in-error", "verification-status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Condition-verification-status"));
		resourceTypeCriteria.put("Condition", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Actions controlled by this rule", "action", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Consent-action"));
		resourceCriteria.add(new LabelKeyValueBean("Resource for the actor (or group, by role)", "actor", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Consent-actor"));
		resourceCriteria.add(new LabelKeyValueBean("Classification of the consent statement - for indexing/retrieval", "category", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Consent-category"));
		resourceCriteria.add(new LabelKeyValueBean("Who is agreeing to the policy and rules", "consentor", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Consent-consentor"));
		resourceCriteria.add(new LabelKeyValueBean("The actual data reference", "data", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Consent-data"));
		resourceCriteria.add(new LabelKeyValueBean("When this Consent was created or indexed", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/clinical-date"));
		resourceCriteria.add(new LabelKeyValueBean("Identifier for this record (external references)", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Custodian of the consent", "organization", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Consent-organization", "Organization"));
		resourceCriteria.add(new LabelKeyValueBean("Who the consent applies to", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("Timeframe for this rule", "period", "", "PERIOD", "http://hl7.org/fhir/SearchParameter/Consent-period"));
		resourceCriteria.add(new LabelKeyValueBean("Context of activities covered by this rule", "purpose", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Consent-purpose"));
		resourceCriteria.add(new LabelKeyValueBean("Which of the four areas this resource covers (extensible)", "scope", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Consent-scope"));
		resourceCriteria.add(new LabelKeyValueBean("Security Labels that define affected resources", "security-label", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Consent-security-label"));
		resourceCriteria.add(new LabelKeyValueBean("Search by reference to a Consent, DocumentReference, Contract  or QuestionnaireResponse", "source-reference", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Consent-source-reference"));
		resourceCriteria.add(new LabelKeyValueBean("draft | proposed | active | rejected | inactive | entered-in-error", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Consent-status"));
		resourceTypeCriteria.put("Consent", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("The authority of the contract", "authority", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Contract-authority", "Organization"));
		resourceCriteria.add(new LabelKeyValueBean("The domain of the contract", "domain", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Contract-domain", "Location"));
		resourceCriteria.add(new LabelKeyValueBean("The identity of the contract", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Contract-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("A source definition of the contract", "instantiates", "", "URI", "http://hl7.org/fhir/SearchParameter/Contract-instantiates"));
		resourceCriteria.add(new LabelKeyValueBean("The date/time the contract was issued", "issued", "", "DATE", "http://hl7.org/fhir/SearchParameter/Contract-issued"));
		resourceCriteria.add(new LabelKeyValueBean("The identity of the subject of the contract (if a patient)", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Contract-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("Contract Signatory Party", "signer", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Contract-signer"));
		resourceCriteria.add(new LabelKeyValueBean("The status of the contract", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Contract-status"));
		resourceCriteria.add(new LabelKeyValueBean("The identity of the subject of the contract", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Contract-subject"));
		resourceCriteria.add(new LabelKeyValueBean("The basal contract definition", "url", "", "URI", "http://hl7.org/fhir/SearchParameter/Contract-url"));
		resourceTypeCriteria.put("Contract", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Covered party", "beneficiary", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Coverage-beneficiary", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("Coverage class (eg. plan, group)", "class-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Coverage-class-type"));
		resourceCriteria.add(new LabelKeyValueBean("Value of the class (eg. Plan number, group number)", "class-value", "", "STRING", "http://hl7.org/fhir/SearchParameter/Coverage-class-value"));
		resourceCriteria.add(new LabelKeyValueBean("Dependent number", "dependent", "", "STRING", "http://hl7.org/fhir/SearchParameter/Coverage-dependent"));
		resourceCriteria.add(new LabelKeyValueBean("The primary identifier of the insured and the coverage", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Coverage-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Retrieve coverages for a patient", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Coverage-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("The identity of the insurer or party paying for services", "payor", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Coverage-payor"));
		// AEGIS Defined Search Parameter period (used for $everything)
		resourceCriteria.add(new LabelKeyValueBean("Coverage start and end dates", "period", "", "PERIOD"));
		resourceCriteria.add(new LabelKeyValueBean("Reference to the policyholder", "policy-holder", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Coverage-policy-holder"));
		resourceCriteria.add(new LabelKeyValueBean("The status of the Coverage", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Coverage-status"));
		resourceCriteria.add(new LabelKeyValueBean("Reference to the subscriber", "subscriber", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Coverage-subscriber"));
		resourceCriteria.add(new LabelKeyValueBean("The kind of coverage (health plan, auto, Workers Compensation)", "type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Coverage-type"));
		resourceTypeCriteria.put("Coverage", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("The creation date for the EOB", "created", "", "DATE", "http://hl7.org/fhir/SearchParameter/CoverageEligibilityRequest-created"));
		resourceCriteria.add(new LabelKeyValueBean("The party who is responsible for the request", "enterer", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/CoverageEligibilityRequest-enterer"));
		resourceCriteria.add(new LabelKeyValueBean("Facility responsible for the goods and services", "facility", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/CoverageEligibilityRequest-facility", "Location"));
		resourceCriteria.add(new LabelKeyValueBean("The business identifier of the Eligibility", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/CoverageEligibilityRequest-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("The reference to the patient", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/CoverageEligibilityRequest-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("The reference to the provider", "provider", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/CoverageEligibilityRequest-provider"));
		resourceCriteria.add(new LabelKeyValueBean("The status of the EligibilityRequest", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/CoverageEligibilityRequest-status"));
		resourceTypeCriteria.put("CoverageEligibilityRequest", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("The creation date", "created", "", "DATE", "http://hl7.org/fhir/SearchParameter/CoverageEligibilityResponse-created"));
		resourceCriteria.add(new LabelKeyValueBean("The contents of the disposition message", "disposition", "", "STRING", "http://hl7.org/fhir/SearchParameter/CoverageEligibilityResponse-disposition"));
		resourceCriteria.add(new LabelKeyValueBean("The business identifier", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/CoverageEligibilityResponse-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("The organization which generated this resource", "insurer", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/CoverageEligibilityResponse-insurer", "Organization"));
		resourceCriteria.add(new LabelKeyValueBean("The processing outcome", "outcome", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/CoverageEligibilityResponse-outcome"));
		resourceCriteria.add(new LabelKeyValueBean("The reference to the patient", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/CoverageEligibilityResponse-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("The EligibilityRequest reference", "request", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/CoverageEligibilityResponse-request", "CoverageEligibilityRequest"));
		resourceCriteria.add(new LabelKeyValueBean("The EligibilityRequest provider", "requestor", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/CoverageEligibilityResponse-requestor"));
		resourceCriteria.add(new LabelKeyValueBean("The EligibilityRequest status", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/CoverageEligibilityResponse-status"));
		resourceTypeCriteria.put("CoverageEligibilityResponse", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("The provider or device that identified the issue", "author", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/DetectedIssue-author"));
		resourceCriteria.add(new LabelKeyValueBean("Issue Category, e.g. drug-drug, duplicate therapy, etc.", "code", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/DetectedIssue-code"));
		resourceCriteria.add(new LabelKeyValueBean("When identified", "identified", "", "DATE", "http://hl7.org/fhir/SearchParameter/DetectedIssue-identified"));
		resourceCriteria.add(new LabelKeyValueBean("Unique id for the detected issue", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Problem resource", "implicated", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/DetectedIssue-implicated"));
		resourceCriteria.add(new LabelKeyValueBean("Associated patient", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-patient", "Patient"));
		resourceTypeCriteria.put("DetectedIssue", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("A server defined search that may match any of the string fields in Device.deviceName or Device.type.", "device-name", "", "STRING", "http://hl7.org/fhir/SearchParameter/Device-device-name"));
		resourceCriteria.add(new LabelKeyValueBean("Instance id from manufacturer, owner, and others", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Device-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("A location, where the resource is found", "location", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Device-location", "Location"));
		resourceCriteria.add(new LabelKeyValueBean("The manufacturer of the device", "manufacturer", "", "STRING", "http://hl7.org/fhir/SearchParameter/Device-manufacturer"));
		resourceCriteria.add(new LabelKeyValueBean("The model of the device", "model", "", "STRING", "http://hl7.org/fhir/SearchParameter/Device-model"));
		resourceCriteria.add(new LabelKeyValueBean("The organization responsible for the device", "organization", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Device-organization", "Organization"));
		resourceCriteria.add(new LabelKeyValueBean("Patient information, if the resource is affixed to a person", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Device-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("active | inactive | entered-in-error | unknown", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Device-status"));
		resourceCriteria.add(new LabelKeyValueBean("The type of the device", "type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Device-type"));
		resourceCriteria.add(new LabelKeyValueBean("UDI Barcode (RFID or other technology) string in *HRF* format.", "udi-carrier", "", "STRING", "http://hl7.org/fhir/SearchParameter/Device-udi-carrier"));
		resourceCriteria.add(new LabelKeyValueBean("The udi Device Identifier (DI)", "udi-di", "", "STRING", "http://hl7.org/fhir/SearchParameter/Device-udi-di"));
		resourceCriteria.add(new LabelKeyValueBean("Network address to contact device", "url", "", "URI", "http://hl7.org/fhir/SearchParameter/Device-url"));
		resourceTypeCriteria.put("Device", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("The identifier of the component", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/DeviceDefinition-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("The parent DeviceDefinition resource", "parent", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/DeviceDefinition-parent", "DeviceDefinition"));
		resourceCriteria.add(new LabelKeyValueBean("The device component type", "type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/DeviceDefinition-type"));
		resourceTypeCriteria.put("DeviceDefinition", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("The category of the metric", "category", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/DeviceMetric-category"));
		resourceCriteria.add(new LabelKeyValueBean("The identifier of the metric", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/DeviceMetric-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("The parent DeviceMetric resource", "parent", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/DeviceMetric-parent", "Device"));
		resourceCriteria.add(new LabelKeyValueBean("The device resource", "source", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/DeviceMetric-source", "Device"));
		resourceCriteria.add(new LabelKeyValueBean("The component type", "type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/DeviceMetric-type"));
		resourceTypeCriteria.put("DeviceMetric", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("When the request transitioned to being actionable", "authored-on", "", "DATE", "http://hl7.org/fhir/SearchParameter/DeviceRequest-authored-on"));
		resourceCriteria.add(new LabelKeyValueBean("Plan/proposal/order fulfilled by this request", "based-on", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/DeviceRequest-based-on"));
		resourceCriteria.add(new LabelKeyValueBean("Code for what is being requested/ordered", "code", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-code"));
		resourceCriteria.add(new LabelKeyValueBean("Reference to resource that is being requested/ordered", "device", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/DeviceRequest-device","Device"));
		resourceCriteria.add(new LabelKeyValueBean("Encounter during which request was created", "encounter", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-encounter", "Encounter"));
		resourceCriteria.add(new LabelKeyValueBean("When service should occur", "event-date", "", "DATE", "http://hl7.org/fhir/SearchParameter/DeviceRequest-event-date"));
		resourceCriteria.add(new LabelKeyValueBean("Composite request this is part of", "group-identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/DeviceRequest-group-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Business identifier for request/order", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Instantiates FHIR protocol or definition", "instantiates-canonical", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/DeviceRequest-instantiates-canonical"));
		resourceCriteria.add(new LabelKeyValueBean("Instantiates external protocol or definition", "instantiates-uri", "", "URI", "http://hl7.org/fhir/SearchParameter/DeviceRequest-instantiates-uri"));
		resourceCriteria.add(new LabelKeyValueBean("Associated insurance coverage", "insurance", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/DeviceRequest-insurance"));
		resourceCriteria.add(new LabelKeyValueBean("proposal | plan | original-order |reflex-order", "intent", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/DeviceRequest-intent"));
		resourceCriteria.add(new LabelKeyValueBean("Individual the service is ordered for", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("Desired performer for service", "performer", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/DeviceRequest-performer"));
		resourceCriteria.add(new LabelKeyValueBean("Request takes the place of referenced completed or terminated requests", "prior-request", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/DeviceRequest-prior-request"));
		resourceCriteria.add(new LabelKeyValueBean("Who/what is requesting service", "requester", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/DeviceRequest-requester"));
		resourceCriteria.add(new LabelKeyValueBean("entered-in-error | draft | active |suspended | completed", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/DeviceRequest-status"));
		resourceCriteria.add(new LabelKeyValueBean("Individual the service is ordered for", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/DeviceRequest-subject"));
		resourceTypeCriteria.put("DeviceRequest", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Search by device", "device", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/DeviceUseStatement-device", "Device"));
		resourceCriteria.add(new LabelKeyValueBean("Search by identifier", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/DeviceUseStatement-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Search by subject - a patient", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-patient"));
		resourceCriteria.add(new LabelKeyValueBean("Search by subject", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/DeviceUseStatement-subject"));
		// AEGIS Defined Search Parameter recorded-on (used for $everything)
		resourceCriteria.add(new LabelKeyValueBean("When statement was recorded", "recorded-on", "", "DATE"));
		resourceTypeCriteria.put("DeviceUseStatement", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Reference to the service request.", "based-on", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/DiagnosticReport-based-on"));
		resourceCriteria.add(new LabelKeyValueBean("Which diagnostic discipline/department created the report", "category", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/DiagnosticReport-category"));
		resourceCriteria.add(new LabelKeyValueBean("The code for the report, as opposed to codes for the atomic results, which are the names on the observation resource referred to from the result", "code", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-code"));
		resourceCriteria.add(new LabelKeyValueBean("A coded conclusion (interpretation/impression) on the report", "conclusion", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/DiagnosticReport-conclusion"));
		resourceCriteria.add(new LabelKeyValueBean("The clinically relevant time of the report", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/clinical-date"));
		resourceCriteria.add(new LabelKeyValueBean("The Encounter when the order was made", "encounter", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-encounter", "Encounter"));
		resourceCriteria.add(new LabelKeyValueBean("An identifier for the report", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("When the report was issued", "issued", "", "DATE", "http://hl7.org/fhir/SearchParameter/DiagnosticReport-issued"));
		resourceCriteria.add(new LabelKeyValueBean("A reference to the image source.", "media", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/DiagnosticReport-media", "Media"));
		resourceCriteria.add(new LabelKeyValueBean("The subject of the report if a patient", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("Who is responsible for the report", "performer", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/DiagnosticReport-performer"));
		resourceCriteria.add(new LabelKeyValueBean("Link to an atomic result (observation resource)", "result", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/DiagnosticReport-result", "Observation"));
		resourceCriteria.add(new LabelKeyValueBean("Who was the source of the report", "results-interpreter", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/DiagnosticReport-results-interpreter"));
		resourceCriteria.add(new LabelKeyValueBean("The specimen details", "specimen", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/DiagnosticReport-specimen", "Specimen"));
		resourceCriteria.add(new LabelKeyValueBean("The status of the report", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/DiagnosticReport-status"));
		resourceCriteria.add(new LabelKeyValueBean("The subject of the report", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/DiagnosticReport-subject"));
		resourceTypeCriteria.put("DiagnosticReport", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Who and/or what authored the DocumentManifest", "author", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/DocumentManifest-author"));
		resourceCriteria.add(new LabelKeyValueBean("When this document manifest created", "created", "", "DATE", "http://hl7.org/fhir/SearchParameter/DocumentManifest-created"));
		resourceCriteria.add(new LabelKeyValueBean("Human-readable description (title)", "description", "", "STRING", "http://hl7.org/fhir/SearchParameter/DocumentManifest-description"));
		resourceCriteria.add(new LabelKeyValueBean("Unique Identifier for the set of documents", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Items in manifest", "item", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/DocumentManifest-item"));
		resourceCriteria.add(new LabelKeyValueBean("The subject of the set of documents", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("Intended to get notified about this set of documents", "recipient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/DocumentManifest-recipient"));
		resourceCriteria.add(new LabelKeyValueBean("Identifiers of things that are related", "related-id", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/DocumentManifest-related-id"));
		resourceCriteria.add(new LabelKeyValueBean("Related Resource", "related-ref", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/DocumentManifest-related-ref"));
		resourceCriteria.add(new LabelKeyValueBean("The source system/application/software", "source", "", "URI", "http://hl7.org/fhir/SearchParameter/DocumentManifest-source"));
		resourceCriteria.add(new LabelKeyValueBean("current | superseded | entered-in-error", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/DocumentManifest-status"));
		resourceCriteria.add(new LabelKeyValueBean("The subject of the set of documents", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/DocumentManifest-subject"));
		resourceCriteria.add(new LabelKeyValueBean("Kind of document set", "type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-type"));
		resourceTypeCriteria.put("DocumentManifest", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Who/what authenticated the document", "authenticator", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/DocumentReference-authenticator"));
		resourceCriteria.add(new LabelKeyValueBean("Who and/or what authored the document", "author", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/DocumentReference-author"));
		resourceCriteria.add(new LabelKeyValueBean("Categorization of document", "category", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/DocumentReference-category"));
		resourceCriteria.add(new LabelKeyValueBean("Mime type of the content, with charset etc.", "contenttype", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/DocumentReference-contenttype"));
		resourceCriteria.add(new LabelKeyValueBean("Organization which maintains the document", "custodian", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/DocumentReference-custodian", "Organization"));
		resourceCriteria.add(new LabelKeyValueBean("When this document reference was created", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/DocumentReference-date"));
		resourceCriteria.add(new LabelKeyValueBean("Human-readable description", "description", "", "STRING", "http://hl7.org/fhir/SearchParameter/DocumentReference-description"));
		resourceCriteria.add(new LabelKeyValueBean("Context of the document  content", "encounter", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-encounter"));
		resourceCriteria.add(new LabelKeyValueBean("Main clinical acts documented", "event", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/DocumentReference-event"));
		resourceCriteria.add(new LabelKeyValueBean("Kind of facility where patient was seen", "facility", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/DocumentReference-facility"));
		resourceCriteria.add(new LabelKeyValueBean("Format/content rules for the document", "format", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/DocumentReference-format"));
		resourceCriteria.add(new LabelKeyValueBean("Master Version Specific Identifier", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Human language of the content (BCP-47)", "language", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/DocumentReference-language"));
		resourceCriteria.add(new LabelKeyValueBean("Uri where the data can be found", "location", "", "URI", "http://hl7.org/fhir/SearchParameter/DocumentReference-location"));
		resourceCriteria.add(new LabelKeyValueBean("Who/what is the subject of the document", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("Time of service that is being documented", "period", "", "PERIOD", "http://hl7.org/fhir/SearchParameter/DocumentReference-period"));
		resourceCriteria.add(new LabelKeyValueBean("Related identifiers or resources", "related", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/DocumentReference-related"));
		resourceCriteria.add(new LabelKeyValueBean("Target of the relationship", "relatesto", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/DocumentReference-relatesto", "DocumentReference"));
		resourceCriteria.add(new LabelKeyValueBean("replaces | transforms | signs | appends", "relation", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/DocumentReference-relation"));
		resourceCriteria.add(new LabelKeyValueBean("Combination of relation and relatesTo", "relationship", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/DocumentReference-relationship"));
		resourceCriteria.add(new LabelKeyValueBean("Document security-tags", "security-label", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/DocumentReference-security-label"));
		resourceCriteria.add(new LabelKeyValueBean("Additional details about where the content was created (e.g. clinical specialty)", "setting", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/DocumentReference-setting"));
		resourceCriteria.add(new LabelKeyValueBean("current | superseded | entered-in-error", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/DocumentReference-status"));
		resourceCriteria.add(new LabelKeyValueBean("Who/what is the subject of the document", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/DocumentReference-subject"));
		resourceCriteria.add(new LabelKeyValueBean("Kind of document (LOINC if possible)", "type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-type"));
		resourceTypeCriteria.put("DocumentReference", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("A use context assigned to the effect evidence synthesis", "context", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/EffectEvidenceSynthesis-context"));
		resourceCriteria.add(new LabelKeyValueBean("A quantity- or range-valued use context assigned to the effect evidence synthesis", "context-quantity", "", "QUANTITY", "http://hl7.org/fhir/SearchParameter/EffectEvidenceSynthesis-context-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A type of use context assigned to the effect evidence synthesis", "context-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/EffectEvidenceSynthesis-context-type"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and quantity- or range-based value assigned to the effect evidence synthesis", "context-type-quantity", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/EffectEvidenceSynthesis-context-type-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and value assigned to the effect evidence synthesis", "context-type-value", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/EffectEvidenceSynthesis-context-type-value"));
		resourceCriteria.add(new LabelKeyValueBean("The effect evidence synthesis publication date", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/EffectEvidenceSynthesis-date"));
		resourceCriteria.add(new LabelKeyValueBean("The description of the effect evidence synthesis", "description", "", "STRING", "http://hl7.org/fhir/SearchParameter/EffectEvidenceSynthesis-description"));
		resourceCriteria.add(new LabelKeyValueBean("The time during which the effect evidence synthesis is intended to be in use", "effective", "", "PERIOD", "http://hl7.org/fhir/SearchParameter/EffectEvidenceSynthesis-effective"));
		resourceCriteria.add(new LabelKeyValueBean("External identifier for the effect evidence synthesis", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/EffectEvidenceSynthesis-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Intended jurisdiction for the effect evidence synthesis", "jurisdiction", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/EffectEvidenceSynthesis-jurisdiction"));
		resourceCriteria.add(new LabelKeyValueBean("Computationally friendly name of the effect evidence synthesis", "name", "", "STRING", "http://hl7.org/fhir/SearchParameter/EffectEvidenceSynthesis-name"));
		resourceCriteria.add(new LabelKeyValueBean("Name of the publisher of the effect evidence synthesis", "publisher", "", "STRING", "http://hl7.org/fhir/SearchParameter/EffectEvidenceSynthesis-publisher"));
		resourceCriteria.add(new LabelKeyValueBean("The current status of the effect evidence synthesis", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/EffectEvidenceSynthesis-status"));
		resourceCriteria.add(new LabelKeyValueBean("The human-friendly name of the effect evidence synthesis", "title", "", "STRING", "http://hl7.org/fhir/SearchParameter/EffectEvidenceSynthesis-title"));
		resourceCriteria.add(new LabelKeyValueBean("The uri that identifies the effect evidence synthesis", "url", "", "URI", "http://hl7.org/fhir/SearchParameter/EffectEvidenceSynthesis-url"));
		resourceCriteria.add(new LabelKeyValueBean("The business version of the effect evidence synthesis", "version", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/EffectEvidenceSynthesis-version"));
		resourceTypeCriteria.put("EffectEvidenceSynthesis", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("The set of accounts that may be used for billing for this Encounter", "account", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Encounter-account", "Account"));
		resourceCriteria.add(new LabelKeyValueBean("The appointment that scheduled this encounter", "appointment", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Encounter-appointment", "Appointment"));
		resourceCriteria.add(new LabelKeyValueBean("The ServiceRequest that initiated this encounter", "based-on", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Encounter-based-on", "ServiceRequest"));
		resourceCriteria.add(new LabelKeyValueBean("Classification of patient encounter", "class", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Encounter-class"));
		resourceCriteria.add(new LabelKeyValueBean("A date within the period the Encounter lasted", "date", "", "PERIOD", "http://hl7.org/fhir/SearchParameter/clinical-date"));
		resourceCriteria.add(new LabelKeyValueBean("The diagnosis or procedure relevant to the encounter", "diagnosis", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Encounter-diagnosis"));
		resourceCriteria.add(new LabelKeyValueBean("Episode(s) of care that this encounter should be recorded against", "episode-of-care", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Encounter-episode-of-care", "EpisodeOfCare"));
		resourceCriteria.add(new LabelKeyValueBean("Identifier(s) by which this encounter is known", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Length of encounter in days", "length", "", "QUANTITY", "http://hl7.org/fhir/SearchParameter/Encounter-length"));
		resourceCriteria.add(new LabelKeyValueBean("Location the encounter takes place", "location", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Encounter-location", "Location"));
		resourceCriteria.add(new LabelKeyValueBean("Time period during which the patient was present at the location", "location-period", "", "PERIOD", "http://hl7.org/fhir/SearchParameter/Encounter-location-period"));
		resourceCriteria.add(new LabelKeyValueBean("Another Encounter this encounter is part of", "part-of", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Encounter-part-of", "Encounter"));
		resourceCriteria.add(new LabelKeyValueBean("Persons involved in the encounter other than the patient", "participant", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Encounter-participant"));
		resourceCriteria.add(new LabelKeyValueBean("Role of participant in encounter", "participant-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Encounter-participant-type"));
		resourceCriteria.add(new LabelKeyValueBean("The patient or group present at the encounter", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("Persons involved in the encounter other than the patient", "practitioner", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Encounter-practitioner", "Practitioner"));
		resourceCriteria.add(new LabelKeyValueBean("Coded reason the encounter takes place", "reason-code", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Encounter-reason-code"));
		resourceCriteria.add(new LabelKeyValueBean("Reason the encounter takes place (reference)", "reason-reference", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Encounter-reason-reference"));
		resourceCriteria.add(new LabelKeyValueBean("The organization (facility) responsible for this encounter", "service-provider", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Encounter-service-provider", "Organization"));
		resourceCriteria.add(new LabelKeyValueBean("Wheelchair, translator, stretcher, etc.", "special-arrangement", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Encounter-special-arrangement"));
		resourceCriteria.add(new LabelKeyValueBean("planned | arrived | triaged | in-progress | onleave | finished | cancelled +", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Encounter-status"));
		resourceCriteria.add(new LabelKeyValueBean("The patient or group present at the encounter", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Encounter-subject"));
		resourceCriteria.add(new LabelKeyValueBean("Specific type of encounter", "type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-type"));
		resourceTypeCriteria.put("Encounter", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Protocol/Profile/Standard to be used with this endpoint connection", "connection-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Endpoint-connection-type"));
		resourceCriteria.add(new LabelKeyValueBean("Identifies this endpoint across multiple systems", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Endpoint-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("A name that this endpoint can be identified by", "name", "", "STRING", "http://hl7.org/fhir/SearchParameter/Endpoint-name"));
		resourceCriteria.add(new LabelKeyValueBean("The organization that is managing the endpoint", "organization", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Endpoint-organization", "Organization"));
		resourceCriteria.add(new LabelKeyValueBean("The type of content that may be used at this endpoint (e.g. XDS Discharge summaries)", "payload-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Endpoint-payload-type"));
		resourceCriteria.add(new LabelKeyValueBean("The current status of the Endpoint (usually expected to be active)", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Endpoint-status"));
		resourceTypeCriteria.put("Endpoint", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		// AEGIS Defined Search Parameter created (used for $everything)
		resourceCriteria.add(new LabelKeyValueBean("Creation date", "created", "", "DATE"));
		resourceCriteria.add(new LabelKeyValueBean("The business identifier of the Enrollment", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/EnrollmentRequest-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("The party to be enrolled", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/EnrollmentRequest-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("The status of the enrollment", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/EnrollmentRequest-status"));
		resourceCriteria.add(new LabelKeyValueBean("The party to be enrolled", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/EnrollmentRequest-subject", "Patient"));
		resourceTypeCriteria.put("EnrollmentRequest", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("The business identifier of the EnrollmentResponse", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/EnrollmentResponse-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("The reference to the claim", "request", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/EnrollmentResponse-request", "EnrollmentRequest"));
		resourceCriteria.add(new LabelKeyValueBean("The status of the enrollment response", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/EnrollmentResponse-status"));
		resourceTypeCriteria.put("EnrollmentResponse", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Care manager/care coordinator for the patient", "care-manager", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/EpisodeOfCare-care-manager", "Practitioner"));
		resourceCriteria.add(new LabelKeyValueBean("Conditions/problems/diagnoses this episode of care is for", "condition", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/EpisodeOfCare-condition", "Condition"));
		resourceCriteria.add(new LabelKeyValueBean("The provided date search value falls within the episode of care's period", "date", "", "PERIOD", "http://hl7.org/fhir/SearchParameter/clinical-date"));
		resourceCriteria.add(new LabelKeyValueBean("Business Identifier(s) relevant for this EpisodeOfCare", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Incoming Referral Request", "incoming-referral", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/EpisodeOfCare-incoming-referral", "ServiceRequest"));
		resourceCriteria.add(new LabelKeyValueBean("The organization that has assumed the specific responsibilities of this EpisodeOfCare", "organization", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/EpisodeOfCare-organization", "Organization"));
		resourceCriteria.add(new LabelKeyValueBean("The patient who is the focus of this episode of care", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("The current status of the Episode of Care as provided (does not check the status history collection)", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/EpisodeOfCare-status"));
		resourceCriteria.add(new LabelKeyValueBean("Type/class  - e.g. specialist referral, disease management", "type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-type"));
		resourceTypeCriteria.put("EpisodeOfCare", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("What resource is being referenced", "composed-of", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/EventDefinition-composed-of"));
		resourceCriteria.add(new LabelKeyValueBean("A use context assigned to the event definition", "context", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/EventDefinition-context"));
		resourceCriteria.add(new LabelKeyValueBean("A quantity- or range-valued use context assigned to the event definition", "context-quantity", "", "QUANTITY", "http://hl7.org/fhir/SearchParameter/EventDefinition-context-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A type of use context assigned to the event definition", "context-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/EventDefinition-context-type"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and quantity- or range-based value assigned to the event definition", "context-type-quantity", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/EventDefinition-context-type-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and value assigned to the event definition", "context-type-value", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/EventDefinition-context-type-value"));
		resourceCriteria.add(new LabelKeyValueBean("The event definition publication date", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/EventDefinition-date"));
		resourceCriteria.add(new LabelKeyValueBean("What resource is being referenced", "depends-on", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/EventDefinition-depends-on"));
		resourceCriteria.add(new LabelKeyValueBean("What resource is being referenced", "derived-from", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/EventDefinition-derived-from"));
		resourceCriteria.add(new LabelKeyValueBean("The description of the event definition", "description", "", "STRING", "http://hl7.org/fhir/SearchParameter/EventDefinition-description"));
		resourceCriteria.add(new LabelKeyValueBean("The time during which the event definition is intended to be in use", "effective", "", "PERIOD", "http://hl7.org/fhir/SearchParameter/EventDefinition-effective"));
		resourceCriteria.add(new LabelKeyValueBean("External identifier for the event definition", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/EventDefinition-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Intended jurisdiction for the event definition", "jurisdiction", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/EventDefinition-jurisdiction"));
		resourceCriteria.add(new LabelKeyValueBean("Computationally friendly name of the event definition", "name", "", "STRING", "http://hl7.org/fhir/SearchParameter/EventDefinition-name"));
		resourceCriteria.add(new LabelKeyValueBean("What resource is being referenced", "predecessor", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/EventDefinition-predecessor"));
		resourceCriteria.add(new LabelKeyValueBean("Name of the publisher of the event definition", "publisher", "", "STRING", "http://hl7.org/fhir/SearchParameter/EventDefinition-publisher"));
		resourceCriteria.add(new LabelKeyValueBean("The current status of the event definition", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/EventDefinition-status"));
		resourceCriteria.add(new LabelKeyValueBean("What resource is being referenced", "successor", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/EventDefinition-successor"));
		resourceCriteria.add(new LabelKeyValueBean("The human-friendly name of the event definition", "title", "", "STRING", "http://hl7.org/fhir/SearchParameter/EventDefinition-title"));
		resourceCriteria.add(new LabelKeyValueBean("Topics associated with the module", "topic", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/EventDefinition-topic"));
		resourceCriteria.add(new LabelKeyValueBean("The uri that identifies the event definition", "url", "", "URI", "http://hl7.org/fhir/SearchParameter/EventDefinition-url"));
		resourceCriteria.add(new LabelKeyValueBean("The business version of the event definition", "version", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/EventDefinition-version"));
		resourceTypeCriteria.put("EventDefinition", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("What resource is being referenced", "composed-of", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Evidence-composed-of"));
		resourceCriteria.add(new LabelKeyValueBean("A use context assigned to the evidence", "context", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Evidence-context"));
		resourceCriteria.add(new LabelKeyValueBean("A quantity- or range-valued use context assigned to the evidence", "context-quantity", "", "QUANTITY", "http://hl7.org/fhir/SearchParameter/Evidence-context-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A type of use context assigned to the evidence", "context-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Evidence-context-type"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and quantity- or range-based value assigned to the evidence", "context-type-quantity", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/Evidence-context-type-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and value assigned to the evidence", "context-type-value", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/Evidence-context-type-value"));
		resourceCriteria.add(new LabelKeyValueBean("The evidence publication date", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/Evidence-date"));
		resourceCriteria.add(new LabelKeyValueBean("What resource is being referenced", "depends-on", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Evidence-depends-on"));
		resourceCriteria.add(new LabelKeyValueBean("What resource is being referenced", "derived-from", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Evidence-derived-from"));
		resourceCriteria.add(new LabelKeyValueBean("The description of the evidence", "description", "", "STRING", "http://hl7.org/fhir/SearchParameter/Evidence-description"));
		resourceCriteria.add(new LabelKeyValueBean("The time during which the evidence is intended to be in use", "effective", "", "PERIOD", "http://hl7.org/fhir/SearchParameter/Evidence-effective"));
		resourceCriteria.add(new LabelKeyValueBean("External identifier for the evidence", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Evidence-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Intended jurisdiction for the evidence", "jurisdiction", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Evidence-jurisdiction"));
		resourceCriteria.add(new LabelKeyValueBean("Computationally friendly name of the evidence", "name", "", "STRING", "http://hl7.org/fhir/SearchParameter/Evidence-name"));
		resourceCriteria.add(new LabelKeyValueBean("What resource is being referenced", "predecessor", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Evidence-predecessor"));
		resourceCriteria.add(new LabelKeyValueBean("Name of the publisher of the evidence", "publisher", "", "STRING", "http://hl7.org/fhir/SearchParameter/Evidence-publisher"));
		resourceCriteria.add(new LabelKeyValueBean("The current status of the evidence", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Evidence-status"));
		resourceCriteria.add(new LabelKeyValueBean("What resource is being referenced", "successor", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Evidence-successor"));
		resourceCriteria.add(new LabelKeyValueBean("The human-friendly name of the evidence", "title", "", "STRING", "http://hl7.org/fhir/SearchParameter/Evidence-title"));
		resourceCriteria.add(new LabelKeyValueBean("Topics associated with the Evidence", "topic", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Evidence-topic"));
		resourceCriteria.add(new LabelKeyValueBean("The uri that identifies the evidence", "url", "", "URI", "http://hl7.org/fhir/SearchParameter/Evidence-url"));
		resourceCriteria.add(new LabelKeyValueBean("The business version of the evidence", "version", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Evidence-version"));
		resourceTypeCriteria.put("Evidence", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("What resource is being referenced", "composed-of", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/EvidenceVariable-composed-of"));
		resourceCriteria.add(new LabelKeyValueBean("A use context assigned to the evidence variable", "context", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/EvidenceVariable-context"));
		resourceCriteria.add(new LabelKeyValueBean("A quantity- or range-valued use context assigned to the evidence variable", "context-quantity", "", "QUANTITY", "http://hl7.org/fhir/SearchParameter/EvidenceVariable-context-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A type of use context assigned to the evidence variable", "context-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/EvidenceVariable-context-type"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and quantity- or range-based value assigned to the evidence variable", "context-type-quantity", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/EvidenceVariable-context-type-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and value assigned to the evidence variable", "context-type-value", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/EvidenceVariable-context-type-value"));
		resourceCriteria.add(new LabelKeyValueBean("The evidence variable publication date", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/EvidenceVariable-date"));
		resourceCriteria.add(new LabelKeyValueBean("What resource is being referenced", "depends-on", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/EvidenceVariable-depends-on"));
		resourceCriteria.add(new LabelKeyValueBean("What resource is being referenced", "derived-from", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/EvidenceVariable-derived-from"));
		resourceCriteria.add(new LabelKeyValueBean("The description of the evidence variable", "description", "", "STRING", "http://hl7.org/fhir/SearchParameter/EvidenceVariable-description"));
		resourceCriteria.add(new LabelKeyValueBean("The time during which the evidence variable is intended to be in use", "effective", "", "PERIOD", "http://hl7.org/fhir/SearchParameter/EvidenceVariable-effective"));
		resourceCriteria.add(new LabelKeyValueBean("External identifier for the evidence variable", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/EvidenceVariable-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Intended jurisdiction for the evidence variable", "jurisdiction", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/EvidenceVariable-jurisdiction"));
		resourceCriteria.add(new LabelKeyValueBean("Computationally friendly name of the evidence variable", "name", "", "STRING", "http://hl7.org/fhir/SearchParameter/EvidenceVariable-name"));
		resourceCriteria.add(new LabelKeyValueBean("What resource is being referenced", "predecessor", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/EvidenceVariable-predecessor"));
		resourceCriteria.add(new LabelKeyValueBean("Name of the publisher of the evidence variable", "publisher", "", "STRING", "http://hl7.org/fhir/SearchParameter/EvidenceVariable-publisher"));
		resourceCriteria.add(new LabelKeyValueBean("The current status of the evidence variable", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/EvidenceVariable-status"));
		resourceCriteria.add(new LabelKeyValueBean("What resource is being referenced", "successor", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/EvidenceVariable-successor"));
		resourceCriteria.add(new LabelKeyValueBean("The human-friendly name of the evidence variable", "title", "", "STRING", "http://hl7.org/fhir/SearchParameter/EvidenceVariable-title"));
		resourceCriteria.add(new LabelKeyValueBean("Topics associated with the EvidenceVariable", "topic", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/EvidenceVariable-topic"));
		resourceCriteria.add(new LabelKeyValueBean("The uri that identifies the evidence variable", "url", "", "URI", "http://hl7.org/fhir/SearchParameter/EvidenceVariable-url"));
		resourceCriteria.add(new LabelKeyValueBean("The business version of the evidence variable", "version", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/EvidenceVariable-version"));
		resourceTypeCriteria.put("EvidenceVariable", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("A use context assigned to the example scenario", "context", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ExampleScenario-context"));
		resourceCriteria.add(new LabelKeyValueBean("A quantity- or range-valued use context assigned to the example scenario", "context-quantity", "", "QUANTITY", "http://hl7.org/fhir/SearchParameter/ExampleScenario-context-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A type of use context assigned to the example scenario", "context-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ExampleScenario-context-type"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and quantity- or range-based value assigned to the example scenario", "context-type-quantity", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/ExampleScenario-context-type-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and value assigned to the example scenario", "context-type-value", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/ExampleScenario-context-type-value"));
		resourceCriteria.add(new LabelKeyValueBean("The example scenario publication date", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/ExampleScenario-date"));
		resourceCriteria.add(new LabelKeyValueBean("External identifier for the example scenario", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ExampleScenario-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Intended jurisdiction for the example scenario", "jurisdiction", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ExampleScenario-jurisdiction"));
		resourceCriteria.add(new LabelKeyValueBean("Computationally friendly name of the example scenario", "name", "", "STRING", "http://hl7.org/fhir/SearchParameter/ExampleScenario-name"));
		resourceCriteria.add(new LabelKeyValueBean("Name of the publisher of the example scenario", "publisher", "", "STRING", "http://hl7.org/fhir/SearchParameter/ExampleScenario-publisher"));
		resourceCriteria.add(new LabelKeyValueBean("The current status of the example scenario", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ExampleScenario-status"));
		resourceCriteria.add(new LabelKeyValueBean("The uri that identifies the example scenario", "url", "", "URI", "http://hl7.org/fhir/SearchParameter/ExampleScenario-url"));
		resourceCriteria.add(new LabelKeyValueBean("The business version of the example scenario", "version", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ExampleScenario-version"));
		resourceTypeCriteria.put("ExampleScenario", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Member of the CareTeam", "care-team", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ExplanationOfBenefit-care-team"));
		resourceCriteria.add(new LabelKeyValueBean("The reference to the claim", "claim", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ExplanationOfBenefit-claim", "Claim"));
		resourceCriteria.add(new LabelKeyValueBean("The plan under which the claim was adjudicated", "coverage", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ExplanationOfBenefit-coverage", "Coverage"));
		resourceCriteria.add(new LabelKeyValueBean("The creation date for the EOB", "created", "", "DATE", "http://hl7.org/fhir/SearchParameter/ExplanationOfBenefit-created"));
		resourceCriteria.add(new LabelKeyValueBean("UDI associated with a line item detail product or service", "detail-udi", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ExplanationOfBenefit-detail-udi", "Device"));
		resourceCriteria.add(new LabelKeyValueBean("The contents of the disposition message", "disposition", "", "STRING", "http://hl7.org/fhir/SearchParameter/ExplanationOfBenefit-disposition"));
		resourceCriteria.add(new LabelKeyValueBean("Encounters associated with a billed line item", "encounter", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ExplanationOfBenefit-encounter", "Encounter"));
		resourceCriteria.add(new LabelKeyValueBean("The party responsible for the entry of the Claim", "enterer", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ExplanationOfBenefit-enterer"));
		resourceCriteria.add(new LabelKeyValueBean("Facility responsible for the goods and services", "facility", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ExplanationOfBenefit-facility", "Location"));
		resourceCriteria.add(new LabelKeyValueBean("The business identifier of the Explanation of Benefit", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ExplanationOfBenefit-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("UDI associated with a line item product or service", "item-udi", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ExplanationOfBenefit-item-udi", "Device"));
		resourceCriteria.add(new LabelKeyValueBean("The reference to the patient", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ExplanationOfBenefit-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("The party receiving any payment for the Claim", "payee", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ExplanationOfBenefit-payee"));
		resourceCriteria.add(new LabelKeyValueBean("UDI associated with a procedure", "procedure-udi", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ExplanationOfBenefit-procedure-udi", "Device"));
		resourceCriteria.add(new LabelKeyValueBean("The reference to the provider", "provider", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ExplanationOfBenefit-provider"));
		resourceCriteria.add(new LabelKeyValueBean("Status of the instance", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ExplanationOfBenefit-status"));
		resourceCriteria.add(new LabelKeyValueBean("UDI associated with a line item detail subdetail product or service", "subdetail-udi", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ExplanationOfBenefit-subdetail-udi", "Device"));
		resourceTypeCriteria.put("ExplanationOfBenefit", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("A search by a condition code", "code", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-code"));
		resourceCriteria.add(new LabelKeyValueBean("When history was recorded or last updated", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/clinical-date"));
		resourceCriteria.add(new LabelKeyValueBean("A search by a record identifier", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Instantiates FHIR protocol or definition", "instantiates-canonical", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/FamilyMemberHistory-instantiates-canonical"));
		resourceCriteria.add(new LabelKeyValueBean("Instantiates external protocol or definition", "instantiates-uri", "", "URI", "http://hl7.org/fhir/SearchParameter/FamilyMemberHistory-instantiates-uri"));
		resourceCriteria.add(new LabelKeyValueBean("The identity of a subject to list family member history items for", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("A search by a relationship type", "relationship", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/FamilyMemberHistory-relationship"));
		resourceCriteria.add(new LabelKeyValueBean("A search by a sex code of a family member", "sex", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/FamilyMemberHistory-sex"));
		resourceCriteria.add(new LabelKeyValueBean("partial | completed | entered-in-error | health-unknown", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/FamilyMemberHistory-status"));
		resourceTypeCriteria.put("FamilyMemberHistory", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Flag creator", "author", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Flag-author"));
		resourceCriteria.add(new LabelKeyValueBean("Time period when flag is active", "date", "", "PERIOD", "http://hl7.org/fhir/SearchParameter/clinical-date"));
		resourceCriteria.add(new LabelKeyValueBean("Alert relevant during encounter", "encounter", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-encounter", "Encounter"));
		resourceCriteria.add(new LabelKeyValueBean("Business identifier", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Flag-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("The identity of a subject to list flags for", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("The identity of a subject to list flags for", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Flag-subject"));
		resourceTypeCriteria.put("Flag", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("in-progress | improving | worsening | no-change | achieved | sustaining | not-achieved | no-progress | not-attainable", "achievement-status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Goal-achievement-status"));
		resourceCriteria.add(new LabelKeyValueBean("E.g. Treatment, dietary, behavioral, etc.", "category", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Goal-category"));
		resourceCriteria.add(new LabelKeyValueBean("External Ids for this goal", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("proposed | planned | accepted | active | on-hold | completed | cancelled | entered-in-error | rejected", "lifecycle-status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Goal-lifecycle-status"));
		resourceCriteria.add(new LabelKeyValueBean("Who this goal is intended for", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("When goal pursuit begins", "start-date", "", "DATE", "http://hl7.org/fhir/SearchParameter/Goal-start-date"));
		resourceCriteria.add(new LabelKeyValueBean("Who this goal is intended for", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Goal-subject"));
		resourceCriteria.add(new LabelKeyValueBean("Reach goal on or before", "target-date", "", "DATE", "http://hl7.org/fhir/SearchParameter/Goal-target-date"));
		resourceTypeCriteria.put("Goal", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("A use context assigned to the graph definition", "context", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-context"));
		resourceCriteria.add(new LabelKeyValueBean("A quantity- or range-valued use context assigned to the graph definition", "context-quantity", "", "QUANTITY", "http://hl7.org/fhir/SearchParameter/conformance-context-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A type of use context assigned to the graph definition", "context-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-context-type"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and quantity- or range-based value assigned to the graph definition", "context-type-quantity", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/conformance-context-type-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and value assigned to the graph definition", "context-type-value", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/conformance-context-type-value"));
		resourceCriteria.add(new LabelKeyValueBean("The graph definition publication date", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/conformance-date"));
		resourceCriteria.add(new LabelKeyValueBean("The description of the graph definition", "description", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-description"));
		resourceCriteria.add(new LabelKeyValueBean("Intended jurisdiction for the graph definition", "jurisdiction", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-jurisdiction"));
		resourceCriteria.add(new LabelKeyValueBean("Computationally friendly name of the graph definition", "name", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-name"));
		resourceCriteria.add(new LabelKeyValueBean("Name of the publisher of the graph definition", "publisher", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-publisher"));
		resourceCriteria.add(new LabelKeyValueBean("Type of resource at which the graph starts", "start", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/GraphDefinition-start"));
		resourceCriteria.add(new LabelKeyValueBean("The current status of the graph definition", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-status"));
		resourceCriteria.add(new LabelKeyValueBean("The uri that identifies the graph definition", "url", "", "URI", "http://hl7.org/fhir/SearchParameter/conformance-url"));
		resourceCriteria.add(new LabelKeyValueBean("The business version of the graph definition", "version", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-version"));
		resourceTypeCriteria.put("GraphDefinition", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Descriptive or actual", "actual", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Group-actual"));
		resourceCriteria.add(new LabelKeyValueBean("Kind of characteristic", "characteristic", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Group-characteristic"));
		resourceCriteria.add(new LabelKeyValueBean("A composite of both characteristic and value", "characteristic-value", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/Group-characteristic-value"));
		resourceCriteria.add(new LabelKeyValueBean("The kind of resources contained", "code", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Group-code"));
		resourceCriteria.add(new LabelKeyValueBean("Group includes or excludes", "exclude", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Group-exclude"));
		resourceCriteria.add(new LabelKeyValueBean("Unique id", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Group-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Entity that is the custodian of the Group's definition", "managing-entity", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Group-managing-entity"));
		resourceCriteria.add(new LabelKeyValueBean("Reference to the group member", "member", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Group-member"));
		// AEGIS Defined Search Parameter member-period (used for $everything)
		resourceCriteria.add(new LabelKeyValueBean("Period member belonged to the group", "member-period", "", "PERIOD"));
		resourceCriteria.add(new LabelKeyValueBean("The type of resources the group contains", "type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Group-type"));
		resourceCriteria.add(new LabelKeyValueBean("Value held by characteristic", "value", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Group-value"));
		resourceTypeCriteria.put("Group", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("The identifier of the guidance response", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/GuidanceResponse-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("The identity of a patient to search for guidance response results", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/GuidanceResponse-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("The identifier of the request associated with the response", "request", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/GuidanceResponse-request"));
		resourceCriteria.add(new LabelKeyValueBean("The subject that the guidance response is about", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/GuidanceResponse-subject"));
		resourceTypeCriteria.put("GuidanceResponse", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("The Healthcare Service is currently marked as active", "active", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/HealthcareService-active"));
		resourceCriteria.add(new LabelKeyValueBean("One of the HealthcareService's characteristics", "characteristic", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/HealthcareService-characteristic"));
		resourceCriteria.add(new LabelKeyValueBean("Location(s) service is intended for/available to", "coverage-area", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/HealthcareService-coverage-area", "Location"));
		resourceCriteria.add(new LabelKeyValueBean("Technical endpoints providing access to electronic services operated for the healthcare service", "endpoint", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/HealthcareService-endpoint", "Endpoint"));
		resourceCriteria.add(new LabelKeyValueBean("External identifiers for this item", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/HealthcareService-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("The location of the Healthcare Service", "location", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/HealthcareService-location", "Location"));
		resourceCriteria.add(new LabelKeyValueBean("A portion of the Healthcare service name", "name", "", "STRING", "http://hl7.org/fhir/SearchParameter/HealthcareService-name"));
		resourceCriteria.add(new LabelKeyValueBean("The organization that provides this Healthcare Service", "organization", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/HealthcareService-organization", "Organization"));
		resourceCriteria.add(new LabelKeyValueBean("One of the Programs supported by this HealthcareService", "program", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/HealthcareService-program"));
		resourceCriteria.add(new LabelKeyValueBean("Service Category of the Healthcare Service", "service-category", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/HealthcareService-service-category"));
		resourceCriteria.add(new LabelKeyValueBean("The type of service provided by this healthcare service", "service-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/HealthcareService-service-type"));
		resourceCriteria.add(new LabelKeyValueBean("The specialty of the service provided by this healthcare service", "specialty", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/HealthcareService-specialty"));
		resourceTypeCriteria.put("HealthcareService", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("The order for the image", "basedon", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ImagingStudy-basedon"));
		resourceCriteria.add(new LabelKeyValueBean("The body site studied", "bodysite", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ImagingStudy-bodysite"));
		resourceCriteria.add(new LabelKeyValueBean("The type of the instance", "dicom-class", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ImagingStudy-dicom-class"));
		resourceCriteria.add(new LabelKeyValueBean("The context of the study", "encounter", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ImagingStudy-encounter", "Encounter"));
		resourceCriteria.add(new LabelKeyValueBean("The endpoint for the study or series", "endpoint", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ImagingStudy-endpoint", "Endpoint"));
		resourceCriteria.add(new LabelKeyValueBean("Identifiers for the Study, such as DICOM Study Instance UID and Accession number", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("SOP Instance UID for an instance", "instance", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ImagingStudy-instance"));
		resourceCriteria.add(new LabelKeyValueBean("Who interpreted the images", "interpreter", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ImagingStudy-interpreter"));
		resourceCriteria.add(new LabelKeyValueBean("The modality of the series", "modality", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ImagingStudy-modality"));
		resourceCriteria.add(new LabelKeyValueBean("Who the study is about", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("The person who performed the study", "performer", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ImagingStudy-performer"));
		resourceCriteria.add(new LabelKeyValueBean("The reason for the study", "reason", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ImagingStudy-reason"));
		resourceCriteria.add(new LabelKeyValueBean("The referring physician", "referrer", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ImagingStudy-referrer"));
		resourceCriteria.add(new LabelKeyValueBean("DICOM Series Instance UID for a series", "series", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ImagingStudy-series"));
		resourceCriteria.add(new LabelKeyValueBean("When the study was started", "started", "", "DATE", "http://hl7.org/fhir/SearchParameter/ImagingStudy-started"));
		resourceCriteria.add(new LabelKeyValueBean("The status of the study", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ImagingStudy-status"));
		resourceCriteria.add(new LabelKeyValueBean("Who the study is about", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ImagingStudy-subject"));
		resourceTypeCriteria.put("ImagingStudy", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Vaccination  (non)-Administration Date", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/clinical-date"));
		resourceCriteria.add(new LabelKeyValueBean("Business identifier", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("The service delivery location or facility in which the vaccine was / was to be administered", "location", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Immunization-location", "Location"));
		resourceCriteria.add(new LabelKeyValueBean("Vaccine Lot Number", "lot-number", "", "STRING", "http://hl7.org/fhir/SearchParameter/Immunization-lot-number"));
		resourceCriteria.add(new LabelKeyValueBean("Vaccine Manufacturer", "manufacturer", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Immunization-manufacturer", "Organization"));
		resourceCriteria.add(new LabelKeyValueBean("The patient for the vaccination record", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("The practitioner or organization who played a role in the vaccination", "performer", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Immunization-performer"));
		resourceCriteria.add(new LabelKeyValueBean("Additional information on reaction", "reaction", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Immunization-reaction", "Observation"));
		resourceCriteria.add(new LabelKeyValueBean("When reaction started", "reaction-date", "", "DATE", "http://hl7.org/fhir/SearchParameter/Immunization-reaction-date"));
		resourceCriteria.add(new LabelKeyValueBean("Reason why the vaccine was administered", "reason-code", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Immunization-reason-code"));
		resourceCriteria.add(new LabelKeyValueBean("Why immunization occurred", "reason-reference", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Immunization-reason-reference"));
		resourceCriteria.add(new LabelKeyValueBean("The series being followed by the provider", "series", "", "STRING", "http://hl7.org/fhir/SearchParameter/Immunization-series"));
		resourceCriteria.add(new LabelKeyValueBean("Immunization event status", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Immunization-status"));
		resourceCriteria.add(new LabelKeyValueBean("Reason why the vaccine was not administered", "status-reason", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Immunization-status-reason"));
		resourceCriteria.add(new LabelKeyValueBean("The target disease the dose is being administered against", "target-disease", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Immunization-target-disease"));
		resourceCriteria.add(new LabelKeyValueBean("Vaccine Product Administered", "vaccine-code", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Immunization-vaccine-code"));
		resourceTypeCriteria.put("Immunization", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Date the evaluation was generated", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/ImmunizationEvaluation-date"));
		resourceCriteria.add(new LabelKeyValueBean("The status of the dose relative to published recommendations", "dose-status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ImmunizationEvaluation-dose-status"));
		resourceCriteria.add(new LabelKeyValueBean("ID of the evaluation", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ImmunizationEvaluation-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("The vaccine administration event being evaluated", "immunization-event", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ImmunizationEvaluation-immunization-event", "Immunization"));
		resourceCriteria.add(new LabelKeyValueBean("The patient being evaluated", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ImmunizationEvaluation-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("Immunization evaluation status", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ImmunizationEvaluation-status"));
		resourceCriteria.add(new LabelKeyValueBean("The vaccine preventable disease being evaluated against", "target-disease", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ImmunizationEvaluation-target-disease"));
		resourceTypeCriteria.put("ImmunizationEvaluation", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Date recommendation(s) created", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/ImmunizationRecommendation-date"));
		resourceCriteria.add(new LabelKeyValueBean("Business identifier", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ImmunizationRecommendation-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Patient observations supporting recommendation", "information", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ImmunizationRecommendation-information"));
		resourceCriteria.add(new LabelKeyValueBean("Who this profile is for", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ImmunizationRecommendation-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("Vaccine recommendation status", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ImmunizationRecommendation-status"));
		resourceCriteria.add(new LabelKeyValueBean("Past immunizations supporting recommendation", "support", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ImmunizationRecommendation-support"));
		resourceCriteria.add(new LabelKeyValueBean("Disease to be immunized against", "target-disease", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ImmunizationRecommendation-target-disease"));
		resourceCriteria.add(new LabelKeyValueBean("Vaccine  or vaccine group recommendation applies to", "vaccine-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ImmunizationRecommendation-vaccine-type"));
		resourceTypeCriteria.put("ImmunizationRecommendation", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("A use context assigned to the implementation guide", "context", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-context"));
		resourceCriteria.add(new LabelKeyValueBean("A quantity- or range-valued use context assigned to the implementation guide", "context-quantity", "", "QUANTITY", "http://hl7.org/fhir/SearchParameter/conformance-context-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A type of use context assigned to the implementation guide", "context-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-context-type"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and quantity- or range-based value assigned to the implementation guide", "context-type-quantity", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/conformance-context-type-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and value assigned to the implementation guide", "context-type-value", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/conformance-context-type-value"));
		resourceCriteria.add(new LabelKeyValueBean("The implementation guide publication date", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/conformance-date"));
		resourceCriteria.add(new LabelKeyValueBean("Identity of the IG that this depends on", "depends-on", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ImplementationGuide-depends-on", "ImplementationGuide"));
		resourceCriteria.add(new LabelKeyValueBean("The description of the implementation guide", "description", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-description"));
		resourceCriteria.add(new LabelKeyValueBean("For testing purposes, not real usage", "experimental", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ImplementationGuide-experimental"));
		resourceCriteria.add(new LabelKeyValueBean("Profile that all resources must conform to", "global", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ImplementationGuide-global", "StructureDefinition"));
		resourceCriteria.add(new LabelKeyValueBean("Intended jurisdiction for the implementation guide", "jurisdiction", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-jurisdiction"));
		resourceCriteria.add(new LabelKeyValueBean("Computationally friendly name of the implementation guide", "name", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-name"));
		resourceCriteria.add(new LabelKeyValueBean("Name of the publisher of the implementation guide", "publisher", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-publisher"));
		resourceCriteria.add(new LabelKeyValueBean("Location of the resource", "resource", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ImplementationGuide-resource"));
		resourceCriteria.add(new LabelKeyValueBean("The current status of the implementation guide", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-status"));
		resourceCriteria.add(new LabelKeyValueBean("The human-friendly name of the implementation guide", "title", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-title"));
		resourceCriteria.add(new LabelKeyValueBean("The uri that identifies the implementation guide", "url", "", "URI", "http://hl7.org/fhir/SearchParameter/conformance-url"));
		resourceCriteria.add(new LabelKeyValueBean("The business version of the implementation guide", "version", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-version"));
		resourceTypeCriteria.put("ImplementationGuide", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("A server defined search that may match any of the string fields in the Address, including line, city, district, state, country, postalCode, and/or text", "address", "", "STRING", "http://hl7.org/fhir/SearchParameter/InsurancePlan-address"));
		resourceCriteria.add(new LabelKeyValueBean("A city specified in an address", "address-city", "", "STRING", "http://hl7.org/fhir/SearchParameter/InsurancePlan-address-city"));
		resourceCriteria.add(new LabelKeyValueBean("A country specified in an address", "address-country", "", "STRING", "http://hl7.org/fhir/SearchParameter/InsurancePlan-address-country"));
		// AEGIS Extra Search Parameter address-district
		resourceCriteria.add(new LabelKeyValueBean("A district specified in an address", "address-district", "", "STRING"));
		resourceCriteria.add(new LabelKeyValueBean("A postal code specified in an address", "address-postalcode", "", "STRING", "http://hl7.org/fhir/SearchParameter/InsurancePlan-address-postalcode"));
		resourceCriteria.add(new LabelKeyValueBean("A state specified in an address", "address-state", "", "STRING", "http://hl7.org/fhir/SearchParameter/InsurancePlan-address-state"));
		resourceCriteria.add(new LabelKeyValueBean("A use code specified in an address", "address-use", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/InsurancePlan-address-use"));
		resourceCriteria.add(new LabelKeyValueBean("Product administrator", "administered-by", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/InsurancePlan-administered-by", "Organization"));
		resourceCriteria.add(new LabelKeyValueBean("Technical endpoint", "endpoint", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/InsurancePlan-endpoint", "Endpoint"));
		resourceCriteria.add(new LabelKeyValueBean("Any identifier for the organization (not the accreditation issuer's identifier)", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/InsurancePlan-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("A portion of the organization's name or alias", "name", "", "STRING", "http://hl7.org/fhir/SearchParameter/InsurancePlan-name"));
		resourceCriteria.add(new LabelKeyValueBean("An organization of which this organization forms a part", "owned-by", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/InsurancePlan-owned-by", "Organization"));
		resourceCriteria.add(new LabelKeyValueBean("A portion of the organization's name using some kind of phonetic matching algorithm", "phonetic", "", "STRING", "http://hl7.org/fhir/SearchParameter/InsurancePlan-phonetic"));
		resourceCriteria.add(new LabelKeyValueBean("Is the Organization record active", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/InsurancePlan-status"));
		resourceCriteria.add(new LabelKeyValueBean("A code for the type of organization", "type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/InsurancePlan-type"));
		resourceTypeCriteria.put("InsurancePlan", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Account that is being balanced", "account", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Invoice-account", "Account"));
		resourceCriteria.add(new LabelKeyValueBean("Invoice date / posting date", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/Invoice-date"));
		resourceCriteria.add(new LabelKeyValueBean("Business Identifier for item", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Invoice-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Issuing Organization of Invoice", "issuer", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Invoice-issuer", "Organization"));
		resourceCriteria.add(new LabelKeyValueBean("Individual who was involved", "participant", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Invoice-participant"));
		resourceCriteria.add(new LabelKeyValueBean("Type of involvement in creation of this Invoice", "participant-role", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Invoice-participant-role"));
		resourceCriteria.add(new LabelKeyValueBean("Recipient(s) of goods and services", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Invoice-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("Recipient of this invoice", "recipient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Invoice-recipient"));
		resourceCriteria.add(new LabelKeyValueBean("draft | issued | balanced | cancelled | entered-in-error", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Invoice-status"));
		resourceCriteria.add(new LabelKeyValueBean("Recipient(s) of goods and services", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Invoice-subject"));
		resourceCriteria.add(new LabelKeyValueBean("Gross total of this Invoice", "totalgross", "", "QUANTITY", "http://hl7.org/fhir/SearchParameter/Invoice-totalgross"));
		resourceCriteria.add(new LabelKeyValueBean("Net total of this Invoice", "totalnet", "", "QUANTITY", "http://hl7.org/fhir/SearchParameter/Invoice-totalnet"));
		resourceCriteria.add(new LabelKeyValueBean("Type of Invoice", "type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Invoice-type"));
		resourceTypeCriteria.put("Invoice", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("What resource is being referenced", "composed-of", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Library-composed-of"));
		resourceCriteria.add(new LabelKeyValueBean("The type of content in the library (e.g. text/cql)", "content-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Library-content-type"));
		resourceCriteria.add(new LabelKeyValueBean("A use context assigned to the library", "context", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Library-context"));
		resourceCriteria.add(new LabelKeyValueBean("A quantity- or range-valued use context assigned to the library", "context-quantity", "", "QUANTITY", "http://hl7.org/fhir/SearchParameter/Library-context-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A type of use context assigned to the library", "context-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Library-context-type"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and quantity- or range-based value assigned to the library", "context-type-quantity", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/Library-context-type-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and value assigned to the library", "context-type-value", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/Library-context-type-value"));
		resourceCriteria.add(new LabelKeyValueBean("The library publication date", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/Library-date"));
		resourceCriteria.add(new LabelKeyValueBean("What resource is being referenced", "depends-on", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Library-depends-on"));
		resourceCriteria.add(new LabelKeyValueBean("What resource is being referenced", "derived-from", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Library-derived-from"));
		resourceCriteria.add(new LabelKeyValueBean("The description of the library", "description", "", "STRING", "http://hl7.org/fhir/SearchParameter/Library-description"));
		resourceCriteria.add(new LabelKeyValueBean("The time during which the library is intended to be in use", "effective", "", "PERIOD", "http://hl7.org/fhir/SearchParameter/Library-effective"));
		resourceCriteria.add(new LabelKeyValueBean("External identifier for the library", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Library-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Intended jurisdiction for the library", "jurisdiction", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Library-jurisdiction"));
		resourceCriteria.add(new LabelKeyValueBean("Computationally friendly name of the library", "name", "", "STRING", "http://hl7.org/fhir/SearchParameter/Library-name"));
		resourceCriteria.add(new LabelKeyValueBean("What resource is being referenced", "predecessor", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Library-predecessor"));
		resourceCriteria.add(new LabelKeyValueBean("Name of the publisher of the library", "publisher", "", "STRING", "http://hl7.org/fhir/SearchParameter/Library-publisher"));
		resourceCriteria.add(new LabelKeyValueBean("The current status of the library", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Library-status"));
		resourceCriteria.add(new LabelKeyValueBean("What resource is being referenced", "successor", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Library-successor"));
		resourceCriteria.add(new LabelKeyValueBean("The human-friendly name of the library", "title", "", "STRING", "http://hl7.org/fhir/SearchParameter/Library-title"));
		resourceCriteria.add(new LabelKeyValueBean("Topics associated with the module", "topic", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Library-topic"));
		resourceCriteria.add(new LabelKeyValueBean("The type of the library (e.g. logic-library, model-definition, asset-collection, module-definition)", "type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Library-type"));
		resourceCriteria.add(new LabelKeyValueBean("The uri that identifies the library", "url", "", "URI", "http://hl7.org/fhir/SearchParameter/Library-url"));
		resourceCriteria.add(new LabelKeyValueBean("The business version of the library", "version", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Library-version"));
		resourceTypeCriteria.put("Library", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Author of the Linkage", "author", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Linkage-author"));
		resourceCriteria.add(new LabelKeyValueBean("Matches on any item in the Linkage", "item", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Linkage-item"));
		resourceCriteria.add(new LabelKeyValueBean("Matches on any item in the Linkage with a type of 'source'", "source", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Linkage-source"));
		resourceTypeCriteria.put("Linkage", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("What the purpose of this list is", "code", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-code"));
		resourceCriteria.add(new LabelKeyValueBean("When the list was prepared", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/clinical-date"));
		resourceCriteria.add(new LabelKeyValueBean("Why list is empty", "empty-reason", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/List-empty-reason"));
		resourceCriteria.add(new LabelKeyValueBean("Context in which list created", "encounter", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-encounter", "Encounter"));
		resourceCriteria.add(new LabelKeyValueBean("Business identifier", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Actual entry", "item", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/List-item"));
		resourceCriteria.add(new LabelKeyValueBean("The annotation  - text content (as markdown)", "notes", "", "STRING", "http://hl7.org/fhir/SearchParameter/List-notes"));
		resourceCriteria.add(new LabelKeyValueBean("If all resources have the same subject", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("Who and/or what defined the list contents (aka Author)", "source", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/List-source"));
		resourceCriteria.add(new LabelKeyValueBean("current | retired | entered-in-error", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/List-status"));
		resourceCriteria.add(new LabelKeyValueBean("If all resources have the same subject", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/List-subject"));
		resourceCriteria.add(new LabelKeyValueBean("Descriptive name for the list", "title", "", "STRING", "http://hl7.org/fhir/SearchParameter/List-title"));
		resourceTypeCriteria.put("List", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("A (part of the) address of the location", "address", "", "STRING", "http://hl7.org/fhir/SearchParameter/Location-address"));
		resourceCriteria.add(new LabelKeyValueBean("A city specified in an address", "address-city", "", "STRING", "http://hl7.org/fhir/SearchParameter/Location-address-city"));
		resourceCriteria.add(new LabelKeyValueBean("A country specified in an address", "address-country", "", "STRING", "http://hl7.org/fhir/SearchParameter/Location-address-country"));
		// AEGIS Extra Search Parameter address-district
		resourceCriteria.add(new LabelKeyValueBean("A district specified in an address", "address-district", "", "STRING"));
		resourceCriteria.add(new LabelKeyValueBean("A postal code specified in an address", "address-postalcode", "", "STRING", "http://hl7.org/fhir/SearchParameter/Location-address-postalcode"));
		resourceCriteria.add(new LabelKeyValueBean("A state specified in an address", "address-state", "", "STRING", "http://hl7.org/fhir/SearchParameter/Location-address-state"));
		resourceCriteria.add(new LabelKeyValueBean("A use code specified in an address", "address-use", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Location-address-use"));
		resourceCriteria.add(new LabelKeyValueBean("Technical endpoints providing access to services operated for the location", "endpoint", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Location-endpoint", "Endpoint"));
		resourceCriteria.add(new LabelKeyValueBean("An identifier for the location", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Location-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("A portion of the location's name or alias", "name", "", "STRING", "http://hl7.org/fhir/SearchParameter/Location-name"));
		// Geospatial Search Criteria
		resourceCriteria.add(new LabelKeyValueBean("Search for locations where the location.position is near to, or within a specified distance of, the provided coordinates expressed as [latitude]|[longitude]|[distance]|[units] (using the WGS84 datum, see notes).", "near", "", "SPECIAL", "http://hl7.org/fhir/SearchParameter/Location-near"));
		resourceCriteria.add(new LabelKeyValueBean("Searches for locations (typically bed/room) that have an operational status (e.g. contaminated, housekeeping)", "operational-status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Location-operational-status"));
		resourceCriteria.add(new LabelKeyValueBean("Searches for locations that are managed by the provided organization", "organization", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Location-organization", "Organization"));
		resourceCriteria.add(new LabelKeyValueBean("A location of which this location is a part", "partof", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Location-partof", "Location"));
		resourceCriteria.add(new LabelKeyValueBean("Searches for locations with a specific kind of status", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Location-status"));
		resourceCriteria.add(new LabelKeyValueBean("A code for the type of location", "type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Location-type"));
		resourceTypeCriteria.put("Location", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("What resource is being referenced", "composed-of", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Measure-composed-of"));
		resourceCriteria.add(new LabelKeyValueBean("A use context assigned to the measure", "context", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Measure-context"));
		resourceCriteria.add(new LabelKeyValueBean("A quantity- or range-valued use context assigned to the measure", "context-quantity", "", "QUANTITY", "http://hl7.org/fhir/SearchParameter/Measure-context-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A type of use context assigned to the measure", "context-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Measure-context-type"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and quantity- or range-based value assigned to the measure", "context-type-quantity", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/Measure-context-type-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and value assigned to the measure", "context-type-value", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/Measure-context-type-value"));
		resourceCriteria.add(new LabelKeyValueBean("The measure publication date", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/Measure-date"));
		resourceCriteria.add(new LabelKeyValueBean("What resource is being referenced", "depends-on", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Measure-depends-on"));
		resourceCriteria.add(new LabelKeyValueBean("What resource is being referenced", "derived-from", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Measure-derived-from"));
		resourceCriteria.add(new LabelKeyValueBean("The description of the measure", "description", "", "STRING", "http://hl7.org/fhir/SearchParameter/Measure-description"));
		resourceCriteria.add(new LabelKeyValueBean("The time during which the measure is intended to be in use", "effective", "", "PERIOD", "http://hl7.org/fhir/SearchParameter/Measure-effective"));
		resourceCriteria.add(new LabelKeyValueBean("External identifier for the measure", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Measure-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Intended jurisdiction for the measure", "jurisdiction", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Measure-jurisdiction"));
		resourceCriteria.add(new LabelKeyValueBean("Computationally friendly name of the measure", "name", "", "STRING", "http://hl7.org/fhir/SearchParameter/Measure-name"));
		resourceCriteria.add(new LabelKeyValueBean("What resource is being referenced", "predecessor", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Measure-predecessor"));
		resourceCriteria.add(new LabelKeyValueBean("Name of the publisher of the measure", "publisher", "", "STRING", "http://hl7.org/fhir/SearchParameter/Measure-publisher"));
		resourceCriteria.add(new LabelKeyValueBean("The current status of the measure", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Measure-status"));
		resourceCriteria.add(new LabelKeyValueBean("What resource is being referenced", "successor", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Measure-successor"));
		resourceCriteria.add(new LabelKeyValueBean("The human-friendly name of the measure", "title", "", "STRING", "http://hl7.org/fhir/SearchParameter/Measure-title"));
		resourceCriteria.add(new LabelKeyValueBean("Topics associated with the measure", "topic", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Measure-topic"));
		resourceCriteria.add(new LabelKeyValueBean("The uri that identifies the measure", "url", "", "URI", "http://hl7.org/fhir/SearchParameter/Measure-url"));
		resourceCriteria.add(new LabelKeyValueBean("The business version of the measure", "version", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Measure-version"));
		resourceTypeCriteria.put("Measure", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("The date of the measure report", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/MeasureReport-date"));
		resourceCriteria.add(new LabelKeyValueBean("An evaluated resource referenced by the measure report", "evaluated-resource", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/MeasureReport-evaluated-resource"));
		resourceCriteria.add(new LabelKeyValueBean("External identifier of the measure report to be returned", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/MeasureReport-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("The measure to return measure report results for", "measure", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/MeasureReport-measure", "Measure"));
		resourceCriteria.add(new LabelKeyValueBean("The identity of a patient to search for individual measure report results for", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/MeasureReport-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("The period of the measure report", "period", "", "PERIOD", "http://hl7.org/fhir/SearchParameter/MeasureReport-period"));
		resourceCriteria.add(new LabelKeyValueBean("The reporter to return measure report results for", "reporter", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/MeasureReport-reporter"));
		resourceCriteria.add(new LabelKeyValueBean("The status of the measure report", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/MeasureReport-status"));
		resourceCriteria.add(new LabelKeyValueBean("The identity of a subject to search for individual measure report results for", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/MeasureReport-subject"));
		resourceTypeCriteria.put("MeasureReport", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Procedure that caused this media to be created", "based-on", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Media-based-on"));
		resourceCriteria.add(new LabelKeyValueBean("When Media was collected", "created", "", "DATE", "http://hl7.org/fhir/SearchParameter/Media-created"));
		resourceCriteria.add(new LabelKeyValueBean("Observing Device", "device", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Media-device"));
		resourceCriteria.add(new LabelKeyValueBean("Encounter associated with media", "encounter", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Media-encounter", "Encounter"));
		resourceCriteria.add(new LabelKeyValueBean("Identifier(s) for the image", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Media-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("The type of acquisition equipment/process", "modality", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Media-modality"));
		resourceCriteria.add(new LabelKeyValueBean("The person who generated the image", "operator", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Media-operator"));
		resourceCriteria.add(new LabelKeyValueBean("Who/What this Media is a record of", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Media-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("Observed body part", "site", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Media-site"));
		resourceCriteria.add(new LabelKeyValueBean("preparation | in-progress | not-done | suspended | aborted | completed | entered-in-error | unknown", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Media-status"));
		resourceCriteria.add(new LabelKeyValueBean("Who/What this Media is a record of", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Media-subject"));
		resourceCriteria.add(new LabelKeyValueBean("Classification of media as image, video, or audio", "type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Media-type"));
		resourceCriteria.add(new LabelKeyValueBean("Imaging view, e.g. Lateral or Antero-posterior", "view", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Media-view"));
		resourceTypeCriteria.put("Media", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Returns medications for a specific code", "code", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-code"));
		resourceCriteria.add(new LabelKeyValueBean("Returns medications in a batch with this expiration date", "expiration-date", "", "DATE", "http://hl7.org/fhir/SearchParameter/Medication-expiration-date"));
		resourceCriteria.add(new LabelKeyValueBean("Returns medications for a specific dose form", "form", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Medication-form"));
		resourceCriteria.add(new LabelKeyValueBean("Returns medications with this external identifier", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Medication-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Returns medications for this ingredient reference", "ingredient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Medication-ingredient"));
		resourceCriteria.add(new LabelKeyValueBean("Returns medications for this ingredient code", "ingredient-code", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Medication-ingredient-code"));
		resourceCriteria.add(new LabelKeyValueBean("Returns medications in a batch with this lot number", "lot-number", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Medication-lot-number"));
		resourceCriteria.add(new LabelKeyValueBean("Returns medications made or sold for this manufacturer", "manufacturer", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Medication-manufacturer", "Organization"));
		resourceCriteria.add(new LabelKeyValueBean("Returns medications for this status", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Medication-status"));
		resourceTypeCriteria.put("Medication", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Return administrations of this medication code", "code", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-code"));
		resourceCriteria.add(new LabelKeyValueBean("Return administrations that share this encounter or episode of care", "context", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/MedicationAdministration-context"));
		resourceCriteria.add(new LabelKeyValueBean("Return administrations with this administration device identity", "device", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/MedicationAdministration-device", "Device"));
		resourceCriteria.add(new LabelKeyValueBean("Date administration happened (or did not happen)", "effective-time", "", "DATE", "http://hl7.org/fhir/SearchParameter/MedicationAdministration-effective-time"));
		resourceCriteria.add(new LabelKeyValueBean("Return administrations with this external identifier", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Return administrations of this medication resource", "medication", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/medications-medication", "Medication"));
		resourceCriteria.add(new LabelKeyValueBean("The identity of a patient to list administrations  for", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("The identity of the individual who administered the medication", "performer", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/MedicationAdministration-performer"));
		resourceCriteria.add(new LabelKeyValueBean("Reasons for administering the medication", "reason-given", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/MedicationAdministration-reason-given"));
		resourceCriteria.add(new LabelKeyValueBean("Reasons for not administering the medication", "reason-not-given", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/MedicationAdministration-reason-not-given"));
		resourceCriteria.add(new LabelKeyValueBean("The identity of a request to list administrations from", "request", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/MedicationAdministration-request"));
		resourceCriteria.add(new LabelKeyValueBean("MedicationAdministration event status (for example one of active/paused/completed/nullified)", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/medications-status"));
		resourceCriteria.add(new LabelKeyValueBean("The identity of the individual or group to list administrations for", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/MedicationAdministration-subject"));
		resourceTypeCriteria.put("MedicationAdministration", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Returns dispenses of this medicine code", "code", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-code"));
		resourceCriteria.add(new LabelKeyValueBean("Returns dispenses with a specific context (episode or episode of care)", "context", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/MedicationDispense-context"));
		resourceCriteria.add(new LabelKeyValueBean("Returns dispenses that should be sent to a specific destination", "destination", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/MedicationDispense-destination", "Location"));
		resourceCriteria.add(new LabelKeyValueBean("Returns dispenses with this external identifier", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Returns dispenses of this medicine resource", "medication", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/medications-medication", "Medication"));
		resourceCriteria.add(new LabelKeyValueBean("The identity of a patient to list dispenses  for", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("Returns dispenses performed by a specific individual", "performer", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/MedicationDispense-performer"));
		resourceCriteria.add(new LabelKeyValueBean("The identity of a prescription to list dispenses from", "prescription", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/medications-prescription", "MedicationRequest"));
		resourceCriteria.add(new LabelKeyValueBean("The identity of a receiver to list dispenses for", "receiver", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/MedicationDispense-receiver"));
		resourceCriteria.add(new LabelKeyValueBean("Returns dispenses with the specified responsible party", "responsibleparty", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/MedicationDispense-responsibleparty"));
		resourceCriteria.add(new LabelKeyValueBean("Returns dispenses with a specified dispense status", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/medications-status"));
		resourceCriteria.add(new LabelKeyValueBean("The identity of a patient for whom to list dispenses", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/MedicationDispense-subject"));
		resourceCriteria.add(new LabelKeyValueBean("Returns dispenses of a specific type", "type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/MedicationDispense-type"));
		resourceCriteria.add(new LabelKeyValueBean("Returns dispenses handed over on this date", "whenhandedover", "", "DATE", "http://hl7.org/fhir/SearchParameter/MedicationDispense-whenhandedover"));
		resourceCriteria.add(new LabelKeyValueBean("Returns dispenses prepared on this date", "whenprepared", "", "DATE", "http://hl7.org/fhir/SearchParameter/MedicationDispense-whenprepared"));
		resourceTypeCriteria.put("MedicationDispense", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Specific category assigned to the medication", "classification", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/MedicationKnowledge-classification"));
		resourceCriteria.add(new LabelKeyValueBean("The type of category for the medication (for example, therapeutic classification, therapeutic sub-classification)", "classification-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/MedicationKnowledge-classification-type"));
		resourceCriteria.add(new LabelKeyValueBean("Code that identifies this medication", "code", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/MedicationKnowledge-code"));
		resourceCriteria.add(new LabelKeyValueBean("powder | tablets | capsule +", "doseform", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/MedicationKnowledge-doseform"));
		resourceCriteria.add(new LabelKeyValueBean("Medication(s) or substance(s) contained in the medication", "ingredient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/MedicationKnowledge-ingredient", "Substance"));
		resourceCriteria.add(new LabelKeyValueBean("Medication(s) or substance(s) contained in the medication", "ingredient-code", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/MedicationKnowledge-ingredient-code"));
		resourceCriteria.add(new LabelKeyValueBean("Manufacturer of the item", "manufacturer", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/MedicationKnowledge-manufacturer", "Organization"));
		resourceCriteria.add(new LabelKeyValueBean("Name of the reviewing program", "monitoring-program-name", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/MedicationKnowledge-monitoring-program-name"));
		resourceCriteria.add(new LabelKeyValueBean("Type of program under which the medication is monitored", "monitoring-program-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/MedicationKnowledge-monitoring-program-type"));
		resourceCriteria.add(new LabelKeyValueBean("Associated documentation about the medication", "monograph", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/MedicationKnowledge-monograph"));
		resourceCriteria.add(new LabelKeyValueBean("The category of medication document", "monograph-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/MedicationKnowledge-monograph-type"));
		resourceCriteria.add(new LabelKeyValueBean("The source or owner for the price information", "source-cost", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/MedicationKnowledge-source-cost"));
		resourceCriteria.add(new LabelKeyValueBean("active | inactive | entered-in-error", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/MedicationKnowledge-status"));
		resourceTypeCriteria.put("MedicationKnowledge", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Return prescriptions written on this date", "authoredon", "", "DATE", "http://hl7.org/fhir/SearchParameter/MedicationRequest-authoredon"));
		resourceCriteria.add(new LabelKeyValueBean("Returns prescriptions with different categories", "category", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/MedicationRequest-category"));
		resourceCriteria.add(new LabelKeyValueBean("Return prescriptions of this medication code", "code", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-code"));
		resourceCriteria.add(new LabelKeyValueBean("Returns medication request to be administered on a specific date", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/medications-date"));
		resourceCriteria.add(new LabelKeyValueBean("Return prescriptions with this encounter identifier", "encounter", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/medications-encounter", "Encounter"));
		resourceCriteria.add(new LabelKeyValueBean("Return prescriptions with this external identifier", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Returns prescriptions intended to be dispensed by this Organization", "intended-dispenser", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/MedicationRequest-intended-dispenser", "Organization"));
		resourceCriteria.add(new LabelKeyValueBean("Returns the intended performer of the administration of the medication request", "intended-performer", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/MedicationRequest-intended-performer"));
		resourceCriteria.add(new LabelKeyValueBean("Returns requests for a specific type of performer", "intended-performertype", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/MedicationRequest-intended-performertype"));
		resourceCriteria.add(new LabelKeyValueBean("Returns prescriptions with different intents", "intent", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/MedicationRequest-intent"));
		resourceCriteria.add(new LabelKeyValueBean("Return prescriptions for this medication reference", "medication", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/medications-medication", "Medication"));
		resourceCriteria.add(new LabelKeyValueBean("Returns prescriptions for a specific patient", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("Returns prescriptions with different priorities", "priority", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/MedicationRequest-priority"));
		resourceCriteria.add(new LabelKeyValueBean("Returns prescriptions prescribed by this prescriber", "requester", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/MedicationRequest-requester"));
		resourceCriteria.add(new LabelKeyValueBean("Status of the prescription", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/medications-status"));
		resourceCriteria.add(new LabelKeyValueBean("The identity of a patient to list orders  for", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/MedicationRequest-subject"));
		resourceTypeCriteria.put("MedicationRequest", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Returns statements of this category of medicationstatement", "category", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/MedicationStatement-category"));
		resourceCriteria.add(new LabelKeyValueBean("Return statements of this medication code", "code", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-code"));
		resourceCriteria.add(new LabelKeyValueBean("Returns statements for a specific context (episode or episode of Care).", "context", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/MedicationStatement-context"));
		resourceCriteria.add(new LabelKeyValueBean("Date when patient was taking (or not taking) the medication", "effective", "", "DATE", "http://hl7.org/fhir/SearchParameter/MedicationStatement-effective"));
		resourceCriteria.add(new LabelKeyValueBean("Return statements with this external identifier", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Return statements of this medication reference", "medication", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/medications-medication", "Medication"));
		resourceCriteria.add(new LabelKeyValueBean("Returns statements that are part of another event.", "part-of", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/MedicationStatement-part-of"));
		resourceCriteria.add(new LabelKeyValueBean("Returns statements for a specific patient.", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("Who or where the information in the statement came from", "source", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/MedicationStatement-source"));
		resourceCriteria.add(new LabelKeyValueBean("Return statements that match the given status", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/medications-status"));
		resourceCriteria.add(new LabelKeyValueBean("The identity of a patient, animal or group to list statements for", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/MedicationStatement-subject"));
		resourceTypeCriteria.put("MedicationStatement", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Business identifier for this product. Could be an MPID", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/MedicinalProduct-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("The full product name", "name", "", "STRING", "http://hl7.org/fhir/SearchParameter/MedicinalProduct-name"));
		resourceCriteria.add(new LabelKeyValueBean("Language code for this name", "name-language", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/MedicinalProduct-name-language"));
		// AEGIS Defined Search Parameters
		resourceCriteria.add(new LabelKeyValueBean("Regulatory type, e.g Investigational or Authorized", "type", "", "TOKEN"));
		resourceTypeCriteria.put("MedicinalProduct", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("The country in which the marketing authorization has been granted", "country", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/MedicinalProductAuthorization-country"));
		resourceCriteria.add(new LabelKeyValueBean("Marketing Authorization Holder", "holder", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/MedicinalProductAuthorization-holder", "Organization"));
		resourceCriteria.add(new LabelKeyValueBean("Business identifier for the marketing authorization, as assigned by a regulator", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/MedicinalProductAuthorization-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("The status of the marketing authorization", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/MedicinalProductAuthorization-status"));
		resourceCriteria.add(new LabelKeyValueBean("The medicinal product that is being authorized", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/MedicinalProductAuthorization-subject"));
		resourceTypeCriteria.put("MedicinalProductAuthorization", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("The medication for which this is an contraindication", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/MedicinalProductContraindication-subject"));
		// AEGIS Defined Search Parameters
		resourceCriteria.add(new LabelKeyValueBean("The disease, symptom or procedure for the contraindication", "disease", "", "TOKEN"));
		resourceTypeCriteria.put("MedicinalProductContraindication", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("The medication for which this is an indication", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/MedicinalProductIndication-subject"));
		// AEGIS Defined Search Parameters
		resourceCriteria.add(new LabelKeyValueBean("The disease, symptom or procedure that is the indication for treatment", "disease", "", "TOKEN"));
		resourceCriteria.add(new LabelKeyValueBean("The status of the disease or symptom for which the indication applies", "disease-status", "", "TOKEN"));
		resourceTypeCriteria.put("MedicinalProductIndication", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		// AEGIS Defined Search Parameters
		resourceCriteria.add(new LabelKeyValueBean("Identifier for the ingredient", "identifier", "", "TOKEN"));
		resourceCriteria.add(new LabelKeyValueBean("Ingredient role e.g. Active ingredient, excipient", "role", "", "TOKEN"));
		resourceTypeCriteria.put("MedicinalProductIngredient", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("The medication for which this is an interaction", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/MedicinalProductInteraction-subject"));
		// AEGIS Defined Search Parameters
		resourceCriteria.add(new LabelKeyValueBean("The effect of the interaction", "effect", "", "TOKEN"));
		resourceCriteria.add(new LabelKeyValueBean("The type of the interaction", "type", "", "TOKEN"));
		resourceTypeCriteria.put("MedicinalProductInteraction", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		// AEGIS Defined Search Parameters
		resourceCriteria.add(new LabelKeyValueBean("Dose form as manufactured and before any transformation into the pharmaceutical product", "dose-form", "", "TOKEN"));
		resourceCriteria.add(new LabelKeyValueBean("Ingredient of the manufactured item", "ingredient", "", "REFERENCE", null, "MedicinalProductIngredient"));
		resourceCriteria.add(new LabelKeyValueBean("Manufacturer of the item", "manufacturer", "", "REFERENCE", null, "Organization"));
		resourceTypeCriteria.put("MedicinalProductManufactured", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Unique identifier", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/MedicinalProductPackaged-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("The product with this is a pack for", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/MedicinalProductPackaged-subject", "MedicinalProduct"));
		// AEGIS Defined Search Parameters
		resourceCriteria.add(new LabelKeyValueBean("Textual description", "description", "", "STRING"));
		resourceTypeCriteria.put("MedicinalProductPackaged", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("An identifier for the pharmaceutical medicinal product", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/MedicinalProductPharmaceutical-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Coded expression for the route", "route", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/MedicinalProductPharmaceutical-route"));
		resourceCriteria.add(new LabelKeyValueBean("Coded expression for the species", "target-species", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/MedicinalProductPharmaceutical-target-species"));
		// AEGIS Defined Search Parameters
		resourceCriteria.add(new LabelKeyValueBean("Accompanying device", "device", "", "STRING"));
		resourceTypeCriteria.put("MedicinalProductPharmaceutical", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("The medication for which this is an undesirable effect", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/MedicinalProductUndesirableEffect-subject"));
		// AEGIS Defined Search Parameters
		resourceCriteria.add(new LabelKeyValueBean("Classification of the effect", "classification", "", "TOKEN"));
		resourceCriteria.add(new LabelKeyValueBean("The symptom, condition or undesirable effect", "symptom", "", "TOKEN"));
		resourceTypeCriteria.put("MedicinalProductUndesirableEffect", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("The behavior associated with the message", "category", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/MessageDefinition-category"));
		resourceCriteria.add(new LabelKeyValueBean("A use context assigned to the message definition", "context", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-context"));
		resourceCriteria.add(new LabelKeyValueBean("A quantity- or range-valued use context assigned to the message definition", "context-quantity", "", "QUANTITY", "http://hl7.org/fhir/SearchParameter/conformance-context-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A type of use context assigned to the message definition", "context-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-context-type"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and quantity- or range-based value assigned to the message definition", "context-type-quantity", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/conformance-context-type-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and value assigned to the message definition", "context-type-value", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/conformance-context-type-value"));
		resourceCriteria.add(new LabelKeyValueBean("The message definition publication date", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/conformance-date"));
		resourceCriteria.add(new LabelKeyValueBean("The description of the message definition", "description", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-description"));
		resourceCriteria.add(new LabelKeyValueBean("The event that triggers the message or link to the event definition.", "event", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/MessageDefinition-event"));
		resourceCriteria.add(new LabelKeyValueBean("A resource that is a permitted focus of the message", "focus", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/MessageDefinition-focus"));
		resourceCriteria.add(new LabelKeyValueBean("External identifier for the message definition", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Intended jurisdiction for the message definition", "jurisdiction", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-jurisdiction"));
		resourceCriteria.add(new LabelKeyValueBean("Computationally friendly name of the message definition", "name", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-name"));
		resourceCriteria.add(new LabelKeyValueBean("A resource that is the parent of the definition", "parent", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/MessageDefinition-parent"));
		resourceCriteria.add(new LabelKeyValueBean("Name of the publisher of the message definition", "publisher", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-publisher"));
		resourceCriteria.add(new LabelKeyValueBean("The current status of the message definition", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-status"));
		resourceCriteria.add(new LabelKeyValueBean("The human-friendly name of the message definition", "title", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-title"));
		resourceCriteria.add(new LabelKeyValueBean("The uri that identifies the message definition", "url", "", "URI", "http://hl7.org/fhir/SearchParameter/conformance-url"));
		resourceCriteria.add(new LabelKeyValueBean("The business version of the message definition", "version", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-version"));
		resourceTypeCriteria.put("MessageDefinition", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("The source of the decision", "author", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/MessageHeader-author"));
		resourceCriteria.add(new LabelKeyValueBean("ok | transient-error | fatal-error", "code", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/MessageHeader-code"));
		resourceCriteria.add(new LabelKeyValueBean("Name of system", "destination", "", "STRING", "http://hl7.org/fhir/SearchParameter/MessageHeader-destination"));
		resourceCriteria.add(new LabelKeyValueBean("Actual destination address or id", "destination-uri", "", "URI", "http://hl7.org/fhir/SearchParameter/MessageHeader-destination-uri"));
		resourceCriteria.add(new LabelKeyValueBean("The source of the data entry", "enterer", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/MessageHeader-enterer"));
		resourceCriteria.add(new LabelKeyValueBean("Code for the event this message represents or link to event definition", "event", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/MessageHeader-event"));
		resourceCriteria.add(new LabelKeyValueBean("The actual content of the message", "focus", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/MessageHeader-focus"));
		resourceCriteria.add(new LabelKeyValueBean("Intended \"real-world\" recipient for the data", "receiver", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/MessageHeader-receiver"));
		resourceCriteria.add(new LabelKeyValueBean("Id of original message", "response-id", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/MessageHeader-response-id"));
		resourceCriteria.add(new LabelKeyValueBean("Final responsibility for event", "responsible", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/MessageHeader-responsible"));
		resourceCriteria.add(new LabelKeyValueBean("Real world sender of the message", "sender", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/MessageHeader-sender"));
		resourceCriteria.add(new LabelKeyValueBean("Name of system", "source", "", "STRING", "http://hl7.org/fhir/SearchParameter/MessageHeader-source"));
		resourceCriteria.add(new LabelKeyValueBean("Actual message source address or id", "source-uri", "", "URI", "http://hl7.org/fhir/SearchParameter/MessageHeader-source-uri"));
		resourceCriteria.add(new LabelKeyValueBean("Particular delivery destination within the destination", "target", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/MessageHeader-target", "Device"));
		resourceTypeCriteria.put("MessageHeader", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Chromosome number of the reference sequence", "chromosome", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/MolecularSequence-chromosome"));
		resourceCriteria.add(new LabelKeyValueBean("Search parameter by chromosome and variant coordinate. This will refer to part of a locus or part of a gene where search region will be represented in 1-based system. Since the coordinateSystem can either be 0-based or 1-based, this search query will include the result of both coordinateSystem that contains the equivalent segment of the gene or whole genome sequence. For example, a search for sequence can be represented as `chromosome-variant-coordinate=1$lt345$gt123`, this means it will search for the MolecularSequence resource with variants on chromosome 1 and with position >123 and <345, where in 1-based system resource, all strings within region 1:124-344 will be revealed, while in 0-based system resource, all strings within region 1:123-344 will be revealed. You may want to check detail about 0-based v.s. 1-based above.", "chromosome-variant-coordinate", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/MolecularSequence-chromosome-variant-coordinate"));
		resourceCriteria.add(new LabelKeyValueBean("Search parameter by chromosome and window. This will refer to part of a locus or part of a gene where search region will be represented in 1-based system. Since the coordinateSystem can either be 0-based or 1-based, this search query will include the result of both coordinateSystem that contains the equivalent segment of the gene or whole genome sequence. For example, a search for sequence can be represented as `chromosome-window-coordinate=1$lt345$gt123`, this means it will search for the MolecularSequence resource with a window on chromosome 1 and with position >123 and <345, where in 1-based system resource, all strings within region 1:124-344 will be revealed, while in 0-based system resource, all strings within region 1:123-344 will be revealed. You may want to check detail about 0-based v.s. 1-based above.", "chromosome-window-coordinate", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/MolecularSequence-chromosome-window-coordinate"));
		resourceCriteria.add(new LabelKeyValueBean("The unique identity for a particular sequence", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/MolecularSequence-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("The subject that the observation is about", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/MolecularSequence-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("Reference Sequence of the sequence", "referenceseqid", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/MolecularSequence-referenceseqid"));
		resourceCriteria.add(new LabelKeyValueBean("Search parameter by reference sequence and variant coordinate. This will refer to part of a locus or part of a gene where search region will be represented in 1-based system. Since the coordinateSystem can either be 0-based or 1-based, this search query will include the result of both coordinateSystem that contains the equivalent segment of the gene or whole genome sequence. For example, a search for sequence can be represented as `referenceSeqId-variant-coordinate=NC_000001.11$lt345$gt123`, this means it will search for the MolecularSequence resource with variants on NC_000001.11 and with position >123 and <345, where in 1-based system resource, all strings within region NC_000001.11:124-344 will be revealed, while in 0-based system resource, all strings within region NC_000001.11:123-344 will be revealed. You may want to check detail about 0-based v.s. 1-based above.", "referenceseqid-variant-coordinate", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/MolecularSequence-referenceseqid-variant-coordinate"));
		resourceCriteria.add(new LabelKeyValueBean("Search parameter by reference sequence and window. This will refer to part of a locus or part of a gene where search region will be represented in 1-based system. Since the coordinateSystem can either be 0-based or 1-based, this search query will include the result of both coordinateSystem that contains the equivalent segment of the gene or whole genome sequence. For example, a search for sequence can be represented as `referenceSeqId-window-coordinate=NC_000001.11$lt345$gt123`, this means it will search for the MolecularSequence resource with a window on NC_000001.11 and with position >123 and <345, where in 1-based system resource, all strings within region NC_000001.11:124-344 will be revealed, while in 0-based system resource, all strings within region NC_000001.11:123-344 will be revealed. You may want to check detail about 0-based v.s. 1-based above.", "referenceseqid-window-coordinate", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/MolecularSequence-referenceseqid-window-coordinate"));
		resourceCriteria.add(new LabelKeyValueBean("Amino Acid Sequence/ DNA Sequence / RNA Sequence", "type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/MolecularSequence-type"));
		resourceCriteria.add(new LabelKeyValueBean("End position (0-based exclusive, which menas the acid at this position will not be included, 1-based inclusive, which means the acid at this position will be included) of the variant.", "variant-end", "", "NUMBER", "http://hl7.org/fhir/SearchParameter/MolecularSequence-variant-end"));
		resourceCriteria.add(new LabelKeyValueBean("Start position (0-based inclusive, 1-based inclusive, that means the nucleic acid or amino acid at this position will be included) of the variant.", "variant-start", "", "NUMBER", "http://hl7.org/fhir/SearchParameter/MolecularSequence-variant-start"));
		resourceCriteria.add(new LabelKeyValueBean("End position (0-based exclusive, which menas the acid at this position will not be included, 1-based inclusive, which means the acid at this position will be included) of the reference sequence.", "window-end", "", "NUMBER", "http://hl7.org/fhir/SearchParameter/MolecularSequence-window-end"));
		resourceCriteria.add(new LabelKeyValueBean("Start position (0-based inclusive, 1-based inclusive, that means the nucleic acid or amino acid at this position will be included) of the reference sequence.", "window-start", "", "NUMBER", "http://hl7.org/fhir/SearchParameter/MolecularSequence-window-start"));
		resourceTypeCriteria.put("MolecularSequence", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Name of an individual to contact", "contact", "", "STRING", "http://hl7.org/fhir/SearchParameter/NamingSystem-contact"));
		resourceCriteria.add(new LabelKeyValueBean("A use context assigned to the naming system", "context", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-context"));
		resourceCriteria.add(new LabelKeyValueBean("A quantity- or range-valued use context assigned to the naming system", "context-quantity", "", "QUANTITY", "http://hl7.org/fhir/SearchParameter/conformance-context-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A type of use context assigned to the naming system", "context-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-context-type"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and quantity- or range-based value assigned to the naming system", "context-type-quantity", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/conformance-context-type-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and value assigned to the naming system", "context-type-value", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/conformance-context-type-value"));
		resourceCriteria.add(new LabelKeyValueBean("The naming system publication date", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/conformance-date"));
		resourceCriteria.add(new LabelKeyValueBean("The description of the naming system", "description", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-description"));
		resourceCriteria.add(new LabelKeyValueBean("oid | uuid | uri | other", "id-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/NamingSystem-id-type"));
		resourceCriteria.add(new LabelKeyValueBean("Intended jurisdiction for the naming system", "jurisdiction", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-jurisdiction"));
		resourceCriteria.add(new LabelKeyValueBean("codesystem | identifier | root", "kind", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/NamingSystem-kind"));
		resourceCriteria.add(new LabelKeyValueBean("Computationally friendly name of the naming system", "name", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-name"));
		resourceCriteria.add(new LabelKeyValueBean("When is identifier valid?", "period", "", "PERIOD", "http://hl7.org/fhir/SearchParameter/NamingSystem-period"));
		resourceCriteria.add(new LabelKeyValueBean("Name of the publisher of the naming system", "publisher", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-publisher"));
		resourceCriteria.add(new LabelKeyValueBean("Who maintains system namespace?", "responsible", "", "STRING", "http://hl7.org/fhir/SearchParameter/NamingSystem-responsible"));
		resourceCriteria.add(new LabelKeyValueBean("The current status of the naming system", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-status"));
		resourceCriteria.add(new LabelKeyValueBean("Contact details for individual or organization", "telecom", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/NamingSystem-telecom"));
		resourceCriteria.add(new LabelKeyValueBean("e.g. driver,  provider,  patient, bank etc.", "type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/NamingSystem-type"));
		resourceCriteria.add(new LabelKeyValueBean("The unique identifier", "value", "", "STRING", "http://hl7.org/fhir/SearchParameter/NamingSystem-value"));
		resourceTypeCriteria.put("NamingSystem", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Type of module component to add to the feeding", "additive", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/NutritionOrder-additive"));
		resourceCriteria.add(new LabelKeyValueBean("Return nutrition orders requested on this date", "datetime", "", "DATE", "http://hl7.org/fhir/SearchParameter/NutritionOrder-datetime"));
		resourceCriteria.add(new LabelKeyValueBean("Return nutrition orders with this encounter identifier", "encounter", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-encounter", "Encounter"));
		resourceCriteria.add(new LabelKeyValueBean("Type of enteral or infant formula", "formula", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/NutritionOrder-formula"));
		resourceCriteria.add(new LabelKeyValueBean("Return nutrition orders with this external identifier", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Instantiates FHIR protocol or definition", "instantiates-canonical", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/NutritionOrder-instantiates-canonical"));
		resourceCriteria.add(new LabelKeyValueBean("Instantiates external protocol or definition", "instantiates-uri", "", "URI", "http://hl7.org/fhir/SearchParameter/NutritionOrder-instantiates-uri"));
		resourceCriteria.add(new LabelKeyValueBean("Type of diet that can be consumed orally (i.e., take via the mouth).", "oraldiet", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/NutritionOrder-oraldiet"));
		resourceCriteria.add(new LabelKeyValueBean("The identity of the person who requires the diet, formula or nutritional supplement", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("The identity of the provider who placed the nutrition order", "provider", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/NutritionOrder-provider"));
		resourceCriteria.add(new LabelKeyValueBean("Status of the nutrition order.", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/NutritionOrder-status"));
		resourceCriteria.add(new LabelKeyValueBean("Type of supplement product requested", "supplement", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/NutritionOrder-supplement"));
		resourceTypeCriteria.put("NutritionOrder", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Reference to the service request.", "based-on", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Observation-based-on"));
		resourceCriteria.add(new LabelKeyValueBean("The classification of the type of observation", "category", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Observation-category"));
		resourceCriteria.add(new LabelKeyValueBean("The code of the observation type", "code", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-code"));
		resourceCriteria.add(new LabelKeyValueBean("Code and coded value parameter pair", "code-value-concept", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/Observation-code-value-concept"));
		resourceCriteria.add(new LabelKeyValueBean("Code and date/time value parameter pair", "code-value-date", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/Observation-code-value-date"));
		resourceCriteria.add(new LabelKeyValueBean("Code and quantity value parameter pair", "code-value-quantity", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/Observation-code-value-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("Code and string value parameter pair", "code-value-string", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/Observation-code-value-string"));
		resourceCriteria.add(new LabelKeyValueBean("The code of the observation type or component type", "combo-code", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Observation-combo-code"));
		resourceCriteria.add(new LabelKeyValueBean("Code and coded value parameter pair, including in components", "combo-code-value-concept", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/Observation-combo-code-value-concept"));
		resourceCriteria.add(new LabelKeyValueBean("Code and quantity value parameter pair, including in components", "combo-code-value-quantity", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/Observation-combo-code-value-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("The reason why the expected value in the element Observation.value[x] or Observation.component.value[x] is missing.", "combo-data-absent-reason", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Observation-combo-data-absent-reason"));
		resourceCriteria.add(new LabelKeyValueBean("The value or component value of the observation, if the value is a CodeableConcept", "combo-value-concept", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Observation-combo-value-concept"));
		resourceCriteria.add(new LabelKeyValueBean("The value or component value of the observation, if the value is a Quantity, or a SampledData (just search on the bounds of the values in sampled data)", "combo-value-quantity", "", "QUANTITY", "http://hl7.org/fhir/SearchParameter/Observation-combo-value-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("The component code of the observation type", "component-code", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Observation-component-code"));
		resourceCriteria.add(new LabelKeyValueBean("Component code and component coded value parameter pair", "component-code-value-concept", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/Observation-component-code-value-concept"));
		resourceCriteria.add(new LabelKeyValueBean("Component code and component quantity value parameter pair", "component-code-value-quantity", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/Observation-component-code-value-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("The reason why the expected value in the element Observation.component.value[x] is missing.", "component-data-absent-reason", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Observation-component-data-absent-reason"));
		resourceCriteria.add(new LabelKeyValueBean("The value of the component observation, if the value is a CodeableConcept", "component-value-concept", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Observation-component-value-concept"));
		resourceCriteria.add(new LabelKeyValueBean("The value of the component observation, if the value is a Quantity, or a SampledData (just search on the bounds of the values in sampled data)", "component-value-quantity", "", "QUANTITY", "http://hl7.org/fhir/SearchParameter/Observation-component-value-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("The reason why the expected value in the element Observation.value[x] is missing.", "data-absent-reason", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Observation-data-absent-reason"));
		resourceCriteria.add(new LabelKeyValueBean("Obtained date/time. If the obtained element is a period, a date that falls in the period", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/clinical-date"));
		resourceCriteria.add(new LabelKeyValueBean("Related measurements the observation is made from", "derived-from", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Observation-derived-from"));
		resourceCriteria.add(new LabelKeyValueBean("The Device that generated the observation data.", "device", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Observation-device"));
		resourceCriteria.add(new LabelKeyValueBean("Encounter related to the observation", "encounter", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-encounter", "Encounter"));
		resourceCriteria.add(new LabelKeyValueBean("The focus of an observation when the focus is not the patient of record.", "focus", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Observation-focus"));
		resourceCriteria.add(new LabelKeyValueBean("Related resource that belongs to the Observation group", "has-member", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Observation-has-member"));
		resourceCriteria.add(new LabelKeyValueBean("The unique id for a particular observation", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("The method used for the observation", "method", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Observation-method"));
		resourceCriteria.add(new LabelKeyValueBean("Part of referenced event", "part-of", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Observation-part-of"));
		resourceCriteria.add(new LabelKeyValueBean("The subject that the observation is about (if patient)", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("Who performed the observation", "performer", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Observation-performer"));
		resourceCriteria.add(new LabelKeyValueBean("Specimen used for this observation", "specimen", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Observation-specimen", "Specimen"));
		resourceCriteria.add(new LabelKeyValueBean("The status of the observation", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Observation-status"));
		resourceCriteria.add(new LabelKeyValueBean("The subject that the observation is about", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Observation-subject"));
		resourceCriteria.add(new LabelKeyValueBean("The value of the observation, if the value is a CodeableConcept", "value-concept", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Observation-value-concept"));
		resourceCriteria.add(new LabelKeyValueBean("The value of the observation, if the value is a date or period of time", "value-date", "", "DATE", "http://hl7.org/fhir/SearchParameter/Observation-value-date"));
		resourceCriteria.add(new LabelKeyValueBean("The value of the observation, if the value is a Quantity, or a SampledData (just search on the bounds of the values in sampled data)", "value-quantity", "", "QUANTITY", "http://hl7.org/fhir/SearchParameter/Observation-value-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("The value of the observation, if the value is a string, and also searches in CodeableConcept.text", "value-string", "", "STRING", "http://hl7.org/fhir/SearchParameter/Observation-value-string"));
		// Observation $lastn support
		resourceCriteria.add(new LabelKeyValueBean("Maximum number of observations to return", "max", "", "NUMBER", ""));
		resourceTypeCriteria.put("Observation", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		// AEGIS Defined Search Parameters
		resourceCriteria.add(new LabelKeyValueBean("Category of observation", "category", "", "TOKEN"));
		resourceCriteria.add(new LabelKeyValueBean("Type of observation (code / type)", "code", "", "TOKEN"));
		resourceCriteria.add(new LabelKeyValueBean("The method or technique used to perform the observation", "method", "", "TOKEN"));
		resourceTypeCriteria.put("ObservationDefinition", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Marks this as a profile of the base", "base", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/OperationDefinition-base", "OperationDefinition"));
		resourceCriteria.add(new LabelKeyValueBean("Name used to invoke the operation", "code", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/OperationDefinition-code"));
		resourceCriteria.add(new LabelKeyValueBean("A use context assigned to the operation definition", "context", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-context"));
		resourceCriteria.add(new LabelKeyValueBean("A quantity- or range-valued use context assigned to the operation definition", "context-quantity", "", "QUANTITY", "http://hl7.org/fhir/SearchParameter/conformance-context-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A type of use context assigned to the operation definition", "context-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-context-type"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and quantity- or range-based value assigned to the operation definition", "context-type-quantity", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/conformance-context-type-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and value assigned to the operation definition", "context-type-value", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/conformance-context-type-value"));
		resourceCriteria.add(new LabelKeyValueBean("The operation definition publication date", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/conformance-date"));
		resourceCriteria.add(new LabelKeyValueBean("The description of the operation definition", "description", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-description"));
		resourceCriteria.add(new LabelKeyValueBean("Validation information for in parameters", "input-profile", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/OperationDefinition-input-profile", "StructureDefinition"));
		resourceCriteria.add(new LabelKeyValueBean("Invoke on an instance?", "instance", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/OperationDefinition-instance"));
		resourceCriteria.add(new LabelKeyValueBean("Intended jurisdiction for the operation definition", "jurisdiction", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-jurisdiction"));
		resourceCriteria.add(new LabelKeyValueBean("operation | query", "kind", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/OperationDefinition-kind"));
		resourceCriteria.add(new LabelKeyValueBean("Computationally friendly name of the operation definition", "name", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-name"));
		resourceCriteria.add(new LabelKeyValueBean("Validation information for out parameters", "output-profile", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/OperationDefinition-output-profile", "StructureDefinition"));
		resourceCriteria.add(new LabelKeyValueBean("Name of the publisher of the operation definition", "publisher", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-publisher"));
		resourceCriteria.add(new LabelKeyValueBean("The current status of the operation definition", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-status"));
		resourceCriteria.add(new LabelKeyValueBean("Invoke at the system level?", "system", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/OperationDefinition-system"));
		resourceCriteria.add(new LabelKeyValueBean("The human-friendly name of the operation definition", "title", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-title"));
		resourceCriteria.add(new LabelKeyValueBean("Invoke at the type level?", "type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/OperationDefinition-type"));
		resourceCriteria.add(new LabelKeyValueBean("The uri that identifies the operation definition", "url", "", "URI", "http://hl7.org/fhir/SearchParameter/conformance-url"));
		resourceCriteria.add(new LabelKeyValueBean("The business version of the operation definition", "version", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-version"));
		resourceTypeCriteria.put("OperationDefinition", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceTypeCriteria.put("OperationOutcome", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Is the Organization record active", "active", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Organization-active"));
		resourceCriteria.add(new LabelKeyValueBean("A server defined search that may match any of the string fields in the Address, including line, city, district, state, country, postalCode, and/or text", "address", "", "STRING", "http://hl7.org/fhir/SearchParameter/Organization-address"));
		resourceCriteria.add(new LabelKeyValueBean("A city specified in an address", "address-city", "", "STRING", "http://hl7.org/fhir/SearchParameter/Organization-address-city"));
		resourceCriteria.add(new LabelKeyValueBean("A country specified in an address", "address-country", "", "STRING", "http://hl7.org/fhir/SearchParameter/Organization-address-country"));
		// AEGIS Extra Search Parameter address-district
		resourceCriteria.add(new LabelKeyValueBean("A district specified in an address", "address-district", "", "STRING"));
		resourceCriteria.add(new LabelKeyValueBean("A postal code specified in an address", "address-postalcode", "", "STRING", "http://hl7.org/fhir/SearchParameter/Organization-address-postalcode"));
		resourceCriteria.add(new LabelKeyValueBean("A state specified in an address", "address-state", "", "STRING", "http://hl7.org/fhir/SearchParameter/Organization-address-state"));
		resourceCriteria.add(new LabelKeyValueBean("A use code specified in an address", "address-use", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Organization-address-use"));
		resourceCriteria.add(new LabelKeyValueBean("Technical endpoints providing access to services operated for the organization", "endpoint", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Organization-endpoint", "Endpoint"));
		resourceCriteria.add(new LabelKeyValueBean("Any identifier for the organization (not the accreditation issuer's identifier)", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Organization-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("A portion of the organization's name or alias", "name", "", "STRING", "http://hl7.org/fhir/SearchParameter/Organization-name"));
		resourceCriteria.add(new LabelKeyValueBean("An organization of which this organization forms a part", "partof", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Organization-partof", "Organization"));
		resourceCriteria.add(new LabelKeyValueBean("A portion of the organization's name using some kind of phonetic matching algorithm", "phonetic", "", "STRING", "http://hl7.org/fhir/SearchParameter/Organization-phonetic"));
		resourceCriteria.add(new LabelKeyValueBean("A code for the type of organization", "type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Organization-type"));
		resourceTypeCriteria.put("Organization", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Whether this organization affiliation record is in active use", "active", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/OrganizationAffiliation-active"));
		resourceCriteria.add(new LabelKeyValueBean("The period during which the participatingOrganization is affiliated with the primary organization", "date", "", "PERIOD", "http://hl7.org/fhir/SearchParameter/OrganizationAffiliation-date"));
		resourceCriteria.add(new LabelKeyValueBean("A value in an email contact", "email", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/OrganizationAffiliation-email"));
		resourceCriteria.add(new LabelKeyValueBean("Technical endpoints providing access to services operated for this role", "endpoint", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/OrganizationAffiliation-endpoint", "Endpoint"));
		resourceCriteria.add(new LabelKeyValueBean("An organization affiliation's Identifier", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/OrganizationAffiliation-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("The location(s) at which the role occurs", "location", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/OrganizationAffiliation-location", "Location"));
		resourceCriteria.add(new LabelKeyValueBean("Health insurance provider network in which the participatingOrganization provides the role's services (if defined) at the indicated locations (if defined)", "network", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/OrganizationAffiliation-network", "Organization"));
		resourceCriteria.add(new LabelKeyValueBean("The organization that provides services to the primary organization", "participating-organization", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/OrganizationAffiliation-participating-organization", "Organization"));
		resourceCriteria.add(new LabelKeyValueBean("A value in a phone contact", "phone", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/OrganizationAffiliation-phone"));
		resourceCriteria.add(new LabelKeyValueBean("The organization that receives the services from the participating organization", "primary-organization", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/OrganizationAffiliation-primary-organization", "Organization"));
		resourceCriteria.add(new LabelKeyValueBean("Definition of the role the participatingOrganization plays", "role", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/OrganizationAffiliation-role"));
		resourceCriteria.add(new LabelKeyValueBean("Healthcare services provided through the role", "service", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/OrganizationAffiliation-service", "HealthcareService"));
		resourceCriteria.add(new LabelKeyValueBean("Specific specialty of the participatingOrganization in the context of the role", "specialty", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/OrganizationAffiliation-specialty"));
		resourceCriteria.add(new LabelKeyValueBean("The value in any kind of contact", "telecom", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/OrganizationAffiliation-telecom"));
		resourceTypeCriteria.put("OrganizationAffiliation", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Whether the patient record is active", "active", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Patient-active"));
		resourceCriteria.add(new LabelKeyValueBean("A server defined search that may match any of the string fields in the Address, including line, city, district, state, country, postalCode, and/or text", "address", "", "STRING", "http://hl7.org/fhir/SearchParameter/individual-address"));
		resourceCriteria.add(new LabelKeyValueBean("A city specified in an address", "address-city", "", "STRING", "http://hl7.org/fhir/SearchParameter/individual-address-city"));
		resourceCriteria.add(new LabelKeyValueBean("A country specified in an address", "address-country", "", "STRING", "http://hl7.org/fhir/SearchParameter/individual-address-country"));
		// AEGIS Extra Search Parameter address-district
		resourceCriteria.add(new LabelKeyValueBean("A district specified in an address", "address-district", "", "STRING"));
		resourceCriteria.add(new LabelKeyValueBean("A postalCode specified in an address", "address-postalcode", "", "STRING", "http://hl7.org/fhir/SearchParameter/individual-address-postalcode"));
		resourceCriteria.add(new LabelKeyValueBean("A state specified in an address", "address-state", "", "STRING", "http://hl7.org/fhir/SearchParameter/individual-address-state"));
		resourceCriteria.add(new LabelKeyValueBean("A use code specified in an address", "address-use", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/individual-address-use"));
		resourceCriteria.add(new LabelKeyValueBean("The patient's date of birth", "birthdate", "", "DATE", "http://hl7.org/fhir/SearchParameter/individual-birthdate"));
		resourceCriteria.add(new LabelKeyValueBean("The date of death has been provided and satisfies this search value", "death-date", "", "DATE", "http://hl7.org/fhir/SearchParameter/Patient-death-date"));
		resourceCriteria.add(new LabelKeyValueBean("This patient has been marked as deceased, or as a death date entered", "deceased", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Patient-deceased"));
		resourceCriteria.add(new LabelKeyValueBean("A value in an email contact", "email", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/individual-email"));
		resourceCriteria.add(new LabelKeyValueBean("A portion of the family name of the patient", "family", "", "STRING", "http://hl7.org/fhir/SearchParameter/individual-family"));
		resourceCriteria.add(new LabelKeyValueBean("Gender of the patient", "gender", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/individual-gender"));
		resourceCriteria.add(new LabelKeyValueBean("Patient's nominated general practitioner, not the organization that manages the record", "general-practitioner", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Patient-general-practitioner"));
		resourceCriteria.add(new LabelKeyValueBean("A portion of the given name of the patient", "given", "", "STRING", "http://hl7.org/fhir/SearchParameter/individual-given"));
		resourceCriteria.add(new LabelKeyValueBean("A patient identifier", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Patient-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Language code (irrespective of use value)", "language", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Patient-language"));
		resourceCriteria.add(new LabelKeyValueBean("All patients linked to the given patient", "link", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Patient-link"));
		resourceCriteria.add(new LabelKeyValueBean("A server defined search that may match any of the string fields in the HumanName, including family, give, prefix, suffix, suffix, and/or text", "name", "", "STRING", "http://hl7.org/fhir/SearchParameter/Patient-name"));
		resourceCriteria.add(new LabelKeyValueBean("The organization that is the custodian of the patient record", "organization", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Patient-organization", "Organization"));
		resourceCriteria.add(new LabelKeyValueBean("A value in a phone contact", "phone", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/individual-phone"));
		resourceCriteria.add(new LabelKeyValueBean("A portion of either family or given name using some kind of phonetic matching algorithm", "phonetic", "", "STRING", "http://hl7.org/fhir/SearchParameter/individual-phonetic"));
		resourceCriteria.add(new LabelKeyValueBean("The value in any kind of telecom details of the patient", "telecom", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/individual-telecom"));
		// Extensions - US-Core: United States Realm FHIR Profiles
		resourceCriteria.add(new LabelKeyValueBean("Returns patients with an ethnicity extension matching the specified code", "ethnicity", "", "TOKEN", "http://hl7.org/fhir/us/core/SearchParameter/us-core-ethnicity"));
		resourceCriteria.add(new LabelKeyValueBean("Search based on patient's mother's maiden name", "mothersMaidenName", "", "STRING", "http://hl7.org/fhir/SearchParameter/patient-extensions-Patient-mothersMaidenName"));
		resourceCriteria.add(new LabelKeyValueBean("Returns patients with a race extension matching the specified code", "race", "", "TOKEN", "http://hl7.org/fhir/us/core/SearchParameter/us-core-race"));
		resourceTypeCriteria.put("Patient", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Creation date fro the notice", "created", "", "DATE", "http://hl7.org/fhir/SearchParameter/PaymentNotice-created"));
		resourceCriteria.add(new LabelKeyValueBean("The business identifier of the notice", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/PaymentNotice-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("The type of payment notice", "payment-status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/PaymentNotice-payment-status"));
		resourceCriteria.add(new LabelKeyValueBean("The reference to the provider", "provider", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/PaymentNotice-provider"));
		resourceCriteria.add(new LabelKeyValueBean("The Claim", "request", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/PaymentNotice-request"));
		resourceCriteria.add(new LabelKeyValueBean("The ClaimResponse", "response", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/PaymentNotice-response"));
		resourceCriteria.add(new LabelKeyValueBean("The status of the payment notice", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/PaymentNotice-status"));
		resourceTypeCriteria.put("PaymentNotice", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("The creation date", "created", "", "DATE", "http://hl7.org/fhir/SearchParameter/PaymentReconciliation-created"));
		resourceCriteria.add(new LabelKeyValueBean("The contents of the disposition message", "disposition", "", "STRING", "http://hl7.org/fhir/SearchParameter/PaymentReconciliation-disposition"));
		resourceCriteria.add(new LabelKeyValueBean("The business identifier of the ExplanationOfBenefit", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/PaymentReconciliation-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("The processing outcome", "outcome", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/PaymentReconciliation-outcome"));
		resourceCriteria.add(new LabelKeyValueBean("The organization which generated this resource", "payment-issuer", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/PaymentReconciliation-payment-issuer", "Organiztaion"));
		resourceCriteria.add(new LabelKeyValueBean("The reference to the claim", "request", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/PaymentReconciliation-request", "Task"));
		resourceCriteria.add(new LabelKeyValueBean("The reference to the provider who submitted the claim", "requestor", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/PaymentReconciliation-requestor"));
		resourceCriteria.add(new LabelKeyValueBean("The status of the payment reconciliation", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/PaymentReconciliation-status"));
		resourceTypeCriteria.put("PaymentReconciliation", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("A server defined search that may match any of the string fields in the Address, including line, city, district, state, country, postalCode, and/or text", "address", "", "STRING", "http://hl7.org/fhir/SearchParameter/individual-address"));
		resourceCriteria.add(new LabelKeyValueBean("A city specified in an address", "address-city", "", "STRING", "http://hl7.org/fhir/SearchParameter/individual-address-city"));
		resourceCriteria.add(new LabelKeyValueBean("A country specified in an address", "address-country", "", "STRING", "http://hl7.org/fhir/SearchParameter/individual-address-country"));
		// AEGIS Extra Search Parameter address-district
		resourceCriteria.add(new LabelKeyValueBean("A district specified in an address", "address-district", "", "STRING"));
		resourceCriteria.add(new LabelKeyValueBean("A postal code specified in an address", "address-postalcode", "", "STRING", "http://hl7.org/fhir/SearchParameter/individual-address-postalcode"));
		resourceCriteria.add(new LabelKeyValueBean("A state specified in an address", "address-state", "", "STRING", "http://hl7.org/fhir/SearchParameter/individual-address-state"));
		resourceCriteria.add(new LabelKeyValueBean("A use code specified in an address", "address-use", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/individual-address-use"));
		resourceCriteria.add(new LabelKeyValueBean("The person's date of birth", "birthdate", "", "DATE", "http://hl7.org/fhir/SearchParameter/individual-birthdate"));
		resourceCriteria.add(new LabelKeyValueBean("A value in an email contact", "email", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/individual-email"));
		resourceCriteria.add(new LabelKeyValueBean("The gender of the person", "gender", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/individual-gender"));
		resourceCriteria.add(new LabelKeyValueBean("A person Identifier", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Person-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Any link has this Patient, Person, RelatedPerson or Practitioner reference", "link", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Person-link"));
		resourceCriteria.add(new LabelKeyValueBean("A server defined search that may match any of the string fields in the HumanName, including family, give, prefix, suffix, suffix, and/or text", "name", "", "STRING", "http://hl7.org/fhir/SearchParameter/Person-name"));
		resourceCriteria.add(new LabelKeyValueBean("The organization at which this person record is being managed", "organization", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Person-organization", "Organization"));
		resourceCriteria.add(new LabelKeyValueBean("The Person links to this Patient", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Person-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("A value in a phone contact", "phone", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/individual-phone"));
		resourceCriteria.add(new LabelKeyValueBean("A portion of name using some kind of phonetic matching algorithm", "phonetic", "", "STRING", "http://hl7.org/fhir/SearchParameter/individual-phonetic"));
		resourceCriteria.add(new LabelKeyValueBean("The Person links to this Practitioner", "practitioner", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Person-practitioner", "Practitioner"));
		resourceCriteria.add(new LabelKeyValueBean("The Person links to this RelatedPerson", "relatedperson", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Person-relatedperson", "RelatedPerson"));
		resourceCriteria.add(new LabelKeyValueBean("The value in any kind of contact", "telecom", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/individual-telecom"));
		resourceTypeCriteria.put("Person", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("What resource is being referenced", "composed-of", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/PlanDefinition-composed-of"));
		resourceCriteria.add(new LabelKeyValueBean("A use context assigned to the plan definition", "context", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/PlanDefinition-context"));
		resourceCriteria.add(new LabelKeyValueBean("A quantity- or range-valued use context assigned to the plan definition", "context-quantity", "", "QUANTITY", "http://hl7.org/fhir/SearchParameter/PlanDefinition-context-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A type of use context assigned to the plan definition", "context-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/PlanDefinition-context-type"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and quantity- or range-based value assigned to the plan definition", "context-type-quantity", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/PlanDefinition-context-type-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and value assigned to the plan definition", "context-type-value", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/PlanDefinition-context-type-value"));
		resourceCriteria.add(new LabelKeyValueBean("The plan definition publication date", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/PlanDefinition-date"));
		resourceCriteria.add(new LabelKeyValueBean("Activity or plan definitions used by plan definition", "definition", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/PlanDefinition-definition"));
		resourceCriteria.add(new LabelKeyValueBean("What resource is being referenced", "depends-on", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/PlanDefinition-depends-on"));
		resourceCriteria.add(new LabelKeyValueBean("What resource is being referenced", "derived-from", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/PlanDefinition-derived-from"));
		resourceCriteria.add(new LabelKeyValueBean("The description of the plan definition", "description", "", "STRING", "http://hl7.org/fhir/SearchParameter/PlanDefinition-description"));
		resourceCriteria.add(new LabelKeyValueBean("The time during which the plan definition is intended to be in use", "effective", "", "PERIOD", "http://hl7.org/fhir/SearchParameter/PlanDefinition-effective"));
		resourceCriteria.add(new LabelKeyValueBean("External identifier for the plan definition", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/PlanDefinition-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Intended jurisdiction for the plan definition", "jurisdiction", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/PlanDefinition-jurisdiction"));
		resourceCriteria.add(new LabelKeyValueBean("Computationally friendly name of the plan definition", "name", "", "STRING", "http://hl7.org/fhir/SearchParameter/PlanDefinition-name"));
		resourceCriteria.add(new LabelKeyValueBean("What resource is being referenced", "predecessor", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/PlanDefinition-predecessor"));
		resourceCriteria.add(new LabelKeyValueBean("Name of the publisher of the plan definition", "publisher", "", "STRING", "http://hl7.org/fhir/SearchParameter/PlanDefinition-publisher"));
		resourceCriteria.add(new LabelKeyValueBean("The current status of the plan definition", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/PlanDefinition-status"));
		resourceCriteria.add(new LabelKeyValueBean("What resource is being referenced", "successor", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/PlanDefinition-successor"));
		resourceCriteria.add(new LabelKeyValueBean("The human-friendly name of the plan definition", "title", "", "STRING", "http://hl7.org/fhir/SearchParameter/PlanDefinition-title"));
		resourceCriteria.add(new LabelKeyValueBean("Topics associated with the module", "topic", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/PlanDefinition-topic"));
		resourceCriteria.add(new LabelKeyValueBean("The type of artifact the plan (e.g. order-set, eca-rule, protocol)", "type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/PlanDefinition-type"));
		resourceCriteria.add(new LabelKeyValueBean("The uri that identifies the plan definition", "url", "", "URI", "http://hl7.org/fhir/SearchParameter/PlanDefinition-url"));
		resourceCriteria.add(new LabelKeyValueBean("The business version of the plan definition", "version", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/PlanDefinition-version"));
		resourceTypeCriteria.put("PlanDefinition", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Whether the practitioner record is active", "active", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Practitioner-active"));
		resourceCriteria.add(new LabelKeyValueBean("A server defined search that may match any of the string fields in the Address, including line, city, district, state, country, postalCode, and/or text", "address", "", "STRING", "http://hl7.org/fhir/SearchParameter/individual-address"));
		resourceCriteria.add(new LabelKeyValueBean("A city specified in an address", "address-city", "", "STRING", "http://hl7.org/fhir/SearchParameter/individual-address-city"));
		resourceCriteria.add(new LabelKeyValueBean("A country specified in an address", "address-country", "", "STRING", "http://hl7.org/fhir/SearchParameter/individual-address-country"));
		// AEGIS Extra Search Parameter address-district
		resourceCriteria.add(new LabelKeyValueBean("A district specified in an address", "address-district", "", "STRING"));
		resourceCriteria.add(new LabelKeyValueBean("A postalCode specified in an address", "address-postalcode", "", "STRING", "http://hl7.org/fhir/SearchParameter/individual-address-postalcode"));
		resourceCriteria.add(new LabelKeyValueBean("A state specified in an address", "address-state", "", "STRING", "http://hl7.org/fhir/SearchParameter/individual-address-state"));
		resourceCriteria.add(new LabelKeyValueBean("A use code specified in an address", "address-use", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/individual-address-use"));
		resourceCriteria.add(new LabelKeyValueBean("One of the languages that the practitioner can communicate with", "communication", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Practitioner-communication"));
		resourceCriteria.add(new LabelKeyValueBean("A value in an email contact", "email", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/individual-email"));
		resourceCriteria.add(new LabelKeyValueBean("A portion of the family name", "family", "", "STRING", "http://hl7.org/fhir/SearchParameter/individual-family"));
		resourceCriteria.add(new LabelKeyValueBean("Gender of the practitioner", "gender", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/individual-gender"));
		resourceCriteria.add(new LabelKeyValueBean("A portion of the given name", "given", "", "STRING", "http://hl7.org/fhir/SearchParameter/individual-given"));
		resourceCriteria.add(new LabelKeyValueBean("A practitioner's Identifier", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Practitioner-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("A server defined search that may match any of the string fields in the HumanName, including family, give, prefix, suffix, suffix, and/or text", "name", "", "STRING", "http://hl7.org/fhir/SearchParameter/Practitioner-name"));
		resourceCriteria.add(new LabelKeyValueBean("A value in a phone contact", "phone", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/individual-phone"));
		resourceCriteria.add(new LabelKeyValueBean("A portion of either family or given name using some kind of phonetic matching algorithm", "phonetic", "", "STRING", "http://hl7.org/fhir/SearchParameter/individual-phonetic"));
		resourceCriteria.add(new LabelKeyValueBean("The value in any kind of contact", "telecom", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/individual-telecom"));
		resourceTypeCriteria.put("Practitioner", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Whether this practitioner role record is in active use", "active", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/PractitionerRole-active"));
		resourceCriteria.add(new LabelKeyValueBean("The period during which the practitioner is authorized to perform in these role(s)", "date", "", "PERIOD", "http://hl7.org/fhir/SearchParameter/PractitionerRole-date"));
		resourceCriteria.add(new LabelKeyValueBean("A value in an email contact", "email", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/individual-email"));
		resourceCriteria.add(new LabelKeyValueBean("Technical endpoints providing access to services operated for the practitioner with this role", "endpoint", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/PractitionerRole-endpoint", "Endpoint"));
		resourceCriteria.add(new LabelKeyValueBean("A practitioner's Identifier", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/PractitionerRole-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("One of the locations at which this practitioner provides care", "location", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/PractitionerRole-location", "Location"));
		resourceCriteria.add(new LabelKeyValueBean("The identity of the organization the practitioner represents / acts on behalf of", "organization", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/PractitionerRole-organization", "Organization"));
		resourceCriteria.add(new LabelKeyValueBean("A value in a phone contact", "phone", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/individual-phone"));
		resourceCriteria.add(new LabelKeyValueBean("Practitioner that is able to provide the defined services for the organization", "practitioner", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/PractitionerRole-practitioner", "Practitioner"));
		resourceCriteria.add(new LabelKeyValueBean("The practitioner can perform this role at for the organization", "role", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/PractitionerRole-role"));
		resourceCriteria.add(new LabelKeyValueBean("The list of healthcare services that this worker provides for this role's Organization/Location(s)", "service", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/PractitionerRole-service", "HealthcareService"));
		resourceCriteria.add(new LabelKeyValueBean("The practitioner has this specialty at an organization", "specialty", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/PractitionerRole-specialty"));
		resourceCriteria.add(new LabelKeyValueBean("The value in any kind of contact", "telecom", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/individual-telecom"));
		resourceTypeCriteria.put("PractitionerRole", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("A request for this procedure", "based-on", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Procedure-based-on"));
		resourceCriteria.add(new LabelKeyValueBean("Classification of the procedure", "category", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Procedure-category"));
		resourceCriteria.add(new LabelKeyValueBean("A code to identify a  procedure", "code", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-code"));
		resourceCriteria.add(new LabelKeyValueBean("When the procedure was performed", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/clinical-date"));
		resourceCriteria.add(new LabelKeyValueBean("Encounter created as part of", "encounter", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-encounter", "Encounter"));
		resourceCriteria.add(new LabelKeyValueBean("A unique identifier for a procedure", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Instantiates FHIR protocol or definition", "instantiates-canonical", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Procedure-instantiates-canonical"));
		resourceCriteria.add(new LabelKeyValueBean("Instantiates external protocol or definition", "instantiates-uri", "", "URI", "http://hl7.org/fhir/SearchParameter/Procedure-instantiates-uri"));
		resourceCriteria.add(new LabelKeyValueBean("Where the procedure happened", "location", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Procedure-location", "Location"));
		resourceCriteria.add(new LabelKeyValueBean("Part of referenced event", "part-of", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Procedure-part-of"));
		resourceCriteria.add(new LabelKeyValueBean("Search by subject - a patient", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("The reference to the practitioner", "performer", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Procedure-performer"));
		resourceCriteria.add(new LabelKeyValueBean("Coded reason procedure performed", "reason-code", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Procedure-reason-code"));
		resourceCriteria.add(new LabelKeyValueBean("The justification that the procedure was performed", "reason-reference", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Procedure-reason-reference"));
		resourceCriteria.add(new LabelKeyValueBean("preparation | in-progress | not-done | suspended | aborted | completed | entered-in-error | unknown", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Procedure-status"));
		resourceCriteria.add(new LabelKeyValueBean("Search by subject", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Procedure-subject"));
		resourceTypeCriteria.put("Procedure", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Who participated", "agent", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Provenance-agent"));
		resourceCriteria.add(new LabelKeyValueBean("What the agents role was", "agent-role", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Provenance-agent-role"));
		resourceCriteria.add(new LabelKeyValueBean("How the agent participated", "agent-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Provenance-agent-type"));
		resourceCriteria.add(new LabelKeyValueBean("Identity of entity", "entity", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Provenance-entity"));
		resourceCriteria.add(new LabelKeyValueBean("Where the activity occurred, if relevant", "location", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Provenance-location", "Location"));
		resourceCriteria.add(new LabelKeyValueBean("Target Reference(s) (usually version specific)", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Provenance-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("When the activity was recorded / updated", "recorded", "", "DATE", "http://hl7.org/fhir/SearchParameter/Provenance-recorded"));
		resourceCriteria.add(new LabelKeyValueBean("Indication of the reason the entity signed the object(s)", "signature-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Provenance-signature-type"));
		resourceCriteria.add(new LabelKeyValueBean("Target Reference(s) (usually version specific)", "target", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Provenance-target"));
		resourceCriteria.add(new LabelKeyValueBean("When the activity occurred", "when", "", "DATE", "http://hl7.org/fhir/SearchParameter/Provenance-when"));
		resourceTypeCriteria.put("Provenance", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("A code that corresponds to one of its items in the questionnaire", "code", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Questionnaire-code"));
		resourceCriteria.add(new LabelKeyValueBean("A use context assigned to the questionnaire", "context", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Questionnaire-context"));
		resourceCriteria.add(new LabelKeyValueBean("A quantity- or range-valued use context assigned to the questionnaire", "context-quantity", "", "QUANTITY", "http://hl7.org/fhir/SearchParameter/Questionnaire-context-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A type of use context assigned to the questionnaire", "context-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Questionnaire-context-type"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and quantity- or range-based value assigned to the questionnaire", "context-type-quantity", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/Questionnaire-context-type-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and value assigned to the questionnaire", "context-type-value", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/Questionnaire-context-type-value"));
		resourceCriteria.add(new LabelKeyValueBean("The questionnaire publication date", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/Questionnaire-date"));
		resourceCriteria.add(new LabelKeyValueBean("ElementDefinition - details for the item", "definition", "", "URI", "http://hl7.org/fhir/SearchParameter/Questionnaire-definition"));
		resourceCriteria.add(new LabelKeyValueBean("The description of the questionnaire", "description", "", "STRING", "http://hl7.org/fhir/SearchParameter/Questionnaire-description"));
		resourceCriteria.add(new LabelKeyValueBean("The time during which the questionnaire is intended to be in use", "effective", "", "PERIOD", "http://hl7.org/fhir/SearchParameter/Questionnaire-effective"));
		resourceCriteria.add(new LabelKeyValueBean("External identifier for the questionnaire", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Questionnaire-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Intended jurisdiction for the questionnaire", "jurisdiction", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Questionnaire-jurisdiction"));
		resourceCriteria.add(new LabelKeyValueBean("Computationally friendly name of the questionnaire", "name", "", "STRING", "http://hl7.org/fhir/SearchParameter/Questionnaire-name"));
		resourceCriteria.add(new LabelKeyValueBean("Name of the publisher of the questionnaire", "publisher", "", "STRING", "http://hl7.org/fhir/SearchParameter/Questionnaire-publisher"));
		resourceCriteria.add(new LabelKeyValueBean("The current status of the questionnaire", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Questionnaire-status"));
		resourceCriteria.add(new LabelKeyValueBean("Resource that can be subject of QuestionnaireResponse", "subject-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Questionnaire-subject-type"));
		resourceCriteria.add(new LabelKeyValueBean("The human-friendly name of the questionnaire", "title", "", "STRING", "http://hl7.org/fhir/SearchParameter/Questionnaire-title"));
		resourceCriteria.add(new LabelKeyValueBean("The uri that identifies the questionnaire", "url", "", "URI", "http://hl7.org/fhir/SearchParameter/Questionnaire-url"));
		resourceCriteria.add(new LabelKeyValueBean("The business version of the questionnaire", "version", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Questionnaire-version"));
		resourceTypeCriteria.put("Questionnaire", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("The author of the questionnaire response", "author", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/QuestionnaireResponse-author"));
		resourceCriteria.add(new LabelKeyValueBean("When the questionnaire response was last changed", "authored", "", "DATE", "http://hl7.org/fhir/SearchParameter/QuestionnaireResponse-authored"));
		resourceCriteria.add(new LabelKeyValueBean("Plan/proposal/order fulfilled by this questionnaire response", "based-on", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/QuestionnaireResponse-based-on"));
		resourceCriteria.add(new LabelKeyValueBean("Encounter associated with the questionnaire response", "encounter", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/QuestionnaireResponse-encounter", "Encounter"));
		resourceCriteria.add(new LabelKeyValueBean("The unique identifier for the questionnaire response", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/QuestionnaireResponse-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Procedure or observation this questionnaire response was performed as a part of", "part-of", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/QuestionnaireResponse-part-of"));
		resourceCriteria.add(new LabelKeyValueBean("The patient that is the subject of the questionnaire response", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/QuestionnaireResponse-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("The questionnaire the answers are provided for", "questionnaire", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/QuestionnaireResponse-questionnaire", "Questionnaire"));
		resourceCriteria.add(new LabelKeyValueBean("The individual providing the information reflected in the questionnaire respose", "source", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/QuestionnaireResponse-source"));
		resourceCriteria.add(new LabelKeyValueBean("The status of the questionnaire response", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/QuestionnaireResponse-status"));
		resourceCriteria.add(new LabelKeyValueBean("The subject of the questionnaire response", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/QuestionnaireResponse-subject"));
		resourceTypeCriteria.put("QuestionnaireResponse", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Indicates if the related person record is active", "active", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/RelatedPerson-active"));
		resourceCriteria.add(new LabelKeyValueBean("A server defined search that may match any of the string fields in the Address, including line, city, district, state, country, postalCode, and/or text", "address", "", "STRING", "http://hl7.org/fhir/SearchParameter/individual-address"));
		resourceCriteria.add(new LabelKeyValueBean("A city specified in an address", "address-city", "", "STRING", "http://hl7.org/fhir/SearchParameter/individual-address-city"));
		resourceCriteria.add(new LabelKeyValueBean("A country specified in an address", "address-country", "", "STRING", "http://hl7.org/fhir/SearchParameter/individual-address-country"));
		// AEGIS Extra Search Parameter address-district
		resourceCriteria.add(new LabelKeyValueBean("A district specified in an address", "address-district", "", "STRING"));
		resourceCriteria.add(new LabelKeyValueBean("A postal code specified in an address", "address-postalcode", "", "STRING", "http://hl7.org/fhir/SearchParameter/individual-address-postalcode"));
		resourceCriteria.add(new LabelKeyValueBean("A state specified in an address", "address-state", "", "STRING", "http://hl7.org/fhir/SearchParameter/individual-address-state"));
		resourceCriteria.add(new LabelKeyValueBean("A use code specified in an address", "address-use", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/individual-address-use"));
		resourceCriteria.add(new LabelKeyValueBean("The Related Person's date of birth", "birthdate", "", "DATE", "http://hl7.org/fhir/SearchParameter/individual-birthdate"));
		resourceCriteria.add(new LabelKeyValueBean("A value in an email contact", "email", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/individual-email"));
		resourceCriteria.add(new LabelKeyValueBean("Gender of the related person", "gender", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/individual-gender"));
		resourceCriteria.add(new LabelKeyValueBean("An Identifier of the RelatedPerson", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/RelatedPerson-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("A server defined search that may match any of the string fields in the HumanName, including family, give, prefix, suffix, suffix, and/or text", "name", "", "STRING", "http://hl7.org/fhir/SearchParameter/RelatedPerson-name"));
		resourceCriteria.add(new LabelKeyValueBean("The patient this related person is related to", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/RelatedPerson-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("A value in a phone contact", "phone", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/individual-phone"));
		resourceCriteria.add(new LabelKeyValueBean("A portion of name using some kind of phonetic matching algorithm", "phonetic", "", "STRING", "http://hl7.org/fhir/SearchParameter/individual-phonetic"));
		resourceCriteria.add(new LabelKeyValueBean("The relationship between the patient and the relatedperson", "relationship", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/RelatedPerson-relationship"));
		resourceCriteria.add(new LabelKeyValueBean("The value in any kind of contact", "telecom", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/individual-telecom"));
		resourceTypeCriteria.put("RelatedPerson", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("The author of the request group", "author", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/RequestGroup-author"));
		resourceCriteria.add(new LabelKeyValueBean("The date the request group was authored", "authored", "", "DATE", "http://hl7.org/fhir/SearchParameter/RequestGroup-authored"));
		resourceCriteria.add(new LabelKeyValueBean("The code of the request group", "code", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/RequestGroup-code"));
		resourceCriteria.add(new LabelKeyValueBean("The encounter the request group applies to", "encounter", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/RequestGroup-encounter", "Encounter"));
		resourceCriteria.add(new LabelKeyValueBean("The group identifier for the request group", "group-identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/RequestGroup-group-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("External identifiers for the request group", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/RequestGroup-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("The FHIR-based definition from which the request group is realized", "instantiates-canonical", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/RequestGroup-instantiates-canonical"));
		resourceCriteria.add(new LabelKeyValueBean("The external definition from which the request group is realized", "instantiates-uri", "", "URI", "http://hl7.org/fhir/SearchParameter/RequestGroup-instantiates-uri"));
		resourceCriteria.add(new LabelKeyValueBean("The intent of the request group", "intent", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/RequestGroup-intent"));
		resourceCriteria.add(new LabelKeyValueBean("The participant in the requests in the group", "participant", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/RequestGroup-participant"));
		resourceCriteria.add(new LabelKeyValueBean("The identity of a patient to search for request groups", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/RequestGroup-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("The priority of the request group", "priority", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/RequestGroup-priority"));
		resourceCriteria.add(new LabelKeyValueBean("The status of the request group", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/RequestGroup-status"));
		resourceCriteria.add(new LabelKeyValueBean("The subject that the request group is about", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/RequestGroup-subject"));
		resourceTypeCriteria.put("RequestGroup", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("What resource is being referenced", "composed-of", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ResearchDefinition-composed-of"));
		resourceCriteria.add(new LabelKeyValueBean("A use context assigned to the research definition", "context", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ResearchDefinition-context"));
		resourceCriteria.add(new LabelKeyValueBean("A quantity- or range-valued use context assigned to the research definition", "context-quantity", "", "QUANTITY", "http://hl7.org/fhir/SearchParameter/ResearchDefinition-context-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A type of use context assigned to the research definition", "context-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ResearchDefinition-context-type"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and quantity- or range-based value assigned to the research definition", "context-type-quantity", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/ResearchDefinition-context-type-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and value assigned to the research definition", "context-type-value", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/ResearchDefinition-context-type-value"));
		resourceCriteria.add(new LabelKeyValueBean("The research definition publication date", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/ResearchDefinition-date"));
		resourceCriteria.add(new LabelKeyValueBean("What resource is being referenced", "depends-on", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ResearchDefinition-depends-on"));
		resourceCriteria.add(new LabelKeyValueBean("What resource is being referenced", "derived-from", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ResearchDefinition-derived-from"));
		resourceCriteria.add(new LabelKeyValueBean("The description of the research definition", "description", "", "STRING", "http://hl7.org/fhir/SearchParameter/ResearchDefinition-description"));
		resourceCriteria.add(new LabelKeyValueBean("The time during which the research definition is intended to be in use", "effective", "", "PERIOD", "http://hl7.org/fhir/SearchParameter/ResearchDefinition-effective"));
		resourceCriteria.add(new LabelKeyValueBean("External identifier for the research definition", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ResearchDefinition-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Intended jurisdiction for the research definition", "jurisdiction", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ResearchDefinition-jurisdiction"));
		resourceCriteria.add(new LabelKeyValueBean("Computationally friendly name of the research definition", "name", "", "STRING", "http://hl7.org/fhir/SearchParameter/ResearchDefinition-name"));
		resourceCriteria.add(new LabelKeyValueBean("What resource is being referenced", "predecessor", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ResearchDefinition-predecessor"));
		resourceCriteria.add(new LabelKeyValueBean("Name of the publisher of the research definition", "publisher", "", "STRING", "http://hl7.org/fhir/SearchParameter/ResearchDefinition-publisher"));
		resourceCriteria.add(new LabelKeyValueBean("The current status of the research definition", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ResearchDefinition-status"));
		resourceCriteria.add(new LabelKeyValueBean("What resource is being referenced", "successor", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ResearchDefinition-successor"));
		resourceCriteria.add(new LabelKeyValueBean("The human-friendly name of the research definition", "title", "", "STRING", "http://hl7.org/fhir/SearchParameter/ResearchDefinition-title"));
		resourceCriteria.add(new LabelKeyValueBean("Topics associated with the ResearchDefinition", "topic", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ResearchDefinition-topic"));
		resourceCriteria.add(new LabelKeyValueBean("The uri that identifies the research definition", "url", "", "URI", "http://hl7.org/fhir/SearchParameter/ResearchDefinition-url"));
		resourceCriteria.add(new LabelKeyValueBean("The business version of the research definition", "version", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ResearchDefinition-version"));
		resourceTypeCriteria.put("ResearchDefinition", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("What resource is being referenced", "composed-of", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ResearchElementDefinition-composed-of"));
		resourceCriteria.add(new LabelKeyValueBean("A use context assigned to the research element definition", "context", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ResearchElementDefinition-context"));
		resourceCriteria.add(new LabelKeyValueBean("A quantity- or range-valued use context assigned to the research element definition", "context-quantity", "", "QUANTITY", "http://hl7.org/fhir/SearchParameter/ResearchElementDefinition-context-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A type of use context assigned to the research element definition", "context-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ResearchElementDefinition-context-type"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and quantity- or range-based value assigned to the research element definition", "context-type-quantity", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/ResearchElementDefinition-context-type-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and value assigned to the research element definition", "context-type-value", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/ResearchElementDefinition-context-type-value"));
		resourceCriteria.add(new LabelKeyValueBean("The research element definition publication date", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/ResearchElementDefinition-date"));
		resourceCriteria.add(new LabelKeyValueBean("What resource is being referenced", "depends-on", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ResearchElementDefinition-depends-on"));
		resourceCriteria.add(new LabelKeyValueBean("What resource is being referenced", "derived-from", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ResearchElementDefinition-derived-from"));
		resourceCriteria.add(new LabelKeyValueBean("The description of the research element definition", "description", "", "STRING", "http://hl7.org/fhir/SearchParameter/ResearchElementDefinition-description"));
		resourceCriteria.add(new LabelKeyValueBean("The time during which the research element definition is intended to be in use", "effective", "", "PERIOD", "http://hl7.org/fhir/SearchParameter/ResearchElementDefinition-effective"));
		resourceCriteria.add(new LabelKeyValueBean("External identifier for the research element definition", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ResearchElementDefinition-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Intended jurisdiction for the research element definition", "jurisdiction", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ResearchElementDefinition-jurisdiction"));
		resourceCriteria.add(new LabelKeyValueBean("Computationally friendly name of the research element definition", "name", "", "STRING", "http://hl7.org/fhir/SearchParameter/ResearchElementDefinition-name"));
		resourceCriteria.add(new LabelKeyValueBean("What resource is being referenced", "predecessor", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ResearchElementDefinition-predecessor"));
		resourceCriteria.add(new LabelKeyValueBean("Name of the publisher of the research element definition", "publisher", "", "STRING", "http://hl7.org/fhir/SearchParameter/ResearchElementDefinition-publisher"));
		resourceCriteria.add(new LabelKeyValueBean("The current status of the research element definition", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ResearchElementDefinition-status"));
		resourceCriteria.add(new LabelKeyValueBean("What resource is being referenced", "successor", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ResearchElementDefinition-successor"));
		resourceCriteria.add(new LabelKeyValueBean("The human-friendly name of the research element definition", "title", "", "STRING", "http://hl7.org/fhir/SearchParameter/ResearchElementDefinition-title"));
		resourceCriteria.add(new LabelKeyValueBean("Topics associated with the ResearchElementDefinition", "topic", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ResearchElementDefinition-topic"));
		resourceCriteria.add(new LabelKeyValueBean("The uri that identifies the research element definition", "url", "", "URI", "http://hl7.org/fhir/SearchParameter/ResearchElementDefinition-url"));
		resourceCriteria.add(new LabelKeyValueBean("The business version of the research element definition", "version", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ResearchElementDefinition-version"));
		resourceTypeCriteria.put("ResearchElementDefinition", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Classifications for the study", "category", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ResearchStudy-category"));
		resourceCriteria.add(new LabelKeyValueBean("When the study began and ended", "date", "", "PERIOD", "http://hl7.org/fhir/SearchParameter/ResearchStudy-date"));
		resourceCriteria.add(new LabelKeyValueBean("Drugs, devices, etc. under study", "focus", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ResearchStudy-focus"));
		resourceCriteria.add(new LabelKeyValueBean("Business Identifier for study", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ResearchStudy-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Used to search for the study", "keyword", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ResearchStudy-keyword"));
		resourceCriteria.add(new LabelKeyValueBean("Geographic region(s) for study", "location", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ResearchStudy-location"));
		resourceCriteria.add(new LabelKeyValueBean("Part of larger study", "partof", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ResearchStudy-partof", "ResearchStudy"));
		resourceCriteria.add(new LabelKeyValueBean("Researcher who oversees multiple aspects of the study", "principalinvestigator", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ResearchStudy-principalinvestigator"));
		resourceCriteria.add(new LabelKeyValueBean("Steps followed in executing study", "protocol", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ResearchStudy-protocol", "PlanDefinition"));
		resourceCriteria.add(new LabelKeyValueBean("Facility where study activities are conducted", "site", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ResearchStudy-site", "Location"));
		resourceCriteria.add(new LabelKeyValueBean("Organization that initiates and is legally responsible for the study", "sponsor", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ResearchStudy-sponsor", "Organization"));
		resourceCriteria.add(new LabelKeyValueBean("active | administratively-completed | approved | closed-to-accrual | closed-to-accrual-and-intervention | completed | disapproved | in-review | temporarily-closed-to-accrual | temporarily-closed-to-accrual-and-intervention | withdrawn", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ResearchStudy-status"));
		resourceCriteria.add(new LabelKeyValueBean("Name for this study", "title", "", "STRING", "http://hl7.org/fhir/SearchParameter/ResearchStudy-title"));
		resourceTypeCriteria.put("ResearchStudy", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Start and end of participation", "date", "", "PERIOD", "http://hl7.org/fhir/SearchParameter/ResearchSubject-date"));
		resourceCriteria.add(new LabelKeyValueBean("Business Identifier for research subject in a study", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ResearchSubject-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Who is part of study", "individual", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ResearchSubject-individual", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("Who is part of study", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ResearchSubject-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("candidate | eligible | follow-up | ineligible | not-registered | off-study | on-study | on-study-intervention | on-study-observation | pending-on-study | potential-candidate | screening | withdrawn", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ResearchSubject-status"));
		resourceCriteria.add(new LabelKeyValueBean("Study subject is part of", "study", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ResearchSubject-study", "ResearchStudy"));
		resourceTypeCriteria.put("ResearchSubject", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Condition assessed", "condition", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/RiskAssessment-condition", "Condition"));
		resourceCriteria.add(new LabelKeyValueBean("When was assessment made?", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/clinical-date"));
		resourceCriteria.add(new LabelKeyValueBean("Where was assessment performed?", "encounter", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-encounter", "Encounter"));
		resourceCriteria.add(new LabelKeyValueBean("Unique identifier for the assessment", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Evaluation mechanism", "method", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/RiskAssessment-method"));
		resourceCriteria.add(new LabelKeyValueBean("Who/what does assessment apply to?", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("Who did assessment?", "performer", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/RiskAssessment-performer"));
		resourceCriteria.add(new LabelKeyValueBean("Likelihood of specified outcome", "probability", "", "NUMBER", "http://hl7.org/fhir/SearchParameter/RiskAssessment-probability"));
		resourceCriteria.add(new LabelKeyValueBean("Likelihood of specified outcome as a qualitative value", "risk", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/RiskAssessment-risk"));
		resourceCriteria.add(new LabelKeyValueBean("Who/what does assessment apply to?", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/RiskAssessment-subject"));
		resourceTypeCriteria.put("RiskAssessment", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("A use context assigned to the risk evidence synthesis", "context", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/RiskEvidenceSynthesis-context"));
		resourceCriteria.add(new LabelKeyValueBean("A quantity- or range-valued use context assigned to the risk evidence synthesis", "context-quantity", "", "QUANTITY", "http://hl7.org/fhir/SearchParameter/RiskEvidenceSynthesis-context-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A type of use context assigned to the risk evidence synthesis", "context-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/RiskEvidenceSynthesis-context-type"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and quantity- or range-based value assigned to the risk evidence synthesis", "context-type-quantity", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/RiskEvidenceSynthesis-context-type-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and value assigned to the risk evidence synthesis", "context-type-value", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/RiskEvidenceSynthesis-context-type-value"));
		resourceCriteria.add(new LabelKeyValueBean("The risk evidence synthesis publication date", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/RiskEvidenceSynthesis-date"));
		resourceCriteria.add(new LabelKeyValueBean("The description of the risk evidence synthesis", "description", "", "STRING", "http://hl7.org/fhir/SearchParameter/RiskEvidenceSynthesis-description"));
		resourceCriteria.add(new LabelKeyValueBean("The time during which the risk evidence synthesis is intended to be in use", "effective", "", "PERIOD", "http://hl7.org/fhir/SearchParameter/RiskEvidenceSynthesis-effective"));
		resourceCriteria.add(new LabelKeyValueBean("External identifier for the risk evidence synthesis", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/RiskEvidenceSynthesis-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Intended jurisdiction for the risk evidence synthesis", "jurisdiction", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/RiskEvidenceSynthesis-jurisdiction"));
		resourceCriteria.add(new LabelKeyValueBean("Computationally friendly name of the risk evidence synthesis", "name", "", "STRING", "http://hl7.org/fhir/SearchParameter/RiskEvidenceSynthesis-name"));
		resourceCriteria.add(new LabelKeyValueBean("Name of the publisher of the risk evidence synthesis", "publisher", "", "STRING", "http://hl7.org/fhir/SearchParameter/RiskEvidenceSynthesis-publisher"));
		resourceCriteria.add(new LabelKeyValueBean("The current status of the risk evidence synthesis", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/RiskEvidenceSynthesis-status"));
		resourceCriteria.add(new LabelKeyValueBean("The human-friendly name of the risk evidence synthesis", "title", "", "STRING", "http://hl7.org/fhir/SearchParameter/RiskEvidenceSynthesis-title"));
		resourceCriteria.add(new LabelKeyValueBean("The uri that identifies the risk evidence synthesis", "url", "", "URI", "http://hl7.org/fhir/SearchParameter/RiskEvidenceSynthesis-url"));
		resourceCriteria.add(new LabelKeyValueBean("The business version of the risk evidence synthesis", "version", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/RiskEvidenceSynthesis-version"));
		resourceTypeCriteria.put("RiskEvidenceSynthesis", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Is the schedule in active use", "active", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Schedule-active"));
		resourceCriteria.add(new LabelKeyValueBean("The individual(HealthcareService, Practitioner, Location, ...) to find a Schedule for", "actor", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Schedule-actor"));
		resourceCriteria.add(new LabelKeyValueBean("Search for Schedule resources that have a period that contains this date specified", "date", "", "PERIOD", "http://hl7.org/fhir/SearchParameter/Schedule-date"));
		resourceCriteria.add(new LabelKeyValueBean("A Schedule Identifier", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Schedule-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("High-level category", "service-category", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Schedule-service-category"));
		resourceCriteria.add(new LabelKeyValueBean("The type of appointments that can be booked into associated slot(s)", "service-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Schedule-service-type"));
		resourceCriteria.add(new LabelKeyValueBean("Type of specialty needed", "specialty", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Schedule-specialty"));
		resourceTypeCriteria.put("Schedule", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("The resource type(s) this search parameter applies to", "base", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/SearchParameter-base"));
		resourceCriteria.add(new LabelKeyValueBean("Code used in URL", "code", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/SearchParameter-code"));
		resourceCriteria.add(new LabelKeyValueBean("Defines how the part works", "component", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/SearchParameter-component", "SearchParameter"));
		resourceCriteria.add(new LabelKeyValueBean("A use context assigned to the search parameter", "context", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-context"));
		resourceCriteria.add(new LabelKeyValueBean("A quantity- or range-valued use context assigned to the search parameter", "context-quantity", "", "QUANTITY", "http://hl7.org/fhir/SearchParameter/conformance-context-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A type of use context assigned to the search parameter", "context-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-context-type"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and quantity- or range-based value assigned to the search parameter", "context-type-quantity", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/conformance-context-type-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and value assigned to the search parameter", "context-type-value", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/conformance-context-type-value"));
		resourceCriteria.add(new LabelKeyValueBean("The search parameter publication date", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/conformance-date"));
		resourceCriteria.add(new LabelKeyValueBean("Original definition for the search parameter", "derived-from", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/SearchParameter-derived-from", "SearchParameter"));
		resourceCriteria.add(new LabelKeyValueBean("The description of the search parameter", "description", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-description"));
		resourceCriteria.add(new LabelKeyValueBean("Intended jurisdiction for the search parameter", "jurisdiction", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-jurisdiction"));
		resourceCriteria.add(new LabelKeyValueBean("Computationally friendly name of the search parameter", "name", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-name"));
		resourceCriteria.add(new LabelKeyValueBean("Name of the publisher of the search parameter", "publisher", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-publisher"));
		resourceCriteria.add(new LabelKeyValueBean("The current status of the search parameter", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-status"));
		resourceCriteria.add(new LabelKeyValueBean("Types of resource (if a resource reference)", "target", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/SearchParameter-target"));
		resourceCriteria.add(new LabelKeyValueBean("number | date | string | token | reference | composite | quantity | uri | special", "type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/SearchParameter-type"));
		resourceCriteria.add(new LabelKeyValueBean("The uri that identifies the search parameter", "url", "", "URI", "http://hl7.org/fhir/SearchParameter/conformance-url"));
		resourceCriteria.add(new LabelKeyValueBean("The business version of the search parameter", "version", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-version"));
		resourceTypeCriteria.put("SearchParameter", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Date request signed", "authored", "", "DATE", "http://hl7.org/fhir/SearchParameter/ServiceRequest-authored"));
		resourceCriteria.add(new LabelKeyValueBean("What request fulfills", "based-on", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ServiceRequest-based-on"));
		resourceCriteria.add(new LabelKeyValueBean("Where procedure is going to be done", "body-site", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ServiceRequest-body-site"));
		resourceCriteria.add(new LabelKeyValueBean("Classification of service", "category", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ServiceRequest-category"));
		resourceCriteria.add(new LabelKeyValueBean("What is being requested/ordered", "code", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-code"));
		resourceCriteria.add(new LabelKeyValueBean("An encounter in which this request is made", "encounter", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-encounter", "Encounter"));
		resourceCriteria.add(new LabelKeyValueBean("Identifiers assigned to this order", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Instantiates FHIR protocol or definition", "instantiates-canonical", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ServiceRequest-instantiates-canonical"));
		resourceCriteria.add(new LabelKeyValueBean("Instantiates external protocol or definition", "instantiates-uri", "", "URI", "http://hl7.org/fhir/SearchParameter/ServiceRequest-instantiates-uri"));
		resourceCriteria.add(new LabelKeyValueBean("proposal | plan | order +", "intent", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ServiceRequest-intent"));
		resourceCriteria.add(new LabelKeyValueBean("When service should occur", "occurrence", "", "DATE", "http://hl7.org/fhir/SearchParameter/ServiceRequest-occurrence"));
		resourceCriteria.add(new LabelKeyValueBean("Search by subject - a patient", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("Requested performer", "performer", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ServiceRequest-performer"));
		resourceCriteria.add(new LabelKeyValueBean("Performer role", "performer-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ServiceRequest-performer-type"));
		resourceCriteria.add(new LabelKeyValueBean("routine | urgent | asap | stat", "priority", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ServiceRequest-priority"));
		resourceCriteria.add(new LabelKeyValueBean("What request replaces", "replaces", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ServiceRequest-replaces", "ServiceRequest"));
		resourceCriteria.add(new LabelKeyValueBean("Who/what is requesting service", "requester", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ServiceRequest-requester"));
		resourceCriteria.add(new LabelKeyValueBean("Composite Request ID", "requisition", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ServiceRequest-requisition"));
		resourceCriteria.add(new LabelKeyValueBean("Specimen to be tested", "specimen", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ServiceRequest-specimen", "Specimen"));
		resourceCriteria.add(new LabelKeyValueBean("draft | active | suspended | completed | entered-in-error | cancelled", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ServiceRequest-status"));
		resourceCriteria.add(new LabelKeyValueBean("Search by subject", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/ServiceRequest-subject"));
		resourceTypeCriteria.put("ServiceRequest", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("The style of appointment or patient that may be booked in the slot (not service type)", "appointment-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Slot-appointment-type"));
		resourceCriteria.add(new LabelKeyValueBean("A Slot Identifier", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Slot-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("The Schedule Resource that we are seeking a slot within", "schedule", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Slot-schedule", "Schedule"));
		resourceCriteria.add(new LabelKeyValueBean("A broad categorization of the service that is to be performed during this appointment", "service-category", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Slot-service-category"));
		resourceCriteria.add(new LabelKeyValueBean("The type of appointments that can be booked into the slot", "service-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Slot-service-type"));
		resourceCriteria.add(new LabelKeyValueBean("The specialty of a practitioner that would be required to perform the service requested in this appointment", "specialty", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Slot-specialty"));
		resourceCriteria.add(new LabelKeyValueBean("Appointment date/time.", "start", "", "DATE", "http://hl7.org/fhir/SearchParameter/Slot-start"));
		resourceCriteria.add(new LabelKeyValueBean("The free/busy status of the appointment", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Slot-status"));
		resourceTypeCriteria.put("Slot", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("The accession number associated with the specimen", "accession", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Specimen-accession"));
		resourceCriteria.add(new LabelKeyValueBean("The code for the body site from where the specimen originated", "bodysite", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Specimen-bodysite"));
		resourceCriteria.add(new LabelKeyValueBean("The date the specimen was collected", "collected", "", "DATE", "http://hl7.org/fhir/SearchParameter/Specimen-collected"));
		resourceCriteria.add(new LabelKeyValueBean("Who collected the specimen", "collector", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Specimen-collector"));
		resourceCriteria.add(new LabelKeyValueBean("The kind of specimen container", "container", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Specimen-container"));
		resourceCriteria.add(new LabelKeyValueBean("The unique identifier associated with the specimen container", "container-id", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Specimen-container-id"));
		resourceCriteria.add(new LabelKeyValueBean("The unique identifier associated with the specimen", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Specimen-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("The parent of the specimen", "parent", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Specimen-parent", "Specimen"));
		resourceCriteria.add(new LabelKeyValueBean("The patient the specimen comes from", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Specimen-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("available | unavailable | unsatisfactory | entered-in-error", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Specimen-status"));
		resourceCriteria.add(new LabelKeyValueBean("The subject of the specimen", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Specimen-subject"));
		resourceCriteria.add(new LabelKeyValueBean("The specimen type", "type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Specimen-type"));
		resourceTypeCriteria.put("Specimen", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("The type of specimen conditioned in container expected by the lab", "container", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/SpecimenDefinition-container"));
		resourceCriteria.add(new LabelKeyValueBean("The unique identifier associated with the specimen", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/SpecimenDefinition-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("The type of collected specimen", "type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/SpecimenDefinition-type"));
		resourceTypeCriteria.put("SpecimenDefinition", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Whether the structure is abstract", "abstract", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/StructureDefinition-abstract"));
		resourceCriteria.add(new LabelKeyValueBean("Definition that this type is constrained/specialized from", "base", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/StructureDefinition-base", "StructureDefinition"));
		resourceCriteria.add(new LabelKeyValueBean("Path that identifies the base element", "base-path", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/StructureDefinition-base-path"));
		resourceCriteria.add(new LabelKeyValueBean("A use context assigned to the structure definition", "context", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-context"));
		resourceCriteria.add(new LabelKeyValueBean("A quantity- or range-valued use context assigned to the structure definition", "context-quantity", "", "QUANTITY", "http://hl7.org/fhir/SearchParameter/conformance-context-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A type of use context assigned to the structure definition", "context-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-context-type"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and quantity- or range-based value assigned to the structure definition", "context-type-quantity", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/conformance-context-type-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and value assigned to the structure definition", "context-type-value", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/conformance-context-type-value"));
		resourceCriteria.add(new LabelKeyValueBean("The structure definition publication date", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/conformance-date"));
		resourceCriteria.add(new LabelKeyValueBean("specialization | constraint - How relates to base definition", "derivation", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/StructureDefinition-derivation"));
		resourceCriteria.add(new LabelKeyValueBean("The description of the structure definition", "description", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-description"));
		resourceCriteria.add(new LabelKeyValueBean("For testing purposes, not real usage", "experimental", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/StructureDefinition-experimental"));
		resourceCriteria.add(new LabelKeyValueBean("The system is the URL for the context-type: e.g. http://hl7.org/fhir/extension-context-type#element|CodeableConcept.text", "ext-context", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/StructureDefinition-ext-context"));
		resourceCriteria.add(new LabelKeyValueBean("External identifier for the structure definition", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Intended jurisdiction for the structure definition", "jurisdiction", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-jurisdiction"));
		resourceCriteria.add(new LabelKeyValueBean("A code for the StructureDefinition", "keyword", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/StructureDefinition-keyword"));
		resourceCriteria.add(new LabelKeyValueBean("primitive-type | complex-type | resource | logical", "kind", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/StructureDefinition-kind"));
		resourceCriteria.add(new LabelKeyValueBean("Computationally friendly name of the structure definition", "name", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-name"));
		resourceCriteria.add(new LabelKeyValueBean("A path that is constrained in the StructureDefinition", "path", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/StructureDefinition-path"));
		resourceCriteria.add(new LabelKeyValueBean("Name of the publisher of the structure definition", "publisher", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-publisher"));
		resourceCriteria.add(new LabelKeyValueBean("The current status of the structure definition", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-status"));
		resourceCriteria.add(new LabelKeyValueBean("The human-friendly name of the structure definition", "title", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-title"));
		resourceCriteria.add(new LabelKeyValueBean("Type defined or constrained by this structure", "type", "", "URI", "http://hl7.org/fhir/SearchParameter/StructureDefinition-type"));
		resourceCriteria.add(new LabelKeyValueBean("The uri that identifies the structure definition", "url", "", "URI", "http://hl7.org/fhir/SearchParameter/conformance-url"));
		resourceCriteria.add(new LabelKeyValueBean("A vocabulary binding reference", "valueset", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/StructureDefinition-valueset", "ValueSet"));
		resourceCriteria.add(new LabelKeyValueBean("The business version of the structure definition", "version", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-version"));
		resourceTypeCriteria.put("StructureDefinition", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("A use context assigned to the structure map", "context", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-context"));
		resourceCriteria.add(new LabelKeyValueBean("A quantity- or range-valued use context assigned to the structure map", "context-quantity", "", "QUANTITY", "http://hl7.org/fhir/SearchParameter/conformance-context-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A type of use context assigned to the structure map", "context-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-context-type"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and quantity- or range-based value assigned to the structure map", "context-type-quantity", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/conformance-context-type-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and value assigned to the structure map", "context-type-value", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/conformance-context-type-value"));
		resourceCriteria.add(new LabelKeyValueBean("The structure map publication date", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/conformance-date"));
		resourceCriteria.add(new LabelKeyValueBean("The description of the structure map", "description", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-description"));
		resourceCriteria.add(new LabelKeyValueBean("External identifier for the structure map", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Intended jurisdiction for the structure map", "jurisdiction", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-jurisdiction"));
		resourceCriteria.add(new LabelKeyValueBean("Computationally friendly name of the structure map", "name", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-name"));
		resourceCriteria.add(new LabelKeyValueBean("Name of the publisher of the structure map", "publisher", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-publisher"));
		resourceCriteria.add(new LabelKeyValueBean("The current status of the structure map", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-status"));
		resourceCriteria.add(new LabelKeyValueBean("The human-friendly name of the structure map", "title", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-title"));
		resourceCriteria.add(new LabelKeyValueBean("The uri that identifies the structure map", "url", "", "URI", "http://hl7.org/fhir/SearchParameter/conformance-url"));
		resourceCriteria.add(new LabelKeyValueBean("The business version of the structure map", "version", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-version"));
		resourceTypeCriteria.put("StructureMap", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Contact details for the subscription", "contact", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Subscription-contact"));
		resourceCriteria.add(new LabelKeyValueBean("The search rules used to determine when to send a notification", "criteria", "", "STRING", "http://hl7.org/fhir/SearchParameter/Subscription-criteria"));
		resourceCriteria.add(new LabelKeyValueBean("The mime-type of the notification payload", "payload", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Subscription-payload"));
		resourceCriteria.add(new LabelKeyValueBean("The current state of the subscription", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Subscription-status"));
		resourceCriteria.add(new LabelKeyValueBean("The type of channel for the sent notifications", "type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Subscription-type"));
		resourceCriteria.add(new LabelKeyValueBean("The uri that will receive the notifications", "url", "", "URI", "http://hl7.org/fhir/SearchParameter/Subscription-url"));
		resourceTypeCriteria.put("Subscription", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("The category of the substance", "category", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Substance-category"));
		resourceCriteria.add(new LabelKeyValueBean("The code of the substance or ingredient", "code", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Substance-code"));
		resourceCriteria.add(new LabelKeyValueBean("Identifier of the package/container", "container-identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Substance-container-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Expiry date of package or container of substance", "expiry", "", "DATE", "http://hl7.org/fhir/SearchParameter/Substance-expiry"));
		resourceCriteria.add(new LabelKeyValueBean("Unique identifier for the substance", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Substance-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Amount of substance in the package", "quantity", "", "QUANTITY", "http://hl7.org/fhir/SearchParameter/Substance-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("active | inactive | entered-in-error", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Substance-status"));
		resourceCriteria.add(new LabelKeyValueBean("A component of the substance", "substance-reference", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Substance-substance-reference", "Substance"));
		resourceTypeCriteria.put("Substance", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		// AEGIS Defined Search Parameters
		resourceCriteria.add(new LabelKeyValueBean("The type of the sequence shall be specified based on a controlled vocabulary", "sequence-type", "", "TOKEN"));
		resourceTypeCriteria.put("SubstanceNucleicAcid", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		// AEGIS Defined Search Parameters
		resourceCriteria.add(new LabelKeyValueBean("Class of polymer", "class", "", "TOKEN"));
		resourceCriteria.add(new LabelKeyValueBean("Geometry of polymer", "geometry", "", "TOKEN"));
		resourceCriteria.add(new LabelKeyValueBean("Modification this polymer initiates", "modification", "", "STRING"));
		resourceTypeCriteria.put("SubstancePolymer", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceTypeCriteria.put("SubstanceProtein", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceTypeCriteria.put("SubstanceReferenceInformation", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceTypeCriteria.put("SubstanceSourceMaterial", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Codes associated with the substance", "code", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/SubstanceSpecification-code"));
		// AEGIS Defined Search Parameters
		resourceCriteria.add(new LabelKeyValueBean("Identifier by which this substance is known", "identifier", "", "TOKEN"));
		resourceCriteria.add(new LabelKeyValueBean("High level categorisation, e.g. polymer or nucleic acid", "type", "", "TOKEN"));
		resourceTypeCriteria.put("SubstanceSpecification", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("External identifier", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-identifier"));
		// AEGIS Defined Search Parameter occurrence (used for $everything)
		resourceCriteria.add(new LabelKeyValueBean("When event occurred", "occurrence", "", "DATE"));
		resourceCriteria.add(new LabelKeyValueBean("Patient for whom the item is supplied", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("Who collected the Supply", "receiver", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/SupplyDelivery-receiver"));
		resourceCriteria.add(new LabelKeyValueBean("in-progress | completed | abandoned | entered-in-error", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/SupplyDelivery-status"));
		resourceCriteria.add(new LabelKeyValueBean("Dispenser", "supplier", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/SupplyDelivery-supplier"));
		resourceTypeCriteria.put("SupplyDelivery", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("The kind of supply (central, non-stock, etc.)", "category", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/SupplyRequest-category"));
		resourceCriteria.add(new LabelKeyValueBean("When the request was made", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/clinical-date"));
		resourceCriteria.add(new LabelKeyValueBean("Business Identifier for SupplyRequest", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Individual making the request", "requester", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/SupplyRequest-requester"));
		resourceCriteria.add(new LabelKeyValueBean("draft | active | suspended +", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/SupplyRequest-status"));
		resourceCriteria.add(new LabelKeyValueBean("The destination of the supply", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/SupplyRequest-subject"));
		resourceCriteria.add(new LabelKeyValueBean("Who is intended to fulfill the request", "supplier", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/SupplyRequest-supplier"));
		resourceTypeCriteria.put("SupplyRequest", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Search by creation date", "authored-on", "", "DATE", "http://hl7.org/fhir/SearchParameter/Task-authored-on"));
		resourceCriteria.add(new LabelKeyValueBean("Search by requests this task is based on", "based-on", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Task-based-on"));
		resourceCriteria.add(new LabelKeyValueBean("Search by business status", "business-status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Task-business-status"));
		resourceCriteria.add(new LabelKeyValueBean("Search by task code", "code", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Task-code"));
		resourceCriteria.add(new LabelKeyValueBean("Search by encounter", "encounter", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Task-encounter", "Encounter"));
		resourceCriteria.add(new LabelKeyValueBean("Search by task focus", "focus", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Task-focus"));
		resourceCriteria.add(new LabelKeyValueBean("Search by group identifier", "group-identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Task-group-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Search for a task instance by its business identifier", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Task-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Search by task intent", "intent", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Task-intent"));
		resourceCriteria.add(new LabelKeyValueBean("Search by last modification date", "modified", "", "DATE", "http://hl7.org/fhir/SearchParameter/Task-modified"));
		resourceCriteria.add(new LabelKeyValueBean("Search by task owner", "owner", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Task-owner"));
		resourceCriteria.add(new LabelKeyValueBean("Search by task this task is part of", "part-of", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Task-part-of", "Task"));
		resourceCriteria.add(new LabelKeyValueBean("Search by patient", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Task-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("Search by recommended type of performer (e.g., Requester, Performer, Scheduler).", "performer", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Task-performer"));
		resourceCriteria.add(new LabelKeyValueBean("Search by period Task is/was underway", "period", "", "PERIOD", "http://hl7.org/fhir/SearchParameter/Task-period"));
		resourceCriteria.add(new LabelKeyValueBean("Search by task priority", "priority", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Task-priority"));
		resourceCriteria.add(new LabelKeyValueBean("Search by task requester", "requester", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Task-requester"));
		resourceCriteria.add(new LabelKeyValueBean("Search by task status", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/Task-status"));
		resourceCriteria.add(new LabelKeyValueBean("Search by subject", "subject", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/Task-subject"));
		resourceTypeCriteria.put("Task", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("A use context assigned to the terminology capabilities", "context", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-context"));
		resourceCriteria.add(new LabelKeyValueBean("A quantity- or range-valued use context assigned to the terminology capabilities", "context-quantity", "", "QUANTITY", "http://hl7.org/fhir/SearchParameter/conformance-context-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A type of use context assigned to the terminology capabilities", "context-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-context-type"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and quantity- or range-based value assigned to the terminology capabilities", "context-type-quantity", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/conformance-context-type-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and value assigned to the terminology capabilities", "context-type-value", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/conformance-context-type-value"));
		resourceCriteria.add(new LabelKeyValueBean("The terminology capabilities publication date", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/conformance-date"));
		resourceCriteria.add(new LabelKeyValueBean("The description of the terminology capabilities", "description", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-description"));
		resourceCriteria.add(new LabelKeyValueBean("Intended jurisdiction for the terminology capabilities", "jurisdiction", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-jurisdiction"));
		resourceCriteria.add(new LabelKeyValueBean("Computationally friendly name of the terminology capabilities", "name", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-name"));
		resourceCriteria.add(new LabelKeyValueBean("Name of the publisher of the terminology capabilities", "publisher", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-publisher"));
		resourceCriteria.add(new LabelKeyValueBean("The current status of the terminology capabilities", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-status"));
		resourceCriteria.add(new LabelKeyValueBean("The human-friendly name of the terminology capabilities", "title", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-title"));
		resourceCriteria.add(new LabelKeyValueBean("The uri that identifies the terminology capabilities", "url", "", "URI", "http://hl7.org/fhir/SearchParameter/conformance-url"));
		resourceCriteria.add(new LabelKeyValueBean("The business version of the terminology capabilities", "version", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-version"));
		resourceTypeCriteria.put("TerminologyCapabilities", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("An external identifier for the test report", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/TestReport-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("The test report generation date", "issued", "", "DATE", "http://hl7.org/fhir/SearchParameter/TestReport-issued"));
		resourceCriteria.add(new LabelKeyValueBean("The reference to a participant in the test execution", "participant", "", "URI", "http://hl7.org/fhir/SearchParameter/TestReport-participant"));
		resourceCriteria.add(new LabelKeyValueBean("The result disposition of the test execution", "result", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/TestReport-result"));
		resourceCriteria.add(new LabelKeyValueBean("The name of the testing organization", "tester", "", "STRING", "http://hl7.org/fhir/SearchParameter/TestReport-tester"));
		resourceCriteria.add(new LabelKeyValueBean("The test script executed to produce this report", "testscript", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/TestReport-testscript", "TestScript"));
		resourceTypeCriteria.put("TestReport", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("A use context assigned to the test script", "context", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/TestScript-context"));
		resourceCriteria.add(new LabelKeyValueBean("A quantity- or range-valued use context assigned to the test script", "context-quantity", "", "QUANTITY", "http://hl7.org/fhir/SearchParameter/TestScript-context-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A type of use context assigned to the test script", "context-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/TestScript-context-type"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and quantity- or range-based value assigned to the test script", "context-type-quantity", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/TestScript-context-type-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and value assigned to the test script", "context-type-value", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/TestScript-context-type-value"));
		resourceCriteria.add(new LabelKeyValueBean("The test script publication date", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/TestScript-date"));
		resourceCriteria.add(new LabelKeyValueBean("The description of the test script", "description", "", "STRING", "http://hl7.org/fhir/SearchParameter/TestScript-description"));
		resourceCriteria.add(new LabelKeyValueBean("External identifier for the test script", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/TestScript-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Intended jurisdiction for the test script", "jurisdiction", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/TestScript-jurisdiction"));
		resourceCriteria.add(new LabelKeyValueBean("Computationally friendly name of the test script", "name", "", "STRING", "http://hl7.org/fhir/SearchParameter/TestScript-name"));
		resourceCriteria.add(new LabelKeyValueBean("Name of the publisher of the test script", "publisher", "", "STRING", "http://hl7.org/fhir/SearchParameter/TestScript-publisher"));
		resourceCriteria.add(new LabelKeyValueBean("The current status of the test script", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/TestScript-status"));
		resourceCriteria.add(new LabelKeyValueBean("TestScript required and validated capability", "testscript-capability", "", "STRING", "http://hl7.org/fhir/SearchParameter/TestScript-testscript-capability"));
		resourceCriteria.add(new LabelKeyValueBean("The human-friendly name of the test script", "title", "", "STRING", "http://hl7.org/fhir/SearchParameter/TestScript-title"));
		resourceCriteria.add(new LabelKeyValueBean("The uri that identifies the test script", "url", "", "URI", "http://hl7.org/fhir/SearchParameter/TestScript-url"));
		resourceCriteria.add(new LabelKeyValueBean("The business version of the test script", "version", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/TestScript-version"));
		resourceTypeCriteria.put("TestScript", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("This special parameter searches for codes in the value set. See additional notes on the ValueSet resource", "code", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/ValueSet-code"));
		resourceCriteria.add(new LabelKeyValueBean("A use context assigned to the value set", "context", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-context"));
		resourceCriteria.add(new LabelKeyValueBean("A quantity- or range-valued use context assigned to the value set", "context-quantity", "", "QUANTITY", "http://hl7.org/fhir/SearchParameter/conformance-context-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A type of use context assigned to the value set", "context-type", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-context-type"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and quantity- or range-based value assigned to the value set", "context-type-quantity", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/conformance-context-type-quantity"));
		resourceCriteria.add(new LabelKeyValueBean("A use context type and value assigned to the value set", "context-type-value", "", "COMPOSITE", "http://hl7.org/fhir/SearchParameter/conformance-context-type-value"));
		resourceCriteria.add(new LabelKeyValueBean("The value set publication date", "date", "", "DATE", "http://hl7.org/fhir/SearchParameter/conformance-date"));
		resourceCriteria.add(new LabelKeyValueBean("The description of the value set", "description", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-description"));
		resourceCriteria.add(new LabelKeyValueBean("Identifies the value set expansion (business identifier)", "expansion", "", "URI", "http://hl7.org/fhir/SearchParameter/ValueSet-expansion"));
		resourceCriteria.add(new LabelKeyValueBean("External identifier for the value set", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("Intended jurisdiction for the value set", "jurisdiction", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-jurisdiction"));
		resourceCriteria.add(new LabelKeyValueBean("Computationally friendly name of the value set", "name", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-name"));
		resourceCriteria.add(new LabelKeyValueBean("Name of the publisher of the value set", "publisher", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-publisher"));
		resourceCriteria.add(new LabelKeyValueBean("A code system included or excluded in the value set or an imported value set", "reference", "", "URI", "http://hl7.org/fhir/SearchParameter/ValueSet-reference"));
		resourceCriteria.add(new LabelKeyValueBean("The current status of the value set", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-status"));
		resourceCriteria.add(new LabelKeyValueBean("The human-friendly name of the value set", "title", "", "STRING", "http://hl7.org/fhir/SearchParameter/conformance-title"));
		resourceCriteria.add(new LabelKeyValueBean("The uri that identifies the value set", "url", "", "URI", "http://hl7.org/fhir/SearchParameter/conformance-url"));
		resourceCriteria.add(new LabelKeyValueBean("The business version of the value set", "version", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/conformance-version"));
		resourceTypeCriteria.put("ValueSet", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("A resource that was validated", "target", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/VerificationResult-target"));
		// AEGIS Defined Search Parameters
		resourceCriteria.add(new LabelKeyValueBean("none | initial | periodic", "need", "", "TOKEN"));
		resourceCriteria.add(new LabelKeyValueBean("attested | validated | in-process | req-revalid | val-fail | reval-fail", "status", "", "TOKEN"));
		resourceCriteria.add(new LabelKeyValueBean("nothing | primary | multiple", "type", "", "TOKEN"));
		resourceTypeCriteria.put("VerificationResult", resourceCriteria);

		resourceCriteria = new ArrayList<LabelKeyValueBean>();
		resourceCriteria.add(new LabelKeyValueBean("Return prescriptions written on this date", "datewritten", "", "DATE", "http://hl7.org/fhir/SearchParameter/VisionPrescription-datewritten"));
		resourceCriteria.add(new LabelKeyValueBean("Return prescriptions with this encounter identifier", "encounter", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-encounter", "Encounter"));
		resourceCriteria.add(new LabelKeyValueBean("Return prescriptions with this external identifier", "identifier", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/clinical-identifier"));
		resourceCriteria.add(new LabelKeyValueBean("The identity of a patient to list dispenses for", "patient", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/clinical-patient", "Patient"));
		resourceCriteria.add(new LabelKeyValueBean("Who authorized the vision prescription", "prescriber", "", "REFERENCE", "http://hl7.org/fhir/SearchParameter/VisionPrescription-prescriber"));
		resourceCriteria.add(new LabelKeyValueBean("The status of the vision prescription", "status", "", "TOKEN", "http://hl7.org/fhir/SearchParameter/VisionPrescription-status"));
		resourceTypeCriteria.put("VisionPrescription", resourceCriteria);

		// Add extra global search parameters that should not be included in individual resource search parameters
		allGlobalResourceCriteria = new ArrayList<LabelKeyValueBean>();
		allGlobalResourceCriteria.addAll(globalResourceCriteria);
	}

	public static boolean isValidOperationResourceType(String resourceType) {
		boolean isValid = false;

		if (resourceType != null && operationResourceTypes.contains(resourceType)) {
			isValid = true;
		}

		return isValid;
	}


	public static boolean isSupportedGlobalOperation(String operationName) {
		boolean isSupported = false;

		for (LabelKeyValueBean globalOperation : globalOperations) {
			if (globalOperation.getKey() != null && !globalOperation.getKey().equalsIgnoreCase("internal")) {
				if (globalOperation.getLabel().equals(operationName)) {
					isSupported = true;
					break;
				}
			}
		}

		return isSupported;
	}


	public static boolean isSupportedGlobalOperation(String operationName, String operationDefinition) {
		boolean isSupported = false;

		for (LabelKeyValueBean globalOperation : globalOperations) {
			if (globalOperation.getKey() != null && !globalOperation.getKey().equalsIgnoreCase("internal")) {
				if (globalOperation.getLabel().equals(operationName) && globalOperation.getValue().equals(operationDefinition)) {
					isSupported = true;
					break;
				}
			}
		}

		return isSupported;
	}

	public static List<LabelKeyValueBean> getSupportedResourceOperations(String resourceTypeName) {
		List<LabelKeyValueBean> resourceOperationList = resourceOperations.get(resourceTypeName);

		return resourceOperationList;
	}

	public static boolean isSupportedResourceOperation(String resourceTypeName, String operationName) {
		boolean isSupported = false;

		List<LabelKeyValueBean> resourceOperationList = resourceOperations.get(resourceTypeName);

		if (resourceTypeName != null && resourceOperationList != null) {

			for (LabelKeyValueBean resourceOperation : resourceOperationList) {
				if (resourceOperation.getLabel() != null && resourceOperation.getLabel().equals(operationName)) {
					isSupported = true;
					break;
				}
			}
		}

		if (isSupported == false) {
			// Check for "mixed" global operation
			for (LabelKeyValueBean globalOperation : globalOperations) {
				if (globalOperation.getKey() != null && globalOperation.getKey().equalsIgnoreCase("mixed")) {
					if (globalOperation.getLabel().equals(operationName)) {
						isSupported = true;
						break;
					}
				}
			}
		}

		return isSupported;
	}

	public static String getOperationOAuthScope(String resourceTypeName, String operationName) {
		String scope = null;

		// Check resource type operations first
		List<LabelKeyValueBean> resourceOperationList = resourceOperations.get(resourceTypeName);

		if (resourceTypeName != null && resourceOperationList != null) {

			for (LabelKeyValueBean resourceOperation : resourceOperationList) {

				if (resourceOperation.getLabel() != null && resourceOperation.getLabel().equals(operationName)) {
					scope = resourceOperation.getType();
					break;
				}
			}
		}

		// Check global operation if scope not found for resource type
		if (scope == null) {

			for (LabelKeyValueBean globalOperation : globalOperations) {

				if (globalOperation.getLabel().equals(operationName)) {
					scope = globalOperation.getType();
					break;
				}
			}
		}

		// If scope not found for operation, return default "read"
		if (scope == null) {
			scope = "read";
		}

		return scope;
	}

	public static boolean isValidResourceType(String resourceType) {
		boolean isValid = false;

		if (resourceType != null && resourceTypes.contains(resourceType)) {
			isValid = true;
		}

		return isValid;
	}


	public static boolean isSupportedResourceType(String resourceType) {
		boolean isSupported = false;

		if (resourceType != null && supportedResourceTypes.contains(resourceType)) {
			isSupported = true;
		}

		return isSupported;
	}


	public static boolean isSupportedResourceCriteriaType(String resourceType, String criteriaName) {

//		System.out.println("  isSupportedResourceCriteriaType( " + resourceType + ", " + criteriaName + " )");

		boolean isSupported = false;

		if (criteriaName.equals("page")) {
			// Special case for page parameter
			isSupported = true;
		}
		else if (criteriaName.startsWith("_include:")) {
			// Special case for global _include parameter with modifier; only allow iterate modifier
			if (criteriaName.equals("_include:iterate")) {
				isSupported = true;
			}
		}
		else if (criteriaName.startsWith("_id:") ||
				criteriaName.startsWith("_lastUpdated:") ||
				criteriaName.startsWith("_tag:") ||
				criteriaName.startsWith("_profile:") ||
				criteriaName.startsWith("_security:") ||
				criteriaName.startsWith("_text:") ||
				criteriaName.startsWith("_content:") ||
				criteriaName.startsWith("_list:") ||
				criteriaName.startsWith("_has:") ||
				criteriaName.startsWith("_type:") ||
				criteriaName.startsWith("_sort:") ||
				criteriaName.startsWith("_count:") ||
				criteriaName.startsWith("_revinclude:") ||
				criteriaName.startsWith("_summary:") ||
				criteriaName.startsWith("_total:") ||
				criteriaName.startsWith("_elements:") ||
				criteriaName.startsWith("_contained:") ||
				criteriaName.startsWith("_containedType:")
			) {
			// Special case for global and result parameters with modifier; no modifier allowed
			isSupported = false;
		}
		else if (resourceType == null) {
			// Only check global parameters
			for (LabelKeyValueBean resourceCriteria : allGlobalResourceCriteria) {

				if (criteriaName.equals(resourceCriteria.getKey())) {
					isSupported = true;
					break;
				}
			}
		}
		else {
			// Check global parameters first
			for (LabelKeyValueBean resourceCriteria : allGlobalResourceCriteria) {

				if (criteriaName.equals(resourceCriteria.getKey())) {
					isSupported = true;
					break;
				}
			}

			if (isSupported == false) {
				// Check specific resource parameters, account for chained parameter
				LabelKeyValueBean lookupResourceCriteria = null;
				String chainedResourceTypeName = "";
				String lookupResourceTypeName = resourceType;
				String lookupResourceCriteriaKey = criteriaName;
				int prefixEnd = criteriaName.lastIndexOf(".");
				if (prefixEnd >= 0) {
					lookupResourceTypeName = "";
//					System.out.println("     --> prefixEnd [" + prefixEnd + "]");
					// reset lookupResourceTypeName and lookupResourceCriteriaKey
					lookupResourceCriteriaKey = criteriaName.substring(prefixEnd + 1, criteriaName.length());
//					System.out.println("     --> lookupResourceCriteriaKey [" + lookupResourceCriteriaKey + "]");

					String chainPrefix = criteriaName.substring(0, prefixEnd);
//					System.out.println("     --> chainPrefix [" + chainPrefix + "]");
					int delimPos = chainPrefix.indexOf(":");
					if (delimPos >= 0) {
						chainedResourceTypeName = chainPrefix.substring(delimPos + 1, chainPrefix.length());
//						System.out.println("     --> chainedResourceTypeName [" + chainedResourceTypeName + "]");
					}

					// If chainedResourceTypeName.isEmpty() then chain parameter prefix did not contain explicit chain resource type
					if (chainedResourceTypeName.isEmpty()) {
//						System.out.println("    <#> isSupportedResourceCriteriaType - chained parameter - did not contain explicit chain resource type!");

						// Get search resource type criteria for chain prefix key
						lookupResourceCriteria = findResourceTypeResourceCriteria(resourceType, chainPrefix);

						if (lookupResourceCriteria != null) {
							// Check refType - if single resource type, use it; if not, return empty type
							if (lookupResourceCriteria.getRefType() != null && !lookupResourceCriteria.getRefType().isEmpty() && !lookupResourceCriteria.getRefType().equals("*")) {
								lookupResourceTypeName = lookupResourceCriteria.getRefType();
							}
						}
					}
					else {
						lookupResourceTypeName = chainedResourceTypeName;
					}
				}
				else {
//					System.out.println("    <#> findResourceTypeResourceCriteriaType - not chained parameter - using lookupResourceTypeName [" + lookupResourceTypeName + "], lookupResourceCriteriaKey [" + lookupResourceCriteriaKey + "]");

					int delimPos = criteriaName.indexOf(":");
					if (delimPos >= 0) {
						lookupResourceCriteriaKey = criteriaName.substring(0, delimPos);
//						System.out.println("     --> lookupResourceCriteriaKey [" + lookupResourceCriteriaKey + "]");
					}
				}

//				System.out.println("    <#> isSupportedResourceCriteriaType - using lookupResourceTypeName [" + lookupResourceTypeName + "], lookupResourceCriteriaKey [" + lookupResourceCriteriaKey + "]");

				if (lookupResourceTypeName != null && !lookupResourceTypeName.isEmpty()) {
					// Check global parameters first
					for (LabelKeyValueBean resourceCriteria : allGlobalResourceCriteria) {

						if (lookupResourceCriteriaKey.equals(resourceCriteria.getKey())) {
							isSupported = true;
							break;
						}
					}

					if (!isSupported) {
						// Check lookup Resource Type parameters
						for (LabelKeyValueBean resourceCriteria : resourceTypeCriteria.get(lookupResourceTypeName)) {

							if (lookupResourceCriteriaKey.equals(resourceCriteria.getKey())) {
								isSupported = true;
								break;
							}
						}
					}
				}
			}
		}

		return isSupported;
	}

	public static boolean isValidCompartment(String compartment) {
		boolean isValid = false;

		if (compartment != null && compartments.contains(compartment)) {
			isValid = true;
		}

		return isValid;
	}

	public static boolean isSupportedCompartment(String compartment) {
		boolean isSupported = false;

		if (compartment != null && supportedCompartments.contains(compartment)) {
			isSupported = true;
		}

		return isSupported;
	}

	public static List<String> findCompartmentResourceTypeCriteria(String compartment, String resourceType) {
		List<String> criteriaName = new ArrayList<String>();

		if (compartment != null && resourceType != null) {

			for (LabelKeyValueBean lkvb : compartmentResourceTypeCriteria) {

				if (lkvb.getLabel().equals(compartment) && lkvb.getKey().equals(resourceType)) {
					criteriaName.add(lkvb.getValue());
					break;
				}
			}
		}

		return criteriaName;
	}

	public static List<LabelKeyValueBean> getCompartmentResourceTypeCriteria(String compartment) {
		List<LabelKeyValueBean> compartmentResourceTypeCriteriaList = new ArrayList<LabelKeyValueBean>();

		if (compartment != null) {

			for (LabelKeyValueBean lkvb : compartmentResourceTypeCriteria) {

				if (lkvb.getLabel().equals(compartment)) {
					compartmentResourceTypeCriteriaList.add(lkvb);
				}
			}
		}

		return compartmentResourceTypeCriteriaList;
	}

	public static boolean isValidCompartmentResourceType(String compartment, String resourceType) {
		boolean isValid = false;

		if (compartment != null && resourceType != null) {

			for (LabelKeyValueBean lkvb : compartmentResourceTypeCriteria) {

				if (lkvb.getLabel().equals(compartment) && lkvb.getKey().equals(resourceType)) {
					isValid = true;
					break;
				}
			}
		}

		return isValid;
	}

	public static LabelKeyValueBean getEverythingDateCriteria(String resourceTypeName) {
		LabelKeyValueBean everythingDateCriteria = null;

		for (LabelKeyValueBean lkvb : everythingResourceTypeDateCriteria) {
			if (lkvb.getLabel().equals("date") && lkvb.getKey().equals(resourceTypeName)) {
				everythingDateCriteria = lkvb;
				break;
			}
		}

		return everythingDateCriteria;
	}

	public static LabelKeyValueBean findResourceTypeResourceCriteria(String resourceTypeName, String resourceCriteriaKey) {

//		System.out.println("    <+> findResourceTypeResourceCriteria( " + resourceTypeName + ", " + resourceCriteriaKey + " )");

		LabelKeyValueBean resourceCriteria = null;

		if (resourceTypeName == null) {
			for (LabelKeyValueBean lkvb : allGlobalResourceCriteria) {
				if (resourceCriteriaKey.equals(lkvb.getKey())) {
					resourceCriteria = lkvb;
					break;
				}
			}
		}
		else {
			for (LabelKeyValueBean lkvb : allGlobalResourceCriteria) {
				if (resourceCriteriaKey.equals(lkvb.getKey())) {
					resourceCriteria = lkvb;
					break;
				}
			}

			if (resourceCriteria == null) {
				List<LabelKeyValueBean> criteriaList = resourceTypeCriteria.get(resourceTypeName);
				for (LabelKeyValueBean lkvb : criteriaList) {
					if (resourceCriteriaKey.equals(lkvb.getKey())) {
						resourceCriteria = lkvb;
						break;
					}
				}
			}
		}


		return resourceCriteria;
	}

	public static String findResourceTypeResourceCriteriaType(String resourceTypeName, String resourceCriteriaKey) {

//		System.out.println("");
//		System.out.println("    <-> findResourceTypeResourceCriteriaType( " + resourceTypeName + ", " + resourceCriteriaKey + " )");

		LabelKeyValueBean resourceCriteria = null;
		String chainedResourceTypeName = "";
		String lookupResourceTypeName = resourceTypeName;
		String lookupResourceCriteriaKey = resourceCriteriaKey;
		String resourceCriteriaType = "";

		if (resourceCriteriaKey != null && !resourceCriteriaKey.isEmpty()) {
			// Trim resourceCriteriaKey of any chained parameter prefixes
			int prefixEnd = resourceCriteriaKey.lastIndexOf(".");
			int chainedResourceEnd = resourceCriteriaKey.lastIndexOf(":");
			if (prefixEnd >= 0) {
				lookupResourceTypeName = "";
				String chainPrefix = "";
//				System.out.println("     --> prefixEnd [" + prefixEnd + "]");
//				System.out.println("     --> chainedResourceEnd [" + chainedResourceEnd + "]");

				if (prefixEnd < chainedResourceEnd) {
					// Special case where no chained parameter after chained resource name
					// Need to fallback to parameter before chained resource which is the original reference

					// reset lookupResourceTypeName and lookupResourceCriteriaKey
					lookupResourceCriteriaKey = resourceCriteriaKey.substring(prefixEnd + 1, chainedResourceEnd);
//					System.out.println("     --> lookupResourceCriteriaKey [" + lookupResourceCriteriaKey + "] (special case)");

					chainedResourceTypeName = resourceTypeName;
//					System.out.println("     --> chainedResourceTypeName [" + chainedResourceTypeName + "] (special case)");
				}
				else {
					// reset lookupResourceTypeName and lookupResourceCriteriaKey
					lookupResourceCriteriaKey = resourceCriteriaKey.substring(prefixEnd + 1, resourceCriteriaKey.length());
//					System.out.println("     --> lookupResourceCriteriaKey [" + lookupResourceCriteriaKey + "]");

					chainPrefix = resourceCriteriaKey.substring(0, prefixEnd);

//					System.out.println("     --> chainPrefix [" + chainPrefix + "]");
					int delimPos = chainPrefix.indexOf(":");
					if (delimPos >= 0) {
						chainedResourceTypeName = chainPrefix.substring(delimPos + 1, chainPrefix.length());
//						System.out.println("     --> chainedResourceTypeName [" + chainedResourceTypeName + "]");
					}

					// Additional special check for nested chained parameter
					prefixEnd = chainPrefix.lastIndexOf(".");
					if (prefixEnd >= 0) {
						chainPrefix = chainPrefix.substring(prefixEnd + 1);
//						System.out.println("     --> chainPrefix (nested) [" + chainPrefix + "]");
					}
				}

				// If chainedResourceTypeName.isEmpty() then chain parameter prefix did not contain explicit chain resource type
				if (chainedResourceTypeName.isEmpty()) {
//					System.out.println("    <#> findResourceTypeResourceCriteriaType - chained parameter - did not contain explicit chain resource type!");

					// Get search resource type criteria for chain prefix key
					resourceCriteria = findResourceTypeResourceCriteria(resourceTypeName, chainPrefix);

					if (resourceCriteria != null) {
						// Check refType - if single resource type, use it; if not, return empty type
						if (resourceCriteria.getRefType() != null && !resourceCriteria.getRefType().isEmpty() && !resourceCriteria.getRefType().equals("*")) {
							lookupResourceTypeName = resourceCriteria.getRefType();
						}
						else {
//							System.out.println("  findResourceTypeResourceCriteriaType - chained parameter - resourceTypeCriteria.getRefType() NOT single resource type!");
							return resourceCriteriaType;
						}
					}
					else {
//						System.out.println("  findResourceTypeResourceCriteriaType - chained parameter - could not determine explicit resource type!");
						return resourceCriteriaType;
					}
				}
				else {
					lookupResourceTypeName = chainedResourceTypeName;
				}
			}
			else {
				// Not a chained parameter; need to check for explicit resource type and remove
				int delimPos = resourceCriteriaKey.indexOf(":");
				if (delimPos >= 0) {
					lookupResourceCriteriaKey = resourceCriteriaKey.substring(0, delimPos);
				}
			}

//			System.out.println("  findResourceTypeResourceCriteriaType - resourceTypeName [" + resourceTypeName +
//					"]; lookupResourceTypeName [" + lookupResourceTypeName +
//					"]; resourceCriteriaKey [" + resourceCriteriaKey +
//					"]; lookupResourceCriteriaKey [" + lookupResourceCriteriaKey + "]");

			if (lookupResourceTypeName != null && !lookupResourceTypeName.isEmpty()) {
				resourceCriteria = findResourceTypeResourceCriteria(lookupResourceTypeName, lookupResourceCriteriaKey);
			}

			if (resourceCriteria != null) {
				resourceCriteriaType = resourceCriteria.getType();
			}
		}

//		System.out.println("    <-> found resource criteria type - '" + resourceCriteriaType + "'");

		return resourceCriteriaType;
	}

	public static String findResourceTypeResourceRefType(String resourceTypeName, String resourceCriteriaKey) {

//		System.out.println("");
//		System.out.println("    <-> findResourceTypeResourceRefType( " + resourceTypeName + ", " + resourceCriteriaKey + " )");

		LabelKeyValueBean resourceCriteria = null;
		String chainedResourceTypeName = "";
		String lookupResourceTypeName = resourceTypeName;
		String lookupResourceCriteriaKey = resourceCriteriaKey;
		String resourceRefType = "";

		if (resourceCriteriaKey != null && !resourceCriteriaKey.isEmpty()) {
			// Trim resourceCriteriaKey of any chained parameter prefixes
			int prefixEnd = resourceCriteriaKey.lastIndexOf(".");
			if (prefixEnd >= 0) {
				lookupResourceTypeName = "";
//				System.out.println("     --> prefixEnd [" + prefixEnd + "]");
				// reset lookupResourceTypeName and lookupResourceCriteriaKey
				lookupResourceCriteriaKey = resourceCriteriaKey.substring(prefixEnd + 1, resourceCriteriaKey.length());
//				System.out.println("     --> lookupResourceCriteriaKey [" + lookupResourceCriteriaKey + "]");

				String chainPrefix = resourceCriteriaKey.substring(0, prefixEnd);
//				System.out.println("     --> chainPrefix [" + chainPrefix + "]");
				int delimPos = chainPrefix.indexOf(":");
				if (delimPos >= 0) {
					chainedResourceTypeName = chainPrefix.substring(delimPos + 1, chainPrefix.length());
//					System.out.println("     --> chainedResourceTypeName [" + chainedResourceTypeName + "]");
				}

				// If chainedResourceTypeName.isEmpty() then chain parameter prefix did not contain explicit chain resource type
				if (chainedResourceTypeName.isEmpty()) {
//					System.out.println("    <#> findResourceTypeResourceCriteriaType - chained parameter - did not contain explicit chain resource type!");

					// Get search resource type criteria for chain prefix key
					resourceCriteria = findResourceTypeResourceCriteria(resourceTypeName, chainPrefix);

					if (resourceCriteria != null) {
						// Check refType - if single resource type, lookup criteria for chained resource type and parameter; if not, return empty type
						if (resourceCriteria.getRefType() != null && !resourceCriteria.getRefType().isEmpty() && !resourceCriteria.getRefType().equals("*")) {
							String tempRefType = resourceCriteria.getRefType();
							resourceCriteria = findResourceTypeResourceCriteria(tempRefType, lookupResourceCriteriaKey);

							if (resourceCriteria != null) {
								// Check refType - if single resource type, use it; if not, return empty type
								if (resourceCriteria.getRefType() != null && !resourceCriteria.getRefType().isEmpty() && !resourceCriteria.getRefType().equals("*")) {
									resourceRefType = resourceCriteria.getRefType();
								}
							}
						}
					}
				}
				else {
//					System.out.println("    <#> findResourceTypeResourceCriteriaType - chained parameter - contains explicit chain resource type!");

					// Get search resource type criteria for chain prefix key
					resourceCriteria = findResourceTypeResourceCriteria(chainedResourceTypeName, lookupResourceCriteriaKey);

					if (resourceCriteria != null) {
						// Check refType - if single resource type, use it; if not, return empty type
						if (resourceCriteria.getRefType() != null && !resourceCriteria.getRefType().isEmpty() && !resourceCriteria.getRefType().equals("*")) {
							resourceRefType = resourceCriteria.getRefType();
						}
					}
				}
			}
			else {
//				System.out.println("    <#> findResourceTypeResourceCriteriaType - not chained parameter - using lookupResourceTypeName [" + lookupResourceTypeName + "], lookupResourceCriteriaKey [" + lookupResourceCriteriaKey + "]");

				int delimPos = resourceCriteriaKey.indexOf(":");
				if (delimPos >= 0) {
					resourceRefType = resourceCriteriaKey.substring(delimPos + 1, resourceCriteriaKey.length());
//					System.out.println("     --> resourceRefType [" + resourceRefType + "]");
					if (!isValidResourceType(resourceRefType)) {
						resourceRefType = "";
					}
				}
				else {
					if (lookupResourceTypeName != null && !lookupResourceTypeName.isEmpty()) {
						resourceCriteria = findResourceTypeResourceCriteria(lookupResourceTypeName, lookupResourceCriteriaKey);

						if (resourceCriteria != null) {
							resourceRefType = resourceCriteria.getRefType();
						}
					}
				}
			}
		}

//		System.out.println("    <-> found resource ref type - '" + resourceRefType + "'");

		return resourceRefType;
	}

	public static List<LabelKeyValueBean> getResourceTypeResourceCriteria(String resourceTypeName) {
		List<LabelKeyValueBean> resourceCriteriaList = resourceTypeCriteria.get(resourceTypeName);

		return resourceCriteriaList;
	}

	public static String findValidResourceType(String searchString) {
		String validResourceType = null;

		// iterate in reverse order to avoid false positive matches
		for (int i = resourceTypes.size() - 1; i >= 0; i--) {
			if (searchString.contains(resourceTypes.get(i))) {
				validResourceType = resourceTypes.get(i);
				break;
			}
		}

		return validResourceType;
	}

}
