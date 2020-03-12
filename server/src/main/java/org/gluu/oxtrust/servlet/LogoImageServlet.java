/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.gluu.oxtrust.model.GluuOrganization;
import org.gluu.oxtrust.service.OrganizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet to upload organization logo
 * 
 * @author Yuriy Movchan Date: 11.16.2010
 * @author Mougang Gasmyr Date: 13.06.2019
 */
@WebServlet(urlPatterns = "/servlet/logo")
public class LogoImageServlet extends HttpServlet {

	private static final long serialVersionUID = 5445488800130871634L;

	private static final Logger log = LoggerFactory.getLogger(LogoImageServlet.class);

	public static final String BASE_OXTRUST_LOGO_PATH = "/opt/gluu/jetty/identity/custom/static/logo/";

	@Inject
	private OrganizationService organizationService;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("image/jpg");
		response.setDateHeader("Expires", new Date().getTime()+1000L*1800);
		GluuOrganization organization = organizationService.getOrganization();
		String scale=request.getParameter("scale");
		boolean hasSucceed = readCustomLogo(response, organization);
		if (!hasSucceed) {
			readDefaultLogo(response,scale);
		}
	}

	private boolean readDefaultLogo(HttpServletResponse response,String scale) {
		String defaultLogoFileName = "/WEB-INF/static/images/default_logo.png";
		if(scale!=null) {
			defaultLogoFileName = "/WEB-INF/static/images/default_logo_lg.png";
		}
		try (InputStream in = getServletContext().getResourceAsStream(defaultLogoFileName);
				OutputStream out = response.getOutputStream()) {
			IOUtils.copy(in, out);
			return true;
		} catch (IOException e) {
			log.debug("Error loading default logo: " + e.getMessage());
			return false;
		}
	}

	private boolean readCustomLogo(HttpServletResponse response, GluuOrganization organization) {
		if (organization.getOxTrustLogoPath() == null || StringUtils.isEmpty(organization.getOxTrustLogoPath())) {
			return false;
		}
		File directory = new File(BASE_OXTRUST_LOGO_PATH);
		if (!directory.exists()) {
			directory.mkdir();
		}
		File logoPath = new File(organization.getOxTrustLogoPath());
		if (!logoPath.exists()) {
			return false;
		}
		try (InputStream in = new FileInputStream(logoPath); OutputStream out = response.getOutputStream()) {
			IOUtils.copy(in, out);
			return true;
		} catch (IOException e) {
			log.debug("Error loading custom logo: " + e.getMessage());
			return false;
		}
	}
}
