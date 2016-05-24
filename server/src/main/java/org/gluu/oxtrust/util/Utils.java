/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.util;

import java.io.*;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.gluu.oxtrust.config.OxTrustConfiguration;
import org.gluu.oxtrust.ldap.service.GroupService;
import org.gluu.oxtrust.ldap.service.IGroupService;
import org.gluu.oxtrust.ldap.service.IPersonService;
import org.gluu.oxtrust.ldap.service.PersonService;
import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.model.scim.ScimPersonEmails;
import org.gluu.oxtrust.model.scim2.Email;
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
	public static void personMembersAdder(GluuGroup gluuGroup, String dn) throws Exception {
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
	public static void groupMembersAdder(GluuCustomPerson gluuPerson, String dn) throws Exception {

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
        String filepath = baseDir + File.separator + Math.abs(random.nextLong()) + "." + extension;
        
        File dir = new File(baseDir);
        if (!dir.exists())
            dir.mkdirs();
        else if (!dir.isDirectory())
            throw new IllegalArgumentException("parameter baseDir should be directory. The value: " + baseDir);
        
        InputStream in = uploadedFile.getInputStream();
        FileOutputStream out = new FileOutputStream(filepath);
        try {
            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
        } finally {
            out.close();
            in.close();
        }
        return filepath;
    }

	/**
	 * One-way sync from "oxTrustEmail" to "mail". Ultimately this is persisted so "mail" will be
	 * updated by values from "oxTrustEmail".
	 *
	 * @param gluuCustomPerson
	 * @return
	 * @throws Exception
	 */
	public static GluuCustomPerson syncEmailForward(GluuCustomPerson gluuCustomPerson, boolean isScim2) throws Exception {

		GluuCustomAttribute mail = gluuCustomPerson.getGluuCustomAttribute("mail");
		GluuCustomAttribute oxTrustEmail = gluuCustomPerson.getGluuCustomAttribute("oxTrustEmail");

		if (oxTrustEmail != null && oxTrustEmail.getValues().length > 0) {

			String[] oxTrustEmails = oxTrustEmail.getValues();  // JSON array in element 0
			String[] newMails = null;

			if (isScim2) {

				Email[] emails = getObjectMapper().readValue(oxTrustEmails[0], Email[].class);
				newMails = new String[emails.length];

				for (int i = 0; i < emails.length; i++) {
					newMails[i] = emails[i].getValue();
				}

				if (mail == null) {
					mail = new GluuCustomAttribute();
				}
				mail.setValues(newMails);

			} else {

				ScimPersonEmails[] emails = getObjectMapper().readValue(oxTrustEmails[0], ScimPersonEmails[].class);
				newMails = new String[emails.length];

				for (int i = 0; i < emails.length; i++) {
					newMails[i] = emails[i].getValue();
				}

				if (mail == null) {
					mail = new GluuCustomAttribute();
				}
				mail.setValues(newMails);
			}

			gluuCustomPerson.setAttribute("mail", newMails);

		/* // Just do nothing if null, same as in UserWebService.updateUser()
		} else {
			gluuCustomPerson.setAttribute("mail", new String[0]);
		*/
		}

		return gluuCustomPerson;
	}

	/**
	 * One-way sync from "mail" to "oxTrustEmail". In the call chain the sync-ing is ultimately
	 * not persisted so this is for API purposes only. This is the case because both SCIM 2.0 and
	 * 1.1 persist to the same field "oxTrustEmail" and during runtime the choice whether to
	 * use SCIM 2.0 or 1.1 is ambiguous.
	 *
	 * @param gluuCustomPerson
	 * @param isScim2
	 * @return
	 * @throws Exception
     */
	public static GluuCustomPerson syncEmailReverse(GluuCustomPerson gluuCustomPerson, boolean isScim2) throws Exception {

		GluuCustomAttribute mail = gluuCustomPerson.getGluuCustomAttribute("mail");
		GluuCustomAttribute oxTrustEmail = gluuCustomPerson.getGluuCustomAttribute("oxTrustEmail");

		if (mail != null && mail.getValues().length > 0) {

			String[] mails = mail.getValues();  // String array

			String[] oxTrustEmails = null;
			if (oxTrustEmail != null) {
				oxTrustEmails = oxTrustEmail.getValues();  // JSON array in element 0
			}

			if (isScim2) {

				List<Email> newOxTrustEmails = new ArrayList<Email>();
				Email[] emails = new Email[mails.length];

				if (oxTrustEmails != null && oxTrustEmails.length > 0) {
					emails = getObjectMapper().readValue(oxTrustEmails[0], Email[].class);
				}

				for (int i = 0; i < mails.length; i++) {

					if (i < emails.length && (emails[i] != null)) {

						Email email = emails[i];
						email.setDisplay((email.getDisplay() != null && email.getDisplay().equalsIgnoreCase(email.getValue())) ? mails[i] : email.getDisplay());
						email.setValue(mails[i]);

						newOxTrustEmails.add(email);

					} else {

						Email email = new Email();
						email.setValue(mails[i]);
						email.setPrimary(i == 0 ? true : false);
						email.setDisplay(mails[i]);
						email.setType(Email.Type.OTHER);

						newOxTrustEmails.add(email);
					}
				}

				StringWriter stringWriter = new StringWriter();
				getObjectMapper().writeValue(stringWriter, newOxTrustEmails);
				String newOxTrustEmail = stringWriter.toString();

				gluuCustomPerson.setAttribute("oxTrustEmail", newOxTrustEmail);

			} else {

				List<ScimPersonEmails> newOxTrustEmails = new ArrayList<ScimPersonEmails>();
				ScimPersonEmails[] scimPersonEmails = new ScimPersonEmails[mails.length];

				if (oxTrustEmails != null && oxTrustEmails.length > 0) {
					scimPersonEmails = getObjectMapper().readValue(oxTrustEmails[0], ScimPersonEmails[].class);
				}

				for (int i = 0; i < mails.length; i++) {

					if (i < scimPersonEmails.length && (scimPersonEmails[i] != null)) {

						ScimPersonEmails scimPersonEmail = scimPersonEmails[i];
						scimPersonEmail.setValue(mails[i]);

						newOxTrustEmails.add(scimPersonEmail);

					} else {

						ScimPersonEmails scimPersonEmail = new ScimPersonEmails();
						scimPersonEmail.setValue(mails[i]);
						scimPersonEmail.setPrimary(i == 0 ? "true" : "false");
						scimPersonEmail.setType(Email.Type.OTHER.getValue());

						newOxTrustEmails.add(scimPersonEmail);
					}
				}

				StringWriter stringWriter = new StringWriter();
				getObjectMapper().writeValue(stringWriter, newOxTrustEmails);
				String newOxTrustEmail = stringWriter.toString();

				gluuCustomPerson.setAttribute("oxTrustEmail", newOxTrustEmail);
			}
		}

		return gluuCustomPerson;
	}

	public static ObjectMapper getObjectMapper() {

		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS);
		mapper.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);

		return mapper;
	}
}
