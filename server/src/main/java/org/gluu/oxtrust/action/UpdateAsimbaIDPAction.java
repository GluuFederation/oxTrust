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
import javax.faces.model.SelectItem;
import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.ldap.service.ClientService;
import org.gluu.oxtrust.ldap.service.SvnSyncTimer;
import org.gluu.oxtrust.ldap.service.TemplateService;
import org.gluu.oxtrust.ldap.service.TrustService;
import org.gluu.oxtrust.model.GluuSAMLTrustRelationship;
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
import org.xdi.ldap.model.GluuStatus;
import org.xdi.model.GluuAttribute;
import org.xdi.model.GluuUserRole;
import org.xdi.model.SchemaEntry;
import org.xdi.service.SchemaService;
import org.xdi.util.StringHelper;
import org.xdi.util.io.FileUploadWrapper;
import org.xdi.util.io.ResponseHelper;
import org.gluu.asimba.util.ldap.idp.IDPEntry;


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
    
    private IDPEntry idp = new IDPEntry();
    
    private ArrayList<IDPEntry> idpList = new ArrayList<IDPEntry>();
    
    private String searchPattern = "";
    
    public UpdateAsimbaIDPAction() {
        init();
    }
    
    public void init() {
        IDPEntry entry = new IDPEntry();
        entry.setId("IDP_1");
        entry.setFriendlyName("IDP 1");
        entry.setLastModified(new Date());
        idpList.add(entry);
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
