package org.gluu.oxtrust.util.test;

import java.util.List;

import org.gluu.oxtrust.ldap.service.IGroupService;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.model.GluuGroupVisibility;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Scope(ScopeType.STATELESS)
@Name("groupService")
@AutoCreate
public class MockGroupService implements IGroupService{

	@Override
	public void addGroup(GluuGroup group) throws Exception {
		throw new IllegalStateException("Not Implemented");
	}

	@Override
	public void removeGroup(GluuGroup group) {
		throw new IllegalStateException("Not Implemented");
	}

	@Override
	public List<GluuGroup> getAllGroups() {
		throw new IllegalStateException("Not Implemented");
	}

	@Override
	public boolean isMemberOrOwner(String groupDN, String personDN) {
		throw new IllegalStateException("Not Implemented");
	}

	@Override
	public GluuGroup getGroupByInum(String inum) {
		throw new IllegalStateException("Not Implemented");
	}

	@Override
	public String getDnForGroup(String inum) {
		if("group_value".equals(inum)){
			return "Mocked DN";		
		}else if("group_value1".equals(inum)){
			return "Mocked DN1";
		}else{
			return null;
		}
	}

	@Override
	public void updateGroup(GluuGroup group) throws Exception {
		throw new IllegalStateException("Not Implemented");
	}

	@Override
	public int countGroups() {
		throw new IllegalStateException("Not Implemented");
	}

	@Override
	public String generateInumForNewGroup() throws Exception {
		throw new IllegalStateException("Not Implemented");
	}

	@Override
	public String generateInameForNewGroup(String name) throws Exception {
		throw new IllegalStateException("Not Implemented");
	}

	@Override
	public List<GluuGroup> searchGroups(String pattern, int sizeLimit) throws Exception {
		throw new IllegalStateException("Not Implemented");
	}

	@Override
	public GluuGroupVisibility[] getVisibilityTypes() throws Exception {
		throw new IllegalStateException("Not Implemented");
	}

	@Override
	public List<GluuGroup> getAllGroupsList() throws Exception {
		throw new IllegalStateException("Not Implemented");
	}

	@Override
	public GluuGroup getGroupByDn(String Dn) {
		throw new IllegalStateException("Not Implemented");
	}

	@Override
	public GluuGroup getGroupByIname(String iname) throws Exception {
		throw new IllegalStateException("Not Implemented");
	}

	@Override
	public GluuGroup getGroupByDisplayName(String DisplayName) throws Exception {
		throw new IllegalStateException("Not Implemented");
	}

}
