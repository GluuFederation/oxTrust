oxTrust API Client library
======

With oxTrust API integration test application (usage example).


Library usage:

    String baseURI = "https://localhost/identity/";
    String login = "admin";
    String password = "password";
    
    OxTrustClient client = new OxTrustClient(baseURI, login, password);
    TrustRelationshipClient samlClient = client.getTrustRelationshipClient();
    
    List<SAMLTrustRelationshipShort> trustRelationships = samlClient.list();
    samlClient.delete(trustRelationships.get(0).getInum());
    
    
    
Integration test application usage:

1. Edit client/conf/configuration.properties with your oxTrust server parameters.
2. Run java -cp target/oxtrust-client-4.3.0.Final.jar org.gluu.oxtrust.api.test.TestMain

Embedding to automatic tests:

    org.gluu.oxtrust.api.test.TestMain.main() returns 0 if all tests passed and 1 if an error has been occured.
