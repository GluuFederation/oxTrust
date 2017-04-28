/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.ldap.service.ImageService;
import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.richfaces.event.FileUploadEvent;
import org.richfaces.model.UploadedFile;
import org.xdi.model.GluuAttribute;
import org.xdi.model.GluuAttributeDataType;
import org.xdi.model.GluuImage;
import org.xdi.util.StringHelper;

/**
 * Action class for work with custom attributes
 * 
 * @author Yuriy Movchan Date: 12/24/2012
 */
@Scope(ScopeType.CONVERSATION)
@Name("customAttributeAction")
public class CustomAttributeAction implements Serializable {

	private static final long serialVersionUID = -719594782175672946L;

	@Logger
	private Log log;

	@In
	private AttributeService attributeService;

	@In
	private ImageService imageService;

	private GluuImage uploadedImage;

	private List<GluuAttribute> attributes;
	private Map<GluuAttribute, String> attributeIds;
	private Map<String, List<GluuAttribute>> attributeByOrigin;
	private Map<String, GluuAttribute> attributeInums;
	private List<String> availableAttributeIds;

	private Map<String, String> originDisplayNames;
	private String activeOrigin;

	private List<GluuImage> addedPhotos;
	private List<GluuImage> removedPhotos;

	private List<GluuCustomAttribute> customAttributes;
	private ArrayList<GluuCustomAttribute> origCustomAttributes;

	@Create
	public void init() {
		this.addedPhotos = new ArrayList<GluuImage>();
		this.removedPhotos = new ArrayList<GluuImage>();
	}

