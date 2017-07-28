/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.gluu.oxtrust.config.ConfigurationFactory;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;

/**
 * Provides operations with velocity templates
 * 
 * @author Yuriy Movchan Date: 12.15.2010
 */
@ApplicationScoped
@Named("templateService")
public class TemplateService implements Serializable {

	private static final long serialVersionUID = 4898430090669045605L;

	@Inject
	private Logger log;

	@Inject
	private ConfigurationFactory configurationFactory;

	@Inject
	private AppConfiguration appConfiguration;

	/*
	 * Generate relying-party.xml using relying-party.xml.vm template
	 */
	public String generateConfFile(String template, VelocityContext context) {
		StringWriter sw = new StringWriter();
		try {
			Velocity.mergeTemplate(template + ".vm", "UTF-8", context, sw);
		} catch (Exception ex) {
			log.error("Failed to load velocity template '{}'", template, ex);
			return null;
		}

		return sw.toString();
	}

	public boolean writeConfFile(String confFile, String conf) {
		try {
			FileUtils.writeStringToFile(new File(confFile), conf, "UTF-8");
		} catch (IOException ex) {
			log.error("Failed to write IDP configuration file '{}'", confFile, ex);
			ex.printStackTrace();
			return false;
		}

		return true;
	}

	/*
	 * Load Velocity configuration from classpath
	 */
	private Properties getTemplateEngineConfiguration() {
		Properties properties = new Properties();
		InputStream is = TemplateService.class.getClassLoader().getResourceAsStream("velocity.properties");
		try {
			properties.load(is);
			String loaderType = properties.getProperty("resource.loader").trim();
			properties.setProperty("runtime.log", appConfiguration.getVelocityLog());

			// Set right folder for file loader
			if (loaderType.indexOf("file") == 0) {
				String idpTemplatesLocation = configurationFactory.getIDPTemplatesLocation();
				String folder1 = idpTemplatesLocation + "shibboleth3"
						+ File.separator + "idp";
				String folder2 = idpTemplatesLocation + "shibboleth3"
						+ File.separator + "sp";
				String folder3 = idpTemplatesLocation + "ldif";
				String folder4 = idpTemplatesLocation + "shibboleth3"
						+ File.separator + "idp" + File.separator + "MetadataFilter";
				String folder5 = idpTemplatesLocation + "shibboleth3"
						+ File.separator + "idp" + File.separator + "ProfileConfiguration";
				String folder6 = idpTemplatesLocation + "template"
						+ File.separator + "conf";
				String folder7 = idpTemplatesLocation + "template"
						+ File.separator + "shibboleth3";
				properties.setProperty("file.resource.loader.path", folder1 + ", " + folder2 + ", " + folder3 + ", " + folder4 + ", "
						+ folder5 + ", " + folder6  + ", " + folder7);
				log.info("file.resource.loader.path = " + folder1 + ", " + folder2 + ", " + folder3 + ", " + folder4 + ", "
						+ folder5 + ", " + folder6 + ", " + folder7);
			}
		} catch (IOException ex) {
			log.error("Failed to load velocity.properties", ex);
		} finally {
			IOUtils.closeQuietly(is);
		}

		return properties;
	}

	/*
	 * Initialize singleton instance during startup
	 */
	public void initTemplateEngine() {
		try {
			Velocity.init(getTemplateEngineConfiguration());
		} catch (Exception ex) {
			log.error("Failed to initialize Velocity", ex);
		}
	}

}
