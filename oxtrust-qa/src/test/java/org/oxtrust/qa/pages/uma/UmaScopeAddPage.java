package org.oxtrust.qa.pages.uma;

import java.util.List;
import java.util.UUID;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.oxtrust.qa.pages.AbstractPage;

public class UmaScopeAddPage extends AbstractPage {

	public void save() {
		WebElement buttonBar = webDriver.findElement(By.className("box-footer"));
		buttonBar.click();
		buttonBar.findElements(By.tagName("input")).get(0).click();
		fluentWait(2);
	}

	public void setScopeId(String id) {
		WebElement input = webDriver.findElement(By.className("oxId"));
		input.clear();
		input.sendKeys(id);
	}

	public void setRandomScopeId() {
		String id = UUID.randomUUID().toString();
		setScopeId(id);
	}

	public void setDisplayName(String dn) {
		WebElement input = webDriver.findElement(By.className("displayNameId"));
		input.clear();
		input.sendKeys(dn);
		input.sendKeys(Keys.TAB);
	}

	public void setLogo(String url) {
		WebElement input = webDriver.findElement(By.className("IconUrlId"));
		input.clear();
		input.sendKeys(url);
		input.sendKeys(Keys.TAB);
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
