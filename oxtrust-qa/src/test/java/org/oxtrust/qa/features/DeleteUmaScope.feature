Feature: Delete uma scope
@gluuQA
Scenario: Delete uma scope 
	When 	I sign in as administrator 
	Then 	I should see gluu home page 
	When 	I go to uma scope add page 
	And 	I set uma scope id to a random value 
	And 	I set uma scope display name to 'QAUmaScopeToBeDeletedDN' 
	And 	I save the scope
	When 	I go to uma scope list page
	And 	I search for scopes with pattern 'QAUmaScopeToBeDeletedDN'
	Then 	I should see a uma scope named 'QAUmaScopeToBeDeletedDN'
	And 	I start the edit of the scope named 'QAUmaScopeToBeDeletedDN'
	And 	I delete the current scope
	When 	I go to uma scope list page
	And 	I search for scopes with pattern 'QAUmaScopeToBeDeletedDN'
	Then 	I should not see a uma scope named 'QAUmaScopeToBeDeletedDN'
	And 	I sign out	