Feature: Check Server Status
@gluuQA
Scenario: Check Server Status 
	When 	I sign in as administrator
	And 	I go to server status page
	Then 	I should see '9' server parameters
	Then 	I should see the hostname is present and not empty
	And 	I should see the ip address is present and not empty
	And 	I should see the system uptime is present and not empty
	And 	I should see the last update is present and not empty
	And 	I should see the polling interval is present and not empty
	And 	I should see the person count is present and not empty
	And 	I should see the group count is present and not empty
	And 	I should see the free memory is present and not empty
	And 	I should see the free disk is present and not empty
	Then 	I sign out