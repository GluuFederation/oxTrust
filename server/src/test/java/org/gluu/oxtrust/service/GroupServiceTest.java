/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import javax.inject.Inject;

import org.gluu.oxtrust.action.BaseTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Test class for GroupService
 *
 * @author Yuriy Movchan Date: 02/06/2014
 */
public class GroupServiceTest extends BaseTest {

	@Inject
	private IGroupService groupService;

	@Test
	@Parameters({ "test.group.dn", "test.group.dn.ownerDn", "test.group.dn.nonMemberDn" })
	public void testIsMemberOrOwner(final String groupDn, final String ownerDn, final String nonMemberDn)
			throws Exception {
		boolean isMemberOrOwner = groupService.isMemberOrOwner(groupDn, ownerDn);
		assertTrue(isMemberOrOwner, String.format("Failed to confirm group '%s' owner '%s'", groupDn, ownerDn));

		boolean isMemberOrOwnerWrong = groupService.isMemberOrOwner(groupDn, nonMemberDn);
		assertFalse(isMemberOrOwnerWrong, "Wrong person recognised as group member");
	}

}
