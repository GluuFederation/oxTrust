Feature: Update System Configuration
  @gluuQA
  Scenario: Configure system as an admin
    When 	I sign in as administrator
    Then 	I should see gluu home page
    When 	I go to system organization configuration page
    And     I set the Self-Service Password Reset to 'Disabled'
    And     I set the SCIM Support to 'Enabled'
    And     I set the Passport Support to 'Enabled'
    And     I set the DNS Server to '666'
    And     I set the Maximum Log Size to '1111'
    And     I set the User to Edit Own Profile to 'Disabled'
    And     I set the Contact Email to 'natt.tester@gmail.com'
    And     I click on the Update button
    Then    I should see the Self-Service Password Reset set to 'Disabled'
    And     I should see the SCIM Support set to 'Enabled'
    And     I should see the Passport Support set to 'Enabled'
    And     I should see the DNS Server set to '666'
    And     I should see the Maximum Log Size Value set to '1111'
    And     I should see the User to Edit Own Profile set to 'Disabled'
    And     I should see the Contact Email set to 'natt.tester@gmail.com'
    And     I sign out


