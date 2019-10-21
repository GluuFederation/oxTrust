/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

import org.xdi.service.cdi.async.Asynchronous;
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
import org.xdi.ldap.model.GluuStatus;
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
	private static final String FEDERATION_FILE_INVALID_MESSAGE = "The metadata of this federation is invalid, hence all TRs based on this federation are invalidated.";

	private static final int DEFAULT_INTERVAL = 5 * 60; // 2 minutes

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
		log.info("#######################################################################");
		for (GluuSAMLTrustRelationship tr : trustService.getAllTrustRelationships()) {
			if (tr.isFederation()) {
				String idpMetadataFolder = appConfiguration.getShibboleth3IdpRootDir() + File.separator
						+ Shibboleth3ConfService.SHIB3_IDP_METADATA_FOLDER + File.separator;
				File metadataFile = new File(idpMetadataFolder + tr.getSpMetaDataFN());
				List<String> entityIds = SAMLMetadataParser.getEntityIdFromMetadataFile(metadataFile);
				Set<String> fromFileEntityIds = new HashSet<String>(entityIds);
				if (fromFileEntityIds != null && !fromFileEntityIds.isEmpty()) {
					Collection<String> disjunction = CollectionUtils.disjunction(fromFileEntityIds,
							tr.getGluuEntityId());
					log.info("EntityIds disjunction: " + serviceUtil.iterableToString(disjunction));
					if (!disjunction.isEmpty()) {
						tr.setGluuEntityId(fromFileEntityIds);
						List<GluuSAMLTrustRelationship> federatedTrs = trustService.getChildTrusts(tr);
						for (GluuSAMLTrustRelationship federatedTr : federatedTrs) {
							log.info("Processing TR part: " + federatedTr.getDn());
							boolean isActive = federatedTr.getStatus() != null
									&& GluuStatus.ACTIVE.equals(federatedTr.getStatus());
							log.trace("isActive:" + isActive);
							boolean entityIdPresent = fromFileEntityIds != null
									&& fromFileEntityIds.contains(federatedTr.getEntityId());
							log.trace("entityIdPresent:" + entityIdPresent);
							boolean previouslyDisabled = federatedTr.getValidationLog() != null
									&& federatedTr.getValidationLog()
											.contains(ENTITY_ID_VANISHED_MESSAGE + " : " + federatedTr.getEntityId());
							log.trace("previouslyDisabled:" + previouslyDisabled);
							if (isActive && !entityIdPresent) {
								log.trace("no entityId found for part : " + federatedTr.getDn());
								federatedTr.setStatus(GluuStatus.INACTIVE);
								List<String> log = new ArrayList<String>();
								log.add(ENTITY_ID_VANISHED_MESSAGE + " : " + federatedTr.getEntityId());
								federatedTr.setValidationLog(log);
							} else if (entityIdPresent && previouslyDisabled) {
								log.trace("entityId found for part : " + federatedTr.getDn());
								federatedTr.setStatus(GluuStatus.ACTIVE);
								federatedTr.setValidationStatus(GluuValidationStatus.VALIDATION_SUCCESS);
								List<String> log = federatedTr.getValidationLog();
								List<String> updatedLog = new ArrayList<String>(log);
								updatedLog.remove(ENTITY_ID_VANISHED_MESSAGE + " : " + federatedTr.getEntityId());
								if (updatedLog.isEmpty()) {
									updatedLog = null;
								}
								federatedTr.setValidationLog(updatedLog);
							} else {
								if (federatedTr.getValidationStatus().equals(GluuValidationStatus.VALIDATION_FAILED)) {
									federatedTr.setStatus(GluuStatus.ACTIVE);
									federatedTr.setValidationStatus(GluuValidationStatus.VALIDATION_SUCCESS);
								}
							}
							trustService.updateTrustRelationship(federatedTr);
						}
						tr.setStatus(GluuStatus.ACTIVE);
						tr.setValidationStatus(GluuValidationStatus.VALIDATION_SUCCESS);
						trustService.updateTrustRelationship(tr);

					} else {
						if (tr.getStatus().equals(GluuStatus.INACTIVE)) {
							tr.setStatus(GluuStatus.ACTIVE);
							tr.setValidationStatus(GluuValidationStatus.VALIDATION_SUCCESS);
							if (tr.getValidationLog() != null && !tr.getValidationLog().isEmpty()) {
								List<String> validationLog = new ArrayList<String>(tr.getValidationLog());
								validationLog.remove(FEDERATION_FILE_INVALID_MESSAGE);
								tr.setValidationLog(validationLog);
							}
							List<GluuSAMLTrustRelationship> federatedTrs = trustService.getChildTrusts(tr);
							if (federatedTrs != null && !federatedTrs.isEmpty()) {
								for (GluuSAMLTrustRelationship child : federatedTrs) {
									child.setValidationStatus(GluuValidationStatus.VALIDATION_SUCCESS);
									child.setStatus(GluuStatus.ACTIVE);
									trustService.updateTrustRelationship(child);
								}
							}
							trustService.updateTrustRelationship(tr);
						}
					}

				} else {
					tr.setStatus(GluuStatus.INACTIVE);
					tr.setValidationStatus(GluuValidationStatus.VALIDATION_FAILED);
					if (tr.getValidationLog() != null
							&& !tr.getValidationLog().contains(FEDERATION_FILE_INVALID_MESSAGE)) {
						List<String> validationLog = new ArrayList<String>(tr.getValidationLog());
						validationLog.add(FEDERATION_FILE_INVALID_MESSAGE);
						tr.setValidationLog(validationLog);
					} else {
						tr.setValidationLog(Arrays.asList(FEDERATION_FILE_INVALID_MESSAGE));
					}
					List<GluuSAMLTrustRelationship> federatedTrs = trustService.getChildTrusts(tr);
					if (federatedTrs != null && !federatedTrs.isEmpty()) {
						for (GluuSAMLTrustRelationship child : federatedTrs) {
							child.setValidationStatus(GluuValidationStatus.VALIDATION_FAILED);
							child.setStatus(GluuStatus.INACTIVE);
							trustService.updateTrustRelationship(child);
						}
					}
					trustService.updateTrustRelationship(tr);
				}

			}
		}
	}

}
