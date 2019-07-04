Feature: List uma scopes 
@gluuQA
Scenario: List uma scopes 
	When 	I sign in as administrator 
	Then 	I should see gluu home page 
	When 	I go to uma scope list page
	Then 	I should see a uma scope named 'SCIM Access'
	And 	I should see a uma scope named 'API Read Access'
	And 	I should see a uma scope named 'API Write Access'
	Then 	I sign out