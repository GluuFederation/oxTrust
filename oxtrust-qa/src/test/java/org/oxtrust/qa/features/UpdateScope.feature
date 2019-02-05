Feature: Update a scope 
@gluuQA
Scenario: Update a scope 
	When 	I sign in as administrator 
	Then 	I should see gluu home page 
	When 	I go to openid connect scopes list page
	And 	I start the process to add new scope
	And 	I set the display name 'QaScopeToBeEditedDN'
	And 	I set the description 'QaScopeToBeEditedDes'
	And 	I set the scope type to 'OpenID'
	And 	I set dynamic registration to 'True'
	And 	I save the scope registration
	When 	I go to openid connect scopes list page
	And 	I search for openid scopes with pattern 'QaScopeToBeEditedDN'
	Then 	I should see an openid scope named 'QaScopeToBeEditedDN'
	When 	I start the process to edit the scope named 'QaScopeToBeEditedDN'
	And 	I edit the display name value to 'QaScopeEditedDN'
	And 	I edit the description value to 'QaScopeEditedDes'
	And 	I set the scope type to 'OpenID'
	And 	I set dynamic registration to 'False'
	And 	I save the scope edition
	When 	I go to openid connect scopes list page
	And 	I search for openid scopes with pattern 'QaScopeEditedDN'
	Then 	I should see an openid scope named 'QaScopeEditedDN'
	When 	I start the process to edit the scope named 'QaScopeEditedDN'
	When 	I delete that scope
	And 	I search for openid scopes with pattern 'QaScopeEditedDN'
	Then 	I should not see an openid scope named 'QaScopeEditedDN'
	Then 	I sign out