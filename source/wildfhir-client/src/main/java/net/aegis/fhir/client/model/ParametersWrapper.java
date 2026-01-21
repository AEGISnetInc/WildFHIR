package net.aegis.fhir.client.model;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r4.formats.JsonParser;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.formats.IParser.OutputStyle;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;

/**
 * This class is a wrapper around the Parameters object.
 *
 * @author richard.ettema
 */
public class ParametersWrapper {

	private Parameters parameters;
	private String parametersJSON;
	private String parametersXML;
	private String resourceId;
	private String versionId;

	private List<ParameterComponentWrapper> parameterList = new ArrayList<ParameterComponentWrapper>();

	public ParametersWrapper(Parameters parameters) {
		this.parameters = parameters;

		try {
			if (parameters != null) {
				// Get XML representation of Resource
				XmlParser xmlP = new XmlParser();
				xmlP.setOutputStyle(OutputStyle.PRETTY);
				ByteArrayOutputStream oResource = new ByteArrayOutputStream();
				xmlP.compose(oResource, parameters, true);
				this.parametersXML = oResource.toString();

				// Get JSON representation of the Bundle Resource
				JsonParser jsonP = new JsonParser();
				jsonP.setOutputStyle(OutputStyle.PRETTY);
				oResource = new ByteArrayOutputStream();
				jsonP.compose(oResource, parameters);
				this.parametersJSON = oResource.toString();

				// Resource Id
				if (parameters.getId() != null) {
					this.resourceId = parameters.getId();
				}

				// Version Id
				if (parameters.getMeta() != null && parameters.getMeta().getVersionId() != null) {

					this.versionId = parameters.getMeta().getVersionId();
				}

				for (ParametersParameterComponent p : this.parameters.getParameter()) {
					parameterList.add(new ParameterComponentWrapper(p));
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Parameters getParameters() {
		return parameters;
	}

	public void setParameters(Parameters parameters) {
		this.parameters = parameters;
	}

	public String getParametersJSON() {
		return parametersJSON;
	}

	public void setParametersJSON(String parametersJSON) {
		this.parametersJSON = parametersJSON;
	}

	public String getParametersXML() {
		return parametersXML;
	}

	public void setParametersXML(String parametersXML) {
		this.parametersXML = parametersXML;
	}

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	public String getVersionId() {
		return versionId;
	}

	public void setVersionId(String versionId) {
		this.versionId = versionId;
	}

	public List<ParameterComponentWrapper> getParameterList() {
		return parameterList;
	}

	public void setParameterList(List<ParameterComponentWrapper> parameterList) {
		this.parameterList = parameterList;
	}

	public int getTotal() {
		if (this.parameterList != null) {
			return this.parameterList.size();
		} else {
			return 0;
		}
	}

}
