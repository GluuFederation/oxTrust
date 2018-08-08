package org.gluu.oxtrust.api.groups;

import java.util.List;
import java.util.Random;

import org.gluu.oxtrust.api.GluuGroupApi;
import org.gluu.oxtrust.api.GluuPersonApi;
import org.gluu.oxtrust.api.users.PeopleRepository;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xdi.model.GluuStatus;

public class GroupApiTest {

	private GluuGroupApi group;
	private GluuPersonApi user;
	private GroupRepository groupRepository;
	private PeopleRepository peopleRepository;
	private static boolean canRunOtherTest = false;

	@BeforeClass
	public static void testConnection() {
		try {
			GroupRepository groupRepository = new GroupRepository();
			groupRepository.searchGroup("admin", 1);
			canRunOtherTest = true;
		} catch (Exception e) {
			System.out.println("***********************");
			System.out.println("ERROR OCCURS: POSSIBLE CAUSES");
			System.out.println("1. MAKE SURE THE HOSTNAME DEFINE IN CONFIGURATION FILE IS RESOLVABLE");
			System.out.println("2. MAKE SURE THE CERTS FILE ARE IMPORTED IN JAVA KEY STORE");
			System.out.println("***********************");
		}
	}

	@Before
	public void setup() {
		Assume.assumeTrue(canRunOtherTest);
		groupRepository = new GroupRepository();
	}

	@Test
	public void getAllGroupsTest() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("List groups");
		System.out.println("==================");
		
		List<GluuGroupApi> groups = groupRepository.getAllGroups();
		
		Assert.assertNotNull(groups);
		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void addGroupTest() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("Add Group");
		System.out.println("==================");
		int sizeBefore = groupRepository.getAllGroups().size();
		
		group = groupRepository.createGroup(generateNewGroup());
		
		int sizeAfter = groupRepository.getAllGroups().size();
		Assert.assertNotNull(group);
		Assert.assertNotNull(group.getInum());
		Assert.assertTrue((sizeBefore + 1) == sizeAfter);
		System.out.println(group.toString());
		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void updateGroupTest() {
		Assume.assumeTrue(canRunOtherTest);
		group = groupRepository.createGroup(generateNewGroup());
		String displayName = "updated Display name";
		String inum = group.getInum();
		group.setDisplayName(displayName);
		System.out.println("=================Update group: " + inum);
		System.out.println("==================");
		
		group = groupRepository.updateGroup(group);
		
		Assert.assertEquals(displayName, group.getDisplayName());
		Assert.assertEquals(inum, group.getInum());
		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void deleteGroupTest() {
		Assume.assumeTrue(canRunOtherTest);
		group = groupRepository.createGroup(generateNewGroup());
		String inum = group.getInum();
		System.out.println("==================");
		System.out.println("Delete group  " + inum);
		System.out.println("==================");
		
		groupRepository.deleteGroup(group.getInum());
		
		Assert.assertEquals(null, groupRepository.getGroup(inum));
		Assert.assertTrue(inum.equalsIgnoreCase(group.getInum()));
		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void searchGroupsTest() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("Search groups");
		System.out.println("==================");
		String pattern = "user";
		
		List<GluuGroupApi> foundGroups = groupRepository.searchGroup(pattern, 10);
		
		Assert.assertNotNull(foundGroups);
		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void getGroupByInumTest() {
		Assume.assumeTrue(canRunOtherTest);
		group = groupRepository.createGroup(generateNewGroup());
		String inum = group.getInum();
		System.out.println("==================");
		System.out.println("Get group by inum:" + inum);
		System.out.println("==================");
		
		group = groupRepository.getGroup(inum);
		
		Assert.assertNotNull(group);
		Assert.assertTrue(inum.equalsIgnoreCase(group.getInum()));
		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void getGroupMembersTest() {
		Assume.assumeTrue(canRunOtherTest);
		group = groupRepository.createGroup(generateNewGroup());
		String inum = group.getInum();
		System.out.println("==================");
		System.out.println("Get group members:");
		System.out.println("==================");
		
		List<GluuPersonApi> foundMembers = groupRepository.getGroupMembers(inum);
		
		Assert.assertNotNull(foundMembers);
		Assert.assertTrue(foundMembers.size() == 0);
		groupRepository.deleteGroup(inum);
		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void addGroupMemberTest() {
		Assume.assumeTrue(canRunOtherTest);
		peopleRepository = new PeopleRepository();
		group = groupRepository.createGroup(generateNewGroup());
		user = peopleRepository.searchPersons("admin").get(0);
		String inum = group.getInum();
		System.out.println("==================");
		System.out.println("Add group member:" + group.getDisplayName());
		System.out.println("==================");
		
		groupRepository.addGroupMember(inum, user.getInum());
		
		List<GluuPersonApi> foundMembers = groupRepository.getGroupMembers(inum);
		Assert.assertNotNull(foundMembers);
		groupRepository.deleteGroup(inum);
		Assert.assertTrue(foundMembers.size() == 1);
		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void removeGroupMemberTest() {
		Assume.assumeTrue(canRunOtherTest);
		peopleRepository = new PeopleRepository();
		group = groupRepository.createGroup(generateNewGroup());
		user = peopleRepository.searchPersons("admin").get(0);
		String inum = group.getInum();
		System.out.println("==================");
		System.out.println("Remove group member:" + group.getDisplayName());
		System.out.println("==================");
		groupRepository.addGroupMember(inum, user.getInum());
		List<GluuPersonApi> foundMembers = groupRepository.getGroupMembers(inum);
		Assert.assertNotNull(foundMembers);
		groupRepository.deleteGroup(inum);
		Assert.assertTrue(foundMembers.size() == 1);

		groupRepository.deleteGroupMember(inum, user.getInum());
		
		foundMembers = groupRepository.getGroupMembers(inum);
		Assert.assertNotNull(foundMembers);
		groupRepository.deleteGroup(inum);
		Assert.assertTrue(foundMembers.size() == 0);
		System.out.println("*******************");
		System.out.println("Done");
	}

	private GluuGroupApi generateNewGroup() {
		int next1 = new Random().nextInt(100);
		int next2 = new Random().nextInt(50);
		String name = "GroupAddeByTest" + next1 + next2;
		GluuGroupApi group = new GluuGroupApi();
		group.setDescription("UDM Students");
		group.setDisplayName(name + next1 + next2);
		group.setStatus(GluuStatus.ACTIVE);
		group.setMembers(null);
		return group;
	}

}
