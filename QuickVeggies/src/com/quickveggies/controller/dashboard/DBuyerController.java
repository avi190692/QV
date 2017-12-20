package com.quickveggies.controller.dashboard;

import static com.quickveggies.entities.Buyer.COLD_STORE_BUYER_TITLE;
import static com.quickveggies.entities.Buyer.GODOWN_BUYER_TITLE;
import static com.quickveggies.misc.Utils.isEmptyString;
import static com.quickveggies.misc.Utils.isNumStrEmpty;
import static com.quickveggies.misc.Utils.isUnderDateRange;
import static com.quickveggies.misc.Utils.toDate;
import static com.quickveggies.misc.Utils.toInt;
import static com.quickveggies.misc.Utils.toStr;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.ai.util.dates.DateUtil;
import com.ai_int.utils.ExcelExportUtil;
import com.ai_int.utils.FileUtil;
import com.ai_int.utils.PDFUtil;
import com.ai_int.utils.javafx.ListViewUtil;
import com.ai_int.utils.javafx.TableUtil;
import com.quickveggies.Main;
import com.quickveggies.UserGlobalParameters;
import com.quickveggies.controller.popup.SendEmailsPopupController;
import com.quickveggies.controller.MoneyPaidRecdController;
import com.quickveggies.controller.MoneyPaidRecdController.AmountType;
import com.quickveggies.controller.popup.PrintpopupController;
import com.quickveggies.controller.popup.SendsmspopupController;
import com.quickveggies.controller.popup.SendEmailData;
import com.quickveggies.dao.DatabaseClient;
import com.quickveggies.entities.Buyer;
import com.quickveggies.entities.Buyer.CreditPeriodSourceEnum;
import com.quickveggies.entities.DBuyerTableLine;
import com.quickveggies.entities.ExpenseInfo;
import com.quickveggies.entities.MoneyPaidRecd;
import com.quickveggies.entities.DBuyerTableList;
import com.quickveggies.entities.LadaanBijakSaleDeal;
import com.quickveggies.misc.DeleteTableButtonCell;
import com.quickveggies.misc.EditTableButtonCell;
import com.quickveggies.misc.MailTableButtonCell;
import com.quickveggies.misc.PrintTableButtonCell;
import com.quickveggies.misc.Utils;
import com.quickveggies.model.EntityType;
import java.util.HashMap;

import javafx.scene.control.ScrollBar;
import javafx.geometry.Orientation;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.converter.NumberStringConverter;

public class DBuyerController implements Initializable {

    private static final String RECEIVED = "Received from (Cr.)";

    private static final String PAID = "Paid to (Dr.)";

    private static final String NET_AMOUNT = "Net Amount";

    private static final String CASES = "Cases";

    @FXML
    private Label lblDueAmt;

    @FXML
    private Label dueAmt;

    @FXML
    private Label overdue;

    @FXML
    private Label lblPaid;

    @FXML
    private Label lblOverdueAmt;

    @FXML
    private Label Title;

    @FXML
    private ComboBox<String> batchActions;

    @FXML
    private TextField searchField;

    @FXML
    private Button newBuyer;

    @FXML
    private TableView<DBuyerTableLine> buyerDealsTable;
    
    @FXML
    private TableView<DBuyerTableLine> tableTotal;

    @FXML
    private Button btnDefault;

    @FXML
    private Button btnTimeline;

    @FXML
    private ComboBox<String> cboBuyerNew;

    @FXML
    private Button btnColSettings;

    @FXML
    private Button btnPrint;

    @FXML
    private Button btnExport;

    @FXML
    private Pane paneOverdue;

    @FXML
    private Pane paneDue;

    @FXML
    private Button btnSendSms;

    @FXML
    private DatePicker dtpDealDate;

    private final BooleanProperty isTimeLineView = new SimpleBooleanProperty(false);

    private final DatabaseClient dbclient = DatabaseClient.getInstance();

    private ObservableList<DBuyerTableLine> timelineViewBuyDeals;
    
    private Map<Integer, LadaanBijakSaleDeal> ladaanDeals = new HashMap<>();
    
    private final ObservableList<DBuyerTableLine> defaultViewBuyDeals =
            FXCollections.observableArrayList(
                    item -> new Observable[] { item.amountedTotalProperty(), item.casesProperty() });
    private final ObservableList<DBuyerTableLine> dueBuyerDeals = 
            FXCollections.observableArrayList(
                    item -> new Observable[] { item.amountedTotalProperty(), item.casesProperty() });
    private final ObservableList<DBuyerTableLine> overdueBuyerDeals =
            FXCollections.observableArrayList(
                    item -> new Observable[] { item.amountedTotalProperty(), item.casesProperty() });

    private final Map<String, Buyer> buyerMap = new LinkedHashMap<>();

    private final TableColumn actionCols = new TableColumn<>("Actions");

    public static final int REGULAR = 1, LADAAN_BIJAK = 2;

    private int tableMode = REGULAR;
    
    private boolean alreadySet;

    private final Map<String, TableColumn<?, ?>> columnNameMap = new LinkedHashMap<>();

    private final ObservableList<String> amtPaidRecdList = FXCollections.observableArrayList(new String[]{PAID, RECEIVED});
    
