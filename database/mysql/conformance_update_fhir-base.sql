/*
-- Update the 'fhir-base' conformance row with the WildFHIRBaseCapabilityStatement.xml contents
*/
UPDATE wildfhirr4.conformance
SET resourceContents = LOAD_FILE('./WildFHIRBaseCapabilityStatement.xml')
WHERE resourceId = 'fhir-base';
