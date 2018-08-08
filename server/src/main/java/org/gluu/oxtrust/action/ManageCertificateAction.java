/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.security.auth.x500.X500Principal;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.WordUtils;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.provider.JCERSAPrivateCrtKey;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.util.encoders.Base64;
import org.gluu.jsf2.io.ResponseHelper;
import org.gluu.jsf2.message.FacesMessages;
import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.ldap.service.SSLService;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.model.cert.TrustStoreCertificate;
import org.gluu.oxtrust.model.cert.TrustStoreConfiguration;
import org.gluu.oxtrust.security.Identity;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.persist.exception.BasePersistenceException;
import org.richfaces.event.FileUploadEvent;
import org.richfaces.model.UploadedFile;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.service.security.Secure;
import org.xdi.util.StringHelper;
import org.xdi.util.io.FileHelper;

/**
 * Manages SSL certificates
 * 
 * @author Oleksiy Tataryn
 * @author Yuriy Movchan Date: 03/03/2014
 * 
 */
@ConversationScoped
@Named
@Secure("#{permissionService.hasPermission('configuration', 'access')}")
public class ManageCertificateAction implements Serializable {
	public static final String BEGIN_CERT_REQ = "-----BEGIN CERTIFICATE REQUEST-----";
	public static final String END_CERT_REQ = "-----END CERTIFICATE REQUEST-----";

	private static final long serialVersionUID = 4012709440384265524L;

	@Inject
	private Logger log;

	@Inject
	private OrganizationService organizationService;

	@Inject
	private FacesMessages facesMessages;

	@Inject
	private SSLService sslService;

	@Inject
	private AppConfiguration appConfiguration;

	@Inject
	private ApplianceService applianceService;

	@Inject
	private Identity identity;

	private TrustStoreConfiguration trustStoreConfiguration;
	private List<TrustStoreCertificate> trustStoreCertificates;

	private String orgInumFN, tomcatCertFN, idpCertFN;

	private HashMap<String, String> issuer;
	private HashMap<String, String> subject;

	private String uploadMarker;
	private TrustStoreCertificate trustStoreCertificateUploadMarker;

	private boolean certsMmanagePossible;
	private boolean initialized;
	private boolean wereAnyChanges;

	public String init() {
		if (this.initialized) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		this.wereAnyChanges = false;

		this.certsMmanagePossible = prepareTempWorkspace();

		this.orgInumFN = StringHelper.removePunctuation(organizationService.getOrganizationInum());
		this.tomcatCertFN = orgInumFN + "-java.crt";
		this.idpCertFN = orgInumFN + "-shib.crt";

		try {
			GluuAppliance appliance = applianceService.getAppliance();

			if (appliance == null) {
				return OxTrustConstants.RESULT_FAILURE;
			}

			trustStoreConfiguration = appliance.getTrustStoreConfiguration();
			if (trustStoreConfiguration == null) {
				trustStoreConfiguration = new TrustStoreConfiguration();
			}

			trustStoreCertificates = appliance.getTrustStoreCertificates();
			if (trustStoreCertificates == null) {
				trustStoreCertificates = new ArrayList<TrustStoreCertificate>();
			}
		} catch (Exception ex) {
			log.error("Failed to load appliance configuration", ex);

			return OxTrustConstants.RESULT_FAILURE;
		}

		this.initialized = true;

		return OxTrustConstants.RESULT_SUCCESS;
	}

	/**
	 * Fills issuer and subject maps with data about currently selected
	 * certificate
	 */

	public void getCert(String fileName) {
		X509Certificate cert = sslService.getPEMCertificate(getTempCertDir() + fileName);
		loadCert(cert);
	}

	/**
	 * Fills issuer and subject maps with data about currently selected
	 * certificate
	 */

	public void getCert(TrustStoreCertificate trustStoreCertificate) {
		this.issuer = new HashMap<String, String>();
		this.subject = new HashMap<String, String>();

		if (trustStoreCertificate != null) {
			X509Certificate cert = sslService
					.getPEMCertificate(new ByteArrayInputStream(trustStoreCertificate.getCertificate().getBytes()));
			loadCert(cert);
		}
	}

	private void loadCert(X509Certificate cert) {
		if (cert != null) {
			String issuerDN = cert.getIssuerX500Principal().getName();
			String[] values = issuerDN.split("(?<!\\\\),");
			for (String value : values) {
				String[] keyValue = value.split("=");
				issuer.put(keyValue[0], keyValue[1]);
			}
			String subjectDN = cert.getSubjectX500Principal().getName();
			values = subjectDN.split("(?<!\\\\),");
			for (String value : values) {
				String[] keyValue = value.split("=");
				subject.put(keyValue[0], keyValue[1]);
			}
			subject.put("validUntil", StringHelper.toString(cert.getNotAfter()));
			subject.put("validAfter", StringHelper.toString(cert.getNotBefore()));
		}
	}

