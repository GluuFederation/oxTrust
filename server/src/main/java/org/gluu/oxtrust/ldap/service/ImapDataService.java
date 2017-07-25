package org.gluu.oxtrust.ldap.service;

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.xdi.model.GluuIMAPData;
import org.xdi.service.JsonService;


/**
 * Service class to work with images in photo repository
 * 
 * @author Yuriy Movchan Date: 11.04.2010
 */
@Named("imapDataService")
@ApplicationScoped
public class ImapDataService {

	
	@Inject
	private Logger log;

	@Inject
	private JsonService jsonService;

	
	public String getJsonStringFromImap(GluuIMAPData imapData){
		try {
			return jsonService.objectToJson(imapData);
		} catch (Exception ex) {
			log.error("Failed to convert GluuIMAPData {} to JSON", imapData, ex);
		} 
		return null;
		
	}
	
	public GluuIMAPData getGluuIMAPDataFromJson(String json) {
		
		try {
			return jsonService.jsonToObject(json, GluuIMAPData.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("Failed to convert GluuIMAPData {} to JSON", e, json);
		}
		return null;
	}

	
	
}
