package com.quickveggies.controller;

import static java.awt.event.KeyEvent.VK_CAPS_LOCK;

import java.awt.Robot;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import com.quickveggies.GeneralMethods;
import com.quickveggies.UserGlobalParameters;
import com.quickveggies.controller.FreshEntryTableData.AddLotTableLine;
import com.quickveggies.controller.FreshEntryTableData.BuyerEntryTableLine;
import com.quickveggies.controller.FreshEntryTableData.GrowerEntryTableLine;
import com.quickveggies.dao.DatabaseClient;
import com.quickveggies.entities.BoxSize;
import com.quickveggies.entities.Buyer;
import com.quickveggies.entities.ExpenseInfo;
import com.quickveggies.entities.QualityType;
import com.quickveggies.entities.PartyType;
import com.quickveggies.misc.AutoCompleteTableCell;
import com.quickveggies.misc.AutoCompleteTextField;
import com.quickveggies.misc.CustomComboboxTableCell;
import com.quickveggies.misc.PartySearchTableCell;
import com.quickveggies.misc.SearchPartyButton;
import com.quickveggies.misc.Utils;
import com.quickveggies.model.LotTableColumnNameEnum;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewFocusModel;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;

public class FreshEntryController implements Initializable {

    private static final String STR_ADD_NEW = "Add new...";

    private static final String GROWER_QTY_COL_NAME = "Quantity";

    private TableColumn<?, String> nextTabColumnToFocus;

    @FXML
    private TextField grNo;

    @FXML
    private TextArea expenses;

    @FXML
    private AutoCompleteTextField grower;

    @FXML
    private TextField truckNo;

    @FXML
    private TextField driverNoField;

    @FXML
    private TextField chlNo;

    @FXML
    private AutoCompleteTextField fwAgent;

    @FXML
    private TextField totalQty;

    @FXML
    private TextField totalQty2;

    @FXML
    private TextField gross;

    @FXML
    private TextField halfCase;

    @FXML
    private TextField remarks;

    @FXML
    private TextField charges;

    @FXML
    private TextField net;

    @FXML
    private DatePicker date;

    @FXML
    private TextField fullCase;

    @FXML
    private CheckBox display;

    @FXML
    private SearchPartyButton growerSearch;
    @FXML
    private SearchPartyButton agentSearch;

    @FXML
    private TableView<AddLotTableLine> addLots;

    @FXML
    private TableView<GrowerEntryTableLine> growersEntry;

    @FXML
    private TableView<BuyerEntryTableLine> buyersEntry;

    @FXML
    private ChoiceBox<String> fruitSelection;

    @FXML
    private Button saveButton;

    @FXML
    private Pane parentPane;

    private final String defaultComboBgColor = new ComboBox<>().getStyle();

    private final static List<ExpenseInfo> expenseList = DatabaseClient.getInstance().getBuyerExpenseInfoList();

    private Parent parent;
    private static Stage chargesView;
    private ObservableList<Control> traversibleNodes = null;

    private final SessionDataController sessionController = SessionDataController.getInstance();

    private final ArrayList<TextFieldTableCell<?, ?>> growerFruitColList = new ArrayList<>();
    private final ArrayList<TextFieldTableCell<?, ?>> growerQualityColList = new ArrayList<>();
    private final ArrayList<CustomComboboxTableCell<?, ?>> growerBoxSizeColList = new ArrayList<>();
    private final ArrayList<TextFieldTableCell<?, ?>> buyerBoxSizeColList = new ArrayList<>();
    private final ArrayList<CustomComboboxTableCell<?, ?>> lotQualityColList = new ArrayList<>();
    private final ArrayList<TextFieldTableCell<?, ?>> lotQtyColList = new ArrayList<>();

    public static int REGULAR = 0, STORAGE = 1;
    private final String[] freshEntryTypes = new String[] { "Regular", "Storage" };

    private int dealID = 0;
    private int freshEntryType;
    private int currentTraversedNodeIdx;

    private boolean shouldFireTabKey = false;
    private boolean reOpenComboBox = false;

    public FreshEntryController(int freshEntryType) {
        this.freshEntryType = freshEntryType;
    }

    // GLOBAL OBJECTS FOR THIS CLASS
    // ------------------------------------
    javafx.collections.ObservableList<String> qualityTypesList = FXCollections
            .observableArrayList(UserGlobalParameters.kinnowMangoQualitiesList);

    final javafx.collections.ObservableList<String> fruitTypesList = FXCollections
            .observableArrayList(new String("Mango"), new String("Kinnow"), new String("Apple"));

    javafx.collections.ObservableList<String> boxSizesList = FXCollections
            .observableArrayList(UserGlobalParameters.boxSizes);

    final FreshEntryTableData data = new FreshEntryTableData();

    public ObservableList<FreshEntryTableData.AddLotTableLine> addLotLines = FXCollections
            .observableArrayList(data.getAddLotTableLine("", "0", ""));

    public ObservableList<FreshEntryTableData.GrowerEntryTableLine> growerLines = FXCollections.observableArrayList();
    // .observableArrayList(data.getGrowerEntryTableLine("", "0", "0", ""));

    public ObservableList<FreshEntryTableData.BuyerEntryTableLine> buyerLines = FXCollections.observableArrayList();
    // .observableArrayList(data.getBuyerEntryTableLine("", "0", "0", ""));

    private DatabaseClient dbclient = DatabaseClient.getInstance();
    java.util.TreeSet<String> growersList = null;
    java.util.TreeSet<String> buyersList = null;

    private int totalLotQty = 0;

