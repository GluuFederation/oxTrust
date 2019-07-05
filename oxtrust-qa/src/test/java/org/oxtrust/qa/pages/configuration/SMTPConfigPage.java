package org.oxtrust.qa.pages.configuration;


import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.oxtrust.qa.pages.AbstractPage;

public class SMTPConfigPage extends AbstractPage {

	public void setSmtpHost(String host) {
		WebElement element = webDriver.findElement(By.className("smtpHostTextBox"));
		element.clear();
		element.sendKeys(host);
	}

	public void setSmtpFromName(String host) {
		WebElement element = webDriver.findElement(By.className("fromNameTextBox"));
		element.clear();
		element.sendKeys(host);
	}

	public void setSmtpFromEmailAddress(String host) {
		WebElement element = webDriver.findElement(By.className("fromEmailAddressTextBox"));
		element.clear();
		element.sendKeys(host);
	}

	public void setSmtpRequireAuthentication(boolean value) {
		WebElement element = webDriver.findElement(By.className("requiresAuthenticationCheckBox"))
				.findElement(By.xpath(".."));
		boolean checked = element.getAttribute("class").contains("checked");
		if (value && !checked) {
			element.click();
		} else if (!value && checked) {
			element.click();
		}
	}

	public void setSmtpUserName(String value) {
		WebElement element = webDriver.findElement(By.className("smtpUserNameTextBox"));
		element.clear();
		element.sendKeys(value);
	}

	public void setSmtpPassword(String value) {
		WebElement element = webDriver.findElement(By.className("smtpPasswordTextBox"));
		element.clear();
		element.sendKeys(value);
	}

	public void setSmtpRequireSSL(boolean value) {
		WebElement element = webDriver.findElement(By.className("requiresSSLCheckBox")).findElement(By.xpath(".."));
		boolean checked = element.getAttribute("class").contains("checked");
		if (value && !checked) {
			element.click();
		} else if (!value && checked) {
			element.click();
		}
	}

	public void setSmtpTrustServer(boolean value) {
		WebElement element = webDriver.findElement(By.className("trustserverCheckBox")).findElement(By.xpath(".."));
		boolean checked = element.getAttribute("class").contains("checked");
		if (value && !checked) {
			element.click();
		} else if (!value && checked) {
			element.click();
		}
	}

	public void setSmtpPort(String value) {
		WebElement element = webDriver.findElement(By.className("smtpPortTextBox"));
		element.clear();
		element.sendKeys(value);
	}

	public void test() {
		WebElement textButton = webDriver.findElement(By.className("verifyButton"));
		textButton.click();
		fluentWait(SMALL);
	}

	public void update() {
		WebElement footer = webDriver.findElement(By.id("organizationForm:updateButtons"));
		fluentWait(3);
		footer.findElements(By.tagName("input")).get(0).click();
		fluentWait(5);
	}
}
