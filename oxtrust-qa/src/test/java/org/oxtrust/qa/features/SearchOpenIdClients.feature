Feature: Search OpenID connect clients
@gluuQA
Scenario: Search OpenID connect clients
	When 	I sign in as administrator 
	Then 	I should see gluu home page 
	When 	I go to openid connect clients list page
	And 	I search for openid clients with pattern 'SCIM Requesting Party Client'
	Then 	I should see an openid client named 'SCIM Requesting Party Client'
	And 	I sign out