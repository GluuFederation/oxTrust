package org.gluu.oxtrust.ldap.cache.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.gluu.oxtrust.util.OxTrustConstants;
import org.xdi.util.StringHelper;
import org.xdi.util.properties.FileConfiguration;

/**
 * Provides methods to access cache configuration
 * 
 * @author Yuriy Movchan Date: 07.13.2011
 */
public class CacheRefreshConfiguration extends FileConfiguration {

	public CacheRefreshConfiguration() {
		super(OxTrustConstants.CONFIGURATION_FILE_CACHE_PROPERTIES_FILE);
	}

	public String[] getSourceServerConfigs() {
		return getStringArray("server.source.configs", null);
	}

	public String getInumDbServerConfig() {
		return getString("server.inum.config", null);
	}

	public String getDestinationServerConfig() {
		return getString("server.target.config", null);
	}

	public Properties getServerConfigProperties(String serverConfig) {
		return getPropertiesByPrefix(String.format("ldap.conf.%s.", serverConfig));
	}

	public String getSnapshotFolder() {
		return getString("snapshot.folder", null);
	}

	public int getSnapshotMaxCount() {
		return getInt("snapshot.max.count", 10);
	}

	public int getSizeLimit() {
		return getInt("cache.config.sizelimit", 0);
	}

	public String[] getCompoundKeyAttributes() {
		return getStringArray("ldap.conf.source.compoundKey.attr");
	}

	public List<String> getCompoundKeyAttributesList() {
		return getStringList("ldap.conf.source.compoundKey.attr");
	}

	public String[] getCompoundKeyObjectClasses() {
		return getStringArray("ldap.conf.source.compoundKey.objectClasses");
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
		return getStringArray("ldap.conf.source.attrs");
	}

	public String getCustomLdapFilter() {
		return getString("ldap.conf.source.custom.filter", null);
	}

	public CacheRefreshUpdateMethod getUpdateMethod() {
		String updateMethod = getString("target.server.update.method", CacheRefreshUpdateMethod.VDS.getValue());
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
		return getBoolean("target.keep.external.person", false);
	}

	public boolean isLoadSourceUsingSearchLimit() {
		return getBoolean("source.load.use.search.limit", true);
	}

	public Map<String, String> getTargetServerAttributesMapping() {
		String attributePrefix = "ldap.conf.target.attr.mapping.";
		Properties mappingProperties = getPropertiesByPrefix(attributePrefix);

		Map<String, String> result = new HashMap<String, String>();
		for (Entry<Object, Object> entry : mappingProperties.entrySet()) {
			String destinationAttribute = StringHelper.toLowerCase((String) entry.getKey());
			String sourceAttribute = StringHelper.toLowerCase((String) entry.getValue());

			result.put(destinationAttribute, sourceAttribute);
		}

		return result;
	}

	public String getInterceptorScriptFileName() {
		return getString("interceptor.script.fileName", null);
	}

}