    private final ReadOnlyBooleanWrapper injected = new ReadOnlyBooleanWrapper(this, "injected", false);

    public DBuyerController(int tableMode) {
        this.tableMode = tableMode;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupBatchActions();
        
        String tmpText = "Buyer (Kharedar)";
        if (tableMode == LADAAN_BIJAK) 
        {
            tmpText = "Ladaan Bijak ";
        }
        final String titleText = tmpText;

        btnExport.setOnAction((event) -> {
            
            TableColumn<?, ?> tcAction = actionCols;
            boolean prevVisibility = tcAction.isVisible();
            tcAction.setVisible(false);
            String[][] tableData = TableUtil.toArray(buyerDealsTable);
            String fileName = FileUtil.getSaveToFileName(btnExport.getScene(), "Select Excel file",
                    FileUtil.getExcelExtMap());
            if (fileName != null) {
                try {
                    ExcelExportUtil.exportTableData(tableData, titleText + " Deal List", fileName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            tcAction.setVisible(prevVisibility);
        });
        btnPrint.setOnAction((event) -> TableUtil.printTable(buyerDealsTable, titleText + "Deal List", actionCols));
        dtpDealDate.setValue(LocalDate.now());

        final Pane pane = (Pane) btnColSettings.getParent().getParent();
        buyerDealsTable.getColumns().remove(actionCols);
        ListViewUtil.addColumnSettingsButtonHandler(buyerDealsTable, pane, btnColSettings);

        paneOverdue.setOnMouseClicked(genericBtnHandler(pane));
        paneDue.setOnMouseClicked(genericBtnHandler(pane));

        btnSendSms.setOnAction((ActionEvent event) -> {
            if (dtpDealDate.getValue() == null || dtpDealDate.getValue().isAfter(LocalDate.now())) {
                return;
            }
            List<SendsmspopupController.SendSmsBuyer> dealsForDate = new ArrayList<>();
            for (DBuyerTableLine tableLine : timelineViewBuyDeals) {
                LocalDate dealDate = Utils.toDate(tableLine.getDate());
                if (!dealDate.isEqual(dtpDealDate.getValue())) {
                    continue;
                }
                Buyer buyer = getBuyerInfo(tableLine);
                dealsForDate.add(SendsmspopupController.SendSmsBuyer.buildSendSmsBuyer(
                        buyer.getFirstName(), buyer.getLastName(), tableLine.getDate(),
                        tableLine.getAggregatedAmount(), buyer.getMobile()));
            }
            DashboardController.showPopup("/fxml/sendsmspopup.fxml", "SMS",
                    new SendsmspopupController(dealsForDate));
        });
        cboBuyerNew.setItems(amtPaidRecdList);
        cboBuyerNew.setOnAction(new EventHandler<ActionEvent>() {
            
            private final EntityType partyType = EntityType.BUYER;
            private AmountType amountType = null;

            @Override
            public void handle(ActionEvent event) {
                String value = cboBuyerNew.getValue();
                String title = null;
                switch (value) {
                    case PAID:
                        amountType = AmountType.PAID;
                        title = "Money Paid Entry";
                        break;
                    case RECEIVED:
                        amountType = AmountType.RECEIVED;
                        title = "Money Received Entry";
                }
                if (amountType != null) {
                    MoneyPaidRecdController controller = new MoneyPaidRecdController(partyType, amountType);
                    handleAddPaidRecdMoney("/fxml/moneypaid.fxml", title, controller);
                }
            }
        });
        buildLists();

        btnDefault.setOnAction((ActionEvent event) -> {
            isTimeLineView.set(false);
            batchActions.setDisable(true);
            btnDefault.setDisable(true);
            btnTimeline.setDisable(false);
            if (buyerDealsTable != null) {
                if (defaultViewBuyDeals.isEmpty()) {
                    return;
                }
                TableColumn<?, ?> casesCol = columnNameMap.get(CASES);
                casesCol.setText("Total Cases");
                TableColumn<?, ?> netAmtCol = columnNameMap.get(NET_AMOUNT);
                netAmtCol.setText("Total Net Amount");
                
                buyerDealsTable.setItems(defaultViewBuyDeals);
                if (buyerDealsTable.getColumns().contains(actionCols)) {
                    buyerDealsTable.getColumns().remove(actionCols);
                }
                setupTotalAmountsTable(defaultViewBuyDeals);
                buyerDealsTable.refresh();
                ListViewUtil.addColumnSettingsButtonHandler(buyerDealsTable, pane, btnColSettings);
            }
        });
        btnTimeline.setOnMouseClicked(genericBtnHandler(pane));

        buyerDealsTable.setEditable(true);
        TableColumn selectedCol = new TableColumn();
        CheckBox chkBox = new CheckBox();
        chkBox.setContentDisplay(null);
        selectedCol.setGraphic(chkBox);
        chkBox.selectedProperty().addListener((ObservableValue<? extends Boolean> observable,
                Boolean oldValue, Boolean newValue) -> {
            for (DBuyerTableLine line : buyerDealsTable.getItems()) {
                line.getIsSelected().set(newValue);
            }
        });
        selectedCol.setCellFactory(
                new Callback<TableColumn<DBuyerTableLine, Boolean>, CheckBoxTableCell<DBuyerTableLine, Boolean>>() {
            @Override
            public CheckBoxTableCell<DBuyerTableLine, Boolean> call(
                    TableColumn<DBuyerTableLine, Boolean> param) {
                return buildCheckboxColumn(buyerDealsTable);
            }
        });
        selectedCol.setEditable(true);
        TableColumn saleNoCol = new TableColumn("S.No.");
        saleNoCol.setCellValueFactory(
                new Callback<CellDataFeatures<DBuyerTableLine, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<DBuyerTableLine, String> p) {
                if (p.getValue().isTotalLine()) {
                    return new ReadOnlyObjectWrapper("Total");
                }
                return new ReadOnlyObjectWrapper(buyerDealsTable.getItems().indexOf(p.getValue()) + 1 + "");
            }
        });
        TableColumn dealIdCol = new TableColumn("Invoice No");
        dealIdCol.setCellValueFactory(new PropertyValueFactory<>("dealID"));
        dealIdCol.setCellFactory(TextFieldTableCell.forTableColumn());
        dealIdCol.setEditable(false);
        TableColumn buyerTitleCol = new TableColumn("Buyer");
        buyerTitleCol.setCellValueFactory(new PropertyValueFactory<>("buyerTitle"));
        buyerTitleCol.setCellFactory(TextFieldTableCell.forTableColumn());
        buyerTitleCol.setEditable(false);
        TableColumn dateCol = new TableColumn("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setCellFactory(TextFieldTableCell.forTableColumn());
        dateCol.setEditable(false);
        TableColumn totalSumCol = new TableColumn("Net Amt");
        totalSumCol.setCellValueFactory(new PropertyValueFactory<>("amountedTotal"));
        totalSumCol.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter()));
//        totalSumCol.setCellFactory(column -> {
//            return new TableCell<DBuyerTableLine, String>() {
//                @Override
//                protected void updateItem(String item, boolean empty) {
//                    super.updateItem(item, empty);
//                    if (item == null || empty) {
//                        setText(null);
//                    }
//                    else {
//                        setText(item);
//                    }
//                }
//            };
//        });
        totalSumCol.setEditable(false);
        TableColumn casesCol = new TableColumn(CASES);
        casesCol.setCellValueFactory(new PropertyValueFactory<>("cases"));
        casesCol.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter()));
        casesCol.setEditable(false);

        TableColumn mailCol = new TableColumn();
        mailCol.setCellValueFactory(new PropertyValueFactory<>("saleNo"));
        mailCol.setCellFactory(new Callback<TableColumn<DBuyerTableLine, String>, TableCell<DBuyerTableLine, String>>() {
            @Override
            public TableCell<DBuyerTableLine, String> call(TableColumn<DBuyerTableLine, String> param) {

                MailTableButtonCell<DBuyerTableLine, String> cell = new MailTableButtonCell<>(
                        (Integer index)->{
                            //Send SMS message
                            List<SendsmspopupController.SendSmsBuyer> dealsForDate = new ArrayList<>();
                            DBuyerTableLine currDeal = buyerDealsTable.getItems().get(index);
                            Buyer buyer = getBuyerInfo(currDeal);
                            dealsForDate.add(SendsmspopupController.SendSmsBuyer.buildSendSmsBuyer(
                                    buyer.getFirstName(), buyer.getLastName(), currDeal.getDate(),
                                    currDeal.getAggregatedAmount(), buyer.getMobile()));
                            DashboardController.showPopup("/fxml/sendsmspopup.fxml", "SMS",
                                    new SendsmspopupController(dealsForDate));
                        },
                        (Integer index)->{
                            //Send Email Message
                            DBuyerTableLine currDeal = buyerDealsTable.getItems().get(index);
                            String email = getBuyerInfo(currDeal).getEmail();
                            List<SendEmailData> emails = new ArrayList<>();
                            emails.add(SendEmailData.buildSendEmailData(email,
                                    UserGlobalParameters.userEmail, prepareInvPdf(currDeal), "", ""));
                            SendEmailsPopupController.showSendEmailsPopup(emails);
                        }
                );
                return cell;
            }
        });
        TableColumn editCol = new TableColumn();
        editCol.setCellValueFactory(new PropertyValueFactory<>("saleNo"));
        editCol.setCellFactory(
                new Callback<TableColumn<DBuyerTableLine, String>, TableCell<DBuyerTableLine, String>>() {
            @Override
            public TableCell<DBuyerTableLine, String> call(TableColumn<DBuyerTableLine, String> param) {
                return new EditTableButtonCell("DBuyerTableLine", () -> {
                    //Todo: fix calculations
                    buildLists();
                    calculateDues();
                });
            }
        });
        TableColumn<DBuyerTableLine, String> printCol = new TableColumn<>();
        printCol.setCellValueFactory(new PropertyValueFactory<>("saleNo"));
        printCol.setCellFactory(
                new Callback<TableColumn<DBuyerTableLine, String>, TableCell<DBuyerTableLine, String>>() {
            @Override
            public TableCell<DBuyerTableLine, String> call(TableColumn<DBuyerTableLine, String> param) {
                PrintTableButtonCell<DBuyerTableLine, String> pTabBtn = new PrintTableButtonCell<DBuyerTableLine, String>() {
                    String lineNum = null;

                    @Override
                    public void updateItem(String item, boolean isEmpty) {
                        super.updateItem(item, isEmpty);
                        lineNum = item;

                        this.getPrintButton().setOnMouseClicked((event) -> {
                            if (!event.getButton().equals(MouseButton.PRIMARY)) {
                                return;
                            }
                            DBuyerTableLine currDeal = buyerDealsTable.getItems().stream().filter(buyerDeal -> {
                                if (buyerDeal.getSaleNo().equals(lineNum)) {
                                    return true;
                                }
                                return false;
                            }).collect(Collectors.toList()).get(0);
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
        deleteCol.setCellValueFactory(new PropertyValueFactory<>("saleNo"));
        deleteCol.setCellFactory(
                new Callback<TableColumn<DBuyerTableLine, String>, TableCell<DBuyerTableLine, String>>() {
            @Override
            public TableCell<DBuyerTableLine, String> call(TableColumn<DBuyerTableLine, String> param) {
                return new DeleteTableButtonCell("buyerDeals", "id");
            }
        });
        actionCols.getColumns().setAll(Arrays.asList(new TableColumn[]{mailCol, editCol, printCol, deleteCol}));
        if (tableMode == REGULAR) {
            buyerDealsTable.getColumns().setAll(selectedCol, saleNoCol, dateCol,
                    dealIdCol, buyerTitleCol, totalSumCol, casesCol, actionCols);
        }
        else { // the buyer is ladaan/bijak
            Title.setText("Ladaan/Bijak");
            TableColumn ladanEditCol = new TableColumn<>();
            ladanEditCol.setCellValueFactory(
                    new Callback<CellDataFeatures<DBuyerTableLine, Boolean>, ObservableValue<Boolean>>() {
                @Override
                public ObservableValue<Boolean> call(CellDataFeatures<DBuyerTableLine, Boolean> param) {
                    return new ReadOnlyBooleanWrapper(param.getValue().isLadaanEdited());
                }
            });
            ladanEditCol.setCellFactory(
                    new Callback<TableColumn<DBuyerTableLine, Boolean>, TableCell<DBuyerTableLine, Boolean>>() {
                @Override
                public TableCell call(TableColumn<DBuyerTableLine, Boolean> param) {
                    return new TableCell<DBuyerTableLine, Boolean>() {
                        @Override
                        protected void updateItem(Boolean item, boolean empty) {
                            super.updateItem(item, empty);
                            // System.out.println("Item :" + item);
                            if (item != null) {
                                if (item == false) {
                                    BackgroundImage backgroundImage = new BackgroundImage(
                                            new Image(getClass().getResource("/icons/exclamation_mark.png")
                                                    .toExternalForm(), 23, 23, true, true),
                                            BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                                            BackgroundPosition.CENTER, BackgroundSize.DEFAULT);
                                    Background background = new Background(backgroundImage);
                                    this.setBackground(background);
                                    this.setTooltip(new Tooltip(
                                            "This Ladaan/Bijak entry is not edited yet, please click on edit button to start editing it"));
                                }
                                else {
                                    BackgroundImage backgroundImage = new BackgroundImage(
                                            new Image(getClass().getResource("/icons/check-icon.png")
                                                    .toExternalForm(), 23, 23, true, false),
                                            BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                                            BackgroundPosition.CENTER, BackgroundSize.DEFAULT);
                                    Background background = new Background(backgroundImage);
                                    this.setBackground(background);
                                    this.setTooltip(new Tooltip(
                                            "This Ladaan/Bijak entry is fulfilled"));
                                }
                            }
                        }
                    };
                }
            });
            ladanEditCol.setPrefWidth(35);
            TableColumn buyerTypeCol = new TableColumn("Type");
            buyerTypeCol.setCellValueFactory(new PropertyValueFactory<>("buyerType"));
            buyerTypeCol.setCellFactory(TextFieldTableCell.forTableColumn());
            buyerTypeCol.setEditable(false);
            actionCols.getColumns().setAll(mailCol, editCol, printCol, deleteCol, ladanEditCol);
            buyerDealsTable.getColumns().setAll(selectedCol, saleNoCol, dealIdCol, buyerTitleCol, buyerTypeCol,
                    totalSumCol, casesCol, actionCols);
        }
        columnNameMap.put(CASES, casesCol);
        columnNameMap.put(NET_AMOUNT, totalSumCol);
        buyerDealsTable.getItems().addAll(timelineViewBuyDeals);

        newBuyer.setOnAction((ActionEvent event) -> {
            showNewBuyerDialog();
        });
        performPostInit();
        setupTotalAmountsTable(timelineViewBuyDeals);
    }
    
    private void setupTotalAmountsTable(final ObservableList<DBuyerTableLine> list) {
        //Setup total amounts table
        tableTotal.getColumns().clear();
        for (TableColumn column : buyerDealsTable.getColumns()) {
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
        buyerDealsTable.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            //Run after initialization to get controls
            for (Node bar1 : buyerDealsTable.lookupAll(".scroll-bar")) {
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

    private EventHandler<MouseEvent> genericBtnHandler(final Pane pane) {
        return (MouseEvent event) -> {
            if (!event.getButton().equals(MouseButton.PRIMARY)) {
                return;
            }
            batchActions.setDisable(false);
            btnDefault.setDisable(false);
            ObservableList<DBuyerTableLine> list = null;
            String compId = ((Node) event.getSource()).getId();
            if (compId == null) {
                return;
            }
            switch (compId) {
                case "paneDue":
                    list = dueBuyerDeals;
                    btnTimeline.setDisable(false);
                    break;
                    
                case "paneOverdue":
                    list = overdueBuyerDeals;
                    btnTimeline.setDisable(false);
                    break;
                    
                case "btnTimeline":
                    list = timelineViewBuyDeals;
                    btnTimeline.setDisable(true);
                    break;
            }
            isTimeLineView.set(true);
            if (buyerDealsTable != null) {
                TableColumn<?, ?> casesCol = columnNameMap.get(CASES);
                casesCol.setText(CASES);
                TableColumn<?, ?> netAmtCol = columnNameMap.get(NET_AMOUNT);
                netAmtCol.setText(NET_AMOUNT);
                if (!buyerDealsTable.getColumns().contains(actionCols)) {
                    buyerDealsTable.getColumns().add(actionCols);
                }
                buyerDealsTable.setItems(list);
                setupTotalAmountsTable(timelineViewBuyDeals);
                buyerDealsTable.refresh();
                ListViewUtil.addColumnSettingsButtonHandler(buyerDealsTable, pane, btnColSettings);
            }
        };
    }

    public static void showNewBuyerDialog() {
        final Stage addBuyer = new Stage();
        addBuyer.centerOnScreen();
        addBuyer.setTitle("Add new Buyer/Kharedar");
        addBuyer.initModality(Modality.APPLICATION_MODAL);
        addBuyer.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent event) {
                Main.getStage().getScene().getRoot().setEffect(null);
            }
        });
        try {
            Parent parent = FXMLLoader.load(DBuyerController.class.getResource("/fxml/buyeradd.fxml"));
            Scene scene = new Scene(parent, 687, 400);
            scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
                public void handle(KeyEvent event) {
                    if (event.getCode() == KeyCode.ESCAPE) {
                        Main.getStage().getScene().getRoot().setEffect(null);
                        addBuyer.close();
                    }
                }
            });
            addBuyer.setScene(scene);
            addBuyer.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void performPostInit() {
        actionCols.visibleProperty().bindBidirectional(isTimeLineView);
        calculateDues();
        calculatePaidAmount();
        btnDefault.fire();
    }

    private void buildLists() {
        //Update lines from sql database
        try {
            // actual data list. Use an extractor so the "total" line can observe changes:
            timelineViewBuyDeals = FXCollections.observableArrayList(
                    (item -> new Observable[] { item.amountedTotalProperty(), item.casesProperty() }));
            timelineViewBuyDeals.clear();
            defaultViewBuyDeals.clear();
            List<DBuyerTableLine> lines2 = dbclient.getBuyerDealEntries(null,
                    new String[] { COLD_STORE_BUYER_TITLE, GODOWN_BUYER_TITLE });
            for (DBuyerTableLine line : lines2) {
                line.setAmountedTotal(line.getAggregatedAmount());
                String buyerType = dbclient.getBuyerByName(line.getBuyerTitle()).getBuyerType();
                
                if ((tableMode == REGULAR && buyerType.equalsIgnoreCase("regular"))
                        || (tableMode == LADAAN_BIJAK && (!buyerType.equalsIgnoreCase("regular")))) {
                    timelineViewBuyDeals.add(line);
                }
            }
        } catch (SQLException e) {
            System.out.print("sqlexception while fetching buyer deal entries from sql");
            e.printStackTrace();
        }
        //Group list by deal ID
        groupListByDeal();
        
        if (timelineViewBuyDeals != null && !timelineViewBuyDeals.isEmpty()) {
            buildDefaultViewList();
        }
    }
    
    private void groupListByDeal() {
        ladaanDeals.clear();
        Map<String, DBuyerTableLine> map = new LinkedHashMap<>();
        for (DBuyerTableLine srcLine : timelineViewBuyDeals) {
            DBuyerTableLine tl = map.get(srcLine.getDealID() + srcLine.getBuyerTitle());
            if (tl == null) {
                tl = new DBuyerTableLine(srcLine.getAll());
                map.put(tl.getDealID() + srcLine.getBuyerTitle(), tl);
                //Get ladaan/bijak deal
                LadaanBijakSaleDeal ladBijDeal = dbclient.getLadBijSaleDeal(Integer.valueOf(tl.getDealID()));
                ladaanDeals.put(Integer.valueOf(tl.getDealID()), ladBijDeal);
                if (ladBijDeal != null) {
                    Integer  aggAmt = toInt(ladBijDeal.getAggregatedAmount());
                    Integer amt = aggAmt - (Integer.valueOf(ladBijDeal.getFreight())
                            * Integer.valueOf(ladBijDeal.getCases())
                            + Integer.valueOf(ladBijDeal.getComission()) * aggAmt / 100);
                    Integer netAmount = amt - Integer.valueOf(tl.getAggregatedAmount());
                    ladBijDeal.setAmountedTotal(String.valueOf(netAmount));
                    netAmount = Integer.valueOf(ladBijDeal.getAmountedTotal())
                            + tl.getAmountedTotal();
                    tl.setAmountedTotal(String.valueOf(netAmount));
                }
            }
            else {
                Integer destAggAmt = isEmptyString(tl.getAggregatedAmount()) ? 0 : toInt(tl.getAggregatedAmount());
                Integer srcAggAmt = isEmptyString(srcLine.getAggregatedAmount()) ? 0
                        : toInt(srcLine.getAggregatedAmount());
                tl.setAggregatedAmount(toStr(destAggAmt + srcAggAmt));
                Integer destRecAmt = isEmptyString(tl.getAmountReceived()) ? 0 : toInt(tl.getAmountReceived());
                Integer srcRecAmt = isEmptyString(srcLine.getAmountReceived()) ? 0 : toInt(srcLine.getAmountReceived());
                Integer destAmtTot = tl.getAmountedTotal();
                Integer srcAmtTot = srcLine.getAmountedTotal();
                tl.setAmountReceived(toStr(srcRecAmt + destRecAmt));
                tl.setAmountedTotal(toStr(srcAmtTot + destAmtTot));
                Integer destCases = tl.getCases();
                Integer srcCases = srcLine.getCases();
                tl.setCases(toStr(destCases + srcCases));
                tl.setDate(DateUtil.getNewerDateString(tl.getDate(), srcLine.getDate()));
            }
        }
        //Update grouped lines
        timelineViewBuyDeals.clear();
        for (DBuyerTableLine newDLine : map.values()) {
            timelineViewBuyDeals.add(newDLine);
        }
    }

    private void buildDefaultViewList() {
        Map<String, DBuyerTableLine> map = new LinkedHashMap<>();
        for (DBuyerTableLine srcLine : timelineViewBuyDeals) {
            DBuyerTableLine tl = map.get(srcLine.getBuyerTitle());
            if (tl == null) {
                tl = new DBuyerTableLine(srcLine.getAll());
                tl.setSaleNo("--");
                tl.setDealID("NA");
                map.put(tl.getBuyerTitle(), tl);
            }
            else {
                Integer destAggAmt = isEmptyString(tl.getAggregatedAmount()) ? 0 : toInt(tl.getAggregatedAmount());
                Integer srcAggAmt = isEmptyString(srcLine.getAggregatedAmount()) ? 0
                        : toInt(srcLine.getAggregatedAmount());
                tl.setAggregatedAmount(toStr(destAggAmt + srcAggAmt));
                Integer destRecAmt = isEmptyString(tl.getAmountReceived()) ? 0 : toInt(tl.getAmountReceived());
                Integer srcRecAmt = isEmptyString(srcLine.getAmountReceived()) ? 0 : toInt(srcLine.getAmountReceived());
                Integer destAmtTot = tl.getAmountedTotal();
                Integer srcAmtTot = srcLine.getAmountedTotal();
                tl.setAmountReceived(toStr(srcRecAmt + destRecAmt));
                tl.setAmountedTotal(toStr(srcAmtTot + destAmtTot));
                Integer destCases = tl.getCases();
                Integer srcCases = srcLine.getCases();
                tl.setCases(toStr(destCases + srcCases));
                tl.setDate(DateUtil.getNewerDateString(tl.getDate(), srcLine.getDate()));
            }
        }
        for (DBuyerTableLine newDLine : map.values()) {
            defaultViewBuyDeals.add(newDLine);
        }
    }

    private void handleAddPaidRecdMoney(String resource, String title, Object controller) {
        try {
            final Stage stage = new Stage();
            stage.centerOnScreen();
            stage.setTitle(title);
            stage.initModality(Modality.APPLICATION_MODAL);
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(resource));
                loader.setController(controller);
                Scene scene = new Scene((Parent) loader.load());

                scene.setOnKeyPressed((KeyEvent event) -> {
                    if (event.getCode() == KeyCode.ESCAPE) {
                        Main.getStage().getScene().getRoot().setEffect(null);
                        stage.close();
                    }
                });
                EventHandler<WindowEvent> wEvent = (WindowEvent event) -> {
                    buyerDealsTable.refresh();
                    buildLists();
                    performPostInit();
                };
                stage.setOnCloseRequest(wEvent);
                stage.setOnHiding(wEvent);
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Builds a column with check boxes
     */
    private CheckBoxTableCell<DBuyerTableLine, Boolean> buildCheckboxColumn(
            final TableView<DBuyerTableLine> tableView) {
        CheckBoxTableCell<DBuyerTableLine, Boolean> cell = new CheckBoxTableCell<>(
                new Callback<Integer, ObservableValue<Boolean>>() {
            @Override
            public ObservableValue<Boolean> call(final Integer index) {
                BooleanProperty selected = tableView.getItems().get(index).getIsSelected();
                selected.addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> obs, Boolean wasSelected,
                            Boolean isSelected) {
                        DBuyerTableLine item = tableView.getItems().get(index);
                        item.getIsSelected().set(isSelected);
                    }
                });
                return selected;
            }
        });
        return cell;
    }

    private void setupBatchActions() {
        batchActions.setDisable(true);
        ObservableList<String> actions = FXCollections.observableArrayList(
                new String[] { "Batch Actions", "SMS", "EMAIL", "PRINT" });
        batchActions.setItems(actions);
        batchActions.valueProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            boolean processed = false;
            if ("SMS".equals(newValue)) {
                //Send SMS message
                List<SendsmspopupController.SendSmsBuyer> dealsForDate = new ArrayList<>();
                for (DBuyerTableLine line : buyerDealsTable.getItems()) {
                    if (line.getIsSelected().get()) {
                        Buyer buyer = getBuyerInfo(line);
                        dealsForDate.add(SendsmspopupController.SendSmsBuyer.buildSendSmsBuyer(
                                buyer.getFirstName(), buyer.getLastName(), line.getDate(),
                                line.getAggregatedAmount(), buyer.getMobile()));
                    }
                }
                processed = true;
                if (dealsForDate.size() > 0) {
                    DashboardController.showPopup("/fxml/sendsmspopup.fxml", "SMS",
                            new SendsmspopupController(dealsForDate));
                }
            }
            else if ("EMAIL".equals(newValue)) {
                //Prepare and Send Email Messages
                List<SendEmailData> emails = new ArrayList<>();
                for (DBuyerTableLine line : buyerDealsTable.getItems()) {
                    if (line.getIsSelected().get()) {
                        String email = getBuyerInfo(line).getEmail();
                        emails.add(SendEmailData.buildSendEmailData(email,
                                UserGlobalParameters.userEmail, "", "", prepareInvPdf(line)));
                    }
                }
                processed = true;
                if (emails.size() > 0) {
                    SendEmailsPopupController.showSendEmailsPopup(emails);
                }
            }
            else if ("PRINT".equals(newValue)) {
                List<String> fileNames = new ArrayList<>();
                for (DBuyerTableLine line : buyerDealsTable.getItems()) {
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
        int overDues = 0;
        int dues = 0;
        int overdueCount = 0;
        int dueCount = 0;
        Map<String, List<MoneyPaidRecd>> buyerMprListMap = new LinkedHashMap<>();
        Map<String, Buyer> buyerTitleMap = new LinkedHashMap<>();
        dueBuyerDeals.clear();
        overdueBuyerDeals.clear();
        for (DBuyerTableLine tl : timelineViewBuyDeals) {
            String title = tl.getBuyerTitle();
            try {
                Buyer buyer = buyerTitleMap.get(title);
                if (buyer == null) {
                    buyer = dbclient.getBuyerByName(title);
                    buyerTitleMap.put(title, buyer);
                }
                String creditLimit = buyer.getCreditPeriod();
                CreditPeriodSourceEnum en = CreditPeriodSourceEnum.getEnumForTitle(creditLimit);
                int creditTime = 30;
                if (en == null) {
                    System.err.println(String.format("Unknown credit period found:%s , using default as:%s",
                            creditLimit, CreditPeriodSourceEnum.DAY_30.getTitle()));
                } else {
                    creditTime = en.getIntValue();
                }
                //calculate over dues as per credit period
                int amtPaidInPeriod = 0;
                List<MoneyPaidRecd> currBuyerMprList = buyerMprListMap.get(title);
                if (currBuyerMprList == null) {
                    currBuyerMprList = dbclient.getMoneyPaidRecdList(title, EntityType.BUYER);
                    buyerMprListMap.put(title, currBuyerMprList);
                }
                LocalDate dealDate = toDate(tl.getDate()).minusDays(1);
                for (MoneyPaidRecd mpr : currBuyerMprList) {
                    if (isNumStrEmpty(mpr.getReceived())) {
                        continue;
                    }
                    LocalDate paymentDate = toDate(mpr.getDate());
                    if (paymentDate.isAfter(dealDate)) {
                        amtPaidInPeriod += toInt(mpr.getReceived());
                    }
                }
                Integer totalDealAmount = tl.amountedTotalProperty() == null ?
                        toInt(tl.getAggregatedAmount()) : tl.getAmountedTotal();
//                Integer totalDealAmount = toInt(tl.getAggregatedAmount());
                if (amtPaidInPeriod < totalDealAmount) {
                    Integer pendingAmt = totalDealAmount - amtPaidInPeriod;
                    if (isUnderDateRange(tl.getDate(), creditTime)) {
                        dueCount++;
                        dues += pendingAmt;
                        dueBuyerDeals.add(tl);
                    } else {
                        overdueCount++;
                        overDues += pendingAmt;
                        overdueBuyerDeals.add(tl);
                    }
                }

            } catch (NoSuchElementException | SQLException e) {
                e.printStackTrace();
            }
        }
        lblOverdueAmt.setText(toStr(overDues));
        overdue.setText(overdueCount + " Over Due(s)");
        lblDueAmt.setText(toStr(dues));
        dueAmt.setText(dueCount + " Due(s)");
    }

    private void calculatePaidAmount() {
        double totalPaid = 0d;// total money paid by user in 30 days
        List<MoneyPaidRecd> allPartyMprList = dbclient.getAllMoneyPaidRecdList(EntityType.BUYER);

        /* calculate paid amount in last 30 days */
        for (MoneyPaidRecd mpr : allPartyMprList) {
            if (isNumStrEmpty(mpr.getReceived())) {
                continue;
            }
            LocalDate srcDate = LocalDate.now().minusDays(30);
            LocalDate transDate = toDate(mpr.getDate());
            if (transDate.isAfter(srcDate)) {
                totalPaid += toInt(mpr.getReceived());
            }
        }
        lblPaid.setText(toStr((long) totalPaid));

    }

    public static String[][] buildBuyerInvoicePdf(DBuyerTableLine line,
            List<DBuyerTableLine> buyerDeals, List<ExpenseInfo> buyerExpenses) {
        int recCount = buyerDeals.size();
        String[][] invArr = new String[recCount + 1][4];
        invArr[0] = new String[]{"Size", "Cases", "Rates", "Amount"};
        Integer[][] caseRateArr = new Integer[recCount][2];
        List<List<String>> table = new ArrayList<>();
        AtomicInteger rowCount = new AtomicInteger(0);
        final AtomicInteger baseSum = new AtomicInteger(0);
        final AtomicInteger caseCount = new AtomicInteger(0);
        buyerDeals.stream().forEach((t) -> {
            int i = rowCount.incrementAndGet();
            Integer cases = t.getCases();
            Integer rate = Utils.toInt(t.getBuyerRate());
            invArr[i] = new String[]{ t.getBoxSizeType(), cases.toString(),
                rate.toString(), String.valueOf(Integer.valueOf(t.getBuyerRate()) * t.getCases()) };
            caseRateArr[i - 1][0] = cases;
            caseRateArr[i - 1][1] = rate;
            table.add(Arrays.asList(invArr[i]));
            baseSum.addAndGet(Integer.valueOf(t.getBuyerRate()) * t.getCases());
            caseCount.addAndGet(cases);
        });
        List<Integer> expenseAmtList = new ArrayList<>();
        AtomicInteger j = new AtomicInteger(0);
        List<Integer> fee = new ArrayList<>();
        for (Integer[] crArr : caseRateArr) {
            AtomicInteger count = new AtomicInteger(0);
            buyerExpenses.stream().forEach((t) -> {
                int i = count.incrementAndGet();
                String type = t.getType();
                Integer totAmount = 0;
                int crArrIdx = j.incrementAndGet();
                Integer rate = crArr[1];
                Integer cases = crArr[0];
                int amount = Integer.parseInt(t.getDefaultAmount());
                if (type.trim().equals("@")) {
                    totAmount = amount * cases;
                }
                else if (type.trim().equals("%")) {
                    double percent = amount * cases * rate / 100.0;
                    fee.add((int)percent);
                    totAmount = rate * cases * amount / 100;
                }
                expenseAmtList.add(totAmount);
                table.add(Arrays.asList(new String[]{"", "", t.getName(), totAmount.toString()}));
            });
        }
        Integer totalAmt = expenseAmtList.stream().mapToInt(i -> i).sum();
        Integer fees = fee.stream().mapToInt(i -> i).sum();
        table.add(Arrays.asList(new String[]{"", "", "TOTAL AMOUNT", String.valueOf(baseSum.get() + totalAmt)}));
        table.add(Arrays.asList(new String[]{"", caseCount.toString(), "", (baseSum.get() + totalAmt) + ""}));
        String[][] tmpArr = new String[table.size() + 1][4];
        tmpArr[0] = invArr[0];
        for (int i = 0; i < table.size(); i++) {
            tmpArr[i + 1] = table.get(i).toArray(new String[table.get(i).size()]);
        }
        return tmpArr;
    }

    private Buyer getBuyerInfo(DBuyerTableLine dealLine) {
        String buyerTitle = dealLine.getBuyerTitle();
        if (!buyerMap.containsKey(buyerTitle)) {
            try {
                Buyer buyer = dbclient.getBuyerByName(buyerTitle);
                buyerMap.put(buyerTitle, buyer);
            } catch (NoSuchElementException | SQLException e) {
                e.printStackTrace();
            }
        }
        Buyer buyer = buyerMap.get(buyerTitle);
        return buyer;
    }

    private String prepareInvPdf(DBuyerTableLine currDeal) {
        String dealId = currDeal.getDealID();
        List<DBuyerTableLine> buyerDeals = timelineViewBuyDeals.stream()
                .filter((test) -> test.getDealID().equals(dealId)
                        && test.getBuyerTitle().equals(currDeal.getBuyerTitle()))
                .collect(Collectors.toList());
        List<ExpenseInfo> buyerExpenses = dbclient.getBuyerExpenseInfoList();
        String[][] dataArr = buildBuyerInvoicePdf(currDeal, buyerDeals, buyerExpenses);
        Buyer buyer = getBuyerInfo(currDeal);
        String buyerName = currDeal.getBuyerTitle();
        if (buyer != null) {
            buyerName = buyer.getFirstName() + " " + buyer.getLastName();
        }
        String pdfFile = PDFUtil.buildBuyerInvoicePdf(currDeal.getBuyerTitle(),
                buyerName, currDeal.getDealID(), currDeal.getDate(), 0, dataArr);
        return pdfFile;
    }
}
