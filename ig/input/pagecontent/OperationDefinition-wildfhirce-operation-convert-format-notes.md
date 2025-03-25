
The operation can only be invoked using the POST Syntax as follows:

`POST [base]/$convert-format`

---

### Example

**Request the syntax conversion of an XML syntax representation of a Patient to the JSON syntax representation**

~~~
POST [base]/$convert-format
Accept: application/fhir+json;charset=utf-8
Content-Type: application/fhir+xml;charset=utf-8
[other headers]
~~~

**POST request body:**

~~~
    <Patient xmlns="http://hl7.org/fhir">
      <id value="parameters-patient-convert-in-xml-example"/>
      <text>
        <status value="generated"/>
        <div xmlns="http://www.w3.org/1999/xhtml">
          <p>Peter James Chalmers</p>
        </div>
      </text>
      <identifier>
        <system value="urn:oid:1.2.36.146.595.217.0.1"/>
        <value value="12345"/>
      </identifier>
      <active value="true"/>
      <name>
        <use value="official"/>
        <family value="Chalmers"/>
        <given value="Peter"/>
        <given value="James"/>
      </name>
      <telecom>
        <system value="phone"/>
        <value value="(03) 5555 6473"/>
        <use value="work"/>
      </telecom>
      <gender value="male"/>
      <birthDate value="1974-12-25"/>
      <address>
        <city value="PleasantVille"/>
        <state value="Vic"/>
        <postalCode value="3999"/>
      </address>
    </Patient>
~~~

**Response**

~~~
HTTP/1.1 200 OK
Content-Type: application/fhir+json;charset=utf-8
[other headers]
~~~

**Response body:**

~~~
    {
      "resourceType": "Patient",
      "id": "parameters-patient-convert-out-json-example",
      "text": {
        "status": "generated",
        "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><p>Peter James Chalmers</p></div>"
      },
      "identifier": [
        {
          "system": "urn:oid:1.2.36.146.595.217.0.1",
          "value": "12345"
        }
      ],
      "active": true,
      "name": [
        {
          "use": "official",
          "family": "Chalmers",
          "given": [
            "Peter",
            "James"
          ]
        }
      ],
      "telecom": [
          "system": "phone",
          "value": "(03) 5555 6473",
          "use": "work"
        }
      ],
      "gender": "male",
      "birthDate": "1974-12-25",
      "address": [
        {
          "city": "PleasantVille",
          "state": "Vic",
          "postalCode": "3999"
        }
      ]
    }
~~~