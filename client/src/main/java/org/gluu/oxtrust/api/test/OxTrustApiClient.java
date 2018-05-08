package org.gluu.oxtrust.api.test;

public class OxTrustApiClient {

	public static void main(String[] args) throws Exception {
		GroupRepositoryImpl groupRepository = new GroupRepositoryImpl();
		 PeopleRepositoryImpl peopleRepositoryImpl = new PeopleRepositoryImpl();
		 OxAuthClientRepositoryImpl oxAuthClientRepositoryImpl = new
		 OxAuthClientRepositoryImpl();
		 GluuScopeRepositoryImpl scopeRepositoryImpl = new GluuScopeRepositoryImpl();
		 SectorIdentifierRepositoryImpl sectorIdentifierRepositoryImpl = new
		 SectorIdentifierRepositoryImpl();
		String groupInum = "@!C378.2535.607C.A39E!0001!1CF4.0960!0003!24B0.2A18";
		String memberInum = "@!C378.2535.607C.A39E!0001!1CF4.0960!0000!90B0.B8DE.92FA.5E9B";
		groupRepository.getGroupMembers(groupInum);
		groupRepository.addGroupMember(groupInum, memberInum);
		groupRepository.getGroupMembers(groupInum);
		groupRepository.deleteGroupMember(groupInum, memberInum);
		 peopleRepositoryImpl.testAll();
		 oxAuthClientRepositoryImpl.testAll();
		 scopeRepositoryImpl.testAll();
		 sectorIdentifierRepositoryImpl.testAll();
	}
}
