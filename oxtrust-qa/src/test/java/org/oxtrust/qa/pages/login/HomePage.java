package org.oxtrust.qa.pages.login;

import java.util.List;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.oxtrust.qa.pages.AbstractPage;

public class HomePage extends AbstractPage {

	public void goToUsersMenu() {
		WebElement userMenu = webDriver.findElement(By.xpath("//*[@id='menuUsers']"));
		userMenu.click();
	}

	public void goToConfigurationMenu() {
		WebElement configurationMenu = webDriver.findElement(By.xpath("//*[@id='menuConfiguration']"));
		configurationMenu.click();
	}

	public void goSamlMenu() {
		WebElement samlMenu = webDriver.findElement(By.xpath("//*[@id='menuSAML']"));
		samlMenu.click();
	}

	public void goToPassportMenu() {
		WebElement passportMenu = webDriver.findElement(By.xpath("//*[@id='menuPassport']"));
		passportMenu.click();
	}

	public void goToPassportProviderListPage() {
		WebElement passportMenu = webDriver.findElement(By.xpath("//*[@id='menuPassport']"));
		passportMenu.click();
		WebElement subMenu = waitElementByID("menuPassportProviders");
		subMenu.click();
		fluentWait(3);
	}

	public void goToPassportConfigPage() {
		fluentWait(5);
		WebElement passportMenu = webDriver.findElement(By.xpath("//*[@id='menuPassport']"));
		passportMenu.click();
		fluentWait(5);
		WebElement subMenu = waitElementByID("menuPassportBasicConfig");
		subMenu.click();
		fluentWait(3);
	}

	public void goToPassportIdpPage() {
		WebElement passportMenu = webDriver.findElement(By.xpath("//*[@id='menuPassport']"));
		passportMenu.click();

		WebElement subMenu = waitElementByID("menuPassportIdpInitiated");
		subMenu.click();
		fluentWait(3);
	}

	public void goSamlTrListPage() {
		fluentWait(1);
		WebElement samlMenu = webDriver.findElement(By.xpath("//*[@id='menuSAML']"));
		samlMenu.click();
		fluentWait(1);
		WebElement subMenu = waitElementByID("subMenuLinkSAML1");
		subMenu.click();
		fluentWait(3);
	}

	public void goSamlTrAddPage() {
		WebElement samlMenu = webDriver.findElement(By.xpath("//*[@id='menuSAML']"));
		samlMenu.click();

		WebElement subMenu = waitElementByID("subMenuLinkSAML2");
		subMenu.click();
		fluentWait(3);
	}

	public void goNameIdConfigurePage() {
		WebElement samlMenu = webDriver.findElement(By.xpath("//*[@id='menuSAML']"));
		samlMenu.click();
		fluentWait(1);
		WebElement subMenu = waitElementByID("subMenuLinkSAML3");
		subMenu.click();
		fluentWait(3);
	}

	public void goToProfileMenu() {
		WebElement profileMenu = webDriver.findElement(By.xpath("//*[@id='menuPersonal']"));
		profileMenu.click();
		fluentWait(3);
	}

	public void goToProfileViewMenu() {
		WebElement userMenu = webDriver.findElement(By.xpath("//*[@id='menuPersonal']"));
		userMenu.click();
		WebElement subMenu = waitElementByID("subMenuLinkPersonal1");
		subMenu.click();
		fluentWait(3);
	}

	public void goToUmaMenu() {
		WebElement umaMenu = webDriver.findElement(By.xpath("//*[@id='menuUMA']"));
		umaMenu.click();
		WebElement subMenu = waitElementByID("subMenuLinkUMA1");
		subMenu.click();
		fluentWait(3);
	}

	public void goToUmaScopeManagePage() {
		fluentWait(3);
		WebElement userMenu = waitElement("//*[@id='menuUMA']");
		userMenu.click();
		WebElement subMenu = waitElementByID("subMenuLinkUMA1");
		subMenu.click();
		fluentWait(3);
	}

