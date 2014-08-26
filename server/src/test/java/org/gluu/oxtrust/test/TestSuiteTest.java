package org.gluu.oxtrust.test;

import java.io.File;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.eu.ingwar.tools.arquillian.extension.suite.annotations.ArquilianSuiteDeployment;
import org.gluu.oxtrust.service.test.PersonServiceTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OverProtocol;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * Test suite
 *
 * @author Yuriy Movchan Date: 02/06/2014
 */
@ArquilianSuiteDeployment
public class TestSuiteTest {
	@Deployment
	@OverProtocol("Servlet 3.0")
	public static WebArchive createDeployment() {
		WebArchive web = ShrinkWrap.create(WebArchive.class, "test.war");
		web.addPackage(PersonServiceTest.class.getPackage());

		// Install org.jboss.seam.mock.MockSeamListener
		web.delete("/WEB-INF/web.xml");
		web.addAsWebInfResource("web.xml");

		// TODO: Workaround
		WebArchive web2 = ShrinkWrap.create(ZipImporter.class, "oxtrust.war").importFrom(new File("target/oxtrust-server.war"))
				.as(WebArchive.class);

		InputStream is = web2.get("/WEB-INF/components.xml").getAsset().openStream();
		try {
			String components = IOUtils.toString(is);
			String marker = "<!-- Inum DB configuration -->";
			int idx1 = components.indexOf(marker);
			int idx2 = components.indexOf(marker, idx1 + 1);
			components = components.substring(0, idx1 + marker.length()) + components.substring(idx2);
			StringAsset componentsAsset = new StringAsset(components);
			web.add(componentsAsset, "/WEB-INF/components.xml");
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			IOUtils.closeQuietly(is);
		}
		//        web.addAsWebInfResource("in-container-components.xml", "components.xml");

		return web;
	}
}
