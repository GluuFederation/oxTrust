Feature: Search user 
@gluuQA 
Scenario: Search user 
	When 	I sign in as administrator 
	Then 	I should see gluu home page 
	When 	I go to users manage page 
	And 	I search for user with pattern 'admin' 
	Then 	I should see a user named 'admin' 
	And 	I should see a user with display name 'Default Admin User' 
	Then 	I sign out 
