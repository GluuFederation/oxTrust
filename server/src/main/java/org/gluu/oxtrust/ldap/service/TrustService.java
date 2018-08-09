/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.model.RenderParameters;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuMetadataSourceType;
import org.gluu.oxtrust.model.GluuSAMLTrustRelationship;
import org.gluu.oxtrust.model.OrganizationalUnit;
import org.gluu.oxtrust.service.render.RenderService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.persist.PersistenceEntryManager;
import org.gluu.persist.model.base.InumEntry;
import org.gluu.search.filter.Filter;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.model.GluuAttribute;
import org.xdi.model.GluuStatus;
import org.xdi.model.TrustContact;
import org.xdi.service.MailService;
import org.xdi.service.XmlService;
import org.xdi.util.INumGenerator;
import org.xdi.util.StringHelper;

/**
 * Provides operations with trust relationships
 * 
 * @author Pankaj
 * @author Yuriy Movchan Date: 11.05.2010
 * 
 */
@Stateless
@Named("trustService")
public class TrustService implements Serializable {

	private static final long serialVersionUID = -8128546040230316737L;

	@Inject
	private Logger log;

    @Inject
    private FacesMessages facesMessages;

	@Inject
	private PersistenceEntryManager ldapEntryManager;

	@Inject
	private AttributeService attributeService;

	@Inject
	private ApplianceService applianceService;

	@Inject
	private XmlService xmlService;

	@Inject
	private AppConfiguration appConfiguration;

    @Inject
	private MailService mailService;

    @Inject
    private RenderParameters rendererParameters;

    @Inject
    private RenderService renderService;

	public static final String GENERATED_SSL_ARTIFACTS_DIR = "ssl";

	public void addTrustRelationship(GluuSAMLTrustRelationship trustRelationship) {
		log.info("Creating TR " + trustRelationship.getInum());
		String[] clusterMembers = appConfiguration.getClusteredInums();
		String applianceInum = appConfiguration.getApplianceInum();
		if (clusterMembers == null || clusterMembers.length == 0) {
			log.debug("there is no cluster configuration. Assuming standalone appliance.");
			clusterMembers = new String[] { applianceInum };
		}

		String dn = trustRelationship.getDn();
		for (String clusterMember : clusterMembers) {
			String clusteredDN = StringHelper.replaceLast(dn, applianceInum, clusterMember);
			trustRelationship.setDn(clusteredDN);
			GluuSAMLTrustRelationship tr = new GluuSAMLTrustRelationship();
			tr.setDn(trustRelationship.getDn());
			if(! containsTrustRelationship(tr)){
				log.debug("Adding TR" + clusteredDN);
				OrganizationalUnit ou = new OrganizationalUnit();
				ou.setDn(getDnForTrustRelationShip(null));
		        if(! ldapEntryManager.contains(ou)){
		            ldapEntryManager.persist(ou);
		        }
				ldapEntryManager.persist(trustRelationship);
			}else{
				ldapEntryManager.merge(trustRelationship);
			}
		}
		trustRelationship.setDn(dn);
	}

	public void updateTrustRelationship(GluuSAMLTrustRelationship trustRelationship) {
		log.debug("Updating TR " + trustRelationship.getInum());
		String[] clusterMembers = appConfiguration.getClusteredInums();
		String applianceInum = appConfiguration.getApplianceInum();
		if (clusterMembers == null || clusterMembers.length == 0) {
			log.debug("there is no cluster configuration. Assuming standalone appliance.");
			clusterMembers = new String[] { applianceInum };
		}
		String dn = trustRelationship.getDn();
		for (String clusterMember : clusterMembers) {
			String clusteredDN = StringHelper.replaceLast(dn, applianceInum, clusterMember);
			trustRelationship.setDn(clusteredDN);
			GluuSAMLTrustRelationship tr = new GluuSAMLTrustRelationship();
			tr.setDn(trustRelationship.getDn());
			if(containsTrustRelationship(tr)){
				log.trace("Updating TR" + clusteredDN);
				ldapEntryManager.merge(trustRelationship);
			}else{
			    OrganizationalUnit ou = new OrganizationalUnit();
                ou.setDn(getDnForTrustRelationShip(null));
                if(! ldapEntryManager.contains(ou)){
                    ldapEntryManager.persist(ou);
                }
				ldapEntryManager.persist(trustRelationship);
			}
		}
		trustRelationship.setDn(dn);
	}

