package org.gluu.oxtrust.exception;

import java.util.Iterator;

import javax.enterprise.context.NonexistentConversationException;
import javax.faces.FacesException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.gluu.oxtrust.security.Identity;
import org.gluu.service.cdi.util.CdiUtil;
import org.gluu.service.security.SecurityEvaluationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by eugeniuparvan on 5/23/17.
 */
public class GlobalExceptionHandler extends ExceptionHandlerWrapper {
    private Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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
            try {
				if (isSecurityException(t)) {
					performRedirect(externalContext, "/login.htm");
				} else if (isConversationException(t)) {
					log.trace(t.getMessage(), t);
					performRedirect(externalContext, "/conversation_error.htm");
				} if (isViewExpiredException(t)) {
                    storeRequestURI();
                    performRedirect(externalContext, "/login.htm");
				} else {
					log.debug(t.getMessage(), t);
					performRedirect(externalContext, "/error.htm");
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
            log.trace("Can't perform redirect to viewId: " + viewId, e);
        }
    }

}
