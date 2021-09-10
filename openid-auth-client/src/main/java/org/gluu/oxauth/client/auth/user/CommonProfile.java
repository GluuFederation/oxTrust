/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxauth.client.auth.user;

/**
 * This class contains common attributes
 * 
 * @author Yuriy Movchan 11/14/2014
 */
public class CommonProfile extends UserProfile {

	private static final long serialVersionUID = 470255919465444352L;

	public void setUserName(final String userName) {
		addAttribute("username", userName);
	}

	public String getUserName() {
		return (String) getAttribute("username");
	}

	public void setEmail(final String email) {
		addAttribute("email", email);
	}

	public String getEmail() {
		return (String) getAttribute("email");
	}

	public void setFirstName(final String firstName) {
		addAttribute("first_name", firstName);
	}

	public String getFirstName() {
		return (String) getAttribute("first_name");
	}

	public void setFamilyName(final String familyName) {
		addAttribute("family_name", familyName);
	}

	public String getFamilyName() {
		return (String) getAttribute("family_name");
	}

	public void setDisplayName(final String displayName) {
		addAttribute("display_name", displayName);
	}

	public String getDisplayName() {
		return (String) getAttribute("display_name");
	}

	public void setLocale(final String locale) {
		addAttribute("locale", locale);
	}

	public String getLocale() {
		return (String) getAttribute("locale");
	}

	public void setZone(final String zone) {
		addAttribute("zone", zone);
	}

	public String getZone() {
		return (String) getAttribute("zone");
	}

}
