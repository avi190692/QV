package com.quickveggies.controller;

import java.awt.Robot;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import com.ai.util.dates.DateUtil;
import com.quickveggies.controller.dashboard.DashboardController;
import com.quickveggies.GeneralMethods;
import com.quickveggies.controller.FreshEntryTableData.BuyerEntryTableLine;
import com.quickveggies.dao.DatabaseClient;
import com.quickveggies.entities.DBuyerTableLine;
import com.quickveggies.entities.DSalesTableLine;
import com.quickveggies.entities.DSupplierTableLine;
import com.quickveggies.entities.PartyType;
import com.quickveggies.misc.AutoCompleteTableCell;
import com.quickveggies.misc.AutoCompleteTextField;
import com.quickveggies.misc.PartySearchTableCell;
import com.quickveggies.misc.Utils;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewFocusModel;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

public class UpdatePendingSalesController implements Initializable {

    @FXML
    private TableView<FreshEntryTableData.BuyerEntryTableLine> buyersEntry;

    @FXML
    private TableView<DBuyerTableLine> tvExistingBuyerEntry;

    @FXML
    private Button commitButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button btnAdjustRates;

    private final DSupplierTableLine inprocessSupplierDeal;

    private Map<String, List<DSupplierTableLine>> buyerDealMap = new LinkedHashMap<>();

    private static final String STR_ADD_NEW = "Add new...";

    private TableColumn<?, ?> nextTabColumnToFocus;

    public ObservableList<FreshEntryTableData.BuyerEntryTableLine> buyerLines = FXCollections.observableArrayList();

    private boolean shouldFireTabKey = false;

    java.util.TreeSet<String> buyersList = null;

    private ObservableList<DBuyerTableLine> existingBuyerDealList = FXCollections.observableArrayList();

    private int totGrowerQtyForBoxType;

    private DatabaseClient dbclient = DatabaseClient.getInstance();

    private DSalesTableLine saleDeal;

    private FreshEntryTableData data = new FreshEntryTableData();

    private SessionDataController sessionController = SessionDataController.getInstance();

    private final String fruit;

    public UpdatePendingSalesController(DBuyerTableLine existingBuyerDeal, DSalesTableLine saleDeal, DSupplierTableLine inprocessSupplierDeal) {
        if (existingBuyerDeal == null) {
            throw new IllegalArgumentException("Buyer deal line object shouldn't be null");
        }
        sessionController.setBoxSize(existingBuyerDeal.getBoxSizeType());
        existingBuyerDealList.add(existingBuyerDeal);
        this.saleDeal = saleDeal;
        this.inprocessSupplierDeal = inprocessSupplierDeal;
        fruit = existingBuyerDeal.getFruit();
        FreshEntryTableData.buyerEntryLinesSql = new ArrayList<>();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        buyersEntry.setEditable(true);
        FreshEntryTableData.buyerEntryLinesSql = new ArrayList<>();
        setupOldEntryTable();
        setupBuyerEntryTable();
        totGrowerQtyForBoxType = existingBuyerDealList.get(0).getCases();
        if (totGrowerQtyForBoxType > getPendingQty()) {
            addNewBuyersEntry(buyersEntry, totGrowerQtyForBoxType, 0, sessionController.getBoxSize(), 0);
        }
        buyersEntry.requestFocus();
        buildSaveButtonHandler();

        cancelButton.setOnAction((ActionEvent event) -> {
            cancelButton.getScene().getWindow().hide();
        });
        setupRatesAdjustmentFlow();
    }

