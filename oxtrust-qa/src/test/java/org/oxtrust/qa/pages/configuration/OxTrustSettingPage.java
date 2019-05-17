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
		WebElement orgNameTextField = webDriver.findElement(By.id("organizationForm:displayName:outputInputPanel"));
		String orgName = orgNameTextField.findElement(By.tagName("input")).getAttribute("value");
		Assert.assertFalse(orgName.isEmpty());
	}

	public void setLogo() {
		try {
			pickOrgLogo();
		} catch (NoSuchElementException e) {
			removeOrgLogo();
			pickOrgLogo();
		}

	}

	public void setFavIcon() {
		try {
			pickOrgFavicon();
		} catch (NoSuchElementException e) {
			removeOrgFavicon();
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
		WebElement upLoarder = webDriver.findElement(By.className("uploadLogo"));
		WebElement addButton = upLoarder.findElement(By.cssSelector("input[type='file']"));
		addButton.sendKeys(getResourceFile("qa_logo.png").getAbsolutePath());
		fluentWait(SMALL);
	}

	private void pickOrgFavicon() {
		WebElement upLoarder = webDriver.findElement(By.className("uploadFavicon"));
		WebElement addButton = upLoarder.findElement(By.cssSelector("input[type='file']"));
		addButton.sendKeys(getResourceFile("qa_favicon.jpeg").getAbsolutePath());
		fluentWait(SMALL);
	}

	public void setOrgName(String orgName) {
		WebElement orgNameTextZone = webDriver.findElement(By.id("organizationForm:displayName:outputInputPanel"));
		WebElement orgNameTextInput = orgNameTextZone.findElement(By.tagName("input"));
		orgNameTextInput.clear();
		orgNameTextInput.sendKeys(orgName);
	}

	public void save() {
		scrollDown();
		WebElement footer = webDriver.findElement(By.id("organizationForm:updateButtons"));
		footer.findElements(By.tagName("input")).get(0).click();
		fluentWait(SMALL);
	}

	public void checkOrgName(String orgName) {
		WebElement orgNameTextZone = webDriver.findElement(By.id("organizationForm:displayName:outputInputPanel"));
		WebElement orgNameTextInput = orgNameTextZone.findElement(By.tagName("input"));
		Assert.assertTrue(orgNameTextInput.getAttribute("value").equalsIgnoreCase(orgName));
	}

	public void setLogLevel(String level) {
		WebElement element=webDriver.findElements(By.name("root[loggingLevel]")).get(0);
		Select select=new Select(element);
		select.selectByVisibleText(level);
	}

}
