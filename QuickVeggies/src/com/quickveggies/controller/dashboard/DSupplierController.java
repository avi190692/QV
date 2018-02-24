package com.quickveggies.controller.dashboard;

import static com.quickveggies.misc.Utils.isEmptyString;
import static com.quickveggies.misc.Utils.isNumStrEmpty;
import static com.quickveggies.misc.Utils.toDate;
import static com.quickveggies.misc.Utils.toInt;
import static com.quickveggies.misc.Utils.toStr;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ai.util.dates.DateUtil;
import com.ai_int.utils.ExcelExportUtil;
import com.ai_int.utils.FileUtil;
import com.ai_int.utils.PDFUtil;
import com.ai_int.utils.javafx.ListViewUtil;
import com.ai_int.utils.javafx.TableUtil;

import com.quickveggies.GeneralMethods;
import com.quickveggies.Main;
import com.quickveggies.UserGlobalParameters;
import com.quickveggies.controller.popup.EnteremailpaneController;
import com.quickveggies.controller.popup.SendEmailsPopupController;
import com.quickveggies.controller.MoneyPaidRecdController;
import com.quickveggies.controller.MoneyPaidRecdController.AmountType;
import com.quickveggies.controller.popup.PrintpopupController;
import com.quickveggies.controller.popup.SendEmailData;
import com.quickveggies.dao.DatabaseClient;
import com.quickveggies.entities.Charge;
import com.quickveggies.entities.DSupplierTableLine;
import com.quickveggies.entities.DSupplierTableList;
import com.quickveggies.entities.MoneyPaidRecd;
import com.quickveggies.entities.Supplier;
import com.quickveggies.misc.DeleteTableButtonCell;
import com.quickveggies.misc.EditTableButtonCell;
import com.quickveggies.misc.MailTableButtonCell;
import com.quickveggies.misc.PrintTableButtonCell;
import com.quickveggies.misc.Utils;
import com.quickveggies.model.EntityType;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

public class DSupplierController implements Initializable {

    private static final String NET_AMOUNT = "Net Amount";

    private static final String CASES = "Cases";

    @FXML
    private Label lblDueAmt;

    @FXML
    private Label dueAmt;

    @FXML
    private Label lblPaid;

    @FXML
    private Label Title;

    @FXML
    private ComboBox<String> batchActions;

    @FXML
    private TextField searchField;

    @FXML
    private Button newSupplier;

    @FXML
    private TableView<DSupplierTableLine> supplierDealsTable;
    
    @FXML
    private TableView<DSupplierTableLine> tableTotal;

    private static DatabaseClient dbclient = DatabaseClient.getInstance();

    private ObservableList<DSupplierTableLine> timelineViewSupDeals = FXCollections
            .observableArrayList(new DSupplierTableLine("", "", "", "", "", "", "", "", "", "", "", "", "", ""));

    private ObservableList<DSupplierTableLine> dueSuppDeals = FXCollections.observableArrayList();

    @FXML
    private Button btnDefault;

    @FXML
    private Button btnTimeline;

    @FXML
    private ComboBox<String> cboSupplierNew;

    @FXML
    private Button btnColSettings;

    @FXML
    private Button btnPrint;

    @FXML
    private Button btnExport;

    @FXML
    private Pane paneDue;

    private BooleanProperty isTimeLineView = new SimpleBooleanProperty(false);

    private ObservableList<DSupplierTableLine> defaultViewSupDeals = FXCollections.observableArrayList();

    private Map<String, TableColumn<?, ?>> columnNameMap = new LinkedHashMap<>();

    private ObservableList<String> amtPaidRecdList = FXCollections
            .observableArrayList(new String[]{MoneyPaidRecdController.PAID, MoneyPaidRecdController.RECEIVED});

    @SuppressWarnings("rawtypes")
    private TableColumn actionCols = new TableColumn<>("Actions");

