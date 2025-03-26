Instance: WildFHIRCEPatientPurge
InstanceOf: Patient
Description: "WildFHIR CE Patient Purge"
Usage: #example
* id = "wildfhirce-patient-purge"
* active = true
* name[0].family = "Purged"
* name[0].given[0] = "Tubee"
* gender = http://hl7.org/fhir/administrative-gender#male
* birthDate = "1996-11-21"
* address[0].type = http://hl7.org/fhir/address-type#physical
* address[0].line[0] = "49 MEADOW ST"
* address[0].city = "MOUNDS"
* address[0].state = "OK"
* address[0].postalCode = "74047"
* identifier[+].value = "1032704"
* identifier[=].system = "http://example.org/identifiers"
