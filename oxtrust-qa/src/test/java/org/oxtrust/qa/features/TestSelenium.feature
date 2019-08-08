Feature: Test selenium
@gluuSE
Scenario: Test selenium
	When 	I sign in as administrator 
	Then 	I should see gluu home page 
	When 	I go to openid connect clients list page
	And     I sign out