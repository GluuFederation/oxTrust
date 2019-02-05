Feature: Cancel System Configuration
  @gluuQA
  Scenario: Cancel system configuration as an admin
    When 	I sign in as administrator
    Then 	I should see gluu home page
    When 	I go to system organization configuration page
    And     I set the Self-Service Password Reset to 'Enabled'
    And     I set the SCIM Support to 'Disabled'
    And     I set the Passport Support to 'Disabled'
    And     I set the DNS Server to '333'
    And     I set the Maximum Log Size to '1'
    And     I set the User to Edit Own Profile to 'Enabled'
    And     I set the Contact Email to 'testytest@example.com'
    And     I click on the Cancel button
    Then    I should see gluu home page
    And     I go to system organization configuration page
    And     I should not see the DNS Server set to '333'
    And     I should not see the Maximum Log Size Value set to '1'
    And     I should not see the Contact Email set to 'testytest@example.com'
    And     I sign out