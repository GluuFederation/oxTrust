package org.oxtrust.qa.pages.login;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.oxtrust.qa.pages.AbstractPage;

public class SignInPage extends AbstractPage {

	private boolean passportSocialIsEnable = false;

	public void fillForm(String userName, String password) {
		passportSocialIsEnable = getCurrentPageTitle().contains("Passport");
		if (passportSocialIsEnable) {
			WebElement userNameElement = webDriver.findElement(By.xpath("//*[@id='loginForm:username']"));
			userNameElement.sendKeys(userName);
			WebElement passwordElement = webDriver.findElement(By.xpath("//*[@id='loginForm:password']"));
			passwordElement.sendKeys(password);
		} else {
			WebElement userNameElement = webDriver.findElement(By.xpath("//*[@id='loginForm:username']"));
			userNameElement.sendKeys(userName);
			WebElement passwordElement = webDriver.findElement(By.xpath("//*[@id='loginForm:password']"));
			passwordElement.sendKeys(password);
		}
	}

	public void submit() {
		WebElement loginButton = webDriver.findElement(By.id("loginForm:loginButton"));
		loginButton.click();
		fluentWait(ONE_SEC);
	}

	public void checkCurrentPageIsHomePage() {
		String currentPageUrl = getCurrentPageUrl();
		Assert.assertTrue(currentPageUrl.contains("home"));
	}

	public void fillFormAsAdmin() {
		fillForm(settings.getUserName(), settings.getPassword());
	}

	public void checkCurrentPageIsLoginPage() {
		String currentPageUrl = getCurrentPageUrl();
		Assert.assertTrue(currentPageUrl.endsWith("oxauth/login.htm")
				|| currentPageUrl.endsWith("oxauth/auth/passport/passportlogin.htm"));
	}

	public void clickForgotPasswordLink() {
		WebElement link = webDriver.findElement(By.className("forgot_link"));
		link.click();
		fluentWait(ONE_SEC);
	}

	public void doSomeWork() {
		fluentWait(4);
	}
}