    private final int LOT = 1, GROWERS = 2, BUYERS = 3;
    private int currTable = LOT;
    // --------------------------------------

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void initialize(final URL location, final ResourceBundle resources) {

        try {
            if (!Toolkit.getDefaultToolkit().getLockingKeyState(VK_CAPS_LOCK)) {
                Toolkit.getDefaultToolkit().setLockingKeyState(VK_CAPS_LOCK, true);
            }
        }
        catch (Exception ex) {
            System.err.println("WARNING:".concat(ex.getMessage()));
        }
        traversibleNodes = FXCollections.observableArrayList(grNo, date, fruitSelection, chlNo, grower, totalQty,
                fullCase, halfCase, fwAgent, truckNo, driverNoField, remarks, display, addLots, growersEntry,
                buyersEntry);

        try {
            parent = FXMLLoader.load(getClass().getResource("/fxml/addcharges.fxml"));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        sessionController.setCharges(null); // reset supplier charges
        FreshEntryTableData.buyerEntryLinesSql = new ArrayList<>();
        FreshEntryTableData.growerEntryLinesSql = new ArrayList<>();

        ChangeListener<Boolean> dateFocusChangeListener = (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
                date.show();
            }
        };
        date.focusedProperty().addListener(dateFocusChangeListener);
        /* There is some issue with Date picker control, when it gets focus and
         * is expanded, the month component of data picker control gets focused
         * and enter key doesn't work, so we are calling arrow key event, to
         * shift the focus from month component to date component, now this sets
         * the focus to date, and enter key works as expected
         */
        date.setOnShown((Event event) -> {
            fireKeyPress(java.awt.event.KeyEvent.VK_DOWN);
        });
        date.setValue(LocalDate.now());

        SessionDataController.getInstance().linkController(this);
        // SUPPLIER ID SECTION
        // set fruit selection:
        List<String> frNames = dbclient.getAllFruitTypes();
        fruitTypesList.clear();
        if (!frNames.isEmpty()) {
            fruitTypesList.addAll(frNames);
            fruitSelection.setItems(fruitTypesList);
            fruitSelection.setValue(fruitTypesList.get(0));
            resetFruitQualsAndBoxes();
        } else {
            fruitSelection.setDisable(true);
            GeneralMethods.errorMsg("No fruit is added, please add fruits from settings");
            saveButton.setDisable(true);
            return;
        }
        fruitSelection.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                resetFruitQualsAndBoxes();
            }
        });
        growersList = updateGrowersList();
        grower.setEntries(growersList);
        // grower.setLinkedTextFields(new TextField[] { grNo });
        grower.setLinkedFieldsReturnType(AutoCompleteTextField.ENTRY_IND);
        grower.linkToWindow(FreshEntryController.this, "/fxml/supplieradd.fxml", "Add new supplier", STR_ADD_NEW,
                new AddSupplierController());
        grower.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue,
                    Boolean newValue) {
                grower.getMenu().hide();
                growersList = updateGrowersList();
                grower.setEntries(growersList);
            }
        });
        growerSearch.setPartyType(PartyType.SUPPLIERS);
        growerSearch.setLinkedObject(grower);

        fwAgent.setEntries(growersList);
        fwAgent.linkToWindow(FreshEntryController.this, "/fxml/supplieradd.fxml", "Add new supplier", STR_ADD_NEW,
                new AddSupplierController());
        fwAgent.focusedProperty().addListener((ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean aBoolean2) -> {
            fwAgent.getMenu().hide();
            growersList = updateGrowersList();
            fwAgent.setEntries(growersList);
        });
        agentSearch.setPartyType(PartyType.SUPPLIERS);
        agentSearch.setLinkedObject(fwAgent);
        //After a change to the supplier id field, update the grNo
        grNo.setOnKeyPressed((KeyEvent ke) -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                String strGrNo = grNo.getText();
                //set to empty string if get text returns null
                strGrNo = strGrNo == null ? "" : strGrNo;
                if (!grNo.getText().equals("")) {
                    handleGrNoKeyPressed(strGrNo);
                    keyPressed(ke.getCode());
                }
            }
        });
        totalQty2.setEditable(false);
        gross.setEditable(false);
        net.setEditable(false);
        charges.setText("0");
        charges.setEditable(false);
        totalQty.setText("1");
        totalQty.focusedProperty().addListener(
                GeneralMethods.createTxtFieldChangeListener(totalQty, 1, null, new TextField[]{totalQty2}));
        /* nullify the supplier gross sum */
        sessionController.setSupplierGrossSum(0);

        growersEntry.setEditable(true);
        buyersEntry.setEditable(true);

        // ADD LOT ENTRY
        // -----------------------------------------
        TableColumn tcLotsFruit = new TableColumn("Fruit");
        // fruitCol.setCellValueFactory(new
        // PropertyValueFactory<FreshEntryTableData.AddLotTableLine,String>("fruit"));
        tcLotsFruit.setCellFactory(
                new javafx.util.Callback<TableColumn<FreshEntryTableData.AddLotTableLine, String>, TableCell<FreshEntryTableData.AddLotTableLine, String>>() {
            @Override
            public TableCell<FreshEntryTableData.AddLotTableLine, String> call(
                    TableColumn<FreshEntryTableData.AddLotTableLine, String> param) {
                TextFieldTableCell<FreshEntryTableData.AddLotTableLine, String> cell = new TextFieldTableCell<FreshEntryTableData.AddLotTableLine, String>() {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            String fruit = sessionController.getFruitType();
                            setItem(fruit);
                            setText(fruit);
                            setStyle("-fx-background-color: #a0beff; -fx-text-fill: #000000");
                        }
                    }

                };
                cell.setText(fruitSelection.getValue().toString());
                growerFruitColList.add(cell);
                return cell;
            }
        });

        final TableColumn tcLotsQualType = new TableColumn("Quality type (Marka)");
        tcLotsQualType.setCellValueFactory(
                new PropertyValueFactory<FreshEntryTableData.AddLotTableLine, String>("qualityType"));

        final TableColumn tcLotsQty = new TableColumn("Qty (No. of Box)");
        tcLotsQty.setCellValueFactory(new PropertyValueFactory<FreshEntryTableData.AddLotTableLine, String>("qty"));

        // tcLotsQty.setCellFactory(TextFieldTableCell.forTableColumn());
        tcLotsQty.setCellFactory(
                new Callback<TableColumn<FreshEntryTableData.AddLotTableLine, String>, TableCell<FreshEntryTableData.AddLotTableLine, String>>() {
            @Override
            public TableCell<AddLotTableLine, String> call(TableColumn<AddLotTableLine, String> param) {
                TextFieldTableCell<AddLotTableLine, String> tabCell = new TextFieldTableCell<AddLotTableLine, String>();
                tabCell.setConverter(new DefaultStringConverter());
                lotQtyColList.add(tabCell);
                return tabCell;
            }

        });

        tcLotsQty.setEditable(true);
        tcLotsQty.setOnEditCommit(createTableCellFieldEventHandler("qty"));
        addLots.setItems(addLotLines);

        tcLotsFruit.setEditable(false);
        addLots.getColumns().addAll(tcLotsFruit, tcLotsQualType, tcLotsQty);
        addLots.setEditable(true);
        tcLotsQualType.setCellFactory(
                new Callback<TableColumn<FreshEntryTableData.AddLotTableLine, String>, TableCell<FreshEntryTableData.AddLotTableLine, String>>() {
            @Override
            public TableCell<FreshEntryTableData.AddLotTableLine, String> call(
                    TableColumn<FreshEntryTableData.AddLotTableLine, String> param) {
                final CustomComboboxTableCell lotQualComboCell = new CustomComboboxTableCell(qualityTypesList);
                final ComboBox<String> comboBox = lotQualComboCell.getComboBox();
                lotQualComboCell.setComboBoxHandler(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> obs, String oldValue,
                            String newValue) {
                        // attempt to update property:
                        // System.out.println("old value" + oldValue +
                        // "- new value:" + newValue);
                        ObservableValue<FreshEntryTableData.AddLotTableLine> property = lotQualComboCell
                                .getTableColumn().getCellObservableValue(lotQualComboCell.getIndex());
                        if (property instanceof javafx.beans.value.WritableValue) {
                            ((javafx.beans.value.WritableValue<String>) property).setValue(newValue);
                        }
                        ObservableList<AddLotTableLine> lines = lotQualComboCell.getTableView().getItems();
                        int count = 0;
                        for (AddLotTableLine lotLine : lines) {
                            if (newValue == null || newValue.trim().isEmpty()) {
                                continue;
                            }
                            if (lotLine.getQualityType().trim().equals(newValue)) {
                                count++;
                            }
                        }
                        if (newValue == null) {
                            newValue = "";
                        }
                        if (lines.size() > 1 && count > 0) {
                            comboBox.setStyle("-fx-background-color: red");
                        } else {
                            comboBox.setStyle(defaultComboBgColor);
                        }

                        Object lastLine = lines.get(lines.size() - 1);
                        ((FreshEntryTableData.AddLotTableLine) lastLine).set(LotTableColumnNameEnum.qualityType,
                                newValue.toString());
                        for (TextFieldTableCell cell : growerQualityColList) {
                            if (!cell.isEmpty()) {
                                cell.setItem(newValue.toString());
                                cell.setText(cell.getItem().toString());
                            }
                        }

                        for (CustomComboboxTableCell cbCell : lotQualityColList) {
                            if (cbCell != lotQualComboCell) {
                                cbCell.getComboBox().setStyle(defaultComboBgColor);
                            }
                        }
                        sessionController.setQuality(newValue);
                    }
                }); // end comboBoxCell Handler
                // add the focus listener
                setTableComboFocusListener(comboBox);
                // add key press listener for combo box
                setTableComboKeyPressListener(addLots, comboBox, tcLotsQty);

                lotQualityColList.add(lotQualComboCell);
                return lotQualComboCell;
            }
        });
        /* When the table gets focused, the currently selected row's quality
	 * combo box gets popped up if it hasn't already.
         */
        setFocusListenerForTableColumn(addLots, tcLotsQualType);
        /* Only pressing enter key on quality column combo box doesn't put the
         * quantity column in the edit mode state, so this key event handler is being added.
         */
        addLots.setOnKeyReleased((KeyEvent event) -> {
            if (event.getCode() == KeyCode.ENTER) {
                TablePosition<FreshEntryTableData.AddLotTableLine, String> focusedCell = addLots.getFocusModel()
                        .getFocusedCell();
                if (focusedCell != null) {
                    int row = focusedCell.getRow();
                    if (focusedCell.getTableColumn() == tcLotsQualType && row == 0) {
                        fireKeyPress(java.awt.event.KeyEvent.VK_TAB);
                    }
                    if (focusedCell.getTableColumn() == tcLotsQty) {
                        addLots.edit(row, tcLotsQty);
                        if (shouldFireTabKey) {
                            shouldFireTabKey = false;
                            fireKeyPress(java.awt.event.KeyEvent.VK_TAB);
                        }
                    }
                }
            }
        });
        // ----------------------------------------------------
        // SUPPLIER(GROWER) ENTRY
        // -----------------------------------------------
        TableColumn tcGrowerQual = new TableColumn("Quality");
        tcGrowerQual.setCellFactory(
                new javafx.util.Callback<TableColumn<FreshEntryTableData.GrowerEntryTableLine, String>, TableCell<FreshEntryTableData.GrowerEntryTableLine, String>>() {
            @Override
            public TableCell<FreshEntryTableData.GrowerEntryTableLine, String> call(
                    TableColumn<FreshEntryTableData.GrowerEntryTableLine, String> param) {
                TextFieldTableCell<FreshEntryTableData.GrowerEntryTableLine, String> cell = new TextFieldTableCell<FreshEntryTableData.GrowerEntryTableLine, String>() {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setGraphic(null);
                            setText(null);
                        }
                        else {
                            setText(item.toString());
                            setStyle("-fx-background-color: #a0beff; -fx-text-fill: #000000");
                        }
                    }
                };
                growerQualityColList.add(cell);
                return cell;
            }
        });
        tcGrowerQual.setCellValueFactory(
                new PropertyValueFactory<FreshEntryTableData.GrowerEntryTableLine, String>("quality"));
        tcGrowerQual.setEditable(false);
        final TableColumn tcGrowerQty = new TableColumn(GROWER_QTY_COL_NAME);
        tcGrowerQty.setCellValueFactory(
                new PropertyValueFactory<FreshEntryTableData.GrowerEntryTableLine, String>("growerQty"));
        tcGrowerQty.setCellFactory(TextFieldTableCell.forTableColumn());
        tcGrowerQty.setOnEditCommit(createTableCellFieldEventHandler("growerQty"));

        final TableColumn tcGrowerBoxSize = new TableColumn("Box Size");
        tcGrowerBoxSize.setCellValueFactory(
                new PropertyValueFactory<FreshEntryTableData.GrowerEntryTableLine, String>("boxSize"));
        tcGrowerBoxSize.setCellFactory(
                new javafx.util.Callback<TableColumn<FreshEntryTableData.GrowerEntryTableLine, String>, TableCell<FreshEntryTableData.GrowerEntryTableLine, String>>() {
            @Override
            public TableCell<FreshEntryTableData.GrowerEntryTableLine, String> call(
                    TableColumn<FreshEntryTableData.GrowerEntryTableLine, String> param) {
                final CustomComboboxTableCell cboGrowerBoxSizeCell = new CustomComboboxTableCell(boxSizesList);
                cboGrowerBoxSizeCell.setComboBoxHandler(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> obs, String oldValue,
                            String newValue) {
                        // attempt to update property:
                        ObservableValue<FreshEntryTableData.AddLotTableLine> property = cboGrowerBoxSizeCell
                                .getTableColumn().getCellObservableValue(cboGrowerBoxSizeCell.getIndex());
                        if (property instanceof javafx.beans.value.WritableValue) {
                            ((javafx.beans.value.WritableValue<String>) property).setValue(newValue);
                        }
                        ObservableList<FreshEntryTableData.GrowerEntryTableLine> lines = cboGrowerBoxSizeCell
                                .getTableView().getItems();
                        int count = 0;
                        if (newValue == null) {
                            newValue = "";
                        }
                        for (GrowerEntryTableLine grLine : lines) {
                            if (newValue.trim().equals("")) {
                                continue;
                            }
                            if (grLine.getBoxSize().trim().equals(newValue)) {
                                count++;
                            }
                        }
                        if (lines.size() > 1 && count > 0) {
                            cboGrowerBoxSizeCell.getComboBox().setStyle("-fx-background-color: red");
                        } else {
                            cboGrowerBoxSizeCell.getComboBox().setStyle(defaultComboBgColor);
                        }
                        FreshEntryTableData.GrowerEntryTableLine lastLine = lines.get(lines.size() - 1);
                        lastLine.set("boxSize", newValue.toString());
                        for (TextFieldTableCell cell : buyerBoxSizeColList) {
                            if (!cell.isEmpty()) {
                                cell.setText(newValue.toString());
                            }
                        }
                        sessionController.setBoxSize(newValue.toString());
                        CustomComboboxTableCell currentCell = cboGrowerBoxSizeCell;
                        for (CustomComboboxTableCell cbCell : growerBoxSizeColList) {
                            if (cbCell != currentCell) {
                                cbCell.getComboBox().setStyle(defaultComboBgColor);
                            }
                        }

                    }
                });
                growerBoxSizeColList.add(cboGrowerBoxSizeCell);
                ComboBox grBoxCombo = cboGrowerBoxSizeCell.getComboBox();
                setTableComboFocusListener(grBoxCombo);
                setTableComboKeyPressListener(growersEntry, grBoxCombo, tcGrowerQty);

                return cboGrowerBoxSizeCell;
            }
        });
        final TableColumn tcGrowerRatePBox = new TableColumn("Rate Per Box");
        tcGrowerRatePBox.setCellValueFactory(
                new PropertyValueFactory<FreshEntryTableData.GrowerEntryTableLine, String>("growerRate"));
        tcGrowerRatePBox.setCellFactory(TextFieldTableCell.forTableColumn());
        tcGrowerRatePBox.setOnEditCommit(createTableCellFieldEventHandler("growerRate"));
        // add focus listener for Growers table
        setFocusListenerForTableColumn(growersEntry, tcGrowerBoxSize);
        growersEntry.setItems(growerLines);
        growersEntry.getColumns().addAll(tcGrowerQual, tcGrowerBoxSize, tcGrowerQty, tcGrowerRatePBox);
        growersEntry.setEditable(true);
        // key press listener for table
        growersEntry.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                // System.out.println(event.getCode());
                if (event.getCode() == KeyCode.ENTER) {
                    TablePosition<FreshEntryTableData.GrowerEntryTableLine, String> focusedCell = growersEntry
                            .getFocusModel().getFocusedCell();
                    if (focusedCell != null) {
                        int row = focusedCell.getRow();
                        if (focusedCell.getTableColumn() == tcGrowerBoxSize && row == 0) {
                            // System.out.println("In Grower box size combo
                            // column cell");
                            fireKeyPress(java.awt.event.KeyEvent.VK_TAB);
                        } else if (focusedCell.getTableColumn() == tcGrowerQty) {
                            growersEntry.edit(row, tcGrowerQty);
                        } else if (focusedCell.getTableColumn() == tcGrowerRatePBox) {
                            // System.out.println("In Grower rate box column
                            // cell");
                            growersEntry.edit(row, tcGrowerRatePBox);
                            if (shouldFireTabKey) {
                                shouldFireTabKey = false;
                                fireKeyPress(java.awt.event.KeyEvent.VK_TAB);
                            }
                        }
                    }
                }
            }
        });
        // -----------------------------------------------
        // BUYER ENTRY
        // ----------------------------------------
        // populate buyers list, to choose buyer from it
        // ------------
        TableColumn tcBuyerBoxSize = new TableColumn("Box Size");
        tcBuyerBoxSize.setCellValueFactory(
                new PropertyValueFactory<FreshEntryTableData.BuyerEntryTableLine, String>("boxSize"));
        tcBuyerBoxSize.setCellFactory(
                new javafx.util.Callback<TableColumn<FreshEntryTableData.BuyerEntryTableLine, String>, TableCell<FreshEntryTableData.BuyerEntryTableLine, String>>() {
            @Override
            public TableCell<FreshEntryTableData.BuyerEntryTableLine, String> call(
                    TableColumn<FreshEntryTableData.BuyerEntryTableLine, String> param) {
                TextFieldTableCell<FreshEntryTableData.BuyerEntryTableLine, String> cell = new TextFieldTableCell<FreshEntryTableData.BuyerEntryTableLine, String>() {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            String boxsize = sessionController.getBoxSize();
                            setItem(boxsize);
                            setText(boxsize);
                            setStyle("-fx-background-color: #a0beff; -fx-text-fill: #000000");
                        }
                    }

                };
                buyerBoxSizeColList.add(cell);
                // growerBoxSizeColList.add(cell); //TODO: check with
                // old code if this is the correct place for this stmt
                return cell;
            }
        });

        buyersEntry.setItems(buyerLines);
        final TableColumn tcBuyerRate = new TableColumn("Rate Per Box");
        tcBuyerRate.setCellValueFactory(
                new PropertyValueFactory<FreshEntryTableData.BuyerEntryTableLine, String>("buyerRate"));
        tcBuyerRate.setCellFactory(TextFieldTableCell.forTableColumn());
        tcBuyerRate.setOnEditCommit(createTableCellFieldEventHandler("buyerRate"));
        final TableColumn tcBuyerQty = new TableColumn("Quantity");
        tcBuyerQty.setCellValueFactory(
                new PropertyValueFactory<FreshEntryTableData.BuyerEntryTableLine, String>("buyerQty"));
        tcBuyerQty.setCellFactory(TextFieldTableCell.forTableColumn());
        tcBuyerQty.setOnEditCommit(createTableCellFieldEventHandler("buyerQty"));
        final TableColumn tcBuyerSelect = new TableColumn("Select Buyer");

        tcBuyerSelect.setCellValueFactory(
                new PropertyValueFactory<>("buyerSelect"));
        tcBuyerSelect.setCellFactory(
                new Callback<TableColumn<FreshEntryTableData.BuyerEntryTableLine, String>,
                        TableCell<FreshEntryTableData.BuyerEntryTableLine, String>>() {
            @Override
            public TableCell<FreshEntryTableData.BuyerEntryTableLine, String> call(
                    TableColumn<FreshEntryTableData.BuyerEntryTableLine, String> param) {
                AutoCompleteTextField acTextField = createAutoCompleteBuyerTextField();
                final AutoCompleteTableCell autCompCellBuyerSelect = new AutoCompleteTableCell(
                        acTextField);
                
                autCompCellBuyerSelect.setOnKeyReleased((KeyEvent event) -> {
                    if (event.getCode() == KeyCode.ENTER) {
                        TableRow<?> tr = autCompCellBuyerSelect.getTableRow();
                        int row = tr.getIndex();
                        String selectedBuyer = acTextField.getText();
                        if (selectedBuyer == null || selectedBuyer.trim().isEmpty() || !buyersList.contains(selectedBuyer)) {
                            GeneralMethods.errorMsg("Please select a valid buyer");
                            nextTabColumnToFocus = tcBuyerSelect;
                        } else {
                            nextTabColumnToFocus = tcBuyerQty;
                        }
                        // System.out.println("The row number for
                        // select buyer field:" + row);
                        buyersEntry.getFocusModel().focus(row, (TableColumn<BuyerEntryTableLine, String>) nextTabColumnToFocus);
                        buyersEntry.edit(row, (TableColumn<BuyerEntryTableLine, String>) nextTabColumnToFocus);
                    }
                });
                return autCompCellBuyerSelect;
            }
        });

        TableColumn tcBuyerSearch = new TableColumn();
        tcBuyerSearch.setCellFactory(
                new Callback<TableColumn<FreshEntryTableData.BuyerEntryTableLine, String>, TableCell<FreshEntryTableData.BuyerEntryTableLine, String>>() {
            @Override
            public TableCell<FreshEntryTableData.BuyerEntryTableLine, String> call(
                    TableColumn<FreshEntryTableData.BuyerEntryTableLine, String> param) {
                PartySearchTableCell cell = new PartySearchTableCell(PartyType.BUYERS);
                return cell;
            }
        });

        buyersEntry.getColumns().addAll(tcBuyerBoxSize, tcBuyerSelect, tcBuyerSearch, tcBuyerQty, tcBuyerRate);
        tcBuyerBoxSize.setEditable(false);
        // key press listener for buyers entry table
        buyersEntry.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER) {
                    TablePosition<FreshEntryTableData.BuyerEntryTableLine, ?> focusedCell = buyersEntry.getFocusModel()
                            .getFocusedCell();
                    if (focusedCell != null) {
                        int row = focusedCell.getRow();

                        if (focusedCell.getTableColumn() == tcBuyerSelect) {
                            /* This block gets executed even when the user is
                             * modifying the table cell, so, this probably due
                             * to auto complete field being used in this column.
                             * So, once the user is done with editing, we need
                             * to set the next column in sequence to have the
                             * focus. For other columns setting procedure of
                             * next column takes place in the cell commit event
                             * handler, so no need to set that info here.
                             */
                            if (focusedCell.getRow() < 1) {
                                fireKeyPress(java.awt.event.KeyEvent.VK_TAB);
                            }
                        }
                        else if (focusedCell.getTableColumn() == tcBuyerQty) {
                            buyersEntry.edit(row, tcBuyerQty);
                        }
                        else if (focusedCell.getTableColumn() == tcBuyerRate) {
                            buyersEntry.edit(row, tcBuyerRate);
                            if (shouldFireTabKey) {
                                shouldFireTabKey = false;
                                fireKeyPress(java.awt.event.KeyEvent.VK_TAB);
                            }
                        }
                    }
                    else {
                        /* Although this block doesn't seem to execute MOST of
                         * the time I ran the code, however, it did, I think, at
                         * once run, so keeping this doesn't sound harmful.
                         */
                        int row = buyersEntry.getItems().size() - 1;
                        buyersEntry.getFocusModel().focus(row, tcBuyerSelect);
                        buyersEntry.edit(row, tcBuyerSelect);
                        fireKeyRelease(java.awt.event.KeyEvent.VK_TAB);
                    }
                }
            }
        });
        try {
			parent = FXMLLoader.load(getClass().getResource("/fxml/freshentry.fxml"));
		} catch (IOException e1) {
			GeneralMethods.errorMsg(e1.getMessage());
		}
        setFocusListenerForTableColumn(buyersEntry, tcBuyerSelect);
        // CHARGES SECTION
        chargesView = new Stage();
        Scene scene = new Scene(parent);
        chargesView.setScene(scene);
        expenses.setOnMouseClicked((MouseEvent event) -> {
            try {
                chargesView.showAndWait();
                Map<String, ChargeTypeValueMap> charges1 = SessionDataController.getInstance().getCharges();
                StringBuilder sb = new StringBuilder("* ");
                for (String name : charges1.keySet()) {
                    sb.append(name + " rate:").append(charges1.get(name).totalValue.getText().toString()).append("\n * ");
                }
                sb.deleteCharAt(sb.toString().length() - 2);
                String expenseText = sb.toString();
                expenses.setText(expenseText);
            }
            catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Error!");
                alert.setHeaderText(null);
                alert.setContentText("Total quantity must be a number!");
                alert.showAndWait();
            }
        });

        // DEFINITION FOR FINAL SAVE ENTRY BUTTON
        saveButton.setOnMousePressed((Event event) -> {
            if (checkQuantities() == 0) {
                // ARRIVAL(SALES) PART:
                dealID = Integer.valueOf(grNo.getText());
                String amanat = sessionController.getAmanatTotal();
                dbclient.saveEntryToSql("arrival",
                        new String[]{"date", "fruit", "challan", "supplier", "totalQuantity", "fullCase",
                            "halfCase", "fwagent", "truck", "driver", "gross", "charges", "net", "remarks",
                            "dealID", "type", "amanat"},
                        new String[]{date.getValue().toString(), fruitSelection.getValue(),
                            chlNo.getText(), grower.getText(), totalQty2.getText(), fullCase.getText(),
                            halfCase.getText(), fwAgent.getText(), truckNo.getText(), driverNoField.getText(),
                            gross.getText(), charges.getText(), net.getText(), remarks.getText(), dealID + "",
                            freshEntryTypes[freshEntryType], amanat});
                // BUYER DEALS PART:
                //Clean deals first
                dbclient.deleteTableEntries("buyerDeals", "dealId", String.valueOf(dealID), false, false);
                for (String[] buyerSqlLine : FreshEntryTableData.buyerEntryLinesSql) {
                    String bRate = buyerSqlLine[5];
                    String boxes = buyerSqlLine[2];
                    String aggregateAmt = getBuyerAggregateAmount(bRate, boxes);
                    dbclient.saveEntryToSql("buyerDeals",
                            new String[]{"buyerTitle", "dealDate", "buyerRate", "buyerPay", "boxes",
                                "aggregatedAmount", "dealID", "boxSizeType", "qualityType", "fruit"},
                            new String[]{buyerSqlLine[0], date.getValue().toString(), bRate, buyerSqlLine[6],
                                boxes, aggregateAmt, dealID + "", buyerSqlLine[4], buyerSqlLine[3], buyerSqlLine[7]});
                }
                sessionController.resetPendingLadaanEntries();
                // SUPPLIER DEALS PART:
                //Clean deals first
                dbclient.deleteTableEntries("supplierDeals", "dealId", String.valueOf(dealID), false, false);
                for (String[] supplierSqlLine : FreshEntryTableData.growerEntryLinesSql) {
                    dbclient.saveEntryToSql("supplierDeals",
                            new String[]{"supplierTitle", "date", "cases", "supplierRate", "net", "agent",
                                "dealID", "boxSizeType", "qualityType", "fruit"},
                            new String[]{grower.getText(), date.getValue().toString(), supplierSqlLine[3],
                                supplierSqlLine[0], net.getText(), fwAgent.getText(), dealID + "",
                                supplierSqlLine[1], supplierSqlLine[2], fruitSelection.getValue()});
                }
                if (sessionController.getCharges() != null) {
                    dbclient.addDealCharges(sessionController.getCharges(), dealID);
                }
                GeneralMethods.msg("New entries were successfully saved");
                saveButton.getScene().getWindow().hide();
            }
        });
    }// END OF INITIALIZE

    public static Stage getChargesView() {
        return chargesView;
    }

    //Cell commit listener for table view text field cell
    private EventHandler<TableColumn.CellEditEvent> createTableCellFieldEventHandler(final String dataToEdit) {
        return (new EventHandler<TableColumn.CellEditEvent>() {

            @SuppressWarnings("unchecked")
            @Override
            public void handle(TableColumn.CellEditEvent event) {
                boolean err = false;
                try {
                    if (Integer.parseInt(event.getNewValue().toString()) <= 0) {
                        GeneralMethods.errorMsg("Invalid value! Value should be a postive number");
                        err = true;
                    }
                } catch (Exception e) {
                    err = true;
                    GeneralMethods.errorMsg("Invalid value! Enter a valid positive number");
                } finally {
                    if (err) {
                        event.consume();
                        nextTabColumnToFocus = event.getTableColumn();
                        event.getTableView().requestFocus();
                        return;
                    }
                }
                TableView tv = event.getTableColumn().getTableView();
                currentTraversedNodeIdx = traversibleNodes.indexOf(tv);

                final String newCellValue = event.getNewValue().toString();

                if (tv == addLots) {
                    currTable = LOT;
                    FreshEntryTableData.AddLotTableLine lotTabLine = (FreshEntryTableData.AddLotTableLine) event
                            .getTableView().getItems().get(event.getTablePosition().getRow());
                    lotTabLine.set(LotTableColumnNameEnum.valueOf(dataToEdit), newCellValue);
                } else if (tv == buyersEntry) {
                    currTable = BUYERS;
                    ((FreshEntryTableData.BuyerEntryTableLine) event.getTableView().getItems()
                            .get(event.getTablePosition().getRow())).set(dataToEdit, newCellValue);

                } else if (tv == growersEntry) {
                    currTable = GROWERS;
                    ((FreshEntryTableData.GrowerEntryTableLine) event.getTableView().getItems()
                            .get(event.getTablePosition().getRow())).set(dataToEdit, newCellValue);
                }

                if (checkQuantities() == -1) {
                    tv.refresh();
                    return;
                }

                // check if this is the last column
                TablePosition<?, ?> tPosition = event.getTablePosition();
                int totalCols = tv.getColumns().size();
                int currColNum = tPosition.getColumn();
                int curRow = tPosition.getRow();
                if ((currColNum + 1) < totalCols) {
                    //There are more columns in table left, so will proceed to next
                    //  table only once all the columns have been filled up
                    TableColumn<?, String> nextColInSeq = (TableColumn<?, String>) tv.getColumns().get(currColNum + 1);
                    System.out.println("The newly focused cell is of column: " + nextColInSeq.getText()); //Todo debug
                    int currRow = tPosition.getRow();
                    startNextColumnEdit(tv, currRow, nextColInSeq);
                    tv.requestFocus();
                    event.consume();
                    return;
                }
                int num = 0;
                boolean isLastColumn = ((currColNum + 1) == totalCols);
                int rowCount = tv.getItems().size();
                boolean isLastRow = ((curRow + 1) == rowCount);
                if ((tv == growersEntry || tv == addLots) && isLastColumn) {
                    // validate box and quality types type
                    boolean isDuplicate = false;
                    String errMsg = "";
                    if (tv == growersEntry) {
                        isDuplicate = isDuplicateBoxSize(sessionController.getBoxSize(), curRow);
                        errMsg = "The box size is already selected, please select a different one";
                    }
                    if (tv == addLots) {
                        isDuplicate = isDuplicateQualityType(sessionController.getQuality(), curRow);
                        errMsg = "The quality type is already selected, please select a different one";

                    }
                    if (isDuplicate) {
                        GeneralMethods.errorMsg(errMsg);
                        shouldFireTabKey = true;
                        reOpenComboBox = true;
                        nextTabColumnToFocus = getLastColumn(tv);
                        startNextColumnEdit(tv, curRow - 1, nextTabColumnToFocus);
                        tv.requestFocus();
                        event.consume();
                        return;
                    }
                }
                if (isLastColumn && !isLastRow) {
                    //The column is last one but there are more rows in the table, so try to move the focus to next row
                    shouldFireTabKey = true;
                    startNextColumnEdit(tv, curRow, getLastColumn(tv));
                    tv.requestFocus();
                    return;
                }
                if (isLastRow) {
                    if (tv == buyersEntry && isLastColumn) {
                        num = handleLastColOfBuyerTable(tv, curRow, event);
                    } // end if (tv=buyer)
                    else if (tv == growersEntry && isLastColumn) {
                        num = handleLastColOfGrowerTable(tv, curRow, event);
                    } else if (tv == addLots && isLastColumn) {
                        num = handleLastColOfLotsTable(tv, curRow, event);
                    }
                }
                if (num == -1 || num == -2) {
                    return;
                }
            }

        });
    }

    @SuppressWarnings("rawtypes")
    public void updateTable(TableView<?> table, ObservableList lines) {
        table.setItems(lines);
    }

    public void addTableLine(ObservableList<FreshEntryTableData.AddLotTableLine> lines,
            FreshEntryTableData.AddLotTableLine newLine) {
        lines.add(newLine);
        updateTable(addLots, lines);
    }

    public void addTableLine(ObservableList<FreshEntryTableData.GrowerEntryTableLine> lines,
            FreshEntryTableData.GrowerEntryTableLine newLine) {
        lines.add(newLine);
        updateTable(growersEntry, lines);
    }

    public void addTableLine(ObservableList<FreshEntryTableData.BuyerEntryTableLine> lines,
            FreshEntryTableData.BuyerEntryTableLine newLine) {
        lines.add(newLine);
        updateTable(buyersEntry, lines);
    }

    private java.util.TreeSet<String> updateGrowersList() {
        int rowsNum = dbclient.getRowsNum("suppliers1");
        java.util.TreeSet<String> result = new java.util.TreeSet<>();
        
        for (int supp_id = 1; supp_id <= rowsNum; supp_id++) {
            try {
                com.quickveggies.entities.Supplier supplier = dbclient.getSupplierById(supp_id);
                result.add(supplier.getTitle());
            }
            catch (java.sql.SQLException e) {
                System.out.print("sqlexception in populating suppliers list");
            }
        }
        if (result.isEmpty()) {
            result.add(STR_ADD_NEW);
        }
        return result;
    }

    private java.util.TreeSet<String> updateBuyersList() {
        java.util.TreeSet<String> result = new java.util.TreeSet<>();
        try {
            for (Buyer buyer : dbclient.getBuyers()) {
                if (!"Bank Sale".equals(buyer.getTitle())) {
                    result.add(buyer.getTitle());
                }
            }
            if (result.isEmpty()) {
                result.add(STR_ADD_NEW);
            }
        }
        catch (SQLException ex) {
            Logger.getLogger(FreshEntryController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    // method to sum the quantities and check integrity
    public int checkQuantities() {

        int result = 0;
        totalLotQty = 0;

        int tempLotQty = Integer.parseInt(addLotLines.get(addLotLines.size() - 1).getQty());
        int tempGrowersQty = 0;
        if (!growerLines.isEmpty()) {
            tempGrowersQty = Integer.parseInt(growerLines.get(growerLines.size() - 1).getGrowerQty());
        }

        for (FreshEntryTableData.AddLotTableLine line : addLotLines) {
            totalLotQty += Integer.parseInt(line.getQty());
        }

        /*
		 * 
		 * for (FreshEntryTableData.GrowerEntryTableLine line : growerLines) {
		 * totalGrowersQty += Integer.parseInt(line.getGrowerQty()); }
		 * 
		 * for (FreshEntryTableData.BuyerEntryTableLine line : buyerLines) {
		 * totalBuyersQty += Integer.parseInt(line.getBuyerQty()); }
         */
        int totalQuantity = Integer.parseInt(totalQty.getText());

        int totalGrowerQtyForQuality = getTotalGrCountOfQtyForQuality(sessionController.quality);
        int totalGrowersQtyForBox = getTotalGrowerQtyForBoxSize(sessionController.boxSize);
        int totalLotQtyForQuality = getTotalLotEntriesForQuality(sessionController.quality);
        int totalBuyerQtyForBox = getTotalBuyerQtyForBoxSize(sessionController.boxSize);

        if (totalLotQty > totalQuantity) {
            String strMsg = String.format("Lot quantity cannot be more than total quantity %d", totalQuantity);
            GeneralMethods.errorMsg(strMsg);
            addLotLines.get(addLotLines.size() - 1).set(LotTableColumnNameEnum.qty, String.valueOf(0));
            updateTable(addLots, addLotLines);
            result = -1;
        }
        if (totalGrowerQtyForQuality > totalLotQtyForQuality) {
            String strMsg = "Supplier quantity for quality type cannot be more than quantity of Lots table";
            GeneralMethods.errorMsg(strMsg);
            growerLines.get(growerLines.size() - 1).set("growerQty", String.valueOf(0));
            updateTable(growersEntry, growerLines);
            result = -1;
        }
        if (totalBuyerQtyForBox > totalGrowersQtyForBox) {
            String strMsg = String.format(
                    "Buyer quantiy for box size type:%s cannot be more than grower's quantiy :%d ",
                    sessionController.boxSize, totalGrowersQtyForBox);
            GeneralMethods.errorMsg(strMsg);
            buyerLines.get(buyerLines.size() - 1).set("buyerQty", String.valueOf(0));
            updateTable(buyersEntry, buyerLines);
            result = -1;
        }

        return result;
    }

    private boolean areTableFieldsEmpty(TableView<?> table) {
        boolean result = false;
        @SuppressWarnings("rawtypes")
        ObservableList lines = table.getItems();
        Object lastLine = lines.get(lines.size() - 1);
        if (lastLine instanceof FreshEntryTableData.AddLotTableLine) {
            FreshEntryTableData.AddLotTableLine line = (FreshEntryTableData.AddLotTableLine) lastLine;
            if (line.getQualityType().equals("") || line.getQty().equals("0")) {
                result = true;
            }
        } else if (lastLine instanceof FreshEntryTableData.GrowerEntryTableLine) {
            FreshEntryTableData.GrowerEntryTableLine line = (FreshEntryTableData.GrowerEntryTableLine) lastLine;
            if (line.getBoxSize().equals("") || line.getGrowerQty().equals("0")) {
                result = true;
            }
        } else if (lastLine instanceof FreshEntryTableData.BuyerEntryTableLine) {
            FreshEntryTableData.BuyerEntryTableLine line = (FreshEntryTableData.BuyerEntryTableLine) lastLine;
            if (line.getBuyerSelect().equals("") || line.getBuyerQty().equals("0")) {
                result = true;
            }
        }
        return result;
    }

    /**
     * Handles the enter key press event for gr no, returns false if there is an
     * error converting the number, the calling method then can discontinue the
     * flow of event handler
     *
     * @param strGrNo - the Grower No string
     * @return boolean - indicating the successful operation
     */
    private boolean handleGrNoKeyPressed(String strGrNo) {
        boolean isValidNo = true;
        int intValue = -1;
        try {
            intValue = Integer.valueOf(strGrNo);
        } catch (NumberFormatException nfe) {
            isValidNo = false;
            GeneralMethods.errorMsg("Invalid number, please enter a valid number");
        }
        int nextGrNo = 0;
        if (intValue == 0) {
            DatabaseClient client = DatabaseClient.getInstance();
            try {
                nextGrNo = client.getNextTransIdForFreshEntry() + 1;
                grNo.setText(String.valueOf(nextGrNo));
                /*
				 * TODO: confirmed by client, grower/buyer has no link with gr
				 * no being changed if (growersList.size() > nextGrNo) {
				 * grower.setText(growersList.toArray()[nextGrNo].toString());
				 * grower.getMenu().hide(); }
                 */ } catch (Exception ex) {
                GeneralMethods
                        .errorMsg("Some database or other error occured while getting the last id no for grower index :"
                                + ex.getMessage());
            }
        }
        return isValidNo;

    }

    // TABLES INTERACTION ALGORITHM
    public void keyPressed(KeyCode key) {
        if (key == KeyCode.ENTER) {
            /*
			 * System.out.println("Enter key event for non table components");
             */
 /*
			 * confirm table changes, check if more lines are needed or save
			 * entry to sql
             */

            // check which node has the focus and focus the next one
            for (Node currentNode : traversibleNodes) {
                if (currentNode == null) {
                    continue;
                }
                if (currentNode.isFocused() && !(currentNode instanceof TableView)) {
                    int currInd = traversibleNodes.indexOf(currentNode) + 1;
                    currentTraversedNodeIdx = currInd;
                    if (currInd < traversibleNodes.size()) {
                        Node nextNode = traversibleNodes.get(currInd);
                        if (nextNode == date) {
                            /*
							 * * For some reason the request focus for date
							 * picker doesn't work, it could be a javafx bug, so
							 * when we invoke that method, it opens the date
							 * picker and closes in fraction of second, so we
							 * are using java AWT robot class' function to
							 * invoke a tab key press event
                             */
                            fireKeyPress(java.awt.event.KeyEvent.VK_TAB);
                            date.show();
                        }
                        if (currentNode == grower) {
                            String selectedSupplier = grower.getText();
                            if (selectedSupplier == null || selectedSupplier.trim().isEmpty() || !growersList.contains(selectedSupplier.trim())) {
                                GeneralMethods.errorMsg("Please select a valid supplier/grower");
                                nextNode = grower;
                            }
                        }

                        nextNode.requestFocus();
                        /*
						 * System.out.println("node with focus:" +
						 * nextNode.getParent().getScene().focusOwnerProperty().
						 * getValue().getId());
                         */

                    }
                    return;
                }
            }

            /*
			 * checkQuantities(); // only one table can be enabled at any moment
			 * (the one being // edited) String emptyFieldMsg =
			 * "Can't continue-current table hasn't been properly filled!"; if
			 * (currTable == LOT) { addLots.requestFocus(); if
			 * (areTableFieldsEmpty(addLots))
			 * GeneralMethods.errorMsg(emptyFieldMsg); else { //
			 * addLots.setEditable(false); growersEntry.setEditable(true);
			 * growersEntry.requestFocus(); currTable = GROWERS; } }
			 * 
			 * else if (currTable == GROWERS) { if
			 * (areTableFieldsEmpty(growersEntry))
			 * GeneralMethods.errorMsg(emptyFieldMsg); else { // if the entry is
			 * a regular one, continue to populate the // buyers table if
			 * (freshEntryType == FreshEntryController.REGULAR) {
			 * buyersEntry.setEditable(true); buyersEntry.requestFocus();
			 * currTable = BUYERS; } // if the entry is coldstore/godown, skip
			 * the buyers and // check the growers else { // check if more
			 * grower lines are needed if (totalGrowersQty <
			 * Integer.parseInt(addLotLines.get(addLotLines.size() -
			 * 1).getQty())) { addTableLine(growerLines,
			 * data.getGrowerEntryTableLine("", "0", "0", ""));
			 * growersEntry.requestFocus(); currTable = GROWERS; } else { //
			 * prepare grower entries for sql, such that are // specific for
			 * each deal // variables: box size,quality,rate for
			 * (FreshEntryTableData.GrowerEntryTableLine line : growerLines) {
			 * FreshEntryTableData.growerEntryLinesSql.add(new String[] {
			 * line.getGrowerRate() }); }
			 * 
			 * sessionController
			 * .setSupplierGrossSum(sessionController.getSupplierGrossSum() +
			 * getSuppliersGross());
			 * gross.setText(sessionController.getSupplierGrossSum() + "");
			 * updateAmountCalc();// calculate the charges and net // sum
			 * 
			 * growerLines.clear();
			 * growerLines.add(data.getGrowerEntryTableLine("", "0", "0", ""));
			 * updateTable(growersEntry, growerLines);
			 * growersEntry.setEditable(false); // check if more lot lines are
			 * needed if (totalLotQty < Integer.parseInt(totalQty.getText())) {
			 * addTableLine(addLotLines, data.getAddLotTableLine("", "0", ""));
			 * // addLots.setEditable(true); addLots.requestFocus(); currTable =
			 * LOT; } } } }
			 * 
			 * } else if (currTable == BUYERS) {
			 * sessionController.setSupplierGrossSum(sessionController.
			 * getSupplierGrossSum() + getSuppliersGross());
			 * gross.setText(sessionController.getSupplierGrossSum() + "");
			 * updateAmountCalc();// calculate the charges and net sum if
			 * (areTableFieldsEmpty(buyersEntry))
			 * GeneralMethods.errorMsg(emptyFieldMsg); else { checkQuantities();
			 * // check if more buyers lines are needed if (totalBuyersQty <
			 * Integer.parseInt(growerLines.get(growerLines.size() -
			 * 1).getGrowerQty())) { addTableLine(buyerLines,
			 * data.getBuyerEntryTableLine("", "0", "0", "")); } else { //
			 * prepare buyers entries for sql // variables: //
			 * buyer,grower,buyerqty,quality,boxsize,buyerRate,totalProfit for
			 * (FreshEntryTableData.BuyerEntryTableLine line : buyerLines) {
			 * FreshEntryTableData.buyerEntryLinesSql .add(new String[] {
			 * line.getBuyerSelect(), grower.getText(), line.getBuyerQty(),
			 * addLotLines.get(addLotLines.size() - 1).getQualityType(),
			 * growerLines.get(growerLines.size() - 1).getBoxSize(),
			 * line.getBuyerRate(), "" + (Integer.parseInt(line.getBuyerRate())
			 * Integer.parseInt(line.getBuyerQty())) }); } buyerLines.clear();
			 * buyerLines.add(data.getBuyerEntryTableLine("", "0", "0", "")); //
			 * data.disableCommittedLines(growerLines); updateTable(buyersEntry,
			 * buyerLines); buyersEntry.setEditable(false); // check if more
			 * grower lines are needed if (totalGrowersQty <
			 * Integer.parseInt(addLotLines.get(addLotLines.size() -
			 * 1).getQty())) { addTableLine(growerLines,
			 * data.getGrowerEntryTableLine("", "0", "0", ""));
			 * growersEntry.requestFocus(); currTable = GROWERS; } else { //
			 * prepare grower entries for sql, such that are // specific for
			 * each deal // variables: box size,quality,rate for
			 * (FreshEntryTableData.GrowerEntryTableLine line : growerLines) {
			 * FreshEntryTableData.growerEntryLinesSql.add(new String[] {
			 * line.getGrowerRate() }); } //
			 * sessionController.setSupplierGrossSum(sessionController.
			 * getSupplierGrossSum()+getSuppliersGross()); growerLines.clear();
			 * growerLines.add(data.getGrowerEntryTableLine("", "0", "0", ""));
			 * updateTable(growersEntry, growerLines);
			 * growersEntry.setEditable(false); // check if more lot lines are
			 * needed if (totalLotQty < Integer.parseInt(totalQty.getText())) {
			 * addTableLine(addLotLines, data.getAddLotTableLine("", "0", ""));
			 * buyersEntry.setEditable(false); // addLots.setEditable(true);
			 * addLots.requestFocus(); currTable = LOT; } } } } }
             */ }
    }

    /**
     * Helper method to fire a key release event. This method depends on native
     * system permissions and may not be effective if low level key events are
     * disallowed by underlying OS.
     *
     * @param java.awt.event.KeyEvent key code
     */
    private void fireKeyRelease(int keyCode) {
        fireKey(false, keyCode);
    }

    private void fireKeyPress(int keyCode) {
        fireKey(true, keyCode);
    }

    private void fireKey(boolean press, int keyCode) {
        Robot robot;
        try {
            robot = new Robot();
            if (press) {
                robot.keyPress(keyCode);
            } else {
                robot.keyRelease(keyCode);
            }
        }
        catch (Exception ex) {
            System.out.println(
                    "Automatic key event failed, please use keyboard or mouse to get desired behavior. Exception cause : "
                    + ex.getMessage());
        }
    }

    private AutoCompleteTextField createAutoCompleteBuyerTextField() {
        buyersList = updateBuyersList();
        final AutoCompleteTextField buyerSelectTextField = new AutoCompleteTextField();
        buyerSelectTextField.setEntries(buyersList);
        buyerSelectTextField.linkToWindow(FreshEntryController.this, "/fxml/buyeradd.fxml", "Add new buyer", STR_ADD_NEW,
                new AddBuyerController());
        buyerSelectTextField.focusedProperty().addListener((
                ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean focused) -> {
            buyerSelectTextField.getMenu().hide();
            if (focused) {
                buyersList = updateBuyersList();
                buyerSelectTextField.setEntries(buyersList);
            } else {
                buyersList = updateBuyersList();
                buyerSelectTextField.setEntries(buyersList);
                buyerLines.get(buyerLines.size() - 1).set("buyerSelect", buyerSelectTextField.getText());
            }
        });
        return buyerSelectTextField;
    }

    public void updateAmountCalc() {
        SessionDataController sessionController = SessionDataController.getInstance();
        if (totalQty.getText().equals("")) {
            GeneralMethods.errorMsg("Enter num. of boxes first!");
            return;
        }
        if (gross.getText().equals("")) {
            GeneralMethods.errorMsg("Enter supplier profits first!");
            return;
        }
        sessionController.setTotalBoxes(Integer.parseInt(totalQty.getText()));
        int boxesNum = Integer.parseInt(totalQty.getText());
        Map<String, ChargeTypeValueMap> chargesMap = sessionController.getCharges();
        if (chargesMap != null) {
            if (!chargesMap.isEmpty()) {

                int sum = 0;
                for (String name : chargesMap.keySet()) {
                    String rateStr = chargesMap.get(name).totalValue.getText().toString();
                    Integer dbl = Integer.parseInt(rateStr);
                    sum += dbl.intValue();
                }
                this.charges.setText("" + sum);
                net.setText("" + (Integer.parseInt(gross.getText()) - Integer.parseInt(charges.getText())));
            } else {
                net.setText("" + (Integer.parseInt(gross.getText())));
            }
        } else {
            net.setText("" + (Integer.parseInt(gross.getText())));
        }
    }

    public int getSuppliersGross() {
        int result = 0;
        for (FreshEntryTableData.GrowerEntryTableLine line : growerLines) {
            result += line.getGrossIncome();
        }
        return result;
    }

    /**
     * Generic method to add focused property listener for table column. So,
     * when a particular table gets focused, the column specified will get the
     * focus.
     *
     * @param tv - Table which gets focus
     * @param tc - Column of the table to have the focus
     */
    private <T> void setFocusListenerForTableColumn(final TableView<T> tv,
            final TableColumn<T, String> tc) {
        tv.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            int listSize = tv.getItems().size();
            int row = listSize < 1 ? 0 : listSize - 1;
            if (listSize == 0) {
                return;
            }
            TableViewFocusModel<T> tvFocusModel = tv.getFocusModel();
            if (nextTabColumnToFocus != null) {
                if (tv.getEditingCell() != null) {
                    row = tv.getEditingCell().getRow();
                }
                tv.getSelectionModel().clearAndSelect(row, (TableColumn<T, String>) nextTabColumnToFocus);
                tvFocusModel.focus(row, (TableColumn<T, String>) nextTabColumnToFocus);
            }
            else {
                tv.getSelectionModel().clearAndSelect(row, tc);
                tvFocusModel.focus(row, tc);
            }
            int columnIndex = tv.getColumns().indexOf(tc);
            int totalCols = tv.getColumns().size();
            if ((columnIndex + 1) < totalCols) {
                //Get next column of the table
                TableColumn<?, String> nextColInSeq = (TableColumn<?, String>) tv.getColumns().get(columnIndex + 1);
                nextTabColumnToFocus = nextColInSeq;
            }
        });
    }

    /**
     * Listener for combo boxes. When the first time combo box gets focus, its
     * pop up dialog is displayed. For subsequent focus event, the pop up is not
     * displayed, as user can press space bar to pop up the dialog
     *
     * @param comboBox
     */
    private void setTableComboFocusListener(final ComboBox<?> comboBox) {
        comboBox.focusedProperty().addListener(new ChangeListener<Boolean>() {
            /*
			 * flag for preventing reopening the combo box every time table gets
			 * focus
             */
            private boolean wasShownOnFocus = false;

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                // System.out.println("In combo box focus event");
                if (!wasShownOnFocus) {
                    wasShownOnFocus = true;
                    comboBox.show();
                }
                if (reOpenComboBox && !comboBox.isShowing()) {
                    reOpenComboBox = false;
                    comboBox.show();
                }

            }
        });

    }

    /**
     * Add key event handler on combo box for showing/hiding it, and to move to
     * next column if it is not null
     *
     * @param tv - Table view component
     * @param comboBox
     * @param tc - optional table column to move the focus to, set to null if no
     * moving is required
     */
    private void setTableComboKeyPressListener(final TableView<?> tv, final ComboBox<?> comboBox,
            final TableColumn<FreshEntryTableData.AddLotTableLine, ?> tc) {
        assert (tc != null);
        comboBox.setOnKeyPressed((KeyEvent event) -> {
            if (event.getCode() == KeyCode.ENTER) {
                /*
                * System.out.
                * println("in table combo box enter key event, for table column: "
                * + tc.getText());
                 */
                if (comboBox.isShowing()) {
                    comboBox.hide();
                    /* lets return here just after closing the pop up */
                    return;
                }
                int currRow = tv.getFocusModel().getFocusedCell().getRow();
                // System.out.println("Combo key press event, Current row:"
                // + currRow);
                tv.getSelectionModel().clearAndSelect(currRow);//Todo: fix: , tc
                tv.getFocusModel().focus(currRow);//Todo: fix: , tc
                // fireKey(java.awt.event.KeyEvent.VK_TAB);

            }
            if (event.getCode() == KeyCode.SPACE) {
                // System.out.println("in table combo box space key event");
                if (!comboBox.isShowing()) {
                    comboBox.show();
                }
            }
        });
    }

    /**
     * Counts total quantity in buyers' table for a particular box size.
     *
     * Returns -1 to indicate if there were any error parsing the string to
     * number, 0 or total quantity otherwise
     */
    private int getTotalBuyerQtyForBoxSize(String buyerBoxSizeType) {
        int buyerQty = 0;
        try {

            for (BuyerEntryTableLine tmpBEtl : buyersEntry.getItems()) {
                if (tmpBEtl.getBoxSize().trim().equals(buyerBoxSizeType)) {
                    buyerQty += Integer.valueOf(tmpBEtl.getBuyerQty());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GeneralMethods.errorMsg(ex.getMessage());
            return -1;
        }
        return buyerQty;
    }

    /**
     * Counts total quantity in Growers' table for a particular box type.
     *
     * Returns -1 to indicate if there were any error parsing the string to
     * number, 0 or total quantity otherwise
     */
    private int getTotalGrowerQtyForBoxSize(String buyerBoxSizeType) {
        int totGrowerQtyForBoxType = 0;
        for (GrowerEntryTableLine geTL : growersEntry.getItems()) {
            if (geTL.getBoxSize().equals(buyerBoxSizeType)) {
                int tmpQty = 0;
                try {
                    tmpQty = Integer.valueOf(geTL.getGrowerQty());
                } catch (Exception ex) {
                    GeneralMethods.errorMsg(ex.getMessage());
                    return -1;
                }
                totGrowerQtyForBoxType += tmpQty;
            }
        }
        return totGrowerQtyForBoxType;
    }

    /*
	 * Counts total quantity in Growers' table for specified quality type.
	 * 
	 * Returns -1 to indicate if there were any error parsing the string to
	 * number, 0 or total quantity otherwise
     */
    private int getTotalGrCountOfQtyForQuality(String qualityType) {
        int totalGrCountForQuality = 0;
        for (GrowerEntryTableLine geTL : growerLines) {
            if (geTL.getQuality().equals(qualityType)) {
                int tmpQualityCount = 0;
                try {
                    tmpQualityCount = Integer.valueOf(geTL.getGrowerQty());
                } catch (Exception ex) {
                    GeneralMethods.errorMsg(ex.getMessage());
                    return -1;
                }
                totalGrCountForQuality += tmpQualityCount;
            }
        }
        return totalGrCountForQuality;
    }

    /*
	 * Counts total quantity in Lots' table for specified quality type.
	 * 
	 * Returns -1 to indicate if there were any error parsing the string to
	 * number, 0 or total quantity otherwise
     */
    private int getTotalLotEntriesForQuality(String qualityType) {
        int totalLotCountForQuality = 0;
        for (AddLotTableLine alTL : addLots.getItems()) {
            if (alTL.getQualityType().equals(qualityType)) {
                int tmpCountForQuality = 0;
                try {
                    tmpCountForQuality = Integer.valueOf(alTL.getQty());
                } catch (Exception ex) {
                    GeneralMethods.errorMsg(ex.getMessage());
                    return -1;
                }
                totalLotCountForQuality += tmpCountForQuality;
            }
        }
        return totalLotCountForQuality;
    }

    /**
     * Checks the quantities sum by buyer for particular box size, and adds new
     * lines if the buyer quantity doesn't equal to the grower's quantity for
     * the concerned box size type -1 No error, but to indicate the method
     * calling this method should return after the call to this methods -2 error
     * +1 to to proceed execution in the calling method +3 same table to set
     * focus
     */
    private int handleLastColOfBuyerTable(TableView<FreshEntryTableData.BuyerEntryTableLine> tv, int curRow,
            TableColumn.CellEditEvent event) {
        event.consume();
        String emptyFieldMsg = "Can't continue-current table hasn't been properly filled!";
        if (areTableFieldsEmpty(buyersEntry)) {
            GeneralMethods.errorMsg(emptyFieldMsg);
            return -1;
        }
        TableView<FreshEntryTableData.BuyerEntryTableLine> buyerTable = tv;
        ObservableList<BuyerEntryTableLine> buyerEntryList = buyerTable.getItems();
        BuyerEntryTableLine beTabLine = buyerEntryList.get(curRow);
        String buyerBoxSizeType = sessionController.getBoxSize();
        beTabLine.setBoxSize(buyerBoxSizeType);
        int totGrowerQtyForBoxType = getTotalGrowerQtyForBoxSize(buyerBoxSizeType);
        if (totGrowerQtyForBoxType == -1) {
            /* An error occurred */
            return -2;
        }
        updateTable(buyerTable, buyerEntryList);
        int buyerQty = getTotalBuyerQtyForBoxSize(buyerBoxSizeType);
        if (buyerQty == -1) {
            /* An error occurred */
            return -2;
        }
        /*
		 * System.out.println("Buyer quantity for box :" + buyerQty);
		 * System.out.println("Grower quantity for box :" + totalGrowersQty);
		 * System.out.println("buyer list - before");
		 * System.out.println(buyerEntryList);
         */
        if (buyerQty < totGrowerQtyForBoxType) {
            shouldFireTabKey = true;
            addNewBuyersEntry(buyerTable, totGrowerQtyForBoxType, buyerQty, buyerBoxSizeType, curRow);
            nextTabColumnToFocus = getLastColumn(buyerTable);
            startNextColumnEdit(buyerTable, curRow, (TableColumn<BuyerEntryTableLine, String>) nextTabColumnToFocus);
            buyerTable.requestFocus();
            return -1;
        } else if (buyerQty == totGrowerQtyForBoxType) {
            shouldFireTabKey = true;
            addBuyerDataToQueryList();
            List<BuyerEntryTableLine> tmpList = new ArrayList<>(buyerLines.size() + 10);
            tmpList.addAll(buyerLines);
            buyerLines.clear();
            /* we've entered the values for a particular box type in buyer's
             * table, now switch to growers table if there are more entries to be made
             */
            int totGrowerQtyForFruitQuality = getTotalGrCountOfQtyForQuality(sessionController.quality);
            int qtyOfLotForQuality = getTotalLotEntriesForQuality(sessionController.quality);
            if (totGrowerQtyForFruitQuality < qtyOfLotForQuality) {
                GrowerEntryTableLine gtl = growerLines.get(growerLines.size() - 1);
                gtl.setNewRecord(false);
                disableTableRow(growersEntry);
                int grListLastRow = growerLines.size() - 1;
                addNewGrowersEntry(growersEntry, qtyOfLotForQuality, totGrowerQtyForFruitQuality,
                        sessionController.quality);
                nextTabColumnToFocus = getLastColumn(growersEntry);
                startNextColumnEdit(growersEntry, grListLastRow, (TableColumn<GrowerEntryTableLine, String>) nextTabColumnToFocus);
                growersEntry.requestFocus();
                return -1;
            }
            if (getTotalLotQty() < Integer.valueOf(totalQty.getText())) {
                // growersEntry.setEditable(false);
                addGrowerDataToQueryList();
                growersEntry.getItems().clear();

                // buyersEntry.setEditable(false);
                // addLots.setEditable(true);
                AddLotTableLine ltl = addLotLines.get(addLotLines.size() - 1);
                ltl.setNewRecord(false);
                disableTableRow(addLots);
                int curLotRow = addLotLines.size() - 1;
                addNewLotLine();
                nextTabColumnToFocus = getLastColumn(addLots);
                startNextColumnEdit(addLots, curLotRow, (TableColumn<AddLotTableLine, String>) nextTabColumnToFocus);
                addLots.requestFocus();
                return -1;
            } else if (getTotalLotQty() == Integer.valueOf(totalQty.getText())) {
                addGrowerDataToQueryList();
                growerLines.clear();
                addLots.setEditable(false);
                saveButton.requestFocus();
                return -1;
            }

        }
        return 1;
    }

    private void disableTableRow(TableView<?> tv) {
        // TODO: Not needed at this time, remove the following block to proceed
        // with normal execution
        if (1 != 0) {
            return;
        }
        int i = 0;
        for (Node n : tv.lookupAll("TableRow")) {
            if (n instanceof TableRow) {
                TableRow<?> row = (TableRow<?>) n;
                if (tv == addLots) {
                    updateTable(tv, addLotLines);
                    tv.refresh();
                    if (!addLots.getItems().get(i).isNewRecord()) {
                        row.setDisable(true);
                    } else {
                        row.setDisable(false);
                    }
                } else if (tv == growersEntry) {
                    updateTable(tv, growerLines);
                    tv.refresh();
                    if (!growersEntry.getItems().get(i).isNewRecord()) {
                        row.setDisable(true);
                    }
                }
                i++;
                if (i == tv.getItems().size()) {
                    break;
                }
            }
        }
    }

    //
    private int handleLastColOfGrowerTable(TableView<FreshEntryTableData.GrowerEntryTableLine> tv, int curRow,
            TableColumn.CellEditEvent event) {
        event.consume();

        TableView<FreshEntryTableData.GrowerEntryTableLine> growerTable = tv;
        ObservableList<GrowerEntryTableLine> growerEntryList = growerTable.getItems();
        GrowerEntryTableLine geTabLine = growerEntryList.get(curRow);
        /* Check if the buyer table has been filled up for all the box types */
        for (GrowerEntryTableLine geLine : growerEntryList) {
            String boxSizeType = geLine.getBoxSize().trim();
            int buyerQtyForBoxSize = getTotalBuyerQtyForBoxSize(boxSizeType);
            int growerQtyForBoxSize = getTotalGrowerQtyForBoxSize(boxSizeType);
            if (buyerQtyForBoxSize == -1 || (buyerQtyForBoxSize < growerQtyForBoxSize)) {
                // growersEntry.setEditable(false);
                // buyersEntry.setEditable(true);
                int emptyRowCountInBuyersTable = 0;
                for (BuyerEntryTableLine beLine : buyersEntry.getItems()) {
                    if (beLine.getBoxSize().isEmpty()) {
                        emptyRowCountInBuyersTable++;
                    }
                }
                if ((buyerQtyForBoxSize == 0 && emptyRowCountInBuyersTable < 1) || buyersEntry.getItems().isEmpty()) {
                    addNewBuyersEntry(buyersEntry, growerQtyForBoxSize, buyerQtyForBoxSize, boxSizeType, curRow);
                }
                buyersEntry.requestFocus();
                return -1;
            }
        }
        String qualtype = sessionController.getQuality();
        int totalLotCountForQuality = getTotalLotEntriesForQuality(qualtype);

        geTabLine.setQuality(qualtype);

        if (totalLotCountForQuality == -1) {
            /* An error occurred */
            return -2;
        }
        updateTable(growerTable, growerEntryList);
        int growerLotQty = getTotalGrCountOfQtyForQuality(qualtype);
        if (growerLotQty == -1) {
            /* An error occurred */
            return -2;
        }

        if (growerLotQty < totalLotCountForQuality) {
            // add a new entry in growers table
            int grListLastRow = growerLines.size() - 1;
            addNewGrowersEntry(growerTable, totalLotCountForQuality, growerLotQty, qualtype);
            nextTabColumnToFocus = getLastColumn(growersEntry);
            startNextColumnEdit(growersEntry, grListLastRow, (TableColumn<GrowerEntryTableLine, String>) nextTabColumnToFocus);
            growersEntry.requestFocus();
            return -1;
        } else {
            addGrowerDataToQueryList();
            disableTableRow(addLots);
            recalculate();

            updateTable(growerTable, growerEntryList);
            /*
			 * we've entered the values for a particular quality type in buyer's
			 * table, now switch to Lots table if there are more entries to be
			 * made
             */
            if (totalLotQty < Integer.valueOf(totalQty.getText())) {
                // growersEntry.setEditable(false);
                // addLots.setEditable(true);
                int curLotRow = addLotLines.size() - 1;
                addNewLotLine();
                nextTabColumnToFocus = getLastColumn(addLots);
                startNextColumnEdit(addLots, curLotRow, (TableColumn<AddLotTableLine, String>) nextTabColumnToFocus);
                addLots.requestFocus();
                return -1;
            }
        }
        return 1;

    }

    // handle lot table last column entry
    private int handleLastColOfLotsTable(TableView<FreshEntryTableData.AddLotTableLine> tv, int curRow,
            TableColumn.CellEditEvent event) {
        event.consume();
        if (areTableFieldsEmpty(tv)) {
            GeneralMethods.errorMsg("Table data cannot be empty");
            tv.requestFocus();
            return -1;
        }
        TableView<FreshEntryTableData.AddLotTableLine> lotTable = tv;
        ObservableList<AddLotTableLine> lotEntryList = lotTable.getItems();
        AddLotTableLine lotTabLine = lotEntryList.get(curRow);
        /* Check if the buyer table has been filled up for all the box types */
        for (AddLotTableLine lotLine : lotEntryList) {
            String qualityType = sessionController.getQuality();
            int growerQtyForQualityType = getTotalGrCountOfQtyForQuality(qualityType);
            int lotQtyForQualityType = getTotalLotEntriesForQuality(qualityType);
            if (growerQtyForQualityType == -1 || (growerQtyForQualityType < lotQtyForQualityType)) {
                // growersEntry.setEditable(true);
                // addLots.setEditable(false);
                int emptyRowCountInGrowersTable = 0;
                for (GrowerEntryTableLine geLine : growersEntry.getItems()) {
                    if (geLine.getBoxSize().isEmpty()) {
                        emptyRowCountInGrowersTable++;
                    }
                }
                if ((growerQtyForQualityType == 0 && emptyRowCountInGrowersTable < 1) || growerLines.isEmpty()) {
                    addNewGrowersEntry(growersEntry, lotQtyForQualityType, growerQtyForQualityType, qualityType);
                }
                nextTabColumnToFocus = null;
                disableTableRow(addLots);
                growersEntry.requestFocus();
                return -1;
            }
        }
        return 1;

    }

    /* Adds a new line to lot table */
    private void addNewLotLine() {
        int remainderQty = Integer.valueOf(totalQty.getText()) - getTotalLotQty();
        addLots.getItems().add(data.new AddLotTableLine("", String.valueOf(remainderQty), fruitSelection.getValue()));
        nextTabColumnToFocus = null;
    }

    private int getTotalLotQty() {
        int tmpQty = 0;
        for (AddLotTableLine lotLine : addLots.getItems()) {
            try {
                tmpQty += Integer.valueOf(lotLine.getQty());
            } catch (NumberFormatException e) {
                GeneralMethods.errorMsg("Error parsing Quantity in lot table to number:" + e.getMessage());
            }
        }
        return tmpQty;
    }

    /* Add a new line to growers table */
    private void addNewGrowersEntry(TableView<FreshEntryTableData.GrowerEntryTableLine> growerTable,
            int totalLotCountForQuality, int growerLotQty, String qualType) {
        int remainderQty = totalLotCountForQuality - growerLotQty;
        // System.out.println("Quality type:-" + qualType);
        growerTable.getItems().add(data.new GrowerEntryTableLine("", String.valueOf(remainderQty), "", qualType));
        updateTable(growerTable, growerLines);
        growerTable.refresh();
        /*
		 * System.out.println("buyer list - after");
		 * System.out.println(growerEntryList);
         */
        //
        /* grower select table */
        nextTabColumnToFocus = null;

    }

    private void addNewBuyersEntry(TableView<FreshEntryTableData.BuyerEntryTableLine> buyerTable,
            int totGrowerQtyForBoxType, int buyerQty, String buyerBoxSizeType, int curRow) {
        int remainderQty = totGrowerQtyForBoxType - buyerQty;
        buyerTable.getItems().add(data.new BuyerEntryTableLine("", String.valueOf(remainderQty), "", buyerBoxSizeType));

        // System.out.println(buyerLines);
        updateTable(buyerTable, buyerLines);
        buyerTable.refresh();
        //
        /* buyer select table */
        nextTabColumnToFocus = null;

    }

    /*
     * Adds the current buyer list data to sql list
     */
    private void addBuyerDataToQueryList() {
        for (FreshEntryTableData.BuyerEntryTableLine line : buyerLines) {
            String title = line.getBuyerSelect();
            if (title.toLowerCase().equals("godown")) {
                freshEntryType = STORAGE;
                line.setBuyerSelect(Buyer.GODOWN_BUYER_TITLE);
            }
            if (title.toLowerCase().equals("coldstore") || title.toLowerCase().equals("cold store")) {
                freshEntryType = STORAGE;
                line.setBuyerSelect(Buyer.COLD_STORE_BUYER_TITLE);
            }
            FreshEntryTableData.buyerEntryLinesSql.add(new String[]{line.getBuyerSelect(), grower.getText(),
                line.getBuyerQty(), addLotLines.get(addLotLines.size() - 1).getQualityType(),
                growerLines.get(growerLines.size() - 1).getBoxSize(), line.getBuyerRate(),
                "" + (Integer.parseInt(line.getBuyerRate()) * Integer.parseInt(line.getBuyerQty())), fruitSelection.getValue()});
        }
    }

    /*
	 * Adds the current grower list data to sql list
     */
    private void addGrowerDataToQueryList() {
        recalculate();
        for (FreshEntryTableData.GrowerEntryTableLine line : growerLines) {
            FreshEntryTableData.growerEntryLinesSql.add(new String[]{line.getGrowerRate(), line.getBoxSize(), line.getQuality(), line.getGrowerQty()});
        }
    }

    /*
	 * starts editing of next table column
     */
    private <T> void startNextColumnEdit(TableView<T> tv, int currRow, TableColumn<T, String> nextColInSeq) {
        if (nextColInSeq == null) {
            System.err.println(
                    "Next column in sequence is null, UI may misbehave, please confirm if this is a desired way");
        }
        // System.out.printf("Column going to have focus:%s, for the row:%d \n",
        // nextColInSeq.getText(), currRow);
        tv.getSelectionModel().clearAndSelect(currRow, nextColInSeq);
        tv.getFocusModel().focus(currRow, nextColInSeq);
//        tv.edit(currRow, nextColInSeq.getId());//Todo: fix it: , nextColInSeq
        nextTabColumnToFocus = (TableColumn<?, String>) nextColInSeq;
    }

    /*
	 * calculate the charges and net sum
     */
    private void recalculate() {
        //	System.out.println(growerLines);
        int totalGrowerSum = growerLines.stream().mapToInt(GrowerEntryTableLine::getGrossIncome).sum();
        sessionController.setSupplierGrossSum(totalGrowerSum + sessionController.getSupplierGrossSum());
        gross.setText(Utils.toStr(sessionController.getSupplierGrossSum()));
        updateAmountCalc();
    }

    @SuppressWarnings("rawtypes")
    private TableColumn getLastColumn(TableView<?> tv) {
        if (tv != null && tv.getColumns().size() > 0) {
            return tv.getColumns().get(tv.getColumns().size() - 1);
        }
        return null;
    }

    /* Checks if the table has a duplicate box size in current table */
    private boolean isDuplicateBoxSize(String newBoxSize, int curRow) {
        boolean hasDuplicate = false;
        for (int idx = 0; idx < growerLines.size(); idx++) {
            if (idx != curRow) {
                GrowerEntryTableLine geTL = growerLines.get(idx);
                if (geTL.getBoxSize().equals(newBoxSize)) {
                    hasDuplicate = true;
                    break;
                }
            }
        }
        return hasDuplicate;
    }

    private boolean isDuplicateQualityType(String newQualityType, int curRow) {
        boolean hasDuplicate = false;
        for (int idx = 0; idx < addLotLines.size(); idx++) {
            if (idx != curRow) {
                AddLotTableLine ltl = addLotLines.get(idx);
                if (ltl.getQualityType().equals(newQualityType)) {
                    hasDuplicate = true;
                    break;
                }
            }
        }
        return hasDuplicate;
    }

    private void resetFruitQualsAndBoxes() {
        String fruit = fruitSelection.getValue().toString();
        for (TextFieldTableCell cell : growerFruitColList) {
            if (!cell.isEmpty()) {
                cell.setText(fruit);
            }
        }
        sessionController.setFruitType(fruit);
        List<BoxSize> boxSizes = dbclient.getBoxSizesForFruit(fruit);
        List<QualityType> qualityTypes = dbclient.getQualityTypesForFruit(fruit);
        qualityTypesList.clear();
        for (QualityType qt : qualityTypes) {
            qualityTypesList.add(qt.getName());
        }
        boxSizesList.clear();
        for (BoxSize bs : boxSizes) {
            boxSizesList.add(bs.getName());
        }

    }

    public static String getBuyerAggregateAmount(String bRate, String boxes) {
        double totAmount = Double.parseDouble(bRate) * Double.parseDouble(boxes);
        for (ExpenseInfo ei : expenseList) {
            String type = ei.getType();
            double amount = 0;
            try {
                String defAmt = ei.getDefaultAmount() == null ? "0" : ei.getDefaultAmount().trim();
                amount = Double.parseDouble(defAmt);
            } catch (Exception ex) {
                System.out.println("Invalid amount field for expense:" + ei.getName()
                        + ", so, setting it to 0,\nExact Error:" + ex.getMessage());
                amount = 0;
            }
            if (type.trim().equals("@")) {
                totAmount += amount * Double.parseDouble(boxes);
            } else if (type.trim().equals("%")) {
                // System.out.println("amount before calc:" + totAmount);
                double percentedAmt = totAmount * (amount / 100);
                totAmount += percentedAmt;
                // System.out.println("Amount after calc:" + totAmount);
            }
        }
        return String.valueOf((int) totAmount);
    }

}// end of main class
