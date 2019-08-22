Feature: Check Certificates
@gluuQA
Scenario: Check Certificates 
	When 	I sign in as administrator
	And 	I go to certificates page
	Then 	I should see '3' certs in the list
	And 	I should see a cert named 'HTTPD SSL'
	#And 	I should see a cert named 'OpenDJ SSL'
	And 	I should see a cert named 'IDP SIGNING'
	And 	I should see a cert named 'IDP ENCRYPTION'
	Then 	I sign out