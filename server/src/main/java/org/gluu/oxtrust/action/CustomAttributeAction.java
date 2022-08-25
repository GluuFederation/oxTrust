/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.jsf2.message.FacesMessages;
import org.gluu.model.GluuAttribute;
import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.service.AttributeService;
import org.gluu.util.StringHelper;
import org.slf4j.Logger;

/**
 * Action class for work with custom attributes
 * 
 * @author Yuriy Movchan Date: 12/24/2012
 */
@ConversationScoped
@Named
public class CustomAttributeAction implements Serializable {

	private static final long serialVersionUID = -719594782175672946L;

	@Inject
	private Logger log;

	@Inject
	private FacesMessages facesMessages;

	@Inject
	private AttributeService attributeService;

	private List<GluuAttribute> attributes;
	private Map<GluuAttribute, String> attributeIds;
	private Map<String, GluuAttribute> attributeToIds;
	private Map<String, List<GluuAttribute>> attributeByOrigin;
	private Map<String, GluuAttribute> attributeInums;
	private List<String> availableAttributeIds;

	private Map<String, String> originDisplayNames;
	private String activeOrigin;

	private List<GluuCustomAttribute> customAttributes;
	private ArrayList<GluuCustomAttribute> origCustomAttributes;

	public void initCustomAttributes(List<GluuAttribute> attributes, List<GluuCustomAttribute> customAttributes,
			List<String> origins, String[] objectClassTypes, String[] objectClassDisplayNames) {
		this.attributes = attributes;
		this.customAttributes = customAttributes;
		this.origCustomAttributes = new ArrayList<GluuCustomAttribute>(customAttributes);

		// Set meta-data and sort by metadata.displayName
		attributeService.setAttributeMetadata(customAttributes, this.attributes);
		attributeService.sortCustomAttributes(customAttributes, "metadata.displayName");

		// Prepare map which allows to build tab
		this.attributeByOrigin = groupAttributesByOrigin(this.attributes);

		// Init special list and maps
		this.availableAttributeIds = new ArrayList<String>();
		this.attributeIds = new IdentityHashMap<GluuAttribute, String>();
		this.attributeToIds = new HashMap<String, GluuAttribute>();
		this.attributeInums = new HashMap<String, GluuAttribute>();

		int componentId = 1;
		for (GluuAttribute attribute : attributes) {
			log.debug("attribute: {}", attribute.getName());
			String id = "a" + String.valueOf(componentId++) + "Id";
			this.availableAttributeIds.add(id);
			this.attributeInums.put(attribute.getInum(), attribute);
			this.attributeIds.put(attribute, id);
			this.attributeToIds.put(id, attribute);
		}

		// Init origin display names
		this.originDisplayNames = attributeService.getAllAttributeOriginDisplayNames(origins, objectClassTypes,
				objectClassDisplayNames);
		this.activeOrigin = this.originDisplayNames.get(origins.get(0));

		// Sync Ids map
		for (GluuCustomAttribute personAttribute : customAttributes) {
			if (personAttribute.getMetadata() != null) {
				String id = this.attributeIds.get(personAttribute.getMetadata());
				this.availableAttributeIds.remove(id);
			}
		}
	}

	public Map<String, List<GluuAttribute>> groupAttributesByOrigin(List<GluuAttribute> attributes) {
		Map<String, List<GluuAttribute>> resultMap = new HashMap<String, List<GluuAttribute>>();

		for (GluuAttribute attribute : attributes) {
			String origin = attribute.getOrigin();

			if (resultMap.containsKey(origin)) {
				resultMap.get(origin).add(attribute);
			} else {
				resultMap.put(origin, new ArrayList<GluuAttribute>(Arrays.asList(attribute)));
			}
		}

		return resultMap;
	}

	public boolean containsCustomAttribute(GluuAttribute attribute) {
		if (attribute == null) {
			return false;
		}

		for (GluuCustomAttribute customAttribute : this.customAttributes) {
			if (attribute.equals(customAttribute.getMetadata())) {
				return true;
			}
		}

		return false;
	}

	public void addCustomAttribute(String inum, boolean mandatory) {
		if (StringHelper.isEmpty(inum)) {
			return;
		}

		GluuAttribute tmpAttribute = attributeInums.get(inum);
		if ((tmpAttribute == null) || containsCustomAttribute(tmpAttribute)) {
			return;
		}

		String id = this.attributeIds.get(tmpAttribute);
		this.availableAttributeIds.remove(id);

		GluuCustomAttribute tmpGluuPersonAttribute = new GluuCustomAttribute(tmpAttribute.getName(), (Object) null,
				true, mandatory);
		tmpGluuPersonAttribute.setMetadata(tmpAttribute);

		this.customAttributes.add(tmpGluuPersonAttribute);
	}

