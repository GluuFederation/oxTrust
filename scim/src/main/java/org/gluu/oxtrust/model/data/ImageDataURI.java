/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.data;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.gluu.oxtrust.model.exception.SCIMDataValidationException;

/**
 * A URI of the form data:image/[image extension][;base64],data
 */
public class ImageDataURI extends DataURI {

    public static final String IMAGE_MIME_TYPE = "data:image/";

    /**
     * 
     * @param imageUri
     *        A String presenting a URI of the form data:image/[image extension][;base64],data
     * @throws SCIMDataValidationException
     *         If the given string violates RFC 2396, as augmented by the above deviations
     */
    public ImageDataURI(String imageUri) {
        super(imageUri);
        if (!super.toString().startsWith(IMAGE_MIME_TYPE)) {
            throw new SCIMDataValidationException("The given URI '" + imageUri
                    + "' is not a image data URI.");
        }
    }

    /**
     * 
     * @param dataUri
     *        A URI of the form data:image/[image extension][;base64],data
     * @throws SCIMDataValidationException
     *         if the URI doesn't expects the schema
     */
    public ImageDataURI(URI imageUri) {
        super(imageUri);
        if (!super.toString().startsWith(IMAGE_MIME_TYPE)) {
            throw new SCIMDataValidationException("The given URI '" + imageUri.toString()
                    + "' is not a image data URI.");
        }
    }

    /**
     * 
     * @param inputStream
     *        a inputStream which will be transformed into an DataURI
     * @throws IOException
     *         if the stream can not be read
     * @throws SCIMDataValidationException
     *         if the inputStream can't be converted into an URI
     */
    public ImageDataURI(InputStream inputStream) throws IOException {
        super(inputStream);
        if (!super.toString().startsWith(IMAGE_MIME_TYPE)) {
            throw new SCIMDataValidationException("The given input stream is not an image.");
        }
    }

}
