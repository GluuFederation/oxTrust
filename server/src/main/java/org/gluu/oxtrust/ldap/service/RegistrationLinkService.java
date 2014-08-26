package org.gluu.oxtrust.ldap.service;

import java.util.ArrayList;
import java.util.List;
import org.gluu.oxtrust.config.OxTrustConfiguration;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.OrganizationalUnit;
import org.gluu.oxtrust.model.OxLink;
import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.jboss.seam.web.ServletContexts;
import org.xdi.util.StringHelper;

/**
 * Provides operations with registration links
 * @author Oleksiy Tataryn Date: 23.03.2014
 * 
 */
@Scope(ScopeType.STATELESS)
@Name("registrationLinkService")
@AutoCreate
public class RegistrationLinkService {

	@Logger
	private Log log;

	@In
	LdapEntryManager ldapEntryManager;
	
	/**
	 * @param username
	 * @return
	 */
	public List<OxLink> getLinks(String username) {
		log.debug("Seaching for oxLinks owned by "+ username);
		OrganizationalUnit ou = new OrganizationalUnit();
		ou.setDn(getDnForLink(null));
		if(ldapEntryManager.contains(ou)){
			OxLink link= new OxLink();
			link.setDn(getDnForLink(null));
			link.setLinkCreator(username);
			return ldapEntryManager.findEntries(link);
		}else{
			return new ArrayList<OxLink>();
		}
	}

	/**
	 * @param link
	 */
	public void addLink(OxLink link) {
		OrganizationalUnit ou= new OrganizationalUnit();
		ou.setDn(getDnForLink(null));
		if(! ldapEntryManager.contains(ou)){
			ldapEntryManager.persist(ou);
		}
		
		link.setDn(getDnForLink(link.getGuid()));
		ldapEntryManager.persist(link);
	}

	public String getDnForLink(String guid) {
		String orgDN = OrganizationService.instance().getDnForOrganization();
		if (StringHelper.isEmpty(guid)) {
			return String.format("ou=links,%s", orgDN);
		}

		return String.format("oxGuid=%s,ou=links,%s", guid, orgDN);
	}

	/**
	 * @param invitationGuid
	 */
	public OxLink getLinkByGuid(String invitationGuid) {
		if(invitationGuid != null){
			log.trace("Seaching for oxLinks by guid "+ invitationGuid);
			OrganizationalUnit ou = new OrganizationalUnit();
			ou.setDn(getDnForLink(null));
			if(ldapEntryManager.contains(ou)){
				OxLink link= new OxLink();
				link.setDn(getDnForLink(null));
				link.setGuid(invitationGuid);
				if(ldapEntryManager.contains(link)){
					return (OxLink) ldapEntryManager.findEntries(link).get(0);
				}	
			}
		}
		return null;
	}

	/**
	 * @param invitationLink
	 * @param inum
	 */
	public synchronized void  addPendingUser(OxLink invitationLink, String inum) {
		if(invitationLink == null){
			return;
		}
		List<String> newPending = new ArrayList<String>();
		newPending.add(inum);
 		List<String> pending = invitationLink.getLinkPending();
		if(pending != null){
			newPending.addAll(pending);
		}
		invitationLink.setLinkPending(newPending);
		ldapEntryManager.merge(invitationLink);
	}

	/**
	 * @return
	 */
	public List<OxLink> getAllLinks() {
		OrganizationalUnit ou = new OrganizationalUnit();
		ou.setDn(getDnForLink(null));
		if(ldapEntryManager.contains(ou)){
			OxLink link= new OxLink();
			link.setDn(getDnForLink(null));
			return ldapEntryManager.findEntries(link);
		}
		return null;
	}

	/**
	 * @param link
	 */
	public void update(OxLink link) {
		
		ldapEntryManager.merge(link);
		
	}

	public String getRegistrationLink(OxLink currentLink) {
		String contextPath = ServletContexts.instance().getRequest().getContextPath();
		String registrationLink = OxTrustConfiguration.instance()
			.getApplicationConfiguration().getApplianceUrl()
			+ contextPath + "/register/" + currentLink.getGuid();
		return registrationLink;
	}

	/**
	 * @param link
	 */
	public void removeLink(OxLink link) {
		OrganizationalUnit ou = new OrganizationalUnit();
		ou.setDn(getDnForLink(null));
		if(ldapEntryManager.contains(ou)){
			List<String> pendingUsers = link.getLinkPending();
			if(pendingUsers != null && ! pendingUsers.isEmpty()){
				for(String userDN: pendingUsers){
					GluuCustomPerson pendingPerson = PersonService.instance().getPersonByInum(userDN);
					if(pendingPerson != null){
						PersonService.instance().removePerson(pendingPerson);
					}
				}
			}
			ldapEntryManager.remove(link);
		}
	}

}
