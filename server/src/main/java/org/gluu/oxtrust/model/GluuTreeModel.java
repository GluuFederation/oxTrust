package org.gluu.oxtrust.model;

import org.gluu.model.custom.script.CustomScriptType;

import net.bootsfaces.component.tree.model.DefaultNodeImpl;


public class GluuTreeModel extends DefaultNodeImpl{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -1080821778334196823L;
	
	private String inum;
	
	private String dn;
	
	private boolean isParent;
	
	private CustomScriptType customScriptType;

	public String getInum() {
		return inum;
	}

	public void setInum(String inum) {
		this.inum = inum;
	}

	public String getDn() {
		return dn;
	}

	public void setDn(String dn) {
		this.dn = dn;
	}

	public boolean isParent() {
		return isParent;
	}

	public void setParent(boolean isParent) {
		this.isParent = isParent;
	}

	public CustomScriptType getCustomScriptType() {
		return customScriptType;
	}

	public void setCustomScriptType(CustomScriptType customScriptType) {
		this.customScriptType = customScriptType;
	}


}