	public void addMultiValuesInAttributes(String inum, boolean mandatory) {
		if (StringHelper.isEmpty(inum)) {
			return;
		}

		GluuAttribute tmpAttribute = this.attributeInums.get(inum);
		if (tmpAttribute == null) {
			return;
		}

		String id = this.attributeIds.get(tmpAttribute);
		this.availableAttributeIds.remove(id);

		Object[] values = null;
		int index = 0;
		for (GluuCustomAttribute customAttribute : this.customAttributes) {
			if (tmpAttribute.equals(customAttribute.getMetadata())) {
				values = customAttribute.getValues();
				break;
			}
			index++;
		}

		Object[] newValues = new Object[values.length + 1];
		System.arraycopy(values, 0, newValues, 0, values.length);
		removeCustomAttribute(inum);
		GluuCustomAttribute tmpGluuPersonAttribute = new GluuCustomAttribute(tmpAttribute.getName(), newValues, true,
				mandatory);
		tmpGluuPersonAttribute.setMetadata(tmpAttribute);

		this.customAttributes.add(index, tmpGluuPersonAttribute);
	}

	public void removeMultiValuesInAttributes(String inum, boolean mandatory, String removeValue) {
		if (StringHelper.isEmpty(inum)) {
			return;
		}

		GluuAttribute tmpAttribute = this.attributeInums.get(inum);
		if (tmpAttribute == null) {
			return;
		}

		String id = this.attributeIds.get(tmpAttribute);
		this.availableAttributeIds.remove(id);

		Object[] values = null;
		Object[] newValues = null;
		int index = 0;
		for (GluuCustomAttribute customAttribute : this.customAttributes) {
			if (tmpAttribute.equals(customAttribute.getMetadata())) {
				values =  customAttribute.getValues();
				newValues = removeElementFromArray(values, removeValue);
				break;
			}
			index++;
		}

		removeCustomAttribute(inum);
		GluuCustomAttribute tmpGluuPersonAttribute = new GluuCustomAttribute(tmpAttribute.getName(), newValues, true,
				mandatory);
		tmpGluuPersonAttribute.setMetadata(tmpAttribute);

		this.customAttributes.add(index, tmpGluuPersonAttribute);
	}

	private Object[] removeElementFromArray(Object[] n, Object removeElement) {
		if (StringHelper.isEmptyString(removeElement)) {
			removeElement = null;
		}
		List<Object> list = new ArrayList<Object>();
		Collections.addAll(list, n);
		list.remove(removeElement);
		n = list.toArray(new String[list.size()]);
		return n;
	}

	public void addCustomAttribute(String inum) {
		addCustomAttribute(inum, false);
	}

	public void addCustomAttributes(List<GluuCustomAttribute> newCustomAttributes) {
		attributeService.setAttributeMetadata(newCustomAttributes, this.attributes);

		for (GluuCustomAttribute newCustomAttribute : newCustomAttributes) {
			GluuAttribute metaData = newCustomAttribute.getMetadata();
			if (metaData != null) {
				addCustomAttribute(metaData.getInum(), newCustomAttribute.isMandatory());
			}
		}
	}

	public void removeCustomAttribute(String inum) {
		if (StringHelper.isEmpty(inum)) {
			return;
		}

		GluuAttribute tmpAttribute = attributeInums.get(inum);
		if ((tmpAttribute == null) || !containsCustomAttribute(tmpAttribute)) {
			return;
		}

		String id = this.attributeIds.get(tmpAttribute);
		this.availableAttributeIds.add(id);

		for (Iterator<GluuCustomAttribute> iterator = this.customAttributes.iterator(); iterator.hasNext();) {
			GluuCustomAttribute tmpGluuPersonAttribute = iterator.next();
			if (tmpAttribute.equals(tmpGluuPersonAttribute.getMetadata())) {
				iterator.remove();
				break;
			}
		}
	}

	public void handleMultiValuedAttributeRemoval(String inum, boolean canRemove) {
		if (canRemove) {
			removeCustomAttribute(inum);
		}
	}

	private void deselectCustomAttributes(List<GluuCustomAttribute> customAttributes) {
		for (GluuCustomAttribute customAttribute : customAttributes) {
			String id = this.attributeIds.get(customAttribute);
			this.availableAttributeIds.add(id);
		}
	}

	private void selectCustomAttributes(List<GluuCustomAttribute> customAttributes) {
		for (GluuCustomAttribute customAttribute : this.customAttributes) {
			GluuAttribute tmpAttribute = attributeInums.get(customAttribute.getMetadata().getInum());
			if ((tmpAttribute == null) || containsCustomAttribute(tmpAttribute)) {
				continue;
			}

			String id = this.attributeIds.get(tmpAttribute);
			this.availableAttributeIds.remove(id);
		}
	}

	public void refreshCustomAttributes(List<GluuCustomAttribute> newCustomAttributes) {
		deselectCustomAttributes(this.customAttributes);

		this.customAttributes = newCustomAttributes;

		selectCustomAttributes(newCustomAttributes);
	}

	/**
	 * Override this method if you need to organize attributes to some specific set
	 * of origins.
	 * 
	 * @param attribute
	 * @return attribute.getOrigin()
	 */
	public String getOriginForAttribute(GluuAttribute attribute) {
		return attribute.getOrigin();
	}

