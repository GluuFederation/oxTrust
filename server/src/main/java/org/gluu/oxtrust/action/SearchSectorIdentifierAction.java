package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.oxtrust.ldap.service.SectorIdentifierService;
import org.gluu.oxtrust.model.OxAuthSectorIdentifier;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.slf4j.Logger;
import org.xdi.service.security.Secure;
import org.xdi.util.Util;

/**
 * Action class for search sector identifiers
 *
 * @author Javier Rojas Blum
 * @version January 15, 2016
 */
@Named
@ConversationScoped
@Secure("#{permissionService.hasPermission('sectorIdentifier', 'access')}")
public class SearchSectorIdentifierAction implements Serializable {

    private static final long serialVersionUID = -5270460481895022455L;

    @Inject
    private Logger log;

	@Inject
	private FacesMessages facesMessages;

	@Inject
	private ConversationService conversationService;

    @NotNull
    @Size(min = 0, max = 30, message = "Length of search string should be between 0 and 30")
    private String searchPattern;

    private String oldSearchPattern;

    private List<OxAuthSectorIdentifier> sectorIdentifierList;

    @Inject
    private SectorIdentifierService sectorIdentifierService;

    public String start() {
        return search();
    }

    public String search() {
		if ((this.searchPattern != null) && Util.equals(this.oldSearchPattern, this.searchPattern)) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

        try {
			if (searchPattern == null || searchPattern.isEmpty()) {
				this.sectorIdentifierList = sectorIdentifierService.getAllSectorIdentifiers();
			} else {
	            this.sectorIdentifierList = sectorIdentifierService.searchSectorIdentifiers(this.searchPattern, OxTrustConstants.searchSectorIdentifierSizeLimit);
			}

			log.debug("Found \"" + this.sectorIdentifierList.size() + "\" sector identifiers.");
            this.oldSearchPattern = this.searchPattern;
        } catch (Exception ex) {
            log.error("Failed to find sector identifiers", ex);

            facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to find sector identifiers");
			conversationService.endConversation();

			return OxTrustConstants.RESULT_FAILURE;
        }

        return OxTrustConstants.RESULT_SUCCESS;
    }

    public String getSearchPattern() {
        return searchPattern;
    }

    public void setSearchPattern(String searchPattern) {
        this.searchPattern = searchPattern;
    }

    public List<OxAuthSectorIdentifier> getSectorIdentifierList() {
        return sectorIdentifierList;
    }
}
