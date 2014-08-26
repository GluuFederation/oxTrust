package org.gluu.oxtrust.ldap.service;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.faces.application.ViewHandler;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuOrganization;
import org.gluu.oxtrust.model.OxLink;
import org.gluu.oxtrust.model.RegistrationConfiguration;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.Expiration;
import org.jboss.seam.annotations.async.IntervalDuration;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.xdi.ldap.model.GluuStatus;

/**
 *
 * 
 * @author Oleksiy Tataryn
 * 
 */
@Name("registrationsExpirationService")
@Scope(ScopeType.APPLICATION)
@AutoCreate
public class RegistrationsExpirationService {
	
	@In
	RegistrationLinkService registrationLinkService;
	
	@In
	OrganizationService organizationService;
	
	@Asynchronous
	public void expireLinks(@Expiration Date when, @IntervalDuration Long interval){
		Date now = new Date();
		List<OxLink> links = registrationLinkService.getAllLinks();
		if(links != null && links.isEmpty()){
			for(OxLink link : links){
				if(link.getLinkExpirationDate() != null && now.after(link.getLinkExpirationDate())){
					registrationLinkService.removeLink(link);
				}
			}
		}
	}
	
	@Asynchronous
	public void expireUsers(@Expiration Date when, @IntervalDuration Long interval){
		Calendar now = Calendar.getInstance();
		
		GluuOrganization org = organizationService.getOrganization();
		RegistrationConfiguration config = org.getOxRegistrationConfiguration();
		if(config.isAccountsTimeLimited()){
			now.add(Calendar.MINUTE, Integer.parseInt(config.getAccountsExpirationPeriod()));
			List<GluuCustomPerson> expiredPeople = PersonService.instance().findPersonsForExpiration(now.getTime());
			if(expiredPeople != null && !expiredPeople.isEmpty()){
				for(GluuCustomPerson person: expiredPeople){
					person.setStatus(GluuStatus.EXPIRED);
					PersonService.instance().updatePerson(person);
				}
			}
		}

	}

	/**
	 * @return
	 */
	public int getDefaultLinksExpirationFrequency() {
		return (12*60);
	}

	/**
	 * @return
	 */
	public int getDefaultAccountsExpirationServiceFrequency() {
		return (24*60);
	}

	/**
	 * @return
	 */
	public int getDefaultAccountsExpirationPeriod() {
		return (30*24*60);
	}
	
	public static RegistrationsExpirationService instance() {
		return (RegistrationsExpirationService) Component.getInstance(RegistrationsExpirationService.class);
	}

}