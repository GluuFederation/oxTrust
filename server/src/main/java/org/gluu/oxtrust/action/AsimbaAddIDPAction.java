/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;
import org.gluu.asimba.util.ldap.idp.IDPEntry;
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
@Name("asimbaAddIDPAction")
@Restrict("#{identity.loggedIn}")
public class AsimbaAddIDPAction implements Serializable {

    private static final long serialVersionUID = -1024167091985943689L;
    
    @Logger
    private Log log;

    @In
    private SvnSyncTimer svnSyncTimer;
    
    @In
    private AsimbaService asimbaService;
    
    private IDPEntry idp;
    
    @Create
    public void init() {        
        log.info("init() IDP call");
    }
    
    public void refresh() {
        log.info("refresh() IDP call");
        
        idp = new IDPEntry();
    }
    
    @Restrict("#{s:hasPermission('trust', 'access')}")
    public String add() {
        log.info("add new IDP", idp);
        // save
        synchronized (svnSyncTimer) {
            asimbaService.addIDPEntry(idp);
        }
        refresh();
        return OxTrustConstants.RESULT_SUCCESS;
    }
    
    @Restrict("#{s:hasPermission('trust', 'access')}")
    public String cancel() {
        log.info("cancel IDP", idp);
        
        return OxTrustConstants.RESULT_SUCCESS;
    }
    
}
