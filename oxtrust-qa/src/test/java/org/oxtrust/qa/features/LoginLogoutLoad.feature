Feature: Load test login logout
@gluuLoadTest
Scenario: Load test login logout
 	When 	I load test the login logout feature '10' times as user 'admin' with password 'toor'
 		