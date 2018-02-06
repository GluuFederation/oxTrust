/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2017, Gluu
 */
package org.gluu.oxtrust.ldap.service;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.persist.ldap.impl.LdapEntryManager;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.LdapShibbolethCASProtocolConfiguration;
import org.xdi.config.oxtrust.ShibbolethCASProtocolConfiguration;
import org.xdi.util.INumGenerator;
import org.xdi.util.StringHelper;

/**
 * CAS LDAP configuration service.
 * 
 * @author Dmitry Ognyannikov, 2017
 */
@Stateless
@Named("casService")
public class CASService implements Serializable {
    
	private static final long serialVersionUID = -6130872937911013810L;

	@Inject
    private Logger log;
    
    @Inject
    private LdapEntryManager ldapEntryManager;
    
    @Inject
    OrganizationService organizationService;
     
    @PostConstruct
    public void init() {
    }
    
    @PreDestroy
    public void destroy() {
    }
    
    public ShibbolethCASProtocolConfiguration loadCASConfiguration() {
        log.info("loadCASConfiguration() call");
        List<LdapShibbolethCASProtocolConfiguration> entries = ldapEntryManager.findEntries(getDnForLdapShibbolethCASProtocolConfiguration(null), LdapShibbolethCASProtocolConfiguration.class, null);
        if (!entries.isEmpty())
            return entries.get(0).getCasProtocolConfiguration();
        else
            return null;
    }
    
    public void updateCASConfiguration(ShibbolethCASProtocolConfiguration entry) {
        log.info("updateCASConfiguration() call");
        LdapShibbolethCASProtocolConfiguration ldapEntry = ldapEntryManager.find(LdapShibbolethCASProtocolConfiguration.class, getDnForLdapShibbolethCASProtocolConfiguration(entry.getInum()));
        ldapEntry.setInum(entry.getInum());
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
            entry.setInum(inum);
            ldapEntry.setInum(inum);
            ldapEntry.setDn(getDnForLdapShibbolethCASProtocolConfiguration(inum));
            ldapEntryManager.persist(ldapEntry);
        } catch (Exception e) {
            log.error("addIDPEntry() exception", e);
        }
    }
    
    private String getDnForLdapShibbolethCASProtocolConfiguration(String inum) {
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
    
}
