package org.oxtrust.qa.pages.openidconnect;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.oxtrust.qa.pages.AbstractPage;

public class OpenIdConnectScopeAddPage extends AbstractPage {

	public void save() {
		WebElement buttonBar = webDriver.findElement(By.className("box-footer"));
		buttonBar.click();
		buttonBar.findElements(By.tagName("input")).get(0).click();
		fluentWait(SMALL);
	}

	public void setDisplayName(String dn) {
		WebElement main = webDriver.findElement(By.className("box-header"));
		WebElement input = main.findElement(By.className("displayNameId"));
		input.clear();
		input.sendKeys(dn);
		input.sendKeys(Keys.TAB);
	}

	public void setDescription(String des) {
		WebElement main = webDriver.findElement(By.className("box-header"));
		WebElement description = main.findElement(By.tagName("textarea"));
		description.clear();
		description.sendKeys(des);
	}

	public void setType(String type) {
		WebElement main = webDriver.findElement(By.className("box-header"));
		WebElement selectBox = main.findElements(By.tagName("select")).get(0);
		Select select = new Select(selectBox);
		select.selectByVisibleText(type);
	}

	public void setRegistrationType(String rType) {
		enableCheckBox("registrationType");
	}

}
