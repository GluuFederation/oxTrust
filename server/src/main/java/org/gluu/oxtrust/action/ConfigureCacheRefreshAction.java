/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.velocity.VelocityContext;
import org.gluu.oxtrust.ldap.cache.model.GluuCacheRefreshConfiguration;
import org.gluu.oxtrust.ldap.cache.model.GluuSimplePerson;
import org.gluu.oxtrust.ldap.cache.service.CacheRefreshConfiguration;
import org.gluu.oxtrust.ldap.cache.service.CacheRefreshService;
import org.gluu.oxtrust.ldap.cache.service.CacheRefreshUpdateMethod;
import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.ldap.service.InumService;
import org.gluu.oxtrust.ldap.service.PersonService;
import org.gluu.oxtrust.ldap.service.TemplateService;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.LdapConfigurationModel;
import org.gluu.oxtrust.model.SimpleCustomPropertiesListModel;
import org.gluu.oxtrust.model.SimpleDoubleProperty;
import org.gluu.oxtrust.model.SimplePropertiesListModel;
import org.gluu.oxtrust.service.external.ExternalCacheRefreshService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.oxtrust.util.PropertyUtil;
import org.gluu.oxtrust.util.jsf.ValidationUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.xdi.config.CryptoConfigurationFile;
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.ldap.model.GluuStatus;
import org.xdi.model.SimpleCustomProperty;
import org.xdi.model.SimpleProperty;
import org.xdi.model.ldap.GluuLdapConfiguration;
import org.xdi.util.ArrayHelper;
import org.xdi.util.StringHelper;
import org.xdi.util.security.StringEncrypter;

/**
 * Action class for configuring cache refresh
 * 
 * @author Yuriy Movchan Date: 07.26.2011
 */
@Name("configureCacheRefreshAction")
@Scope(ScopeType.CONVERSATION)
@Restrict("#{identity.loggedIn}")
public class ConfigureCacheRefreshAction implements SimplePropertiesListModel, SimpleCustomPropertiesListModel, LdapConfigurationModel, Serializable {

	private static final long serialVersionUID = -5210460481895022468L;
	private static final String CACHE_REFRESH_CONFIGURATION_FILE = "oxTrustCacheRefresh.properties";
	private static final String CACHE_REFRESH_TEMPLATE_FILE = "oxTrustCacheRefresh-template.properties";

	@Logger
	private Log log;

	@In
	private CacheRefreshConfiguration cacheRefreshConfiguration;

	@In
	private TemplateService templateService;

	@In
	private PersonService personService;

	@In
	private ExternalCacheRefreshService externalCacheRefreshService;

	@In
	private InumService inumService;

	@In
	private AttributeService attributeService;

	@In
	private CacheRefreshService cacheRefreshService;

	@In
	private FacesMessages facesMessages;

	@In(value = "#{oxTrustConfiguration.applicationConfiguration}")
	private ApplicationConfiguration applicationConfiguration;
	
	@In(value = "#{oxTrustConfiguration.cryptoConfiguration}")
	private CryptoConfigurationFile cryptoConfiguration;

	private boolean cacheRefreshEnabled;
	private int cacheRefreshEnabledIntervalMinutes;

	private List<GluuLdapConfiguration> sourceConfigs;
	private GluuLdapConfiguration inumConfig;
	private GluuLdapConfiguration targetConfig;

	private GluuLdapConfiguration activeLdapConfig;

	private GluuAppliance appliance;

	private GluuCacheRefreshConfiguration cacheRefreshConfig;

	private boolean showInterceptorValidationDialog;
	private String interceptorValidationMessage;

	private boolean initialized;

	@Restrict("#{s:hasPermission('configuration', 'access')}")
	public String init() {
		if ((this.sourceConfigs != null) && (this.inumConfig != null) && (this.targetConfig != null)) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		this.showInterceptorValidationDialog = false;

		this.appliance = ApplianceService.instance().getAppliance();

		// Make sure that we modify up to date properties
		this.cacheRefreshConfiguration.reloadProperties();
		initConfigurations();

		this.initialized = true;

		return OxTrustConstants.RESULT_SUCCESS;
	}

