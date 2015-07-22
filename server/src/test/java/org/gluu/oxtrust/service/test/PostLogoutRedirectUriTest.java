/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service.test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.gluu.oxtrust.action.test.AbstractAuthorizationTest;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.seam.mock.JUnitSeamTest;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class PostLogoutRedirectUriTest extends AbstractAuthorizationTest {

	
	@Test
	public void testAddPostLogoutRedirectUri() throws Exception {
		loginAndCheckLoggedInFacesRequest("test.login.user.admin");
		
		final Map<String, Object> data = new HashMap<String, Object>(6);
		new JUnitSeamTest.FacesRequest() {
            @Override
            public void invokeApplication() throws Exception {
//            	TrustService trustService = (TrustService) getInstance("trustService");
//            	List<GluuSAMLTrustRelationship> trs = trustService.getAllTrustRelationships();
//            	int trСount = trs.size();
//            	assert (trСount > 1) : "Not enough Trust Relationships were found at test server(at least 2 required). Cannot continue the test";
//            	
//            	GluuSAMLTrustRelationship tr1 = trs.get(random(trСount-1));
//            	GluuSAMLTrustRelationship tr2 = trs.get(random(trСount-1));
//            	while(tr2.equals(tr1)){
//            		tr2 = trs.get(random(trСount-1));
//            	}
//            	
//            	ClientService clientService = (ClientService) getInstance("clientService");
//            	OxTrustConfiguration oxTrustConfiguration = (OxTrustConfiguration) getInstance("oxTrustConfiguration");
//            	ApplicationConfiguration applicationConfiguration = oxTrustConfiguration.getApplicationConfiguration();
//            	OxAuthClient client = clientService.getClientByInum(applicationConfiguration.getOxAuthClientId());
//            	String randomUrl1 = random(1)>0?"http":"https" + "://"+ StringHelper.getRandomString(random(10)+1) + "." + StringHelper.getRandomString(random(3)+1);
//            	String randomUrl2 = random(1)>0?"http":"https" + "://"+ StringHelper.getRandomString(random(10)+1) + "." + StringHelper.getRandomString(random(3)+1);
//            	data.put("tr1Inum", tr1.getInum());
//            	data.put("tr2Inum", tr2.getInum());
//            	data.put("clientInum", client.getInum());
//            	data.put("tr1Logout", tr1.getSpLogoutURL());
//            	data.put("tr2Logout", tr2.getSpLogoutURL());
//            	data.put("randomUrl1", randomUrl1);
//            	data.put("randomUrl2", randomUrl2);
//            	data.put("clientLogout", client.getPostLogoutRedirectUris());
            }
        }.run();
        
		new JUnitSeamTest.FacesRequest("/trustmanager/update/" + (String) data.get("tr1Inum")) {
//			
//			  @Override
//			  protected void processValidations() throws Exception {
//				  System.out.println("#{_trustRelationship.displayName} resulted in " + getValue("#{_trustRelationship.displayName}"));
//		            validateValue("#{_trustRelationship.spLogoutURL}", (String) data.get("randomUrl1"));
//		            assert !isValidationFailure();
//		         }
//			  
//		      @Override
//		      protected void updateModelValues() throws Exception {
//		    	  setValue("#{_trustRelationship.spLogoutURL}", (String) data.get("randomUrl1"));
//		      }
//		      
//		      @Override
//		      protected void invokeApplication(){
//		    	  System.out.println("#{updateTrustRelationshipAction.save} resulted in " + invokeMethod("#{updateTrustRelationshipAction.save}"));
//		      }
			           
        }.run();
        
        logoutUserFacesRequest();
    }
	
	

}
