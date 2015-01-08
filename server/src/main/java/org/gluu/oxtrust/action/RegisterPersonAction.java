/*
 * oxTrust is available under the MIT License (2008). 
 * See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;
import net.tanesha.recaptcha.ReCaptchaResponse;

import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.ldap.service.PersonService;
import org.gluu.oxtrust.ldap.service.RegistrationLinkService;
import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuOrganization;
import org.gluu.oxtrust.model.OxLink;
import org.gluu.oxtrust.model.RegistrationConfiguration;
import org.gluu.oxtrust.service.external.RegistrationInterceptionService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.oxtrust.util.RecaptchaUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.log.Log;
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.ldap.model.GluuStatus;
import org.xdi.model.GluuAttribute;
import org.xdi.model.GluuUserRole;

/**
 * User: Dejan Maric
 */
@Scope(ScopeType.CONVERSATION)
@Name("registerPersonAction")
@Data
public class RegisterPersonAction implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Logger
    private Log log;

    private String invitationGuid;

    @In(value = "#{facesContext.externalContext}")
    private ExternalContext externalContext;

    @In
    private RegistrationLinkService registrationLinkService;

    @In
    private AttributeService attributeService;

    @In
    private OrganizationService organizationService;

    @In(create = true)
    @Out(scope = ScopeType.CONVERSATION)
    private CustomAttributeAction customAttributeAction;

    private GluuCustomPerson person;

    @In
    private PersonService personService;

    private List<String> hiddenAttributes;

    private String inum;

    @NotNull
    @Size(min = 2, max = 30,
                    message = "Length of password should be between 2 and 30")
    private String password;

    @NotNull
    @Size(min = 2, max = 30,
                    message = "Length of password should be between 2 and 30")
    private String repeatPassword;

    @In
    private FacesMessages facesMessages;

    @In(value = "#{oxTrustConfiguration.applicationConfiguration}")
    private ApplicationConfiguration applicationConfiguration;

    private String redirectUri;

    @In
    private RegistrationInterceptionService registrationInterceptionService;

    private Map<String, String[]> requestParameters
        = new HashMap<String, String[]>();

    private boolean captchaDisabled = false;
    /**
     * Initializes attributes for registering new person
     *
     * @return String describing success of the operation
     * @throws Exception
     */
    public String initPerson(){
        if (this.person != null) {
            return OxTrustConstants.RESULT_SUCCESS;
        }

        requestParameters.putAll(
            externalContext.getRequestParameterValuesMap());
        GluuOrganization organization = organizationService.getOrganization();
        RegistrationConfiguration config
            = organization.getOxRegistrationConfiguration();
        boolean registrationCustomized = config != null;
        boolean inviteCodesActive
            = registrationCustomized
                && config.isInvitationCodesManagementEnabled();
        boolean inviteCodeOptional
            = registrationCustomized
                && inviteCodesActive
                && config.isUninvitedRegistrationAllowed();
        
        this.captchaDisabled
        = registrationCustomized
            && config.isCaptchaDisabled();

        if((! inviteCodesActive) && (invitationGuid != null)){
            return OxTrustConstants.RESULT_DISABLED;
        }

        if(inviteCodesActive
            && (! inviteCodeOptional)
            && invitationGuid == null){
            return OxTrustConstants.RESULT_NO_PERMISSIONS;
        }

        if(inviteCodesActive && (invitationGuid != null)){
            OxLink invitationLink
                = registrationLinkService.getLinkByGuid(invitationGuid);
            if(invitationLink == null){
                return OxTrustConstants.RESULT_FAILURE;
            }
        }


        if(inum == null){
            this.person = new GluuCustomPerson();
        }else{
            this.person = personService.getPersonByInum(inum);
            if(GluuStatus.ACTIVE.equals(person.getStatus())
                || GluuStatus.INACTIVE.equals(person.getStatus())){
                return OxTrustConstants.RESULT_NO_PERMISSIONS;
            }
        }
        initAttributes();
        boolean result
            = registrationInterceptionService.runInitRegistrationScripts(
                                            this.person, requestParameters);
        if(result){
            return OxTrustConstants.RESULT_SUCCESS;
        }else{
            return OxTrustConstants.RESULT_FAILURE;
        }
    }

    public String register() {
        GluuOrganization organization = organizationService.getOrganization();
        RegistrationConfiguration registrationConfig
            = organization.getOxRegistrationConfiguration();
        boolean registrationCustomized = registrationConfig != null;
        this.captchaDisabled
            = registrationCustomized
                && registrationConfig.isCaptchaDisabled();
        ReCaptchaResponse reCaptchaResponse = null;
        if(! captchaDisabled){
            reCaptchaResponse
                = RecaptchaUtils.getRecaptchaResponseFromServletContext();
        }
        if (captchaDisabled || reCaptchaResponse != null && reCaptchaResponse.isValid() && password.equals(repeatPassword)) {
            String customObjectClass = attributeService.getCustomOrigin();


            this.person.setCustomObjectClasses(
                new String[] { customObjectClass });

            // Save person
            if(person.getInum() == null){
                String inum = personService.generateInumForNewPerson();
                this.person.setInum(inum);
            }

            if(person.getIname() == null){
                String iname
                    = personService.generateInameForNewPerson(
                                                this.person.getUid());
                this.person.setIname(iname);
            }

            if(person.getDn() == null){
                String dn = personService.getDnForPerson(this.person.getInum());
                this.person.setDn(dn);
            }


            boolean invitationCodeAllowed = registrationCustomized
                    && registrationConfig.isInvitationCodesManagementEnabled();
//          boolean invitationCodeOptional = registrationCustomized
//                    && registrationConfig.isUninvitedRegistrationAllowed();
            boolean  invitationCodePresent = invitationGuid != null;
            OxLink invitationLink
                = registrationLinkService.getLinkByGuid(invitationGuid);
            boolean invitationCodeModerated
                = invitationCodePresent && invitationLink != null
                    && invitationLink.getLinkModerated();

            if(invitationCodePresent && invitationCodeAllowed){
                this.person.setOxInviteCode(invitationGuid);
                registrationLinkService.addPendingUser(invitationLink,
                                                       this.person.getInum());
            }

            if( invitationCodeModerated ){
                this.person.setStatus(GluuStatus.INACTIVE);
            } else {
                this.person.setStatus(GluuStatus.ACTIVE);
            }

            List<GluuCustomAttribute> personAttributes
                = this.person.getCustomAttributes();
            if (!personAttributes.contains(
                                    new GluuCustomAttribute("cn", ""))) {
                List<GluuCustomAttribute> changedAttributes
                                    = new ArrayList<GluuCustomAttribute>();
                changedAttributes.addAll(personAttributes);
                changedAttributes.add(
                        new GluuCustomAttribute("cn",
                                    this.person.getGivenName()
                                        + " "
                                        + this.person.getSurname()));
                this.person.setCustomAttributes(changedAttributes);
            } else {
                this.person.setCommonName(this.person.getCommonName());
            }

            // save password
            this.person.setUserPassword(password);
            this.person.setOxCreationTimestamp(new Date());

            try {

                boolean result = registrationInterceptionService
                        .runPreRegistrationScripts(this.person,
                                                   requestParameters);
                if(! result){
                    return OxTrustConstants.RESULT_FAILURE;
                }
                if(this.inum != null){
                    personService.updatePerson(this.person);
                }else{
                    personService.addPerson(this.person);
                }
                result = registrationInterceptionService
                    .runPostRegistrationScripts(this.person,
                                                requestParameters);

                Events.instance().raiseEvent(
                            OxTrustConstants.EVENT_PERSON_SAVED,
                            this.person, null, null, null, null, true);
                if(! result){
                    return OxTrustConstants.RESULT_FAILURE;
                }
            } catch (Exception ex) {
                log.error("Failed to add new person {0}", ex,
                          this.person.getInum());
                facesMessages.add(StatusMessage.Severity.ERROR,
                                  "Failed to add new person");
                return OxTrustConstants.RESULT_FAILURE;
            }

            if(redirectUri == null){
                redirectUri = applicationConfiguration.getApplianceUrl()
                                + externalContext.getRequestContextPath()
                                + "/postRegister.htm";
            }

            return OxTrustConstants.RESULT_SUCCESS;
        }
        return OxTrustConstants.RESULT_CAPTCHA_VALIDATION_FAILED;
    }

    public void cancel() {
    }

    private void initAttributes(){
        List<GluuAttribute> attributes
            = attributeService.getAllPersonAttributes(GluuUserRole.ADMIN);
        List<String> origins
            = attributeService.getAllAttributeOrigins(attributes);
        GluuOrganization organization = organizationService.getOrganization();

        List<GluuCustomAttribute> customAttributes
            = this.person.getCustomAttributes();
        boolean newPerson = (customAttributes == null)
                                || customAttributes.isEmpty();
        if (newPerson) {
            customAttributes = new ArrayList<GluuCustomAttribute>();
            this.person.setCustomAttributes(customAttributes);
        }

        customAttributeAction
            .initCustomAttributes(attributes, customAttributes, origins,
                applicationConfiguration.getPersonObjectClassTypes(),
                applicationConfiguration.getPersonObjectClassDisplayNames());

        List<GluuCustomAttribute> mandatoryAttributes
                                        = new ArrayList<GluuCustomAttribute>();

        RegistrationConfiguration config
                            = organization.getOxRegistrationConfiguration();
        boolean registrationCustomized = config != null;
        boolean registrationAttributesCustomized
            = registrationCustomized
                && config.getAdditionalAttributes() !=null
                && ! config.getAdditionalAttributes().isEmpty();
        if(registrationAttributesCustomized){
            for(String attributeInum: config.getAdditionalAttributes()){
                GluuAttribute attribute
                    = attributeService.getAttributeByInum(attributeInum);
                mandatoryAttributes.add(
                        new GluuCustomAttribute(attribute.getName(),
                                                "", false, false));
            }
        }
        for (GluuCustomAttribute attribute:
                                    personService.getMandatoryAtributes()){
            if(! mandatoryAttributes.contains(attribute)){
                mandatoryAttributes.add(attribute);
            }
        }
        mandatoryAttributes.addAll(personService.getMandatoryAtributes());


        if (newPerson) {
            customAttributeAction.addCustomAttributes(mandatoryAttributes);
        }

        hiddenAttributes = new ArrayList<String>();
        hiddenAttributes.add("inum");
        hiddenAttributes.add("iname");
        hiddenAttributes.add("userPassword");
        hiddenAttributes.add("gluuStatus");
        hiddenAttributes.add("oxExternalUid");
    }

    /**
     * Returns list of mandatory attributes
     *
     * @return list of person's mandatory attributes
     * @throws Exception
     */
    public List<GluuCustomAttribute> getMandatoryAttributes() {
        return personService.getMandatoryAtributes();
    }

    protected String getActionName() {
        return "registerPersonAction";
    }

    /**
     * Returns person's attributes
     *
     * @return list of person's attributes
     */
    public List<GluuCustomAttribute> getCustomAttributes() {
        return this.person.getCustomAttributes();
    }

    protected String getEventQueue() {
        return "personQueue";
    }
    
    public GluuCustomPerson getPerson(){
    	return person;
    }
}
