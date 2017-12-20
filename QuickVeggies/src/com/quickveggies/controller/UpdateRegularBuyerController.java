package com.quickveggies.controller;

import java.net.URL;
import java.util.ResourceBundle;

import com.quickveggies.GeneralMethods;
import com.quickveggies.dao.DatabaseClient;
import com.quickveggies.entities.DBuyerTableLine;

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
import javafx.util.converter.NumberStringConverter;

public class UpdateRegularBuyerController implements Initializable {

    private static final String INVOICE_NO = "Invoice No";
    private static final String CASES = "Cases";
    private static final String RATE = "Buyer Rate";
    private static final String TOTAL_SUM = "Total Sum";
    @FXML
    private Button commitButton;
    @FXML
    private Button cancelButton;
    @FXML
    private TableView<DBuyerTableLine> updateEntryTable;

    private int lineId;
    private String tableName = null;
    private String[] colNamesList = null;
    private String[] oldValuesList = null;
    private String[] cellValuesFactoryList = null;
    private String[] sqlNames = null;

    private DBuyerTableLine line;
    private ObservableList<DBuyerTableLine> newLineWrapper = null;
    private String tableLineType = null;

    public UpdateRegularBuyerController(String tableLineType, String lineId, String[] valuesList) {
        this.lineId = Integer.parseInt(lineId);
        this.oldValuesList = valuesList;
        this.tableLineType = tableLineType;
        colNamesList = new String[]{"Date", INVOICE_NO, "Buyer Rate", CASES, TOTAL_SUM};
        cellValuesFactoryList = new String[]{"date", "dealID", "buyerRate", "cases", "aggregatedAmount"};
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tableName = "buyerDeals";
        line = new DBuyerTableLine(oldValuesList);
        sqlNames = SessionDataController.dBuyerTableSqlColNames;

        newLineWrapper = FXCollections.observableArrayList(line);

        updateEntryTable.setFixedCellSize(50.0);
        updateEntryTable.setEditable(true);

        for (int i = 0; i < colNamesList.length; i++) {
            final String cellValueFactoryName = cellValuesFactoryList[i];
            final TableColumn col1 = new TableColumn(colNamesList[i]);
            col1.setCellValueFactory(new PropertyValueFactory<>(cellValuesFactoryList[i]));
            
            if (i == 3) {
                col1.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter()));
            }
            else {
                col1.setCellFactory(TextFieldTableCell.forTableColumn());
            }
            // disable the total amount columns, they are to be calculated from
            // rate X boxes
            String colName = col1.getText();
            if (colName.equals(TOTAL_SUM) || colName.equals(INVOICE_NO)) {
                col1.setEditable(false);
            }
            col1.setOnEditCommit(new EventHandler<CellEditEvent<DBuyerTableLine, String>>() {
                @Override
                public void handle(CellEditEvent event) {
                    ((DBuyerTableLine) event.getTableView().getItems().get(event.getTablePosition().getRow()))
                            .set(cellValueFactoryName, event.getNewValue().toString());
                    // if the edited cell is rate/cases, recalculate the net sum
                    if (col1.getText().equals(RATE) || col1.getText().equals(CASES)) {
                        DBuyerTableLine line = ((DBuyerTableLine) (event.getTableView().getItems().get(0)));
                        String totAmount = FreshEntryController.getBuyerAggregateAmount(
                                line.getBuyerRate(), String.valueOf(line.getCases()));
                        line.setAggregatedAmount(totAmount);
                        updateEntryTable.refresh();
                    }

                }
            });
            updateEntryTable.getColumns().add(col1);
        }
        updateEntryTable.setItems(newLineWrapper);

        commitButton.setOnAction((ActionEvent event) -> {
            //Update the changes in the database
            DatabaseClient dbclient = DatabaseClient.getInstance();
            
            dbclient.updateTableEntry(tableName, UpdateRegularBuyerController.this.lineId, sqlNames,
                    getValuesFromTableLine(updateEntryTable, lineId, tableLineType), true);
            commitButton.getScene().getWindow().hide();
        });
        cancelButton.setOnAction((ActionEvent event) -> {
            cancelButton.getScene().getWindow().hide();
        });
    }

    private String[] getValuesFromTableLine(TableView<DBuyerTableLine> table, int lineId, String tableLineType) {
        String[] result = null;
        DBuyerTableLine line = table.getItems().get(0);
        result = new String[]{line.getSaleNo(), line.getDate(), line.getDealID(),
            line.getBuyerRate(), String.valueOf(line.getCases()), String.valueOf(line.getAggregatedAmount())};
        return result;
    }
}
