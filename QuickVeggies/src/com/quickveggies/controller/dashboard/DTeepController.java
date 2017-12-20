package com.quickveggies.controller.dashboard;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;

import com.ai_int.utils.ExcelExportUtil;
import com.ai_int.utils.FileUtil;
import com.ai_int.utils.javafx.ListViewUtil;
import com.ai_int.utils.javafx.TableUtil;
import com.quickveggies.controller.TeepDetailController;
import com.quickveggies.dao.DatabaseClient;
import com.quickveggies.entities.DBuyerTableLine;
import com.quickveggies.entities.DSalesTableLine;
import com.quickveggies.entities.DBuyerTableList;
import com.quickveggies.entities.DSupplierTableLine;
import com.quickveggies.entities.StorageBuyerDeal;
import com.quickveggies.misc.Utils;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.scene.layout.Pane;
import javafx.util.Callback;

public class DTeepController implements Initializable {

    @FXML
    private TableView<DBuyerTableLine> tvTeeps;
    
    @FXML
    private TableView<DBuyerTableLine> tableTotal;

    @FXML
    private Label lblComission;

    @FXML
    private Label lblTurnover;

    @FXML
    private Button btnColSettings;

    @FXML
    private Button btnPrint;

    @FXML
    private Button btnExport;

    private ObservableList<DBuyerTableLine> buyerDeals = FXCollections.observableArrayList();

    private ObservableList<DSupplierTableLine> supplierDeals = FXCollections.observableArrayList();

    private ObservableList<DSalesTableLine> salesDeals = FXCollections.observableArrayList();

    private Map<PartyDealKey, String> supplierDealChargesMap = new LinkedHashMap<>();

    private static DatabaseClient dbClient = DatabaseClient.getInstance();

    private StringProperty totalCommissionProp = new SimpleStringProperty("0");

    private StringProperty totalTurnoverProp = new SimpleStringProperty("0");

    private Map<PartyDealKey, DBuyerTableLine> buyerDealMap = new LinkedHashMap<>();

    private Map<PartyDealKey, DSupplierTableLine> supplierDealMap = new LinkedHashMap<>();

    private Map<String, DSalesTableLine> saleDealMap = new LinkedHashMap<>();

    private Map<PartyDealKey, DBuyerTableLine> storageBuyerMap = new LinkedHashMap<>();

    private Map<String, String> storageDidBuyerDidMap = new LinkedHashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        final Pane pane = (Pane) btnColSettings.getParent().getParent();
        ListViewUtil.addColumnSettingsButtonHandler(tvTeeps, pane, btnColSettings);

        fillBuyerDeals();
        fillSupplierDeals();
        fillSaleDeals();
        fillStorageIdMap();
        tvTeeps.setItems(buyerDeals);
        addColumnInfo();
        lblComission.textProperty().bind(totalCommissionProp);
        lblTurnover.textProperty().bind(totalTurnoverProp);
        calculateTurnover();
        calculateCommssion();

