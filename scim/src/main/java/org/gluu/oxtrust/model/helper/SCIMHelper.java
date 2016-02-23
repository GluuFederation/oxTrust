/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.helper;

import org.gluu.oxtrust.model.scim2.Email;
import org.gluu.oxtrust.model.scim2.User;

import com.google.common.base.Optional;

/**
 * This class is a collection of different helper methods around the scim2 schema context
 */
public class SCIMHelper {

    private SCIMHelper(){
    }
    
    /**
     * try to extract an email from the User. 
     * If the User has a primary email address this email will be returned.
     * If not the first email address found will be returned.
     * If no Email has been found email.isPresent() == false 
     * @param user a {@link User} with a possible email
     * @return an email if found
     */
    public static Optional<Email> getPrimaryOrFirstEmail(User user){
        for (Email email : user.getEmails()) {
            if (email.isPrimary()) {
                return Optional.of(email);
            }
        }
        
        if(user.getEmails().size() > 0){
            return Optional.of(user.getEmails().get(0));
        }
        return Optional.absent();
    }
}
