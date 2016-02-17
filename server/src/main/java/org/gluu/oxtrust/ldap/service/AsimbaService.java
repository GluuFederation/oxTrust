/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2016, Gluu
 */
package org.gluu.oxtrust.ldap.service;

import com.unboundid.ldap.sdk.Filter;
import java.util.ArrayList;
import java.util.List;
import org.gluu.asimba.util.ldap.LdapConfigurationEntry;
import org.gluu.asimba.util.ldap.idp.IDPEntry;
import org.gluu.asimba.util.ldap.idp.LdapIDPEntry;
import org.gluu.asimba.util.ldap.selector.ApplicationSelectorEntry;
import org.gluu.asimba.util.ldap.selector.LDAPApplicationSelectorEntry;
import org.gluu.asimba.util.ldap.sp.LDAPRequestorEntry;
import org.gluu.asimba.util.ldap.sp.LDAPRequestorPoolEntry;
import org.gluu.asimba.util.ldap.sp.RequestorEntry;
import org.gluu.asimba.util.ldap.sp.RequestorPoolEntry;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.xdi.util.INumGenerator;
import org.xdi.util.StringHelper;

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
    
    @In
    private LdapEntryManager ldapEntryManager;
    
    @In
    ApplianceService applianceService;
     
    @Create
    public void init() {
    }
    
    @Destroy
    public void destroy() {        
    }
    
    public LdapConfigurationEntry loadAsimbaConfiguration() {
        String applianceDn = applianceService.getDnForAppliance();
        LdapConfigurationEntry ldapConfiguration = ldapEntryManager.find(LdapConfigurationEntry.class, "ou=oxasimba,ou=configuration,"+applianceDn, null);
        
        return ldapConfiguration;
    }
    
    public List<IDPEntry> loadIDPs() {
        List<LdapIDPEntry> entries = ldapEntryManager.findEntries(getDnForLdapIDPEntry(null), LdapIDPEntry.class, null);
        List<IDPEntry> result = new ArrayList<IDPEntry>();
        for (LdapIDPEntry entry : entries) {
            result.add(entry.getEntry());
        }
        return result;
    }
    
    public List<RequestorPoolEntry> loadRequestorPools() {
        List<LDAPRequestorPoolEntry> entries = ldapEntryManager.findEntries(getDnForLDAPRequestorPoolEntry(null), 
                LDAPRequestorPoolEntry.class, null);
        List<RequestorPoolEntry> result = new ArrayList<RequestorPoolEntry>();
        for (LDAPRequestorPoolEntry entry : entries) {
            result.add(entry.getEntry());
        }
        return result;
    }
    
    public List<RequestorEntry> loadRequestors() {
        List<LDAPRequestorEntry> entries = ldapEntryManager.findEntries(getDnForLDAPRequestorEntry(null),
                LDAPRequestorEntry.class, null);
        List<RequestorEntry> result = new ArrayList<RequestorEntry>();
        for (LDAPRequestorEntry entry : entries) {
            result.add(entry.getEntry());
        }
        return result;
    }
    
    public List<ApplicationSelectorEntry> loadSelectors() {
        List<LDAPApplicationSelectorEntry> entries = ldapEntryManager.findEntries(getDnForLDAPApplicationSelectorEntry(null),
                LDAPApplicationSelectorEntry.class, null);
        List<ApplicationSelectorEntry> result = new ArrayList<ApplicationSelectorEntry>();
        for (LDAPApplicationSelectorEntry entry : entries) {
            result.add(entry.getEntry());
        }
        return result;
    }
    
    /**
    * Search by pattern
    * 
    * @param pattern Pattern
    * @param sizeLimit Maximum count of results
    * @return List of scopes
    * @throws Exception
    */
    public List<IDPEntry> searchIDPs(String pattern, int sizeLimit) throws Exception {
        // filter
        String[] targetArray = new String[] { pattern };
        Filter idFilter = Filter.createSubstringFilter(OxTrustConstants.uniqueIdentifier, null, targetArray, null);
        Filter friendlyNameFilter = Filter.createSubstringFilter(OxTrustConstants.friendlyName, null, targetArray, null);
        Filter descriptionFilter = Filter.createSubstringFilter(OxTrustConstants.description, null, targetArray, null);
        Filter inameFilter = Filter.createSubstringFilter(OxTrustConstants.iname, null, targetArray, null);
        Filter identificationURLFilter = Filter.createSubstringFilter(OxTrustConstants.identificationURL, null, targetArray, null);
        Filter searchFilter = Filter.createORFilter(idFilter, friendlyNameFilter, descriptionFilter, inameFilter, identificationURLFilter);

        // search
        final List<LdapIDPEntry> entries = ldapEntryManager.findEntries(getDnForLdapIDPEntry(null), LdapIDPEntry.class, searchFilter, sizeLimit);

        // convert result
        List<IDPEntry> ret = new ArrayList<IDPEntry>();
        for (LdapIDPEntry entry : entries) {
            ret.add(entry.getEntry());
        }
        return ret;
    }
    
    /**
    * Search by pattern
    * 
    * @param pattern Pattern
    * @param sizeLimit Maximum count of results
    * @return List of scopes
    * @throws Exception
    */
    public List<ApplicationSelectorEntry> searchSelectors(String pattern, int sizeLimit) throws Exception {
        // filter
        String[] targetArray = new String[] { pattern };
        Filter idFilter = Filter.createSubstringFilter(OxTrustConstants.uniqueIdentifier, null, targetArray, null);
        Filter friendlyNameFilter = Filter.createSubstringFilter(OxTrustConstants.friendlyName, null, targetArray, null);
        Filter descriptionFilter = Filter.createSubstringFilter(OxTrustConstants.description, null, targetArray, null);
        Filter inameFilter = Filter.createSubstringFilter(OxTrustConstants.iname, null, targetArray, null);
        Filter organizationIdFilter = Filter.createSubstringFilter(OxTrustConstants.organizationId, null, targetArray, null);
        Filter searchFilter = Filter.createORFilter(idFilter, friendlyNameFilter, descriptionFilter, inameFilter, organizationIdFilter);

        // search
        List<LDAPApplicationSelectorEntry> entries = ldapEntryManager.findEntries(getDnForLDAPApplicationSelectorEntry(null), LDAPApplicationSelectorEntry.class, searchFilter, sizeLimit);

        // convert result
        List<ApplicationSelectorEntry> ret = new ArrayList<ApplicationSelectorEntry>();
        for (LDAPApplicationSelectorEntry entry : entries) {
            ret.add(entry.getEntry());
        }
        return ret;
    }
    
    /**
    * Search by pattern
    * 
    * @param pattern Pattern
    * @param sizeLimit Maximum count of results
    * @return List of scopes
    * @throws Exception
    */
    public List<RequestorEntry> searchRequestors(String pattern, int sizeLimit) throws Exception {
        // filter
        String[] targetArray = new String[] { pattern };
        Filter idFilter = Filter.createSubstringFilter(OxTrustConstants.uniqueIdentifier, null, targetArray, null);
        Filter friendlyNameFilter = Filter.createSubstringFilter(OxTrustConstants.friendlyName, null, targetArray, null);
        Filter descriptionFilter = Filter.createSubstringFilter(OxTrustConstants.description, null, targetArray, null);
        Filter inameFilter = Filter.createSubstringFilter(OxTrustConstants.iname, null, targetArray, null);
        Filter searchFilter = Filter.createORFilter(idFilter, friendlyNameFilter, descriptionFilter, inameFilter);

        // search
        List<LDAPRequestorEntry> entries = ldapEntryManager.findEntries(getDnForLDAPRequestorEntry(null), LDAPRequestorEntry.class, searchFilter, sizeLimit);

        // convert result
        List<RequestorEntry> ret = new ArrayList<RequestorEntry>();
        for (LDAPRequestorEntry entry : entries) {
            ret.add(entry.getEntry());
        }
        return ret;
    }
    
    /**
    * Search by pattern
    * 
    * @param pattern Pattern
    * @param sizeLimit Maximum count of results
    * @return List of scopes
    * @throws Exception
    */
    public List<RequestorPoolEntry> searchRequestorPools(String pattern, int sizeLimit) throws Exception {
        // filter
        String[] targetArray = new String[] { pattern };
        Filter idFilter = Filter.createSubstringFilter(OxTrustConstants.uniqueIdentifier, null, targetArray, null);
        Filter friendlyNameFilter = Filter.createSubstringFilter(OxTrustConstants.friendlyName, null, targetArray, null);
        Filter descriptionFilter = Filter.createSubstringFilter(OxTrustConstants.description, null, targetArray, null);
        Filter inameFilter = Filter.createSubstringFilter(OxTrustConstants.iname, null, targetArray, null);
        Filter searchFilter = Filter.createORFilter(idFilter, friendlyNameFilter, descriptionFilter, inameFilter);

        // search
        List<LDAPRequestorPoolEntry> entries = ldapEntryManager.findEntries(getDnForLDAPRequestorPoolEntry(null), LDAPRequestorPoolEntry.class, searchFilter, sizeLimit);

        // convert result
        List<RequestorPoolEntry> ret = new ArrayList<RequestorPoolEntry>();
        for (LDAPRequestorPoolEntry entry : entries) {
            ret.add(entry.getEntry());
        }
        return ret;
    }
    
    /**
    * Build DN string for LdapIDPEntry
    * 
    * @param inum entry Inum
    * @return DN string for specified entry or DN for entry branch if inum is null
    * @throws Exception
    */
    public String getDnForLdapIDPEntry(String inum) {
        String applianceDn = applianceService.getDnForAppliance();
        if (StringHelper.isEmpty(inum)) {
                return String.format("ou=idps,ou=oxasimba,ou=configuration,%s", applianceDn);
        }
        return String.format("inum=%s,ou=idps,ou=oxasimba,ou=configuration,%s", inum, applianceDn);
    }
    
    /**
    * Build DN string for LDAPApplicationSelectorEntry
    * 
    * @param inum entry Inum
    * @return DN string for specified entry or DN for entry branch if inum is null
    * @throws Exception
    */
    public String getDnForLDAPApplicationSelectorEntry(String inum) {
        String applianceDn = applianceService.getDnForAppliance();
        if (StringHelper.isEmpty(inum)) {
                return String.format("ou=selectors,ou=oxasimba,ou=configuration,%s", applianceDn);
        }
        return String.format("inum=%s,ou=selectors,ou=oxasimba,ou=configuration,%s", inum, applianceDn);
    }
    
    /**
    * Build DN string for LDAPRequestorEntry
    * 
    * @param inum entry Inum
    * @return DN string for specified entry or DN for entry branch if inum is null
    * @throws Exception
    */
    public String getDnForLDAPRequestorEntry(String inum) {
        String applianceDn = applianceService.getDnForAppliance();
        if (StringHelper.isEmpty(inum)) {
                return String.format("ou=requestors,ou=oxasimba,ou=configuration,%s", applianceDn);
        }
        return String.format("inum=%s,ou=requestors,ou=oxasimba,ou=configuration,%s", inum, applianceDn);
    }
    
    /**
    * Build DN string for LDAPRequestorPoolEntry
    * 
    * @param inum entry Inum
    * @return DN string for specified entry or DN for entry branch if inum is null
    * @throws Exception
    */
    public String getDnForLDAPRequestorPoolEntry(String inum) {
        String applianceDn = applianceService.getDnForAppliance();
        if (StringHelper.isEmpty(inum)) {
                return String.format("ou=requestorpools,ou=oxasimba,ou=configuration,%s", applianceDn);
        }
        return String.format("inum=%s,ou=requestorpools,ou=oxasimba,ou=configuration,%s", inum, applianceDn);
    }
    
    /**
    * Generate new inum for Scope
    * 
    * @return New inum for Scope
    * @throws Exception
    */
    private String generateInumImpl() {
        String orgInum = applianceService.getApplianceInum();
        return orgInum + OxTrustConstants.inumDelimiter + INumGenerator.generate(8);
    }
    
    /**
    * Add new IDPEntry.
    * 
    * @param entry IDPEntry
    */
    public void addIDPEntry(IDPEntry entry) {
        log.info("addIDPEntry() call");
        try {
            LdapIDPEntry ldapEntry = new LdapIDPEntry();
            ldapEntry.setEntry(entry);
            String inum = generateInumImpl();
            ldapEntry.setInum(inum);
            ldapEntry.setDn(getDnForLdapIDPEntry(inum));
            ldapEntryManager.persist(ldapEntry);
        } catch (Exception e) {
            log.error("addIDPEntry() exception", e);
        }
    }

    /**
    * Update IDPEntry.
    * 
    * @param entry IDPEntry
    */
    public void updateIDPEntry(IDPEntry entry) {
        LdapIDPEntry ldapEntry = new LdapIDPEntry();
        ldapEntry.setDn(getDnForLdapIDPEntry(entry.getInum()));
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
        ldapEntry.setDn(getDnForLdapIDPEntry(entry.getInum()));
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
        ldapEntry.setDn(getDnForLdapIDPEntry(entry.getInum()));
        ldapEntry.setEntry(entry);
        return ldapEntryManager.contains(ldapEntry);
    }

    /**
    * Read IDPEntry from LDAP.
    * 
    * @param inum Entry inum.
    * @return IDPEntry
    */
    public IDPEntry readIDPEntry(String inum) {
        LdapIDPEntry result = ldapEntryManager.find(LdapIDPEntry.class, getDnForLdapIDPEntry(inum));
        return result.getEntry();
    }
    
    
    /**
    * Add new LDAPRequestorPoolEntry.
    * 
    * @param entry LDAPRequestorPoolEntry
    */
    public void addRequestorPoolEntry(RequestorPoolEntry entry) {
        LDAPRequestorPoolEntry ldapEntry = new LDAPRequestorPoolEntry();
        ldapEntry.setEntry(entry);
        String inum = generateInumImpl();
        ldapEntry.setInum(inum);
        ldapEntry.setDn(getDnForLDAPRequestorPoolEntry(inum));
        ldapEntryManager.persist(ldapEntry);
    }

    /**
    * Update LDAPRequestorPoolEntry.
    * 
    * @param entry LDAPRequestorPoolEntry
    */
    public void updateRequestorPoolEntry(RequestorPoolEntry entry) {
        LDAPRequestorPoolEntry ldapEntry = new LDAPRequestorPoolEntry();
        ldapEntry.setDn(getDnForLDAPRequestorPoolEntry(entry.getInum()));
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
        ldapEntry.setDn(getDnForLDAPRequestorPoolEntry(entry.getInum()));
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
        ldapEntry.setDn(getDnForLDAPRequestorPoolEntry(entry.getInum()));
        ldapEntry.setEntry(entry);
        return ldapEntryManager.contains(ldapEntry);
    }

    /**
    * Read RequestorPoolEntry from LDAP.
    * 
    * @param inum Entry inum.
    * @return RequestorPoolEntry
    */
    public RequestorPoolEntry readRequestorPoolEntry(String inum) {
        LDAPRequestorPoolEntry result = ldapEntryManager.find(LDAPRequestorPoolEntry.class, getDnForLDAPRequestorPoolEntry(inum));
        return result.getEntry();
    }
    
    
    /**
    * Add new LDAPRequestorEntry.
    * 
    * @param entry LDAPRequestorEntry
    */
    public void addRequestorEntry(RequestorEntry entry) {
        LDAPRequestorEntry ldapEntry = new LDAPRequestorEntry();
        ldapEntry.setEntry(entry);
        String inum = generateInumImpl();
        ldapEntry.setInum(inum);
        ldapEntry.setDn(getDnForLDAPRequestorEntry(inum));
        ldapEntryManager.persist(ldapEntry);
    }

    /**
    * Update LDAPRequestorEntry.
    * 
    * @param entry LDAPRequestorEntry
    */
    public void updateRequestorEntry(RequestorEntry entry) {
        LDAPRequestorEntry ldapEntry = new LDAPRequestorEntry();
        ldapEntry.setDn(getDnForLDAPRequestorEntry(entry.getInum()));
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
        ldapEntry.setDn(getDnForLDAPRequestorEntry(entry.getInum()));
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
        ldapEntry.setDn(getDnForLDAPRequestorEntry(entry.getInum()));
        ldapEntry.setEntry(entry);
        return ldapEntryManager.contains(entry);
    }

    /**
    * Read RequestorEntry from LDAP.
    * 
    * @param inum Entry inum.
    * @return RequestorEntry
    */
    public RequestorEntry readRequestorEntry(String inum) {
        LDAPRequestorEntry result = ldapEntryManager.find(LDAPRequestorEntry.class, getDnForLDAPRequestorEntry(inum));
        return result.getEntry();
    }
    
    
    /**
    * Add new LDAPApplicationSelectorEntry.
    * 
    * @param entry LDAPApplicationSelectorEntry
    */
    public void addApplicationSelectorEntry(ApplicationSelectorEntry entry) {
        LDAPApplicationSelectorEntry ldapEntry = new LDAPApplicationSelectorEntry();
        ldapEntry.setEntry(entry);
        String inum = generateInumImpl();
        ldapEntry.setInum(inum);
        ldapEntry.setDn(getDnForLDAPApplicationSelectorEntry(inum));
        ldapEntryManager.persist(ldapEntry);
    }

    /**
    * Update LDAPApplicationSelectorEntry.
    * 
    * @param entry LDAPApplicationSelectorEntry
    */
    public void updateApplicationSelectorEntry(ApplicationSelectorEntry entry) {
        LDAPApplicationSelectorEntry ldapEntry = new LDAPApplicationSelectorEntry();
        ldapEntry.setDn(getDnForLDAPApplicationSelectorEntry(entry.getInum()));
        ldapEntry.setEntry(entry);
        ldapEntryManager.merge(ldapEntry);
    }

    /**
    * Remove LDAPApplicationSelectorEntry.
    * 
    * @param entry LDAPApplicationSelectorEntry
    */
    public void removeApplicationSelectorEntry(ApplicationSelectorEntry entry) {
        LDAPApplicationSelectorEntry ldapEntry = new LDAPApplicationSelectorEntry();
        ldapEntry.setDn(getDnForLDAPApplicationSelectorEntry(entry.getInum()));
        ldapEntry.setEntry(entry);
        ldapEntryManager.remove(ldapEntry);
    }

    /**
    * Check if LDAP server contains LDAPApplicationSelectorEntry with specified attributes.
    * 
    * @param entry LDAPApplicationSelectorEntry
    * @return True if entry with specified attributes exist
    */
    public boolean containsApplicationSelectorEntry(ApplicationSelectorEntry entry) {
        LDAPApplicationSelectorEntry ldapEntry = new LDAPApplicationSelectorEntry();
        ldapEntry.setDn(getDnForLDAPApplicationSelectorEntry(entry.getInum()));
        ldapEntry.setEntry(entry);
        return ldapEntryManager.contains(ldapEntry);
    }

    /**
    * Read ApplicationSelectorEntry from LDAP.
    * 
    * @param inum Entry inum.
    * @return ApplicationSelectorEntry
    */
    public ApplicationSelectorEntry readApplicationSelectorEntry(String inum) {
        LDAPApplicationSelectorEntry result = ldapEntryManager.find(LDAPApplicationSelectorEntry.class, getDnForLDAPApplicationSelectorEntry(inum));
        return result.getEntry();
    }
}
