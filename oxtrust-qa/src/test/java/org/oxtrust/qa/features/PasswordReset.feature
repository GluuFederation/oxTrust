Feature: Password reset 
@gluuQA
Scenario: Password reset 
	When 	I sign in as administrator 
	Then 	I should see gluu home page 
	When 	I go to user add page 
	And 	I add a user named 'QaUserForMail' 
	And 	With first name 'QaUserFNForMail' 
	And 	With last name 'QaUserLNForMail' 
	And 	With display name 'QaUserDNForMail' 
	And 	With email 'thomas@gluu.org' 
	And 	With password 'QaUser' 
	And 	With status 'Inactive' 
	And 	I save the user
	When 	I go to smtp configuration page
	And 	I set 'smtp.gmail.com' as smtp host
	And 	I set 'GluuQA' as from name
	And 	I set 'gluutestmail@gmail.com' as from email address
	And 	I set 'gluutestmail@gmail.com' as username
	And 	I set 'GluuTestMail' as password
	And 	I ckeck require authentication
	And 	I ckeck require ssl
	And 	I set '587' as smtp port
	And 	I test the configuration
	And 	I save the configuration
	When 	I go to system organization configuration page
    And     I set the Self-Service Password Reset to 'Enabled'
    And     I click on the Update button
    And 	I sign out
    And 	I click on password reset link
    Then 	I set 'thomas@gluu.org' as email
    And 	I send the mail
    And 	I sign in as administrator
    When 	I go to users manage page 
	And 	I search for user with pattern 'QaUserDNForMail' 
	Then 	I should see a user named 'QaUserForMail'
	And 	I should see a user with display name 'QaUserDNForMail'
	When 	I start to update that user 
	And 	I delete the current user
	When 	I search for user with pattern 'QaUserDNForMail' 
	Then 	I should not see a user named 'QaUserDNForMail'
    And 	I sign out
