package org.gluu.oxtrust.action;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.gluu.jsf2.message.FacesMessages;
import org.gluu.oxtrust.model.SamlAcr;
import org.gluu.service.security.Secure;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@ConversationScoped
@Named("samlAcrAction")
@Secure("#{permissionService.hasPermission('trust', 'access')}")
public class SamlAcrAction implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4373491307640582394L;
	@Inject
	private Logger log;

	@Inject
	private FacesMessages facesMessages;

	private File inputFile;

	private SamlAcr samlAcr;

	private boolean edit;

	private List<SamlAcr> acrs = new ArrayList<>();

	public List<SamlAcr> getAcrs() {
		return acrs;
	}

	public void setAcrs(List<SamlAcr> acrs) {
		this.acrs = acrs;
	}

	@PostConstruct
	public void init() {

		try {
			inputFile = new File("/opt/shibboleth-idp/conf/authn/gluuacrlist.xml");
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(inputFile);
			Node list = doc.getElementsByTagName("list").item(0);
			NodeList nodes = list.getChildNodes();
			for (int temp = 0; temp < nodes.getLength(); temp++) {
				Node node = nodes.item(temp);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) node;
					acrs.add(new SamlAcr(eElement.getAttribute("parent"), eElement.getAttribute("c:classRef")));
				}
			}
		} catch (Exception e) {
			log.error("Error loading saml acrs", e);
		}
	}

	public void save() {
		facesMessages.add(FacesMessage.SEVERITY_INFO, "Save succesfully!");
		for (SamlAcr acr : this.acrs) {
			log.info("==========Valie is:" + acr.getClassRef());
		}
	}

	public void edit() {
		this.edit = true;
		this.samlAcr = new SamlAcr();
	}

	public void removeEntry(SamlAcr acr) {
		acrs.remove(acr);
		facesMessages.add(FacesMessage.SEVERITY_INFO, acr.getClassRef() + " removed!");
	}

	public void addEntry() {
		if (this.samlAcr.getParent() != null && this.samlAcr.getClassRef() != null) {
			acrs.add(this.samlAcr);
			this.edit = false;
			facesMessages.add(FacesMessage.SEVERITY_INFO, this.samlAcr.getClassRef() + " added!");
		} else {
			this.edit = false;
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "All fields are required!");
		}
	}

	public boolean isEdit() {
		return edit;
	}

	public void setEdit(boolean edit) {
		this.edit = edit;
	}

	public SamlAcr getSamlAcr() {
		if (this.samlAcr.getParent() == null) {
			this.samlAcr.setParent("shibboleth.SAML2AuthnContextClassRef");
		}
		return samlAcr;
	}

	public void setSamlAcr(SamlAcr samlAcr) {
		this.samlAcr = samlAcr;
	}
}
