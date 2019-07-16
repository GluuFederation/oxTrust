Feature: Cas protocol 
@gluuQA
Scenario: Check Cas protocol
	When 	I sign in as administrator 
	Then 	I should see gluu home page 
	When 	I go to CAS protocol page
	Then 	I should see that the cas protocol is disable
	And 	I should see that service type is 'shibboleth.StorageService'
	And 	I should see that base url end with '/idp/profile/cas'
	And 	I save the cas config
	And 	I save the cas configuration update
	Then 	I sign out 