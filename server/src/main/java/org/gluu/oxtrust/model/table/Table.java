/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.table;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Table
 * 
 * @author Yuriy Movchan Date: 02.15.2010
 */
public class Table implements Serializable {

	private static final long serialVersionUID = 2766629412918360302L;

	private HashMap<Integer, HashMap<Integer, Cell>> cells = new HashMap<Integer, HashMap<Integer, Cell>>();
	private int countCols = -1, countRows = -1;

	public String getCellValue(int col, int row) {
		HashMap<Integer, Cell> rows = cells.get(col);
		if (rows == null) {
			return "";
		}

		Cell cell = rows.get(row);
		if (cell == null) {
			return "";
		}

		return cell.getValue();
	}

	public void addCell(Cell cell) {
		int col = cell.getCol();
		int row = cell.getRow();

		HashMap<Integer, Cell> rows = this.cells.get(col);
		if (rows == null) {
			rows = new HashMap<Integer, Cell>();
			cells.put(col, rows);
		}

		rows.put(row, cell);

		this.countCols = Math.max(this.countCols, col);
		this.countRows = Math.max(this.countRows, row);
	}

	public int getCountCols() {
		return countCols;
	}

	public void setCountCols(int countCols) {
		this.countCols = countCols;
	}

	public int getCountRows() {
		return countRows;
	}

	public void setCountRows(int countRows) {
		this.countRows = countRows;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Table:\n");

		for (int i = 0; i < this.countCols; i++) {
			sb.append("Column: ").append(i).append(":\t");
			for (int j = 0; j < this.countRows; j++) {
				sb.append(getCellValue(i, j));
				if (j == this.countRows - 1) {
					sb.append("\n");
				} else {
					sb.append(", ");
				}
			}
		}

		return sb.toString();
	}

}
