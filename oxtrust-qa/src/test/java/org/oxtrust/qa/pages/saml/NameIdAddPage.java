package org.oxtrust.qa.pages.saml;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.oxtrust.qa.pages.AbstractPage;

public class NameIdAddPage extends AbstractPage {

	public void addNamedId(String source, String type, String enable) {

		WebElement section = webDriver.findElement(By.className("NewNamedID"));

		WebElement element = section.findElement(By.className("sourceAttributeSelectBox"));
		Select select = new Select(element);
		select.selectByVisibleText(source);
		WebElement element2 = section.findElement(By.className("nameIdTypeSelectBox"));
		Select select1 = new Select(element2);
		select1.selectByVisibleText(type);

		if (enable.equalsIgnoreCase("true")) {
			WebElement checkBoxButton = section.findElement(By.className("enableCheckBoxButton"));
			checkBoxButton.click();
		}
	}

	public void enable(String value) {

	}

	public void save() {
		WebElement footer = webDriver.findElement(By.id("updateButtons"));
		footer.findElements(By.tagName("input")).get(0).click();
		fluentWait(ONE_SEC);
	}

	public void assertNamedExist(String name) {
		Select select = new Select(webDriver.findElement(By.className("sourceAttributeSelectBox")));
		Assert.assertTrue(select.getFirstSelectedOption().getText().equalsIgnoreCase(name));
	}

	public void assertNamedDontExist(String name) {
		try {
			Select select = new Select(webDriver.findElement(By.className("sourceAttributeSelectBox")));
			Assert.assertFalse(select.getFirstSelectedOption().getText().equalsIgnoreCase(name));
		} catch (Exception e) {
			Assert.assertTrue(true);
		}

	}

}
