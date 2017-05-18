package org.gluu.oxtrust.service.custom;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;

/**
 * @author Yuriy Movchan Date: 05/02/2017
 */
@Qualifier
@Retention(RUNTIME)
@Target({ METHOD, FIELD, PARAMETER, TYPE })
@Documented
public @interface LdapCentralConfigurationReload {

	public static final class Literal extends AnnotationLiteral<LdapCentralConfigurationReload> implements LdapCentralConfigurationReload {

		public static final Literal INSTANCE = new Literal();

		private static final long serialVersionUID = 1L;

	}

}
