package com.quickveggies.controller.dashboard;

import static com.quickveggies.entities.Buyer.COLD_STORE_BUYER_TITLE;
import static com.quickveggies.entities.Buyer.GODOWN_BUYER_TITLE;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;

import com.ai_int.utils.ExcelExportUtil;
import com.ai_int.utils.FileUtil;
import com.ai_int.utils.javafx.ListViewUtil;
import com.ai_int.utils.javafx.TableUtil;
import com.quickveggies.GeneralMethods;
import com.quickveggies.controller.UpdatePendingSalesController;
import com.quickveggies.dao.DatabaseClient;
import com.quickveggies.entities.Buyer;
import com.quickveggies.entities.DBuyerTableLine;
import com.quickveggies.entities.DSalesTableLine;
import com.quickveggies.entities.DBuyerTableList;
import com.quickveggies.entities.DSupplierTableLine;
import com.quickveggies.misc.Utils;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.converter.NumberStringConverter;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

public class DPendingSalesController implements Initializable {

    @FXML
    private Label lblStoreType;

    @FXML
    private Label lblCaseCount;

    @FXML
    private TableView<DBuyerTableLine> tvSalesDash;
    
    @FXML
    private TableView<DBuyerTableLine> tableTotal;

    @FXML
    private Button btnColSettings;

    @FXML
    private Button btnPrint;

    @FXML
    private Button btnExport;

    private DatabaseClient dbClient = DatabaseClient.getInstance();

    private String displayDealsType;
    private ObservableList<DBuyerTableLine> buyerDeals = FXCollections.observableArrayList();
    private Map<String, DBuyerTableLine> buyerAndIdMap = new LinkedHashMap<>();
    private Map<String, DSalesTableLine> saleDealMap = new LinkedHashMap<>();
    private List<DBuyerTableLine> godownBuyerDeals = new ArrayList<>();
    private List<DBuyerTableLine> coldStoreBuyerDeals = new ArrayList<>();
    private Map<String, List<DSupplierTableLine>> supplierDealMap = new LinkedHashMap<>();

