/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2016, Gluu
 */
package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.gluu.oxtrust.ldap.service.SvnSyncTimer;
import org.gluu.oxtrust.service.asimba.AsimbaXMLConfigurationService;
import org.gluu.oxtrust.util.KeystoreWrapper;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.oxtrust.util.X509CertificateShortInfo;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;

/**
 * Action class for security certificate management.
 * 
 * @author Dmitry Ognyannikov
 */
@Scope(ScopeType.SESSION)
@Name("certificateManagementAction")
@Restrict("#{identity.loggedIn}")
public class CertificateManagementAction implements Serializable {

    private static final long serialVersionUID = -1938167091985945238L;
    
    @Logger
    private Log log;

    @In
    private SvnSyncTimer svnSyncTimer;
    
    @In
    private AsimbaXMLConfigurationService asimbaXMLConfigurationService;
    
    private KeystoreWrapper keystore;
    
    private List<X509CertificateShortInfo> asimbaCertificates;
    
    @NotNull
    @Size(min = 0, max = 30, message = "Length of search string should be less than 30")
    private String searchPattern = "";
    
    @Create
    public void init() {        
        log.info("init() CertificateManagemen call");
        try {
            keystore = asimbaXMLConfigurationService.getKeystore();
            
            asimbaCertificates = keystore.listCertificates();
        } catch (Exception e) {
            log.error("Load Asimba keystore configuration exception", e); 
        }
    }
    
    public void refresh() {
        log.info("refresh() CertificateManagemen call");
        
        try {
            keystore = asimbaXMLConfigurationService.getKeystore();
            
            asimbaCertificates = keystore.listCertificates();
        } catch (Exception e) {
            log.error("Load Asimba keystore configuration exception", e); 
        }
    }
    
    @Restrict("#{s:hasPermission('trust', 'access')}")
    public String add() {
        log.info("add");
        // save
        synchronized (svnSyncTimer) {
            //TODO
        }
        refresh();
        return OxTrustConstants.RESULT_SUCCESS;
    }
    
    @Restrict("#{s:hasPermission('trust', 'access')}")
    public String cancel() {
        log.info("cancel CertificateManagement");
        
        return OxTrustConstants.RESULT_SUCCESS;
    }
    
    @Restrict("#{s:hasPermission('person', 'access')}")
    public String search() {
        log.info("search() CertificateManagement searchPattern:", searchPattern);
        
        //TODO
        
        return OxTrustConstants.RESULT_SUCCESS;
    }

    /**
     * @return the asimbaCertificates
     */
    public List<X509CertificateShortInfo> getAsimbaCertificates() {
        return asimbaCertificates;
    }

    /**
     * @param asimbaCertificates the asimbaCertificates to set
     */
    public void setAsimbaCertificates(List<X509CertificateShortInfo> asimbaCertificates) {
        this.asimbaCertificates = asimbaCertificates;
    }

    /**
     * @return the searchPattern
     */
    public String getSearchPattern() {
        return searchPattern;
    }

    /**
     * @param searchPattern the searchPattern to set
     */
    public void setSearchPattern(String searchPattern) {
        this.searchPattern = searchPattern;
    }
}
