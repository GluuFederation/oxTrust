package org.oxtrust.qa.steps;

import org.oxtrust.qa.pages.login.HomePage;
import org.oxtrust.qa.pages.passport.PassportPage;

import cucumber.api.java.After;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class PassportIdpSteps extends BaseSteps {

	private HomePage homePage = new HomePage();

	private PassportPage passportPage = new PassportPage();

	@When("^I go to passport idp page$")
	public void goToProvidersPage() {
		homePage.goToPassportIdpPage();
	}

	@Then("^I should see that the endpoint is not empty$")
	public void checkEndpointIsNotEmpty() {
		passportPage.assertEndPoinIsNotEmpty();
	}

	@Then("^I should see that the acr is not empty$")
	public void checkAcrIsNotEmpty() {
		passportPage.assertAcrIsNotEmpty();
	}

	@Then("^I select '(.+)'$")
	public void selectClient(String client) {
		passportPage.selectValue(client);
	}

	@Then("^I save the idp config$")
	public void save() {
		passportPage.save();
	}
	
	@After
	public void clear() {
		homePage.clear();
	}

}
