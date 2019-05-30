Feature: List uma resources 
@gluuQA
Scenario: List uma resources 
	When 	I sign in as administrator 
	Then 	I should see gluu home page 
	When 	I go to uma resources list page
	Then 	I should see a uma resource named 'SCIM Resource' with scopes 'SCIM Access'
	And 	I should see a uma resource named 'oxTrust api Resource' with scopes 'API Read Access'
	Then 	I sign out