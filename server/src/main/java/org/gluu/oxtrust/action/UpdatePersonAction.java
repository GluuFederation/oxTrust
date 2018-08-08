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

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.ldap.service.FidoDeviceService;
import org.gluu.oxtrust.ldap.service.GroupService;
import org.gluu.oxtrust.ldap.service.IPersonService;
import org.gluu.oxtrust.ldap.service.MemberService;
import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.model.Device;
import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.model.MobileDevice;
import org.gluu.oxtrust.model.OTPDevice;
import org.gluu.oxtrust.model.Phone;
import org.gluu.oxtrust.model.fido.GluuCustomFidoDevice;
import org.gluu.oxtrust.model.fido.GluuDeviceDataBean;
import org.gluu.oxtrust.service.external.ExternalUpdateUserService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.oxtrust.util.ServiceUtil;
import org.gluu.persist.PersistenceEntryManager;
import org.gluu.persist.exception.BasePersistenceException;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.model.GluuAttribute;
import org.xdi.model.GluuStatus;
import org.xdi.model.user.UserRole;
import org.xdi.oxauth.model.fido.u2f.protocol.DeviceData;
import org.xdi.service.security.Secure;
import org.xdi.util.ArrayHelper;
import org.xdi.util.StringHelper;

/**
 * Action class for updating person's attributes
 * 
 * @author Yuriy Movchan Date: 10.23.2010
 */
@ConversationScoped
@Named("updatePersonAction")
@Secure("#{permissionService.hasPermission('person', 'access')}")
public class UpdatePersonAction implements Serializable {

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
	private IPersonService personService;

	@Inject
	private CustomAttributeAction customAttributeAction;

	@Inject
	private UserPasswordAction userPasswordAction;

	@Inject
	private WhitePagesAction whitePagesAction;

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

	private GluuStatus gluuStatus;

	private String password;
	
	private String confirmPassword;
	
	private List <GluuDeviceDataBean> deviceDataMap;
	
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

	private List<String> externalAuthCustomAttributes;

	private DeviceData deviceDetail;


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

	public GluuStatus getGluuStatus() {
		return gluuStatus;
	}

	public void setGluuStatus(GluuStatus gluuStatus) {
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
			log.error("Failed to find person {}", inum, ex);

			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to find person");
			conversationService.endConversation();

			return OxTrustConstants.RESULT_FAILURE;
		}

