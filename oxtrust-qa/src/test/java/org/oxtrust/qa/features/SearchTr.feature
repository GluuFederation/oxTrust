Feature: Search a Trust relationship 
@gluuQA
Scenario: Search a Trust relationship
	When 	I sign in as administrator 
	Then 	I should see gluu home page 
	When 	I go to tr add page
	Then 	I set 'QaTRSearchDN' as display name
	And 	I set 'QaTRSearchDesc' as description
	And 	I set 'Single SP' as entity type
	And 	I set 'File' as metadata location
	And 	I set the metadata
	And 	I configure sp with 'SAML2SSO' profile
	And 	I release the following attributes 'Username Email'
	And 	I save the current tr
	When 	I go to tr list page
	And 	I search for tr named 'QaTRSearchDN'
	Then 	I should see a tr with display name 'QaTRSearchDN' in the list
	When 	I delete the tr named 'QaTRSearchDN' 
	And 	I go to tr list page 
	And 	I search for tr named 'QaTRSearchDN' 
	Then 	I should not see a tr with display name 'QaTRSearchDN' in the list 
	And 	I sign out
