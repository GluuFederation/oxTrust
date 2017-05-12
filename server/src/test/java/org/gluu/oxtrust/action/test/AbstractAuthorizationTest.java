/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action.test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import javax.enterprise.context.Conversation;
import javax.inject.Inject;

import org.gluu.oxtrust.security.Identity;
import org.xdi.model.security.Credentials;

/**
 * Base class for all seam test which require authorization
 * 
 * @author Yuriy Movchan
 */
public abstract class AbstractAuthorizationTest extends ConfigurableTest {
	
	@Inject
	private Identity identity;

	@Inject
	private Conversation conversation;

	/**
	 * Make attempt to login using specified userPropsKey. 
	 * For this method we must define in properties file 2 key/value pairs:
	 * userPropsKey.uid and userPropsKey.password. 
	 * 
	 * @param userPropsKey Prefix of the key in properties file
	 * @throws java.lang.Exception 
	 */
	protected void loginAndCheckLoggedIn(String userPropsKey) throws Exception {
		checkLoggedIn(false);
		checkLoginUser(testData.getString(userPropsKey + ".uid"), testData.getString(userPropsKey + ".password"));
		checkLoggedIn(true);
	}

	/**
	 * Check if user logged in
	 * 
	 * @param loggedIn Current user logged in state
	 * @throws java.lang.Exception
	 */
	protected void checkLoggedIn(final boolean loggedIn) throws Exception {
		assertTrue(identity.isLoggedIn());
	}

	/**
	 * Login using specified user credentials
	 * 
	 * @param user User login name
	 * @param password User login password
	 * @throws java.lang.Exception 
	 */
	protected void checkLoginUser(final String user, final String password) throws Exception {
		Credentials credentials = identity.getCredentials();
		credentials.setUsername(user);
		credentials.setPassword(password);
		
		identity.login();
		
		assertTrue(identity.isLoggedIn());
	}

	/**
	 * Check user logout
	 * 
	 * @throws java.lang.Exception 
	 */
	protected void logoutUser() throws Exception {
		identity.logout();

		assertFalse(identity.isLoggedIn());
	}

}
