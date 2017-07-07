/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import org.xdi.service.cdi.async.Asynchronous;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.model.GluuSAMLTrustRelationship;
import org.gluu.oxtrust.model.SubversionFile;
import org.gluu.oxtrust.service.cdi.event.SvnSyncEvent;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.service.cdi.event.Scheduled;
import org.xdi.service.timer.event.TimerEvent;
import org.xdi.service.timer.schedule.TimerSchedule;
import org.xdi.util.StringHelper;

@ApplicationScoped
@Named
public class SvnSyncTimer implements Serializable {

	private static final long serialVersionUID = -6875538084008069405L;

	private final static int DEFAULT_INTERVAL = 5 * 60; // 5 minutes

	@Inject
	private Logger log;

	@Inject
	private Event<TimerEvent> timerEvent;

	@Inject
	private Shibboleth3ConfService shibboleth3ConfService;

	@Inject
	private SubversionService subversionService;
	
	@Inject
	private OrganizationService organizationService;

	@Inject
	private AppConfiguration appConfiguration;

	private LinkedBlockingQueue<Pair<GluuSAMLTrustRelationship, String>> removedTrustRelationship;

	private String svnComment = "";

	List<Pair<GluuSAMLTrustRelationship, String>> alteredTrustRelations;

	@Inject
	private TrustService trustService;

	private AtomicBoolean isActive;

	@PostConstruct
	public void init() {
		this.isActive = new AtomicBoolean(true);
		try {
			this.removedTrustRelationship = new LinkedBlockingQueue<Pair<GluuSAMLTrustRelationship, String>>();
			this.alteredTrustRelations = new ArrayList<Pair<GluuSAMLTrustRelationship, String>>();
		} finally {
			this.isActive.set(false);
		}
	}

	public void initTimer() {
		log.debug("Initializing SVN Sync Timer");

		if (!appConfiguration.isPersistSVN()) {
			return;
		}

		final int delay = 30;
		final int interval = DEFAULT_INTERVAL;

		timerEvent.fire(new TimerEvent(new TimerSchedule(delay, interval), new SvnSyncEvent(),
				Scheduled.Literal.INSTANCE));
	}

	@Asynchronous
	public void processSvnSyncTimerEvent(@Observes @Scheduled SvnSyncEvent svnSyncEvent) {
		if (this.isActive.get()) {
			return;
		}

		if (!this.isActive.compareAndSet(false, true)) {
			return;
		}

		try {
			processSvnSync();
		} catch (Throwable ex) {
			log.error("Exception happened while running SVN sync", ex);
		} finally {
			this.isActive.set(false);
		}
	}

	private void processSvnSync() {
		commitShibboleth3Configuration(trustService.getAllActiveTrustRelationships());
	}

	private void commitShibboleth3Configuration(List<GluuSAMLTrustRelationship> trustRelationships) {
		synchronized (this) {
			List<SubversionFile> subversionFiles = new ArrayList<SubversionFile>();
			try {
				subversionFiles = subversionService.getDifferentFiles(shibboleth3ConfService
						.getConfigurationFilesForSubversion(trustRelationships));
			} catch (IOException e) {
				log.error("Failed to prepare files list to be persisted in svn", e);
			}

			List<SubversionFile> removeSubversionFiles = new ArrayList<SubversionFile>();
			while (!removedTrustRelationship.isEmpty()) {
				Pair<GluuSAMLTrustRelationship, String> removedRelationship = removedTrustRelationship.poll();

				SubversionFile file = shibboleth3ConfService.getConfigurationFileForSubversion(removedRelationship.getValue0());
				if (file != null) {
					removeSubversionFiles.add(file);
				}
			}
			String idpSvnComment = "";
			// Find all TRs modified not by user.
			for (SubversionFile file : subversionFiles) {
				String filename = file.getLocalFile();
				if (filename.matches(".*/DA[0-9A-F]*-sp-metadata\\.xml")) {
					boolean found = false;
					String inum = filename.replaceAll("-sp-metadata\\.xml", "").replaceAll(".*/", "");
					for (Pair<GluuSAMLTrustRelationship, String> trust : alteredTrustRelations) {
						if (StringHelper.removePunctuation(trust.getValue0().getInum()).equals(inum)) {
							found = true;
							break;
						}
					}

					if (!found) {
						GluuSAMLTrustRelationship unknownTrust = trustService.getTrustByUnpunctuatedInum(inum);
						if (unknownTrust != null) {
							idpSvnComment += "Trust relationship '" + unknownTrust.getDisplayName() + "' was updated automatically\n";
						} else {
							idpSvnComment += "Appliance have no information about  '" + filename
									+ "'. Please report this issue to appliance admin.\n";
						}
					}
				}
			}
			log.debug("Files to be persisted in repository: " + StringHelper.toString(subversionFiles.toArray(new SubversionFile[] {})));
			log.debug("Files to be removed from repository: "
					+ StringHelper.toString(removeSubversionFiles.toArray(new SubversionFile[] {})));
			if (!subversionService.commitShibboleth3ConfigurationFiles(organizationService.getOrganization(), subversionFiles,
					removeSubversionFiles, svnComment + idpSvnComment)) {
				log.error("Failed to commit Shibboleth3 configuration to SVN repository");
			} else {
				svnComment = "";
				alteredTrustRelations.clear();
				log.info("Shibboleth3 configuration commited successfully to SVN repository");
			}
		}
	}

	public void removeTrustRelationship(GluuSAMLTrustRelationship trustRelationship, String user) throws InterruptedException {
		removedTrustRelationship.put(new Pair<GluuSAMLTrustRelationship, String>(trustRelationship, user));
		alteredTrustRelations.add(new Pair<GluuSAMLTrustRelationship, String>(trustRelationship, user));
		svnComment += "User " + user + " removed trust relationship " + trustRelationship.getDisplayName() + "\n";
	}

	public void addTrustRelationship(GluuSAMLTrustRelationship trustRelationship, String user) {
		alteredTrustRelations.add(new Pair<GluuSAMLTrustRelationship, String>(trustRelationship, user));
		svnComment += "User " + user + " added trust relationship " + trustRelationship.getDisplayName() + "\n";
	}

	public void updateTrustRelationship(GluuSAMLTrustRelationship trustRelationship, String user) {
		alteredTrustRelations.add(new Pair<GluuSAMLTrustRelationship, String>(trustRelationship, user));
		svnComment += "User " + user + " updated trust relationship " + trustRelationship.getDisplayName() + "\n";
	}
}
