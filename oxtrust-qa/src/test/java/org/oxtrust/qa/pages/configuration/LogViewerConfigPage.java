package org.oxtrust.qa.pages.configuration;


import java.util.List;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.oxtrust.qa.pages.AbstractPage;

public class LogViewerConfigPage extends AbstractPage {

	public void checkOxTrustExternalLog4jLocation() {
		String value = webDriver.findElement(By.className("oxTrustLogConfigLocation")).getAttribute("value");
		Assert.assertTrue(value.isEmpty());
	}

	public void checkOxAuthExternalLog4jLocation() {
		String value = webDriver.findElement(By.className("oxAuthLogConfigLocation")).getAttribute("value");
		Assert.assertTrue(value.isEmpty());
	}

	public void addNewLogTemplate(String name, String value) {
		fluentWait(ONE_SEC);
		webDriver.findElement(By.className("addNewPropertyButton")).click();
		fluentWait(ONE_SEC);
		List<WebElement> tables = webDriver.findElements(By.className("propertiesList"));
		WebElement table = tables.get(tables.size() - 1).findElement(By.tagName("tbody"));
		WebElement lastElement = table.findElements(By.tagName("tr")).get(0);
		lastElement.findElement(By.className("propertyLabelTextBox")).clear();
		lastElement.findElement(By.className("propertyLabelTextBox")).sendKeys(name);
		lastElement.findElement(By.className("propertyValueTextBox")).sendKeys(value);
		fluentWait(ONE_SEC);
		save();
	}

	private void save() {
		webDriver.findElement(By.id("updateButtons")).findElements(By.tagName("input")).get(0).click();
		fluentWait(ONE_SEC);
	}

	public void checkTemplate(String name, String value) {
		boolean found = false;
		List<WebElement> tables = webDriver.findElements(By.className("propertiesList"));
		for (WebElement table : tables) {
			WebElement element = table.findElements(By.tagName("tr")).get(0);
			if (element.findElement(By.className("propertyLabelTextBox")).getAttribute("value")
					.equalsIgnoreCase(name)) {
				Assert.assertTrue(element.findElement(By.className("propertyValueTextBox")).getAttribute("value")
						.equalsIgnoreCase(value));
				found = true;
				break;
			}
		}
		Assert.assertTrue(found);
	}

	public void checkTemplateNotExist(String name, String value) {
		boolean found = false;
		List<WebElement> tables = webDriver.findElements(By.className("propertiesList"));
		for (WebElement table : tables) {
			WebElement element = table.findElements(By.tagName("tr")).get(0);
			if (element.findElement(By.className("propertyLabelTextBox")).getAttribute("value")
					.equalsIgnoreCase(name)) {
				Assert.assertTrue(element.findElement(By.className("propertyValueTextBox")).getAttribute("value")
						.equalsIgnoreCase(value));
				found = true;
				break;
			}
		}
		Assert.assertFalse(found);
	}

	public void delete(String name) {
		List<WebElement> tables = webDriver.findElements(By.className("propertiesList"));
		for (WebElement table : tables) {
			WebElement element = table.findElements(By.tagName("tr")).get(0);
			if (element.findElement(By.className("propertyLabelTextBox")).getAttribute("value")
					.equalsIgnoreCase(name)) {
				element.findElement(By.className("removePropertyButton")).click();
				break;
			}
		}
		save();
	}

}
