/*
 * oxAuth is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.log.Log;
import org.xdi.service.cache.MemcachedConfiguration;

/**
 * Holds factory methods to create services
 *
 * @author Yuriy Movchan Date: 02/14/2017
 */
@Scope(ScopeType.APPLICATION)
@Name("applicationFactory")
@Startup
public class ApplicationFactory {
    
    @In
    private ApplianceService applianceService;

    @Logger
    private Log log;

	@Factory(value = "memcachedConfiguration", scope = ScopeType.APPLICATION, autoCreate = true)
	public MemcachedConfiguration createMemcachedConfiguration() {
		MemcachedConfiguration memcachedConfiguration = applianceService.getAppliance().getMemcachedConfiguration();
		if (memcachedConfiguration == null || StringUtils.isBlank(memcachedConfiguration.getServers())) {
			throw new RuntimeException("Failed to load memcached configuration from ldap. Please check appliance ldap entry.");
		} else {
			log.trace("Memcached configuration: " + memcachedConfiguration);
		}
		return memcachedConfiguration;
	}

}