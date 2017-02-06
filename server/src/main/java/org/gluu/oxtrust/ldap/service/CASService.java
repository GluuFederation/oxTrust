/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2017, Gluu
 */
package org.gluu.oxtrust.ldap.service;

import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.xdi.config.oxtrust.LdapShibbolethCASProtocolConfiguration;
import org.xdi.config.oxtrust.ShibbolethCASProtocolConfiguration;
import org.xdi.util.INumGenerator;
import org.xdi.util.StringHelper;

/**
 * CAS LDAP configuration service.
 * 
 * @author Dmitry Ognyannikov, 2017
 */
@Name("casService")
@AutoCreate
@Scope(ScopeType.APPLICATION)
public class CASService {
    
    @Logger
    private Log log;
    
    @In
    private LdapEntryManager ldapEntryManager;
    
    @In
    OrganizationService organizationService;
     
    @Create
    public void init() {
    }
    
    @Destroy
    public void destroy() {
    }
    
    public ShibbolethCASProtocolConfiguration loadCASConfiguration() {
        String organizationDn = organizationService.getDnForOrganization();
        LdapShibbolethCASProtocolConfiguration ldapConfiguration = ldapEntryManager.find(LdapShibbolethCASProtocolConfiguration.class, "ou=cas,ou=oxidp,"+organizationDn, null);
        
        return ldapConfiguration.getCasProtocolConfiguration();
    }
    
    public void updateCASConfiguration(ShibbolethCASProtocolConfiguration entry) {
        String organizationDn = organizationService.getDnForOrganization();
        LdapShibbolethCASProtocolConfiguration ldapEntry = ldapEntryManager.find(LdapShibbolethCASProtocolConfiguration.class, "ou=cas,ou=oxidp,"+organizationDn);
        ldapEntry.setInum(entry.getOrgInum());
        ldapEntry.setCasProtocolConfiguration(entry);
        ldapEntryManager.merge(ldapEntry);
    }
    
    public void addCASConfiguration(ShibbolethCASProtocolConfiguration entry) {
        log.info("addCASConfiguration() call");
        try {
            LdapShibbolethCASProtocolConfiguration ldapEntry = new LdapShibbolethCASProtocolConfiguration();
            ldapEntry.setCasProtocolConfiguration(entry);
            String inum = generateInum();
            log.info("getDnForLdapShibbolethCASProtocolConfiguration(inum) retsult: " + getDnForLdapShibbolethCASProtocolConfiguration(inum));
            entry.setOrgInum(inum);
            ldapEntry.setInum(inum);
            ldapEntry.setDn(getDnForLdapShibbolethCASProtocolConfiguration(inum));
            ldapEntryManager.persist(ldapEntry);
        } catch (Exception e) {
            log.error("addIDPEntry() exception", e);
        }
    }
    
    public String getDnForLdapShibbolethCASProtocolConfiguration(String inum) {
        String organizationDn = organizationService.getDnForOrganization();
        if (StringHelper.isEmpty(inum)) {
            return String.format("ou=cas,ou=oxidp,%s", organizationDn);
        }
        return String.format("inum=%s,ou=cas,ou=oxidp,%s", inum, organizationDn);
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
    
    public static CASService instance() {
        return (CASService) Component.getInstance(CASService.class);
    }
    
}
