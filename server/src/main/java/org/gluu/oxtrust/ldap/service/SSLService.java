/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.security.cert.X509Certificate;

import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

/**
 * Provides common ssl certificates management
 * 
 * @author �Oleksiy Tataryn�
 */
@Scope(ScopeType.STATELESS)
@Name("sslService")
@AutoCreate
public class SSLService implements Serializable {

	private static final long serialVersionUID = -874807269234589084L;

	@Logger
	private Log log;

	/**
	 * Extracts X509 certificate from pem-encoded file.
	 * 
	 * @param fileName
	 * @return
	 */
	public X509Certificate getCertificate(String fileName) {
		X509Certificate cert = null;

		try {
			cert = getCertificate(new FileInputStream(fileName));
		} catch (FileNotFoundException e) {
			log.error("Certificate file does not exist : " + fileName);

		}

		return cert;
	}

	/**
	 * Extracts X509 certificate from pem-encoded stream.
	 * 
	 * @param certStream
	 * @return
	 */
	public X509Certificate getCertificate(InputStream certStream) {
		X509Certificate cert = null;
		Reader reader = null;
		PEMReader r = null;

		try {
			reader = new InputStreamReader(certStream);
			r = new PEMReader(reader, new PasswordFinder() {
				public char[] getPassword() {
					return null;
				}
			});

			cert = (X509Certificate) r.readObject();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return null;
		} finally {
			try {
				r.close();
				reader.close();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				return null;
			}
		}

		return cert;
	}

	/**
	 * Get SSLService instance
	 * 
	 * @return SSLService instance
	 */
	public static SSLService instance() {
		return (SSLService) Component.getInstance(SSLService.class);
	}
}
