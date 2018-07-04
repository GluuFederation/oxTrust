package org.gluu.oxtrust.api.configuration;

import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OxAuthConfigTest {

    @Test
    public void url_validation_happy_path() {
        String opPolicyUri = "http://ox.gluu.org/doku.php?id=oxauth:policy";
        assertTrue(Pattern.compile(OxAuthConfig.URL_PATTERN).matcher(opPolicyUri).matches());
    }

}