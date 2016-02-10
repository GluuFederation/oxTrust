/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2016, Gluu
 */
package org.gluu.oxtrust.ldap.service;

import java.util.ArrayList;
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
        List<IDPEntry> result = new ArrayList<IDPEntry>();
        List<LdapIDPEntry> entries = ldapEntryManager.findEntries(LdapIDPEntry.class);
        for (LdapIDPEntry entry : entries) {
            result.add(entry.getEntry());
        }
        return result;
    }
    
    public List<RequestorPoolEntry> loadRequestorPools() {
        List<RequestorPoolEntry> result = new ArrayList<RequestorPoolEntry>();
        List<LDAPRequestorPoolEntry> entries = ldapEntryManager.findEntries(LDAPRequestorPoolEntry.class);
        for (LDAPRequestorPoolEntry entry : entries) {
            result.add(entry.getEntry());
        }
        return result;
    }
    
    public List<RequestorEntry> loadRequestors() {
        List<RequestorEntry> result = new ArrayList<RequestorEntry>();
        List<LDAPRequestorEntry> entries = ldapEntryManager.findEntries(LDAPRequestorEntry.class);
        for (LDAPRequestorEntry entry : entries) {
            result.add(entry.getEntry());
        }
        return result;
    }
    
    public List<ApplicationSelectorEntry> loadSelectors() {
        List<ApplicationSelectorEntry> result = new ArrayList<ApplicationSelectorEntry>();
        List<LDAPApplicationSelectorEntry> entries = ldapEntryManager.findEntries(LDAPApplicationSelectorEntry.class);
        for (LDAPApplicationSelectorEntry entry : entries) {
            result.add(entry.getEntry());
        }
        return result;
    }
    
    
    /**
    * Add new IDPEntry.
    * 
    * @param entry IDPEntry
    */
    public void addIDPEntry(IDPEntry entry) {
        LdapIDPEntry ldapEntry = new LdapIDPEntry();
        ldapEntry.setEntry(entry);
        ldapEntryManager.persist(ldapEntry);
    }

    /**
    * Update IDPEntry.
    * 
    * @param entry IDPEntry
    */
    public void updateIDPEntry(IDPEntry entry) {
        LdapIDPEntry ldapEntry = new LdapIDPEntry();
        ldapEntry.setEntry(entry);
        ldapEntryManager.merge(ldapEntry);
    }

    /**
    * Remove IDPEntry.
    * 
    * @param entry IDPEntry
    */
    public void removeIDPEntry(IDPEntry entry) {
        LdapIDPEntry ldapEntry = new LdapIDPEntry();
        ldapEntry.setEntry(entry);
        ldapEntryManager.remove(ldapEntry);
    }

    /**
    * Check if LDAP server contains IDPEntry with specified attributes.
    * 
    * @param entry IDPEntry
    * @return True if entry with specified attributes exist
    */
    public boolean containsIDPEntry(IDPEntry entry) {
        LdapIDPEntry ldapEntry = new LdapIDPEntry();
        ldapEntry.setEntry(entry);
        return ldapEntryManager.contains(ldapEntry);
    }
    
    
    /**
    * Add new LDAPRequestorPoolEntry.
    * 
    * @param entry LDAPRequestorPoolEntry
    */
    public void addRequestorPoolEntry(RequestorPoolEntry entry) {
        LDAPRequestorPoolEntry ldapEntry = new LDAPRequestorPoolEntry();
        ldapEntry.setEntry(entry);
        ldapEntryManager.persist(ldapEntry);
    }

    /**
    * Update LDAPRequestorPoolEntry.
    * 
    * @param entry LDAPRequestorPoolEntry
    */
    public void updateRequestorPoolEntry(RequestorPoolEntry entry) {
        LDAPRequestorPoolEntry ldapEntry = new LDAPRequestorPoolEntry();
        ldapEntry.setEntry(entry);
        ldapEntryManager.merge(ldapEntry);
    }

    /**
    * Remove LDAPRequestorPoolEntry.
    * 
    * @param entry LDAPRequestorPoolEntry
    */
    public void removeRequestorPoolEntry(RequestorPoolEntry entry) {
        LDAPRequestorPoolEntry ldapEntry = new LDAPRequestorPoolEntry();
        ldapEntry.setEntry(entry);
        ldapEntryManager.remove(ldapEntry);
    }

    /**
    * Check if LDAP server contains LDAPRequestorPoolEntry with specified attributes.
    * 
    * @param entry LDAPRequestorPoolEntry
    * @return True if entry with specified attributes exist
    */
    public boolean containsRequestorPoolEntry(RequestorPoolEntry entry) {
        LDAPRequestorPoolEntry ldapEntry = new LDAPRequestorPoolEntry();
        ldapEntry.setEntry(entry);
        return ldapEntryManager.contains(ldapEntry);
    }
    
    
    /**
    * Add new LDAPRequestorEntry.
    * 
    * @param entry LDAPRequestorEntry
    */
    public void addRequestorEntry(RequestorEntry entry) {
        LDAPRequestorEntry ldapEntry = new LDAPRequestorEntry();
        ldapEntry.setEntry(entry);
        ldapEntryManager.persist(ldapEntry);
    }

    /**
    * Update LDAPRequestorEntry.
    * 
    * @param entry LDAPRequestorEntry
    */
    public void updateRequestorEntry(RequestorEntry entry) {
        LDAPRequestorEntry ldapEntry = new LDAPRequestorEntry();
        ldapEntry.setEntry(entry);
        ldapEntryManager.merge(entry);
    }

    /**
    * Remove LDAPRequestorEntry.
    * 
    * @param entry LDAPRequestorEntry
    */
    public void removeRequestorEntry(RequestorEntry entry) {
        LDAPRequestorEntry ldapEntry = new LDAPRequestorEntry();
        ldapEntry.setEntry(entry);
        ldapEntryManager.remove(entry);
    }

    /**
    * Check if LDAP server contains LDAPRequestorEntry with specified attributes.
    * 
    * @param entry LDAPRequestorEntry
    * @return True if entry with specified attributes exist
    */
    public boolean containsRequestorEntry(RequestorEntry entry) {
        LDAPRequestorEntry ldapEntry = new LDAPRequestorEntry();
        ldapEntry.setEntry(entry);
        return ldapEntryManager.contains(entry);
    }
    
    
    /**
    * Add new LDAPApplicationSelectorEntry.
    * 
    * @param entry LDAPApplicationSelectorEntry
    */
    public void addRequestorEntry(ApplicationSelectorEntry entry) {
        LDAPApplicationSelectorEntry ldapEntry = new LDAPApplicationSelectorEntry();
        ldapEntry.setEntry(entry);
        ldapEntryManager.persist(ldapEntry);
    }

    /**
    * Update LDAPApplicationSelectorEntry.
    * 
    * @param entry LDAPApplicationSelectorEntry
    */
    public void updateRequestorEntry(ApplicationSelectorEntry entry) {
        LDAPApplicationSelectorEntry ldapEntry = new LDAPApplicationSelectorEntry();
        ldapEntry.setEntry(entry);
        ldapEntryManager.merge(ldapEntry);
    }

    /**
    * Remove LDAPApplicationSelectorEntry.
    * 
    * @param entry LDAPApplicationSelectorEntry
    */
    public void removeRequestorEntry(ApplicationSelectorEntry entry) {
        LDAPApplicationSelectorEntry ldapEntry = new LDAPApplicationSelectorEntry();
        ldapEntry.setEntry(entry);
        ldapEntryManager.remove(ldapEntry);
    }

    /**
    * Check if LDAP server contains LDAPApplicationSelectorEntry with specified attributes.
    * 
    * @param entry LDAPApplicationSelectorEntry
    * @return True if entry with specified attributes exist
    */
    public boolean containsRequestorEntry(ApplicationSelectorEntry entry) {
        LDAPApplicationSelectorEntry ldapEntry = new LDAPApplicationSelectorEntry();
        ldapEntry.setEntry(entry);
        return ldapEntryManager.contains(ldapEntry);
    }
}