    public DPendingSalesController(String displayDealsType) {
        this.displayDealsType = displayDealsType;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // set title according to the type:
        if (displayDealsType.equalsIgnoreCase("GODOWN")) {
            lblStoreType.setText("Godown");
        } else if (displayDealsType.equalsIgnoreCase("COLDSTORE")) {
            lblStoreType.setText("Cold Store");
        }

        final Pane pane = (Pane) btnColSettings.getParent().getParent();
        ListViewUtil.addColumnSettingsButtonHandler(tvSalesDash, pane, btnColSettings);
        //Loading data from the database
        fillSupplierDeals();
        fillBuyerDeals();
        fillSaleDeals();
        btnExport.setOnAction((event) -> {
            TableColumn<?, ?> tcAction = tvSalesDash.getColumns().get(6);
            boolean prevVisibility = tcAction.isVisible();
            tcAction.setVisible(false);
            String[][] tableData = TableUtil.toArray(tvSalesDash);
            String fileName = FileUtil.getSaveToFileName(btnExport.getScene(), "Select Excel file", FileUtil.getExcelExtMap());
            if (fileName != null) {
                try {
                    ExcelExportUtil.exportTableData(tableData, lblStoreType.getText() + " List", fileName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            tcAction.setVisible(prevVisibility);
        });
        btnPrint.setOnAction((event) -> {
            TableColumn<?, ?> tcAction = tvSalesDash.getColumns().get(6);
            TableUtil.printTable(tvSalesDash, lblStoreType.getText() + " List", tcAction);
        });

        tvSalesDash.setEditable(false);
        double width = tvSalesDash.getPrefWidth();
        TableColumn saleNoCol = new TableColumn("Invoice No");
        saleNoCol.setCellValueFactory(new PropertyValueFactory<DSalesTableLine, String>("dealID"));
        saleNoCol.setCellFactory(TextFieldTableCell.forTableColumn());
//        saleNoCol.setPrefWidth((13 * width) / 100);
        //
        TableColumn dateCol = new TableColumn("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<DSalesTableLine, String>("date"));
        dateCol.setCellFactory(TextFieldTableCell.forTableColumn());
//        dateCol.setPrefWidth((15 * width) / 100);
        //
        TableColumn suppCol = new TableColumn<>("Supplier");
        suppCol.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<DBuyerTableLine, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<DBuyerTableLine, String> param) {
                String dealId = param.getValue().getDealID();
                DSalesTableLine line = saleDealMap.get(dealId);
                if (line == null) {
                    return new ReadOnlyStringWrapper("");
                }
                return new ReadOnlyStringWrapper(line.getSupplier());
            }
        });
        suppCol.setCellFactory(TextFieldTableCell.forTableColumn());
//        suppCol.setPrefWidth((23 * width) / 100);
        //
        TableColumn boxesCol = new TableColumn("Cases");
        boxesCol.setCellValueFactory(new PropertyValueFactory<>("cases"));
        boxesCol.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter()));
//        boxesCol.setPrefWidth((11 * width) / 100);
        //
        TableColumn<DBuyerTableLine, String> statusCol = new TableColumn("Status");
        statusCol.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<DBuyerTableLine, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<DBuyerTableLine, String> param) {
                DBuyerTableLine buyerDeal = param.getValue();
                if (buyerDeal.isTotalLine()) {
                    return new ReadOnlyStringWrapper("");
                }
                String val = "Cleared";
                if (buyerDeal.getCases() > 0) {
                    val = "Pending";
                }
                final String value = val;
                return new ReadOnlyStringWrapper(value);
            }
        });
        statusCol.setCellFactory(
                new Callback<TableColumn<DBuyerTableLine, String>, TableCell<DBuyerTableLine, String>>() {

            @Override
            public TableCell<DBuyerTableLine, String> call(TableColumn<DBuyerTableLine, String> param) {
                return new TableCell<DBuyerTableLine, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        if (item != null) {
                            Text txt = new Text(item);
                            if (item.equalsIgnoreCase("pending")) {
                                txt.setFill(Color.DARKRED);
                            } else {
                                txt.setFill(Color.DARKGREEN);
                            }
                            setGraphic(txt);
                        }
                    }
                };
            }
        });

        TableColumn grossCol = new TableColumn("Amounted");

        grossCol.setCellValueFactory(new PropertyValueFactory<>("aggregatedAmount"));
        grossCol.setCellFactory(TextFieldTableCell.forTableColumn());
        TableColumn<DBuyerTableLine, String> viewCol = new TableColumn<>("Action");
        viewCol.setCellFactory(
                new Callback<TableColumn<DBuyerTableLine, String>, TableCell<DBuyerTableLine, String>>() {

            @Override
            public TableCell<DBuyerTableLine, String> call(TableColumn<DBuyerTableLine, String> param) {
                TableCell<DBuyerTableLine, String> cell = new TableCell<DBuyerTableLine, String>() {
                    @Override
                    public void updateItem(final String item, boolean empty) {
                        if (item != null) {
                            final DBuyerTableLine buyerDeal = buyerAndIdMap.get(item);
                            if (buyerDeal == null) {
                                Label lbl = new Label();
                                setGraphic(lbl);
                                return;
                            }
                            final Hyperlink hLink = new Hyperlink();
                            hLink.setText("View");
                            if (buyerDeal.getCases() < 1) {
                                hLink.setDisable(true);
                            }
                            hLink.setOnAction((ActionEvent event) -> {
                                DSupplierTableLine linkedSupplierDeal = getLinkedSupplierDeal(buyerDeal);
                                if (linkedSupplierDeal == null) {
                                    return;
                                }
                                UpdatePendingSalesController controller = new UpdatePendingSalesController(
                                        buyerDeal, saleDealMap.get(buyerDeal.getDealID()),
                                        linkedSupplierDeal);
                                Stage stage = DashboardController.showPopup("/fxml/updatePendingSales.fxml",
                                        "Pending Entries", controller);
                                
                                EventHandler<WindowEvent> we = (WindowEvent event1) -> {
                                    fillBuyerDeals();
                                    fillSaleDeals();
                                    tvSalesDash.refresh();
                                    DBuyerTableLine currDeal = buyerAndIdMap.get(item);
                                    if (currDeal.getCases() < 1) {
                                        List<DBuyerTableLine> otherStoreDeal = null;
                                        if (currDeal.getBuyerTitle().equals(Buyer.GODOWN_BUYER_TITLE)
                                                && (displayDealsType.equalsIgnoreCase("GODOWN"))) {
                                            otherStoreDeal = coldStoreBuyerDeals;
                                        } else {
                                            otherStoreDeal = godownBuyerDeals;
                                        }
                                        int otherStoreCount = getStoreCasesCountFor(otherStoreDeal,
                                                currDeal.getDealID());
                                        if (otherStoreCount < 1) {
                                            DSalesTableLine saleLine = saleDealMap
                                                    .get(currDeal.getDealID());
                                            dbClient.updateTableEntry("arrival",
                                                    Utils.toInt(saleLine.getSaleNo()), "type",
                                                    "Regular", null);
                                        }
                                    }
                                };
                                stage.setOnCloseRequest(we);
                                stage.setOnHidden(we);
                            });
                            setGraphic(hLink);
                        }
                    }
                };
                return cell;
            }
        });
        viewCol.setCellValueFactory(new PropertyValueFactory<DBuyerTableLine, String>("saleNo"));
