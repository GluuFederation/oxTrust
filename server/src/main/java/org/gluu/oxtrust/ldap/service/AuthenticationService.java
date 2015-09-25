/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.model.SimpleProperty;
import org.xdi.model.ldap.GluuLdapConfiguration;
import org.xdi.util.StringHelper;

/**
 * Provides operations with persons
 * 
 * @author Yuriy Movchan Date: 10.13.2010
 */
@Scope(ScopeType.STATELESS)
@Name("authenticationService")
@AutoCreate
public class AuthenticationService implements Serializable {

	private static final long serialVersionUID = 6685720517520443399L;

	@Logger
	private Log log;

	@In(required = false)
	private GluuLdapConfiguration ldapAuthConfig;

	@In
	private LdapEntryManager ldapAuthEntryManager;

	@In(value = "#{oxTrustConfiguration.applicationConfiguration}")
	private ApplicationConfiguration applicationConfiguration;

	/**
	 * Authenticate user
	 * 
	 * @param userName
	 *            User name
	 * @param password
	 *            User password
	 * @return
	 */
	public boolean authenticate(String userName, String password) {
        log.debug("Authenticating User with LDAP: username: {0}", userName);
        if (ldapAuthConfig == null) {
        	return ldapAuthEntryManager.authenticate(userName, password, applicationConfiguration.getBaseDN());
       } else {
	        String primaryKey = "uid";
	        if (StringHelper.isNotEmpty(ldapAuthConfig.getPrimaryKey())) {
	            primaryKey = ldapAuthConfig.getPrimaryKey();
	        }
	
//	        String localPrimaryKey = "uid";
//	        if (StringHelper.isNotEmpty(ldapAuthConfig.getLocalPrimaryKey())) {
//	            localPrimaryKey = ldapAuthConfig.getLocalPrimaryKey();
//	        }
        
	        log.debug("Attempting to find userDN by primary key: {0}", primaryKey);

            final List<SimpleProperty> baseDNs = ldapAuthConfig.getBaseDNs();
            if (baseDNs != null && !baseDNs.isEmpty()) {
                for (SimpleProperty baseDnProperty : baseDNs) {
                    String baseDn = baseDnProperty.getValue();

                    GluuCustomPerson user = getUserByAttribute(baseDn, primaryKey, userName);
                    if (user != null) {
                        String userDn = user.getDn();
                        log.debug("Attempting to authenticate userDN: {0}", userDn);
                        if (ldapAuthEntryManager.authenticate(userDn, password)) {
                            log.debug("User authenticated: {0}", userDn);
                            
                            // TODO: If we will get issues we need to use localPrimaryKey to map remote user to local user. Please contact me about this.
                            // We don't need this in oxTrsut+oxAuth mode
                            return true;
                        }
                    }
                }
            } else {
                log.error("There are no baseDns specified in authentication configuration.");
            }
        }

        return false;
    }

    private GluuCustomPerson getUserByAttribute(String baseDn, String attributeName, String attributeValue) {
        log.debug("Getting user information from LDAP: attributeName = '{0}', attributeValue = '{1}'", attributeName, attributeValue);

        GluuCustomPerson user = new GluuCustomPerson();
        user.setDn(baseDn);
        
        List<GluuCustomAttribute> customAttributes =  new ArrayList<GluuCustomAttribute>();
        customAttributes.add(new GluuCustomAttribute(attributeName, attributeValue));

        user.setCustomAttributes(customAttributes);

        List<GluuCustomPerson> entries = ldapAuthEntryManager.findEntries(user);
        log.debug("Found '{0}' entries", entries.size());

        if (entries.size() > 0) {
            return entries.get(0);
        } else {
            return null;
        }
    }

}