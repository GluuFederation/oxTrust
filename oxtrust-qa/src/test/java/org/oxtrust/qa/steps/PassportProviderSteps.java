package org.oxtrust.qa.steps;

import org.oxtrust.qa.pages.login.HomePage;
import org.oxtrust.qa.pages.passport.PassportPage;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class PassportProviderSteps extends BaseSteps {

	private HomePage homePage = new HomePage();

	private PassportPage passportPage = new PassportPage();
	
	@Before
	public void setup(Scenario scenario) {
		startRecorder(scenario);
	}

	@When("^I go to passport providers list$")
	public void goToProvidersPage() {
		homePage.goToPassportProviderListPage();
	}

	@When("^I start the process to add new provider$")
	public void goToProviderAddPage() {
		passportPage.clickAddButton();
	}

	@When("^I set display name '(.+)'$")
	public void setDisplayName(String value) {
		passportPage.fillTextFillByClass("DisplayName", value);
	}

	@When("^I set type '(.+)'$")
	public void setType(String value) {
		passportPage.selectBoxByClass("ProviderType", value);
	}

	@When("^I set client id to '(.+)'$")
	public void setId(String id) {
		passportPage.fillTextFillByClass("propertyValueTextBox", id, 0);
	}

	@When("^I set client secret to '(.+)'$")
	public void setSecret(String secret) {
		passportPage.fillTextFillByClass("propertyValueTextBox", secret, 1);
	}

	@When("^I apply the change$")
	public void save() {
		passportPage.save();
	}

	@When("^I delete it$")
	public void delete() {
		passportPage.delete();
	}

	@Then("^I should see a provider named '(.+)'$")
	public void searchProviderInList(String provider) {
		passportPage.assertProviderExist(provider);
	}

	@Then("^I should not see a provider named '(.+)'$")
	public void searchProviderNotInList(String provider) {
		passportPage.assertProviderNotExist(provider);
	}

	@And("^I start the process to edit the provider named '(.+)'$")
	public void editCurrentClient(String provider) {
		passportPage.editProvider(provider);
	}
	
	@After
	public void clear(Scenario scenario) {
		homePage.takeScreenShot(scenario);
		stopRecorder();
		homePage.clear();
	}

}
