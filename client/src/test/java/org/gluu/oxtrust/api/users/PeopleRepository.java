package org.gluu.oxtrust.api.users;

import java.util.List;

import javax.ws.rs.NotFoundException;

import org.gluu.oxtrust.api.BaseRepository;
import org.gluu.oxtrust.api.GluuPersonApi;
import org.gluu.oxtrust.api.proxy.PeopleApiProxy;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

public class PeopleRepository extends BaseRepository {

	private String PATH = "restv1/api/people";
	private ResteasyClient client;
	private String inum;
	private String searchPattern = "mim";

	public PeopleRepository() {
		super();
		PATH = baseURI + PATH;
		client = new ResteasyClientBuilder().build();
	}

	public List<GluuPersonApi> getAllPersons() {
		ResteasyWebTarget target = client.target(PATH);
		PeopleApiProxy peopleClient = target.proxy(PeopleApiProxy.class);
		return peopleClient.getAllPersons();
	}

	public List<GluuPersonApi> searchPersons(String pattern) {
		ResteasyWebTarget target = client.target(PATH);
		PeopleApiProxy peopleClient = target.proxy(PeopleApiProxy.class);
		return peopleClient.getSearchPersons(pattern);
	}

	public void deletePerson(String inum) {
		ResteasyWebTarget target = client.target(PATH);
		PeopleApiProxy simpleClient = target.proxy(PeopleApiProxy.class);
		simpleClient.deletePerson(inum);
	}

	public GluuPersonApi getPerson(String inum) {
		ResteasyWebTarget target = client.target(PATH);
		PeopleApiProxy simpleClient = target.proxy(PeopleApiProxy.class);
		try {
			return simpleClient.getPerson(inum);
		} catch (NotFoundException e) {
			return null;
		} catch (Exception e) {
			return null;
		}

	}

	public GluuPersonApi createPerson(GluuPersonApi person) {
		ResteasyWebTarget target = client.target(PATH);
		PeopleApiProxy simpleClient = target.proxy(PeopleApiProxy.class);
		GluuPersonApi createPerson = simpleClient.createPerson(person);
		return createPerson;
	}

	public GluuPersonApi updatePerson(GluuPersonApi person) {
		ResteasyWebTarget target = client.target(PATH);
		PeopleApiProxy simpleClient = target.proxy(PeopleApiProxy.class);
		return simpleClient.updatePerson(person);
	}

	public void testAll() {
		this.getAllPersons();
		this.searchPersons(searchPattern);
		this.getPerson(inum);
		inum = "@!619C.061B.1A7E.5AF4!0001!4377.CD0A!0000!6DF9.5726.8553.49B4";
		this.deletePerson(inum);
	}

}
