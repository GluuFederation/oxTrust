package org.gluu.oxtrust.api.configuration;

import org.gluu.oxtrust.api.GluuServerStatus;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ServerStatusApiTest {
	private ServerStatusRepository serverStatusRepository;
	private static boolean canRunOtherTest = false;

	@BeforeClass
	public static void testConnection() {
		try {
			ServerStatusRepository serverStatusRepository = new ServerStatusRepository();
			serverStatusRepository.getServerStatus();
			canRunOtherTest = true;
		} catch (Exception e) {
			System.out.println("***********************");
			System.out.println("ERROR OCCURS: POSSIBLE CAUSES");
			System.out.println("1. MAKE SURE THE HOSTNAME DEFINE IN CONFIGURATION FILE IS RESOLVABLE");
			System.out.println("2. MAKE SURE THE CERTS FILE ARE IMPORTED IN JAVA KEY STORE");
			System.out.println("***********************");
		}
	}

	@Before
	public void setup() {
		Assume.assumeTrue(canRunOtherTest);
		serverStatusRepository = new ServerStatusRepository();
	}

	@Test
	public void getServerStatusTest() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("Get Server Status");
		System.out.println("==================");
		GluuServerStatus serverStatus = serverStatusRepository.getServerStatus();
		Assert.assertNotNull(serverStatus);
		Assert.assertNotNull(serverStatus.getHostname());
		Assert.assertNotNull(serverStatus.getIpAddress());
		System.out.println(serverStatus.toString());
		System.out.println("*******************");
		System.out.println("Done");
	}

}
