/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.util;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import org.gluu.oxtrust.config.OxTrustConfiguration;
import org.gluu.oxtrust.ldap.service.GroupService;
import org.gluu.oxtrust.ldap.service.IGroupService;
import org.gluu.oxtrust.ldap.service.IPersonService;
import org.gluu.oxtrust.ldap.service.PersonService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuGroup;
import org.richfaces.model.UploadedFile;

/**
 * User: Dejan Maric
 */
public class Utils implements Serializable {

	/**
     *
     */
	private static final long serialVersionUID = -2842459224631032594L;
    
        private static final SecureRandom random = new SecureRandom();

	/**
	 * Delete a Group from a Person
	 * 
	 * @return void
	 * @throws Exception
	 */
	public static void deleteGroupFromPerson(GluuGroup group, String dn) throws Exception {

		IPersonService personService = PersonService.instance();

		List<String> persons = group.getMembers();
		for (String onePerson : persons) {

			GluuCustomPerson gluuPerson = personService.getPersonByDn(onePerson);
			List<String> memberOflist = gluuPerson.getMemberOf();

			List<String> tempMemberOf = new ArrayList<String>();
			for (String aMemberOf : memberOflist) {
				tempMemberOf.add(aMemberOf);
			}

			for (String oneMemberOf : tempMemberOf) {
				if (oneMemberOf.equalsIgnoreCase(dn)) {
					tempMemberOf.remove(oneMemberOf);
					break;
				}
			}

			List<String> cleanMemberOf = new ArrayList<String>();

			for (String aMemberOf : tempMemberOf) {
				cleanMemberOf.add(aMemberOf);
			}

			gluuPerson.setMemberOf(cleanMemberOf);
			personService.updatePerson(gluuPerson);

		}

	}

