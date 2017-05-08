/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import javax.faces.application.FacesMessage;import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.gluu.asimba.util.ldap.idp.IDPEntry;
import org.gluu.oxtrust.ldap.service.AsimbaService;
import org.gluu.oxtrust.ldap.service.SvnSyncTimer;
import org.gluu.oxtrust.service.asimba.AsimbaXMLConfigurationService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.oxtrust.util.ServiceUtil;
import org.gluu.jsf2.message.FacesMessages;
import org.jboss.seam.international.FacesMessage;
import org.richfaces.event.FileUploadEvent;
import org.richfaces.model.UploadedFile;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;

import jnr.ffi.annotations.Out;


/**
 * Action class for updating and adding the SAML IDP to Asimba.
 * 
 * @author Dmitry Ognyannikov
 */
@SessionScoped
@Named("updateAsimbaIDPAction")
//TODO CDI @Restrict("#{identity.loggedIn}")
public class UpdateAsimbaIDPAction implements Serializable {

    private static final long serialVersionUID = -1032167091333943680L;
    
    @Inject
    private Logger log;

    @Inject
    private AppConfiguration appConfiguration;

    @Inject
    private SvnSyncTimer svnSyncTimer;
    
    @Inject
    private FacesMessages facesMessages;

    @Inject
    private FacesContext facesContext;
    
    @Inject
    private ResourceLoader resourceLoader;
    
    @Inject
    private AsimbaService asimbaService;
    
    @Inject
    private AsimbaXMLConfigurationService asimbaXMLConfigurationService;
    
    @Out
    private IDPEntry idp;
    
    private String selectedIdpId = "";
    
    private boolean newEntry = true;
    
    private String editEntryInum = null;
    
    private List<IDPEntry> idpList = new ArrayList<IDPEntry>();
    
    private String idpType;
    
    @NotNull
    @Size(min = 0, max = 30, message = "Length of search string should be less than 30")
    private String searchPattern = "";
    
    private byte uploadedCertBytes[] = null;
    
    public UpdateAsimbaIDPAction() {
        
    }
    
    @PostConstruct
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
            
