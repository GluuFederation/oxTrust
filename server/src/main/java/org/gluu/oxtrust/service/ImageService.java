/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service;

import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.FilenameUtils;
import org.gluu.model.GluuImage;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.util.repository.RepositoryUtility;
import org.richfaces.model.UploadedFile;
import org.slf4j.Logger;

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

	public GluuImage constructImageWithThumbnail(GluuCustomPerson creator, UploadedFile uploadedFile, int thumbWidth,
			int thumbHeight) {
		GluuImage image = constructImage(creator, uploadedFile);
		try {
			imageRepository.addThumbnail(image, thumbWidth, thumbHeight);
		} catch (Exception ex) {
			log.error("Failed to generate thumbnail for photo {}", image, ex);
		}
		return image;
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
