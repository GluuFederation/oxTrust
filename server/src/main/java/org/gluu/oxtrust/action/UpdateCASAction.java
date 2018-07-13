/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2016, Gluu
 */
package org.gluu.oxtrust.action;

import org.gluu.jsf2.message.FacesMessages;
import org.gluu.oxtrust.ldap.service.CASService;
import org.gluu.oxtrust.ldap.service.SvnSyncTimer;
import org.gluu.oxtrust.service.config.cas.CASProtocolConfiguration;
import org.gluu.oxtrust.service.config.cas.CASProtocolConfigurationProvider;
import org.gluu.oxtrust.api.authorization.casprotocol.SessionStorageType;
import org.gluu.oxtrust.service.config.cas.ShibbolethService;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.config.oxtrust.ShibbolethCASProtocolConfiguration;
import org.xdi.service.security.Secure;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

    @Inject
    private Logger log;

    @Inject
    private AppConfiguration appConfiguration;

    @Inject
    private SvnSyncTimer svnSyncTimer;

    @Inject
    private FacesMessages facesMessages;

    @Inject
    private CASService casService;

    @Inject
    transient private CASProtocolConfigurationProvider casProtocolConfigurationProvider;

    @Inject
    transient private ShibbolethService shibbolethService;

    private List<String> sessionStorageTypes = new ArrayList<String>();

    private CASProtocolConfiguration casProtocolConfiguration;

    public UpdateCASAction() {

    }

    @PostConstruct
    public void init() {
        log.info("init() CAS call");

        sessionStorageTypes = new ArrayList<String>();
        sessionStorageTypes.add(SessionStorageType.DEFAULT_STORAGE_SERVICE.getName());
        sessionStorageTypes.add(SessionStorageType.MEMCACHED_STORE_SERVICE.getName());

        try {
            casProtocolConfiguration = casProtocolConfigurationProvider.get();
        } catch (Exception e) {
            log.error("init() CAS - load from LDAP exception", e);
            casProtocolConfiguration = new CASProtocolConfiguration(appConfiguration.getIdpUrl() + "/idp/profile/cas", createNewConfiguration());
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
        newConfiguration.setSessionStorageType(SessionStorageType.DEFAULT_STORAGE_SERVICE.getName());
        return newConfiguration;
    }

    public void save() {
        log.info("save() CAS call");

        try {
            casProtocolConfiguration.save(casService);
            shibbolethService.update(casProtocolConfiguration);

        } catch (Exception e) {
            log.error("save() CAS exception", e);
        }
    }

    /**
     * @return the casBaseURL
     */
    public String getCasBaseURL() {
        return casProtocolConfiguration.getCasBaseURL();
    }

    /**
     * @param casBaseURL the casBaseURL to set
     */
    public void setCasBaseURL(String casBaseURL) {
        this.casProtocolConfiguration.setCasBaseURL(casBaseURL);
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
        return this.casProtocolConfiguration.getConfiguration();
    }

    /**
     * @param configuration the configuration to set
     */
    public void setConfiguration(ShibbolethCASProtocolConfiguration configuration) {
        this.casProtocolConfiguration.setConfiguration(configuration);
    }
}
