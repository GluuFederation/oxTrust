package org.oxtrust.qa.pages.configuration.json;

import java.util.List;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.oxtrust.qa.pages.AbstractPage;

public class OxAuthConfigurationPage extends AbstractPage {

	public void assertAuthorizationEndPointIsCorrect() {
		WebElement element = webDriver.findElement(By.name("root[authorizationEndpoint]"));
		Assert.assertNotNull(element);
	}

	public void assertTokenEndPointIsCorrect() {
		WebElement element = webDriver.findElement(By.name("root[tokenEndpoint]"));
		Assert.assertNotNull(element);
	}

	public void assertUserInfoEndPointIsCorrect() {
		WebElement element = webDriver.findElement(By.name("root[userInfoEndpoint]"));
		Assert.assertNotNull(element);
	}

	public void assertClientInfoEndPointIsCorrect() {
		WebElement element = webDriver.findElement(By.name("root[clientInfoEndpoint]"));
		Assert.assertNotNull(element);
	}

	public void assertEndSessionEndPointIsCorrect() {
		WebElement element = webDriver.findElement(By.name("root[endSessionEndpoint]"));
		Assert.assertNotNull(element);
	}

	public void assertRegistrationEndPointIsCorrect() {
		WebElement element = webDriver.findElement(By.name("root[registrationEndpoint]"));
		Assert.assertNotNull(element);
	}

	public void assertOidcDiscoveryEndPointIsCorrect() {
		WebElement element = webDriver.findElement(By.name("root[openIdDiscoveryEndpoint]"));
		Assert.assertNotNull(element);
	}

	public void assertOidcConfigEndPointIsCorrect() {
		WebElement element = webDriver.findElement(By.name("root[openIdConfigurationEndpoint]"));
		Assert.assertNotNull(element);
	}

	public void save() {
		webDriver.findElement(By.id("save_oxAuthConfig")).click();
	}

	public void changeMetricReportedValue(String value) {
		List<WebElement> elements = webDriver.findElements(By.name("root[metricReporterEnabled]"));
		Assert.assertTrue(elements.size() == 2);
		scrollDownUntil(elements.get(1));
		Select select = new Select(elements.get(1));
		select.selectByVisibleText(value);
	}

	public void setMetricReportedInterval(String value) {
		List<WebElement> elements = webDriver.findElements(By.name("root[metricReporterInterval]"));
		Assert.assertTrue(elements.size() == 2);
		WebElement element = elements.get(1);
		scrollDownUntil(element);
		element.clear();
		element.sendKeys(value);
	}

	public void waitConfig() {
		fluentWait(TWO_MINUTE);
		fluentWait(TWO_MINUTE);
		fluentWait(TWO_MINUTE);
	}

	public void saveOxtrust() {
		webDriver.findElement(By.id("save_oxTrustConfig")).click();		
	}

}
