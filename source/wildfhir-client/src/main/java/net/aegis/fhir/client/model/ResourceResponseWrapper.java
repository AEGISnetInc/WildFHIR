package net.aegis.fhir.client.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.formats.JsonParser;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.formats.IParser.OutputStyle;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Resource;

import net.aegis.fhir.client.util.HttpHeadersKeys;
import net.aegis.fhir.service.util.ServicesUtil;

/**
 * <p>
 * This class is a wrapper for {@link net.aegis.fhir.model.Resource}.
 * </p>
 *
 * @author richard.ettema
 */
public class ResourceResponseWrapper {

	private net.aegis.fhir.model.Resource resourceBean;
	private BundleWrapper bundle;
	private ParametersWrapper parameters;
	private Resource resource;
	private String lastUpdated;
	private String resourceJSON;
	private String resourceXML;
	private int responseStatus;
	private boolean resrc = false;
	private boolean bndl = false;
	private boolean params = false;

	public ResourceResponseWrapper() {
		super();
	}

	public ResourceResponseWrapper(Response response) {
		if (response != null) {
			try {
				boolean isFHIRJSON = false;
				boolean isFHIRXML = false;

				String entityString = response.readEntity(String.class);

				// Process response body if present
				if (!StringUtils.isEmpty(entityString)) {
					int firstValid = entityString.indexOf("<");
					if (firstValid >= 0 && firstValid < 7) {
						isFHIRXML = true;
						entityString = entityString.substring(firstValid);
					}
					else {
						firstValid = entityString.indexOf("{");
						if (firstValid >= 0) {
							isFHIRJSON = true;
							entityString = entityString.substring(firstValid);
						}
					}
				}

				/*
				 *  Attempt to parse response body as a FHIR resource type; if not (exception),
				 *  set resource contents according to initially determined format (JSON or XML)
				 *  and set all wrapper indicators
				 */
				Resource responseResource = null;
				JsonParser jsonP = new JsonParser();
				jsonP.setOutputStyle(OutputStyle.PRETTY);
				XmlParser xmlP = new XmlParser();
				xmlP.setOutputStyle(OutputStyle.PRETTY);
				if (isFHIRJSON) {
					responseResource = jsonP.parse(new ByteArrayInputStream(entityString.getBytes()));
				}
				if (isFHIRXML) {
					responseResource = xmlP.parse(new ByteArrayInputStream(entityString.getBytes()));
				}

				if (responseResource != null) {
					this.setResource(responseResource);

					if (responseResource instanceof Bundle) {
						this.setBundle((Bundle)responseResource);
						this.setBndl(true);
						this.setParams(false);
						this.setResrc(false);
					}
					else if (responseResource instanceof Parameters) {
						this.setParameters((Parameters)responseResource);
						this.setBndl(false);
						this.setParams(true);
						this.setResrc(false);
					}
					else {
						this.setBndl(false);
						this.setParams(false);
						this.setResrc(true);
					}

					/*
					 * Compose response resource back to both JSON and XML
					 */
					ByteArrayOutputStream oResource = null;
					oResource = new ByteArrayOutputStream();
					jsonP.compose(oResource, responseResource);
					this.resourceJSON = oResource.toString();

					oResource = new ByteArrayOutputStream();
					xmlP.compose(oResource, responseResource, true);
					this.resourceXML = oResource.toString();
				}

				// Set FHIR resource metadata if available
				net.aegis.fhir.model.Resource resourceBean = new net.aegis.fhir.model.Resource();
				if (response.getHeaders().get(HttpHeadersKeys.Content_Location.toString()) != null) {
					resourceBean.setResourceId(ServicesUtil.INSTANCE.extractResourceIdFromURL((String) response.getHeaders().get(HttpHeadersKeys.Content_Location.toString()).get(0)));
					resourceBean.setVersionId(ServicesUtil.INSTANCE.extractVersionIdIntFromURL((String) response.getHeaders().get(HttpHeadersKeys.Content_Location.toString()).get(0)));
				}
				else if (response.getHeaders().get(HttpHeadersKeys.Location.toString()) != null) {
					resourceBean.setResourceId(ServicesUtil.INSTANCE.extractResourceIdFromURL((String) response.getHeaders().get(HttpHeadersKeys.Location.toString()).get(0)));
					resourceBean.setVersionId(ServicesUtil.INSTANCE.extractVersionIdIntFromURL((String) response.getHeaders().get(HttpHeadersKeys.Location.toString()).get(0)));
				}
				else {
					// Response headers do not contain the Content-Location or Location; Extract values directly from the resource
					if (responseResource != null) {
						if (responseResource.getId() != null) {
							resourceBean.setResourceId(responseResource.getId());
						}
						else {
							resourceBean.setResourceId("Not Defined!");
						}
						if (responseResource.hasMeta() && responseResource.getMeta().hasVersionId()) {
							if (StringUtils.isNumeric(responseResource.getMeta().getVersionId())) {
								resourceBean.setVersionId(Integer.valueOf(responseResource.getMeta().getVersionId()));
							}
						}
						else {
							resourceBean.setVersionId(-1);
						}
					}
					else {
						resourceBean.setResourceId("Not Found!");
						resourceBean.setVersionId(-1);
					}
				}

				if (response.getHeaders().get(HttpHeadersKeys.Last_Modified.toString()) != null) {
					this.setLastUpdated((String) response.getHeaders().get(HttpHeadersKeys.Last_Modified.toString()).get(0));
				}
				else if (response.getHeaders().get(HttpHeadersKeys.Todays_Date.toString()) != null) {
					this.setLastUpdated((String) response.getHeaders().get(HttpHeadersKeys.Todays_Date.toString()).get(0));
				}
				else {
					this.setLastUpdated("Last-Modified not found!");
				}

				this.setResourceBean(resourceBean);
				if (isFHIRJSON) {
					this.setResourceJSON(entityString);
				}
				if (isFHIRXML) {
					this.setResourceXML(entityString);
				}

				this.setResponseStatus(response.getStatus());
			}
			catch (Exception e) {
				// Cannot process response
				e.printStackTrace();
				this.setResource(null);
				this.setBndl(false);
				this.setResrc(false);
				this.setParams(false);
			}
		}
	}

