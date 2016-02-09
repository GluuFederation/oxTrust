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
import org.gluu.asimba.util.ldap.sp.LDAPRequestorPoolEntry;
import org.gluu.asimba.util.ldap.sp.RequestorPoolEntry;
import org.gluu.oxtrust.ldap.service.AsimbaService;
import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.ldap.service.ClientService;
import org.gluu.oxtrust.ldap.service.SvnSyncTimer;
import org.gluu.oxtrust.ldap.service.TemplateService;
import org.gluu.oxtrust.ldap.service.TrustService;
import org.gluu.oxtrust.model.GluuSAMLTrustRelationship;
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
import org.xdi.model.GluuAttribute;
import org.xdi.model.GluuUserRole;


/**
 * Action class for updating and adding the SAML SP to Asimba
 * 
 * @author Dmitry Ognyannikov
 */
@Scope(ScopeType.CONVERSATION)
@Name("updateAsimbaSPAction")
@Restrict("#{identity.loggedIn}")
public class UpdateAsimbaSPAction implements Serializable {
    
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
    
    private RequestorPoolEntry sp = new RequestorPoolEntry();
    
    private String spAdditionalProperties = "";
    
    private ArrayList<RequestorPoolEntry> spList = new ArrayList<RequestorPoolEntry>();
    
    @NotNull
    @Size(min = 0, max = 30, message = "Length of search string should be less than 30")
    private String searchPattern = "";
    
    public UpdateAsimbaSPAction() {
        //init();
    }
    
    @Create
    public void init() {
        RequestorPoolEntry entry = new RequestorPoolEntry();
        entry.setId("RequestorPool_1");
        entry.setFriendlyName("RequestorPool 1");
        entry.setLastModified(new Date());
        spList.add(entry);
        //TODO: add list loading
    }
    
    private List<GluuAttribute> getAllAttributes() {
        List<GluuAttribute> attributes = attributeService.getAllPersonAttributes(GluuUserRole.ADMIN);
        return attributes;
    }

    private List<GluuAttribute> getAllActiveAttributes() {
        List<GluuAttribute> attributes = attributeService.getAllActivePersonAttributes(GluuUserRole.ADMIN);
        return attributes;
    }
    
    private void initAttributes(GluuSAMLTrustRelationship trust) {
            List<GluuAttribute> attributes = getAllActiveAttributes();
            List<String> origins = attributeService.getAllAttributeOrigins(attributes);
    }
        
    public ArrayList<SelectItem> getAllSPs() {
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
    
    @Restrict("#{s:hasPermission('person', 'access')}")
    public String search() {
        synchronized (svnSyncTimer) {

        }
        return OxTrustConstants.RESULT_SUCCESS;
    }

    /**
     * @return the sp
     */
    public RequestorPoolEntry getSp() {
        return sp;
    }

    /**
     * @param sp the sp to set
     */
    public void setSp(RequestorPoolEntry sp) {
        this.sp = sp;
    }

    /**
     * @return the spList
     */
    public ArrayList<RequestorPoolEntry> getSpList() {
        return spList;
    }

    /**
     * @param spList the spList to set
     */
    public void setSpList(ArrayList<RequestorPoolEntry> spList) {
        this.spList = spList;
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
    public String getSpAdditionalProperties() {
        return spAdditionalProperties;
    }
    
    public Properties getSpAdditionalPropertiesAsProperties() throws IOException {
        Properties result = new Properties();
        result.load(new StringReader(spAdditionalProperties));
        return result;
    }

    /**
     * @param spAdditionalProperties the spAdditionalProperties to set
     */
    public void setSpAdditionalProperties(String spAdditionalProperties) {
        this.spAdditionalProperties = spAdditionalProperties;
    }
    
    public void setSpAdditionalProperties(Properties additionalProperties) {
        StringWriter writer = new StringWriter();
        for (String property : additionalProperties.stringPropertyNames()) {
            writer.write(property);
            writer.write("=");
            writer.write(additionalProperties.getProperty(property));
            writer.write("\n");
        }
        this.spAdditionalProperties = writer.toString();
    }
}
