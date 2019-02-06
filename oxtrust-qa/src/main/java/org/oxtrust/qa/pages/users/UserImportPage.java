package org.oxtrust.qa.pages.users;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.oxtrust.qa.pages.AbstractPage;

public class UserImportPage extends AbstractPage {

	public void importUsers() {
		WebElement upLoader = webDriver.findElement(By.className("uploadFile"));
		WebElement addButton = upLoader.findElement(By.cssSelector("input[type='file']"));
		addButton.sendKeys(getResourceFile("ImportUsers.xls").getAbsolutePath());
		fluentWait(ONE_SEC);
		validate();
		performImport();
	}

	private void validate() {
		Actions actions = new Actions(webDriver);
		WebElement footer = webDriver.findElement(By.id("updateButtons"));
		actions.moveToElement(footer.findElements(By.tagName("input")).get(0)).click().perform();
		fluentWait(ONE_SEC);
	}

	private void performImport() {
		WebElement panel = webDriver.findElement(By.id("updateButtons"));
		panel.findElements(By.tagName("input")).get(0).click();
		fluentWait(ONE_SEC);
	}

}
