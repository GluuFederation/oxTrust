package org.gluu.oxtrust.ldap.service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.gluu.oxtrust.config.OxTrustConfiguration;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.async.TimerSchedule;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.Log;

/**
 * @author Yuriy Movchan
 * @version 0.1, 05/20/2013
 */
@Name("configurationUpdateTimer")
@AutoCreate
@Scope(ScopeType.APPLICATION)
@Startup(depends = "appInitializer")
public class ConfigurationUpdateTimer {

    private final static String EVENT_TYPE = "ConfigurationUpdateTimerEvent";
    private final static long DEFAULT_INTERVAL = TimeUnit.MINUTES.toMillis(1); // 1 minute

    @Logger
    private Log log;

    private AtomicBoolean isActive;

    @Observer("org.jboss.seam.postInitialization")
    public void init() {
        this.isActive = new AtomicBoolean(false);
        Events.instance().raiseTimedEvent(EVENT_TYPE, new TimerSchedule(DEFAULT_INTERVAL, DEFAULT_INTERVAL));
    }

    @Observer(EVENT_TYPE)
    @Asynchronous
    public void process() {
        if (this.isActive.get()) {
            return;
        }

        if (this.isActive.compareAndSet(false, true)) {
            try {
            	Events.instance().raiseEvent(OxTrustConfiguration.EVENT_UPDATE_CONFIGURATION);
            	log.trace("Configuration updated from LDAP successfully.");
            } finally {
                this.isActive.set(false);
            }
        }
    }
}