	public void goToUmaResourceManagePage() {
		WebElement umaMenu = webDriver.findElement(By.xpath("//*[@id='menuUMA']"));
		umaMenu.click();
		WebElement subMenu = waitElementByID("subMenuLinkUMA2");
		subMenu.click();
		fluentWait(3);
	}

	public void goToOpenIDMenu() {
		WebElement openIdMenu = webDriver.findElement(By.xpath("//*[@id='menuOpenID']"));
		openIdMenu.click();
	}

	public void goToGroupAddPage() {
		goToGroupsManagePage();
		fluentWait(3);
		WebElement addButton = webDriver.findElement(By.className("addGroup"));
		addButton.click();
		fluentWait(3);
	}

	public void goToGroupsManagePage() {
		WebElement groupMenu = webDriver.findElement(By.xpath("//*[@id='menuUsers']"));
		groupMenu.click();
		WebElement subMenu = waitElement("//*[@id='subMenuLinkUsers1']");
		subMenu.click();
		fluentWait(3);
	}

	public void goToUsersManagePage() {
		WebElement userMenu = webDriver.findElement(By.xpath("//*[@id='menuUsers']"));
		userMenu.click();
		fluentWait(1);
		WebElement subMenu = waitElement("//*[@id='subMenuLinkUsers2']");
		subMenu.click();
		fluentWait(3);
	}

	public void goToUsersAddPage() {
		WebElement userMenu = webDriver.findElement(By.xpath("//*[@id='menuUsers']"));
		userMenu.click();
		WebElement subMenu = waitElement("//*[@id='subMenuLinkUsers3']");
		subMenu.click();
		fluentWait(3);
	}

	public void goToUsersImportPage() {
		WebElement userMenu = webDriver.findElement(By.xpath("//*[@id='menuUsers']"));
		userMenu.click();
		WebElement subMenu = waitElement("//*[@id='subMenuLinkUsers4']");
		subMenu.click();
		fluentWait(3);
	}

	public void goToOpenIDScopePage() {
		fluentWait(1);
		WebElement openIdMenu = webDriver.findElement(By.xpath("//*[@id='menuOpenID']"));
		openIdMenu.click();
		fluentWait(2);
		WebElement subMenu = waitElement("//*[@id='subMenuLinkOpenID1']");
		subMenu.click();
		fluentWait(3);
	}

	public void goToOpenIDClientsListPage() {
		WebElement openIdMenu = webDriver.findElement(By.xpath("//*[@id='menuOpenID']"));
		openIdMenu.click();
		fluentWait(1);
		WebElement subMenu = waitElement("//*[@id='subMenuLinkOpenID2']");
		subMenu.click();
		fluentWait(3);
	}

	public void goToSectorListPage() {
		fluentWait(1);
		goToUsersMenu();
		WebElement openIdMenu = webDriver.findElement(By.xpath("//*[@id='menuOpenID']"));
		openIdMenu.click();
		fluentWait(2);
		WebElement subMenu = waitElement("//*[@id='subMenuLinkOpenID3']");
		subMenu.click();
		fluentWait(3);
	}

	public void goToConfigurationMenuPage() {
		WebElement openIdMenu = webDriver.findElement(By.xpath("//*[@id='menuConfiguration']"));
		openIdMenu.click();
		fluentWait(3);
	}

	public void goToOrganisationConfigurationMenuPage() {
		WebElement openIdMenu = webDriver.findElement(By.xpath("//*[@id='menuConfiguration']"));
		openIdMenu.click();
		WebElement subMenu = waitElement("//*[@id='subMenuLinkConfiguration1']");
		subMenu.click();
		fluentWait(3);
	}

	public void goToJsonConfigurationMenuPage() {
		WebElement openIdMenu = webDriver.findElement(By.xpath("//*[@id='menuConfiguration']"));
		openIdMenu.click();
		WebElement subMenu = waitElement("//*[@id='subMenuLinkConfiguration2']");
		subMenu.click();
		fluentWait(3);
	}

	public void goToManageAutheticationMenuPage() {
		WebElement openIdMenu = webDriver.findElement(By.xpath("//*[@id='menuConfiguration']"));
		openIdMenu.click();
		fluentWait(3);
		WebElement subMenu = waitElement("//*[@id='subMenuLinkConfiguration3']");
		subMenu.click();
		fluentWait(3);
	}

