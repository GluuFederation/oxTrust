package org.oxtrust.qa.pages.openidconnect;

import java.util.List;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.oxtrust.qa.pages.AbstractPage;

public class OpenIdConnectClientManagePage extends AbstractPage {

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

	public void assertClientExist(String clientName) {
		Assert.assertTrue(assertClientExistInList(clientName));
	}

	private boolean assertClientExistInList(String clientName) {
		try {
			webDriver.findElement(By.className("umaClientListClass"));
			WebElement body = webDriver.findElement(By.className("umaClientListClass"))
					.findElements(By.tagName("tbody")).get(0);
			List<WebElement> listItems = body.findElements(By.tagName("tr"));
			boolean found = false;
			for (WebElement element : listItems) {
				if (element.getText().contains(clientName)) {
					found = true;
					break;
				}
			}
			return found;
		} catch (Exception e) {
			return false;
		}

	}

	public void editClient(String scope) {
		webDriver.findElement(By.className("umaClientListClass"));
		WebElement body = webDriver.findElement(By.className("umaClientListClass")).findElements(By.tagName("tbody"))
				.get(0);
		List<WebElement> listItems = body.findElements(By.tagName("tr"));
		for (WebElement element : listItems) {
			if (element.getText().contains(scope)) {
				element.findElements(By.tagName("td")).get(0).click();
				break;
			}
		}
	}

	public void goToClientAddPage() {
		webDriver.findElement(By.className("addClientButtonClass")).click();
	}

	public void edit() {
		WebElement footer = webDriver.findElement(By.id("updateButtons"));
		footer.findElements(By.tagName("input")).get(0).click();
		fluentWait(SMALL);
	}

	public void delete() {
		WebElement delete = webDriver.findElement(By.className("deleteOIDCButton"));
		delete.click();
		fluentWait(SMALL);
		WebElement dialog = webDriver.findElement(By.id("deleteConfirmation:acceptRemovalModalPanel_content"));
		dialog.findElements(By.tagName("input")).get(0).click();
		fluentWait(SMALL);
	}

	public void assertClientDontExist(String client) {
		Assert.assertFalse(assertClientExistInList(client));
	}

}
