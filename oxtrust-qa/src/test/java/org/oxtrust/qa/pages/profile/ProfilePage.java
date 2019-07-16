package org.oxtrust.qa.pages.profile;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.oxtrust.qa.pages.AbstractPage;

public class ProfilePage extends AbstractPage {

	public void changePassword(String oldPwd, String newPwd) {
		showPasswordDiolag();
		WebElement old = webDriver.findElement(By.id("personPassword:changePasswordForm:old"));
		old.sendKeys(oldPwd);
		WebElement newField = webDriver.findElement(By.id("personPassword:changePasswordForm:pass"));
		newField.sendKeys(newPwd);
		WebElement confirm = webDriver.findElement(By.id("personPassword:changePasswordForm:conf"));
		confirm.sendKeys(newPwd);
		performPasswordChange();
	}

	private void showPasswordDiolag() {
		webDriver.findElement(By.className("changeUserPasswordButton")).click();
	}

	private void performPasswordChange() {
		WebElement main = webDriver.findElement(By.id("personPassword:changePasswordModalPanel_content"));
		Assert.assertNotNull(main);
		fluentWait(ONE_SEC);
		WebElement button = main.findElement(By.className("savePasswordButton"));
		button.click();
		fluentWait(ONE_SEC);
		webDriver.findElement(By.className("updateProfileButton")).click();
	}

}
