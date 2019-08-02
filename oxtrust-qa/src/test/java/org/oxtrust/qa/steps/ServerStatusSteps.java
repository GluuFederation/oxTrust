package org.oxtrust.qa.steps;

import org.oxtrust.qa.pages.configuration.ServerStatusPage;
import org.oxtrust.qa.pages.login.HomePage;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;

public class ServerStatusSteps extends BaseSteps {
	private HomePage homePage=new HomePage();
	private ServerStatusPage serverStatusPage=new ServerStatusPage();

	@And("^I go to server status page$")
	public void goToServerStatusPage() {
		homePage.goToServerStatusMenuPage();
	}

	@Then("^I should see '(.+)' server parameters$")
	public void checkParametersSize(String size) {
		serverStatusPage.assertSizeIs(size);
	}

	@Then("^I should see the hostname is present and not empty$")
	public void checkServerHostName() {
		serverStatusPage.checkHostName();
	}

	@Then("^I should see the ip address is present and not empty$")
	public void checkServerIp() {
		serverStatusPage.checkIpAddress();
	}

	@Then("^I should see the system uptime is present and not empty$")
	public void checkServerSysUptime() {
		serverStatusPage.checkSystemUptime();
	}

	@Then("^I should see the last update is present and not empty$")
	public void checkServerLsatUpdate() {
		serverStatusPage.checkLastUpdate();
	}

	@Then("^I should see the polling interval is present and not empty$")
	public void checkServerPollingInterval() {
		serverStatusPage.checkPolling();
	}

	@Then("^I should see the person count is present and not empty$")
	public void checkServerPersonCount() {
		serverStatusPage.checkPersonCount();
	}

	@Then("^I should see the group count is present and not empty$")
	public void checkServerGroupCount() {
		serverStatusPage.checkGroupCount();
	}

	@Then("^I should see the free memory is present and not empty$")
	public void checkServerFreeMemory() {
		serverStatusPage.checkFreeMemory();
	}

	@Then("^I should see the free disk is present and not empty$")
	public void checkServerFreeDisk() {
		serverStatusPage.checkFreeDisk();
	}

	@After
	public void clear(Scenario scenario) {
		homePage.takeScreenShot(scenario);
		homePage.clear();
	}

}
