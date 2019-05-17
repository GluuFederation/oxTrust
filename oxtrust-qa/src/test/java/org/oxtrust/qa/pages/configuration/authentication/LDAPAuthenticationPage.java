package org.oxtrust.qa.pages.configuration.authentication;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.oxtrust.qa.pages.AbstractPage;

public class LDAPAuthenticationPage extends AbstractPage {
	private WebElement table;
	private List<WebElement> sourceServers = new ArrayList<WebElement>();

	private void init() {
		table = webDriver.findElement(By.id("customAuthenticationForm:sourceConfigsId"));
		WebElement first = table.findElements(By.tagName("tr")).get(0);
		sourceServers.clear();
		sourceServers.add(first);
		sourceServers.addAll(first.findElements(By.xpath("following-sibling::tr")));
	}

	public void checkLdapSourceServer(String name, String bindDn, String maxCon, String pKey, String lPKey,
			String servers, String baseDn, String useSSl) {
		init();
		WebElement currentSource = getRightSourceServer(name);

		String nameValue = currentSource.findElement(By.className("nameTextBox")).getAttribute("value");
		Assert.assertTrue(nameValue.equalsIgnoreCase(name));

		String bindDnValue = currentSource.findElement(By.className("bindDnTextBox")).getAttribute("value");
		Assert.assertNotNull(bindDnValue);

		String maxConValue = currentSource.findElement(By.className("maxConnectionTextBox")).getAttribute("value");
		Assert.assertTrue(maxConValue.equalsIgnoreCase(maxCon));

		String pKeyValue = currentSource.findElement(By.className("primaryKeyTextBox")).getAttribute("value");
		Assert.assertTrue(pKeyValue.equalsIgnoreCase(pKey));

		String lPKeyValue = currentSource.findElement(By.className("localPrimaryKeyTextBox")).getAttribute("value");
		Assert.assertTrue(lPKeyValue.equalsIgnoreCase(lPKey));

		String selector1 = "input[class='col-md-6 form-control " + servers + "']";
		WebElement serversValue = currentSource.findElement(By.cssSelector(selector1));
		Assert.assertTrue(serversValue.getAttribute("value").equalsIgnoreCase(servers));

		String selector2 = "input[class='col-md-6 form-control " + baseDn + "']";
		WebElement baseDnValue = currentSource.findElement(By.cssSelector(selector2));
		Assert.assertTrue(baseDnValue.getAttribute("value").equalsIgnoreCase(baseDn));

		WebElement useSslBox = currentSource.findElement(By.className("useSSLSelectBox"));
		String classes = useSslBox.findElement(By.xpath("..")).getAttribute("class");
		if (useSSl.equalsIgnoreCase("true")) {
			Assert.assertTrue(classes.contains("checked"));
		} else {
			Assert.assertFalse(classes.contains("checked"));
		}

	}

	public void assertSourceDontExist(String name) {
		init();
		WebElement currentSource = getRightSourceServer(name);
		Assert.assertNull(currentSource);
	}

	private WebElement getRightSourceServer(String name) {
		WebElement found = null;
		for (WebElement element : sourceServers) {
			String nameValue = element.findElement(By.className("nameTextBox")).getAttribute("value");
			if (nameValue.equalsIgnoreCase(name)) {
				found = element;
				break;
			}
		}
		return found;
	}

	public void clickOnAddSourceServer() {
		webDriver.findElement(By.className("addSourceLDAPServer")).click();
		fluentWait(LITTLE);
	}

	public void addLdapSourceServer(String name, String bindDn, String maxCon, String pKey, String lPKey,
			String servers, String baseDn, String useSSl) {
		init();
		WebElement currentSource = sourceServers.get(sourceServers.size() - 1);
		currentSource.findElement(By.className("nameTextBox")).sendKeys(name);
		currentSource.findElement(By.className("bindDnTextBox")).sendKeys(bindDn);
		currentSource.findElement(By.className("maxConnectionTextBox")).sendKeys(maxCon);
		currentSource.findElement(By.className("primaryKeyTextBox")).sendKeys(pKey);
		currentSource.findElement(By.className("localPrimaryKeyTextBox")).sendKeys(lPKey);

		currentSource.findElements(By.className("addItemButton")).get(0).click();
		fluentWait(ONE_SEC);
		List<WebElement> properties = currentSource.findElements(By.className("NewPropertyBox"));
		WebElement current = properties.get(properties.size() - 1);
		current.sendKeys(servers);

		currentSource.findElements(By.className("addItemButton")).get(1).click();
		fluentWait(ONE_SEC);
		properties = currentSource.findElements(By.className("NewPropertyBox"));
		current = properties.get(properties.size() - 1);
		current.sendKeys(baseDn);

		WebElement useSslBox = currentSource.findElement(By.className("useSSLSelectBox"));
		WebElement parent = useSslBox.findElement(By.xpath(".."));
		if (useSSl.equalsIgnoreCase("true") && !parent.getAttribute("class").contains("checked")) {
			parent.click();
		}
		if (useSSl.equalsIgnoreCase("false") && parent.getAttribute("class").contains("checked")) {
			parent.click();
		}
		save();
		fluentWait(ONE_SEC);
	}

	public void save() {
		WebElement footer = webDriver.findElement(By.id("updateButtons"));
		footer.findElements(By.tagName("input")).get(0).click();
		fluentWait(ONE_SEC);
	}

	public void deleteSourceServer(String name) {
		init();
		scrollDown();
		fluentWait(LITTLE);
		WebElement currentSource = getRightSourceServer(name);
		currentSource.findElement(By.className("deleteSourceServerButton")).click();
		scrollUp();
		save();
		fluentWait(ONE_SEC);
	}
}
