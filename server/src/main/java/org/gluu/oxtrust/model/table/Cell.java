/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.table;

import java.io.Serializable;

/**
 * Table cell
 * 
 * @author Yuriy Movchan Date: 02.15.2010
 */
public class Cell implements Serializable {

	private static final long serialVersionUID = 2175016678917607494L;

	private int col, row;
	private String value;

	public Cell(int col, int row, String value) {
		this.col = col;
		this.row = row;
		this.value = value;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
