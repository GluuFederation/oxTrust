package org.gluu.oxtrust.i18n;

import org.gluu.jsf2.customization.FacesLocalizationConfigPopulator;

/**
 * @author Yuriy Movchan
 * @version 06/11/2020
 */
public class ApplicationFacesLocalizationConfigPopulator extends FacesLocalizationConfigPopulator {
	private static final String LANGUAGE_FILE_PATTERN = "^oxtrust_(.*)\\.properties$";

	@Override
	public String getLanguageFilePattern() {
		return LANGUAGE_FILE_PATTERN;
	}

}
