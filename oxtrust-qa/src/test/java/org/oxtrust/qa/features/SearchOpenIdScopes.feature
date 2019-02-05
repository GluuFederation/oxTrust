Feature: Search OpenID connect scopes
@gluuQA
Scenario: Search OpenID connect scopes
	When 	I sign in as administrator 
	Then 	I should see gluu home page 
	When 	I go to openid connect scopes list page
	And 	I search for openid scopes with pattern 'openid'
	Then 	I should see an openid scope named 'openid'
	And 	I sign out