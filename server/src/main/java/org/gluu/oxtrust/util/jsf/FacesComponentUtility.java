/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.util.jsf;

import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;

/**
 * Functions to work with UIComponents
 * 
 * @author Yuriy Movchan Date: 08.10.2011
 */
public final class FacesComponentUtility {

	private FacesComponentUtility() {
	}

	/**
	 * Get the field label.
	 * 
	 * @param messageId
	 *            id of message in the resourcebundle
	 * @return Message from the Message Source.
	 */
	public static String getMessageFromBundle(final String messageId) {
		FacesContext context = FacesContext.getCurrentInstance();
		Locale locale = context.getViewRoot().getLocale();

		String bundleName = context.getApplication().getMessageBundle();

		ResourceBundle bundle = ResourceBundle.getBundle(bundleName, locale, getClassLoader());

		/** Look for formId.fieldName, e.g., EmployeeForm.firstName. */

		String label = null;
		try {
			label = bundle.getString(messageId);
			return label;
		} catch (MissingResourceException e) {
		}

		return label;
	}

	private static ClassLoader getClassLoader() {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		if (classLoader == null) {
			return FacesComponentUtility.class.getClassLoader();
		}
		return classLoader;
	}

	public static void resetInputComponents(String rootCompId) {
		UIComponent rootUIComponent = findComponentById(rootCompId);
		resetInputComponents(rootUIComponent);
	}

	private static void resetInputComponents(UIComponent rootUIComponent) {
		if ((rootUIComponent == null) || (rootUIComponent.getChildCount() == 0)) {
			return;
		}

		for (UIComponent comp : rootUIComponent.getChildren()) {
			if (comp instanceof UIInput) {
				UIInput uiInput = (UIInput) comp;
				uiInput.setSubmittedValue(null);
				uiInput.setValid(true);
				uiInput.setLocalValueSet(false);
				uiInput.resetValue();
			}
			resetInputComponents(comp);
		}
	}

	public static void dumpComponentsTree(List<UIComponent> componetns, int level) {
		if ((componetns == null) || (componetns.size() == 0)) {
			return;
		}

		StringBuffer levelString = new StringBuffer();
		for (int i = 0; i < level; i++) {
			levelString.append(" ");
		}

		for (UIComponent comp : componetns) {
			System.out.println(levelString + comp.getId());
			if (comp.getChildCount() > 0) {
				dumpComponentsTree(comp.getChildren(), level++);
			}
		}
	}

	public static void dumpComponentsTree() {
		dumpComponentsTree(FacesContext.getCurrentInstance().getViewRoot().getChildren(), 0);
	}

	public static UIComponent findComponentById(String compId) {
		return findComponentById(FacesContext.getCurrentInstance().getViewRoot().getChildren(), compId);
	}

	private static UIComponent findComponentById(List<UIComponent> components, String compId) {
		if ((components == null) || (components.size() == 0)) {
			return null;
		}

		for (UIComponent comp : components) {
			if (compId.equals(comp.getId())) {
				return comp;
			}
			UIComponent tempResult = findComponentById(comp.getChildren(), compId);
			if (tempResult != null) {
				return tempResult;
			}
		}

		return null;
	}

}