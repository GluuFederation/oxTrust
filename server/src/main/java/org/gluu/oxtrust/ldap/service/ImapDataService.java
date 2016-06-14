package org.gluu.oxtrust.ldap.service;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.xdi.model.GluuIMAPData;
import org.xdi.model.GluuImage;
import org.xdi.service.JsonService;
import org.xdi.service.XmlService;


/**
 * Service class to work with images in photo repository
 * 
 * @author Yuriy Movchan Date: 11.04.2010
 */
@Name("imapDataService")
@Scope(ScopeType.APPLICATION)
@AutoCreate
public class ImapDataService {

	
	@Logger
	private Log log;

	@In
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