        btnExport.setOnAction((event) -> {
            TableColumn<?, ?> tcAction = tvTeeps.getColumns().get(6);
            boolean prevVisibility = tcAction.isVisible();
            tcAction.setVisible(false);
            String[][] tableData = TableUtil.toArray(tvTeeps);
            String fileName = FileUtil.getSaveToFileName(btnExport.getScene(), "Select Excel file", FileUtil.getExcelExtMap());
            if (fileName != null) {
                try {
                    ExcelExportUtil.exportTableData(tableData, "Teep Detail List", fileName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            tcAction.setVisible(prevVisibility);
        });

        btnPrint.setOnAction((event) -> {
            TableColumn<?, ?> tcAction = tvTeeps.getColumns().get(6);
            TableUtil.printTable(tvTeeps, "Teep Detail List", tcAction);
        });

        setupTotalAmountsTable(buyerDeals);
    }
    
    private void setupTotalAmountsTable(final ObservableList<DBuyerTableLine> list) {
        //Setup total amounts table
        tableTotal.getColumns().clear();
        for (TableColumn column : tvTeeps.getColumns()) {
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
        tvTeeps.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            //Run after initialization to get controls
            for (Node bar1 : tvTeeps.lookupAll(".scroll-bar")) {
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

    private void calculateTurnover() {
        Integer turnover = 0;
        for (DSalesTableLine line : salesDeals) {
            turnover += Utils.toInt(line.getGross());
        }
        totalTurnoverProp.set(String.valueOf(turnover));
    }

    private void fillBuyerDeals() {
        try {
            for (DBuyerTableLine line : dbClient.getBuyerDealEntries(null, new String[] {})) {
                    String buyerTitle = line.getBuyerTitle().trim().toLowerCase();
                    if (buyerTitle.equals("cold store") || buyerTitle.equals("godown")) {
                        storageBuyerMap.put(buildPartyKey(line), line);
                        continue;
                    }
                    buyerDealMap.put(buildPartyKey(line), line);
                    buyerDeals.add(line);
            }
        }
        catch (NoSuchElementException | SQLException e) {
            e.printStackTrace();
        }
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
            supplierDealChargesMap.put(buildPartyKey(line), line.getNet());
            supplierDealMap.put(buildPartyKey(line), line);
            supplierDeals.add(line);
        }
    }

    private void fillSaleDeals() {
        for (DBuyerTableLine buyerLine : buyerDeals) {
            try {
                DSalesTableLine dstl = dbClient.getSalesEntryLineByDealId(Integer.valueOf(buyerLine.getDealID()));
                salesDeals.add(dstl);
                saleDealMap.put(dstl.getDealID(), dstl);
            } catch (NumberFormatException | NoSuchElementException | SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void fillStorageIdMap() {
        for (StorageBuyerDeal deal : dbClient.getStorageBuyerDeals()) {
            storageDidBuyerDidMap.put(deal.getBuyerDealLineId().toString(), deal.getStrorageDealLineId().toString());
        }
    }

    private void calculateCommssion() {
        Integer totalCommsssion = Integer.valueOf(totalCommissionProp.getValue());
        for (DBuyerTableLine buyerDeal : buyerDeals) {
            String strBuyerCharge = buyerDeal.getAggregatedAmount();
            String strSupplierCharge = getSupplierCharge(buyerDeal);

            Integer commission = getDifference(strBuyerCharge, strSupplierCharge);
            totalCommsssion += commission;
        }
        String commStr = String.valueOf(totalCommsssion);
        totalCommissionProp.set(commStr);
    }

    @SuppressWarnings("unchecked")
    private void addColumnInfo() {
        double tableWidth = tvTeeps.getPrefWidth();

        TableColumn<DBuyerTableLine, String> serialCol = new TableColumn<>("S.No");
        serialCol.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<DBuyerTableLine, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<DBuyerTableLine, String> param) {
                return new ReadOnlyObjectWrapper<String>(tvTeeps.getItems().indexOf(param.getValue()) + 1 + "");
            }
        });
        serialCol.setPrefWidth((tableWidth * 10) / 100);
        //
        TableColumn<DBuyerTableLine, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<DBuyerTableLine, String>("date"));
        dateCol.setPrefWidth((tableWidth * 15) / 100);
        ///
        TableColumn<DBuyerTableLine, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<DBuyerTableLine, String>("buyerTitle"));
        titleCol.setPrefWidth((tableWidth * 15) / 100);
        //
        TableColumn<DBuyerTableLine, String> casesCol = new TableColumn<>("Cases");
        casesCol.setCellValueFactory(new PropertyValueFactory<DBuyerTableLine, String>("cases"));
        casesCol.setPrefWidth((tableWidth * 10) / 100);

        TableColumn<DBuyerTableLine, String> diffAmountCol = new TableColumn<>("Difference Amount");
        diffAmountCol.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<DBuyerTableLine, String>, ObservableValue<String>>() {

            @Override
            public ObservableValue<String> call(CellDataFeatures<DBuyerTableLine, String> param) {
                DBuyerTableLine buyerDeal = param.getValue();
                String strBuyerCharge = buyerDeal.getAggregatedAmount();
                String strSupplierCharge = getSupplierCharge(buyerDeal);
                Integer commission = getDifference(strBuyerCharge, strSupplierCharge);
                String commStr = String.valueOf(commission);
                return new ReadOnlyObjectWrapper<String>(commStr);
            }
        });
        diffAmountCol.setPrefWidth((tableWidth * 19) / 100);

        TableColumn<DBuyerTableLine, String> percentCol = new TableColumn<>("Percent");
        percentCol.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<DBuyerTableLine, String>, ObservableValue<String>>() {

            @Override
            public ObservableValue<String> call(CellDataFeatures<DBuyerTableLine, String> param) {
                DBuyerTableLine buyerDeal = param.getValue();
                if (buyerDeal.isTotalLine()) {
                    return new ReadOnlyObjectWrapper<String>("");
                }
                String strBuyerCharge = buyerDeal.getAggregatedAmount();
                String strSupplierCharge = getSupplierCharge(buyerDeal);

                return new ReadOnlyObjectWrapper<String>(
                        getPercent(strBuyerCharge, strSupplierCharge).toString().concat("%"));
            }
        });
        percentCol.setPrefWidth((tableWidth * 13) / 100);

        TableColumn<DBuyerTableLine, PartyDealKey> viewCol = new TableColumn<>("Action");
        viewCol.setCellFactory(
                new Callback<TableColumn<DBuyerTableLine, PartyDealKey>, TableCell<DBuyerTableLine, PartyDealKey>>() {

            @Override
            public TableCell<DBuyerTableLine, PartyDealKey> call(
                    TableColumn<DBuyerTableLine, PartyDealKey> param) {
                TableCell<DBuyerTableLine, PartyDealKey> cell = new TableCell<DBuyerTableLine, PartyDealKey>() {
                    @Override
                    public void updateItem(final PartyDealKey item, boolean empty) {
                        if (item != null) {
                            final DBuyerTableLine buyerDeal = buyerDealMap.get(item);
                            if (buyerDeal != null && !buyerDeal.isTotalLine()) {
                                Hyperlink hLink = new Hyperlink();
                                hLink.setText("View");
                                hLink.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent event) {
                                        DSupplierTableLine suppDeal = supplierDealMap
                                                .get(findSupplierDealKeyFrom(buyerDeal));
                                        TeepDetailController controller = new TeepDetailController(buyerDeal,
                                                suppDeal, getSupplierCharge(buyerDeal));
                                        DashboardController.showPopup("/teepdetails.fxml", "Teep Difference Detail",
                                                controller);
                                    }
                                });
                                setGraphic(hLink);
                            }
                        }
                    }
                };
                return cell;
            }
        });
        viewCol.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<DBuyerTableLine, PartyDealKey>, ObservableValue<PartyDealKey>>() {

            @Override
            public ObservableValue<PartyDealKey> call(CellDataFeatures<DBuyerTableLine, PartyDealKey> param) {
                DBuyerTableLine line = param.getValue();
                return new ReadOnlyObjectWrapper<>(buildPartyKey(line));
            }
        });

        tvTeeps.getColumns().addAll(
                new TableColumn[]{serialCol, dateCol, titleCol, casesCol, diffAmountCol, percentCol, viewCol});
    }

    private Integer getDifference(String strBuyerCharge, String strSupplierCharge) {
        Integer buyerCharge = strBuyerCharge.isEmpty() ? 0 : Utils.toInt(strBuyerCharge);
        Integer supplierCharge = strSupplierCharge.isEmpty() ? 0 : Utils.toInt(strSupplierCharge);
        return buyerCharge - supplierCharge;
    }

    private BigDecimal getPercent(String strBuyerCharge, String strSupplierCharge) {
        Double suppCharge = Double.valueOf(Utils.toInt(strSupplierCharge));
        if (suppCharge == 0) {
            System.err.println("WARNING: Supplier charges are 0, please check database for data problem");
            return BigDecimal.ZERO;
        }
        Double profit = Double.valueOf(getDifference(strBuyerCharge, strSupplierCharge));
        BigDecimal percentProfit = BigDecimal.valueOf((profit / suppCharge) * 100);
        percentProfit = percentProfit.setScale(2, RoundingMode.CEILING);
        return percentProfit;

    }

    private String getSupplierCharge(DBuyerTableLine buyerDeal) {
        PartyDealKey buyerKey = buildPartyKey(buyerDeal);
        PartyDealKey suppKey = findSupplierDealKeyFrom(buyerDeal);
        int cases = buyerDeal.getCases();
        if (suppKey == null) {
            String errorStr = String.format("Corresponding supplier deal not found for the buyer deal id:%s, fruit:%s",
                    buyerDeal.getDealID(), buyerDeal.getFruit());
            System.err.println(errorStr);
            // throw new IllegalStateException(errorStr);
            return "0";
        }
        String strSupplierCharge = "0";
        DBuyerTableLine storageBuyerDeal = getStorageBuyerDeal(buyerDeal);
        if (storageBuyerDeal != null) {
            strSupplierCharge = String.valueOf((Utils.toInt(storageBuyerDeal.getBuyerRate()) * cases));
            supplierDealMap.get(suppKey).setNet(strSupplierCharge);
        } else {
            DSupplierTableLine suppDeal = supplierDealMap.get(suppKey);
            strSupplierCharge = (Utils.toInt(suppDeal.getSupplierRate()) * cases) + "";
            suppDeal.setNet(strSupplierCharge);
        }
        return strSupplierCharge;
    }

    private DBuyerTableLine getStorageBuyerDeal(DBuyerTableLine buyerDeal) {
        DBuyerTableLine storageLine = null;
        String storageDealLineId = null;
        for (String lineId : storageDidBuyerDidMap.keySet()) {
            if (lineId.equals(buyerDeal.getSaleNo().trim())) {
                storageDealLineId = storageDidBuyerDidMap.get(lineId);
                break;
            }
        }
        if (storageDealLineId != null) {
            for (DBuyerTableLine line : storageBuyerMap.values()) {
                if (line.getSaleNo().trim().equals(storageDealLineId)) {
                    System.out.println("Found storage deal for buyer deal:" + buyerDeal.getSaleNo());
                    storageLine = line;
                    break;
                }
            }
        }
        return storageLine;
    }

    private class PartyDealKey {

        private String dealId;

        private String quality;

        private String fruit;

        private String boxSizeType;

        private String title;

        public PartyDealKey(String dealId, String quality, String fruit, String boxSizeType, String title) {
            this.dealId = dealId;
            this.quality = quality;
            this.fruit = fruit;
            this.boxSizeType = boxSizeType;
            this.title = title;

        }

        public boolean equals(Object object) {
            if (!(object instanceof PartyDealKey)) {
                return false;
            }
            PartyDealKey other = (PartyDealKey) object;
            if (other.dealId.equals(dealId) && other.quality.equals(quality) && other.boxSizeType.equals(boxSizeType)
                    && other.fruit.equals(fruit) && other.title.equals(title)) {
                return true;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return quality.hashCode() + fruit.hashCode() + boxSizeType.hashCode() + dealId.hashCode() + 37;
        }
    }

    private PartyDealKey buildPartyKey(DBuyerTableLine line) {
        return new PartyDealKey(line.getDealID(), line.getQualityType(), line.getFruit(), line.getBoxSizeType(),
                line.getBuyerTitle());
    }

    private PartyDealKey buildPartyKey(DSupplierTableLine line) {
        return new PartyDealKey(line.getDealID(), line.getQualityType(), line.getFruit(), line.getBoxSizeType(),
                line.getSupplierTitle());
    }

    private PartyDealKey findSupplierDealKeyFrom(DBuyerTableLine line) {
        PartyDealKey buyerKey = buildPartyKey(line);
        PartyDealKey suppKey = null;
        for (DSupplierTableLine suppDeal : supplierDeals) {
            PartyDealKey tmpKey = buildPartyKey(suppDeal);
            if (tmpKey.fruit.equalsIgnoreCase(buyerKey.fruit) && tmpKey.quality.equalsIgnoreCase(buyerKey.quality)
                    && tmpKey.boxSizeType.equalsIgnoreCase(buyerKey.boxSizeType)) {
                suppKey = tmpKey;
            }
        }
        return suppKey;
    }

}
