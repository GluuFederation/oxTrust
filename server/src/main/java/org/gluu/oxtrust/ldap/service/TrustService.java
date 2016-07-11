/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuMetadataSourceType;
import org.gluu.oxtrust.model.GluuSAMLTrustRelationship;
import org.gluu.oxtrust.model.OrganizationalUnit;
import org.gluu.oxtrust.util.MailUtils;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.ldap.model.GluuStatus;
import org.xdi.ldap.model.InumEntry;
import org.xdi.model.GluuAttribute;
import org.xdi.model.GluuUserRole;
import org.xdi.model.TrustContact;
import org.xdi.service.XmlService;
import org.xdi.util.INumGenerator;
import org.xdi.util.StringHelper;

import com.unboundid.ldap.sdk.Filter;

/**
 * Provides operations with trust relationships
 * 
 * @author Pankaj
 * @author Yuriy Movchan Date: 11.05.2010
 * 
 */
@Scope(ScopeType.STATELESS)
@Name("trustService")
@AutoCreate
public class TrustService {

	@Logger
	private Log log;

	@In
	LdapEntryManager ldapEntryManager;

	@In
	private Shibboleth2ConfService shibboleth2ConfService;

	@In
	private AttributeService attributeService;

	@In
	private XmlService xmlService;
	
	public static final String GENERATED_SSL_ARTIFACTS_DIR = "ssl";

	@In(value = "#{oxTrustConfiguration.applicationConfiguration}")
	private ApplicationConfiguration applicationConfiguration;

	public void addTrustRelationship(GluuSAMLTrustRelationship trustRelationship) {
		log.info("Creating TR " + trustRelationship.getInum());
		String[] clusterMembers = applicationConfiguration.getClusteredInums();
		String applianceInum = applicationConfiguration.getApplianceInum();
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
		String[] clusterMembers = applicationConfiguration.getClusteredInums();
		String applianceInum = applicationConfiguration.getApplianceInum();
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
		String[] clusterMembers = applicationConfiguration.getClusteredInums();
		String applianceInum = applicationConfiguration.getApplianceInum();
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
		String newDn = applicationConfiguration.getBaseDN();
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
		return applicationConfiguration.getApplianceInum();
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
		String applianceDN = ApplianceService.instance().getDnForAppliance();
		if (StringHelper.isEmpty(inum)) {
			return String.format("ou=trustRelationships,%s", applianceDN);
		}

		return String.format("inum=%s,ou=trustRelationships,%s", inum, applianceDN);
	}

	/**
	 * Get trustService instance
	 * 
	 * @return TrustService instance
	 */
	public static TrustService instance() {
		return (TrustService) Component.getInstance(TrustService.class);
	}

	/**
	 * Adds Trust relationship for own shibboleth SP and restarts services after
	 * done.
	 * 
	 * @author �Oleksiy Tataryn�
	 */
	public void addGluuSP() {
	    String gluuSPInum = generateInumForNewTrustRelationship();
	    String metadataFN = shibboleth2ConfService.getSpNewMetadataFileName(gluuSPInum);
	    GluuSAMLTrustRelationship gluuSP = new GluuSAMLTrustRelationship();
		gluuSP.setInum(gluuSPInum);
		gluuSP.setDisplayName("gluu SP on appliance");
		gluuSP.setDescription("Trust Relationship for the SP");
		gluuSP.setSpMetaDataSourceType(GluuMetadataSourceType.FILE);
		gluuSP.setSpMetaDataFN(metadataFN);
		//TODO: 
		gluuSP.setEntityId(StringHelper.removePunctuation(gluuSP.getInum()));
		gluuSP.setUrl(applicationConfiguration.getApplianceUrl());

		String certificate = "";
		boolean result = false;
		try {
			certificate = FileUtils.readFileToString(new File(applicationConfiguration.getGluuSpCert())).replaceAll("-{5}.*?-{5}", "");
			shibboleth2ConfService.generateSpMetadataFile(gluuSP, certificate);
			result = shibboleth2ConfService.isCorrectSpMetadataFile(gluuSP.getSpMetaDataFN());

		} catch (IOException e) {
			log.error("Failed to gluu SP read certificate file.", e);
		}

		GluuAppliance appliance = null;
		if (result) {
			gluuSP.setStatus(GluuStatus.ACTIVE);
			String inum = gluuSP.getInum();
			String dn = getDnForTrustRelationShip(inum);

			gluuSP.setDn(dn);
			List<GluuCustomAttribute> customAttributes = new ArrayList<GluuCustomAttribute>();
			List<GluuAttribute> attributes = attributeService.getAllPersonAttributes(GluuUserRole.ADMIN);
			HashMap<String, GluuAttribute> attributesByDNs = attributeService.getAttributeMapByDNs(attributes);
			List<String> customAttributeDNs = new ArrayList<String>();
			List<String> attributeNames = new ArrayList<String>();

			for (String attributeName : applicationConfiguration.getGluuSpAttributes()) {
				GluuAttribute attribute = attributeService.getAttributeByName(attributeName, attributes);
				if (attribute != null) {
					customAttributeDNs.add(attribute.getDn());
				}
			}

			customAttributes.addAll(attributeService.getCustomAttributesByAttributeDNs(customAttributeDNs, attributesByDNs));
			gluuSP.setReleasedCustomAttributes(customAttributes);
			gluuSP.setReleasedAttributes(attributeNames);
			updateReleasedAttributes(gluuSP);
			addTrustRelationship(gluuSP);

			appliance = ApplianceService.instance().getAppliance();
			appliance.setGluuSPTR(gluuSP.getInum());
		}

		if (result) {
			ApplianceService.instance().updateAppliance(appliance);
			log.warn("gluuSP EntityID set to " + StringHelper.removePunctuation(gluuSP.getInum())
					+ ". shibboleth2 configuration should be updated.");
			// ApplianceService.instance().restartServices();
		} else {
			log.error("IDP configuration update failed. GluuSP was not generated.");
		}
	}

