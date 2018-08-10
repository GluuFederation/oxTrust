/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.gluu.oxtrust.model.table.Table;
import org.slf4j.Logger;

/**
 * Service class to work with Excel files
 * 
 * @author Yuriy Movchan Date: 02.15.2011
 * @author Gasmyr Mougang Date: 11.06.2018
 */
@Named("excelService")
@ApplicationScoped
public class ExcelService {

	@Inject
	private Logger log;

	public Table read(InputStream is) {
		org.gluu.oxtrust.model.table.Table result = null;
		try {
			result = new org.gluu.oxtrust.model.table.Table();
			Workbook workbook = WorkbookFactory.create(is);
			Sheet datatypeSheet = workbook.getSheetAt(0);
			Iterator<Row> iterator = datatypeSheet.iterator();
			while (iterator.hasNext()) {
				Row currentRow = iterator.next();
				Iterator<Cell> cellIterator = currentRow.iterator();
				while (cellIterator.hasNext()) {
					Cell currentCell = cellIterator.next();
					result.addCell(new org.gluu.oxtrust.model.table.Cell(currentCell.getColumnIndex(),
							currentCell.getRowIndex(), currentCell.getStringCellValue()));
				}
			}
		} catch (IOException e) {
			log.error("Error: " + e);
		} catch (EncryptedDocumentException e) {
			log.error("Error: " + e);
		} catch (InvalidFormatException e) {
			log.error("Error: " + e);
		}
		return result;
	}

}
