/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.ArrayUtils;
import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.model.GluuImage;
import org.gluu.util.image.ImageTransformationUtility;
import org.gluu.util.repository.RepositoryUtility;
import org.slf4j.Logger;

import com.google.common.net.MediaType;

/**
 * Manage images in photo repository
 * 
 * @author Yuriy Movchan Date: 11.03.2010
 */
@Named("imageRepository")
@ApplicationScoped
public class ImageRepository {

	@Inject
	private Logger log;

	@Inject
	private AppConfiguration appConfiguration;
	private static boolean createBackupDuringRemoval = true;
	private String sourceHome, thumbHome;
	private String tmpSourceHome, tmpThumbHome;
	private String removedSourceHome, removedThumbHome;

	private File photoRepositoryRootDirFile;

	private byte[] blankImage, blankPhoto, blankIcon;

	private int countLevels;
	private int countFoldersPerLevel;

	private FileTypeMap fileTypeMap;

	@PostConstruct
	public void init() throws Exception {

	}

	public void initFileTypesMap() throws Exception {
		fileTypeMap = MimetypesFileTypeMap.getDefaultFileTypeMap();
		try(InputStream is = ImageRepository.class.getClassLoader().getResourceAsStream("META-INF/mimetypes-gluu.default")) {
			if (is != null) {
				fileTypeMap = new MimetypesFileTypeMap(is);
			}
		} catch (Exception ex) {
			log.error("Failed to load file types map. Using default one.", ex);
			fileTypeMap = new MimetypesFileTypeMap();
		} 
	}

	/**
	 * Creates image in repository
	 * 
	 * @param image
	 *            image file
	 * @return true if image was added successfully, false otherwise
	 * @throws Exception
	 */

	public boolean addThumbnail(GluuImage image, int thumbWidth, int thumbHeight) throws Exception {
		if (!image.getSourceContentType().matches("image/(gif|png|jpeg|jpg|bmp)")) {
			return false;
		}

		// Load source image
		BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image.getData()));
		if (bufferedImage == null) {
			throw new IOException("The image data is empty");
		}

		// Set source image size
		image.setWidth(bufferedImage.getWidth());
		image.setHeight(bufferedImage.getHeight());

		BufferedImage bi = ImageTransformationUtility.scaleImage(bufferedImage, thumbWidth, thumbHeight);

		// Set thumb properties
		image.setThumbWidth(bi.getWidth());
		image.setThumbHeight(bi.getHeight());

		image.setThumbContentType(MediaType.PNG.toString());

		// Store thumb image
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ImageIO.write(bufferedImage, "png", bos);
			image.setThumbData(bos.toByteArray());
		} finally {
			bos.close();
		}

		return true;
	}

	private void setGeneratedImagePathes(GluuImage image, String thumbExt) throws Exception {
		String uuid = RepositoryUtility.generateUUID();
		String ext = RepositoryUtility.getFileNameExtension(image.getSourceName());
		String sourceFileName = uuid + ext;
		String thumbFileName = uuid + (thumbExt == null ? ext : thumbExt);

		String sourceFilePath = RepositoryUtility.generateTreeFolderPath(countLevels, countFoldersPerLevel,
				sourceFileName);
		String thumbFilePath = RepositoryUtility.generateTreeFolderPath(countLevels, countFoldersPerLevel,
				thumbFileName);

		image.setUuid(uuid);
		image.setSourceFilePath(sourceFilePath);
		image.setThumbFilePath(thumbFilePath);
	}

	private boolean deleteFile(File file, boolean removeEmptyfoldersTree) {
		boolean result = true;

		if (file.exists()) {
			result = file.delete();
			if (removeEmptyfoldersTree) {
				removeEmptyfoldersTree(file.getParentFile(), countLevels);
			}
		}

		return result;
	}

	private void removeEmptyfoldersTree(File folder, int remainLevels) {
		if (photoRepositoryRootDirFile.equals(folder) || (remainLevels == 0)) {
			return;
		}

		File[] files = folder.listFiles();
		if (files == null) { // null if security restricted
			return;
		}

		if (files.length == 0) {
			File parent = folder.getParentFile();
			deleteFile(folder, false);
			removeEmptyfoldersTree(parent, --remainLevels);
		}
	}

	private void createFoldersTree(File folder) {
		if (folder != null && folder.mkdirs()) {
			// findbugs: probably needs to do something here
		}
	}

	public byte[] getBlankImage() {
		// findbugs: copy on return to not expose internal representation
		return ArrayUtils.clone(blankImage);
	}

	public byte[] getBlankPhoto() {
		// findbugs: copy on return to not expose internal representation
		return ArrayUtils.clone(blankPhoto);
	}

	public byte[] getBlankIcon() {
		return ArrayUtils.clone(blankIcon);
	}

	public boolean isIconImage(GluuImage image) {
		if (image.getSourceContentType().equals("application/octet-stream")) {
			image.setSourceContentType(fileTypeMap.getContentType(image.getSourceName()));
		}

		return image.getSourceContentType().matches("image/(x-icon|x-ico|jpeg|jpg|vnd.microsoft.icon)");
	}

}
