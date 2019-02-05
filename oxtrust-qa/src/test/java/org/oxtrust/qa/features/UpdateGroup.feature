Feature: Update a group 
@gluuQA
Scenario: Update an existing group 
	When 	I sign in as administrator 
	Then 	I should see gluu home page
	When 	I go to group add page 
	And 	I add a group with display name 'QAGroupToBeUpdatedDN' 
	And 	I add a group with description 'QAGroupToBeUpdatedDescription' 
	And 	I add a group with visibility 'Public'
	And 	I save the group 
	When 	I go to groups manage page 
	And 	I search for group with pattern 'QAGroupToBeUpdatedDN'
	And 	I should see a group with display name 'QAGroupToBeUpdatedDN'
	When 	I start to update that group
	And 	I set the new display name to 'QAGroupNewDisplayName' 
	And 	I set the new description to 'QAGroupNewDescription'
	And 	I set the new visibility to 'Private'
	And 	I save the group edition
	When 	I go to groups manage page 
	And 	I search for group with pattern 'QAGroupNewDisplayName' 
	Then 	I should see a group with description 'QAGroupNewDescription' 
	And 	I should see a group with display name 'QAGroupNewDisplayName' 
	When 	I start to update that group 
	And 	I delete the current group 
	And 	I search for group with pattern 'QAGroupNewDisplayName'
	Then 	I should not see a group with display name 'QAGroupNewDisplayName'
	Then 	I sign out