            // sort by priority field
            try {
                Collections.sort(idpList, new Comparator<IDPEntry>() {
                    @Override
                    public int compare(IDPEntry entry1, IDPEntry entry2) {
                        return (entry1.getViewPriorityIndex() < entry2.getViewPriorityIndex()) ? -1 : (entry1.getViewPriorityIndex() > entry2.getViewPriorityIndex()) ? 1 : 0;
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                log.error("sort exception", e);
            }
        } else {
            // search mode, clear pattern
            searchPattern = null;
        }
    }
    
    public void clearEdit() {
        log.info("clearEdit() IDP call");
        idp = new IDPEntry();
        editEntryInum = null;
        newEntry = true;
        uploadedCertBytes = null;
    }
    
    //TODO CDI @Restrict("#{s:hasPermission('trust', 'access')}")
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
    
    //TODO CDI @Restrict("#{s:hasPermission('trust', 'access')}")
    public String add() {
        log.info("add new IDP", idp);
        // save
        synchronized (svnSyncTimer) {
            asimbaService.addIDPEntry(idp);
        }
        // save certificate
        try {
            if (uploadedCertBytes != null) {
                String message = asimbaXMLConfigurationService.addCertificateFile(uploadedCertBytes, idp.getId());
            }
        } catch (Exception e) {
            log.error("Requestor certificate - add CertificateFile exception", e);
        }
        clearEdit();
        return OxTrustConstants.RESULT_SUCCESS;
    }
    
    //TODO CDI @Restrict("#{s:hasPermission('trust', 'access')}")
    public String update() {
        log.info("update IDP", idp);
        idp.setId(idp.getId().trim());
        // save
        synchronized (svnSyncTimer) {
            asimbaService.updateIDPEntry(idp);
        }
        // save certificate
        try {
            if (uploadedCertBytes != null) {
                String message = asimbaXMLConfigurationService.addCertificateFile(uploadedCertBytes, idp.getId());
            }
        } catch (Exception e) {
            log.error("Requestor certificate - add CertificateFile exception", e);
        }
        newEntry = false;
        return OxTrustConstants.RESULT_SUCCESS;
    }
    
    //TODO CDI @Restrict("#{s:hasPermission('trust', 'access')}")
    public String cancel() {
        log.info("cancel IDP", idp);
        clearEdit();
        return OxTrustConstants.RESULT_SUCCESS;
    }
    
    //TODO CDI @Restrict("#{s:hasPermission('trust', 'access')}")
    public String uploadFile(FileUploadEvent event) {
        log.info("uploadFile() call for IDP");
        try {
            UploadedFile uploadedFile = event.getUploadedFile();
            String filepath = asimbaService.saveIDPMetadataFile(uploadedFile, idp);
            idp.setMetadataFile(filepath);
            idp.setMetadataUrl("");
            facesMessages.add(FacesMessage.SEVERITY_INFO, "File uploaded");
        } catch (Exception e) {
            log.error("IDP metadata - uploadFile() exception", e);
            facesMessages.add(FacesMessage.SEVERITY_ERROR, "Requestor metadata - uploadFile exception", e);
            return OxTrustConstants.RESULT_FAILURE;
        }
        return OxTrustConstants.RESULT_SUCCESS;
    }
    
    //TODO CDI @Restrict("#{s:hasPermission('trust', 'access')}")
    public String uploadCertificateFile(FileUploadEvent event) {
        log.info("uploadCertificateFile() call for IDP");
         try {
            UploadedFile uploadedFile = event.getUploadedFile();
            uploadedCertBytes = ServiceUtil.readFully(uploadedFile.getInputStream());
            
            // check alias for valid url
            String id = idp.getId();
            if (id != null && id.trim().toLowerCase().startsWith("http")) {
                id = id.trim();
                URL u = new URL(id); // this would check for the protocol
                u.toURI(); // does the extra checking required for validation of URI 
                
                String message = asimbaXMLConfigurationService.addCertificateFile(uploadedFile, id);
                // display message
                if (!OxTrustConstants.RESULT_SUCCESS.equals(message)) {
                    facesMessages.add(FacesMessage.SEVERITY_ERROR, "Add Certificate ERROR: ", message);
                } else {
                    facesMessages.add(FacesMessage.SEVERITY_INFO, "Certificate uploaded");
                }
            } else {
                facesMessages.add(FacesMessage.SEVERITY_INFO, "Add valid URL to ID");
            }
        } catch (Exception e) {
            log.info("IDP certificate - uploadCertificateFile() exception", e);
            facesMessages.add(FacesMessage.SEVERITY_ERROR, "Add Certificate ERROR: ", e.getMessage());
            return OxTrustConstants.RESULT_VALIDATION_ERROR;
        }
        return OxTrustConstants.RESULT_SUCCESS;
    }
    
    //TODO CDI @Restrict("#{s:hasPermission('person', 'access')}")
    public String delete() {
        synchronized (svnSyncTimer) {
            asimbaService.removeIDPEntry(idp);
        }
        clearEdit();
        return OxTrustConstants.RESULT_SUCCESS;
    }
    
    //TODO CDI @Restrict("#{s:hasPermission('person', 'access')}")
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
    
    //TODO CDI @Restrict("#{s:hasPermission('trust', 'access')}")
    public void moveIdpUp() {
        log.info("moveIdpUp()");
        log.info("selectedIdpId: " + selectedIdpId);
        
        if (selectedIdpId == null || "".equals(selectedIdpId))
            return;
        
        IDPEntry idp = null;
        
        // serch entry
        for (IDPEntry entry : idpList)
            if (selectedIdpId.equals(entry.getId())) {
                idp = entry;
                break;
            }
        
        int index = idpList.lastIndexOf(idp);
        
        if (index > 0) {            
            // move other entries to 1 step lowest
            for (int i=0; i<idpList.size(); i++) {
                IDPEntry entry = idpList.get(i);
                
                if (i == index-1) {
                    // position to swap
                    entry.setViewPriorityIndex(i+1);
                } else if (i == index) {
                    // target idp to up
                    //move entry priority to 1 step topest 
                    entry.setViewPriorityIndex(i-1);
                } else {
                    // before and after new idp position
                    entry.setViewPriorityIndex(i);
                }
                
                asimbaService.updateIDPEntry(entry);
            }
        }
        
        selectedIdpId = null;
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

    /**
     * @return the idpType
     */
    public String getIdpType() {
        return idpType;
    }

    /**
     * @param idpType the idpType to set
     */
    public void setIdpType(String idpType) {
        this.idpType = idpType;
    }

    /**
     * @return the selectedIdpId
     */
    public String getSelectedIdpId() {
        return selectedIdpId;
    }

    /**
     * @param selectedIdpId the selectedIdpId to set
     */
    public void setSelectedIdpId(String selectedIdpId) {
        this.selectedIdpId = selectedIdpId;
    }
}
