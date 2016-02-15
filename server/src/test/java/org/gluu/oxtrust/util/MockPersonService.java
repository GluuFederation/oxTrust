package org.gluu.oxtrust.util;

import java.util.List;
import java.util.Map;

import org.gluu.oxtrust.ldap.service.IPersonService;
import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.User;
import org.gluu.site.ldap.exception.DuplicateEntryException;
import org.gluu.site.ldap.persistence.AttributeData;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.xdi.model.GluuAttribute;

@Scope(ScopeType.STATELESS)
@Name("personService")
@Install(precedence = Install.MOCK)
public class MockPersonService implements IPersonService {

	private GluuCustomPerson person;
	
	public GluuCustomPerson mockGetPerson() {
		return person;
	}

	@Override
	public void addCustomObjectClass(GluuCustomPerson person) {
		this.person = person;
	}

	public void addPerson(GluuCustomPerson person) throws DuplicateEntryException {
		throw new IllegalStateException("Not Implemented");
	}

	public void updatePerson(GluuCustomPerson person) {
		throw new IllegalStateException("Not Implemented");
	}

	public void removePerson(GluuCustomPerson person) {
		throw new IllegalStateException("Not Implemented");
	}

	public List<GluuCustomPerson> searchPersons(String pattern, int sizeLimit) {
		throw new IllegalStateException("Not Implemented");
	}

	public List<GluuCustomPerson> findPersons(GluuCustomPerson person, int sizeLimit) {
		throw new IllegalStateException("Not Implemented");
	}

	public List<GluuCustomPerson> searchPersons(String pattern, int sizeLimit, List<GluuCustomPerson> excludedPersons)
			throws Exception {
		throw new IllegalStateException("Not Implemented");
	}

	public List<GluuCustomPerson> findAllPersons(String[] returnAttributes) {
		throw new IllegalStateException("Not Implemented");
	}

	public List<GluuCustomPerson> findPersonsByUids(List<String> uids, String[] returnAttributes) throws Exception {
		throw new IllegalStateException("Not Implemented");
	}

	public GluuCustomPerson findPersonByDn(String dn, String... returnAttributes) {
		throw new IllegalStateException("Not Implemented");
	}

	public boolean containsPerson(GluuCustomPerson person) {
		throw new IllegalStateException("Not Implemented");
	}

	public boolean contains(String dn) {
		throw new IllegalStateException("Not Implemented");
	}

	public GluuCustomPerson getPersonByDn(String dn) {
		throw new IllegalStateException("Not Implemented");
	}

	public GluuCustomPerson getPersonByInum(String inum) {
		throw new IllegalStateException("Not Implemented");
	}

	public GluuCustomPerson getPersonByUid(String uid) {
		if("existing".equals(uid)){
			return new GluuCustomPerson();
		}else if("exception".equals(uid)){
			throw new IllegalStateException("requested Exception");
		}else{
			return null;
		}		
	}

	public int countPersons() {
		throw new IllegalStateException("Not Implemented");
	}

	public String generateInumForNewPerson() {
		throw new IllegalStateException("Not Implemented");
	}

	public String generateInameForNewPerson(String uid) {
		throw new IllegalStateException("Not Implemented");
	}

	public String getDnForPerson(String inum) {
		throw new IllegalStateException("Not Implemented");
	}

	public boolean authenticate(String userName, String password) {
		throw new IllegalStateException("Not Implemented");
	}

	public List<GluuCustomAttribute> getMandatoryAtributes() {
		throw new IllegalStateException("Not Implemented");
	}

	public String getPersonString(List<GluuCustomPerson> persons) throws Exception {
		throw new IllegalStateException("Not Implemented");
	}

	public List<GluuCustomPerson> createEntities(Map<String, List<AttributeData>> entriesAttributes) throws Exception {
		throw new IllegalStateException("Not Implemented");
	}

	public boolean isMemberOrOwner(String[] groupDNs, String personDN) throws Exception {
		throw new IllegalStateException("Not Implemented");
	}

	public GluuCustomPerson getPersonByEmail(String email) {
		throw new IllegalStateException("Not Implemented");
	}

	public GluuCustomPerson getPersonByAttribute(String attribute, String value) throws Exception {
		throw new IllegalStateException("Not Implemented");
	}

	public void removeAttribute(GluuAttribute attribute) {
		throw new IllegalStateException("Not Implemented");
	}

	public User getUserByUid(String uid) {
		throw new IllegalStateException("Not Implemented");
	}

	public List<GluuCustomPerson> getPersonsByAttribute(String attribute, String value) throws Exception {
		throw new IllegalStateException("Not Implemented");
	}

}
