/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2016, Gluu
 */
package org.gluu.oxtrust.util;

import java.io.File;
import java.io.Serializable;
import java.security.cert.X509Certificate;
import java.util.Date;

/**
 * Short information about X509 Certificate in keystore.
 * 
 * @author Dmitry Ognyannikov, 2016
 */
public class X509CertificateShortInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 135442950364886448L;

	private static final String HIGHLIGHT_STYLE_UNVALID = "background-color: rgb(255, 0, 0);";

	private static final String HIGHLIGHT_STYLE_VALID = "";

	private static final String HIGHLIGHT_STYLE_WARNING = "background-color: rgb(255, 255, 0);";

	/**
	 * 3 months in nanoseconds.
	 */
	private static final long NANOSEC_3_MONTH = 3 * 30 * 24 * 60 * 60 * 1000L;

	private String alias;
	private String issuer;
	private String subject;
	private String algorithm;
	private Date notBeforeDatetime;
	private Date notAfterDatetime;
	private String viewStyle;
	private boolean warning = false;

	private String path;

	public X509CertificateShortInfo() {
	}

	public X509CertificateShortInfo(String alias, X509Certificate cert) {
		this.alias = alias;

		if (cert.getIssuerDN() != null)
			issuer = cert.getIssuerDN().getName();
		if (cert.getSubjectDN() != null)
			subject = cert.getSubjectDN().getName();
		algorithm = cert.getSigAlgName();
		notBeforeDatetime = cert.getNotBefore();
		notAfterDatetime = cert.getNotAfter();

		updateViewStyle();
	}

	public X509CertificateShortInfo(String path, String alias, X509Certificate cert) {
		this(alias, cert);
		this.path = path;
	}

	public void updateViewStyle() {
		final Date currentTime = new Date();
		final Date time3MonthAfter = new Date(System.currentTimeMillis() + NANOSEC_3_MONTH);

		// check dates
		if (currentTime.after(getNotAfterDatetime())) {
			setViewStyle(HIGHLIGHT_STYLE_UNVALID);// expired
			warning = true;
		} else if (getNotBeforeDatetime().after(getNotAfterDatetime())) {
			setViewStyle(HIGHLIGHT_STYLE_UNVALID);// error in certificate
			warning = true;
		} else if (currentTime.before(getNotBeforeDatetime())) {
			setViewStyle(HIGHLIGHT_STYLE_WARNING);
			warning = true;
		} else if (time3MonthAfter.after(getNotAfterDatetime())) {
			setViewStyle(HIGHLIGHT_STYLE_UNVALID);// 3 month before expiration
			warning = true;
		} else {
			setViewStyle(HIGHLIGHT_STYLE_VALID);
		}
	}

	/**
	 * @return the alias
	 */
	public String getAlias() {
		return alias;
	}

	/**
	 * @param alias
	 *            the alias to set
	 */
	public void setAlias(String alias) {
		this.alias = alias;
	}

	/**
	 * @return the issuer
	 */
	public String getIssuer() {
		return issuer;
	}

	/**
	 * @param issuer
	 *            the issuer to set
	 */
	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	/**
	 * @return the subject
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * @param subject
	 *            the subject to set
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}

	/**
	 * @return the algorithm
	 */
	public String getAlgorithm() {
		return algorithm;
	}

	/**
	 * @param algorithm
	 *            the algorithm to set
	 */
	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	/**
	 * @return the notBeforeDatetime
	 */
	public Date getNotBeforeDatetime() {
		return notBeforeDatetime;
	}

	/**
	 * @param notBeforeDatetime
	 *            the notBeforeDatetime to set
	 */
	public void setNotBeforeDatetime(Date notBeforeDatetime) {
		this.notBeforeDatetime = notBeforeDatetime;
	}

	/**
	 * @return the notAfterDatetime
	 */
	public Date getNotAfterDatetime() {
		return notAfterDatetime;
	}

	/**
	 * @param notAfterDatetime
	 *            the notAfterDatetime to set
	 */
	public void setNotAfterDatetime(Date notAfterDatetime) {
		this.notAfterDatetime = notAfterDatetime;
	}

	/**
	 * @return the viewStyle
	 */
	public String getViewStyle() {
		return viewStyle;
	}

	/**
	 * @param viewStyle
	 *            the viewStyle to set
	 */
	public void setViewStyle(String viewStyle) {
		this.viewStyle = viewStyle;
	}

	/**
	 * @return the warning
	 */
	public boolean isWarning() {
		return warning;
	}

	/**
	 * @param warning
	 *            the warning to set
	 */
	public void setWarning(boolean warning) {
		this.warning = warning;
	}

	public String getPath() {
		return path;
	}

	public String getName() {
		if (this.path != null) {
			File file = new File(this.path);
			return file.getName();
		} else {
			return "certificate.crt";
		}
	}

	public void setPath(String path) {
		this.path = path;
	}
}