	public GluuCustomAttribute getCustomAttribute(String inum) {
		if (StringHelper.isEmpty(inum)) {
			return null;
		}

		GluuAttribute tmpAttribute = attributeInums.get(inum);
		for (GluuCustomAttribute customAttribute : this.customAttributes) {
			if ((customAttribute.getMetadata() != null) && customAttribute.getMetadata().equals(tmpAttribute)) {
				return customAttribute;
			}
		}

		return null;
	}

	public GluuAttribute getCustomAttribute(String origin, String name) {
		List<GluuAttribute> originAttributes = attributeByOrigin.get(origin);
		if (originAttributes == null) {
			return null;
		}

		for (GluuAttribute attribute : originAttributes) {
			if (StringHelper.equalsIgnoreCase(attribute.getName(), name)) {
				return attribute;
			}
		}

		return null;
	}

	public List<GluuCustomAttribute> detectRemovedAttributes() {
		Set<String> origCustomAttributesSet = new HashSet<String>();

		for (GluuCustomAttribute origCustomAttribute : origCustomAttributes) {
			String attributeName = StringHelper.toLowerCase(origCustomAttribute.getName());
			origCustomAttributesSet.add(attributeName);
		}

		for (GluuCustomAttribute currentCustomAttribute : customAttributes) {
			String attributeName = StringHelper.toLowerCase(currentCustomAttribute.getName());
			origCustomAttributesSet.remove(attributeName);
		}

		List<GluuCustomAttribute> removedCustomAttributes = new ArrayList<GluuCustomAttribute>(
				origCustomAttributesSet.size());
		for (String removeCustomAttribute : origCustomAttributesSet) {
			removedCustomAttributes.add(new GluuCustomAttribute(removeCustomAttribute, new String[0]));
		}

		return removedCustomAttributes;
	}

	public void updateOriginCustomAttributes() {
		this.origCustomAttributes = new ArrayList<GluuCustomAttribute>(customAttributes);

	}

	public List<GluuAttribute> getAttributes() {
		return attributes;
	}

	public Map<GluuAttribute, String> getAttributeIds() {
		return attributeIds;
	}

	public void setAttributeIds(Map<GluuAttribute, String> attributeIds) {
		this.attributeIds = attributeIds;
	}

	public Map<String, List<GluuAttribute>> getAttributeByOrigin() {
		return attributeByOrigin;
	}

	public List<String> getAttributeByOriginKeyList() {
		facesMessages.add(FacesMessage.SEVERITY_ERROR, "" + attributeByOrigin.keySet().size());
		return new ArrayList<>(attributeByOrigin.keySet());
	}

	public void setAttributeByOrigin(Map<String, List<GluuAttribute>> attributeByOrigin) {
		this.attributeByOrigin = attributeByOrigin;
	}

	public Map<String, String> getOriginDisplayNames() {
		return originDisplayNames;
	}

	public List<GluuCustomAttribute> getCustomAttributes() {
		return new ArrayList<GluuCustomAttribute>(customAttributes);
	}

	public String getActiveOrigin() {
		return activeOrigin;
	}

	public void setActiveOrigin(String activeOrigin) {
		this.activeOrigin = activeOrigin;
	}

	public void renderAttribute(ComponentSystemEvent event) {
		// Replace dummy component id with real one
		String dummyId = event.getComponent().getAttributes().get("aid").toString();
		String clientId = event.getComponent().getClientId();

		GluuAttribute attribute = this.attributeToIds.get(dummyId);
		this.attributeIds.put(attribute, clientId);
	}

	public void validateAttributeValues(ComponentSystemEvent event) {
		final FacesContext facesContext = FacesContext.getCurrentInstance();
		final List<Object> values = new ArrayList<Object>();

		event.getComponent().visitTree(VisitContext.createVisitContext(facesContext), new VisitCallback() {
			@Override
			public VisitResult visit(VisitContext context, UIComponent target) {
				if (target instanceof UIInput) {
					GluuAttribute attribute = (GluuAttribute) target.getAttributes().get("attribute");
					if (attribute != null) {
						values.add(((UIInput) target).getValue());
					}
				}

				return VisitResult.ACCEPT;
			}
		});

		values.removeAll(Arrays.asList(null, ""));
		Set<Object> uniqValues = new HashSet<Object>(values);

		if (values.size() != uniqValues.size()) {
			event.getComponent().visitTree(VisitContext.createVisitContext(facesContext), new VisitCallback() {
				@Override
				public VisitResult visit(VisitContext context, UIComponent target) {
					if (target instanceof UIInput) {
						GluuAttribute attribute = (GluuAttribute) target.getAttributes().get("attribute");
						if (attribute != null) {
							((UIInput) target).setValid(false);

							String message = "Please fill out an unique value for all of '" + attribute.getDisplayName()
									+ "' fields";
							facesMessages.add(target.getClientId(facesContext), FacesMessage.SEVERITY_ERROR, message);
						}
					}
					return VisitResult.ACCEPT;
				}
			});

			facesContext.validationFailed();
		}
	}

}