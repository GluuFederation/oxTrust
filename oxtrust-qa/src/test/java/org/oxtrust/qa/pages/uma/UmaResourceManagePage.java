package org.oxtrust.qa.pages.uma;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.oxtrust.qa.pages.AbstractPage;

public class UmaResourceManagePage extends AbstractPage {

	public void assertUmaResourceExist(String resName, String scopeName) {
		Assert.assertTrue(assertElementExistInList("resourceScopeListClass", resName, scopeName));
	}

	public void goToResourceAddPage() {
	}

	public void searchUmaResource(String scope) {
		WebElement searchBox = webDriver.findElement(By.className("searchBoxClass"));
		searchBox.clear();
		searchBox.sendKeys(scope);
		performSearch();
	}

	private void performSearch() {
		webDriver.findElement(By.className("searchButtonClass")).click();
		fluentWait(ONE_SEC);
	}
}
