package org.gluu.oxtrust.api.test;

public class OxTrustApiClient {

	public static void main(String[] args) throws Exception {
		UmaScopeRepository umaScopeRepository = new UmaScopeRepository();
		UmaResourceRepository umaResourceRepository = new UmaResourceRepository();
		umaScopeRepository.testAll();
		umaResourceRepository.testAll();
	}
}
