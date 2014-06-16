package org.gluu.oxtrust.service.test;

import org.gluu.oxtrust.action.test.ConfigurableTest;
import org.gluu.oxtrust.ldap.service.GroupService;
import org.jboss.seam.mock.AbstractSeamTest;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * User: Dejan Maric
 * Date: 25.10.11.
 */
public class GroupServiceTest extends ConfigurableTest {
	@BeforeTest
	public void initTestConfiguration() throws Exception {
		initTest();
	}


	@Test
	public void testIsMemberOrOwner() throws Exception {
		new AbstractSeamTest.FacesRequest() {
            @Override
            public void invokeApplication() throws Exception {
                GroupService groupService = (GroupService) getInstance("groupService");
                String groupDN = getConf().getString("groupService.testIsMemberOrOwner.groupDN");
                String ownerDN = getConf().getString("groupService.testIsMemberOrOwner.ownerDN");
                String nonMemberDN = getConf().getString("groupService.testIsMemberOrOwner.nonMemberDN");
                Assert.assertTrue(groupService.isMemberOrOwner(groupDN, ownerDN), "Failed to confirm group owner");
                Assert.assertTrue(!groupService.isMemberOrOwner(groupDN, nonMemberDN), "Wrong person recognised as group member");
            }
        }.run();
    }
}
