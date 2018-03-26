package org.gluu.oxtrust.api.test;

public class OxTrustApiClient {

	public static void main(String[] args) throws Exception {
		GroupRepositoryImpl groupRepositoryImpl = new GroupRepositoryImpl();
		groupRepositoryImpl.testAll();
	}
}
