Feature: Setup Cache Refresh

@gluuQA
Scenario: Setup cache refresh
	When 	I sign in as administrator
	And 	I go to cache refresh page
	And 	I select the tab named 'Cache Refresh'
	And 	I delete all source to destination attribute
	And 	I add a source attribute named 'uid' to destination attribute named 'uid'
	And 	I add a source attribute named 'ldapName' to destination attribute named 'Username'
	And 	I set polling interval to '2' minutes
	And 	I enable cache refresh
	And 	I select the tab named 'Customer Backend Key/Attributes'
	And 	I delete all key attributes
	And 	I add a key attribute named 'KeyAtrribOne'
	And 	I add a key attribute named 'KeyAtrribTwo'
	And 	I delete all objects
	And 	I add an object class named 'top'
	And 	I add an object class named 'account'
	And 	I delete source all attributes
	And 	I add source attribute named 'uid'
	And 	I add source attribute named 'name'
	Then 	I select the tab named 'Source Backend LDAP Servers'
	And 	I add a source server named 'secondSource' with bindDn 'cn=directory manager,o=gluu' with maxCon '1000' with servers 'localhost:1636' with baseDns 'o=gluu;o=site' using ssl 'true'
	And 	I change the bind dn password to 'toor'
	And 	I save the cache refresh configuration
	Then 	I sign out
	
