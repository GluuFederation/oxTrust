package org.gluu.oxtrust.service;

import org.apache.commons.lang3.RandomStringUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.security.SecureRandom;

@ApplicationScoped
@Named
public class PasswordGenerator {
    private static final String PASSWORD_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int PASSWORD_LENGTH = 24;

    public String generate() {
        return RandomStringUtils.random(PASSWORD_LENGTH, 0, PASSWORD_CHARACTERS.length(),
                false, false, PASSWORD_CHARACTERS.toCharArray(), new SecureRandom());
    }
}