	public void removeTrustRelationship(GluuSAMLTrustRelationship trustRelationship) {
		log.info("Removing TR " + trustRelationship.getInum());
		String[] clusterMembers = appConfiguration.getClusteredInums();
		String applianceInum = appConfiguration.getApplianceInum();
		if (clusterMembers == null || clusterMembers.length == 0) {
			log.debug("there is no cluster configuration. Assuming standalone appliance.");
			clusterMembers = new String[] { applianceInum };
		}
		String dn = trustRelationship.getDn();
		for (String clusterMember : clusterMembers) {
			String clusteredDN = StringHelper.replaceLast(dn, applianceInum, clusterMember);
			trustRelationship.setDn(clusteredDN);
			GluuSAMLTrustRelationship tr = new GluuSAMLTrustRelationship();
			tr.setDn(trustRelationship.getDn());
			if(containsTrustRelationship(tr)){
				log.debug("Removing TR" + clusteredDN);
				ldapEntryManager.remove(trustRelationship);
			}
		}
		trustRelationship.setDn(dn);

	}

	public GluuSAMLTrustRelationship getRelationshipByInum(String inum) {
		return ldapEntryManager.find(GluuSAMLTrustRelationship.class, getDnForTrustRelationShip(inum));
	}

	public GluuSAMLTrustRelationship getRelationshipByDn(String dn) {
		if (StringHelper.isNotEmpty(dn)) {
			return ldapEntryManager.find(GluuSAMLTrustRelationship.class, dn);
		}
		return null;
	}

	/**
	 * This is a LDAP operation as LDAP and IDP will always be in sync. We can
	 * just call LDAP to fetch all Trust Relationships.
	 */
	public List<GluuSAMLTrustRelationship> getAllTrustRelationships() {
		return ldapEntryManager.findEntries(getDnForTrustRelationShip(null), GluuSAMLTrustRelationship.class, null);
	}

	public List<GluuSAMLTrustRelationship> getAllActiveTrustRelationships() {
		GluuSAMLTrustRelationship trustRelationship = new GluuSAMLTrustRelationship();
		trustRelationship.setBaseDn(getDnForTrustRelationShip(null));
		trustRelationship.setStatus(GluuStatus.ACTIVE);

		return ldapEntryManager.findEntries(trustRelationship);
	}

	public List<GluuSAMLTrustRelationship> getAllFederations() {
		List<GluuSAMLTrustRelationship> result = new ArrayList<GluuSAMLTrustRelationship>();
		for (GluuSAMLTrustRelationship trust : getAllActiveTrustRelationships()) {
			if (trust.isFederation()) {
				result.add(trust);
			}
		}

		return result;
	}

	public List<GluuSAMLTrustRelationship> getAllOtherFederations(String inum) {
		List<GluuSAMLTrustRelationship> result = getAllFederations();
		result.remove(getRelationshipByInum(inum));
		return result;
	}

	/**
	 * Check if LDAP server contains trust relationship with specified
	 * attributes
	 * 
	 * @return True if trust relationship with specified attributes exist
	 */
	public boolean containsTrustRelationship(GluuSAMLTrustRelationship trustRelationship) {
		return ldapEntryManager.contains(trustRelationship);
	}

	/**
	 * Generate new inum for trust relationship
	 * 
	 * @return New inum for trust relationship
	 */
	public String generateInumForNewTrustRelationship() {
		InumEntry entry = new InumEntry();
		String newDn = appConfiguration.getBaseDN();
		entry.setDn(newDn);
		String newInum;
		do {
			newInum = generateInumForNewTrustRelationshipImpl();
			entry.setInum(newInum);
		} while (ldapEntryManager.contains(entry));

		return newInum;
	}

	/**
	 * Generate new inum for trust relationship
	 * 
	 * @return New inum for trust relationship
	 */
	private String generateInumForNewTrustRelationshipImpl() {
		return getApplianceInum() + OxTrustConstants.inumDelimiter + "0006" + OxTrustConstants.inumDelimiter + INumGenerator.generate(2);
	}

	/**
	 * Return current organization inum
	 * 
	 * @return Current organization inum
	 */
	private String getApplianceInum() {
		return appConfiguration.getApplianceInum();
	}

	/**
	 * Get all metadata source types
	 * 
	 * @return Array of metadata source types
	 */
	public GluuMetadataSourceType[] getMetadataSourceTypes() {
		return GluuMetadataSourceType.values();
	}

