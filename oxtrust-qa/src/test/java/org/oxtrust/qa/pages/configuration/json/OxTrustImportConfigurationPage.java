package org.oxtrust.qa.pages.configuration.json;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.oxtrust.qa.pages.AbstractPage;

public class OxTrustImportConfigurationPage extends AbstractPage {
	private WebElement main;

	private void init() {
		main = webDriver.findElement(By.id("oxTrustImportPersonConfig"));
	}

	public void veryMappingEntries() {
		init();
		WebElement entry0 = main.findElement(By.cssSelector("div[data-schemapath='root.mappings.0']"));
		Assert.assertNotNull(entry0);
		WebElement ldapName0 = entry0.findElement(By.cssSelector("div[data-schemapath='root.mappings.0.ldapName']"));
		Assert.assertNotNull(ldapName0);
		WebElement displayName0 = entry0
				.findElement(By.cssSelector("div[data-schemapath='root.mappings.0.displayName']"));
		Assert.assertNotNull(displayName0);
		WebElement type0 = entry0.findElement(By.cssSelector("div[data-schemapath='root.mappings.0.dataType']"));
		Assert.assertNotNull(type0);
		WebElement required0 = entry0.findElement(By.cssSelector("div[data-schemapath='root.mappings.0.required']"));
		Assert.assertNotNull(required0);

		WebElement entry1 = main.findElement(By.cssSelector("div[data-schemapath='root.mappings.1']"));
		Assert.assertNotNull(entry1);
		WebElement ldapName1 = entry1.findElement(By.cssSelector("div[data-schemapath='root.mappings.1.ldapName']"));
		Assert.assertNotNull(ldapName1);
		WebElement displayName1 = entry1
				.findElement(By.cssSelector("div[data-schemapath='root.mappings.1.displayName']"));
		Assert.assertNotNull(displayName1);
		WebElement type1 = entry1.findElement(By.cssSelector("div[data-schemapath='root.mappings.1.dataType']"));
		Assert.assertNotNull(type1);
		WebElement required1 = entry1.findElement(By.cssSelector("div[data-schemapath='root.mappings.1.required']"));
		Assert.assertNotNull(required1);

		WebElement entry2 = main.findElement(By.cssSelector("div[data-schemapath='root.mappings.2']"));
		Assert.assertNotNull(entry2);
		WebElement ldapName2 = entry2.findElement(By.cssSelector("div[data-schemapath='root.mappings.2.ldapName']"));
		Assert.assertNotNull(ldapName2);
		WebElement displayName2 = entry2
				.findElement(By.cssSelector("div[data-schemapath='root.mappings.2.displayName']"));
		Assert.assertNotNull(displayName2);
		WebElement type2 = entry2.findElement(By.cssSelector("div[data-schemapath='root.mappings.2.dataType']"));
		Assert.assertNotNull(type2);
		WebElement required2 = entry2.findElement(By.cssSelector("div[data-schemapath='root.mappings.2.required']"));
		Assert.assertNotNull(required2);

		WebElement entry3 = main.findElement(By.cssSelector("div[data-schemapath='root.mappings.3']"));
		Assert.assertNotNull(entry3);
		WebElement ldapName3 = entry3.findElement(By.cssSelector("div[data-schemapath='root.mappings.3.ldapName']"));
		Assert.assertNotNull(ldapName3);
		WebElement displayName3 = entry3
				.findElement(By.cssSelector("div[data-schemapath='root.mappings.3.displayName']"));
		Assert.assertNotNull(displayName3);
		WebElement type3 = entry3.findElement(By.cssSelector("div[data-schemapath='root.mappings.3.dataType']"));
		Assert.assertNotNull(type3);
		WebElement required3 = entry3.findElement(By.cssSelector("div[data-schemapath='root.mappings.3.required']"));
		Assert.assertNotNull(required3);

		WebElement entry4 = main.findElement(By.cssSelector("div[data-schemapath='root.mappings.4']"));
		Assert.assertNotNull(entry4);
		WebElement ldapName4 = entry4.findElement(By.cssSelector("div[data-schemapath='root.mappings.4.ldapName']"));
		Assert.assertNotNull(ldapName4);
		WebElement displayName4 = entry4
				.findElement(By.cssSelector("div[data-schemapath='root.mappings.4.displayName']"));
		Assert.assertNotNull(displayName4);
		WebElement type4 = entry4.findElement(By.cssSelector("div[data-schemapath='root.mappings.4.dataType']"));
		Assert.assertNotNull(type4);
		WebElement required4 = entry4.findElement(By.cssSelector("div[data-schemapath='root.mappings.4.required']"));
		Assert.assertNotNull(required4);

		WebElement entry5 = main.findElement(By.cssSelector("div[data-schemapath='root.mappings.5']"));
		Assert.assertNotNull(entry5);
		WebElement ldapName5 = entry5.findElement(By.cssSelector("div[data-schemapath='root.mappings.5.ldapName']"));
		Assert.assertNotNull(ldapName5);
		WebElement displayName5 = entry5
				.findElement(By.cssSelector("div[data-schemapath='root.mappings.5.displayName']"));
		Assert.assertNotNull(displayName5);
		WebElement type5 = entry5.findElement(By.cssSelector("div[data-schemapath='root.mappings.5.dataType']"));
		Assert.assertNotNull(type5);
		WebElement required5 = entry5.findElement(By.cssSelector("div[data-schemapath='root.mappings.5.required']"));
		Assert.assertNotNull(required5);
	}

}
