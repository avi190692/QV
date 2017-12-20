package com.ai_int.utils;

import static org.apache.poi.ss.usermodel.CellStyle.VERTICAL_CENTER;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;

public class ExcelExportUtil {

	public static void main(String[] args) {
		try {
			//exportTableData(tableData, "Sales Table", "C:/users/shoeb/Desktop/qbExport.xls");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void exportTableData(String[][] data, String tableName, String fileName ) throws Exception {
		
		if (data == null) {
			System.err.println("No records to export in the table");
			return;
		}

		/* Start with Creating a workbook and worksheet object */
		//Workbook wb = WorkbookFactory.create(new File(fileName));
		File file = new File(fileName);
		if (file.exists()) {
			file.delete();
		}
		Workbook wb = new HSSFWorkbook();
		HSSFSheet sheet = (HSSFSheet) wb.createSheet();
		Map<String, CellStyle> styles = createStyles(wb);
		sheet.createFreezePane(0,1);

		for (int i = 0; i < data.length; i++) {
			/* Create a Row */
			HSSFRow row = sheet.createRow(i);
			for (int j = 0; j < data[i].length; j++) {
				HSSFCell localXSSFCell = row.createCell(j);
				//localXSSFCell.
				localXSSFCell.setCellValue(new HSSFRichTextString(data[i][j]));
				if (i == 0) {
					row.setHeight((short)549);
					localXSSFCell.setCellStyle(styles.get("header"));
				} else {
					localXSSFCell.setCellStyle(styles.get("cell"));
				}
			}
		}
		
		for (int i = 0; i < data[0].length; i++) {
			sheet.autoSizeColumn(i);
		}

        FileOutputStream out = new FileOutputStream(fileName);
        wb.write(out);
        out.close();

	}
	

    /**
     * Create a library of cell styles
     */
    private static Map<String, CellStyle> createStyles(Workbook wb){
        Map<String, CellStyle> styles = new HashMap<String, CellStyle>();
        CellStyle style;
        Font titleFont = wb.createFont();
        titleFont.setFontHeightInPoints((short)18);
        titleFont.setBold(true);
        style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(VERTICAL_CENTER);
        style.setFont(titleFont);
        styles.put("title", style);

        Font monthFont = wb.createFont();
        monthFont.setFontHeightInPoints((short)11);
        monthFont.setColor(IndexedColors.WHITE.getIndex());
        style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(VERTICAL_CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setFont(monthFont);
        style.setWrapText(true);
        styles.put("header", style);

        style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setWrapText(true);
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderTop(CellStyle.BORDER_THIN);
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        styles.put("cell", style);

        style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(VERTICAL_CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setDataFormat(wb.createDataFormat().getFormat("0.00"));
        styles.put("formula", style);

        style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(VERTICAL_CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setDataFormat(wb.createDataFormat().getFormat("0.00"));
        styles.put("formula_2", style);

        return styles;
    }
    
    


	private static final String[][] testTableData = {
			{ "No.", "Deal ID", " Type", " Sale Date", " Fruit", " Challan", " Supplier", " Tot. boxes", " Full case",
					" Half case", " FW Agent", " Truck No.", " Driver No.", " Gross Amount", " Charges", " Amanat",
					" Net Amount", " Remarks" },
			{ "1", " 1", " regular", " 2016-12-19", " Mango", " 123456", " 0 Amrood", " 1", " 6", " 3", " 0 Amrood",
					" PN2344", " 2342387", " 2", " 1", " 5", " -4", " Rem first" },
			{ "2", " 2", " regular", " 2016-12-20", " Mango", " 325", " 0 Amrood", " 7", " 9", " 5", " 1 Pawan",
					" PB3242", " 23423432", " 1868", " 371", " 280", " 1217", " Rem amanat test" },
			{ "3", " 3", " regular", " 2016-12-27", " Mango", " 6547", " 1 Pawan", " 11", " 7", " 3", " 1 Pawan",
					" MH1234", " 1231231", " 4212", " 396", " 631", " 3185", " Profile Entry test" },
			{ "4", " 4", " regular", " 2017-01-06", " mango", " 123", " 2 Milky", " 100", " ", " ", " 2 Milky",
					" jkh87", " 877894646", " 15000", " 0", " ", " 15000", " jh" },
			{ "5", " 5", " regular", " 2017-01-12", " picko", " 145", " 0 Amrood", " 100", " ", " ", " 0 Amrood",
					" ph9876", " 879876557", " 15000", " 109700", " 300", " -95000", " hj" },
			{ "6", " 6", " regular", " 2017-01-12", " nuke", " 125", " 2 Milky", " 100", " ", " ", " 2 Milky",
					" mh8754", " 98765432", " 15000", " 7198", " 500", " 7302", " poiuy" },
			{ "7", " 7", " regular", " 2017-01-07", " chickoo", " 987", " 0 Amrood", " 100", " ", " ", " 0 Amrood",
					" pk9876", " 877655443", " 15000", " 3299", " 500", " 11201", " polik" },
			{ "8", " 8", " regular", " 2017-01-08", " mango", " 254", " 0 Amrood", " 100", " ", " ", " 0 Amrood",
					" mh6543", " 123456", " 15000", " 0", " ", " 15000", " poi" },
			{ "9", " 9", " regular", " 2017-01-08", " mango", " 123456", " 2 Milky", " 11", " ", " ", " 2 Milky",
					" po98766", " 99999999", " 825", " 0", " ", " 825", " po" },
			{ "10", " 10", " regular", " 2017-01-08", " mango", " 123", " 0 Amrood", " 100", " ", " ", " 0 Amrood",
					" kj888", " 123654", " 15000", " 0", " ", " 15000", " lk" },
			{ "11", " 11", " regular", " 2017-01-08", " mango", " 25", " 0 Amrood", " 100", " ", " ", " 0 Amrood",
					" 222", " 225", " 14000", " 0", " ", " 14000", " 555" },
			{ "12", " 12", " regular", " 2017-01-08", " mango", " 321", " 0 Amrood", " 100", " ", " ", " 0 Amrood",
					" 025", " 365", " 9000", " 0", " ", " 9000", " 32" },
			{ "13", " 13", " regular", " 2017-01-09", " mango", " ", " 0 Amrood", " 100", " ", " ", " 1 Pawan",
					" mh123", " 6555", " 14000", " 0", " ", " 14000", " lkj" },
			{ "14", " 14", " regular", " 2017-01-12", " mango", " 123", " 0 Amrood", " 100", " ", " ", " 1 Pawan",
					" 123", " 5646464", " 1500", " 0", " ", " 1500", " sad" },
			{ "15", " 15", " regular", " 2017-01-14", " mango", " 12", " 1 Pawan", " 100", " ", " ", " 2 Milky",
					" 12dfs23", " 123213123", " 10000", " 0", " ", " 10000", " frwe" },
			{ "16", " 16", " regular", " 2017-01-14", " banana", " dsfd", " 0 Amrood", " 100", " ", " ", " 0 Amrood",
					" ds231", " 234234234", " 110000", " 0", " ", " 110000", " sfsdfs" },
			{ "17", " 17", " regular", " 2017-01-14", " oilk", " dfs", " 0 Amrood", " 100", " ", " ", " 1 Pawan",
					" dsf32424", " 324322", " 13000", " 0", " ", " 13000", " sd" },
			{ "18", " 18", " regular", " 2017-01-14", " keive", " 12ewre", " 1 Pawan", " 200", " ", " ", " 2 Milky",
					" sd1212", " 12321123", " 2000", " 0", " ", " 2000", " aasd" },
			{ "19", " 19", " regular", " 2017-01-14", " mango", " sdf", " 2 Milky", " 300", " ", " ", " 0 Amrood",
					" mh231", " 123423", " 5100", " 0", " ", " 5100", " sdf" },
			{ "20", " 20", " regular", " 2017-01-14", " keive", " dsds", " 0 Amrood", " 500", " ", " ", " 1 Pawan",
					" sd3242", " 24323423", " 6000", " 0", " ", " 6000", " fdg" },
			{ "21", " 21", " regular", " 2017-01-14", " mango", " 23423", " 0 Amrood", " 1", " ", " ", " 0 Amrood",
					" 342", " 23432", " 324", " 0", " ", " 324", " 234" },
			{ "22", " 22", " regular", " 2017-01-15", " mango", " dfg", " 2 Milky", " 10", " ", " ", " 1 Pawan",
					" mk1212", " 121212", " 130", " 0", " ", " 130", " dsf" },
			{ "23", " 23", " regular", " 2017-01-15", " jamun", " jlk", " 0 Amrood", " 15", " ", " ", " 2 Milky",
					" 1212", " 12121", " 210", " 0", " ", " 210", " sdf" },
			{ "24", " 24", " regular", " 2017-01-15", " mosambi", " dfg", " 1 Pawan", " 1", " ", " ", " 1 Pawan",
					" sdf2323", " 1232321", " 12", " 0", " ", " 12", " sdfsdf" },
			{ "25", " 25", " regular", " 2017-01-15", " nuke", " dsf", " 1 Pawan", " 15", " ", " ", " 1 Pawan",
					" sd34243", " 213234", " 225", " 0", " ", " 225", " dsf" },
			{ "26", " 26", " regular", " 2017-01-23", " mango", " 12345", " 0 Amrood", " 50", " ", " ", " 0 Amrood",
					" kl897", " 9089098", " 750", " 0", " ", " 750", " kjlk" },
			{ "27", " 27", " regular", " 2017-01-23", " picko", " poi", " 0 Amrood", " 100", " ", " ", " 1 Pawan",
					" sd32432", " 234234", " 500", " 20", " 200", " 280", " sd" },
			{ "28", " 28", " regular", " 2017-02-06", " mango", " 12323", " 1 Pawan", " 150", " 12", " 6", " 1 Pawan",
					" 324fdg", " 234324", " 1800", " 126", " 36", " 1638", " sfs" },
			{ "29", " 29", " storage", " 2017-02-09", " mango", " lkju", " 1 Pawan", " 18", " ", " ", " 1 Pawan",
					" kj5444", " 2342323", " 216", " 0", " ", " 216", " por" },
			{ "30", " 30", " storage", " 2017-02-09", " mango", " ljkl", " 0 Amrood", " 10", " ", " ", " 0 Amrood",
					" 123", " 131", " 250", " 0", " ", " 250", " 123" },
			{ "31", " 31", " storage", " 2017-02-10", " mango", " 1235", " 1 Pawan", " 19", " ", " ", " 1 Pawan",
					" sd2311", " 12312", " 1254", " 0", " ", " 1254", " sdf" },
			{ "32", " 32", " storage", " 2017-02-10", " mango", " kl", " 0 Amrood", " 21", " ", " ", " 1 Pawan",
					" kj6547", " 564665", " 315", " 0", " ", " 315", " lkj" },
			{ "33", " 33", " Regular", " 2017-02-14", " mango", " 1565", " 1 Pawan", " 100", " 1", " ", " 2 Milky",
					" 09832", " 2342234", " 1200", " 0", " ", " 1200", " 3243242" },
			{ "34", " 34", " Storage", " 2017-02-14", " mango", " lkj1233", " 1 Pawan", " 15", " 1", " ", " 2 Milky",
					" 3242d", " 123213", " 180", " 0", " ", " 180", " sdf" },
			{ "35", " 35", " Storage", " 2017-02-14", " mango", " lkj", " 2 Milky", " 19", " ", " ", " 0 Amrood",
					" 12dsf", " 12312321", " 285", " 0", " ", " 285", " dsf" },
			{ "36", " 36", " Storage", " 2017-02-15", " mango", " sdf", " 1 Pawan", " 17", " ", " ", " 1 Pawan",
					" 23432", " 23423", " 289", " 2", " 34", " 253", " 234234" },
			{ "37", " 37", " Storage", " 2017-02-15", " mango", " 45", " 0 Amrood", " 11", " ", " ", " 0 Amrood",
					" 56454", " 45645", " 869", " 67", " 33", " 769", " 54564" },
			{ "38", " 38", " Storage", " 2017-02-15", " mango", " dfgdf", " 1 Pawan", " 33", " 33", " ", " 2 Milky",
					" 12121", " 12121", " 19272", " 4046", " 165", " 15061", " dfds" },
			{ "39", " 39", " Storage", " 2017-02-16", " mango", " 123", " 0 Amrood", " 135", " ", " ", " 1 Pawan",
					" sfs23423", " 2342342342", " 2740", " 217", " 675", " 1848", " 2sfss" },
			{ "40", " 40", " Storage", " 2017-02-16", " mango", " fdg", " 1 Pawan", " 25", " ", " ", " 1 Pawan",
					" fsd2342", " 2342334", " 644", " 30", " 25", " 589", " sfs" },
			{ "41", " 41", " Storage", " 2017-02-16", " mango", " dfgdf", " 2 Milky", " 39", " ", " ", " 1 Pawan",
					" ssd32423", " 23434", " 898", " 32", " 195", " 671", " sdfds" },
			{ "42", " 42", " Storage", " 2017-02-16", " mango", " dfgdf", " 1 Pawan", " 13", " ", " ", " 1 Pawan",
					" fsdfs", " 2131", " 268", " 8", " 13", " 247", " 12312" },
			{ "43", " 43", " Storage", " 2017-02-16", " banana", " dfg", " 1 Pawan", " 17", " ", " ", " 1 Pawan",
					" 12121rr", " 32423423", " 709", " 28", " 17", " 664", " 234" },
			{ "44", " 44", " Storage", " 2017-02-16", " mango", " ", " 1 Pawan", " 12", " ", " ", " 0 Amrood", " 1212",
					" 121", " 144", " 4", " 12", " 128", " fsf" },
			{ "45", " 45", " Storage", " 2017-02-18", " mango", " 122", " 1 Pawan", " 19", " ", " ", " 1 Pawan", " 1df",
					" 32432423", " 437", " 16", " 19", " 402", " dsf" },
			{ "46", " 46", " Storage", " 2017-02-19", " mango", " jh", " 1 Pawan", " 100", " ", " ", " 1 Pawan",
					" dfs1312", " 12312123", " 1300", " 0", " ", " 1300", " " },
			{ "47", " 47", " Regular", " 2017-04-04", " mango", " dfg", " 0 Amrood", " ", " 2", " 1", " 0 Amrood",
					" 435435", " 34534534", " 12", " 0", " ", " 12", " dfgfdgd" },
			{ "48", " 48", " Regular", " 2017-04-04", " mango", " 32423", " 0 Amrood", " ", " 2", " 1", " 1 Pawan", " ",
					" ", " 1", " 0", " ", " 1", " " },
			{ "49", " 49", " Regular", " 2017-04-06", " Ananas", " 12", " 1 Pawan", " 12", " 12", " 5", " 1 Pawan",
					" 234", " 32423", " 180", " 4", " 24", " 152", " 234fds" },
			{ "50", " 50", " Regular", " 2017-04-08", " mango", " 324", " 1 Pawan", " ", " 1", " 1", " 3 Desk",
					" sdf123", " 2311231", " 19", " 0", " 1", " 18", " req" },
			{ "51", " 51", " Storage", " 2017-04-09", " banana", " sdf342", " 0 Amrood", " 16", " 12", " 4",
					" 0 Amrood", " 1212", " 1212", " 690", " 24", " 16", " 650", " dfgd" } };

}
