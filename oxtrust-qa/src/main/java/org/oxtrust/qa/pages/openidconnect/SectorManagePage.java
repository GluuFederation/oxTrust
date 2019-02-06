package org.oxtrust.qa.pages.openidconnect;

import java.util.List;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.oxtrust.qa.pages.AbstractPage;

public class SectorManagePage extends AbstractPage {

	public void goToSectorAddPage() {
		WebElement element = webDriver.findElement(By.className("addSectorIdentifier"));
		element.click();
	}

	public void assertListIsNotEmpty() {
		Assert.assertFalse(isEmpty());
	}

	public void assertListIsEmpty() {
		Assert.assertTrue(isEmpty());
	}

	public boolean isEmpty() {
		WebElement form = webDriver.findElement(By.id("sectorIdentifiersFormId"));
		boolean isEmpty = false;
		try {
			WebElement table = form.findElement(By.tagName("table"));
			Assert.assertNotNull(table);
			WebElement body = table.findElement(By.tagName("tbody"));
			Assert.assertNotNull(body);
			List<WebElement> trs = body.findElements(By.tagName("tr"));
			Assert.assertNotNull(trs);
			Assert.assertNotNull(trs.get(0));
		} catch (Exception e) {
			isEmpty = true;
		}
		return isEmpty;
	}

	public void deleteFirstSector() {
		WebElement form = webDriver.findElement(By.id("sectorIdentifiersFormId"));
		WebElement table = form.findElement(By.id("sectorIdentifiersFormId:sectorIdentifierListId"));
		WebElement body = table.findElement(By.id("sectorIdentifiersFormId:sectorIdentifierListId:tb"));
		WebElement tr = body.findElements(By.tagName("tr")).get(0);
		tr.findElement(By.tagName("a")).click();
		fluentWait(ADJUST);
		webDriver.findElement(By.id("updateButtons")).findElements(By.tagName("input")).get(1).click();
		fluentWait(ADJUST);
		webDriver.findElement(By.id("deleteConfirmation:acceptRemovalModalPanel_content"))
				.findElement(By.className("confirmDialogButton")).click();
		fluentWait(ADJUST);
	}

}
