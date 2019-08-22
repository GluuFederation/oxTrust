package org.oxtrust.qa.pages.configuration.organization;

import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.oxtrust.qa.pages.AbstractPage;

/**
 * Created by Nat on 2018-07-16.
 */

public class SystemConfigurationPage extends AbstractPage {

	public void setSelfServicePasswordReset(String resetState) {
		if (resetState.equalsIgnoreCase("true")) {
			enableCheckBox("passwordResetSelectBox");
		} else {
			disableCheckBox("passwordResetSelectBox");
		}
	}

	public void setSaml(String resetState) {
		if (resetState.equalsIgnoreCase("true")) {
			enableCheckBox("samlSelectBox");
		} else {
			disableCheckBox("samlSelectBox");
		}
	}

	public void setRaduis(String resetState) {
		if (resetState.equalsIgnoreCase("true")) {
			enableCheckBox("raduisSelectBox");
		} else {
			disableCheckBox("raduisSelectBox");
		}
	}

	public void setSCIMSupport(String scimState) {
		if (scimState.equalsIgnoreCase("true")) {
			enableCheckBox("scimEnableStateSelectBox");
		} else {
			disableCheckBox("scimEnableStateSelectBox");
		}
	}

	public void setPassportSupport(String passportState) {
		if (passportState.equalsIgnoreCase("true")) {
			enableCheckBox("passportEnableStateSelectBox");
		} else {
			disableCheckBox("passportEnableStateSelectBox");
		}
	}

	public void setDNSServer(String dnsServerValue) {
		WebElement setDNSServerField = webDriver.findElement(By.className("dnsServerTextBox"));
		setDNSServerField.clear();
		setDNSServerField.sendKeys(dnsServerValue);
	}

	public void setMaximumLogSize(String maximumLogSizeValue) {
		WebElement logSizeField = webDriver.findElement(By.className("maxLogSizeTextBox"));
		logSizeField.clear();
		logSizeField.sendKeys(maximumLogSizeValue);

	}

	public void setUserCanEditOwnProfile(String editOwnProfile) {
		if (editOwnProfile.equalsIgnoreCase("true")) {
			enableCheckBox("profileManagmentSelectBox");
		} else {
			disableCheckBox("profileManagmentSelectBox");
		}

	}

	public void setContactEmail(String setContactEmail) {
		WebElement contactEmailField = webDriver.findElement(By.className("contactEmailTextBox"));
		contactEmailField.clear();
		contactEmailField.sendKeys(setContactEmail);

	}

	public void clickCancelButton() {
		WebElement cancelButton = webDriver.findElement(By.id("organizationForm:updateButtons"));
		cancelButton.findElements(By.tagName("input")).get(1).click();
	}

	public void clickUpdateButton() {
		WebElement updateButton = webDriver.findElement(By.id("organizationForm:updateButtons"));
		updateButton.findElements(By.tagName("input")).get(0).click();
	}

	public void assertPasswordResetStatus(String enabled) {
		if (enabled.equalsIgnoreCase("true")) {
			assertIsEnable("passwordResetSelectBox");
		} else {
			assertIsDisable("passwordResetSelectBox");
		}
	}

	public void assertSCIMSupportStatus(String disabled) {
		if (disabled.equalsIgnoreCase("true")) {
			assertIsEnable("scimEnableStateSelectBox");
		} else {
			assertIsDisable("scimEnableStateSelectBox");
		}
	}

	public void assertPassportSupportStatus(String disabled) {
		if (disabled.equalsIgnoreCase("true")) {
			assertIsEnable("passportEnableStateSelectBox");
		} else {
			assertIsDisable("passportEnableStateSelectBox");
		}
	}

	public void assertUserCanEditOwnProfileValue(String editProfileValue) {
		if (editProfileValue.equalsIgnoreCase("true")) {
			assertIsEnable("profileManagmentSelectBox");
		} else {
			assertIsDisable("profileManagmentSelectBox");
		}
	}

	public void assertDNSServerValue(String dnsServerValue) {
		WebElement dnsServerField = webDriver.findElement(By.className("dnsServerTextBox"));
		assertThat(dnsServerField.getAttribute("value")).isEqualTo(dnsServerValue);

	}

	public void assertNotDNSServerValue(String notDNSServerValue) {
		WebElement notDNSServerField = webDriver.findElement(By.className("dnsServerTextBox"));
		assertThat(notDNSServerField.getAttribute("value")).isNotEqualTo(notDNSServerValue);
	}

	public void assertMaximumLogSizeValue(String logSizeValue) {
		WebElement maximumLogSizeField = webDriver.findElement(By.className("maxLogSizeTextBox"));
		assertThat(maximumLogSizeField.getAttribute("value")).isEqualTo(logSizeValue);

	}

	public void assertNotMaximumLogSizeValue(String notLogSizeValue) {
		WebElement notMaximumLogSizeField = webDriver.findElement(By.className("maxLogSizeTextBox"));
		assertThat(notMaximumLogSizeField.getAttribute("value")).isNotEqualTo(notLogSizeValue);

	}

	public void assertContactEmailValue(String contactEmailValue) {
		WebElement contactEmailSet = webDriver.findElement(By.className("contactEmailTextBox"));
		assertThat(contactEmailSet.getAttribute("value")).isEqualTo(contactEmailValue);

	}

	public void assertNotContactEmailValue(String notContactEmailValue) {
		WebElement notContactEmailSet = webDriver.findElement(By.className("contactEmailTextBox"));
		assertThat(notContactEmailSet.getAttribute("value")).isNotEqualTo(notContactEmailValue);

	}
}