	private void initConfigurations() {
		this.sourceConfigs = new ArrayList<GluuLdapConfiguration>();
		if (!this.cacheRefreshConfiguration.isLoaded()) {
			this.inumConfig = new GluuLdapConfiguration();
			this.targetConfig = new GluuLdapConfiguration();

			this.cacheRefreshConfig = new GluuCacheRefreshConfiguration();
			return;
		}

		String[] sourceConfigurationIds = this.cacheRefreshConfiguration.getSourceServerConfigs();
		if (ArrayHelper.isNotEmpty(sourceConfigurationIds)) {
			for (String sourceConfigurationId : sourceConfigurationIds) {
				this.sourceConfigs.add(prepareLdapConfig(sourceConfigurationId));
			}
		}

		this.inumConfig = prepareLdapConfig(this.cacheRefreshConfiguration.getInumDbServerConfig());
		this.targetConfig = prepareLdapConfig(this.cacheRefreshConfiguration.getDestinationServerConfig());

		this.cacheRefreshConfig = prepareCacheRefreshConfig();
	}

	@Restrict("#{s:hasPermission('configuration', 'access')}")
	public String update() {
		if (!vdsCacheRefreshPollingInterval()) {
			return OxTrustConstants.RESULT_FAILURE;
		}

		if (!validateLists()) {
			return OxTrustConstants.RESULT_FAILURE;
		}

		if (!generateCacheConfigurationFile()) {
			return OxTrustConstants.RESULT_FAILURE;
		}

		updateAppliance();

		return OxTrustConstants.RESULT_SUCCESS;
	}

	private void updateAppliance() {
		GluuAppliance updateAppliance = ApplianceService.instance().getAppliance();
		updateAppliance.setVdsCacheRefreshPollingInterval(updateAppliance.getVdsCacheRefreshPollingInterval());
		ApplianceService.instance().updateAppliance(updateAppliance);
	}

	// TODO: Yuriy Movchan: Use @Min property annotation + convert type from String to Integer 
	private boolean vdsCacheRefreshPollingInterval() {
		String intervalString = this.appliance.getVdsCacheRefreshPollingInterval();
		if (StringHelper.isEmpty(intervalString)) {
			return true;
		}

		Integer interval = null;
		try {
			interval = Integer.valueOf(intervalString);
		} catch (NumberFormatException ex) {
		}

		if ((interval == null) || (interval < 0)) {
			log.error("Invalid cache refresh pooling interval specified: {0}", intervalString);
			ValidationUtil.addErrorMessageToInput("vdsCacheRefreshPollingIntervalId", "Invalid cache refresh pooling interval specified");
			return false;
		}

		return true;
	}

	private boolean validateLists() {
		boolean result = true;
		for (GluuLdapConfiguration sourceConfig : this.sourceConfigs) {
			result &= validateList(sourceConfig, "Source", true);
		}

		result &= validateList(inumConfig, "Inum", true);

		if (CacheRefreshUpdateMethod.VDS.equals(cacheRefreshConfig.getUpdateMethod())) {
			result &= validateList(targetConfig, "Target", false);
		}

		result &= validateList(cacheRefreshConfig.getAttrs(), "Key attribute");
		result &= validateList(cacheRefreshConfig.getObjectClasses(), "Object class");
		result &= validateList(cacheRefreshConfig.getSourceAttributes(), "Source attribute");

		return result;
	}

	private boolean validateList(GluuLdapConfiguration ldapConfig, String configType, boolean validateBaseDNs) {
		boolean result = true;
		if (ldapConfig.getServers().size() == 0) {
			log.error("{0} LDAP configuration '{1}' should contains at least one server", configType, ldapConfig.getConfigId());
			facesMessages.add(Severity.ERROR, "{0} LDAP configuration '{1}' should contains at least one server", configType,
					ldapConfig.getConfigId());
			result = false;
		}

		if (validateBaseDNs && (ldapConfig.getBaseDNs().size() == 0)) {
			log.error("{0} LDAP configuration '{1}' should contains at least one Base DN", configType, ldapConfig.getConfigId());
			facesMessages.add(Severity.ERROR, "{0} LDAP configuration '{1}' should contains at least one Base DN", configType,
					ldapConfig.getConfigId());
			result = false;
		}

		return result;
	}

	private boolean validateList(List<SimpleProperty> values, String attributeName) {
		if (values.size() == 0) {
			log.error("{0} should contains at least one {0}", attributeName);
			facesMessages.add(Severity.ERROR, "{0} should contains at least one {0}", attributeName);
			return false;
		}

		return true;
	}

	@Restrict("#{s:hasPermission('configuration', 'access')}")
	public void cancel() {
	}

