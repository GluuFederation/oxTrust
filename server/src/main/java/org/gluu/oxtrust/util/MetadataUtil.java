/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2016, Gluu
 */
package org.gluu.oxtrust.util;

/**
 * Extract SAML Metadata fields.
 *
 * @author Dmitry Ognyannikov, 2016
 */
public class MetadataUtil {
    private static final String METADATA_ENTITY_ID_XPATH = "/*/@entityID";
    /*
    public static String parseMetadata(File xmlDocument) {
        try {
            if (xmlDocument == null)// test 
            xmlDocument = new File("/home/sirius/temp/metadata.xml");
            //InputSource inputSource = new InputSource(new FileInputStream(xmlDocument));
            
            XPathFactory  factory = XPathFactory.newInstance();
            XPath xPath = factory.newXPath();
            
//            XPathExpression  xPathExpression = xPath.compile("/");
//            String root = xPathExpression.evaluate(new InputSource(new FileInputStream(xmlDocument)));
//            System.out.println("root: " + root);
            
            String entityID = xPath.evaluate(METADATA_ENTITY_ID_XPATH, new InputSource(new FileInputStream(xmlDocument)));
            System.out.println("entityID: " + entityID);
            
            
            return entityID;//inputSource.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static SamlIdpMetadata parseMetadata(String xmlURL) {
        try {
            HttpClient client = prepareClientCommons();
            
            String xml;
            HttpMethod method = new GetMethod(xmlURL);
            try {
            client.executeMethod(method);
            xml = method.getResponseBodyAsString();
            } finally {
                method.releaseConnection();
            }
            
            XPathFactory  factory = XPathFactory.newInstance();
            XPath xPath = factory.newXPath();
            String entityID = xPath.evaluate(METADATA_ENTITY_ID_XPATH, new InputSource(xml));
            System.out.println("entityID: " + entityID);
            
            SamlIdpMetadata metadata = new SamlIdpMetadata();
            metadata.setEntityID(entityID);
            
            return metadata;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static HttpClient prepareClientCommons() {
        try {           
            //SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).useTLS().build();
            
            Protocol protocol = new Protocol("https", new EasySSLProtocolSocketFactory(), 443);
            Protocol.registerProtocol("https", protocol);
            
           // HttpConnectionManager httpConnectionManager = new SimpleHttpConnectionManager(true);
            //HttpConnectionManagerParams hcmp = new HttpConnectionManagerParams();
            //httpConnectionManager.setParams(hcmp);
            HttpParams params = new DefaultHttpParams();
            HttpClientParams clientParams = new HttpClientParams(params);
            HttpClient client = new HttpClient(clientParams);
            
            //client.setHttpConnectionManager(httpConnectionManager);
            return client;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }*/
}
