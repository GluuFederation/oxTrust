/**
 * 
 */
package org.gluu.oxtrust.ldap.service;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingQueue;

import org.gluu.oxtrust.config.OxTrustConfiguration;
import org.gluu.oxtrust.model.GluuSAMLTrustRelationship;
import org.gluu.oxtrust.model.GluuValidationStatus;
import org.gluu.oxtrust.util.GluuErrorHandler;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.Expiration;
import org.jboss.seam.annotations.async.IntervalDuration;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.jboss.seam.log.Log;
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.ldap.model.GluuStatus;
import org.xdi.util.StringHelper;

/**
 * @author �Oleksiy Tataryn�
 * 
 */
@AutoCreate
@Scope(ScopeType.APPLICATION)
@Name("metadataValidationTimer")
public class MetadataValidationTimer {

	@Logger
	Log log;

	@In
	private OxTrustConfiguration oxTrustConfiguration;

	private static LinkedBlockingQueue<String> metadataUpdates = new LinkedBlockingQueue<String>();

	public static void queue(String fileName) {
		synchronized (metadataUpdates) {
			metadataUpdates.add(fileName);
		}
	}

	public static boolean isQueued(String gluuSAMLspMetaDataFN) {
		synchronized (metadataUpdates) {
			for (String filename : metadataUpdates) {
				if (filename.contains(gluuSAMLspMetaDataFN)) {
					return true;
				}
			}
			return false;
		}
	}

	@Asynchronous
	public QuartzTriggerHandle scheduleValidation(@Expiration Date when, @IntervalDuration Long interval) {
		process(when, interval);
		return null;
	}

	private void process(Date when, Long interval) {
		log.debug("Starting metadata validation");
		ApplicationConfiguration applicationConfiguration = oxTrustConfiguration.getApplicationConfiguration();
		validateMetadata(applicationConfiguration.getShibboleth2IdpRootDir() + File.separator
				+ Shibboleth2ConfService.SHIB2_IDP_TEMPMETADATA_FOLDER + File.separator, applicationConfiguration
				.getShibboleth2IdpRootDir() + File.separator + Shibboleth2ConfService.SHIB2_IDP_METADATA_FOLDER + File.separator);
		log.debug("Metadata validation finished");
	}

	/**
	 * @param shib2IdpTempmetadataFolder
	 * @param shib2IdpMetadataFolder
	 */
	private void validateMetadata(String shib2IdpTempmetadataFolder, String shib2IdpMetadataFolder) {
		log.trace("Starting metadata validation process.");
		ApplicationConfiguration applicationConfiguration = oxTrustConfiguration.getApplicationConfiguration();

		String metadataFN = null;
		synchronized (metadataUpdates) {
			if (!metadataUpdates.isEmpty()) {
				metadataFN = metadataUpdates.poll();
			}
		}
		synchronized (this) {
			if (StringHelper.isNotEmpty(metadataFN)) {
				File metadata = new File(shib2IdpTempmetadataFolder + metadataFN);
				File target = new File(shib2IdpMetadataFolder + metadataFN.replaceAll(".{4}\\..{4}$", ""));
				GluuSAMLTrustRelationship tr = TrustService.instance().getTrustByUnpunctuatedInum(
						metadataFN.split("-" + Shibboleth2ConfService.SHIB2_IDP_SP_METADATA_FILE)[0]);
				if (tr == null) {
					metadataUpdates.add(metadataFN);
					return;
				}
				tr.setValidationStatus(GluuValidationStatus.VALIDATION);
				TrustService.instance().updateTrustRelationship(tr);

				GluuErrorHandler handler = null;
				List<String> validationLog = null;
				try {
					handler = Shibboleth2ConfService.instance().validateMetadata(new FileInputStream(metadata));
				} catch (Exception e) {
					tr.setValidationStatus(GluuValidationStatus.VALIDATION_FAILED);
					tr.setStatus(GluuStatus.INACTIVE);
					validationLog = new ArrayList<String>();
					validationLog.add(e.getMessage());
					log.warn("Validation of " + tr.getInum() + " failed: " + e.getMessage() );
					tr.setValidationLog(validationLog);
					TrustService.instance().updateTrustRelationship(tr);
					return;
				}
				if (handler.isValid()) {
					tr.setValidationLog(handler.getLog());
					tr.setValidationStatus(GluuValidationStatus.VALIDATION_SUCCESS);
					if (((!target.exists()) || target.delete()) && (!metadata.renameTo(target))) {
						log.error("Failed to move metadata file to location:" + target.getAbsolutePath());
						tr.setStatus(GluuStatus.INACTIVE);
					} else {
						tr.setSpMetaDataFN(metadataFN.replaceAll(".{4}\\..{4}$", ""));
					}
					boolean federation = TrustService.instance().isFederation(tr);
					tr.setFederation(federation);
					String idpMetadataFolder = applicationConfiguration.getShibboleth2IdpRootDir() + File.separator
							+ Shibboleth2ConfService.SHIB2_IDP_METADATA_FOLDER + File.separator;
					File metadataFile = new File(idpMetadataFolder + tr.getSpMetaDataFN());
					
					
					List<String> entityIdList = Shibboleth2ConfService.instance().getEntityIdFromMetadataFile(metadataFile);
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

					TrustService.instance().updateTrustRelationship(tr);

				}else if(applicationConfiguration.isIgnoreValidation()){
					tr.setValidationLog(new ArrayList<String>(new HashSet<String>(handler.getLog())));
					tr.setValidationStatus(GluuValidationStatus.VALIDATION_FAILED);
					if( (( ! target.exists() ) ||  target.delete()) && ( ! metadata.renameTo(target) )){
						log.error("Failed to move metadata file to location:" + target.getAbsolutePath());
						tr.setStatus(GluuStatus.INACTIVE);
					}else{
						tr.setSpMetaDataFN(metadataFN.replaceAll("....$", ""));
					}
					boolean federation = TrustService.instance().isFederation(tr);
					tr.setFederation(federation);
					String idpMetadataFolder = applicationConfiguration.getShibboleth2IdpRootDir() + File.separator + Shibboleth2ConfService.SHIB2_IDP_METADATA_FOLDER + File.separator;
					File metadataFile = new File(idpMetadataFolder + tr.getSpMetaDataFN());
					
					List<String> entityIdList = Shibboleth2ConfService.instance().getEntityIdFromMetadataFile(metadataFile);
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
					TrustService.instance().updateTrustRelationship(tr);
				} else {
					tr.setValidationLog(new ArrayList<String>(new HashSet<String>(handler.getLog())));
					tr.setValidationStatus(GluuValidationStatus.VALIDATION_FAILED);
					tr.setStatus(GluuStatus.INACTIVE);
					TrustService.instance().updateTrustRelationship(tr);
				}
			}
		}
	}
}
