package org.gluu.oxtrust.util.velocity;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.apache.velocity.util.ClassUtils;
import org.apache.velocity.util.ExtProperties;

/**
 * A loader for templates stored on the classpath. Treats the template as
 * relative to the configured root path. If the root path is empty treats the
 * template name as an absolute path.
 */
public class ClasspathPathResourceLoader extends ResourceLoader {
	/**
	 * The paths to search for templates.
	 */
	private List<String> paths = new ArrayList<>();

	/**
	 * @see ResourceLoader#init(org.apache.velocity.util.ExtProperties)
	 */
	public void init(ExtProperties configuration) {
		log.trace("PathClasspathResourceLoader: initialization starting.");

		paths.addAll(configuration.getVector(RuntimeConstants.RESOURCE_LOADER_PATHS));

		// trim spaces from all paths
		for (ListIterator<String> it = paths.listIterator(); it.hasNext();) {
			String path = StringUtils.trim(it.next());
			it.set(path);
			log.debug("PathClasspathResourceLoader: adding path '{}'", path);
		}
		log.trace("PathClasspathResourceLoader: initialization complete.");
	}

	/**
	 * Get a Reader so that the Runtime can build a template with it.
	 *
	 * @param name     name of template to get
	 * @param encoding asked encoding
	 * @return InputStream containing the template
	 * @throws ResourceNotFoundException if template not found in classpath.
	 * @since 2.0
	 */
	public Reader getResourceReader(String templateName, String encoding) throws ResourceNotFoundException {
		Reader result = null;

		if (StringUtils.isEmpty(templateName)) {
			throw new ResourceNotFoundException("No template name provided");
		}

		for (String path : paths) {
			InputStream rawStream = null;
			try {
				rawStream = ClassUtils.getResourceAsStream(getClass(), path + "/" + templateName);
				if (rawStream != null) {
					result = buildReader(rawStream, encoding);
					if (result != null) {
						break;
					}
				}
			} catch (Exception fnfe) {
				if (rawStream != null) {
					try {
						rawStream.close();
					} catch (IOException ioe) {
					}
				}
				throw new ResourceNotFoundException("ClasspathResourceLoader problem with template: " + templateName, fnfe);
			}
		}

		if (result == null) {
			String msg = "ClasspathResourceLoader Error: cannot find resource " + templateName;

			throw new ResourceNotFoundException(msg);
		}

		return result;
	}

	/**
	 * @see ResourceLoader#isSourceModified(org.apache.velocity.runtime.resource.Resource)
	 */
	public boolean isSourceModified(Resource resource) {
		return false;
	}

	/**
	 * @see ResourceLoader#getLastModified(org.apache.velocity.runtime.resource.Resource)
	 */
	public long getLastModified(Resource resource) {
		return 0;
	}
}
