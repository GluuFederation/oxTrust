package org.oxtrust.qa.pages.users;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.oxtrust.qa.pages.AbstractPage;

public class GroupUpdatePage extends AbstractPage {
	WebElement footer;

	public void fillDisplayName(String displayName) {
		WebElement input = webDriver.findElement(By.className("displayNameField"));
		input.clear();
		input.sendKeys(displayName);
	}

	public void fillDescription(String des) {
		WebElement element = webDriver.findElement(By.tagName("textarea"));
		element.clear();
		element.sendKeys(des);
	}

	public void fillVisibility(String value) {
		WebElement dropDown = webDriver.findElement(By.className("visibilityField"));
		Select select = new Select(dropDown);
		select.selectByVisibleText(value);
	}

	public void save() {
		footer = webDriver.findElement(By.id("updateButtons"));
		footer.findElements(By.tagName("button")).get(0).click();
	}

	public void delete() {
		footer = webDriver.findElement(By.id("updateButtons"));
		footer.findElements(By.tagName("input")).get(0).click();

		WebElement dialog = webDriver.findElement(By.id("deleteConfirmation:acceptRemovalModalPanel_content"));
		dialog.findElements(By.tagName("input")).get(0).click();
		fluentWait(LITTLE);
	}

	public void cancel() {
		footer = webDriver.findElement(By.id("updateButtons"));
		footer.findElements(By.tagName("input")).get(2).click();
	}

}
