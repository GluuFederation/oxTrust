/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.model.RenderParameters;
import org.gluu.model.GluuAttribute;
import org.gluu.model.GluuStatus;
import org.gluu.model.TrustContact;
import org.gluu.oxtrust.model.GluuConfiguration;
import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuMetadataSourceType;
import org.gluu.oxtrust.model.GluuSAMLTrustRelationship;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.persist.PersistenceEntryManager;
import org.gluu.search.filter.Filter;
import org.gluu.service.MailService;
import org.gluu.service.XmlService;
import org.gluu.util.StringHelper;
import org.slf4j.Logger;

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
	private ConfigurationService configurationService;

	@Inject
	private OrganizationService organizationService;

	@Inject
	private XmlService xmlService;

	@Inject
	private MailService mailService;

	@Inject
	private RenderParameters rendererParameters;

	public static final String GENERATED_SSL_ARTIFACTS_DIR = "ssl";

	public void addTrustRelationship(GluuSAMLTrustRelationship trustRelationship) {
		log.debug("Adding TR: {}", trustRelationship.getInum());
		String dn = trustRelationship.getDn();

		if (!containsTrustRelationship(dn)) {
			log.debug("Adding TR: {}", dn);
			ldapEntryManager.persist(trustRelationship);
		} else {
			ldapEntryManager.merge(trustRelationship);
		}
	}

	public void updateTrustRelationship(GluuSAMLTrustRelationship trustRelationship) {
		String dn = trustRelationship.getDn();
		boolean containsTrustRelationship = trustExist(dn);
		if (containsTrustRelationship) {
			log.info("Updating TR: {}", dn);
			ldapEntryManager.merge(trustRelationship);
		} else {
			log.info("Adding TR: {}", dn);
			ldapEntryManager.persist(trustRelationship);
		}
	}

	public void removeTrustRelationship(GluuSAMLTrustRelationship trustRelationship) {
		log.info("Removing TR: {}", trustRelationship.getInum());
		String dn = trustRelationship.getDn();

		if (containsTrustRelationship(dn)) {
			log.debug("Removing TR: {}", dn);
			ldapEntryManager.remove(trustRelationship);
		}
	}

	public GluuSAMLTrustRelationship getRelationshipByInum(String inum) {
		try {
			return ldapEntryManager.find(GluuSAMLTrustRelationship.class, getDnForTrustRelationShip(inum));
		} catch (Exception e) {
			log.error(e.getMessage());
			return null;
		}

	}

	public GluuSAMLTrustRelationship getRelationshipByDn(String dn) {
		if (StringHelper.isNotEmpty(dn)) {
			try {
				return ldapEntryManager.find(GluuSAMLTrustRelationship.class, dn);
			} catch (Exception e) {
				log.info(e.getMessage());
			}

		}
		return null;
	}

	/**
	 * This is a LDAP operation as LDAP and IDP will always be in sync. We can just
	 * call LDAP to fetch all Trust Relationships.
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
	 * Check if LDAP server contains trust relationship with specified attributes
	 * 
	 * @return True if trust relationship with specified attributes exist
	 */
	public boolean containsTrustRelationship(String dn) {
		return ldapEntryManager.contains(dn, GluuSAMLTrustRelationship.class);
	}

	public boolean trustExist(String dn) {
		GluuSAMLTrustRelationship trust = null;
		try {
			trust = ldapEntryManager.find(GluuSAMLTrustRelationship.class, dn);
		} catch (Exception e) {
			trust = null;
		}
		return (trust != null) ? true : false;
	}

	/**
	 * Generate new inum for trust relationship
	 * 
	 * @return New inum for trust relationship
	 */
	public String generateInumForNewTrustRelationship() {
		String newDn = null;
		String newInum = null;
		do {
			newInum = generateInumForNewTrustRelationshipImpl();
			newDn = getDnForTrustRelationShip(newInum);
		} while (containsTrustRelationship(newDn));

		return newInum;
	}

	/**
	 * Generate new inum for trust relationship
	 * 
	 * @return New inum for trust relationship
	 */
	private String generateInumForNewTrustRelationshipImpl() {
		return UUID.randomUUID().toString();
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
		String organizationDN = organizationService.getDnForOrganization();
		if (StringHelper.isEmpty(inum)) {
			return String.format("ou=trustRelationships,%s", organizationDN);
		}
		return String.format("inum=%s,ou=trustRelationships,%s", inum, organizationDN);
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
				GluuConfiguration configuration = configurationService.getConfiguration();

				if (configuration.getContactEmail() == null || configuration.getContactEmail().isEmpty())
					log.warn("Failed to send the 'Attributes released' notification email: unconfigured contact email");
				else if (configuration.getSmtpConfiguration() == null
						|| StringHelper.isEmpty(configuration.getSmtpConfiguration().getHost()))
					log.warn("Failed to send the 'Attributes released' notification email: unconfigured SMTP server");
				else {
					String subj = facesMessages.evalResourceAsString("#{msg['mail.trust.released.subject']}");
					rendererParameters.setParameter("trustRelationshipName", trustRelationship.getDisplayName());
					rendererParameters.setParameter("trustRelationshipInum", trustRelationship.getInum());
					String preMsgPlain = facesMessages.evalResourceAsString("#{msg['mail.trust.released.name.plain']}");
					String preMsgHtml = facesMessages.evalResourceAsString("#{msg['mail.trust.released.name.html']}");
					boolean result = mailService.sendMail(configuration.getContactEmail(), null, subj,
							preMsgPlain + mailMsgPlain, preMsgHtml + mailMsgHtml);

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

	public List<GluuSAMLTrustRelationship> getDeconstructedTrustRelationships(
			GluuSAMLTrustRelationship trustRelationship) {
		List<GluuSAMLTrustRelationship> result = new ArrayList<GluuSAMLTrustRelationship>();
		for (GluuSAMLTrustRelationship trust : getAllTrustRelationships()) {
			if (trustRelationship.equals(getTrustContainerFederation(trust))) {
				result.add(trust);
			}
		}
		return result;
	}

	public List<GluuSAMLTrustRelationship> getChildTrusts(GluuSAMLTrustRelationship trustRelationship) {
		List<GluuSAMLTrustRelationship> all = getAllTrustRelationships();
		if (all != null && !all.isEmpty()) {
			return all.stream().filter(e -> !e.isFederation())
					.filter(e -> e.getGluuContainerFederation().equalsIgnoreCase(trustRelationship.getDn()))
					.collect(Collectors.toList());
		} else {
			return new ArrayList<GluuSAMLTrustRelationship>();
		}
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
		GluuSAMLTrustRelationship relationshipByDn = getRelationshipByDn(trustRelationship.getDn());
		return relationshipByDn;
	}

	public GluuSAMLTrustRelationship getTrustContainerFederation(String dn) {
		GluuSAMLTrustRelationship relationshipByDn = getRelationshipByDn(dn);
		return relationshipByDn;
	}

	public List<GluuSAMLTrustRelationship> searchSAMLTrustRelationships(String pattern, int sizeLimit) {
		String[] targetArray = new String[] { pattern };
		Filter displayNameFilter = Filter.createSubstringFilter(OxTrustConstants.displayName, null, targetArray, null);
		Filter descriptionFilter = Filter.createSubstringFilter(OxTrustConstants.description, null, targetArray, null);
		Filter inumFilter = Filter.createSubstringFilter(OxTrustConstants.inum, null, targetArray, null);
		Filter searchFilter = Filter.createORFilter(displayNameFilter, descriptionFilter, inumFilter);
		return ldapEntryManager.findEntries(getDnForTrustRelationShip(null), GluuSAMLTrustRelationship.class,
				searchFilter, sizeLimit);

	}

	public List<GluuSAMLTrustRelationship> getAllSAMLTrustRelationships(int sizeLimit) {
		return ldapEntryManager.findEntries(getDnForTrustRelationShip(null), GluuSAMLTrustRelationship.class, null,
				sizeLimit);
	}

	/**
	 * Remove attribute
	 * 
	 * @param attribute
	 *            Attribute
	 */
	public boolean removeAttribute(GluuAttribute attribute) {
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
