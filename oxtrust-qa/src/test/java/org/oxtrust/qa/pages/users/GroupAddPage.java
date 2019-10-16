package org.oxtrust.qa.pages.users;

import java.util.List;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.oxtrust.qa.pages.AbstractPage;

public class GroupAddPage extends AbstractPage {

	public void fillDisplayName(String value) {
		WebElement input = webDriver.findElement(By.className("displayNameField"));
		input.clear();
		input.sendKeys(value);
	}

	public void fillDescription(String value) {
		WebElement element = webDriver.findElement(By.tagName("textarea"));
		element.clear();
		element.sendKeys(value);
	}

	public void fillVisiblility(String value) {
		WebElement dropDown = webDriver.findElement(By.className("visibilityField"));
		Select select = new Select(dropDown);
		select.selectByVisibleText(value);
	}

	public void save() {
		WebElement footer = webDriver.findElement(By.id("updateButtons"));
		footer.findElements(By.tagName("button")).get(0).click();
		fluentWait(ONE_SEC);
	}

	public void cancel() {
		WebElement footer = webDriver.findElement(By.id("updateButtons"));
		footer.findElements(By.tagName("input")).get(1).click();
	}

	public void pickUser(String user) {
		WebElement addButton = webDriver.findElement(By.className("AddMember"));
		addButton.click();
		fluentWait(ONE_SEC);
		WebElement dialogBox = webDriver.findElement(By.id("member:selectMemberModalPanel_container"));
		WebElement content = dialogBox.findElement(By.id("member:selectMemberModalPanel_content"));
		content = content.findElement(By.tagName("table"));
		List<WebElement> inupts = content.findElements(By.tagName("input"));
		inupts.get(0).clear();
		inupts.get(0).sendKeys(user);
		inupts.get(1).click();
		fluentWait(ONE_SEC);
		WebElement dialogBox1 = waitElementByID("member:selectMemberModalPanel_container");
		WebElement main = dialogBox1.findElement(By.id("member:selectMemberModalPanel_content"));
		WebElement result = main.findElement(By.tagName("table"));
		WebElement searchResult = result.findElements(By.tagName("tr")).get(1);
		Assert.assertNotNull(searchResult);
		WebElement searchResult1 = searchResult.findElement(By.tagName("tbody"));
		searchResult1.click();
		fluentWait(ONE_SEC);
		WebElement firstRow = searchResult1.findElements(By.tagName("tr")).get(0);
		Assert.assertNotNull(firstRow);
		firstRow.findElements(By.tagName("td")).get(0).click();

		List<WebElement> trs = result.findElements(By.tagName("tr"));
		WebElement buttonZone = trs.get(trs.size() - 1);
		buttonZone.findElements(By.tagName("input")).get(0).click();
		fluentWait(ADJUST);
	}
}
