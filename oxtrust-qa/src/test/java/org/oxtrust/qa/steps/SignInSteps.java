package org.oxtrust.qa.steps;

import org.oxtrust.qa.pages.login.PasswordResetPage;
import org.oxtrust.qa.pages.login.SignInPage;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class SignInSteps extends BaseSteps {
	
	SignInPage signInPage=new SignInPage();
	PasswordResetPage passwordResetPage= new PasswordResetPage();
	
	@Before
	public void setup(Scenario scenario) {
		startRecorder(scenario);
	}

	@When("^I sign in as administrator$")
	public void signInAsAdmin() {
		signInPage.goToLoginPage();
		signInPage.fillFormAsAdmin();
		signInPage.submit();
	}

	@Then("^I should be able to login as '(.+)' with password '(.+)'$")
	public void loginAsUserWithPassword(String userName, String pwd) {
		signInPage.goToLoginPage();
		signInPage.fillForm(userName, pwd);
		signInPage.submit();
	}

	@When("^I sign in with username '(.+)' and password '(.+)'$")
	public void signInWithUserNameAndPassword(String userName, String password) {
		signInPage.goToLoginPage();
		signInPage.fillForm(userName, password);
		signInPage.submit();
	}

	@Then("^I should see gluu home page$")
	public void assertCurrentPageIsAdminPage() {
		signInPage.checkCurrentPageIsHomePage();
	}

	@When("^I sign out$")
	public void signOut() {
		signInPage.signOut();
	}

	@Then("^I should see the gluu login page$")
	public void checkLoginPage() {
		signInPage.checkCurrentPageIsLoginPage();
	}

	@And("^I click on password reset link$")
	public void clickPasswordReset() {
		signInPage.clickForgotPasswordLink();
	}

	@And("^I send the mail$")
	public void sendMail() {
		passwordResetPage.sendMail();
	}

	

	@Then("^I set '(.+)' as email$")
	public void setEmail(String email) {
		passwordResetPage.setEmail(email);
	}

	@Then("^I should see that the mail was send$")
	public void checkEmailWasSend() {
		passwordResetPage.verifyMailWasSend();
	}

	@When("^I load test the login logout feature '(.+)' times as user '(.+)' with password '(.+)'$")
	public void loadTestLogin(String times, String userName, String password) {
		int count = Integer.valueOf(times);
		for (int i = 0; i < count; i++) {
			signInPage.goToLoginPage();
			signInPage.fillForm(userName, password);
			signInPage.submit();
			signInPage.doSomeWork();
			signInPage.signOut();
		}

	}
	
	@After
	public void clear(Scenario scenario) {
		signInPage.takeScreenShot(scenario);
		stopRecorder();
		signInPage.clear();
	}

	
}