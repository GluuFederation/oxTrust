Feature: Update an Attribute
  @gluuQA
  Scenario: Update an Attribute
    When I sign in as administrator
    Then I should see gluu home page
    When I go to Attributes page
    Then I want to see all attributes
    And I click on the first listed attribute
    And I choose the multivalued option
    And I set the Attribute description: 'Random description to check if the test works'
    And I update an attribute
    When I go to Attributes page
    Then I want to see all attributes
    Then I check if an attribute exists with the following description: 'Random description to check if the test works'
    And I click on the first listed attribute
    And I register SAML1 URI: 'urn:mace:dir:attribute-def:TEST'
    And I register SAML2 URI: 'urn:oid:2.5.4.666'
    And I register a display name: 'TESTY TEST'
    And I set a type
    And I choose the edit type
    And I choose the view type
    And I choose the usage type
    And I choose the multivalued option
    And I register a claim name: 'Test_QA'
    And I choose the SCIM attribute option
    And I set the Attribute description: 'This is a test attribute'
    And I enable custom validation
    And I enable a tooltip for this attribute
    And I set the tooltip text to: 'TEST TOOLTIP'
    And I set the minimum length: '1'
    And I set the maximum length: '666'
    And I set the regex pattern: 'regex-test'
    And I update an attribute
    When I go to Attributes page
    Given I want to see all attributes
    Then I check if an attribute exists with the following description: 'This is a test attribute'
    And I sign out


    