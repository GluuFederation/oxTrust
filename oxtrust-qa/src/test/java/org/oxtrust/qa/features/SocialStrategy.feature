Feature: Social strategy 
@gluuQA
Scenario: Add social strategy
	When 	I sign in as administrator 
	When 	I go to strategy page
	And 	I enable passport status to 'Enabled'
	And 	I save the passport status changed
	When 	I go to strategy page
	And 	I add new strategy named 'github' with id 'a97a8b59b74d81887316' and secret 'df71abbb97eef23701af1d1b3402de5a02e883ae'
	And  	I go to strategy page
	Then 	I should see a strategy named 'github' in the list
	Then 	I sign out 
	
@gluuQA
Scenario: Delete social strategy
	When 	I sign in as administrator 
	Then 	I should see gluu home page 
	When 	I go to strategy page
	And 	I delete the strategy named 'github'
	And 	I save the passport status changed
	When 	I go to strategy page
    Then 	I should not see a strategy named 'github' in the list
	Then 	I sign out