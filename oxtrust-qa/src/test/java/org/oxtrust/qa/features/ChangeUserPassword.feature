Feature: Change user password 
@gluuQA
Scenario: Change user password 
	When 	I sign in as administrator 
	When 	I go to user add page 
	And 	I add a user named 'QaUserPasswordChanged' 
	And 	With first name 'QaUserPasswordChangedFN' 
	And 	With last name 'QaUserPasswordChangedLN' 
	And 	With display name 'QaUserPasswordChangedDN' 
	And 	With email 'QaUserPasswordChanged@gmail.com' 
	And 	With password 'QaUserPassword' 
	And 	With status 'Active' 
	And 	I save the user 
	When 	I go to users manage page 
	And 	I search for user with pattern 'QaUserPasswordChanged' 
	Then 	I should see a user named 'QaUserPasswordChanged' 
	When 	I start to update that user 
	And 	I set his password to 'QaUserPasswordChanged'
	And 	I sign out
	Then 	I should be able to login as 'QaUserPasswordChanged' with password 'QaUserPasswordChanged'
	And 	I sign out
	Then 	I sign in as administrator
	When 	I go to users manage page 
	And 	I search for user with pattern 'QaUserPasswordChanged' 
	Then 	I should see a user named 'QaUserPasswordChanged' 
	When 	I start to update that user 
	And 	I delete the current user
	When 	I search for user with pattern 'QaUserPasswordChanged' 
	Then 	I should not see a user named 'QaUserPasswordChanged'
	And 	I sign out 
	
@gluuQA
Scenario: Change user password from profile page
    When 	I sign in as administrator 
	And 	I go to user add page 
	And 	I add a user named 'UserProfilePassword' 
	And 	With first name 'UserProfilePasswordFN' 
	And 	With last name 'UserProfilePasswordLN' 
	And 	With display name 'UserProfilePasswordDN' 
	And 	With email 'UserProfilePassword@gmail.com' 
	And 	With password 'UserProfilePassword' 
	And 	With status 'Active' 
	And 	I save the user
	When 	I go to system organization configuration page
    And     I set the User to Edit Own Profile to 'true'
    And     I click on the Update button
	And 	I sign out
	Then 	I should be able to login as 'UserProfilePassword' with password 'UserProfilePassword'
	When 	I go to my profile page
	And 	I change my password from 'UserProfilePassword' to 'UserProfilePasswordChanged'
	And 	I sign out
	Then 	I should be able to login as 'UserProfilePassword' with password 'UserProfilePasswordChanged'
	And 	I sign out
	When 	I sign in as administrator
	And 	I go to users manage page 
	And 	I search for user with pattern 'UserProfilePassword' 
	Then 	I should see a user named 'UserProfilePassword' 
	When 	I start to update that user 
	And 	I delete the current user
	When 	I search for user with pattern 'UserProfilePassword' 
	Then 	I should not see a user named 'UserProfilePassword'
	And 	I sign out
	
	
	

