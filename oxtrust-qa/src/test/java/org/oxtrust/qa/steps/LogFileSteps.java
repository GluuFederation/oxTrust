package org.oxtrust.qa.steps;

import org.oxtrust.qa.pages.configuration.ViewLogFilePage;
import org.oxtrust.qa.pages.login.HomePage;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class LogFileSteps extends BaseSteps {

	private HomePage homePage = new HomePage();
	private ViewLogFilePage viewLogFilePage = new ViewLogFilePage();

	@When("^I go to view log file page$")
	public void goToScopeManagePage() {
		homePage.goToLogFileViewMenuPage();
	}

	@Then("^I should see log files named '(.+)' and '(.+)'")
	public void assertLogFilesExist(String oxTrustLogs, String oxAuthLogs) {
		viewLogFilePage.assertLogFilesExist(oxTrustLogs, oxAuthLogs);
	}

	@After
	public void clear(Scenario scenario) {
		homePage.takeScreenShot(scenario);
		homePage.clear();
	}

}
