Feature: Passport provider 
@gluuQA
Scenario: Passport provider 
    When    I sign in as administrator 
    Then    I should see gluu home page 
    When    I go to system organization configuration page
    And     I set the Passport Support to 'true'
    And     I click on the Update button
    When    I go to passport providers list 
    And     I start the process to add new provider 
    And     I set id 'LinkedInID'
    And     I set display name 'LinkedIn' 
    And     I set type 'openidconnect' 
    And     I set client id to 'MyClientID' 
    And     I set client secret to 'MyClientSecret' 
    And     I apply the change
    When    I go to passport providers list
    Then    I should see a provider named 'LinkedIn'
    When    I start the process to edit the provider named 'LinkedIn'
    And     I set display name 'LinkedInQA'
    And     I apply the change
    When    I go to passport providers list
    Then    I should see a provider named 'LinkedInQA'
    When    I start the process to edit the provider named 'LinkedInQA'
    And     I delete it
    Then    I should not see a provider named 'LinkedInQA'
    Then    I sign out
    
    
    
    
