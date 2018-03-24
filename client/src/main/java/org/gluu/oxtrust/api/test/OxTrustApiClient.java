package org.gluu.oxtrust.api.test;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gluu.oxtrust.api.client.OxTrustClient;

public class OxTrustApiClient {
	private static final Logger logger = LogManager.getLogger(TestMain.class);

	private Properties configuration;

	private String baseURI;

	private String login;

	private String password;
	static OxTrustClient client;

	public void initAndConnect() throws IOException, KeyManagementException, NoSuchAlgorithmException {
		final String confFile = "conf/configuration.properties";
		configuration = new Properties();
		configuration.load(new FileInputStream(confFile));
		baseURI = configuration.getProperty("baseURI");
		login = configuration.getProperty("login");
		password = configuration.getProperty("password");
		client = new OxTrustClient(baseURI, login, password);
		System.out.println("Base Url:"+client.getBaseURI());
	}

	public static void main(String[] args) {
		OxTrustApiClient oxTrustApiClient = new OxTrustApiClient();
		testGroupScenary(oxTrustApiClient);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	private static void testGroupScenary(OxTrustApiClient oxTrustApiClient ) {
		try {
			oxTrustApiClient.initAndConnect();
			GroupTestScenary groupTestScenary = GroupTestScenary.builder().withOxTrustClient(client).build();
			groupTestScenary.performAdd();
			//groupTestScenary.performFetchAll();
			//String inum="@!619C.061B.1A7E.5AF4!0001!4377.CD0A!0003!BEB7.1E6E";
			//groupTestScenary.performFetchById(inum);
			//groupTestScenary.performDelete(inum);
		} catch (KeyManagementException e) {
			logger.error("", e);
		} catch (NoSuchAlgorithmException e) {
			logger.error("", e);
		} catch (IOException e) {
			logger.error("", e);
		}
	}

}
