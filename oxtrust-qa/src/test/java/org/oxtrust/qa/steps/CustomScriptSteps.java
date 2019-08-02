package org.oxtrust.qa.steps;


import org.oxtrust.qa.pages.configuration.CustomScriptManagePage;
import org.oxtrust.qa.pages.login.HomePage;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.When;

public class CustomScriptSteps extends BaseSteps {

	HomePage homePage=new HomePage();
	CustomScriptManagePage customScriptManagePage=new CustomScriptManagePage();
	
	@Before
	public void setup(Scenario scenario) {
		startRecorder(scenario);
	}

	@When("^I go to Manage Custom Script$")
	public void goToCustomManageScriptPage() {
		homePage.goToManageCustomScriptsMenuPage();
	}

	@When("^I select the '(.+)' tab$")
	public void goToCustomScriptAddPage(String tabText) {
		customScriptManagePage.pickTab(tabText);
	}

	@When("^I click the add custom script button$")
	public void clickAddScriptButton() {
		customScriptManagePage.hitAddButton();
	}

	@When("^I set the custom script name to '(.+)'$")
	public void setScriptName(String name) {
		customScriptManagePage.setName(name);
	}

	@And("^I set the custom script description to '(.+)'$")
	public void setScriptDescription(String description) {
		customScriptManagePage.setDescription(description);
	}

	@And("^I set the custom script level to '(.+)'$")
	public void setScriptLevel(String level) {
		customScriptManagePage.setLevel(level);
	}

	@And("^I set the custom script location type to '(.+)'$")
	public void setScriptLocationType(String type) {
		customScriptManagePage.setLocationType(type);
	}

	@And("^I should see a custom script named '(.+)' in '(.+)' tab$")
	public void checkScriptExistance(String scriptName, String tabName) {
		customScriptManagePage.assertScriptExist(scriptName, tabName);
	}
	
	@And("^I should not see a custom script named '(.+)' in '(.+)' tab$")
	public void checkScriptNonExistance(String scriptName, String tabName) {
		customScriptManagePage.assertScriptDontExist(scriptName, tabName);
	}

	@And("^I delete the custom script named '(.+)' on '(.+)' tab$")
	public void deleteScript(String scriptName, String tabName) {
		customScriptManagePage.deleteScript(scriptName, tabName);
	}
	
	@And("^I enable the custom script named '(.+)' on '(.+)' tab$")
	public void enableScript(String scriptName, String tabName) {
		customScriptManagePage.enableScript(scriptName, tabName);
	}
	
	@And("^I add new property named '(.+)' with value '(.+)'$")
	public void addProperty(String label, String value) {
		customScriptManagePage.addNewproperty(label,value);
	}

	@And("^I set the custom script usage type to '(.+)'$")
	public void setScriptUsageType(String type) {
		customScriptManagePage.setUsageType(type);
	}

	@And("^I set the custom script content to '(.+)'$")
	public void setScriptContent(String text) {
		customScriptManagePage.setContent(text);
	}

	@And("^I save the custom script$")
	public void saveScript() {
		customScriptManagePage.save();
	}

	@And("^I enable the script$")
	public void enableScript() {
		customScriptManagePage.enable();
	}
	@After
	public void clear(Scenario scenario) {
		homePage.takeScreenShot(scenario);
		stopRecorder();
		homePage.clear();
	}
}
