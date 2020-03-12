package org.gluu.oxtrust.service;

import java.util.List;

import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuUserPairwiseIdentifier;

public interface IPairwiseIdService {

	boolean removePairWiseIdentifier(GluuCustomPerson person, GluuUserPairwiseIdentifier pairwiseIdentifier);

	public abstract List<GluuUserPairwiseIdentifier> findAllUserPairwiseIdentifiers(GluuCustomPerson person);

	String getDnForPairWiseIdentifier(String oxid,String personInum);

}
