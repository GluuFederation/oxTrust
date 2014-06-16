package org.gluu.oxtrust.action.test.util;

import org.apache.log4j.Logger;
import org.gluu.oxtrust.action.test.AbstractAuthorizationTest;
import org.gluu.oxtrust.test.AbstractTest;
import org.gluu.site.ldap.LDAPConnectionProvider;
import org.gluu.site.ldap.persistence.LdifDataUtility;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.mock.AbstractSeamTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.unboundid.ldap.sdk.LDAPConnection;

/**
 * Functional test to cleanup data from LDAP server
 *
 * @author Yuriy Movchan Date: 09.23.2010
 */
public class CleanupTestingDataTest extends AbstractAuthorizationTest {

	private static final Logger log = Logger.getLogger(CleanupTestingDataTest.class);

	@BeforeTest
	public void initTestConfiguration() throws Exception {
		initTest();
	}

	@Test
	public void testReloadTestingData() throws Exception {
		new AbstractSeamTest.ComponentTest() {
			protected void testComponents() throws Exception {
				if (getConf().getBoolean("test.configuration.load-and-cleanup-data")) {
					log.debug("Importing new data to LDAP server...");
					LdifDataUtility ldapServerUtility = LdifDataUtility.instance();
					LDAPConnectionProvider connectionProvider = (LDAPConnectionProvider) Contexts.getApplicationContext().get("connectionProvider");
					LDAPConnection connection = connectionProvider.getConnection();
					long startImport = 0, endImport = 0;
					try {
						startImport = System.currentTimeMillis();
						String ldifFileName = AbstractTest.class.getClassLoader().getResource(getConf().getString("test.configuration.ldif-file-name-cleanup")).getFile();
						ldapServerUtility.importLdifFile(connection, ldifFileName);
						endImport = System.currentTimeMillis();
					} finally {
						connectionProvider.releaseConnection(connection);
					}
					log.debug(String.format("Data cleanedup from LDAP server within %d ms", (endImport - startImport)));
				}
			}
		}.run();
	}

}
