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
import javax.servlet.ServletException;
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

@WebServlet(urlPatterns = "/servlet/favicon")
public class FaviconImageServlet extends HttpServlet {
	
	@Inject
	private OrganizationService organizationService;
	
	@Inject
	private ImageService imageService;

	private static final long serialVersionUID = 5445488800130871634L;

	private static final Logger log = LoggerFactory.getLogger(FaviconImageServlet.class);

	@Override
	protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse response) throws ServletException, IOException {
		log.debug("Starting organization favicon upload");
		String preview = httpServletRequest.getParameter("preview");
		GluuOrganization organization = null;
		try {
			organization = organizationService.getOrganization();
		} catch (Exception ex) {
			log.error("an Error Occured", ex);
		}

		GluuImage image = null;
		if ("true".equals(preview)) {
			image = imageService.getGluuImageFromXML(organization.getTempFaviconImage());
			if (image != null) {
				image.setStoreTemporary(true);
			}

		}

		if (!"true".equals(preview) || image == null) {
			image = imageService.getGluuImageFromXML(organization.getFaviconImage());
		}

		if (image != null) {
			image.setLogo(false);
		}
		OutputStream os = null;
		InputStream is = null;
		try {
			DownloadWrapper downloadWrapper = null;

			// Send customized organization logo
			if (image != null) {
				File file = null;
				try {
					file = imageService.getSourceFile(image);
				} catch (Exception ex) {
					log.error("an Error Occured", ex);

				}
				try {
					is = FileUtils.openInputStream(file);
					if (is != null && file != null) {
						downloadWrapper = new DownloadWrapper(is, image.getSourceName(), image.getSourceContentType(),
								image.getCreationDate(), (int) file.length());
					}
				} catch (IOException ex) {
					log.error("Organization favicon image doesn't exist", ex);
					FileDownloader.sendError(response);
					return;
				}
			} else {
				// If customized logo doesn't exist then send default
				// organization logo
				String defaultFaviconFileName = "/WEB-INF/static/images/favicon_icosahedron.ico";
				is = getServletContext().getResourceAsStream(defaultFaviconFileName);
				if (is == null) {
					log.error("Default organization favicon image doesn't exist");
					FileDownloader.sendError(response);
					return;
				}

				// Calculate default logo size
				long contentLength;
				try {
					contentLength = is.skip(Long.MAX_VALUE);
				} catch (IOException ex) {
					log.error("Failed to calculate default organization favicon image size", ex);
					FileDownloader.sendError(response);
					return;
				} finally {
					IOUtils.closeQuietly(is);
				}

				is = getServletContext().getResourceAsStream(defaultFaviconFileName);
				downloadWrapper = new DownloadWrapper(is, "favicon_ic.ico", "image/x-icon", new Date(), (int) contentLength);
			}

			try {
				int logoSize = FileDownloader.writeOutput(downloadWrapper, ContentDisposition.INLINE, response);
				response.getOutputStream().flush();
				log.debug("Successfully send organization favicon with size", logoSize);
			} catch (IOException ex) {
				log.error("Failed to send organization favicon", ex);
				FileDownloader.sendError(response);
			}
		} finally {
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(os);
		}
	}
}
