package org.gluu.oxtrust.ldap.service.intercept;

import java.io.File;
import java.io.Serializable;

import org.codehaus.jackson.map.ObjectMapper;
import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.model.GluuOrganization;
import org.gluu.oxtrust.model.InumConf;
import org.gluu.oxtrust.model.python.DummyInumGenerator;
import org.gluu.oxtrust.model.python.InumGeneratorType;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.python.core.PyLong;
import org.python.core.PyObject;
import org.xdi.exception.PythonException;
import org.xdi.service.PythonService;
import org.xdi.util.StringHelper;

/**
 * Provides factory methods needed to create inum generator interceptor
 * 
 * @author Reda Zerrad Date: 08.22.2012
 */
@Scope(ScopeType.STATELESS)
@Name("inumGeneratorInterceptorService")
@AutoCreate
public class InumGeneratorInterceptorService implements Serializable {

	/**
     *
     */
	private static final long serialVersionUID = -4654543365985799827L;

	@Logger
	private Log log;

	@In
	private PythonService pythonService;

	@In
	private OrganizationService organizationService;

	private static final InumGeneratorType DUMMY_INUM_GENERATOR = new DummyInumGenerator();

	private static final String PYTHON_INUM_GENERATOR_TYPE = "InumGenerator";

	public InumGeneratorType createInumGenerator() throws Exception {
		InumGeneratorType inumGenerator;
		try {
			inumGenerator = createInumGeneratorWithPythonException();
		} catch (PythonException ex) {
			log.error("Failed to prepare interceptor interface", ex);
			return null;
		}
		if (inumGenerator == null) {
			log.debug("Using default interceptor class");
			inumGenerator = DUMMY_INUM_GENERATOR;
		}

		return inumGenerator;

	}

	private InumGeneratorType createInumGeneratorWithPythonException() throws Exception {
		GluuOrganization org = organizationService.getOrganization();
		InumConf conf = (InumConf) jsonToObject(org.getOxInumConfig(), InumConf.class);
		if (StringHelper.isEmpty(conf.getScriptName())) {
			return null;
		}

		String tomcatHome = System.getProperty("catalina.home");
		if (tomcatHome == null) {
			return null;
		}

		String fullPath = tomcatHome + File.separator + "conf" + File.separator + "python" + File.separator + conf.getScriptName();

		return pythonService.loadPythonScript(fullPath, PYTHON_INUM_GENERATOR_TYPE, InumGeneratorType.class, new PyObject[] { new PyLong(
				System.currentTimeMillis()) });
	}

	private Object jsonToObject(String json, Class<?> clazz) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(json, clazz);
	}

	public String executeInumGenerator(InumGeneratorType inumGeneratorType, String orgInum, String prefix) {

		try {
			log.debug("Executing python script interceptor in order to generate inum");
			return inumGeneratorType.generateInum(orgInum, prefix);

		} catch (Exception ex) {
			log.error(ex);
			log.error(String.format("Failed to generate inum via python interceptor script"));
			return null;
		}
	}

	/**
	 * Get InumGeneratorInterceptorService instance
	 * 
	 * @return InumGeneratorInterceptorService instance
	 */
	public static InumGeneratorInterceptorService instance() throws Exception {
		return (InumGeneratorInterceptorService) Component.getInstance(InumGeneratorInterceptorService.class);
	}

}
