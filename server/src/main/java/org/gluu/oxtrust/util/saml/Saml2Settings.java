package org.gluu.oxtrust.util.saml;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.X509Certificate;

import com.onelogin.saml2.model.Organization;

public class Saml2Settings extends com.onelogin.saml2.settings.Saml2Settings {
	
	private Organization organizationSub;
	private String spEntityIdSub;
	private String spNameIDFormatSub;
	
	public void setSpSingleLogoutServiceUrlSub(URL spSingleLogoutServiceUrl){
		setSpSingleLogoutServiceUrl(spSingleLogoutServiceUrl);
	}
	
	public void setSpAssertionConsumerServiceUrlSub(URL spAssertionConsumerServiceUrl) {
		setSpAssertionConsumerServiceUrl(spAssertionConsumerServiceUrl);
	}
	
	public void setSpX509certSub(X509Certificate spX509cert) {
		setSpX509cert(spX509cert);
	}
	
	public void setOrganizationSub(Organization organization) {
		setOrganization(organization);
	}
	
	public Organization getOrganizationSub( ) {
		return getOrganization();
	}

	public String getSpEntityIdSub() {
		return getSpEntityId();
	}

	public void setSpEntityIdSub(String spEntityIdSub) {
		setSpEntityId(spEntityIdSub);
	}
	
	public String getSpNameIDFormatSub() {
		return getSpNameIDFormat();
	}

	public void setSpNameIDFormatSub(String spNameIDFormatSub) {
		setSpNameIDFormat(spNameIDFormatSub);
	}


}
