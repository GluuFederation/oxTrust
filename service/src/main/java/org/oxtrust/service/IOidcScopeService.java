package org.oxtrust.service;

import java.util.List;

import org.gluu.oxtrust.model.OxAuthScope;

public interface IOidcScopeService {

	OxAuthScope getScopeByDn(String scopeDn) throws Exception;

	OxAuthScope getScopeByInum(String sinum) throws Exception;

	String getDnForScope(String inum) throws Exception;

	List<OxAuthScope> searchScopes(String object, int i);

	void addScope(OxAuthScope scope) throws Exception;

	String generateInumForNewScope() throws Exception;

	void updateScope(OxAuthScope scope) throws Exception;

	void removeScope(OxAuthScope scope) throws Exception;

	List<OxAuthScope> getAllScopesList(int i) throws Exception;

}
