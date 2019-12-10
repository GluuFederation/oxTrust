/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.oxtrust.config.ConfigurationFactory;
import org.gluu.oxtrust.model.GluuConfiguration;
import org.gluu.oxtrust.model.GluuOxTrustStat;
import org.gluu.oxtrust.service.cdi.event.StatusCheckerDailyEvent;
import org.gluu.persist.exception.BasePersistenceException;
import org.gluu.service.cdi.async.Asynchronous;
import org.gluu.service.cdi.event.Scheduled;
import org.gluu.service.timer.event.TimerEvent;
import org.gluu.service.timer.schedule.TimerSchedule;
import org.slf4j.Logger;

@ApplicationScoped
@Named("statusCheckerDaily")
public class StatusCheckerDaily {

	// Group count and person count will now be checked daily
	public static final int DEFAULT_INTERVAL = 60 * 60 * 24;

	@Inject
	private Logger log;

	@Inject
	private Event<TimerEvent> timerEvent;

	@Inject
	private ConfigurationService configurationService;

	@Inject
	private IGroupService groupService;

	@Inject
	private IPersonService personService;

	@Inject
	private CentralLdapService centralLdapService;

	@Inject
	private ConfigurationFactory configurationFactory;

	private AtomicBoolean isActive;

	public void initTimer() {
		log.info("Initializing Daily Status Cheker Timer");
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
		log.debug("Starting daily status checker");
		AppConfiguration appConfiguration = configurationFactory.getAppConfiguration();
		if (!appConfiguration.isUpdateStatus()) {
			return;
		}

		log.debug("Getting data from ldap");
		int groupCount = groupService.countGroups();
		int personCount = personService.countPersons();

		GluuConfiguration configuration = configurationService.getConfiguration();
		GluuOxTrustStat serverDetail = configurationService.getOxtrustStat();

		log.debug("Setting ldap attributes");
		serverDetail.setGroupCount(String.valueOf(groupCount));
		serverDetail.setPersonCount(String.valueOf(personCount));
		Date currentDateTime = new Date();
		configuration.setLastUpdate(currentDateTime);

		configurationService.updateConfiguration(configuration);
		configurationService.updateServerDetail(serverDetail);

		if (centralLdapService.isUseCentralServer()) {
			try {
				boolean existConfiguration = centralLdapService.containsConfiguration(configuration.getDn());

				if (existConfiguration) {
					centralLdapService.updateConfiguration(configuration);
				} else {
					centralLdapService.addConfiguration(configuration);
				}
			} catch (BasePersistenceException ex) {
				log.error("Failed to update configuration at central server", ex);
				return;
			}
		}

		log.debug("Daily Configuration status update finished");
	}

}