	/**
	 * Build DN string for trust relationship
	 * 
	 * @param inum
	 *            Inum
	 * @return DN string for specified trust relationship or DN for trust
	 *         relationships branch if inum is null
	 */
	public String getDnForTrustRelationShip(String inum) {
		String applianceDN = applianceService.getDnForAppliance();
		if (StringHelper.isEmpty(inum)) {
			return String.format("ou=trustRelationships,%s", applianceDN);
		}

		return String.format("inum=%s,ou=trustRelationships,%s", inum, applianceDN);
	}

	public void updateReleasedAttributes(GluuSAMLTrustRelationship trustRelationship) {
		List<String> releasedAttributes = new ArrayList<String>();

		String mailMsgPlain = "";
		String mailMsgHtml = "";
		for (GluuCustomAttribute customAttribute : trustRelationship.getReleasedCustomAttributes()) {
			if (customAttribute.isNew()) {
				rendererParameters.setParameter("attributeName", customAttribute.getName());
				rendererParameters.setParameter("attributeDisplayName", customAttribute.getMetadata().getDisplayName());
				rendererParameters.setParameter("attributeValue", customAttribute.getValue());

				mailMsgPlain += facesMessages.evalResourceAsString("#{msg['mail.trust.released.attribute.plain']}");
				mailMsgHtml += facesMessages.evalResourceAsString("#{msg['mail.trust.released.attribute.html']}");
				rendererParameters.reset();

				customAttribute.setNew(false);
			}
			releasedAttributes.add(customAttribute.getMetadata().getDn());
		}

                // send email notification
		if (!StringUtils.isEmpty(mailMsgPlain)) {
			try {
				GluuAppliance appliance = applianceService.getAppliance();
                                
                if (appliance.getContactEmail() == null || appliance.getContactEmail().isEmpty()) 
                    log.warn("Failed to send the 'Attributes released' notification email: unconfigured contact email");
                else if (appliance.getSmtpConfiguration() == null || StringHelper.isEmpty(appliance.getSmtpConfiguration().getHost())) 
                    log.warn("Failed to send the 'Attributes released' notification email: unconfigured SMTP server");
                else {
                    String subj = facesMessages.evalResourceAsString("#{msg['mail.trust.released.subject']}");

                    rendererParameters.setParameter("trustRelationshipName", trustRelationship.getDisplayName());
                    rendererParameters.setParameter("trustRelationshipInum", trustRelationship.getInum());

                    String preMsgPlain = facesMessages.evalResourceAsString("#{msg['mail.trust.released.name.plain']}");
                    String preMsgHtml = facesMessages.evalResourceAsString("#{msg['mail.trust.released.name.html']}");

//            		rendererParameters.setParameter("mail_body", preMsgHtml + mailMsgHtml);
//            		String mailHtml = renderService.renderView("/WEB-INF/mail/trust_relationship.xhtml");
                    
    				boolean result = mailService.sendMail(appliance.getContactEmail(), null, subj, preMsgPlain + mailMsgPlain, preMsgHtml + mailMsgHtml);
    				
    				if (!result) {
    					log.error("Failed to send the notification email");
    				}
                }
			} catch (Exception ex) {
				log.error("Failed to send the notification email: ", ex);
			}
		}
                
		if (!releasedAttributes.isEmpty()) {
			trustRelationship.setReleasedAttributes(releasedAttributes);
		} else {
			trustRelationship.setReleasedAttributes(null);
		}
	}

	public List<TrustContact> getContacts(GluuSAMLTrustRelationship trustRelationship) {
		List<String> gluuTrustContacts = trustRelationship.getGluuTrustContact();
		List<TrustContact> contacts = new ArrayList<TrustContact>();
		if (gluuTrustContacts != null) {
			for (String contact : gluuTrustContacts) {
				contacts.add(xmlService.getTrustContactFromXML(contact));
			}
		}
		return contacts;
	}

	public void saveContacts(GluuSAMLTrustRelationship trustRelationship, List<TrustContact> contacts) {
		if (contacts != null && !contacts.isEmpty()) {
			List<String> gluuTrustContacts = new ArrayList<String>();
			for (TrustContact contact : contacts) {
				gluuTrustContacts.add(xmlService.getXMLFromTrustContact(contact));
			}
			trustRelationship.setGluuTrustContact(gluuTrustContacts);
		}
	}

