Feature: Update user 
@gluuQA
Scenario: Update an existing user 
	When 	I sign in as administrator 
	Then 	I should see gluu home page 
	When 	I go to user add page 
	And 	I add a user named 'QaUserUpdateName' 
	And 	With first name 'QaUserUpdateFN' 
	And 	With last name 'QaUserUpdateLN' 
	And 	With display name 'QaUserUpdateDN' 
	And 	With email 'QaUserUpdate@gmail.com' 
	And 	With password 'QaUserUpdate' 
	And 	With status 'Inactive' 
	And 	I save the user
	When 	I go to users manage page 
	And 	I search for user with pattern 'QaUserUpdateName' 
	Then 	I should see a user named 'QaUserUpdateName' 
	When 	I start to update that user 
	And 	I set the display name to 'QaUserUpdate Display Name' 
	And 	I set the email to 'QaUserUpdate@gluu.com' 
	And 	I set the userName to 'QaUserUpdatedName' 
	And 	I save the update 
	When 	I go to users manage page
	And 	I search for user with pattern 'QaUserUpdatedName' 
	Then 	I should see a user named 'QaUserUpdatedName' 
	And 	I should see a user with display name 'QaUserUpdate Display Name' 
	When 	I start to update that user 
	And 	I delete the current user
	When 	I search for user with pattern 'QaUserUpdatedName' 
	Then 	I should not see a user named 'QaUserUpdatedName'
	And 	I sign out 
