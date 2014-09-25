package org.gluu.oxtrust.util;

import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.jboss.seam.annotations.In;
import org.xdi.config.CryptoConfigurationFile;
import org.xdi.model.SimpleProperty;
import org.xdi.util.StringHelper;
import org.xdi.util.security.StringEncrypter;
import org.xdi.util.security.StringEncrypter.EncryptionException;

/**
 * Utility class with helpers methods to generate configuration files
 * 
 * @author Yuriy Movchan Date: 08.02.2011
 */
public class PropertyUtil {

	private static final Logger log = Logger.getLogger(PropertyUtil.class);

	@In(value = "#{oxTrustConfiguration.cryptoConfiguration}")
	private CryptoConfigurationFile cryptoConfiguration;
	
	public String encryptString(String value) {
		try {
			return StringEncrypter.defaultInstance().encrypt(value, cryptoConfiguration.getEncodeSalt());
		} catch (EncryptionException ex) {
			log.error("Failed to encrypt string: " + value, ex);
		}

		return null;
	}

	public String stringsToCommaSeparatedList(List<String> values) {
		StringBuilder sb = new StringBuilder();

		int count = values.size();
		for (int i = 0; i < count; i++) {
			sb.append(escapeString(values.get(i)));
			if (i < count - 1) {
				sb.append(", ");
			}
		}

		return sb.toString();
	}

	public String simplePropertiesToCommaSeparatedList(List<SimpleProperty> values) {
		StringBuilder sb = new StringBuilder();

		int count = values.size();
		for (int i = 0; i < count; i++) {
			sb.append(escapeString(values.get(i).getValue()));
			if (i < count - 1) {
				sb.append(", ");
			}
		}

		return sb.toString();
	}

	public String escapeString(String value) {
		if (StringHelper.isEmpty(value)) {
			return "";
		}

		return escapeComma(StringEscapeUtils.escapeJava(value));
	}

	/**
	 * Inserts a backslash before every comma
	 */
	private static String escapeComma(String s) {
		StringBuffer buf = new StringBuffer(s);
		for (int i = 0; i < buf.length(); i++) {
			char c = buf.charAt(i);
			if (c == ',') {
				buf.insert(i, '\\');
				i++;
			}
		}
		return buf.toString();
	}
	
	public boolean isEmptyString(String string) {
		return StringHelper.isEmpty(string);
	}

}
