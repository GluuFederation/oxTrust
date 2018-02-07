/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2016, Gluu
 */
package org.gluu.oxtrust.ldap.service;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.asimba.util.ldap.LDAPUtility;
import org.gluu.asimba.util.ldap.idp.IDPEntry;
import org.gluu.asimba.util.ldap.idp.LdapIDPEntry;
import org.gluu.asimba.util.ldap.selector.ApplicationSelectorEntry;
import org.gluu.asimba.util.ldap.selector.LDAPApplicationSelectorEntry;
import org.gluu.asimba.util.ldap.sp.LDAPRequestorEntry;
import org.gluu.asimba.util.ldap.sp.LDAPRequestorPoolEntry;
import org.gluu.asimba.util.ldap.sp.RequestorEntry;
import org.gluu.asimba.util.ldap.sp.RequestorPoolEntry;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.oxtrust.util.ServiceUtil;
import org.gluu.persist.ldap.impl.LdapEntryManager;
import org.gluu.search.filter.Filter;
import org.richfaces.model.UploadedFile;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.LdapOxAsimbaConfiguration;
import org.xdi.util.INumGenerator;
import org.xdi.util.StringHelper;

/**
 * Asimba LDAP configuration service.
 * 
 * @author Dmitry Ognyannikov, 2016
 */
@ApplicationScoped
@Named
public class AsimbaService implements Serializable {
    public static String METADATA_IDP_CONFIGURATION_DIR = "${webapp.root}/WEB-INF/sample-data/";
    public static String METADATA_SP_CONFIGURATION_DIR = "${webapp.root}/WEB-INF/sample-data/";
    
    @Inject
    private Logger log;
    
    @Inject
    private LdapEntryManager ldapEntryManager;
    
    @Inject
    private OrganizationService organizationService;
    
    @Inject
    private ServiceUtil serviceUtil;
    
    public LdapOxAsimbaConfiguration loadAsimbaConfiguration() {
        String organizationDn = organizationService.getDnForOrganization();
        LdapOxAsimbaConfiguration ldapConfiguration = ldapEntryManager.find(LdapOxAsimbaConfiguration.class, "ou=oxasimba,"+organizationDn, null);
        
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
        Filter searchFilter = Filter.createORFilter(idFilter, friendlyNameFilter, descriptionFilter, inameFilter);

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
        String organizationDn = organizationService.getDnForOrganization();
        if (StringHelper.isEmpty(inum)) {
            return String.format("ou=idps,ou=oxasimba,%s", organizationDn);
        }
        return String.format("inum=%s,ou=idps,ou=oxasimba,%s", inum, organizationDn);
    }
    
    /**
    * Build DN string for LDAPApplicationSelectorEntry
    * 
    * @param inum entry Inum
    * @return DN string for specified entry or DN for entry branch if inum is null
    * @throws Exception
    */
    public String getDnForLDAPApplicationSelectorEntry(String inum) {
        String organizationDn = organizationService.getDnForOrganization();
        if (StringHelper.isEmpty(inum)) {
            return String.format("ou=selectors,ou=oxasimba,%s", organizationDn);
        }
        return String.format("inum=%s,ou=selectors,ou=oxasimba,%s", inum, organizationDn);
    }
    
    /**
    * Build DN string for LDAPRequestorEntry
    * 
    * @param inum entry Inum
    * @return DN string for specified entry or DN for entry branch if inum is null
    * @throws Exception
    */
    public String getDnForLDAPRequestorEntry(String inum) {
        String organizationDn = organizationService.getDnForOrganization();
        if (StringHelper.isEmpty(inum)) {
            return String.format("ou=requestors,ou=oxasimba,%s", organizationDn);
        }
        return String.format("inum=%s,ou=requestors,ou=oxasimba,%s", inum, organizationDn);
    }
    
    /**
    * Build DN string for LDAPRequestorPoolEntry
    * 
    * @param inum entry Inum
    * @return DN string for specified entry or DN for entry branch if inum is null
    * @throws Exception
    */
    public String getDnForLDAPRequestorPoolEntry(String inum) {
        String organizationDn = organizationService.getDnForOrganization();
        if (StringHelper.isEmpty(inum)) {
            return String.format("ou=requestorpools,ou=oxasimba,%s", organizationDn);
        }
        return String.format("inum=%s,ou=requestorpools,ou=oxasimba,%s", inum, organizationDn);
    }
    
    /**
    * Generate new inum for Scope
    * 
    * @return New inum for Scope
    * @throws Exception
    */
    private static String generateInum() {
        return INumGenerator.generate(1);
    }
    
