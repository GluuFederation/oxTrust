package org.gluu.oxtrust.ldap.service;

import java.io.InputStream;

import jxl.Sheet;
import jxl.Workbook;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

/**
 * Service class to work with Excel files
 * 
 * @author Yuriy Movchan Date: 02.15.2011
 */
@Name("excelService")
@Scope(ScopeType.APPLICATION)
@AutoCreate
public class ExcelService {

	@Logger
	private Log log;

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

	/**
	 * Get excelService instance
	 * 
	 * @return ExcelService instance
	 */
	public static ExcelService instance() {
		return (ExcelService) Component.getInstance(ExcelService.class);
	}

}
