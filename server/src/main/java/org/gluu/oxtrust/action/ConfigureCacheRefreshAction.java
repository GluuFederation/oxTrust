/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gluu.oxtrust.config.OxTrustConfiguration;
import org.gluu.oxtrust.ldap.cache.model.GluuSimplePerson;
import org.gluu.oxtrust.ldap.cache.service.CacheRefreshService;
import org.gluu.oxtrust.ldap.cache.service.CacheRefreshUpdateMethod;
import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.ldap.service.IPersonService;
import org.gluu.oxtrust.ldap.service.InumService;
import org.gluu.oxtrust.ldap.service.JsonConfigurationService;
import org.gluu.oxtrust.ldap.service.TemplateService;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.LdapConfigurationModel;
import org.gluu.oxtrust.model.SimpleCustomPropertiesListModel;
import org.gluu.oxtrust.model.SimplePropertiesListModel;
import org.gluu.oxtrust.service.external.ExternalCacheRefreshService;
import org.gluu.oxtrust.util.OxTrustConstants;
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
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.config.oxtrust.CacheRefreshAttributeMapping;
import org.xdi.config.oxtrust.CacheRefreshConfiguration;
import org.xdi.ldap.model.GluuStatus;
import org.xdi.model.SimpleCustomProperty;
import org.xdi.model.SimpleProperty;
import org.xdi.model.ldap.GluuLdapConfiguration;
import org.xdi.service.JsonService;
import org.xdi.util.StringHelper;
import org.xdi.util.security.StringEncrypter;
import org.xdi.util.security.StringEncrypter.EncryptionException;

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

	@Logger
	private Log log;

	@In
	private OxTrustConfiguration oxTrustConfiguration;

	@In
	private TemplateService templateService;

	@In
	private IPersonService personService;

	@In
	private ExternalCacheRefreshService externalCacheRefreshService;

	@In
	private InumService inumService;

	@In
	private AttributeService attributeService;

	@In
	private CacheRefreshService cacheRefreshService;

	@In
	private JsonConfigurationService jsonConfigurationService;

	@In
	private JsonService jsonService;

	@In
	private FacesMessages facesMessages;

	@In(value = "#{oxTrustConfiguration.applicationConfiguration}")
	private ApplicationConfiguration applicationConfiguration;
	
	@In(value = "#{oxTrustConfiguration.cryptoConfigurationSalt}")
	private String cryptoConfigurationSalt;

	private CacheRefreshConfiguration cacheRefreshConfiguration;

	private boolean cacheRefreshEnabled;
	private int cacheRefreshEnabledIntervalMinutes;

	private GluuLdapConfiguration activeLdapConfig;

	private GluuAppliance appliance;

	private List<SimpleProperty> keyAttributes;
	private List<SimpleProperty> keyObjectClasses;
	private List<SimpleProperty> sourceAttributes;
	private List<SimpleCustomProperty> attributeMapping;

	private boolean showInterceptorValidationDialog;
	private String interceptorValidationMessage;

	private boolean initialized;

	private CacheRefreshUpdateMethod updateMethod;
	
	@Restrict("#{s:hasPermission('configuration', 'access')}")
	public String init() {
		if (this.cacheRefreshConfiguration != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		this.showInterceptorValidationDialog = false;

		this.appliance = ApplianceService.instance().getAppliance();

		this.cacheRefreshConfiguration = getOxTrustCacheRefreshConfig();

		this.initialized = true;

		return OxTrustConstants.RESULT_SUCCESS;
	}

	private CacheRefreshConfiguration getOxTrustCacheRefreshConfig() {
		CacheRefreshConfiguration cacheRefreshConfiguration = jsonConfigurationService.getOxTrustCacheRefreshConfiguration();
		
		if (cacheRefreshConfiguration == null) {
			cacheRefreshConfiguration = new CacheRefreshConfiguration();
			cacheRefreshConfiguration.setUpdateMethod(CacheRefreshUpdateMethod.COPY.getValue());
			cacheRefreshConfiguration.setSourceConfigs(new ArrayList<GluuLdapConfiguration>());
			cacheRefreshConfiguration.setInumConfig(new GluuLdapConfiguration());
			cacheRefreshConfiguration.setTargetConfig(new GluuLdapConfiguration());
			cacheRefreshConfiguration.setKeyAttributes(new ArrayList<String>(0));
			cacheRefreshConfiguration.setKeyObjectClasses(new ArrayList<String>());
			cacheRefreshConfiguration.setSourceAttributes(new ArrayList<String>());
			cacheRefreshConfiguration.setAttributeMapping(new ArrayList<CacheRefreshAttributeMapping>());
			cacheRefreshConfiguration.setDefaultInumServer(true);
		}
		
		this.updateMethod = CacheRefreshUpdateMethod.getByValue(cacheRefreshConfiguration.getUpdateMethod());
		this.keyAttributes = toSimpleProperties(cacheRefreshConfiguration.getKeyAttributes());
		this.keyObjectClasses = toSimpleProperties(cacheRefreshConfiguration.getKeyObjectClasses());
		this.sourceAttributes = toSimpleProperties(cacheRefreshConfiguration.getSourceAttributes());
		this.attributeMapping = toSimpleCustomProperties(cacheRefreshConfiguration.getAttributeMapping());

		return cacheRefreshConfiguration;
	}

	@Restrict("#{s:hasPermission('configuration', 'access')}")
	public String update() {
		if (!vdsCacheRefreshPollingInterval()) {
			return OxTrustConstants.RESULT_FAILURE;
		}

		updateLists();

		if (!validateLists()) {
			return OxTrustConstants.RESULT_FAILURE;
		}

		fixLdapConfigurations(this.cacheRefreshConfiguration.getSourceConfigs());
		fixLdapConfiguration(this.cacheRefreshConfiguration.getInumConfig());
		fixLdapConfiguration(this.cacheRefreshConfiguration.getTargetConfig());
		
		try {
			jsonConfigurationService.saveOxTrustCacheRefreshConfiguration(this.cacheRefreshConfiguration);

			updateAppliance();
		} catch (Exception ex) {
			log.error("Failed to save Cache Refresh configuration", ex);
			return OxTrustConstants.RESULT_FAILURE;
		}

		return OxTrustConstants.RESULT_SUCCESS;
	}

	private void updateLists() {
		cacheRefreshConfiguration.setUpdateMethod(this.updateMethod.getValue());
		cacheRefreshConfiguration.setKeyAttributes(toStringList(this.keyAttributes));
		cacheRefreshConfiguration.setKeyObjectClasses(toStringList(this.keyObjectClasses));
		cacheRefreshConfiguration.setSourceAttributes(toStringList(this.sourceAttributes));
		cacheRefreshConfiguration.setAttributeMapping(toAttributeMappingList(this.attributeMapping));
	}

	private void updateAppliance() {
		GluuAppliance updateAppliance = ApplianceService.instance().getAppliance();
		updateAppliance.setVdsCacheRefreshEnabled(this.appliance.getVdsCacheRefreshEnabled());
		updateAppliance.setVdsCacheRefreshPollingInterval(this.appliance.getVdsCacheRefreshPollingInterval());
		updateAppliance.setCacheRefreshServerIpAddress(this.appliance.getCacheRefreshServerIpAddress());
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
		for (GluuLdapConfiguration sourceConfig : this.cacheRefreshConfiguration.getSourceConfigs()) {
			result &= validateList(sourceConfig, "Source", true);
		}

		result &= validateList(this.cacheRefreshConfiguration.getInumConfig(), "Inum", true);

		if (CacheRefreshUpdateMethod.VDS.equals(cacheRefreshConfiguration.getUpdateMethod())) {
			result &= validateList(this.cacheRefreshConfiguration.getTargetConfig(), "Target", false);
		}

		result &= validateList(this.cacheRefreshConfiguration.getKeyAttributes(), "Key attribute");
		result &= validateList(this.cacheRefreshConfiguration.getKeyObjectClasses(), "Object class");
		result &= validateList(this.cacheRefreshConfiguration.getSourceAttributes(), "Source attribute");

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

	private boolean validateList(List<String> values, String attributeName) {
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

	private List<SimpleProperty> toSimpleProperties(List<String> values) {
		List<SimpleProperty> result = new ArrayList<SimpleProperty>();

		for (String value : values) {
			result.add(new SimpleProperty(value));
		}

		return result;
	}

	private List<SimpleCustomProperty> toSimpleCustomProperties(List<CacheRefreshAttributeMapping> attributeMappings) {
		List<SimpleCustomProperty> result = new ArrayList<SimpleCustomProperty>();

		for (CacheRefreshAttributeMapping attributeMapping : attributeMappings) {
			result.add(new SimpleCustomProperty(attributeMapping.getSource(), attributeMapping.getDestination()));
		}

		return result;
	}

	private List<String> toStringList(List<SimpleProperty> simpleProperties) {
		List<String> result = new ArrayList<String>();

		for (SimpleProperty simpleProperty : simpleProperties) {
			result.add(simpleProperty.getValue());
		}

		return result;
	}

	private List<CacheRefreshAttributeMapping> toAttributeMappingList(List<SimpleCustomProperty> simpleCustomProperties) {
		List<CacheRefreshAttributeMapping> result = new ArrayList<CacheRefreshAttributeMapping>();

		for (SimpleCustomProperty simpleCustomProperty : simpleCustomProperties) {
			result.add(new CacheRefreshAttributeMapping(simpleCustomProperty.getValue1(), simpleCustomProperty.getValue2()));
		}

		return result;
	}

	public void addSourceConfig() {
		addLdapConfig(this.cacheRefreshConfiguration.getSourceConfigs());
	}

	public List<GluuLdapConfiguration> getSourceConfigs() {
		return this.cacheRefreshConfiguration.getSourceConfigs();
	}

	public GluuLdapConfiguration getInumConfig() {
		return this.cacheRefreshConfiguration.getInumConfig();
	}

	public GluuLdapConfiguration getTargetConfig() {
		return this.cacheRefreshConfiguration.getTargetConfig();
	}

	public CacheRefreshConfiguration getCacheRefreshConfig() {
		return this.cacheRefreshConfiguration;
	}

	public GluuAppliance getAppliance() {
		return appliance;
	}
	
	private GluuLdapConfiguration fixLdapConfiguration(GluuLdapConfiguration ldapConfig) {
		ldapConfig.updateStringsLists();
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
		String[] keyAttributesWithoutValues = getCompoundKeyAttributesWithoutValues(cacheRefreshConfiguration);
		String[] sourceAttributes = getSourceAttributes(cacheRefreshConfiguration);

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
		Map<String, String> targetServerAttributesMapping = getTargetServerAttributesMapping(cacheRefreshConfiguration);
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
		if (this.activeLdapConfig == null) {
			return;
		}

		try {
        	this.activeLdapConfig.setBindPassword(StringEncrypter.defaultInstance().encrypt(this.activeLdapConfig.getBindPassword(), cryptoConfigurationSalt));
        } catch (EncryptionException ex) {
            log.error("Failed to encrypt password", ex);
        }
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

	private String[] getSourceAttributes(CacheRefreshConfiguration cacheRefreshConfigurationuration) {
		return cacheRefreshConfigurationuration.getSourceAttributes().toArray(new String[0]);
	}

	private String[] getCompoundKeyAttributesWithoutValues(CacheRefreshConfiguration cacheRefreshConfigurationuration) {
		String[] result = cacheRefreshConfigurationuration.getKeyAttributes().toArray(new String[0]);
		for (int i = 0; i < result.length; i++) {
			int index = result[i].indexOf('=');
			if (index != -1) {
				result[i] = result[i].substring(0, index);
			}
		}

		return result;
	}

	private Map<String, String> getTargetServerAttributesMapping(CacheRefreshConfiguration cacheRefreshConfigurationuration) {
		Map<String, String> result = new HashMap<String, String>();
		for (CacheRefreshAttributeMapping attributeMapping : cacheRefreshConfigurationuration.getAttributeMapping()) {
			result.put(attributeMapping.getDestination(), attributeMapping.getSource());
		}

		return result;
	}

	public CacheRefreshUpdateMethod getUpdateMethod() {
		return updateMethod;
	}

	public void setUpdateMethod(CacheRefreshUpdateMethod updateMethod) {
		this.updateMethod = updateMethod;
	}

	public List<SimpleProperty> getKeyAttributes() {
		return keyAttributes;
	}

	public List<SimpleProperty> getKeyObjectClasses() {
		return keyObjectClasses;
	}

	public List<SimpleProperty> getSourceAttributes() {
		return sourceAttributes;
	}

	public List<SimpleCustomProperty> getAttributeMapping() {
		return attributeMapping;
	}

}
