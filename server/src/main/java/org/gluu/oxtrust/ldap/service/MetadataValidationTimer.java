/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.model.GluuSAMLTrustRelationship;
import org.gluu.oxtrust.model.GluuValidationStatus;
import org.gluu.oxtrust.service.cdi.event.MetadataValidationEvent;
import org.gluu.saml.metadata.SAMLMetadataParser;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.model.GluuStatus;
import org.xdi.service.cdi.async.Asynchronous;
import org.xdi.service.cdi.event.Scheduled;
import org.xdi.service.timer.event.TimerEvent;
import org.xdi.service.timer.schedule.TimerSchedule;
import org.xdi.util.StringHelper;
import org.xdi.xml.GluuErrorHandler;

/**
 * @author �Oleksiy Tataryn�
 * @author Yuriy Mochan
 * 
 */
@ApplicationScoped
@Named
public class MetadataValidationTimer {

	private final static int DEFAULT_INTERVAL = 60; // 60 seconds

	@Inject
	private Logger log;

	@Inject
	private Event<TimerEvent> timerEvent;
	
	@Inject
	private AppConfiguration appConfiguration;
	
	@Inject
	private TrustService trustService;
	
	@Inject
	private Shibboleth3ConfService shibboleth3ConfService;

	private AtomicBoolean isActive;

	private LinkedBlockingQueue<String> metadataUpdates;

	@PostConstruct
	public void init() {
		this.isActive = new AtomicBoolean(true);
		try {
			this.metadataUpdates = new LinkedBlockingQueue<String>();
		} finally {
			this.isActive.set(false);
		}
	}

	public void initTimer() {
		log.debug("Initializing Metadata Validation Timer");

		final int delay = 30;
		final int interval = DEFAULT_INTERVAL;

		timerEvent.fire(new TimerEvent(new TimerSchedule(delay, interval), new MetadataValidationEvent(),
				Scheduled.Literal.INSTANCE));
	}

	@Asynchronous
	public void processMetadataValidationTimerEvent(@Observes @Scheduled MetadataValidationEvent metadataValidationEvent) {
		if (this.isActive.get()) {
			return;
		}

		if (!this.isActive.compareAndSet(false, true)) {
			return;
		}

		try {
			procesMetadataValidation();
		} catch (Throwable ex) {
			log.error("Exception happened while reloading application configuration", ex);
		} finally {
			this.isActive.set(false);
		}
	}

	private void procesMetadataValidation() {
		log.debug("Starting metadata validation");
		boolean result = validateMetadata(appConfiguration.getShibboleth3IdpRootDir() + File.separator
				+ Shibboleth3ConfService.SHIB3_IDP_TEMPMETADATA_FOLDER + File.separator, appConfiguration
				.getShibboleth3IdpRootDir() + File.separator + Shibboleth3ConfService.SHIB3_IDP_METADATA_FOLDER + File.separator);
		log.debug("Metadata validation finished with result: '{}'", result);
		
		if (result) {
			regenerateConfigurationFiles();
		}
	}

	public void queue(String fileName) {
		synchronized (metadataUpdates) {
			metadataUpdates.add(fileName);
		}
	}

	public boolean isQueued(String gluuSAMLspMetaDataFN) {
		synchronized (metadataUpdates) {
			for (String filename : metadataUpdates) {
				if (filename.contains(gluuSAMLspMetaDataFN)) {
					return true;
				}
			}
			return false;
		}
	}

	private void regenerateConfigurationFiles() {
		boolean createConfig = appConfiguration.isConfigGeneration();
		log.info("IDP config generation is set to " + createConfig);
		
		if (createConfig) {
			List<GluuSAMLTrustRelationship> trustRelationships = trustService.getAllActiveTrustRelationships();
			shibboleth3ConfService.generateConfigurationFiles(trustRelationships);

			log.info("IDP config generation files finished. TR count: '{}'", trustRelationships.size());
		}

	}

