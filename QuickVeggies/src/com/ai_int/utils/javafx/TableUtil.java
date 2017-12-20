package com.ai_int.utils.javafx;

import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.ai_int.utils.PDFUtil;
import com.ai_int.utils.PrintUtil;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class TableUtil {

	private static final String PRE_VISIBILITY = "preVisibility";

	@SuppressWarnings("rawtypes")
	public static String[][] toArray(TableView<?> tv) {
		if (tv == null || tv.getItems() == null || tv.getItems().size() < 1) {
			return null;
		}
		TreeSet<Integer> validColIdxSet = new TreeSet<>();
		for (TableColumn<?, ?> tc : tv.getColumns()) {
			if (isColumnValidForExport(tc)) {
				int idx = tv.getColumns().indexOf(tc);
				validColIdxSet.add(idx);
			}
		}

		String[][] tableData = new String[tv.getItems().size() + 1][validColIdxSet.size()];

		int count = 0;

		for (Integer colIdx : validColIdxSet) {
			TableColumn<?, ?> tc = tv.getColumns().get(colIdx);
			tableData[0][count++] = tc.getText();
		}

		for (int rowIdx = 0; rowIdx < tv.getItems().size(); rowIdx++) {
			count = 0;
			for (Integer colIdx : validColIdxSet) {
				TableColumn currCol = tv.getColumns().get(colIdx);
				Object cellData = currCol.getCellData(rowIdx);
				// System.out.println("Col idx : " + colIdx + " cellData: " +
				// cellData);
				cellData = cellData == null ? "" : cellData;
				tableData[rowIdx + 1][count++] = cellData.toString();
			}
		}

		for (int i = 0; i < tableData.length; i++) {
		//	System.out.println(Arrays.toString(tableData[i]));
		}
		return tableData;
	}

	public static boolean isColumnValidForExport(TableColumn<?, ?> tc) {
		boolean isValidColHead = false;
		if (tc.isVisible()) {
			String columnText = tc.getText();
			if (columnText != null && !columnText.trim().isEmpty()) {
				isValidColHead = true;
			}
		}
		return isValidColHead;
	}
	
	public static void printTable(TableView<?> tvToPrint, String fileTitle, TableColumn<?, ?>... ignoreColumList) {
		toggleColumnVisibility(ignoreColumList, false);
		TableView<?> tv = tvToPrint;
		String[][] dataArr = TableUtil.toArray(tv);
		double totalColumnSize = tv.getColumns().stream().filter(tc -> TableUtil.isColumnValidForExport(tc))
				.mapToDouble(t -> t.getWidth()).sum();
		List<TableColumn<?, ?>> validCols = tv.getColumns().stream()
				.filter(tc -> TableUtil.isColumnValidForExport(tc)).collect(Collectors.toList());
		float[] widthPcentArr = new float[validCols.size()];
		AtomicInteger count = new AtomicInteger(0);
		validCols.forEach((tc) -> {
			double size = tc.getWidth();
			Double percent = (size * 100) / totalColumnSize;
			widthPcentArr[count.getAndIncrement()] = percent.intValue();
		});
		String pdfFile = PDFUtil.prepareListPdf(fileTitle, dataArr, widthPcentArr);
		PrintUtil.printPDF(pdfFile);
		toggleColumnVisibility(ignoreColumList, true);
	}

	/*
	 * Sets the column's visibility of false if parameter visibility is set to false, and sets it to earlier
	 * state if it is true
	 */
	private static void toggleColumnVisibility(TableColumn<?, ?>[] ignoreColumList, boolean visibility) {
		if (ignoreColumList == null || ignoreColumList.length < 1) 
			return;
		for (TableColumn<?, ?>  tc : ignoreColumList) {
			if (tc == null)
				continue;
			if (visibility == false) {
				tc.getProperties().put(PRE_VISIBILITY, tc.isVisible());
				tc.setVisible(false);			
			} else {
				tc.setVisible((boolean) tc.getProperties().get(PRE_VISIBILITY));
			}
		}
				
	}

}
