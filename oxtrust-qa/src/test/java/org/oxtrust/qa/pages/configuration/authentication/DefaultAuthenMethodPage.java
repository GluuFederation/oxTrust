package org.oxtrust.qa.pages.configuration.authentication;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.oxtrust.qa.pages.AbstractPage;

public class DefaultAuthenMethodPage extends AbstractPage {

	public void checkDefaultAcr(String defaultAcr) {
		Select select = new Select(webDriver.findElement(By.className("defaultAcrSelectBox")));
		Assert.assertTrue(defaultAcr.equalsIgnoreCase(select.getFirstSelectedOption().getText()));
	}

	public void setDefaultAcr(String defaultAcr) {
		Select select = new Select(webDriver.findElement(By.className("defaultAcrSelectBox")));
		select.selectByVisibleText(defaultAcr);
	}

	public void setOxtrustAcr(String acr) {
		Select select = new Select(webDriver.findElement(By.className("oxTrustAcrSelectBox")));
		select.selectByVisibleText(acr);
	}

	public void checkOxtrustAcr(String acr) {
		Select select = new Select(webDriver.findElement(By.className("oxTrustAcrSelectBox")));
		Assert.assertTrue(acr.equalsIgnoreCase(select.getFirstSelectedOption().getText()));
	}

	public void save() {
		WebElement footer = webDriver.findElement(By.id("updateButtons"));
		footer.findElements(By.tagName("input")).get(0).click();
		fluentWait(SMALL);
	}

}
