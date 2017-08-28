/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.gluu.asimba.util.ldap.idp.IDPEntry;
import org.gluu.asimba.util.ldap.selector.ApplicationSelectorEntry;
import org.gluu.asimba.util.ldap.sp.RequestorEntry;
import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.oxtrust.ldap.service.AsimbaService;
import org.gluu.oxtrust.ldap.service.SvnSyncTimer;
import org.gluu.oxtrust.security.Identity;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.service.security.Secure;


/**
 * Action class for updating and adding the Requestor->IDP Selector to Asimba
 * 
 * @author Dmitry Ognyannikov
 */
@SessionScoped
@Named("updateAsimbaSelectorAction")
@Secure("#{permissionService.hasPermission('trust', 'access')}")
public class UpdateAsimbaSelectorAction implements Serializable {

    private static final long serialVersionUID = -1242167044333943680L;
    
    @Inject
    private Logger log;

    @Inject
    private AppConfiguration appConfiguration;

    @Inject
    private Identity identity;

    @Inject
    private SvnSyncTimer svnSyncTimer;
    
    @Inject
    private FacesMessages facesMessages;
    
    @Inject
    private AsimbaService asimbaService;
    
    @Inject
    private ConversationService conversationService;
    
    @Produces
    private ApplicationSelectorEntry selector;
    
    private boolean newEntry = true;
    
    private String editEntryInum = null;
    
    private List<ApplicationSelectorEntry> selectorList = new ArrayList<ApplicationSelectorEntry>();
    
    private List<SelectItem> idpList;
    
    private List<SelectItem> spRequestorList;
    
    @NotNull
    @Size(min = 0, max = 30, message = "Length of search string should be less than 30")
    private String searchPattern = "";
    
    public UpdateAsimbaSelectorAction() {
        
    }
    
    @PostConstruct
    public void init() {
        log.info("init() Selector call");
        
        clearEdit();
        
        refresh();
    }
    
    public void refresh() {
        log.info("refresh() Selector call");
        
        if (searchPattern == null || "".equals(searchPattern)) {
            //list loading
            selectorList = asimbaService.loadSelectors();
        } else {
            // search mode, clear pattern
            searchPattern = null;
        }
        
        // Load edit lists
        idpList = new ArrayList<SelectItem>();
        List<IDPEntry> idpListEntries = asimbaService.loadIDPs();
        for (IDPEntry entry : idpListEntries) {
            idpList.add(new SelectItem(entry.getId(), entry.getId(), entry.getFriendlyName()));
        }
        
        spRequestorList = new ArrayList<SelectItem>();
        List<RequestorEntry> spRequestorListEntries = asimbaService.loadRequestors();
        for (RequestorEntry entry : spRequestorListEntries) {
            spRequestorList.add(new SelectItem(entry.getId(), entry.getId(), entry.getFriendlyName()));
        }
    }
    
    public void clearEdit() {
        selector = new ApplicationSelectorEntry();
        editEntryInum = null;
        newEntry = true;
    }
    
    /**
     * Set "add new" or "edit" mode by inum request parameter.
     */
    public void edit() {
        log.info("edit() Selector call, inum: "+editEntryInum);
        if (editEntryInum == null || "".equals(editEntryInum) || "new".equals(editEntryInum)) {
            // no inum, new entry mode
            clearEdit();
        } else if ((editEntryInum != null) && (selector != null) && (editEntryInum != selector.getInum())) {
            // edit entry
            newEntry = false;
            selector = asimbaService.readApplicationSelectorEntry(editEntryInum);
        }
    }
    
    public String add() {
        log.info("add() Selector call", selector);
        synchronized (svnSyncTimer) {
            asimbaService.addApplicationSelectorEntry(selector);
        }
        clearEdit();
        conversationService.endConversation();
        
        asimbaService.restartAsimbaService();
        
        return OxTrustConstants.RESULT_SUCCESS;
    }
    
    public String update() {
        log.info("update() Selector", selector);
        synchronized (svnSyncTimer) {
            asimbaService.updateApplicationSelectorEntry(selector);
        }
        clearEdit();
        conversationService.endConversation();
        
        asimbaService.restartAsimbaService();
        
        return OxTrustConstants.RESULT_SUCCESS;
    }
    
    public String cancel() {
        log.info("cancel() Selector", selector);
        clearEdit();
        conversationService.endConversation();
        return OxTrustConstants.RESULT_SUCCESS;
    }
    
    public String delete() {
        log.info("delete() Selector", selector);
        synchronized (svnSyncTimer) {
            asimbaService.removeApplicationSelectorEntry(selector);
        }
        clearEdit();
        conversationService.endConversation();
        return OxTrustConstants.RESULT_SUCCESS;
    }
    
    public String search() {
        log.info("search() Selector searchPattern:", searchPattern);
        synchronized (svnSyncTimer) {
            if (searchPattern != null && !"".equals(searchPattern)){
                try {
                    selectorList = asimbaService.searchSelectors(searchPattern, 0);
                } catch (Exception ex) {
                    log.error("LDAP search exception", ex);
                    return OxTrustConstants.RESULT_FAILURE;
                }
            } else {
                //list loading
                selectorList = asimbaService.loadSelectors();
            }
        }
        return OxTrustConstants.RESULT_SUCCESS;
    }

    /**
     * @return the selector
     */
    public ApplicationSelectorEntry getSelector() {
        return selector;
    }

    /**
     * @param selector the selector to set
     */
    public void setSelector(ApplicationSelectorEntry selector) {
        this.selector = selector;
    }

    /**
     * @return the selectorList
     */
    public List<ApplicationSelectorEntry> getSelectorList() {
        return selectorList;
    }

    /**
     * @param selectorList the selectorList to set
     */
    public void setSelectorList(List<ApplicationSelectorEntry> selectorList) {
        this.selectorList = selectorList;
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
     * @return the idpList
     */
    public List<SelectItem> getIdpList() {
        return idpList;
    }

    /**
     * @param idpList the idpList to set
     */
    public void setIdpList(List<SelectItem> idpList) {
        this.idpList = idpList;
    }

    /**
     * @return the spRequestorList
     */
    public List<SelectItem> getSpRequestorList() {
        return spRequestorList;
    }

    /**
     * @param spRequestorList the spRequestorList to set
     */
    public void setSpRequestorList(List<SelectItem> spRequestorList) {
        this.spRequestorList = spRequestorList;
    }
}