	// public List<DeconstructedTrustRelationship>
	// getDeconstruction(GluuSAMLTrustRelationship trustRelationship) {
	// List<String> gluuTrustDeconstruction =
	// trustRelationship.getGluuTrustDeconstruction();
	// List<DeconstructedTrustRelationship> deconstruction = new
	// ArrayList<DeconstructedTrustRelationship>();
	// if(gluuTrustDeconstruction != null){
	// for (String deconstructedTR : gluuTrustDeconstruction){
	// deconstruction.add(xmlService.getDeconstructedTrustRelationshipFromXML(deconstructedTR));
	// }
	// }
	// return deconstruction;
	// }
	//
	// public void saveDeconstruction(GluuSAMLTrustRelationship
	// trustRelationship,
	// List<DeconstructedTrustRelationship> deconstruction) {
	// List<String> gluuTrustDeconstruction = new ArrayList<String>();
	// for (DeconstructedTrustRelationship deconstructedTR : deconstruction){
	// gluuTrustDeconstruction.add(xmlService.getXMLFromDeconstructedTrustRelationship(deconstructedTR));
	// }
	// trustRelationship.setGluuTrustDeconstruction(gluuTrustDeconstruction);
	// }

	public List<GluuSAMLTrustRelationship> getDeconstructedTrustRelationships(GluuSAMLTrustRelationship trustRelationship) {
		List<GluuSAMLTrustRelationship> result = new ArrayList<GluuSAMLTrustRelationship>();
		for (GluuSAMLTrustRelationship trust : getAllTrustRelationships()) {
			if (trustRelationship.equals(getTrustContainerFederation(trust))) {
				result.add(trust);
			}
		}
		return result;
	}

	public GluuSAMLTrustRelationship getTrustByUnpunctuatedInum(String unpunctuated) {
		for (GluuSAMLTrustRelationship trust : getAllTrustRelationships()) {
			if (StringHelper.removePunctuation(trust.getInum()).equals(unpunctuated)) {
				return trust;
			}
		}
		return null;
	}
        
        public GluuSAMLTrustRelationship getTrustContainerFederation(GluuSAMLTrustRelationship trustRelationship) {
            return getRelationshipByDn(trustRelationship.getGluuContainerFederation());
        }
	
	public List<GluuSAMLTrustRelationship> searchSAMLTrustRelationships(String pattern, int sizeLimit) {
		String[] targetArray = new String[] { pattern };
		Filter displayNameFilter = Filter.createSubstringFilter(OxTrustConstants.displayName, null, targetArray, null);
		Filter descriptionFilter = Filter.createSubstringFilter(OxTrustConstants.description, null, targetArray, null);
		Filter inameFilter = Filter.createSubstringFilter(OxTrustConstants.iname, null, targetArray, null);
		Filter inumFilter = Filter.createSubstringFilter(OxTrustConstants.inum, null, targetArray, null);
		Filter searchFilter = Filter.createORFilter(displayNameFilter, descriptionFilter, inameFilter, inumFilter);

		List<GluuSAMLTrustRelationship> result = ldapEntryManager.findEntries(getDnForTrustRelationShip(null), GluuSAMLTrustRelationship.class, searchFilter, sizeLimit);

		return result;
	}
	
	public List<GluuSAMLTrustRelationship> getAllSAMLTrustRelationships(int sizeLimit) {		
			return ldapEntryManager.findEntries(getDnForTrustRelationShip(null), GluuSAMLTrustRelationship.class, null, sizeLimit);
	}

	/**
	 * Remove attribute
	 * 
	 * @param attribute
	 *            Attribute
	 */
	public boolean removeAttribute(GluuAttribute attribute) {
		log.info("Attribute removal started");

		log.trace("Removing attribute from trustRelationships");
		List<GluuSAMLTrustRelationship> trustRelationships = getAllTrustRelationships();
		log.trace(String.format("Iterating '%d' trustRelationships", trustRelationships.size()));
		for (GluuSAMLTrustRelationship trustRelationship : trustRelationships) {
			log.trace("Analyzing '%s'.", trustRelationship.getDisplayName());
			List<String> customAttrs = trustRelationship.getReleasedAttributes();
			if (customAttrs != null) {
				for (String attrDN : customAttrs) {
					log.trace("'%s' has custom attribute '%s'", trustRelationship.getDisplayName(), attrDN);
					if (attrDN.equals(attribute.getDn())) {
						log.trace("'%s' matches '%s'.  deleting it.", attrDN, attribute.getDn());
						List<String> updatedAttrs = new ArrayList<String>();
						updatedAttrs.addAll(customAttrs);
						updatedAttrs.remove(attrDN);
						if (updatedAttrs.size() == 0) {
							trustRelationship.setReleasedAttributes(null);
						} else {
							trustRelationship.setReleasedAttributes(updatedAttrs);
						}

						updateTrustRelationship(trustRelationship);
						break;
					}
				}
			}
		}
		
		attributeService.removeAttribute(attribute);

		return true;
	}

}