    private void setupRatesAdjustmentFlow() {
        btnAdjustRates.setOnAction((ActionEvent event) -> {
            SupplierAdjustRateController controller = new SupplierAdjustRateController(saleDeal, inprocessSupplierDeal);
            Stage stage = DashboardController.showPopup("/supplieradjustrates.fxml",
                    "Pending Entries", controller);
            EventHandler<WindowEvent> we = new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    //setupOldEntryTable();
                }
            };
            stage.setOnCloseRequest(we);
            stage.setOnHiding(we);
        });
    }

    private void buildSaveButtonHandler() {
        commitButton.setOnAction((ActionEvent event) -> {
            int boughtQty = 0;
            int boughtAmt = 0;
            addBuyerDataToQueryList();
            for (int dealIndex = 0; dealIndex < FreshEntryTableData.buyerEntryLinesSql.size(); dealIndex++) {
                String[] buyerSqlLine = FreshEntryTableData.buyerEntryLinesSql.get(dealIndex);
                String title = buyerSqlLine[0];
                String bRate = buyerSqlLine[5];
                String qty = buyerSqlLine[2];

                if (title.trim().isEmpty() || qty.trim().isEmpty() || bRate.trim().isEmpty()) {
                    continue;
                }
                String aggregateAmt = FreshEntryController.getBuyerAggregateAmount(bRate, qty);
                boughtAmt += Utils.toInt(aggregateAmt);
                boughtQty += Utils.toInt(qty);

                int newBuyerDealId = dbclient.saveEntryToSql("buyerDeals",
                        new String[]{"buyerTitle", "dealDate", "buyerRate", "buyerPay", "boxes",
                            "aggregatedAmount", "dealID", "boxSizeType", "qualityType", "fruit"},
                        new String[]{buyerSqlLine[0], getDealDate(), bRate, buyerSqlLine[6],
                            qty, aggregateAmt, saleDeal.getDealID(), buyerSqlLine[4], buyerSqlLine[3], buyerSqlLine[7]});
                dbclient.addStorageBuyerDealInfo(newBuyerDealId, Utils.toInt(existingBuyerDealList.get(0).getSaleNo()));
            }
            DBuyerTableLine be = existingBuyerDealList.get(0);
            int existingAmount = Utils.toInt(be.getAggregatedAmount());
            int existingQty = be.getCases();
            int newAmount = existingAmount - boughtAmt;
            int newQty = existingQty - boughtQty;
            dbclient.updateTableEntry("buyerDeals", Integer.valueOf(be.getSaleNo()), new String[]{"boxes", "aggregatedAmount"}, new String[]{String.valueOf(newQty), String.valueOf(newAmount)}, false);
            GeneralMethods.msg("Successfully updated the entries");
            commitButton.getScene().getWindow().hide();
        });

    }

    private String getDealDate() {
        DBuyerTableLine be = existingBuyerDealList.get(0);
        String format = DateUtil.determineDateFormat(be.getDate());
        String date = LocalDate.now().toString();
        if (format != null) {
            try {
                Date d1 = DateUtil.parse(date);
                date = new SimpleDateFormat(format).format(d1);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return date;
    }

    private int getPendingQty() {
        if (buyerLines.isEmpty()) {
            return 0;
        }
        int qty = 0;
        for (FreshEntryTableData.BuyerEntryTableLine line : buyerLines) {
            qty += Utils.toInt(line.getBuyerQty());
        }
        return totGrowerQtyForBoxType - qty;
    }

    private void setupOldEntryTable() {
        TableColumn<DBuyerTableLine, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<DBuyerTableLine, String> fruitCol = new TableColumn<>("Fruit");
        fruitCol.setCellValueFactory(new PropertyValueFactory<>("fruit"));

        TableColumn<DBuyerTableLine, String> qualityCol = new TableColumn<>("Quality");
        qualityCol.setCellValueFactory(new PropertyValueFactory<>("qualityType"));

        TableColumn<DBuyerTableLine, String> boxSizeCol = new TableColumn<>("Box Size");
        boxSizeCol.setCellValueFactory(new PropertyValueFactory<>("boxSizeType"));

        TableColumn<DBuyerTableLine, String> qtyCol = new TableColumn<>("Pending Quantity");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("cases"));
        
        TableColumn<DBuyerTableLine, String> rateCol = new TableColumn<>("Rate");
        rateCol.setCellValueFactory(new PropertyValueFactory<>("buyerRate"));

        tvExistingBuyerEntry.getColumns().addAll(dateCol, fruitCol, qualityCol, qtyCol, rateCol);
        tvExistingBuyerEntry.setItems(FXCollections.observableArrayList(existingBuyerDealList));
    }

    private void setupBuyerEntryTable() {
        sessionController.setBoxSize(existingBuyerDealList.get(0).getBoxSizeType());
        TableColumn tcBuyerBoxSize = new TableColumn("Box Size");
        tcBuyerBoxSize.setCellValueFactory(new PropertyValueFactory<>("boxSize"));
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
                            String boxsize = item;
                            setItem(boxsize);
                            setText(boxsize);
                            setStyle("-fx-background-color: #a0beff; -fx-text-fill: #000000");
                        }
                    }
                };
                return cell;
            }
        });
        tcBuyerBoxSize.setEditable(false);

        buyersEntry.setItems(buyerLines);
        final TableColumn tcBuyerRate = new TableColumn("Rate Per Box");
        tcBuyerRate.setCellValueFactory(
                new PropertyValueFactory<>("buyerRate"));
        tcBuyerRate.setCellFactory(TextFieldTableCell.forTableColumn());
        tcBuyerRate.setOnEditCommit(createTableCellFieldEventHandler("buyerRate"));
        final TableColumn tcBuyerQty = new TableColumn("Quantity");
        tcBuyerQty.setCellValueFactory(
                new PropertyValueFactory<>("buyerQty"));
        tcBuyerQty.setCellFactory(TextFieldTableCell.forTableColumn());
        tcBuyerQty.setOnEditCommit(createTableCellFieldEventHandler("buyerQty"));
        final TableColumn tcBuyerSelect = new TableColumn("Select Buyer");

        tcBuyerSelect.setCellValueFactory(
                new PropertyValueFactory<>("buyerSelect"));
        tcBuyerSelect.setCellFactory(
                new javafx.util.Callback<TableColumn<FreshEntryTableData.BuyerEntryTableLine, String>, TableCell<FreshEntryTableData.BuyerEntryTableLine, String>>() {
            @Override
            public TableCell<FreshEntryTableData.BuyerEntryTableLine, String> call(
                    TableColumn<FreshEntryTableData.BuyerEntryTableLine, String> param) {
                final AutoCompleteTableCell autCompCellBuyerSelect = new AutoCompleteTableCell(
                        createAutoCompleteBuyerTextField());
                autCompCellBuyerSelect.setOnKeyReleased((KeyEvent event) -> {
                    if (event.getCode() == KeyCode.ENTER) {
                        // System.out.println("Enter key event for
                        // Buyer Select field");
                        TableRow<?> tr = autCompCellBuyerSelect.getTableRow();
                        int row = tr.getIndex();
                        // System.out.println("The row number for
                        // select buyer field:" + row);
                        nextTabColumnToFocus = tcBuyerQty;
                        buyersEntry.getFocusModel().focus(row, tcBuyerQty);
                        buyersEntry.edit(row, tcBuyerQty);
                    }
                });
                return autCompCellBuyerSelect;
            }
        });

        TableColumn tcBuyerSearch = new TableColumn();
        tcBuyerSearch.setCellFactory(
                new Callback<TableColumn<FreshEntryTableData.BuyerEntryTableLine, String>,
                        TableCell<FreshEntryTableData.BuyerEntryTableLine, String>>() {
            @Override
            public TableCell<FreshEntryTableData.BuyerEntryTableLine, String> call(
                    TableColumn<FreshEntryTableData.BuyerEntryTableLine, String> param) {
                return new PartySearchTableCell(PartyType.BUYERS);
            }
        });
        buyersEntry.getColumns().addAll(tcBuyerBoxSize, tcBuyerSelect, tcBuyerSearch, tcBuyerQty, tcBuyerRate);
        // tcBuyerBoxSize.setEditable(false);
        // key press listener for buyers entry table
        buyersEntry.setOnKeyReleased((KeyEvent event) -> {
            if (event.getCode() == KeyCode.ENTER) {
                /*
                * System.out.println("in buyer entry key pressed event");
                 */
                TablePosition<FreshEntryTableData.BuyerEntryTableLine, ?> focusedCell
                        = buyersEntry.getFocusModel().getFocusedCell();
                if (focusedCell != null) {
                    int row = focusedCell.getRow();
                    // System.out.printf("buyer entry table: row:%d and
                    // col:%s\n" , focusedCell.getRow(),
                    // focusedCell.getTableColumn());

                    if (focusedCell.getTableColumn() == tcBuyerSelect) {
                        /**
                         * This block gets executed even when the user is
                         * modifying the table cell, so, this probably due to
                         * auto complete field being used in this column. So,
                         * once the user is done with editing, we need to set
                         * the next column in sequence to have the focus. For
                         * other columns setting procedure of next column takes
                         * place in the cell commit event handler, so no need to
                         * set that info here.
                         */
                        // System.out.println("in buyer entry, buyer select,
                        // key pressed event");
                        if (focusedCell.getRow() < 1) {
                            fireKeyPress(java.awt.event.KeyEvent.VK_TAB);
                        }
                    } else if (focusedCell.getTableColumn() == tcBuyerQty) {
                        /*
                        * System.out.
                        * println("in buyer entry, buyer Quantity,  key pressed event"
                        * );
                         */
                        buyersEntry.edit(row, tcBuyerQty);
                    } else if (focusedCell.getTableColumn() == tcBuyerRate) {

                        // System.out.println("In Buyer rate column cell");
                        // System.out.println("Should fire tab key:" +
                        // shouldFireTabKey);
                        buyersEntry.edit(row, tcBuyerRate);
                        if (shouldFireTabKey) {
                            shouldFireTabKey = false;
                            fireKeyPress(java.awt.event.KeyEvent.VK_TAB);
                        }
                    }
                } else {
                    /**
                     * Although this block doesn't seem to execute MOST of the
                     * time I ran the code, however, it did, I think, at once
                     * run, so keeping this doesn't sound harmful.
                     */
                    /*
                    * System.out.
                    * println("buyer entry table, No selected rows");
                     */
                    int row = buyersEntry.getItems().size() - 1;
                    buyersEntry.getFocusModel().focus(row, tcBuyerSelect);
                    buyersEntry.edit(row, tcBuyerSelect);
                    fireKeyRelease(java.awt.event.KeyEvent.VK_TAB);
                }
            }
        });
        setFocusListenerForTableColumn(buyersEntry, tcBuyerSelect);
    }

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
        } catch (Exception ex) {
            System.out.println(
                    "Automatic key event failed, please use keyboard or mouse to get desired behavior. Exception cause : "
                    + ex.getMessage());
        }

    }

    // //Cell commit listener for table view text field cell
    @SuppressWarnings("rawtypes")
    private EventHandler<TableColumn.CellEditEvent> createTableCellFieldEventHandler(final String dataToEdit) {
        return (new EventHandler<TableColumn.CellEditEvent>() {
            @SuppressWarnings("unchecked")
            @Override
            public void handle(TableColumn.CellEditEvent event) {
                final String newCellValue = event.getNewValue().toString();

                boolean err = false;
                try {
                    if (Integer.parseInt(event.getNewValue().toString()) <= 0) {
                        GeneralMethods.errorMsg("Invalid value! Value should be a postive number");
                        err = true;
                    }
                    BuyerEntryTableLine tableLine = ((FreshEntryTableData.BuyerEntryTableLine) event.getTableView().getItems()
                            .get(event.getTablePosition().getRow()));
                    tableLine.set(dataToEdit, newCellValue);

                    if (getTotalBuyerQtyForBoxSize(sessionController.getBoxSize()) > totGrowerQtyForBoxType) {
                        GeneralMethods.errorMsg("New quantity cannot be more than pending quantity");
                        tableLine.setBuyerQty(event.getOldValue().toString());
                        buyersEntry.refresh();
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

                // check if this is the last column
                TablePosition<?, ?> tPosition = event.getTablePosition();
                int totalCols = tv.getColumns().size();
                int currColNum = tPosition.getColumn();
                int curRow = tPosition.getRow();
                /*
				 * System.out.println("The processing cell is from column " +
				 * event.getTableColumn().getText());
                 */
                if ((currColNum + 1) < totalCols) {
                    /*
					 * There are more columns in table left, so will proceed to
					 * next table only once all the columns have been filled up
                     */
                    TableColumn<?, String> nextColInSeq = (TableColumn<?, String>) tv.getColumns().get(currColNum + 1);
                    /*
					 * System.out.
					 * println("The newly focused cell is of column: " +
					 * nextColInSeq.getText());
                     */
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

                if (isLastColumn && !isLastRow) {
                    /*
					 * The column is last one but there are more rows in the
					 * table, so try to move the focus to next row
                     */
                    shouldFireTabKey = true;
                    startNextColumnEdit(tv, curRow, getLastColumn(tv));
                    tv.requestFocus();
                    return;
                }
                if (isLastRow) {
                    if (isLastColumn) {
                        num = handleLastColOfBuyerTable(tv, curRow, event);
                    }
                }
                if (num == -1 || num == -2) {
                    return;
                }

            }

        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void updateTable(TableView<?> table, ObservableList lines) {
        table.setItems(lines);
    }

    /**
     * Checks the quantities sum by buyer for particular box size, and adds new
     * lines if the buyer quantity doesn't equal to the grower's quantity for
     * the concerned box size type -1 No error, but to indicate the method
     * calling this method should return after the call to this methods -2 error
     * +1 to to proceed execution in the calling method +3 same table to set
     * focus
     */
    @SuppressWarnings("rawtypes")
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

        updateTable(buyerTable, buyerEntryList);
        int buyerQty = getTotalBuyerQtyForBoxSize(sessionController.getBoxSize());
        if (buyerQty == -1) {
            /* An error occurred */
            return -2;
        }

        if (buyerQty < totGrowerQtyForBoxType) {
            shouldFireTabKey = true;
            addNewBuyersEntry(buyerTable, totGrowerQtyForBoxType, buyerQty, sessionController.getBoxSize(), curRow);
            nextTabColumnToFocus = getLastColumn(buyerTable);
            //Todo: check it
            startNextColumnEdit(buyerTable, curRow, (TableColumn<BuyerEntryTableLine, ?>) nextTabColumnToFocus);
            buyerTable.requestFocus();
            return -1;
        } else if (buyerQty == totGrowerQtyForBoxType) {
            shouldFireTabKey = true;
            addBuyerDataToQueryList();
            List<BuyerEntryTableLine> tmpList = new ArrayList<>(buyerLines.size() + 10);
            tmpList.addAll(buyerLines);
            //buyerLines.clear();
            commitButton.requestFocus();
            // data.disableCommittedLines(growerLines);

        }
        return 1;
    }


    /*
	 * Adds the current buyer list data to sql list
     */
    private void addBuyerDataToQueryList() {
        FreshEntryTableData.buyerEntryLinesSql.clear();
        for (FreshEntryTableData.BuyerEntryTableLine line : buyerLines) {
            if (line.getBuyerSelect().isEmpty() || line.getBuyerRate().isEmpty() || line.getBuyerQty().isEmpty()) {
                continue;
            }
            FreshEntryTableData.buyerEntryLinesSql.add(new String[]{line.getBuyerSelect(), saleDeal.getSupplier(),
                line.getBuyerQty(), existingBuyerDealList.get(0).getQualityType(),
                sessionController.getBoxSize(), line.getBuyerRate(),
                "" + (Integer.parseInt(line.getBuyerRate()) * Integer.parseInt(line.getBuyerQty())), fruit});
        }
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

    private AutoCompleteTextField createAutoCompleteBuyerTextField() {
        buyersList = updateBuyersList();
        final AutoCompleteTextField buyerSelectTextField = new AutoCompleteTextField();
        buyerSelectTextField.setEntries(buyersList);
        buyerSelectTextField.linkToWindow(UpdatePendingSalesController.this, "/buyeradd.fxml", "Add new buyer", STR_ADD_NEW,
                new AddBuyerController());
        buyerSelectTextField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean focused) {
                buyerSelectTextField.getMenu().hide();
                if (focused) {
                    buyersList = updateBuyersList();
                    buyerSelectTextField.setEntries(buyersList);
                } else {
                    buyersList = updateBuyersList();
                    buyerSelectTextField.setEntries(buyersList);
                    buyerLines.get(buyerLines.size() - 1).set("buyerSelect", buyerSelectTextField.getText());
                }
            }
        });
        return buyerSelectTextField;
    }

    private java.util.TreeSet<String> updateBuyersList() {
        int rowsNum = dbclient.getRowsNum("buyers1");
        java.util.TreeSet<String> result = new java.util.TreeSet<String>();
        for (int buyr_id = 1; buyr_id <= rowsNum; buyr_id++) {
            try {
                com.quickveggies.entities.Buyer buyer = dbclient.getBuyerById(buyr_id);
                result.add(buyer.getTitle());
            } catch (java.sql.SQLException e) {
                System.out.print("sqlexception in populating buyers list");
            }
        }
        result.add(STR_ADD_NEW);
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
    @SuppressWarnings({"rawtypes", "unchecked"})
    private <T> void setFocusListenerForTableColumn(final TableView<T> tv, final TableColumn<T, ?> tc) {
        tv.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                /*
				 * System.out.println("In the table focus event");
                 */

                int listSize = tv.getItems().size();
                int row = listSize < 1 ? 0 : listSize - 1;
                if (listSize == 0) {
                    return;
                }
                TableViewFocusModel<T> tvFocusModel = tv.getFocusModel();

                // System.out.println("Current editing cell:" +
                // tv.getEditingCell());
                if (nextTabColumnToFocus != null) {
                    if (tv.getEditingCell() != null) {
                        row = tv.getEditingCell().getRow();
                        // System.out.println("Row being edited:" + row);
                    }
                    tv.getSelectionModel().clearAndSelect(row, (TableColumn<T, ?>) nextTabColumnToFocus);
                    tvFocusModel.focus(row, (TableColumn<T, ?>) nextTabColumnToFocus);
                    nextTabColumnToFocus = null;
                } else {
                    tv.getSelectionModel().clearAndSelect(row, tc);
                    tvFocusModel.focus(row, tc);
                }

            }
        });
    }

    /*
	 * starts editing of next table column
     */
    private <T> void startNextColumnEdit(TableView<T> tv, int currRow, TableColumn<T, ?> nextColInSeq) {
        if (nextColInSeq == null) {
            System.err.println(
                    "Next column in sequence is null, UI may misbehave, please confirm if this is a desired way");
        }
        // System.out.printf("Column going to have focus:%s, for the row:%d \n",
        // nextColInSeq.getText(), currRow);
        tv.getSelectionModel().clearAndSelect(currRow, nextColInSeq);
        tv.getFocusModel().focus(currRow, nextColInSeq);
        tv.edit(currRow, nextColInSeq);
        nextTabColumnToFocus = nextColInSeq;
    }

    private <T> TableColumn<T, ?> getLastColumn(TableView<T> tv) {
        if (tv != null && tv.getColumns().size() > 0) {
            return tv.getColumns().get(tv.getColumns().size() - 1);
        }
        return null;
    }

    private boolean areTableFieldsEmpty(TableView<?> table) {
        boolean result = false;
        @SuppressWarnings("rawtypes")
        ObservableList lines = table.getItems();
        Object lastLine = lines.get(lines.size() - 1);

        FreshEntryTableData.BuyerEntryTableLine line = (FreshEntryTableData.BuyerEntryTableLine) lastLine;
        if (line.getBuyerSelect().equals("") || line.getBuyerQty().equals("0")) {
            result = true;
        }
        return result;
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

}