	public String generateCSR(String fileName) throws IOException {
		if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
			Security.addProvider(new BouncyCastleProvider());
		}

		KeyPair pair = getKeyPair(fileName);
		boolean result = false;
		if (pair != null) {
			String url = appConfiguration.getIdpUrl().replaceFirst(".*//", "");
			String csrPrincipal = String.format("CN=%s", url);
			X500Principal principal = new X500Principal(csrPrincipal);

			PKCS10CertificationRequest csr = null;
			try {
				csr = new PKCS10CertificationRequest("SHA1withRSA", principal, pair.getPublic(), null, pair.getPrivate());
			} catch (GeneralSecurityException e) {
				log.error(e.getMessage(), e);
				return OxTrustConstants.RESULT_FAILURE;
			}

			// Form download responce
			StringBuilder response = new StringBuilder();

			response.append(BEGIN_CERT_REQ + "\n");
			response.append(WordUtils.wrap(new String(Base64.encode(csr.getEncoded(ASN1Encoding.DER))), 64, "\n", true) + "\n");
			response.append(END_CERT_REQ + "\n");

			FacesContext facesContext = FacesContext.getCurrentInstance();
			result = ResponseHelper.downloadFile("csr.pem", OxTrustConstants.CONTENT_TYPE_TEXT_PLAIN, response.toString().getBytes(),
					facesContext);
		}

