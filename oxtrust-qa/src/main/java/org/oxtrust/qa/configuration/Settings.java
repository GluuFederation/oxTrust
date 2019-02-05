package org.oxtrust.qa.configuration;


public class Settings {

	private String url;
	private String userName;
	private String password;
	private String browser;
	private String os;

	public Settings(String url, String userName, String password, String browser, String os) {
		super();
		this.url = url;
		this.userName = userName;
		this.password = password;
		this.browser = browser;
		this.os = os;
	}

	public String getUrl() {
		return url;
	}

	public String getBrowser() {
		return browser;
	}

	public String getOs() {
		return os;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	@Override
	public String toString() {
		return "QA running on " + os + " operating system using " + browser + " browser tagerting Gluu server with URL "
				+ url + "\n Will login as user: " + userName + " with password: " + password;
	}
}
