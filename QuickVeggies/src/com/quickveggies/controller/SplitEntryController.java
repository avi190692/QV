package com.quickveggies.controller;

import static com.quickveggies.controller.dashboard.DashboardController.STAGE_CLOSED_MANUALLY;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Optional;

import com.quickveggies.GeneralMethods;
import com.quickveggies.dao.DatabaseClient;
import com.quickveggies.entities.AccountEntryLine;
import com.quickveggies.misc.AutoCompleteTableCell;
import com.quickveggies.misc.AutoCompleteTextField;
import com.quickveggies.misc.CustomComboboxTableCell;
import com.quickveggies.misc.PartySearchTableCell;
import com.quickveggies.misc.TableButtonCell;
import com.quickveggies.misc.TableTextCell;
import com.quickveggies.misc.Utils;
import com.quickveggies.model.EntityType;
import com.quickveggies.entities.PartyType;
import com.quickveggies.controller.dashboard.DBuyerController;
import com.quickveggies.controller.dashboard.DSupplierController;
import com.quickveggies.controller.dashboard.DBankAccountsController;
import com.quickveggies.entities.AccountEntryPayment;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Callback;

public class SplitEntryController implements Initializable {

    @FXML
    private Button split;

    @FXML
    private Label sumLeftLabel;

    @FXML
    private Pane splitMainPane;

    @FXML
    private Button btnNewParty;
    @FXML
    private ComboBox<String> cboNewParty;

    private static List<String> allParties = new ArrayList<>();

    private AccountEntryLine origLine = null;

    private DatabaseClient dbclient = DatabaseClient.getInstance();

    private ArrayList<AccountEntryLine> entryLines = new ArrayList<AccountEntryLine>();

    private Set<Integer> applyedLines = new HashSet<>();
    
    private final int WITHDRAWAL = 1, DEPOSIT = 2;

    private int transactionType;

    // private boolean confirm=false;
    private double prevAmount = 0.0;

    private final double origAmount;

    private List<String> expTypes;

    private Button addLine = new Button();

    public SplitEntryController(AccountEntryLine line, List<String> expTypes) {
        this.origLine = line;
        this.expTypes = expTypes;
        
        if (origLine.getWithdrawalCol() > 0.) {
            origAmount = origLine.getWithdrawalCol();
        }
        else {
            origAmount = origLine.getDepositCol();
        }
    }

