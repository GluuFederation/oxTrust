package org.oxtrust.qa.pages.saml;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.oxtrust.qa.pages.AbstractPage;

public class TrManagePage extends AbstractPage {

	private static final String TRUST_RELATIONSSHIP_LISTRELATIONSHIPSTABLE_TABLE = "trustRelationsshipListrelationshipstableTable";

	public void assertTrExist(String trName) {
		Assert.assertTrue(assertElementExistInList(TRUST_RELATIONSSHIP_LISTRELATIONSHIPSTABLE_TABLE, trName));

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
		WebElement table = webDriver.findElement(By.className(TRUST_RELATIONSSHIP_LISTRELATIONSHIPSTABLE_TABLE));
		WebElement body = table.findElement(By.tagName("tbody"));
		Assert.assertTrue(body.findElement(By.tagName("tr")).getText().contains(name));
		body.findElement(By.tagName("tr")).findElement(By.tagName("td")).click();
		fluentWait(ONE_SEC);
	}

	public void assertTrNotExist(String trName) {
		Assert.assertFalse(assertElementExistInList(TRUST_RELATIONSSHIP_LISTRELATIONSHIPSTABLE_TABLE, trName));
	}
}
