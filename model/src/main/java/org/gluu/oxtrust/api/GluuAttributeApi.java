package org.gluu.oxtrust.api;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.xdi.model.GluuAttribute;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
public class GluuAttributeApi extends GluuAttribute{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8161676720448531706L;

}
