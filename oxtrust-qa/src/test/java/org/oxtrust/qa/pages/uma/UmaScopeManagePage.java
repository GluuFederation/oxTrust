package org.oxtrust.qa.pages.uma;

import java.util.List;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.oxtrust.qa.pages.AbstractPage;

public class UmaScopeManagePage extends AbstractPage {

	private static final String RESOURCE_SCOPE_LIST_CLASS = "umaScopeListClass";

	public void assertUmaScopeExist(String scopeName) {
		Assert.assertTrue(assertElementExistInList(RESOURCE_SCOPE_LIST_CLASS, scopeName));
		fluentWait(ONE_SEC);
	}

	public void goToScopeAddPage() {
		webDriver.findElement(By.className("addScopeButtonClass")).click();
	}

	public void searchUmaScope(String scope) {
		WebElement searchBox = webDriver.findElement(By.className("searchBoxClass"));
		searchBox.clear();
		searchBox.sendKeys(scope);
		performSearch();
	}

	private void performSearch() {
		webDriver.findElement(By.className("searchButtonClass")).click();
		fluentWait(ONE_SEC);
	}

	public void editScope(String scope) {
		webDriver.findElement(By.className("umaScopeListClass"));
		WebElement body = webDriver.findElement(By.className("umaScopeListClass")).findElements(By.tagName("tbody"))
				.get(0);
		List<WebElement> listItems = body.findElements(By.tagName("tr"));
		for (WebElement element : listItems) {
			if (element.getText().contains(scope)) {
				System.out.println("######################True:" + element.getText());
				element.findElements(By.tagName("td")).get(0).findElement(By.tagName("a")).click();
				System.out.println("######################Click Done");
				break;
			}
		}
	}

	public void assertUmaScopeNotExist(String scopeName) {
		Assert.assertFalse(assertElementExistInList(RESOURCE_SCOPE_LIST_CLASS, scopeName));
	}

}