	public boolean isCacheRefreshEnabled() {
		return cacheRefreshEnabled;
	}

	public int getCacheRefreshEnabledIntervalMinutes() {
		return cacheRefreshEnabledIntervalMinutes;
	}

	private GluuLdapConfiguration prepareLdapConfig(String ldapConfigId) {
		if (StringHelper.isEmpty(ldapConfigId)) {
			return new GluuLdapConfiguration();
		}

		String prefix = String.format("ldap.conf.%s.", ldapConfigId);

		String bindPassword = "";
		try {
			bindPassword = StringEncrypter.defaultInstance().decrypt(cacheRefreshConfiguration.getString(prefix + "bindPassword"), cryptoConfiguration.getEncodeSalt());
		} catch (Exception ex) {
			log.error("Failed to decrypt password for property: {0}", ex, prefix + "bindPassword");
		}

		return new GluuLdapConfiguration(ldapConfigId, cacheRefreshConfiguration.getString(prefix + "bindDN"), bindPassword,
				toSimpleProperties(cacheRefreshConfiguration.getStringList(prefix + "servers")), cacheRefreshConfiguration.getInt(prefix
						+ "maxconnections", 2), cacheRefreshConfiguration.getBoolean(prefix + "useSSL", false),
				toSimpleProperties(cacheRefreshConfiguration.getStringList(prefix + "baseDNs")), null, null, cacheRefreshConfiguration.getBoolean(
						prefix + "useAnonymousBind", false));
	}

	private GluuCacheRefreshConfiguration prepareCacheRefreshConfig() {
		return new GluuCacheRefreshConfiguration(toSimpleProperties(cacheRefreshConfiguration.getCompoundKeyAttributes()),
				toSimpleProperties(cacheRefreshConfiguration.getCompoundKeyObjectClasses()),
				toSimpleProperties(cacheRefreshConfiguration.getSourceAttributes()), cacheRefreshConfiguration.getCustomLdapFilter(),
				cacheRefreshConfiguration.getSnapshotFolder(), cacheRefreshConfiguration.getSnapshotMaxCount(),
				cacheRefreshConfiguration.getSizeLimit(), cacheRefreshConfiguration.getUpdateMethod(),
				cacheRefreshConfiguration.isKeepExternalPerson(), cacheRefreshConfiguration.isLoadSourceUsingSearchLimit(),
				toSimpleDoubleProperties(cacheRefreshConfiguration.getTargetServerAttributesMapping()));
	}

	private List<SimpleProperty> toSimpleProperties(List<String> values) {
		List<SimpleProperty> result = new ArrayList<SimpleProperty>();

		for (String value : values) {
			result.add(new SimpleProperty(value));
		}

		return result;
	}

	private List<SimpleDoubleProperty> toSimpleDoubleProperties(Map<String, String> valuesMapping) {
		List<SimpleDoubleProperty> result = new ArrayList<SimpleDoubleProperty>();

		for (Entry<String, String> valueMapEntry : valuesMapping.entrySet()) {
			result.add(new SimpleDoubleProperty(valueMapEntry.getValue(), valueMapEntry.getKey()));
		}

		return result;
	}

	private List<SimpleProperty> toSimpleProperties(String[] values) {
		List<SimpleProperty> result = new ArrayList<SimpleProperty>();

		for (String value : values) {
			result.add(new SimpleProperty(value));
		}

		return result;
	}

	public void addItemToSimpleDoubleProperties(List<SimpleDoubleProperty> items) {
		items.add(new SimpleDoubleProperty("", ""));
	}

	public void removeItemFromSimpleDoubleProperties(List<SimpleDoubleProperty> items, SimpleDoubleProperty item) {
		items.remove(item);
	}

	public void addSourceConfig() {
		addLdapConfig(this.sourceConfigs);
	}

	public List<GluuLdapConfiguration> getSourceConfigs() {
		return sourceConfigs;
	}

	public GluuLdapConfiguration getInumConfig() {
		return inumConfig;
	}

	public GluuLdapConfiguration getTargetConfig() {
		return targetConfig;
	}

	public GluuCacheRefreshConfiguration getCacheRefreshConfig() {
		return cacheRefreshConfig;
	}

	public GluuAppliance getAppliance() {
		return appliance;
	}

