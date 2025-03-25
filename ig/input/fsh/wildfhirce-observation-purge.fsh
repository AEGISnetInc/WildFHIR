Instance: WildFHIRCEObservationPurge
InstanceOf: Observation
Description: "WildFHIR CE Observation Purge"
Usage: #example
* id = "wildfhirce-observation-purge"
* status = #final
* code = http://loinc.org#11332-4
* subject.reference = "Patient/wildfhirce-patient-purge"
* effectiveDateTime = "2025-03-24T11:15:00-04:00"
* performer[0].reference = "Patient/wildfhirce-patient-purge"
* valueString = "I feel alert and am under no stress."
