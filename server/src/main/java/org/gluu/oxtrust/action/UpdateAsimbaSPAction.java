/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.ArrayList;
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
    
    private String sp;
    
    
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
    
    @Restrict("#{s:hasPermission('person', 'access')}")
    public String delete() {
        synchronized (svnSyncTimer) {

        }
        return OxTrustConstants.RESULT_SUCCESS;
    }

    /**
     * @return the sp
     */
    public String getSp() {
        return sp;
    }

    /**
     * @param sp the sp to set
     */
    public void setSp(String sp) {
        this.sp = sp;
    }
}
