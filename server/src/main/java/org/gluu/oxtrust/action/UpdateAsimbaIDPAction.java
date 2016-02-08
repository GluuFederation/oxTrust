/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.ldap.service.ClientService;
import org.gluu.oxtrust.ldap.service.SvnSyncTimer;
import org.gluu.oxtrust.ldap.service.TemplateService;
import org.gluu.oxtrust.ldap.service.TrustService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.core.ResourceLoader;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.gluu.asimba.util.ldap.idp.IDPEntry;
import org.gluu.oxtrust.ldap.service.AsimbaService;
import org.jboss.seam.annotations.Observer;


/**
 * Action class for updating and adding the SAML IDP to Asimba
 * 
 * @author Dmitry Ognyannikov
 */
@Scope(ScopeType.CONVERSATION)
@Name("updateAsimbaIDPAction")
@Restrict("#{identity.loggedIn}")
public class UpdateAsimbaIDPAction implements Serializable {
    
    @Logger
    private Log log;

    @In(value = "#{oxTrustConfiguration.applicationConfiguration}")
    private ApplicationConfiguration applicationConfiguration;
    
    @In
    protected AttributeService attributeService;

    @In
    private TrustService trustService;

    @In
    private ClientService clientService;

    @In
    private Identity identity;

    @In
    private TemplateService templateService;

    @In
    private SvnSyncTimer svnSyncTimer;
    
    @In
    private FacesMessages facesMessages;

    @In(value = "#{facesContext}")
    private FacesContext facesContext;
    
    @In
    private ResourceLoader resourceLoader;
    
    @In
    private AsimbaService asimbaService;
    
    private IDPEntry idp = new IDPEntry();
    
    private ArrayList<IDPEntry> idpList = new ArrayList<IDPEntry>();
    
    @NotNull
    @Size(min = 0, max = 30, message = "Length of search string should be less than 30")
    private String searchPattern = "";
    
    public UpdateAsimbaIDPAction() {
        //init();
    }
    
    @Observer("org.jboss.seam.postInitialization")
    public void init() {
        IDPEntry entry = new IDPEntry();
        entry.setId("IDP_1");
        entry.setFriendlyName("IDP 1");
        entry.setLastModified(new Date());
        idpList.add(entry);
        //TODO: add list loading
    }
        
    public ArrayList<SelectItem> getAllIDPs() {
        ArrayList<SelectItem> result = new ArrayList<SelectItem>();
//            for (GluuSAMLTrustRelationship federation : trustService.getAllFederations()) {
//                    result.add(new SelectItem(federation, federation.getDisplayName()));
//            }
        return result;
    }
    
    @Restrict("#{s:hasPermission('trust', 'access')}")
    public String add() {
        return OxTrustConstants.RESULT_SUCCESS;
    }
    
    @Restrict("#{s:hasPermission('trust', 'access')}")
    public String update() {
        return OxTrustConstants.RESULT_SUCCESS;
    }
    
    @Restrict("#{s:hasPermission('trust', 'access')}")
    public void cancel() {
    }

    @Restrict("#{s:hasPermission('trust', 'access')}")
    public String save() {
        synchronized (svnSyncTimer) {

        }
        return OxTrustConstants.RESULT_SUCCESS;
    }
    
    @Restrict("#{s:hasPermission('trust', 'access')}")
    public String uploadFile() {
        synchronized (svnSyncTimer) {

        }
        return OxTrustConstants.RESULT_SUCCESS;
    }
    
    @Restrict("#{s:hasPermission('person', 'access')}")
    public String delete() {
        synchronized (svnSyncTimer) {

        }
        return OxTrustConstants.RESULT_SUCCESS;
    }
    
    @Restrict("#{s:hasPermission('person', 'access')}")
    public String search() {
        synchronized (svnSyncTimer) {

        }
        return OxTrustConstants.RESULT_SUCCESS;
    }
    
    

    /**
     * @return the idp
     */
    public IDPEntry getIdp() {
        return idp;
    }

    /**
     * @param idp the idp to set
     */
    public void setIdp(IDPEntry idp) {
        this.idp = idp;
    }

    /**
     * @return the idpList
     */
    public ArrayList<IDPEntry> getIdpList() {
        return idpList;
    }

    /**
     * @param idpList the idpList to set
     */
    public void setIdpList(ArrayList<IDPEntry> idpList) {
        this.idpList = idpList;
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
