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
	private int currentSize = 0;
	private WebElement newCriptElement;

	public void hitAddButton() {
		scrollDown();
		currentSize = computeTheNumberOfScriptForCurrentTab();
		scrollDown();
		WebElement addButton = fluentWaitFor(By.className("addConfigButton"));
		addButton.click();
		fluentWaitForTableCompute(currentSize);
	}

	public void pickTab(String tabText) {
		currentTabText = tabText;
		WebElement tabSection = webDriver.findElement(By.id("customScriptForm:scriptTypeTabPanelIdheader"));
		WebElement tabsSection = tabSection.findElement(By.className("rf-tab-hdr-tabs"));
		List<WebElement> tabs = tabsSection.findElements(By.tagName("td"));
		for (WebElement tab : tabs) {
			if (tab.getText().contains(tabText)) {
				tab.findElement(By.tagName("span")).click();
				fluentWait(SMALL);
				break;
			}
		}
	}

	private int computeTheNumberOfScriptForCurrentTab() {
		String className = "allScriptFor".concat(currentTabText.split("\\s+")[0]);
		WebElement table = waitElementByClass(className);
		WebElement firstElement = table.findElements(By.tagName("tr")).get(0);
		List<WebElement> scripts = new ArrayList<>();
		scripts.add(firstElement);
		scripts.addAll(firstElement.findElements(By.xpath("following-sibling::tr")));
		return scripts.size();
	}

	private void computeLastScriptScriptForCurrentTab() {
		String className = "allScriptFor".concat(currentTabText.split("\\s+")[0]);
		WebElement table = waitElementByClass(className);
		WebElement firstElement = table.findElements(By.tagName("tr")).get(0);
		List<WebElement> scripts = new ArrayList<>();
		scripts.add(firstElement);
		scripts.addAll(firstElement.findElements(By.xpath("following-sibling::tr")));
		newCriptElement = scripts.get(scripts.size() - 1);
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
		computeLastScriptScriptForCurrentTab();
		WebElement textBox = newCriptElement.findElement(By.className("scriptNameTextBox"));
		textBox.clear();
		textBox.sendKeys(name);
	}

	public void setDescription(String description) {
		computeLastScriptScriptForCurrentTab();
		WebElement textBox = newCriptElement.findElement(By.className("descriptionTextBox"));
		textBox.clear();
		textBox.sendKeys(description);
	}

	public void setLevel(String level) {
		computeLastScriptScriptForCurrentTab();
		WebElement element = newCriptElement.findElement(By.className("inputNumberSpinnerBox"));
		WebElement input = element.findElement(By.tagName("input"));
		input.clear();
		input.sendKeys(level);
	}

	public void setContent(String text) {
		scrollDown();
		computeLastScriptScriptForCurrentTab();
		WebElement element = newCriptElement.findElement(By.className("scriptTextArea"));
		element.clear();
		element.sendKeys(text);
	}

	public void setProgrammingLanguage(String lang) {
		computeLastScriptScriptForCurrentTab();
		Select select = new Select(newCriptElement.findElement(By.className("programmingLanguageSelectBox")));
		select.selectByVisibleText(lang);
	}

	public void setUsageType(String type) {
		computeLastScriptScriptForCurrentTab();
		Select select = new Select(newCriptElement.findElement(By.className("usageTypeSelectBox")));
		select.selectByVisibleText(type);
	}

	public void setLocationType(String type) {
		computeLastScriptScriptForCurrentTab();
		Select select = new Select(newCriptElement.findElement(By.className("locationTypeSelectBox")));
		select.selectByVisibleText(type);
	}

	public void addNewproperty(String label, String value) {
		computeLastScriptScriptForCurrentTab();
		WebElement element = newCriptElement.findElement(By.className("addNewPropertyButton"));
		element.click();
		fluentWait(SMALL);
		WebElement table = newCriptElement.findElement(By.className("propertiesList"));
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
		scrollDown();
		WebElement element = fluentWaitFor(By.id("updateButtons"));
		element.findElements(By.tagName("input")).get(0).click();
	}

	public void enable() {
		computeLastScriptScriptForCurrentTab();
		WebElement chekBox = newCriptElement.findElement(By.className("customScriptStatusCheckBox"));
		chekBox.click();
	}

	public void deleteScript(String scriptName, String tabName) {
		currentTabText = tabName;
		String className = "allScriptFor".concat(currentTabText.split("\\s+")[0]);
		WebElement table = waitElementByClass(className);
		WebElement firstElement = table.findElements(By.tagName("tr")).get(0);
		List<WebElement> scripts = new ArrayList<>();
		scripts.add(firstElement);
		scripts.addAll(firstElement.findElements(By.xpath("following-sibling::tr")));
		WebElement rightSection = null;
		for (WebElement scriptSection : scripts) {
			if (scriptSection.findElement(By.tagName("a")).getText().contains(scriptName)) {
				rightSection = scriptSection;
				break;
			}
		}
		WebElement header = rightSection.findElement(By.tagName("a"));
		header.click();
		WebElement deleteButton = rightSection.findElement(By.className("deleteScriptButton"));
		deleteButton.click();
		fluentWait(MEDIUM);
		WebElement dialog = fluentWaitFor(By.className("dialogBoxPanelFor".concat(scriptName)));
		WebElement okButton = dialog.findElement(By.className("confirmDialogButton"));
		okButton.click();
		fluentWait(MEDIUM);
		save();
	}

	public void enableScript(String scriptName, String tabName) {
		WebElement me = null;
		WebElement element = webDriver.findElement(By.className("allScriptForPerson"));
		List<WebElement> scripts = element.findElement(By.tagName("tbody")).findElements(By.tagName("tr"));
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++");
		System.out.println("SIZE: " + scripts.size());
		for (WebElement script : scripts) {
			if (script.getText().contains(scriptName)) {
				me = script;
				break;
			}
		}
		me.click();

	}

	public void assertScriptExist(String scriptName, String tabName) {
		currentTabText = tabName;
		String className = "allScriptFor".concat(currentTabText.split("\\s+")[0]);
		WebElement table = waitElementByClass(className);
		WebElement firstElement = table.findElements(By.tagName("tr")).get(0);
		List<WebElement> scripts = new ArrayList<>();
		scripts.add(firstElement);
		scripts.addAll(firstElement.findElements(By.xpath("following-sibling::tr")));
		boolean found = false;
		for (WebElement scriptSection : scripts) {
			if (scriptSection.findElement(By.tagName("a")).getText().contains(scriptName)) {
				found = true;
				break;
			}
		}
		Assert.assertTrue(found);
	}

	public void assertScriptDontExist(String scriptName, String tabName) {
		currentTabText = tabName;
		String className = "allScriptFor".concat(currentTabText.split("\\s+")[0]);
		WebElement table = waitElementByClass(className);
		WebElement firstElement = table.findElements(By.tagName("tr")).get(0);
		List<WebElement> scripts = new ArrayList<>();
		scripts.add(firstElement);
		scripts.addAll(firstElement.findElements(By.xpath("following-sibling::tr")));
		boolean found = false;
		for (WebElement scriptSection : scripts) {
			if (scriptSection.findElement(By.tagName("a")).getText().contains(scriptName)) {
				found = true;
				break;
			}
		}
		Assert.assertFalse(found);
		fluentWait(LITTLE);
	}

}
