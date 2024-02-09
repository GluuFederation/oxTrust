/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections.CollectionUtils;
import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.model.GluuStatus;
import org.gluu.oxtrust.model.GluuSAMLTrustRelationship;
import org.gluu.oxtrust.model.GluuValidationStatus;
import org.gluu.oxtrust.service.cdi.event.EntityIdMonitoringEvent;
import org.gluu.oxtrust.util.ServiceUtil;
import org.gluu.saml.metadata.SAMLMetadataParser;
import org.gluu.service.cdi.async.Asynchronous;
import org.gluu.service.cdi.event.Scheduled;
import org.gluu.service.timer.event.TimerEvent;
import org.gluu.service.timer.schedule.TimerSchedule;
import org.slf4j.Logger;

/**
 * @author otataryn
 * 
 */
@ApplicationScoped
@Named("entityIDMonitoringService")
public class EntityIDMonitoringService {

	private static final String ENTITY_ID_VANISHED_MESSAGE = "Invalidated because parent federation does not contain this entityId any more.";
	private static final String FEDERATION_FILE_INVALID_MESSAGE = "The metadata of this federation is invalid, hence all TRs based on this federation are invalidated.";

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
	
	@Inject
	private Shibboleth3ConfService shibboleth3ConfService;
	
	@Inject
	private SAMLMetadataParser samlMetadataParser;

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
			boolean isConfigGeneration = appConfiguration.isConfigGeneration();
			if(isConfigGeneration) {
				process();
			}else {
				log.debug("EntityID monitoring config generation disabled");
			}
		} catch (Throwable ex) {
			log.error("Exception happened while monitoring EntityId", ex);
			ex.printStackTrace();
		} finally {
			this.isActive.set(false);
		}
	}

	public void process() {
		log.trace("Starting entityId monitoring process.");
		log.trace("EVENT_METADATA_ENTITY_ID_UPDATE Starting");
		for (GluuSAMLTrustRelationship tr : trustService.getAllTrustRelationships().stream()
				.filter(e -> e.isFederation()).filter(e -> !e.isMdqFederation()).collect(Collectors.toList())) {
			log.info("==========================CURRENT TR " + tr.getInum());
			String idpMetadataFolder = shibboleth3ConfService.getIdpMetadataDir();
			String metadataFile = idpMetadataFolder + tr.getSpMetaDataFN();
			List<String> entityIds = samlMetadataParser.getEntityIdFromMetadataFile(metadataFile);
			Set<String> fromFileEntityIds = new HashSet<String>(entityIds);
			if (fromFileEntityIds != null && !fromFileEntityIds.isEmpty()) {
				log.trace("EntityIds from metadata: " + serviceUtil.iterableToString(entityIds));
				log.trace("Unique entityIds: " + serviceUtil.iterableToString(fromFileEntityIds));
				Collection<String> disjunction = CollectionUtils.disjunction(fromFileEntityIds, tr.getGluuEntityId());
				log.trace("EntityIds disjunction: " + serviceUtil.iterableToString(disjunction));
				if (!disjunction.isEmpty()) {
					log.trace("EntityIds disjunction is not empty. Somthing has changed. Processing further.");
					tr.setUniqueGluuEntityId(fromFileEntityIds);
					List<GluuSAMLTrustRelationship> federatedTrs = trustService.getChildTrusts(tr);
					for (GluuSAMLTrustRelationship federatedTr : federatedTrs) {
						log.trace("Processing TR part: " + federatedTr.getDn());
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
							federatedTr.setValidationStatus(GluuValidationStatus.SUCCESS);
							List<String> log = federatedTr.getValidationLog();
							List<String> updatedLog = new ArrayList<String>(log);
							updatedLog.remove(ENTITY_ID_VANISHED_MESSAGE + " : " + federatedTr.getEntityId());
							if (updatedLog.isEmpty()) {
								updatedLog = null;
							}
							federatedTr.setValidationLog(updatedLog);
						} else {
							if (federatedTr.getValidationStatus().equals(GluuValidationStatus.FAILED)) {
								federatedTr.setStatus(GluuStatus.ACTIVE);
								federatedTr.setValidationStatus(GluuValidationStatus.SUCCESS);
							}
						}
						trustService.updateTrustRelationship(federatedTr);
					}
					tr.setStatus(GluuStatus.ACTIVE);
					tr.setValidationStatus(GluuValidationStatus.SUCCESS);
					trustService.updateTrustRelationship(tr);
				} else {
					if (tr.getStatus().equals(GluuStatus.INACTIVE)) {
						tr.setStatus(GluuStatus.ACTIVE);
						tr.setValidationStatus(GluuValidationStatus.SUCCESS);
						if (tr.getValidationLog() != null && !tr.getValidationLog().isEmpty()) {
							List<String> validationLog = new ArrayList<>(tr.getValidationLog());
							validationLog.remove(FEDERATION_FILE_INVALID_MESSAGE);
							tr.setValidationLog(validationLog);
						}
						List<GluuSAMLTrustRelationship> federatedTrs = trustService.getChildTrusts(tr);
						if (federatedTrs != null && !federatedTrs.isEmpty()) {
							for (GluuSAMLTrustRelationship child : federatedTrs) {
								child.setValidationStatus(GluuValidationStatus.SUCCESS);
								child.setStatus(GluuStatus.ACTIVE);
								trustService.updateTrustRelationship(child);
							}
						}
						trustService.updateTrustRelationship(tr);
					}
				}
			} else {
				tr.setStatus(GluuStatus.INACTIVE);
				tr.setValidationStatus(GluuValidationStatus.FAILED);
				if (tr.getValidationLog() != null && !tr.getValidationLog().contains(FEDERATION_FILE_INVALID_MESSAGE)) {
					List<String> validationLog = new ArrayList<>(tr.getValidationLog());
					validationLog.add(FEDERATION_FILE_INVALID_MESSAGE);
					tr.setValidationLog(validationLog);
				} else {
					tr.setValidationLog(Arrays.asList(FEDERATION_FILE_INVALID_MESSAGE));
				}
				List<GluuSAMLTrustRelationship> federatedTrs = trustService.getChildTrusts(tr);
				if (federatedTrs != null && !federatedTrs.isEmpty()) {
					for (GluuSAMLTrustRelationship child : federatedTrs) {
						child.setValidationStatus(GluuValidationStatus.FAILED);
						child.setStatus(GluuStatus.INACTIVE);
						trustService.updateTrustRelationship(child);
					}
				}
				trustService.updateTrustRelationship(tr);
			}

		}
	}

}
