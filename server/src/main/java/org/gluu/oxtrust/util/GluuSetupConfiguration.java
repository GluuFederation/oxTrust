/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.util;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Parse stored setup properties list from the Gluu Server istaller.
 * 
 * @author Dmitry Ognyannikov, 2016
 */
public class GluuSetupConfiguration {
    private static final Log log = LogFactory.getLog(GluuSetupConfiguration.class);
    
    private final String SETUP_PROPERTIES_FILE_PATH = "/install/community-edition-setup/setup.properties.last";
    
    public static final String ASSIMBA_INSTALLED_KEY = "installAsimba";
    public static final String SAML_IDP_INSTALLED_KEY = "installSaml";
    public static final String CAS_INSTALLED_KEY = "installCas";
    public static final String OX_AUTH_INSTALLED_KEY = "installOxAuth";
    public static final String OX_TRUST_INSTALLED_KEY = "installOxTrust";
    public static final String LDAP_INSTALLED_KEY = "installLdap";
    public static final String HTTPD_INSTALLED_KEY = "installHttpd";
    public static final String OX_AUTH_RP_INSTALLED_KEY = "installOxAuthRP";
    
    private final Properties setupProperties;
    
    public GluuSetupConfiguration() {
        setupProperties = new Properties();
        try {
            setupProperties.load(new FileInputStream(SETUP_PROPERTIES_FILE_PATH));
        } catch (Exception e) {
            log.error("Read Gluu Server setup properties list exception", e);
        }
    }

    /**
     * @return the setupProperties
     */
    public Properties getSetupProperties() {
        return setupProperties;
    }
    
    public String getSetupProperty(String key) {
        return setupProperties.getProperty(key);
    }
    
    public Boolean getSetupPropertyBoolean(String key) {
        String value = setupProperties.getProperty(key);
        
        if (value == null || "".equals(value)) {
            return null;
        } else if ("true".equalsIgnoreCase(value)) {
            return true;
        } else {
            return false;
        }
    }
}
