package org.gluu.oxtrust.api.test;

public class OxTrustApiClient {

	public static void main(String[] args) throws Exception {
		AttributeRepository attributeRepository = new AttributeRepository();
		UmaScopeRepository umaScopeRepository = new UmaScopeRepository();
		UmaResourceRepository umaResourceRepository = new UmaResourceRepository();
		OxAuthClientRepositoryImpl oxAuthClientRepositoryImpl = new OxAuthClientRepositoryImpl();
		SectorIdentifierRepositoryImpl sectorIdentifierRepositoryImpl = new SectorIdentifierRepositoryImpl();
		oxAuthClientRepositoryImpl.testAll();
		sectorIdentifierRepositoryImpl.testAll();
		attributeRepository.testAll();
		umaScopeRepository.testAll();
		umaResourceRepository.testAll();
	}
}
