package org.oxtrust.qa.pages.configuration;


import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.oxtrust.qa.pages.AbstractPage;

public class RegistrationManagePage extends AbstractPage {

	public void checkCaptchaIsEnable(String status) {
		WebElement parent = webDriver.findElement(By.className("enbaleCaptchaSelectBox")).findElement(By.xpath(".."));
		if (status.equalsIgnoreCase("true")) {
			Assert.assertTrue(parent.getAttribute("class").contains("checked"));
		} else {
			Assert.assertFalse(parent.getAttribute("class").contains("checked"));
		}
	}

	public void enableCaptcha(String status) {
		WebElement parent = webDriver.findElement(By.className("enbaleCaptchaSelectBox")).findElement(By.xpath(".."));
		if (status.equalsIgnoreCase("true") && !parent.getAttribute("class").contains("checked")) {
			parent.click();
		}
		if (status.equalsIgnoreCase("false") && parent.getAttribute("class").contains("checked")) {
			parent.click();
		}
	}

	public void checkRegistrationAttribIsEnable(String status) {
		WebElement parent = webDriver.findElement(By.className("registrationSelectBox")).findElement(By.xpath(".."));
		if (status.equalsIgnoreCase("true")) {
			Assert.assertTrue(parent.getAttribute("class").contains("checked"));
		} else {
			Assert.assertFalse(parent.getAttribute("class").contains("checked"));
		}
	}

	public void enableRegistrationAttrib(String status) {
		WebElement parent = webDriver.findElement(By.className("registrationSelectBox")).findElement(By.xpath(".."));
		if (status.equalsIgnoreCase("true") && !parent.getAttribute("class").contains("checked")) {
			parent.click();
		}
		if (status.equalsIgnoreCase("false") && parent.getAttribute("class").contains("checked")) {
			parent.click();
		}
	}

	public void checkSiteKey(String key) {
		if (key.equalsIgnoreCase("empty")) {
			Assert.assertTrue(
					webDriver.findElement(By.className("recaptchaSiteKeyTextBox")).getAttribute("value").isEmpty());
		} else {
			Assert.assertTrue(webDriver.findElement(By.className("recaptchaSiteKeyTextBox")).getAttribute("value")
					.equalsIgnoreCase(key));
		}
	}

	public void setSiteKey(String key) {
		webDriver.findElement(By.className("recaptchaSiteKeyTextBox")).sendKeys(key);
	}

	public void checkSiteSecret(String secret) {
		if (secret.equalsIgnoreCase("empty")) {
			Assert.assertTrue(
					webDriver.findElement(By.className("recaptchaSiteSecretTextBox")).getAttribute("value").isEmpty());
		} else {
			Assert.assertTrue(webDriver.findElement(By.className("recaptchaSiteSecretTextBox")).getAttribute("value")
					.equalsIgnoreCase(secret));
		}
	}

	public void setSiteSecret(String secret) {
		webDriver.findElement(By.className("recaptchaSiteSecretTextBox")).sendKeys(secret);
	}

	public void checkCssLocation(String location) {
		if (location.equalsIgnoreCase("empty")) {
			Assert.assertTrue(
					webDriver.findElement(By.className("cssLocationTextBox")).getAttribute("value").isEmpty());
		} else {
			Assert.assertTrue(webDriver.findElement(By.className("cssLocationTextBox")).getAttribute("value")
					.equalsIgnoreCase(location));
		}
	}

	public void setCssLocation(String location) {
		webDriver.findElement(By.className("cssLocationTextBox")).sendKeys(location);
	}

	public void checkJsLocation(String location) {
		if (location.equalsIgnoreCase("empty")) {
			Assert.assertTrue(
					webDriver.findElement(By.className("javascriptLocationTextBox")).getAttribute("value").isEmpty());
		} else {
			Assert.assertTrue(webDriver.findElement(By.className("javascriptLocationTextBox")).getAttribute("value")
					.equalsIgnoreCase(location));
		}
	}

	public void setJsLocation(String location) {
		webDriver.findElement(By.className("javascriptLocationTextBox")).sendKeys(location);
	}

}
