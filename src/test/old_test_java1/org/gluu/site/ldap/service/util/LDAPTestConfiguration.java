package org.gluu.site.ldap.service.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.gluu.site.properties.FileConfiguration;

public class LDAPTestConfiguration extends FileConfiguration {

	private static final String SITE_LDAP_TEST_PROPERTIES_FILE = "siteLdapTest.properties";

	protected PropertiesConfiguration conf;

	public LDAPTestConfiguration() {
		super(SITE_LDAP_TEST_PROPERTIES_FILE);
	}

	@SuppressWarnings("unchecked")
	public boolean isKeyExist(String key) {
		Iterator<String> keyIterator = conf.getKeys();
		while (keyIterator.hasNext()) {
			if (keyIterator.next().equals(key)) {
				return true;
			}
		}

		return false;
	}

	@SuppressWarnings("unchecked")
	public List<String> getKeysStartWith(String keyPrefix) {
		List<String> result = new ArrayList<String>();
		Iterator<String> keyIterator = conf.getKeys();
		while (keyIterator.hasNext()) {
			String key = keyIterator.next();
			if (key.startsWith(keyPrefix)) {
				result.add(key);
			}
		}

		return result;
	}

	public List<String> getStringList(String key) {
		return Arrays.asList(getStringArray(key));
	}

	public Map<String, String> getStringMap(String key, String keyValue) {
		String keys[] = conf.getStringArray(key);
		String values[] = conf.getStringArray(keyValue);

		HashMap<String, String> result = new HashMap<String, String>(keys.length);
		for (int i = 0; i < keys.length; i++) {
			result.put(keys[i], values[i]);
		}

		return result;
	}

	/**
	 * Create map of values based on key/values which keys starts with base key
	 * @param baseKey Base key
	 **/
	@SuppressWarnings("unchecked")
	public Map<String, ?> getCollection(String baseKey) {
		Map result = new HashMap();

		Iterator<String> keyIterator = conf.getKeys();
		while (keyIterator.hasNext()) {
			String currKey = keyIterator.next();
			if (!currKey.startsWith(baseKey)) {
				continue;
			}

			String resultKey = currKey.substring(currKey.indexOf(baseKey) + baseKey.length() + 1);
			result.put(resultKey, conf.getString(currKey));
		}

		return result;
	}

}