    static {
        allParties.addAll(Arrays.asList(new String[]{"BUYER", "SUPPLIER"}));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        applyedLines.clear();
        if (origLine.getWithdrawalCol() > 0.) {
            transactionType = WITHDRAWAL;
            sumLeftLabel.setText(origLine.getWithdrawalCol() + "");
        } else {
            transactionType = DEPOSIT;
            sumLeftLabel.setText(origLine.getDepositCol() + "");
        }
        cboNewParty.setItems(FXCollections.observableArrayList(allParties));
        btnNewParty.setOnAction((ActionEvent event) -> {
            if (cboNewParty.isShowing()) {
                cboNewParty.hide();
            } else {
                cboNewParty.show();
            }
        });
        cboNewParty.setOnAction((ActionEvent event) -> {
            if (cboNewParty.getValue().equals("BUYER")) {
                DBuyerController.showNewBuyerDialog();
            } else if (cboNewParty.getValue().equals("SUPPLIER")) {
                DSupplierController.showNewSupplierDialog();
            }
        });
        // BUILD THE ENTRY TABLE
        int tableY = 100;
        final TableView table = new TableView();
        table.setLayoutX(10);
        table.setLayoutY(tableY);
        table.setPrefHeight(200.0);
        table.setPrefWidth(560.0);

        TableColumn expenseCol = new TableColumn("Categ.");
        expenseCol.setCellValueFactory(new PropertyValueFactory<>("expense"));
        expenseCol.setCellFactory(
                new javafx.util.Callback<TableColumn<AccountEntryLine, String>, TableCell<AccountEntryLine, String>>() {
            @Override
            public TableCell<AccountEntryLine, String> call(TableColumn<AccountEntryLine, String> param) {
                ObservableList<String> expensesTypes = FXCollections.observableArrayList(expTypes);
                final CustomComboboxTableCell comboBoxCell = new CustomComboboxTableCell(expensesTypes);

                comboBoxCell.setComboBoxHandler(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> obs, String oldValue,
                            String newValue) {
                        Object item = comboBoxCell.getTableRow().getItem();
                        AccountEntryLine line = null;
                        if (item != null) {
                            line = (AccountEntryLine) item;
                            line.setExpense(comboBoxCell.getComboBox().getValue().toString());
                        }
                        sumLeftLabel.setText(String.valueOf(origAmount - getTotalLinesAmt()));
                    }
                });
                return comboBoxCell;
            }
        });
        expenseCol.setStyle("-fx-pref-width: 140;");

        TableColumn amountCol = new TableColumn("Amount");
        if (transactionType == WITHDRAWAL) {
            amountCol.setCellValueFactory(new PropertyValueFactory<AccountEntryLine, String>("withdrawalCol"));
        } else if (transactionType == DEPOSIT) {
            amountCol.setCellValueFactory(new PropertyValueFactory<AccountEntryLine, String>("depositCol"));
        }
        amountCol.setCellFactory(
                new javafx.util.Callback<TableColumn<AccountEntryLine, String>, TableCell<AccountEntryLine, String>>() {
            @Override
            public TableCell<AccountEntryLine, String> call(TableColumn<AccountEntryLine, String> param) {
                final TableTextCell cell = new TableTextCell(null);
                final TextField txtField = cell.getTextField();
                txtField.focusedProperty().addListener((ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean aBoolean2) -> {
                    Object item = cell.getTableRow().getItem();
                    AccountEntryLine line = null;
                    if (item != null) {
                        line = (AccountEntryLine) item;
                    }
                    
                    if (aBoolean2) {
                        // focus gained. save the current value for
                        // if the user plans to change
                        // the input
                        try {
                            if (txtField.getText() != null) {
                                prevAmount = Double.parseDouble(txtField.getText());
                            }
                        } catch (NumberFormatException e) {
                        }
                    } else {// focus lost
                        Double remainingAmt = Double.parseDouble(sumLeftLabel.getText());
                        double curValue = 0;
                        try {
                            curValue = Double.parseDouble(txtField.getText());
                            resetLineAmount(line, curValue);
                        } catch (NumberFormatException e) {
                            GeneralMethods.errorMsg("Please enter a valid number for amount");
                            txtField.setText("" + prevAmount);
                            txtField.requestFocus();
                            return;
                        }
                        double totalNewLineSum = getTotalLinesAmt();
                        
                        if ((totalNewLineSum) > origAmount) {
                            remainingAmt = totalNewLineSum;// - getLineAmount(line);
                            GeneralMethods.errorMsg("split sum can't exceed the total entry sum");
                            txtField.setText(remainingAmt.toString());
                            txtField.requestFocus();
                            return;
                        }
                        // recalculate the sum left in original
                        // entry
                        sumLeftLabel.setText((origAmount - (totalNewLineSum)) + "");
                    }
                });
                return cell;
            }
        });
        TableColumn actionCol = new TableColumn("Action");

        TableColumn<AccountEntryLine, Integer> deleteCol = new TableColumn<>();
        // deleteCol.setCellValueFactory(new
        // PropertyValueFactory<AccountEntryLine,String>("id"));
        deleteCol.setCellFactory(
                new javafx.util.Callback<TableColumn<AccountEntryLine, Integer>, TableCell<AccountEntryLine, Integer>>() {
            @Override
            public TableCell<AccountEntryLine, Integer> call(TableColumn<AccountEntryLine, Integer> param) {
                final TableButtonCell<AccountEntryLine, Integer> cell = new TableButtonCell<AccountEntryLine, Integer>(null);
                Button button = cell.getButton();

                button.setPrefSize(20, 20);
                BackgroundImage deleteBackgroundImage = new BackgroundImage(
                        new Image(getClass().getResource("/icons/delete.png").toExternalForm(), 20, 20, true,
                                true),
                        BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                        BackgroundSize.DEFAULT);
                Background deleteBackground = new Background(deleteBackgroundImage);
                button.setBackground(deleteBackground);

                button.setOnMouseClicked((MouseEvent event) -> {
                    AccountEntryLine line = (AccountEntryLine) cell.getTableRow().getItem();
                    if (line ==null) {
                        return; // ignore empty cells
                    }
                    if (line.getId() != null) {
                        Alert alert = new Alert(AlertType.CONFIRMATION);
                        alert.setTitle("Confirmation Dialog");
                        alert.setHeaderText("Account Splitted Entry Undo");
                        alert.setContentText("Are you ok to remove Splitted Entry?");
                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.get() != ButtonType.OK) {
                            return;
                        }
                        dbclient.setAccountEntryStatus(line.getId(), 0, "Account Entry line removed from InSoftware");
                        AccountEntryPayment payment = dbclient.getAccountEntryPayment(line.getId());
                        if ("partyMoney".equalsIgnoreCase(payment.getPaymentTable())) {
                            dbclient.deleteMoneyPaidRecd(payment.getPaymentId());
                        }
                        else if ("expenditures".equalsIgnoreCase(payment.getPaymentTable())) {
                            dbclient.deleteExpenditureEntry(payment.getPaymentId(), true);
                        }
                        dbclient.deleteAccountEntry(line.getId());
                        dbclient.deleteAccountPayment(line.getId());
                        applyedLines.remove(line.getId());
                    }
                    entryLines.remove(line);
                    sumLeftLabel.setText(String.valueOf(origAmount - getTotalLinesAmt()));
                    // refresh the table
                    table.setItems(null);
                    table.layout();
                    table.setItems(FXCollections.observableArrayList(entryLines));
                });
                return cell;
            }
        });
        deleteCol.setPrefWidth(55);

        /*TableColumn<AccountEntryLine, Integer> addCol = new TableColumn<>();
        addCol.setCellFactory(
                new Callback<TableColumn<AccountEntryLine, Integer>, TableCell<AccountEntryLine, Integer>>() {
                    
            @Override
            public TableCell<AccountEntryLine, Integer> call(TableColumn<AccountEntryLine, Integer> param) {
                
                final TableButtonCell<AccountEntryLine, Integer> cell = new TableButtonCell<AccountEntryLine, Integer>("ADD") {
                    
                    @Override
                    public void updateItem(Integer item, boolean empty) {
                        super.updateItem(item, empty);
                        AccountEntryLine line = (AccountEntryLine) getTableRow().getItem();
                        if (line == null) {
                            return;
                        }
                        Button button = getButton();
                        if (line.getId() != null && applyedLines.contains(line.getId())) {
                            button.setOnMouseClicked(new UndoButtonEvent(this));
                            button.setText("UNDO");
                            return;
                        }
                        button.setOnMouseClicked(new AddButtonEvent(this));
                        button.setText("ADD");
                    }
                };
                Button button = cell.getButton();
                button.setOnMouseClicked(new AddButtonEvent(cell));
                button.setText("ADD");
                return cell;
            }
        });
        addCol.setPrefWidth(80);*/
        actionCol.getColumns().addAll(deleteCol);
        actionCol.setPrefWidth(80);

        TableColumn payeeCol = new TableColumn("Payee");

        TableColumn<AccountEntryLine, String> payeeNameCol = new TableColumn();
        payeeNameCol.setCellValueFactory(new PropertyValueFactory<>("payee"));
        payeeNameCol.setCellFactory(
                new Callback<TableColumn<AccountEntryLine, String>, TableCell<AccountEntryLine, String>>() {

            @Override
            public TableCell<AccountEntryLine, String> call(final TableColumn<AccountEntryLine, String> param) {
                AutoCompleteTextField field = new AutoCompleteTextField();
                field.setEditable(false);
                return new AutoCompleteTableCell<AccountEntryLine, String>(field) {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        sumLeftLabel.setText(String.valueOf(origAmount - getTotalLinesAmt()));
                    }
                };
            }
        });
        payeeNameCol.setPrefWidth(120);

        payeeNameCol.setEditable(false);

        TableColumn searchPayeeCol = new TableColumn();
        searchPayeeCol.setCellValueFactory(new PropertyValueFactory<>("payee"));
        searchPayeeCol.setCellFactory(
                new javafx.util.Callback<TableColumn<FreshEntryTableData.BuyerEntryTableLine, String>, TableCell<FreshEntryTableData.BuyerEntryTableLine, String>>() {
            @Override
            public TableCell<FreshEntryTableData.BuyerEntryTableLine, String> call(
                    TableColumn<FreshEntryTableData.BuyerEntryTableLine, String> param) {
                PartySearchTableCell cell = new PartySearchTableCell(PartyType.BUYER_SUPPLIERS) {
                    @Override
                    public void updateItem(Object item, boolean empty) {
                        super.updateItem(item, empty);
                        sumLeftLabel.setText(String.valueOf(origAmount - getTotalLinesAmt()));
                    }
                };
                cell.setId("partyCell");
                return cell;
            }
            
            
        });
        searchPayeeCol.setStyle("-fx-pref-height: 0;");
        searchPayeeCol.setEditable(true);

        payeeCol.getColumns().addAll(payeeNameCol, searchPayeeCol);

        table.getColumns().addAll(payeeCol, expenseCol, amountCol, actionCol);

        addLine.setLayoutY(tableY + table.getPrefHeight() + 10);
        addLine.setLayoutX(10.0);
        addLine.setText("Add Line");
        addLine.setOnAction((ActionEvent event) -> {
            Double sumLeft = origAmount - getTotalLinesAmt();
            if (sumLeft <= 0) {
                return;
            }
            for (AccountEntryLine line : entryLines) {
                if (isLineEmpty(line)) {
                    return;
                }
            }
            AccountEntryLine newLine = new AccountEntryLine(origLine.getAccountName(), origLine.getTransIdCol(),
                    origLine.getDateCol(), origLine.getChqnoCol(), origLine.getDescriptionCol(), 0d, 0d,
                    origLine.getBalanceCol(), origLine.getStatus(), origLine.getPayee(), origLine.getExpense(),
                    origLine.getComment());
            
            resetLineAmount(newLine, sumLeft);
            entryLines.add(newLine);
            table.getItems().add(newLine);
            table.refresh();
            sumLeftLabel.setText(String.valueOf(origAmount - getTotalLinesAmt()));
        });
        split.setText("Save Split Entries");
        split.setOnMouseClicked((MouseEvent event) -> {
            if (table.getItems().isEmpty()) {
                return;
            }
            for (Object line : table.getItems()) {
                AccountEntryLine entry = (AccountEntryLine) line;
                if (isLineEmpty(entry)) {
                    return;
                }
                String expense = entry.getExpense();
                if (expense == null || expense.isEmpty() || !expTypes.contains(expense)) {
                    return;
                }
                if (entry.getExpense().length() == 0) {
                    GeneralMethods.errorMsg("can't add : expense category not specified");
                    return;
                }
            }
            for (Object line : table.getItems()) {
                AccountEntryLine entry = (AccountEntryLine) line;
                updateDatabase(entry);
                Stage stage = DBankAccountsController.buildWindowForExpense(entry,
                        EntityType.getEntityTypeForValue(entry.getPayeeType()), null);
                Boolean closedManually = (Boolean) stage.getProperties().get(STAGE_CLOSED_MANUALLY);

                if (closedManually) {
//                    return;
                    continue;
                }
                Integer generatedKey = DBankAccountsController.getGeneratedKey();
                if (generatedKey != null) {
                    applyedLines.add(generatedKey);
//                    entry.setPaymentId(generatedKey);
                }
//                updateDatabase(entry);
            }
            split.getScene().getWindow().hide();
        });
        splitMainPane.getChildren().addAll(table, addLine);
    } // END OF INITIALIZE
    
    /*private class AddButtonEvent implements EventHandler<MouseEvent> {

        private final TableButtonCell<AccountEntryLine, Integer> cell;
        
        AddButtonEvent(TableButtonCell<AccountEntryLine, Integer> cell) {
            this.cell = cell;
        }
        
        @Override
        public void handle(MouseEvent event) {
            AccountEntryLine line = (AccountEntryLine) cell.getTableRow().getItem();
            if (line ==null) {
                return; // ignore empty cells
            }
            final Button button = (Button) event.getSource();
            
            String expense = line.getExpense();
            if (expense == null || expense.isEmpty() || !expTypes.contains(expense)) {
                return;
            }
            if (line.getExpense().length() == 0) {
                GeneralMethods.errorMsg("can't add : expense category not specified");
                return;
            }
            // update the status of the relevant account
            // entry line INSIDE the array of lines for this table
            Stage stage = DBankAccountsController.buildWindowForExpense(line,
                    EntityType.getEntityTypeForValue(line.getPayeeType()), null);
            Boolean closedManually = (Boolean) stage.getProperties().get(STAGE_CLOSED_MANUALLY);

            if (!closedManually) {
                updateDatabase(line);
                Integer generatedKey = DBankAccountsController.getGeneratedKey();
                if (generatedKey != null) {
                    applyedLines.add(generatedKey);
                    line.setId(generatedKey);
                    button.setText("UNDO");
                    button.setOnMouseClicked(new UndoButtonEvent(cell));
                }
            }
        }
    }
    
    private class UndoButtonEvent implements EventHandler<MouseEvent> {
        
        private final TableButtonCell<AccountEntryLine, Integer> cell;
        
        UndoButtonEvent(TableButtonCell<AccountEntryLine, Integer> cell) {
            this.cell = cell;
        }
        
        @Override
        public void handle(MouseEvent event) {
            try {
                AccountEntryLine line = (AccountEntryLine) cell.getTableRow().getItem();
                if (line ==null) {
                    return; // ignore empty cells
                }
                Button button = (Button) event.getSource();
                Alert alert = new Alert(AlertType.CONFIRMATION);
                alert.setTitle("Confirmation Dialog");
                alert.setHeaderText("Account Splitted Entry Undo");
                alert.setContentText("Are you ok to remove Splitted Entry?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    dbclient.deleteAccountPayment(cell.getItem());
                    applyedLines.remove(line.getId());
                    line.setId(null);
                    button.setText("ADD");
                    button.setOnMouseClicked(new AddButtonEvent(cell));
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }*/

    private boolean isLineEmpty(AccountEntryLine line) {
        boolean result = false;
        if (Utils.isEmptyString(line.getPayee()) || Utils.isEmptyString(line.getExpense())
                || ((line.getDepositCol() != 0 && line.getWithdrawalCol() != 0))) {
            result = true;
        }
        return result;
    }

    private double getTotalLinesAmt() {
        double tmpVal = 0;
        for (AccountEntryLine tl : entryLines) {
            if (tl.getPayeeType() == null || tl.getPayee() == null) {
                continue;
            }
            if (transactionType == WITHDRAWAL) {
                tmpVal += tl.getWithdrawalCol();
            } else if (transactionType == DEPOSIT) {
                tmpVal += tl.getDepositCol();
            }
        }
        return tmpVal;
    }

    private void resetLineAmount(AccountEntryLine line, double sum) {
        if (transactionType == WITHDRAWAL) {
            line.setWithdrawalCol(sum);
        } else if (transactionType == DEPOSIT) {
            line.setDepositCol(sum);
        }
    }

    private double getLineAmount(AccountEntryLine line) {
        double sum = 0d;

        if (transactionType == WITHDRAWAL) {
            sum = line.getWithdrawalCol();
        } else if (transactionType == DEPOSIT) {
            sum = line.getDepositCol();
        }
        return sum;
    }

    private void updateDatabase(AccountEntryLine lineToSave) {
        lineToSave.setParentId(origLine.getId());
        Double remainder = 0.0;
        if (transactionType == WITHDRAWAL) {
            remainder = origLine.getWithdrawalCol() - getLineAmount(lineToSave);
            origLine.setWithdrawalCol(remainder);
            dbclient.updateTableEntry("accountEntries", origLine.getId(), "withdrawalCol", sumLeftLabel.getText(), "");
        }
        else if (transactionType == DEPOSIT) {
            remainder = origLine.getDepositCol() - getLineAmount(lineToSave);
            origLine.setDepositCol(remainder);
            dbclient.updateTableEntry("accountEntries", origLine.getId(), "depositCol", sumLeftLabel.getText(), "");
        }
        lineToSave.setStatus(AccountEntryLine.INSOFTWARE);
        dbclient.saveAccountEntryLine(lineToSave);
        if (remainder <= 0.0) {
            dbclient.setAccountEntryStatus(origLine.getId(), -1, "");
        }
    }

}
