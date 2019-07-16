package org.oxtrust.qa.pages.users;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.oxtrust.qa.pages.AbstractPage;

public class UserAddPage extends AbstractPage {

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
		WebElement element = webDriver.findElement(By.className("Status"));
		Select select = new Select(element);
		select.selectByVisibleText(status);
	}

	public void save() {
		scrollDown();
		scrollDown();
		WebElement footer = webDriver.findElement(By.className("saveButton"));
		footer.click();
		fluentWait(SMALL);
	}

}
