/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import javax.faces.application.FacesMessage;
import org.gluu.asimba.util.ldap.sp.RequestorEntry;
import org.gluu.oxtrust.ldap.service.AsimbaService;
import org.gluu.oxtrust.ldap.service.SvnSyncTimer;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.slf4j.Logger;

/**
 * Action class for adding the SAML IDP to Asimba.
 * 
 * @author Dmitry Ognyannikov
 */
@SessionScoped
@Named("asimbaAddSPAction")
//TODO CDI @Restrict("#{identity.loggedIn}")
public class AsimbaAddSPAction implements Serializable {

    private static final long serialVersionUID = -1024167091985943689L;
    
    @Inject
    private Logger log;

    @Inject
    private SvnSyncTimer svnSyncTimer;
    
    @Inject
    private AsimbaService asimbaService;
    
    private RequestorEntry spRequestor;
    
    private String spURL;
    
    private String spType;
    
    @PostConstruct
    public void init() {        
        log.info("init() SP call");
    }
    
    public void refresh() {
        log.info("refresh() SP call");
        
        spRequestor = new RequestorEntry();
    }
    
    //TODO CDI @Restrict("#{s:hasPermission('trust', 'access')}")
    public String add() {
        log.info("add new SP", spRequestor);
        // save
        synchronized (svnSyncTimer) {
            //TODO
        }
        refresh();
        return OxTrustConstants.RESULT_SUCCESS;
    }
    
    //TODO CDI @Restrict("#{s:hasPermission('trust', 'access')}")
    public String cancel() {
        log.info("cancel SP", spRequestor);
        
        return OxTrustConstants.RESULT_SUCCESS;
    }

    /**
     * @return the spURL
     */
    public String getSpURL() {
        return spURL;
    }

    /**
     * @param spURL the spURL to set
     */
    public void setSpURL(String spURL) {
        this.spURL = spURL;
    }

    /**
     * @return the spRequestor
     */
    public RequestorEntry getSpRequestor() {
        return spRequestor;
    }

    /**
     * @param spRequestor the spRequestor to set
     */
    public void setSpRequestor(RequestorEntry spRequestor) {
        this.spRequestor = spRequestor;
    }

    /**
     * @return the spType
     */
    public String getSpType() {
        return spType;
    }

    /**
     * @param spType the spType to set
     */
    public void setSpType(String spType) {
        this.spType = spType;
    }
    
}
