Feature: Add new uma scope 
@gluuQA
Scenario: Add new uma scope 
	When 	I sign in as administrator 
	Then 	I should see gluu home page 
	When 	I go to uma scope add page 
	And 	I set uma scope id to a random value 
	And 	I set uma scope display name to 'QAUmaScopeDN' 
	And 	I set uma scope logo to 'https://gluu.org/logo.png' 
	And 	I save the scope
	When 	I go to uma scope list page
	And 	I search for scopes with pattern 'QAUmaScopeDN'
	Then 	I should see a uma scope named 'QAUmaScopeDN'
	And 	I start the edit of the scope named 'QAUmaScopeDN'
	And 	I delete the current scope
	And 	I search for scopes with pattern 'QAUmaScopeDN'
	Then 	I should not see a uma scope named 'QAUmaScopeDN'
	And 	I sign out