package org.gluu.oxtrust.service;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class PasswordGeneratorTest {

    @Test
    public void testGenerate() {
        PasswordGenerator passwordGenerator = new PasswordGenerator();
        String password = passwordGenerator.generate();
        assertEquals(24, password.length());
    }

}