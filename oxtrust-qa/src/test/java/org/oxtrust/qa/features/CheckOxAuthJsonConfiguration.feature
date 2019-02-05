Feature: oxAuth json configuration 
@gluuQA
Scenario: oxAuth json configuration 
	When 	I sign in as administrator 
	When 	I go to oxauth Json configuration page 
	And 	I should see that the base endpoint is present 
	And 	I should see that the authorization endpoint is correct 
	And 	I should see that the token endpoint is correct 
	And 	I should see that the userinfo endpoint is correct 
	And 	I should see that the clientinfo endpoint is correct 
	And 	I should see that the endsession endpoint is correct 
	And 	I should see that the registration endpoint is correct 
	And 	I should see that the oidc discovery endpoint is correct 
	And 	I should see that the oidc configuration endpoint is correct
	Then  	I sign out