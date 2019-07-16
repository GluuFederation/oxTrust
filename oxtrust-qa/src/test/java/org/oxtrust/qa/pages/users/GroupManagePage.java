package org.oxtrust.qa.pages.users;

import java.util.List;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.oxtrust.qa.pages.AbstractPage;

public class GroupManagePage extends AbstractPage {
	private WebElement tableBody;
	private WebElement foundGroup;

	public void searchGroup(String pattern) {
		WebElement searchBox = webDriver.findElement(By.className("searchTextBox"));
		searchBox.sendKeys(pattern);
		webDriver.findElement(By.className("searchButton"));
		fluentWait(ONE_SEC);
	}

	public boolean groupExistList(String username) {
		try {
			tableBody = webDriver.findElement(By.id("groupsFormId:groupsListId")).findElements(By.tagName("tbody"))
					.get(0);
		} catch (Exception e) {
			return false;
		}

		List<WebElement> trs = tableBody.findElements(By.tagName("tr"));
		boolean found = false;
		for (WebElement element : trs) {
			if (element.getText().contains(username)) {
				found = true;
				foundGroup = element;
				break;
			}
		}
		return found;
	}

	public void assertGroupWithExist(String value) {
		Assert.assertTrue(groupExistList(value));
	}

	public void startGroupUpdate() {
		foundGroup.findElements(By.tagName("td")).get(0).click();
	}

	public void assertGroupNotWithExist(String value) {
		Assert.assertFalse(groupExistList(value));
	}

}
