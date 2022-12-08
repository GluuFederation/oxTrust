
package org.gluu.oxtrust.api.server.api.impl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.gluu.oxtrust.api.Certificates;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.gluu.oxtrust.api.server.util.ApiScopeConstants;
import org.gluu.oxtrust.service.ConfigurationService;
import org.gluu.oxtrust.service.SSLService;
import org.gluu.oxtrust.model.GluuConfiguration;
import org.gluu.oxtrust.model.cert.TrustStoreCertificate;
import org.gluu.oxtrust.service.filter.ProtectedApi;
import org.gluu.oxtrust.util.X509CertificateShortInfo;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.FileInputStream;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

@Path(ApiConstants.BASE_API_URL + ApiConstants.CERTIFICATES)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class CertificatesWebResource extends BaseWebResource {

	private static final String OPENLDAP_CERTIFICATE_FILE = "/etc/certs/openldap.crt";
	private static final String OPENDJ_CERTIFICATE_FILE = "/etc/certs/opendj.crt";
	private static final String HTTPD_CERTIFICATE_FILE = "/etc/certs/httpd.crt";
	private static final String IDP_SIGNING_CERTIFICATE_FILE = "/etc/certs/idp-signing.crt";
	private static final String IDP_ENCRYPT_CERTIFICATE_FILE = "/etc/certs/idp-encryption.crt";
	
	@Inject
	private Logger logger;

	@Inject
	private ConfigurationService configurationService;

	private List<X509CertificateShortInfo> trustStoreCertificates;

	private List<X509CertificateShortInfo> internalCertificates;

	@GET
	@Operation(summary = "List certificates", description = "List Gluu Server's certificates. You can get only description of certificates, not keys.",
	security = @SecurityRequirement(name = "oauth2", scopes = {
			ApiScopeConstants.SCOPE_CERTIFICATES_READ }))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Certificates[].class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_CERTIFICATES_READ })
	public Response listCertificates() {
		log(logger, "Processing certificates retrieval request");
		try {
			List<X509CertificateShortInfo> certificates = new ArrayList<>();
			certificates.addAll(trustStoreCertificates());
			certificates.addAll(internalCertificates());
			return Response.ok(certificates).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	private List<X509CertificateShortInfo> trustStoreCertificates() {
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
						logger.error("Certificate load exception", e);
					}
				}
			}
		} catch (Exception e) {
			logger.error("Load trustStoreCertificates configuration exception", e);
		}

		return trustStoreCertificates;
	}

	private List<X509CertificateShortInfo> internalCertificates() {
		try {
			internalCertificates = new ArrayList<X509CertificateShortInfo>();
			try {
				X509Certificate openDJCerts[] = SSLService
						.loadCertificates(new FileInputStream(OPENDJ_CERTIFICATE_FILE));
				for (X509Certificate openDJCert : openDJCerts) {
					internalCertificates
							.add(new X509CertificateShortInfo(OPENDJ_CERTIFICATE_FILE, "OpenDJ SSL", openDJCert));
				}
			} catch (Exception e) {
				logger.warn("OPENDJ certificate load exception");
			}
			try {
				X509Certificate httpdCerts[] = SSLService.loadCertificates(new FileInputStream(HTTPD_CERTIFICATE_FILE));
				for (X509Certificate httpdCert : httpdCerts) {
					internalCertificates
							.add(new X509CertificateShortInfo(HTTPD_CERTIFICATE_FILE, "HTTPD SSL", httpdCert));
				}
			} catch (Exception e) {
				logger.warn("HTTPD Certificate load exception");
			}
			try {
				X509Certificate idpSigingCerts[] = SSLService
						.loadCertificates(new FileInputStream(IDP_SIGNING_CERTIFICATE_FILE));
				for (X509Certificate idpSigingCert : idpSigingCerts) {
					internalCertificates.add(
							new X509CertificateShortInfo(IDP_SIGNING_CERTIFICATE_FILE, "IDP SIGNING", idpSigingCert));
				}
			} catch (Exception e) {
				logger.warn("IDP SIGNING certificate load exception");
			}
			try {
				X509Certificate idpEncryptionCerts[] = SSLService
						.loadCertificates(new FileInputStream(IDP_ENCRYPT_CERTIFICATE_FILE));
				for (X509Certificate idpEncryptionCert : idpEncryptionCerts) {
					internalCertificates.add(new X509CertificateShortInfo(IDP_ENCRYPT_CERTIFICATE_FILE,
							"IDP ENCRYPTION", idpEncryptionCert));
				}
			} catch (Exception e) {
				logger.warn("IDP ENCRYPTION certificate load exception");
			}

			try {
				X509Certificate idpEncryptionCerts[] = SSLService
						.loadCertificates(new FileInputStream(OPENLDAP_CERTIFICATE_FILE));
				for (X509Certificate idpEncryptionCert : idpEncryptionCerts) {
					internalCertificates.add(new X509CertificateShortInfo(OPENLDAP_CERTIFICATE_FILE,
							"OpenLDAP ENCRYPTION", idpEncryptionCert));
				}
			} catch (Exception e) {
				logger.warn("OpenLDAP certificate load exception");
			}

		} catch (Exception e) {
			logger.error("Load internalCertificates configuration exception", e);
		}
		return internalCertificates;
	}
}
