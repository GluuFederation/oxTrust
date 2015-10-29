/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.util;

import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.log4j.Logger;
import org.gluu.oxtrust.config.OxTrustConfiguration;
import org.xdi.config.CryptoConfigurationFile;
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.util.security.StringEncrypter;

public class DbConnectionUtil {

	private static final Logger log = Logger.getLogger(DbConnectionUtil.class);

	String dbUrl = "";
	String userName = "";
	String password = "";

	static DbConnectionUtil dbConUtil = null;

	/**
	 * Default constructor.
	 */
	private DbConnectionUtil() {
		ApplicationConfiguration applicationConfiguration = OxTrustConfiguration.instance().getApplicationConfiguration();
		String cryptoConfigurationSalt = OxTrustConfiguration.instance().getCryptoConfigurationSalt();
		
		this.dbUrl = applicationConfiguration.getMysqlUrl();
		this.userName = applicationConfiguration.getMysqlUser();
		try {
			String password = applicationConfiguration.getMysqlPassword();
			this.password = StringEncrypter.defaultInstance().decrypt(password, cryptoConfigurationSalt);
			log.debug("Url::: " + dbUrl + " User: " + userName + " Password: " + password);
		} catch (Exception ex) {
			log.error("Error while decrypting MySql connection password: " + applicationConfiguration.getMysqlPassword() + " Msg: "
					+ ex.getMessage());
		}
	}

	public static DbConnectionUtil getInstance() {
		if (dbConUtil == null) {
			dbConUtil = new DbConnectionUtil();
		}
		return dbConUtil;
	}

	public Connection getConnection() throws Exception {
		Class.forName("com.mysql.jdbc.Driver");
		log.debug("Url::: " + dbUrl + " User: " + userName + " Password: " + password);
		Connection con = DriverManager.getConnection(dbUrl, userName, password);
		return con;
	}

	public void closeConnection(Connection con) {
		try {
			if (con != null) {
				con.close();
			}
		} catch (Exception ex) {
			log.error("Error in Connection Closing.");
		}
	}

}
