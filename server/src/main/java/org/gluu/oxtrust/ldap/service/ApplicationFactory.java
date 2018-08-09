/*
 * oxAuth is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.config.ConfigurationFactory;
import org.gluu.oxtrust.config.ConfigurationFactory.PersistenceConfiguration;
import org.apache.commons.lang.StringUtils;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.persist.PersistenceEntryManagerFactory;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.model.SmtpConfiguration;
import org.xdi.service.cache.CacheConfiguration;
import org.xdi.service.cache.InMemoryConfiguration;

/**
 * Holds factory methods to create services
 *
 * @author Yuriy Movchan Date: 02/14/2017
 */
@ApplicationScoped
@Named
public class ApplicationFactory {

    @Inject
    private Logger log;

    @Inject
    private ConfigurationFactory configurationFactory;

    @Inject
    private ApplianceService applianceService;

    @Inject
    private Instance<PersistenceEntryManagerFactory> persistenceEntryManagerFactoryInstance;

    @Inject
    private AppConfiguration appConfiguration;

    public static final String PERSISTENCE_ENTRY_MANAGER_FACTORY_NAME = "persistenceEntryManagerFactory";

    public static final String PERSISTENCE_ENTRY_MANAGER_NAME = "persistenceEntryManager";

    public static final String PERSISTENCE_CENTRAL_ENTRY_MANAGER_NAME = "centralPersistenceEntryManager";


    @Produces @ApplicationScoped
   	public CacheConfiguration getCacheConfiguration() {
   		CacheConfiguration cacheConfiguration = applianceService.getAppliance().getCacheConfiguration();
   		if (cacheConfiguration == null || cacheConfiguration.getCacheProviderType() == null) {
   			log.error("Failed to read cache configuration from LDAP. Please check appliance oxCacheConfiguration attribute " +
   					"that must contain cache configuration JSON represented by CacheConfiguration.class. Applieance DN: " + applianceService.getAppliance().getDn());
   			log.info("Creating fallback IN-MEMORY cache configuration ... ");

   			cacheConfiguration = new CacheConfiguration();
   			cacheConfiguration.setInMemoryConfiguration(new InMemoryConfiguration());

   			log.info("IN-MEMORY cache configuration is created.");
   		} else if (cacheConfiguration.getNativePersistenceConfiguration() != null) {
			cacheConfiguration.getNativePersistenceConfiguration().setBaseDn(appConfiguration.getBaseDN());
		}
   		log.info("Cache configuration: " + cacheConfiguration);
   		return cacheConfiguration;
   	}

	@Produces @RequestScoped
	public SmtpConfiguration getSmtpConfiguration() {
		GluuAppliance appliance = applianceService.getAppliance();
		SmtpConfiguration smtpConfiguration = appliance.getSmtpConfiguration();
		
		if (smtpConfiguration == null) {
			return new SmtpConfiguration();
		}

		applianceService.decryptSmtpPassword(smtpConfiguration);

		return smtpConfiguration;
	}

    public PersistenceEntryManagerFactory getPersistenceEntryManagerFactory() {
        PersistenceConfiguration persistenceConfiguration = configurationFactory.getPersistenceConfiguration();
        PersistenceEntryManagerFactory persistenceEntryManagerFactory = persistenceEntryManagerFactoryInstance
                .select(persistenceConfiguration.getEntryManagerFactoryType()).get();

        return persistenceEntryManagerFactory;
    }

}