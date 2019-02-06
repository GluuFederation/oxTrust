package org.oxtrust.qa.pages.configuration.attributesImExport;

import java.io.File;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.oxtrust.qa.pages.AbstractPage;

public class AttributesImportPage extends AbstractPage {

	public void importAttributesFromFile(String fileName) {
		WebElement upLoader = webDriver.findElement(By.className("uploadFile"));
		WebElement addButton = upLoader.findElement(By.cssSelector("input[type='file']"));
		String finalPath = getUserDir().concat(File.separator).concat("attributes.ldif");
		addButton.sendKeys(finalPath);
		fluentWait(ONE_SEC);
	}

	public void validateAndImport() {
		webDriver.findElement(By.className("validateButton")).click();
		fluentWait(ONE_SEC);
		webDriver.findElement(By.className("importButton")).click();
		fluentWait(ONE_SEC);
	}

}
