package org.gluu.oxtrust.api.test;

public class OxTrustApiClient {

	public static void main(String[] args) throws Exception {
		UmaScopeRepository umaScopeRepository = new UmaScopeRepository();
		UmaResourceRepository umaResourceRepository = new UmaResourceRepository();
		OxAuthClientRepositoryImpl oxAuthClientRepositoryImpl = new OxAuthClientRepositoryImpl();
		SectorIdentifierRepositoryImpl sectorIdentifierRepositoryImpl = new SectorIdentifierRepositoryImpl();
		oxAuthClientRepositoryImpl.testAll();
		sectorIdentifierRepositoryImpl.testAll();
		umaScopeRepository.testAll();
		umaResourceRepository.testAll();
	}
}
