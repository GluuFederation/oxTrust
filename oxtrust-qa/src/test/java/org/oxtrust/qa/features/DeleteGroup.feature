Feature: Delete a group 
@gluuQA
Scenario:   Delete an existing group 
	When 	I sign in as administrator
	When 	I go to group add page 
	And 	I add a group with display name 'QAGroupDeleteDN' 
	And 	I add a group with description 'QAGroupDeleteDes' 
	And 	I add a group with visibility 'Public'
	And 	I save the group 
	When 	I go to groups manage page 
	And 	I search for group with pattern 'QAGroupDeleteDN' 
	And 	I should see a group with display name 'QAGroupDeleteDN' 
	When 	I start to update that group 
	And 	I delete the current group 
	And 	I search for group with pattern 'QAGroupDeleteDN'
	Then 	I should not see a group with display name 'QAGroupDeleteDN'
	Then 	I sign out