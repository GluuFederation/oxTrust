/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.load.conf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.site.ldap.persistence.exception.EntryPersistenceException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.xdi.model.GluuAttribute;
import org.xdi.model.GluuAttributeDataType;
import org.xdi.util.StringHelper;
import org.xdi.util.properties.FileConfiguration;

@Scope(ScopeType.APPLICATION)
@AutoCreate
@Name("importPersonConfiguration")
public class ImportPersonConfiguration extends FileConfiguration {

	private static final String GLUU_IMPORT_PERSON_PROPERTIES_FILE = "gluuImportPerson.properties";

	private static final String ATTRIBUTE_LDAP_NAME_SUFFIX = ".ldapName";
	private static final String ATTRIBUTE_DISPLAY_NAME_SUFFIX = ".displayName";
	private static final String ATTRIBUTE_DATA_TYPE_SUFFIX = ".dataType";
	private static final String ATTRIBUTE_DATA_REQUIRED_SUFFIX = ".required";

	@Logger
	private Log log;

	@In
	private AttributeService attributeService;

	private List<GluuAttribute> attributes;

	public ImportPersonConfiguration() {
		super(GLUU_IMPORT_PERSON_PROPERTIES_FILE, true);
	}

	@Observer("org.jboss.seam.postInitialization")
	public void init() throws Exception {
		this.attributes = prepareAttributes();
	}

	private List<GluuAttribute> prepareAttributes() throws Exception {
		List<GluuAttribute> result = new ArrayList<GluuAttribute>();

		Iterator<?> keys = propertiesConfiguration.getKeys();
		while (keys.hasNext()) {
			String key = (String) keys.next();

			if (key.endsWith(ATTRIBUTE_LDAP_NAME_SUFFIX)) {
				int index = key.lastIndexOf(ATTRIBUTE_LDAP_NAME_SUFFIX);
				String prefix = key.substring(0, index);

				String attributeName = propertiesConfiguration.getString(prefix + ATTRIBUTE_LDAP_NAME_SUFFIX, null);
				boolean required = propertiesConfiguration.getBoolean(prefix + ATTRIBUTE_DATA_REQUIRED_SUFFIX, false);

				if (StringHelper.isNotEmpty(attributeName)) {
					GluuAttribute attr = null;
					try {
						attr = attributeService.getAttributeByName(attributeName);
					} catch (EntryPersistenceException ex) {
						log.error("Failed to load attribute '{0}' definition from LDAP", ex, attributeName);
					}
					if (attr == null) {
						log.warn("Failed to find attribute '{0}' definition in LDAP", attributeName);
						attr = createAttributeFromConfig(propertiesConfiguration, prefix);
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
			}
		}

		return result;
	}

	private GluuAttribute createAttributeFromConfig(PropertiesConfiguration propertiesConfiguration, String prefix) {
		String attributeName = propertiesConfiguration.getString(prefix + ATTRIBUTE_LDAP_NAME_SUFFIX, null);
		String displayName = propertiesConfiguration.getString(prefix + ATTRIBUTE_DISPLAY_NAME_SUFFIX, null);
		String dataType = propertiesConfiguration.getString(prefix + ATTRIBUTE_DATA_TYPE_SUFFIX, null);
		boolean required = propertiesConfiguration.getBoolean(prefix + ATTRIBUTE_DATA_REQUIRED_SUFFIX, false);

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
		return attributes;
	}

}