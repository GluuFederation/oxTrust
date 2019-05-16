Feature: Check all custom scripts
@gluuQA
Scenario: Check all custom scripts
	When 	I sign in as administrator 
	Then 	I should see gluu home page 
	When 	I go to Manage Custom Script
	And 	I select the 'Consent Gathering' tab
	Then 	I should see a custom script named 'consent_gathering' in 'Consent Gathering' tab
	When 	I select the 'Update User' tab
	Then 	I should see a custom script named 'update_user' in 'Update User' tab
	When 	I select the 'User Registration' tab
	Then 	I should see a custom script named 'user_registration' in 'User Registration' tab
	Then 	I should see a custom script named 'user_confirm_registration' in 'User Registration' tab
	When 	I select the 'Client Registration' tab
	Then 	I should see a custom script named 'client_registration' in 'Client Registration' tab
	When 	I select the 'Dynamic Scopes' tab
	Then 	I should see a custom script named 'dynamic_permission' in 'Dynamic Scopes' tab
	Then 	I should see a custom script named 'work_phone' in 'Dynamic Scopes' tab
	Then 	I should see a custom script named 'org_name' in 'Dynamic Scopes' tab
	When 	I select the 'Id Generator' tab
	Then 	I should see a custom script named 'id_generator' in 'Id Generator' tab
	When 	I select the 'Cache Refresh' tab
	Then 	I should see a custom script named 'cache_refresh' in 'Cache Refresh' tab
	When 	I select the 'Application Session' tab
	Then 	I should see a custom script named 'application_session' in 'Application Session' tab
	When 	I select the 'UMA RPT Policies' tab
	Then 	I should see a custom script named 'scim_access_policy' in 'UMA RPT Policies' tab
	And 	I should see a custom script named 'uma_rpt_policy' in 'UMA RPT Policies' tab
	When 	I select the 'SCIM' tab
	Then 	I should see a custom script named 'scim_event_handler' in 'SCIM' tab
	And 	I sign out