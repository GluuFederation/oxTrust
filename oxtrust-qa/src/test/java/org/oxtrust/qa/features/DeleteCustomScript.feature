Feature: Delete custom script 
@gluuQA
Scenario: Delete custom script 
	When 	I sign in as administrator 
	Then 	I should see gluu home page 
	When 	I go to Manage Custom Script 
	Then    I select the 'Person Authentication' tab
	And 	I click the add custom script button
	And 	I set the custom script name to 'QACustomScriptToBeDeleted' 
	And 	I set the custom script description to 'QACustomScriptToBeDeletedDesc' 
	And 	I set the custom script level to '5' 
	And 	I set the custom script location type to 'Ldap'
	And 	I set the custom script usage type to 'Web'
	And 	I set the custom script content to 'QACustomScriptContentToBeDeleted'
	And 	I enable the script
	And 	I save the custom script
	When 	I delete the custom script named 'QACustomScriptToBeDeleted' on 'Person Authentication' tab
	And 	I sign out	