package net.aegis.fhir.client.model;

import java.io.ByteArrayOutputStream;

import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.formats.IParser.OutputStyle;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r4.model.PrimitiveType;

/**
 * This class is a wrapper around the ParametersParameterComponent object.
 *
 * @author richard.ettema
 */
public class ParameterComponentWrapper {

	private String parameterName;
	private String valueString;
	private String resourceId;
	private String resourceXML;
	private ParametersParameterComponent parameter;

	@SuppressWarnings("rawtypes")
	public ParameterComponentWrapper(ParametersParameterComponent parameter) {
		this.parameter = parameter;

		/*
		 * Parse parameter for value string and resource XML
		 */
		try {
			if (parameter != null && parameter.hasName()) {
				this.parameterName = parameter.getName();
			}
			else {
				this.parameterName = "[UNNAMED]";
			}

			if (parameter != null && parameter.hasResource()) {
				// Get XML representation of Resource
				XmlParser xmlP = new XmlParser();
				xmlP.setOutputStyle(OutputStyle.PRETTY);
				ByteArrayOutputStream oResource = new ByteArrayOutputStream();
				xmlP.compose(oResource, parameter.getResource(), true);
				this.resourceXML = oResource.toString();

				if (parameter.getResource().hasId()) {
					this.resourceId = parameter.getResource().getId();
				}
				else {
					this.resourceId = "[UNDEFINED]";
				}
			}
			else if (parameter != null && parameter.hasValue()) {
				if (parameter.getValue() instanceof PrimitiveType) {
					this.valueString = ((PrimitiveType) parameter.getValue()).asStringValue();
				}
				else {
					this.valueString = "[EMPTY]";
				}
				this.resourceId = "ParameterValueType";
			}
			else {
				this.resourceId = "[EMPTY]";
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getParameterName() {
		return parameterName;
	}

	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}

	public String getValueString() {
		return valueString;
	}

	public void setValueString(String valueString) {
		this.valueString = valueString;
	}

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	public String getResourceXML() {
		return resourceXML;
	}

	public void setResourceXML(String resourceXML) {
		this.resourceXML = resourceXML;
	}

	public ParametersParameterComponent getParameter() {
		return parameter;
	}

	public void setParameter(ParametersParameterComponent parameter) {
		this.parameter = parameter;
	}

	public boolean isContainsResource() {
		return (this.resourceXML != null ? true : false);
	}

	public boolean isContainsValue() {
		return (this.valueString != null ? true : false);
	}

	public String getParameterType() {
		if (this.isContainsResource()) {
			return parameter.getResource().getResourceType().name();
		}
		else if (this.isContainsValue()) {
			return parameter.getValue().getClass().getName();
		}
		else {
			return "[UNKNOWN]";
		}
	}
}
