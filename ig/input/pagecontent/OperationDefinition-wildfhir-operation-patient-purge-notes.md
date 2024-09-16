
The operation can be invoked using the GET Syntax as all parameters are primitive types:

`GET [base]/Patient/[id]/$purge?{parameters}`

Otherwise the POST transaction is used as follows:

`POST [base]/Patient/[id]/$purge`

---

### Example

**Request the purge of all related data for a patient using `GET` syntax**

~~~
GET [base]/Patient/[id]/$purge?start=2015-01-01
[other headers]
~~~

**Request the purge of all related data for a patient using `POST` syntax**

~~~
POST [base]/Patient/[id]/$purge
[other headers]
~~~

**POST request body:**

~~~
    <?xml version="1.0" encoding="UTF-8"?>
    <Parameters xmlns="http://hl7.org/fhir">
      <id value="parameters-patient-purge-in-example"/>
      <parameter>
        <name value="start"/>
        <valueDate value="2015-01-01"/>
      </parameter> 
      <parameter>
        <name value="end"/>
        <valueDate value="2018-12-31"/>
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
    <OperationOutcome xmlns="http://hl7.org/fhir">
      <id value="parameters-patient-purge-out-example"/>
      <text>
        <status value="additional"/>
        <div xmlns="http://www.w3.org/1999/xhtml">
          <p>Purge successfully completed</p>
        </div>
      </text>
      <issue> 
        <severity value="information"/>
        <code value="informational"/>
        <details>
          <text value="Purge successfully completed"/>
        </details>
     </issue>
    </OperationOutcome>
~~~