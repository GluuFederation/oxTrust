package org.gluu.oxtrust.api.profile;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.gluu.oxtrust.api.openidconnect.BaseWebResource;
import org.gluu.oxtrust.util.OxTrustApiConstants;

@Path(OxTrustApiConstants.BASE_API_URL + OxTrustApiConstants.SCOPES)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProfileWebResource extends BaseWebResource{

}
