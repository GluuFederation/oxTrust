Feature: Passport Configuration 
@gluuQA
Scenario: Passport Configuration 
    When    I sign in as administrator 
    Then    I should see gluu home page
    When    I go to system organization configuration page
    And     I set the Passport Support to 'true'
    And     I click on the Update button
    When    I go to passport configuration page
    And     I set the log level to 'debug'
    And     I update the config
    Then    I sign out