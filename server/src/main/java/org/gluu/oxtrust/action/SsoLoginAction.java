/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.IOException;
import java.io.Serializable;

import javax.enterprise.context.ConversationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.oxtrust.security.Identity;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.service.security.Secure;
import org.slf4j.Logger;

/**
 * Action class for SSO login
 * 
 * @author Yuriy Movchan Date: 11.26.2010
 */
@ConversationScoped
@Named("ssoLoginAction")
@Secure("#{identity.loggedIn}")
@Deprecated
public class SsoLoginAction implements Serializable {

	private static final long serialVersionUID = 7409229786722653317L;

	@Inject
	private Logger log;

	@Inject
	private Identity identity;

	private String userName;
	private String password;

	private String relyingPartyId;
	private String contextKey;
	private String relayState;
	private String relayStateValue;
	private String actionUrl;

	private boolean initialized = false;

	@Inject
	private AppConfiguration appConfiguration;
/*
	public String start() {
		if (initialized) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();

		relyingPartyId = request.getHeader("relyingPartyId");
		setActionUrl(request.getHeader("actionUrl"));
		log.debug("relyingPartyId is" + relyingPartyId);
		log.debug("actionUrl is" + actionUrl);
		if (StringHelper.isEmpty(relyingPartyId)) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Direct access to this page is not supported");
			// return Configuration.RESULT_FAILURE;
		}

		try {
			log.debug("Getting SSL HTTP Client");
			// Create HTTP local context

			// Bind cookie store to the local context

			// Add user cookies
			log.debug("Setting HTTP Client cookies from user session");

		} catch (Exception ex) {
			log.error("Failed to initialize HTTP Client", ex);
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to prepare login form");

			// return Configuration.RESULT_FAILURE;
		}

		initialized = true;

		RuleBase ruleBase = null;

		try {
			log.info("Checking for customized login pages");
			InputStream is = getClass().getClassLoader().getResourceAsStream("selection.drl");
			if (is != null) {
				log.info("Login page customization rules found.");
				Reader reader = new InputStreamReader(is);
				try {
					ruleBase = RuleBaseLoader.getInstance().loadFromReader(reader);

					WorkingMemory workingMemory = ruleBase.newStatefulSession();

					workingMemory.insert(relyingPartyId);
					// workingMemory.insert(contextKey);
					// workingMemory.insert(relayState);
					// workingMemory.insert(relayStateValue);
					// workingMemory.insert(requestedSessionId);
					List<String> viewId = new ArrayList<String>();
					workingMemory.insert(viewId);
					workingMemory.fireAllRules();
					if (viewId.size() > 0) {
						log.info("Login page customization rules fired: " + viewId.get(0));
						facesService.redirect(viewId.get(0));
					}
				} finally {
					IOUtils.closeQuietly(reader);
				}
			}
		} catch (CheckedDroolsException e) {
			e.printStackTrace();
		} catch (IOException e) {
			log.warn("There were error reading selection.drl");
		}

		return OxTrustConstants.RESULT_SUCCESS;
	}
*/
	public String logout() {
		boolean isShib3Authentication = OxTrustConstants.APPLICATION_AUTHORIZATION_NAME_SHIBBOLETH3.equals(identity.getSessionMap().get(
				OxTrustConstants.APPLICATION_AUTHORIZATION_TYPE));

		if (isShib3Authentication) {
			FacesContext facesContext = FacesContext.getCurrentInstance();
			// After this redirect we should invalidate this session
			try {
				HttpServletResponse userResponse = (HttpServletResponse) facesContext.getExternalContext().getResponse();
				HttpServletRequest userRequest = (HttpServletRequest) facesContext.getExternalContext().getRequest();

				String redirectUrl = String.format("%s%s", appConfiguration.getIdpUrl(), "/idp/logout.jsp");
				String url = String.format("%s://%s/Shibboleth.sso/Logout?return=%s", userRequest.getScheme(), userRequest.getServerName(),
						redirectUrl);

				userResponse.sendRedirect(url);
				facesContext.responseComplete();
			} catch (IOException ex) {
				log.error("Failed to redirect to SSO logout page", ex);
			}
		}

		return isShib3Authentication ? OxTrustConstants.RESULT_LOGOUT_SSO : OxTrustConstants.RESULT_LOGOUT;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRelyingPartyId() {
		return relyingPartyId;
	}

	public void setRelyingPartyId(String relyingPartyId) {
		this.relyingPartyId = relyingPartyId;
	}

	public String getContextKey() {
		return contextKey;
	}

	public void setContextKey(String contextKey) {
		this.contextKey = contextKey;
	}

	public String getRelayState() {
		return relayState;
	}

	public void setRelayState(String relayState) {
		this.relayState = relayState;
	}

	public String getRelayStateValue() {
		return relayStateValue;
	}

	public void setRelayStateValue(String relayStateValue) {
		this.relayStateValue = relayStateValue;
	}

	// public String getRequestedSessionId() {
	// return requestedSessionId;
	// }
	//
	// public void setRequestedSessionId(String requestedSessionId) {
	// this.requestedSessionId = requestedSessionId;
	// }

	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * @return the actionUrl
	 */
	public String getActionUrl() {
		return actionUrl;
	}

	/**
	 * @param actionUrl
	 *            the actionUrl to set
	 */
	public void setActionUrl(String actionUrl) {
		this.actionUrl = actionUrl;
	}

}
