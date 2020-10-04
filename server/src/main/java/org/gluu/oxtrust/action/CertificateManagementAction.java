/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2016, Gluu
 */
package org.gluu.oxtrust.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.gluu.jsf2.message.FacesMessages;
import org.gluu.oxtrust.model.GluuConfiguration;
import org.gluu.oxtrust.model.cert.TrustStoreCertificate;
import org.gluu.oxtrust.service.ConfigurationService;
import org.gluu.oxtrust.service.SSLService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.oxtrust.util.X509CertificateShortInfo;
import org.slf4j.Logger;
import org.gluu.service.security.Secure;

/**
 * Action class for security certificate management.
 * 
 * @author Dmitry Ognyannikov
 */
@Named
@SessionScoped
@Secure("#{permissionService.hasPermission('configuration', 'access')}")
public class CertificateManagementAction implements Serializable {

	private static final long serialVersionUID = -1938167091985945238L;
	private String OPENLDAP_CERTIFICATE_FILE = "/etc/certs/openldap.crt";
	private String OPENDJ_CERTIFICATE_FILE = "/etc/certs/opendj.crt";
	private String HTTPD_CERTIFICATE_FILE = "/etc/certs/httpd.crt";
	private String IDP_SIGNING_CERTIFICATE_FILE = "/etc/certs/idp-signing.crt";
	private String IDP_ENCRYPT_CERTIFICATE_FILE = "/etc/certs/idp-encryption.crt";

	@Inject
	private Logger log;

	@Inject
	private FacesMessages facesMessages;

	@Inject
	private ConfigurationService configurationService;

	private List<X509CertificateShortInfo> trustStoreCertificates;

	private List<X509CertificateShortInfo> internalCertificates;

	@PostConstruct
	public void init() {
		log.info("init() CertificateManagement call");
		refresh();
	}

	public void refresh() {
		log.info("refresh() CertificateManagement call");
		updateTableView();
	}

	public String cancel() {
		log.info("cancel CertificateManagement");

		facesMessages.add(FacesMessage.SEVERITY_INFO, "Certificates not updated");
		return OxTrustConstants.RESULT_SUCCESS;
	}

	/**
	 * Load and process certificate lists.
	 * 
	 * Set highlight for obsolete certificates. Apply search pattern.
	 */
	private void updateTableView() {
		try {
			trustStoreCertificates = new ArrayList<X509CertificateShortInfo>();
			GluuConfiguration configuration = configurationService.getConfiguration();
			List<TrustStoreCertificate> trustStoreCertificatesList = configuration.getTrustStoreCertificates();
			if (trustStoreCertificatesList != null) {
				for (TrustStoreCertificate trustStoreCertificate : trustStoreCertificatesList) {
					try {
						X509Certificate certs[] = SSLService
								.loadCertificates(trustStoreCertificate.getCertificate().getBytes());
						for (X509Certificate cert : certs) {
							X509CertificateShortInfo entry = new X509CertificateShortInfo(
									trustStoreCertificate.getName(), cert);
							trustStoreCertificates.add(entry);
						}
					} catch (Exception e) {
						log.error("Certificate load exception", e);
					}
				}
			}
		} catch (Exception e) {
			log.error("Load trustStoreCertificates configuration exception", e);
		}

		try {
			// load internalCertificates
			internalCertificates = new ArrayList<X509CertificateShortInfo>();
			try {
				X509Certificate openDJCerts[] = SSLService
						.loadCertificates(new FileInputStream(OPENDJ_CERTIFICATE_FILE));
				for (X509Certificate openDJCert : openDJCerts) {
					internalCertificates
							.add(new X509CertificateShortInfo(OPENDJ_CERTIFICATE_FILE, "OpenDJ SSL", openDJCert));
				}
			} catch (Exception e) {
				log.warn("OPENDJ certificate load exception");
			}
			try {
				X509Certificate httpdCerts[] = SSLService.loadCertificates(new FileInputStream(HTTPD_CERTIFICATE_FILE));
				for (X509Certificate httpdCert : httpdCerts) {
					internalCertificates
							.add(new X509CertificateShortInfo(HTTPD_CERTIFICATE_FILE, "HTTPD SSL", httpdCert));
				}
			} catch (Exception e) {
				log.warn("HTTPD Certificate load exception");
			}
			try {
				X509Certificate idpSigingCerts[] = SSLService
						.loadCertificates(new FileInputStream(IDP_SIGNING_CERTIFICATE_FILE));
				for (X509Certificate idpSigingCert : idpSigingCerts) {
					internalCertificates.add(
							new X509CertificateShortInfo(IDP_SIGNING_CERTIFICATE_FILE, "IDP SIGNING", idpSigingCert));
				}
			} catch (Exception e) {
				log.warn("IDP SIGNING certificate load exception");
			}
			try {
				X509Certificate idpEncryptionCerts[] = SSLService
						.loadCertificates(new FileInputStream(IDP_ENCRYPT_CERTIFICATE_FILE));
				for (X509Certificate idpEncryptionCert : idpEncryptionCerts) {
					internalCertificates.add(new X509CertificateShortInfo(IDP_ENCRYPT_CERTIFICATE_FILE,
							"IDP ENCRYPTION", idpEncryptionCert));
				}
			} catch (Exception e) {
				log.warn("IDP ENCRYPTION certificate load exception");
			}

			try {
				X509Certificate idpEncryptionCerts[] = SSLService
						.loadCertificates(new FileInputStream(OPENLDAP_CERTIFICATE_FILE));
				for (X509Certificate idpEncryptionCert : idpEncryptionCerts) {
					internalCertificates.add(new X509CertificateShortInfo(OPENLDAP_CERTIFICATE_FILE,
							"OpenLDAP ENCRYPTION", idpEncryptionCert));
				}
			} catch (Exception e) {
				log.warn("OpenLDAP certificate load exception");
			}

		} catch (Exception e) {
			log.error("Load internalCertificates configuration exception", e);
		}
	}

	/**
	 * @return the trustStoreCertificates
	 */
	public List<X509CertificateShortInfo> getTrustStoreCertificates() {
		return trustStoreCertificates;
	}

	/**
	 * @param trustStoreCertificates
	 *            the trustStoreCertificates to set
	 */
	public void setTrustStoreCertificates(List<X509CertificateShortInfo> trustStoreCertificates) {
		this.trustStoreCertificates = trustStoreCertificates;
	}

	/**
	 * @return the internalCertificates
	 */
	public List<X509CertificateShortInfo> getInternalCertificates() {
		return internalCertificates;
	}

	/**
	 * @param internalCertificates
	 *            the internalCertificates to set
	 */
	public void setInternalCertificates(List<X509CertificateShortInfo> internalCertificates) {
		this.internalCertificates = internalCertificates;
	}

	public void download(X509CertificateShortInfo cert) {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
		response.setContentType("text/plain");
		String header = "attachment; filename=\"" + cert.getName() + "\"";
		response.addHeader("Content-disposition", header);
		try (ServletOutputStream os = response.getOutputStream()) {
			File file = new File(cert.getPath());
			InputStream in = Files.newInputStream(file.toPath());
			IOUtils.copy(in, os);
			os.flush();
			facesContext.responseComplete();
		} catch (Exception e) {
			log.error("", e);
		}
	}
}
