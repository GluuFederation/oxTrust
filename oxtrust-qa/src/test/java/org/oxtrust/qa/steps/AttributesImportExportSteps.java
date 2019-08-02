package org.oxtrust.qa.steps;

import org.oxtrust.qa.pages.configuration.attributesImExport.AttributesExportPage;
import org.oxtrust.qa.pages.configuration.attributesImExport.AttributesImportPage;
import org.oxtrust.qa.pages.login.HomePage;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;

public class AttributesImportExportSteps extends BaseSteps {

	private HomePage homePage = new HomePage();
	private AttributesExportPage attributesExportPage = new AttributesExportPage();
	private AttributesImportPage attributesImportPage = new AttributesImportPage();

	@Before
	public void setup(Scenario scenario) {
		startRecorder(scenario);
	}

	@And("^I go to Attributes export page$")
	public void goToExportPage() {
		homePage.goToImportExportAttributesMenuPage();
		attributesExportPage.clickExportButton();
	}

	@And("^I go to Attributes import page$")
	public void goToAttributeImportPage() {
		homePage.goToImportExportAttributesMenuPage();
	}

	@And("^I import the file named '(.+) from the download directory$")
	public void importAttributes(String fileName) {
		attributesImportPage.importAttributesFromFile(fileName);
	}

	@Then("^I validate and import those attributes$")
	public void validateImport() {
		attributesImportPage.validateAndImport();
	}

	@And("^I pick the attribute named '(.+)'$")
	public void pickAttributeToExport(String name) {
		attributesExportPage.pickAttributeToImport(name);
	}

	@And("^I export them$")
	public void export() {
		attributesExportPage.export();
	}

	@Then("^I should see a file named '(.+)' in downloads folder")
	public void checkFileDownloaded(String fileName) {
		attributesExportPage.verifyFile(fileName);
	}

	@After
	public void clear(Scenario scenario) {
		homePage.takeScreenShot(scenario);
		stopRecorder();
		homePage.clear();
	}

}
