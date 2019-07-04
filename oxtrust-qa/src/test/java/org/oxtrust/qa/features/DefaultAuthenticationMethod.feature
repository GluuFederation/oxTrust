Feature: Default authentication method 
@gluuQA
Scenario: Check default authentication method 
	When 	I sign in as administrator 
	Then 	I should see gluu home page 
	When 	I go to default authentication page 
	And 	I should see default acr set to 'auth_ldap_server' 
	And 	I should see oxtrust acr set to 'auth_ldap_server' 
	Then 	I sign out 
	
@gluuQA 
Scenario: Change default authentication method 
	When 	I sign in as administrator 
	Then 	I should see gluu home page 
	When 	I go to default authentication page 
	And 	I set to default acr to 'auth_ldap_server' 
	And 	I set to oxtrust acr to 'auth_ldap_server' 
	And 	I save the default method configuration 
	And 	I go to default authentication page
	Then 	I should see default acr set to 'auth_ldap_server' 
	And 	I should see oxtrust acr set to 'auth_ldap_server' 
	Then 	I sign out	