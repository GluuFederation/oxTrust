Feature: List OpenID connect scopes
@gluuQA
Scenario: List OpenID connect scopes
	When 	I sign in as administrator 
	Then 	I should see gluu home page 
	When 	I go to openid connect scopes list page
	Then 	I should see an openid scope named 'address'
	And 	I should see an openid scope named 'clientinfo'
	And 	I should see an openid scope named 'email'
	And 	I should see an openid scope named 'mobile_phone'
	And  	I should see an openid scope named 'openid'
	And  	I should see an openid scope named 'permission'
	And 	I should see an openid scope named 'phone'
	And 	I should see an openid scope named 'profile'
	And 	I should see an openid scope named 'uma_protection'
	And 	I should see an openid scope named 'user_name'
	And 	I sign out