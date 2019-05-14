Feature: Import users
@gluuQAPending1
Scenario:   Import users to gluu server
	When 	I sign in as administrator 
	Then 	I should see gluu home page 
	When 	I go to users import page
	And 	I import the sample excel file
	When 	I go to users manage page 
	And 	I search for user with pattern 'fname' 
	Then 	I should see a user named 'fname' 
	And 	I should see a user with display name 'fname'
	When 	I start to update that user 
	And 	I delete the current user
	When 	I search for user with pattern 'fname' 
	Then 	I should not see a user named 'fname'
	And 	I sign out 
