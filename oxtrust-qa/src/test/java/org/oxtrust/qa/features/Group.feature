Feature: Groups featute
@gluuQA
Scenario: Add/Delete new group 
	When 	I sign in as administrator 
	Then 	I should see gluu home page 
	When 	I go to group add page 
	And 	I add a group with display name 'QAGroupAddedDN' 
	And 	I add a group with description 'QAGroupAddedDescription' 
	And 	I add a group with visibility 'Public'
	And 	I add the user named 'admin' as member
	And 	I save the group
	Then 	I go to groups manage page 
	And 	I search for group with pattern 'QAGroupAddedDN' 
	Then 	I should see a group with description 'QAGroupAddedDescription' 
	And 	I should see a group with display name 'QAGroupAddedDN'
	When 	I start to update that group 
	And 	I delete the current group 
	And 	I search for group with pattern 'QAGroupAddedDN'
	Then 	I should not see a group with display name 'QAGroupAddedDN'
	Then 	I sign out

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