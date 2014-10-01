/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.util.jsf;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.log4j.Logger;

import com.sun.faces.facelets.tag.AbstractTagLibrary;

/**
 * Gluu tag library.
 * 
 * @author Yuriy Movchan Date: 11.09.2010
 */
public class GluuJsfLibrary extends AbstractTagLibrary {

	/**
	 * Tag library namespace to import this library in facelets
	 */
	public static final String NAMESPACE = "http://www.gluu.org/jsf/functions";

	private static Logger log = Logger.getLogger(GluuJsfLibrary.class);

	public static final GluuJsfLibrary INSTANCE = new GluuJsfLibrary();

	public GluuJsfLibrary() {
		super(NAMESPACE);
		registerStaticMethods();
	}

	private void registerStaticMethods() {
		try {
			Method[] methods = JsfFunctions.class.getMethods();

			for (Method method : methods) {
				if (Modifier.isStatic(method.getModifiers())) {
					this.addFunction(method.getName(), method);
				}
			}
		} catch (Exception e) {
			log.error(e);
		}
	}

}
