<<<<<<< HEAD
/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action.test;

import java.io.File;

import org.gluu.oxtrust.ldap.service.util.TestFileConfiguration;
import org.jboss.seam.mock.JUnitSeamTest;
import org.junit.Before;

/**
 * Base class for all seam test which requre external configuration
 * 
 * @author Pankaj Date: 08.24.2010
 */
public abstract class ConfigurableTest extends JUnitSeamTest {

	public TestFileConfiguration testData;

	/**
	 * Prepare configuration before executing tests
	 * 
	 * @throws Exception
	 */
	@Before
	public void initTest() {
		super.begin();
		this.testData = new TestFileConfiguration(System.getProperty("catalina.home") + File.separator + "conf" + File.separator + "oxTrustLdapTest.properties");
	}

	/**
	 * Get configuration
	 */
	public TestFileConfiguration getTestData() {
		return testData;
	}

}
=======
/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action.test;

import org.gluu.oxtrust.ldap.service.util.TestFileConfiguration;
import org.jboss.seam.mock.JUnitSeamTest;
import org.junit.Before;

/**
 * Base class for all seam test which requre external configuration
 * 
 * @author Pankaj Date: 08.24.2010
 */
public abstract class ConfigurableTest extends JUnitSeamTest {

	public TestFileConfiguration testData;

	/**
	 * Prepare configuration before executing tests
	 * 
	 * @throws Exception
	 */
	@Before
	public void initTest() {
		super.begin();
		this.testData = new TestFileConfiguration("oxTrustLdapTest.properties");
	}

	/**
	 * Get configuration
	 */
	public TestFileConfiguration getTestData() {
		return testData;
	}

}
>>>>>>> origin/scim-2.0
