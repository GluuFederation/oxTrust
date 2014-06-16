package org.gluu.oxtrust.ldap.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.xdi.config.oxtrust.ApplicationConfiguration;

/**
 * Provides operations with velocity templates
 * 
 * @author Yuriy Movchan Date: 12.15.2010
 */
@Scope(ScopeType.APPLICATION)
@Name("templateService")
@AutoCreate
public class TemplateService implements Serializable {

	private static final long serialVersionUID = 4898430090669045605L;

	@Logger
	private Log log;

	@In(value = "#{oxTrustConfiguration.applicationConfiguration}")
	private ApplicationConfiguration applicationConfiguration;

	/*
	 * Generate relying-party.xml using relying-party.xml.vm template
	 */
	public String generateConfFile(String template, VelocityContext context) {
		StringWriter sw = new StringWriter();
		try {
			Velocity.mergeTemplate(template + ".vm", "UTF-8", context, sw);
		} catch (Exception ex) {
			log.error("Failed to load velocity template '{0}'", ex, template);
			return null;
		}

		return sw.toString();
	}

	public boolean writeConfFile(String confFile, String conf) {
		try {
			FileUtils.writeStringToFile(new File(confFile), conf, "UTF-8");
		} catch (IOException ex) {
			log.error("Failed to write IDP configuration file '{0}'", ex, confFile);
			return false;
		}

		return true;
	}

	public boolean writeApplicationConfFile(String confFile, String conf) {
		return writeConfFile(System.getProperty("catalina.home") + File.separator + "conf" + File.separator + confFile, conf);
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
			properties.setProperty("runtime.log", applicationConfiguration.getVelocityLog());

			// Set right folder for file loader
			if (loaderType.indexOf("file") == 0) {
				String folder1 = System.getProperty("catalina.home") + File.separator + "conf" + File.separator + "shibboleth2"
						+ File.separator + "idp";
				String folder2 = System.getProperty("catalina.home") + File.separator + "conf" + File.separator + "shibboleth2"
						+ File.separator + "sp";
				String folder3 = System.getProperty("catalina.home") + File.separator + "conf" + File.separator + "ldif";
				String folder4 = System.getProperty("catalina.home") + File.separator + "conf" + File.separator + "shibboleth2"
						+ File.separator + "idp" + File.separator + "MetadataFilter";
				String folder5 = System.getProperty("catalina.home") + File.separator + "conf" + File.separator + "shibboleth2"
						+ File.separator + "idp" + File.separator + "ProfileConfiguration";
				String folder6 = System.getProperty("catalina.home") + File.separator + "conf" + File.separator + "template"
						+ File.separator + "conf";
				String folder7 = System.getProperty("catalina.home") + File.separator + "conf" + File.separator + "template"
						+ File.separator + "shibboleth2";
				properties.setProperty("file.resource.loader.path", folder1 + ", " + folder2 + ", " + folder3 + ", " + folder4 + ", "
						+ folder5 + ", " + folder6  + ", " + folder7);
				log.info("file.resource.loader.path" + folder1 + ", " + folder2 + ", " + folder3 + ", " + folder4 + ", "
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

	/**
	 * Get shibboleth2ConfService instance
	 * 
	 * @return Shibboleth2ConfService instance
	 */
	public static TemplateService instance() {
		return (TemplateService) Component.getInstance(TemplateService.class);
	}

}
