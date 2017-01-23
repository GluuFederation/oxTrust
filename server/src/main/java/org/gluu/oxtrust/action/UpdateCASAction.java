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
import org.gluu.asimba.util.ldap.idp.IDPEntry;
import org.gluu.oxtrust.ldap.service.Shibboleth3ConfService;
import org.gluu.oxtrust.ldap.service.SvnSyncTimer;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.core.ResourceLoader;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.xdi.config.oxtrust.ApplicationConfiguration;

/**
 * Action class for updating CAS protocol Shibboleth IDP properties.
 * 
 * @author Dmitry Ognyannikov
 */
@Scope(ScopeType.SESSION)
@Name("updateCASAction")
@Restrict("#{identity.loggedIn}")
public class UpdateCASAction implements Serializable {

    private static final long serialVersionUID = 1061838191485356624L;
    
    private static final String IDP_SESSION_STORAGESERVICE = "idp.session.StorageService";
    
    private static final String IDP_CAS_STORAGESERVICE = "idp.cas.StorageService";
    
    private static final String SHIBBOLETH_STORAGESERVICE = "shibboleth.StorageService";
    
    
    
    @Logger
    private Log log;

    @In(value = "#{oxTrustConfiguration.applicationConfiguration}")
    private ApplicationConfiguration applicationConfiguration;

    @In
    private SvnSyncTimer svnSyncTimer;
    
    @In
    private FacesMessages facesMessages;

    @In(value = "#{facesContext}")
    private FacesContext facesContext;
    
    @In
    private ResourceLoader resourceLoader;
    
    @In
    private Shibboleth3ConfService shibboleth3ConfService;
    
    @Out
    private boolean enabled;
    
    @Out
    private boolean extended;
    
    @Out
    private String casBaseURL;
    
    @Out
    private boolean enableToProxyPatterns;
    
    @Out
    private String authorizedToProxyPattern = "https://([A-Za-z0-9_-]+\\.)*example\\.org(:\\d+)?/.*";
    
    @Out
    private String unauthorizedToProxyPattern = "https://([A-Za-z0-9_-]+\\.)*example\\.org(:\\d+)?/.*";
    
    @Out
    private String sessionStorageType = SHIBBOLETH_STORAGESERVICE;
    
    private List<String> sessionStorageTypes = new ArrayList<String>();
    
    
    
    public UpdateCASAction() {
        
    }
    
    @Create
    public void init() {        
        log.info("init() CAS call");
        
        sessionStorageTypes = new ArrayList<String>();
        sessionStorageTypes.add(SHIBBOLETH_STORAGESERVICE);
        
        casBaseURL = applicationConfiguration.getIdpUrl() + "/idp/profile/cas";
        
        enableToProxyPatterns = false;
        
        clearEdit();
        
        refresh();
    }
    
    public void refresh() {
        log.info("refresh() CAS call");
    }
    
    public void clearEdit() {
        log.info("clearEdit() CAS call");
    }
    
    @Restrict("#{s:hasPermission('trust', 'access')}")
    public void save() {
        log.info("save() CAS call");
        
        if (enabled)
            enable();
        else
            disable();
    }    
    
    
    public void enable() {
        try {
            // enable server-side storage in idp.properties
            String idpConfFolder = shibboleth3ConfService.getIdpConfDir();
            PropertiesConfiguration idpPropertiesConfiguration = new PropertiesConfiguration(idpConfFolder + shibboleth3ConfService.SHIB3_IDP_PROPERTIES_FILE);
            PropertiesConfigurationLayout layoutConfiguration = new PropertiesConfigurationLayout(idpPropertiesConfiguration);
            
            layoutConfiguration.getConfiguration().setProperty(IDP_SESSION_STORAGESERVICE, SHIBBOLETH_STORAGESERVICE);
            layoutConfiguration.getConfiguration().setProperty(IDP_CAS_STORAGESERVICE, SHIBBOLETH_STORAGESERVICE);
            layoutConfiguration.getConfiguration().save();
            
            // enable CAS beans in relying-party.xml
            
        } catch (Exception e) {
            log.error("enable() CAS exception", e);
        }
    }
    
    public void disable() {
        try {
            // enable server-side storage in idp.properties
            String idpConfFolder = shibboleth3ConfService.getIdpConfDir();
            PropertiesConfiguration idpPropertiesConfiguration = new PropertiesConfiguration(idpConfFolder + shibboleth3ConfService.SHIB3_IDP_PROPERTIES_FILE);
            PropertiesConfigurationLayout layoutConfiguration = new PropertiesConfigurationLayout(idpPropertiesConfiguration);
            
            layoutConfiguration.getConfiguration().setProperty(IDP_SESSION_STORAGESERVICE, SHIBBOLETH_STORAGESERVICE);
            layoutConfiguration.getConfiguration().setProperty(IDP_CAS_STORAGESERVICE, SHIBBOLETH_STORAGESERVICE);
            layoutConfiguration.getConfiguration().save();
            
            // disable CAS beans in relying-party.xml
            
        } catch (Exception e) {
            log.error("disable() CAS exception", e);
        }
    }

    /**
     * @return the enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled the enabled to set
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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
     * @return the sessionStorageType
     */
    public String getSessionStorageType() {
        return sessionStorageType;
    }

    /**
     * @param sessionStorageType the sessionStorageType to set
     */
    public void setSessionStorageType(String sessionStorageType) {
        this.sessionStorageType = sessionStorageType;
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
     * @return the enableToProxyPatterns
     */
    public boolean isEnableToProxyPatterns() {
        return enableToProxyPatterns;
    }

    /**
     * @param enableToProxyPatterns the enableToProxyPatterns to set
     */
    public void setEnableToProxyPatterns(boolean enableToProxyPatterns) {
        this.enableToProxyPatterns = enableToProxyPatterns;
    }

    /**
     * @return the authorizedToProxyPattern
     */
    public String getAuthorizedToProxyPattern() {
        return authorizedToProxyPattern;
    }

    /**
     * @param authorizedToProxyPattern the authorizedToProxyPattern to set
     */
    public void setAuthorizedToProxyPattern(String authorizedToProxyPattern) {
        this.authorizedToProxyPattern = authorizedToProxyPattern;
    }

    /**
     * @return the unauthorizedToProxyPattern
     */
    public String getUnauthorizedToProxyPattern() {
        return unauthorizedToProxyPattern;
    }

    /**
     * @param unauthorizedToProxyPattern the unauthorizedToProxyPattern to set
     */
    public void setUnauthorizedToProxyPattern(String unauthorizedToProxyPattern) {
        this.unauthorizedToProxyPattern = unauthorizedToProxyPattern;
    }
}
