package com.quickveggies.controller;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import com.ai.util.dates.DateUtil;
import com.quickveggies.GeneralMethods;
import com.quickveggies.dao.DatabaseClient;
import com.quickveggies.entities.DBuyerTableLine;
import com.quickveggies.entities.ExpenseInfo;
import com.quickveggies.entities.LadaanBijakSaleDeal;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.converter.NumberStringConverter;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

public class UpdateLadaanEntryController implements Initializable {

    @FXML
    private TextField txtFreight;
    @FXML
    private TextField txtComission;
    @FXML
    private Button commitButton;
    @FXML
    private Button cancelButton;
    @FXML
    private TableView<DBuyerTableLine> updateEntryTable;
    @FXML
    private TableView<LadaanBijakSaleDeal> ladaanBijakEntryTable;
    @FXML
    private Label lblFreightTot;
    @FXML
    private Label lblCommissionTot;
    @FXML
    private Label lblResult;

    private int buyDealLineId;
    private int ladBijDealLineId;
    private String tableName = null;
    private String[] colNamesList = null;
    private String[] oldValuesList = null;
    private String[] cellValuesFactoryList = null;
    private String[] sqlNames = null;

    private DBuyerTableLine buyDealLine;
    private ObservableList<?> colsList = null;
    private ObservableList buyerDealLineWrapper = null;
    private ObservableList<LadaanBijakSaleDeal> newLadaanDealLineWrapper = null;
    private String tableLineType = null;
    final List<ExpenseInfo> expenseList = DatabaseClient.getInstance().getBuyerExpenseInfoList();
    private DatabaseClient dbc = DatabaseClient.getInstance();
    private LadaanBijakSaleDeal ladBijDeal;
    private String strResult = "";

