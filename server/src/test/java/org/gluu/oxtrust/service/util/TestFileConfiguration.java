/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.gluu.util.properties.FileConfiguration;

public class TestFileConfiguration extends FileConfiguration {

	public TestFileConfiguration(String fileName) {
		super(fileName);
	}

	public boolean isKeyExist(String key) {
		Iterator<String> keyIterator = getPropertiesConfiguration().getKeys();
		while (keyIterator.hasNext()) {
			if (keyIterator.next().equals(key)) {
				return true;
			}
		}

		return false;
	}

	public List<String> getKeysStartWith(String keyPrefix) {
		List<String> result = new ArrayList<String>();
		Iterator<String> keyIterator = getPropertiesConfiguration().getKeys();
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
		String keys[] = getPropertiesConfiguration().getStringArray(key);
		String values[] = getPropertiesConfiguration().getStringArray(keyValue);

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

		Iterator<String> keyIterator = getPropertiesConfiguration().getKeys();
		while (keyIterator.hasNext()) {
			String currKey = keyIterator.next();
			if (!currKey.startsWith(baseKey)) {
				continue;
			}

			String resultKey = currKey.substring(currKey.indexOf(baseKey) + baseKey.length() + 1);
			result.put(resultKey, getPropertiesConfiguration().getString(currKey));
		}

		return result;
	}

}