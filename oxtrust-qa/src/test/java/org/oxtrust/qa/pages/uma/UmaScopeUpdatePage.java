package org.oxtrust.qa.pages.uma;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.oxtrust.qa.pages.AbstractPage;

public class UmaScopeUpdatePage extends AbstractPage {
	private List<WebElement> items;

	public void editUmaScopeId(String id) {
		initTableItems();
		WebElement area = items.get(0).findElement(By.className("oxId"));
		area.findElement(By.tagName("span")).click();
		List<WebElement> inputs = items.get(0).findElements(By.cssSelector("input"));
		for (WebElement input : inputs) {
			if (input.getAttribute("type").equals("text")) {
				input.clear();
				input.sendKeys(id);
				break;
			}
		}
	}

	public void editUmaScopeDisplayName(String dn) {
		initTableItems();
		WebElement area = items.get(1).findElement(By.className("displayNameId"));
		area.findElement(By.tagName("span")).click();
		List<WebElement> inputs = items.get(1).findElements(By.cssSelector("input"));
		for (WebElement input : inputs) {
			if (input.getAttribute("type").equals("text")) {
				input.clear();
				input.sendKeys(dn);
				input.sendKeys(Keys.TAB);
				break;
			}
		}
	}

	public void save() {
		WebElement buttonBar = webDriver.findElement(By.className("box-footer"));
		buttonBar.click();
		buttonBar.findElements(By.tagName("input")).get(0).click();
		fluentWait(ONE_SEC);
	}

	public void delete() {
		WebElement buttonBar = webDriver.findElement(By.className("box-footer"));
		buttonBar.click();
		buttonBar.findElements(By.tagName("input")).get(1).click();
		fluentWait(ONE_SEC);
		WebElement dialog = webDriver.findElement(By.id("deleteConfirmation:acceptRemovalModalPanel_content"));
		dialog.findElements(By.tagName("input")).get(0).click();
		fluentWait(ONE_SEC);
	}

	private void initTableItems() {
		WebElement tableBody = webDriver.findElement(By.tagName("tbody"));
		items = tableBody.findElements(By.tagName("tr"));
	}

}
