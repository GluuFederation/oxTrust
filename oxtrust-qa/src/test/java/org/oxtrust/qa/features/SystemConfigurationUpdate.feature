Feature: Update System Configuration
  @gluuQA
  Scenario: Configure system as an admin
    When 	I sign in as administrator
    Then 	I should see gluu home page
    When 	I go to system organization configuration page
    And     I set the Self-Service Password Reset to 'false'
    And     I set the SCIM Support to 'true'
    And     I set the Passport Support to 'true'
    And     I set the Maximum Log Size to '1111'
    And     I set the User to Edit Own Profile to 'false'
    And     I set the Contact Email to 'natt.tester@gmail.com'
    And     I click on the Update button
    Then    I should see the Self-Service Password Reset set to 'false'
    And     I should see the SCIM Support set to 'true'
    And     I should see the Passport Support set to 'true'
    And     I should see the Maximum Log Size Value set to '1111'
    And     I should see the User to Edit Own Profile set to 'false'
    And     I should see the Contact Email set to 'natt.tester@gmail.com'
    And     I sign out


