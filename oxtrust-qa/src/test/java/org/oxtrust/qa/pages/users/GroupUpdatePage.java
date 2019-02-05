package org.oxtrust.qa.pages.users;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.oxtrust.qa.pages.AbstractPage;

public class GroupUpdatePage extends AbstractPage {
	WebElement footer;

	public void fillDisplayName(String displayName) {
		WebElement element = webDriver.findElement(By.className("displayNameField"));
		element.click();
		WebElement input = element.findElements(By.tagName("input")).get(1);
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
		footer.findElements(By.tagName("input")).get(0).click();
	}

	public void delete() {
		footer = webDriver.findElement(By.id("updateButtons"));
		footer.findElements(By.tagName("input")).get(1).click();

		WebElement dialog = webDriver.findElement(By.id("deleteConfirmation:acceptRemovalModalPanel_content"));
		dialog.findElements(By.tagName("input")).get(0).click();
		fluentWait(LITTLE);
	}

	public void cancel() {
		footer = webDriver.findElement(By.id("updateButtons"));
		footer.findElements(By.tagName("input")).get(2).click();
	}

}
