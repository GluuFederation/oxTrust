Feature: Check Cache Provider configuration 
@gluuQA
Scenario: Oxtrust json configuration 
	When 	I sign in as administrator 
	Then 	I should see gluu home page 
	When 	I go to cache provider Json configuration page 
	Then 	I should see the cache provider type set to 'IN_MEMORY' or 'NATIVE_PERSISTENCE' 
	And 	I should see a memcache config with type 'IN_MEMORY' with servers 'localhost:11211' with maxOQL '100000' with buffer '32768' with put expiration '60' 
	And 	I should see a redis config with type 'STANDALONE' with servers 'localhost:6379' and put expriration '60' 
	Then 	I sign out