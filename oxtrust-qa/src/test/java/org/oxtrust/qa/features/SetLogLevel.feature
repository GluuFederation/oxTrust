Feature: Set oxTrust log level
@gluuQA
Scenario: Set oxTrust log level
	When 	I sign in as administrator
	When 	I go to oxtrust Json configuration page
	And 	I set the oxtrust logging level to 'INFO'
	And 	I save the oxtrust json configuration
	Then 	I sign out

@gluuQA
Scenario: Set oxAuth log level
	When 	I sign in as administrator
	When 	I go to oxauth Json configuration page
	And 	I set the oxauth logging level to 'INFO'
	And 	I save the json configuration
	Then 	I sign out	