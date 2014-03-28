package org.gluu.oxtrust.util.jsf;

import java.io.Serializable;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.apache.commons.lang.StringEscapeUtils;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.faces.Converter;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

/**
 * @author: Yuriy Movchan Date: 07.11.2012
 */
@Converter()
@Name("newLineToBRConverter")
@BypassInterceptors
public class NewLineToBRConverter implements javax.faces.convert.Converter, Serializable {

	private static final long serialVersionUID = -9107757423031967456L;

	public Object getAsObject(FacesContext arg0, UIComponent converter, String str) {
		return str;
	}

	public String getAsString(FacesContext arg0, UIComponent converter, Object obj) {
		return StringEscapeUtils.escapeHtml((String) obj).replace("\r\n", "<br/>").replace("\n", "<br/>");
	}

}
