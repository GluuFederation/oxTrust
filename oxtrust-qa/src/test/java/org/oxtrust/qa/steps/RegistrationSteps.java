package org.oxtrust.qa.steps;

import org.oxtrust.qa.pages.configuration.RegistrationManagePage;
import org.oxtrust.qa.pages.login.HomePage;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;

public class RegistrationSteps extends BaseSteps {

	private HomePage homePage = new HomePage();

	private RegistrationManagePage registrationManagePage = new RegistrationManagePage();
	
	@Before
	public void setup(Scenario scenario) {
		startRecorder(scenario);
	}

	@And("^I go to registration manage page$")
	public void goToRegistrationManagePage() {
		homePage.goToManageRegistrationMenuPage();
	}

	@Then("^I should see that the captcha status is '(.+)'")
	public void checkCaptcha(String status) {
		registrationManagePage.checkCaptchaIsEnable(status);
	}

	@Then("^I should see that the registration status is '(.+)'")
	public void checkRegistration(String status) {
		registrationManagePage.checkRegistrationAttribIsEnable(status);
	}

	@Then("^I should see that the site key is '(.+)'")
	public void checkSiteKey(String key) {
		registrationManagePage.checkSiteKey(key);
	}

	@Then("^I should see that the site secret is '(.+)'")
	public void checkSiteSecret(String secret) {
		registrationManagePage.checkSiteSecret(secret);
	}

	@Then("^I should see that the css location is '(.+)'")
	public void checkCssLocation(String location) {
		registrationManagePage.checkCssLocation(location);
	}

	@Then("^I should see that the js location is '(.+)'")
	public void checkJsLocation(String location) {
		registrationManagePage.checkJsLocation(location);
	}

	@After
	public void clear(Scenario scenario) {
		homePage.takeScreenShot(scenario);
		stopRecorder();
		homePage.clear();
	}

}
