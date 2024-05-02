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
package net.aegis.fhir.service.summary;

import org.hl7.fhir.r4.model.ResourceType;

/**
 * @author richard.ettema
 *
 */
public class ResourceSummaryProxyObjectFactory {

	/**
	 * Return an instance of the ResourceSummaryProxy class.
	 *
	 * @return An instance of the ResourceSummaryProxy class.
	 */
	public ResourceSummaryProxy getResourceSummaryProxy(String resourceType) {

		ResourceSummaryProxy proxy = null;

		if (resourceType != null) {
			if (resourceType.equalsIgnoreCase(ResourceType.Account.getPath())) {
				proxy = new ResourceSummaryAccount();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.ActivityDefinition.getPath())) {
				proxy = new ResourceSummaryActivityDefinition();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.AdverseEvent.getPath())) {
				proxy = new ResourceSummaryAdverseEvent();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.AllergyIntolerance.getPath())) {
				proxy = new ResourceSummaryAllergyIntolerance();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Appointment.getPath())) {
				proxy = new ResourceSummaryAppointment();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.AppointmentResponse.getPath())) {
				proxy = new ResourceSummaryAppointmentResponse();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.AuditEvent.getPath())) {
				proxy = new ResourceSummaryAuditEvent();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Basic.getPath())) {
				proxy = new ResourceSummaryBasic();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Binary.getPath())) {
				proxy = new ResourceSummaryBinary();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.BiologicallyDerivedProduct.getPath())) {
				proxy = new ResourceSummaryBiologicallyDerivedProduct();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.BodyStructure.getPath())) {
				proxy = new ResourceSummaryBodyStructure();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Bundle.getPath())) {
				proxy = new ResourceSummaryBundle();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.CapabilityStatement.getPath())) {
				proxy = new ResourceSummaryCapabilityStatement();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.CarePlan.getPath())) {
				proxy = new ResourceSummaryCarePlan();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.CareTeam.getPath())) {
				proxy = new ResourceSummaryCareTeam();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.CatalogEntry.getPath())) {
				proxy = new ResourceSummaryCatalogEntry();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.ChargeItem.getPath())) {
				proxy = new ResourceSummaryChargeItem();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.ChargeItemDefinition.getPath())) {
				proxy = new ResourceSummaryChargeItemDefinition();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Claim.getPath())) {
				proxy = new ResourceSummaryClaim();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.ClaimResponse.getPath())) {
				proxy = new ResourceSummaryClaimResponse();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.ClinicalImpression.getPath())) {
				proxy = new ResourceSummaryClinicalImpression();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.CodeSystem.getPath())) {
				proxy = new ResourceSummaryCodeSystem();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Communication.getPath())) {
				proxy = new ResourceSummaryCommunication();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.CommunicationRequest.getPath())) {
				proxy = new ResourceSummaryCommunicationRequest();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.CompartmentDefinition.getPath())) {
				proxy = new ResourceSummaryCompartmentDefinition();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Composition.getPath())) {
				proxy = new ResourceSummaryComposition();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.ConceptMap.getPath())) {
				proxy = new ResourceSummaryConceptMap();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Condition.getPath())) {
				proxy = new ResourceSummaryCondition();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Consent.getPath())) {
				proxy = new ResourceSummaryConsent();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Contract.getPath())) {
				proxy = new ResourceSummaryContract();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Coverage.getPath())) {
				proxy = new ResourceSummaryCoverage();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.CoverageEligibilityRequest.getPath())) {
				proxy = new ResourceSummaryCoverageEligibilityRequest();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.CoverageEligibilityResponse.getPath())) {
				proxy = new ResourceSummaryCoverageEligibilityResponse();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.DetectedIssue.getPath())) {
				proxy = new ResourceSummaryDetectedIssue();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Device.getPath())) {
				proxy = new ResourceSummaryDevice();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.DeviceDefinition.getPath())) {
				proxy = new ResourceSummaryDeviceDefinition();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.DeviceMetric.getPath())) {
				proxy = new ResourceSummaryDeviceMetric();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.DeviceRequest.getPath())) {
				proxy = new ResourceSummaryDeviceRequest();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.DeviceUseStatement.getPath())) {
				proxy = new ResourceSummaryDeviceUseStatement();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.DiagnosticReport.getPath())) {
				proxy = new ResourceSummaryDiagnosticReport();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.DocumentManifest.getPath())) {
				proxy = new ResourceSummaryDocumentManifest();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.DocumentReference.getPath())) {
				proxy = new ResourceSummaryDocumentReference();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.EffectEvidenceSynthesis.getPath())) {
				proxy = new ResourceSummaryEffectEvidenceSynthesis();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Encounter.getPath())) {
				proxy = new ResourceSummaryEncounter();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Endpoint.getPath())) {
				proxy = new ResourceSummaryEndpoint();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.EnrollmentRequest.getPath())) {
				proxy = new ResourceSummaryEnrollmentRequest();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.EnrollmentResponse.getPath())) {
				proxy = new ResourceSummaryEnrollmentResponse();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.EpisodeOfCare.getPath())) {
				proxy = new ResourceSummaryEpisodeOfCare();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.EventDefinition.getPath())) {
				proxy = new ResourceSummaryEventDefinition();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Evidence.getPath())) {
				proxy = new ResourceSummaryEvidence();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.EvidenceVariable.getPath())) {
				proxy = new ResourceSummaryEvidenceVariable();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.ExampleScenario.getPath())) {
				proxy = new ResourceSummaryExampleScenario();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.ExplanationOfBenefit.getPath())) {
				proxy = new ResourceSummaryExplanationOfBenefit();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.FamilyMemberHistory.getPath())) {
				proxy = new ResourceSummaryFamilyMemberHistory();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Flag.getPath())) {
				proxy = new ResourceSummaryFlag();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Goal.getPath())) {
				proxy = new ResourceSummaryGoal();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.GraphDefinition.getPath())) {
				proxy = new ResourceSummaryGraphDefinition();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Group.getPath())) {
				proxy = new ResourceSummaryGroup();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.GuidanceResponse.getPath())) {
				proxy = new ResourceSummaryGuidanceResponse();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.HealthcareService.getPath())) {
				proxy = new ResourceSummaryHealthcareService();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.ImagingStudy.getPath())) {
				proxy = new ResourceSummaryImagingStudy();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Immunization.getPath())) {
				proxy = new ResourceSummaryImmunization();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.ImmunizationEvaluation.getPath())) {
				proxy = new ResourceSummaryImmunizationEvaluation();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.ImmunizationRecommendation.getPath())) {
				proxy = new ResourceSummaryImmunizationRecommendation();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.ImplementationGuide.getPath())) {
				proxy = new ResourceSummaryImplementationGuide();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.InsurancePlan.getPath())) {
				proxy = new ResourceSummaryInsurancePlan();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Invoice.getPath())) {
				proxy = new ResourceSummaryInvoice();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Library.getPath())) {
				proxy = new ResourceSummaryLibrary();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Linkage.getPath())) {
				proxy = new ResourceSummaryLinkage();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.List.getPath())) {
				proxy = new ResourceSummaryList();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Location.getPath())) {
				proxy = new ResourceSummaryLocation();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Measure.getPath())) {
				proxy = new ResourceSummaryMeasure();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.MeasureReport.getPath())) {
				proxy = new ResourceSummaryMeasureReport();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Media.getPath())) {
				proxy = new ResourceSummaryMedia();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Medication.getPath())) {
				proxy = new ResourceSummaryMedication();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.MedicationAdministration.getPath())) {
				proxy = new ResourceSummaryMedicationAdministration();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.MedicationDispense.getPath())) {
				proxy = new ResourceSummaryMedicationDispense();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.MedicationKnowledge.getPath())) {
				proxy = new ResourceSummaryMedicationKnowledge();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.MedicationRequest.getPath())) {
				proxy = new ResourceSummaryMedicationRequest();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.MedicationStatement.getPath())) {
				proxy = new ResourceSummaryMedicationStatement();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.MedicinalProduct.getPath())) {
				proxy = new ResourceSummaryMedicinalProduct();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.MedicinalProductAuthorization.getPath())) {
				proxy = new ResourceSummaryMedicinalProductAuthorization();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.MedicinalProductContraindication.getPath())) {
				proxy = new ResourceSummaryMedicinalProductContraindication();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.MedicinalProductIndication.getPath())) {
				proxy = new ResourceSummaryMedicinalProductIndication();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.MedicinalProductIngredient.getPath())) {
				proxy = new ResourceSummaryMedicinalProductIngredient();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.MedicinalProductInteraction.getPath())) {
				proxy = new ResourceSummaryMedicinalProductInteraction();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.MedicinalProductManufactured.getPath())) {
				proxy = new ResourceSummaryMedicinalProductManufactured();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.MedicinalProductPackaged.getPath())) {
				proxy = new ResourceSummaryMedicinalProductPackaged();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.MedicinalProductPharmaceutical.getPath())) {
				proxy = new ResourceSummaryMedicinalProductPharmaceutical();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.MedicinalProductUndesirableEffect.getPath())) {
				proxy = new ResourceSummaryMedicinalProductUndesirableEffect();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.MessageDefinition.getPath())) {
				proxy = new ResourceSummaryMessageDefinition();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.MessageHeader.getPath())) {
				proxy = new ResourceSummaryMessageHeader();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.MolecularSequence.getPath())) {
				proxy = new ResourceSummaryMolecularSequence();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.NamingSystem.getPath())) {
				proxy = new ResourceSummaryNamingSystem();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.NutritionOrder.getPath())) {
				proxy = new ResourceSummaryNutritionOrder();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Observation.getPath())) {
				proxy = new ResourceSummaryObservation();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.ObservationDefinition.getPath())) {
				proxy = new ResourceSummaryObservationDefinition();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.OperationDefinition.getPath())) {
				proxy = new ResourceSummaryOperationDefinition();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.OperationOutcome.getPath())) {
				proxy = new ResourceSummaryOperationOutcome();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Organization.getPath())) {
				proxy = new ResourceSummaryOrganization();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.OrganizationAffiliation.getPath())) {
				proxy = new ResourceSummaryOrganizationAffiliation();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Patient.getPath())) {
				proxy = new ResourceSummaryPatient();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.PaymentNotice.getPath())) {
				proxy = new ResourceSummaryPaymentNotice();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.PaymentReconciliation.getPath())) {
				proxy = new ResourceSummaryPaymentReconciliation();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Person.getPath())) {
				proxy = new ResourceSummaryPerson();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.PlanDefinition.getPath())) {
				proxy = new ResourceSummaryPlanDefinition();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Practitioner.getPath())) {
				proxy = new ResourceSummaryPractitioner();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.PractitionerRole.getPath())) {
				proxy = new ResourceSummaryPractitionerRole();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Procedure.getPath())) {
				proxy = new ResourceSummaryProcedure();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Provenance.getPath())) {
				proxy = new ResourceSummaryProvenance();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Questionnaire.getPath())) {
				proxy = new ResourceSummaryQuestionnaire();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.QuestionnaireResponse.getPath())) {
				proxy = new ResourceSummaryQuestionnaireResponse();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.RelatedPerson.getPath())) {
				proxy = new ResourceSummaryRelatedPerson();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.RequestGroup.getPath())) {
				proxy = new ResourceSummaryRequestGroup();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.ResearchDefinition.getPath())) {
				proxy = new ResourceSummaryResearchDefinition();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.ResearchElementDefinition.getPath())) {
				proxy = new ResourceSummaryResearchElementDefinition();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.ResearchStudy.getPath())) {
				proxy = new ResourceSummaryResearchStudy();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.ResearchSubject.getPath())) {
				proxy = new ResourceSummaryResearchSubject();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.RiskAssessment.getPath())) {
				proxy = new ResourceSummaryRiskAssessment();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.RiskEvidenceSynthesis.getPath())) {
				proxy = new ResourceSummaryRiskEvidenceSynthesis();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Schedule.getPath())) {
				proxy = new ResourceSummarySchedule();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.SearchParameter.getPath())) {
				proxy = new ResourceSummarySearchParameter();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.ServiceRequest.getPath())) {
				proxy = new ResourceSummaryServiceRequest();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Slot.getPath())) {
				proxy = new ResourceSummarySlot();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Specimen.getPath())) {
				proxy = new ResourceSummarySpecimen();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.SpecimenDefinition.getPath())) {
				proxy = new ResourceSummarySpecimenDefinition();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.StructureDefinition.getPath())) {
				proxy = new ResourceSummaryStructureDefinition();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.StructureMap.getPath())) {
				proxy = new ResourceSummaryStructureMap();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Subscription.getPath())) {
				proxy = new ResourceSummarySubscription();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Substance.getPath())) {
				proxy = new ResourceSummarySubstance();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.SubstanceNucleicAcid.getPath())) {
				proxy = new ResourceSummarySubstanceNucleicAcid();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.SubstancePolymer.getPath())) {
				proxy = new ResourceSummarySubstancePolymer();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.SubstanceProtein.getPath())) {
				proxy = new ResourceSummarySubstanceProtein();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.SubstanceReferenceInformation.getPath())) {
				proxy = new ResourceSummarySubstanceReferenceInformation();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.SubstanceSourceMaterial.getPath())) {
				proxy = new ResourceSummarySubstanceSourceMaterial();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.SubstanceSpecification.getPath())) {
				proxy = new ResourceSummarySubstanceSpecification();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.SupplyDelivery.getPath())) {
				proxy = new ResourceSummarySupplyDelivery();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.SupplyRequest.getPath())) {
				proxy = new ResourceSummarySupplyRequest();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Task.getPath())) {
				proxy = new ResourceSummaryTask();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.TerminologyCapabilities.getPath())) {
				proxy = new ResourceSummaryTerminologyCapabilities();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.TestReport.getPath())) {
				proxy = new ResourceSummaryTestReport();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.TestScript.getPath())) {
				proxy = new ResourceSummaryTestScript();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.ValueSet.getPath())) {
				proxy = new ResourceSummaryValueSet();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.VerificationResult.getPath())) {
				proxy = new ResourceSummaryVerificationResult();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.VisionPrescription.getPath())) {
				proxy = new ResourceSummaryVisionPrescription();
			}
		}

		return proxy;
	}

}
