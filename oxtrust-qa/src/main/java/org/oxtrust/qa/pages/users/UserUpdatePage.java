package org.oxtrust.qa.pages.users;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;
import org.oxtrust.qa.pages.AbstractPage;

public class UserUpdatePage extends AbstractPage {
	public void fillUserName(String userName) {
		WebElement element = webDriver.findElement(By.className("Username"));
		element.clear();
		element.sendKeys(userName);
	}

	public void fillFirstName(String firstName) {
		WebElement element = webDriver.findElement(By.className("First"));
		element.clear();
		element.sendKeys(firstName);
	}

	public void fillLastName(String lastName) {
		WebElement element = webDriver.findElement(By.className("Last"));
		element.clear();
		element.sendKeys(lastName);
	}

	public void fillDisplayName(String displayName) {
		WebElement element = webDriver.findElement(By.className("Display"));
		element.clear();
		element.sendKeys(displayName);
	}

	public void fillEmail(String email) {
		WebElement element = webDriver.findElement(By.className("Email"));
		element.clear();
		element.sendKeys(email);
	}

	public void fillPassword(String pwd) {
		WebElement element = webDriver.findElements(By.className("Password")).get(0);
		element.clear();
		element.sendKeys(pwd);
		fillPassword2(pwd);
	}

	private void fillPassword2(String pwd) {
		WebElement element = webDriver.findElements(By.className("Password")).get(1);
		element.clear();
		element.sendKeys(pwd);
	}

	public void fillStatus(String status) {
		WebElement element = webDriver.findElement(By.className("User"));
		Select select = new Select(element);
		select.selectByVisibleText(status);
	}

	public void save() {
		WebElement button = webDriver.findElement(By.className("savePersonButon"));
		Actions actions = new Actions(webDriver);
		actions.moveToElement(button).click().perform();
		fluentWait(LARGE);
	}

	public void delete() {
		scrollDown();
		WebElement button = webDriver.findElement(By.className("deletePersonButton"));
		Actions actions = new Actions(webDriver);
		actions.moveToElement(button).click().perform();
		fluentWait(ONE_SEC);
		WebElement dialog = webDriver.findElement(By.id("deleteConfirmation:acceptRemovalModalPanel_content"));
		dialog.findElements(By.tagName("input")).get(0).click();
		fluentWait(ONE_SEC);
	}

	public void cancel() {
		WebElement footer = webDriver.findElement(By.className("updateButtons"));
		List<WebElement> buttons = footer.findElements(By.tagName("input"));
		Actions actions = new Actions(webDriver);
		actions.moveToElement(buttons.get(3)).click().perform();
	}

	public void changePassword(String pwd) {
		WebElement footer = webDriver.findElement(By.className("updateButtons"));
		List<WebElement> buttons = footer.findElements(By.tagName("input"));
		Actions actions = new Actions(webDriver);
		actions.moveToElement(buttons.get(0)).click().perform();

		WebElement form = webDriver.findElement(By.id("personPassword:changePasswordForm:passwordGroupId"));
		List<WebElement> passwordFields = form.findElements(By.tagName("input"));
		for (WebElement element : passwordFields) {
			element.clear();
			element.sendKeys(pwd);
		}
		List<WebElement> spans = form.findElements(By.xpath("following-sibling::span"));
		spans.get(0).findElements(By.tagName("input")).get(0).click();
		fluentWait(SMALL);
	}

}
