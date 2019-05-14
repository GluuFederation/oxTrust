Feature: Attributes LDIFExport 
@gluuQA
Scenario: Attributes LDIF Export 
	When 	I sign in as administrator 
	And 	I go to Attributes export page 
	And 	I pick the attribute named 'c' 
	And 	I pick the attribute named 'mail' 
	And 	I export them
	Then 	I should see a file named 'attributes.ldif' in downloads folder
	And 	I sign out 
	
@gluuQA
Scenario: Attributes LDIF import 
	When 	I sign in as administrator 
	And 	I go to Attributes import page
	And 	I import the file named 'attributes.ldif' from the download directory
	Then 	I validate and import those attributes
	Then 	I sign out  
	