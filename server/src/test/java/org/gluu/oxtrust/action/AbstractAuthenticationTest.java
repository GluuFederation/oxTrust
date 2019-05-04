/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import javax.inject.Inject;

import org.gluu.oxtrust.action.Authenticator;
import org.gluu.oxtrust.action.LogoutAction;
import org.gluu.oxtrust.security.Identity;

/**
 * Base class for all seam test which require authorization
 * 
 * @author Yuriy Movchan
 */
public abstract class AbstractAuthenticationTest extends ConfigurableTest {
	
	@Inject
	private Identity identity;

	@Inject
	private Authenticator authenticator;

    @Inject
    private LogoutAction logoutAction;

	/**
	 * Make attempt to login using specified userPropsKey. 
	 * For this method we must define in properties file 2 key/value pairs:
	 * userPropsKey.uid and userPropsKey.password. 
	 * 
	 * @param userPropsKey Prefix of the key in properties file
	 */
	protected void loginAndCheckLoggedIn(String userPropsKey) {
		checkLoggedIn(false);
		checkLoginUser(testData.getString(userPropsKey + ".uid"), testData.getString(userPropsKey + ".password"));
		checkLoggedIn(true);
	}

	/**
	 * Check if user logged in
	 * 
	 * @param loggedIn Current user logged in state
	 */
	protected void checkLoggedIn(final boolean loggedIn) {
		assertEquals(identity.isLoggedIn(), loggedIn);
	}

	/**
	 * Login using specified user credentials
	 * 
	 * @param user User login name
	 * @param password User login password
	 */
	protected void checkLoginUser(final String user, final String password) {
		identity.getOauthData().setUserUid(user);
		identity.getOauthData().setIdToken("dummy_id_token");

		authenticator.authenticate();
		
		assertTrue(identity.isLoggedIn());
	}

	/**
	 * Process user logout
	 */
	protected void logoutUser() throws Exception {
	    logoutAction.postLogout();

		assertFalse(identity.isLoggedIn());
	}

}
