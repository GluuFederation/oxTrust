/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2016, Gluu
 */
package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.PropertiesConfigurationLayout;
import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.config.oxtrust.ShibbolethCASProtocolConfiguration;
import org.gluu.oxtrust.model.GluuSAMLTrustRelationship;
import org.gluu.oxtrust.service.CASService;
import org.gluu.oxtrust.service.Shibboleth3ConfService;
import org.gluu.oxtrust.service.TrustService;
import org.gluu.service.security.Secure;
import org.slf4j.Logger;

/**
 * Action class for updating CAS protocol Shibboleth IDP properties.
 * 
 * @author Dmitry Ognyannikov
 */
@SessionScoped
@Named
@Secure("#{permissionService.hasPermission('trust', 'access')}")
public class UpdateCASAction implements Serializable {

    private static final long serialVersionUID = 1061838191485356624L;
    
    private String IDP_SESSION_STORAGESERVICE = "idp.session.StorageService";
    
    private String IDP_CAS_STORAGESERVICE = "idp.cas.StorageService";
    
    // server-side storage of user sessions
    private String SHIBBOLETH_STORAGESERVICE = "shibboleth.StorageService";
    
    // client-side storage of user sessions
    private String CLIENT_SESSION_STORAGESERVICE = "shibboleth.ClientSessionStorageService";
    
    // client-side storage of user sessions
    private String SHIBBOLETH_MEMCACHEDSTORAGESERVICE = "shibboleth.MemcachedStorageService";
    
    @Inject
    private Logger log;

    @Inject
    private AppConfiguration appConfiguration;
    
    @Inject
    private TrustService trustService;
    
    @Inject
    private Shibboleth3ConfService shibboleth3ConfService;
    
    @Inject
    private CASService casService;
    
    private String casBaseURL;
    
    private List<String> sessionStorageTypes = new ArrayList<String>();
    
    @Produces
    private ShibbolethCASProtocolConfiguration configuration;
    
    public UpdateCASAction() {
        
    }
    
    @PostConstruct
    public void init() {        
        log.info("init() CAS call");
        
        sessionStorageTypes = new ArrayList<String>();
        sessionStorageTypes.add(SHIBBOLETH_STORAGESERVICE);
        sessionStorageTypes.add(SHIBBOLETH_MEMCACHEDSTORAGESERVICE);
        
        casBaseURL = appConfiguration.getIdpUrl() + "/idp/profile/cas";
        
        try {
            configuration = casService.loadCASConfiguration();
            
            if (configuration == null) {
                log.info("CAS Configuration not found, create new");
                configuration = createNewConfiguration();
                
                casService.addCASConfiguration(configuration);
            }
        } catch (Exception e) {
            log.error("init() CAS - load from LDAP exception", e);
            configuration = createNewConfiguration();
        }
        
        clearEdit();
        
        refresh();
    }
    
    public void refresh() {
        log.info("refresh() CAS call");
    }
    
    public void clearEdit() {
        log.info("clearEdit() CAS call");
    }
    
    private ShibbolethCASProtocolConfiguration createNewConfiguration() {
        ShibbolethCASProtocolConfiguration newConfiguration =  new ShibbolethCASProtocolConfiguration();
        newConfiguration.setEnabled(false);
        newConfiguration.setEnableToProxyPatterns(false);
        newConfiguration.setAuthorizedToProxyPattern("https://([A-Za-z0-9_-]+\\.)*example\\.org(:\\d+)?/.*");
        newConfiguration.setUnauthorizedToProxyPattern("https://([A-Za-z0-9_-]+\\.)*example\\.org(:\\d+)?/.*");
        newConfiguration.setSessionStorageType(SHIBBOLETH_STORAGESERVICE);
        return newConfiguration;
    }
    
    public void save() {
        log.info("save() CAS call");
        
        try {
            if (configuration.getInum() == null || configuration.getInum().isEmpty() )
                casService.addCASConfiguration(configuration);
            else
                casService.updateCASConfiguration(configuration);
            
            if (configuration.isEnabled())
                enable();
            else
                disable();
        } catch (Exception e) {
            log.error("save() CAS exception", e);
        }
    }    
    
    
    public void enable() {
        try {
            log.info("enable() CAS call");
            // enable server-side storage in idp.properties
            String idpConfFolder = shibboleth3ConfService.getIdpConfDir();
            PropertiesConfiguration idpPropertiesConfiguration = new PropertiesConfiguration(idpConfFolder + Shibboleth3ConfService.SHIB3_IDP_PROPERTIES_FILE);
            PropertiesConfigurationLayout layoutConfiguration = new PropertiesConfigurationLayout(idpPropertiesConfiguration);
            
            // CAS require server-side storage
            layoutConfiguration.getConfiguration().setProperty(IDP_SESSION_STORAGESERVICE, configuration.getSessionStorageType());
            layoutConfiguration.getConfiguration().setProperty(IDP_CAS_STORAGESERVICE, configuration.getSessionStorageType());
            layoutConfiguration.getConfiguration().save();
            
            // enable CAS beans in relying-party.xml
            
            updateShibboleth3Configuration();
            
            log.info("enable() CAS - enabled");
        } catch (Exception e) {
            log.error("enable() CAS exception", e);
        }
    }
    
    public void disable() {
        try {
            log.info("disable() CAS call");
            // enable server-side storage in idp.properties
            String idpConfFolder = shibboleth3ConfService.getIdpConfDir();
            PropertiesConfiguration idpPropertiesConfiguration = new PropertiesConfiguration(idpConfFolder + Shibboleth3ConfService.SHIB3_IDP_PROPERTIES_FILE);
            PropertiesConfigurationLayout layoutConfiguration = new PropertiesConfigurationLayout(idpPropertiesConfiguration);
            
            // Restore default - client session storage
            layoutConfiguration.getConfiguration().setProperty(IDP_SESSION_STORAGESERVICE, CLIENT_SESSION_STORAGESERVICE);
            layoutConfiguration.getConfiguration().setProperty(IDP_CAS_STORAGESERVICE, configuration.getSessionStorageType());
            layoutConfiguration.getConfiguration().save();
            
            // disable CAS beans in relying-party.xml
            
            updateShibboleth3Configuration();  
            
            log.info("disable() CAS - enabled");
        } catch (Exception e) {
            log.error("disable() CAS exception", e);
        }
    }

    private void updateShibboleth3Configuration() {
        List<GluuSAMLTrustRelationship> trustRelationships = trustService.getAllActiveTrustRelationships();
        shibboleth3ConfService.generateConfigurationFiles(trustRelationships);
    }

    /**
     * @return the casBaseURL
     */
    public String getCasBaseURL() {
        return casBaseURL;
    }

    /**
     * @param casBaseURL the casBaseURL to set
     */
    public void setCasBaseURL(String casBaseURL) {
        this.casBaseURL = casBaseURL;
    }

    /**
     * @return the sessionStorageTypes
     */
    public List<String> getSessionStorageTypes() {
        return sessionStorageTypes;
    }

    /**
     * @param sessionStorageTypes the sessionStorageTypes to set
     */
    public void setSessionStorageTypes(List<String> sessionStorageTypes) {
        this.sessionStorageTypes = sessionStorageTypes;
    }

    /**
     * @return the configuration
     */
    public ShibbolethCASProtocolConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * @param configuration the configuration to set
     */
    public void setConfiguration(ShibbolethCASProtocolConfiguration configuration) {
        this.configuration = configuration;
    }
}
