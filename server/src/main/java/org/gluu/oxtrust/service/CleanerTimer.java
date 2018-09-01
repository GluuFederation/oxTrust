package org.gluu.oxtrust.service;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.ejb.DependsOn;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.persist.PersistenceEntryManager;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.model.ApplicationType;
import org.xdi.service.CacheService;
import org.xdi.service.cache.NativePersistenceCacheProvider;
import org.xdi.service.cdi.async.Asynchronous;
import org.xdi.service.cdi.event.CleanerEvent;
import org.xdi.service.cdi.event.Scheduled;
import org.xdi.service.timer.event.TimerEvent;
import org.xdi.service.timer.schedule.TimerSchedule;

/**
 * Cleaner service
 * 
 * @author Yuriy Movchan Date: 09/01/2018
 */
@ApplicationScoped
@DependsOn("appInitializer")
@Named
public class CleanerTimer {

    public final static int BATCH_SIZE = 100;
    private final static int DEFAULT_INTERVAL = 600; // 10 minutes

    @Inject
    private Logger log;

    @Inject
    private PersistenceEntryManager ldapEntryManager;

    @Inject
    private PasswordResetService passwordResetService;

    @Inject
    private CacheService cacheService;

    @Inject
    private MetricService metricService;

    @Inject
    private AppConfiguration appConfiguration;

    @Inject
    private Event<TimerEvent> cleanerEvent;

    private AtomicBoolean isActive;

    public void initTimer() {
        log.debug("Initializing Cleaner Timer");
        this.isActive = new AtomicBoolean(false);

        int interval = appConfiguration.getCleanServiceInterval();
        if (interval <= 0) {
            interval = DEFAULT_INTERVAL;
        }

        cleanerEvent.fire(new TimerEvent(new TimerSchedule(interval, interval), new CleanerEvent(), Scheduled.Literal.INSTANCE));
    }

    @Asynchronous
    public void process(@Observes @Scheduled CleanerEvent cleanerEvent) {
        if (this.isActive.get()) {
            return;
        }

        if (!this.isActive.compareAndSet(false, true)) {
            return;
        }

        try {
            Date now = new Date();

            processCache(now);
            processPasswordReset();
            processMetricEntries();
        } finally {
            this.isActive.set(false);
        }
    }

    protected void processPasswordReset() {
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        calendar.add(Calendar.SECOND, -appConfiguration.getPasswordResetRequestExpirationTime());
        final Date expirationDate = calendar.getTime();
        
        passwordResetService.cleanup(expirationDate);
    }

    private void processCache(Date now) {
        try {
            if (cacheService.getCacheProvider() instanceof NativePersistenceCacheProvider) {
                ((NativePersistenceCacheProvider) cacheService.getCacheProvider()).cleanup(now, BATCH_SIZE);
            }
        } catch (Exception e) {
            log.error("Failed to clean up cache.", e);
        }
    }

    private void processMetricEntries() {
        log.debug("Start metric entries clean up");

        int keepDataDays = appConfiguration.getMetricReporterKeepDataDays();

        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        calendar.add(Calendar.DATE, -keepDataDays);
        Date expirationDate = calendar.getTime();

        metricService.removeExpiredMetricEntries(expirationDate, ApplicationType.OX_AUTH, metricService.applianceInum(), 0, BATCH_SIZE);

        log.debug("End metric entries clean up");
    }

}
