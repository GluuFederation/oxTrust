package org.oxtrust.qa.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.oxtrust.qa.pages.AbstractPage;

public class ApplicationDriver {

	private static final String LINUX = "LINUX";
	private static final String CHROME = "CHROM";
	private static final String GLUU_SERVER_URL = "GLUU_SERVER_URL";
	private static final String GLUU_USERNAME = "GLUU_USERNAME";
	private static final String GLUU_USERPWD = "GLUU_USERPWD";
	private static final String QA_BROWSER = "QA_BROWSER";
	private static final String QA_OS = "QA_OS";
	private static final String QA_MODE = "HEADLESS";
	private static WebDriver driver;
	private static ChromeDriverService service;
	private static Settings settings;
	private static ChromeOptions options;

	public static WebDriver getInstance() {
		try {
			if (driver == null) {
				readConfiguration();
				initDriverOptions();
				options.setHeadless(settings.isHeadless());
				if (settings.getOs().equalsIgnoreCase(LINUX) && settings.getBrowser().startsWith(CHROME)) {
					System.setProperty("webdriver.chrome.driver", "/usr/bin/chromedriver");
					startService();
					driver = new RemoteWebDriver(service.getUrl(), options);
					return driver;

				} else {
					throw new IllegalArgumentException("OS or Browser not supported yet");
				}
			}
			return driver;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			System.out.println("----------WEB DRIVER INSTANCE IS NULL");
			System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			return null;
		}

	}

	public static Settings getSettings() {
		if (settings == null) {
			readConfiguration();
		}
		return settings;
	}

	public static String getUserDir() {
		String result = System.getProperty("user.dir");
		System.out.println("##################### Current Worlking Directory:" + result);
		return result;
	}

	private ApplicationDriver() {
	}

	public static void initDriverOptions() {
		options = new ChromeOptions();
		HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
		chromePrefs.put("profile.default_content_settings.popups", 0);
		chromePrefs.put("download.default_directory", getUserDir());
		options.setExperimentalOption("prefs", chromePrefs);
		options.setCapability("acceptInsecureCerts", true);
		options.setCapability("applicationCacheEnabled", true);
		options.setCapability("browserConnectionEnabled", true);
		options.setCapability("networkConnectionEnabled", true);
		options.setExperimentalOption("useAutomationExtension", false);
		options.addArguments("--no-sandbox");
		options.addArguments("--disable-dev-shm-usage");
		options.addArguments("start-maximized");
		options.addArguments("disable-infobars");
		options.addArguments("--disable-extensions");
	}

	public static void startService() {
		if (service == null) {
			File file = new File("/usr/bin/chromedriver");
			service = new ChromeDriverService.Builder().usingDriverExecutable(file).usingAnyFreePort().build();
		}
		try {
			service.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void readConfiguration() {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(getResourceFile("config.properties").getAbsolutePath());
			prop.load(input);
			settings = new Settings(prop.getProperty(GLUU_SERVER_URL), prop.getProperty(GLUU_USERNAME),
					prop.getProperty(GLUU_USERPWD), prop.getProperty(QA_BROWSER), prop.getProperty(QA_OS),
					Boolean.valueOf(prop.getProperty(QA_MODE)));
			AbstractPage.settings = settings;
			System.out.println("*********************************************************************");
			System.out.println(settings.toString());
			System.out.println("*********************************************************************");
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	protected static File getResourceFile(String resName) {
		ClassLoader classLoader = ApplicationDriver.class.getClassLoader();
		return new File(classLoader.getResource(resName).getFile());
	}
}
