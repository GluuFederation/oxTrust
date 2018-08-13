package org.gluu.oxtrust.service.internationalization;

import org.gluu.jsf2.message.FacesMessages;

import javax.inject.Inject;

public class MessageSourceProvider {

    @Inject
    private FacesMessages facesMessages;

    public String getMessage(String key) {
        return facesMessages.evalResourceAsString(String.format("#{msg['%s']}", key));
    }

}
