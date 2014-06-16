/**
 * 
 */
package org.gluu.oxtrust.model;

import lombok.Data;

/**
 * @author "Oleksiy Tataryn"
 *
 */
public @Data class Tuple<A, B> {

	private A value0;
	private B value1;
}
