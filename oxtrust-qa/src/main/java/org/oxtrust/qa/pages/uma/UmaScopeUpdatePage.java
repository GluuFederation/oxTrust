package org.oxtrust.qa.pages.uma;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.oxtrust.qa.pages.AbstractPage;

public class UmaScopeUpdatePage extends AbstractPage {
	public void editUmaScopeId(String id) {
		WebElement input = webDriver.findElement(By.className("oxId"));
		input.clear();
		input.sendKeys(id);
	}

	public void editUmaScopeDisplayName(String dn) {
		WebElement input = webDriver.findElement(By.className("displayNameId"));
		input.clear();
		input.sendKeys(dn);
		input.sendKeys(Keys.TAB);
	}

	public void save() {
		WebElement buttonBar = webDriver.findElement(By.className("box-footer"));
		buttonBar.click();
		buttonBar.findElements(By.tagName("input")).get(0).click();
		fluentWait(ONE_SEC);
	}

	public void delete() {
		WebElement buttonBar = webDriver.findElement(By.className("box-footer"));
		buttonBar.click();
		buttonBar.findElements(By.tagName("input")).get(1).click();
		fluentWait(ONE_SEC);
		WebElement dialog = webDriver.findElement(By.id("deleteConfirmation:acceptRemovalModalPanel_content"));
		dialog.findElements(By.tagName("input")).get(0).click();
		fluentWait(3);
	}

}
