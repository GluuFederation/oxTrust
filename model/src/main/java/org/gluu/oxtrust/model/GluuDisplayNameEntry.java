package org.gluu.oxtrust.model;

import org.gluu.model.DisplayNameEntry;
import org.gluu.persist.annotation.DataEntry;
import org.gluu.persist.annotation.ObjectClass;

@DataEntry
@ObjectClass("gluuAttribute")
public class GluuDisplayNameEntry extends DisplayNameEntry {

    public GluuDisplayNameEntry() {

    }
    public GluuDisplayNameEntry(String dn, String inum, String displayName) {
        super(dn, inum, displayName);
    }
}
