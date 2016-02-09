/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2016, Gluu
 */
package org.gluu.oxtrust.ldap.service;

import java.util.List;
import org.gluu.asimba.util.ldap.LDAPUtility;
import org.gluu.asimba.util.ldap.idp.IDPEntry;
import org.gluu.asimba.util.ldap.idp.LdapIDPEntry;
import org.gluu.asimba.util.ldap.selector.ApplicationSelectorEntry;
import org.gluu.asimba.util.ldap.selector.LDAPApplicationSelectorEntry;
import org.gluu.asimba.util.ldap.sp.LDAPRequestorEntry;
import org.gluu.asimba.util.ldap.sp.LDAPRequestorPoolEntry;
import org.gluu.asimba.util.ldap.sp.RequestorEntry;
import org.gluu.asimba.util.ldap.sp.RequestorPoolEntry;
import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

/**
 * Asimba LDAP configuration service.
 * 
 * @author Dmitry Ognyannikov, 2016
 */
@Name("asimbaService")
@AutoCreate
@Scope(ScopeType.APPLICATION)
public class AsimbaService {
    @Logger
    private Log log;
    
    private LdapEntryManager ldapEntryManager;
    
//    @In
//    ApplianceService applianceService;
     
    @Create
    public void init() {
        try {
            ldapEntryManager = LDAPUtility.getLDAPEntryManager();
        } catch (Exception e) {
            log.error("AsimbaService Init exception", e);
        }
    }
    
    @Destroy
    public void destroy() {
        
    }
    
//    public LdapConfigurationEntry loadAsimbaConfiguration() {
//        String dn = applianceService.getDnForAppliance();
//        
//        LdapConfigurationEntry ldapConfiguration = ldapEntryManager.find(LdapConfigurationEntry.class, applianceService.getDnForAppliance(applianceService.getApplianceInum()), null);
//        
//        return ldapConfiguration;
//    }
    
    public List<IDPEntry> loadIDPs() {
        return ldapEntryManager.findEntries(LdapIDPEntry.class);
    }
    
    public List<RequestorPoolEntry> loadRequestorPools() {
        return ldapEntryManager.findEntries(new RequestorPoolEntry());
    }
    
    public List<RequestorEntry> loadRequestors() {
        return ldapEntryManager.findEntries(new RequestorEntry());
    }
    
    public List<ApplicationSelectorEntry> loadSelectors() {
        return ldapEntryManager.findEntries(new ApplicationSelectorEntry());
    }
    
    
    /**
    * Add new IDPEntry
    * 
    * @param entry IDPEntry
    */
    public void addIDPEntry(IDPEntry entry) {
        ldapEntryManager.persist(entry);
    }

    /**
    * Update IDPEntry
    * 
    * @param entry IDPEntry
    */
    public void updateIDPEntry(IDPEntry entry) {
        ldapEntryManager.merge(entry);
    }

    /**
    * Check if LDAP server contains IDPEntry with specified attributes
    * 
    * @param entry IDPEntry
    * @return True if entry with specified attributes exist
    */
    public boolean containsIDPEntry(IDPEntry entry) {
        return ldapEntryManager.contains(entry);
    }
    
    
    /**
    * Add new LDAPRequestorPoolEntry
    * 
    * @param entry LDAPRequestorPoolEntry
    */
    public void addRequestorPoolEntry(LDAPRequestorPoolEntry entry) {
        ldapEntryManager.persist(entry);
    }

    /**
    * Update LDAPRequestorPoolEntry
    * 
    * @param entry LDAPRequestorPoolEntry
    */
    public void updateRequestorPoolEntry(LDAPRequestorPoolEntry entry) {
        ldapEntryManager.merge(entry);
    }

    /**
    * Check if LDAP server contains LDAPRequestorPoolEntry with specified attributes
    * 
    * @param entry LDAPRequestorPoolEntry
    * @return True if entry with specified attributes exist
    */
    public boolean containsRequestorPoolEntry(LDAPRequestorPoolEntry entry) {
        return ldapEntryManager.contains(entry);
    }
    
    
    /**
    * Add new LDAPRequestorEntry
    * 
    * @param entry LDAPRequestorEntry
    */
    public void addRequestorEntry(LDAPRequestorEntry entry) {
        ldapEntryManager.persist(entry);
    }

    /**
    * Update LDAPRequestorEntry
    * 
    * @param entry LDAPRequestorEntry
    */
    public void updateRequestorEntry(LDAPRequestorEntry entry) {
        ldapEntryManager.merge(entry);
    }

    /**
    * Check if LDAP server contains LDAPRequestorEntry with specified attributes
    * 
    * @param entry LDAPRequestorEntry
    * @return True if entry with specified attributes exist
    */
    public boolean containsRequestorEntry(LDAPRequestorEntry entry) {
        return ldapEntryManager.contains(entry);
    }
    
    
    /**
    * Add new LDAPApplicationSelectorEntry
    * 
    * @param entry LDAPApplicationSelectorEntry
    */
    public void addRequestorEntry(LDAPApplicationSelectorEntry entry) {
        ldapEntryManager.persist(entry);
    }

    /**
    * Update LDAPApplicationSelectorEntry
    * 
    * @param entry LDAPApplicationSelectorEntry
    */
    public void updateRequestorEntry(LDAPApplicationSelectorEntry entry) {
        ldapEntryManager.merge(entry);
    }

    /**
    * Check if LDAP server contains LDAPApplicationSelectorEntry with specified attributes
    * 
    * @param entry LDAPApplicationSelectorEntry
    * @return True if entry with specified attributes exist
    */
    public boolean containsRequestorEntry(LDAPApplicationSelectorEntry entry) {
        return ldapEntryManager.contains(entry);
    }
}
