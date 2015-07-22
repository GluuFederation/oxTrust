/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.util.jsf;

import org.apache.commons.lang.StringEscapeUtils;
import org.xdi.model.DisplayNameEntry;
import org.xdi.service.LookupService;
import org.xdi.util.StringHelper;

//import org.gluu.site.model.Entry;

/**
 * Tag library to help display data in facelets
 * 
 * @author Yuriy Movchan Date: 11.09.2010
 */
public class JsfFunctions {

	public static DisplayNameEntry getDisplayNameEntry(String dn) {
		if (dn == null) {
			return null;
		}

		try {
			return LookupService.instance().getDisplayNameEntry(dn);
		} catch (Exception ex) {
			return null;
		}
	}

	// public static List<DisplayNameEntry> getDisplayNameEntries(String baseDn,
	// List<? extends Entry> entries) {
	// if ((baseDn == null) || (entries == null)) {
	// return null;
	// }
	//
	// try {
	// return LookupService.instance().getDisplayNameEntriesByEntries(baseDn,
	// entries);
	// } catch (Exception ex) {
	// return null;
	// }
	// }
	//
	// public static String encodeString(String str) {
	// if ((str == null) || (str.length() == 0)) {
	// return str;
	// }
	// try {
	// return (new URI(null, str, null)).toString();
	// } catch (URISyntaxException ex) {
	// return null;
	// }
	// }
	//
	public static String splitByLines(String str, int maxLength) {
		if ((str == null) || (str.length() == 0) || (maxLength == 0)) {
			return str;
		}

		String tmpStr = StringEscapeUtils.escapeHtml(str);

		StringBuilder result = new StringBuilder();
		int startIndex = -1;
		int oldStartIndex = -1;
		while (startIndex + 1 < tmpStr.length()) {
			startIndex = tmpStr.indexOf(' ', startIndex + 1);
			if (startIndex == -1) {
				break;
			}
			if ((startIndex - oldStartIndex + 1) > maxLength) {
				result.append(tmpStr.substring(oldStartIndex + 1, startIndex));
				result.append("<br/>");
				oldStartIndex = startIndex;
			}
			startIndex++;
		}

		if (oldStartIndex < tmpStr.length()) {
			result.append(tmpStr.substring(oldStartIndex + 1));
		}

		return result.toString();
	}

	public static String trimFileName(String str, int maxLength) {
		if ((str == null) || (str.length() == 0) || (maxLength < 10)) {
			return str;
		}

		int length = str.length();
		int middleMaxLength = (maxLength - 3) / 2;

		if (length <= maxLength) {
			return str;
		}

		return str.substring(0, middleMaxLength - 1) + "..." + str.substring(length - middleMaxLength - 2);
	}

	public static String getColor(String color, String defaultColor) {
		String tmpColor = StringHelper.isEmpty(color) ? defaultColor : color;
		return "#" + tmpColor + ";";
	}

}
