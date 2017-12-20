package com.quickveggies.misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.util.Callback;

public class XlsTableReader {

    private String DR = "DR";
    private String CR = "CR";
    private int rowsNum = 0;
    private String parsedFilePath = null;
    private String crDrColName = "Cr/Dr";
    private List<List<String>> tabularData = new ArrayList<>();
    private List<String> columnHeaders = null;
    private Integer headerRowNumber = 0;

    public void setCrDrColName(String crDrColName) {
        this.crDrColName = crDrColName;
    }

    public void setTransAmtColName(String transAmtColName) {
        this.transAmtColName = transAmtColName;
    }

    private String transAmtColName = "Transaction Amount(INR)";
    private final boolean isSingleColumnSheet;

    public int getRowsNum() {
        return rowsNum;
    }

    public XlsTableReader(boolean isSingleColumnSheet) {
        this.isSingleColumnSheet = isSingleColumnSheet;
    }

    // xls reading tool
    public void browseXls() {
        // choose xls file
        File xlsFile = chooseXlsFile();
        if (xlsFile == null) {
            return;
        }
        processXlsFile(xlsFile);
    }

    public void processXlsFile(File xlsFile) throws EncryptedDocumentException {
        try (FileInputStream inputStream = new FileInputStream(xlsFile);
                Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet firstSheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = firstSheet.iterator();
            
            while (iterator.hasNext()) {
                Row nextRow = iterator.next();
                Iterator<Cell> cellIterator = nextRow.cellIterator();
                //Check if row isn't empty
                int number = 0;
                
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    
                    if (cell.getCellType() != Cell.CELL_TYPE_BLANK) {
                        number++;
                    }
                }
                if (number < 3) {
                    //Go to the next row
                    continue;
                }
                //Copy the current row
                List<String> currRow = new ArrayList<>();
                tabularData.add(currRow);
                cellIterator = nextRow.cellIterator();
                
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    String strVal = null;
                    
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_STRING:
                            strVal = cell.getStringCellValue();
                            break;
                        case Cell.CELL_TYPE_BOOLEAN:
                            strVal = cell.getBooleanCellValue() + "";
                            break;
                        case Cell.CELL_TYPE_NUMERIC:
                            if (DateUtil.isCellDateFormatted(cell)) {
                                Date date = cell.getDateCellValue();
                                strVal = new SimpleDateFormat("dd/MM/yyyy").format(date);
                            } else {
                                strVal = Integer.toString(((int) cell.getNumericCellValue()));
                            }
                            break;
                    }
                    if (strVal != null) {
                        currRow.add(strVal);
                    }
                    else {
                        currRow.add("");
                    }
                }
                rowsNum++;
            }
        }
        catch (FileNotFoundException e) {
            System.out.println("file not found for readXls");
            e.printStackTrace();
        }
        catch (IOException ex) {
            System.out.println("ioexception for readXls");
            ex.printStackTrace();
        }
        catch (InvalidFormatException exc) {
            System.out.println("invalid format exception for readXls");
            exc.printStackTrace();
        }
        List<Integer> nonEmptyColumns = new ArrayList<>();
        for (int i = 0; i < tabularData.get(0).size(); i++) {
            boolean isEmpty = true;
            for (int j = 1; j < tabularData.size(); j++) {
                if (i >= tabularData.get(j).size()) {
                    break;
                }
                String value = tabularData.get(j).get(i).trim();
                if (!value.isEmpty()) {
                    isEmpty = false;
                    break;
                }
            }
            if (!isEmpty) {
                nonEmptyColumns.add(i);
            }
        }
        List<List<String>> tmpTable = new ArrayList<>();
        for (Integer existingColIdx : nonEmptyColumns) {
            for (int i = 0; i < tabularData.size(); i++) {
                List<String> oldRow = tabularData.get(i);
                List<String> newRow;
                if (tmpTable.size() <= i) {
                    newRow = new ArrayList<>();
                    tmpTable.add(newRow);
                }
                else {
                    newRow = tmpTable.get(i);
                }
                if (existingColIdx < oldRow.size()) {
                    newRow.add(oldRow.get(existingColIdx));
                }
            }
        }
        tabularData = tmpTable;
        columnHeaders = new ArrayList<>();
        columnHeaders.addAll(tabularData.get(headerRowNumber));
    }

    private File chooseXlsFile() {
        JFileChooser fileChooser = new JFileChooser();
        FileFilter filter = new FileNameExtensionFilter("Excel Files", "xls", "xlsx");
        fileChooser.setFileFilter(filter);
        int ret = fileChooser.showDialog(null, "Open file");

        if (ret == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            parsedFilePath = selectedFile.getAbsolutePath();
            return selectedFile;
        } else {
            return null;
        }
    }

    public List<String> getColumnHeaders() {
        return columnHeaders;
    }

    public String[][] getDataAs2dArray() {
        List<String> headers = getColumnHeaders();
        String[][] result = new String[tabularData.size() - headerRowNumber][headers.size()];
        
        for (int i = headerRowNumber + 1; i < tabularData.size(); i++) {
            List<String> row = tabularData.get(i);
            
            for (int j = 0; j < row.size(); j++) {
                if (j >= headers.size()) {
                    break;
                }
                String value = row.get(j);
                result[i - headerRowNumber][j] = value == null ? "" : value;
            }
        }
        if (isSingleColumnSheet) {
            String[][] tableValues = result;
            int idxAmounttCol = getTransColIndex(headers);
            int idxCrDrCol = getCrDrColumnIndex(headers);
            headers.set(idxAmounttCol, "Deposit");
            headers.set(idxCrDrCol, "Withdrawal");
            for (int i = 1; i < tableValues.length; i++) {
                if (tableValues[i][idxCrDrCol].trim().equalsIgnoreCase(CR)) {
                    tableValues[i][idxCrDrCol] = "0";
                }
                else if (tableValues[i][idxCrDrCol].trim().equalsIgnoreCase(DR)) {
                    tableValues[i][idxCrDrCol] = tableValues[i][idxAmounttCol];
                    tableValues[i][idxAmounttCol] = "0";
                }
                else {
                    throw new IllegalArgumentException("Unknown value found in CR/DR column");
                }
            }
        }
        return result;
    }

    public TableView<String[]> getDataAsTableView() throws NullPointerException {
        String[][] tableValues = getDataAs2dArray();
        List<String> headers = getColumnHeaders();
        
        if (headers == null) {
            throw new IllegalStateException("Column header info not set");
        }
        return arrayToTableView(tableValues, headers, null);
    }

    public String getParsedFilePath() {
        return parsedFilePath;
    }

    @SuppressWarnings("unchecked")
    public TableView<String[]> arrayToTableView(String[][] values, List<String> columnNames, Integer colWidth) {
        ObservableList<String[]> data = FXCollections.observableArrayList();
        data.addAll(Arrays.asList(values));
        data.remove(0);
        TableView<String[]> table = new TableView<>();
        for (int i = 0; i < values[0].length; i++) {
            @SuppressWarnings("rawtypes")
            TableColumn tc = new TableColumn(columnNames.get(i));
            final int colNo = i;
            tc.setCellValueFactory(new Callback<CellDataFeatures<String[], String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(CellDataFeatures<String[], String> p) {
                    return new SimpleStringProperty((p.getValue()[colNo]));
                }
            });
            if (colWidth != null) {
                tc.setPrefWidth(colWidth);
            }
            table.getColumns().add(tc);
        }
        table.setItems(data);
        return table;
    }

    private int getCrDrColumnIndex(List<String> columnHeaders) {
        int idx = -1;
        for (int i = 0; i < columnHeaders.size(); i++) {
            if (columnHeaders.get(i).equals(crDrColName)) {
                idx = i;
                break;
            }
        }
        return idx;
    }

    private int getTransColIndex(List<String> columnHeaders) {
        int idx = -1;
        for (int i = 0; i < columnHeaders.size(); i++) {
            if (columnHeaders.get(i).equals(transAmtColName)) {
                idx = i;
                break;
            }
        }
        return idx;
    }

    public void setDR(String dR) {
        DR = dR;
    }

    public void setCR(String cR) {
        CR = cR;
    }

    public Integer getHeaderRowNumber() {
        return headerRowNumber;
    }

    public void setHeaderRowNumber(int headerRowNumber) {
        this.headerRowNumber = headerRowNumber;
    }

    public static void main(String args[]) {
        XlsTableReader xtr = new XlsTableReader(false);
        // xtr.processXlsFile(new
        // File("C:/Users/Shoeb/Documents/0618XXXXXXXXX123416-10-2017.xls"));;
        // xtr.processXlsFile(new File("C:/Users/Shoeb/Documents/demo.xlsx"));;
        xtr.processXlsFile(new File("C:/Users/Shoeb/Documents/demo-2-col.xlsx"));
        ;

        // TableView<String[]> tableView = xtr.getDataAsTableView();
        System.out.println(xtr.getColumnHeaders());

        String[][] data = xtr.getDataAs2dArray();
        for (int i = 1; i < data.length; i++) {
            // System.out.println(data[i].length);
            System.out.println(Arrays.toString(data[i]));
        }
    }

}