    private Map<String, Supplier> supplierMap = new LinkedHashMap<>();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void initialize(URL location, ResourceBundle resources) {
        setupBatchActions();

        final Pane pane = (Pane) btnColSettings.getParent().getParent();
        supplierDealsTable.getColumns().remove(actionCols);
        ListViewUtil.addColumnSettingsButtonHandler(supplierDealsTable, pane, btnColSettings);

        cboSupplierNew.setItems(amtPaidRecdList);
        cboSupplierNew.setOnAction(new EventHandler<ActionEvent>() {
            EntityType partyType = EntityType.SUPPLIER;
            AmountType amountType = null;

            @Override
            public void handle(ActionEvent event) {
                String value = cboSupplierNew.getValue();
                String title = null;
                switch (value) {
                    case MoneyPaidRecdController.PAID:
                        amountType = AmountType.PAID;
                        title = "Money Paid Entry";
                        break;
                    case MoneyPaidRecdController.RECEIVED:
                        amountType = AmountType.RECEIVED;
                        title = "Money Received Entry";
                }
                if (amountType != null) {
                    MoneyPaidRecdController controller = new MoneyPaidRecdController(partyType, amountType, true);
                    handleAddPaidRecdMoney("/fxml/moneypaid.fxml", title, controller);

                }
            }
        });

        buildLists();

        btnDefault.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                isTimeLineView.set(false);
                ;
                btnDefault.setDisable(true);
                btnTimeline.setDisable(false);
                if (supplierDealsTable != null) {
                    if (defaultViewSupDeals.isEmpty()) {
                        return;
                    }
                    TableColumn<?, ?> casesCol = columnNameMap.get(CASES);
                    casesCol.setText("Total Cases");
                    TableColumn<?, ?> netAmtCol = columnNameMap.get(NET_AMOUNT);
                    netAmtCol.setText("Total Net Amount");
                    supplierDealsTable.setItems(defaultViewSupDeals);
                    ObservableList<TableColumn<DSupplierTableLine, ?>> tCols = supplierDealsTable.getColumns();
                    if (tCols.contains(actionCols)) {
                        supplierDealsTable.getColumns().remove(actionCols);
                    }
                    GeneralMethods.refreshTableView(supplierDealsTable, defaultViewSupDeals);
                    supplierDealsTable.refresh();
                    ListViewUtil.addColumnSettingsButtonHandler(supplierDealsTable, pane, btnColSettings);
                }
            }
        });

        paneDue.setOnMouseClicked(genericBtnHandler(pane));
        btnTimeline.setOnMouseClicked(genericBtnHandler(pane));

        supplierDealsTable.setEditable(true);
        TableColumn selectedCol = new TableColumn();
        CheckBox chkBox = new CheckBox();
        chkBox.setContentDisplay(null);
        selectedCol.setGraphic(chkBox);
        chkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                for (DSupplierTableLine line : supplierDealsTable.getItems()) {
                    line.getIsSelected().set(newValue);
                }
            }
        });
        selectedCol.setCellFactory(
                new javafx.util.Callback<TableColumn<DSupplierTableLine, Boolean>, CheckBoxTableCell<DSupplierTableLine, Boolean>>() {
            @Override
            public CheckBoxTableCell<DSupplierTableLine, Boolean> call(
                    TableColumn<DSupplierTableLine, Boolean> param) {
                return buildCheckboxColumn(supplierDealsTable);
            }
        });
        selectedCol.setEditable(true);

        TableColumn saleNoCol = new TableColumn("S.No.");
        saleNoCol.setCellValueFactory(
                new Callback<CellDataFeatures<DSupplierTableLine, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<DSupplierTableLine, String> p) {
                return new ReadOnlyObjectWrapper(supplierDealsTable.getItems().indexOf(p.getValue()) + 1 + "");
            }
        });
        TableColumn dealIdCol = new TableColumn("Gr No");
        dealIdCol.setCellValueFactory(new PropertyValueFactory<>("dealID"));
        dealIdCol.setCellFactory(TextFieldTableCell.forTableColumn());
        TableColumn supplierTitleCol = new TableColumn("Supplier");
        supplierTitleCol.setCellValueFactory(new PropertyValueFactory<>("supplierTitle"));
        supplierTitleCol.setCellFactory(TextFieldTableCell.forTableColumn());
        TableColumn dateCol = new TableColumn("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setCellFactory(TextFieldTableCell.forTableColumn());
        TableColumn proprietorCol = new TableColumn("Proprietor");
        proprietorCol.setCellValueFactory(new PropertyValueFactory<>("proprietor"));
        proprietorCol.setCellFactory(TextFieldTableCell.forTableColumn());
        TableColumn amanatCol = new TableColumn("Amanat");
        amanatCol.setCellValueFactory(new PropertyValueFactory<>("amanat"));
        amanatCol.setCellFactory(TextFieldTableCell.forTableColumn());
        TableColumn supplierRateCol = new TableColumn("Rate");
        supplierRateCol.setCellValueFactory(new PropertyValueFactory<>("supplierRate"));
        supplierRateCol.setCellFactory(TextFieldTableCell.forTableColumn());
        TableColumn totalSumCol = new TableColumn(NET_AMOUNT);
        totalSumCol.setCellValueFactory(new PropertyValueFactory<>("net"));
        totalSumCol.setCellFactory(TextFieldTableCell.forTableColumn());
        TableColumn casesCol = new TableColumn(CASES);
        casesCol.setCellValueFactory(new PropertyValueFactory<>("cases"));
        casesCol.setCellFactory(TextFieldTableCell.forTableColumn());
        TableColumn agentCol = new TableColumn("Agent");
        agentCol.setCellValueFactory(new PropertyValueFactory<>("agent"));
        agentCol.setCellFactory(TextFieldTableCell.forTableColumn());

        TableColumn mailCol = new TableColumn();
        mailCol.setCellValueFactory(new PropertyValueFactory<>("saleNo"));
        mailCol.setCellFactory(new javafx.util.Callback<TableColumn<DSupplierTableLine, String>, TableCell<DSupplierTableLine, String>>() {
            @Override
            public TableCell<DSupplierTableLine, String> call(TableColumn<DSupplierTableLine, String> param) {
                MailTableButtonCell<DSupplierTableLine, String> cell = new MailTableButtonCell<>(
                        null,
                        (Integer index)->{
                            //Send Email Message
                            DSupplierTableLine currDeal = supplierDealsTable.getItems().get(index);
                            String email = getSupplierInfo(currDeal).getEmail();
                            EnteremailpaneController.showMailWindow(email,
                                    UserGlobalParameters.userEmail, prepareInvPdf(currDeal));
                        }
                );
                return cell;

            }
        });
        TableColumn editCol = new TableColumn();
        editCol.setCellValueFactory(new PropertyValueFactory<>("saleNo"));
        editCol.setCellFactory(
                new Callback<TableColumn<DSupplierTableLine, String>, TableCell<DSupplierTableLine, String>>() {
            @Override
            public TableCell<DSupplierTableLine, String> call(TableColumn<DSupplierTableLine, String> param) {
                return new EditTableButtonCell("DSupplierTableLine");
            }
        });
        TableColumn<DSupplierTableLine, String> printCol = new TableColumn<>();
        printCol.setCellValueFactory(new PropertyValueFactory<>("saleNo"));
        printCol.setCellFactory(
                new Callback<TableColumn<DSupplierTableLine, String>, TableCell<DSupplierTableLine, String>>() {

            @Override
            public TableCell<DSupplierTableLine, String> call(TableColumn<DSupplierTableLine, String> param) {
                PrintTableButtonCell<DSupplierTableLine, String> pTabBtn = new PrintTableButtonCell<DSupplierTableLine, String>() {
                    String lineNum = null;

                    @Override
                    public void updateItem(String item, boolean isEmpty) {
                        super.updateItem(item, isEmpty);
                        lineNum = item;

                        this.getPrintButton().setOnMouseClicked((event) -> {
                            //System.out.println("Print button cell");
                            if (!event.getButton().equals(MouseButton.PRIMARY)) {
                                return;
                            }
                            
                            DSupplierTableLine currDeal = supplierDealsTable.getItems().get(getIndex());
                            
                            DashboardController.showPopup("/fxml/printpopup.fxml", "Print Preview",
                                    new PrintpopupController(new ArrayList<String>() {{
                                        this.add(prepareInvPdf(currDeal));
                                    }}));
                        });
                    }
                };
                return pTabBtn;
            }
        });
        TableColumn deleteCol = new TableColumn();
        deleteCol.setCellValueFactory(new PropertyValueFactory<>("dealID"));
        deleteCol.setCellFactory(
                new Callback<TableColumn<DSupplierTableLine, String>, TableCell<DSupplierTableLine, String>>() {
            @Override
            public DeleteTableButtonCell<DSupplierTableLine, String> call(
                    TableColumn<DSupplierTableLine, String> param) {
                DeleteTableButtonCell cell = new DeleteTableButtonCell("supplierDeals", "id");
                cell.setMultipleDelete(new String[]{"supplierDeals", "buyerDeals", "arrival"}, "dealID");
                return cell;
            }
        });

        columnNameMap.put(CASES, casesCol);
        columnNameMap.put(NET_AMOUNT, totalSumCol);

        actionCols.getColumns()
                .addAll(Arrays.asList(new TableColumn[]{agentCol, mailCol, editCol, printCol, deleteCol}));

        supplierDealsTable.getItems().addAll(timelineViewSupDeals);
        supplierDealsTable.getColumns().addAll(selectedCol, saleNoCol, dealIdCol, dateCol, supplierTitleCol,
                proprietorCol, casesCol, amanatCol, totalSumCol, actionCols);
        for (TableColumn<DSupplierTableLine, ?> column : supplierDealsTable.getColumns()) {
            if (!"".equals(column.getText())) {
                column.setEditable(false);
            }
        }
        newSupplier.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                showNewSupplierDialog();

            }
        });

        btnExport.setOnAction((event) -> {
            TableColumn<?, ?> tcAction = actionCols;
            boolean prevVisibility = tcAction.isVisible();
            tcAction.setVisible(false);
            String[][] tableData = TableUtil.toArray(supplierDealsTable);
            String fileName = FileUtil.getSaveToFileName(btnExport.getScene(), "Select Excel file", FileUtil.getExcelExtMap());
            if (fileName != null) {
                try {
                    ExcelExportUtil.exportTableData(tableData, "Supplier Deal List", fileName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            tcAction.setVisible(prevVisibility);
        });
        btnPrint.setOnAction((event) -> TableUtil.printTable(supplierDealsTable, "Supplier Deals", actionCols));
        performPostInit();
        setupTotalAmountsTable(timelineViewSupDeals);
    }
    
    private void setupTotalAmountsTable(final ObservableList<DSupplierTableLine> list) {
        //Setup total amounts table
        tableTotal.getColumns().clear();
        for (TableColumn column : supplierDealsTable.getColumns()) {
            TableColumn newColumn = new TableColumn("");
            if (!column.getText().isEmpty()) {
                newColumn.setCellFactory(column.getCellFactory());
                newColumn.setCellValueFactory(column.getCellValueFactory());
            }
            newColumn.prefWidthProperty().bind(column.widthProperty());
            tableTotal.getColumns().add(newColumn);
        }
        tableTotal.setEditable(false);
        tableTotal.getItems().addAll(new DSupplierTableList(list));
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
        supplierDealsTable.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            //Run after initialization to get controls
            for (Node bar1 : supplierDealsTable.lookupAll(".scroll-bar")) {
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

    public static void showNewSupplierDialog() {
        // Main.getStage().getScene().getRoot().setEffect(new
        // GaussianBlur());
        final Stage addSupplier = new Stage();
        addSupplier.centerOnScreen();
        addSupplier.setTitle("Add new Supplier/Grower");
        addSupplier.initModality(Modality.APPLICATION_MODAL);
        addSupplier.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent event) {
                Main.getStage().getScene().getRoot().setEffect(null);
            }
        });
        try {
            Parent parent = FXMLLoader.load(DSupplierController.class.getResource("/fxml/supplieradd.fxml"));
            Scene scene = new Scene(parent, 687, 400);
            scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
                public void handle(KeyEvent event) {
                    if (event.getCode() == KeyCode.ESCAPE) {
                        Main.getStage().getScene().getRoot().setEffect(null);
                        addSupplier.close();
                    }
                }
            });
            addSupplier.setScene(scene);
            addSupplier.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void buildLists() {
        // update lines from sql database:
        try {
            List<String[]> lines = dbclient.getSupplierDealEntryLines(null);
            timelineViewSupDeals = FXCollections.observableArrayList();
            defaultViewSupDeals.clear();
            timelineViewSupDeals.clear();
            for (int i = 0; i < lines.size(); i++) {
                timelineViewSupDeals.add(new DSupplierTableLine(lines.get(i)));
            }
        } catch (SQLException | NoSuchElementException e) {
            System.out.println("sqlexception while fetching supplier deal entries from sql\n" + e.getMessage());
        }
        //Group list by deal ID
        groupListByDeal();
        timelineViewSupDeals = timelineViewSupDeals.sorted(Comparator.comparing((DSupplierTableLine line) -> {
            LocalDate date1 = Utils.toDate(line.getDate());
            return date1;
        }).thenComparing(DSupplierTableLine::getSupplierTitle));

        if (timelineViewSupDeals != null && !timelineViewSupDeals.isEmpty()) {
            buildDefaultViewList();
        }
    }
    
    private void groupListByDeal() {
        Map<String, DSupplierTableLine> map = new LinkedHashMap<>();
        for (DSupplierTableLine srcLine : timelineViewSupDeals) {
            DSupplierTableLine tl = map.get(srcLine.getDealID());
            if (tl == null) {
                tl = new DSupplierTableLine(srcLine.getAll());
                map.put(tl.getDealID(), tl);
            } else {
                Integer destNetAmt = isEmptyString(tl.getNet()) ? 0 : toInt(tl.getNet());
                Integer srcNetAmt = isEmptyString(srcLine.getNet()) ? 0 : toInt(srcLine.getNet());
                tl.setNet(toStr(destNetAmt + srcNetAmt));
                Integer destRecAmt = isEmptyString(tl.getAmountReceived()) ? 0 : toInt(tl.getAmountReceived());
                Integer srcRecAmt = isEmptyString(srcLine.getAmountReceived()) ? 0 : toInt(srcLine.getAmountReceived());
                Integer destAmanatTot = isEmptyString(tl.getAmanat()) ? 0 : toInt(tl.getAmanat());
                Integer srcAmanatTot = isEmptyString(srcLine.getAmanat()) ? 0 : toInt(srcLine.getAmanat());
                tl.setAmountReceived(toStr(srcRecAmt + destRecAmt));
                tl.setAmanat(toStr(srcAmanatTot + destAmanatTot));
                Integer destCases = toInt(tl.getCases());
                Integer srcCases = toInt(srcLine.getCases());
                tl.setCases(toStr(destCases + srcCases));
                tl.setDate(DateUtil.getNewerDateString(tl.getDate(), srcLine.getDate()));
            }
        }
        timelineViewSupDeals.clear();
        for (DSupplierTableLine newDLine : map.values()) {
            timelineViewSupDeals.add(newDLine);
        }
    }

    private void performPostInit() {
        lblPaid.setText(Utils.toStr(getAmountReceivedFromParty(30, EntityType.SUPPLIER)));
        calculateDues();
        actionCols.visibleProperty().bindBidirectional(isTimeLineView);
        btnDefault.fire();
    }

    private void buildDefaultViewList() {
        Map<String, DSupplierTableLine> map = new LinkedHashMap<>();
        for (DSupplierTableLine srcLine : timelineViewSupDeals) {
            DSupplierTableLine tl = map.get(srcLine.getSupplierTitle());
            if (tl == null) {
                tl = new DSupplierTableLine(srcLine.getAll());
                tl.setSaleNo("--");
                tl.setDealID("NA");
                tl.setAgent("NA");
                tl.setProprietor("NA");
                tl.setOrchard("NA");
                map.put(tl.getSupplierTitle(), tl);
            }
            else {
                Integer destNetAmt = isEmptyString(tl.getNet()) ? 0 : toInt(tl.getNet());
                Integer srcNetAmt = isEmptyString(srcLine.getNet()) ? 0 : toInt(srcLine.getNet());
                tl.setNet(toStr(destNetAmt + srcNetAmt));
                Integer destRecAmt = isEmptyString(tl.getAmountReceived()) ? 0 : toInt(tl.getAmountReceived());
                Integer srcRecAmt = isEmptyString(srcLine.getAmountReceived()) ? 0 : toInt(srcLine.getAmountReceived());
                Integer destAmanatTot = isEmptyString(tl.getAmanat()) ? 0 : toInt(tl.getAmanat());
                Integer srcAmanatTot = isEmptyString(srcLine.getAmanat()) ? 0 : toInt(srcLine.getAmanat());
                tl.setAmountReceived(toStr(srcRecAmt + destRecAmt));
                tl.setAmanat(toStr(srcAmanatTot + destAmanatTot));
                Integer destCases = toInt(tl.getCases());
                Integer srcCases = toInt(srcLine.getCases());
                tl.setCases(toStr(destCases + srcCases));
                tl.setDate(DateUtil.getNewerDateString(tl.getDate(), srcLine.getDate()));
            }
        }
        for (DSupplierTableLine newDLine : map.values()) {
            defaultViewSupDeals.add(newDLine);
        }
    }

    private void handleAddPaidRecdMoney(String resource, String title, Object controller) {
        try {
            final Stage stage = new Stage();
            stage.centerOnScreen();
            stage.setTitle(title);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                public void handle(WindowEvent event) {
                    Main.getStage().getScene().getRoot().setEffect(null);
                }
            });
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(resource));
                loader.setController(controller);
                Scene scene = new Scene((Parent) loader.load());

                scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
                    public void handle(KeyEvent event) {
                        if (event.getCode() == KeyCode.ESCAPE) {
                            Main.getStage().getScene().getRoot().setEffect(null);
                            stage.close();
                        }
                    }
                });

                EventHandler<WindowEvent> wEvent = new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent event) {
                        supplierDealsTable.refresh();
                        buildLists();
                        performPostInit();
                    }
                };
                stage.setOnCloseRequest(wEvent);
                stage.setOnHiding(wEvent);
                stage.setScene(scene);
                stage.show();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Builds a column with check boxes
     */
    private CheckBoxTableCell<DSupplierTableLine, Boolean> buildCheckboxColumn(
            final TableView<DSupplierTableLine> tableView) {
        CheckBoxTableCell<DSupplierTableLine, Boolean> cell = new CheckBoxTableCell<>(
                new Callback<Integer, ObservableValue<Boolean>>() {
            @Override
            public ObservableValue<Boolean> call(final Integer index) {
                BooleanProperty selected = tableView.getItems().get(index).getIsSelected();
                selected.addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> obs, Boolean wasSelected,
                            Boolean isSelected) {
                        DSupplierTableLine item = tableView.getItems().get(index);
                        item.getIsSelected().set(isSelected);
                    }
                });
                return selected;
            }
        });
        return cell;
    }

    private void setupBatchActions() {
        ObservableList<String> actions = FXCollections.observableArrayList(new String[] { "Batch Actions", "EMAIL", "PRINT" });
        batchActions.setItems(actions);
        batchActions.valueProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            boolean processed = false;
            if ("EMAIL".equals(newValue)) {
                //Prepare and Send Email Messages
                List<SendEmailData> emails = new ArrayList<>();
                for (DSupplierTableLine line : supplierDealsTable.getItems()) {
                    if (line.getIsSelected().get()) {
                        String email = getSupplierInfo(line).getEmail();
                        emails.add(SendEmailData.buildSendEmailData(email,
                                UserGlobalParameters.userEmail, "", "", prepareInvPdf(line)));
                    }
                }
                if (emails.size() > 0) {
                    SendEmailsPopupController.showSendEmailsPopup(emails);
                }
            }
            else if ("PRINT".equals(newValue)) {
                List<String> fileNames = new ArrayList<>();
                for (DSupplierTableLine line : supplierDealsTable.getItems()) {
                    if (line.getIsSelected().get()) {
                        fileNames.add(prepareInvPdf(line));
                    }
                }
                processed = true;
                if (fileNames.size() > 0) {
                    DashboardController.showPopup("/fxml/printpopup.fxml", "Print Preview",
                            new PrintpopupController(fileNames));
                }
            }
            if (processed) {
                Platform.runLater(() -> {
                    batchActions.getSelectionModel().selectFirst();
                });
            }
        });
    }

    private void calculateDues() {
        Long dues = 0l;
        LocalDate firstDealDate = LocalDate.now();
        EntityType ptSupplier = EntityType.SUPPLIER;
        for (DSupplierTableLine tl : timelineViewSupDeals) {
            try {
                dues += tl.getNet().isEmpty() ? 0 : Double.valueOf(tl.getNet()).longValue();
                LocalDate dealDate = tl.getDate().isEmpty() ? new java.sql.Date(0).toLocalDate()
                        : toDate(tl.getDate()).minusDays(1);
                if (dealDate.isEqual(firstDealDate) || dealDate.isBefore(firstDealDate)) {
                    firstDealDate = dealDate;
                }
            }
            catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        long days = firstDealDate.toEpochDay();
        days = LocalDate.now().toEpochDay() - days;
        // System.out.println(days);
        dues = (dues + getAmountPaidToParty(ptSupplier)) - getAmountReceivedFromParty(days, ptSupplier);
        lblDueAmt.setText(toStr(dues));
        dueAmt.setText(" Due(s)");
    }

    /**
     * Calculates and returns the total amount paid by the party in the
     * specified period
     *
     *
     * @param duration - No of days amount was paid
     * @return sum of total amount paid by party
     */
    public static Long getAmountReceivedFromParty(long days, EntityType pType) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        List<MoneyPaidRecd> allPartyMprList = dbclient.getAllMoneyPaidRecdList(pType);
        double totalReceived = 0d;// total money paid by user in 30 days
        for (MoneyPaidRecd mpr : allPartyMprList) {
            if (isNumStrEmpty(mpr.getReceived())) {
                continue;
            }
            LocalDate transDate = toDate(mpr.getDate());
            if (transDate.isAfter(startDate)) {
                totalReceived += toInt(mpr.getReceived());
            }
        }
        return (long) totalReceived;
    }

    /**
     * Calculates and returns the total amount paid to the party
     *
     *
     * @param duration - No of days amount was paid
     * @return sum of total amount paid by party
     */
    public static Long getAmountPaidToParty(EntityType pType) {
        List<MoneyPaidRecd> allPartyMprList = dbclient.getAllMoneyPaidRecdList(pType);
        double totalPaid = 0d;// total money paid by user in 30 days
        for (MoneyPaidRecd mpr : allPartyMprList) {
            if (isNumStrEmpty(mpr.getPaid())) {
                continue;
            }

            totalPaid += toInt(mpr.getPaid());
        }
        return (long) totalPaid;
    }

    private EventHandler<MouseEvent> genericBtnHandler(final Pane pane) {

        return new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (!event.getButton().equals(MouseButton.PRIMARY)) {
                    return;
                }
                btnDefault.setDisable(false);
                ObservableList<DSupplierTableLine> list = null;
                String compId = ((Node) event.getSource()).getId();
                if (compId == null) {
                    return;
                }
                switch (compId) {
                    case "paneDue":
                        list = dueSuppDeals;
                        btnTimeline.setDisable(false);
                        break;
                    case "btnTimeline":
                        list = timelineViewSupDeals;
                        btnTimeline.setDisable(true);
                        break;

                }
                btnDefault.setDisable(false);
                btnTimeline.setDisable(true);
                isTimeLineView.set(true);
                if (supplierDealsTable != null) {
                    TableColumn<?, ?> casesCol = columnNameMap.get(CASES);
                    casesCol.setText(CASES);
                    TableColumn<?, ?> netAmtCol = columnNameMap.get(NET_AMOUNT);
                    netAmtCol.setText(NET_AMOUNT);
                    ObservableList<TableColumn<DSupplierTableLine, ?>> tCols = supplierDealsTable.getColumns();
                    if (!tCols.contains(actionCols)) {
                        supplierDealsTable.getColumns().add(actionCols);
                    }
                    supplierDealsTable.setItems(timelineViewSupDeals);
                    supplierDealsTable.refresh();
                    ListViewUtil.addColumnSettingsButtonHandler(supplierDealsTable, pane, btnColSettings);
                }
            }
        };
    }

    private Supplier getSupplierInfo(DSupplierTableLine dealLine) {
        String suppTitle = dealLine.getSupplierTitle();
        if (!supplierMap.containsKey(suppTitle)) {
            try {
                Supplier supplier = dbclient.getSupplierByName(suppTitle);
                supplierMap.put(suppTitle, supplier);
            } catch (NoSuchElementException | SQLException e) {
                e.printStackTrace();
            }
        }
        Supplier supplier = supplierMap.get(suppTitle);
        return supplier;
    }

    private String prepareInvPdf(DSupplierTableLine currDeal) {
        String[][] dataArr = buildInvTabForEmail(currDeal);
        Arrays.asList(dataArr).forEach(data -> {Arrays.asList(data).forEach(System.out::println);});
        Supplier supplier = getSupplierInfo(currDeal);
        String supplierName = currDeal.getSupplierTitle();
        if (supplier != null) {
            supplierName = supplier.getFirstName() + " " + supplier.getLastName();
        }
        String pdfFile = PDFUtil.buildSupplierInvoicePdf(currDeal.getSupplierTitle(), supplierName, currDeal.getDealID(),
                currDeal.getDate(), dataArr);
        return pdfFile;
    }

    public static String[][] buildInvTabForEmail(DSupplierTableLine line) {
        String dealId = line.getDealID();
        List<DSupplierTableLine> suppDeals;
        try {
            suppDeals = dbclient.getSupplierDealEntries(line.getSupplierTitle());
        } catch (SQLException ex) {
            Logger.getLogger(DSupplierController.class.getName()).log(Level.SEVERE, null, ex);
            return new String[0][0];
        }
        Iterator<DSupplierTableLine> lines = suppDeals.iterator();
        while (lines.hasNext()) {
            DSupplierTableLine deal = lines.next();
            if (!deal.getDealID().equals(line.getDealID())) {
                lines.remove();
            }
            else if (deal.getSaleNo().equals(line.getSaleNo())) {
                deal.deserialize(line.serialize());
            }
        }
        int recCount = suppDeals.size();
        List<String[]> table = new ArrayList<>();

        table.add(new String[]{"FRUIT", "QUALITY", "SIZE", "CASES", "RATE", "GROSS AMT", "EXP NAME", "AMOUNT"});
        Integer[][] caseRateArr = new Integer[recCount][2];

        AtomicInteger rowCount = new AtomicInteger(0);
        suppDeals.stream().forEach((t) -> {
            int i = rowCount.incrementAndGet();
            Integer cases = Utils.toInt(t.getCases());
            Integer rate = Utils.toInt(t.getSupplierRate());
            String[] dealLine = new String[]{t.getFruit(), t.getQualityType(), t.getBoxSizeType(), cases.toString(), rate.toString(),
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
        List<Charge> suppCharges = dbclient.getDealCharges(Integer.valueOf(dealId));
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
