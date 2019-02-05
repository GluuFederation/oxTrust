Feature: Check Oxtrust import json configuration 
@gluuQA
Scenario: Oxtrust import json configuration 
	When 	I sign in as administrator 
	Then 	I should see gluu home page 
	When 	I go to oxtrust import Json configuration page 
	Then 	I should see that the there are six items present in the list
	Then 	I sign out