/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.python;

/**
 * Base interface for python script
 * 
 * @author Reda Zerrad Date: 08.22.2012
 */
public interface InumGeneratorType {

	public String generateInum(String orgInum, String prefix);

}
