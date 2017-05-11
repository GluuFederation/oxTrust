/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service.status.ldap;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.ejb.Asynchronous;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.site.ldap.LDAPConnectionProvider;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.slf4j.Logger;
import org.xdi.service.timer.schedule.TimerSchedule;

/**
 * @author Yuriy Movchan
 * @version 0.1, 11/18/2012
 */
@Named("ldapStatusTimer")
@ApplicationScoped
public class LdapStatusTimer {

    private final static String EVENT_TYPE = "LdapStatusTimerEvent";
    private final static long DEFAULT_INTERVAL = 60 * 1000; // 1 minute

    @Inject
    private Logger log;

    private AtomicBoolean isActive;

    @Observer("org.jboss.seam.postInitialization")
    public void init() {
        log.info("Initializing LdapStatusTimer");
        this.isActive = new AtomicBoolean(false);

        Events.instance().raiseTimedEvent(EVENT_TYPE, new TimerSchedule(DEFAULT_INTERVAL, DEFAULT_INTERVAL));
    }

    @Observer(EVENT_TYPE)
    @Asynchronous
    public void process() {
        if (this.isActive.get()) {
            return;
        }

        if (!this.isActive.compareAndSet(false, true)) {
            return;
        }

        try {
            processInt();
        } finally {
            this.isActive.set(false);
        }
    }

    private void processInt() {
        logConnectionProviderStatistic("connectionProvider", null);
        logConnectionProviderStatistic("centralConnectionProvider", null);
        logConnectionProviderStatistic("authConnectionProvider", null);
    }

	public void logConnectionProviderStatistic(String connectionProvider, String bindConnectionProvider) {
        if ((connectionProvider != null) && Contexts.getApplicationContext().isSet(connectionProvider)) {
    		LDAPConnectionProvider ldapConnectionProvider = (LDAPConnectionProvider) Contexts.getApplicationContext().get(connectionProvider);

    		if (ldapConnectionProvider.getConnectionPool() == null) {
            	log.error("{0} is empty", connectionProvider);
            } else {
            	log.info("{0} statistics: {1}", connectionProvider, ldapConnectionProvider.getConnectionPool().getConnectionPoolStatistics());
            }
        }

        if ((bindConnectionProvider != null) && Contexts.getApplicationContext().isSet(bindConnectionProvider)) {
            LDAPConnectionProvider bindLdapConnectionProvider = (LDAPConnectionProvider) Contexts.getApplicationContext().get(bindConnectionProvider);

            if (bindLdapConnectionProvider.getConnectionPool() == null) {
            	log.error("{0} is empty", bindConnectionProvider);
            } else {
            	log.info("{0} statistics: {1}", bindConnectionProvider, bindLdapConnectionProvider.getConnectionPool().getConnectionPoolStatistics());
            }
        }
	}

}