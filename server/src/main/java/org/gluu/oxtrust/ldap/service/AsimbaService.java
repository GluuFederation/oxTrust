/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2016, Gluu
 */
package org.gluu.oxtrust.ldap.service;

import java.util.List;
import org.gluu.asimba.util.ldap.LDAPUtility;
import org.gluu.asimba.util.ldap.LdapConfigurationEntry;
import org.gluu.asimba.util.ldap.idp.IDPEntry;
import org.gluu.asimba.util.ldap.idp.LdapIDPEntry;
import org.gluu.asimba.util.ldap.selector.ApplicationSelectorLDAPEntry;
import org.gluu.asimba.util.ldap.sp.RequestorEntry;
import org.gluu.asimba.util.ldap.sp.RequestorPoolEntry;
import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
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
    
//    @In
//    private LdapEntryManager ldapEntryManager;
    
//    @In
//    ApplianceService applianceService;
    
    @Observer("org.jboss.seam.postInitialization")
    public void init() {
//        try {
//            ldapEntryManager = LDAPUtility.getLDAPEntryManager();
//        } catch (Exception e) {
//            log.error("AsimbaService Init exception", e);
//        }
    }
    
//    public LdapConfigurationEntry loadAsimbaConfiguration() {
//        String dn = applianceService.getDnForAppliance();
//        
//        LdapConfigurationEntry ldapConfiguration = ldapEntryManager.find(LdapConfigurationEntry.class, applianceService.getDnForAppliance(applianceService.getApplianceInum()), null);
//        
//        return ldapConfiguration;
//    }
    
//    public List<IDPEntry> loadIDPs() {
//        return ldapEntryManager.findEntries(LdapIDPEntry.class);
//    }
//    
//    public List<RequestorPoolEntry> loadRequestorPools() {
//        return ldapEntryManager.findEntries(new RequestorPoolEntry());
//    }
//    
//    public List<RequestorEntry> loadRequestors() {
//        return ldapEntryManager.findEntries(new RequestorEntry());
//    }
//    
//    public List<ApplicationSelectorLDAPEntry> loadSelectors() {
//        return ldapEntryManager.findEntries(new ApplicationSelectorLDAPEntry());
//    }
//    
//    
//    /**
//    * Add new IDPEntry
//    * 
//    * @param entry IDPEntry
//    */
//    public void addIDPEntry(IDPEntry entry) {
//        ldapEntryManager.persist(entry);
//    }
//
//    /**
//    * Update IDPEntry
//    * 
//    * @param entry IDPEntry
//    */
//    public void updateIDPEntry(IDPEntry entry) {
//        ldapEntryManager.merge(entry);
//    }
//
//    /**
//    * Check if LDAP server contains IDPEntry with specified attributes
//    * 
//    * @param entry IDPEntry
//    * @return True if entry with specified attributes exist
//    */
//    public boolean containsIDPEntry(IDPEntry entry) {
//        return ldapEntryManager.contains(entry);
//    }
//    
//    
//    /**
//    * Add new RequestorPoolEntry
//    * 
//    * @param entry RequestorPoolEntry
//    */
//    public void addRequestorPoolEntry(RequestorPoolEntry entry) {
//        ldapEntryManager.persist(entry);
//    }
//
//    /**
//    * Update RequestorPoolEntry
//    * 
//    * @param entry RequestorPoolEntry
//    */
//    public void updateRequestorPoolEntry(RequestorPoolEntry entry) {
//        ldapEntryManager.merge(entry);
//    }
//
//    /**
//    * Check if LDAP server contains RequestorPoolEntry with specified attributes
//    * 
//    * @param entry RequestorPoolEntry
//    * @return True if entry with specified attributes exist
//    */
//    public boolean containsRequestorPoolEntry(RequestorPoolEntry entry) {
//        return ldapEntryManager.contains(entry);
//    }
//    
//    
//    /**
//    * Add new RequestorEntry
//    * 
//    * @param entry RequestorEntry
//    */
//    public void addRequestorEntry(RequestorEntry entry) {
//        ldapEntryManager.persist(entry);
//    }
//
//    /**
//    * Update RequestorEntry
//    * 
//    * @param entry RequestorEntry
//    */
//    public void updateRequestorEntry(RequestorEntry entry) {
//        ldapEntryManager.merge(entry);
//    }
//
//    /**
//    * Check if LDAP server contains RequestorEntry with specified attributes
//    * 
//    * @param entry RequestorEntry
//    * @return True if entry with specified attributes exist
//    */
//    public boolean containsRequestorEntry(RequestorEntry entry) {
//        return ldapEntryManager.contains(entry);
//    }
//    
//    
//    /**
//    * Add new ApplicationSelectorLDAPEntry
//    * 
//    * @param entry ApplicationSelectorLDAPEntry
//    */
//    public void addRequestorEntry(ApplicationSelectorLDAPEntry entry) {
//        ldapEntryManager.persist(entry);
//    }
//
//    /**
//    * Update ApplicationSelectorLDAPEntry
//    * 
//    * @param entry ApplicationSelectorLDAPEntry
//    */
//    public void updateRequestorEntry(ApplicationSelectorLDAPEntry entry) {
//        ldapEntryManager.merge(entry);
//    }
//
//    /**
//    * Check if LDAP server contains ApplicationSelectorLDAPEntry with specified attributes
//    * 
//    * @param entry ApplicationSelectorLDAPEntry
//    * @return True if entry with specified attributes exist
//    */
//    public boolean containsRequestorEntry(ApplicationSelectorLDAPEntry entry) {
//        return ldapEntryManager.contains(entry);
//    }
}
