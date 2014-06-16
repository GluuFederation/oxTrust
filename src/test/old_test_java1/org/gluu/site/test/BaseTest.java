package org.gluu.site.test;

import org.jboss.seam.mock.EnhancedMockHttpServletResponse;
import org.jboss.seam.mock.SeamTest;

public abstract class BaseTest extends SeamTest {

	public void showResponse(String title, EnhancedMockHttpServletResponse response) {
		System.out.println(" ");
		System.out.println("RESPONSE FOR: " + title);
		System.out.println(response.getStatus());
		for (Object headerName : response.getHeaderNames()) {
			System.out.println(headerName + ": " + response.getHeader(headerName.toString()));
		}
		System.out.println(response.getContentAsString());
		System.out.println(" ");
	}
}
