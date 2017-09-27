/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.gluu.oxtrust.ldap.service.IGroupService;
import org.gluu.oxtrust.ldap.service.IPersonService;
import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.model.scim.ScimPersonEmails;
import org.gluu.oxtrust.model.scim2.user.Email;
import org.richfaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdi.config.oxtrust.AppConfiguration;

/**
 * User: Dejan Maric
 */
@Named
public class ServiceUtil implements Serializable {

    private static Logger logger = LoggerFactory.getLogger(ServiceUtil.class);

    private static final long serialVersionUID = -2842459224631032594L;

    @Inject
    private IPersonService personService;

    @Inject
    private IGroupService groupService;

    @Inject
    private AppConfiguration appConfiguration;

    private static final SecureRandom random = new SecureRandom();

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.disable(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS);
        mapper.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    /**
     * Delete a Group from a Person
     *
     * @return void
     * @throws Exception
     */
    public void deleteGroupFromPerson(GluuGroup group, String dn) throws Exception {
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

    public String iterableToString(Iterable<?> list) {
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
    public void deleteUserFromGroup(GluuCustomPerson person, String dn) throws Exception {
        List<String> groups = person.getMemberOf();
        for (String oneGroup : groups) {

            GluuGroup aGroup = groupService.getGroupByDn(oneGroup);
            List<String> groupMembers = aGroup.getMembers();

            List<String> tempGroupMembers = new ArrayList<String>();
            if (groupMembers != null && !groupMembers.isEmpty()) {
                for (String aMember : groupMembers) {
                    tempGroupMembers.add(aMember);
                }
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

            if (aGroup.getMembers() != null && aGroup.getMembers().isEmpty()) {
                aGroup.setMembers(null);  // Reset to no members
            }

            groupService.updateGroup(aGroup);
        }
    }

    /**
     * Adds a group to a person's memberOf
     *
     * @return void
     * @throws Exception
     */
    public void personMembersAdder(GluuGroup gluuGroup, String dn) throws Exception {
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
    private boolean isMemberOfExist(List<String> groups, String dn) {
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
    public void groupMembersAdder(GluuCustomPerson gluuPerson, String dn) throws Exception {
        List<String> groups = gluuPerson.getMemberOf();

        for (String group : groups) {

            GluuGroup oneGroup = groupService.getGroupByDn(group);

            List<String> groupMembers = oneGroup.getMembers();

            if ((groupMembers != null && !groupMembers.isEmpty()) && !isMemberExist(groupMembers, dn)) {

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
    private boolean isMemberExist(List<String> groupMembers, String dn) {

        for (String member : groupMembers) {
            if (member.equalsIgnoreCase(dn)) {
                return true;
            }
        }
        return false;
    }

    public String getPersonParentInum() {
        return appConfiguration.getOrgInum() + OxTrustConstants.inumDelimiter
                + OxTrustConstants.INUM_PERSON_OBJECTTYPE;
    }

    public String getPersonParentIname() {
        return appConfiguration.getOrgIname() + OxTrustConstants.inameDelimiter
                + OxTrustConstants.INAME_PERSON_OBJECTTYPE;
    }

    /**
     * Copy uploaded file to byte array.
     *
     * @param uploadedFile
     * @return byte array
     * @throws IOException
     */
    public byte[] copyUploadedFile(UploadedFile uploadedFile) throws IOException {

        InputStream in = uploadedFile.getInputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
        } finally {
            out.close();
            in.close();
        }
        return out.toByteArray();
    }

    /**
     * Save file with random name with provided base directory and extension.
     * @param array binary content of file.
     * @param baseDir Write to directory.
     * @param extension Filename extension.
     * @return Return full path
     * @throws IOException
     */
    public static String saveRandomFile(byte[] array, String baseDir, String extension) throws IOException {
        final String filepath = baseDir + File.separator + Math.abs(random.nextLong()) + "." + extension;

        final File dir = new File(baseDir);
        if (!dir.exists())
            dir.mkdirs();
        else if (!dir.isDirectory())
            throw new IllegalArgumentException("parameter baseDir should be directory. The value: " + baseDir);

        InputStream in = new ByteArrayInputStream(array);
        FileOutputStream out = new FileOutputStream(filepath);
        try {
            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
        } finally {
            in.close();
            out.close();
        }
        return filepath;
    }

    /**
     * Save uploaded file with random name.
     * @param uploadedFile
     * @param baseDir Write to directory.
     * @param extension Filename extension.
     * @return Return full path
     * @throws IOException
     */
    public String saveUploadedFile(UploadedFile uploadedFile, String baseDir, String extension) throws IOException {
        byte[] fileContent = copyUploadedFile(uploadedFile);

        return saveRandomFile(fileContent, baseDir, extension);
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

        logger.info(" IN Utils.syncEmailForward()...");

        GluuCustomAttribute oxTrustEmail = gluuCustomPerson.getGluuCustomAttribute("oxTrustEmail");

        if (oxTrustEmail != null && oxTrustEmail.getValues().length > 0) {

            String[] oxTrustEmails = oxTrustEmail.getValues();  // JSON array in element 0
            String[] newMails = new String[oxTrustEmails.length];

            ObjectMapper mapper = getObjectMapper();

            if (isScim2) {

                for (int i = 0; i < oxTrustEmails.length; i++) {
                    newMails[i] = mapper.readValue(oxTrustEmails[i], Email.class).getValue();
                }

            } else {
                //TODO: determine if this branch is useful at all. If not, can be dropped as well as ScimPersonEmails class
				/*
				ScimPersonEmails[] emails = mapper.readValue(oxTrustEmails[0], ScimPersonEmails[].class);
				newMails = new String[emails.length];

				for (int i = 0; i < emails.length; i++) {
					newMails[i] = emails[i].getValue();
				}
				*/

                for (int i = 0; i < oxTrustEmails.length; i++) {
                    ScimPersonEmails email = mapper.readValue(oxTrustEmails[i], ScimPersonEmails.class);
                    newMails[i] = email.getValue();
                }
            }

            gluuCustomPerson.setAttribute("mail", newMails);

		/* // Just do nothing if null, same as in UserWebService.updateUser()
		} else {
			gluuCustomPerson.setAttribute("mail", new String[0]);
		*/
        }

        logger.info(" LEAVING Utils.syncEmailForward()...");

        return gluuCustomPerson;
    }

    /**
     * One-way sync from "mail" to "oxTrustEmail". This will persist the email in "oxTrustEmail"
     * in SCIM 2.0 format since the SCIM 1.1 format is a subset of SCIM 2.0.
     *
     * @param gluuCustomPerson
     * @param isScim2
     * @return
     * @throws Exception
     */
    public static GluuCustomPerson syncEmailReverse(GluuCustomPerson gluuCustomPerson, boolean isScim2) throws Exception {

        logger.info(" IN Utils.syncEmailReverse()...");

        GluuCustomAttribute mail = gluuCustomPerson.getGluuCustomAttribute("mail");
        GluuCustomAttribute oxTrustEmail = gluuCustomPerson.getGluuCustomAttribute("oxTrustEmail");

        if (mail != null && mail.getValues().length > 0) {

            String[] mails = mail.getValues();  // String array

            String[] oxTrustEmails = null;
            if (oxTrustEmail != null) {
                oxTrustEmails = oxTrustEmail.getValues();  // In the old format, JSON array is in element 0
            }

            ObjectMapper mapper = getObjectMapper();

            // Retain the switch just in case this will be useful in the future
            // if (isScim2) {

            List<String> newOxTrustEmails = new ArrayList<String>();
            Email[] emails = null;

            if (oxTrustEmails != null && oxTrustEmails.length > 0) {

                // emails = mapper.readValue(oxTrustEmails[0], Email[].class);
                emails = new Email[oxTrustEmails.length];

                for (int i = 0; i < oxTrustEmails.length; i++) {
                    Email email = mapper.readValue(oxTrustEmails[i], Email.class);
                    emails[i] = email;
                }
            }

            for (int i = 0; i < mails.length; i++) {

                if (emails != null && (i < emails.length) && (emails[i] != null)) {

                    Email email = emails[i];
                    email.setDisplay((email.getDisplay() != null && !email.getDisplay().equalsIgnoreCase(email.getValue())) ? email.getDisplay() : mails[i]);
                    email.setValue(mails[i]);

                    newOxTrustEmails.add(mapper.writeValueAsString(email));

                } else {

                    Email email = new Email();
                    email.setValue(mails[i]);
                    email.setPrimary(i == 0 ? true : false);
                    email.setDisplay(mails[i]);
                    email.setType("other");

                    newOxTrustEmails.add(mapper.writeValueAsString(email));
                }
            }

			    /*
				StringWriter stringWriter = new StringWriter();
				getObjectMapper().writeValue(stringWriter, newOxTrustEmails);
				String newOxTrustEmail = stringWriter.toString();
				*/

            gluuCustomPerson.setAttribute("oxTrustEmail", newOxTrustEmails.toArray(new String[]{}));

			/*
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
			*/
        }

        logger.info(" LEAVING Utils.syncEmailReverse()...");

        return gluuCustomPerson;
    }

    public static ObjectMapper getObjectMapper() {
        return mapper;
    }

    /**
     * Read all bytes from the supplied input stream. Closes the input stream.
     *
     * @param is Input stream
     * @return All bytes
     * @throws IOException If an I/O problem occurs
     */
    public static byte[] readFully(InputStream is) throws IOException {
        ByteArrayOutputStream baos = null;

        try {
            baos = new ByteArrayOutputStream();

            byte[] buffer = new byte[2048];
            int read = 0;

            while ((read = is.read(buffer)) != -1) {
                baos.write(buffer, 0, read);
            }

            return baos.toByteArray();
        } finally {
            IOUtils.closeQuietly(baos);
            IOUtils.closeQuietly(is);
        }
    }
}
