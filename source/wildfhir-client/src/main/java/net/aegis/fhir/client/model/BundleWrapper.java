package net.aegis.fhir.client.model;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r4.formats.IParser.OutputStyle;
import org.hl7.fhir.r4.formats.JsonParser;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;

/**
 * This class is a wrapper around the Bundle object.
 *
 * @author richard.ettema
 *
 */
public class BundleWrapper {

	private Bundle bundle;
	private String bundleJSON;
	private String bundleXML;
	private String resourceId;
	private String versionId;

	private List<BundleEntryWrapper> entryList = new ArrayList<BundleEntryWrapper>();

	public BundleWrapper(Bundle bundle) {
		this.bundle = bundle;

		try {
			if (bundle != null) {
				// Get XML representation of the Bundle Resource
				XmlParser xmlP = new XmlParser();
				xmlP.setOutputStyle(OutputStyle.PRETTY);
				ByteArrayOutputStream xBundle = new ByteArrayOutputStream();
				xmlP.compose(xBundle, bundle, true);
				this.bundleXML = xBundle.toString();

				// Get JSON representation of the Bundle Resource
				JsonParser jsonP = new JsonParser();
				jsonP.setOutputStyle(OutputStyle.PRETTY);
				ByteArrayOutputStream jBundle = new ByteArrayOutputStream();
				jsonP.compose(jBundle, bundle);
				this.bundleJSON = jBundle.toString();

				// Resource Id
				if (bundle.getId() != null) {
					this.resourceId = bundle.getId();
				}

				// Version Id
				if (bundle.getMeta() != null && bundle.getMeta().getVersionId() != null) {

					this.versionId = bundle.getMeta().getVersionId();
				}

				for (BundleEntryComponent e : this.bundle.getEntry()) {
					entryList.add(new BundleEntryWrapper(e));
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public BundleWrapper(Bundle bundle, boolean isJSON) {
		this.bundle = bundle;

		try {
			if (bundle != null) {
				if (isJSON) {
					// Get JSON representation of the Bundle Resource
					JsonParser jsonP = new JsonParser();
					jsonP.setOutputStyle(OutputStyle.PRETTY);
					ByteArrayOutputStream jBundle = new ByteArrayOutputStream();
					jsonP.compose(jBundle, bundle);
					this.bundleJSON = jBundle.toString();
				}
				else {
					// Get XML representation of the Bundle Resource
					XmlParser xmlP = new XmlParser();
					xmlP.setOutputStyle(OutputStyle.PRETTY);
					ByteArrayOutputStream xBundle = new ByteArrayOutputStream();
					xmlP.compose(xBundle, bundle, true);
					this.bundleXML = xBundle.toString();
				}

				// Resource Id
				if (bundle.getId() != null) {
					this.resourceId = bundle.getId();
				}

				// Version Id
				if (bundle.getMeta() != null && bundle.getMeta().getVersionId() != null) {

					this.versionId = bundle.getMeta().getVersionId();
				}

				for (BundleEntryComponent e : this.bundle.getEntry()) {
					entryList.add(new BundleEntryWrapper(e, isJSON));
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Bundle getBundle() {
		return bundle;
	}

	public String getBundleJSON() {
		return bundleJSON;
	}

	public String getBundleXML() {
		return bundleXML;
	}

	public String getResourceId() {
		return resourceId;
	}

	public String getVersionId() {
		return versionId;
	}

	public List<BundleEntryWrapper> getEntryList() {
		return entryList;
	}

	public String getId() {
		if (bundle != null) {
			return bundle.getId();
		}
		return null;
	}

	public String getType() {
		if (bundle != null && bundle.getType() != null) {
			return bundle.getType().toCode();
		}
		return null;
	}

	public int getTotal() {
		if (bundle != null) {
			return bundle.getTotal();
		}
		return -1;
	}

}
