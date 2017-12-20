package com.quickveggies.misc;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

import com.quickveggies.dao.DatabaseClient;
import com.quickveggies.entities.Account;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;

public class AccountBox implements Initializable {

    @FXML
    private Pane mainPane;
    @FXML
    private Pane subpane;

    @FXML
    private Label accountNameLabel;
    @FXML
    private Label balanceSum;
    @FXML
    private Label rsSoftware;
    @FXML
    private Button editAccountBox;
    @FXML
    private Button deleteAccountBox;

    @FXML
    private Label updatedDays;

    public static final int BANK_ACCOUNT = 0;

    private final double width = 200.0, height = 99.0;
    private double layoutX = 0.0;

    @FXML
    private CheckBox selected;

    private String accountName = null;
    private double balance;
    private double inSoftware;

    private DatabaseClient dbclient = DatabaseClient.getInstance();

    public AccountBox(double layoutX) {
        this.layoutX = layoutX;
    }

    public void initialize(URL location, ResourceBundle resources) {
        mainPane.setPrefWidth(width);
        mainPane.setPrefHeight(height);
        mainPane.setLayoutX(layoutX);
        subpane.setPrefWidth(width);

        //adjust and configure the action buttons:
        editAccountBox.setPrefSize(20, 20);
        BackgroundImage editBackgroundImage = new BackgroundImage(new Image(getClass().getResource("/icons/edit.png").toExternalForm(), 20, 20, true, true), BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
        Background editBackground = new Background(editBackgroundImage);
        editAccountBox.setBackground(editBackground);

        deleteAccountBox.setPrefSize(20, 20);
        BackgroundImage deleteBackgroundImage = new BackgroundImage(new Image(getClass().getResource("/icons/delete.png").toExternalForm(), 20, 20, true, true), BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
        Background deleteBackground = new Background(deleteBackgroundImage);
        deleteAccountBox.setBackground(deleteBackground);
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public void linkBankAccount(Account account) {
        this.balance = account.getBalance();
        this.accountName = account.getAccountName();
        accountNameLabel.setText(accountName);
        balanceSum.setText("Rs. " + balance);
        setUpdatedDays(((int) (System.currentTimeMillis() / (1000 * 3600 * 24))) - account.getLastupdated());
    }

    public void setBalance(double balance) {
        balanceSum.setText("Rs. " + balance);
    }
    
    public void setSoftwareBalance(double balance) {
        rsSoftware.setText("Rs. " + balance);
    }

    public String getAccountName() {
        return accountNameLabel.getText();
    }

    public CheckBox getSelectedCheckBox() {
        return selected;
    }

    public Button getDeleteButton() {
        return deleteAccountBox;
    }

    public Button getEditButton() {
        return editAccountBox;
    }

    public void setUpdatedDays(int value) {
        if (value < 999) {
            updatedDays.setText("updated " + value + " days ago");
        } else {
            updatedDays.setText("updated >999 days ago");
        }
    }

}
