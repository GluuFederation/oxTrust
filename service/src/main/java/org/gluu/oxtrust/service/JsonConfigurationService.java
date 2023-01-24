/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.config.oxtrust.AttributeResolverConfiguration;
import org.gluu.config.oxtrust.CacheRefreshConfiguration;
import org.gluu.config.oxtrust.DbApplicationConfiguration;
import org.gluu.config.oxtrust.ImportPersonConfig;
import org.gluu.config.oxtrust.LdapOxAuthConfiguration;
import org.gluu.config.oxtrust.LdapOxTrustConfiguration;
import org.gluu.service.config.ConfigurationFactory;
import org.gluu.oxtrust.model.AuditConfigLogDetails;
import org.gluu.oxtrust.model.GluuConfiguration;
import org.gluu.oxtrust.security.Identity;
import org.gluu.persist.PersistenceEntryManager;
import org.gluu.persist.exception.BasePersistenceException;
import org.gluu.service.JsonService;
import org.gluu.service.cache.CacheConfiguration;
import org.gluu.service.cache.RedisConfiguration;
import org.gluu.service.document.store.conf.DocumentStoreConfiguration;
import org.gluu.util.security.StringEncrypter;
import org.slf4j.Logger;

/**
 * Provides operations with JSON oxAuth/oxTrust configuration
 * 
 * @author Yuriy Movchan Date: 12.15.2010
 */
@ApplicationScoped
@Named("jsonConfigurationService")
public class JsonConfigurationService implements Serializable {

	private static final long serialVersionUID = -3840968275007784641L;

	@Inject
	private Logger log;

	@Inject
	private PersistenceEntryManager persistenceEntryManager;
	@Inject
	private JsonService jsonService;

	@Inject
	private StringEncrypter stringEncrypter;

	@Inject
	private ConfigurationFactory<?> configurationFactory;

	@Inject
	private ConfigurationService configurationService;
	
    @Inject
    private Identity identity;

	public AppConfiguration getOxTrustappConfiguration() {
		LdapOxTrustConfiguration ldapOxTrustConfiguration = getOxTrustConfiguration();
		return ldapOxTrustConfiguration.getApplication();
	}

	public CacheConfiguration getOxMemCacheConfiguration() {
		return configurationService.getConfiguration().getCacheConfiguration();
	}

	public DocumentStoreConfiguration getDocumentStoreConfiguration() {
		return configurationService.getConfiguration().getDocumentStoreConfiguration();
	}

	public ImportPersonConfig getOxTrustImportPersonConfiguration() {
		return getOxTrustConfiguration().getImportPersonConfig();
	}

	public CacheRefreshConfiguration getOxTrustCacheRefreshConfiguration() {
		return getOxTrustConfiguration().getCacheRefresh();
	}

	private LdapOxTrustConfiguration getOxTrustConfiguration() {
		String configurationDn = configurationFactory.getConfigurationDn();
		return loadOxTrustConfig(configurationDn);
	}

	public String getOxAuthDynamicConfigJson() throws IOException {
		String configurationDn = configurationFactory.getConfigurationDn();
		return loadOxAuthConfig(configurationDn).getOxAuthConfigDynamic();
	}

	public org.gluu.oxauth.model.configuration.AppConfiguration getOxauthAppConfiguration() throws IOException {
		return jsonService.jsonToObject(getOxAuthDynamicConfigJson(),
				org.gluu.oxauth.model.configuration.AppConfiguration.class);
	}

	public boolean saveOxTrustappConfiguration(AppConfiguration oxTrustappConfiguration) {
		LdapOxTrustConfiguration ldapOxTrustConfiguration = getOxTrustConfiguration();
		diff( ldapOxTrustConfiguration.getApplication(), oxTrustappConfiguration);
		ldapOxTrustConfiguration.setApplication(oxTrustappConfiguration);
		ldapOxTrustConfiguration.setRevision(ldapOxTrustConfiguration.getRevision() + 1);
		persistenceEntryManager.merge(ldapOxTrustConfiguration);
		return true;
	}

	public boolean saveOxTrustImportPersonConfiguration(ImportPersonConfig oxTrustImportPersonConfiguration) {
		LdapOxTrustConfiguration ldapOxTrustConfiguration = getOxTrustConfiguration();
		diff( ldapOxTrustConfiguration.getImportPersonConfig(), oxTrustImportPersonConfiguration);
		ldapOxTrustConfiguration.setImportPersonConfig(oxTrustImportPersonConfiguration);
		ldapOxTrustConfiguration.setRevision(ldapOxTrustConfiguration.getRevision() + 1);
		persistenceEntryManager.merge(ldapOxTrustConfiguration);
		return true;
	}

