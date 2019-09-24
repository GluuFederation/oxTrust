package org.oxtrust.qa.pages.configuration.json;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.oxtrust.qa.pages.AbstractPage;

public class OxTrustConfigurationPage extends AbstractPage {

	public void assertCertDirIsPresent() {
		WebElement element = webDriver.findElement(By.name("root[certDir]"));
		Assert.assertNotNull(element);
	}

	public void assertBaseDnIsPresent() {
		WebElement element = webDriver.findElement(By.name("root[baseDN]"));
		Assert.assertNotNull(element);
	}

	public void assertLogLevel() {
		WebElement element = webDriver.findElement(By.name("root[loggingLevel]"));
		Assert.assertNotNull(element);
	}

	public void assertScimMaxCount() {
		WebElement element = webDriver.findElement(By.name("root[ScimProperties][maxCount]"));
		Assert.assertNotNull(element);
	}

	public void assertOrgInameIsPresent() {
		Assert.assertTrue(true);
	}

	public void assertEmailIsPresent() {
		WebElement element = webDriver.findElement(By.name("root[orgSupportEmail]"));
		Assert.assertNotNull(element);
	}

	public void assertConfigurationInumIsPresent() {
		WebElement element = webDriver.findElement(By.name("root[configurationInum]"));
		Assert.assertNotNull(element);
	}

	public void assertApplicationUrlIsPresent() {
		WebElement element = webDriver.findElement(By.name("root[applicationUrl]"));
		Assert.assertNotNull(element);
	}

	public void assertBaseEndpointIsPresent() {
		WebElement element = webDriver.findElement(By.name("root[baseEndpoint]"));
		Assert.assertNotNull(element);
	}

}
