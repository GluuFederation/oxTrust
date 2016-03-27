/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service.test;

import org.gluu.oxtrust.action.test.ConfigurableTest;
import org.gluu.oxtrust.ldap.service.IGroupService;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.seam.mock.JUnitSeamTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test class for GroupService
 *
 * @author Yuriy Movchan Date: 02/06/2014
 */
@RunWith(Arquillian.class)
public class GroupServiceTest extends ConfigurableTest {

	@Test
	public void testIsMemberOrOwner() throws Exception {
		new JUnitSeamTest.FacesRequest() {
            @Override
            public void invokeApplication() throws Exception {
                IGroupService groupService = (IGroupService) getInstance("groupService");

                String groupDn = testData.getString("test.group.dn");
                String ownerDn = testData.getString("test.group.dn.ownerDn");
                String nonMemberDn = testData.getString("test.group.dn.nonMemberDn");
                

                boolean isMemberOrOwner = groupService.isMemberOrOwner(groupDn, ownerDn);
                Assert.assertTrue(String.format("Failed to confirm group '%s' owner '%s'", groupDn, ownerDn), isMemberOrOwner);

                boolean isMemberOrOwnerWrong = groupService.isMemberOrOwner(groupDn, nonMemberDn);
                Assert.assertFalse("Wrong person recognised as group member", isMemberOrOwnerWrong);
            }
        }.run();
    }

}
