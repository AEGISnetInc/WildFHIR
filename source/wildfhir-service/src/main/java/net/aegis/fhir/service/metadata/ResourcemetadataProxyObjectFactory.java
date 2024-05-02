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
package net.aegis.fhir.service.metadata;

import org.hl7.fhir.r4.model.ResourceType;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataProxyObjectFactory {

    /**
     * Return an instance of the ResourcemetadataProxy class.
     *
     * @return An instance of the ResourcemetadataProxy class.
     */
	public ResourcemetadataProxy getResourcemetadataProxy(String resourceType) {

		ResourcemetadataProxy proxy = null;

		if (resourceType != null) {
			if (resourceType.equalsIgnoreCase(ResourceType.Account.getPath())) {
				proxy = new ResourcemetadataAccount();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.ActivityDefinition.getPath())) {
				proxy = new ResourcemetadataActivityDefinition();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.AdverseEvent.getPath())) {
				proxy = new ResourcemetadataAdverseEvent();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.AllergyIntolerance.getPath())) {
				proxy = new ResourcemetadataAllergyIntolerance();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Appointment.getPath())) {
				proxy = new ResourcemetadataAppointment();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.AppointmentResponse.getPath())) {
				proxy = new ResourcemetadataAppointmentResponse();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.AuditEvent.getPath())) {
				proxy = new ResourcemetadataAuditEvent();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Basic.getPath())) {
				proxy = new ResourcemetadataBasic();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Binary.getPath())) {
				proxy = new ResourcemetadataBinary();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.BiologicallyDerivedProduct.getPath())) {
				proxy = new ResourcemetadataBiologicallyDerivedProduct();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.BodyStructure.getPath())) {
				proxy = new ResourcemetadataBodyStructure();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Bundle.getPath())) {
				proxy = new ResourcemetadataBundle();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.CapabilityStatement.getPath())) {
				proxy = new ResourcemetadataCapabilityStatement();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.CarePlan.getPath())) {
				proxy = new ResourcemetadataCarePlan();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.CareTeam.getPath())) {
				proxy = new ResourcemetadataCareTeam();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.CatalogEntry.getPath())) {
				proxy = new ResourcemetadataCatalogEntry();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.ChargeItem.getPath())) {
				proxy = new ResourcemetadataChargeItem();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.ChargeItemDefinition.getPath())) {
				proxy = new ResourcemetadataChargeItemDefinition();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Claim.getPath())) {
				proxy = new ResourcemetadataClaim();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.ClaimResponse.getPath())) {
				proxy = new ResourcemetadataClaimResponse();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.ClinicalImpression.getPath())) {
				proxy = new ResourcemetadataClinicalImpression();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.CodeSystem.getPath())) {
				proxy = new ResourcemetadataCodeSystem();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Communication.getPath())) {
				proxy = new ResourcemetadataCommunication();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.CommunicationRequest.getPath())) {
				proxy = new ResourcemetadataCommunicationRequest();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.CompartmentDefinition.getPath())) {
				proxy = new ResourcemetadataCompartmentDefinition();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Composition.getPath())) {
				proxy = new ResourcemetadataComposition();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.ConceptMap.getPath())) {
				proxy = new ResourcemetadataConceptMap();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Condition.getPath())) {
				proxy = new ResourcemetadataCondition();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Consent.getPath())) {
				proxy = new ResourcemetadataConsent();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Contract.getPath())) {
				proxy = new ResourcemetadataContract();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Coverage.getPath())) {
				proxy = new ResourcemetadataCoverage();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.CoverageEligibilityRequest.getPath())) {
				proxy = new ResourcemetadataCoverageEligibilityRequest();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.CoverageEligibilityResponse.getPath())) {
				proxy = new ResourcemetadataCoverageEligibilityResponse();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.DetectedIssue.getPath())) {
				proxy = new ResourcemetadataDetectedIssue();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Device.getPath())) {
				proxy = new ResourcemetadataDevice();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.DeviceDefinition.getPath())) {
				proxy = new ResourcemetadataDeviceDefinition();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.DeviceMetric.getPath())) {
				proxy = new ResourcemetadataDeviceMetric();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.DeviceRequest.getPath())) {
				proxy = new ResourcemetadataDeviceRequest();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.DeviceUseStatement.getPath())) {
				proxy = new ResourcemetadataDeviceUseStatement();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.DiagnosticReport.getPath())) {
				proxy = new ResourcemetadataDiagnosticReport();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.DocumentManifest.getPath())) {
				proxy = new ResourcemetadataDocumentManifest();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.DocumentReference.getPath())) {
				proxy = new ResourcemetadataDocumentReference();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.EffectEvidenceSynthesis.getPath())) {
				proxy = new ResourcemetadataEffectEvidenceSynthesis();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Encounter.getPath())) {
				proxy = new ResourcemetadataEncounter();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Endpoint.getPath())) {
				proxy = new ResourcemetadataEndpoint();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.EnrollmentRequest.getPath())) {
				proxy = new ResourcemetadataEnrollmentRequest();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.EnrollmentResponse.getPath())) {
				proxy = new ResourcemetadataEnrollmentResponse();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.EpisodeOfCare.getPath())) {
				proxy = new ResourcemetadataEpisodeOfCare();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.EventDefinition.getPath())) {
				proxy = new ResourcemetadataEventDefinition();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Evidence.getPath())) {
				proxy = new ResourcemetadataEvidence();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.EvidenceVariable.getPath())) {
				proxy = new ResourcemetadataEvidenceVariable();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.ExampleScenario.getPath())) {
				proxy = new ResourcemetadataExampleScenario();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.ExplanationOfBenefit.getPath())) {
				proxy = new ResourcemetadataExplanationOfBenefit();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.FamilyMemberHistory.getPath())) {
				proxy = new ResourcemetadataFamilyMemberHistory();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Flag.getPath())) {
				proxy = new ResourcemetadataFlag();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Goal.getPath())) {
				proxy = new ResourcemetadataGoal();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.GraphDefinition.getPath())) {
				proxy = new ResourcemetadataGraphDefinition();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Group.getPath())) {
				proxy = new ResourcemetadataGroup();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.GuidanceResponse.getPath())) {
				proxy = new ResourcemetadataGuidanceResponse();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.HealthcareService.getPath())) {
				proxy = new ResourcemetadataHealthcareService();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.ImagingStudy.getPath())) {
				proxy = new ResourcemetadataImagingStudy();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Immunization.getPath())) {
				proxy = new ResourcemetadataImmunization();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.ImmunizationEvaluation.getPath())) {
				proxy = new ResourcemetadataImmunizationEvaluation();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.ImmunizationRecommendation.getPath())) {
				proxy = new ResourcemetadataImmunizationRecommendation();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.ImplementationGuide.getPath())) {
				proxy = new ResourcemetadataImplementationGuide();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Invoice.getPath())) {
				proxy = new ResourcemetadataInvoice();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.InsurancePlan.getPath())) {
				proxy = new ResourcemetadataInsurancePlan();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Library.getPath())) {
				proxy = new ResourcemetadataLibrary();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Linkage.getPath())) {
				proxy = new ResourcemetadataLinkage();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.List.getPath())) {
				proxy = new ResourcemetadataList();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Location.getPath())) {
				proxy = new ResourcemetadataLocation();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Measure.getPath())) {
				proxy = new ResourcemetadataMeasure();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.MeasureReport.getPath())) {
				proxy = new ResourcemetadataMeasureReport();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Media.getPath())) {
				proxy = new ResourcemetadataMedia();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Medication.getPath())) {
				proxy = new ResourcemetadataMedication();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.MedicationAdministration.getPath())) {
				proxy = new ResourcemetadataMedicationAdministration();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.MedicationDispense.getPath())) {
				proxy = new ResourcemetadataMedicationDispense();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.MedicationKnowledge.getPath())) {
				proxy = new ResourcemetadataMedicationKnowledge();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.MedicationRequest.getPath())) {
				proxy = new ResourcemetadataMedicationRequest();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.MedicationStatement.getPath())) {
				proxy = new ResourcemetadataMedicationStatement();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.MedicinalProduct.getPath())) {
				proxy = new ResourcemetadataMedicinalProduct();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.MedicinalProductAuthorization.getPath())) {
				proxy = new ResourcemetadataMedicinalProductAuthorization();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.MedicinalProductContraindication.getPath())) {
				proxy = new ResourcemetadataMedicinalProductContraindication();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.MedicinalProductIndication.getPath())) {
				proxy = new ResourcemetadataMedicinalProductIndication();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.MedicinalProductIngredient.getPath())) {
				proxy = new ResourcemetadataMedicinalProductIngredient();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.MedicinalProductInteraction.getPath())) {
				proxy = new ResourcemetadataMedicinalProductInteraction();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.MedicinalProductManufactured.getPath())) {
				proxy = new ResourcemetadataMedicinalProductManufactured();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.MedicinalProductPackaged.getPath())) {
				proxy = new ResourcemetadataMedicinalProductPackaged();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.MedicinalProductPharmaceutical.getPath())) {
				proxy = new ResourcemetadataMedicinalProductPharmaceutical();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.MedicinalProductUndesirableEffect.getPath())) {
				proxy = new ResourcemetadataMedicinalProductUndesirableEffect();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.MessageDefinition.getPath())) {
				proxy = new ResourcemetadataMessageDefinition();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.MessageHeader.getPath())) {
				proxy = new ResourcemetadataMessageHeader();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.MolecularSequence.getPath())) {
				proxy = new ResourcemetadataMolecularSequence();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.NamingSystem.getPath())) {
				proxy = new ResourcemetadataNamingSystem();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.NutritionOrder.getPath())) {
				proxy = new ResourcemetadataNutritionOrder();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Observation.getPath())) {
				proxy = new ResourcemetadataObservation();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.ObservationDefinition.getPath())) {
				proxy = new ResourcemetadataObservationDefinition();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.OperationDefinition.getPath())) {
				proxy = new ResourcemetadataOperationDefinition();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.OperationOutcome.getPath())) {
				proxy = new ResourcemetadataOperationOutcome();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Organization.getPath())) {
				proxy = new ResourcemetadataOrganization();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.OrganizationAffiliation.getPath())) {
				proxy = new ResourcemetadataOrganizationAffiliation();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Patient.getPath())) {
				proxy = new ResourcemetadataPatient();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Patient.getPath() + "Match")) {
				proxy = new ResourcemetadataPatientMatch();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.PaymentNotice.getPath())) {
				proxy = new ResourcemetadataPaymentNotice();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.PaymentReconciliation.getPath())) {
				proxy = new ResourcemetadataPaymentReconciliation();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Person.getPath())) {
				proxy = new ResourcemetadataPerson();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.PlanDefinition.getPath())) {
				proxy = new ResourcemetadataPlanDefinition();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Practitioner.getPath())) {
				proxy = new ResourcemetadataPractitioner();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.PractitionerRole.getPath())) {
				proxy = new ResourcemetadataPractitionerRole();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Procedure.getPath())) {
				proxy = new ResourcemetadataProcedure();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Provenance.getPath())) {
				proxy = new ResourcemetadataProvenance();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Questionnaire.getPath())) {
				proxy = new ResourcemetadataQuestionnaire();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.QuestionnaireResponse.getPath())) {
				proxy = new ResourcemetadataQuestionnaireResponse();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.RelatedPerson.getPath())) {
				proxy = new ResourcemetadataRelatedPerson();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.RequestGroup.getPath())) {
				proxy = new ResourcemetadataRequestGroup();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.ResearchDefinition.getPath())) {
				proxy = new ResourcemetadataResearchDefinition();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.ResearchElementDefinition.getPath())) {
				proxy = new ResourcemetadataResearchElementDefinition();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.ResearchStudy.getPath())) {
				proxy = new ResourcemetadataResearchStudy();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.ResearchSubject.getPath())) {
				proxy = new ResourcemetadataResearchSubject();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.RiskAssessment.getPath())) {
				proxy = new ResourcemetadataRiskAssessment();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.RiskEvidenceSynthesis.getPath())) {
				proxy = new ResourcemetadataRiskEvidenceSynthesis();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Schedule.getPath())) {
				proxy = new ResourcemetadataSchedule();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.SearchParameter.getPath())) {
				proxy = new ResourcemetadataSearchParameter();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.ServiceRequest.getPath())) {
				proxy = new ResourcemetadataServiceRequest();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Slot.getPath())) {
				proxy = new ResourcemetadataSlot();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Specimen.getPath())) {
				proxy = new ResourcemetadataSpecimen();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.SpecimenDefinition.getPath())) {
				proxy = new ResourcemetadataSpecimenDefinition();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.StructureDefinition.getPath())) {
				proxy = new ResourcemetadataStructureDefinition();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.StructureMap.getPath())) {
				proxy = new ResourcemetadataStructureMap();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Subscription.getPath())) {
				proxy = new ResourcemetadataSubscription();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Substance.getPath())) {
				proxy = new ResourcemetadataSubstance();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.SubstanceNucleicAcid.getPath())) {
				proxy = new ResourcemetadataSubstanceNucleicAcid();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.SubstancePolymer.getPath())) {
				proxy = new ResourcemetadataSubstancePolymer();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.SubstanceProtein.getPath())) {
				proxy = new ResourcemetadataSubstanceProtein();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.SubstanceReferenceInformation.getPath())) {
				proxy = new ResourcemetadataSubstanceReferenceInformation();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.SubstanceSourceMaterial.getPath())) {
				proxy = new ResourcemetadataSubstanceSourceMaterial();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.SubstanceSpecification.getPath())) {
				proxy = new ResourcemetadataSubstanceSpecification();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.SupplyDelivery.getPath())) {
				proxy = new ResourcemetadataSupplyDelivery();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.SupplyRequest.getPath())) {
				proxy = new ResourcemetadataSupplyRequest();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.Task.getPath())) {
				proxy = new ResourcemetadataTask();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.TerminologyCapabilities.getPath())) {
				proxy = new ResourcemetadataTerminologyCapabilities();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.TestReport.getPath())) {
				proxy = new ResourcemetadataTestReport();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.TestScript.getPath())) {
				proxy = new ResourcemetadataTestScript();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.ValueSet.getPath())) {
				proxy = new ResourcemetadataValueSet();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.VerificationResult.getPath())) {
				proxy = new ResourcemetadataVerificationResult();
			}
			if (resourceType.equalsIgnoreCase(ResourceType.VisionPrescription.getPath())) {
				proxy = new ResourcemetadataVisionPrescription();
			}
		}

		return proxy;
	}

}
