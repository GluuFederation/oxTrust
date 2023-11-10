/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.model.GluuAttribute;
import org.gluu.model.GluuUserRole;
import org.gluu.model.attribute.AttributeValidation;
import org.gluu.oxauth.model.fido.u2f.protocol.DeviceData;
import org.gluu.oxtrust.exception.DuplicateEmailException;
import org.gluu.oxtrust.model.Device;
import org.gluu.oxtrust.model.GluuBoolean;
import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuFido2Device;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.model.GluuUserPairwiseIdentifier;
import org.gluu.oxtrust.model.MobileDevice;
import org.gluu.oxtrust.model.OTPDevice;
import org.gluu.oxtrust.model.OxAuthClient;
import org.gluu.oxtrust.model.Phone;
import org.gluu.oxtrust.model.fido.GluuCustomFidoDevice;
import org.gluu.oxtrust.model.fido.GluuDeviceDataBean;
import org.gluu.oxtrust.model.scim2.user.Email;
import org.gluu.oxtrust.security.Identity;
import org.gluu.oxtrust.service.AttributeService;
import org.gluu.oxtrust.service.ClientService;
import org.gluu.oxtrust.service.Fido2DeviceService;
import org.gluu.oxtrust.service.FidoDeviceService;
import org.gluu.oxtrust.service.GroupService;
import org.gluu.oxtrust.service.MemberService;
import org.gluu.oxtrust.service.OrganizationService;
import org.gluu.oxtrust.service.OxTrustAuditService;
import org.gluu.oxtrust.service.PairwiseIdService;
import org.gluu.oxtrust.service.PersonService;
import org.gluu.oxtrust.service.external.ExternalUpdateUserService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.oxtrust.util.ProductInstallationChecker;
import org.gluu.persist.PersistenceEntryManager;
import org.gluu.persist.exception.BasePersistenceException;
import org.gluu.service.JsonService;
import org.gluu.service.security.Secure;
import org.gluu.util.ArrayHelper;
import org.gluu.util.StringHelper;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Action class for updating person's attributes
 * 
 * @author Yuriy Movchan Date: 10.23.2010
 */
