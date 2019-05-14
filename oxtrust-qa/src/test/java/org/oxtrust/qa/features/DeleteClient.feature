Feature: Delete OpenID connect clients
@gluuQA
Scenario: Delete OpenID connect clients
	When 	I sign in as administrator 
	When 	I go to openid connect clients list page
	And 	I start the process to add new client
	And 	I select the OIDC 'StandardTab' tab
	And 	I set the client name to 'QaClientToBeDeletedName'
	And 	I set the client description to 'QaClientToBeDeletedDescription'
	And 	I set the client secret to 'secret'
	And 	I set application type to 'Web'
	#And 	I set preauthorization to 'False'
	#And 	I set persist authorization to 'True'
	And 	I set subject type to 'public'
	And 	I set authentication method to 'client_secret_basic'
	And 	I add the scope named 'openid'
	And 	I add the response type named 'code'
	And 	I add the grant type named 'authorization_code'
	And 	I add the login redirect named 'https://qalogin/redirect'
	And 	I save the client registration
	When 	I go to openid connect clients list page
	And 	I search for openid clients with pattern 'QaClientToBeDeletedName'
	Then 	I should see an openid client named 'QaClientToBeDeletedName'
	When 	I start the process to edit the client named 'QaClientToBeDeletedName' 
	And 	I delete that client
	And 	I search for openid clients with pattern 'QaClientToBeDeletedName'
	Then 	I should not see an openid client named 'QaClientToBeDeletedName'
	And 	I sign out