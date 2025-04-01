/*
-- Insert default codes and their settings
*/
INSERT INTO wildfhirr4.code (codeName,value,intValue,description,resourceContents) VALUES
('baseUrl','http://localhost:8080/r4',0,'WildFHIR base url (used by capabilitystatement-reload)',NULL),
('conditionalDelete','multiple',0,'Conditional delete support setting (single, multiple, not-supported)',NULL),
('conditionalRead','full-support',0,'Conditional read support setting (full-support, modified-since, not-match, not-supported)',NULL),
('conditionalCreate','true',1,'Conditional create support setting (true, false)',NULL),
('conditionalUpdate','true',1,'Conditional update support setting (true, false)',NULL),
('createResponsePayload','representation',0,'Success create response payload preference setting: minimal, representation(default), OperationOutcome',NULL),
('searchResponsePayload','default',0,'Search response payload: default - do not include OperationOutcome, OperationOutcome - include OperationOutcome',NULL),
('updateResponsePayload','representation',0,'Success update response payload preference setting: minimal, representation(default), OperationOutcome',NULL),
('documentSignature','WildFHIRSig.jpg',0,'WildFHIR image contents used to sign generated document bundles (image name and base64 contents)','/9j/4AAQSkZJRgABAQECVwJXAAD/2wBDAAMCAgMCAgMDAwMEAwMEBQgFBQQEBQoHBwYIDAoMDAsKCwsNDhIQDQ4RDgsLEBYQERMUFRUVDA8XGBYUGBIUFRT/2wBDAQMEBAUEBQkFBQkUDQsNFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBT/wgARCAARADIDAREAAhEBAxEB/8QAGwAAAQUBAQAAAAAAAAAAAAAAAAIDBAUGBwj/xAAaAQEAAgMBAAAAAAAAAAAAAAAAAQIDBAUG/9oADAMBAAIQAxAAAAH0hGKatJmy0gAY6urbM3NPN+wWPTDcW0W7z9r2OA0rUzerNykA/8QAIhAAAgEDAgcAAAAAAAAAAAAAAgQBAwUSFDQAEBEVIDIz/9oACAEBAAEFAqdYWOBAcRnIfCY0LGqLpNqeKlFmYKmVmbmr2RzFe1vguz8lPRneqbjn/8QAJxEAAAQEBQQDAAAAAAAAAAAAAAECEQMEIVEUIDEyYRITQXGBodH/2gAIAQMBAT8BI3HrNsMJNzYFNSyDSSV0L2MbBffT5/AU3B6Ul3KtXXjgYyA++9708WESZljWZt9BWgTm/8QAJREAAQEGBQUAAAAAAAAAAAAAAQACAwQRFFESMDJhcSEiQbHR/9oACAECAQE/AcgmQVNENA4mOp4VI9lp9fUYV7M9nnbdUj6Wi1rcpiHfhkCeR//EACwQAAIABAEJCQAAAAAAAAAAAAECAAMREiEEEBQgIjFRYXETIzJBcrHR0vD/2gAIAQEABj8CLePgimFsHZmlRSAdUg7GJtNNkiKoomuF2ZaYe8TRNyfv2a4MHX5/VhrcnYtaVBuTiefSJx0MhDMBQVTBcefMQRorYqg3rvCkHz4xKUvaQoFL4OZOv1iV6Rqf/8QAIxABAAEDAwMFAAAAAAAAAAAAAREAITFBUXEQIGGBkaHw8f/aAAgBAQABPyER2+TGhuVlsQrT13q3sSTHbieUeaG4x81C9ChvcWoBI7KKjF+X5Kl4xKCVBtnDUM6JIUzk2PHgrWUI4UnkhpoKJo2x0Gbpr6Hbs//aAAwDAQACAAMAAAAQjsklpzhXekn/xAAfEQEAAgICAwEBAAAAAAAAAAABESEAMUFhIFFxEKH/2gAIAQMBAT8QG++sAg0wZJ8XS169ZByCfsHbWU04ETgRNHNPUVtwiqLC1xAjossfMGgIQouzB+Ab3ercqMdrWqEfEjrVmAqQr/X4a5znPh//xAAfEQEAAQQCAwEAAAAAAAAAAAABEQAhMUFhcSBRgaH/2gAIAQIBAT8QmfPDSIhL6pR3JRnZn3q/c3wUzAcwh9LP4KS+CEvYZb2pbHNirglgdhD9MPPdNaEBvimiteP/xAAiEAEAAgIBAwUBAAAAAAAAAAABESEAMUEgYaFRcZGx8PH/2gAIAQEAAT8QmcmaMpualrdnFMYGDhAE1IChcPvIjDnGTKUxJrpRCJPSFCaFOEfGErzFSVtICo7SVeRUX07OJAFEEyCdSpuUyVLwACaBNQ4NlbsgbVtDYCE7JXF/YzAVsSwhjcpSrhGilaqs87PLPrPw98fq+ro//9k='),
('supportedVersions','4.0',0,'Supported FHIR versions comma separated (WildFHIR currently only supports 1 version)',NULL),
('resourcePurgeAllEnabled','false',0,'Resource Purge All operation enabled setting (true, false)',NULL),
('lastnProcessEmptyDate','false',0,'Observation $lastn process empty effective date values setting (true, false)',NULL),
('lastnEmptyDateValue','1900-01-01',0,'Observation $lastn empty effective date value; set value to control date sort (yyyy-MM-dd)',NULL),
('auditEventServiceEnabled','false',0,'Audit service enabled setting (true, false)',NULL),
('provenanceServiceEnabled','false',0,'Provenance service enabled setting (true, false)',NULL),
('subscriptionServiceEnabled','false',0,'Subscription Framework support setting (true, false)',NULL),
('txConcurrentLimit','true',2,'Batch/transaction concurrent requests allowed processing limit (intValue); enabled setting (true, false)',NULL);
