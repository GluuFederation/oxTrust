package org.oxtrust.qa.pages.configuration.cr;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.oxtrust.qa.pages.AbstractPage;

public class CacheRefreshPage extends AbstractPage {

	public void addSourcedestinationAttribute(String source, String destination) {
		WebElement element = webDriver.findElement(By.className("addNewPropertyButton"));
		element.click();
		fluentWait(ONE_SEC);
		List<WebElement> tables = webDriver.findElements(By.className("propertiesList"));
		WebElement lastTable = tables.get(tables.size() - 1);
		lastTable.findElement(By.className("propertyLabelTextBox")).sendKeys(source);
		lastTable.findElement(By.className("propertyValueTextBox")).sendKeys(destination);
		fluentWait(ONE_SEC);
	}

	public void deleteAllSourceDestinationAttributes() {
		fluentWait(ONE_SEC);
		List<WebElement> tables = webDriver.findElements(By.className("propertiesList"));
		while (tables.size() >= 1) {
			tables.get(0).findElement(By.className("removePropertyButton")).click();
			fluentWait(ONE_SEC);
			tables = webDriver.findElements(By.className("propertiesList"));
		}
	}

	public void setPollingInterval(String intervall) {
		webDriver.findElement(By.className("pollingIntervalTextBox")).clear();
		webDriver.findElement(By.className("pollingIntervalTextBox")).sendKeys(intervall);
	}

	public void enableCacheRefresh() {
		enableCheckBox("enableCacheRefreshSelectBox");
	}

	public void save() {
		webDriver.findElement(By.className("updateButon")).click();
		fluentWait(ONE_SEC);
	}

	public void addKeyAttrib(String attrib) {
		webDriver.findElements(By.className("addItemButton")).get(0).click();
		fluentWait(ONE_SEC);
		List<WebElement> attribs = webDriver.findElements(By.className("KeyAttributeList"));
		WebElement newAttrib = attribs.get(attribs.size() - 1);
		newAttrib.findElement(By.tagName("input")).clear();
		newAttrib.findElement(By.tagName("input")).sendKeys(attrib);
		fluentWait(ONE_SEC);
	}

	public void deleteAllKeyAttributes() {
		fluentWait(ONE_SEC);
		List<WebElement> attribs = webDriver.findElements(By.className("KeyAttributeList"));
		while (attribs.size() >= 1) {
			attribs.get(0).findElement(By.className("removePropertyButton")).click();
			fluentWait(ONE_SEC);
			attribs = webDriver.findElements(By.className("KeyAttributeList"));
		}
	}

	public void deleteAllObject() {
		fluentWait(ONE_SEC);
		List<WebElement> attribs = webDriver.findElements(By.className("ObjectClassList"));
		while (attribs.size() >= 1) {
			attribs.get(0).findElement(By.className("removePropertyButton")).click();
			fluentWait(ONE_SEC);
			attribs = webDriver.findElements(By.className("ObjectClassList"));
		}
	}

	public void addObjectClass(String name) {
		webDriver.findElements(By.className("addItemButton")).get(1).click();
		fluentWait(ONE_SEC);
		List<WebElement> classes = webDriver.findElements(By.className("ObjectClassList"));
		WebElement newClass = classes.get(classes.size() - 1);
		newClass.findElement(By.tagName("input")).clear();
		newClass.findElement(By.tagName("input")).sendKeys(name);
		fluentWait(ONE_SEC);
	}

	public void deleteAllSourceAttrib() {
		fluentWait(ONE_SEC);
		List<WebElement> attribs = webDriver.findElements(By.className("SourceAttribeList"));
		while (attribs.size() >= 1) {
			attribs.get(0).findElement(By.className("removePropertyButton")).click();
			fluentWait(ONE_SEC);
			attribs = webDriver.findElements(By.className("SourceAttribeList"));
		}
	}

	public void addAttrib(String name) {
		webDriver.findElements(By.className("addItemButton")).get(2).click();
		fluentWait(ONE_SEC);
		List<WebElement> attribs = webDriver.findElements(By.className("SourceAttribeList"));
		WebElement newSourceAttrib = attribs.get(attribs.size() - 1);
		newSourceAttrib.findElement(By.tagName("input")).clear();
		newSourceAttrib.findElement(By.tagName("input")).sendKeys(name);
		fluentWait(ONE_SEC);
	}

	public void addSourceServer(String name, String bindDn, String maxCon, String servers, String baseDns,
			String useSSl) {
		webDriver.findElement(By.className("addSourceServerButton")).click();
		fluentWait(ONE_SEC);
		webDriver.findElement(By.className("nameTextBox")).clear();
		webDriver.findElement(By.className("nameTextBox")).sendKeys(name);

		webDriver.findElement(By.className("bindDnTextBox")).clear();
		webDriver.findElement(By.className("bindDnTextBox")).sendKeys(bindDn);

		webDriver.findElement(By.className("maxConnectionTextBox")).clear();
		webDriver.findElement(By.className("maxConnectionTextBox")).sendKeys(maxCon);

		WebElement container = webDriver.findElement(By.id("cacheRefreshForm:sourceConfigsId:dgb"));
		container.findElements(By.className("addItemButton")).get(0).click();
		fluentWait(ONE_SEC);
		List<WebElement> properties = container.findElements(By.className("NewPropertyBox"));
		properties.get(properties.size() - 1).sendKeys(servers);
		WebElement main = webDriver.findElement(By.id("cacheRefreshForm:sourceConfigsId:dgb"));
		main.findElements(By.className("addItemButton")).get(1).click();
		fluentWait(ONE_SEC);
		properties = main.findElements(By.className("NewPropertyBox"));
		properties.get(properties.size() - 1).sendKeys(baseDns);
		WebElement element = main.findElement(By.className("useSSLSelectBox"));
		WebElement parent = element.findElement(By.xpath(".."));
		if (useSSl.equalsIgnoreCase("true") && !parent.getAttribute("class").contains("checked")) {
			parent.click();
		}
		if (useSSl.equalsIgnoreCase("false") && parent.getAttribute("class").contains("checked")) {
			parent.click();
		}
		fluentWait(ONE_SEC);
	}

	public void changeBindDnPassword(String password) {
		WebElement button = webDriver.findElement(By.className("changeBindDnButton"));
		button.click();
		fluentWait(ONE_SEC);
		WebElement main = webDriver.findElement(By.id("bindPasswordDialogId:changePasswordForm:passwordGroupId"));
		WebElement input1 = main.findElement(By.id("bindPasswordDialogId:changePasswordForm:pass"));
		input1.clear();
		input1.sendKeys(password);
		WebElement input2 = main.findElement(By.id("bindPasswordDialogId:changePasswordForm:conf"));
		input2.clear();
		input2.sendKeys(password);
		fluentWait(ONE_SEC);
		webDriver.findElement(By.className("saveBindDNButton")).click();
		webDriver.findElement(By.className("saveBindDNButton")).click();
		fluentWait(ONE_SEC);
	}
}
