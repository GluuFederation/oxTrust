Feature: OxAuth setting config 
@gluuQA
Scenario: OxAuth setting config 
	When 	I sign in as administrator 
	Then 	I should see gluu home page 
	When 	I go to oxauth setting configuration page 
	Then 	I should that the server ip is empty
	Then 	I sign out