package org.oxtrust.qa.steps;


import org.oxtrust.qa.pages.configuration.CertificatesPage;
import org.oxtrust.qa.pages.login.HomePage;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;

public class CertificateSteps extends BaseSteps {

	private HomePage homePage = new HomePage();
	private CertificatesPage certificatesPage = new CertificatesPage();

	@And("^I go to certificates page$")
	public void goToCertPage() {
		homePage.goTocertificatesMenuPage();
	}

	@Then("^I should see '(.+)' certs in the list$")
	public void verifyCertsSize(String size) {
		certificatesPage.assertThereAreCerts(size);
	}

	@And("^I should see a cert named '(.+)'$")
	public void checkCert(String certName) {
		certificatesPage.assertCertExist(certName);
	}

	@After
	public void clear(Scenario scenario) {
		homePage.takeScreenShot(scenario);
		homePage.clear();
	}

}
