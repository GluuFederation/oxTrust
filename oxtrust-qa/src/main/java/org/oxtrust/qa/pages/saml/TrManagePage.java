package org.oxtrust.qa.pages.saml;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.oxtrust.qa.pages.AbstractPage;

public class TrManagePage extends AbstractPage {

	public void assertTrExist(String trName) {
		WebElement form = webDriver.findElement(By.id("trustRelationsshipList:relationshipstable"));
		WebElement body = form.findElement(By.id("trustRelationsshipList:relationshipstable:tb"));
		Assert.assertTrue(body.findElement(By.tagName("tr")).getText().contains(trName));
	}

	public void searchFor(String pattern) {
		WebElement searchField = webDriver.findElement(By.className("searchBox"));
		searchField.clear();
		searchField.sendKeys(pattern);
		WebElement searchButton = webDriver.findElement(By.className("searchButton"));
		searchButton.click();
		fluentWait(ONE_SEC);
	}

	public void goToDetailOf(String name) {
		fluentWait(ONE_SEC);
		WebElement form = webDriver.findElement(By.id("trustRelationsshipList:relationshipstable"));
		WebElement body = form.findElement(By.id("trustRelationsshipList:relationshipstable:tb"));
		Assert.assertTrue(body.findElement(By.tagName("tr")).getText().contains(name));
		body.findElement(By.tagName("tr")).findElement(By.tagName("td")).click();
		fluentWait(ONE_SEC);
	}

	public void assertTrNotExist(String trName) {
		boolean exist = false;
		try {
			WebElement form = webDriver.findElement(By.id("trustRelationsshipList:relationshipstable"));
			WebElement body = form.findElement(By.id("trustRelationsshipList:relationshipstable:tb"));
			Assert.assertTrue(body.findElement(By.tagName("tr")).getText().contains(trName));
			exist = true;
		} catch (Exception e) {
		}
		Assert.assertFalse(exist);
	}
}
