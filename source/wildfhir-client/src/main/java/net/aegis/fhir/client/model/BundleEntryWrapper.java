package net.aegis.fhir.client.model;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;

import net.aegis.fhir.service.util.UTCDateUtil;
import net.aegis.fhir.service.util.UUIDUtil;

import org.hl7.fhir.r4.formats.IParser.OutputStyle;
import org.hl7.fhir.r4.formats.JsonParser;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;

/**
 * This class is a wrapper around the BundleEntryComponent object.
 *
 * @author richard.ettema
 *
 */
public class BundleEntryWrapper {

	private String uniqueId;
	private String entryJSON;
	private String entryXML;
	private BundleEntryComponent entry;
	private String resourceId;
	private String versionId;
	private String deletedResourceId;
	private String deletedVersionId;
	private boolean deleted = false;

	public BundleEntryWrapper(BundleEntryComponent entry) {
		this.entry = entry;

		try {
			if (entry != null) {

				if (entry.hasResource()) {

					// Get XML representation of the Bundle Entry Resource
					XmlParser xmlP = new XmlParser();
					xmlP.setOutputStyle(OutputStyle.PRETTY);
					ByteArrayOutputStream xBundleEntry = new ByteArrayOutputStream();
					xmlP.compose(xBundleEntry, entry.getResource(), true);
					this.entryXML = xBundleEntry.toString();

					// Get JSON representation of the Bundle Entry Resource
					JsonParser jsonP = new JsonParser();
					jsonP.setOutputStyle(OutputStyle.PRETTY);
					ByteArrayOutputStream jBundleEntry = new ByteArrayOutputStream();
					jsonP.compose(jBundleEntry, entry.getResource());
					this.entryJSON = jBundleEntry.toString();

					// Resource Id
					if (entry.getResource().getId() != null) {
						this.resourceId = entry.getResource().getId();
					}

					// Version Id
					if (entry.getResource().getMeta() != null && entry.getResource().getMeta().getVersionId() != null) {

						this.versionId = entry.getResource().getMeta().getVersionId();
					}
				}

				if (entry.hasRequest()) {

					// Check for Deleted Resource
					if (entry.getRequest().hasMethod() && entry.getRequest().getMethod().equals(HTTPVerb.DELETE)) {
						this.deletedResourceId = "DELETED";
						this.deletedVersionId = "DELETED";
						this.deleted = true;
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public BundleEntryWrapper(BundleEntryComponent entry, boolean isJSON) {
		this.entry = entry;

		try {
			if (entry != null) {

				if (entry.hasResource()) {

					if (isJSON) {
						// Get JSON representation of the Bundle Entry Resource
						JsonParser jsonP = new JsonParser();
						jsonP.setOutputStyle(OutputStyle.PRETTY);
						ByteArrayOutputStream jBundleEntry = new ByteArrayOutputStream();
						jsonP.compose(jBundleEntry, entry.getResource());
						this.entryJSON = jBundleEntry.toString();
					}
					else {
						// Get XML representation of the Bundle Entry Resource
						XmlParser xmlP = new XmlParser();
						xmlP.setOutputStyle(OutputStyle.PRETTY);
						ByteArrayOutputStream xBundleEntry = new ByteArrayOutputStream();
						xmlP.compose(xBundleEntry, entry.getResource(), true);
						this.entryXML = xBundleEntry.toString();
					}

					// Resource Id
					if (entry.getResource().getId() != null) {
						this.resourceId = entry.getResource().getId();
					}

					// Version Id
					if (entry.getResource().getMeta() != null && entry.getResource().getMeta().getVersionId() != null) {

						this.versionId = entry.getResource().getMeta().getVersionId();
					}
				}

				if (entry.hasRequest()) {

					// Check for Deleted Resource
					if (entry.getRequest().hasMethod() && entry.getRequest().getMethod().equals(HTTPVerb.DELETE)) {
						this.deletedResourceId = "DELETED";
						this.deletedVersionId = "DELETED";
						this.deleted = true;
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getUniqueId() {
		if (uniqueId == null) {
			uniqueId = UUIDUtil.getUUID();
		}
		return uniqueId;
	}

	public String getEntryJSON() {
		return entryJSON;
	}

	public String getEntryXML() {
		return entryXML;
	}

	public BundleEntryComponent getEntry() {
		return entry;
	}

	public String getResourceId() {
		return resourceId;
	}

	public String getVersionId() {
		return versionId;
	}

	public String getId() {
		return entry.getId();
	}

	public java.util.Calendar getUpdated() {
		if (entry != null && entry.getResource() != null && entry.getResource().getMeta() != null && entry.getResource().getMeta().getLastUpdated() != null) {

			Calendar updatedTime = Calendar.getInstance();
			updatedTime.setTime(entry.getResource().getMeta().getLastUpdated());

			return updatedTime;
		}
		return null;
	}

	public Resource getResource() {
		if (entry != null) {
			return entry.getResource();
		}
		return null;
	}

	public String getResourceType() {
		if (entry != null && entry.getResource() != null) {
			return entry.getResource().getClass().getSimpleName();
		}
		return null;
	}

	public XhtmlNode getText() {
		if (entry.getResource() != null && entry.getResource() instanceof DomainResource) {

			DomainResource domainResource = (DomainResource) entry.getResource();
			if (domainResource.getText() != null && domainResource.getText().getDiv() != null) {

				return domainResource.getText().getDiv();
			}
		}
		return null;
	}

	public boolean isDeleted() {
		return this.deleted;
	}

	public String getDeletedResourceId() {
		return this.deletedResourceId;
	}

	public String getDeletedVersionId() {
		return this.deletedVersionId;
	}

	public java.util.Calendar getDeletedInstant() {
		return null;
	}

	public Patient getPatient() {
		if (entry != null && entry.hasResource() && entry.getResource() instanceof Patient) {
			return (Patient) entry.getResource();
		}
		return null;
	}

	public String getPatientInformation() {
		StringBuilder patientInfo = new StringBuilder();
		if (entry != null && entry.hasResource() && entry.getResource() instanceof Patient) {
			Patient patient = (Patient) entry.getResource();

			if (patient.hasName()) {
				patientInfo.append("Name: ");

				if (patient.getName().get(0).hasFamily()) {
					patientInfo.append(patient.getName().get(0).getFamily());
				}
				if (patient.getName().get(0).hasGiven()) {

					if (patient.getName().get(0).hasFamily()) {
						patientInfo.append(", ");
					}
					patientInfo.append(patient.getName().get(0).getGiven().get(0).getValue());
				}
			}

			if (patient.hasBirthDate()) {
				if (patientInfo.length() > 1) {
					patientInfo.append("\n");
				}
				UTCDateUtil utcDateUtil = new UTCDateUtil();
				patientInfo.append("DOB: ").append(utcDateUtil.formatDate(patient.getBirthDate(), UTCDateUtil.DATE_ONLY_FORMAT_UTC));
			}
		}
		return patientInfo.toString();
	}

}
