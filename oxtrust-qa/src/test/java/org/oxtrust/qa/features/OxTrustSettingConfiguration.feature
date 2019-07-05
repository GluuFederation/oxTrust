Feature: Organisation setting config 
@gluuQA
Scenario: OxTrust setting config 
	When 	I sign in as administrator 
	When 	I go to oxtrust setting configuration page
	And 	I should that the org name is not empty
	And 	I set the new org name to 'GLUU QA'
	And 	I should that 'Gluu Manager Group' is the admin group
	And 	I set the default qa logo as organisation logo
	And 	I set the default qa logo as organisation favicon
	And 	I save the oxtrust configuration
	Then 	I sign out