	public static String iterableToString(Iterable<?> list) {
		if (list == null) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		for (Object item : list) {
			sb.append(item);
			sb.append(",");
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	/**
	 * Delete a person from a group
	 * 
	 * @return void
	 * @throws Exception
	 */
	public static void deleteUserFromGroup(GluuCustomPerson person, String dn) throws Exception {

		IGroupService groupService = GroupService.instance();

		List<String> groups = person.getMemberOf();
		for (String oneGroup : groups) {

			GluuGroup aGroup = groupService.getGroupByDn(oneGroup);
			List<String> groupMembers = aGroup.getMembers();

			List<String> tempGroupMembers = new ArrayList<String>();
			for (String aMember : groupMembers) {
				tempGroupMembers.add(aMember);
			}

			for (String oneMember : tempGroupMembers) {

				if (oneMember.equalsIgnoreCase(dn)) {

					tempGroupMembers.remove(oneMember);

					break;
				}
			}

			List<String> cleanGroupMembers = new ArrayList<String>();
			for (String aMember : tempGroupMembers) {
				cleanGroupMembers.add(aMember);
			}

			aGroup.setMembers(cleanGroupMembers);

			groupService.updateGroup(aGroup);
		}

	}

	/**
	 * Adds a group to a person's memberOf
	 * 
	 * @return void
	 * @throws Exception
	 */
	public static void personMemebersAdder(GluuGroup gluuGroup, String dn) throws Exception {
		IPersonService personService = PersonService.instance();

		List<String> members = gluuGroup.getMembers();

		for (String member : members) {
			GluuCustomPerson gluuPerson = personService.getPersonByDn(member);

			List<String> groups = gluuPerson.getMemberOf();
			if (!isMemberOfExist(groups, dn)) {

				List<String> cleanGroups = new ArrayList<String>();
				cleanGroups.add(dn);
				for (String aGroup : groups) {
					cleanGroups.add(aGroup);
				}
				;
				gluuPerson.setMemberOf(cleanGroups);
				personService.updatePerson(gluuPerson);
			}

		}

	}

	/**
	 * checks if the memeberOf attribute already contains a given group
	 * 
	 * @return boolean
	 */
	private static boolean isMemberOfExist(List<String> groups, String dn) {
		for (String group : groups) {
			if (group.equalsIgnoreCase(dn)) {
				return true;
			}

		}
		return false;
	}

	/**
	 * Adds a person to a group
	 * 
	 * @return void
	 * @throws Exception
	 */
	public static void groupMemebersAdder(GluuCustomPerson gluuPerson, String dn) throws Exception {

		IGroupService groupService = GroupService.instance();

		List<String> groups = gluuPerson.getMemberOf();

		for (String group : groups) {

			GluuGroup oneGroup = groupService.getGroupByDn(group);

			List<String> groupMembers = oneGroup.getMembers();

			if (!isMemberExist(groupMembers, dn)) {

				List<String> cleanGroupMembers = new ArrayList<String>();
				cleanGroupMembers.add(dn);

				for (String personDN : groupMembers) {
					cleanGroupMembers.add(personDN);
				}

				oneGroup.setMembers(cleanGroupMembers);
				groupService.updateGroup(oneGroup);
			}
		}

	}

	/**
	 * checks if the member already exist in a group
	 * 
	 * @return boolean
	 */
	private static boolean isMemberExist(List<String> groupMembers, String dn) {

		for (String member : groupMembers) {
			if (member.equalsIgnoreCase(dn)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * gets the authentication mode true if its basic or false if its oxAuth
	 * 
	 * @return boolean
	 */

	public static boolean isBasicAuth() {
		String mode = OxTrustConfiguration.instance().getApplicationConfiguration().getAuthMode();
		if ("basic".equalsIgnoreCase(mode)) {
			return true;
		}
		return false;
	}

	/**
	 * gets the authentication mode true if its basic or false if its oxAuth
	 * 
	 * @return boolean
	 */

	public static boolean isOxAuth() {
		String mode = OxTrustConfiguration.instance().getApplicationConfiguration().getAuthMode();
		if ("oxauth".equalsIgnoreCase(mode)) {
			return true;
		}
		return false;
	}

	/**
	 * Returns an xri (last quad) in lower case for given inum
	 * 
	 * @param inum
	 *            object's inum
	 * @return lower case representation of xri
	 */
	public static String getXriFromInum(String inum) {
		String xri = inum.substring(inum.lastIndexOf(OxTrustConstants.inumDelimiter));
		return xri.toLowerCase();
	}

	/**
	 * Returns parent xri in lower case for a given inum
	 * 
	 * @param inum
	 *            object's inum
	 * @return parent xri in lower case
	 */
	public static String getParentXriFromInum(String inum) {
		String parentXri = inum.substring(0, inum.lastIndexOf(OxTrustConstants.inumDelimiter));
		return parentXri.toLowerCase();
	}

	/**
	 * Returns an xri (last quad) in lower case for given iname
	 * 
	 * @param iname
	 *            object's iname
	 * @return lower case representation of xri
	 */
	public static String getXriFromIname(String iname) {
		String xri = iname.substring(iname.lastIndexOf(OxTrustConstants.inameDelimiter));
		return xri.toLowerCase();
	}

	/**
	 * Returns parent xri in lower case for a given inum
	 * 
	 * @param inum
	 *            object's inum
	 * @return parent xri in lower case
	 */
	public static String getParentXriFromIname(String inum) {
		String parentXri = inum.substring(0, inum.lastIndexOf(OxTrustConstants.inameDelimiter));
		return parentXri.toLowerCase();
	}

	/*
	 * public static String getSchoolClassParentIname() { return
	 * OxTrustConfiguration
	 * .instance().getApplicationConfiguration().getOrgInum() +
	 * Configuration.inameDelimiter +
	 * OxTrustConfiguration.instance().getApplicationConfiguration
	 * ().getOxPlusIname() + Configuration.inameDelimiter +
	 * Configuration.INAME_CLASS_OBJECTTYPE; }
	 */
	public static String getPersonParentInum() {
		return OxTrustConfiguration.instance().getApplicationConfiguration().getOrgInum() + OxTrustConstants.inumDelimiter
				+ OxTrustConstants.INUM_PERSON_OBJECTTYPE;
	}

	public static String getPersonParentIname() {
		return OxTrustConfiguration.instance().getApplicationConfiguration().getOrgIname() + OxTrustConstants.inameDelimiter
				+ OxTrustConstants.INAME_PERSON_OBJECTTYPE;
	}
        
        
        
    
    /**
     * Save uploaded file with random name.
     * @param uploadedFile
     * @param baseDir Write to directory. 
     * @param extension Filename extension.
     * @return Return full path
     * @throws IOException 
     */
    public static String saveUploadedFile(UploadedFile uploadedFile, String baseDir, String extension) throws IOException {
        String filepath = baseDir + File.separator + random.nextLong() + "." + extension;
        
        File dir = new File(filepath);
        if (!dir.exists())
            dir.mkdirs();
        else if (!dir.isDirectory())
            throw new IllegalArgumentException("parameter baseDir should be directory. The value: " + baseDir);
        
        uploadedFile.write(filepath);
        return filepath;
    }

}
