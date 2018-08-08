/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.model.GluuMetadataSourceType;
import org.gluu.oxtrust.model.GluuSAMLFederationProposal;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.persist.PersistenceEntryManager;
import org.gluu.persist.model.base.InumEntry;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.model.GluuStatus;
import org.xdi.util.INumGenerator;
import org.xdi.util.StringHelper;

@Stateless
@Named
public class FederationService implements Serializable {

	private static final long serialVersionUID = 3701922947171190714L;

	@Inject
	private PersistenceEntryManager ldapEntryManager;
	@Inject
	private ApplianceService applianceService;

	@Inject
	private Shibboleth3ConfService shibboleth3ConfService;

	@Inject
	private AppConfiguration appConfiguration;

	public void addFederationProposal(GluuSAMLFederationProposal federationProposal) {
		String[] clusterMembers = appConfiguration.getClusteredInums();
		String applianceInum = appConfiguration.getApplianceInum();
		if (clusterMembers == null || clusterMembers.length == 0) {
			clusterMembers = new String[] { applianceInum };
		}

		String dn = federationProposal.getDn();
		for (String clusterMember : clusterMembers) {
			String clusteredDN = StringHelper.replaceLast(dn, applianceInum, clusterMember);
			federationProposal.setDn(clusteredDN);
			ldapEntryManager.persist(federationProposal);
		}
		federationProposal.setDn(dn);
	}

	/**
	 * Generate new inum for federation proposal
	 * 
	 * @return New inum for federation proposal
	 */
	public String generateInumForNewFederationProposal() {
		InumEntry entry = new InumEntry();
		String newDn = appConfiguration.getBaseDN();
		entry.setDn(newDn);
		String newInum;
		do {
			newInum = generateInumForNewFederationProposalImpl();
			entry.setInum(newInum);
		} while (ldapEntryManager.contains(entry));

		return newInum;
	}

	/**
	 * Generate new inum for federation proposal
	 * 
	 * @return New inum for federation proposal
	 */
	private String generateInumForNewFederationProposalImpl() {
		return getApplianceInum() + OxTrustConstants.inumDelimiter + "0006" + OxTrustConstants.inumDelimiter + INumGenerator.generate(2);
	}

