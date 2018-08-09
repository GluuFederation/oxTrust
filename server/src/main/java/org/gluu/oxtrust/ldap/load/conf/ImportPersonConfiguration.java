/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.load.conf;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.config.ConfigurationFactory;
import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.persist.exception.EntryPersistenceException;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.ImportPerson;
import org.xdi.model.GluuAttribute;
import org.xdi.model.attribute.AttributeDataType;
import org.xdi.util.StringHelper;
import org.xdi.util.properties.FileConfiguration;

@ApplicationScoped
@Named
public class ImportPersonConfiguration {

	private static final String GLUU_IMPORT_PERSON_PROPERTIES_FILE = "gluuImportPerson.properties";

	private static final String ATTRIBUTE_LDAP_NAME_SUFFIX = ".ldapName";
	private static final String ATTRIBUTE_DISPLAY_NAME_SUFFIX = ".displayName";
	private static final String ATTRIBUTE_DATA_TYPE_SUFFIX = ".dataType";
	private static final String ATTRIBUTE_DATA_REQUIRED_SUFFIX = ".required";

	@Inject
	private Logger log;

	@Inject
	private ConfigurationFactory configurationFactory;

	@Inject
	private AttributeService attributeService;

	private FileConfiguration importConfiguration;
	private List<GluuAttribute> attributes;

	@PostConstruct
	public void create() {
		this.importConfiguration = new FileConfiguration(configurationFactory.confDir() + File.separator + GLUU_IMPORT_PERSON_PROPERTIES_FILE, true);
		try {
			this.attributes = prepareAttributes();
		} catch (Exception ex) {
			log.error("Failed to load import configuration", ex);
		}
	}

	private List<GluuAttribute> prepareAttributes() throws Exception {
		List<GluuAttribute> result = new ArrayList<GluuAttribute>();
		List<ImportPerson>  mappings = configurationFactory.getImportPersonConfig().getMappings();
		Iterator<ImportPerson> it = mappings.iterator();

		while (it.hasNext()) {
			ImportPerson importPerson = (ImportPerson) it.next();

				String attributeName = importPerson.getLdapName();
				boolean required = importPerson.getRequired();				

				if (StringHelper.isNotEmpty(attributeName)) {
					GluuAttribute attr = null;
					try {
						attr = attributeService.getAttributeByName(attributeName);
					} catch (EntryPersistenceException ex) {
						log.error("Failed to load attribute '{}' definition from LDAP", attributeName, ex);
					}
					if (attr == null) {
						log.warn("Failed to find attribute '{}' definition in LDAP", attributeName);
						attr = createAttributeFromConfig(importPerson);
						if (attr == null) {
							log.error("Failed to find attribute '{}' definition in '{}'", attributeName,
									GLUU_IMPORT_PERSON_PROPERTIES_FILE);
							continue;
						}
					} else {
						attr.setRequred(required);
					}
					result.add(attr);
				}
			//}
		}

		return result;
	}

	private GluuAttribute createAttributeFromConfig(String prefix) {
		String attributeName = importConfiguration.getString(prefix + ATTRIBUTE_LDAP_NAME_SUFFIX, null);
		String displayName = importConfiguration.getString(prefix + ATTRIBUTE_DISPLAY_NAME_SUFFIX, null);
		String dataType = importConfiguration.getString(prefix + ATTRIBUTE_DATA_TYPE_SUFFIX, null);
		boolean required = importConfiguration.getBoolean(prefix + ATTRIBUTE_DATA_REQUIRED_SUFFIX, false);

		if (StringHelper.isNotEmpty(attributeName) && StringHelper.isNotEmpty(displayName) && StringHelper.isNotEmpty(dataType)) {
			AttributeDataType attributeDataType = AttributeDataType.getByValue(dataType);
			if (attributeDataType != null) {
				GluuAttribute attr = new GluuAttribute();
				attr.setName(attributeName);
				attr.setDisplayName(displayName);
				attr.setDataType(attributeDataType);
				attr.setRequred(required);

				return attr;
			}
		}

		return null;
	}
	
	private GluuAttribute createAttributeFromConfig(ImportPerson importPerson) {
		String attributeName = importPerson.getLdapName();
		String displayName = importPerson.getDisplayName();
		String dataType = importPerson.getDataType();
		boolean required = importPerson.getRequired();

		if (StringHelper.isNotEmpty(attributeName) && StringHelper.isNotEmpty(displayName) && StringHelper.isNotEmpty(dataType)) {
			AttributeDataType attributeDataType = AttributeDataType.getByValue(dataType);
			if (attributeDataType != null) {
				GluuAttribute attr = new GluuAttribute();
				attr.setName(attributeName);
				attr.setDisplayName(displayName);
				attr.setDataType(attributeDataType);
				attr.setRequred(required);

				return attr;
			}
		}

		return null;
	}


	public List<GluuAttribute> getAttributes() {
		if(attributes == null){
			try {
				attributes = prepareAttributes();
			} catch (Exception ex) {
				log.error("Failed to load import configuration", ex);
			}
		}
		return attributes;
	}

}