	private boolean generateCacheConfigurationFile() {
		VelocityContext context = new VelocityContext();
		context.put("propUtil", new PropertyUtil());
		context.put("sourceConfigs", fixLdapConfigurations(sourceConfigs));
		context.put("inumConfig", fixLdapConfiguration(inumConfig));
		context.put("targetConfig", fixLdapConfiguration(targetConfig));
		context.put("cacheRefreshConfig", cacheRefreshConfig);

		String conf = templateService.generateConfFile(CACHE_REFRESH_TEMPLATE_FILE, context);
		if (conf == null) {
			return false;
		}

		return templateService.writeApplicationConfFile(CACHE_REFRESH_CONFIGURATION_FILE, conf);
	}
	
	private GluuLdapConfiguration fixLdapConfiguration(GluuLdapConfiguration ldapConfig) {
		if (ldapConfig.isUseAnonymousBind()) {
			ldapConfig.setBindDN(null);
		}
		
		return ldapConfig;
	}

	private List<GluuLdapConfiguration> fixLdapConfigurations(List<GluuLdapConfiguration> ldapConfigs) {
		for (GluuLdapConfiguration ldapConfig : ldapConfigs) {
			fixLdapConfiguration(ldapConfig);
		}
		
		return ldapConfigs;
	}

	public CacheRefreshUpdateMethod[] getAllCacheRefreshUpdateMethods() {
		return CacheRefreshUpdateMethod.values();
	}

	public void validateInterceptorScript() {
		String result = update();
		if (!OxTrustConstants.RESULT_SUCCESS.equals(result)) {
			return;
		}

		// Reinit dialog
		init();

		// Reload current configuration
		cacheRefreshConfiguration.reloadProperties();

		if (StringHelper.isEmpty(cacheRefreshConfiguration.getInterceptorScriptFileName())) {
			return;
		}

		this.showInterceptorValidationDialog = true;

		boolean loadedScripts = externalCacheRefreshService.getCustomScriptConfigurations().size() > 0;
		if (!loadedScripts) {
			String message = "Can't load Cache Refresh scripts. Using default script";
			log.error(message);
			this.interceptorValidationMessage = message;

			return;
		}

		// Prepare data for dummy entry
		String targetInum = inumService.generateInums(OxTrustConstants.INUM_TYPE_PEOPLE_SLUG, false);
		String targetPersonDn = personService.getDnForPerson(targetInum);
		String[] targetCustomObjectClasses = applicationConfiguration.getPersonObjectClassTypes();

		// Collect all attributes
		String[] keyAttributesWithoutValues = cacheRefreshConfiguration.getCompoundKeyAttributesWithoutValues();
		String[] sourceAttributes = cacheRefreshConfiguration.getSourceAttributes();

		// Merge all attributes into one set
		Set<String> allAttributes = new HashSet<String>();
		for (String attribute : keyAttributesWithoutValues) {
			allAttributes.add(attribute);
		}

		for (String attribute : sourceAttributes) {
			allAttributes.add(attribute);
		}

		// Prepare source person entry with default attributes values
		GluuSimplePerson sourcePerson = new GluuSimplePerson();
		List<GluuCustomAttribute> customAttributes = sourcePerson.getCustomAttributes();
		for (String attribute : allAttributes) {
			customAttributes.add(new GluuCustomAttribute(attribute, "Test value"));
		}

		// Prepare target person
		GluuCustomPerson targetPerson = new GluuCustomPerson();
		targetPerson.setDn(targetPersonDn);
		targetPerson.setInum(targetInum);
		targetPerson.setStatus(GluuStatus.ACTIVE);
		targetPerson.setCustomObjectClasses(targetCustomObjectClasses);

		// Execute mapping according to configuration
		Map<String, String> targetServerAttributesMapping = cacheRefreshConfiguration.getTargetServerAttributesMapping();
		cacheRefreshService.setTargetEntryAttributes(sourcePerson, targetServerAttributesMapping, targetPerson);

		// Execute interceptor script
		boolean executionResult = externalCacheRefreshService.executeExternalUpdateUserMethods(targetPerson);
		if (!executionResult) {
			String message = "Can't execute Cache Refresh scripts.";
			log.error(message);
			this.interceptorValidationMessage = message;

			return;
		}

		log.info("Script has been executed successfully.\n\nSample source entry is:\n'{0}'.\n\nSample result entry is:\n'{1}'",
				getGluuSimplePersonAttributesWithValues(sourcePerson), getGluuCustomPersonAttributesWithValues(targetPerson));
		this.interceptorValidationMessage = String.format(
				"Script has been executed successfully.\n\nSample source entry is:\n%s.\n\nSample result entry is:\n%s",
				getGluuSimplePersonAttributesWithValues(sourcePerson), getGluuCustomPersonAttributesWithValues(targetPerson));
	}

