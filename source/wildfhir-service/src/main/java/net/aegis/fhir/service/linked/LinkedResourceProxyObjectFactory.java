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
package net.aegis.fhir.service.linked;

import org.hl7.fhir.r4.model.ResourceType;

/**
 * @author richard.ettema
 *
 */
public class LinkedResourceProxyObjectFactory {

	public LinkedResourceProxy getLinkedResourceProxy(String resourceType) {

		LinkedResourceProxy proxy = null;

		if (resourceType.equalsIgnoreCase(ResourceType.Account.getPath())) {
			proxy = new LinkedResourceAccount();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.ActivityDefinition.getPath())) {
			proxy = new LinkedResourceActivityDefinition();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.AdverseEvent.getPath())) {
			proxy = new LinkedResourceAdverseEvent();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.AllergyIntolerance.getPath())) {
			proxy = new LinkedResourceAllergyIntolerance();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.Appointment.getPath())) {
			proxy = new LinkedResourceAppointment();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.AppointmentResponse.getPath())) {
			proxy = new LinkedResourceAppointmentResponse();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.AuditEvent.getPath())) {
			proxy = new LinkedResourceAuditEvent();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.Basic.getPath())) {
			proxy = new LinkedResourceBasic();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.Binary.getPath())) {
			proxy = new LinkedResourceBinary();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.BiologicallyDerivedProduct.getPath())) {
			proxy = new LinkedResourceBiologicallyDerivedProduct();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.BodyStructure.getPath())) {
			proxy = new LinkedResourceBodyStructure();
		}
		// Bundle - no references
		// CapabilityStatement - conformance resource
		if (resourceType.equalsIgnoreCase(ResourceType.CarePlan.getPath())) {
			proxy = new LinkedResourceCarePlan();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.CareTeam.getPath())) {
			proxy = new LinkedResourceCareTeam();
		}
		// CatalogEntry - collections resource
		if (resourceType.equalsIgnoreCase(ResourceType.ChargeItem.getPath())) {
			proxy = new LinkedResourceChargeItem();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.ChargeItemDefinition.getPath())) {
			proxy = new LinkedResourceChargeItemDefinition();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.Claim.getPath())) {
			proxy = new LinkedResourceClaim();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.ClaimResponse.getPath())) {
			proxy = new LinkedResourceClaimResponse();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.ClinicalImpression.getPath())) {
			proxy = new LinkedResourceClinicalImpression();
		}
		// CodeSystem - conformance resource
		if (resourceType.equalsIgnoreCase(ResourceType.Communication.getPath())) {
			proxy = new LinkedResourceCommunication();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.CommunicationRequest.getPath())) {
			proxy = new LinkedResourceCommunicationRequest();
		}
		// CompartmentDefinition - conformance resource
		// Composition - used specifically for FHIR documents
		// ConceptMap - conformance resource
		if (resourceType.equalsIgnoreCase(ResourceType.Condition.getPath())) {
			proxy = new LinkedResourceCondition();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.Consent.getPath())) {
			proxy = new LinkedResourceConsent();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.Contract.getPath())) {
			proxy = new LinkedResourceContract();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.Coverage.getPath())) {
			proxy = new LinkedResourceCoverage();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.CoverageEligibilityRequest.getPath())) {
			proxy = new LinkedResourceCoverageEligibilityRequest();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.CoverageEligibilityResponse.getPath())) {
			proxy = new LinkedResourceCoverageEligibilityResponse();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.DetectedIssue.getPath())) {
			proxy = new LinkedResourceDetectedIssue();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.Device.getPath())) {
			proxy = new LinkedResourceDevice();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.DeviceDefinition.getPath())) {
			proxy = new LinkedResourceDeviceDefinition();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.DeviceMetric.getPath())) {
			proxy = new LinkedResourceDeviceMetric();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.DeviceRequest.getPath())) {
			proxy = new LinkedResourceDeviceRequest();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.DeviceUseStatement.getPath())) {
			proxy = new LinkedResourceDeviceUseStatement();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.DiagnosticReport.getPath())) {
			proxy = new LinkedResourceDiagnosticReport();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.DocumentManifest.getPath())) {
			proxy = new LinkedResourceDocumentManifest();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.DocumentReference.getPath())) {
			proxy = new LinkedResourceDocumentReference();
		}
		// EffectEvidenceSynthesis - used in research studies
		if (resourceType.equalsIgnoreCase(ResourceType.Encounter.getPath())) {
			proxy = new LinkedResourceEncounter();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.Endpoint.getPath())) {
			proxy = new LinkedResourceEndpoint();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.EnrollmentRequest.getPath())) {
			proxy = new LinkedResourceEnrollmentRequest();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.EnrollmentResponse.getPath())) {
			proxy = new LinkedResourceEnrollmentResponse();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.EpisodeOfCare.getPath())) {
			proxy = new LinkedResourceEpisodeOfCare();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.EventDefinition.getPath())) {
			proxy = new LinkedResourceEventDefinition();
		}
		// Evidence - used in research studies
		// EvidenceVariable - used in research studies
		// ExampleScenario - used in workflow processing
		if (resourceType.equalsIgnoreCase(ResourceType.ExplanationOfBenefit.getPath())) {
			proxy = new LinkedResourceExplanationOfBenefit();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.FamilyMemberHistory.getPath())) {
			proxy = new LinkedResourceFamilyMemberHistory();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.Flag.getPath())) {
			proxy = new LinkedResourceFlag();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.Goal.getPath())) {
			proxy = new LinkedResourceGoal();
		}
		// GraphDefinition - infrastructure resource
		if (resourceType.equalsIgnoreCase(ResourceType.Group.getPath())) {
			proxy = new LinkedResourceGroup();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.GuidanceResponse.getPath())) {
			proxy = new LinkedResourceGuidanceResponse();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.HealthcareService.getPath())) {
			proxy = new LinkedResourceHealthcareService();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.ImagingStudy.getPath())) {
			proxy = new LinkedResourceImagingStudy();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.Immunization.getPath())) {
			proxy = new LinkedResourceImmunization();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.ImmunizationEvaluation.getPath())) {
			proxy = new LinkedResourceImmunizationEvaluation();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.ImmunizationRecommendation.getPath())) {
			proxy = new LinkedResourceImmunizationRecommendation();
		}
		// ImplementationGuide - conformance resource
		if (resourceType.equalsIgnoreCase(ResourceType.InsurancePlan.getPath())) {
			proxy = new LinkedResourceInsurancePlan();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.Invoice.getPath())) {
			proxy = new LinkedResourceInvoice();
		}
		// Library - used for knowledge assets
		if (resourceType.equalsIgnoreCase(ResourceType.Linkage.getPath())) {
			proxy = new LinkedResourceLinkage();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.List.getPath())) {
			proxy = new LinkedResourceList();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.Location.getPath())) {
			proxy = new LinkedResourceLocation();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.Measure.getPath())) {
			proxy = new LinkedResourceMeasure();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.MeasureReport.getPath())) {
			proxy = new LinkedResourceMeasureReport();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.Media.getPath())) {
			proxy = new LinkedResourceMedia();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.Medication.getPath())) {
			proxy = new LinkedResourceMedication();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.MedicationAdministration.getPath())) {
			proxy = new LinkedResourceMedicationAdministration();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.MedicationDispense.getPath())) {
			proxy = new LinkedResourceMedicationDispense();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.MedicationKnowledge.getPath())) {
			proxy = new LinkedResourceMedicationKnowledge();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.MedicationRequest.getPath())) {
			proxy = new LinkedResourceMedicationRequest();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.MedicationStatement.getPath())) {
			proxy = new LinkedResourceMedicationStatement();
		}
		// MedicinalProduct - draft resource
		// MedicinalProductAuthorization - draft resource
		// MedicinalProductClinicals - draft resource
		// MedicinalProductContraindication - draft resource
		// MedicinalProductDeviceSpec - draft resource
		// MedicinalProductIndication - draft resource
		// MedicinalProductIngredient - draft resource
		// MedicinalProductInteraction - draft resource
		// MedicinalProductManufactured - draft resource
		// MedicinalProductPackaged - draft resource
		// MedicinalProductPharmaceutical - draft resource
		// MedicinalProductUndesirableEffect - draft resource
		// MessageDefinition - used specifically for FHIR messages
		// MessageHeader - used specifically for FHIR messages
		if (resourceType.equalsIgnoreCase(ResourceType.MolecularSequence.getPath())) {
			proxy = new LinkedResourceMolecularSequence();
		}
		// NamingSystem - conformance resource
		if (resourceType.equalsIgnoreCase(ResourceType.NutritionOrder.getPath())) {
			proxy = new LinkedResourceNutritionOrder();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.Observation.getPath())) {
			proxy = new LinkedResourceObservation();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.ObservationDefinition.getPath())) {
			proxy = new LinkedResourceObservationDefinition();
		}
		// OperationDefinition - conformance resource
		// OperationOutcome - conformance resource
		if (resourceType.equalsIgnoreCase(ResourceType.Organization.getPath())) {
			proxy = new LinkedResourceOrganization();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.OrganizationAffiliation.getPath())) {
			proxy = new LinkedResourceOrganizationAffiliation();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.Patient.getPath())) {
			proxy = new LinkedResourcePatient();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.PaymentNotice.getPath())) {
			proxy = new LinkedResourcePaymentNotice();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.PaymentReconciliation.getPath())) {
			proxy = new LinkedResourcePaymentReconciliation();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.Person.getPath())) {
			proxy = new LinkedResourcePerson();
		}
		// PlanDefinition - used specifically for workflow processing
		if (resourceType.equalsIgnoreCase(ResourceType.Practitioner.getPath())) {
			proxy = new LinkedResourcePractitioner();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.PractitionerRole.getPath())) {
			proxy = new LinkedResourcePractitionerRole();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.Procedure.getPath())) {
			proxy = new LinkedResourceProcedure();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.Provenance.getPath())) {
			proxy = new LinkedResourceProvenance();
		}
		// Questionnaire - used for specific question processing
		// QuestionnaireResponse - used for specific question processing
		if (resourceType.equalsIgnoreCase(ResourceType.RelatedPerson.getPath())) {
			proxy = new LinkedResourceRelatedPerson();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.RequestGroup.getPath())) {
			proxy = new LinkedResourceRequestGroup();
		}
		// ResearchDefinition - used specifically for workflow processing
		// ResearchElementDefinition - used specifically for workflow processing
		if (resourceType.equalsIgnoreCase(ResourceType.ResearchStudy.getPath())) {
			proxy = new LinkedResourceResearchStudy();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.ResearchSubject.getPath())) {
			proxy = new LinkedResourceResearchSubject();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.RiskAssessment.getPath())) {
			proxy = new LinkedResourceRiskAssessment();
		}
		// RiskEvidenceSynthesis - used in research studies
		if (resourceType.equalsIgnoreCase(ResourceType.Schedule.getPath())) {
			proxy = new LinkedResourceSchedule();
		}
		// SearchParameter - conformance resource
		if (resourceType.equalsIgnoreCase(ResourceType.ServiceRequest.getPath())) {
			proxy = new LinkedResourceServiceRequest();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.Slot.getPath())) {
			proxy = new LinkedResourceSlot();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.Specimen.getPath())) {
			proxy = new LinkedResourceSpecimen();
		}
		// SpecimenDefinition - used specifically for workflow processing
		// StructureDefinition - conformance resource
		// StructureMap - conformance resource
		// Subscription - no reference elements
		if (resourceType.equalsIgnoreCase(ResourceType.Substance.getPath())) {
			proxy = new LinkedResourceSubstance();
		}
		// SubstancePolymer - no reference elements
		// SubstanceProtein - no reference elements
		// SubstanceReferenceInformation - draft resource
		// SubstanceSpecification - draft resource
		if (resourceType.equalsIgnoreCase(ResourceType.SupplyDelivery.getPath())) {
			proxy = new LinkedResourceSupplyDelivery();
		}
		if (resourceType.equalsIgnoreCase(ResourceType.SupplyRequest.getPath())) {
			proxy = new LinkedResourceSupplyRequest();
		}
		// Task - used specifically for workflow processing
		// TerminologyCapabilities - conformance resource
		// TestReport - testing resource
		// TestScript - testing resource
		// ValueSet - conformance resource
		// VerificationResult - draft resource
		if (resourceType.equalsIgnoreCase(ResourceType.VisionPrescription.getPath())) {
			proxy = new LinkedResourceVisionPrescription();
		}

		return proxy;
	}

}
