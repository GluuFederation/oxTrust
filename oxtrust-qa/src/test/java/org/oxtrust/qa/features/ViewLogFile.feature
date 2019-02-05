Feature: View log files 
@gluuQA
Scenario: View log files
	When 	I sign in as administrator 
	Then 	I should see gluu home page 
	When 	I go to view log file page
	Then 	I should see log files named 'oxtrust.log' and 'oxauth.log'
	Then 	I sign out