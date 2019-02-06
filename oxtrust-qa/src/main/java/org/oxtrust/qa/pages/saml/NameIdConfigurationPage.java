package org.oxtrust.qa.pages.saml;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.oxtrust.qa.pages.AbstractPage;

public class NameIdConfigurationPage extends AbstractPage {

	public void startAddingNewConfiguration() {
		fluentWait(ONE_SEC);
		WebElement element = webDriver.findElement(By.className("addConfigButton"));
		element.click();
		fluentWait(ONE_SEC);
	}

	public void delete(String name) {
		scrollDown();
		WebElement section = webDriver.findElement(By.className(name));
		WebElement deleteButton = section.findElement(By.tagName("a"));
		JavascriptExecutor executor = (JavascriptExecutor) webDriver;
		executor.executeScript("arguments[0].click()", deleteButton);
		fluentWait(LITTLE);
		WebElement confirm = webDriver.findElement(By.className("confirmDialogButton"));
		executor.executeScript("arguments[0].click()", confirm);
		fluentWait(ONE_SEC);
	}

}
