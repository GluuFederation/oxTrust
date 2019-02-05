Feature: SMTP config 
@gluuQA
Scenario: SMTP config 
	When 	I sign in as administrator 
	Then 	I should see gluu home page 
	When 	I go to smtp configuration page
	And 	I set 'smtp.gmail.com' as smtp host
	And 	I set 'GluuQA' as from name
	And 	I set 'gluutestmail@gmail.com' as from email address
	And 	I set 'gluutestmail@gmail.com' as username
	And 	I set 'GluuTestMail' as password
	And 	I set '587' as smtp port
	And 	I test the configuration
	And 	I save the configuration
	Then 	I sign out
	