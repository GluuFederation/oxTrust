package org.gluu.oxtrust.service;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.oxtrust.model.GluuSAMLTrustRelationship;
import org.slf4j.Logger;

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
	private TrustService trustService;
	
	@Inject
	private SamlAcrService samlAcrService;
	
	@Inject
	private Shibboleth3ConfService shibboleth3ConfService;

	@Inject
	private ShibbolethReloadService shibbolethReloadService;
	
	public boolean createShibbolethConfiguration() {
		boolean createConfig = appConfiguration.isConfigGeneration();
		log.info("IDP config generation is set to " + createConfig);
		if (createConfig) {
			List<GluuSAMLTrustRelationship> trustRelationships = trustService.getAllActiveTrustRelationships();
			String shibbolethVersion = appConfiguration.getShibbolethVersion();
			log.info("########## shibbolethVersion = " + shibbolethVersion);
			shibboleth3ConfService.generateMetadataFiles();
			shibboleth3ConfService.generateConfigurationFiles(trustRelationships);
			shibboleth3ConfService.generateConfigurationFiles(samlAcrService.getAll());
			if(shibboleth3ConfService.generateGluuAttributeRulesFile()) {
				if(!shibbolethReloadService.reloadAttributeRegistryService()) {
					log.error("Shibboleth attribute registry reload failed. (kindly restart service manually)");
				}
			}
		}

		return true;
	}

}
