/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.cache.service.intercept;

import java.io.Serializable;

import org.gluu.oxtrust.ldap.cache.service.CacheRefreshConfiguration;
import org.gluu.oxtrust.ldap.cache.service.intercept.interfaces.DummyEntryInterceptor;
import org.gluu.oxtrust.ldap.cache.service.intercept.interfaces.EntryInterceptorType;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.python.core.PyLong;
import org.python.core.PyObject;
import org.xdi.exception.PythonException;
import org.xdi.service.PythonService;
import org.xdi.util.StringHelper;

/**
 * Provides factory methods needed to create cache refresh interceptor
 * 
 * @author Yuriy Movchan Date: 07.04.2012
 */
@Scope(ScopeType.STATELESS)
@Name("cacheRefreshInterceptorService")
@AutoCreate
public class CacheRefreshInterceptorService implements Serializable {

	private static final long serialVersionUID = -1225880597520443390L;

	private static final EntryInterceptorType DUMMY_ENTRY_INTERCEPTOR = new DummyEntryInterceptor();

	private static final String PYTHON_ENTRY_INTERCEPTOR_TYPE = "EntryInterceptor";

	@Logger
	private Log log;

	@In
	private CacheRefreshConfiguration cacheRefreshConfiguration;

	@In
	private PythonService pythonService;

	public EntryInterceptorType createEntryInterceptor() {
		EntryInterceptorType entryInterceptor;
		try {
			entryInterceptor = createEntryInterceptorWithPythonException();
		} catch (PythonException ex) {
			log.error("Failed to prepare interceptor interface", ex);
			return null;
		}

		if (entryInterceptor == null) {
			log.debug("Using default interceptor class");
			entryInterceptor = DUMMY_ENTRY_INTERCEPTOR;
		}

		return entryInterceptor;
	}

	public EntryInterceptorType createEntryInterceptorWithPythonException() throws PythonException {
		String entryInterceptorPythonScript = cacheRefreshConfiguration.getInterceptorScriptFileName();
		if (StringHelper.isEmpty(entryInterceptorPythonScript)) {
			return null;
		}

		return pythonService.loadPythonScript(entryInterceptorPythonScript, PYTHON_ENTRY_INTERCEPTOR_TYPE, EntryInterceptorType.class,
				new PyObject[] { new PyLong(System.currentTimeMillis()) });
	}

	public void executeEntryInterceptor(EntryInterceptorType entryInterceptorType, GluuCustomPerson targetPerson) {
		try {
			log.debug("Executing python script interceptor in order update entry '{0}'", targetPerson.getInum());
			boolean result = entryInterceptorType.updateAttributes(targetPerson);
			if (!result) {
				log.error(String.format("Failed to update entry '%s' attributes via python interceptor script", targetPerson.getInum()));
			}
		} catch (Exception ex) {
			log.error(ex);
		}
	}

	public void executeEntryInterceptorWithPythonException(EntryInterceptorType entryInterceptorType, GluuCustomPerson targetPerson)
			throws PythonException {
		try {
			boolean result = entryInterceptorType.updateAttributes(targetPerson);
			if (!result) {
				throw new PythonException(String.format("Failed to update entry '%s' attributes via python interceptor script",
						targetPerson.getInum()));
			}
		} catch (Exception ex) {
			throw new PythonException(String.format("Failed to update entry '%s' attributes via python interceptor script",
					targetPerson.getInum()), ex);
		}
	}

}