@ConversationScoped
@Named("updatePersonAction")
@Secure("#{permissionService.hasPermission('person', 'access')}")
public class UpdatePersonAction implements Serializable {

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}

	private String MOBILE = "mobile";

	private String OTP_DEVICE = "OTP Device";

	private String HOTP = "hotp";

	private String TOTP = "totp";

	private String COLON = ":";

	private String DASH = "-";

	private String PASSPORT = "Passport";

	private String MAIL = "mail";

	private static final long serialVersionUID = -3242167044333943689L;

	@Inject
	private Logger log;

	private String inum;
	private boolean update;

	private GluuCustomPerson person;

	@Inject
	private FacesMessages facesMessages;

	@Inject
	private ConversationService conversationService;

	@Inject
	private OrganizationService organizationService;

	@Inject
	private GroupService groupService;

	@Inject
	private AttributeService attributeService;

	@Inject
	private PersonService personService;

	@Inject
	private ClientService clientService;

	@Inject
	private CustomAttributeAction customAttributeAction;

	@Inject
	private AppConfiguration appConfiguration;

	@Inject
	private ExternalUpdateUserService externalUpdateUserService;

	@Inject
	private MemberService memberService;

	@Inject
	private PersistenceEntryManager ldapEntryManager;

	@Inject
	private FidoDeviceService fidoDeviceService;

	@Inject
	private Fido2DeviceService fido2DeviceService;

	@Inject
	private PairwiseIdService pairwiseIdService;

	@Inject
	private Identity identity;

	@Inject
	private OxTrustAuditService oxTrustAuditService;

	@Inject
	private JsonService jsonService;
	
	@Inject
	private LogoutAction logoutAction;

	private String gluuStatus;

	private String password;

	private String confirmPassword;

	private GluuDeviceDataBean deviceToBeRemove;

	private GluuUserPairwiseIdentifier pwiToBeRemove;

	public GluuUserPairwiseIdentifier getPwiToBeRemove() {
		return pwiToBeRemove;
	}

	public void setPwiToBeRemove(GluuUserPairwiseIdentifier pwiToBeRemove) {
		this.pwiToBeRemove = pwiToBeRemove;
	}

	private List<GluuDeviceDataBean> deviceDataMap = new ArrayList<GluuDeviceDataBean>();

	private List<GluuUserPairwiseIdentifier> userPairWideIdentifiers = new ArrayList<GluuUserPairwiseIdentifier>();

	public List<GluuUserPairwiseIdentifier> getUserPairWideIdentifiers() {
		return userPairWideIdentifiers;
	}

	public void setUserPairWideIdentifiers(List<GluuUserPairwiseIdentifier> userPairWideIdentifiers) {
		this.userPairWideIdentifiers = userPairWideIdentifiers;
	}

	private GluuCustomFidoDevice fidoDevice;

	public GluuCustomFidoDevice getFidoDevice() {
		return fidoDevice;
	}

	public void setFidoDevice(GluuCustomFidoDevice fidoDevice) {
		this.fidoDevice = fidoDevice;
	}

	public List<GluuDeviceDataBean> getDeviceDataMap() {
		return deviceDataMap;
	}

	public void setDeviceDataMap(List<GluuDeviceDataBean> deviceDataMap) {
		this.deviceDataMap = deviceDataMap;
	}

	private List<String> externalAuthCustomAttributes = new ArrayList<String>();
	private List<String> oxExternalUids = new ArrayList<String>();
	private DeviceData deviceDetail;

	private String oldUid;

	public DeviceData getDeviceDetail() {
		return deviceDetail;
	}

	public void setDeviceDetail(DeviceData deviceDetail) {
		this.deviceDetail = deviceDetail;
	}

	public List<String> getExternalAuthCustomAttributes() {
		return externalAuthCustomAttributes;
	}

	public void setExternalAuthCustomAttributes(List<String> externalAuthCustomAttributes) {
		this.externalAuthCustomAttributes = externalAuthCustomAttributes;
	}

	public String getGluuStatus() {
		return gluuStatus;
	}

	public void setGluuStatus(String gluuStatus) {
		this.gluuStatus = gluuStatus;
	}

	/**
	 * Initializes attributes for adding new person
	 * 
	 * @return String describing success of the operation
	 * @throws Exception
	 */
	public String add() {
		if (!organizationService.isAllowPersonModification()) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to add new person");
			conversationService.endConversation();
			return OxTrustConstants.RESULT_FAILURE;
		}

		if (this.person != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}
		this.update = false;
		this.person = new GluuCustomPerson();
		initAttributes(true);
		return OxTrustConstants.RESULT_SUCCESS;
	}

	/**
	 * Initializes attributes for updating person
	 * 
	 * @return String describing success of the operation
	 * @throws Exception
	 */
	public String update() {
		if (this.person != null) {
			return OxTrustConstants.RESULT_SUCCESS;

		}
		this.update = true;
		try {
			this.person = personService.getPersonByInum(inum);
		} catch (BasePersistenceException ex) {
			return handleFailure(ex);
		}
		loadUserPairwiseIdentifiers();
		initAttributes(false);
		try {
			this.gluuStatus = this.person.getStatus();
			this.oxExternalUids = this.person.getOxExternalUid();
			fillExternalAuthCustomAttributes();
			addExternalUids();
			addFidoDevices();
			addFido2Devices();
			addOtpDevices();
			addMobileDevices();
			this.oldUid = person.getUid();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return OxTrustConstants.RESULT_FAILURE;
		}
		return OxTrustConstants.RESULT_SUCCESS;
	}

	private void addExternalUids() {
		if (oxExternalUids != null && oxExternalUids.size() > 0) {
			for (String oxExternalUid : oxExternalUids) {
				String[] args = oxExternalUid.split(COLON);
				GluuDeviceDataBean gluuDeviceDataBean = new GluuDeviceDataBean();
				String firstPart = args[0];
				if (firstPart.startsWith(PASSPORT) || firstPart.startsWith(PASSPORT.toLowerCase())) {
					gluuDeviceDataBean.setNickName(firstPart);
					gluuDeviceDataBean.setModality(PASSPORT);
					gluuDeviceDataBean.setId(args[1]);
					gluuDeviceDataBean.setCreationDate(DASH);
					deviceDataMap.add(gluuDeviceDataBean);
				} else if (firstPart.equalsIgnoreCase(TOTP) || firstPart.equalsIgnoreCase(HOTP)) {
					if (!ProductInstallationChecker.isCasaInstalled()) {
						gluuDeviceDataBean.setNickName(firstPart);
						gluuDeviceDataBean.setModality(TOTP + "/" + HOTP);
						gluuDeviceDataBean.setId(args[1]);
						gluuDeviceDataBean.setCreationDate(DASH);
						deviceDataMap.add(gluuDeviceDataBean);
					}
				} else {
					gluuDeviceDataBean.setNickName(firstPart);
					gluuDeviceDataBean.setModality(DASH);
					gluuDeviceDataBean.setId(args[1]);
					gluuDeviceDataBean.setCreationDate(DASH);
					deviceDataMap.add(gluuDeviceDataBean);
				}

			}
		}
	}

	private boolean canBeConvertToInteger(String id) {
		try {
			Long.valueOf(id);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@SuppressWarnings("deprecation")
	private void addMobileDevices() throws IOException {
		String oxMobileDevices = this.person.getOxMobileDevices();
		if (oxMobileDevices != null && !oxMobileDevices.trim().equals("")) {
			ObjectMapper mapper = new ObjectMapper();
			MobileDevice mobileDevice = mapper.readValue(oxMobileDevices, MobileDevice.class);
			ArrayList<Phone> phones = mobileDevice.getPhones();
			if (phones != null && phones.size() > 0) {
				for (Phone phone : phones) {
					GluuDeviceDataBean gluuDeviceDataBean = new GluuDeviceDataBean();
					gluuDeviceDataBean.setNickName(phone.getNickName());
					gluuDeviceDataBean.setModality("Mobile Device");
					gluuDeviceDataBean.setId(phone.getNumber());
					Timestamp stamp = new Timestamp(Long.valueOf(phone.getAddedOn()).longValue());
					gluuDeviceDataBean.setCreationDate(stamp.toGMTString());
					deviceDataMap.add(gluuDeviceDataBean);
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void addOtpDevices() {
		ArrayList<Device> devices = new ArrayList<Device>();
		OTPDevice oxOTPDevices = this.person.getOxOTPDevices();
		if (oxOTPDevices != null) {
			devices = oxOTPDevices.getDevices();
		}
		if (devices != null && devices.size() > 0) {
			List<String> oxExternalUids = this.person.getOxExternalUid();
			boolean canProceed = false;
			canProceed = oxExternalUids != null && oxExternalUids.size() > 0;
			for (Device device : devices) {
				GluuDeviceDataBean gluuDeviceDataBean = new GluuDeviceDataBean();
				gluuDeviceDataBean.setNickName(device.getNickName());
				gluuDeviceDataBean.setModality(OTP_DEVICE);
				gluuDeviceDataBean.setId(device.getId());
				String hash = device.getId();
				if (canProceed && canBeConvertToInteger(hash)) {
					for (String oxExternalUid : oxExternalUids) {
						String firstPart = oxExternalUid.split(COLON)[0];
						if (firstPart.equalsIgnoreCase(TOTP) || firstPart.equalsIgnoreCase(HOTP)) {
							String key = oxExternalUid.replaceFirst("hotp:", "").replaceFirst("totp:", "");
							int idx = key.indexOf(";");
							if (idx > 0) {
								key = key.substring(0, idx);
							}
							if (String.valueOf(key.hashCode()).equalsIgnoreCase(hash)) {
								gluuDeviceDataBean.setId(key);
								break;
							}
						}
					}
				}
				gluuDeviceDataBean
						.setCreationDate(new Timestamp(Long.valueOf(device.getAddedOn()).longValue()).toGMTString());
				deviceDataMap.add(gluuDeviceDataBean);
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void addFido2Devices() {
		try {
			List<GluuFido2Device> fido2Devices = fido2DeviceService.findAllFido2Devices(this.person);
			if (fido2Devices != null && fido2Devices.size() > 0) {
				for (GluuFido2Device entry : fido2Devices) {
					GluuDeviceDataBean gluuDeviceDataBean = new GluuDeviceDataBean();
					gluuDeviceDataBean.setId(entry.getId());
					String convertedDate = ldapEntryManager
							.decodeTime(null, ldapEntryManager.encodeTime(null, entry.getCreationDate())).toGMTString();
					gluuDeviceDataBean.setCreationDate(convertedDate.toString());
					gluuDeviceDataBean.setModality("FIDO2");
					gluuDeviceDataBean.setNickName(entry.getDisplayName() != null ? entry.getDisplayName() : DASH);
					deviceDataMap.add(gluuDeviceDataBean);
				}
			}
		} catch (Exception e) {
			log.warn("No fido2 devices enrolled for " + this.person.getDisplayName());
		}
	}

	@SuppressWarnings("deprecation")
	private void addFidoDevices() {
		String baseDnForU2fDevices = fidoDeviceService.getDnForFidoDevice(this.person.getInum(), null);
		List<GluuCustomFidoDevice> fidoDevices = fidoDeviceService.searchFidoDevices(this.person.getInum());
		if (fidoDevices != null && !fidoDevices.isEmpty()) {
			int count = 1;
			for (GluuCustomFidoDevice gluuCustomFidoDevice : fidoDevices) {
				GluuDeviceDataBean gluuDeviceDataBean = new GluuDeviceDataBean();
				String creationDate = gluuCustomFidoDevice.getCreationDate();
				if (creationDate != null) {
					gluuDeviceDataBean.setCreationDate(
							ldapEntryManager.decodeTime(baseDnForU2fDevices, creationDate).toGMTString());
				} else {
					gluuDeviceDataBean.setCreationDate(DASH);
				}
				gluuDeviceDataBean.setId(gluuCustomFidoDevice.getId());
				String devicedata = gluuCustomFidoDevice.getDeviceData();
				String modality = DASH;
				if (devicedata != null) {
					modality = "Super-Gluu Device";
				} else {
					modality = "Security Key";
				}
				if (gluuCustomFidoDevice.getDisplayName() != null) {
					gluuDeviceDataBean.setNickName(gluuCustomFidoDevice.getDisplayName());
				}
				if (gluuDeviceDataBean.getNickName() == null || gluuDeviceDataBean.getNickName().isEmpty()) {
					gluuDeviceDataBean.setNickName(gluuCustomFidoDevice.getDescription());
				}
				if (gluuDeviceDataBean.getNickName() == null || gluuDeviceDataBean.getNickName().isEmpty()) {
					gluuDeviceDataBean.setNickName(this.person.getDisplayName() + DASH + "Device" + DASH + count);
				}
				gluuDeviceDataBean.setModality(modality);
				deviceDataMap.add(gluuDeviceDataBean);
				count++;
			}
		}
	}

	private void fillExternalAuthCustomAttributes() {
		if (oxExternalUids != null && oxExternalUids.size() > 0) {
			for (String oxExternalUid : oxExternalUids) {
				externalAuthCustomAttributes.add(oxExternalUid.split(COLON)[0]);
			}
		}
	}

	private void loadUserPairwiseIdentifiers() {
		userPairWideIdentifiers.clear();
		userPairWideIdentifiers.addAll(pairwiseIdService.findAllUserPairwiseIdentifiers(person));
	}

	private String handleFailure(Exception ex) {
		log.error("Failed to find person {}", inum, ex);
		facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to find person");
		conversationService.endConversation();
		return OxTrustConstants.RESULT_FAILURE;
	}

	public String cancel() {
		if (update) {
			facesMessages.add(FacesMessage.SEVERITY_INFO,
					"Person '#{updatePersonAction.person.displayName}' not updated");
		} else {
			facesMessages.add(FacesMessage.SEVERITY_INFO, "New person not added");
		}

		conversationService.endConversation();

		return OxTrustConstants.RESULT_SUCCESS;
	}

	/**
	 * Saves person to ldap
	 * 
	 * @return String describing success of the operation
	 */
	public String save() throws Exception {
		if (!organizationService.isAllowPersonModification()) {
			return OxTrustConstants.RESULT_FAILURE;
		}
		if (!update) {

			if (!isValidPassword()) {
				facesMessages.add(FacesMessage.SEVERITY_ERROR, "Password length must be between 3 and 60 characters");
				return OxTrustConstants.RESULT_FAILURE;
			}

			if (!userNameIsUniqAtCreationTime(this.person.getUid())) {
				facesMessages.add(FacesMessage.SEVERITY_ERROR, "#{msgs['UpdatePersonAction.faileAddUserUidExist']} %s",
						this.person.getUid());
				return OxTrustConstants.RESULT_FAILURE;
			}
			if (appConfiguration.getEnforceEmailUniqueness()) {
				if (!userEmailIsUniqAtCreationTime(this.person.getAttribute(MAIL))) {
					facesMessages.add(FacesMessage.SEVERITY_ERROR,
							"#{msgs['UpdatePersonAction.faileUpdateUserMailidExist']} %s",
							this.person.getAttribute(MAIL));
					return OxTrustConstants.RESULT_FAILURE;
				}
			}

		} else {
			if (!userNameIsUniqAtEditionTime(this.person.getUid())) {
				facesMessages.add(FacesMessage.SEVERITY_ERROR, "#{msgs['UpdatePersonAction.faileAddUserUidExist']} %s",
						this.person.getUid());
				return OxTrustConstants.RESULT_FAILURE;
			}
			if (appConfiguration.getEnforceEmailUniqueness()) {
				if (!userEmailIsUniqAtEditionTime(this.person.getAttribute(MAIL))) {
					facesMessages.add(FacesMessage.SEVERITY_ERROR,
							"#{msgs['UpdatePersonAction.faileUpdateUserMailidExist']} %s",
							this.person.getAttribute(MAIL));
					return OxTrustConstants.RESULT_FAILURE;
				}
			}
		}
		updateCustomObjectClasses();
		List<GluuCustomAttribute> removedAttributes = customAttributeAction.detectRemovedAttributes();
		customAttributeAction.updateOriginCustomAttributes();
		List<GluuCustomAttribute> customAttributes = customAttributeAction.getCustomAttributes();
		for (GluuCustomAttribute customAttribute : customAttributes) {
			if (customAttribute.getName().equalsIgnoreCase("gluuStatus")) {
				customAttribute.setValue(gluuStatus);
			}
			if (customAttribute.getName().equalsIgnoreCase("oxTrustActive")) {
				if(gluuStatus.equalsIgnoreCase("active")) {
					customAttribute.setValue(GluuBoolean.TRUE);
					customAttribute.setBooleanValue(GluuBoolean.TRUE);
				}else {
					customAttribute.setValue(GluuBoolean.FALSE);
					customAttribute.setBooleanValue(GluuBoolean.FALSE);
				}
			}
		}
		this.person.setCustomAttributes(customAttributeAction.getCustomAttributes());
		this.person.getCustomAttributes().addAll(removedAttributes);
		// Sync email, in reverse ("oxTrustEmail" <- "mail")
		this.person = syncEmailReverse(this.person, true);
		boolean runScript = externalUpdateUserService.isEnabled();
		if (update) {
			try {
				if (runScript) {
					externalUpdateUserService.executeExternalUpdateUserMethods(this.person);
				}
				personService.updatePerson(this.person);
				oxTrustAuditService.audit(
						"USER " + this.person.getInum() + " **" + this.person.getDisplayName() + "** UPDATED",
						identity.getUser(),
						(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest());
				if (runScript) {
					externalUpdateUserService.executeExternalPostUpdateUserMethods(this.person);
				}
				if(identity.getUser().getUid().equals(this.oldUid)) {
						
						facesMessages.add(FacesMessage.SEVERITY_INFO,
								"Profile '#{userProfileAction.person.displayName}' updated successfully");
						logoutAction.processLogout();
						return OxTrustConstants.RESULT_SUCCESS;
				}
			} catch (DuplicateEmailException ex) {
				log.error("Failed to update person {}", inum, ex);
				facesMessages.add(FacesMessage.SEVERITY_ERROR, ex.getMessage());
				return OxTrustConstants.RESULT_FAILURE;
			} catch (Exception ex) {
				log.error("Failed to update person {}", inum, ex);
				facesMessages.add(FacesMessage.SEVERITY_ERROR,
						"Failed to update person '#{updatePersonAction.person.displayName}'");
				return OxTrustConstants.RESULT_FAILURE;
			}
			facesMessages.add(FacesMessage.SEVERITY_INFO,
					"Person '#{updatePersonAction.person.displayName}' updated successfully");
		} else {

			this.inum = personService.generateInumForNewPerson();
			String dn = personService.getDnForPerson(this.inum);

			// Save person
			this.person.setDn(dn);
			this.person.setInum(this.inum);
			this.person.setUserPassword(this.password);

			List<GluuCustomAttribute> personAttributes = this.person.getCustomAttributes();
			if (!personAttributes.contains(new GluuCustomAttribute("cn", ""))) {
				List<GluuCustomAttribute> changedAttributes = new ArrayList<GluuCustomAttribute>();
				changedAttributes.addAll(personAttributes);
				changedAttributes.add(
						new GluuCustomAttribute("cn", this.person.getGivenName() + " " + this.person.getDisplayName()));
				this.person.setCustomAttributes(changedAttributes);
			} else {
				this.person.setCommonName(this.person.getCommonName() + " " + this.person.getGivenName());
			}

			try {
				if (runScript) {
					externalUpdateUserService.executeExternalAddUserMethods(this.person);
				}
				personService.addPerson(this.person);
				oxTrustAuditService.audit(
						"USER " + this.person.getInum() + " **" + this.person.getDisplayName() + "** ADDED",
						identity.getUser(),
						(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest());
				if (runScript) {
					externalUpdateUserService.executeExternalPostAddUserMethods(this.person);
				}
			} catch (DuplicateEmailException ex) {
				log.error("Failed to add new person {}", this.person.getInum(), ex);
				facesMessages.add(FacesMessage.SEVERITY_ERROR, ex.getMessage());
				return OxTrustConstants.RESULT_FAILURE;
			} catch (Exception ex) {
				log.error("Failed to add new person {}", this.person.getInum(), ex);
				facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to add new person'");
				return OxTrustConstants.RESULT_FAILURE;
			}
			facesMessages.add(FacesMessage.SEVERITY_INFO,
					"New person '#{updatePersonAction.person.displayName}' added successfully");
			conversationService.endConversation();
			this.update = true;
		}

		return OxTrustConstants.RESULT_SUCCESS;
	}

	/**
	 * One-way sync from "mail" to "oxTrustEmail". This method takes current values
	 * of "oxTrustEmail" attribute, deletes those that do not match any of those in
	 * "mail", and adds new ones that are missing.
	 * 
	 * @param gluuCustomPerson
	 * @param isScim2
	 * @return
	 * @throws Exception
	 */
	public GluuCustomPerson syncEmailReverse(GluuCustomPerson gluuCustomPerson, boolean isScim2) throws Exception {

		/*
		 * Implementation of this method could not be simplified to creating a new empty
		 * list for oxTrustEmail and then do the respective additions based on current
		 * mail values since information such as display, primary, etc. would be lost.
		 * Instead, it uses set operations to know which existing entries must be
		 * removed or retained, and then apply additions of new data.
		 */
		log.info(" IN Utils.syncEmailReverse()...");

		GluuCustomAttribute mail = gluuCustomPerson.getGluuCustomAttribute("mail");
		GluuCustomAttribute oxTrustEmail = gluuCustomPerson.getGluuCustomAttribute("oxTrustEmail");

		if (mail == null) {
			gluuCustomPerson.setAttribute("oxTrustEmail", new String[0]);
		} else {
			Set<String> mailSet = new HashSet<String>();
			if (mail.getValues() != null)
				mailSet.addAll(Arrays.asList(mail.getStringValues()));

			Set<String> mailSetCopy = new HashSet<String>(mailSet);
			Set<String> oxTrustEmailSet = new HashSet<String>();
			List<Email> oxTrustEmails = new ArrayList<Email>();

			if (oxTrustEmail != null && oxTrustEmail.getValues() != null) {
				for (String oxTrustEmailJson : oxTrustEmail.getStringValues()) {
					oxTrustEmails.add(jsonService.jsonToObject(oxTrustEmailJson, Email.class));
				}

				for (Email email : oxTrustEmails) {
					oxTrustEmailSet.add(email.getValue());
				}
			}
			mailSetCopy.removeAll(oxTrustEmailSet); // Keep those in "mail" and not in oxTrustEmail
			oxTrustEmailSet.removeAll(mailSet); // Keep those in oxTrustEmail and not in "mail"

			List<Integer> delIndexes = new ArrayList<Integer>();
			// Build a list of indexes that should be removed in oxTrustEmails
			for (int i = 0; i < oxTrustEmails.size(); i++) {
				if (oxTrustEmailSet.contains(oxTrustEmails.get(i).getValue())) {
					delIndexes.add(0, i);
				}
			}
			// Delete unmatched oxTrustEmail entries from highest index to lowest
			for (Integer idx : delIndexes) {
				oxTrustEmails.remove(idx.intValue()); // must not pass an Integer directly
			}

			List<String> newValues = new ArrayList<String>();
			for (Email email : oxTrustEmails) {
				newValues.add(jsonService.objectToPerttyJson(email));
			}

			for (String mailStr : mailSetCopy) {
				Email email = new Email();
				email.setValue(mailStr);
				email.setPrimary(false);
				newValues.add(jsonService.objectToPerttyJson(email));
			}

			gluuCustomPerson.setAttribute("oxTrustEmail", newValues.toArray(new String[0]));

		}

		log.info(" LEAVING Utils.syncEmailReverse()...");

		return gluuCustomPerson;

	}

	private void updateCustomObjectClasses() {
		personService.addCustomObjectClass(this.person);

		// Update objectClasses
		String[] allObjectClasses = ArrayHelper.arrayMerge(appConfiguration.getPersonObjectClassTypes(),
				this.person.getCustomObjectClasses());
		String[] resultObjectClasses = new HashSet<String>(Arrays.asList(allObjectClasses)).toArray(new String[0]);

		this.person.setCustomObjectClasses(resultObjectClasses);
	}

	/**
	 * Delete selected person from ldap
	 * 
	 * @return String describing success of the operation
	 * @throws Exception
	 */
	public String delete() {
		if (!organizationService.isAllowPersonModification()) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR,
					"Failed to remove person '#{updatePersonAction.person.displayName}'");
			return OxTrustConstants.RESULT_FAILURE;
		}

		if (update) {
			// Remove person
			try {
				boolean runScript = externalUpdateUserService.isEnabled();
				if (runScript) {
					externalUpdateUserService.executeExternalDeleteUserMethods(this.person);
				}
				memberService.removePerson(this.person);
				oxTrustAuditService.audit(
						"USER " + this.person.getInum() + " **" + this.person.getDisplayName() + "** REMOVED",
						identity.getUser(),
						(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest());
				if (runScript) {
					externalUpdateUserService.executeExternalPostDeleteUserMethods(this.person);
				}

				facesMessages.add(FacesMessage.SEVERITY_INFO,
						"Person '#{updatePersonAction.person.displayName}' removed successfully");
				conversationService.endConversation();

				return OxTrustConstants.RESULT_SUCCESS;
			} catch (BasePersistenceException ex) {
				log.error("Failed to remove person {}", this.person.getInum(), ex);
			}
		}

		facesMessages.add(FacesMessage.SEVERITY_ERROR,
				"Failed to remove person '#{updatePersonAction.person.displayName}'");

		return OxTrustConstants.RESULT_FAILURE;
	}

	private void initAttributes(boolean add) {
		if (add && externalUpdateUserService.isEnabled()) {
			externalUpdateUserService.executeExternalNewUserMethods(this.person);
		}

		List<GluuAttribute> attributes = attributeService.getAllPersonAttributes(GluuUserRole.ADMIN);
		List<String> origins = attributeService.getAllAttributeOrigins(attributes);

		List<GluuCustomAttribute> customAttributes = this.person.getCustomAttributes();
		boolean newPerson = (customAttributes == null) || customAttributes.isEmpty();
		if (newPerson) {
			customAttributes = new ArrayList<GluuCustomAttribute>();
			this.person.setCustomAttributes(customAttributes);
		}

		customAttributeAction.initCustomAttributes(attributes, customAttributes, origins,
				appConfiguration.getPersonObjectClassTypes(), appConfiguration.getPersonObjectClassDisplayNames());

		if (newPerson) {
			customAttributeAction.addCustomAttributes(personService.getMandatoryAtributes());
		}
	}

	public String getGroupName(String dn) {
		if (dn != null) {
			GluuGroup group = groupService.getGroupByDn(dn);
			if (group != null) {
				String groupName = group.getDisplayName();
				if (groupName != null && !groupName.isEmpty()) {
					return groupName;
				}
			}
		}
		return "invalid group name";

	}

	/**
	 * Returns person's inum
	 * 
	 * @return inum
	 */
	public String getInum() {
		return inum;
	}

	/**
	 * Sets person's inum
	 * 
	 * @param inum
	 */
	public void setInum(String inum) {
		this.inum = inum;
	}

	/**
	 * Returns person
	 * 
	 * @return GluuCustomPerson
	 */
	public GluuCustomPerson getPerson() {
		return person;
	}

	/**
	 * Return true if person is being updated, false if adding new person
	 * 
	 * @return
	 */
	public boolean isUpdate() {
		return update;
	}

	public List<String> getActiveInactiveStatuses() {
		return appConfiguration.getSupportedUserStatus();
	}

	public void validateConfirmPassword(FacesContext context, UIComponent comp, Object value) {
		Pattern pattern = null;
		String attributeValue = (String) value;
		if (StringHelper.isEmpty(attributeValue)) {
			FacesMessage message = new FacesMessage("Value is required");
			message.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ValidatorException(message);
		}
		AttributeValidation validation = attributeService.getAttributeByName("userPassword").getAttributeValidation();
		boolean canValidate = validation != null && validation.getRegexp() != null && !validation.getRegexp().isEmpty();
		if (comp.getClientId().endsWith("custpasswordId")) {
			this.password = (String) value;
		} else if (comp.getClientId().endsWith("custconfirmpasswordId")) {
			this.confirmPassword = (String) value;
		}
		this.confirmPassword = this.confirmPassword == null ? "" : this.confirmPassword;
		if (canValidate) {
			pattern = Pattern.compile(validation.getRegexp());
		}
		if (!StringHelper.equalsIgnoreCase(password, confirmPassword) && this.confirmPassword != null) {
			((UIInput) comp).setValid(false);
			FacesMessage message = new FacesMessage("Both passwords should be the same!");
			message.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ValidatorException(message);
		}
		if (canValidate
				&& (!pattern.matcher(this.password).matches() || !pattern.matcher(this.confirmPassword).matches())) {
			((UIInput) comp).setValid(false);
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					facesMessages.evalResourceAsString("#{msgs['password.validation.invalid']}"),
					facesMessages.evalResourceAsString("#{msgs['password.validation.invalid']}"));
			context.addMessage(comp.getClientId(context), message);
		}
	}

	public boolean isValidPassword() {
		return isValid(this.password) && isValid(this.confirmPassword);
	}

	private boolean isValid(String password) {
		return password != null && password.length() >= 3 && password.length() < 60;
	}

	public void removePairWiseIdentifier(GluuUserPairwiseIdentifier current) {
		this.userPairWideIdentifiers.remove(current);
		boolean result = pairwiseIdService.removePairWiseIdentifier(this.person, current);
		if (result) {
			facesMessages.add(FacesMessage.SEVERITY_INFO, "Successfully remove");
		} else {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Error while removing: check the log for details");
		}
	}

	public void removeDevice() {
		try {
			String idOfDeviceToRemove = deviceToBeRemove.getId();
			removeFidoDevice(deviceToBeRemove, idOfDeviceToRemove);
			removeFido2Device(this.person, deviceToBeRemove);
			removeOxExternalUid(deviceToBeRemove, idOfDeviceToRemove);
			removeOTPDevices(deviceToBeRemove, idOfDeviceToRemove);
			removeMobileDevice(deviceToBeRemove, idOfDeviceToRemove);
		} catch (Exception e) {
			log.error("Failed to remove device ", e);
		}
	}

	private void removeOxExternalUid(GluuDeviceDataBean deleteDeviceData, String idOfDeviceToRemove) {
		List<String> oxExternalUids = this.person.getOxExternalUid();
		if (oxExternalUids != null) {
			for (String oxExternalUid : oxExternalUids) {
				if (idOfDeviceToRemove.equals(oxExternalUid.split(COLON)[1])) {
					if (!oxExternalUid.startsWith(HOTP) && !oxExternalUid.startsWith(TOTP)) {
						oxExternalUids.remove(oxExternalUid);
						this.person.setOxExternalUid(oxExternalUids);
						this.deviceDataMap.remove(deleteDeviceData);
						break;
					}
				}
			}
			return;
		}
	}

	private void removeFidoDevice(GluuDeviceDataBean deleteDeviceData, String idOfDeviceToRemove) {
		List<GluuCustomFidoDevice> gluuCustomFidoDevices = fidoDeviceService.searchFidoDevices(this.person.getInum());
		if (gluuCustomFidoDevices != null) {
			for (GluuCustomFidoDevice gluuCustomFidoDevice : gluuCustomFidoDevices) {
				if (gluuCustomFidoDevice.getId().equals(idOfDeviceToRemove)) {
					fidoDeviceService.removeGluuCustomFidoDevice(gluuCustomFidoDevice);
					this.deviceDataMap.remove(deleteDeviceData);
					return;
				}
			}
		}
	}

	private void removeFido2Device(GluuCustomPerson person, GluuDeviceDataBean device) {
		try {
			fido2DeviceService.removeFido2(person, device.getId());
			this.deviceDataMap.remove(device);
		} catch (Exception e) {
			log.warn("Error Deleting fido2 devices", e);
		}
	}

	private void removeMobileDevice(GluuDeviceDataBean deleteDeviceData, String idOfDeviceToRemove) throws IOException {
		String oxMobileDevices = this.person.getOxMobileDevices();
		if (oxMobileDevices != null && !oxMobileDevices.trim().equals("")) {
			ObjectMapper mapper = new ObjectMapper();
			MobileDevice mobileDevice = mapper.readValue(oxMobileDevices, MobileDevice.class);
			ArrayList<Phone> phones = mobileDevice.getPhones();
			if (phones != null && phones.size() > 0) {
				for (Phone phone : phones) {
					if (phone.getNumber().equals(idOfDeviceToRemove)) {
						deviceDataMap.remove(deleteDeviceData);
						phones.remove(phone);
						Map<String, ArrayList<Phone>> map = new HashMap<String, ArrayList<Phone>>();
						map.put("phones", phones);
						String jsonInString = mapper.writeValueAsString(map);
						this.person.setOxMobileDevices(jsonInString);
						String[] mobiles = this.person.getAttributeStringValues(MOBILE);
						if (mobiles != null && mobiles.length > 0) {
							List<String> values = new ArrayList<String>(Arrays.asList(mobiles));
							for (String mobile : values) {
								if (mobile.equalsIgnoreCase(idOfDeviceToRemove)) {
									values.remove(mobile);
									this.person.setAttribute(MOBILE, values.toArray(new String[values.size()]));
									break;
								}
							}
						}

						return;
					}
				}
			}
		}
	}

	private void removeOTPDevices(GluuDeviceDataBean deleteDeviceData, String idOfDeviceToRemove) {
		OTPDevice oxOTPDevices = this.person.getOxOTPDevices();
		ArrayList<Device> devices = new ArrayList<Device>();
		if (oxOTPDevices != null) {
			devices = oxOTPDevices.getDevices();
		}

		String entry = null;
		String code = null;
		List<String> uids = new ArrayList<String>(this.person.getOxExternalUid());
		if (uids != null) {
			for (String oxExternalUid : uids) {
				String firstPart = oxExternalUid.split(COLON)[0];
				if (firstPart.equalsIgnoreCase(TOTP) || firstPart.equalsIgnoreCase(HOTP)) {
					String key = oxExternalUid.replaceFirst("hotp:", "").replaceFirst("totp:", "");
					int idx = key.indexOf(";");
					if (idx > 0) {
						key = key.substring(0, idx);
					}
					if (idOfDeviceToRemove.equalsIgnoreCase(key)) {
						entry = oxExternalUid;
						code = String.valueOf(key.hashCode());
						break;
					}
				}
			}
		}
		if (entry != null && code != null) {
			uids.remove(entry);
			this.person.setOxExternalUid(uids);
			if (devices != null && devices.size() > 0) {
				for (Device device : devices) {
					if (device.getId().equalsIgnoreCase(code)) {
						devices.remove(device);
						this.person.getOxOTPDevices().setDevices(devices);
						this.deviceDataMap.remove(deleteDeviceData);
						break;
					}
				}
			}
			return;
		}
	}

	private DeviceData getDeviceata(String data) {
		ObjectMapper mapper = new ObjectMapper();
		DeviceData obj = null;
		try {
			obj = mapper.readValue(data, DeviceData.class);
		} catch (IOException e) {
			log.error("Failed to convert device string to object IOException", e);
		}
		return obj;
	}

	public void fetchFidoRecord(String id) {
		this.fidoDevice = fidoDeviceService.getGluuCustomFidoDeviceById(this.person.getInum(), id);
		if (this.fidoDevice.getDeviceData() != null) {
			this.deviceDetail = getDeviceata(this.fidoDevice.getDeviceData());
		} else {
			this.deviceDetail = null;
		}

	}

	public boolean userNameIsUniqAtCreationTime(String uid) {
		boolean userNameIsUniq = true;
		if (uid == null) {
			return userNameIsUniq;
		}
		List<GluuCustomPerson> gluuCustomPersons = personService.getPersonsByUid(uid);
		if (gluuCustomPersons != null && gluuCustomPersons.size() > 0) {
			for (GluuCustomPerson gluuCustomPerson : gluuCustomPersons) {
				if (gluuCustomPerson.getUid().equalsIgnoreCase(uid)) {
					userNameIsUniq = false;
					break;
				}
			}
		}
		return userNameIsUniq;
	}

	private boolean userNameIsUniqAtEditionTime(String uid) {
		if (uid == null) {
			return true;
		}
		boolean userNameIsUniq = false;
		List<GluuCustomPerson> gluuCustomPersons = personService.getPersonsByUid(uid);
		if (gluuCustomPersons == null || gluuCustomPersons.isEmpty()) {
			userNameIsUniq = true;
		}
		if (gluuCustomPersons.size() == 1 && gluuCustomPersons.get(0).getUid().equalsIgnoreCase(uid)
				&& gluuCustomPersons.get(0).getInum().equalsIgnoreCase(this.person.getInum())) {
			userNameIsUniq = true;
		}
		return userNameIsUniq;
	}

	private boolean userEmailIsUniqAtCreationTime(String email) {
		if (email == null) {
			return true;
		}
		boolean emailIsUniq = true;
		List<GluuCustomPerson> gluuCustomPersons = personService.getPersonsByEmail(email);
		if (gluuCustomPersons != null && gluuCustomPersons.size() > 0) {
			for (GluuCustomPerson gluuCustomPerson : gluuCustomPersons) {
				if (gluuCustomPerson.getAttribute(MAIL).equalsIgnoreCase(email)) {
					emailIsUniq = false;
					break;
				}
			}
		}
		return emailIsUniq;
	}

	private boolean userEmailIsUniqAtEditionTime(String email) {
		boolean emailIsUniq = false;
		if (email == null) {
			return true;
		}
		List<GluuCustomPerson> gluuCustomPersons = personService.getPersonsByEmail(email);
		if (gluuCustomPersons == null || gluuCustomPersons.isEmpty()) {
			emailIsUniq = true;
		}
		if (gluuCustomPersons.size() == 1 && gluuCustomPersons.get(0).getAttribute(MAIL).equalsIgnoreCase(email)
				&& gluuCustomPersons.get(0).getInum().equalsIgnoreCase(this.person.getInum())) {
			emailIsUniq = true;
		}

		return emailIsUniq;
	}

	public String getClientName(String inum) {
		OxAuthClient result = clientService.getClientByInum(inum);
		if (result != null) {
			return result.getDisplayName();
		} else {
			return "";
		}
	}

	public GluuDeviceDataBean getDeviceToBeRemove() {
		return deviceToBeRemove;
	}

	public void setDeviceToBeRemove(GluuDeviceDataBean deviceToBeRemove) {
		this.deviceToBeRemove = deviceToBeRemove;
	}
}