//        viewCol.setPrefWidth((10 * width) / 100);
        tvSalesDash.setItems(buyerDeals);
        tvSalesDash.getColumns().addAll(saleNoCol, dateCol, suppCol, boxesCol, statusCol, grossCol, viewCol);
        setupTotalAmountsTable(buyerDeals);
    }
    
    private void setupTotalAmountsTable(final ObservableList<DBuyerTableLine> list) {
        //Setup total amounts table
        tableTotal.getColumns().clear();
        for (TableColumn column : tvSalesDash.getColumns()) {
            TableColumn newColumn = new TableColumn("");
            if (!column.getText().isEmpty()) {
                newColumn.setCellFactory(column.getCellFactory());
                newColumn.setCellValueFactory(column.getCellValueFactory());
            }
            newColumn.prefWidthProperty().bind(column.widthProperty());
            tableTotal.getColumns().add(newColumn);
        }
        tableTotal.setEditable(false);
        tableTotal.getItems().addAll(new DBuyerTableList(list));
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
        tvSalesDash.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            //Run after initialization to get controls
            for (Node bar1 : tvSalesDash.lookupAll(".scroll-bar")) {
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

    private void fillSaleDeals() {
        try {
            for (DSalesTableLine line : dbClient.getSalesEntries()) {
                String typename = line.getType().toLowerCase();
                if (typename.equalsIgnoreCase("storage")) {
                    saleDealMap.put(line.getDealID(), line);
                }
            }
            int caseCount = 0;
            for (DBuyerTableLine line : buyerDeals) {
                if (saleDealMap.containsKey(line.getDealID())) {
                    caseCount += line.getCases();
                }
            }
            lblCaseCount.setText(Utils.toStr(caseCount));
        }
        catch (SQLException e) {
            System.out.print("sqlexception while fetching sales entries from sql");
        }

    }

    private void fillBuyerDeals() {
        buyerDeals.clear();
        godownBuyerDeals.clear();
        coldStoreBuyerDeals.clear();
        try {
            for (DBuyerTableLine line : dbClient.getBuyerDealEntries(null, new String[] {})) {
                if (line.getCases() == 0) {
                    continue;
                }
                if (line.getBuyerTitle().equalsIgnoreCase(GODOWN_BUYER_TITLE)) {
                    godownBuyerDeals.add(line);
                } else if (line.getBuyerTitle().equalsIgnoreCase(COLD_STORE_BUYER_TITLE)) {
                    coldStoreBuyerDeals.add(line);
                }
            }
        } catch (SQLException | NoSuchElementException ex) {
            Logger.getLogger(DPendingSalesController.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (displayDealsType.equalsIgnoreCase("GODOWN")) {
            buyerDeals.addAll(godownBuyerDeals);
        } else if (displayDealsType.equalsIgnoreCase("COLDSTORE")) {
            buyerDeals.addAll(coldStoreBuyerDeals);
        }
        for (DBuyerTableLine line : buyerDeals) {
            buyerAndIdMap.put(line.getSaleNo(), line);
        }
    }

    private int getStoreCasesCountFor(List<DBuyerTableLine> list, String dealId) {
        int count = 0;
        for (DBuyerTableLine line : list) {
            if (line.getDealID().equals(dealId)) {
                count += line.getCases();
            }
        }
        return count;
    }

    private void fillSupplierDeals() {
        List<String[]> values = null;
        try {
            values = dbClient.getSupplierDealEntryLines(null);
        } catch (SQLException ex) {
            Logger.getLogger(DPendingSalesController.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (String[] value : values) {
            DSupplierTableLine line = new DSupplierTableLine(value);
            List<DSupplierTableLine> supplierDeals = supplierDealMap.get(line.getDealID());
            if (supplierDeals == null) {
                supplierDeals = new ArrayList<>();
                supplierDealMap.put(line.getDealID(), supplierDeals);
            }
            supplierDeals.add(line);
        }
    }

    private DSupplierTableLine getLinkedSupplierDeal(DBuyerTableLine buyerDeal) {
        String dealId = buyerDeal.getDealID();
        String errorStr = "Buyer deals in database are not associated with supplier deals, please check your database";
        List<DSupplierTableLine> supplierDeals = supplierDealMap.get(dealId);
        if (supplierDeals == null || supplierDeals.isEmpty()) {
            GeneralMethods.errorMsg(errorStr);
            return null;
        }
        DSupplierTableLine linkedSupplierDeal = null;
        for (DSupplierTableLine line : supplierDeals) {
            if ((line.getBoxSizeType().equalsIgnoreCase(buyerDeal.getBoxSizeType()))
                    && (line.getQualityType().equalsIgnoreCase(buyerDeal.getQualityType()))
                    && (line.getFruit().equalsIgnoreCase(buyerDeal.getFruit()))) {
                linkedSupplierDeal = line;
                break;
            }
        }
        if (linkedSupplierDeal == null) {
            GeneralMethods.errorMsg(errorStr);
            return null;
        }
        return linkedSupplierDeal;
    }

}// end of classs