	public void initCustomAttributes(List<GluuAttribute> attributes, List<GluuCustomAttribute> customAttributes, List<String> origins,
			String[] objectClassTypes, String[] objectClassDisplayNames) {
		this.attributes = new ArrayList<GluuAttribute>(attributes);
		this.customAttributes = customAttributes;
		this.origCustomAttributes = new ArrayList<GluuCustomAttribute>(customAttributes);

		// Set meta-data and sort by metadata.displayName
		attributeService.setAttributeMetadata(customAttributes, this.attributes);
		attributeService.sortCustomAttributes(customAttributes, "metadata.displayName");

		// Prepare map which allows to build tab
		this.attributeByOrigin = groupAttributesByOrigin(attributes);

		// Init special list and maps
		this.availableAttributeIds = new ArrayList<String>();
		this.attributeIds = new IdentityHashMap<GluuAttribute, String>();
		this.attributeInums = new HashMap<String, GluuAttribute>();

		int componentId = 1;
		for (GluuAttribute attribute : attributes) {
			log.debug("attribute: {0}", attribute.getName());
			String id = "a" + String.valueOf(componentId++) + "Id";
			this.availableAttributeIds.add(id);
			this.attributeInums.put(attribute.getInum(), attribute);
			this.attributeIds.put(attribute, id);
		}

		// Init origin display names
		this.originDisplayNames = attributeService.getAllAttributeOriginDisplayNames(origins, objectClassTypes, objectClassDisplayNames);
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

		GluuCustomAttribute tmpGluuPersonAttribute = new GluuCustomAttribute(tmpAttribute.getName(), (String) null, true, mandatory);
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
		
		String[] values = null;
		int index = 0;
		for (GluuCustomAttribute customAttribute : this.customAttributes) {
			if (tmpAttribute.equals(customAttribute.getMetadata())) {
				values = customAttribute.getValues();
				break;
			}
			index ++;
		}
		
		String[] newValues = new String[values.length+1];
		System.arraycopy(values, 0 , newValues , 0 , values.length);
		removeCustomAttribute(inum);
		GluuCustomAttribute tmpGluuPersonAttribute = new GluuCustomAttribute(tmpAttribute.getName(),newValues , true, mandatory);
		tmpGluuPersonAttribute.setMetadata(tmpAttribute);

		this.customAttributes.add(index,tmpGluuPersonAttribute);
	}
	public void removeMultiValuesInAttributes(String inum, boolean mandatory) {
		if (StringHelper.isEmpty(inum)) {
			return;
		}

		GluuAttribute tmpAttribute = this.attributeInums.get(inum);
		if (tmpAttribute == null) {
			return;
		}
		
		String id = this.attributeIds.get(tmpAttribute);
		this.availableAttributeIds.remove(id);
		
		String[] values = null;
		int index = 0;
		for (GluuCustomAttribute customAttribute : this.customAttributes) {
			if (tmpAttribute.equals(customAttribute.getMetadata())) {
				values = customAttribute.getValues();
				if(values.length == 1)
					return; 
				break;
			}
			index ++;
		}
		
		String[] newValues = new String[values.length-1];
		System.arraycopy(values, 0 , newValues , 0 , values.length-1);
		removeCustomAttribute(inum);
		GluuCustomAttribute tmpGluuPersonAttribute = new GluuCustomAttribute(tmpAttribute.getName(),newValues , true, mandatory);
		tmpGluuPersonAttribute.setMetadata(tmpAttribute);

		this.customAttributes.add(index,tmpGluuPersonAttribute);
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
	 * Override this method if you need to organize attributes to some specific
	 * set of origins.
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

		List<GluuCustomAttribute> removedCustomAttributes = new ArrayList<GluuCustomAttribute>(origCustomAttributesSet.size());
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

	public void uploadImage(FileUploadEvent event) {
		UploadedFile uploadedFile = event.getUploadedFile();
		this.uploadedImage = null;
		try {
			GluuImage image = imageService.constructImage((GluuCustomPerson) Component.getInstance("currentPerson"), uploadedFile);
			image.setStoreTemporary(true);
			if (imageService.createImageFiles(image)) {
				this.uploadedImage = image;
			}
		} finally {
			try {
				uploadedFile.delete();
			} catch (IOException ex) {
				log.error("Failed to remove temporary image", ex);
			}
		}
	}

	public void addPhoto(String inum) {
		if (this.uploadedImage == null) {
			return;
		}

		GluuCustomAttribute customAttribute = getCustomAttribute(inum);
		if (customAttribute == null) {
			return;
		}

		setIconImageImpl(customAttribute, this.uploadedImage);
	}

	private void setIconImageImpl(GluuCustomAttribute customAttribute, GluuImage image) {
		GluuImage oldImage = imageService.getGluuImageFromXML(customAttribute.getValue());
		if (oldImage != null) {
			removedPhotos.add(oldImage);
		}

		customAttribute.setValue(imageService.getXMLFromGluuImage(image));
		addedPhotos.add(image);
	}

	public byte[] getPhotoThumbData(String inum) {
		GluuCustomAttribute customAttribute = getCustomAttribute(inum);
		if (customAttribute != null) {
			GluuImage image = imageService.getImage(customAttribute);
			if(image != null){
				image.setStoreTemporary(addedPhotos.contains(image));
				return imageService.getThumImageData(image);
			}
		}

		return imageService.getBlankPhotoData();
	}

	public String getPhotoSourceName(String inum) {
		GluuCustomAttribute customAttribute = getCustomAttribute(inum);
		if (customAttribute != null) {
			GluuImage image = imageService.getImage(customAttribute);

			return image == null ? null : image.getSourceName();
		}

		return null;
	}

	public void removePhoto(String inum) {
		if (StringHelper.isEmpty(inum)) {
			return;
		}

		GluuCustomAttribute customAttribute = getCustomAttribute(inum);
		if ((customAttribute == null) || StringHelper.isEmpty(customAttribute.getValue())) {
			return;
		}

		GluuImage image = imageService.getImage(customAttribute);
		if(image!=null){
			image.setStoreTemporary(addedPhotos.contains(image));
			if (image.isStoreTemporary()) {
				imageService.deleteImage(image);
				addedPhotos.remove(image);
			} else {
				removedPhotos.add(image);
			}
		}
		customAttribute.setValue(null);
	}

	private void removeRemovedPhotos() {
		for (GluuImage image : removedPhotos) {
			imageService.deleteImage(image);
		}

		removedPhotos.clear();
	}

	public void savePhotos() {
		// Move added photos to persistent location
		for (GluuImage image : addedPhotos) {
			imageService.moveImageToPersistentStore(image);
		}

		addedPhotos.clear();
		removeRemovedPhotos();
	}

	public void cancelPhotos() {
		for (GluuImage image : addedPhotos) {
			imageService.deleteImage(image);
		}

		removedPhotos.clear();
	}

	public void deletePhotos() {
		for (GluuCustomAttribute customAttribute : this.customAttributes) {
			if (GluuAttributeDataType.PHOTO.equals(customAttribute.getMetadata().getDataType())) {
				removePhoto(customAttribute.getMetadata().getInum());

			}
		}

		removeRemovedPhotos();
	}

	@Destroy
	public void destroy() {
		// When user decided to leave form without saving we must remove added
		// images from disk
		cancelPhotos();
	}

}