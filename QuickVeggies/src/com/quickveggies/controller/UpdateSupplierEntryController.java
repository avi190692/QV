package com.quickveggies.controller;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

import com.quickveggies.GeneralMethods;
import com.quickveggies.dao.DatabaseClient;
import com.quickveggies.entities.DBuyerTableLine;
import com.quickveggies.entities.DSalesTableLine;
import com.quickveggies.entities.DSupplierTableLine;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

public class UpdateSupplierEntryController implements Initializable {

    private static final String DATE = "Date";
    private static final String PROPRIETOR = "Proprietor";
    private static final String TITLE = "Title";
    private static final String CASES = "Cases";
    private static final String RATE = "Rate";
    private static final String NET = "Net";

    @FXML
    private Button commitButton;
    @FXML
    private Button cancelButton;
    @FXML
    private TableView<DSupplierTableLine> updateEntryTable;

    private final int lineId;
    private String tableName = null;
    private String[] colNamesList = null;
    private String[] oldValuesList = null;
    private String[] cellValuesFactoryList = null;
    private String[] sqlNames = null;

    private DSupplierTableLine line;
    private ObservableList<DSupplierTableLine> newLineWrapper = null;
    private String tableLineType = null;

    public UpdateSupplierEntryController(String tableLineType, String lineId, String[] valuesList) {
        this.lineId = Integer.parseInt(lineId);
        this.oldValuesList = valuesList;
        this.colNamesList = new String[] { DATE, TITLE, PROPRIETOR, CASES, RATE, NET, "Agent" };
        cellValuesFactoryList = new String[]{"date", "supplierTitle", "proprietor", "cases", "supplierRate", "net",
            "agent"};
        this.tableLineType = tableLineType;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tableName = "supplierDeals";
        line = new DSupplierTableLine(oldValuesList);
        sqlNames = SessionDataController.dSupplierTableSqlColNames;
        newLineWrapper = FXCollections.observableArrayList(line);

        updateEntryTable.setFixedCellSize(50.0);
        updateEntryTable.setEditable(true);
        System.out.println(colNamesList);
        for (int i = 0; i < colNamesList.length; i++) {

            final String cellValueFactoryName = cellValuesFactoryList[i];
            final TableColumn col1 = new TableColumn(colNamesList[i]);
            col1.setCellValueFactory(new PropertyValueFactory<>(cellValuesFactoryList[i]));
            col1.setCellFactory(TextFieldTableCell.forTableColumn());
            //disable the total amount columns, they are to be calculated from rate X boxes
            String colName = col1.getText();
            if (!colName.equals(CASES) && !colName.equals(RATE)) {
                col1.setEditable(false);
            }
            else {
                col1.setEditable(true);
            }
            col1.setOnEditCommit(new EventHandler<CellEditEvent<DBuyerTableLine, String>>() {
                @Override
                public void handle(CellEditEvent event) {
                    ((DSupplierTableLine) event.getTableView().getItems().get(event.getTablePosition().getRow()))
                            .set(cellValueFactoryName, event.getNewValue().toString());
                    // if the edited cell is rate/cases, recalculate the net sum
                    if (col1.getText().equals(RATE) || col1.getText().equals(CASES)) {
                        DSupplierTableLine line = ((DSupplierTableLine) (event.getTableView().getItems().get(0)));
                        line.setNet(Integer.parseInt(line.getSupplierRate()) * Integer.parseInt(line.getCases())
                                - getSupplierCharges(line) + "");
                        GeneralMethods.refreshTableView(updateEntryTable, FXCollections.observableArrayList(line));
                    }

                }
            });
            updateEntryTable.getColumns().add(col1);
        }

        updateEntryTable.setItems(newLineWrapper);

        commitButton.setOnAction((ActionEvent event) -> {
            // UPDATE THE CHANGES IN SQL
            DatabaseClient dbclient = DatabaseClient.getInstance();
            
            dbclient.updateTableEntry(tableName, UpdateSupplierEntryController.this.lineId, sqlNames,
                    getValuesFromTableLine(updateEntryTable, lineId, tableLineType), true);
            commitButton.getScene().getWindow().hide();
        });
        cancelButton.setOnAction((ActionEvent event) -> {
            cancelButton.getScene().getWindow().hide();
        });
    }

    private String[] getValuesFromTableLine(TableView<?> table, int lineId, String tableLineType) {
        Object line = table.getItems().get(0);
        String[] result = (((DSupplierTableLine) line).getAll());
        return result;
    }

    private double getSupplierCharges(DSupplierTableLine supplierLine) {
        double result = 0;
        DSalesTableLine salesline = getSaleTableLineById(supplierLine.getDealID());
        String charges = salesline.getCharges();
        result += Double.parseDouble(charges);
        return result;
    }

    private DSalesTableLine getSaleTableLineById(String dealId) {
        DatabaseClient dbclient = DatabaseClient.getInstance();
        DSalesTableLine salesline = null;
        try {
            salesline = dbclient.getSalesEntryLineByDealId(Integer.parseInt(dealId));
        } catch (SQLException e) {
            System.out.print("sqlexception in getSupplierCharges");
        }
        return salesline;

    }

}
