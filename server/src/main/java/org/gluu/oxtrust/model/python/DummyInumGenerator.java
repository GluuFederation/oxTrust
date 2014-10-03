/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.python;

import org.gluu.oxtrust.util.OxTrustConstants;
import org.xdi.util.INumGenerator;
import org.xdi.util.StringHelper;

public class DummyInumGenerator implements InumGeneratorType {

	@Override
	public String generateInum(String orgInum, String prefix) {
		if (StringHelper.isNotEmptyString(orgInum) && StringHelper.isNotEmptyString(prefix)) {
			return orgInum + OxTrustConstants.inumDelimiter + prefix + OxTrustConstants.inumDelimiter + INumGenerator.generate(2);
		}
		return "";

	}

}
