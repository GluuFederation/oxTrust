Feature: Trust relationship 
@gluuQA
Scenario: Add trust relationship
	When 	I sign in as administrator 
	Then 	I should see gluu home page
	When 	I go to system organization configuration page
	And     I set the SAML support to 'true'
	And     I click on the Update button
	When 	I go to tr add page
	Then 	I set 'QaTRAddDN' as display name
	And 	I set 'QaTRAddDesc' as description
	And 	I set 'Single SP' as entity type
	And 	I set 'File' as metadata location
	And 	I set the metadata
	And 	I configure sp with 'SAML2SSO' profile
	And 	I release the following attributes 'Username Email'
	And 	I save the current tr
	When 	I go to tr list page
	And 	I search for tr named 'QaTRAddDN'
	Then 	I should see a tr with display name 'QaTRAddDN' in the list
	When    I start the edition of tr named 'QaTRAddDN' 
    And     I edit the display name to 'QaTRUpdatedDN' 
    And     I edit the description to 'QaTRUpdatedDescription'
    And     I update the current tr
    And     I go to tr list page
    And     I search for tr named 'QaTRUpdatedDN'
    Then    I should see a tr with display name 'QaTRUpdatedDN' in the list
	When    I delete the tr named 'QaTRUpdatedDN' 
    And     I go to tr list page 
    And     I search for tr named 'QaTRUpdatedDN' 
    Then    I should not see a tr with display name 'QaTRUpdatedDN' in the list 
    And     I sign out
	
@gluuQAPending1
Scenario: Add federation trust reletionship
	When 	I sign in as administrator 
	Then 	I should see gluu home page 
	When 	I go to system organization configuration page
	And     I set the SAML support to 'true'
	And     I click on the Update button
	When 	I go to tr add page
	Then 	I set 'QaFederation' as display name
	And 	I set 'QaFederationDesc' as description
	And 	I set 'Single SP' as entity type
	And 	I set 'File' as metadata location
	And 	I set the federation metadata
	And 	I save the current tr
	And 	I sign out
	And 	I sign in as administrator
	And 	I wait for tr validation
	When 	I go to tr add page
	Then 	I set 'QaFederated' as display name
	And 	I set 'QaFederatedDesc' as description
	And 	I set 'Single SP' as entity type
	And 	I set 'Federation' as metadata location
	And 	I select 'QaFederation' as federation tr
	And 	I configure sp with 'SAML2SSO' profile
	And 	I select 'https://sp.testshib.org/shibboleth-sp' as entity id
	And 	I release the following attributes 'Username Email'
	And 	I save the current tr
	And 	I sign out
	And 	I sign in as administrator
	When 	I go to tr list page
	And 	I wait for tr validation
	And 	I search for tr named 'QaFederated'
	Then 	I should see a tr with display name 'QaFederated' in the list
	When 	I delete the tr named 'QaFederated' 
	And 	I go to tr list page 
	And 	I search for tr named 'QaFederated' 
	Then 	I should not see a tr with display name 'QaFederated' in the list
	When 	I go to tr list page
	And 	I search for tr named 'QaFederation'
	Then 	I should see a tr with display name 'QaFederation' in the list
	When 	I delete the tr named 'QaFederation' 
	And 	I go to tr list page 
	And 	I search for tr named 'QaFederation' 
	Then 	I should not see a tr with display name 'QaFederation' in the list 
	And 	I sign out
		