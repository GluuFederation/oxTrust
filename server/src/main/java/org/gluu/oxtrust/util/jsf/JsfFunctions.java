/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.util.jsf;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.gluu.model.DisplayNameEntry;
import org.gluu.oxtrust.model.User;
import org.gluu.oxtrust.service.PermissionService;
import org.gluu.persist.PersistenceEntryManager;
import org.gluu.persist.model.base.Entry;
import org.gluu.service.LookupService;
import org.gluu.service.cdi.util.CdiUtil;
import org.gluu.util.StringHelper;

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
			return CdiUtil.bean(LookupService.class).getDisplayNameEntry(dn);
		} catch (Exception ex) {
			return null;
		}
	}

	public static Object getTypedEntry(String dn, String clazz) {
		if (dn == null) {
			return null;
		}

		try {
			return CdiUtil.bean(LookupService.class).getTypedEntry(dn, clazz);
		} catch (Exception ex) {
			return null;
		}
	}

	public static List<DisplayNameEntry> getDisplayNameEntries(String baseDn, List<? extends Entry> entries) {
		if ((baseDn == null) || (entries == null)) {
			return null;
		}

		try {
			return CdiUtil.bean(LookupService.class).getDisplayNameEntriesByEntries(baseDn, entries);
		} catch (Exception ex) {
			return null;
		}
	}

	public static String encodeString(String str) {
		if ((str == null) || (str.length() == 0)) {
			return str;
		}
		try {
			return (new URI(null, str, null)).toString();
		} catch (URISyntaxException ex) {
			return null;
		}
	}

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

	public static String getPersonDisplayName(User person) {
		if (person == null) {
			return null;
		}

		if (StringHelper.isEmpty(person.getDisplayName())) {
			return person.getUid();
		}

		return person.getDisplayName();
	}

	public static boolean hasPermission(Object target, String action) {
		return CdiUtil.bean(PermissionService.class).hasPermission(target, action);
	}

	public static List<Map.Entry<?, ?>> toList(Map<?, ?> map) {
		return map != null ? new ArrayList<Map.Entry<?, ?>>(map.entrySet()) : null;
	}

	public static String trim(String str) {
		if ((str == null) || (str.length() == 0)) {
			return str;
		}
		return str.trim();
	}

	public static String trimToLength(String str, int maxLength) {
		if (str == null) {
			return str;
		}
		
		int length = str.length();
		if (length <= maxLength) {
			return str;
		}

		return str.substring(0, maxLength) + "...";
	}
	
	public static String convertLdapTimeToDisplay(String dn, String timeStamp) {
		if ((timeStamp == null) || (timeStamp.length() == 0)) {
			return timeStamp;
		}
		
		return CdiUtil.bean(PersistenceEntryManager.class).decodeTime(dn, timeStamp).toGMTString();
		
	}
    
    public static long hashCode(String value) {
        if ((value == null) || (value.length() == 0)) {
            return 0;
        }
        
        return value.hashCode();
        
    }

}
