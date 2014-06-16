package org.gluu.oxtrust.test;

import org.apache.log4j.Logger;
import org.gluu.oxtrust.ldap.service.util.LDAPTestConfiguration;
import org.gluu.oxtrust.util.Configuration;
import org.gluu.site.ldap.LDAPConnectionProvider;
import org.gluu.site.ldap.persistence.LdifDataUtility;
import org.xdi.util.properties.FileConfiguration;

import com.unboundid.ldap.sdk.LDAPConnection;

/**
 * Test class for to test CRUD operations
 *
 * @author Yuriy Movchan
 */
public class AbstractTest {

	private static final Logger log = Logger.getLogger(AbstractTest.class);

	private static LdifDataUtility ldapServerUtility;
	protected static LDAPTestConfiguration conf;
	protected static LDAPConnectionProvider connectionProvider;

	protected static void setSuperBeforeClass() throws Exception {
		log.debug("Preparing connection pool...");
		conf = new LDAPTestConfiguration();
		FileConfiguration localConfiguration = new FileConfiguration(Configuration.CONFIGURATION_FILE_LOCAL_LDAP_PROPERTIES_FILE);
		connectionProvider = new LDAPConnectionProvider(localConfiguration.getProperties());
		if (conf.getBoolean("test.configuration.load-and-cleanup-data")) {
			ldapServerUtility = LdifDataUtility.instance();

			log.debug("Importing new data to LDAP server...");
			LDAPConnection connection = connectionProvider.getConnection();
			long startImport = 0, endImport = 0;
			try {
				startImport = System.currentTimeMillis();
				String ldifFileName = AbstractTest.class.getClassLoader().getResource(conf.getString("test.configuration.ldif-file-name")).getFile();
				ldapServerUtility.importLdifFile(connection, ldifFileName);
				endImport = System.currentTimeMillis();
			} finally {
				connectionProvider.releaseConnection(connection);
			}
			log.debug(String.format("Data imported to LDAP server within %d ms", (endImport - startImport)));
		}
	}

	protected static void tearSuperDownAfterClass() throws Exception {
		if (conf.getBoolean("test.configuration.load-and-cleanup-data")) {
			log.debug("Cleaning up data from LDAP server...");
			LDAPConnection connection = connectionProvider.getConnection();
			long startImport = 0, endImport = 0;
			try {
				startImport = System.currentTimeMillis();
				String ldifFileName = AbstractTest.class.getClassLoader().getResource(conf.getString("test.configuration.ldif-file-name-cleanup")).getFile();
				ldapServerUtility.importLdifFile(connection, ldifFileName);
				endImport = System.currentTimeMillis();
			} finally {
				connectionProvider.releaseConnection(connection);
			}
			log.debug(String.format("Data cleanedup from LDAP server within %d ms", (endImport - startImport)));
		}
		connectionProvider.closeConnectionPool();
	}

}
