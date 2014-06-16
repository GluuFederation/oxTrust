package org.gluu.oxtrust.util;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.scim.ScimCustomAttributes;
import org.gluu.oxtrust.model.scim.ScimPerson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Yuriy Movchan 10/16/2012
 */
public class CopyUtilsTest {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(CopyUtilsTest.class);

    @Test
	public void copySourceToDestinationWithUpdate() throws Exception {
		ScimPerson source = new ScimPerson();
		List<ScimCustomAttributes> customAttributes = new ArrayList<ScimCustomAttributes>();

		ScimCustomAttributes attr1 = new ScimCustomAttributes();
		attr1.setName("test 1");
		attr1.setValues(Arrays.asList("1", "2"));

		ScimCustomAttributes attr2 = new ScimCustomAttributes();
		attr2.setName("test 2");
		attr2.setValues(Arrays.asList(new String[0]));
		
		customAttributes.add(attr1);
		customAttributes.add(attr2);

		source.setCustomAttributes(customAttributes);

		GluuCustomPerson destination = new GluuCustomPerson();

		GluuCustomPerson resultPerson = CopyUtils.copy(source, destination, true);
		Assert.assertNotNull(resultPerson);
		
		List<GluuCustomAttribute> gluuCustomAttributes = destination.getCustomAttributes();
		Assert.assertNotNull(gluuCustomAttributes);
		Assert.assertTrue(gluuCustomAttributes.size() == 2);
		System.out.println(gluuCustomAttributes);
	}

}