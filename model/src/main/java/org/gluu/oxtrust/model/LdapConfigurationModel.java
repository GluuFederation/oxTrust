/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model;

import java.util.List;

import org.gluu.model.ldap.GluuLdapConfiguration;

/**
 * Base operations with GluuLdapConfiguration. Need for JSF2 facelet because it
 * doesn't support parameters in action method
 * 
 * @author Yuriy Movchan Date: 04/18/2013
 */
public interface LdapConfigurationModel {

	public void setActiveLdapConfig(GluuLdapConfiguration activeLdapConfig);

	public void addLdapConfig(List<GluuLdapConfiguration> ldapConfigList);
	public void removeLdapConfig(List<GluuLdapConfiguration> ldapConfigList, GluuLdapConfiguration removeLdapConfig);
}