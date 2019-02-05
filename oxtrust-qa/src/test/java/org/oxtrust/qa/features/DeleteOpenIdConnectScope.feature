Feature: Delete a scope 
@gluuQA
Scenario: Delete a scope 
	When 	I sign in as administrator 
	Then 	I should see gluu home page 
	When 	I go to openid connect scopes list page
	And 	I start the process to add new scope
	And 	I set the display name 'QaScopeToBeDeletedDN'
	And 	I set the description 'QaScopeToBeDeletedDes'
	And 	I set the scope type to 'Dynamic'
	And 	I set dynamic registration to 'True'
	And 	I save the scope registration
	When 	I go to openid connect scopes list page
	And 	I search for openid scopes with pattern 'QaScopeToBeDeletedDN'
	Then 	I should see an openid scope named 'QaScopeToBeDeletedDN'
	When 	I start the process to edit the scope named 'QaScopeToBeDeletedDN'
	When 	I delete that scope
	And 	I search for openid scopes with pattern 'QaScopeToBeDeletedDN'
	Then 	I should not see an openid scope named 'QaScopeToBeDeletedDN'
	Then 	I sign out