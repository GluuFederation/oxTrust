Feature: Search uma scopes 
@gluuQA
Scenario: Search uma scopes 
	When 	I sign in as administrator 
	Then 	I should see gluu home page 
	When 	I go to uma scope list page
	And 	I search for scopes with pattern 'SCIM'
	Then 	I should see a uma scope named 'SCIM Access'
	And 	I sign out