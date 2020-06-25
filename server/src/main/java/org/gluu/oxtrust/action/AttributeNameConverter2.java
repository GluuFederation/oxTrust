package org.gluu.oxtrust.action;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;

import org.gluu.model.GluuAttribute;
import org.gluu.service.AttributeService;
import org.gluu.service.cdi.util.CdiUtil;

@FacesConverter("org.gluu.jsf2.converter.AttributeNameConverter2")
public class AttributeNameConverter2 implements Converter {

	@Inject
	private AttributeService attributeService;

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		if (value == null) {
			return null;
		}
		if (attributeService == null) {
			attributeService = CdiUtil.bean(AttributeService.class);
		}
		return attributeService.getAttributeByName(value);
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		String string = null;
		if (value instanceof GluuAttribute) {
			string = ((GluuAttribute) value).getName();
		}

		return string;
	}

}
