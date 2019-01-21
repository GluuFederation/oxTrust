/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2016, Gluu
 */
package org.gluu.oxtrust.util;

import java.security.cert.X509Certificate;
import java.util.Date;

/**
 * Short information about X509 Certificate in keystore.
 * For view in HTML/JSF table.
 * 
 * @author Dmitry Ognyannikov, 2016
 */
public class X509CertificateShortInfoView extends X509CertificateShortInfo {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 7323704327690340323L;

	private static final String HIGHLIGHT_STYLE_UNVALID = "background-color: rgb(255, 0, 0);";
    
    private static final String HIGHLIGHT_STYLE_VALID = "";
    
    private static final String HIGHLIGHT_STYLE_WARNING = "background-color: rgb(255, 255, 0);";
    
    /**
     * 3 months in nanoseconds.
     */
    private static final long NANOSEC_3_MONTH = 3*30*24*60*60*1000L;
    
    private String viewStyle;
    private boolean warning = false;
    
    public X509CertificateShortInfoView() {}
    
    public X509CertificateShortInfoView(String alias, X509Certificate cert) {
        super(alias, cert);
        
        updateViewStyle();
    }
    
    public final void updateViewStyle() {
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
     * @return the viewStyle
     */
    public String getViewStyle() {
        return viewStyle;
    }

    /**
     * @param viewStyle the viewStyle to set
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
     * @param warning the warning to set
     */
    public void setWarning(boolean warning) {
        this.warning = warning;
    }
}