	private String getGluuSimplePersonAttributesWithValues(GluuSimplePerson gluuSimplePerson) {
		StringBuilder sb = new StringBuilder();

		int index = 0;
		for (GluuCustomAttribute customAttribute : gluuSimplePerson.getCustomAttributes()) {

			// TODO: Do we need this?
			if (index > 0) {
				sb.append("\n");
			}
			sb.append("\n").append(customAttribute.getName()).append(": '");
			if ((customAttribute.getValues() != null) && (customAttribute.getValues().length > 1)) {
				sb.append(Arrays.toString(customAttribute.getValues()));
			} else {
				sb.append(customAttribute.getValue());
			}

			sb.append("'");
			index++;
		}

		return sb.toString();
	}

	private String getGluuCustomPersonAttributesWithValues(GluuCustomPerson gluuCustomPerson) {
		StringBuilder sb = new StringBuilder();
		sb.append("dn: '").append(gluuCustomPerson.getDn()).append("'\n");
		sb.append("inum: '").append(gluuCustomPerson.getInum()).append("',\n");
		sb.append("gluuStatus: '").append(gluuCustomPerson.getStatus()).append("'");

		for (GluuCustomAttribute customAttribute : gluuCustomPerson.getCustomAttributes()) {
			sb.append("\n").append(customAttribute.getName()).append(": '");
			if ((customAttribute.getValues() != null) && (customAttribute.getValues().length > 1)) {
				sb.append(Arrays.toString(customAttribute.getValues()));
			} else {
				sb.append(customAttribute.getValue());
			}
			sb.append("'");
		}

		return sb.toString();
	}

	public String getInterceptorValidationMessage() {
		return interceptorValidationMessage;
	}

	public boolean isShowInterceptorValidationDialog() {
		return showInterceptorValidationDialog;
	}

	public void hideShowInterceptorValidationDialog() {
		this.showInterceptorValidationDialog = false;
	}

	public boolean isInitialized() {
		return initialized;
	}

	@Override
	public void addItemToSimpleProperties(List<SimpleProperty> simpleProperties) {
		if (simpleProperties != null) {
			simpleProperties.add(new SimpleProperty(""));
		}
	}

	@Override
	public void removeItemFromSimpleProperties(List<SimpleProperty> simpleProperties, SimpleProperty simpleProperty) {
		if (simpleProperties != null) {
			simpleProperties.remove(simpleProperty);
		}
	}

	@Override
	public void addItemToSimpleCustomProperties(List<SimpleCustomProperty> simpleCustomProperties) {
		if (simpleCustomProperties != null) {
			simpleCustomProperties.add(new SimpleCustomProperty("", ""));
		}
	}

	@Override
	public void removeItemFromSimpleCustomProperties(List<SimpleCustomProperty> simpleCustomProperties,
			SimpleCustomProperty simpleCustomProperty) {
		if (simpleCustomProperties != null) {
			simpleCustomProperties.remove(simpleCustomProperty);
		}
	}

	public GluuLdapConfiguration getActiveLdapConfig() {
		return activeLdapConfig;
	}

	public void updateBindPassword() {
		// Use this method if we need to save configuration after setting new bind password
	}

	@Override
	public void setActiveLdapConfig(GluuLdapConfiguration activeLdapConfig) {
		this.activeLdapConfig = activeLdapConfig;
	}

	@Override
	public void addLdapConfig(List<GluuLdapConfiguration> ldapConfigList) {
		GluuLdapConfiguration ldapConfiguration = new GluuLdapConfiguration();
		ldapConfiguration.setBindPassword("");
		ldapConfigList.add(ldapConfiguration);
	}

	@Override
	public void removeLdapConfig(List<GluuLdapConfiguration> ldapConfigList, GluuLdapConfiguration removeLdapConfig) {
		for (Iterator<GluuLdapConfiguration> iterator = ldapConfigList.iterator(); iterator.hasNext();) {
			GluuLdapConfiguration ldapConfig = iterator.next();
			if (System.identityHashCode(removeLdapConfig) == System.identityHashCode(ldapConfig)) {
				iterator.remove();
				return;
			}
		}
	}

}