	public void goToManageCustomScriptsMenuPage() {
		WebElement openIdMenu = webDriver.findElement(By.xpath("//*[@id='menuConfiguration']"));
		openIdMenu.click();
		WebElement subMenu = waitElement("//*[@id='subMenuLinkConfiguration4']");
		subMenu.click();
		fluentWait(3);
	}

	public void goToManageRegistrationMenuPage() {
		WebElement openIdMenu = webDriver.findElement(By.xpath("//*[@id='menuConfiguration']"));
		openIdMenu.click();
		WebElement subMenu = waitElement("//*[@id='subMenuLinkConfiguration5']");
		subMenu.click();
		fluentWait(3);
	}

	public void goToAttributesMenuPage() {
		WebElement openIdMenu = webDriver.findElement(By.xpath("//*[@id='menuConfiguration']"));
		openIdMenu.click();
		WebElement subMenu = waitElement("//*[@id='subMenuLinkConfiguration6']");
		subMenu.click();
		fluentWait(3);
	}

	public void goToImportExportAttributesMenuPage() {
		WebElement openIdMenu = webDriver.findElement(By.xpath("//*[@id='menuConfiguration']"));
		openIdMenu.click();
		WebElement subMenu = waitElement("//*[@id='subMenuLinkConfiguration12']");
		subMenu.click();
		fluentWait(3);
	}

	public void goToCacheRefreshMenuPage() {
		WebElement openIdMenu = webDriver.findElement(By.xpath("//*[@id='menuConfiguration']"));
		openIdMenu.click();
		WebElement subMenu = waitElement("//*[@id='subMenuLinkConfiguration8']");
		subMenu.click();
	}

	public void goToLogViewerCongifigurationMenuPage() {
		WebElement openIdMenu = webDriver.findElement(By.xpath("//*[@id='menuConfiguration']"));
		openIdMenu.click();
		WebElement subMenu = waitElement("//*[@id='subMenuLinkConfiguration9']");
		subMenu.click();
		fluentWait(3);
	}

	public void goToLogFileViewMenuPage() {
		WebElement openIdMenu = webDriver.findElement(By.xpath("//*[@id='menuConfiguration']"));
		openIdMenu.click();
		WebElement subMenu = waitElement("//*[@id='subMenuLinkConfiguration10']");
		subMenu.click();
		fluentWait(3);
	}

	public void goToServerStatusMenuPage() {
		WebElement openIdMenu = webDriver.findElement(By.xpath("//*[@id='menuConfiguration']"));
		openIdMenu.click();
		WebElement subMenu = waitElement("//*[@id='subMenuLinkConfiguration11']");
		subMenu.click();
		fluentWait(3);
	}

	public void goTocertificatesMenuPage() {
		WebElement openIdMenu = webDriver.findElement(By.xpath("//*[@id='menuConfiguration']"));
		openIdMenu.click();
		WebElement subMenu = waitElement("//*[@id='subMenuLinkCertificates1']");
		subMenu.click();
		fluentWait(3);
	}

	public void checkDashboard(String value) {
		WebElement main = webDriver.findElement(By.className("box-footer"));
		scrollDownUntil(main);
		List<WebElement> elements = main.findElements(By.className("description-header"));
		Assert.assertTrue(elements.size() == 4);
		if (value.equalsIgnoreCase("is not")) {
			boolean isCorrect = true;
			for (WebElement element : elements) {
				if (!element.getText().equalsIgnoreCase("0")) {
					isCorrect = false;
					break;
				}
			}
			Assert.assertTrue(isCorrect);
		} else if (value.equalsIgnoreCase("is")) {
			int count = 0;
			for (WebElement element : elements) {
				if (element.getText().equalsIgnoreCase("0")) {
					count = count + 1;
				}
			}

			if (count == 4) {
				Assert.assertTrue(false);
			} else {
				Assert.assertTrue(true);
			}
		} else {
			throw new IllegalArgumentException("Unsupported option");
		}

	}
}
