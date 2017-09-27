package org.gluu.oxtrust.model.scim2.group;

import org.gluu.oxtrust.model.scim2.BaseScimResource;
import org.gluu.oxtrust.model.scim2.annotations.Schema;

/**
 * Created by jgomer on 2017-09-12.
 *
 * Core schema group resource.
 * Property names (member names) MUST match exactly as in the spec. Edit carefully! Many other classes are depending on
 * this one via reflection. Annotations applied at every member follow what the spec states
 * Do not remove LdapAttribute annotations. These are used by FilterVisitor classes to convert SCIM filter queries into
 * LDAP queries
 */
@Schema(id="urn:ietf:params:scim:schemas:core:2.0:Group", name="Group", description = "group")
public class GroupResource extends BaseScimResource {
}