	public boolean saveOxTrustCacheRefreshConfiguration(CacheRefreshConfiguration oxTrustCacheRefreshConfiguration) {
		LdapOxTrustConfiguration ldapOxTrustConfiguration = getOxTrustConfiguration();
		diff( ldapOxTrustConfiguration.getCacheRefresh(), oxTrustCacheRefreshConfiguration);
		ldapOxTrustConfiguration.setCacheRefresh(oxTrustCacheRefreshConfiguration);
		ldapOxTrustConfiguration.setRevision(ldapOxTrustConfiguration.getRevision() + 1);
		persistenceEntryManager.merge(ldapOxTrustConfiguration);
		return true;
	}

	public boolean saveOxTrustAttributeResolverConfigurationConfiguration(
			AttributeResolverConfiguration attributeResolverConfiguration) {
		LdapOxTrustConfiguration ldapOxTrustConfiguration = getOxTrustConfiguration();
		diff( ldapOxTrustConfiguration.getAttributeResolverConfig(), attributeResolverConfiguration);
		ldapOxTrustConfiguration.setAttributeResolverConfig(attributeResolverConfiguration);
		ldapOxTrustConfiguration.setRevision(ldapOxTrustConfiguration.getRevision() + 1);
		persistenceEntryManager.merge(ldapOxTrustConfiguration);
		return true;
	}

	public boolean saveOxAuthAppConfiguration(org.gluu.oxauth.model.configuration.AppConfiguration appConfiguration) {
		try {
			diff( getOxauthAppConfiguration(), appConfiguration);
			String appConfigurationJson = jsonService.objectToJson(appConfiguration);
			return saveOxAuthDynamicConfigJson(appConfigurationJson);
		} catch (IOException e) {
			log.error("Failed to serialize AppConfiguration", e);
		}
		return false;
	}

	public boolean saveOxAuthDynamicConfigJson(String oxAuthDynamicConfigJson) throws IOException {
		String configurationDn = configurationFactory.getConfigurationDn();

		LdapOxAuthConfiguration ldapOxAuthConfiguration = loadOxAuthConfig(configurationDn);
		ldapOxAuthConfiguration.setOxAuthConfigDynamic(oxAuthDynamicConfigJson);
		ldapOxAuthConfiguration.setRevision(ldapOxAuthConfiguration.getRevision() + 1);
		persistenceEntryManager.merge(ldapOxAuthConfiguration);
		return true;
	}

	public boolean saveOxMemCacheConfiguration(CacheConfiguration cachedConfiguration) {
		encrypPassword(cachedConfiguration.getRedisConfiguration());
		GluuConfiguration gluuConfiguration = configurationService.getConfiguration();
		diff( gluuConfiguration.getCacheConfiguration(), cachedConfiguration);
		gluuConfiguration.setCacheConfiguration(cachedConfiguration);
		configurationService.updateConfiguration(gluuConfiguration);
		return true;
	}

	public boolean saveDocumentStoreConfiguration(DocumentStoreConfiguration documentStoreConfiguration) {
		GluuConfiguration gluuConfiguration = configurationService.getConfiguration();
		diff( gluuConfiguration.getDocumentStoreConfiguration(), documentStoreConfiguration);
		gluuConfiguration.setDocumentStoreConfiguration(documentStoreConfiguration);
		configurationService.updateConfiguration(gluuConfiguration);
		return true;
	}

	private LdapOxTrustConfiguration loadOxTrustConfig(String configurationDn) {
		try {
			LdapOxTrustConfiguration conf = persistenceEntryManager.find(LdapOxTrustConfiguration.class,
					configurationDn);

			return conf;
		} catch (BasePersistenceException ex) {
			log.error("Failed to load configuration from LDAP");
		}

		return null;
	}

	private LdapOxAuthConfiguration loadOxAuthConfig(String configurationDn) {
		try {
			configurationDn = configurationDn.replace("ou=oxtrust", "ou=oxauth");
			LdapOxAuthConfiguration conf = persistenceEntryManager.find(LdapOxAuthConfiguration.class, configurationDn);
			return conf;
		} catch (BasePersistenceException ex) {
			log.error("Failed to load configuration from LDAP");
		}

		return null;
	}
	
	private void encrypPassword(RedisConfiguration redisConfiguration) {
        try {
            String password = redisConfiguration.getPassword();
            if (StringUtils.isNotBlank(password)) {
                redisConfiguration.setPassword(stringEncrypter.encrypt(password));
                log.trace("Decrypted redis password successfully.");
            }
        } catch (StringEncrypter.EncryptionException e) {
            log.error("Error during redis password decryption", e);
        }
    }

