package org.gluu.oxtrust.exception;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.gluu.oxtrust.security.Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdi.service.cdi.util.CdiUtil;
import org.xdi.service.security.SecurityEvaluationException;

import javax.enterprise.context.NonexistentConversationException;
import javax.faces.FacesException;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import javax.inject.Inject;

import java.util.Iterator;

/**
 * Created by eugeniuparvan on 5/23/17.
 */
public class GlobalExceptionHandler extends ExceptionHandlerWrapper {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ExceptionHandler wrapped;

    GlobalExceptionHandler(ExceptionHandler exception) {
        this.wrapped = exception;
    }

    @Override
    public ExceptionHandler getWrapped() {
        return this.wrapped;
    }

    public void handle() throws FacesException {
        final Iterator<ExceptionQueuedEvent> i = getUnhandledExceptionQueuedEvents().iterator();

        while (i.hasNext()) {
            ExceptionQueuedEvent event = i.next();
            ExceptionQueuedEventContext context = (ExceptionQueuedEventContext) event.getSource();

            Throwable t = context.getException();
            final FacesContext fc = FacesContext.getCurrentInstance();
            final ExternalContext externalContext = fc.getExternalContext();
            final ConfigurableNavigationHandler nav = (ConfigurableNavigationHandler) fc.getApplication().getNavigationHandler();
            try {
				if (isSecurityException(t)) {
					performRedirect(externalContext, "/login");
				} else if (isConversationException(t)) {
					log.error(t.getMessage(), t);
					performRedirect(externalContext, "/conversation_error");
				} if (isViewExpiredException(t)) {
                    storeRequestURI();
                    performRedirect(externalContext, "/login");
				} else {
					log.error(t.getMessage(), t);
					performRedirect(externalContext, "/error");
				}
                fc.renderResponse();
            } finally {
                i.remove();
            }
        }
        getWrapped().handle();
    }

    protected void storeRequestURI() {
        ExternalContext extContext = FacesContext.getCurrentInstance().getExternalContext();
        String requestUri = ((javax.servlet.http.HttpServletRequest) extContext.getRequest()).getRequestURI();

        Identity identity = CdiUtil.bean(Identity.class);
        identity.setSavedRequestUri(requestUri);
    }

    private boolean isSecurityException(Throwable t) {
        return ExceptionUtils.getRootCause(t) instanceof SecurityEvaluationException;
    }

    private boolean isConversationException(Throwable t) {
        return ExceptionUtils.getRootCause(t) instanceof NonexistentConversationException;
    }

    private boolean isViewExpiredException(Throwable t) {
        return t instanceof javax.faces.application.ViewExpiredException;
    }

    private void performRedirect(ExternalContext externalContext, String viewId) {
        try {
            externalContext.redirect(externalContext.getRequestContextPath() + viewId);
        } catch (Exception e) {
            log.error("Can't perform redirect to viewId: " + viewId, e);
        }
    }

}
