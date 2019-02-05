Feature: Add new user 
@gluuQA
Scenario: Add new user
	When 	I sign in as administrator 
	Then 	I should see gluu home page 
	When 	I go to user add page 
	And 	I add a user named 'QaUser' 
	And 	With first name 'QaUserFN' 
	And 	With last name 'QaUserLN' 
	And 	With display name 'QaUserDN' 
	And 	With email 'QaUser@gmail.com' 
	And 	With password 'QaUser' 
	And 	With status 'Inactive' 
	And 	I save the user
	When 	I go to users manage page 
	And 	I search for user with pattern 'QaUserDN' 
	Then 	I should see a user named 'QaUser'
	And 	I should see a user with display name 'QaUserDN'
	When 	I start to update that user 
	And 	I delete the current user
	When 	I search for user with pattern 'QaUserDN' 
	Then 	I should not see a user named 'QaUserDN'
	And 	I sign out
