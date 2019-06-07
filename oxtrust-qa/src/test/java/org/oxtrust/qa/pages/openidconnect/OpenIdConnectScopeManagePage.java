package org.oxtrust.qa.pages.openidconnect;

import java.util.List;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.oxtrust.qa.pages.AbstractPage;

public class OpenIdConnectScopeManagePage extends AbstractPage {

	private static final String SCOPES_FORM_IDSCOPES_LIST_ID_TABLE = "scopesFormIdscopesListIdTable";

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
		Assert.assertTrue(assertElementExistInList(SCOPES_FORM_IDSCOPES_LIST_ID_TABLE, scopeName));
	}

	public void editScope(String scope) {
		webDriver.findElement(By.className(SCOPES_FORM_IDSCOPES_LIST_ID_TABLE));
		WebElement body = webDriver.findElement(By.className(SCOPES_FORM_IDSCOPES_LIST_ID_TABLE))
				.findElements(By.tagName("tbody")).get(0);
		List<WebElement> listItems = body.findElements(By.tagName("tr"));
		for (WebElement element : listItems) {
			if (element.getText().contains(scope)) {
				element.findElements(By.tagName("td")).get(0).findElement(By.tagName("a")).click();
				break;
			}
		}
	}

	public void goToScopeAddPage() {
		fluentWait(1);
		webDriver.findElement(By.className("addScopeButtonClass")).click();
	}

	public void assertScopeDontExist(String scope) {
		Assert.assertFalse(assertElementExistInList(SCOPES_FORM_IDSCOPES_LIST_ID_TABLE, scope));
	}

}
