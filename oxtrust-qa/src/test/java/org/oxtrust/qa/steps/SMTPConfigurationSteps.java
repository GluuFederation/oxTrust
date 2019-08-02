package org.oxtrust.qa.steps;

import org.oxtrust.qa.pages.configuration.OrganizationConfigurationPage;
import org.oxtrust.qa.pages.configuration.SMTPConfigPage;
import org.oxtrust.qa.pages.login.HomePage;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.en.And;
import cucumber.api.java.en.When;

public class SMTPConfigurationSteps extends BaseSteps {
	HomePage homePage = new HomePage();
	SMTPConfigPage smtpConfigPage = new SMTPConfigPage();
	OrganizationConfigurationPage organizationConfigurationPage = new OrganizationConfigurationPage();

	@When("^I go to smtp configuration page$")
	public void goToSmtpPage() {
		homePage.goToOrganisationConfigurationMenuPage();
		organizationConfigurationPage.selectTab("SMTP Server Configuration");
	}

	@And("^I set '(.+)' as smtp host$")
	public void setSmtpHost(String host) {
		smtpConfigPage.setSmtpHost(host);
	}

	@And("^I set '(.+)' as from name$")
	public void setSmtpFromName(String name) {
		smtpConfigPage.setSmtpFromName(name);
	}

	@And("^I set '(.+)' as from email address$")
	public void setSmtpFromEmailAddress(String address) {
		smtpConfigPage.setSmtpFromEmailAddress(address);
	}

	@And("^I set '(.+)' as username$")
	public void setSmtpUserName(String name) {
		smtpConfigPage.setSmtpUserName(name);
	}

	@And("^I set '(.+)' as password$")
	public void setSmtpUserPwd(String pwd) {
		smtpConfigPage.setSmtpPassword(pwd);
	}

	@And("^I set '(.+)' as smtp port$")
	public void setSmtpPort(String port) {
		smtpConfigPage.setSmtpPort(port);
	}

	@And("^I ckeck require authentication$")
	public void setSmtRequireAuthentication() {
		smtpConfigPage.setSmtpRequireAuthentication(true);
	}

	@And("^I ckeck require ssl$")
	public void setSmtSSL() {
		smtpConfigPage.setSmtpRequireSSL(true);
	}

	@And("^I check trust server$")
	public void setSmtpTrustServer() {
		smtpConfigPage.setSmtpTrustServer(true);
	}

	@And("^I test the configuration$")
	public void testSmtpServer() {
		smtpConfigPage.test();
	}

	@And("^I save the configuration$")
	public void saveSmtpServer() {
		smtpConfigPage.update();
	}
	
	@After
	public void clear(Scenario scenario) {
		homePage.takeScreenShot(scenario);
		homePage.clear();
	}

}
