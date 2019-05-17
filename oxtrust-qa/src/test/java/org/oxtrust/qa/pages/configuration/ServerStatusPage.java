package org.oxtrust.qa.pages.configuration;


import java.util.List;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.oxtrust.qa.pages.AbstractPage;

public class ServerStatusPage extends AbstractPage {

	private List<WebElement> rows;

	private void init() {
		WebElement table1 = webDriver.findElement(By.id("statusTable1")).findElement(By.tagName("tbody"));
		WebElement table2 = webDriver.findElement(By.id("statusTable2")).findElement(By.tagName("tbody"));
		rows = table1.findElements(By.tagName("tr"));
		rows.addAll(table2.findElements(By.tagName("tr")));
	}

	public void assertSizeIs(String size) {
		init();
		Assert.assertTrue(rows.size() == Integer.parseInt(size));
	}

	public void checkHostName() {
		WebElement hostNameElement = webDriver.findElement(By.className("hostnameValueBox"));
		Assert.assertTrue(hostNameElement.getText() != null);
	}

	public void checkIpAddress() {
		WebElement ipElement = webDriver.findElement(By.className("ipAddressValueBox"));
		Assert.assertTrue(ipElement.getText() != null);
	}

	public void checkSystemUptime() {
		WebElement systemUptimeElement = webDriver.findElement(By.className("systemUptimeValueBox"));
		Assert.assertTrue(systemUptimeElement.getText() != null);
	}

	public void checkLastUpdate() {
		WebElement lastUpdateElement = webDriver.findElement(By.className("lastUpdateValueBox"));
		Assert.assertTrue(lastUpdateElement.getText() != null);
	}

	public void checkPolling() {
		WebElement pollingElement = webDriver.findElement(By.className("pollingIntervalValueBox"));
		Assert.assertTrue(pollingElement.getText() != null);
	}

	public void checkPersonCount() {
		WebElement personElement = webDriver.findElement(By.className("personCountValueBox"));
		Assert.assertTrue(personElement.getText() != null);
	}

	public void checkGroupCount() {
		WebElement groupElement = webDriver.findElement(By.className("groupCountValueBox"));
		Assert.assertTrue(groupElement.getText() != null);
	}

	public void checkFreeMemory() {
		WebElement groupElement = webDriver.findElement(By.className("freeMemoryValueBox"));
		Assert.assertTrue(groupElement.getText() != null);
	}

	public void checkFreeDisk() {
		WebElement groupElement = webDriver.findElement(By.className("freeDiskSpaceValueBox"));
		Assert.assertTrue(groupElement.getText() != null);
	}

}
