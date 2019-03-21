/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.api.server.api.impl;

import java.io.FileInputStream;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.gluu.oxtrust.api.Certificates;
import org.gluu.oxtrust.ldap.service.ConfigurationService;
import org.gluu.oxtrust.ldap.service.SSLService;
import org.gluu.oxtrust.model.GluuConfiguration;
import org.gluu.oxtrust.model.cert.TrustStoreCertificate;
import org.gluu.oxtrust.service.uma.annotations.UmaSecure;
import org.gluu.oxtrust.util.OxTrustApiConstants;
import org.gluu.oxtrust.util.X509CertificateShortInfo;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import com.wordnik.swagger.annotations.Authorization;

/**
 * WS endpoint for certificates actions.
 * 
 * @author Dmitry Ognyannikov
 */
@Path(OxTrustApiConstants.BASE_API_URL + OxTrustApiConstants.CERTIFICATES)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "/api/certificates", description = "SAML UI API Endpoint", authorizations = {
		@Authorization(value = "Authorization", type = "uma") })
@UmaSecure(scope = "'api_certificates', '/auth/oxtrust.allow-saml-config-all', '/auth/oxtrust.allow-saml-modify-all'")
@ApplicationScoped
public class CertificatesWebService {

	private static final String OPENDJ_CERTIFICATE_FILE = "/etc/certs/opendj.crt";
	private static final String HTTPD_CERTIFICATE_FILE = "/etc/certs/httpd.crt";
	private static final String SHIB_IDP_CERTIFICATE_FILE = "/etc/certs/shibIDP.crt";

	@Inject
	private Logger logger;

	@Inject
	private ConfigurationService configurationService;

	ObjectMapper objectMapper;

	public CertificatesWebService() {
		// configure Jackson ObjectMapper
		objectMapper = new ObjectMapper();
		objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
	}

	@GET
	@Path("/list")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "list certificates", notes = "List Gluu Server's certificates. You can get only description of certificates, not keys.", response = Certificates.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = Certificates.class),
			@ApiResponse(code = 500, message = "Server error") })
	public String list(@Context HttpServletResponse response) {
		try {
			Certificates certificates = new Certificates();
			// collect trustStoreCertificates
			List<X509CertificateShortInfo> trustStoreCertificates = new ArrayList<X509CertificateShortInfo>();
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
						logger.error("Certificate load exception", e);
					}
				}
			}
			certificates.setTrustStoreCertificates(trustStoreCertificates);

			// collect internal certificates
			List<X509CertificateShortInfo> internalCertificates = new ArrayList<X509CertificateShortInfo>();
			try {
				X509Certificate openDJCerts[] = SSLService
						.loadCertificates(new FileInputStream(OPENDJ_CERTIFICATE_FILE));
				for (X509Certificate openDJCert : openDJCerts)
					internalCertificates.add(new X509CertificateShortInfo("OpenDJ SSL", openDJCert));
			} catch (Exception e) {
				logger.error("Certificate load exception", e);
			}
			try {
				X509Certificate httpdCerts[] = SSLService.loadCertificates(new FileInputStream(HTTPD_CERTIFICATE_FILE));
				for (X509Certificate httpdCert : httpdCerts)
					internalCertificates.add(new X509CertificateShortInfo("HTTPD SSL", httpdCert));
			} catch (Exception e) {
				logger.error("Certificate load exception", e);
			}
			try {
				X509Certificate shibIDPCerts[] = SSLService
						.loadCertificates(new FileInputStream(SHIB_IDP_CERTIFICATE_FILE));
				for (X509Certificate shibIDPCert : shibIDPCerts)
					internalCertificates
							.add(new X509CertificateShortInfo("Shibboleth IDP SAML Certificate", shibIDPCert));
			} catch (Exception e) {
				logger.error("Certificate load exception", e);
			}
			certificates.setInternalCertificates(internalCertificates);
			return objectMapper.writeValueAsString(certificates);
		} catch (Exception e) {
			logger.error("list() Exception", e);
			try {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL SERVER ERROR");
			} catch (Exception ex) {
			}
			return null;
		}
	}
}
