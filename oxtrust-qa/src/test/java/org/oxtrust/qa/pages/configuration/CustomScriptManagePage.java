package org.oxtrust.qa.pages.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Wait;
import org.oxtrust.qa.pages.AbstractPage;

public class CustomScriptManagePage extends AbstractPage {
	private String currentTabText = null;

	public void hitAddButton() {
		WebElement addButton = fluentWaitFor(By.className("AddCustomScriptButton"));
		addButton.click();
		fluentWait(2);
	}

	public void pickCategory(String tabText) {
		WebElement tree = webDriver.findElement(By.className("list-group"));
		List<WebElement> items = tree.findElements(By.className("list-group-item"));
		for (WebElement e : items) {
			if (e.getText().equalsIgnoreCase(tabText)) {
				e.click();
				break;
			}
		}
		fluentWait(2);
	}

	public void fluentWaitForTableCompute(int finalSize) {
		Wait<WebDriver> wait = new FluentWait<WebDriver>(webDriver).withTimeout(30, TimeUnit.SECONDS)
				.pollingEvery(5, TimeUnit.SECONDS).ignoring(NoSuchElementException.class);
		wait.until(new Function<WebDriver, Boolean>() {
			public Boolean apply(WebDriver driver) {
				String className = "allScriptFor".concat(currentTabText.split("\\s+")[0]);
				WebElement firstElement = driver.findElement(By.className(className)).findElements(By.tagName("tr"))
						.get(0);
				List<WebElement> scripts = new ArrayList<>();
				scripts.add(firstElement);
				scripts.addAll(firstElement.findElements(By.xpath("following-sibling::tr")));
				return scripts.size() == (finalSize + 1);
			}
		});
	}

	public void setName(String name) {
		WebElement textBox = webDriver.findElement(By.className("scriptNameTextBox"));
		textBox.clear();
		textBox.sendKeys(name);
	}

	public void setDescription(String description) {
		WebElement textBox = webDriver.findElement(By.className("descriptionTextBox"));
		textBox.clear();
		textBox.sendKeys(description);
	}

	public void setLevel(String level) {
		WebElement input = webDriver.findElement(By.className("inputNumberSpinnerBox"));
		input.clear();
		input.sendKeys(level);
	}

	public void setContent(String text) {
		scrollDown();
		WebElement element = webDriver.findElement(By.className("scriptTextArea"));
		element.clear();
		element.sendKeys(text);
	}

	public void setProgrammingLanguage(String lang) {
		Select select = new Select(webDriver.findElement(By.className("programmingLanguageSelectBox")));
		select.selectByVisibleText(lang);
	}

	public void setUsageType(String type) {
		Select select = new Select(webDriver.findElement(By.className("usageTypeSelectBox")));
		select.selectByVisibleText(type);
	}

	public void setLocationType(String type) {
		Select select = new Select(webDriver.findElement(By.className("locationTypeSelectBox")));
		select.selectByVisibleText(type);
	}

	public void addNewproperty(String label, String value) {
		WebElement element = webDriver.findElement(By.className("addNewPropertyButton"));
		element.click();
		fluentWait(SMALL);
		WebElement table = webDriver.findElement(By.className("propertiesList"));
		List<WebElement> rows = table.findElements(By.tagName("tr"));
		WebElement firstRow = rows.get(0);
		WebElement labelBox = firstRow.findElement(By.className("propertyLabelTextBox"));
		labelBox.clear();
		labelBox.sendKeys(label);
		WebElement valueBox = firstRow.findElement(By.className("propertyValueTextBox"));
		valueBox.clear();
		valueBox.sendKeys(value);
	}

	public void save() {
		scrollUp();
		scrollUp();
		webDriver.findElement(By.className("saveScriptButton")).click();
	}

	public void enable() {
		WebElement chekBox = webDriver.findElement(By.className("customScriptStatusCheckBox"));
		chekBox.click();
	}

	public void deleteScript(String scriptName, String tabName) {
		WebElement tree = webDriver.findElement(By.className("list-group"));
		List<WebElement> items = tree.findElements(By.className("list-group-item"));
		for (WebElement e : items) {
			if (e.getText().equalsIgnoreCase(scriptName)) {
				webDriver.findElement(By.className("deleteScriptButton")).click();
				break;
			}
		}
		fluentWait(2);
		WebElement dialog = webDriver.findElement(By.id("deleteConfirmation:acceptRemovalModalPanel_content"));
		dialog.findElements(By.tagName("input")).get(0).click();
		fluentWait(2);
		save();
	}

	public void enableScript(String scriptName, String tabName) {
		WebElement me = null;
		WebElement element = webDriver.findElement(By.className("allScriptForPerson"));
		List<WebElement> scripts = element.findElement(By.tagName("tbody")).findElements(By.tagName("tr"));
		for (WebElement script : scripts) {
			if (script.getText().contains(scriptName)) {
				me = script;
				break;
			}
		}
		me.click();

	}

	public void assertScriptExist(String scriptName, String tabName) {
		boolean found = false;
		WebElement tree = webDriver.findElement(By.className("list-group"));
		List<WebElement> items = tree.findElements(By.className("list-group-item"));
		for (WebElement e : items) {
			if (e.getText().equalsIgnoreCase(scriptName)) {
				found = true;
				break;
			}
		}
		Assert.assertTrue(found);
	}

	public void assertScriptDontExist(String scriptName, String tabName) {
		boolean found = false;
		WebElement tree = webDriver.findElement(By.className("list-group"));
		List<WebElement> items = tree.findElements(By.className("list-group-item"));
		for (WebElement e : items) {
			if (e.getText().equalsIgnoreCase(scriptName)) {
				found = true;
				break;
			}
		}
		Assert.assertFalse(found);
		fluentWait(LITTLE);
	}

}
