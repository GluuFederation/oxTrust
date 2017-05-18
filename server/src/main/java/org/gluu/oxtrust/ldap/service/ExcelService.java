/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.InputStream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;

import jxl.Sheet;
import jxl.Workbook;

/**
 * Service class to work with Excel files
 * 
 * @author Yuriy Movchan Date: 02.15.2011
 */
@Named("excelService")
@ApplicationScoped
public class ExcelService {

	@Inject
	private Logger log;

	public org.gluu.oxtrust.model.table.Table readExcelFile(InputStream excelFile) {
		org.gluu.oxtrust.model.table.Table result = null;

		Workbook workbook = null;
		try {
			workbook = Workbook.getWorkbook(excelFile);
			// Get the first sheet
			Sheet sheet = workbook.getSheet(0);

			result = new org.gluu.oxtrust.model.table.Table();
			// Loop over columns and rows
			for (int j = 0; j < sheet.getColumns(); j++) {
				for (int i = 0; i < sheet.getRows(); i++) {
					result.addCell(new org.gluu.oxtrust.model.table.Cell(j, i, sheet.getCell(j, i).getContents()));
				}
			}
		} catch (Exception ex) {
			log.error("Failed to read Excel file", ex);
		} finally {
			if (workbook != null) {
				workbook.close();
			}
		}

		return result;
	}

}
