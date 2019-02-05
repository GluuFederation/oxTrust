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

	public void goSamlTrListPage() {
		WebElement samlMenu = webDriver.findElement(By.xpath("//*[@id='menuSAML']"));
		samlMenu.click();

		WebElement subMenu = waitElementByID("subMenuLinkSAML1");
		subMenu.click();
	}

	public void goSamlTrAddPage() {
		WebElement samlMenu = webDriver.findElement(By.xpath("//*[@id='menuSAML']"));
		samlMenu.click();

		WebElement subMenu = waitElementByID("subMenuLinkSAML2");
		subMenu.click();
	}

	public void goNameIdConfigurePage() {
		WebElement samlMenu = webDriver.findElement(By.xpath("//*[@id='menuSAML']"));
		samlMenu.click();

		WebElement subMenu = waitElementByID("subMenuLinkSAML3");
		subMenu.click();
	}

	public void goToProfileMenu() {
		WebElement profileMenu = webDriver.findElement(By.xpath("//*[@id='menuPersonal']"));
		profileMenu.click();
	}

	public void goToProfileViewMenu() {
		WebElement userMenu = webDriver.findElement(By.xpath("//*[@id='menuPersonal']"));
		userMenu.click();
		WebElement subMenu = waitElementByID("subMenuLinkPersonal1");
		subMenu.click();
		fluentWait(ONE_SEC);
	}

	public void goToUmaMenu() {
		WebElement umaMenu = webDriver.findElement(By.xpath("//*[@id='menuUMA']"));
		umaMenu.click();
		WebElement subMenu = waitElementByID("subMenuLinkUMA1");
		subMenu.click();
	}

	public void goToUmaScopeManagePage() {
		WebElement userMenu = waitElement("//*[@id='menuUMA']");
		userMenu.click();
		WebElement subMenu = waitElementByID("subMenuLinkUMA1");
		subMenu.click();
	}

	public void goToUmaResourceManagePage() {
		WebElement umaMenu = webDriver.findElement(By.xpath("//*[@id='menuUMA']"));
		umaMenu.click();
		WebElement subMenu = waitElementByID("subMenuLinkUMA2");
		subMenu.click();
	}

	public void goToOpenIDMenu() {
		WebElement openIdMenu = webDriver.findElement(By.xpath("//*[@id='menuOpenID']"));
		openIdMenu.click();
	}

	public void goToGroupAddPage() {
		goToGroupsManagePage();
		WebElement addButton = webDriver.findElement(By.className("addGroup"));
		addButton.click();
	}

	public void goToGroupsManagePage() {
		WebElement groupMenu = webDriver.findElement(By.xpath("//*[@id='menuUsers']"));
		groupMenu.click();
		WebElement subMenu = waitElement("//*[@id='subMenuLinkUsers1']");
		subMenu.click();
	}

	public void goToUsersManagePage() {
		WebElement userMenu = webDriver.findElement(By.xpath("//*[@id='menuUsers']"));
		userMenu.click();
		WebElement subMenu = waitElement("//*[@id='subMenuLinkUsers2']");
		subMenu.click();
	}

	public void goToUsersAddPage() {
		WebElement userMenu = webDriver.findElement(By.xpath("//*[@id='menuUsers']"));
		userMenu.click();
		WebElement subMenu = waitElement("//*[@id='subMenuLinkUsers3']");
		subMenu.click();
	}

	public void goToUsersImportPage() {
		WebElement userMenu = webDriver.findElement(By.xpath("//*[@id='menuUsers']"));
		userMenu.click();
		WebElement subMenu = waitElement("//*[@id='subMenuLinkUsers4']");
		subMenu.click();
	}

	public void goToOpenIDScopePage() {
		WebElement openIdMenu = webDriver.findElement(By.xpath("//*[@id='menuOpenID']"));
		openIdMenu.click();

		WebElement subMenu = waitElement("//*[@id='subMenuLinkOpenID1']");
		subMenu.click();
	}

	public void goToOpenIDClientsListPage() {
		WebElement openIdMenu = webDriver.findElement(By.xpath("//*[@id='menuOpenID']"));
		openIdMenu.click();
		WebElement subMenu = waitElement("//*[@id='subMenuLinkOpenID2']");
		subMenu.click();
	}

	public void goToSectorListPage() {
		WebElement openIdMenu = webDriver.findElement(By.xpath("//*[@id='menuOpenID']"));
		openIdMenu.click();
		WebElement subMenu = waitElement("//*[@id='subMenuLinkOpenID3']");
		subMenu.click();
	}

	public void goToConfigurationMenuPage() {
		WebElement openIdMenu = webDriver.findElement(By.xpath("//*[@id='menuConfiguration']"));
		openIdMenu.click();
	}

	public void goToOrganisationConfigurationMenuPage() {
		WebElement openIdMenu = webDriver.findElement(By.xpath("//*[@id='menuConfiguration']"));
		openIdMenu.click();
		WebElement subMenu = waitElement("//*[@id='subMenuLinkConfiguration1']");
		subMenu.click();
	}

	public void goToJsonConfigurationMenuPage() {
		WebElement openIdMenu = webDriver.findElement(By.xpath("//*[@id='menuConfiguration']"));
		openIdMenu.click();
		WebElement subMenu = waitElement("//*[@id='subMenuLinkConfiguration2']");
		subMenu.click();
	}

	public void goToManageAutheticationMenuPage() {
		WebElement openIdMenu = webDriver.findElement(By.xpath("//*[@id='menuConfiguration']"));
		openIdMenu.click();
		WebElement subMenu = waitElement("//*[@id='subMenuLinkConfiguration3']");
		subMenu.click();
	}

	public void goToManageCustomScriptsMenuPage() {
		WebElement openIdMenu = webDriver.findElement(By.xpath("//*[@id='menuConfiguration']"));
		openIdMenu.click();
		WebElement subMenu = waitElement("//*[@id='subMenuLinkConfiguration4']");
		subMenu.click();
	}

	public void goToManageRegistrationMenuPage() {
		WebElement openIdMenu = webDriver.findElement(By.xpath("//*[@id='menuConfiguration']"));
		openIdMenu.click();
		WebElement subMenu = waitElement("//*[@id='subMenuLinkConfiguration5']");
		subMenu.click();
	}

	public void goToAttributesMenuPage() {
		WebElement openIdMenu = webDriver.findElement(By.xpath("//*[@id='menuConfiguration']"));
		openIdMenu.click();
		WebElement subMenu = waitElement("//*[@id='subMenuLinkConfiguration6']");
		subMenu.click();
	}

	public void goToImportExportAttributesMenuPage() {
		WebElement openIdMenu = webDriver.findElement(By.xpath("//*[@id='menuConfiguration']"));
		openIdMenu.click();
		WebElement subMenu = waitElement("//*[@id='subMenuLinkConfiguration12']");
		subMenu.click();
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
	}

	public void goToLogFileViewMenuPage() {
		WebElement openIdMenu = webDriver.findElement(By.xpath("//*[@id='menuConfiguration']"));
		openIdMenu.click();
		WebElement subMenu = waitElement("//*[@id='subMenuLinkConfiguration10']");
		subMenu.click();
	}

	public void goToServerStatusMenuPage() {
		WebElement openIdMenu = webDriver.findElement(By.xpath("//*[@id='menuConfiguration']"));
		openIdMenu.click();
		WebElement subMenu = waitElement("//*[@id='subMenuLinkConfiguration11']");
		subMenu.click();
	}

	public void goTocertificatesMenuPage() {
		WebElement openIdMenu = webDriver.findElement(By.xpath("//*[@id='menuConfiguration']"));
		openIdMenu.click();
		WebElement subMenu = waitElement("//*[@id='subMenuLinkCertificates1']");
		subMenu.click();
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
