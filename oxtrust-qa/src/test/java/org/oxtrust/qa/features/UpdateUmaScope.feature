Feature: Update uma scope
@gluuQA
Scenario: Update uma scope 
	When 	I sign in as administrator 
	Then 	I should see gluu home page 
	When 	I go to uma scope add page 
	And 	I set uma scope id to a random value 
	And 	I set uma scope display name to 'QAUmaScopeToBeUpdatedDN' 
	And 	I save the scope
	When 	I go to uma scope list page
	And 	I search for scopes with pattern 'QAUmaScopeToBeUpdatedDN'
	Then 	I should see a uma scope named 'QAUmaScopeToBeUpdatedDN'
	And 	I start the edit of the scope named 'QAUmaScopeToBeUpdatedDN'
	And 	I edit uma scope id to '998877665544332211'
	And 	I edit uma scope display name to 'QAUmaScopeUpdatedDN'
	And 	I save scope edition 
	When 	I go to uma scope list page
	And 	I search for scopes with pattern 'QAUmaScopeUpdatedDN'
	Then 	I should see a uma scope named 'QAUmaScopeUpdatedDN'
	And 	I start the edit of the scope named 'QAUmaScopeUpdatedDN'
	And 	I delete the current scope
	And 	I search for scopes with pattern 'QAUmaScopeUpdatedDN'
	Then 	I should not see a uma scope named 'QAUmaScopeUpdatedDN'
	And 	I sign out		