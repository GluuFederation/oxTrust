/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.site.ldap.LDAPConnectionProvider;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.async.TimerSchedule;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.Log;
import org.xdi.util.properties.FileConfiguration;

/**
 * Check periodically if application lost connection to LDAP server and make
 * attempt to reconnect to LDAP server if needed.
 * 
 * @author Yuriy Movchan Date: 09.22.2010
 */
@Name("ldapConnectionChecker")
@Scope(ScopeType.APPLICATION)
@AutoCreate
@Startup(depends = "appInitializer")
public class LdapConnectionCheckerTimer extends AbstractConnectionCheckerTimer {

	@Logger
	Log log;

	@In
	private FileConfiguration localLdapConfiguration;

	@In(required = false)
	private FileConfiguration centralLdapConfiguration;

	@In
	private LDAPConnectionProvider connectionProvider;

	@In(required = false)
	private LDAPConnectionProvider centralConnectionProvider;

	private boolean isActive;

	@Observer("org.jboss.seam.postInitialization")
	public void init() {
		// Schedule to run it every 120 seconds. First event will occur after 30
		// seconds
		Events.instance().raiseTimedEvent(OxTrustConstants.EVENT_LDAP_CONNECTION_CHECKER_TIMER,
				new TimerSchedule(30 * 1000L, AppInitializer.CONNECTION_CHECKER_INTERVAL));
	}

	@Observer(OxTrustConstants.EVENT_LDAP_CONNECTION_CHECKER_TIMER)
	@Asynchronous
	public void process() {
		if (this.isActive) {
			return;
		}

		try {
			this.isActive = true;
			processImpl(localLdapConfiguration, connectionProvider);
			processImpl(centralLdapConfiguration, centralConnectionProvider);
		} catch (Throwable ex) {
			log.error("Exception happened while checking LDAP connections", ex);
		} finally {
			this.isActive = false;
		}
	}

}
