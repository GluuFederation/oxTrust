package org.oxtrust.qa.pages.configuration;


import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.oxtrust.qa.pages.AbstractPage;

public class OxTrustSettingPage extends AbstractPage {

	public void checkGroup(String groupName) {
		WebElement group = webDriver.findElement(By.id("organizationForm:managerGroup:outputInputPanel"));
		Assert.assertTrue(group.findElement(By.tagName("a")).getText().equalsIgnoreCase(groupName));

	}

	public void checkOrgNameIsNotEmpty() {
		WebElement orgNameTextZone = webDriver.findElement(By.className("orgNameTextBox"));
		String orgName = orgNameTextZone.getAttribute("value");
		Assert.assertFalse(orgName.isEmpty());
	}

	public void setLogo() {
		try {
			pickOrgLogo();
		} catch (NoSuchElementException e) {
			pickOrgLogo();
		}

	}

	public void setFavIcon() {
		try {
			pickOrgFavicon();
		} catch (NoSuchElementException e) {
			pickOrgFavicon();
		}
	}

	public void removeOrgLogo() {
		WebElement removeButton = webDriver.findElement(By.className("removeLogo"));
		removeButton.click();
		fluentWait(LITTLE);
	}

	public void removeOrgFavicon() {
		WebElement removeButton = webDriver.findElement(By.className("removeFavicon"));
		removeButton.click();
		fluentWait(LITTLE);
	}

	private void pickOrgLogo() {
		WebElement upLoarder = webDriver.findElement(By.id("organizationForm:oxTrustLogo"));
		WebElement addButton = upLoarder.findElement(By.cssSelector("input[type='file']"));
		addButton.sendKeys(getResourceFile("qa_logo.png").getAbsolutePath());
		fluentWait(SMALL);
	}

	private void pickOrgFavicon() {
		WebElement upLoarder = webDriver.findElement(By.id("organizationForm:oxTrustFavicon"));
		WebElement addButton = upLoarder.findElement(By.cssSelector("input[type='file']"));
		addButton.sendKeys(getResourceFile("qa_favicon.jpeg").getAbsolutePath());
		fluentWait(SMALL);
	}

	public void setOrgName(String orgName) {
		WebElement orgNameTextZone = webDriver.findElement(By.className("orgNameTextBox"));
		orgNameTextZone.clear();
		orgNameTextZone.sendKeys(orgName);
	}

	public void save() {
		scrollDown();
		WebElement footer = webDriver.findElement(By.id("organizationForm:updateButtons"));
		footer.findElements(By.tagName("input")).get(0).click();
		fluentWait(SMALL);
	}

	public void checkOrgName(String orgName) {
		WebElement orgNameTextZone = webDriver.findElement(By.className("orgNameTextBox"));
		Assert.assertTrue(orgNameTextZone.getAttribute("value").equalsIgnoreCase(orgName));
	}

	public void setLogLevel(String level) {
		WebElement element=webDriver.findElements(By.name("root[loggingLevel]")).get(0);
		Select select=new Select(element);
		select.selectByVisibleText(level);
	}

}
