package org.gluu.site.action.test;

import org.jboss.seam.core.Manager;
import org.jboss.seam.web.Session;

/**
 * Base class for all seam test which require authorization
 * 
 * @author Pankaj
 */
public abstract class AbstractAuthorizationTest extends ConfigurableTest {

	/**
	 * Initialize test data
	 * 
	 * @throws java.lang.Exception
	 */
	protected void initTest() throws Exception {
		super.initTest();
	}

	/**
	 * Make attempt to login using specified userPropsKey. 
	 * For this method we must define in properties file 2 key/value pairs:
	 * userPropsKey.uid and userPropsKey.password. 
	 * 
	 * @param userPropsKey Prefix of the key in properties file
	 * @throws java.lang.Exception 
	 */
	protected void loginAndCheckLoggedInFacesRequest(String userPropsKey) throws Exception {
		checkLoggedInFacesRequest(false);
		checkLoginUserFacesRequest(getConf().getString(userPropsKey + ".uid"), getConf().getString(userPropsKey + ".password"));
		checkLoggedInFacesRequest(true);
	}

	/**
	 * Check if user logged in
	 * 
	 * @param loggedIn Current user logged in state
	 * @throws java.lang.Exception
	 */
	protected void checkLoggedInFacesRequest(final boolean loggedIn) throws Exception {
		new FacesRequest() {

			@Override
			protected void invokeApplication() {
				assert !isSessionInvalid();
				assert getValue("#{identity.loggedIn}").equals(loggedIn);
			}

		}.run();
	}

	/**
	 * Login using specified user credentials
	 * 
	 * @param user User login name
	 * @param password User login password
	 * @throws java.lang.Exception 
	 */
	protected void checkLoginUserFacesRequest(final String user, final String password) throws Exception {
		new FacesRequest() {

			@Override
			protected void updateModelValues() throws Exception {
				assert !isSessionInvalid();
				setValue("#{identity.username}", user);
				setValue("#{identity.password}", password);
			}

			@Override
			protected void invokeApplication() {
				invokeAction("#{identity.login}");
			}

			@Override
			protected void renderResponse() {
				assert !Manager.instance().isLongRunningConversation();
				assert getValue("#{identity.loggedIn}").equals(true);
				assert !isSessionInvalid();
			}
		}.run();
	}

	/**
	 * Check user logout
	 * 
	 * @throws java.lang.Exception 
	 */
	protected void logoutUserFacesRequest() throws Exception {
		new FacesRequest() {

			@Override
			protected void invokeApplication() {
				assert !Manager.instance().isLongRunningConversation();
				assert !isSessionInvalid();
				invokeMethod("#{identity.logout}");
				assert Session.instance().isInvalid();
			}

			@Override
			protected void renderResponse() {
				assert getValue("#{identity.loggedIn}").equals(false);
				assert Session.instance().isInvalid();
			}
		}.run();
	}

}
