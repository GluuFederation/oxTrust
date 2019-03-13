package org.gluu.oxtrust.ldap.service;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.model.GluuSAMLTrustRelationship;
import org.gluu.persist.exception.EntryPersistenceException;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;

/**
 * Perform Shibboleth startup time initialization.
 * 
 * @author Yuriy Movchan
 * @version 0.1, 05/04/2017
 */
@ApplicationScoped
@Named
public class ShibbolethInitializer {
	
	@Inject
	private Logger log;

	@Inject
	private AppConfiguration appConfiguration;
	
	@Inject
	private ApplianceService applianceService;
	
	@Inject
	private TrustService trustService;
	
	@Inject
	private Shibboleth3ConfService shibboleth3ConfService;
	
	public boolean createShibbolethConfiguration() {
		boolean createConfig = appConfiguration.isConfigGeneration();
		log.info("IDP config generation is set to " + createConfig);
		if (createConfig) {
			GluuSAMLTrustRelationship gluuSP;
			try {
				String gluuSPInum = applianceService.getAppliance().getGluuSPTR();
				gluuSP = new GluuSAMLTrustRelationship();
				gluuSP.setDn(trustService.getDnForTrustRelationShip(gluuSPInum));

			} catch (EntryPersistenceException ex) {
				log.error("Failed to determine SP inum", ex);
				return false;
			}
			boolean servicesNeedRestarting = false;
			gluuSP = trustService.getRelationshipByInum(applianceService.getAppliance().getGluuSPTR());
			List<GluuSAMLTrustRelationship> trustRelationships = trustService.getAllActiveTrustRelationships();
			String shibbolethVersion = appConfiguration.getShibbolethVersion();
			log.info("########## shibbolethVersion = " + shibbolethVersion);
			shibboleth3ConfService.generateMetadataFiles(gluuSP);
			shibboleth3ConfService.generateConfigurationFiles(trustRelationships);
			if (servicesNeedRestarting) {
				applianceService.restartServices();
			}
		}

		return true;
	}

}
