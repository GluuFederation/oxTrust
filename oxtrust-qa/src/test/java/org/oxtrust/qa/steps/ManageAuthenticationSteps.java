package org.oxtrust.qa.steps;

import org.oxtrust.qa.pages.configuration.authentication.CasProtocolPage;
import org.oxtrust.qa.pages.configuration.authentication.DefaultAuthenMethodPage;
import org.oxtrust.qa.pages.configuration.authentication.LDAPAuthenticationPage;
import org.oxtrust.qa.pages.configuration.authentication.PassportAuthenticationPage;
import org.oxtrust.qa.pages.login.HomePage;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class ManageAuthenticationSteps extends BaseSteps {
	private HomePage homePage = new HomePage();

	private PassportAuthenticationPage authenticationPage = new PassportAuthenticationPage();
	private DefaultAuthenMethodPage defaultAuthenMethodPage = new DefaultAuthenMethodPage();
	private CasProtocolPage casProtocolPage = new CasProtocolPage();
	private LDAPAuthenticationPage ldapAuthenticationPage = new LDAPAuthenticationPage();

	@When("^I go to strategy page$")
	public void goToAuthenticationManagePage() {
		homePage.goToManageAutheticationMenuPage();
		authenticationPage.selectTab("Passport Authentication Method");
	}

	@When("^I go to default authentication page")
	public void goToDefaultAuthenMethodPage() {
		homePage.goToManageAutheticationMenuPage();
		authenticationPage.selectTab("Default Authentication Method");
	}

	@When("^I go to CAS protocol page")
	public void goToCasProtocolPage() {
		homePage.goToManageAutheticationMenuPage();
		authenticationPage.selectTab("CAS Protocol");
	}

	@When("^I go to LDAP Authenticiation page")
	public void goToLDAPAuthenticationPage() {
		homePage.goToManageAutheticationMenuPage();
		authenticationPage.selectTab("Manage LDAP Authentication");
	}

	@And("^I add new strategy named '(.+)' with id '(.+)' and secret '(.+)'$")
	public void addNewStrategy(String name, String id, String secret) {
		authenticationPage.addStrategy(name, id, secret);
	}

	@And("^I delete the strategy named '(.+)'$")
	public void deleteStrategy(String name) {
		authenticationPage.deleteStrategy(name);
	}

	@Then("^I should not see a strategy named '(.+)' in the list")
	public void strategyIsNotPresent(String name) {
		authenticationPage.assertStrategyIsNotPresent(name);
	}

	@Then("^I should see a strategy named '(.+)' in the list")
	public void strategyIsPresent(String name) {
		authenticationPage.assertStrategyIsPresent(name);
	}

	@And("^I enable passport status to '(.+)'$")
	public void setPassportStatus(String status) {
		authenticationPage.setPassportStatus(status);
	}

	@And("^I save the passport status changed$")
	public void save() {
		authenticationPage.save();
	}

	@And("^I should see default acr set to '(.+)'")
	public void checkDefaultAcr(String defaultAcr) {
		defaultAuthenMethodPage.checkDefaultAcr(defaultAcr);
	}

	@And("^I set to default acr to '(.+)'$")
	public void setDefaultAcr(String acr) {
		defaultAuthenMethodPage.setDefaultAcr(acr);
	}

	@And("^I set to oxtrust acr to '(.+)'$")
	public void setOxtrustAcr(String acr) {
		defaultAuthenMethodPage.setOxtrustAcr(acr);
	}

	@And("^I should see oxtrust acr set to '(.+)'")
	public void checkOxtrustAcr(String acr) {
		defaultAuthenMethodPage.checkOxtrustAcr(acr);
	}

	@And("^I save the default method configuration$")
	public void saveConfiguration() {
		defaultAuthenMethodPage.save();
	}

	@Then("^I should see that the cas protocol is enable$")
	public void checkStatusIsOn() {
		casProtocolPage.assertStatusIsEnabled();
	}

	@Then("^I should see that the cas protocol is disable$")
	public void checkStatusIsActive() {
		casProtocolPage.assertStatusIsDisabled();
	}

	@And("^I should see that service type is '(.+)'$")
	public void checkServiceType(String type) {
		casProtocolPage.assertServiceTypeIs(type);
	}

	@And("^I should see that base url end with '(.+)'$")
	public void checkBaseurl(String url) {
		casProtocolPage.assertBaseUrlEndWith(url);
	}

	@And("^I save the cas config$")
	public void saveCasConfig() {
		casProtocolPage.saveCasSetting();
	}

	@And("^I save the cas configuration update$")
	public void saveWholeConfiguration() {
		casProtocolPage.save();
	}
	// Ldap authentivation steps

	@Then("^I should see an ldap source named '(.+)' with bindDn '(.+)' with maxConn '(.+)' with primary key '(.+)' with local primary key '(.+)' with servers '(.+)' with basedn '(.+)' and ssl '(.+)'$")
	public void checkSourceLdapServer(String name, String bindDn, String maxCon, String pKey, String lPKey,
			String servers, String baseDn, String useSSl) {
		ldapAuthenticationPage.checkLdapSourceServer(name, bindDn, maxCon, pKey, lPKey, servers, baseDn, useSSl);
	}

	@Then("^I should not see an ldap source named '(.+)'$")
	public void checkSourceNonExistence(String name) {
		ldapAuthenticationPage.assertSourceDontExist(name);
	}

	@Then("^I add an ldap source named '(.+)' with bindDn '(.+)' with maxConn '(.+)' with primary key '(.+)' with local primary key '(.+)' with servers '(.+)' with basedn '(.+)' and ssl '(.+)'$")
	public void addSourceLdapServer(String name, String bindDn, String maxCon, String pKey, String lPKey,
			String servers, String baseDn, String useSSl) {
		ldapAuthenticationPage.addLdapSourceServer(name, bindDn, maxCon, pKey, lPKey, servers, baseDn, useSSl);
		ldapAuthenticationPage.save();
	}

	@And("^I click the add source server button$")
	public void clickOnSourceServerAddButton() {
		ldapAuthenticationPage.clickOnAddSourceServer();
	}

	@Then("^I delete the ldap source named '(.+)'$")
	public void deleteSourceServer(String name) {
		ldapAuthenticationPage.deleteSourceServer(name);
	}

	@After
	public void clear(Scenario scenario) {
		homePage.takeScreenShot(scenario);
		homePage.clear();
	}

}
