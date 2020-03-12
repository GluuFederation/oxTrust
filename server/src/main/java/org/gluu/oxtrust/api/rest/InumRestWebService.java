/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.api.rest;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.gluu.oxtrust.service.InumService;

//Sets the path to base URL + /inum
@Path("/inum")
public class InumRestWebService {

	@Inject
	private InumService inumService;

	@GET
	@Path("/{type}/")
	@Produces(MediaType.TEXT_PLAIN)
	public String generateTextInum(@PathParam("type") String type) {
		String inum = "";
		inum = inumService.generateInums(type);
		return inum;
	}

	@GET
	@Path("/{type}/")
	@Produces(MediaType.TEXT_XML)
	public String generateXmlInum(@PathParam("type") String type) {
		String inum = "";
		inum = inumService.generateInums(type);
		return xmlText(type, inum);
	}

	private String xmlText(String type, String inum) {
		String typeText = "";
		if ("people".equals(type)) {
			typeText = "people";
		} else if ("organization".equals(type)) {
			typeText = "organization";
		} else if ("configuration".equals(type)) {
			typeText = "configuration";
		} else if ("group".equals(type)) {
			typeText = "group";
		} else if ("server".equals(type)) {
			typeText = "server";
		} else if ("attribute".equals(type)) {
			typeText = "attribute";
		} else if ("trelationship".equals(type)) {
			typeText = "trustRelationship";
		}
		return "<?xml version=\"1.0\"?>" + "<inum type='" + typeText + "'>" + inum + "</inum>";
	}

	@GET
	@Path("/{type}/")
	@Produces(MediaType.TEXT_HTML)
	public String generateHtmlInum(@PathParam("type") String type) {
		String inum = "";
		inum = inumService.generateInums(type);
		return htmlText(type, inum);
	}

	private String htmlText(String type, String inum) {
		String titleText = "";
		if ("people".equals(type)) {
			titleText = "New Unique People Inum Generator";
		} else if ("organization".equals(type)) {
			titleText = "New Unique Organization Inum Generator";
		} else if ("configuration".equals(type)) {
			titleText = "New Unique Configuration Inum Generator";
		} else if ("group".equals(type)) {
			titleText = "New Unique Group Inum Generator";
		} else if ("server".equals(type)) {
			titleText = "New Unique Server Inum Generator";
		} else if ("attribute".equals(type)) {
			titleText = "New Unique Attribute Inum Generator";
		} else if ("trelationship".equals(type)) {
			titleText = "New Unique Trust Relationship Inum Generator";
		}
		return "<html> " + "<title>" + titleText + "</title>" + "<body><h1>" + type + ": " + inum + "</h1></body>" + "</html> ";
	}

	@GET
	@Path("/{type}/")
	@Produces(MediaType.APPLICATION_JSON)
	public String generateJsonInum(@PathParam("type") String type) {
		String inum = "";
		inum = inumService.generateInums(type);
		return "{\"inum\":\"" + inum + "\"}";
	}

}
