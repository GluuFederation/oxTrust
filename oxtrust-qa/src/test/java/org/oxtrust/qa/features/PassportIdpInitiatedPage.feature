Feature: Passport IDP 
@gluuQA
Scenario: Passport IDP
    When    I sign in as administrator 
    Then    I should see gluu home page
    When    I go to system organization configuration page
    And     I set the Passport Support to 'true'
    And     I click on the Update button
    When    I go to passport idp page
    Then    I should see that the endpoint is not empty
    And     I should see that the acr is not empty
    And     I select 'API Requesting Party Client'
    And     I save the idp config
    Then    I sign out