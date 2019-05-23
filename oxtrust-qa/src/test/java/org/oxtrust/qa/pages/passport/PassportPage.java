package org.oxtrust.qa.pages.passport;

import java.util.List;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.oxtrust.qa.pages.AbstractPage;

public class PassportPage extends AbstractPage {

	private static final String PROVIDERS_TABLE = "providersTableIdTable";

	public void clickAddButton() {
		clickOnButtonByClass("addProviderButtonClass");
	}

	public void save() {
		webDriver.findElement(By.id("updateButtons")).findElements(By.tagName("input")).get(0).click();
		fluentWait(2);
	}

	public void delete() {
		webDriver.findElement(By.id("updateButtons")).findElements(By.tagName("input")).get(2).click();
		fluentWait(2);
		WebElement dialog = webDriver
				.findElement(By.id("providerForm:deleteConfirmation:acceptRemovalModalPanel_content"));
		dialog.findElements(By.tagName("input")).get(0).click();
		fluentWait(2);
	}

	public void assertProviderExist(String clientName) {
		Assert.assertTrue(assertElementExistInList(PROVIDERS_TABLE, clientName));
	}

	public void editProvider(String provider) {
		WebElement body = webDriver.findElement(By.className(PROVIDERS_TABLE)).findElements(By.tagName("tbody")).get(0);
		List<WebElement> listItems = body.findElements(By.tagName("tr"));
		for (WebElement element : listItems) {
			if (element.getText().contains(provider)) {
				element.findElements(By.tagName("td")).get(0).click();
				break;
			}
		}
	}

	public void assertProviderNotExist(String provider) {
		Assert.assertFalse(assertElementExistInList(PROVIDERS_TABLE, provider));
	}

	public void setLogLevel(String logLevel) {
		selectBoxByClass("PassportLogLevelID", logLevel);
	}

	public void saveConfig() {
		webDriver.findElement(By.id("updateButtons")).findElements(By.tagName("input")).get(0).click();
		fluentWait(2);
	}

	public void assertEndPoinIsNotEmpty() {
		WebElement element = webDriver.findElement(By.className("AuthorizationEndPointID"));
		Assert.assertTrue(element.getText() != "");
	}

	public void assertAcrIsNotEmpty() {
		WebElement element = webDriver.findElement(By.id("IdpInitiatedForm:PostProfileEndpointID:outputInputPanel"));
		Assert.assertTrue(element.getText() != "");
	}
	
	public void selectValue(String value) {
		Select select=new Select(webDriver.findElement(By.className("select2style")));
		select.selectByVisibleText(value);
	}
}
