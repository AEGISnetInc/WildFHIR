
This global operation can be invoked using the GET Syntax as there are no input parameters:

`GET [base]/$capability-reload`

Or the POST syntax may be used as follows:

`POST [base]/$capability-reload`

---

### Usage

This is a custom operation to regenerate and reload the WildFHIR server CapabilityStatement.

### Example

**Using the `GET` syntax**

~~~
GET [base]/$capability-reload
[other headers]
~~~

**Using the `POST` syntax**

~~~
POST [base]/$capability-reload
[other headers]
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
            <valueString value="Capability statement reload complete."/>
        </parameter>
    </Parameters>
~~~
