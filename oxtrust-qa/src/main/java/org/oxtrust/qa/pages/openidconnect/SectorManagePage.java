package org.oxtrust.qa.pages.openidconnect;

import java.util.List;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
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
		boolean isEmpty = false;
		try {
			WebElement body = webDriver.findElement(By.className("sectorIdentifiersFormIdsectorIdentifierListIdTable"))
					.findElements(By.tagName("tbody")).get(0);

			Assert.assertNotNull(body);
			List<WebElement> trs = body.findElements(By.tagName("tr"));
			Assert.assertNotNull(trs);
			Assert.assertNotNull(trs.get(0));
		} catch (NoSuchElementException e) {
			isEmpty = true;
		}
		return isEmpty;
	}

	public void deleteFirstSector() {
		WebElement body = webDriver.findElement(By.className("sectorIdentifiersFormIdsectorIdentifierListIdTable"))
				.findElements(By.tagName("tbody")).get(0);
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
