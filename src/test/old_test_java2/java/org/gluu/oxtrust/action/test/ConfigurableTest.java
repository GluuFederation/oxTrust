package org.gluu.oxtrust.action.test;

import org.gluu.oxtrust.ldap.service.util.LDAPTestConfiguration;
import org.jboss.seam.mock.SeamTest;

/**
 * Base class for all seam test which requre external configuration
 * 
 * @author Pankaj Date: 08.24.2010
 */
public abstract class ConfigurableTest extends SeamTest {

	private LDAPTestConfiguration conf;

	/**
	 * Prepare configuration before executing tests
	 * 
	 * @throws Exception
	 */
	protected void initTest() throws Exception {
		conf = new LDAPTestConfiguration();
	}

	/**
	 * Get configuration
	 */
	public LDAPTestConfiguration getConf() {
		return conf;
	}

}
