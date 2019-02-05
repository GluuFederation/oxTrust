package org.oxtrust.qa.pages.configuration.authentication;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.oxtrust.qa.pages.AbstractPage;

public class CasProtocolPage extends AbstractPage {

	public void assertStatusIsEnabled() {
		WebElement checkBox = webDriver.findElement(By.className("enableCasProtocolSelectBox"));
		WebElement parent = checkBox.findElement(By.xpath(".."));
		Assert.assertTrue(parent.getAttribute("class").contains("checked"));
	}

	public void enableStatus() {
		WebElement checkBox = webDriver.findElement(By.className("enableCasProtocolSelectBox"));
		WebElement parent = checkBox.findElement(By.xpath(".."));
		if (!parent.getAttribute("class").contains("checked")) {
			checkBox.click();
		}
		Assert.assertTrue(parent.getAttribute("class").contains("checked"));
	}

	public void disableStatus() {
		WebElement checkBox = webDriver.findElement(By.className("enableCasProtocolSelectBox"));
		WebElement parent = checkBox.findElement(By.xpath(".."));
		if (parent.getAttribute("class").contains("checked")) {
			checkBox.click();
		}
		Assert.assertFalse(parent.getAttribute("class").contains("checked"));
	}

	public void assertStatusIsDisabled() {
		WebElement checkBox = webDriver.findElement(By.className("enableCasProtocolSelectBox"));
		WebElement parent = checkBox.findElement(By.xpath(".."));
		Assert.assertFalse(parent.getAttribute("class").contains("checked"));
	}

	public void assertServiceTypeIs(String type) {
		Select select = new Select(webDriver.findElement(By.className("casProtocolSessionStorageTypeSelectBox")));
		Assert.assertTrue(type.equalsIgnoreCase(select.getFirstSelectedOption().getText()));
	}

	public void setServiceTypeIs(String type) {
		Select select = new Select(webDriver.findElement(By.className("casProtocolSessionStorageTypeSelectBox")));
		select.selectByVisibleText(type);
	}

	public void assertBaseUrlEndWith(String url) {
		WebElement element = webDriver.findElement(By.className("casProtocolBaseURLTextBox"));
		Assert.assertTrue(element.getText().endsWith(url));
	}

	public void saveCasSetting() {
		WebElement button = webDriver.findElement(By.className("saveCasButton"));
		button.click();
	}

	public void save() {
		WebElement footer = webDriver.findElement(By.id("updateButtons"));
		footer.findElements(By.tagName("input")).get(0).click();
	}

}
