package org.oxtrust.qa.pages.configuration.authentication;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.oxtrust.qa.pages.AbstractPage;

public class PassportAuthenticationPage extends AbstractPage {

	public void save() {
		WebElement footer = webDriver.findElement(By.id("updateButtons"));
		footer.findElements(By.tagName("input")).get(0).click();
		fluentWait(ONE_SEC);
	}

	public void setPassportStatus(String status) {
		Select select = new Select(webDriver.findElement(By.className("passportEnableSelectBox")));
		select.selectByVisibleText(status);
	}

	public void addStrategy(String name, String id, String secret) {
		WebElement addStrategyButton = webDriver.findElement(By.className("addStrategyButton"));
		addStrategyButton.click();
		fluentWait(ONE_SEC);

		WebElement strategiesArea = webDriver.findElement(By.id("customAuthenticationForm:strategyId"));
		WebElement strategyArea = strategiesArea.findElement(By.className("NewEmptyStrategy"));

		WebElement strategyName = strategyArea.findElement(By.className("strategyNameTextBox"));
		strategyName.clear();
		strategyName.sendKeys(name);

		WebElement value1 = strategyArea.findElements(By.className("propertyValueTextBox")).get(0);
		value1.clear();
		value1.sendKeys(id);

		WebElement value2 = strategyArea.findElements(By.className("propertyValueTextBox")).get(1);
		value2.clear();
		value2.sendKeys(secret);
		fluentWait(ONE_SEC);
		save();
	}

	public void deleteStrategy(String name) {
		WebElement strategyBox = webDriver.findElement(By.className(name));
		WebElement delete = strategyBox.findElement(By.className("deleteStrategy"));
		delete.click();
		fluentWait(ONE_SEC);
		String classname = "dialogBoxPanelFor".concat(name);
		WebElement button = webDriver.findElement(By.className(classname))
				.findElement(By.className("confirmDialogButton"));
		button.click();
		fluentWait(ONE_SEC);
	}

	public void assertStrategyIsNotPresent(String name) {
		boolean found = false;
		try {
			WebElement stragtetyBox = webDriver.findElement(By.className(name));
			Assert.assertNotNull(stragtetyBox);
			found = true;
		} catch (Exception e) {
			found = false;
		}

		Assert.assertFalse(found);
	}

	public void assertStrategyIsPresent(String name) {
		boolean found = false;
		try {
			WebElement stragtetyBox = webDriver.findElement(By.className(name));
			Assert.assertNotNull(stragtetyBox);
			found = true;
		} catch (Exception e) {
			found = false;
		}

		Assert.assertTrue(found);
	}

}
