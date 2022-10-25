package org.gluu.oxtrust.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AgeFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.time.DateUtils;
import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.oxtrust.service.cdi.event.StatusCheckerDailyEvent;
import org.gluu.oxtrust.service.config.ConfigurationFactory;
import org.gluu.service.cdi.async.Asynchronous;
import org.gluu.service.cdi.event.Scheduled;
import org.gluu.service.timer.event.TimerEvent;
import org.gluu.service.timer.schedule.TimerSchedule;
import org.slf4j.Logger;

@ApplicationScoped
@Named("cleanUpFilesDaily")
public class CleanUpFilesDaily {

	public static final int DEFAULT_INTERVAL = 60 * 60 * 24;

	@Inject
	private Logger log;

	@Inject
	private Event<TimerEvent> timerEvent;

	@Inject
	private ConfigurationFactory configurationFactory;

	private AtomicBoolean isActive;

	public void initTimer() {
		log.info("Initializing Clean Up Files Daily");
		this.isActive = new AtomicBoolean(false);

		final int delay = 1 * 60;
		final int interval = DEFAULT_INTERVAL;

		timerEvent.fire(new TimerEvent(new TimerSchedule(delay, interval), new StatusCheckerDailyEvent(),
				Scheduled.Literal.INSTANCE));
	}

	@Asynchronous
	public void process(@Observes @Scheduled StatusCheckerDailyEvent statusCheckerDailyEvent) {
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

	/**
	 * Gather periodically site and server status
	 * 
	 * @param when
	 *            Date
	 * @param interval
	 *            Interval
	 */
	private void processInt() {
		log.info("Starting CleanUp Files Daily Job..................");
		AppConfiguration appConfiguration = configurationFactory.getAppConfiguration();
		String filePath = appConfiguration.getLdifStore();
		int removeFilesOlderDays = appConfiguration.getKeepLdifStoreHistoryDays();
		if(removeFilesOlderDays > 0){
			deleteOldFiles(filePath, removeFilesOlderDays);
		}
		
		log.info("Daily CleanUp Files Job finished...................");
	}
	
	private void deleteOldFiles(String Path, int removeFilesOlderDays) {
		log.debug("inside deleteOldFiles---------------");
		if (Files.exists(Paths.get(Path))) {
			Date oldestAllowedFileDate = DateUtils.addDays(new Date(), -removeFilesOlderDays);
			File targetDir = new File(Path);
			Collection<File> filesToDelete = FileUtils.listFiles(targetDir, new AgeFileFilter(oldestAllowedFileDate),
					TrueFileFilter.TRUE);
			log.debug("Today's file count to delete  " + filesToDelete.size());
			for (File file : filesToDelete) {
				log.debug("Deleting file:  : " + file.getName());
				FileUtils.deleteQuietly(file);
			}
		}
	}

}
