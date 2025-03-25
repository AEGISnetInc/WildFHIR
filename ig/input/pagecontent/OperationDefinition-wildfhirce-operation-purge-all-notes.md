
**WARNING** This operation purges ALL data in the WildFHIR data repository and cannot be undone. Please use with caution.
{:.dragon}

The operation does not define any IN parameters and can be invoked using the GET or POST Syntax. Any request IN parameters will be ignored.

`GET [base]/$purge-all`

or,

`POST [base]/$purge-all`

---

### Example

**Request the purge of ALL data using `GET` syntax**

~~~
GET [base]/$purge-all
[other headers]
~~~

**Request the purge of ALL data using `POST` syntax**

~~~
POST [base]/$purge-all
[other headers]
~~~

**POST request body:**

N/A

**Response**

~~~
HTTP/1.1 200 OK
[other headers]
~~~

**Response body:**

~~~
    <?xml version="1.0" encoding="UTF-8"?>
    <OperationOutcome xmlns="http://hl7.org/fhir">
      <id value="parameters-purge-out-example"/>
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