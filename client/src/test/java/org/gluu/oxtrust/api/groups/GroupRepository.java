package org.gluu.oxtrust.api.groups;

import java.util.List;

import javax.ws.rs.core.Response;

import org.gluu.oxtrust.api.BaseRepository;
import org.gluu.oxtrust.api.GluuGroupApi;
import org.gluu.oxtrust.api.GluuPersonApi;
import org.gluu.oxtrust.api.proxy.GroupApiProxy;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

public class GroupRepository extends BaseRepository {

	private String PATH = "restv1/api/groups";
	private ResteasyClient client;

	public GroupRepository() {
		super();
		PATH = baseURI + PATH;
		client = new ResteasyClientBuilder().disableTrustManager().build();
	}

	public List<GluuGroupApi> getAllGroups() {
		ResteasyWebTarget target = client.target(PATH);
		GroupApiProxy groupClient = target.proxy(GroupApiProxy.class);
		return groupClient.getGroups(0);
	}

	public List<GluuGroupApi> getGroups(int size) {
		ResteasyWebTarget target = client.target(PATH);
		GroupApiProxy groupClient = target.proxy(GroupApiProxy.class);
		return groupClient.getGroups(size);
	}

	public List<GluuGroupApi> searchGroup(String pattern, int size) {
		ResteasyWebTarget target = client.target(PATH);
		GroupApiProxy groupClient = target.proxy(GroupApiProxy.class);
		return groupClient.getSearchGroups(pattern, size);
	}

	public GluuGroupApi createGroup(GluuGroupApi group) {
		ResteasyWebTarget target = client.target(PATH);
		GroupApiProxy simpleClient = target.proxy(GroupApiProxy.class);
		GluuGroupApi createdGroup = simpleClient.createGroup(group);
		return createdGroup;
	}

	public List<GluuPersonApi> getGroupMembers(String groupInum) {
		System.out.println("********** getting groups members of " + groupInum + "*************");
		ResteasyWebTarget target = client.target(PATH);
		GroupApiProxy simpleClient = target.proxy(GroupApiProxy.class);
		List<GluuPersonApi> people = simpleClient.getGroupMembers(groupInum);
		for (GluuPersonApi person : people) {
			System.out.println(person.getDisplayName());
		}
		return people;
	}

	public boolean addGroupMember(String groupInum, String memberInum) {
		System.out.println("********** adding " + memberInum + "  to group " + groupInum + "*************");
		ResteasyWebTarget target = client.target(PATH);
		GroupApiProxy simpleClient = target.proxy(GroupApiProxy.class);
		Response response = simpleClient.addGroupMember(groupInum, memberInum);
		if (response.getStatus() == 200) {
			return true;
		}
		return false;
	}

	public boolean deleteGroupMember(String groupInum, String memberInum) {
		System.out.println("********** Deleting " + memberInum + "  from group " + groupInum + "*************");
		ResteasyWebTarget target = client.target(PATH);
		GroupApiProxy simpleClient = target.proxy(GroupApiProxy.class);
		Response response = simpleClient.removeGroupMember(groupInum, memberInum);
		if (response.getStatus() == 200) {
			return true;
		}
		return false;
	}

	public GluuGroupApi updateGroup(GluuGroupApi group) {
		ResteasyWebTarget target = client.target(PATH);
		GroupApiProxy simpleClient = target.proxy(GroupApiProxy.class);
		GluuGroupApi createdGroup = simpleClient.updateGroup(group);
		return createdGroup;
	}

	public void deleteGroup(String inum) {
		ResteasyWebTarget target = client.target(PATH);
		GroupApiProxy simpleClient = target.proxy(GroupApiProxy.class);
		simpleClient.deleteGroup(inum);
	}

	public GluuGroupApi getGroup(String inum) {
		ResteasyWebTarget target = client.target(PATH);
		GroupApiProxy simpleClient = target.proxy(GroupApiProxy.class);
		try {
			return simpleClient.getGroup(inum);
		} catch (Exception e) {
			return null;
		}

	}
}
