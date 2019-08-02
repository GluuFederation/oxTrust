package org.oxtrust.qa.steps;

import org.oxtrust.qa.pages.configuration.LogViewerConfigPage;
import org.oxtrust.qa.pages.login.HomePage;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;

public class LogViewerSteps extends BaseSteps {

	private HomePage homePage = new HomePage();
	private LogViewerConfigPage logViewerConfigPage = new LogViewerConfigPage();

	@And("^I go to log viewer configuration status page$")
	public void goToLogViewerConfiguration() {
		homePage.goToLogViewerCongifigurationMenuPage();
	}

	@Then("^I should see that the oxTrust external log4j is empty$")
	public void checkOxtrustLog4jLocation() {
		logViewerConfigPage.checkOxTrustExternalLog4jLocation();
	}

	@Then("^I should see that the oxAuth external log4j is empty$")
	public void checkOxAuthLog4jLocation() {
		logViewerConfigPage.checkOxAuthExternalLog4jLocation();
	}

	@And("^I add a new log template named '(.+)' with value '(.+)'$")
	public void addNewLogTemplate(String name, String value) {
		logViewerConfigPage.addNewLogTemplate(name, value);
	}

	@And("^I should see a log template with name '(.+)' and value '(.+)'$")
	public void checkLogTemplate(String name, String value) {
		logViewerConfigPage.checkTemplate(name, value);
	}

	@And("^I should not see a log template with name '(.+)' and value '(.+)'$")
	public void checkLogTemplateNotExist(String name, String value) {
		logViewerConfigPage.checkTemplateNotExist(name, value);
	}

	@And("^I delete the log template named '(.+)'$")
	public void removerLogTemplate(String name) {
		logViewerConfigPage.delete(name);
	}

	@After
	public void clear(Scenario scenario) {
		homePage.takeScreenShot(scenario);
		homePage.clear();
	}
}
