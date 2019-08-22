package org.oxtrust.qa.pages.configuration.json;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.oxtrust.qa.pages.AbstractPage;

public class CacheProviderJsonPage extends AbstractPage {

	public void checkProviderType(String type1, String type2) {
		WebElement element = webDriver.findElement(By.name("root[cacheProviderType]"));
		Assert.assertNotNull(element);
		Select select = new Select(element);
		boolean condition = select.getFirstSelectedOption().getText().equalsIgnoreCase(type1)
				|| select.getFirstSelectedOption().getText().equalsIgnoreCase(type2);
		Assert.assertTrue(condition);

	}

	public void verifyMemCache(String type, String servers, String maxOQL, String bufferSize, String expiration) {
		WebElement element = webDriver
				.findElement(By.name("root[memcachedConfiguration][MemcachedConnectionFactoryType]"));
		Assert.assertNotNull(element);
		Select select = new Select(element);
		Assert.assertTrue(select.getFirstSelectedOption().getText().equalsIgnoreCase(type));

		WebElement elementServers = webDriver.findElement(By.name("root[memcachedConfiguration][servers]"));
		Assert.assertNotNull(elementServers);
		WebElement elementOQL = webDriver.findElement(By.name("root[memcachedConfiguration][maxOperationQueueLength]"));
		Assert.assertNotNull(elementOQL);
		WebElement elementBsize = webDriver.findElement(By.name("root[memcachedConfiguration][bufferSize]"));
		Assert.assertNotNull(elementBsize);
		WebElement elementExp = webDriver.findElement(By.name("root[memcachedConfiguration][defaultPutExpiration]"));
		Assert.assertNotNull(elementExp);

	}

	public void verifyRedisConfig(String type, String servers, String expiration) {
		WebElement element = webDriver.findElement(By.name("root[redisConfiguration][redisProviderType]"));
		Assert.assertNotNull(element);
		Select select = new Select(element);
		Assert.assertTrue(select.getFirstSelectedOption().getText().equalsIgnoreCase(type));

		WebElement elementServers = webDriver.findElement(By.name("root[redisConfiguration][servers]"));
		Assert.assertNotNull(elementServers);

		WebElement elementExp = webDriver.findElement(By.name("root[redisConfiguration][defaultPutExpiration]"));
		Assert.assertNotNull(elementExp);
	}

}
