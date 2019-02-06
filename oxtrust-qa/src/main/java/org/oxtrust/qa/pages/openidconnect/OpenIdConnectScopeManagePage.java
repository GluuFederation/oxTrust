package org.oxtrust.qa.pages.openidconnect;

import java.util.List;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.oxtrust.qa.pages.AbstractPage;

public class OpenIdConnectScopeManagePage extends AbstractPage {

	public void searchFor(String scope) {
		WebElement searchBox = webDriver.findElement(By.className("searchBoxClass"));
		searchBox.clear();
		searchBox.sendKeys(scope);
		performSearch();
	}

	private void performSearch() {
		webDriver.findElement(By.className("searchButtonClass")).click();
		fluentWait(SMALL);
	}

	public void assertScopeExist(String scopeName) {
		Assert.assertTrue(assertScopeExistInList(scopeName));
	}

	private boolean assertScopeExistInList(String umaScope) {
		try {
			webDriver.findElement(By.className("umaScopeListClass"));
			WebElement body = webDriver.findElement(By.className("umaScopeListClass")).findElements(By.tagName("tbody"))
					.get(0);
			List<WebElement> listItems = body.findElements(By.tagName("tr"));
			boolean found = false;
			for (WebElement element : listItems) {
				if (element.getText().contains(umaScope)) {
					found = true;
					break;
				}
			}
			return found;
		} catch (Exception e) {
			return false;
		}

	}

	public void editScope(String scope) {
		webDriver.findElement(By.className("umaScopeListClass"));
		WebElement body = webDriver.findElement(By.className("umaScopeListClass")).findElements(By.tagName("tbody"))
				.get(0);
		List<WebElement> listItems = body.findElements(By.tagName("tr"));
		for (WebElement element : listItems) {
			if (element.getText().contains(scope)) {
				element.findElements(By.tagName("td")).get(0).click();
				break;
			}
		}
	}

	public void goToScopeAddPage() {
		webDriver.findElement(By.className("addScopeButtonClass")).click();
	}

	public void assertScopeDontExist(String scope) {
		Assert.assertFalse(assertScopeExistInList(scope));
	}

}
