Feature: Search Uma resources
@gluuQA
Scenario: Search uma resources
	When 	I sign in as administrator 
	Then 	I should see gluu home page 
	When 	I go to uma resources list page
	And 	I search for resources with pattern 'SCIM'
	Then 	I should see a uma resource named 'SCIM Resource' with scopes 'SCIM Access'
	And 	I sign out