    /**
    * Add new IDPEntry.
    * 
    * @param entry IDPEntry
    */
    public void addIDPEntry(IDPEntry entry) {
        log.info("addIDPEntry() call");
        try {
            entry.setLastModified(new Date());
            LdapIDPEntry ldapEntry = new LdapIDPEntry();
            ldapEntry.setEntry(entry);
            String inum = generateInum();
            ldapEntry.setInum(inum);
            log.info("getDnForLdapIDPEntry(inum) retsult: " + getDnForLdapIDPEntry(inum));
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
        entry.setLastModified(new Date());
        LdapIDPEntry ldapEntry = ldapEntryManager.find(LdapIDPEntry.class, getDnForLdapIDPEntry(entry.getInum()));
        ldapEntry.setEntry(entry);
        ldapEntryManager.merge(ldapEntry);
    }

    /**
    * Remove IDPEntry.
    * 
    * @param entry IDPEntry
    */
    public void removeIDPEntry(IDPEntry entry) {
        LdapIDPEntry ldapEntry = ldapEntryManager.find(LdapIDPEntry.class, getDnForLdapIDPEntry(entry.getInum()));
        ldapEntry.setEntry(entry);
        ldapEntryManager.remove(ldapEntry);
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
        entry.setLastModified(new Date());
        LDAPRequestorPoolEntry ldapEntry = new LDAPRequestorPoolEntry();
        ldapEntry.setEntry(entry);
        String inum = generateInum();
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
        entry.setLastModified(new Date());
        LDAPRequestorPoolEntry ldapEntry = ldapEntryManager.find(LDAPRequestorPoolEntry.class, getDnForLDAPRequestorPoolEntry(entry.getInum()));
        ldapEntry.setEntry(entry);
        ldapEntryManager.merge(ldapEntry);
    }

    /**
    * Remove LDAPRequestorPoolEntry.
    * 
    * @param entry LDAPRequestorPoolEntry
    */
    public void removeRequestorPoolEntry(RequestorPoolEntry entry) {
        LDAPRequestorPoolEntry ldapEntry = ldapEntryManager.find(LDAPRequestorPoolEntry.class, getDnForLDAPRequestorPoolEntry(entry.getInum()));
        ldapEntry.setEntry(entry);
        ldapEntryManager.remove(ldapEntry);
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
        entry.setLastModified(new Date());
        LDAPRequestorEntry ldapEntry = new LDAPRequestorEntry();
        ldapEntry.setEntry(entry);
        String inum = generateInum();
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
        entry.setLastModified(new Date());
        LDAPRequestorEntry ldapEntry = ldapEntryManager.find(LDAPRequestorEntry.class, getDnForLDAPRequestorEntry(entry.getInum()));
        ldapEntry.setEntry(entry);
        ldapEntryManager.merge(ldapEntry);
    }

    /**
    * Remove LDAPRequestorEntry.
    * 
    * @param entry LDAPRequestorEntry
    */
    public void removeRequestorEntry(RequestorEntry entry) {
        LDAPRequestorEntry ldapEntry = ldapEntryManager.find(LDAPRequestorEntry.class, getDnForLDAPRequestorEntry(entry.getInum()));
        ldapEntry.setEntry(entry);
        ldapEntryManager.remove(ldapEntry);
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
        entry.setLastModified(new Date());
        LDAPApplicationSelectorEntry ldapEntry = new LDAPApplicationSelectorEntry();
        ldapEntry.setEntry(entry);
        String inum = generateInum();
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
        entry.setLastModified(new Date());
        LDAPApplicationSelectorEntry ldapEntry = ldapEntryManager.find(LDAPApplicationSelectorEntry.class, getDnForLDAPApplicationSelectorEntry(entry.getInum()));
        ldapEntry.setEntry(entry);
        ldapEntryManager.merge(ldapEntry);
    }

    /**
    * Remove LDAPApplicationSelectorEntry.
    * 
    * @param entry LDAPApplicationSelectorEntry
    */
    public void removeApplicationSelectorEntry(ApplicationSelectorEntry entry) {
        LDAPApplicationSelectorEntry ldapEntry = ldapEntryManager.find(LDAPApplicationSelectorEntry.class, getDnForLDAPApplicationSelectorEntry(entry.getInum()));
        ldapEntry.setEntry(entry);
        ldapEntryManager.remove(ldapEntry);
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
    
    public String saveIDPMetadataFile(UploadedFile uploadedFile, IDPEntry idp) throws IOException {
        String baseDir = LDAPUtility.getBaseDirectory() + File.separator + "conf" + File.separator + "asimba" 
                + File.separator + "metadata" + File.separator + "idp";
        
        byte[] fileContent = serviceUtil.copyUploadedFile(uploadedFile);
        
        // save copy to LDAP:
        idp.setMetadataXMLText(new String(fileContent, "UTF8"));
        
        return ServiceUtil.saveRandomFile(fileContent, baseDir, "xml");
    }
    
    public String saveSPRequestorMetadataFile(UploadedFile uploadedFile) throws IOException {
        String baseDir = LDAPUtility.getBaseDirectory() + File.separator + "conf" + File.separator + "asimba" 
                + File.separator + "metadata" + File.separator + "sp";
        
        return serviceUtil.saveUploadedFile(uploadedFile, baseDir, "xml");
    }
    
    public void restartAsimbaService() {
        try {
            // shell call
            log.info("restart asimba service throught shell");
            Runtime.getRuntime().exec(new String[]{"service", "asimba", "restart"});
        } catch (Exception e) {
            log.error("restartAsimbaService() exception", e);
        }
    } 
    
}
