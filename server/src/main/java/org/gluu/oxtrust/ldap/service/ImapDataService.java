package org.gluu.oxtrust.ldap.service;

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import javax.faces.application.FacesMessage;
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
		} catch (Exception e) {
			log.error("Failed to convert GluuIMAPData {0} to JSON", e, imapData);
		} 
		return null;
		
	}
	
	public GluuIMAPData getGluuIMAPDataFromJson(String json) {
		
		try {
			return jsonService.jsonToObject(json, GluuIMAPData.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("Failed to convert GluuIMAPData {0} to JSON", e, json);
		}
		return null;
	}

	
	
}
