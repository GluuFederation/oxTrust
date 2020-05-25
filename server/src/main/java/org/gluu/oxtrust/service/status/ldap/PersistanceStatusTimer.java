/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service.status.ldap;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.service.ApplicationFactory;
import org.gluu.persist.PersistenceEntryManager;
import org.gluu.persist.ldap.impl.LdapEntryManager;
import org.gluu.persist.ldap.operation.impl.LdapConnectionProvider;
import org.gluu.persist.operation.PersistenceOperationService;
import org.gluu.service.cdi.async.Asynchronous;
import org.gluu.service.cdi.event.LdapStatusEvent;
import org.gluu.service.cdi.event.Scheduled;
import org.gluu.service.timer.event.TimerEvent;
import org.gluu.service.timer.schedule.TimerSchedule;
import org.slf4j.Logger;

/**
 * @author Yuriy Movchan
 * @version 0.1, 11/18/2012
 */
@ApplicationScoped
@Named
public class PersistanceStatusTimer {

    private final static int DEFAULT_INTERVAL = 60; // 1 minute

    @Inject
    private Logger log;

	@Inject
	private Event<TimerEvent> timerEvent;

	@Inject @Named(ApplicationFactory.PERSISTENCE_ENTRY_MANAGER_NAME)
    private PersistenceEntryManager ldapEntryManager;

    private AtomicBoolean isActive;

    public void initTimer() {
        log.info("Initializing Persistance Layer Status Timer");
        this.isActive = new AtomicBoolean(false);

		timerEvent.fire(new TimerEvent(new TimerSchedule(DEFAULT_INTERVAL, DEFAULT_INTERVAL), new LdapStatusEvent(),
				Scheduled.Literal.INSTANCE));
    }

    @Asynchronous
    public void process(@Observes @Scheduled LdapStatusEvent ldapStatusEvent) {
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
    	logConnectionProviderStatistic(ldapEntryManager, "connectionProvider");
    }

	public void logConnectionProviderStatistic(PersistenceEntryManager ldapEntryManager, String connectionProviderName) {
	    PersistenceOperationService persistenceOperationService = ldapEntryManager.getOperationService();
	    if (!(persistenceOperationService instanceof LdapEntryManager)) {
	        return;
	    }

	    LdapConnectionProvider ldapConnectionProvider = (LdapConnectionProvider) persistenceOperationService;
        
        if (ldapConnectionProvider == null) {
        	log.error("{} is empty", connectionProviderName);
        } else {
            if (ldapConnectionProvider.getConnectionPool() == null) {
            	log.error("{} is empty", connectionProviderName);
            } else {
            	log.debug("{} statistics: {}", connectionProviderName, ldapConnectionProvider.getConnectionPool().getConnectionPoolStatistics());
            }
        }
	}

}