	/**
	 * Check if LDAP server contains federation proposal with specified
	 * attributes
	 * 
	 * @return True if federation proposal with specified attributes exist
	 */
	public boolean containsFederationProposal(GluuSAMLFederationProposal federationProposal) {
		return ldapEntryManager.contains(federationProposal);
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
	 * Build DN string for federation proposal
	 * 
	 * @param inum
	 *            Inum
	 * @return DN string for specified federation proposal or DN for federation
	 *         proposal branch if inum is null
	 */
	public String getDnForFederationProposal(String inum) {
		String applianceDn = applianceService.getDnForAppliance();
		if (StringHelper.isEmpty(inum)) {
			return String.format("ou=federations,%s", applianceDn);
		}

		return String.format("inum=%s,ou=federations,%s", inum, applianceDn);
	}

	public void updateFederationProposal(GluuSAMLFederationProposal federationProposal) {
		String[] clusterMembers = appConfiguration.getClusteredInums();
		String applianceInum = appConfiguration.getApplianceInum();
		if (clusterMembers == null || clusterMembers.length == 0) {
			clusterMembers = new String[] { applianceInum };
		}

		String dn = federationProposal.getDn();
		for (String clusterMember : clusterMembers) {
			String clusteredDN = StringHelper.replaceLast(dn, applianceInum, clusterMember);
			federationProposal.setDn(clusteredDN);
			ldapEntryManager.merge(federationProposal);
		}
		federationProposal.setDn(dn);

	}

	/**
	 * This is a LDAP operation as LDAP and IDP will always be in sync. We can
	 * just call LDAP to fetch all Federation Proposals.
	 */
	public List<GluuSAMLFederationProposal> getAllFederationProposals() {
		return ldapEntryManager.findEntries(getDnForFederationProposal(null), GluuSAMLFederationProposal.class, null);
	}

	public GluuSAMLFederationProposal getProposalByInum(String inum) {
		return ldapEntryManager.find(GluuSAMLFederationProposal.class, getDnForFederationProposal(inum));
	}

	public void removeFederationProposal(GluuSAMLFederationProposal federationProposal) {
		if (federationProposal.isFederation()) {
			for (GluuSAMLFederationProposal proposal : getAllFederationProposals()) {
				if (proposal.getContainerFederation() != null && proposal.getContainerFederation().equals(federationProposal)) {
					shibboleth3ConfService.removeMetadataFile(proposal.getSpMetaDataFN());
					removeFederationProposal(proposal);
				}
			}
		} else {
			shibboleth3ConfService.removeMetadataFile(federationProposal.getSpMetaDataFN());
		}

		String[] clusterMembers = appConfiguration.getClusteredInums();
		String applianceInum = appConfiguration.getApplianceInum();
		if (clusterMembers == null || clusterMembers.length == 0) {
			clusterMembers = new String[] { applianceInum };
		}

		String dn = federationProposal.getDn();
		for (String clusterMember : clusterMembers) {
			String clusteredDN = StringHelper.replaceLast(dn, applianceInum, clusterMember);
			federationProposal.setDn(clusteredDN);
			ldapEntryManager.remove(federationProposal);
		}
		federationProposal.setDn(dn);
	}

	/**
	 * Get all metadata source types
	 * 
	 * @return Array of metadata source types
	 */
	public GluuMetadataSourceType[] getMetadataSourceTypes() {
		List<GluuMetadataSourceType> trTypes = Arrays.asList(GluuMetadataSourceType.values());
		List<GluuMetadataSourceType> proposalTypes = new ArrayList<GluuMetadataSourceType>(trTypes);
		proposalTypes.remove(GluuMetadataSourceType.FEDERATION);
		proposalTypes.remove(GluuMetadataSourceType.GENERATE);
		return proposalTypes.toArray(new GluuMetadataSourceType[] {});
	}

	public List<GluuSAMLFederationProposal> getAllActiveFederationProposals() {
		GluuSAMLFederationProposal federationProposal = new GluuSAMLFederationProposal();
		federationProposal.setBaseDn(getDnForFederationProposal(null));
		federationProposal.setStatus(GluuStatus.ACTIVE);

		return ldapEntryManager.findEntries(federationProposal);
	}

	public List<GluuSAMLFederationProposal> getAllFederations() {
		List<GluuSAMLFederationProposal> result = new ArrayList<GluuSAMLFederationProposal>();
		for (GluuSAMLFederationProposal trust : getAllActiveFederationProposals()) {
			if (trust.isFederation()) {
				result.add(trust);
			}
		}

		return result;
	}

	public GluuSAMLFederationProposal getProposalByDn(String dn) {
		if (StringHelper.isNotEmpty(dn)) {
			return ldapEntryManager.find(GluuSAMLFederationProposal.class, dn);
		}
		return null;
	}

	// public GluuSAMLTrustRelationship getRelationshipByInum(String inum) {
	// return ldapEntryManager.find(GluuSAMLTrustRelationship.class,
	// getDnForTrustRelationShip(inum));
	// }

	// public GluuSAMLTrustRelationship getRelationshipByDn(String dn) {
	// if(StringHelper.isNotEmpty(dn)){
	// return ldapEntryManager.find(GluuSAMLTrustRelationship.class, dn);
	// }
	// return null;
	// }

	// /**
	// * This is a LDAP operation as LDAP and IDP will always be in sync. We can
	// just call LDAP to fetch all Trust Relationships.
	// */
	// public List<GluuSAMLTrustRelationship> getAllTrustRelationships() {
	// return
	// ldapEntryManager.findEntries(getDnForTrustRelationShip(null),GluuSAMLTrustRelationship.class,
	// null);
	// }
	//

	//
	//

	//
	// /**
	// * Get trustService instance
	// * @return TrustService instance
	// */
	// public static FederationService instance() {
	// return (FederationService)
	// Component.getInstance(FederationService.class);
	// }
	//
	// public void updateReleasedAttributes(GluuSAMLTrustRelationship
	// trustRelationship) {
	// List<String> releasedAttributes = new ArrayList<String>();
	// trustRelationship.setReleasedAttributes(releasedAttributes);
	//
	// for (GluuCustomAttribute customAttribute :
	// trustRelationship.getReleasedCustomAttributes()) {
	// releasedAttributes.add(customAttribute.getMetadata().getDn());
	// }
	// }
	//
	// /**
	// * Analyzes trustRelationship metadata to find out if it is federation.
	// * @param trustRelationship
	// * @return
	// */
	// public boolean isFederation(GluuSAMLTrustRelationship trustRelationship)
	// {
	// return
	// shibboleth3ConfService.isFederationMetadata(trustRelationship.getSpMetaDataFN());
	// }
	//
	// public List<TrustContact> getContacts(GluuSAMLTrustRelationship
	// trustRelationship) {
	// List<String> gluuTrustContacts = trustRelationship.getGluuTrustContact();
	// List<TrustContact> contacts = new ArrayList<TrustContact>();
	// if(gluuTrustContacts != null){
	// for (String contact : gluuTrustContacts){
	// contacts.add(xmlService.getTrustContactFromXML(contact));
	// }
	// }
	// return contacts;
	// }
	//
	// public void saveContacts(GluuSAMLTrustRelationship trustRelationship,
	// List<TrustContact> contacts) {
	// List<String> gluuTrustContacts = new ArrayList<String>();
	// for (TrustContact contact : contacts){
	// gluuTrustContacts.add(xmlService.getXMLFromTrustContact(contact));
	// }
	// trustRelationship.setGluuTrustContact(gluuTrustContacts);
	// }

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
	//
	// public List<GluuSAMLTrustRelationship>
	// getDeconstructedTrustRelationships(
	// GluuSAMLTrustRelationship trustRelationship) {
	// List<GluuSAMLTrustRelationship> result = new
	// ArrayList<GluuSAMLTrustRelationship>();
	// for(GluuSAMLTrustRelationship trust : getAllTrustRelationships()){
	// if(trustRelationship.equals(trust.getContainerFederation())){
	// result.add(trust);
	// }
	// }
	// return result;
	// }
	//
	// public GluuSAMLTrustRelationship getTrustByUnpunctuatedInum(String
	// unpunctuated) {
	// for(GluuSAMLTrustRelationship trust : getAllTrustRelationships()){
	// if(StringHelper.removePunctuation(trust.getInum()).equals(unpunctuated)){
	// return trust;
	// }
	// }
	// return null;
	// }
}
