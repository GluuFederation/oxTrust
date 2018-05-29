package org.gluu.oxtrust.api.openidconnect;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.gluu.oxtrust.model.OxAuthSectorIdentifier;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SectorIdentifierTest {

	private SectorIdentifierRepository sectorRepository;
	private OxAuthSectorIdentifier sector;
	private static String searchPattern = "-";
	private static boolean canRunOtherTest = false;
	private String oxID;

	@BeforeClass
	public static void testConnection() {
		try {
			SectorIdentifierRepository sectorRepository = new SectorIdentifierRepository();
			sectorRepository.searchSectorIdentifiers(searchPattern);
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
		sectorRepository = new SectorIdentifierRepository();
	}

	@Test
	public void getAllSectorsTest() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("List sector identifiers");
		System.out.println("==================");

		List<OxAuthSectorIdentifier> sectors = sectorRepository.getAllSectorIdentifiers();

		Assert.assertNotNull(sectors);
		Assert.assertTrue(!sectors.isEmpty());
		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void searchSectorsTest() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("Search sector identifiers");
		System.out.println("==================");

		List<OxAuthSectorIdentifier> sectorsFound = sectorRepository.searchSectorIdentifiers(searchPattern);

		Assert.assertNotNull(sectorsFound);
		Assert.assertTrue(!sectorsFound.isEmpty());
		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void addSectorTest() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("Add new sector identifier");
		System.out.println("==================");

		sector = sectorRepository.createSector(generateNewSector());

		Assert.assertNotNull(sector);
		Assert.assertNotNull(sector.getId());
		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void updateSectorTest() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("Update sector identifier");
		System.out.println("==================");
		sector = sectorRepository.createSector(generateNewSector());
		List<String> uris = sector.getRedirectUris();
		uris.add("https://updateuri.redirect.com");
		sector.setRedirectUris(uris);

		sector = sectorRepository.updateSector(sector);

		Assert.assertNotNull(sector);
		Assert.assertNotNull(sector.getId());
		Assert.assertNotNull(sector.getRedirectUris());
		Assert.assertTrue(sector.getRedirectUris().size() == 2);
		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void getSectorByIdTest() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("Get sector identifier");
		System.out.println("==================");
		sector = sectorRepository.createSector(generateNewSector());
		oxID = sector.getId();

		sector = sectorRepository.getSectorIdentifier(oxID);

		Assert.assertNotNull(sector);
		Assert.assertEquals(oxID, sector.getId());
		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void deleteSectorTest() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("Delete sector identifier");
		System.out.println("==================");
		sector = sectorRepository.createSector(generateNewSector());
		oxID = sector.getId();

		sectorRepository.deleteSectorIdentifier(oxID);

		Assert.assertNull(sectorRepository.getSectorIdentifier(oxID));
		System.out.println("*******************");
		System.out.println("Done");
	}

	private OxAuthSectorIdentifier generateNewSector() {
		OxAuthSectorIdentifier sector = new OxAuthSectorIdentifier();
		sector.setId(UUID.randomUUID().toString());
		sector.setSelected(true);
		List<String> redirectUris = new ArrayList<>();
		redirectUris.add("https://simple.redirect.com" + new Random().nextInt(100));
		sector.setRedirectUris(redirectUris);
		return sector;
	}

}
