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

import org.gluu.oxtrust.config.OxTrustConfiguration;
import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.site.ldap.persistence.exception.EntryPersistenceException;
import javax.enterprise.context.ApplicationScoped;
import javax.ejb.Stateless;
import org.jboss.seam.annotations.Create;
import javax.inject.Inject;
import org.jboss.seam.annotations.Logger;
import javax.inject.Named;
import org.jboss.seam.annotations.Observer;
import javax.enterprise.context.ConversationScoped;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.ImportPerson;
import org.xdi.model.GluuAttribute;
import org.xdi.model.GluuAttributeDataType;
import org.xdi.util.StringHelper;
import org.xdi.util.properties.FileConfiguration;

@ApplicationScoped
@Named("importPersonConfiguration")
public class ImportPersonConfiguration {

	private static final String GLUU_IMPORT_PERSON_PROPERTIES_FILE = "gluuImportPerson.properties";

	private static final String ATTRIBUTE_LDAP_NAME_SUFFIX = ".ldapName";
	private static final String ATTRIBUTE_DISPLAY_NAME_SUFFIX = ".displayName";
	private static final String ATTRIBUTE_DATA_TYPE_SUFFIX = ".dataType";
	private static final String ATTRIBUTE_DATA_REQUIRED_SUFFIX = ".required";

	@Inject
	private Logger log;

	@Inject
	private OxTrustConfiguration oxTrustConfiguration;

	@Inject
	private AttributeService attributeService;

	private FileConfiguration importConfiguration;
	private List<GluuAttribute> attributes;

	@PostConstruct
	public void create() {
		this.importConfiguration = new FileConfiguration(oxTrustConfiguration.confDir() + File.separator + GLUU_IMPORT_PERSON_PROPERTIES_FILE, true);
		try {
			this.attributes = prepareAttributes();
		} catch (Exception ex) {
			log.error("Failed to load import configuration", ex);
		}
	}

	private List<GluuAttribute> prepareAttributes() throws Exception {
		List<GluuAttribute> result = new ArrayList<GluuAttribute>();
		List<ImportPerson>  mappings = oxTrustConfiguration.getImportPersonConfig().getMappings();
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
						log.error("Failed to load attribute '{0}' definition from LDAP", ex, attributeName);
					}
					if (attr == null) {
						log.warn("Failed to find attribute '{0}' definition in LDAP", attributeName);
						attr = createAttributeFromConfig(importPerson);
						if (attr == null) {
							log.error("Failed to find attribute '{0}' definition in '{1}'", attributeName,
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
			GluuAttributeDataType attributeDataType = GluuAttributeDataType.getByValue(dataType);
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
			GluuAttributeDataType attributeDataType = GluuAttributeDataType.getByValue(dataType);
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