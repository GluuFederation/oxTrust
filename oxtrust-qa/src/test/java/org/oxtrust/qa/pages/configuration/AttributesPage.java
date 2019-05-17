package org.oxtrust.qa.pages.configuration;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.oxtrust.qa.pages.AbstractPage;

import java.util.List;

public class AttributesPage extends AbstractPage {

	public void showAllAttributes() {
		WebElement allButton = webDriver.findElement(By.className("showAllAttributesButton"));
		allButton.click();

	}

	public void checkInactiveAttributes() {

		List<WebElement> listInactive = webDriver.findElements(By.xpath("//*[contains(text(),'INACTIVE')]"));
		Assert.assertTrue("Text not found!", listInactive.size() > 0);
	}

	public void checkActiveAttributes() {
		List<WebElement> listActive = webDriver.findElements(By.xpath("//*[contains(text(),'ACTIVE')]"));
		Assert.assertTrue("Text not found!", listActive.size() > 0);
	}

	public void checkAttributeDescriptionExists(String descriptionValue) {
		WebDriverWait wait = new WebDriverWait(webDriver, 15);
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[contains(text(),'" + descriptionValue + "')]")));
		List<WebElement> listActive = webDriver
				.findElements(By.xpath("//*[contains(text(),'" + descriptionValue + "')]"));
		Assert.assertTrue("Text not found!", listActive.size() > 0);
	}

	public void clickFirstListedAttribute() {
		WebDriverWait wait = new WebDriverWait(webDriver, 10);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("attributesFormId:attributesListId")));
		WebElement attributeTable = webDriver
				.findElement(By.xpath("//*[@id=\"attributesFormId:attributesListId\"]/tbody/tr[1]/td[1]/a"));
		attributeTable.click();
	}

	public void updateAttribute() {
		WebElement updateButton = webDriver.findElement(By.xpath("//*[@id=\"updateButtons\"]/input[1]"));
		updateButton.click();

	}

	public void registerAttribute() {

		WebElement registerButton = webDriver.findElement(By.id("attributesFormId:register"));
		registerButton.click();

	}

	public void registerSAML1(String registerSAML1UriValue) {
		WebElement registerSAML1Uri = webDriver.findElement(By.className("registerSAML1Field"));
		registerSAML1Uri.clear();
		registerSAML1Uri.sendKeys(registerSAML1UriValue);
	}

	public void registerSAML2(String registerSAML2Value) {
		WebElement registerSAML2Uri = webDriver.findElement(By.className("registerSAML2Field"));
		registerSAML2Uri.clear();
		registerSAML2Uri.sendKeys(registerSAML2Value);

	}

	public void registerDisplayName(String registerDisplayNameValue) {
		WebElement displayName = webDriver.findElement(By.className("registerDisplayNameField"));
		displayName.clear();
		displayName.sendKeys(registerDisplayNameValue);

	}

	public void chooseType() {
		Select typeChosen = new Select(webDriver.findElement(By.className("chooseTypeField")));
		typeChosen.selectByVisibleText("Boolean");

	}

	public void editType() {
		Select typeEdited = new Select(webDriver.findElement(By.className("editTypeField")));
		typeEdited.deselectAll();
		typeEdited.selectByValue("ADMIN");

	}

	public void viewType() {
		Select typeViewed = new Select(webDriver.findElement(By.className("viewTypeField")));
		typeViewed.deselectAll();
		typeViewed.selectByValue("ADMIN");
	}

	public void usageType() {
		Select typeUsed = new Select(webDriver.findElement(By.className("usageTypeField")));
		typeUsed.deselectAll();
		typeUsed.selectByValue("OPENID");
	}

	public void multivalued() {
		enableCheckBox("multivaluedField");
	}

	public void claimName(String claimNameValue) {
		WebElement setClaimName = webDriver.findElement(By.className("claimNameField"));
		setClaimName.clear();
		setClaimName.sendKeys(claimNameValue);
	}

	public void scimAttribute() {
		enableCheckBox("scimExtendedAttributeField");
	}

	public void setAttributeDescription(String descriptionValue) {
		WebElement descriptionText = webDriver.findElement(By.className("setAttributeDescriptionField"));
		descriptionText.clear();
		descriptionText.sendKeys(descriptionValue);
	}

	public void enableCustomValidation() {
		if (!webDriver.findElement(By.className("enableCustomValidationValue")).isSelected()) {
			webDriver.findElement(By.className("enableCustomValidationValue")).findElement(By.xpath("..")).click();
		}
	}

	public void setValidationRegExp(String regExpValue) {
		WebDriverWait wait = new WebDriverWait(webDriver, 10);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("setValidationRegExpField")));
		WebElement regExText = webDriver.findElement(By.className("setValidationRegExpField"));
		regExText.clear();
		regExText.sendKeys(regExpValue);
	}

	public void enableTooltip() {
		if (!webDriver.findElement(By.className("enableTooltipField")).isSelected()) {
			webDriver.findElement(By.className("enableTooltipField")).findElement(By.xpath("..")).click();
		}

	}

	public void tooltipText(String textValue) {
		waitElementByClass("tooltipTextField");
		WebElement tooltipField = webDriver.findElement(By.className("tooltipTextField"));
		tooltipField.sendKeys(textValue);

	}

	public void minimumLength(String minimumLengthSet) {
		WebElement minimumLengthValue = webDriver.findElement(By.className("minimumLengthField"));
		minimumLengthValue.clear();
		minimumLengthValue.sendKeys(minimumLengthSet);

	}

	public void maximumLength(String maximumLengthSet) {
		WebElement maximumLengthValue = webDriver.findElement(By.className("maximumLengthField"));
		maximumLengthValue.clear();
		maximumLengthValue.sendKeys(maximumLengthSet);

	}

	public void regexPattern(String regexPatternSet) {
		WebElement regexPatternValue = webDriver.findElement(By.className("regexPatternField"));
		regexPatternValue.clear();
		regexPatternValue.sendKeys(regexPatternSet);

	}

	public void status() {
		Select statusChosen = new Select(webDriver.findElement(By.className("statusValue")));
		statusChosen.selectByVisibleText("INACTIVE");
	}

	public void cancelButton() {
		WebElement cancelButtonFinal = webDriver.findElement(By.name("cancelButton"));
		cancelButtonFinal.click();
	}

	public void deleteAttribute() {
		WebElement deleteButton = webDriver.findElement(By.id("deleteButton"));
		deleteButton.click();
		WebElement confirmDeletion = webDriver.findElement(By.id("deleteButton"));
		confirmDeletion.click();

	}

}
