package org.oxtrust.qa.pages.openidconnect;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.oxtrust.qa.pages.AbstractPage;

public class SectorAddPage extends AbstractPage {

	public void addLoginRedirect(String url) {
		webDriver.findElements(By.className("AddLoginUrl")).get(0).click();
		WebElement pane = waitElementByID("loginRedirect:inputText_container");
		WebElement main = pane.findElement(By.id("loginRedirect:inputText_content"));
		List<WebElement> inputs = main.findElements(By.cssSelector("input"));
		for (WebElement input : inputs) {
			if (input.getAttribute("type").equals("text")) {
				input.clear();
				input.sendKeys(url);
			}
		}
		WebElement footer = main.findElement(By.className("box-footer"));
		footer.findElements(By.tagName("input")).get(0).click();
	}

	public void addClient(String client) {
		fluentWait(1);
		webDriver.findElements(By.className("AddClient")).get(0).click();
		WebElement pane = waitElementByID("client:selectClientModalPanel_content");
		WebElement searchBox = pane.findElement(By.className("searchParameters"));
		searchBox.clear();
		searchBox.sendKeys(client);
		WebElement searchButton = searchBox.findElements(By.xpath("following-sibling::input")).get(0);
		searchButton.click();
		fluentWait(ONE_SEC);
		waitElementByTag("table");
		List<WebElement> popups = webDriver.findElements(By.tagName("table"));
		WebElement popupBody = popups.get(popups.size() - 1).findElement(By.tagName("tbody"));
		WebElement row = popupBody.findElements(By.tagName("tr")).get(0);
		row.findElements(By.tagName("td")).get(0).findElement(By.tagName("input")).click();

		WebElement panel = waitElementByID("client:selectClientModalPanel_content");
		List<WebElement> inputs = panel.findElements(By.tagName("input"));
		for (WebElement input : inputs) {
			if (input.getAttribute("value").equals("Ok")) {
				input.click();
				break;
			}
		}
		fluentWait(1);
	}

	public void save() {
		WebElement footer = webDriver.findElement(By.className("box-footer"));
		footer.findElements(By.tagName("input")).get(0).click();
		fluentWait(SMALL);
	}

}
