package org.oxtrust.qa.steps;

import org.oxtrust.qa.pages.configuration.AttributesPage;
import org.oxtrust.qa.pages.login.HomePage;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class AttributesSteps extends BaseSteps {
	private HomePage homePage = new HomePage();
	private AttributesPage attributesPage = new AttributesPage();
	
	@Before
	public void setup(Scenario scenario) {
		startRecorder(scenario);
	}

	@When("^I go to Attributes page$")
	public void goToAttributesPage() {
		homePage.goToAttributesMenuPage();

	}

	@Then("^I want to see all attributes$")
	public void seeAllAttributes() {
		attributesPage.showAllAttributes();

	}

	@When("^I want to check active attributes$")
	public void seeActiveAttributes() {
		attributesPage.checkActiveAttributes();
	}

	@When("^I want to check inactive attributes$")
	public void seeInactiveAttributes() {
		attributesPage.checkInactiveAttributes();
	}

	@And("^I click on the first listed attribute$")
	public void seeFirstListedAttribute() {
		try {
			attributesPage.clickFirstListedAttribute();
		} catch (org.openqa.selenium.WebDriverException ex) {
			attributesPage.clickFirstListedAttribute();
		}

	}

	@And("^I update an attribute$")
	public void updateAnAttribute() {
		attributesPage.updateAttribute();
	}

	@Then("^I want to register an attribute$")
	public void registerAnAttribute() {
		attributesPage.registerAttribute();
	}

	@And("^I register SAML1 URI: '(.+)'$")
	public void saml1Value(String saml1ValueSet) {
		attributesPage.registerSAML1(saml1ValueSet);
	}

	@And("^I register SAML2 URI: '(.+)'$")
	public void saml2Value(String saml2ValueSet) {
		attributesPage.registerSAML2(saml2ValueSet);
	}

	@And("^I register a display name: '(.+)'$")
	public void displayName(String displayNameSet) {
		attributesPage.registerDisplayName(displayNameSet);
	}

	@And("^I set a type$")
	public void typeSet() {
		attributesPage.chooseType();
	}

	@And("^I choose the edit type$")
	public void editTypeValue() {
		attributesPage.editType();
	}

	@And("^I choose the view type$")
	public void viewTypeValue() {
		attributesPage.viewType();
	}

	@And("^I choose the usage type$")
	public void usageValue() {
		attributesPage.usageType();
	}

	@And("^I choose the multivalued option$")
	public void multivaluedValue() {
		attributesPage.multivalued();
	}

	@And("^I register a claim name: '(.+)'$")
	public void claimName(String claimNameSet) {
		attributesPage.claimName(claimNameSet);
	}

	@And("^I choose the SCIM attribute option$")
	public void scimAttributeValue() {
		attributesPage.scimAttribute();
	}

	@And("^I set the Attribute description: '(.+)'$")
	public void descriptionValue(String descriptionSet) {
		attributesPage.setAttributeDescription(descriptionSet);
	}

	@And("^I enable custom validation$")
	public void enableCustValidation() {
		attributesPage.enableCustomValidation();
	}

	@And("^I set the validation RegExp: '(.+)'$")
	public void setRegExpValue(String expValueSet) {
		attributesPage.setValidationRegExp(expValueSet);
	}

	@And("^I enable a tooltip for this attribute")
	public void enableThisTooltip() {
		attributesPage.enableTooltip();
	}

	@And("^I set the tooltip text to: '(.+)'$")
	public void setTooltipText(String textValue) {
		attributesPage.tooltipText(textValue);
	}

	@And("^I set the minimum length: '(.+)'$")
	public void minimumLengthValue(String minLengthSet) {
		attributesPage.minimumLength(minLengthSet);
	}

	@And("^I set the maximum length: '(.+)'$")
	public void maximumLengthValue(String maxLengthSet) {
		attributesPage.maximumLength(maxLengthSet);
	}

	@And("^I set the regex pattern: '(.+)'$")
	public void regexPatternSet(String regexSet) {
		attributesPage.regexPattern(regexSet);
	}

	@And("^I choose the inactive status$")
	public void chooseInactiveStatus() {
		attributesPage.status();
	}

	@And("^I click on the cancel button$")
	public void cancelRegistrationButton() {
		attributesPage.cancelButton();
	}

	@Then("^I check if an attribute exists with the following description: '(.+)'$")
	public void checkCreatedAttribute(String descValue) {
		attributesPage.checkAttributeDescriptionExists(descValue);
	}

	@And("^I delete the attribute$")
	public void deleteAnAttribute() {
		attributesPage.deleteAttribute();
	}
	
	@After
	public void clear(Scenario scenario) {
		homePage.takeScreenShot(scenario);
		stopRecorder();
		homePage.clear();
	}
}
