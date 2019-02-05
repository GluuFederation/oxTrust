Feature: Disable oxauth metrics
@gluuQAPending1
Scenario: Disable oxauth metrics
	When 	I sign in as administrator
	And 	I go to oxauth Json configuration page
	And 	I set the metricReporterEnabled to 'false'
	And 	I set the metricReporterInterval to '60'
	And 	I save the json configuration
	And 	I wait to configuration to be apply
	And 	I sign out
	When 	I sign in as administrator
	Then 	I should see that there 'is not' data in the dashboard
	And 	I sign out
	
@gluuQAPending1
Scenario: Enable oxauth metrics
	When 	I sign in as administrator
	And 	I go to oxauth Json configuration page
	And 	I set the metricReporterEnabled to 'true'
	And 	I set the metricReporterInterval to '60'
	And 	I save the json configuration
	And 	I wait to configuration to be apply
	And 	I sign out
	When 	I sign in as administrator
	Then 	I should see that there 'is' data in the dashboard
	And 	I sign out	
	
	
	