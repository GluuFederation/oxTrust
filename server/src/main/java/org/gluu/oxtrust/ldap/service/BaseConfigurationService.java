package org.gluu.oxtrust.ldap.service;

import java.io.Serializable;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.velocity.VelocityContext;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.model.GluuOrganization;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.gluu.site.ldap.persistence.exception.LdapMappingException;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.xdi.config.CryptoConfigurationFile;
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.util.INumGenerator;
import org.xdi.util.StringHelper;
import org.xdi.util.security.StringEncrypter;
import org.xdi.util.security.StringEncrypter.EncryptionException;

/**
 * Provides operations with velocity templates
 * 
 * @author Yuriy Movchan Date: 12.15.2010
 */
@Scope(ScopeType.STATELESS)
@Name("baseConfigurationService")
@AutoCreate
public class BaseConfigurationService implements Serializable {

	private static final long serialVersionUID = -3840968275007784641L;

	private static final String ORGANIZATION_BASE_CONFIGURATION = "baseConfiguration.ldif";

	@Logger
	private Log log;

	@In
	private LdapEntryManager ldapEntryManager;

	@In
	private OrganizationService organizationService;

	@In
	private ApplianceService applianceService;

	@In
	private CentralLdapService centralLdapService;

	@In(value = "#{oxTrustConfiguration.applicationConfiguration}")
	private ApplicationConfiguration applicationConfiguration;
	
	@In(value = "#{oxTrustConfiguration.cryptoConfiguration}")
	private CryptoConfigurationFile cryptoConfiguration;
	
	public boolean checkAndUpdateLdapbaseConfiguration() {
		try {
			return checkAndUpdateLdapbaseConfigurationImpl();
		} catch (Exception ex) {
			log.error("Exception happened during checking base configuration", ex);
		}

		return false;
	}

	public boolean checkAndUpdateLdapbaseConfigurationImpl() {
		log.debug("Updating organization entry in LDAP database");

		String confOrgInum = applicationConfiguration.getOrgInum();
		String confOrgIname = applicationConfiguration.getOrgIname();

		if (StringHelper.isEmpty(confOrgInum)) {
			log.error("Failed to update organization due to invalid value specified in property orgInum. You should check gluuAppliance.properties file.");
			return false;
		} else {
			// Check if LDAP contains organization with specified DN
			GluuOrganization organization = new GluuOrganization();
			organization.setDn(organizationService.getDnForOrganization(confOrgInum));
			boolean containsOrganization = false;
			try {
				containsOrganization = organizationService.containsOrganization(organization);
			} catch (LdapMappingException ex) {
			}

			try {
				if (containsOrganization) {
					organization = organizationService.getOrganizationByInum(confOrgInum);
					organization.setIname(confOrgIname);

					organizationService.updateOrganization(organization);
				} else {
					boolean result = loadOrganizationBaseConfiguration();
					if (!result) {
						log.debug("Failed to load base configuration for organization");
					}
				}
			} catch (LdapMappingException ex) {
				log.error("Failed to update organization entry", ex);
				return false;
			}
		}

		log.debug("Updating appliance entry in LDAP database");

		String confApplianceInum = applicationConfiguration.getApplianceInum();
		String confApplianceIname = applicationConfiguration.getApplianceIname();
		if (StringHelper.isEmpty(confApplianceInum)) {
			log.error("Failed to update appliance due to invalid value specified in property applianceInum. You should check gluuAppliance.properties file.");
			return false;
		} else {
			// Check if LDAP contains appliance with specified DN
			GluuAppliance appliance = new GluuAppliance();
			appliance.setDn(applianceService.getDnForAppliance(confApplianceInum));
			try {
				boolean containsAppliance = false;
				try {
					containsAppliance = applianceService.containsAppliance(appliance);
				} catch (LdapMappingException ex) {
				}

				if (containsAppliance) {
					appliance = applianceService.getApplianceByInum(confApplianceInum);
					appliance.setIname(confApplianceIname);

					applianceService.updateAppliance(appliance);
				} else {
					appliance.setInum(confApplianceInum);
					appliance.setIname(confApplianceIname);
					appliance.setInumFN(StringHelper.removePunctuation(appliance.getInum()));
					String newPassword = RandomStringUtils.randomAlphanumeric(8);
					appliance.setBlowfishPassword(StringEncrypter.defaultInstance().encrypt(newPassword, cryptoConfiguration.getEncodeSalt()));

					if (centralLdapService.isUseCentralServer()) {
						GluuAppliance tmpAppliance = new GluuAppliance();
						tmpAppliance.setDn(appliance.getDn());
						boolean existAppliance = centralLdapService.containsAppliance(tmpAppliance);
	
						if (existAppliance) {
							centralLdapService.updateAppliance(appliance);
						} else {
							centralLdapService.addAppliance(appliance);
						}
					}

					appliance.setUserPassword(newPassword);
					applianceService.addAppliance(appliance);
				}
			} catch (LdapMappingException ex) {
				log.error("Failed to update appliance entry", ex);
				return false;
			} catch (EncryptionException e) {
				log.error("Failed to encrypt password. Appliance creation terminated.", e);
				return false;
			}
		}

		return true;
	}

	private boolean loadOrganizationBaseConfiguration() {
		// Generate top.ldif from template
		VelocityContext context = new VelocityContext();
		context.put("orgInum", applicationConfiguration.getOrgInum());
		context.put("orgIname", applicationConfiguration.getOrgIname());
		context.put("orgDisplayName", applicationConfiguration.getOrgDisplayName());
		context.put("orgShortName", applicationConfiguration.getOrgShortName());

		context.put("orgOwnerGroupInum", applicationConfiguration.getOrgInum() + OxTrustConstants.inumDelimiter + INumGenerator.generate(2));
		context.put("orgManagerGroupInum", applicationConfiguration.getOrgInum() + OxTrustConstants.inumDelimiter + INumGenerator.generate(2));

		String ldifFileContent = TemplateService.instance().generateConfFile(ORGANIZATION_BASE_CONFIGURATION, context);
		if (StringHelper.isEmpty(ldifFileContent)) {
			return false;
		}

		// Load generated top.ldif
		boolean result = ldapEntryManager.loadLdifFileContent(ldifFileContent);
		if (!result) {
			log.error("Failed to load base configuration");
			return false;
		}

		return true;
	}

	/**
	 * Get baseConfigurationService instance
	 * 
	 * @return BaseConfigurationService instance
	 */
	public static BaseConfigurationService instance() {
		return (BaseConfigurationService) Component.getInstance(BaseConfigurationService.class);
	}

}
