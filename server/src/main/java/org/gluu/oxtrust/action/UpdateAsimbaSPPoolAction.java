/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.gluu.asimba.util.ldap.sp.RequestorPoolEntry;
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
 * Action class for updating and adding the SAML SP to Asimba
 * 
 * @author Dmitry Ognyannikov
 */
@SessionScoped
@Named
@Secure("#{permissionService.hasPermission('trust', 'access')}")
public class UpdateAsimbaSPPoolAction implements Serializable {

    private static final long serialVersionUID = -1242167022433943680L;
    
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
    private RequestorPoolEntry spPool;
    
    private boolean newEntry = true;
    
    private String editEntryInum = null;
    
    private String spPoolAdditionalProperties = "";
    
    private List<RequestorPoolEntry> spPoolList = new ArrayList<RequestorPoolEntry>();
    
    @NotNull
    @Size(min = 0, max = 30, message = "Length of search string should be less than 30")
    private String searchPattern = "";
    
    public UpdateAsimbaSPPoolAction() {
        
    }
    
    @PostConstruct
    public void init() {
        log.info("init() SPPool call");
        
        clearEdit();
        
        refresh();
    }
    
    public void refresh() {
        log.info("refresh() SPPool call");
        
        if (searchPattern == null || "".equals(searchPattern)) {
            //list loading
            spPoolList = asimbaService.loadRequestorPools();
        } else {
            // search mode, clear pattern
            searchPattern = null;
        }
    }
    
    public void clearEdit() {
        spPool = new RequestorPoolEntry();
        // dafault fields
        spPool.setPostAuthorizationProfileID("postauthz.1");
        spPool.setAttributeReleasePolicyID("asimba.releasepolicy.1");
        spPool.setAuthenticationProfileIDs("remote.saml2");
        
        editEntryInum = null;
        newEntry = true;
    }
    
    /**
     * Set "add new" or "edit" mode by inum request parameter.
     */
    public void edit() {
        log.info("edit() SPPool call, inum: "+editEntryInum);
        if (editEntryInum == null || "".equals(editEntryInum) || "new".equals(editEntryInum)) {
            // no inum, new entry mode
            clearEdit();
        } else if ((editEntryInum != null) && (spPool != null) && (editEntryInum != spPool.getInum())) {
            // edit entry
            newEntry = false;
            spPool = asimbaService.readRequestorPoolEntry(editEntryInum);
            if (spPool != null) {
                setProperties(spPool.getProperties());
            }
        }
    }
    
    public String add() {
        log.info("add new RequestorPool", spPool);
        spPool.setProperties(getProperties());
        synchronized (svnSyncTimer) {
            asimbaService.addRequestorPoolEntry(spPool);
        }
        clearEdit();
        conversationService.endConversation();
        
        asimbaService.restartAsimbaService();
        
        return OxTrustConstants.RESULT_SUCCESS;
    }
    
    public String update() {
        log.info("update() RequestorPool", spPool);
        spPool.setProperties(getProperties());
        synchronized (svnSyncTimer) {
            asimbaService.updateRequestorPoolEntry(spPool);
        }
        clearEdit();
        conversationService.endConversation();
        
        asimbaService.restartAsimbaService();
        
        return OxTrustConstants.RESULT_SUCCESS;
    }
    
    public String cancel() {
        log.info("cancel() RequestorPool", spPool);
        clearEdit();
        conversationService.endConversation();
        return OxTrustConstants.RESULT_SUCCESS;
    }
    
    public String delete() {
        log.info("delete() RequestorPool", spPool);
        synchronized (svnSyncTimer) {
            asimbaService.removeRequestorPoolEntry(spPool);
        }
        clearEdit();
        conversationService.endConversation();
        return OxTrustConstants.RESULT_SUCCESS;
    }
    
    public String search() {
        log.info("search() RequestorPool searchPattern:", searchPattern);
        synchronized (svnSyncTimer) {
            if (searchPattern != null && !"".equals(searchPattern)){
                try {
                    spPoolList = asimbaService.searchRequestorPools(searchPattern, 0);
                } catch (Exception ex) {
                    log.error("LDAP search exception", ex);
                    return OxTrustConstants.RESULT_FAILURE;
                }
            } else {
                //list loading
                spPoolList = asimbaService.loadRequestorPools();
            }
        }
        return OxTrustConstants.RESULT_SUCCESS;
    }
    
    private Properties getProperties() {
        if (spPoolAdditionalProperties == null || "".equals(spPoolAdditionalProperties)) {
            // empty set
            return new Properties();
        }
        try {
            Properties p = new Properties();
            p.load(new StringReader(spPoolAdditionalProperties));
            return p;
        } catch (Exception ex) {
            log.error("cannot parse SPRequestorPool properties: " + spPoolAdditionalProperties);
            return new Properties(); 
        }
    }
    
    private void setProperties(Properties properties) {
        if (properties != null && properties.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (String propertyName : properties.stringPropertyNames()) {
                String value = properties.getProperty(propertyName);
                sb.append(propertyName + "=" + value + "\n");
            }
            spPoolAdditionalProperties = sb.toString();
        } else {
            spPoolAdditionalProperties = "";
        }
    }

    /**
     * @return the spPool
     */
    public RequestorPoolEntry getSpPool() {
        return spPool;
    }

    /**
     * @param spPool the spPool to set
     */
    public void setSpPool(RequestorPoolEntry spPool) {
        this.spPool = spPool;
    }

    /**
     * @return the spPoolList
     */
    public List<RequestorPoolEntry> getSpPoolList() {
        return spPoolList;
    }

    /**
     * @param spPoolList the spPoolList to set
     */
    public void setSpPoolList(List<RequestorPoolEntry> spPoolList) {
        this.spPoolList = spPoolList;
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
     * @return the spRequestorAdditionalProperties
     */
    public String getSpPoolAdditionalProperties() {
        return spPoolAdditionalProperties;
    }
    
    public Properties getSpAdditionalPropertiesAsProperties() throws IOException {
        Properties result = new Properties();
        result.load(new StringReader(spPoolAdditionalProperties));
        return result;
    }

    /**
     * @param spPoolAdditionalProperties the spPoolAdditionalProperties to set
     */
    public void setSpPoolAdditionalProperties(String spPoolAdditionalProperties) {
        this.spPoolAdditionalProperties = spPoolAdditionalProperties;
    }
    
    public void setSpAdditionalProperties(Properties additionalProperties) {
        StringWriter writer = new StringWriter();
        for (String property : additionalProperties.stringPropertyNames()) {
            writer.write(property);
            writer.write("=");
            writer.write(additionalProperties.getProperty(property));
            writer.write("\n");
        }
        this.spPoolAdditionalProperties = writer.toString();
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
