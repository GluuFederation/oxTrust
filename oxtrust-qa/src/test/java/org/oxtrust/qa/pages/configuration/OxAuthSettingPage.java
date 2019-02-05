package org.oxtrust.qa.pages.configuration;


import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.oxtrust.qa.pages.AbstractPage;

public class OxAuthSettingPage extends AbstractPage {

	public void setServerIp(String ip) {
		WebElement element = webDriver.findElement(By.className("serverIpTextBox"));
		element.clear();
		element.sendKeys(ip);
	}

	public void checkServerIpIsEmpty() {
		WebElement element = webDriver.findElement(By.className("serverIpTextBox"));
		Assert.assertTrue(element.getAttribute("value").isEmpty());
	}
	
	public void setLogLevel(String level) {
		WebElement element=webDriver.findElements(By.name("root[loggingLevel]")).get(1);
		Select select=new Select(element);
		select.selectByVisibleText(level);
	}

}
