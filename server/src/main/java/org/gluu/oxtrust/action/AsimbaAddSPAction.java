/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;
import org.gluu.asimba.util.ldap.sp.RequestorEntry;
import org.gluu.oxtrust.ldap.service.AsimbaService;
import org.gluu.oxtrust.ldap.service.SvnSyncTimer;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;

/**
 * Action class for adding the SAML IDP to Asimba.
 * 
 * @author Dmitry Ognyannikov
 */
@Scope(ScopeType.SESSION)
@Name("asimbaAddSPAction")
@Restrict("#{identity.loggedIn}")
public class AsimbaAddSPAction implements Serializable {

    private static final long serialVersionUID = -1024167091985943689L;
    
    @Logger
    private Log log;

    @In
    private SvnSyncTimer svnSyncTimer;
    
    @In
    private AsimbaService asimbaService;
    
    private RequestorEntry spRequestor;
    
    private String spURL;
    
    @Create
    public void init() {        
        log.info("init() SP call");
    }
    
    public void refresh() {
        log.info("refresh() SP call");
        
        spRequestor = new RequestorEntry();
    }
    
    @Restrict("#{s:hasPermission('trust', 'access')}")
    public String add() {
        log.info("add new SP", spRequestor);
        // save
        synchronized (svnSyncTimer) {
            //TODO
        }
        refresh();
        return OxTrustConstants.RESULT_SUCCESS;
    }
    
    @Restrict("#{s:hasPermission('trust', 'access')}")
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
    
}
