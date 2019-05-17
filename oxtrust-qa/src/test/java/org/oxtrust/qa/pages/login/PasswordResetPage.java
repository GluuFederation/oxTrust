package org.oxtrust.qa.pages.login;


import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.oxtrust.qa.pages.AbstractPage;


public class PasswordResetPage extends AbstractPage {

	public void setEmail(String email) {
		WebElement element = webDriver.findElement(By.id("formArea:username"));
		element.clear();
		element.sendKeys(email);
		fluentWait(ONE_SEC);
	}

	public void sendMail() {
		WebElement element = webDriver.findElement(By.id("formArea:submit"));
		element.click();
		fluentWait(LITTLE);
	}
	
	public void verifyMailWasSend() {
		WebElement element=waitElementByClass("lockscreen-name");
		Assert.assertTrue(element.getText().contains(""));
	}

}
