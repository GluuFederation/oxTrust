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
import java.util.Properties;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.gluu.asimba.util.ldap.sp.LDAPRequestorEntry;
import org.gluu.asimba.util.ldap.sp.RequestorEntry;
import org.gluu.oxtrust.ldap.service.AsimbaService;
import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.ldap.service.ClientService;
import org.gluu.oxtrust.ldap.service.SvnSyncTimer;
import org.gluu.oxtrust.ldap.service.TemplateService;
import org.gluu.oxtrust.ldap.service.TrustService;
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
    
    private ArrayList<RequestorEntry> spRequestorList = new ArrayList<RequestorEntry>();
    
    private ArrayList<SelectItem> spList = new ArrayList<SelectItem>();
    
    @NotNull
    @Size(min = 0, max = 30, message = "Length of search string should be less than 30")
    private String searchPattern = "";
    
    
    public UpdateAsimbaSPRequestorAction() {
        
    }
    
    @Create
    public void init() {
        spList.add(new SelectItem("Pool_1", "Pool_1"));
        
        RequestorEntry entry = new RequestorEntry();
        entry.setId("Requestor_1");
        entry.setFriendlyName("Requestor 1");
        entry.setLastModified(new Date());
        spRequestorList.add(entry);
        //TODO: add list loading
    }
        
    public ArrayList<SelectItem> getAllSPRequestors() {
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
    
    @Restrict("#{s:hasPermission('person', 'access')}")
    public String delete() {
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
    public String search() {
        synchronized (svnSyncTimer) {

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
    public ArrayList<RequestorEntry> getSpRequestorList() {
        return spRequestorList;
    }

    /**
     * @param spRequestorList the spRequestorList to set
     */
    public void setSpRequestorList(ArrayList<RequestorEntry> spRequestorList) {
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
