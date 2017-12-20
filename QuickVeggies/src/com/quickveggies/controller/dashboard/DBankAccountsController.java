package com.quickveggies.controller.dashboard;

import static com.quickveggies.controller.dashboard.DashboardController.STAGE_CLOSED_MANUALLY;
import static com.quickveggies.entities.MoneyPaidRecd.MONEY_PAID;
import static com.quickveggies.entities.MoneyPaidRecd.MONEY_RECEIVED;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;

import com.ai_int.utils.ExcelExportUtil;
import com.ai_int.utils.FileUtil;
import com.ai_int.utils.javafx.ListViewUtil;
import com.ai_int.utils.javafx.TableUtil;
import com.quickveggies.GeneralMethods;
import com.quickveggies.PaymentMethodSource;
import com.quickveggies.controller.AddAccountController;
import com.quickveggies.controller.ExpenseAddController;
import com.quickveggies.controller.FreshEntryTableData;
import com.quickveggies.controller.MoneyPaidRecdController;
import com.quickveggies.controller.MoneyPaidRecdController.AmountType;
import com.quickveggies.controller.SplitEntryController;
import com.quickveggies.dao.DatabaseClient;
import com.quickveggies.entities.Account;
import com.quickveggies.entities.AccountEntryLine;
import com.quickveggies.entities.AccountEntryPayment;
import com.quickveggies.entities.DBuyerTableLine;
import com.quickveggies.misc.AccountBox;
import com.quickveggies.misc.AutoCompleteTableCell;
import com.quickveggies.misc.AutoCompleteTextField;
import com.quickveggies.misc.CustomComboboxTableCell;
import com.quickveggies.misc.PartySearchTableCell;
import com.quickveggies.misc.TableButtonCell;
import com.quickveggies.model.EntityType;
import com.quickveggies.entities.PartyType;
import com.quickveggies.misc.DeleteTableButtonCell;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
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
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.converter.NumberStringConverter;
import javafx.util.Callback;
import com.quickveggies.model.DaoGeneratedKey;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBankAccountsController implements Initializable {

    private static final String CATEGORY_LIST_CELL = "categoryListCell";
    
    private static Integer generatedKey = null;

    @FXML
    private Label noAccountsLabel;

    @FXML
    private Button newAccount;

    @FXML
    private Label rsEstimate1;

    @FXML
    private Label openInvoice11;

    @FXML
    private Label overdue1;

    @FXML
    private Button upload;

    @FXML
    private ComboBox<?> batchActions;

    @FXML
    private AnchorPane newTransTablePane;
    @FXML
    private AnchorPane inSoftwarePane;
    @FXML
    private AnchorPane excludedPane;

    @FXML
    private ComboBox<?> batchActions1;

    @FXML
    private ComboBox<?> batchActions2;

    @FXML
    private Pane accountsView;
    @FXML
    private Label noEntriesLabel;
    @FXML
    private Label noEntriesInSoftwareLabel;
    @FXML
    private Label noEntriesExcludedLabel;
    @FXML
    private Tab newTransactionsTab;
    @FXML
    private Tab inSoftwareTab;
    @FXML
    private Tab excludedTab;
    @FXML
    private Button btnNewParty;
    @FXML
    private ComboBox<String> cboNewParty;

    @FXML
    private Button btnColSettings;

    @FXML
    private Button btnPrint;

    @FXML
    private Button btnExport;

    private final double newBoxWidthAddition = 210.0;
    private double accountsViewWidth = 0.0;

    private ArrayList<AccountBox> accountBoxesList = new ArrayList<AccountBox>();

    private AccountBox selectedAccountBox = null;

    private TableView newTransactionsTable = new TableView();
    private TableView inSoftwareTable = new TableView();
    private TableView excludedTable = new TableView();

    private boolean accountBoxSelected = false;

    private Button refresh;

    private static DatabaseClient dbclient = DatabaseClient.getInstance();

    private static List<String> withdrwalExpTypes = new ArrayList<>();
    private static List<String> depostExpTypes = new ArrayList<>();
    private static List<String> allExpTypes = new ArrayList<>();
    private static List<String> allParties = new ArrayList<>();

    private TableColumn actionCol;

    static {
        for (String expendType : dbclient.getExpenditureTypeList()) {
            withdrwalExpTypes.add(expendType);
        }
        withdrwalExpTypes.addAll(Arrays.asList(MONEY_PAID));
        depostExpTypes.add(MONEY_RECEIVED);
        allExpTypes.addAll(withdrwalExpTypes);
        allExpTypes.addAll(depostExpTypes);
        allParties.addAll(Arrays.asList(new String[]{"BUYER", "SUPPLIER"}));
    }

    public DBankAccountsController(Button refresh) {
        this.refresh = refresh;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void initialize(URL location, ResourceBundle resources) {
        btnExport.setOnAction((event) -> {
            TableView<?> tv = getSelectedTable();
            String[][] tableData = TableUtil.toArray(tv);
            String fileName = FileUtil.getSaveToFileName(btnExport.getScene(), "Select Excel file", FileUtil.getExcelExtMap());
            if (fileName != null) {
                try {
                    ExcelExportUtil.exportTableData(tableData, "Account Entry List", fileName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        btnPrint.setOnAction((event) -> TableUtil.printTable(getSelectedTable(), "Account Entries", actionCol));

        final Pane pane = ((AnchorPane) btnColSettings.getParent().getParent());
        ListViewUtil.addColumnSettingsButtonHandler(newTransactionsTable, pane, btnColSettings);

        newTransactionsTab.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
                ListViewUtil.addColumnSettingsButtonHandler(newTransactionsTable, pane, btnColSettings);
            }
        });
        inSoftwareTab.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
                ListViewUtil.addColumnSettingsButtonHandler(inSoftwareTable, pane, btnColSettings);
            }
        });
        excludedTab.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
                ListViewUtil.addColumnSettingsButtonHandler(excludedTable, pane, btnColSettings);
            }
        });
        populateAccountsView();

        cboNewParty.setItems(FXCollections.observableArrayList(allParties));
        btnNewParty.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (cboNewParty.isShowing()) {
                    cboNewParty.hide();
                } else {
                    cboNewParty.show();
                }
            }
        });
        cboNewParty.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (cboNewParty.getValue().equals("BUYER")) {
                    DBuyerController.showNewBuyerDialog();
                } else if (cboNewParty.getValue().equals("SUPPLIER")) {
                    DSupplierController.showNewSupplierDialog();
                }

            }

        });
        inSoftwarePane.getChildren().clear();
        excludedPane.getChildren().clear();
        inSoftwarePane.getChildren().add(inSoftwareTable);
        excludedPane.getChildren().add(excludedTable);

        inSoftwareTable.setEditable(false);
        inSoftwareTable.setVisible(false);
        excludedTable.setEditable(false);
        excludedTable.setVisible(false);
        newTransactionsTable.setVisible(false);
        newTransactionsTable.setEditable(false);
        newTransTablePane.getChildren().clear();
        newTransTablePane.getChildren().add(newTransactionsTable);
        setupTabPaneAnchors(inSoftwarePane);
        setupTabPaneAnchors(excludedPane);
        setupTabPaneAnchors(newTransTablePane);

        if (accountBoxesList.isEmpty()) {
            upload.setDisable(true);
        } else {
            upload.setDisable(false);
        }
        for (final AccountBox box : accountBoxesList) {
            final CheckBox selected = box.getSelectedCheckBox();
            Button delete = box.getDeleteButton();
            Button edit = box.getEditButton();
            final int boxind = accountBoxesList.indexOf(box);
            // CONFIGURE THE ACCOUNT BOXES HERE (BECAUSE OF THEIR IMPACT ON
            // OBJECTS IN THIS DASHBOARD THEY ARE LINKED TO)
            // --------------------------------------------------------------------------------
            TableColumn transIdCol = new TableColumn("Trans ID");
            transIdCol.setCellFactory(TextFieldTableCell.forTableColumn());
            transIdCol.setCellValueFactory(new PropertyValueFactory<>("transIdCol"));

            TableColumn dateCol = new TableColumn("Date");
            dateCol.setCellFactory(TextFieldTableCell.forTableColumn());
            dateCol.setCellValueFactory(new PropertyValueFactory<>("dateCol"));

            TableColumn chqnoCol = new TableColumn("Cheque No.");
            chqnoCol.setCellValueFactory(new PropertyValueFactory<>("chqnoCol"));

            TableColumn descriptionCol = new TableColumn("Comment");
            descriptionCol.setCellFactory(TextFieldTableCell.forTableColumn());
            descriptionCol.setCellValueFactory(new PropertyValueFactory<>("descriptionCol"));
            descriptionCol.setPrefWidth(220);

            TableColumn withdrawalCol = new TableColumn("Withdrawal");
            withdrawalCol.setCellFactory(
                    TextFieldTableCell.<AccountEntryLine, Number>forTableColumn(new NumberStringConverter()));
            withdrawalCol.setCellValueFactory(new PropertyValueFactory<>("withdrawalCol"));

            TableColumn depositCol = new TableColumn("Deposit");
            depositCol.setCellFactory(
                    TextFieldTableCell.<AccountEntryLine, Number>forTableColumn(new NumberStringConverter()));
            depositCol.setCellValueFactory(new PropertyValueFactory<>("depositCol"));

            TableColumn balanceCol = new TableColumn("Balance");
            balanceCol.setCellFactory(
                    TextFieldTableCell.<AccountEntryLine, Number>forTableColumn(new NumberStringConverter()));
            balanceCol.setCellValueFactory(new PropertyValueFactory<>("balanceCol"));

            TableColumn payeeCol = new TableColumn("Payee");

            TableColumn searchPayeeCol = new TableColumn();
            searchPayeeCol.setCellValueFactory(new PropertyValueFactory<>("payee"));
            searchPayeeCol.setCellFactory(
                    new Callback<TableColumn<FreshEntryTableData.BuyerEntryTableLine, String>, TableCell<FreshEntryTableData.BuyerEntryTableLine, String>>() {
                @Override
                public TableCell<FreshEntryTableData.BuyerEntryTableLine, String> call(
                        TableColumn<FreshEntryTableData.BuyerEntryTableLine, String> param) {
                    PartySearchTableCell cell = new PartySearchTableCell(PartyType.BUYER_SUPPLIERS);
                    cell.setId("partyCell");

                    return cell;
                }
            });
            searchPayeeCol.setStyle("-fx-pref-height: 0;");
            searchPayeeCol.setEditable(true);

            TableColumn<AccountEntryLine, String> payeeNameCol = new TableColumn();
            payeeNameCol.setCellValueFactory(new PropertyValueFactory<>("payee"));
            payeeNameCol.setCellFactory((final TableColumn<AccountEntryLine, String> param) -> {
                AutoCompleteTextField field = new AutoCompleteTextField();
                AutoCompleteTableCell cell = new AutoCompleteTableCell<>(field);
                field.setEditable(false);
                field.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                    if (isNowFocused) {
                        param.getTableView().getSelectionModel().clearAndSelect(cell.getTableRow().getIndex());
                    }
                });
                field.setOnKeyReleased((KeyEvent t) -> {
                    if (t.getCode() == KeyCode.BACK_SPACE) {
                        AccountEntryLine line = param.getTableView().getSelectionModel().getSelectedItem();
                        line.setPayee("");
                        field.setText("");
                        //Save to the database
                        dbclient.updateTableEntry("accountEntries", line.getId(),
                                new String[]{"payee"}, new String[]{""}, false, "");
                    }
                });
                return cell;
            });
            payeeNameCol.setPrefWidth(120);
            payeeNameCol.setEditable(false);
            payeeCol.getColumns().addAll(payeeNameCol, searchPayeeCol);

            TableColumn expenseCol = new TableColumn("Categ.");
            expenseCol.setCellValueFactory(new PropertyValueFactory<>("expense"));
            expenseCol.setCellFactory(
                    new Callback<TableColumn<AccountEntryLine, String>, TableCell<AccountEntryLine, String>>() {
                @Override
                public TableCell<AccountEntryLine, String> call(TableColumn<AccountEntryLine, String> param) {
                    ObservableList<String> expensesTypes = FXCollections.observableArrayList(withdrwalExpTypes);

                    final CustomComboboxTableCell<AccountEntryLine, String> comboBoxCell = new CustomComboboxTableCell<>(
                            expensesTypes);

                    comboBoxCell.setComboBoxHandler((ObservableValue<? extends String> obs, String oldValue, String newValue) -> {
                        Object item = null;
                        if (comboBoxCell.getTableRow() != null) {
                            item = comboBoxCell.getTableRow().getItem();
                        }
                        AccountEntryLine line;
                        if (item != null) {
                            line = (AccountEntryLine) item;
                            line.setExpense(newValue);
                        }
                    });

                    comboBoxCell.getComboBox().focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                        AccountEntryLine line = (AccountEntryLine) comboBoxCell.getTableRow().getItem();
                        if (line != null) {
                            if (line.getDepositCol() == 0) {
                                comboBoxCell.getComboBox()
                                        .setItems(FXCollections.observableArrayList(withdrwalExpTypes));
                            } else {
                                comboBoxCell.getComboBox()
                                        .setItems(FXCollections.observableArrayList(depostExpTypes));
                            }
                        }
                    });
                    comboBoxCell.setId(CATEGORY_LIST_CELL);

                    return comboBoxCell;
                }
            });
            expenseCol.setPrefWidth(140);

            actionCol = new TableColumn("Action");

            TableColumn<AccountEntryLine, String> addCol = new TableColumn();
            addCol.setCellValueFactory(new PropertyValueFactory<>("id"));
            addCol.setCellFactory(
                    new Callback<TableColumn<AccountEntryLine, String>, TableCell<AccountEntryLine, String>>() {
                @Override
                public TableCell<AccountEntryLine, String> call(TableColumn<AccountEntryLine, String> param) {
                    final TableButtonCell cell = new TableButtonCell("ADD");
                    Button button = cell.getButton();
                    button.setOnMouseClicked((MouseEvent event) -> {
                        if (cell.getCellProperty() == null) {
                            return; // ignore empty cells
                        }
                        AccountEntryLine line;
                        try {
                            // line =
                            // dbclient.getAccountEntryLine(Integer.parseInt(cell.getCellProperty()));//
                            line = (AccountEntryLine) cell.getTableRow().getItem();
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                        String expense = line.getExpense();
                        if (expense == null || expense.isEmpty() || !allExpTypes.contains(expense)) {
                            GeneralMethods.errorMsg("can't add : expense category not specified");
                            return;
                        }
                        if (line.getStatus() == AccountEntryLine.INSOFTWARE) {
                            GeneralMethods.errorMsg("entry already in software");
                            return;
                        }
                        // update the status of the relevant account
                        // entry line INSIDE the array of lines for this table
                        Stage stage = buildWindowForExpense(line, getEntityType(line.getPayeeType()),
                                selectedAccountBox.getAccountName());
                        Boolean closedManually = (Boolean) stage.getProperties().get(STAGE_CLOSED_MANUALLY);

                        if (!closedManually) {
                            dbclient.updateTableEntry("accountEntries", line.getId(),
                                    new String[]{"status", "payee", "expense"},
                                    new String[]{AccountEntryLine.INSOFTWARE + "", line.getPayee(),
                                        line.getExpense()},
                                    false, "Bank entry assigned in software");
                        }
                        // repopulate the newTransactionsTable
                        populateAccountTransactionsTable(selectedAccountBox);
                    });
                    return cell;
                }
            });
            addCol.setPrefWidth(80);

            TableColumn excCol = new TableColumn();
            excCol.setCellValueFactory(new PropertyValueFactory<>("status"));
            excCol.setCellFactory(
                    new Callback<TableColumn<AccountEntryLine, String>, TableCell<AccountEntryLine, String>>() {
                @Override
                public TableCell<AccountEntryLine, String> call(TableColumn<AccountEntryLine, String> param) {
                    final TableButtonCell cell = new TableButtonCell("EXC.");
                    Button button = cell.getButton();
                    button.setOnMouseClicked(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            AccountEntryLine line = (AccountEntryLine) cell.getTableRow().getItem();
                            if (line.getStatus() == AccountEntryLine.EXCLUDED) {
                                GeneralMethods.errorMsg("entry already excluded");
                                return;
                            }
                            line.setStatus(AccountEntryLine.EXCLUDED);
                            // update new status in sql server
                            dbclient.updateTableEntry("accountEntries", line.getId(), new String[]{"status"},
                                    new String[]{line.getStatus() + ""}, false,
                                    "Excluded bank acc entry");
                            populateAccountTransactionsTable(selectedAccountBox);
                        }
                    });
                    return cell;
                }
            });
            excCol.setPrefWidth(80);

            TableColumn splitCol = new TableColumn();
            splitCol.setCellValueFactory(new PropertyValueFactory<>("accountName"));
            splitCol.setCellFactory(
                    new Callback<TableColumn<AccountEntryLine, String>, TableCell<AccountEntryLine, String>>() {
                @Override
                public TableCell<AccountEntryLine, String> call(TableColumn<AccountEntryLine, String> param) {
                    final TableButtonCell cell = new TableButtonCell("SPLIT");
                    Button button = cell.getButton();
                    button.setOnMouseClicked((MouseEvent event) -> {
                        for (AccountBox box1 : accountBoxesList) {
                            box1.getSelectedCheckBox().setSelected(false);
                            newTransactionsTable.setVisible(false);
                            noEntriesLabel.setVisible(false);
                        }
                        TableRow tr = cell.getTableRow();
                        AccountEntryLine line = (AccountEntryLine) tr.getItem();
                        List<String> items = line.getDepositCol() == 0 ? withdrwalExpTypes : depostExpTypes;
                        SplitEntryController controller = new SplitEntryController(line, items);
                        DashboardController.showPopup("/fxml/split_entry.fxml", "Split Entry", controller);
                        if (line.getDepositCol() == 0 && line.getWithdrawalCol() == 0) {
                            dbclient.updateTableEntry("accountEntries", line.getId(),
                                    new String[]{"payee", "depositCol", "withdrawalCol"},
                                    new String[]{line.getPayee(), "0", "0"},
                                    false);
                        }
                        // repopulate the newTransactionsTable
                        populateAccountTransactionsTable(selectedAccountBox);
                    });
                    return cell;
                }
            });
            splitCol.setPrefWidth(80);

            TableColumn buffer = new TableColumn();
            buffer.setPrefWidth(80);

            actionCol.getColumns().addAll(addCol, excCol, splitCol, buffer);

            // ---------------------
            newTransactionsTable.getColumns().setAll(transIdCol, dateCol, chqnoCol, descriptionCol, withdrawalCol,
                    depositCol, balanceCol, payeeCol, expenseCol, actionCol);

            selected.setOnAction((ActionEvent event) -> {
                if (selected.isSelected()) {
                    accountBoxSelected = true;
                    selectedAccountBox = box;
                    // deselect other boxes
                    for (AccountBox otherBox : accountBoxesList) {
                        if (accountBoxesList.indexOf(otherBox) == boxind) {
                            continue;
                        }
                        otherBox.getSelectedCheckBox().setSelected(false);
                    }
                    // newTransactionsTable.getColumns().clear();
                    newTransactionsTable.setItems(null);
                    populateAccountTransactionsTable(box);

                } else {
                    // one account always selected
//                     accountBoxSelected=false;
//                     tablesDisappear();
                    selected.setSelected(true);
                }
            } // end of handler for 'select' check box
            );

            delete.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    if (selected.isSelected()) {
                        // if this account is currently displaying its tables, clear them
                        tablesDisappear();
                    }
                    try {
                        // warn the user before deleting several lines:
                        final Stage dialogStage = new Stage();
                        Button ok = new Button("OK");
                        ok.setOnMouseClicked(new EventHandler<MouseEvent>() {
                            @Override
                            public void handle(MouseEvent event) {
                                dbclient.deleteTableEntries("accounts", "acc_name", box.getAccountName(), true);
                                dbclient.deleteTableEntries("templates", "accountName", box.getAccountName(), false);
                                dbclient.deleteTableEntries("accountEntries", "accountName", box.getAccountName(), true);
                                refresh.fire();
                                selectAccountBox(accountBoxesList.get(0));
                                dialogStage.close();
                            }
                        });

                        Button cancel = new Button("Cancel");
                        cancel.setOnMouseClicked(new EventHandler<MouseEvent>() {
                            @Override
                            public void handle(MouseEvent event) {
                                dialogStage.close();
                            }
                        });
                        ok.setLayoutX(25.0);
                        ok.setLayoutY(150.0);
                        cancel.setLayoutX(150.0);
                        cancel.setLayoutY(150.0);

                        String msg = "This account has entries linked to it.\ndeleting the account will delete them to.\ncontinue?";
                        // --------------------
                        try {
                            if (dbclient.getAccountEntryLines(box.getAccountName()).size() > 0) {
                                GeneralMethods.confirm(new Button[]{ok, cancel}, dialogStage,
                                        DBankAccountsController.this, msg);
                            }
                        } catch (NoSuchElementException e) { // no linked
                            // entries, delete the account right away
                            dbclient.deleteTableEntries("accounts", "acc_name", box.getAccountName(), true);
                        }

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    refresh.fire();
                    if (accountBoxesList.size() > 0) {
                        selectAccountBox(accountBoxesList.get(0));
                    }
                }
            });
            edit.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {

                    final Stage addAccount = new Stage();
                    addAccount.centerOnScreen();
                    addAccount.setTitle("Add new bank account");
                    addAccount.initModality(Modality.APPLICATION_MODAL);
                    addAccount.setOnHiding(new EventHandler<WindowEvent>() {
                        public void handle(WindowEvent event) {
                            // populateAccountsView();
                            refresh.fire();
                            selectAccountBox(accountBoxesList.get(0));
                        }
                    });
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/bankadd.fxml"));
                        Account accountToEdit = dbclient.getAccountByName(box.getAccountName());
                        AddAccountController controller = new AddAccountController(accountToEdit);
                        loader.setController(controller);
                        Parent parent = loader.load();
                        Scene scene = new Scene(parent);
                        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
                            public void handle(KeyEvent event) {
                                if (event.getCode() == KeyCode.ESCAPE) {
                                    addAccount.close();
                                }
                            }
                        });
                        addAccount.setScene(scene);
                        addAccount.show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (SQLException exp) {
                        exp.printStackTrace();
                    }
                }
            });
        }
        //PANE TABS DEFINITIONS:
        inSoftwareTab.setOnSelectionChanged(new EventHandler<Event>() {
            public void handle(Event event) {
                if (accountBoxSelected) {
                    updateTabTables(inSoftwareTable, AccountEntryLine.INSOFTWARE, noEntriesInSoftwareLabel);
                }
            }
        });
        excludedTab.setOnSelectionChanged(new EventHandler<Event>() {
            public void handle(Event event) {
                if (accountBoxSelected) {
                    updateTabTables(excludedTable, AccountEntryLine.EXCLUDED, noEntriesExcludedLabel);
                }
            }
        });
        upload.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                for (AccountBox box : accountBoxesList) {
                    box.getSelectedCheckBox().setSelected(false);
                    newTransactionsTable.setVisible(false);
                    noEntriesLabel.setVisible(false);
                }
                final Stage stage = new Stage();
                stage.centerOnScreen();
                stage.setTitle("Upload Account Transaction");
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setOnHiding(new EventHandler<WindowEvent>() {
                    public void handle(WindowEvent event) {
                        selectedAccountBox.getSelectedCheckBox().fire();
                        populateAccountTransactionsTable(selectedAccountBox);
                    }
                });
                try {
                    Parent parent = FXMLLoader.load(getClass().getResource("/fxml/uploadxls.fxml"));
                    Scene scene = new Scene(parent);
                    scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
                        public void handle(KeyEvent event) {
                            if (event.getCode() == KeyCode.ESCAPE) {
                                stage.close();
                            }
                        }
                    });
                    stage.setScene(scene);
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        newAccount.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                final Stage addAccount = new Stage();
                addAccount.centerOnScreen();
                addAccount.setTitle("Add new bank account");
                addAccount.initModality(Modality.APPLICATION_MODAL);
                addAccount.setOnHiding(new EventHandler<WindowEvent>() {
                    public void handle(WindowEvent event) {
                        // populateAccountsView();
                        refresh.fire();
                        if (accountBoxesList != null && !accountBoxesList.isEmpty()) {
                            selectAccountBox(accountBoxesList.get(0));
                        }
                    }
                });
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/bankadd.fxml"));
                    AddAccountController controller = new AddAccountController(null);
                    loader.setController(controller);
                    Parent parent = loader.load();
                    Scene scene = new Scene(parent);
                    scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
                        public void handle(KeyEvent event) {
                            if (event.getCode() == KeyCode.ESCAPE) {
                                addAccount.close();
                            }
                        }
                    });
                    addAccount.setScene(scene);
                    addAccount.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        if (accountBoxesList.size() > 0) {
            selectAccountBox(accountBoxesList.get(0));
        }
    }// end of initialize section

    private void setupTabPaneAnchors(AnchorPane pane) {
        if (pane.getChildren().get(0) != null) {
            AnchorPane.setTopAnchor(pane.getChildren().get(0), 1.0);
            AnchorPane.setLeftAnchor(pane.getChildren().get(0), 1.0);
            AnchorPane.setBottomAnchor(pane.getChildren().get(0), 1.0);
            AnchorPane.setRightAnchor(pane.getChildren().get(0), 1.0);
        }
    }

    private TableView<?> getSelectedTable() {
        TableView<?> tv = null;
        if (newTransactionsTab.isSelected()) {
            tv = newTransactionsTable;
        } else if (inSoftwareTab.isSelected()) {
            tv = inSoftwareTable;
        } else if (excludedTab.isSelected()) {
            tv = excludedTable;
        }
        return tv;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void updateTabTables(TableView table, int status, Label noEntriesLabel) {
        // reset the table
        table.getItems().clear();
        table.getColumns().clear();
        // repopulate the table
        ObservableList<TableColumn> cols = newTransactionsTable.getColumns();
        for (int colInd = 0; colInd < 6; colInd++) {
            TableColumn col = cols.get(colInd);
            TableColumn newcol = new TableColumn(col.getText());
            newcol.setPrefWidth(col.getPrefWidth());
            newcol.setCellFactory(col.getCellFactory());
            newcol.setCellValueFactory(col.getCellValueFactory());
            table.getColumns().add(newcol);
        }
        if (status == AccountEntryLine.INSOFTWARE ||
                status == AccountEntryLine.EXCLUDED) {
            // for this tables, there are 2 specific columns
            if (status == AccountEntryLine.INSOFTWARE) {
                TableColumn payeeTxtFieldCol = new TableColumn("Payee");
                payeeTxtFieldCol.setCellFactory(TextFieldTableCell.forTableColumn());
                payeeTxtFieldCol.setCellValueFactory(new PropertyValueFactory<>("payee"));

                TableColumn expenseTxtFieldCol = new TableColumn("Categ.");
                expenseTxtFieldCol.setCellFactory(TextFieldTableCell.forTableColumn());
                expenseTxtFieldCol.setCellValueFactory(new PropertyValueFactory<>("expense"));
                table.getColumns().addAll(payeeTxtFieldCol, expenseTxtFieldCol);
            }
            TableColumn deleteCol = new TableColumn();
            deleteCol.setCellValueFactory(new PropertyValueFactory<>("id"));
            deleteCol.setCellFactory(
                    new Callback<TableColumn<AccountEntryLine, Integer>, TableCell<AccountEntryLine, Integer>>() {

                @Override
                public TableCell<AccountEntryLine, Integer> call(TableColumn<AccountEntryLine, Integer> param) {
                    DeleteTableButtonCell cell = new DeleteTableButtonCell("", "");
                    cell.setOnButtonPress(()-> {
                        //Move the entity from inSoftware or Excluded tab to newTransactions tab
                        AccountEntryLine line = (AccountEntryLine) cell.getTableRow().getItem();
                        int id = line.getId();
                        if (status == AccountEntryLine.INSOFTWARE) {
                            //Move the entity from inSoftware or Excluded tab to newTransactions tab
                            dbclient.setAccountEntryStatus(id, 0,
                                    "Removed InSoftware Entry from system");
                        }
                        else {
                            dbclient.setAccountEntryStatus(id, 0,
                                    "Removed Excluded Entry from system");
                        }
                        try {
                            AccountEntryLine parent = dbclient.getAccountEntryLine(line.getParentId());
                            if (line.getWithdrawalCol() > 0.) {
                                double leftAmount = parent.getWithdrawalCol() + line.getWithdrawalCol();
                                dbclient.updateTableEntry("accountEntries", line.getParentId(),
                                        "withdrawalCol", String.valueOf(leftAmount), "");
                            }
                            else {
                                double leftAmount = parent.getDepositCol() + line.getDepositCol();
                                dbclient.updateTableEntry("accountEntries", line.getParentId(),
                                        "depositCol", String.valueOf(leftAmount), "");
                            }
                            dbclient.deleteAccountEntry(id);
                            List<AccountEntryPayment> payments = dbclient.getAccountEntryPayments(id);
                            if (!payments.isEmpty()) {
                                AccountEntryPayment payment = payments.get(0);
                                if ("partyMoney".equalsIgnoreCase(payment.getPaymentTable())) {
                                    dbclient.deleteMoneyPaidRecd(payment.getPaymentId());
                                }
                                else if ("expenditures".equalsIgnoreCase(payment.getPaymentTable())) {
                                    dbclient.deleteExpenditureEntry(payment.getPaymentId(), true);
                                }
                            }
                            dbclient.deleteAccountPaymentByEntryId(id);
                            dbclient.setAccountEntryStatus(parent.getId(), 0, "");
                        }
                        catch (SQLException | NoSuchElementException ex) {
                            Logger.getLogger(DBankAccountsController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        if (status == AccountEntryLine.INSOFTWARE) {
                            populateAccountTransactionsTable(selectedAccountBox);
                            updateTabTables(inSoftwareTable, AccountEntryLine.INSOFTWARE, noEntriesInSoftwareLabel);
                        }
                        else {
                            populateAccountTransactionsTable(selectedAccountBox);
                            updateTabTables(excludedTable, AccountEntryLine.EXCLUDED, noEntriesExcludedLabel);
                        }
                    });
                    return cell;
                }
            });
            table.getColumns().add(deleteCol);
        }
        ObservableList<AccountEntryLine> accLines = FXCollections.observableArrayList();
        try {
            accLines = dbclient.getAccountEntryLines(selectedAccountBox.getAccountName());
        } catch (SQLException | NoSuchElementException e) {
            System.err.printf("Exception while reading entries for account:%s\n", selectedAccountBox.getAccountName());
        }
        for (AccountEntryLine line : accLines) {
            if (line.getStatus() == status) {
                table.getItems().add(line);
            }
        }
        // if no entries found, display the message
        if (table.getItems().isEmpty()) {
            table.setVisible(false);
            noEntriesLabel.setVisible(true);
        } else {
            noEntriesLabel.setVisible(false);
            table.setVisible(true);
        }
    }

    // populate the accounts view
    private void populateAccountsView() {
        int accountsNum = dbclient.getRowsNum("accounts");
        accountsView.getChildren().clear();
        if (accountsNum > 0) {
            noAccountsLabel.setVisible(false);
            for (int i = 0; i < accountsNum; i++) {
                AccountBox accountBox = new AccountBox(i * newBoxWidthAddition);
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/accountbox.fxml"));
                loader.setController(accountBox);
                accountsViewWidth += newBoxWidthAddition;
                accountsView.setPrefWidth(accountsViewWidth);
                try {
                    accountsView.getChildren().add((Node) loader.load());
                    accountBox.linkBankAccount(dbclient.getAccountById(i + 1));
                    accountBoxesList.add(accountBox);
                } catch (IOException e) {
                    System.out.println("ioexception while populating accounts view");
                    e.printStackTrace();
                } catch (SQLException e) {
                    System.out.println("sqlexception while populating accounts view");
                    e.printStackTrace();
                }
            }
        } else {
            accountsView.setVisible(false);
            noAccountsLabel.setVisible(true);
        }
    }

    @SuppressWarnings("unchecked")
    private void populateAccountTransactionsTable(AccountBox box) {
        // POPULATE AND DISPLAY THE ACCOUNT TRANSACTIONS TABLE
        // get the transaction entries
        ObservableList<AccountEntryLine> accEntryLines = null;
        ObservableList<AccountEntryLine> newTransEntryLines = FXCollections.observableArrayList();
        try {
            accEntryLines = dbclient.getAccountEntryLines(box.getAccountName());
        }
        catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        catch (NoSuchElementException ex) {
            newTransactionsTable.setVisible(false);
            noEntriesLabel.setVisible(true);
            return;
        }
        // CALCULATE CHANGES IN THE BANK BALANCE:
        Account account;
        try {
            account = dbclient.getAccountByName(box.getAccountName());
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        AccountEntryLine entry = accEntryLines.get(0);
        double balance = entry.getBalanceCol();
        box.setBalance(balance);
        dbclient.updateTableEntry("accounts", account.getId(), new String[]{"balance"},
                new String[]{balance + ""}, false, "");
        newTransactionsTable.setVisible(true);
        noEntriesLabel.setVisible(false);

        balance = 0;
        // fill the new transactions table with the relevant entries
        for (AccountEntryLine line : accEntryLines) {
            if (line.getStatus() == AccountEntryLine.DEFAULT) {
                newTransEntryLines.add(line);
            }
            if (line.getStatus() == AccountEntryLine.INSOFTWARE) {
                balance += line.getDepositCol() - line.getWithdrawalCol();
            }
        }
        box.setSoftwareBalance(balance);
        newTransactionsTable.setItems(newTransEntryLines);
        newTransactionsTable.refresh();
    }

    @SuppressWarnings("unchecked")
    private void tablesDisappear() {
        newTransactionsTable.setItems(null);
        newTransactionsTable.setVisible(false);
        inSoftwareTable.setVisible(false);
        noEntriesLabel.setVisible(false);
        excludedTable.setVisible(false);
        noEntriesExcludedLabel.setVisible(false);
        noEntriesInSoftwareLabel.setVisible(false);
    }

    private void selectAccountBox(AccountBox box) {
        if (accountBoxesList.isEmpty()) {
            return;
        }
        accountBoxSelected = true;
        selectedAccountBox = box;
        for (AccountBox accbox : accountBoxesList) {
            accbox.getSelectedCheckBox().setSelected(false);
        }
        selectedAccountBox.getSelectedCheckBox().setSelected(true);
        // newTransactionsTable.getColumns().clear();
        newTransactionsTable.setItems(null);
        populateAccountTransactionsTable(box);
    }

    public static Stage buildWindowForExpense(AccountEntryLine line, EntityType eType, String bankName) {
        generatedKey = null;
        String expense = line.getExpense();
        Initializable controller;
        String resource;
        String title;
        String table = "";
        switch (expense) {
            case MONEY_PAID:
                controller = new MoneyPaidRecdController(eType, AmountType.PAID, false,
                        Double.toString(line.getWithdrawalCol()), PaymentMethodSource.Bank, line.getPayee(),
                        line.getDateCol(), line.getChqnoCol(), bankName);
                title = "Money Paid Entry";
                resource = "/fxml/moneypaid.fxml";
                table = "partyMoney";
                break;

            case MONEY_RECEIVED:
                controller = new MoneyPaidRecdController(eType, AmountType.RECEIVED, false,
                        Double.toString(line.getDepositCol()), PaymentMethodSource.Bank, line.getPayee(),
                        line.getDateCol(), line.getChqnoCol(), bankName);
                title = "Money Received Entry";
                resource = "/fxml/moneypaid.fxml";
                table = "partyMoney";
                break;

            default:
                controller = new ExpenseAddController(Double.toString(
                        line.getWithdrawalCol()), line.getPayee(), expense,
                        line.getPayeeType(), line.getDateCol(),
                        line.getChqnoCol(), line.getAccountName(),
                        line.getDescriptionCol(), PaymentMethodSource.Bank);
                title = "Add Expense";
                resource = "/fxml/expenditureadd.fxml";
                table = "expenditures";
        }
        Stage stage = DashboardController.showPopup(resource, title, controller);
        Integer key = ((DaoGeneratedKey) controller).getGeneratedKey();
        if (key != null) {
            generatedKey = dbclient.addAccountEntryPayment(
                    new AccountEntryPayment(null, line.getId(), table, key));
        }
        return stage;
    }
    
    public static final Integer getGeneratedKey() {
       return generatedKey;
    }

    private EntityType getEntityType(String partyType) {
        return EntityType.getEntityTypeForValue(partyType);
    }
}
