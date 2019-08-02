package org.oxtrust.qa.steps;

import org.oxtrust.qa.pages.login.HomePage;
import org.oxtrust.qa.pages.saml.NameIdAddPage;
import org.oxtrust.qa.pages.saml.NameIdConfigurationPage;
import org.oxtrust.qa.pages.saml.TrAddPage;
import org.oxtrust.qa.pages.saml.TrManagePage;
import org.oxtrust.qa.pages.saml.TrUpdatePage;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class SamlSteps extends BaseSteps {
	private HomePage homePage = new HomePage();

	private TrAddPage trAddPage = new TrAddPage();

	private TrUpdatePage trUpdatePage = new TrUpdatePage();

	private TrManagePage trManagePage = new TrManagePage();

	private NameIdConfigurationPage namdIdConfigurationPage = new NameIdConfigurationPage();

	private NameIdAddPage nameIdAddPage = new NameIdAddPage();

	@When("^I go to tr add page$")
	public void goToTrAddPage() {
		homePage.goSamlTrAddPage();
	}

	@When("^I go to tr list page$")
	public void goToTrListPage() {
		homePage.goSamlTrListPage();
	}

	@And("^I wait for tr validation$")
	public void waitForTrValidation() {
		trAddPage.waitForTrValidation();
	}

	@Then("^I set '(.+)' as display name$")
	public void setTrDisplayName(String dn) {
		trAddPage.setDisplayName(dn);
	}

	@Then("^I edit the display name to '(.+)'$")
	public void editTrDisplayName(String dn) {
		trUpdatePage.setDisplayName(dn);
	}

	@Then("^I edit the description to '(.+)'$")
	public void editTrDescription(String des) {
		trUpdatePage.setDescription(des);
	}

	@Then("^I set '(.+)' as description$")
	public void setTrDesc(String des) {
		trAddPage.setDescription(des);
	}

	@Then("^I set '(.+)' as entity type$")
	public void setEntityType(String type) {
		trAddPage.setEntityType(type);
	}

	@Then("^I set '(.+)' as metadata location$")
	public void setMetadataType(String type) {
		trAddPage.setMetadataType(type);
	}

	@And("^I select '(.+)' as federation tr$")
	public void selectFederation(String federation) {
		trAddPage.selectFederation(federation);
	}

	@Then("^I set the metadata$")
	public void setMetadata() {
		trAddPage.setMetadata();
	}

	@And("^I set the federation metadata$")
	public void setFederationMetadata() {
		trAddPage.setFederationMetatData();
	}

	@Then("^I configure sp with '(.+)' profile$")
	public void configureSp(String profile) {
		trAddPage.configureRp(profile);
	}

	@And("^I release the following attributes '(.+)'$")
	public void releaseAttributes(String attributes) {
		trAddPage.releaseAttributes(attributes);
	}

	@And("^I save the current tr$")
	public void save() {
		trAddPage.save();
	}

	@And("^I update the current tr$")
	public void update() {
		trUpdatePage.update();
	}

	@And("^I search for tr named '(.+)'")
	public void searchTr(String pattern) {
		trManagePage.searchFor(pattern);
	}

	@Then("^I should see a tr with display name '(.+)' in the list$")
	public void checkTrExistInList(String trName) {
		trManagePage.assertTrExist(trName);
	}

	@Then("^I should not see a tr with display name '(.+)' in the list$")
	public void checkTrNotExistInList(String trName) {
		trManagePage.assertTrNotExist(trName);
	}

	@When("^I start the edition of tr named '(.+)'")
	public void startTrEdition(String name) {
		trManagePage.goToDetailOf(name);
	}

	@When("^I delete the tr named '(.+)'$")
	public void deleteTr(String name) {
		trManagePage.goToDetailOf(name);
		trUpdatePage.delete();
	}

	@When("^I go to custom nameId configuration page$")
	public void goToNameIdConfigPage() {
		homePage.goNameIdConfigurePage();
	}

	@And("^I delete the nameID name '(.+)'$")
	public void deleteConfiguredNamedID(String name) {
		namdIdConfigurationPage.delete(name);
		trUpdatePage.save();
	}

	@And("^I start the process to add new name id configuration$")
	public void startAddingNewNamedId() {
		namdIdConfigurationPage.startAddingNewConfiguration();
	}

	@And("^I add a namedid with source attrib '(.+)' with name '(.+)' with type '(.+)' and enable '(.+)'$")
	public void addNewNamedId(String source, String name, String type, String enable) {
		nameIdAddPage.addNamedId(source, name, type, enable);
	}

	@And("^I save the namedid configuration$")
	public void saveNameId() {
		nameIdAddPage.save();
	}

	@Then("^I should see a named id named '(.+)' in the list$")
	public void checkNamedIdExist(String name) {
		nameIdAddPage.assertNamedExist(name);
	}

	@Then("^I should not see a named id named '(.+)' in the list$")
	public void checkNamedIdDontExist(String name) {
		nameIdAddPage.assertNamedDontExist(name);
	}

	@And("^I select '(.+)' as entity id$")
	public void selectEntityId(String value) {
		trAddPage.selectId(value);
	}

	@After
	public void clear(Scenario scenario) {
		homePage.takeScreenShot(scenario);
		homePage.clear();
	}

}
