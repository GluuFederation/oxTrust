Feature: CheckPersonAuthentication custom script
@gluuQA
Scenario: CheckPersonAuthentication custom script
	When 	I sign in as administrator 
	Then 	I should see gluu home page 
	When 	I go to Manage Custom Script
	And 	I should see a custom script named 'u2f' in 'Person Authentication' tab
	And 	I should see a custom script named 'super_gluu' in 'Person Authentication' tab
	And 	I should see a custom script named 'duo' in 'Person Authentication' tab
	And 	I should see a custom script named 'cert' in 'Person Authentication' tab
	And 	I should see a custom script named 'passport_social' in 'Person Authentication' tab
	And 	I should see a custom script named 'passport_saml' in 'Person Authentication' tab
	And 	I should see a custom script named 'otp' in 'Person Authentication' tab
	And 	I should see a custom script named 'passport_social' in 'Person Authentication' tab
	And 	I should see a custom script named 'twilio_sms' in 'Person Authentication' tab
	And 	I should see a custom script named 'uaf' in 'Person Authentication' tab
	And 	I should see a custom script named 'uaf' in 'Person Authentication' tab
	And 	I should see a custom script named 'yubicloud' in 'Person Authentication' tab
	And 	I should see a custom script named 'basic_lock' in 'Person Authentication' tab
	And 	I should see a custom script named 'basic' in 'Person Authentication' tab
	Then 	I sign out