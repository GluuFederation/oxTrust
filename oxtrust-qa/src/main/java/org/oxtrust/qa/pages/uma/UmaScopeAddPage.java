package org.oxtrust.qa.pages.uma;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.oxtrust.qa.pages.AbstractPage;

public class UmaScopeAddPage extends AbstractPage {
	private List<WebElement> items;

	public void save() {
		WebElement buttonBar = webDriver.findElement(By.className("box-footer"));
		buttonBar.click();
		buttonBar.findElements(By.tagName("input")).get(0).click();
		fluentWait(ONE_SEC);
	}

	private void initTableItems() {
		WebElement tableBody = webDriver.findElement(By.tagName("tbody"));
		items = tableBody.findElements(By.tagName("tr"));
	}

	public void setScopeId(String id) {
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

	public void setRandomScopeId() {
		String id = UUID.randomUUID().toString();
		setScopeId(id);
	}

	public void setDisplayName(String dn) {
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

	public void setLogo() {
		initTableItems();
		WebElement upLoarder = items.get(2).findElement(By.className("uploadFile"));
		WebElement addButton = upLoarder.findElement(By.cssSelector("input[type='file']"));
		File file = getResourceFile("qa_1.png");
		addButton.sendKeys(file.getAbsolutePath());
		fluentWait(SMALL);
	}

	public void setPolicy(String policy) {
		webDriver.findElement(By.className("AddAuthorizationButtonClass")).click();
		WebElement dialogBox = waitElementByID("authorizationPolicy:selectEntityModalPanel_content_scroller");
		WebElement content = dialogBox.findElement(By.tagName("table"));
		content = content.findElement(By.tagName("tbody"));
		content.click();
		WebElement element = content.findElement(By.tagName("tr"));
		element.click();
		List<WebElement> elements = element.findElements(By.xpath("following-sibling::tr"));
		selectPolicyNamed(elements.get(0).findElement(By.tagName("tbody")), policy);
	}

	private void selectPolicyNamed(WebElement table, String name) {
		List<WebElement> rows = table.findElements(By.tagName("tr"));
		for (WebElement row : rows) {
			System.out.println("Row :" + row.getText());
			if (row.getText().contains(name)) {
				row.click();
				break;
			}
		}

	}

}
