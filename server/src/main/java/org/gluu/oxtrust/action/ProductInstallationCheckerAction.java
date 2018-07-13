/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2016, Gluu
 */
package org.gluu.oxtrust.action;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.ldap.service.Shibboleth3ConfService;
import org.gluu.oxtrust.service.config.cas.CASProtocolAvailability;
import org.gluu.oxtrust.util.ProductInstallationChecker;
import org.slf4j.Logger;
import org.xdi.service.security.Secure;

/**
 * Action class for updating and adding the SAML IDP to Asimba.
 * 
 * @author Dmitry Ognyannikov
 */
@ApplicationScoped
@Named
@Secure("#{identity.loggedIn}")
public class ProductInstallationCheckerAction implements Serializable {

    private static final long serialVersionUID = 1125167091541923404L;
    
    @Inject
    private Logger log;

    @Inject
    private Shibboleth3ConfService shibboleth3ConfService;
    
    private boolean showSAMLMenu = true;
    private boolean showAsimbaSubmenu = true;
    private boolean showSAMLSubmenu = true;
    private CASProtocolAvailability casProtocolAvailability = CASProtocolAvailability.ENABLED;
    
    public ProductInstallationCheckerAction() {
    }
    
    @PostConstruct
    public void init() {        
        log.info("init() ProductInstallationCheckerAction call");
        
        showSAMLMenu = !ProductInstallationChecker.isGluuCE() || ProductInstallationChecker.isOxAsimbaInstalled() || (shibboleth3ConfService.isIdpInstalled() && ProductInstallationChecker.isShibbolethIDP3Installed());
        
        showAsimbaSubmenu = !ProductInstallationChecker.isGluuCE() || ProductInstallationChecker.isOxAsimbaInstalled();
        
        showSAMLSubmenu = !ProductInstallationChecker.isGluuCE() || shibboleth3ConfService.isIdpInstalled();

        casProtocolAvailability = CASProtocolAvailability.get();
    }

    /**
     * @return the showSAMLMenu
     */
    public boolean isShowSAMLMenu() {
        return showSAMLMenu;
    }

    /**
     * @param showSAMLMenu the showSAMLMenu to set
     */
    public void setShowSAMLMenu(boolean showSAMLMenu) {
        this.showSAMLMenu = showSAMLMenu;
    }

    /**
     * @return the showAsimbaSubmenu
     */
    public boolean isShowAsimbaSubmenu() {
        return showAsimbaSubmenu;
    }

    /**
     * @param showAsimbaSubmenu the showAsimbaSubmenu to set
     */
    public void setShowAsimbaSubmenu(boolean showAsimbaSubmenu) {
        this.showAsimbaSubmenu = showAsimbaSubmenu;
    }

    /**
     * @return the showSAMLSubmenu
     */
    public boolean isShowSAMLSubmenu() {
        return showSAMLSubmenu;
    }

    /**
     * @param showSAMLSubmenu the showSAMLSubmenu to set
     */
    public void setShowSAMLSubmenu(boolean showSAMLSubmenu) {
        this.showSAMLSubmenu = showSAMLSubmenu;
    }

    /**
     * @return the showIDP_CAS
     */
    public boolean isShowIDP_CAS() {
        return casProtocolAvailability.isAvailable();
    }

    /**
     * @param showIDP_CAS the showIDP_CAS to set
     */
    public void setShowIDP_CAS(boolean showIDP_CAS) {
        this.casProtocolAvailability = CASProtocolAvailability.from(showIDP_CAS);
    }
    
    
    
}
