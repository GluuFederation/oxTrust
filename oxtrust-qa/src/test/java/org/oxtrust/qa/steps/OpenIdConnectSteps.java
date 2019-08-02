package org.oxtrust.qa.steps;

import org.oxtrust.qa.pages.login.HomePage;
import org.oxtrust.qa.pages.openidconnect.ClientAddPage;
import org.oxtrust.qa.pages.openidconnect.OpenIdConnectClientManagePage;
import org.oxtrust.qa.pages.openidconnect.OpenIdConnectScopeAddPage;
import org.oxtrust.qa.pages.openidconnect.OpenIdConnectScopeManagePage;
import org.oxtrust.qa.pages.openidconnect.OpenIdConnectScopeUpdatePage;
import org.oxtrust.qa.pages.openidconnect.SectorAddPage;
import org.oxtrust.qa.pages.openidconnect.SectorManagePage;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class OpenIdConnectSteps extends BaseSteps {
	private HomePage homePage = new HomePage();
	private OpenIdConnectScopeManagePage opConnectScopeManagePage = new OpenIdConnectScopeManagePage();
	private OpenIdConnectClientManagePage openIdConnectClientManagePage = new OpenIdConnectClientManagePage();
	private OpenIdConnectScopeAddPage openIdConnectScopeAddPage = new OpenIdConnectScopeAddPage();
	private OpenIdConnectScopeUpdatePage openIdConnectScopeUpdatePage = new OpenIdConnectScopeUpdatePage();
	private ClientAddPage clientAddPage = new ClientAddPage();
	private SectorAddPage sectorAddPage = new SectorAddPage();
	private SectorManagePage sectorManagePage = new SectorManagePage();
	
	@Before
	public void setup(Scenario scenario) {
		startRecorder(scenario);
	}

	@When("^I go to openid connect scopes list page$")
	public void goToOpenIdScopesListPage() {
		homePage.goToOpenIDScopePage();
	}

	@When("^I go to sectors list page$")
	public void goToSectorListPage() {
		homePage.goToSectorListPage();
	}

	@When("^I search for openid scopes with pattern '(.+)'$")
	public void searchOpenIDScopes(String pattern) {
		opConnectScopeManagePage.searchFor(pattern);
	}

	@Then("^I should see an openid scope named '(.+)'$")
	public void searchScopeInList(String scope) {
		opConnectScopeManagePage.assertScopeExist(scope);
	}

	@Then("^I should not see an openid scope named '(.+)'$")
	public void searchForScopeInList(String scope) {
		opConnectScopeManagePage.assertScopeDontExist(scope);
	}

	@When("^I go to openid connect clients list page$")
	public void goToOpenIdClientListPage() {
		homePage.goToOpenIDClientsListPage();
	}

	@When("^I search for openid clients with pattern '(.+)'$")
	public void searchOpenIDClients(String pattern) {
		openIdConnectClientManagePage.searchFor(pattern);
	}

	@Then("^I should see an openid client named '(.+)'$")
	public void searchClientInList(String client) {
		openIdConnectClientManagePage.assertClientExist(client);
	}

	@Then("^I should not see an openid client named '(.+)'$")
	public void searchClientNotInList(String client) {
		openIdConnectClientManagePage.assertClientDontExist(client);
	}

	@And("^I start the process to add new client$")
	public void clickAddClientButton() {
		openIdConnectClientManagePage.goToClientAddPage();
	}

	@And("^I start the process to add new scope$")
	public void clickAddScopeButton() {
		opConnectScopeManagePage.goToScopeAddPage();
	}

	@And("^I start the process to add new sector$")
	public void clickAddSectorButton() {
		sectorManagePage.goToSectorAddPage();
	}

	@And("^I set the display name '(.+)'$")
	public void setDisplayName(String dn) {
		openIdConnectScopeAddPage.setDisplayName(dn);
	}

	@And("^I edit the display name value to '(.+)'$")
	public void editDisplayName(String dn) {
		openIdConnectScopeAddPage.setDisplayName(dn);
	}

	@And("^I set the description '(.+)'$")
	public void setDescription(String des) {
		openIdConnectScopeAddPage.setDescription(des);
	}

	@And("^I edit the description value to '(.+)'$")
	public void editDescription(String des) {
		openIdConnectScopeAddPage.setDescription(des);
	}

	@And("^I set the scope type to '(.+)'$")
	public void setScopeType(String type) {
		openIdConnectScopeAddPage.setType(type);
	}

	@And("^I set dynamic registration to '(.+)'$")
	public void setRegistrationType(String rType) {
		openIdConnectScopeAddPage.setRegistrationType(rType);
	}

	@And("^I save the scope registration$")
	public void perfomSave() {
		openIdConnectScopeAddPage.save();
	}

	@And("^I save the scope edition$")
	public void perfomEdition() {
		openIdConnectScopeUpdatePage.edit();
	}

	@And("^I save the client edition$")
	public void perfomClientEdition() {
		openIdConnectClientManagePage.edit();
	}

	@And("^I start the process to edit the scope named '(.+)'$")
	public void editCurrentScope(String scope) {
		opConnectScopeManagePage.editScope(scope);
	}

	@And("^I start the process to edit the client named '(.+)'$")
	public void editCurrentClient(String scope) {
		openIdConnectClientManagePage.editClient(scope);
	}

	@When("^I delete that scope$")
	public void deleteScope() {
		openIdConnectScopeUpdatePage.delete();
	}

	@When("^I delete that client$")
	public void deleteClient() {
		openIdConnectClientManagePage.delete();
	}

	@And("^I set the client name to '(.+)'$")
	public void setClientName(String name) {
		clientAddPage.setClientName(name);
	}

	@And("^I edit the client name to '(.+)'$")
	public void editClientName(String name) {
		clientAddPage.setClientName(name);
	}

	@And("^I set the client description to '(.+)'$")
	public void setClientDes(String des) {
		clientAddPage.setDescription(des);
	}

	@And("^I edit the client description to '(.+)'$")
	public void editClientDes(String des) {
		clientAddPage.setDescription(des);
	}

	@And("^I set the client secret to '(.+)'$")
	public void setClientSecret(String secret) {
		clientAddPage.setSecret(secret);
	}

	@And("^I change the client password to '(.+)'$")
	public void changeClientPassword(String pwd) {
		clientAddPage.changePassword(pwd);
	}

	@And("^I edit the client secret to '(.+)'$")
	public void editClientSecret(String secret) {
		clientAddPage.setSecret(secret);
	}

	@And("^I set application type to '(.+)'$")
	public void setType(String type) {
		clientAddPage.setType(type);
	}

	@And("^I set preauthorization to '(.+)'$")
	public void setPreAuth(String value) {
		clientAddPage.setPreAutho(value);
	}

	@And("^I set persist authorization to '(.+)'$")
	public void setPersistAuth(String value) {
		clientAddPage.setPersistAutho(value);
	}

	@And("^I set subject type to '(.+)'$")
	public void setSubjectType(String value) {
		clientAddPage.setSubjectType(value);
	}

	@And("^I set authentication method to '(.+)'$")
	public void setAuthendMethod(String value) {
		clientAddPage.setAuthendMethod(value);
	}

	@And("^I add the scope named '(.+)'$")
	public void addScope(String scope) {
		clientAddPage.addScope(scope);
	}

	@And("^I add the response type named '(.+)'$")
	public void addResponseType(String type) {
		clientAddPage.responseType(type);
	}

	@And("^I add the grant type named '(.+)'$")
	public void addGrantType(String type) {
		clientAddPage.grantType(type);
	}

	@And("^I add the login redirect named '(.+)'$")
	public void addLoginRedirect(String url) {
		clientAddPage.loginRedirect(url);
	}

	@And("^I save the client registration")
	public void saveClient() {
		clientAddPage.save();
	}

	@And("^I set '(.+)' as login redirect$")
	public void setLoginRedirect(String url) {
		sectorAddPage.addLoginRedirect(url);
	}

	@And("^I pick '(.+)' as client$")
	public void chooseClient(String client) {
		sectorAddPage.addClient(client);
	}

	@And("^I save the sector$")
	public void save() {
		sectorAddPage.save();
	}

	@Then("^I should see that the list is not empty$")
	public void checkListIsNotEmpty() {
		sectorManagePage.assertListIsNotEmpty();
	}

	@When("^I delete that sector$")
	public void deleteSector() {
		sectorManagePage.deleteFirstSector();
	}

	@Then("^I should see that the list is empty$")
	public void checkListIsEmpty() {
		sectorManagePage.assertListIsEmpty();
	}

	@And("^I select the OIDC '(.+)' tab$")
	public void selectTab(String tabName) {
		clientAddPage.selectTab(tabName);
	}
	
	@After
	public void clear(Scenario scenario) {
		homePage.takeScreenShot(scenario);
		stopRecorder();
		homePage.clear();
	}

}
