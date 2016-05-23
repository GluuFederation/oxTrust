/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action.test;

import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.web.Session;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

/**
 * @author Yuriy Zabrovarnyy
 * @version 0.9, 15/10/2012
 */

public abstract class BaseComponentTest extends BaseTest {
    @BeforeMethod
    @Override
    public void begin() {
//        Lifecycle.beginCall();
        super.begin();
    }

    @AfterMethod
    @Override
    public void end() {
//        Session.instance().invalidate();
//        Lifecycle.endCall();
        super.end();
    }

    @BeforeClass
    public void setupClass() throws Exception {
        super.setupClass();
//        Lifecycle.beginCall();
        beforeClass();
//        Lifecycle.endCall();
    }

    @AfterClass
    public void cleanupClass() throws Exception {
//        Lifecycle.beginCall();
        afterClass();
//        Lifecycle.endCall();
        super.cleanupClass();
    }

    public abstract void beforeClass();

    public abstract void afterClass();

    public static void sleepSeconds(int p_seconds) {
        try {
            Thread.sleep(p_seconds * 1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
