/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.servlet;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.gluu.oxtrust.ldap.service.ImageService;
import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.ldap.service.uma.ScopeDescriptionService;
import org.gluu.oxtrust.model.GluuOrganization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdi.model.GluuImage;
import org.xdi.oxauth.model.uma.UmaScopeDescription;
import org.xdi.service.JsonService;
import org.xdi.util.io.DownloadWrapper;
import org.xdi.util.io.FileDownloader;
import org.xdi.util.io.FileDownloader.ContentDisposition;

/**
 * Servlet to upload organization logo
 * 
 * @author Yuriy Movchan Date: 11.16.2010
 */
@WebServlet(urlPatterns = "/servlet/umalogo")
public class UmaScopeImageServlet extends HttpServlet {

	private static final long serialVersionUID = 5445488800130871634L;

	private static final Logger log = LoggerFactory.getLogger(UmaScopeImageServlet.class);

	@Inject
	private JsonService jsonService;

	@Inject
	protected ScopeDescriptionService scopeDescriptionService;

	@Override
	protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse response) {
		log.debug("Starting organization logo upload");
		String inum = httpServletRequest.getParameter("inum");

		String dn = scopeDescriptionService.getDnForScopeDescription(inum);
		org.xdi.oxauth.model.uma.persistence.UmaScopeDescription umaScopeDescription = (org.xdi.oxauth.model.uma.persistence.UmaScopeDescription) scopeDescriptionService
				.getScopeDescriptionByDn(dn);
		try {
			GluuImage image = jsonService.jsonToObject(umaScopeDescription.getFaviconImageAsXml(), GluuImage.class);

			InputStream is = null;
			BufferedInputStream input = null;
			BufferedOutputStream output = null;
			try {
			if (image != null) {
				is = new ByteArrayInputStream(image.getData());
			}

			
				input = new BufferedInputStream(is);
				output = new BufferedOutputStream(response.getOutputStream());
				byte[] buffer = new byte[8192];
				int length = 0;
				while ((length = input.read(buffer)) >= 0) {
					output.write(buffer, 0, length);
				}
				response.getOutputStream().flush();
			} finally {
				if (output != null){
					try {
						output.close();
					} catch (IOException e) {
						log.error("Failed to send scope image", e);
					}
			}
				if (input != null){
					try {
						input.close();
					} catch (IOException e) {
						log.error("Failed to send scope image", e);
					}
				}
				
				if (is != null){					
						IOUtils.closeQuietly(is);					
				}
			}

			log.debug("Successfully send scope image");
		} catch (IOException e) {
			log.error("Failed to send scope image", e);
			FileDownloader.sendError(response);
		}catch (Exception e) {
			log.error("Failed to send scope image", e);
			FileDownloader.sendError(response);
		} 
	}
}
