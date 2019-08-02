package org.oxtrust.qa.steps;

import org.oxtrust.qa.pages.configuration.cr.CacheRefreshPage;
import org.oxtrust.qa.pages.login.HomePage;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.en.And;

public class CacheRefreshSteps extends BaseSteps {

	private HomePage homePage = new HomePage();
	private CacheRefreshPage cacheRefreshPage = new CacheRefreshPage();

	@And("^I go to cache refresh page$")
	public void goToCacheRefreshPage() {
		homePage.goToCacheRefreshMenuPage();
	}

	@And("^I select the tab named '(.+)'$")
	public void selectTab(String name) {
		cacheRefreshPage.selectTab(name);
	}

	@And("^I delete all source to destination attribute$")
	public void deleteSourceDestinationAttributes() {
		cacheRefreshPage.deleteAllSourceDestinationAttributes();
	}

	@And("^I add a source attribute named '(.+)' to destination attribute named '(.+)'$")
	public void addSourceDestinationAttribute(String source, String destination) {
		cacheRefreshPage.addSourcedestinationAttribute(source, destination);
	}

	@And("^I set polling interval to '(.+)' minutes$")
	public void setPollingInterval(String intervall) {
		cacheRefreshPage.setPollingInterval(intervall);
	}

	@And("^I enable cache refresh$")
	public void enableCacheRefresh() {
		cacheRefreshPage.enableCacheRefresh();
	}

	@And("^I delete all key attributes$")
	public void deleteAllKeyAttribute() {
		cacheRefreshPage.deleteAllKeyAttributes();
	}

	@And("^I delete all objects$")
	public void deleteAllObject() {
		cacheRefreshPage.deleteAllObject();
	}

	@And("^I delete source all attributes$")
	public void deleteAllSourceAttrib() {
		cacheRefreshPage.deleteAllSourceAttrib();
	}

	@And("^I add a key attribute named '(.+)'$")
	public void addKeyAttrib(String attrib) {
		cacheRefreshPage.addKeyAttrib(attrib);
	}

	@And("^I add an object class named '(.+)'$")
	public void addObjectClass(String name) {
		cacheRefreshPage.addObjectClass(name);
	}

	@And("^I add source attribute named '(.+)'$")
	public void addAttrib(String name) {
		cacheRefreshPage.addAttrib(name);
	}

	@And("^I add a source server named '(.+)' with bindDn '(.+)' with maxCon '(.+)' with servers '(.+)' with baseDns '(.+)' using ssl '(.+)'$")
	public void addSourceServer(String name, String bindDn, String maxCon, String servers, String baseDns,
			String useSSl) {
		cacheRefreshPage.addSourceServer(name, bindDn, maxCon, servers, baseDns, useSSl);
	}

	@And("^I change the bind dn password to '(.+)'$")
	public void changeBinDnPassword(String password) {
		cacheRefreshPage.changeBindDnPassword(password);
	}

	@And("^I save the cache refresh configuration$")
	public void saveCRConfig() {
		cacheRefreshPage.save();
	}

	@After
	public void clear(Scenario scenario) {
		homePage.takeScreenShot(scenario);
		homePage.clear();
	}
}
