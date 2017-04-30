/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2016, Gluu
 */
package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.faces.context.FacesContext;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.PropertiesConfigurationLayout;
import org.gluu.oxtrust.ldap.service.CASService;
import org.gluu.oxtrust.ldap.service.Shibboleth3ConfService;
import org.gluu.oxtrust.ldap.service.SvnSyncTimer;
import org.gluu.oxtrust.ldap.service.TrustService;
import org.gluu.oxtrust.model.GluuSAMLTrustRelationship;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import javax.inject.Inject;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Logger;
import javax.inject.Named;
import javax.enterprise.context.ConversationScoped;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.core.ResourceLoader;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.config.oxtrust.LdapShibbolethCASProtocolConfiguration;
import org.xdi.config.oxtrust.ShibbolethCASProtocolConfiguration;

/**
 * Action class for updating CAS protocol Shibboleth IDP properties.
 * 
 * @author Dmitry Ognyannikov
 */
@Scope(ScopeType.SESSION)
@Named("updateCASAction")
@Restrict("#{identity.loggedIn}")
public class UpdateCASAction implements Serializable {

    private static final long serialVersionUID = 1061838191485356624L;
    
    private static final String IDP_SESSION_STORAGESERVICE = "idp.session.StorageService";
    
    private static final String IDP_CAS_STORAGESERVICE = "idp.cas.StorageService";
    
    // server-side storage of user sessions
    private static final String SHIBBOLETH_STORAGESERVICE = "shibboleth.StorageService";
    
    // client-side storage of user sessions
    private static final String CLIENT_SESSION_STORAGESERVICE = "shibboleth.ClientSessionStorageService";
    
    // client-side storage of user sessions
    private static final String SHIBBOLETH_MEMCACHEDSTORAGESERVICE = "shibboleth.MemcachedStorageService";
    
    @Logger
    private Log log;

    @Inject(value = "#{oxTrustConfiguration.applicationConfiguration}")
    private ApplicationConfiguration applicationConfiguration;

    @Inject
    private SvnSyncTimer svnSyncTimer;
    
    @Inject
    private FacesMessages facesMessages;

    @Inject(value = "#{facesContext}")
    private FacesContext facesContext;
    
    @Inject
    private ResourceLoader resourceLoader;
    
    @Inject
    private CASService casService;
    
    @Out
    private String casBaseURL;
    
    private List<String> sessionStorageTypes = new ArrayList<String>();
    
    @Out
    private ShibbolethCASProtocolConfiguration configuration;
    
    public UpdateCASAction() {
        
    }
    
    @Create
    public void init() {        
        log.info("init() CAS call");
        
        sessionStorageTypes = new ArrayList<String>();
        sessionStorageTypes.add(SHIBBOLETH_STORAGESERVICE);
        sessionStorageTypes.add(SHIBBOLETH_MEMCACHEDSTORAGESERVICE);
        
        casBaseURL = applicationConfiguration.getIdpUrl() + "/idp/profile/cas";
        
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
    
    @Restrict("#{s:hasPermission('trust', 'access')}")
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
            String idpConfFolder = Shibboleth3ConfService.instance().getIdpConfDir();
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
            String idpConfFolder = Shibboleth3ConfService.instance().getIdpConfDir();
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
        List<GluuSAMLTrustRelationship> trustRelationships = TrustService.instance().getAllActiveTrustRelationships();
        Shibboleth3ConfService.instance().generateConfigurationFiles(trustRelationships);
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
