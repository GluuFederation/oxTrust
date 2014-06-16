package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.mail.MessagingException;

import lombok.Data;

import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.ldap.service.LinktrackService;
import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.ldap.service.RegistrationLinkService;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.model.GluuOrganization;
import org.gluu.oxtrust.model.OxLink;
import org.gluu.oxtrust.util.MailUtils;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Credentials;
import org.xdi.oxauth.model.util.StringUtils;
import org.xdi.util.StringHelper;

/**
 * Action class for listing of registration links
 * @author Oleksiy Tataryn Date: 23.03.2014
 * 
 */
@Scope(ScopeType.CONVERSATION)
@Name("registrationLinkInventoryAction")
@Restrict("#{identity.loggedIn}")
public @Data class RegistrationLinkInventoryAction implements Serializable {

	private static final long serialVersionUID = 3337947202256952024L;

	@In
	private RegistrationLinkService registrationLinkService;
	
	@In
	private OrganizationService organizationService;
	
	@In 
	private Credentials credentials;

	@Logger
	private Log log;

	private String from;
	private String subject = "Your custom registration link";
	private String to;
	private List<String> toList = new ArrayList<String>();
	private String body = "Innovation";
	
	private Boolean linktrackEnabled;
	
	private String currentLinkGuid;
	private String currentRecepient;
	
	private List<OxLink> registrationLinks;

	@In
	private LinktrackService linktrackService;

	private String initalBodyFormat = "Your registration link is <a href=\"%1$s\">%1$s</a>.";

	 final static Pattern rfc2822 = Pattern
            .compile ("[a-z0-9!#$%&\'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&\'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?");

	@Restrict("#{s:hasPermission('registrationLinks', 'access')}")
	public String start() {
		if (registrationLinks != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		linktrackEnabled = organizationService.getOrganization().getLinktrackEnabled();
		if(linktrackEnabled == null){
			linktrackEnabled = false;
		}
		return search();
	}

	@Restrict("#{s:hasPermission('registrationLinks', 'access')}")
	public String search() {
		try {
			this.registrationLinks = registrationLinkService.getLinks(credentials.getUsername());
		} catch (Exception ex) {
			log.error("Failed to find registration links", ex);
			return OxTrustConstants.RESULT_FAILURE;
		}
		return OxTrustConstants.RESULT_SUCCESS;
	}
	
	public String updateLink(){
		OxLink currentLink = registrationLinkService.getLinkByGuid(currentLinkGuid);
		GluuOrganization organization = organizationService.getOrganization();
		if(organization.getLinktrackEnabled() != null && organization.getLinktrackEnabled()){
			
			String linktrackLink = linktrackService.newLink(organization.getLinktrackLogin(), 
					organization.getLinktrackPassword(), 
					registrationLinkService.getRegistrationLink(currentLink));
			currentLink.setLinktrackLink(linktrackLink);
		}
		registrationLinkService.update(currentLink);
		search();
		return OxTrustConstants.RESULT_SUCCESS;
	}
	
	public String addTo() {
		String[] recipients = StringHelper.split(to, ",");
		for(String recipient: recipients){
			if(rfc2822.matcher(recipient).matches() && ! toList.contains(recipient)){
				toList.add(recipient);
			}	
		}
		return OxTrustConstants.RESULT_SUCCESS;

	}
	
	public String removeRecepient(){
		
		if(toList.contains(currentRecepient)){
			toList.remove(currentRecepient);
		}
		
		return OxTrustConstants.RESULT_SUCCESS;
	}
	
	public String initShareForm(){
		OxLink currentLink = registrationLinkService.getLinkByGuid(currentLinkGuid);
		String linktrackLink = currentLink.getLinktrackLink();
		if(linktrackLink != null && ! linktrackLink.isEmpty()){
			body=String.format(initalBodyFormat, linktrackLink);
		}else{
			body=String.format(initalBodyFormat, registrationLinkService.getRegistrationLink(currentLink));
		}
		return OxTrustConstants.RESULT_SUCCESS;
	}
	
	public String sendShare(){
		if(toList.isEmpty()){
			return OxTrustConstants.RESULT_VALIDATION_ERROR;
		}
		GluuAppliance appliance = ApplianceService.instance().getAppliance();
		MailUtils mail = new MailUtils(appliance.getSmtpHost(), appliance.getSmtpPort(), appliance.isRequiresSsl(),
				appliance.isRequiresAuthentication(), appliance.getSmtpUserName(), appliance.getSmtpPasswordStr());
		try {
			mail.sendMail(from + " <" + appliance.getSmtpFromEmailAddress() + ">", StringHelper.buildColonDelimitedString(toList.toArray(new String[0])),
					subject, body);
		} catch (MessagingException e) {
			return OxTrustConstants.RESULT_FAILURE;
		}
		return OxTrustConstants.RESULT_SUCCESS;
	}
	
	public String clearTo(){
		toList = new ArrayList<String>();
		return OxTrustConstants.RESULT_SUCCESS;
	}	
	
	public String delete(){
		OxLink currentLink = registrationLinkService.getLinkByGuid(currentLinkGuid);
		registrationLinkService.removeLink(currentLink);
		search();
		return OxTrustConstants.RESULT_SUCCESS;
	}
}
