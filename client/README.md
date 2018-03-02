# oxtrust-api-test
oxTrust API Client library.

oxTrust API integration test application usage example.


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
2. Run java -cp target/oxtrust-client-3.2.0-SNAPSHOT.jar org.gluu.oxtrust.api.test.TestMain