package org.gluu.oxtrust.api.test;

import java.util.Date;
import java.util.Random;

import org.gluu.oxtrust.api.GluuPersonApi;
import org.gluu.oxtrust.api.proxy.PeopleApiProxy;
import org.gluu.persist.model.base.GluuStatus;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

public class PeopleRepositoryImpl extends BaseRepository {

	private String PATH = "restv1/api/people";
	private ResteasyClient client;
	private String inum;
	private String searchPattern = "mim";

	public PeopleRepositoryImpl() {
		super();
		PATH = baseURI + PATH;
		client = new ResteasyClientBuilder().build();
	}

	public void getAllPersons() {
		System.out.println("==================");
		System.out.println("List people");
		ResteasyWebTarget target = client.target(PATH);
		PeopleApiProxy peopleClient = target.proxy(PeopleApiProxy.class);
		for (GluuPersonApi person : peopleClient.getAllPersons()) {
			System.out.println(person.toString());
			inum = person.getInum();
		}
		System.out.println("Done");
	}

	public void getSearchPersons(String pattern) {
		System.out.println("==================");
		System.out.println("Search people");
		ResteasyWebTarget target = client.target(PATH);
		PeopleApiProxy peopleClient = target.proxy(PeopleApiProxy.class);
		for (GluuPersonApi person : peopleClient.getSearchPersons(pattern)) {
			System.out.println(person.toString());
			inum = person.getInum();
		}
		System.out.println("Done");
	}

	public void deletePerson(String inum) {
		System.out.println("==================");
		System.out.println("Delete person " + inum);
		ResteasyWebTarget target = client.target(PATH);
		PeopleApiProxy simpleClient = target.proxy(PeopleApiProxy.class);
		simpleClient.deletePerson(inum);
		System.out.println("Done");
	}

	public void getPerson(String inum) {
		System.out.println("==================");
		System.out.println("Get person" + inum);
		ResteasyWebTarget target = client.target(PATH);
		PeopleApiProxy simpleClient = target.proxy(PeopleApiProxy.class);
		System.out.println(simpleClient.getPerson(inum).toString());
		System.out.println("Done");
	}

	public GluuPersonApi createPerson(GluuPersonApi person) {
		System.out.println("==================");
		System.out.println("Add person");
		ResteasyWebTarget target = client.target(PATH);
		PeopleApiProxy simpleClient = target.proxy(PeopleApiProxy.class);
		GluuPersonApi createPerson = simpleClient.createPerson(person);
		System.out.println(createPerson.toString());
		System.out.println("Done");
		return createPerson;
	}

	public void updatePerson(GluuPersonApi person) {
		System.out.println("==================");
		System.out.println("Update person" + person.getInum());
		ResteasyWebTarget target = client.target(PATH);
		PeopleApiProxy simpleClient = target.proxy(PeopleApiProxy.class);
		GluuPersonApi updatedPerson = simpleClient.updatePerson(person);
		System.out.println(updatedPerson.toString());
		System.out.println("Done");
	}

	public void testAll() {
		this.getAllPersons();
		this.getSearchPersons(searchPattern);
		this.getPerson(inum);
		GluuPersonApi person = this.createPerson(generateAPerson());
		person.setUserName("UserNameChanged");
		this.updatePerson(person);
		inum = "@!619C.061B.1A7E.5AF4!0001!4377.CD0A!0000!6DF9.5726.8553.49B4";
		this.deletePerson(inum);
	}

	private GluuPersonApi generateAPerson() {
		int next = new Random().nextInt(10);
		String name = "User" + next;
		GluuPersonApi person = new GluuPersonApi();
		person.setEmail(name + "@yahoo.fr");
		person.setUserName(name);
		person.setStatus(GluuStatus.ACTIVE);
		person.setPassword(name);
		person.setDisplayName(name.toUpperCase());
		person.setSurName(name);
		person.setGivenName(name);
		person.setCreationDate(new Date());
		return person;
	}

}
