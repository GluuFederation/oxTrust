Feature: Check log viewer configuration
@gluuQA
Scenario: Check log viewer configuration 
	When 	I sign in as administrator
	And 	I go to log viewer configuration status page
	Then 	I should see that the oxTrust external log4j is empty
	And 	I should see that the oxAuth external log4j is empty
	And 	I should see a log template with name 'oxAuth logs' and value '/opt/gluu/jetty/oxauth/logs/*.log'
	And 	I should see a log template with name 'oxTrust logs' and value '/opt/gluu/jetty/identity/logs/*.log'
	Then 	I sign out
	
@gluuQA
Scenario: Add new log template 
	When 	I sign in as administrator
	And 	I go to log viewer configuration status page
	And 	I add a new log template named 'newTemplate' with value '/opt/gluu/jetty/oxauth/logs/*.log'
	And 	I should see a log template with name 'newTemplate' and value '/opt/gluu/jetty/oxauth/logs/*.log'
	Then 	I sign out
	
@gluuQA
Scenario: Delete a log template 
	When 	I sign in as administrator
	And 	I go to log viewer configuration status page
	And 	I delete the log template named 'newTemplate'
	And 	I should not see a log template with name 'newTemplate' and value '/opt/gluu/jetty/oxauth/logs/*.log'
	Then 	I sign out		