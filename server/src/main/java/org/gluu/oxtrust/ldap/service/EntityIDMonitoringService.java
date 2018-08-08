/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections.CollectionUtils;
import org.gluu.oxtrust.model.GluuSAMLTrustRelationship;
import org.gluu.oxtrust.model.GluuValidationStatus;
import org.gluu.oxtrust.service.cdi.event.EntityIdMonitoringEvent;
import org.gluu.oxtrust.util.ServiceUtil;
import org.gluu.saml.metadata.SAMLMetadataParser;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.model.GluuStatus;
import org.xdi.service.cdi.async.Asynchronous;
import org.xdi.service.cdi.event.Scheduled;
import org.xdi.service.timer.event.TimerEvent;
import org.xdi.service.timer.schedule.TimerSchedule;
import org.xdi.util.StringHelper;

/**
 * @author otataryn
 * 
 */
@ApplicationScoped
@Named("entityIDMonitoringService")
public class EntityIDMonitoringService {

	private static final String ENTITY_ID_VANISHED_MESSAGE = "Invalidated because parent federation does not contain this entityId any more.";

	private static final int DEFAULT_INTERVAL = 2 * 60; // 2 minutes

	@Inject
	private Logger log;

	@Inject
	private Event<TimerEvent> timerEvent;

	@Inject
	private AppConfiguration appConfiguration;

	@Inject
	private ServiceUtil serviceUtil;

	@Inject
	private TrustService trustService;

	private AtomicBoolean isActive;

	public void initTimer() {
		log.debug("Initializing EntityId Monitoring Timer");
		this.isActive = new AtomicBoolean(false);

		final int delay = 30;
		final int interval = DEFAULT_INTERVAL;

		timerEvent.fire(new TimerEvent(new TimerSchedule(delay, interval), new EntityIdMonitoringEvent(),
				Scheduled.Literal.INSTANCE));
	}

	@Asynchronous
	public void processMetadataValidationTimerEvent(
			@Observes @Scheduled EntityIdMonitoringEvent entityIdMonitoringEvent) {
		if (this.isActive.get()) {
			return;
		}

		if (!this.isActive.compareAndSet(false, true)) {
			return;
		}

		try {
			process();
		} catch (Throwable ex) {
			log.error("Exception happened while monitoring EntityId", ex);
		} finally {
			this.isActive.set(false);
		}
	}

	public void process() {
		log.trace("Starting entityId monitoring process.");
		log.trace("EVENT_METADATA_ENTITY_ID_UPDATE Starting");
		for (GluuSAMLTrustRelationship tr : trustService.getAllTrustRelationships()) {
			log.trace("Evaluating TR " + tr.getDn());
			boolean meatadataAvailable = tr.getSpMetaDataFN() != null && StringHelper.isNotEmpty(tr.getSpMetaDataFN());
			log.trace("meatadataAvailable:" + meatadataAvailable);
			boolean correctType = trustService.getTrustContainerFederation(tr) == null;
			log.trace("correctType:" + correctType);
			boolean isValidated = GluuValidationStatus.VALIDATION_SUCCESS.equals(tr.getValidationStatus());
			log.trace("isValidated:" + isValidated);
			if (meatadataAvailable && correctType && isValidated) {
				String idpMetadataFolder = appConfiguration.getShibboleth3IdpRootDir() + File.separator
						+ Shibboleth3ConfService.SHIB3_IDP_METADATA_FOLDER + File.separator;
				File metadataFile = new File(idpMetadataFolder + tr.getSpMetaDataFN());
				List<String> entityIds = SAMLMetadataParser.getEntityIdFromMetadataFile(metadataFile);

				log.trace("entityIds from metadata: " + serviceUtil.iterableToString(entityIds));
				Set<String> entityIdSet = new TreeSet<String>();

				if (entityIds != null && !entityIds.isEmpty()) {
					Set<String> duplicatesSet = new TreeSet<String>();
					for (String entityId : entityIds) {
						if (!entityIdSet.add(entityId)) {
							duplicatesSet.add(entityId);
						}
					}
				}

				log.trace("unique entityIds: " + serviceUtil.iterableToString(entityIdSet));
				Collection<String> disjunction = CollectionUtils.disjunction(entityIdSet, tr.getGluuEntityId());
				log.trace("entityIds disjunction: " + serviceUtil.iterableToString(disjunction));

				if (!disjunction.isEmpty()) {
					log.trace("entityIds disjunction is not empty. Somthing has changed. Processing further.");
					tr.setGluuEntityId(entityIdSet);
					if (tr.isFederation()) {
						List<GluuSAMLTrustRelationship> parts = trustService.getDeconstructedTrustRelationships(tr);
						for (GluuSAMLTrustRelationship part : parts) {
							log.trace("Processing TR part: " + part.getDn());
							boolean isActive = part.getStatus() != null && GluuStatus.ACTIVE.equals(part.getStatus());
							log.trace("isActive:" + isActive);
							boolean entityIdPresent = entityIdSet != null && entityIdSet.contains(part.getEntityId());
							log.trace("entityIdPresent:" + entityIdPresent);
							boolean previouslyDisabled = part.getValidationLog() != null && part.getValidationLog()
									.contains(ENTITY_ID_VANISHED_MESSAGE + " : " + part.getEntityId());
							log.trace("previouslyDisabled:" + previouslyDisabled);
							if (isActive && !entityIdPresent) {
								log.trace("no entityId found for part : " + part.getDn());
								part.setStatus(GluuStatus.INACTIVE);
								List<String> log = new ArrayList<String>();
								log.add(ENTITY_ID_VANISHED_MESSAGE + " : " + part.getEntityId());
								part.setValidationLog(log);
								trustService.updateTrustRelationship(part);
							}
							if (entityIdPresent && previouslyDisabled) {
								log.trace("entityId found for part : " + part.getDn());
								part.setStatus(GluuStatus.ACTIVE);
								List<String> log = part.getValidationLog();
								List<String> updatedLog = new ArrayList<String>(log);
								updatedLog.remove(ENTITY_ID_VANISHED_MESSAGE + " : " + part.getEntityId());
								if (updatedLog.isEmpty()) {
									updatedLog = null;
								}
								part.setValidationLog(updatedLog);
								trustService.updateTrustRelationship(part);
							}
						}
					}

					trustService.updateTrustRelationship(tr);
				}
			}
		}
	}

}
