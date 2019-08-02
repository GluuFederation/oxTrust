package org.oxtrust.qa.steps;

import org.oxtrust.qa.pages.configuration.OxAuthSettingPage;
import org.oxtrust.qa.pages.login.HomePage;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class OxAuthSettingSteps extends BaseSteps {

	private HomePage homePage = new HomePage();

	private OxAuthSettingPage oxAuthSettingPage = new OxAuthSettingPage();

	@When("^I go to oxauth setting configuration page$")
	public void goToSmtpPage() {
		homePage.goToOrganisationConfigurationMenuPage();
		oxAuthSettingPage.selectTab("oxAuth Settings");
	}

	@Then("^I should that the server ip is empty$")
	public void checkIpIsEmpty() {
		oxAuthSettingPage.checkServerIpIsEmpty();
	}

	@And("^I set the oxauth logging level to '(.+)'$")
	public void setOxAuthLogLevel(String level) {
		oxAuthSettingPage.setLogLevel(level);
	}

	@After
	public void clear(Scenario scenario) {
		homePage.takeScreenShot(scenario);
		homePage.clear();
	}

}
