Feature: Update  a Trust reletionship 
@gluuQAPending1
Scenario: Update a Trust reletionship 
	When 	I sign in as administrator 
	Then 	I should see gluu home page 
	When 	I go to tr add page 
	Then 	I set 'QaTRUpdateDN' as display name 
	And 	I set 'QaTRUpdateDesc' as description 
	And 	I set 'Single SP' as entity type 
	And 	I set 'File' as metadata location 
	And 	I set the metadata 
	And 	I configure sp with 'SAML2SSO' profile 
	And 	I release the following attributes 'Username Email' 
	And 	I save the current tr 
	When 	I go to tr list page 
	And 	I search for tr named 'QaTRUpdateDN' 
	Then 	I should see a tr with display name 'QaTRUpdateDN' in the list 
	When 	I start the edition of tr named 'QaTRUpdateDN' 
	And 	I edit the display name to 'QaTRUpdatedDN' 
	And 	I edit the description to 'QaTRUpdatedDescription'
	And 	I update the current tr
	And 	I go to tr list page
	And 	I search for tr named 'QaTRUpdateDN'
	Then 	I should see a tr with display name 'QaTRUpdateDN' in the list
	When 	I delete the tr named 'QaTRUpdateDN' 
	And 	I go to tr list page 
	And 	I search for tr named 'QaTRUpdateDN' 
	Then 	I should not see a tr with display name 'QaTRUpdateDN' in the list 
	And 	I sign out