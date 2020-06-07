package org.gluu.oxtrust.service;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;
import javax.ws.rs.ext.Provider;

/**
 * Integration with Resteasy
 *
 * @author Yuriy Movchan
 * @version June 6, 2017
 */
// TODO: Try to move to test source folder
@Provider
public class TestResteasyInitializer extends Application {

	@Override
	public Set<Class<?>> getClasses() {
		HashSet<Class<?>> classes = new HashSet<Class<?>>();
		return classes;
	}

}