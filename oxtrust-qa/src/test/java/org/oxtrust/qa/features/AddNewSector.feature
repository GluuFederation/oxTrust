Feature: Add new sector 
@gluuQA
Scenario: Add new sector 
	When 	I sign in as administrator 
	Then 	I should see gluu home page 
	When 	I go to sectors list page
	And 	I start the process to add new sector
	And 	I set 'https://gluuqa/login/redirect' as login redirect
	And 	I pick 'oxtrust' as client
	And 	I save the sector
	When 	I go to sectors list page
	Then 	I should see that the list is not empty
	When 	I delete that sector
	Then 	I should see that the list is empty
	And 	I sign out