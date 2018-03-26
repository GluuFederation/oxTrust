package org.gluu.oxtrust.api.test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class BaseRepository {
	
	private Properties configuration;

	protected String baseURI;
	
	public BaseRepository() {
		init();
	}

	public void init() {
		final String confFile = "conf/configuration.properties";
		configuration = new Properties();
		try {
			configuration.load(new FileInputStream(confFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		baseURI = configuration.getProperty("baseURI");
	}

}
