Feature: Delete a user 
@gluuQA
Scenario: Delete a user
	When 	I sign in as administrator 
	Then 	I should see gluu home page 
	When 	I go to user add page 
	And 	I add a user named 'QaUserDelete' 
	And 	With first name 'QaUserDeleteFN' 
	And 	With last name 'QaUserDeleteLN' 
	And 	With display name 'QaUserDeleteDN' 
	And 	With email 'QaUserDelete@gmail.com' 
	And 	With password 'QaUserDelete' 
	And 	With status 'Inactive' 
	And 	I save the user
	When 	I go to users manage page 
	And 	I search for user with pattern 'QaUserDelete' 
	Then 	I should see a user named 'QaUserDelete' 
	When 	I start to update that user 
	And 	I delete the current user
	When 	I search for user with pattern 'QaUserDelete' 
	Then 	I should not see a user named 'QaUserDelete'
	And 	I sign out
