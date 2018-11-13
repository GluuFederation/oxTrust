package org.gluu.oxtrust.service.organization;

import org.gluu.oxtrust.api.organization.SmtpStatus;
import org.gluu.oxtrust.service.config.smtp.SmtpConfigurationService;
import org.gluu.oxtrust.service.internationalization.MessageSourceProvider;
import org.xdi.model.SmtpConfiguration;
import org.xdi.service.MailService;

import javax.inject.Inject;

public class SmtpStatusFactory {

    @Inject
    private MailService mailService;
    @Inject
    private MessageSourceProvider messageSourceProvider;
    @Inject
    private SmtpConfigurationService smtpConfigurationService;

    public SmtpStatus create() {
        String messageSubject = messageSourceProvider.getMessage("mail.verify.message.subject");
        String messagePlain = messageSourceProvider.getMessage("mail.verify.message.plain.body");
        String messageHtml = messageSourceProvider.getMessage("mail.verify.message.html.body");

        SmtpConfiguration smtpConfiguration = smtpConfigurationService.findSmtpConfiguration();
        boolean wasSent = mailService.sendMail(smtpConfiguration, smtpConfiguration.getFromEmailAddress(),
                smtpConfiguration.getFromName(), smtpConfiguration.getFromEmailAddress(),
                null, messageSubject, messagePlain, messageHtml);

        return new SmtpStatus(wasSent);
    }
}
