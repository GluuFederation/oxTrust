package org.oxtrust.qa.steps;

import org.oxtrust.qa.pages.login.HomePage;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.en.Then;

public class HomeSteps extends BaseSteps {
	private HomePage homePage = new HomePage();

	@Then("^I should see that there '(.+)' data in the dashboard$")
	public void checkDashboardStatus(String value) {
		homePage.checkDashboard(value);
	}

	@After
	public void clear(Scenario scenario) {
		homePage.takeScreenShot(scenario);
		homePage.clear();
	}

}
