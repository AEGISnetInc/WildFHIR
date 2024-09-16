
This global operation can be invoked using the GET Syntax as there are no input parameters:

`GET [base]/$purge-all`

Or the POST syntax may be used as follows:

`POST [base]/$purge-all`

---

### Usage

The operation provides a standardized mechanism to force a hard delete or purge of all FHIR resources and search meta data.

### Example

**Using the `GET` syntax**

~~~
GET [base]/$purge-all
[other headers]
~~~

**Using the `POST` syntax**

~~~
POST [base]/$purge-all
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
            <valueString value="Purge All operation complete."/>
        </parameter>
    </Parameters>
~~~
