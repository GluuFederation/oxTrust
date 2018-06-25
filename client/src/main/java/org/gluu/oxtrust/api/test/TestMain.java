/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2018, Gluu
 */
package org.gluu.oxtrust.api.test;

import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.SimpleFormatter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gluu.oxtrust.api.client.OxTrustClient;
import org.gluu.oxtrust.api.client.OxTrustAPIException;

/**
 * Test oxTrust API.
 * REST client integration test.
 * 
 * @author Dmitry Ognyannikov
 */
public class TestMain {

    private static final Logger logger = LogManager.getLogger(TestMain.class);
    
    private Properties configuration;
    
    private String baseURI;
    
    private String login;
    
    private String password;
    
    /**
     * Init tests.
     * 
     * @throws IOException
     */
    public void init() throws IOException {
        final String confFile = "client/conf/configuration.properties";
        configuration = new Properties();
        configuration.load(new FileInputStream(confFile));
        
        baseURI = configuration.getProperty("baseURI");
        login = configuration.getProperty("login");
        password = configuration.getProperty("password");
    }
    
    @SuppressWarnings("deprecation")
	public static void initLogging() {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter());
        java.util.logging.Logger.global.addHandler(handler);
    }
    
    /**
     * Run tests.
     * 
     * @throws APITestException
     */
    public void run() throws Exception {
        OxTrustClient client = new OxTrustClient(baseURI, login, password);
        
        ClientTestScenary clientScenary = new ClientTestScenary(client);
        clientScenary.run();
    }
    
    public static void main(String args[]) {
        initLogging();
        try {
            TestMain test = new TestMain();
            test.init();
            test.run();
        } catch (APITestException e) {
            logger.error("Some test failured with exception", e);
            // report failure
            System.exit(1);
        } catch (OxTrustAPIException e) {
            logger.error("Some oxTrust API call failured with exception", e);
            // report failure
            System.exit(1);
        } catch (Throwable t) {
            logger.error("Runtime exception", t);
            // report failure
            System.exit(1);
        }
    }
}
