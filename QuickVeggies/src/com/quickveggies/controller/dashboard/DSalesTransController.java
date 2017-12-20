package com.quickveggies.controller.dashboard;

import com.ai_int.utils.ExcelExportUtil;
import com.ai_int.utils.FileUtil;
import com.ai_int.utils.javafx.ListViewUtil;
import com.ai_int.utils.javafx.TableUtil;
import com.quickveggies.dao.DatabaseClient;
import com.quickveggies.entities.Charge;
import com.quickveggies.entities.DSalesTableLine;
import com.quickveggies.entities.DSupplierTableLine;
import com.quickveggies.entities.DSalesTableList;
import com.quickveggies.misc.DeleteTableButtonCell;
import com.quickveggies.misc.EditTableButtonCell;
import com.quickveggies.misc.PrintTableButtonCell;
import com.quickveggies.misc.Utils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;

public class DSalesTransController implements Initializable {

    @FXML
    private Label Title;

    @FXML
    private Label rsInvoice;

    @FXML
    private Label openInvoice;

    @FXML
    private Label rsOverdue;

    @FXML
    private Label overdue;

    @FXML
    private Label rsPaid;

    @FXML
    private ComboBox<?> batchActions;

    @FXML
    private TableView<DSalesTableLine> salesDashTable;
    
    @FXML
    private TableView<DSalesTableLine> tableTotal;

    @FXML
    private Button newTrans;

    @FXML
    private Label rsEstimate1;

    @FXML
    private Label openInvoice1;

    @FXML
    private Label rsEstimate2;

    @FXML
    private Label overdue1;

    @FXML
    private Button btnColSettings;

    @FXML
    private Button btnPrint;

    @FXML
    private Button btnExport;

    private DatabaseClient dbclient = DatabaseClient.getInstance();