	public DbApplicationConfiguration loadFido2Configuration() {
		try {
			String configurationDn = configurationFactory.getBaseConfiguration()
					.getString("fido2_ConfigurationEntryDN");
			DbApplicationConfiguration conf = persistenceEntryManager.find(DbApplicationConfiguration.class,
					configurationDn);
			return conf;
		} catch (BasePersistenceException ex) {
			log.error("Failed to load Fido2 configuration from LDAP");
		}

		return null;
	}

	public void saveFido2Configuration(String fido2ConfigJson) {
		DbApplicationConfiguration fido2Configuration = loadFido2Configuration();
		fido2Configuration.setDynamicConf(fido2ConfigJson);
		fido2Configuration.setRevision(fido2Configuration.getRevision() + 1);
		persistenceEntryManager.merge(fido2Configuration);
	}
	
	public void diff(Object obj1, Object obj2) {
		// Make sure that 2 objects has same type
		if (obj1 != null && obj2 != null) {
			if (obj1.getClass().equals(obj2.getClass())) {
				LdapOxTrustConfiguration ldapOxTrustConfiguration = getOxTrustConfiguration();
				String auditLogLocation = ldapOxTrustConfiguration.getApplication().getAuditConfigLogsLocation();
				if (auditLogLocation != null && !auditLogLocation.isEmpty()) {
					File file = new File(
							auditLogLocation + "_" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".txt");
					String logline = "";

					try {
						Class c = Class.forName(obj1.getClass().getName());
						Method m[] = c.getDeclaredMethods();
						Object oo1, oo2;

						for (int i = 0; i < m.length; i++) {
							if (m[i].getName().startsWith("get") || m[i].getName().startsWith("is")) {
								oo1 = m[i].invoke(obj1, null);
								oo2 = m[i].invoke(obj2, null);
								if (oo1 != null || oo2 != null) {
									Date date = new Date();
									DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
									if (oo1.getClass().getName().startsWith("org.gluu")) {
										diff(oo1, oo2);
										continue;
									}
									if (oo1.getClass().isArray()) {
										if (!Arrays.deepEquals((Object[]) oo1, (Object[]) oo2)) {
											
											AuditConfigLogDetails auditConfigLogDetails = new AuditConfigLogDetails(
													identity.getUser().getDisplayName(),
													c.getSimpleName(),
													m[i].getName().substring(2),
													oo1.toString(),
													oo2.toString());
											
											logline = logline + dateFormat.format(date) + " " + jsonService.objectToJson(auditConfigLogDetails) + "\n";
											log.info(auditConfigLogDetails.toString());
										}
										continue;
									}
									if (oo1 instanceof List) {
										if (!CollectionUtils.isEqualCollection((List) oo1, (List) oo2)) {
											AuditConfigLogDetails auditConfigLogDetails = new AuditConfigLogDetails(
													identity.getUser().getDisplayName(),
													c.getSimpleName(),
													m[i].getName().substring(2),
													oo1.toString(),
													oo2.toString());
											
											logline = logline + dateFormat.format(date) + " " +  jsonService.objectToJson(auditConfigLogDetails) + "\n";
											log.info(auditConfigLogDetails.toString());
										}
										continue;
									}
									if (!oo1.equals(oo2)) {
										if (m[i].getName().startsWith("is")) {
											AuditConfigLogDetails auditConfigLogDetails = new AuditConfigLogDetails(
													identity.getUser().getDisplayName(),
													c.getSimpleName(),
													m[i].getName().substring(2),
													oo1.toString(),
													oo2.toString());
											
											logline = logline + dateFormat.format(date) + " " + jsonService.objectToJson(auditConfigLogDetails) + "\n";
											
											log.info(auditConfigLogDetails.toString());

										} else {
											AuditConfigLogDetails auditConfigLogDetails = new AuditConfigLogDetails(
													identity.getUser().getDisplayName(),
													c.getSimpleName(),
													m[i].getName().substring(3),
													oo1.toString(),
													oo2.toString());
											
											logline = logline + dateFormat.format(date) + " " +  jsonService.objectToJson(auditConfigLogDetails) + "\n";
											log.info(auditConfigLogDetails.toString());

										}
									}
								}
							}
						}
						FileUtils.writeStringToFile(file, logline, StandardCharsets.UTF_8, true);
					} catch (Throwable e) {
						log.error(e.getMessage());
					}
				} else {
					log.info(
							"Audit log location is not set to save log files.please set AuditLogsLocation property in oxtrust configuration. ");
				}
			} else {
				log.info("compare object class mismatch");
			}
		}
	}

}
