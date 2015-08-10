/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.cache.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.xdi.util.StringHelper;
import org.xdi.util.properties.FileConfiguration;

/**
 * Provides methods to access cache configuration
 * 
 * @author Yuriy Movchan Date: 07.13.2011
 */
public class CacheRefreshConfiguration {
	
	private FileConfiguration fileConfiguration;

	public CacheRefreshConfiguration(FileConfiguration fileConfiguration) {
		this.fileConfiguration = fileConfiguration;
	}

	public String[] getSourceServerConfigs() {
		return fileConfiguration.getStringArray("server.source.configs", null);
	}

	public String getInumDbServerConfig() {
		return fileConfiguration.getString("server.inum.config", null);
	}

	public String getDestinationServerConfig() {
		return fileConfiguration.getString("server.target.config", null);
	}

	public Properties getServerConfigProperties(String serverConfig) {
		return fileConfiguration.getPropertiesByPrefix(String.format("ldap.conf.%s.", serverConfig));
	}

	public String getSnapshotFolder() {
		return fileConfiguration.getString("snapshot.folder", null);
	}

	public int getSnapshotMaxCount() {
		return fileConfiguration.getInt("snapshot.max.count", 10);
	}

	public int getSizeLimit() {
		return fileConfiguration.getInt("cache.config.sizelimit", 0);
	}

	public String[] getCompoundKeyAttributes() {
		return fileConfiguration.getStringArray("ldap.conf.source.compoundKey.attr");
	}

	public List<String> getCompoundKeyAttributesList() {
		return fileConfiguration.getStringList("ldap.conf.source.compoundKey.attr");
	}

	public String[] getCompoundKeyObjectClasses() {
		return fileConfiguration.getStringArray("ldap.conf.source.compoundKey.objectClasses");
	}

	public String[] getCompoundKeyAttributesWithoutValues() {
		String[] result = getCompoundKeyAttributes();
		for (int i = 0; i < result.length; i++) {
			int index = result[i].indexOf('=');
			if (index != -1) {
				result[i] = result[i].substring(0, index);
			}
		}

		return result;
	}

	public String[] getSourceAttributes() {
		return fileConfiguration.getStringArray("ldap.conf.source.attrs");
	}

	public String getCustomLdapFilter() {
		return fileConfiguration.getString("ldap.conf.source.custom.filter", null);
	}

	public CacheRefreshUpdateMethod getUpdateMethod() {
		String updateMethod = fileConfiguration.getString("target.server.update.method", CacheRefreshUpdateMethod.VDS.getValue());
		if (StringHelper.isEmpty(updateMethod)) {
			return CacheRefreshUpdateMethod.VDS;
		}

		CacheRefreshUpdateMethod result = CacheRefreshUpdateMethod.getByValue(updateMethod.toLowerCase());
		if (result == null) {
			return CacheRefreshUpdateMethod.VDS;
		}

		return result;
	}

	public boolean isKeepExternalPerson() {
		return fileConfiguration.getBoolean("target.keep.external.person", false);
	}

	public boolean isLoadSourceUsingSearchLimit() {
		return fileConfiguration.getBoolean("source.load.use.search.limit", true);
	}

	public Map<String, String> getTargetServerAttributesMapping() {
		String attributePrefix = "ldap.conf.target.attr.mapping.";
		Properties mappingProperties = fileConfiguration.getPropertiesByPrefix(attributePrefix);

		Map<String, String> result = new HashMap<String, String>();
		for (Entry<Object, Object> entry : mappingProperties.entrySet()) {
			String destinationAttribute = StringHelper.toLowerCase((String) entry.getKey());
			String sourceAttribute = StringHelper.toLowerCase((String) entry.getValue());

			result.put(destinationAttribute, sourceAttribute);
		}

		return result;
	}

	public String getInterceptorScriptFileName() {
		return fileConfiguration.getString("interceptor.script.fileName", null);
	}

	public String getString(String key) {
		return fileConfiguration.getString(key);
	}

	public String[] getStringArray(String key) {
		return fileConfiguration.getStringArray(key);
	}

	public List<String> getStringList(String key) {
		return fileConfiguration.getStringList(key);
	}

	public int getInt(String key, int defaultValue) {
		return fileConfiguration.getInt(key, defaultValue);
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		return fileConfiguration.getBoolean(key, defaultValue);
	}

	public Properties getPropertiesByPrefix(String propertiesPrefix) {
		return fileConfiguration.getPropertiesByPrefix(propertiesPrefix);
	}

	public boolean isLoaded() {
		return fileConfiguration.isLoaded();
	}

	public void reload() {
		fileConfiguration.reload();
	}

}