	/**
	 * @param shib3IdpTempmetadataFolder
	 * @param shib3IdpMetadataFolder
	 */
	private boolean validateMetadata(String shib3IdpTempmetadataFolder, String shib3IdpMetadataFolder) {
		boolean result = false;
		log.trace("Starting metadata validation process.");

		String metadataFN = null;
		synchronized (metadataUpdates) {
			if (!metadataUpdates.isEmpty()) {
				metadataFN = metadataUpdates.poll();
			}
		}

		synchronized (this) {
			if (StringHelper.isNotEmpty(metadataFN)) {
				File metadata = new File(shib3IdpTempmetadataFolder + metadataFN);
				File target = new File(shib3IdpMetadataFolder + metadataFN.replaceAll(".{4}\\..{4}$", ""));
				GluuSAMLTrustRelationship tr = trustService.getTrustByUnpunctuatedInum(
						metadataFN.split("-" + Shibboleth3ConfService.SHIB3_IDP_SP_METADATA_FILE)[0]);
				if (tr == null) {
					metadataUpdates.add(metadataFN);
					return false;
				}
				tr.setValidationStatus(GluuValidationStatus.VALIDATION);
				trustService.updateTrustRelationship(tr);

				GluuErrorHandler errorHandler = null;
				List<String> validationLog = null;
				try {
					errorHandler = shibboleth3ConfService.validateMetadata(new FileInputStream(metadata));
				} catch (Exception e) {
					tr.setValidationStatus(GluuValidationStatus.VALIDATION_FAILED);
					tr.setStatus(GluuStatus.INACTIVE);
					validationLog = new ArrayList<String>();
					validationLog.add(e.getMessage());
					log.warn("Validation of " + tr.getInum() + " failed: " + e.getMessage() );
					tr.setValidationLog(validationLog);
					trustService.updateTrustRelationship(tr);

					return false;
				}
				if (errorHandler.isValid()) {
					tr.setValidationLog(errorHandler.getLog());
					tr.setValidationStatus(GluuValidationStatus.VALIDATION_SUCCESS);
					if (((!target.exists()) || target.delete()) && (!metadata.renameTo(target))) {
						log.error("Failed to move metadata file to location:" + target.getAbsolutePath());
						tr.setStatus(GluuStatus.INACTIVE);
					} else {
						tr.setSpMetaDataFN(target.getName());
					}
					boolean federation = shibboleth3ConfService.isFederation(tr);
					tr.setFederation(federation);
					String idpMetadataFolder = appConfiguration.getShibboleth3IdpRootDir() + File.separator
							+ Shibboleth3ConfService.SHIB3_IDP_METADATA_FOLDER + File.separator;
					File metadataFile = new File(idpMetadataFolder + tr.getSpMetaDataFN());
					
					
					List<String> entityIdList = SAMLMetadataParser.getEntityIdFromMetadataFile(metadataFile);
					Set<String> entityIdSet = new TreeSet<String>();
					Set<String> duplicatesSet = new TreeSet<String>(); 
					if(entityIdList != null && ! entityIdList.isEmpty()){

						for (String entityId : entityIdList) {
							if (!entityIdSet.add(entityId)) {
								duplicatesSet.add(entityId);
							}
						}
					}

					
					if(! duplicatesSet.isEmpty()){
						validationLog = tr.getValidationLog();
						if(validationLog != null){
							validationLog = new LinkedList<String>(validationLog);
						}else{
							validationLog = new LinkedList<String>();
						}
						validationLog.add("This metadata contains multiple instances of entityId: " + Arrays.toString(duplicatesSet.toArray()));
					}
					tr.setValidationLog(validationLog);
					tr.setGluuEntityId(entityIdSet);
					tr.setStatus(GluuStatus.ACTIVE);

					trustService.updateTrustRelationship(tr);
					result = true;
				} else if(appConfiguration.isIgnoreValidation() || errorHandler.isInternalError()){
					tr.setValidationLog(new ArrayList<String>(new HashSet<String>(errorHandler.getLog())));
					tr.setValidationStatus(GluuValidationStatus.VALIDATION_FAILED);
					if( (( ! target.exists() ) ||  target.delete()) && ( ! metadata.renameTo(target) )){
						log.error("Failed to move metadata file to location:" + target.getAbsolutePath());
						tr.setStatus(GluuStatus.INACTIVE);
					}else{
						tr.setSpMetaDataFN(target.getName());
					}
					boolean federation = shibboleth3ConfService.isFederation(tr);
					tr.setFederation(federation);
					String idpMetadataFolder = appConfiguration.getShibboleth3IdpRootDir() + File.separator + Shibboleth3ConfService.SHIB3_IDP_METADATA_FOLDER + File.separator;
					File metadataFile = new File(idpMetadataFolder + tr.getSpMetaDataFN());
					
					List<String> entityIdList = SAMLMetadataParser.getEntityIdFromMetadataFile(metadataFile);
					Set<String> duplicatesSet = new TreeSet<String>(); 
					Set<String> entityIdSet = new TreeSet<String>();

					for (String entityId : entityIdList) {
						if (!entityIdSet.add(entityId)) {
							duplicatesSet.add(entityId);
						}
					}
					
					tr.setGluuEntityId(entityIdSet);
					tr.setStatus(GluuStatus.ACTIVE);	
					validationLog = tr.getValidationLog();
					if(! duplicatesSet.isEmpty()){
						validationLog.add("This metadata contains multiple instances of entityId: " + Arrays.toString(duplicatesSet.toArray()));
					}
					
                                        if (errorHandler.isInternalError()) {
                                            validationLog = tr.getValidationLog();
                                            
                                            validationLog.add("Warning: cannot validate metadata. Check internet connetion ans www.w3.org availability.");
                                            
                                            // update log with warning
                                            for (String warningLogMessage : errorHandler.getLog()) 
                                                validationLog.add("Warning: " + warningLogMessage);
                                        }
                                        
                                        trustService.updateTrustRelationship(tr);
					result = true;
				} else {
					tr.setValidationLog(new ArrayList<String>(new HashSet<String>(errorHandler.getLog())));
					tr.setValidationStatus(GluuValidationStatus.VALIDATION_FAILED);
					tr.setStatus(GluuStatus.INACTIVE);
					trustService.updateTrustRelationship(tr);
				}
			}
		}

		return result;
	}

}
