package org.oxtrust.qa.pages.openidconnect;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.oxtrust.qa.pages.AbstractPage;

public class OpenIdConnectScopeAddPage extends AbstractPage {

	public void save() {
		fluentWait(SMALL);
		WebElement buttonBar = webDriver.findElement(By.className("box-footer"));
		buttonBar.click();
		buttonBar.findElements(By.tagName("input")).get(0).click();
		fluentWait(SMALL);
	}

	public void setDisplayName(String dn) {
		WebElement input = webDriver.findElement(By.className("displayNameId"));
		input.clear();
		input.sendKeys(dn);
		input.sendKeys(Keys.TAB);
	}

	public void setDescription(String des) {
		WebElement description = webDriver.findElement(By.tagName("textarea"));
		description.clear();
		description.sendKeys(des);
	}

	public void setType(String type) {
		WebElement selectBox = webDriver.findElement(By.className("scopeType"));
		Select select = new Select(selectBox);
		select.selectByVisibleText(type);
	}

	public void setRegistrationType(String rType) {
		enableCheckBox("registrationType");
	}

}
