Feature: Delete  a Trust reletionship 
@gluuQA
Scenario: Delete a Trust reletionship 
	When 	I sign in as administrator 
	Then 	I should see gluu home page 
	When 	I go to tr add page 
	Then 	I set 'QaTRToDeletedDN' as display name 
	And 	I set 'QaTRToDeletedDesc' as description 
	And 	I set 'Single SP' as entity type 
	And 	I set 'File' as metadata location 
	And 	I set the metadata 
	And 	I configure sp with 'SAML2SSO' profile 
	And 	I release the following attributes 'Username Email' 
	And 	I save the current tr 
	When 	I go to tr list page 
	And 	I search for tr named 'QaTRToDeletedDN' 
	Then 	I should see a tr with display name 'QaTRToDeletedDN' in the list 
	When 	I delete the tr named 'QaTRToDeletedDN' 
	And 	I go to tr list page 
	And 	I search for tr named 'QaTRToDeletedDN' 
	Then 	I should not see a tr with display name 'QaTRToDeletedDN' in the list 
	And 	I sign out