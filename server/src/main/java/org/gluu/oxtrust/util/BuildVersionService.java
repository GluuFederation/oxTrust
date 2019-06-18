/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.util;

import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.io.Serializable;
/**
 * Constants with current build info
 * 
 * @author Yuriy Movchan Date: 12.17.2010
 */

@ApplicationScoped
public class BuildVersionService implements Serializable {

	private static final long serialVersionUID = 3790281266924133197L;

	@Inject
	private Logger log;

	private String revisionVersion;
	private String revisionDate;
	private String buildDate;
	private String buildNumber;

	public String getRevisionVersion() {
		return revisionVersion;
	}

	public void setRevisionVersion(String revisionVersion) {
		this.revisionVersion = revisionVersion;
	}

	public String getRevisionDate() {
		return revisionDate;
	}

	public void setRevisionDate(String revisionDate) {
		this.revisionDate = revisionDate;
	}

	public String getBuildDate() {
		return buildDate;
	}

	public void setBuildDate(String buildDate) {
		this.buildDate = buildDate;
	}

	public String getBuildNumber() {
		return buildNumber;
	}

	public void setBuildNumber(String buildNumber) {
		this.buildNumber = buildNumber;
	}
	
	@PostConstruct
	public void initalize() {
		try (InputStream is = getClass().getResourceAsStream("/META-INF/beans.xml")) {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = factory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);
			doc.getDocumentElement().normalize();
			log.info("Root element :" + doc.getDocumentElement().getNodeName());
			NodeList nList = doc.getElementsByTagName("bean");

			if (doc.hasChildNodes()) {
				readBuildDetails(nList);
			}
		} catch (Exception ex) {
			log.error("Failed to obtain build version", ex);
		}

	}

	private void readBuildDetails(NodeList nodeList) {
		for (int count = 0; count < nodeList.getLength(); count++) {
			Node tempNode = nodeList.item(count);

			// make sure it's element node.
			if (tempNode.getNodeType() == Node.ELEMENT_NODE) {

				if (tempNode.hasAttributes()) {
					// get attributes names and values
					NamedNodeMap nodeMap = tempNode.getAttributes();

					for (int i = 0; i < nodeMap.getLength(); i++) {
						Node node = nodeMap.item(i);
						String nodeValue = node.getNodeValue();
						//nodeValue.equalsIgnoreCase("buildNumber") ? this.: "";
						if(nodeValue.equalsIgnoreCase("buildNumber")){
							this.setBuildNumber(tempNode.getTextContent());
							continue;
						}
						if(nodeValue.equalsIgnoreCase("buildDate")){
							this.setBuildDate(tempNode.getTextContent());
							continue;
						}
						if(nodeValue.equalsIgnoreCase("revisionDate")){
							this.setRevisionDate(tempNode.getTextContent());
							continue;
						}
						if(nodeValue.equalsIgnoreCase("revisionVersion")){
							this.setRevisionVersion(tempNode.getTextContent());
							continue;
						}
					}
				}

				if (tempNode.hasChildNodes()) {
					// loop again if has child nodes
					readBuildDetails(tempNode.getChildNodes());

				}
			}
		}
	}

}
