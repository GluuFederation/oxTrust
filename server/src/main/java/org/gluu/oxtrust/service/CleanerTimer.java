package org.gluu.oxtrust.service;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.ejb.DependsOn;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.model.ApplicationType;
import org.gluu.model.metric.ldap.MetricEntry;
import org.gluu.oxtrust.model.PasswordResetRequest;
import org.gluu.persist.PersistenceEntryManager;
import org.gluu.search.filter.Filter;
import org.gluu.service.cache.CacheProvider;
import org.gluu.service.cdi.async.Asynchronous;
import org.gluu.service.cdi.event.CleanerEvent;
import org.gluu.service.cdi.event.Scheduled;
import org.gluu.service.timer.event.TimerEvent;
import org.gluu.service.timer.schedule.TimerSchedule;
import org.slf4j.Logger;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;

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
	private final static int DEFAULT_INTERVAL = 60; // 1 minute

	@Inject
	private Logger log;

	@Inject
	private PasswordResetService passwordResetService;

	@Inject
	private CacheProvider cacheProvider;

	@Inject
	private MetricService metricService;

	@Inject
	private AppConfiguration appConfiguration;

	@Inject
	private Event<TimerEvent> cleanerEvent;

	@Inject
	private CleanUpLogger cleanUpLogger;

    @Inject
    private PersistenceEntryManager entryManager;

	private long lastFinishedTime;

	private AtomicBoolean isActive;

	public void initTimer() {
		log.debug("Initializing Cleaner Timer");
		cleanUpLogger.addNewLogLine("Initializing Cleaner Timer at:" + new Date());

		this.isActive = new AtomicBoolean(false);

		// Schedule to start cleaner every 1 minute
		cleanerEvent.fire(
				new TimerEvent(new TimerSchedule(DEFAULT_INTERVAL, DEFAULT_INTERVAL), new CleanerEvent(), Scheduled.Literal.INSTANCE));

		cleanUpLogger.addNewLogLine("Initialization Done at :" + new Date());
	}

	@Asynchronous
	public void process(@Observes @Scheduled CleanerEvent cleanerEvent) {
		cleanUpLogger.addNewLogLine("++++Starting processing clean up services at:" + new Date());

		if (this.isActive.get()) {
			return;
		}

		if (!this.isActive.compareAndSet(false, true)) {
			return;
		}

		try {
			processImpl();
		} finally {
			this.isActive.set(false);
		}

		cleanUpLogger.addNewLogLine("+Processing clean up services done at:" + new Date());
	}

	public void processImpl() {
        try {
			if (!isStartProcess()) {
				log.trace("Starting conditions aren't reached");
				return;
			}

			int chunkSize = BATCH_SIZE;

            Date now = new Date();

            final Set<String> processedBaseDns = new HashSet<>();
            for (Map.Entry<String, Class<?>> baseDn : createCleanServiceBaseDns().entrySet()) {
                try {
                    if (entryManager.hasExpirationSupport(baseDn.getKey())) {
                        continue;
                    }

                    String processedBaseDn = baseDn.getKey() + "_" + (baseDn.getValue() == null ? "" : baseDn.getValue().getSimpleName());
                    if (processedBaseDns.contains(processedBaseDn)) {
                        log.warn("baseDn: {}, already processed. Please fix cleaner configuration! Skipping second run...", baseDn);
                        continue;
                    }

                    processedBaseDns.add(processedBaseDn);

                    log.debug("Start clean up for baseDn: " + baseDn.getValue() + ", class: " + baseDn.getValue());
                    final Stopwatch started = Stopwatch.createStarted();

                    int removed = cleanup(baseDn, now, chunkSize);

                    log.debug("Finished clean up for baseDn: {}, takes: {}ms, removed items: {}", baseDn, started.elapsed(TimeUnit.MILLISECONDS), removed);
                } catch (Exception e) {
                    log.error("Failed to process clean up for baseDn: " + baseDn + ", class: " + baseDn.getValue(), e);
                }
            }

            processCache(now);

            this.lastFinishedTime = System.currentTimeMillis();
		} catch (Exception e) {
			log.error("Failed to process clean up.", e);
		}
	}

    private Map<String, Class<?>> createCleanServiceBaseDns() {
        final Map<String, Class<?>> cleanServiceBaseDns = Maps.newHashMap();

        cleanServiceBaseDns.put(passwordResetService.getDnForPasswordResetRequest(null), PasswordResetRequest.class);
        cleanServiceBaseDns.put(metricService.buildDn(null, null, ApplicationType.OX_TRUST), MetricEntry.class);

        return cleanServiceBaseDns;
    }

    public int cleanup(final Map.Entry<String, Class<?>> baseDn, final Date now, final int batchSize) {
		cleanUpLogger.addNewLogLine("+Starting " + baseDn.getKey() + " clean up at:" + new Date());
        try {
            Filter filter = Filter.createANDFilter(
                    Filter.createEqualityFilter("del", true),
                    Filter.createLessOrEqualFilter("exp", entryManager.encodeTime(baseDn.getKey(), now)));

            int removedCount = entryManager.remove(baseDn.getKey(), baseDn.getValue(), filter, batchSize);
            log.trace("Removed " + removedCount + " entries from " + baseDn.getKey());
            return removedCount;
        } catch (Exception e) {
            log.error("Failed to perform clean up.", e);
        }
		cleanUpLogger.addNewLogLine("-Finished " + baseDn.getKey() + " clean up at:" + new Date());

        return 0;
    }

	private void processCache(Date now) {
		cleanUpLogger.addNewLogLine("~Starting processing cache at:" + now);
		try {
            cacheProvider.cleanup(now);
		} catch (Exception e) {
			log.error("Failed to clean up cache.", e);
			cleanUpLogger.addNewLogLineAsError("~Error occurs while processing cache");
			cleanUpLogger.addNewLogLineAsError("~Error message: " + e.getMessage());
		}
		cleanUpLogger.addNewLogLine("~Processing cache done at:" + new Date());
	}

	private boolean isStartProcess() {
		int interval = appConfiguration.getCleanServiceInterval();
		if (interval < 0) {
			log.info("Cleaner Timer is disabled.");
			log.warn("Cleaner Timer Interval (cleanServiceInterval in oxauth configuration) is negative which turns OFF internal clean up by the server. Please set it to positive value if you wish internal clean up timer run.");
			return false;
		}

		long cleaningInterval = interval * 1000;

		long timeDiffrence = System.currentTimeMillis() - this.lastFinishedTime;

		return timeDiffrence >= cleaningInterval;
	}

}
