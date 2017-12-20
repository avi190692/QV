package com.quickveggies.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

import com.quickveggies.GeneralMethods;
import com.quickveggies.dao.DatabaseClient;
import com.quickveggies.entities.Account;
import com.quickveggies.misc.AccountBox;

public class AddAccountController implements Initializable {

    @FXML
    private TextField bankName;

    @FXML
    private TextField acNo;

    @FXML
    private TextField balance;

    @FXML
    private TextField accName;

    @FXML
    private TextField phone;

    @FXML
    private ChoiceBox<String> type;

    @FXML
    private TextArea description;

    @FXML
    private Button save;

    private Account oldAccount = null;

    public AddAccountController(Account accountToEdit) {
        if (accountToEdit != null) {//this controller was called to edit an existing account
            this.oldAccount = accountToEdit;
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (oldAccount != null) {
            accName.setText(oldAccount.getAccountName());
            bankName.setText(oldAccount.getBankName());
            acNo.setText(oldAccount.getAccountNumber() + "");
            balance.setText(oldAccount.getBalance() + "");
            phone.setText(oldAccount.getPhone());
            type.setValue(oldAccount.getAccountType() + "");
            description.setText(oldAccount.getDescription());
        }

        type.setItems(FXCollections.observableArrayList("Bank Account"));

        save.setOnAction((ActionEvent event) -> {
            //check if name is already taken
            if (accName.getText() == null || accName.getText().trim().isEmpty()) {
                GeneralMethods.errorMsg("Accout name cannot be empty");
                return;
            }
            if (acNo.getText() == null || acNo.getText().trim().isEmpty()) {
                GeneralMethods.errorMsg("Accout number cannot be empty");
                return;
            }
            if (bankName.getText() == null || bankName.getText().trim().isEmpty()) {
                GeneralMethods.errorMsg("Bank name cannot be empty");
                return;
            }
            
            try {
                DatabaseClient.getInstance().getAccountByName(accName.getText());
            } catch (SQLException e) {
                GeneralMethods.errorMsg("Account name already taken!");
                return;
            }
            
            int accountType = 0;
            if (type.getValue() != null) {
                String typeName = type.getValue();
                switch (typeName) {
                    case "Bank Account":
                        accountType = AccountBox.BANK_ACCOUNT;
                        break;
                    default:
                        accountType = AccountBox.BANK_ACCOUNT;
                        break;
                }
            }
            if (balance.getText() == null || balance.getText().trim().isEmpty()) {
                balance.setText("0");
            }
            if (phone.getText() == null) {
                phone.setText("");
            }
            if (description.getText() == null) {
                description.setText("");
            }
            Account account = new Account(0, acNo.getText(), accountType,
                    Double.parseDouble(balance.getText()), Double.parseDouble(balance.getText()), accName.getText(), bankName.getText(), phone.getText(), description.getText(),
                    (int) (System.currentTimeMillis() / (1000 * 3600 * 24)));
            try {
                DatabaseClient dbclient = DatabaseClient.getInstance();
                if (oldAccount == null) {
                    dbclient.saveAccount(account);
                }
                else {
                    dbclient.updateTableEntry("accounts", oldAccount.getId(),
                            new String[]{"acc_name", "acc_type", "acc_number", "bank_name", "balance", "initBalance", "phone", "description", "lastupdated"},
                            new String[]{accName.getText(), accountType + "", acNo.getText(), bankName.getText(), balance.getText(), balance.getText(), phone.getText(), description.getText(),
                                (int) (System.currentTimeMillis() / (1000 * 3600 * 24)) + ""},
                            false);
                }
                save.getScene().getWindow().hide();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}
