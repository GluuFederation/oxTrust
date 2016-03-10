/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.faces.context.FacesContext;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.gluu.oxtrust.ldap.service.SvnSyncTimer;
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
import org.jboss.seam.annotations.Create;
import org.richfaces.event.FileUploadEvent;
import org.richfaces.model.UploadedFile;


/**
 * Action class for updating and adding the SAML IDP to Asimba
 * 
 * @author Dmitry Ognyannikov
 */
@Scope(ScopeType.SESSION)
@Name("updateAsimbaIDPAction")
@Restrict("#{identity.loggedIn}")
public class UpdateAsimbaIDPAction implements Serializable {

    private static final long serialVersionUID = -1032167091333943680L;
    
    @Logger
    private Log log;

    @In(value = "#{oxTrustConfiguration.applicationConfiguration}")
    private ApplicationConfiguration applicationConfiguration;

    @In
    private Identity identity;

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
    
    private IDPEntry idp;
    
    private boolean newEntry = true;
    
    private String editEntryInum = null;
    
    private List<IDPEntry> idpList = new ArrayList<IDPEntry>();
    
    @NotNull
    @Size(min = 0, max = 30, message = "Length of search string should be less than 30")
    private String searchPattern = "";
    
    public UpdateAsimbaIDPAction() {
        
    }
    
    @Create
    public void init() {        
        log.info("init() IDP call");
        
        clearEdit();
        
        refresh();
    }
    
    public void refresh() {
        log.info("refresh() IDP call");
        
        if (searchPattern == null || "".equals(searchPattern)) {
            // list loading
            idpList = asimbaService.loadIDPs();
        } else {
            // search mode, clear pattern
            searchPattern = null;
        }
    }
    
    public void clearEdit() {
        idp = new IDPEntry();
        editEntryInum = null;
        newEntry = true;
    }
    
    @Restrict("#{s:hasPermission('trust', 'access')}")
    public void edit() {
        log.info("edit() IDP call, inum: "+editEntryInum);
        if (editEntryInum == null || "".equals(editEntryInum)) {
            // no inum, new entry mode
            clearEdit();
        } else {
            // edit entry
            newEntry = false;
            idp = asimbaService.readIDPEntry(editEntryInum);
        }
    }
    
    @Restrict("#{s:hasPermission('trust', 'access')}")
    public String add() {
        log.info("add new IDP", idp);
        // save
        synchronized (svnSyncTimer) {
            asimbaService.addIDPEntry(idp);
        }
        clearEdit();
        return OxTrustConstants.RESULT_SUCCESS;
    }
    
    @Restrict("#{s:hasPermission('trust', 'access')}")
    public String update() {
        log.info("update IDP", idp);
        // save
        synchronized (svnSyncTimer) {
            asimbaService.updateIDPEntry(idp);
        }
        return OxTrustConstants.RESULT_SUCCESS;
    }
    
    @Restrict("#{s:hasPermission('trust', 'access')}")
    public String cancel() {
        log.info("cancel IDP", idp);
        clearEdit();
        return OxTrustConstants.RESULT_SUCCESS;
    }
    
    @Restrict("#{s:hasPermission('trust', 'access')}")
    public String uploadFile(FileUploadEvent event) {
        log.info("uploadFile() call for IDP");
        try {
            UploadedFile uploadedFile = event.getUploadedFile();
            String filepath = asimbaService.saveIDPMetadataFile(uploadedFile);
            idp.setMetadataFile(filepath);
        } catch (Exception e) {
            log.info("IDP metadata - uploadFile() exception", e);
        }
        return OxTrustConstants.RESULT_SUCCESS;
    }
    
    @Restrict("#{s:hasPermission('person', 'access')}")
    public String delete() {
        synchronized (svnSyncTimer) {
            asimbaService.removeIDPEntry(idp);
        }
        clearEdit();
        return OxTrustConstants.RESULT_SUCCESS;
    }
    
    @Restrict("#{s:hasPermission('person', 'access')}")
    public String search() {
        log.info("search() IDP searchPattern:", searchPattern);
        synchronized (svnSyncTimer) {
            if (searchPattern != null && !"".equals(searchPattern)){
                try {
                    idpList = asimbaService.searchIDPs(searchPattern, 0);
                } catch (Exception ex) {
                    log.error("LDAP search exception", ex);
                }
            } else {
                //list loading
                idpList = asimbaService.loadIDPs();
            }
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
    public List<IDPEntry> getIdpList() {
        return idpList;
    }

    /**
     * @param idpList the idpList to set
     */
    public void setIdpList(List<IDPEntry> idpList) {
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

    /**
     * @return the newEntry
     */
    public boolean isNewEntry() {
        return newEntry;
    }

    /**
     * @param newEntry the newEntry to set
     */
    public void setNewEntry(boolean newEntry) {
        this.newEntry = newEntry;
    }

    /**
     * @return the editEntryInum
     */
    public String getEditEntryInum() {
        return editEntryInum;
    }

    /**
     * @param editEntryInum the editEntryInum to set
     */
    public void setEditEntryInum(String editEntryInum) {
        this.editEntryInum = editEntryInum;
    }
}
