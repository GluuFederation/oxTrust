package org.oxtrust.qa.steps;

import org.oxtrust.qa.pages.configuration.OxTrustSettingPage;
import org.oxtrust.qa.pages.login.HomePage;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class OxTrustSettingSteps extends BaseSteps {

	private HomePage homePage = new HomePage();

	private OxTrustSettingPage oxTrustSettingPage = new OxTrustSettingPage();
	
	@Before
	public void setup(Scenario scenario) {
		startRecorder(scenario);
	}

	@When("^I go to oxtrust setting configuration page$")
	public void goToOrganizationSettingPage() {
		homePage.goToOrganisationConfigurationMenuPage();
		oxTrustSettingPage.selectTab("Organization Settings");
	}

	@Then("^I should that the org name is not empty$")
	public void checkOrgNameIsNotEmpty() {
		oxTrustSettingPage.checkOrgNameIsNotEmpty();
	}

	@And("^I should that '(.+)' is the admin group$")
	public void checkgroup(String group) {
		oxTrustSettingPage.checkGroup(group);
	}

	@Then("^I set the default qa logo as organisation logo$")
	public void setOrgLogo() {
		oxTrustSettingPage.setLogo();
	}

	@Then("^I set the default qa logo as organisation favicon$")
	public void setOrgFavIcon() {
		oxTrustSettingPage.setFavIcon();
	}

	@And("^I set the new org name to '(.+)'$")
	public void setOrgName(String orgName) {
		oxTrustSettingPage.setOrgName(orgName);
	}

	@And("^I should that the org name is '(.+)'$")
	public void checkOrgName(String orgName) {
		oxTrustSettingPage.checkOrgName(orgName);
	}

	@And("^I set the oxtrust logging level to '(.+)'$")
	public void setOxTrustLogLevel(String level) {
		oxTrustSettingPage.setLogLevel(level);
	}

	@And("^I save the oxtrust configuration$")
	public void saveOxtrustConfig() {
		oxTrustSettingPage.save();
	}

	@After
	public void clear(Scenario scenario) {
		homePage.takeScreenShot(scenario);
		stopRecorder();
		homePage.clear();
	}
}