		initAttributes(false);
		try {
		this.gluuStatus = this.person.getStatus();
		List <String> oxexternal = this.person.getOxExternalUid();
		OTPDevice oxOTPDevices = this.person.getOxOTPDevices();
		externalAuthCustomAttributes = new ArrayList<String>();
		String oxMobileDevices = this.person.getOxMobileDevices();
		if(oxexternal != null && oxexternal.size()>0){
			for(String oxexternalStr : oxexternal){
				String [] args = oxexternalStr.split(":");
				externalAuthCustomAttributes.add(args[0]);							
			}			
		}
		
	
			List<GluuCustomFidoDevice>  gluuCustomFidoDevices = fidoDeviceService.searchFidoDevices( this.person.getInum(),null);
			deviceDataMap = new ArrayList<GluuDeviceDataBean>();
			if(gluuCustomFidoDevices != null){
				for( GluuCustomFidoDevice gluuCustomFidoDevice : gluuCustomFidoDevices){					
	                GluuDeviceDataBean gluuDeviceDataBean= new GluuDeviceDataBean();
	                gluuDeviceDataBean.setCreationDate(ldapEntryManager.decodeTime(gluuCustomFidoDevice.getCreationDate()).toGMTString());
	                gluuDeviceDataBean.setId(gluuCustomFidoDevice.getId());
	                String devicedata = gluuCustomFidoDevice.getDeviceData();
	                String modality = "";
	                String nickName = "";
	                if(devicedata != null){
	                	DeviceData deviceData = getDeviceata(devicedata);
	                	//nickName = deviceData.getName();
	                	nickName = gluuCustomFidoDevice.getNickname();
		                modality = "Super-Gluu Device";
	                }else{
	                	//nickName = "U2F";
	                	nickName = gluuCustomFidoDevice.getNickname();
	                	modality = "U2F device";
	                }
	                gluuDeviceDataBean.setNickName(nickName);
	                gluuDeviceDataBean.setModality(modality);
	                deviceDataMap.add(gluuDeviceDataBean); 
				}
			}			
			
			/*if (oxOTPDevices != null && !oxOTPDevices.trim().equals("")) {
				ObjectMapper mapper = new ObjectMapper();
				OTPDevice oTPDevice = mapper.readValue(oxOTPDevices, OTPDevice.class);*/
				ArrayList<Device> devices = oxOTPDevices.getDevices();

				if (devices != null && devices.size() > 0) {
					for (Device device : devices) {
						GluuDeviceDataBean gluuDeviceDataBean = new GluuDeviceDataBean();
						gluuDeviceDataBean.setNickName(device.getNickName());
						gluuDeviceDataBean.setModality("OTP Device");
						gluuDeviceDataBean.setId(device.getId());
						Timestamp stamp = new Timestamp(Long.valueOf(device.getAddedOn()).longValue());
						gluuDeviceDataBean.setCreationDate(stamp.toGMTString());
						deviceDataMap.add(gluuDeviceDataBean);
					}
				}
			//}

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
			if (oxexternal != null && oxexternal.size() > 0) {
				for (String oxexternalStr : oxexternal) {
					String[] args = oxexternalStr.split(":");
					GluuDeviceDataBean gluuDeviceDataBean = new GluuDeviceDataBean();
					gluuDeviceDataBean.setNickName(args[0]);
					gluuDeviceDataBean.setModality("Passport");
					gluuDeviceDataBean.setId("-");
					gluuDeviceDataBean.setCreationDate("-");					
					deviceDataMap.add(gluuDeviceDataBean);

				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		userPasswordAction.setPerson(this.person);

		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String cancel() {
		if (update) {
			facesMessages.add(FacesMessage.SEVERITY_INFO, "Person '#{updatePersonAction.person.displayName}' not updated");
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
		
		
		if(!validatePerson(this.person)){
			return OxTrustConstants.RESULT_FAILURE;
		}			

		updateCustomObjectClasses();

		List<GluuCustomAttribute> removedAttributes = customAttributeAction.detectRemovedAttributes();
		customAttributeAction.updateOriginCustomAttributes();

		List<GluuCustomAttribute> customAttributes = customAttributeAction.getCustomAttributes();
		for (GluuCustomAttribute customAttribute : customAttributes) {
			if (customAttribute.getName().equalsIgnoreCase("gluuStatus")) {
				customAttribute.setValue(gluuStatus.getValue());
			}
		}

		this.person.setCustomAttributes(customAttributeAction.getCustomAttributes());
		this.person.getCustomAttributes().addAll(removedAttributes);

		// Sync email, in reverse ("oxTrustEmail" <- "mail")
		this.person = ServiceUtil.syncEmailReverse(this.person, true);

		boolean runScript = externalUpdateUserService.isEnabled();
		if (update) {
			try {
				if (runScript) {
					externalUpdateUserService.executeExternalUpdateUserMethods(this.person);
				}
				personService.updatePerson(this.person);
				if (runScript) {
					externalUpdateUserService.executeExternalPostUpdateUserMethods(this.person);
				}
			} catch (BasePersistenceException ex) {
				log.error("Failed to update person {}", inum, ex);
				facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to update person '#{updatePersonAction.person.displayName}'");

				return OxTrustConstants.RESULT_FAILURE;
			}

			facesMessages.add(FacesMessage.SEVERITY_INFO, "Person '#{updatePersonAction.person.displayName}' updated successfully");
		} else {
			if (personService.getPersonByUid(this.person.getUid()) != null) {
				facesMessages.add(FacesMessage.SEVERITY_ERROR, "Person with the uid '#{updatePersonAction.person.uid}' already exist'");
				return OxTrustConstants.RESULT_DUPLICATE;
			}

			this.inum = personService.generateInumForNewPerson();
			String iname = personService.generateInameForNewPerson(this.person.getUid());
			String dn = personService.getDnForPerson(this.inum);

			// Save person
			this.person.setDn(dn);
			this.person.setInum(this.inum);
			this.person.setIname(iname);
			this.person.setUserPassword(this.password);

			List<GluuCustomAttribute> personAttributes = this.person.getCustomAttributes();
			if (!personAttributes.contains(new GluuCustomAttribute("cn", ""))) {
				List<GluuCustomAttribute> changedAttributes = new ArrayList<GluuCustomAttribute>();
				changedAttributes.addAll(personAttributes);
				changedAttributes.add(new GluuCustomAttribute("cn", this.person.getGivenName() + " " + this.person.getDisplayName()));
				this.person.setCustomAttributes(changedAttributes);
			} else {
				this.person.setCommonName(this.person.getCommonName() + " " + this.person.getGivenName());
			}

			try {
				if (runScript) {
					externalUpdateUserService.executeExternalAddUserMethods(this.person);
				}
				personService.addPerson(this.person);
				if (runScript) {
					externalUpdateUserService.executeExternalPostAddUserMethods(this.person);
				}
			} catch (Exception ex) {
				log.error("Failed to add new person {}", this.person.getInum(), ex);
				facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to add new person'");

				return OxTrustConstants.RESULT_FAILURE;
			}

			facesMessages.add(FacesMessage.SEVERITY_INFO, "New person '#{updatePersonAction.person.displayName}' added successfully");
			conversationService.endConversation();

			this.update = true;
		}

		return OxTrustConstants.RESULT_SUCCESS;
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
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to remove person '#{updatePersonAction.person.displayName}'");
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
				if (runScript) {
					externalUpdateUserService.executeExternalPostDeleteUserMethods(this.person);
				}

				facesMessages.add(FacesMessage.SEVERITY_INFO, "Person '#{updatePersonAction.person.displayName}' removed successfully");
				conversationService.endConversation();

				return OxTrustConstants.RESULT_SUCCESS;
			} catch (BasePersistenceException ex) {
				log.error("Failed to remove person {}", this.person.getInum(), ex);
			}
		}

		facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to remove person '#{updatePersonAction.person.displayName}'");

		return OxTrustConstants.RESULT_FAILURE;
	}

	private void initAttributes(boolean add) {
		if (add && externalUpdateUserService.isEnabled()) {
			externalUpdateUserService.executeExternalNewUserMethods(this.person);
		}

		List<GluuAttribute> attributes = attributeService.getAllPersonAttributes(UserRole.ADMIN);
		List<String> origins = attributeService.getAllAttributeOrigins(attributes);

		List<GluuCustomAttribute> customAttributes = this.person.getCustomAttributes();
		boolean newPerson = (customAttributes == null) || customAttributes.isEmpty();
		if (newPerson) {
			customAttributes = new ArrayList<GluuCustomAttribute>();
			this.person.setCustomAttributes(customAttributes);
		}

		customAttributeAction.initCustomAttributes(attributes, customAttributes, origins, appConfiguration.getPersonObjectClassTypes(),
				appConfiguration.getPersonObjectClassDisplayNames());

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

	public GluuStatus[] getActiveInactiveStatuses() {
		return new GluuStatus[] { GluuStatus.ACTIVE, GluuStatus.INACTIVE };
	}

	public void validateConfirmPassword(FacesContext context, UIComponent comp, Object value) {
		if (comp.getClientId().endsWith("custpasswordId")) {
			this.password = (String) value;
		} else if (comp.getClientId().endsWith("custconfirmpasswordId")) {
			this.confirmPassword = (String) value;
		}

		if (!StringHelper.equalsIgnoreCase(password, confirmPassword)) {
			((UIInput) comp).setValid(false);
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Password and Confirm Password should be same!",
					"Password and Confirm Password should be same!");
			context.addMessage(comp.getClientId(context), message);
		}
	}
	
	public void removeDevice(GluuDeviceDataBean deleteDeviceData){		
		try {
			List<GluuCustomFidoDevice>  gluuCustomFidoDevices = fidoDeviceService.searchFidoDevices( this.person.getInum(),null);
			if(gluuCustomFidoDevices != null){
			for( GluuCustomFidoDevice gluuCustomFidoDevice : gluuCustomFidoDevices){				
                if(gluuCustomFidoDevice.getId().equals(deleteDeviceData.getId())){
                	fidoDeviceService.removeGluuCustomFidoDevice(gluuCustomFidoDevice);
                	this.deviceDataMap.remove(deleteDeviceData);
                	return;
                } 
			}
			}
			
			List <String> list = new ArrayList<String>(this.person.getOxExternalUid());
			if(list != null){
			for( String external : list){				
                if(deleteDeviceData.getId().trim().equals(external.trim())){
                	list.remove(external);
                	this.person.setOxExternalUid(list);
                	this.deviceDataMap.remove(deleteDeviceData);
                	return;
                } 
			}
		}
			
			OTPDevice oxOTPDevices = this.person.getOxOTPDevices();
			/*if (oxOTPDevices != null && !oxOTPDevices.trim().equals("")) {
				ObjectMapper mapper = new ObjectMapper();
				OTPDevice oTPDevice = mapper.readValue(oxOTPDevices, OTPDevice.class);*/
				ArrayList<Device> devices = oxOTPDevices.getDevices();

				if (devices != null && devices.size() > 0) {
					for (Device device : devices) {
						if(deleteDeviceData.getId().equals(device.getId())){						
							deviceDataMap.remove(deleteDeviceData);
							devices.remove(device);
							/*Map<String, ArrayList<Device>> map= new HashMap<String, ArrayList<Device>>();
							map.put("devices", devices);
							String jsonInString = mapper.writeValueAsString(map);*/
							oxOTPDevices.setDevices(devices);
							this.person.setOxOTPDevices(oxOTPDevices);
							return;
						}
					}
				}
			//}

			String oxMobileDevices = this.person.getOxMobileDevices();
			if (oxMobileDevices != null && !oxMobileDevices.trim().equals("")) {
				ObjectMapper mapper = new ObjectMapper();
				MobileDevice mobileDevice = mapper.readValue(oxMobileDevices, MobileDevice.class);
				ArrayList<Phone> phones = mobileDevice.getPhones();

				if (phones != null && phones.size() > 0) {
					for (Phone phone : phones) {
						if(phone.getNumber().equals(deleteDeviceData.getId())){
							deviceDataMap.remove(deleteDeviceData);
							phones.remove(phone);
							Map<String, ArrayList<Phone>> map= new HashMap<String, ArrayList<Phone>>();
							map.put("phones", phones);
							String jsonInString = mapper.writeValueAsString(map);
							this.person.setOxMobileDevices(jsonInString);
							return;
						}
					}
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			 log.error("Failed to remove device ", e);
		}
	}
	
	private DeviceData  getDeviceata(String data) {
		ObjectMapper mapper = new ObjectMapper();

		//JSON from file to Object
		DeviceData obj = null;
		try {
			obj = mapper.readValue(data, DeviceData.class);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			log.error("Failed to convert device string to object JsonParseException", e);
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			log.error("Failed to convert device string to object JsonMappingException", e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("Failed to convert device string to object IOException", e);
		}
		return obj;
	}
	
	public void fetchFidoRecord(String id){
		this.fidoDevice = fidoDeviceService.getGluuCustomFidoDeviceById(this.person.getInum(), id);
		if(this.fidoDevice.getDeviceData() != null){
			this.deviceDetail = getDeviceata(this.fidoDevice.getDeviceData());
		}else{
			this.deviceDetail = null;
		}
		 
	}
	
	private boolean validatePerson(GluuCustomPerson person) throws Exception {
	
		List<GluuCustomPerson>  gluuCustomPersons  = personService.getPersonsByUid(person.getUid());
		if (gluuCustomPersons != null){
			if(!update){
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "#{msg['UpdatePersonAction.faileAddUserUidExist']} %s",
					person.getUid());
					return false;
			}else{
				if(gluuCustomPersons.size()>0){
					for(GluuCustomPerson gluuCustomPerson : gluuCustomPersons){
						if(!gluuCustomPerson.getInum().trim().equals(person.getInum())){
							facesMessages.add(FacesMessage.SEVERITY_ERROR, "#{msg['UpdatePersonAction.faileUpdateUserUidExist']} %s",
									person.getUid());
							return false;
						}
						
					}
				}	
			}
		}
		
		gluuCustomPersons  = personService.getPersonsByEmail(person.getMail());
		if (gluuCustomPersons != null){
			if(!update){
				facesMessages.add(FacesMessage.SEVERITY_ERROR, "#{msg['UpdatePersonAction.faileAddUserMailidExist']} %s",
						person.getMail());
				return false;
			}else{
				if(gluuCustomPersons.size()>0){
					for(GluuCustomPerson gluuCustomPerson : gluuCustomPersons){
						if(!gluuCustomPerson.getInum().trim().equals(person.getInum())){
							facesMessages.add(FacesMessage.SEVERITY_ERROR, "#{msg['UpdatePersonAction.faileUpdateUserMailidExist']} %s",
									person.getMail());
							return false;
						}
					}
				}
			}
			
		}	
		
		return true;
	}
}
