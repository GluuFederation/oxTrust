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

import org.apache.commons.collections.CollectionUtils;
import org.gluu.oxtrust.model.GluuSAMLTrustRelationship;
import org.gluu.oxtrust.model.GluuValidationStatus;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.oxtrust.util.Utils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.async.TimerSchedule;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.Log;
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.ldap.model.GluuStatus;
import org.xdi.util.StringHelper;

/**
 * @author otataryn
 * 
 */

@AutoCreate
@Scope(ScopeType.APPLICATION)
@Name("entityIDMonitoringService")
public class EntityIDMonitoringService {

	private static final String ENTITY_ID_VANISHED_MESSAGE = "Invalidated because parent federation does not contain this entityId any more.";

	@Logger
	Log log;

	@In(value = "#{oxTrustConfiguration.applicationConfiguration}")
	private ApplicationConfiguration applicationConfiguration;

	private boolean isActive;

	@Observer("org.jboss.seam.postInitialization")
	public void init() {
		// Schedule to run it every 120 seconds. First event will occur after 30
		// seconds
		Events.instance().raiseTimedEvent(OxTrustConstants.EVENT_METADATA_ENTITY_ID_UPDATE,
				new TimerSchedule(30 * 1000L, AppInitializer.CONNECTION_CHECKER_INTERVAL));
	}

	@Observer(OxTrustConstants.EVENT_METADATA_ENTITY_ID_UPDATE)
	@Asynchronous
	public void process() {
		log.trace("Starting entityId monitoring process.");
		if (this.isActive) {
			log.trace("EVENT_METADATA_ENTITY_ID_UPDATE Active");
			return;
		}
		log.trace("EVENT_METADATA_ENTITY_ID_UPDATE Starting");
		try {
			this.isActive = true;
			for (GluuSAMLTrustRelationship tr : TrustService.instance().getAllTrustRelationships()) {
				log.trace("Evaluating TR " + tr.getDn());
				boolean meatadataAvailable = tr.getSpMetaDataFN() != null && StringHelper.isNotEmpty(tr.getSpMetaDataFN());
				log.trace("meatadataAvailable:" + meatadataAvailable);
				boolean correctType = tr.getContainerFederation() == null;
				log.trace("correctType:" + correctType);
				boolean isValidated = GluuValidationStatus.VALIDATION_SUCCESS.equals(tr.getValidationStatus());
				log.trace("isValidated:" + isValidated);
				if (meatadataAvailable && correctType && isValidated) {
					String idpMetadataFolder = applicationConfiguration.getShibboleth2IdpRootDir() + File.separator
							+ Shibboleth2ConfService.SHIB2_IDP_METADATA_FOLDER + File.separator;
					File metadataFile = new File(idpMetadataFolder + tr.getSpMetaDataFN());
					List<String> entityIds = Shibboleth2ConfService.instance().getEntityIdFromMetadataFile(metadataFile);
					
					log.trace("entityIds from metadata: " + Utils.iterableToString(entityIds)); 
					Set<String> entityIdSet = new TreeSet<String>();
					
					if(entityIds != null && ! entityIds.isEmpty()){
						Set<String> duplicatesSet = new TreeSet<String>(); 
						for (String entityId : entityIds) {
							if (!entityIdSet.add(entityId)) {
								duplicatesSet.add(entityId);
							}
						}
					}

					log.trace("unique entityIds: " + Utils.iterableToString(entityIdSet));
					Collection<String> disjunction = CollectionUtils.disjunction(entityIdSet, tr.getGluuEntityId());
					log.trace("entityIds disjunction: " + Utils.iterableToString(disjunction));
					
					if(! disjunction.isEmpty()){
						log.trace("entityIds disjunction is not empty. Somthing has changed. Processing further.");
						tr.setGluuEntityId(entityIdSet);
						if (tr.isFederation()) {
							List<GluuSAMLTrustRelationship> parts = TrustService.instance().getDeconstructedTrustRelationships(tr);
							for (GluuSAMLTrustRelationship part : parts) {
								log.trace("Processing TR part: " + part.getDn()); 
								boolean isActive = part.getStatus() != null && GluuStatus.ACTIVE.equals(part.getStatus());
								log.trace("isActive:" + isActive);
								boolean entityIdPresent = entityIdSet != null && entityIdSet.contains(part.getEntityId());
								log.trace("entityIdPresent:" + entityIdPresent);
								boolean previouslyDisabled = part.getValidationLog() != null
										&& part.getValidationLog().contains(ENTITY_ID_VANISHED_MESSAGE + " : " + part.getEntityId());
								log.trace("previouslyDisabled:" + previouslyDisabled);
								if (isActive && !entityIdPresent) {
									log.trace("no entityId found for part : " + part.getDn());
									part.setStatus(GluuStatus.INACTIVE);
									List<String> log = new ArrayList<String>();
									log.add(ENTITY_ID_VANISHED_MESSAGE + " : " + part.getEntityId());
									part.setValidationLog(log);
									TrustService.instance().updateTrustRelationship(part);
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
									TrustService.instance().updateTrustRelationship(part);
								}
							}
						}
						
						TrustService.instance().updateTrustRelationship(tr);
					}
				}
			}

		} catch (Throwable ex) {
			log.error("Exception happened while checking LDAP connections", ex);
		} finally {
			this.isActive = false;
		}
	}

}
