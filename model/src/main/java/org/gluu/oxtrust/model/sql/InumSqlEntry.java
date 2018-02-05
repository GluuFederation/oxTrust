/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.sql;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * InumSqlEntry sql Entry
 * 
 * @author Reda Zerrad Date: 08.25.2012
 */
@Entity
@Table(name = "inumsTable", uniqueConstraints = { @UniqueConstraint(columnNames = "inum") })
public class InumSqlEntry implements Serializable {

	/**
     *
     */
	private static final long serialVersionUID = 8075213460081874416L;
	private Integer id;
	private String inum;
	private String type;

	public InumSqlEntry() {
	}

	@Id
	@GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Column(name = "inum", unique = true)
	public String getInum() {
		return this.inum;
	}

	public void setInum(String inum) {
		this.inum = inum;
	}

	@Column(name = "type")
	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