		return result ? OxTrustConstants.RESULT_SUCCESS : OxTrustConstants.RESULT_FAILURE;
	}

	public boolean compare(String fileName) {
		KeyPair pair = getKeyPair(fileName);
		X509Certificate cert = sslService.getPEMCertificate(getTempCertDir() + fileName);

		boolean noFilesPresent = (pair == null) && (cert == null);

		boolean filesPresent = (pair != null) && (cert != null);
		boolean filesValid = false;
		if (filesPresent) {
			filesValid = (pair.getPublic() != null) && (pair.getPublic().equals(cert.getPublicKey()));
		}

		boolean compareResult = (noFilesPresent || (filesPresent && filesValid));
		log.debug(fileName + " compare result: " + compareResult);
		return compareResult;
	}

	private KeyPair getKeyPair(String fileName) {
		KeyPair pair = null;
		JCERSAPrivateCrtKey privateKey = null;
		PEMParser r = null;
		FileReader fileReader = null;

		File keyFile = new File(getTempCertDir() + fileName.replace("crt", "key"));
		if (keyFile.isFile()) {
			try {
				fileReader = new FileReader(keyFile);
				r = new PEMParser(
						fileReader /*
									 * , new PasswordFinder() { public char[]
									 * getPassword() { // Since keys are stored
									 * without a password this // function
									 * should not be called. return null; } }
									 */);

				Object keys = r.readObject();
				if (keys == null) {
					log.error(" Unable to read keys from: " + keyFile.getAbsolutePath());
					return null;
				}

				if (keys instanceof KeyPair) {
					pair = (KeyPair) keys;
					log.debug(keyFile.getAbsolutePath() + "contains KeyPair");
				} else if (keys instanceof JCERSAPrivateCrtKey) {

					privateKey = (JCERSAPrivateCrtKey) keys;
					log.debug(keyFile.getAbsolutePath() + "contains JCERSAPrivateCrtKey");
					BigInteger exponent = privateKey.getPublicExponent();
					BigInteger modulus = privateKey.getModulus();

					RSAPublicKeySpec publicKeySpec = new java.security.spec.RSAPublicKeySpec(modulus, exponent);
					PublicKey publicKey = null;
					try {
						KeyFactory keyFactory = KeyFactory.getInstance("RSA");

						publicKey = keyFactory.generatePublic(publicKeySpec);
					} catch (Exception e) {
						e.printStackTrace();
					}

					pair = new KeyPair(publicKey, privateKey);
				} else {
					log.error(keyFile.getAbsolutePath() + " Contains unsupported key type: " + keys.getClass().getName());
					return null;
				}

			} catch (IOException e) {
				log.error(e.getMessage(), e);
				return null;
			} finally {
				try {
					r.close();
					fileReader.close();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					return null;
				}
			}
		} else {
			log.error("Key file does not exist : " + keyFile.getAbsolutePath());
		}
		log.debug("KeyPair successfully extracted from: " + keyFile.getAbsolutePath());
		return pair;
	}

	public boolean certPresent(String filename) {
		KeyPair pair = getKeyPair(filename);
		X509Certificate cert = sslService.getPEMCertificate(getTempCertDir() + filename);

		boolean filesPresent = (pair != null) && (cert != null);

		return filesPresent;
	}

	public String getIdpCertFN() {
		return idpCertFN;
	}

	public String getTomcatCertFN() {
		return tomcatCertFN;
	}

	public String getTempCertDir() {
		return appConfiguration.getTempCertDir() + File.separator;
	}

	public HashMap<String, String> getIssuer() {
		return issuer;
	}

	public HashMap<String, String> getSubject() {
		return subject;
	}

	public void setUploadMarker(String uploadMarker) {
		this.uploadMarker = uploadMarker;
		this.trustStoreCertificateUploadMarker = null;
	}

	public void setUploadMarker(TrustStoreCertificate trustStoreCertificate) {
		this.uploadMarker = null;
		this.trustStoreCertificateUploadMarker = trustStoreCertificate;
	}

	private boolean prepareTempWorkspace() {
		String tempDirFN = appConfiguration.getTempCertDir();
		String dirFN = appConfiguration.getCertDir();
		File certDir = new File(dirFN);
		if (tempDirFN == null || dirFN == null || !certDir.isDirectory() || StringHelper.isEmpty(tempDirFN)) {

			return false;
		} else {
			File tempDir = new File(tempDirFN);
			// If tempDir exists - empty it, if not - create. If exists, but
			// isFile - write an error and return false.
			if (tempDir.isDirectory()) {
				File[] files = tempDir.listFiles();
				for (File file : files) {
					if (file.isFile()) {
						file.delete();
					}
				}
			} else {
				if (tempDir.exists()) {
					log.error("Temporary certifcates path exists but is not a directory");
					return false;
				} else {
					tempDir.mkdirs();
				}
			}

			File[] files = certDir.listFiles();
			for (File file : files) {
				if (file.isFile()) {
					try {
						FileHelper.copy(file, new File(tempDirFN + File.separator + file.getName()));
					} catch (IOException e) {
						log.error("Unable to populate temp certs directory: ", e);
						return false;
					}
				}
			}
		}

		return true;
	}

	public String update() {
		if (!isCertsManagePossible()) {
			return OxTrustConstants.RESULT_FAILURE;
		}

		boolean isUpdateTrustCertificates = updateTrustCertificates();
		boolean isUpdatedCertificates = updateCertificates();
		boolean result = isUpdateTrustCertificates && isUpdatedCertificates;

		if (result) {
			tirggerTrustStoreUpdate();
		}

		return result ? OxTrustConstants.RESULT_SUCCESS : OxTrustConstants.RESULT_FAILURE;
	}

	private boolean updateTrustCertificates() {
		try {
			// Reload entry to include latest changes
			GluuAppliance tmpAppliance = applianceService.getAppliance();

			TrustStoreConfiguration currTrustStoreConfiguration = tmpAppliance.getTrustStoreConfiguration();
			List<TrustStoreCertificate> currTrustStoreCertificates = tmpAppliance.getTrustStoreCertificates();
			if (currTrustStoreCertificates == null) {
				currTrustStoreCertificates = new ArrayList<TrustStoreCertificate>(0);
			}

			if (!trustStoreConfiguration.equals(currTrustStoreConfiguration)
					|| !trustStoreCertificates.equals(currTrustStoreCertificates)) {
				this.wereAnyChanges = true;
			}

			tmpAppliance.setTrustStoreConfiguration(trustStoreConfiguration);

			if (trustStoreCertificates.size() == 0) {
				tmpAppliance.setTrustStoreCertificates(null);
			} else {
				tmpAppliance.setTrustStoreCertificates(trustStoreCertificates);
			}

			applianceService.updateAppliance(tmpAppliance);
		} catch (BasePersistenceException ex) {
			log.error("Failed to update appliance configuration", ex);
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to update appliance");
			return false;
		}

		return true;
	}

	/**
	 * Updates certificates from temporary working directory to production and
	 * restarts services.
	 * 
	 * @return true if update was successful. false if update was aborted due to
	 *         some error (perhaps permissions issue.)
	 */
	private boolean updateCertificates() {
		if (!compare(tomcatCertFN) || !compare(idpCertFN)) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Certificates and private keys should match. Certificate update aborted.");
			return false;
		}

		String tempDirFN = appConfiguration.getTempCertDir();
		String dirFN = appConfiguration.getCertDir();
		File certDir = new File(dirFN);
		File tempDir = new File(tempDirFN);
		if (tempDirFN == null || dirFN == null || !certDir.isDirectory() || !tempDir.isDirectory()) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Certificate update aborted due to filesystem error");
			return false;
		} else {
			File[] files = tempDir.listFiles();
			for (File file : files) {
				try {
					if (file.isFile() && !FileUtils.contentEquals(file, new File(dirFN + File.separator + file.getName()))) {
						FileHelper.copy(file, new File(dirFN + File.separator + file.getName()));
						this.wereAnyChanges = true;
					}
				} catch (IOException e) {
					facesMessages.add(FacesMessage.SEVERITY_FATAL,
							"Certificate update failed. Certificates may have been corrupted. Please contact a Gluu administrator for help.");
					log.error("Error occured on certificates update:", e);
				}
			}
		}

		return true;
	}

	private void tirggerTrustStoreUpdate() {
		String certDirFileName = appConfiguration.getCertDir();
		File certDir = new File(certDirFileName);

		if (this.wereAnyChanges) {
			File pkcs12 = new File(certDir, orgInumFN + "-java.pkcs12");
			File pem = new File(certDir, orgInumFN + "-java.pem");
			File jks = new File(certDir, orgInumFN + "-java.jks");

			log.info("Deleting %s : %s", orgInumFN + "-java.pkcs12", pkcs12.delete());
			log.info("Deleting %s : %s", orgInumFN + "-java.pem", pem.delete());
			log.info("Deleting %s : %s", orgInumFN + "-java.jks", jks.delete());

			applianceService.restartServices();

			facesMessages.add(FacesMessage.SEVERITY_WARN,
					"Certificates were updated and appliance service will be restarted. Please log in again in 5 minutes.");

			this.wereAnyChanges = false;
		}
	}

	public void cancel() {
	}

	public void certUpload(FileUploadEvent event) {
		if (this.trustStoreCertificateUploadMarker == null) {
			updateCert(event.getUploadedFile());
		} else {
			updateTrsutStoreCert(event.getUploadedFile());
		}
	}

	private void updateCert(UploadedFile item) {
		InputStream is = null;
		OutputStream os = null;
		try {
			is = item.getInputStream();
			os = new FileOutputStream(getTempCertDir() + this.uploadMarker);
			BufferedOutputStream bos = new BufferedOutputStream(os);

			IOUtils.copy(is, bos);
			bos.flush();
		} catch (IOException ex) {
			log.error("Failed to upload certicicate", ex);
		} finally {
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(os);
		}
	}

	private void updateTrsutStoreCert(UploadedFile item) {
		InputStream is = null;
		try {
			is = item.getInputStream();
			String certificate = IOUtils.toString(is);
			this.trustStoreCertificateUploadMarker.setCertificate(certificate);

			this.trustStoreCertificateUploadMarker.setAddedAt(new Date());
			this.trustStoreCertificateUploadMarker.setAddedBy(identity.getUser().getDn());

		} catch (IOException ex) {
			log.error("Failed to upload key", ex);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	public void keyUpload(FileUploadEvent event) {
		updateKey(event.getUploadedFile());
	}

	private void updateKey(UploadedFile item) {
		InputStream is = null;
		OutputStream os = null;
		try {
			is = item.getInputStream();
			os = new FileOutputStream(getTempCertDir() + this.uploadMarker.replace("crt", "key"));
			BufferedOutputStream bos = new BufferedOutputStream(os);

			IOUtils.copy(is, bos);
			bos.flush();
		} catch (IOException ex) {
			log.error("Failed to upload key", ex);
		} finally {
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(os);
		}
	}

	public void addPublicCertificate() {
		TrustStoreCertificate trustStoreCertificate = new TrustStoreCertificate();
		trustStoreCertificate.setAddedAt(new Date());
		trustStoreCertificate.setAddedBy(identity.getUser().getDn());

		this.trustStoreCertificates.add(trustStoreCertificate);
	}

	public void removePublicCertificate(TrustStoreCertificate removeTrustStoreCertificate) {
		for (Iterator<TrustStoreCertificate> iterator = this.trustStoreCertificates.iterator(); iterator.hasNext();) {
			TrustStoreCertificate trustStoreCertificate = iterator.next();
			if (System.identityHashCode(removeTrustStoreCertificate) == System.identityHashCode(trustStoreCertificate)) {
				iterator.remove();
				return;
			}
		}
	}

	public boolean isInitialized() {
		return initialized;
	}

	public boolean isCertsManagePossible() {
		return certsMmanagePossible;
	}

	public TrustStoreConfiguration getTrustStoreConfiguration() {
		return trustStoreConfiguration;
	}

	public List<TrustStoreCertificate> getTrustStoreCertificates() {
		return trustStoreCertificates;
	}

}