    public UpdateLadaanEntryController(String tableLineType, String lineId, String[] valuesList) {
        this.buyDealLineId = Integer.parseInt(lineId);
        this.oldValuesList = valuesList;
        this.tableLineType = tableLineType;
        colNamesList = new String[]{"Date", "Invoice No", "Buyer Rate", "Cases", "Total Sum"};
        cellValuesFactoryList = new String[]{"date", "dealID", "buyerRate", "cases", "aggregatedAmount"};
        tableName = "buyerDeals";
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        strResult = lblResult.getText() == null ? "" : lblResult.getText().trim();
        buyDealLine = new DBuyerTableLine(oldValuesList);
        sqlNames = SessionDataController.dBuyerTableSqlColNames;
        ladBijDeal = dbc.getLadBijSaleDeal(Integer.valueOf(buyDealLine.getDealID()));
        if (ladBijDeal == null) {
            ladBijDeal = new LadaanBijakSaleDeal();
            ladBijDeal.setSaleNo(-1); // -1 Indicates no record in DB, after
            // insert, this will be actual,
            ladBijDeal.setBuyerRate(buyDealLine.getBuyerRate());
            ladBijDeal.setAggregatedAmount(buyDealLine.getAggregatedAmount());
            ladBijDeal.setBuyerType(buyDealLine.getBuyerType());
            ladBijDeal.setBuyerType(buyDealLine.getBuyerTitle());
            ladBijDeal.setCases(String.valueOf(buyDealLine.getCases()));
            String format = DateUtil.determineDateFormat(buyDealLine.getDate());
            String dateStr = new SimpleDateFormat(format).format(new Date(System.currentTimeMillis()));
            ladBijDeal.setDate(dateStr);
            ladBijDeal.setDealId(buyDealLine.getDealID());
        }
        //System.out.println(ladBijDeal.getDate());
        this.ladBijDealLineId = Integer.valueOf(ladBijDeal.getSaleNo());
        txtFreight.setText(ladBijDeal.getFreight());
        txtComission.setText(ladBijDeal.getComission());
        buyerDealLineWrapper = FXCollections.observableArrayList(buyDealLine);
        newLadaanDealLineWrapper = FXCollections.observableArrayList(ladBijDeal);
        txtFreight.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    validateAndSetFreight();;
                }
            }
        });
        txtFreight.setOnAction((ActionEvent event) -> {
            validateAndSetFreight();;
        });
        txtComission.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
                validateAndSetCommission();
            }
        });

        txtComission.setOnAction((ActionEvent event) -> {
            validateAndSetCommission();
        });
        updateEntryTable.setFixedCellSize(50.0);
        //updateEntryTable.setEditable(true);

        for (int i = 0; i < colNamesList.length; i++) {
            final TableColumn col1 = new TableColumn(colNamesList[i]);
            col1.setCellValueFactory(new PropertyValueFactory<>(cellValuesFactoryList[i]));
            if ("cases".equalsIgnoreCase(colNamesList[i])) {
                col1.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter()));
            } else {
                col1.setCellFactory(TextFieldTableCell.forTableColumn());
            }
            col1.setEditable(false);
            updateEntryTable.getColumns().add(col1);
        }
        updateEntryTable.setItems(buyerDealLineWrapper);

        setupLadBijRateEntryTable();
        /* following if block should be executed only after the table setup is complete*/
        if (this.ladBijDealLineId != -1) {
            recalculateTotal();
        }

        commitButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // UPDATE THE CHANGES IN SQL
                DatabaseClient dbclient = DatabaseClient.getInstance();
                String[] ladaanBijSqlNames = new String[]{"dealDate", "dealID", "buyerRate", "boxes",
                    "aggregatedAmount", "freight", "comission"};
                String[] ladBijColValues = getLadaanDealValsTableLine(ladaanBijakEntryTable, buyDealLineId,
                        tableLineType);

                dbclient.updateTableEntry(tableName, UpdateLadaanEntryController.this.buyDealLineId, sqlNames,
                        getBuyerDealValsTableLine(updateEntryTable, buyDealLineId, tableLineType), true);

                if (UpdateLadaanEntryController.this.ladBijDealLineId != -1) {
                    dbclient.updateTableEntry("ladaanBijakSaleDeals", UpdateLadaanEntryController.this.ladBijDealLineId,
                            ladaanBijSqlNames, ladBijColValues, true);
                } else {
                    ladBijColValues = Arrays.copyOfRange(ladBijColValues, 1, ladBijColValues.length);
                    //System.out.println(Arrays.toString(ladBijColValues));
                    dbclient.saveEntryToSql("ladaanBijakSaleDeals", ladaanBijSqlNames, ladBijColValues);
                }

                commitButton.getScene().getWindow().hide();
            }
        });

        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                cancelButton.getScene().getWindow().hide();
            }
        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void setupLadBijRateEntryTable() {
        // ladaan bijak table
        TableColumn tcDate = new TableColumn<>("Date");
        tcDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        tcDate.setCellFactory(TextFieldTableCell.forTableColumn());
        //
        TableColumn tcInvoice = new TableColumn<>("Invoice No");
        tcInvoice.setCellValueFactory(new PropertyValueFactory<>("dealId"));

        TableColumn tcRate = new TableColumn<>("Rate");
        tcRate.setCellValueFactory(new PropertyValueFactory<>("buyerRate"));
        tcRate.setCellFactory(TextFieldTableCell.forTableColumn());

        TableColumn tcAmount = new TableColumn<>("Total");
        tcAmount.setCellValueFactory(new PropertyValueFactory<>("aggregatedAmount"));
        tcAmount.setCellFactory(TextFieldTableCell.forTableColumn());

        TableColumn tcCases = new TableColumn<>("Cases");
        tcCases.setCellValueFactory(new PropertyValueFactory<>("cases"));

        tcRate.setOnEditCommit(new EventHandler<CellEditEvent<LadaanBijakSaleDeal, String>>() {
            @Override
            public void handle(CellEditEvent event) {
                LadaanBijakSaleDeal line = (LadaanBijakSaleDeal) event.getTableView().getItems().get(event.getTablePosition().getRow());
                line.setBuyerRate(event.getNewValue().toString());
                resetAmount(line);
            }
        });
        tcCases.setOnEditCommit(new EventHandler<CellEditEvent<LadaanBijakSaleDeal, String>>() {
            @Override
            public void handle(CellEditEvent event) {
                LadaanBijakSaleDeal line = (LadaanBijakSaleDeal) event.getTableView().getItems().get(event.getTablePosition().getRow());
                line.setCases(event.getNewValue().toString());
                resetAmount(line);
            }
        });

        ladaanBijakEntryTable.getColumns().addAll(new TableColumn[]{tcDate, tcInvoice, tcRate, tcCases, tcAmount});
        ladaanBijakEntryTable.setItems(newLadaanDealLineWrapper);
        ladaanBijakEntryTable.setEditable(true);
        ladaanBijakEntryTable.setFixedCellSize(50.0);
    }

    private String[] getBuyerDealValsTableLine(TableView<DBuyerTableLine> table, int lineId, String tableLineType) {
        String[] result = null;
        DBuyerTableLine line = table.getItems().get(0);
        result = new String[]{line.getSaleNo(), line.getDate(), line.getDealID(), line.getBuyerRate(),
            String.valueOf(line.getCases()), line.getAggregatedAmount()};
        return result;
    }

    private String[] getLadaanDealValsTableLine(TableView<LadaanBijakSaleDeal> table, int lineId,
            String tableLineType) {
        String[] result = null;
        LadaanBijakSaleDeal line = table.getItems().get(0);
        if (line.getFreight().equals("0") || !txtFreight.getText().trim().equals(line.getComission())) {
            String txt = txtFreight.getText();
            Integer iTxt = Double.valueOf(txt).intValue();
            line.setFreight(iTxt.toString());
        }
        if (line.getComission().equals("0") || !txtComission.getText().trim().equals(line.getComission())) {
            String txt = txtComission.getText();
            Integer iTxt = Double.valueOf(txt).intValue();
            line.setComission(iTxt.toString());
        }
        result = new String[]{line.getSaleNo(), line.getDate(), line.getDealId(), line.getBuyerRate(),
            line.getCases(), line.getAggregatedAmount(), line.getFreight(), line.getComission()};
        return result;
    }

    private void recalculateTotal() {
        LadaanBijakSaleDeal deal = this.ladaanBijakEntryTable.getItems().get(0);
        if (deal == null || deal.getCases().trim().isEmpty()) {
            return;
        }
        Double freight = Double.valueOf(txtFreight.getText().trim());
        Integer commission = Integer.valueOf(txtComission.getText().trim());
        Integer frTot = (int) (freight * (Integer.valueOf(deal.getCases().trim())));
        Integer comTot = commission * (Integer.valueOf(deal.getAggregatedAmount().trim())) / 100;
        lblFreightTot.setText(frTot.toString());
        lblCommissionTot.setText(comTot.toString());
        int result = Integer.valueOf(deal.getAggregatedAmount()) - (frTot + comTot);
        result = result - Integer.valueOf(buyDealLine.getAggregatedAmount());
        boolean isProfit = false;
        if (result > 0) {
            isProfit = true;
        }
        String proftLostStr = isProfit ? " (Profit) " : " (Loss) ";
        lblResult.setText(strResult.concat(" : ").concat(proftLostStr).concat(String.valueOf(result)));
    }

    private void resetAmount(LadaanBijakSaleDeal line) {
        String totAmount = FreshEntryController.getBuyerAggregateAmount(line.getBuyerRate(), line.getCases());
        line.setAggregatedAmount(totAmount);
        ladaanBijakEntryTable.refresh();
        GeneralMethods.refreshTableView(ladaanBijakEntryTable, FXCollections.observableArrayList(line));
        recalculateTotal();
    }

    private void validateAndSetCommission() {
        String strCommission = txtComission.getText().trim();
        try {
            Double.parseDouble(strCommission);
        } catch (NumberFormatException nfe) {
            GeneralMethods.errorMsg("Please enter proper number in Frieght field");
            txtComission.setText("0");
            txtComission.requestFocus();
            return;
        }
        ladBijDeal.setComission(Integer.valueOf(strCommission).toString());
        recalculateTotal();
    }

    private void validateAndSetFreight() {
        String strFreight = txtFreight.getText().trim();
        try {
            Double.parseDouble(strFreight);
        } catch (NumberFormatException nfe) {
            GeneralMethods.errorMsg("Please enter proper number in Frieght field");
            txtFreight.setText("0");
            txtFreight.requestFocus();
            return;
        }
        ladBijDeal.setFreight(Integer.valueOf(strFreight).toString());
        recalculateTotal();
    }

}
