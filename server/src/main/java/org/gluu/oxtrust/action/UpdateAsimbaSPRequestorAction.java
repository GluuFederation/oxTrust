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
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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
import org.jboss.seam.core.ResourceLoader;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;
import org.xdi.config.oxtrust.ApplicationConfiguration;


/**
 * Action class for updating and adding the SAML SP Requestor (=client application) to Asimba
 * 
 * @author Dmitry Ognyannikov
 */
@Scope(ScopeType.CONVERSATION)
@Name("updateAsimbaSPRequestorAction")
@Restrict("#{identity.loggedIn}")
public class UpdateAsimbaSPRequestorAction implements Serializable {

    private static final long serialVersionUID = -1342167044333943680L;
    
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
    
    private RequestorEntry spRequestor = new RequestorEntry();
    
    private String spRequestorAdditionalProperties = "";
    
    private List<RequestorEntry> spRequestorList = new ArrayList<RequestorEntry>();
    
    private ArrayList<SelectItem> spList = new ArrayList<SelectItem>();
    
    @NotNull
    @Size(min = 0, max = 30, message = "Length of search string should be less than 30")
    private String searchPattern = "";
    
    
    public UpdateAsimbaSPRequestorAction() {
        
    }
    
    @Create
    public void init() {
        //TODO: fill spList
        //TODO: rename to spPoolList
        spList.add(new SelectItem("Pool_1", "Pool_1"));
        
//        RequestorEntry entry = new RequestorEntry();
//        entry.setId("Requestor_1");
//        entry.setFriendlyName("Requestor 1");
//        entry.setLastModified(new Date());
//        spRequestorList.add(entry);

        //list loading
        spRequestorList = asimbaService.loadRequestors();
    }
    
    @Restrict("#{s:hasPermission('trust', 'access')}")
    public String add() {
        log.info("save new Requestor", spRequestor);
        asimbaService.addRequestorEntry(spRequestor);
        return OxTrustConstants.RESULT_SUCCESS;
    }
    
    @Restrict("#{s:hasPermission('trust', 'access')}")
    public String update() {
        log.info("update() Requestor", spRequestor);
        asimbaService.updateRequestorEntry(spRequestor);
        return OxTrustConstants.RESULT_SUCCESS;
    }
    
    @Restrict("#{s:hasPermission('trust', 'access')}")
    public void cancel() {
        log.info("cancel() Requestor", spRequestor);
    }

    @Restrict("#{s:hasPermission('trust', 'access')}")
    public String save() {
        log.info("save Requestor", spRequestor);
        synchronized (svnSyncTimer) {

        }
        return OxTrustConstants.RESULT_SUCCESS;
    }
    
    @Restrict("#{s:hasPermission('person', 'access')}")
    public String delete() {
        log.info("delete() Requestor", spRequestor);
        synchronized (svnSyncTimer) {

        }
        return OxTrustConstants.RESULT_SUCCESS;
    }
    
    @Restrict("#{s:hasPermission('trust', 'access')}")
    public String uploadFile() {
        log.info("uploadFile() Requestor", spRequestor);
        synchronized (svnSyncTimer) {

        }
        return OxTrustConstants.RESULT_SUCCESS;
    }
    
    @Restrict("#{s:hasPermission('person', 'access')}")
    public String search() {
        log.info("search() Requestor searchPattern:", searchPattern);
        synchronized (svnSyncTimer) {
            if (searchPattern != null && !"".equals(searchPattern)){
                try {
                    spRequestorList = asimbaService.searchRequestors(searchPattern, 0);
                } catch (Exception ex) {
                    log.error("LDAP search exception", ex);
                }
            } else {
                //list loading
                spRequestorList = asimbaService.loadRequestors();
            }
        }
        return OxTrustConstants.RESULT_SUCCESS;
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
     * @return the spRequestorList
     */
    public List<RequestorEntry> getSpRequestorList() {
        return spRequestorList;
    }

    /**
     * @param spRequestorList the spRequestorList to set
     */
    public void setSpRequestorList(List<RequestorEntry> spRequestorList) {
        this.spRequestorList = spRequestorList;
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
     * @return the spList
     */
    public ArrayList<SelectItem> getSpList() {
        return spList;
    }

    /**
     * @param spList the spList to set
     */
    public void setSpList(ArrayList<SelectItem> spList) {
        this.spList = spList;
    }

    /**
     * @return the spRequestorAdditionalProperties
     */
    public String getSpRequestorAdditionalProperties() {
        return spRequestorAdditionalProperties;
    }
    
    public Properties getSpRequestorAdditionalPropertiesAsProperties() throws IOException {
        Properties result = new Properties();
        result.load(new StringReader(spRequestorAdditionalProperties));
        return result;
    }

    /**
     * @param spRequestorAdditionalProperties the spRequestorAdditionalProperties to set
     */
    public void setSpRequestorAdditionalProperties(String spRequestorAdditionalProperties) {
        this.spRequestorAdditionalProperties = spRequestorAdditionalProperties;
    }
    
    public void setSpRequestorAdditionalProperties(Properties additionalProperties) {
        StringWriter writer = new StringWriter();
        for (String property : additionalProperties.stringPropertyNames()) {
            writer.write(property);
            writer.write("=");
            writer.write(additionalProperties.getProperty(property));
            writer.write("\n");
        }
        this.spRequestorAdditionalProperties = writer.toString();
    }
}