	public net.aegis.fhir.model.Resource getResourceBean() {
		return resourceBean;
	}

	public void setResourceBean(net.aegis.fhir.model.Resource resourceBean) {
		this.resourceBean = resourceBean;
	}

	public BundleWrapper getBundle() {
		return bundle;
	}

	public void setBundle(BundleWrapper bundle) {
		this.bundle = bundle;
	}

	public void setBundle(Bundle bundle) {
		this.bundle = new BundleWrapper(bundle);
	}

	public void setBundle(Bundle bundle, boolean isJSON) {
		this.bundle = new BundleWrapper(bundle, isJSON);
	}

	public ParametersWrapper getParameters() {
		return parameters;
	}

	public void setParameters(ParametersWrapper parameters) {
		this.parameters = parameters;
	}

	public void setParameters(Parameters parameters) {
		this.parameters = new ParametersWrapper(parameters);
	}

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public String getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(String lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public String getResourceJSON() {
		return resourceJSON;
	}

	public void setResourceJSON(String resourceJSON) {
		this.resourceJSON = resourceJSON;
	}

	public String getResourceXML() {
		return resourceXML;
	}

	public void setResourceXML(String resourceXML) {
		this.resourceXML = resourceXML;
	}

	public int getResponseStatus() {
		return responseStatus;
	}

	public void setResponseStatus(int i) {
		this.responseStatus = i;
	}

	public boolean isResrc() {
		return resrc;
	}

	public void setResrc(boolean resrc) {
		this.resrc = resrc;
	}

	public boolean isBndl() {
		return bndl;
	}

	public void setBndl(boolean bndl) {
		this.bndl = bndl;
	}

	public boolean isParams() {
		return params;
	}

	public void setParams(boolean params) {
		this.params = params;
	}

}
