package org.gluu.oxtrust.model;

import org.gluu.model.custom.script.CustomScriptType;
import org.gluu.model.custom.script.model.CustomScript;

import net.bootsfaces.component.tree.model.DefaultNodeImpl;

public class GluuTreeModel extends DefaultNodeImpl {

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

	public void expandParentOfNode(CustomScript selectedScript) {
		if (hasChild()) {
			getChilds().forEach(e -> {
				GluuTreeModel node = (GluuTreeModel) e;
				if (node.hasChildScript(selectedScript)) {
                    node.setExpanded(true);
                    node.setSelected(true);
                    node.selectNodeFor(selectedScript);
                    return;
				}
			});
		}
	}

	private void selectNodeFor(CustomScript selectedScript) {
		if(this.hasChild()) {
			this.getChilds().forEach(e -> {
				GluuTreeModel node=(GluuTreeModel) e;
				if(node.getInum().equalsIgnoreCase(selectedScript.getInum())) {
					node.setSelected(true);
				}
			});
		}
		
	}

	private boolean hasChildScript(CustomScript selectedScript) {
		if (this.hasChild()) {
			return this.getChilds().stream()
					.anyMatch(e -> ((GluuTreeModel) e).getInum().equalsIgnoreCase(selectedScript.getInum()));
		}
		return false;
	}
	
	private boolean hasThisChildNode(GluuTreeModel node) {
		if (this.hasChild()) {
			return this.getChilds().stream()
					.anyMatch(e -> ((GluuTreeModel) e).getInum().equalsIgnoreCase(node.getInum()));
		}
		return false;
	}

	public void closeParentOfNode(GluuTreeModel node) {
		if (this.hasChild()) {
			this.getChilds().forEach(e -> {
				if(((GluuTreeModel) e).hasThisChildNode(node)) {
					e.setExpanded(false);
					return;
				}
			});
		}
	}

}