    private ObservableList<DSalesTableLine> lines = FXCollections.observableArrayList(
            new DSalesTableLine("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""));

    private int displayDealsType;

    public DSalesTransController(int displayDealsType) {
        this.displayDealsType = displayDealsType;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void initialize(URL location, ResourceBundle resources) {
        final Pane pane = (Pane) btnColSettings.getParent().getParent();
        ListViewUtil.addColumnSettingsButtonHandler(salesDashTable, pane, btnColSettings);
        // update lines from sql database:
        try {
            lines.clear();
            for (DSalesTableLine line : dbclient.getSalesEntries()) {
                lines.add(line);
            }
        }
        catch (SQLException e) {
            System.out.print("sqlexception while fetching sales entries from sql");
        }
        salesDashTable.setEditable(false);
        TableColumn saleNoCol = new TableColumn("No.");
        saleNoCol.setCellValueFactory(new PropertyValueFactory<>("saleNo"));
        saleNoCol.setCellFactory(TextFieldTableCell.forTableColumn());
        TableColumn dealIdCol = new TableColumn("Deal ID");
        dealIdCol.setCellValueFactory(new PropertyValueFactory<>("dealID"));
        dealIdCol.setCellFactory(TextFieldTableCell.forTableColumn());
        TableColumn dateCol = new TableColumn("Sale Date");
        TableColumn typeCol = new TableColumn("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setCellFactory(TextFieldTableCell.forTableColumn());
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setCellFactory(TextFieldTableCell.forTableColumn());
        TableColumn fruitCol = new TableColumn("Fruit");
        fruitCol.setCellValueFactory(new PropertyValueFactory<>("fruit"));
        fruitCol.setCellFactory(TextFieldTableCell.forTableColumn());
        TableColumn challanCol = new TableColumn("Challan");
        challanCol.setCellValueFactory(new PropertyValueFactory<>("challan"));
        challanCol.setCellFactory(TextFieldTableCell.forTableColumn());
        TableColumn suppCol = new TableColumn("Supplier");
        suppCol.setCellValueFactory(new PropertyValueFactory<>("supplier"));
        suppCol.setCellFactory(TextFieldTableCell.forTableColumn());
        TableColumn boxesCol = new TableColumn("Tot. boxes");
        boxesCol.setCellValueFactory(new PropertyValueFactory<>("totalQuantity"));
        boxesCol.setCellFactory(TextFieldTableCell.forTableColumn());
        TableColumn fullCaseCol = new TableColumn("Full case");
        fullCaseCol.setCellValueFactory(new PropertyValueFactory<>("fullCase"));
        fullCaseCol.setCellFactory(TextFieldTableCell.forTableColumn());
        TableColumn halfCaseCol = new TableColumn("Half case");
        halfCaseCol.setCellValueFactory(new PropertyValueFactory<>("halfCase"));
        halfCaseCol.setCellFactory(TextFieldTableCell.forTableColumn());
        TableColumn agentCol = new TableColumn("FW Agent");
        agentCol.setCellValueFactory(new PropertyValueFactory<>("agent"));
        agentCol.setCellFactory(TextFieldTableCell.forTableColumn());
        TableColumn truckNoCol = new TableColumn("Truck No.");
        truckNoCol.setCellValueFactory(new PropertyValueFactory<>("truck"));
        truckNoCol.setCellFactory(TextFieldTableCell.forTableColumn());
        TableColumn driverNoCol = new TableColumn("Driver No.");
        driverNoCol.setCellValueFactory(new PropertyValueFactory<>("driver"));
        driverNoCol.setCellFactory(TextFieldTableCell.forTableColumn());
        TableColumn grossCol = new TableColumn("Gross Amount");
        grossCol.setCellValueFactory(new PropertyValueFactory<>("gross"));
        grossCol.setCellFactory(TextFieldTableCell.forTableColumn());
        TableColumn netCol = new TableColumn("Net Amount");
        netCol.setCellValueFactory(new PropertyValueFactory<>("net"));
        netCol.setCellFactory(TextFieldTableCell.forTableColumn());
        TableColumn chargesCol = new TableColumn("Charges");
        chargesCol.setCellValueFactory(new PropertyValueFactory<>("noAmanatCharges"));
        chargesCol.setCellFactory(TextFieldTableCell.forTableColumn());
        TableColumn amanatCol = new TableColumn("Amanat");
        amanatCol.setCellValueFactory(new PropertyValueFactory<>("amanat"));
        amanatCol.setCellFactory(TextFieldTableCell.forTableColumn());
        TableColumn remarksCol = new TableColumn("Remarks");
        remarksCol.setCellValueFactory(new PropertyValueFactory<>("remarks"));
        remarksCol.setCellFactory(TextFieldTableCell.forTableColumn());

        TableColumn editCol = new TableColumn();
        editCol.setCellValueFactory(new PropertyValueFactory<>("saleNo"));
        editCol.setCellFactory(
                new javafx.util.Callback<TableColumn<DSalesTableLine, String>, TableCell<DSalesTableLine, String>>() {
            @Override
            public TableCell<DSalesTableLine, String> call(TableColumn<DSalesTableLine, String> param) {
                return new EditTableButtonCell("DSalesTransController");
            }
        });
        TableColumn printCol = new TableColumn();
        printCol.setCellValueFactory(new PropertyValueFactory<>("saleNo"));
        printCol.setCellFactory(
                new javafx.util.Callback<TableColumn<DSalesTableLine, String>, TableCell<DSalesTableLine, String>>() {
            @Override
            public TableCell<DSalesTableLine, String> call(TableColumn<DSalesTableLine, String> param) {
                return new PrintTableButtonCell();
            }
        });
        TableColumn deleteCol = new TableColumn();
        deleteCol.setCellValueFactory(new PropertyValueFactory<>("saleNo"));
        deleteCol.setCellFactory(
                new javafx.util.Callback<TableColumn<DSalesTableLine, String>, TableCell<DSalesTableLine, String>>() {
            @Override
            public TableCell<DSalesTableLine, String> call(TableColumn<DSalesTableLine, String> param) {
                return new DeleteTableButtonCell("arrival", "saleNo");
            }
        });
        TableColumn actionsCol = new TableColumn("Actions");
        actionsCol.getColumns().setAll(editCol, printCol, deleteCol);

        salesDashTable.getItems().addAll(lines);
        salesDashTable.getColumns().setAll(saleNoCol, dealIdCol, typeCol, dateCol, fruitCol, challanCol, suppCol,
                boxesCol, fullCaseCol, halfCaseCol, agentCol, truckNoCol, driverNoCol, grossCol, chargesCol, amanatCol,
                netCol, remarksCol, actionsCol);

        btnExport.setOnAction((event) -> {
            TableColumn<?, ?> tcAction = actionsCol;
            boolean prevVisibility = tcAction.isVisible();
            tcAction.setVisible(false);
            String[][] tableData = TableUtil.toArray(salesDashTable);
            String fileName = FileUtil.getSaveToFileName(btnExport.getScene(), "Select Excel file", FileUtil.getExcelExtMap());
            if (fileName != null) {
                try {
                    ExcelExportUtil.exportTableData(tableData, "Arrival Transaction List", fileName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            tcAction.setVisible(prevVisibility);
        });
        btnPrint.setOnAction((event) -> TableUtil.printTable(salesDashTable, "Arrival Transaction List", actionsCol));
        setupTotalAmountsTable(lines);
    }
    
    private void setupTotalAmountsTable(final ObservableList<DSalesTableLine> list) {
        //Setup total amounts table
        tableTotal.getColumns().clear();
        for (TableColumn column : salesDashTable.getColumns()) {
            TableColumn newColumn = new TableColumn("");
            if (!column.getText().isEmpty()) {
                newColumn.setCellFactory(column.getCellFactory());
                newColumn.setCellValueFactory(column.getCellValueFactory());
            }
            newColumn.prefWidthProperty().bind(column.widthProperty());
            tableTotal.getColumns().add(newColumn);
        }
        tableTotal.setEditable(false);
        tableTotal.getItems().addAll(new DSalesTableList(list));
        //Hide Header
        tableTotal.widthProperty().addListener(
                (ObservableValue<? extends Number> source, Number oldWidth, Number newWidth) -> {
            Pane header = (Pane) tableTotal.lookup("TableHeaderRow");
            if (header.isVisible()){
                header.setMaxHeight(0);
                header.setMinHeight(0);
                header.setPrefHeight(0);
                header.setVisible(false);
            }
        });
        salesDashTable.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            //Run after initialization to get controls
            for (Node bar1 : salesDashTable.lookupAll(".scroll-bar")) {
                if (bar1 instanceof ScrollBar
                        && ((ScrollBar) bar1).getOrientation().equals(Orientation.HORIZONTAL)) {
                    ((ScrollBar) bar1).valueProperty().addListener((ObservableValue<? extends Number> observ, Number old, Number newVal) -> {
                        for (Node bar : tableTotal.lookupAll(".scroll-bar")) {
                            if (bar instanceof ScrollBar
                                    && ((ScrollBar) bar).getOrientation().equals(Orientation.HORIZONTAL)) {
                                ((ScrollBar) bar).setMax(((ScrollBar) bar1).getMax());
                                ((ScrollBar) bar).setMin(((ScrollBar) bar1).getMin());
                                ((ScrollBar) bar).setValue(((ScrollBar) bar1).getValue());
                            }
                        }
                    });
                }
            }
        });
    }
    
    public static String[][] buildInvTabForEmail(DSalesTableLine line) {
        String dealId = line.getDealID();
        List<DSalesTableLine> salesDeals;
        List<DSupplierTableLine> supplierLine;
        try {
            salesDeals = DatabaseClient.getInstance().getSalesEntryLineBySupplierName(line.getSupplier());
            supplierLine = DatabaseClient.getInstance().getSupplierDealEntries(line.getSupplier());
        } catch (SQLException ex) {
            Logger.getLogger(DSupplierController.class.getName()).log(Level.SEVERE, null, ex);
            return new String[0][0];
        }
        Iterator<DSalesTableLine> lines = salesDeals.iterator();
        while (lines.hasNext()) {
            DSalesTableLine deal = lines.next();
            if (!deal.getDealID().equals(line.getDealID())) {
                lines.remove();
            }
            else if (deal.getSaleNo().equals(line.getSaleNo())) {
                deal.deserialize(line.serialize());
            }
        }
        int recCount = salesDeals.size();
        List<String[]> table = new ArrayList<>();

        table.add(new String[]{"FRUIT", "QUALITY", "SIZE", "CASES", "RATE", "GROSS AMT", "EXP NAME", "AMOUNT"});
        Integer[][] caseRateArr = new Integer[recCount][2];

        AtomicInteger rowCount = new AtomicInteger(0);
        salesDeals.stream().forEach((t) -> {
            DSupplierTableLine foundLine = null;
            for (DSupplierTableLine sarchLine : supplierLine) {
                if (sarchLine.getDealID().equals(t.getDealID())) {
                    foundLine = sarchLine;
                }
            }
            int i = rowCount.incrementAndGet();
            Integer cases = Utils.toInt(t.getTotalQuantity());
            Integer rate = Utils.toInt(t.getGross()) / cases;
            String[] dealLine = new String[]{t.getFruit(), foundLine.getQualityType(),
                foundLine.getBoxSizeType(), cases.toString(), rate.toString(),
                Integer.toString(rate * cases), "", ""};
            caseRateArr[i - 1][0] = cases;
            caseRateArr[i - 1][1] = rate;
            table.add(dealLine);
        });
        Integer baseSum = 0;
        Integer caseCount = 0;
        for (int i = 0; i < caseRateArr.length; i++) {
            baseSum += caseRateArr[i][0] * caseRateArr[i][1];
            caseCount += caseRateArr[i][0];
        }
        List<Charge> suppCharges = DatabaseClient.getInstance().getDealCharges(Integer.valueOf(dealId));
        int tabDataRow = table.size() - 1;
        if (suppCharges.size() > tabDataRow) {
            int remainingLines = suppCharges.size() - tabDataRow;
            for (int i = 0; i < remainingLines; i++) {
                table.add(new String[]{"", "", "", "", "", "", "", ""});
            }
        }
        int count = 0;
        Integer chargesSum = 0;
        for (Charge chg : suppCharges) {
            count++;
            String[] lineItem = table.get(count);
            lineItem[6] = chg.getName();
            lineItem[7] = Utils.toInt(chg.getAmount()).toString();
            chargesSum += Utils.toInt(chg.getAmount());
        }
        table.add(new String[]{"Total Cases", "", "", caseCount.toString(), baseSum.toString(), "", "", chargesSum.toString()});

        String[][] invArr = new String[table.size()][8];
        for (int i = 0; i < table.size(); i++) {
            invArr[i] = table.get(i);
        }
        return invArr;
    }
}
