/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.FilenameUtils;
import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.richfaces.model.UploadedFile;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.model.GluuImage;
import org.xdi.model.attribute.AttributeDataType;
import org.xdi.service.XmlService;
import org.xdi.util.StringHelper;
import org.xdi.util.repository.RepositoryUtility;

/**
 * Service class to work with images in photo repository
 * 
 * @author Yuriy Movchan Date: 11.04.2010
 */
@Named("imageService")
@ApplicationScoped
public class ImageService {

	@Inject
	private Logger log;

	@Inject
	private ImageRepository imageRepository;

	@Inject
	private XmlService xmlService;

	@Inject
	private AppConfiguration appConfiguration;

	public String getXMLFromGluuImage(GluuImage photo) {
		return xmlService.getXMLFromGluuImage(photo);
	}

	public GluuImage getGluuImageFromXML(String xml) {
		return xmlService.getGluuImageFromXML(xml);
	}

	/**
	 * Creates GluuImage object from uploaded file
	 * 
	 * @param creator
	 *            person uploading the file
	 * @param uploadedFile
	 *            uploaded file
	 * @return GluuImage object
	 */
	public GluuImage constructImage(GluuCustomPerson creator, UploadedFile uploadedFile) {
		GluuImage image = new GluuImage();
		image.setUuid(RepositoryUtility.generateUUID());
		image.setCreationDate(new Date());
		image.setCreator(creator.getDn());
		image.setSourceName(FilenameUtils.getName(uploadedFile.getName()));
		image.setSourceContentType(uploadedFile.getContentType());
		image.setSize(uploadedFile.getSize());
		image.setData(uploadedFile.getData());

		return image;
	}

	public GluuImage constructImageWithThumbnail(GluuCustomPerson creator, UploadedFile uploadedFile, int thumbWidth, int thumbHeight) {
		GluuImage image = constructImage(creator, uploadedFile);
		
		try {
			imageRepository.addThumbnail(image, thumbWidth, thumbHeight);
		} catch (Exception ex) {
			log.error("Failed to generate thumbnail for photo {}", image, ex);
		}

		return image;
	}

	/**
	 * Creates image(s) in repository
	 * 
	 * @param image
	 *            GluuImage object
	 * @return true if files are successfully created, false otherwise
	 * @throws Exception
	 */
	public boolean createImageFiles(GluuImage image, int thumbWidth, int thumbHeight) {
		try {
			return imageRepository.createRepositoryImageFiles(image, thumbWidth, thumbHeight);
		} catch (Exception ex) {
			log.error("Failed to save photo {}", image, ex);
		}
		return false;
	}

	/**
	 * Creates image(s) in repository
	 * 
	 * @param image
	 *            GluuImage object
	 * @return true if files are successfully created, false otherwise
	 * @throws Exception
	 */
	public boolean createImageFiles(GluuImage image) {
		return createImageFiles(image, appConfiguration.getPhotoRepositoryThumbWidth(), appConfiguration.getPhotoRepositoryThumbHeight());
	}

	/**
	 * Returns an image
	 * 
	 * @param customAttribute
	 * @return GluuImage object
	 */
	public GluuImage getImage(GluuCustomAttribute customAttribute) {
		if ((customAttribute == null) || StringHelper.isEmpty(customAttribute.getValue())
				|| !AttributeDataType.BINARY.equals(customAttribute.getMetadata().getDataType())) {
			return null;
		}

		return getGluuImageFromXML(customAttribute.getValue());
	}

	/**
	 * Deletes the image from repository
	 * 
	 * @param customAttribute
	 * @throws Exception
	 */
	public void deleteImage(GluuCustomAttribute customAttribute) throws Exception {
		GluuImage image = getImage(customAttribute);
		deleteImage(image);
	}

	public void deleteImage(GluuImage image) {
		if (image != null) {
			imageRepository.deleteImage(image);
		}
	}

	public byte[] getBlankImageData() {
		return imageRepository.getBlankImage();
	}

	public byte[] getBlankPhotoData() {
		return imageRepository.getBlankPhoto();
	}

	public byte[] getBlankIconData() {
		return imageRepository.getBlankIcon();
	}

	public byte[] getThumImageData(GluuCustomAttribute customAttribute) throws Exception {
		GluuImage image = getImage(customAttribute);
		return getThumImageData(image);
	}

	public byte[] getThumImageData(GluuImage image) {
		if (image != null) {
			try {
				return imageRepository.getThumbImageData(image);
			} catch (Exception ex) {
				log.error("Failed to load GluuImage {}", image, ex);
			}
		}

		return getBlankImageData();
	}

	public byte[] getThumIconData(GluuImage image) {
		if (image != null) {
			try {
				return imageRepository.getThumbImageData(image);
			} catch (Exception ex) {
				log.error("Failed to load GluuImage {}", image, ex);
			}
		}

		return getBlankIconData();
	}

	public void moveImageToPersistentStore(GluuImage image) {
		try {
			imageRepository.moveImageToPersistentStore(image);
		} catch (Exception ex) {
			log.error("Failed to load GluuImage {}", image, ex);
		}
	}

	public void moveLogoImageToPersistentStore(GluuImage image) {
		try {
			imageRepository.moveLogoImageToPersistentStore(image);
		} catch (IOException ex) {
			log.error("Failed to load GluuImage {}", image, ex);
		}
	}

	public File getThumbFile(GluuImage image) throws Exception {
		return imageRepository.getThumbFile(image);
	}

	public File getSourceFile(GluuImage image) throws Exception {
		return imageRepository.getSourceFile(image);
	}

	public boolean createFaviconImageFiles(GluuImage image) throws Exception {
		try {
			return imageRepository.createRepositoryFaviconImageFiles(image);
		} catch (IOException ex) {
			log.error("Failed to save photo {}", image, ex);
		}

		return false;
	}

	public boolean isIconImage(GluuImage image) {
		return imageRepository.isIconImage(image);
	}

	public byte[] getImageDate(UploadedFile uploadedFile) {
		if (uploadedFile == null) {
			return null;
		}

		return uploadedFile.getData();
	}

}
