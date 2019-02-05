Feature: Manage LDAP authentication 
@gluuQA
Scenario: Check Manage LDAP authentication 
	When 	I sign in as administrator 
	Then 	I should see gluu home page 
	When 	I go to LDAP Authenticiation page 
	Then 	I should see an ldap source named 'auth_ldap_server' with bindDn 'cn=directory manager,o=gluu' with maxConn '1000' with primary key 'uid' with local primary key 'uid' with servers 'localhost:1636' with basedn 'o=gluu' and ssl 'true'
	And 	I sign out
	
@gluuQA
Scenario: Add Ldap Source Server 
	When 	I sign in as administrator 
	Then 	I should see gluu home page 
	When 	I go to LDAP Authenticiation page
	And 	I click the add source server button 
	Then 	I add an ldap source named 'second_server' with bindDn 'cn=directory manager,o=gluu' with maxConn '1000' with primary key 'uid' with local primary key 'uid' with servers 'localhost:1636' with basedn 'o=gluu' and ssl 'true'
	Then 	I should see an ldap source named 'second_server' with bindDn 'cn=directory manager,o=gluu' with maxConn '1000' with primary key 'uid' with local primary key 'uid' with servers 'localhost:1636' with basedn 'o=gluu' and ssl 'true'
	And 	I sign out
	
@gluuQA
Scenario: Delete Ldap Source Server 
	When 	I sign in as administrator 
	Then 	I should see gluu home page 
	When 	I go to LDAP Authenticiation page
	And 	I delete the ldap source named 'second_server'
	Then 	I should not see an ldap source named 'second_server'
	And 	I sign out		
	