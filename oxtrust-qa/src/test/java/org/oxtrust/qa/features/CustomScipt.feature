Feature: custom script 
@gluuQA
Scenario: Add/Delete custom script 
	When 	I sign in as administrator 
	Then 	I should see gluu home page 
	When 	I go to Manage Custom Script 
	Then    I select the 'Person Authentication' tab
	And 	I click the add custom script button
	And 	I set the custom script name to 'QACustomScriptName' 
	And 	I set the custom script description to 'QACustomScriptDesc' 
	And 	I set the custom script level to '5' 
	And 	I set the custom script location type to 'Ldap'
	And 	I set the custom script usage type to 'Web'
	And 	I add new property named 'QaProperty' with value 'QaPropertyValue'
	And 	I set the custom script content to 'QACustomScriptContent'
	And 	I enable the script
	And 	I save the custom script
	Then 	I should see a custom script named 'QACustomScriptName' in 'Person Authentication' tab
	When 	I delete the custom script named 'QACustomScriptName' on 'Person Authentication' tab
	Then 	I should not see a custom script named 'QACustomScriptName' in 'Person Authentication' tab
	Then 	I sign out