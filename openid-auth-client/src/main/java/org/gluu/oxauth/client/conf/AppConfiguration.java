/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxauth.client.conf;

import java.util.List;

/**
 * Base application configuration
 * 
 * @author Yuriy Movchan
 * @version 0.1, 11/02/2015
 */
public interface AppConfiguration {

	String getApplicationName();

	String getOpenIdProviderUrl();

	String getOpenIdClientId();
	String getOpenIdClientPassword();

	List<String> getOpenIdClientScopes();

}
