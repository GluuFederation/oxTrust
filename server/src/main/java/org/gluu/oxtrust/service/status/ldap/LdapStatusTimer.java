/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service.status.ldap;

import java.util.concurrent.atomic.AtomicBoolean;

import org.xdi.service.cdi.async.Asynchronous;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.ldap.service.AppInitializer;
import org.gluu.oxtrust.ldap.service.CentralLdapService;
import org.gluu.persist.ldap.impl.LdapEntryManager;
import org.gluu.persist.ldap.operation.impl.LdapConnectionProvider;
import org.slf4j.Logger;
import org.xdi.service.cdi.event.LdapStatusEvent;
import org.xdi.service.cdi.event.Scheduled;
import org.xdi.service.timer.event.TimerEvent;
import org.xdi.service.timer.schedule.TimerSchedule;

/**
 * @author Yuriy Movchan
 * @version 0.1, 11/18/2012
 */
@ApplicationScoped
@Named
public class LdapStatusTimer {

    private final static int DEFAULT_INTERVAL = 60; // 1 minute

    @Inject
    private Logger log;
    
    @Inject
    private CentralLdapService centralLdapService;

	@Inject
	private Event<TimerEvent> timerEvent;

	@Inject @Named(AppInitializer.LDAP_ENTRY_MANAGER_NAME)
    private LdapEntryManager ldapEntryManager;

	@Inject @Named(AppInitializer.LDAP_CENTRAL_ENTRY_MANAGER_NAME)
    private LdapEntryManager ldapCentralEntryManager;

    private AtomicBoolean isActive;

    public void initTimer() {
        log.info("Initializing Ldap Status Timer");
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
    	
    	if (centralLdapService.isUseCentralServer() && (ldapCentralEntryManager.getOperationService() != null)) {
    		logConnectionProviderStatistic(ldapCentralEntryManager, "centralConnectionProvider");
    	}
    }

	public void logConnectionProviderStatistic(LdapEntryManager ldapEntryManager, String connectionProviderName) {
		LdapConnectionProvider ldapConnectionProvider = ldapEntryManager.getOperationService().getConnectionProvider();
        
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