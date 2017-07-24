/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.gluu.oxtrust.ldap.service.ImageService;
import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.model.GluuOrganization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdi.model.GluuImage;
import org.xdi.util.io.DownloadWrapper;
import org.xdi.util.io.FileDownloader;
import org.xdi.util.io.FileDownloader.ContentDisposition;

/**
 * Servlet to upload organization logo
 * 
 * @author Yuriy Movchan Date: 11.16.2010
 */
@WebServlet(urlPatterns = "/servlet/logo")
public class LogoImageServlet extends HttpServlet {

	private static final long serialVersionUID = 5445488800130871634L;

	private static final Logger log = LoggerFactory.getLogger(LogoImageServlet.class);
	
	@Inject
	private OrganizationService organizationService;
	
	@Inject
	private ImageService imageService;

	@Override
	protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse response) {
		log.debug("Starting organization logo upload");
		try {
			GluuOrganization organization = organizationService.getOrganization();
			GluuImage image = imageService.getGluuImageFromXML(organization.getLogoImage());
			if (image != null) {
				image.setLogo(true);
			}

			OutputStream os = null;
			InputStream is = null;
			try {
				DownloadWrapper downloadWrapper = null;

				// Send customized organization logo
				if (image != null) {
					File file = imageService.getSourceFile(image);
					try {
						is = FileUtils.openInputStream(file);
						downloadWrapper = new DownloadWrapper(is, image.getSourceName(), image.getSourceContentType(),
								image.getCreationDate(), (int) file.length());
					} catch (IOException ex) {
						log.error("Organization logo image doesn't exist", ex);
						FileDownloader.sendError(response);
						return;
					}
				} else {
					// If customized logo doesn't exist then send default
					// organization logo
					String defaultLogoFileName = "/WEB-INF/static/images/default_logo.png";
					is = getServletContext().getResourceAsStream(defaultLogoFileName);
					if (is == null) {
						log.error("Default organization logo image doesn't exist");
						FileDownloader.sendError(response);
						return;
					}

					// Calculate default logo size
					long contentLength;
					try {
						contentLength = is.skip(Long.MAX_VALUE);
					} catch (IOException ex) {
						log.error("Failed to calculate default organization logo image size", ex);
						FileDownloader.sendError(response);
						return;
					} finally {
						IOUtils.closeQuietly(is);
					}

					is = getServletContext().getResourceAsStream(defaultLogoFileName);
					downloadWrapper = new DownloadWrapper(is, "default_logo.png", "image/png", new Date(), (int) contentLength);
				}

				try {
					int logoSize = FileDownloader.writeOutput(downloadWrapper, ContentDisposition.INLINE, response);
					response.getOutputStream().flush();
					log.debug("Successfully send organization logo with size", logoSize);
				} catch (IOException ex) {
					log.error("Failed to send organization logo", ex);
					FileDownloader.sendError(response);
				}
			} finally {
				IOUtils.closeQuietly(is);
				IOUtils.closeQuietly(os);
			}
		} catch (Exception ex) {
			log.error("Failed to send organization logo", ex);
		}
	}
}
