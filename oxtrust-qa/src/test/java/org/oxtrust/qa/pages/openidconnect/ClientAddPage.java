package org.oxtrust.qa.pages.openidconnect;

import java.util.List;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;
import org.oxtrust.qa.pages.AbstractPage;

public class ClientAddPage extends AbstractPage {

	private static final String BOX_HEADER = "nav-tabs-custom";

	public void setClientName(String name) {
		WebElement main = webDriver.findElement(By.className(BOX_HEADER));
		WebElement element = main.findElement(By.className("clientNameTextBox"));
		element.clear();
		element.sendKeys(name);
	}

	public void setDescription(String des) {
		WebElement main = webDriver.findElement(By.className(BOX_HEADER));
		WebElement element = main.findElement(By.className("clientDescriptionTextArea"));
		element.clear();
		element.sendKeys(des);
	}

	public void setSecret(String secret) {
		WebElement main = webDriver.findElement(By.className(BOX_HEADER));
		WebElement element = main.findElement(By.className("clientSecretTextBox"));
		;
		element.clear();
		element.sendKeys(secret);
	}

	public void setType(String type) {
		WebElement main = webDriver.findElement(By.className(BOX_HEADER));
		WebElement selectBox = main.findElement(By.className("applicationTypeSelectBox"));
		Select select = new Select(selectBox);
		select.selectByVisibleText(type);
	}

	public void setPreAutho(String value) {
		if (value.equalsIgnoreCase("true")) {
			enableCheckBox("oxAuthTrustedClientSelectBox");
		} else {
			disableCheckBox("oxAuthTrustedClientSelectBox");
		}

	}

	public void setPersistAutho(String value) {
		if (value.equalsIgnoreCase("true")) {
			enableCheckBox("persistClientAuthorizationSelectBox");
		} else {
			disableCheckBox("persistClientAuthorizationSelectBox");
		}
	}

	public void setSubjectType(String value) {
		WebElement main = webDriver.findElement(By.className(BOX_HEADER));
		WebElement selectBox = main.findElement(By.className("subjectTypeSelectBox"));
		Select select = new Select(selectBox);
		select.selectByVisibleText(value);
	}

	public void setAuthendMethod(String value) {
		WebElement main = webDriver.findElement(By.className(BOX_HEADER));
		WebElement selectBox = main.findElement(By.className("tokenEndpointAuthMethodSelectBox"));
		Select select = new Select(selectBox);
		select.selectByVisibleText(value);
	}

	public void addScope(String scope) {
		scrollDown();
		fluentWait(LITTLE);
		waitElementByClass(BOX_HEADER);
		WebElement addScopeButton = webDriver.findElement(By.className("AddScopeButton"));
		addScopeButton.click();
		fluentWait(ONE_SEC);
		searchForResponseType("scope:selectEntityModalPanel_content", scope);
	}

	private void searchForResponseType(String id, String value) {
		fluentWait(2);
		WebElement pane = waitElementByID(id);
		WebElement firstTable = pane.findElements(By.tagName("table")).get(0);
		WebElement table = firstTable.findElements(By.tagName("table")).get(0);
		WebElement body = table.findElement(By.tagName("tbody"));
		List<WebElement> rows = body.findElements(By.tagName("tr"));
		for (WebElement row : rows) {
			if (row.getText().contains(value)) {
				row.findElements(By.tagName("td")).get(0).findElement(By.tagName("input")).click();
			}
		}
		List<WebElement> items = firstTable.findElements(By.className("btn-primary"));
		items.get(0).click();
	}

	public void responseType(String type) {
		scrollDown();
		fluentWait(ONE_SEC);
		waitElementByClass(BOX_HEADER);
		WebElement addScopeButton = webDriver.findElement(By.className("AddResponseTypeButton"));
		addScopeButton.click();
		searchForResponseType("responseType:selectEntityModalPanel_content", type);
	}

	public void grantType(String grantType) {
		scrollDown();
		fluentWait(ONE_SEC);
		waitElementByClass(BOX_HEADER);
		WebElement addScopeButton = webDriver.findElement(By.className("AddGrantTypeButton"));
		addScopeButton.click();
		searchForResponseType("grantType:selectEntityModalPanel_content", grantType);
	}

	public void loginRedirect(String url) {
		scrollDown();
		fluentWait(ONE_SEC);
		waitElementByClass(BOX_HEADER);
		WebElement addScopeButton = webDriver.findElement(By.className("AddRedirectLoginUriButton"));
		addScopeButton.click();
		WebElement pane = waitElementByID("loginRedirect:inputText_container");
		WebElement main = pane.findElement(By.id("loginRedirect:inputText_content"));
		List<WebElement> inputs = main.findElements(By.cssSelector("input"));
		for (WebElement input : inputs) {
			if (input.getAttribute("type").equals("text")) {
				input.clear();
				input.sendKeys(url);
			}
		}

		WebElement footer = main.findElement(By.className("box-footer"));
		footer.findElements(By.tagName("input")).get(0).click();
	}

	public void save() {
		scrollDown();
		fluentWait(ONE_SEC);
		WebElement footer = waitElementByID("updateButtons");
		footer.findElements(By.tagName("input")).get(0).click();
		fluentWait(SMALL);
	}

	public void selectTab(String tabName) {
		scrollUp();
		scrollUp();
		WebElement main = webDriver.findElement(By.className(BOX_HEADER));
		WebElement tab = main.findElement(By.className(tabName));
		Actions action = new Actions(webDriver);
		action.click(tab);
		action.build().perform();
	}

	public void changePassword(String pwd) {
		webDriver.findElement(By.className("changeClientSecretButton")).click();
		WebElement newField = webDriver.findElement(By.id("clientPassword:changePasswordForm:pass"));
		newField.sendKeys(pwd);
		WebElement confirm = webDriver.findElement(By.id("clientPassword:changePasswordForm:conf"));
		confirm.sendKeys(pwd);
		WebElement main = webDriver.findElement(By.id("clientPassword:changePasswordModalPanel_content"));
		Assert.assertNotNull(main);
		WebElement button = main.findElement(By.className("savePasswordButton"));
		button.click();
		button.click();
		fluentWait(ONE_SEC);
	}

}
