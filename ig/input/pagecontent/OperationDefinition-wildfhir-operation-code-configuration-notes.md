
The operation can be invoked using the GET Syntax as all parameters are primitive types:

`GET [base]/Patient/[id]/$code-configuration?{parameters}`

Otherwise the POST transaction is used as follows:

`POST [base]/Patient/[id]/$code-configuration`

---

### Update Example

**Request an update to code configuration setting resourcePurgeAllEnabled to 'true' using `GET` syntax**

~~~
GET [base]/$code-configuration?operation=update&codeName=resourcePurgeAllEnabled&value=true
[other headers]
~~~

**Request an update to code configuration setting resourcePurgeAllEnabled to 'true' using `POST` syntax**

~~~
POST [base]/$code-configuration
[other headers]
~~~

**POST request body:**

~~~
    <?xml version="1.0" encoding="UTF-8"?>
    <Parameters xmlns="http://hl7.org/fhir">
      <id value="parameters-code-configuration-in-example"/>
      <parameter>
        <name value="operation"/>
        <valueDate value="update"/>
      </parameter> 
      <parameter>
        <name value="codeName"/>
        <valueDate value="resourcePurgeAllEnabled"/>
      </parameter>
      <parameter>
        <name value="value"/>
        <valueDate value="true"/>
      </parameter>
    </Parameters>
~~~

**Response** (for both GET and POST)

~~~
HTTP/1.1 200 OK
[other headers]
~~~

**Response body:** (for both GET and POST)

~~~
    <?xml version="1.0" encoding="UTF-8"?>
    <OperationOutcome xmlns="http://hl7.org/fhir">
      <id value="parameters-patient-purge-out-example"/>
      <text>
        <status value="additional"/>
        <div xmlns="http://www.w3.org/1999/xhtml">
          <p>$code-configuration update completed. Update of existing code successful - 'resourcePurgeAllEnabled', 'true'.</p>
        </div>
      </text>
      <issue> 
        <severity value="information"/>
        <code value="informational"/>
        <details>
          <text value="$code-configuration update completed. Update of existing code successful - 'resourcePurgeAllEnabled', 'true'."/>
        </details>
     </issue>
    </OperationOutcome>
~~~


### List Example

**Request the list of all code configuration settings (name, value, description) using `GET` syntax**

~~~
GET [base]/$code-configuration?operation=list
[other headers]
~~~

**Request the list of all code configuration settings (name, value, description) using `POST` syntax**

~~~
POST [base]/$code-configuration?operation=list
[other headers]
~~~

**Response** (for both GET and POST)

~~~
HTTP/1.1 200 OK
[other headers]
~~~

**Response body:** (for both GET and POST)

~~~
    <?xml version="1.0" encoding="UTF-8"?>
    <Parameters xmlns="http://hl7.org/fhir">
        <parameter>
            <name value="code"/>
            <part>
                <name value="codeName"/>
                <valueString value="baseUrl"/>
            </part>
            <part>
                <name value="value"/>
                <valueString value="http://localhost/r4"/>
            </part>
            <part>
                <name value="description"/>
                <valueString value="WildFHIR base url (used by capabilitystatement-reload)"/>
            </part>
        </parameter>
        <parameter>
            <name value="code"/>
            [snipped]
        </parameter>
        [snipped]
    </Parameters>
~~~