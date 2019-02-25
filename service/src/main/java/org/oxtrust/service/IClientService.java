package org.oxtrust.service;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.gluu.oxtrust.model.OxAuthClient;

public interface IClientService {

	List<OxAuthClient> getAllClients();

	void updateClient(OxAuthClient client);

	String generateInumForNewClient();

	String getDnForClient(String inum);

	void addClient(OxAuthClient client);

	void removeClient(OxAuthClient client);

	OxAuthClient getClientByInum(String inum);

	OxAuthClient getClientByDn(String dn);

	List<OxAuthClient> searchClients(
			@NotNull @Size(min = 0, max = 30, message = "Length of search string should be less than 30") String searchAvailableClientPattern,
			int searchclientssizelimit);

	List<OxAuthClient> getAllClients(int i);

}
