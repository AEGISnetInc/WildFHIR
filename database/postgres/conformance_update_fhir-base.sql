UPDATE wildfhirr4.conformance
SET resourcecontents = pg_read_file('D:/GitHub/AEGISnetInc/WildFHIR/database/postgres/WildFHIRBaseCapabilityStatement.xml')
WHERE id = 2;