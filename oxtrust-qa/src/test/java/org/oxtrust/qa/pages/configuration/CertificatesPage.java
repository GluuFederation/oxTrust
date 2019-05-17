package org.oxtrust.qa.pages.configuration;


import java.util.List;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.oxtrust.qa.pages.AbstractPage;

public class CertificatesPage extends AbstractPage {

	private List<WebElement> certs;

	private void init() {
		WebElement table = webDriver.findElement(By.id("internalCertificatesFormId:asimbaCertificatesListId"))
				.findElement(By.tagName("tbody"));
		certs = table.findElements(By.tagName("tr"));
	}

	public void assertThereAreCerts(String size) {
		init();
		Assert.assertTrue(certs.size() >= Integer.valueOf(size));
	}

	public void assertCertExist(String certName) {
		init();
		boolean found = false;
		for (WebElement cert : certs) {
			if (cert.findElements(By.tagName("td")).get(0).getText().equalsIgnoreCase(certName)) {
				found = true;
				break;
			}
		}
		Assert.assertTrue(found);
	}

}
