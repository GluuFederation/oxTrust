Feature: Passport Configuration 
@gluuQA
Scenario: Passport Configuration 
    When    I sign in as administrator 
    Then    I should see gluu home page
    When    I go to passport configuration page
    And     I set the log level to 'debug'
    And     I update the config