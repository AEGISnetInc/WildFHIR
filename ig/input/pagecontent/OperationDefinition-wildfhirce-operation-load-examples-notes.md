
This global operation can be invoked using the GET Syntax using all parameters:

`GET [base]$load-examples?{parameters}`

Otherwise the POST transaction is used as follows:

`POST [base]$load-examples`

---

### Usage

The operation will process all files in the specified directory folder in ascending alphanumeric order by filename and create/update them on the target WildFHIR server. In order to insure proper search parameter indexing and referential integrity please name the files such that independent resources are listed first followed by dependent resources. For example, Patient resources should be processed before Condition resources belonging to those Patient(s). Using a numeric prefix naming convention will facilitate this processing - "01-patient-example.xml", "02-condition-example.xml".

Both parameters - "baseurl" and "dirpath" - are required:

#### baseurl

The "baseurl" parameter is needed to insure search parameter indexing for reference search parameter values is correct for the WildFHIR server's resource repository. 

#### dirpath

The "dirpath" parameter must point to a valid directory folder on the physical machine hosting the WildFHIR server.

### Example

**Request the load of all example resource instances in a specific directory folder with a given FHIR server base URL for search parameter indexing using the `GET` syntax**

~~~
GET [base]$load-examples?baseurl=http://wildfhir.aegis.net/fhir4-0-1&dirpath=C:/Temp/fhir4_0_1/examples
[other headers]
~~~

**Request the load of all example resource instances in a specific directory folder with a given FHIR server base URL for search parameter indexing using the `POST` syntax**

~~~
POST [base]$load-examples
[other headers]
~~~

**POST request body:**

~~~
    <?xml version="1.0" encoding="UTF-8"?>
    <Parameters xmlns="http://hl7.org/fhir">
      <id value="parameters-load-examples-in-example"/>
      <parameter>
        <name value="baseurl"/>
        <valueString value="http://wildfhir.aegis.net/fhir4-0-1"/>
      </parameter> 
      <parameter>
        <name value="dirpath"/>
        <valueString value="C:/Temp/fhir4_0_1/examples"/>
      </parameter>
    </Parameters>
~~~

**Response**

~~~
HTTP/1.1 200 OK
[other headers]
~~~

**Response body:**

~~~
    <?xml version="1.0" encoding="UTF-8"?>
    <Parameters xmlns="http://hl7.org/fhir">
        <parameter>
            <name value="result"/>
            <valueString value="Processing complete. 1 resources imported; 0 resources skipped."/>
        </parameter>
    </Parameters>
~~~