	public void updateReleasedAttributes(GluuSAMLTrustRelationship trustRelationship) {
		List<String> releasedAttributes = new ArrayList<String>();

		String mailMsg = "";
		for (GluuCustomAttribute customAttribute : trustRelationship.getReleasedCustomAttributes()) {
			if (customAttribute.isNew()) {
				mailMsg += "\nAttribute name: " + customAttribute.getName() + " Display name: "
						+ customAttribute.getMetadata().getDisplayName() + " Attribute value: " + customAttribute.getValue();
				customAttribute.setNew(false);
			}
			releasedAttributes.add(customAttribute.getMetadata().getDn());
		}

		if (!StringUtils.isEmpty(mailMsg)) {
			try {
				String preMsg = "Trust RelationShip name: " + trustRelationship.getDisplayName() + " (inum:" + trustRelationship.getInum()
						+ ")\n\n";
				GluuAppliance appliance = ApplianceService.instance().getAppliance();
				String subj = "Attributes with Privacy level 5 are released in a Trust Relationaship";
				MailUtils mail = new MailUtils(appliance.getSmtpHost(), appliance.getSmtpPort(), appliance.isRequiresSsl(),
						appliance.isRequiresAuthentication(), appliance.getSmtpUserName(), appliance.getSmtpPasswordStr());
				mail.sendMail(appliance.getSmtpFromName() + " <" + appliance.getSmtpFromEmailAddress() + ">", appliance.getContactEmail(),
						subj, preMsg + mailMsg);
			} catch (AuthenticationFailedException ex) {
				log.error("SMTP Authentication Error: ", ex);
			} catch (MessagingException ex) {
				log.error("SMTP Host Connection Error", ex);
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

	/**
	 * Analyzes trustRelationship metadata to find out if it is federation.
	 * 
	 * @author �Oleksiy Tataryn�
	 * @param trustRelationship
	 * @return
	 */
	public boolean isFederation(GluuSAMLTrustRelationship trustRelationship) {
	    //TODO: optimize this method. should not take so long
		return shibboleth2ConfService.isFederationMetadata(trustRelationship.getSpMetaDataFN());
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
			if (trustRelationship.equals(trust.getContainerFederation())) {
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
	
	public List<GluuSAMLTrustRelationship> searchSAMLTrustRelationships(String pattern, int sizeLimit) {
		String[] targetArray = new String[] { pattern };
		Filter displayNameFilter = Filter.createSubstringFilter(OxTrustConstants.displayName, null, targetArray, null);
		Filter descriptionFilter = Filter.createSubstringFilter(OxTrustConstants.description, null, targetArray, null);
		Filter inameFilter = Filter.createSubstringFilter(OxTrustConstants.iname, null, targetArray, null);
		Filter inumFilter = Filter.createSubstringFilter(OxTrustConstants.inum, null, targetArray, null);
		Filter searchFilter = Filter.createORFilter(displayNameFilter, descriptionFilter, inameFilter, inumFilter);

		List<GluuSAMLTrustRelationship> result = ldapEntryManager.findEntries(getDnForTrustRelationShip(null), GluuSAMLTrustRelationship.class, searchFilter, 0, sizeLimit);

		return result;
	}
}
