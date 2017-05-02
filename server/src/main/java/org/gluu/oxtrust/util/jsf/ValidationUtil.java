/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.util.jsf;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;

import org.jboss.seam.core.Expressions;

/**
 * Functions to help with validation
 * 
 * @author Yuriy Movchan Date: 08.10.2010
 */
public class ValidationUtil {

	private ValidationUtil() {}

	public static void addErrorMessageToInput(UIComponent uiComponent, String message) {
		if (uiComponent instanceof UIInput) {
			((UIInput) uiComponent).setValid(false);
		}
		message = Expressions.instance().createValueExpression(message).getValue().toString();
		FacesMessage facesMessage = new FacesMessage(message);
		facesMessage.setSeverity(FacesMessage.SEVERITY_ERROR);
		javax.faces.context.FacesContext fc = javax.faces.context.FacesContext.getCurrentInstance();
		fc.addMessage(uiComponent.getClientId(fc), facesMessage);
	}

	public static void addErrorMessageToInput(String uiComponentId, String message) {
		UIComponent uiComponent = FacesComponentUtility.findComponentById(uiComponentId);
		if (uiComponent != null) {
			addErrorMessageToInput(uiComponent, message);
		}
	}

	public static boolean validateLength(String text, UIInput uiComponent, int min, int max, String message) {
		text = text == null ? "" : text;
		if (text.length() > max || text.length() < min) {
			addErrorMessageToInput(uiComponent, message);
			return false;
		}

		return true;
	}

}
