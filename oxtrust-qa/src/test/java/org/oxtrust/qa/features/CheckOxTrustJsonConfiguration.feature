Feature: Check Oxtrust json configuration 
@gluuQA
Scenario: Oxtrust json configuration 
	When 	I sign in as administrator 
	Then 	I should see gluu home page 
	When 	I go to oxtrust Json configuration page 
	Then 	I should see the cert dir is present
	And 	I should see the base dn is present 
	And 	I should see that the support mail is present 
	And 	I should see that the application url is present 
	And 	I should see that the base endpoint is present 
	And 	I should see that the log level is present 
	And 	I should see that the sicm max count is present
	Then 	I sign out 
	